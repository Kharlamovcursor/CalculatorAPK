package ru.hdlabs.electrosmeta.feature.estimate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.hdlabs.electrosmeta.data.local.dao.EstimateWithLines
import ru.hdlabs.electrosmeta.data.local.entities.CategoryEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateLineEntity
import ru.hdlabs.electrosmeta.data.local.entities.ServiceEntity
import ru.hdlabs.electrosmeta.data.repository.EstimateRepository
import ru.hdlabs.electrosmeta.data.repository.PriceRepository
import ru.hdlabs.electrosmeta.domain.CalculationInput
import ru.hdlabs.electrosmeta.domain.CalculationResult
import ru.hdlabs.electrosmeta.domain.EstimateCalculator

data class EstimateEditorUiState(
    val estimate: EstimateEntity? = null,
    val lines: List<EstimateLineEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val servicesByCategory: Map<Long, List<ServiceEntity>> = emptyMap(),
    val searchQuery: String = "",
    val searchResults: List<ServiceEntity> = emptyList(),
    val favorites: List<ServiceEntity> = emptyList(),
    val calculation: CalculationResult? = null,
    val loading: Boolean = true,
)

class EstimateEditorViewModel(
    private val estimateId: Long,
    private val estimateRepository: EstimateRepository,
    private val priceRepository: PriceRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(EstimateEditorUiState())
    val state: StateFlow<EstimateEditorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { reload() }
        viewModelScope.launch {
            combine(
                priceRepository.observeCategories(),
                priceRepository.observeAllServices(),
                priceRepository.observeFavorites(),
            ) { categories, services, favorites ->
                _state.value = _state.value.copy(
                    categories = categories,
                    servicesByCategory = services.groupBy { it.categoryId },
                    favorites = favorites,
                )
            }.collect { }
        }
    }

    fun reload() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val withLines = estimateRepository.getWithLines(estimateId)
            if (withLines != null) {
                val subtotal = withLines.lines.sumOf {
                    EstimateCalculator.lineTotalKopecks(it.quantity, it.unitPriceKopecks)
                }
                val calc = EstimateCalculator.calculate(
                    CalculationInput(
                        linesSubtotalKopecks = subtotal,
                        discountPercent = withLines.estimate.discountPercent,
                        discountAmountKopecks = withLines.estimate.discountAmountKopecks,
                        extraFeeKopecks = withLines.estimate.extraFeeKopecks,
                        complexityMultiplier = withLines.estimate.complexityMultiplier,
                        taxPercent = withLines.estimate.taxPercent,
                        roundToWholeRubles = withLines.estimate.roundToWholeRubles,
                    ),
                )
                _state.value = _state.value.copy(
                    estimate = withLines.estimate,
                    lines = withLines.lines,
                    calculation = calc,
                    loading = false,
                )
            } else {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }

    fun search(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        if (query.length < 2) {
            _state.value = _state.value.copy(searchResults = emptyList())
            return
        }
        viewModelScope.launch {
            val results = priceRepository.searchServices(query)
            _state.value = _state.value.copy(searchResults = results)
        }
    }

    fun addService(service: ServiceEntity) {
        viewModelScope.launch {
            val sort = _state.value.lines.size
            estimateRepository.addLineFromService(estimateId, service, sort)
            reload()
        }
    }

    fun addServices(services: List<ServiceEntity>) {
        if (services.isEmpty()) return
        viewModelScope.launch {
            var sort = _state.value.lines.size
            services.forEach { service ->
                estimateRepository.addLineFromService(estimateId, service, sort)
                sort++
            }
            reload()
        }
    }

    fun updateLine(line: EstimateLineEntity) {
        viewModelScope.launch {
            estimateRepository.updateLine(line)
            reload()
        }
    }

    fun removeLine(lineId: Long) {
        viewModelScope.launch {
            estimateRepository.deleteLine(lineId, estimateId)
            reload()
        }
    }

    fun addQuickLine(name: String, unit: String, priceRub: String, qty: String) {
        val price = priceRub.replace(',', '.').toDoubleOrNull()?.times(100)?.toLong() ?: return
        val quantity = qty.replace(',', '.').toDoubleOrNull() ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            estimateRepository.addCustomLine(
                estimateId, name, unit.ifBlank { "шт." }, price, quantity, _state.value.lines.size,
            )
            reload()
        }
    }

    fun saveEstimateFields(
        title: String,
        clientName: String,
        clientAddress: String,
        workTitle: String,
        discountPercent: Double,
        discountAmountKopecks: Long,
        extraFeeKopecks: Long,
        complexity: Double,
        taxPercent: Double,
        roundRubles: Boolean,
        comment: String,
    ) {
        val current = _state.value.estimate ?: return
        viewModelScope.launch {
            estimateRepository.saveEstimate(
                current.copy(
                    title = title,
                    clientName = clientName.ifBlank { null },
                    clientAddress = clientAddress.ifBlank { null },
                    workTitle = workTitle.ifBlank { null },
                    discountPercent = discountPercent,
                    discountAmountKopecks = discountAmountKopecks,
                    extraFeeKopecks = extraFeeKopecks,
                    complexityMultiplier = complexity,
                    taxPercent = taxPercent,
                    roundToWholeRubles = roundRubles,
                    comment = comment.ifBlank { null },
                ),
            )
            reload()
        }
    }

    fun getWithLines(): EstimateWithLines? {
        val est = _state.value.estimate ?: return null
        return EstimateWithLines(est, _state.value.lines)
    }

    class Factory(
        private val estimateId: Long,
        private val estimateRepository: EstimateRepository,
        private val priceRepository: PriceRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EstimateEditorViewModel(estimateId, estimateRepository, priceRepository) as T
        }
    }
}
