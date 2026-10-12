package ru.hdlabs.electrosmeta.feature.template

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.hdlabs.electrosmeta.data.local.entities.CategoryEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateLineEntity
import ru.hdlabs.electrosmeta.data.local.entities.ServiceEntity
import ru.hdlabs.electrosmeta.data.repository.PriceRepository
import ru.hdlabs.electrosmeta.data.repository.TemplateRepository

data class TemplateEditorUiState(
    val template: EstimateTemplateEntity? = null,
    val categoryOrder: List<Long> = emptyList(),
    val lines: List<EstimateTemplateLineEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val servicesByCategory: Map<Long, List<ServiceEntity>> = emptyMap(),
    val favorites: List<ServiceEntity> = emptyList(),
    val loading: Boolean = true,
)

class TemplateEditorViewModel(
    private val templateId: Long,
    private val templateRepository: TemplateRepository,
    private val priceRepository: PriceRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TemplateEditorUiState())
    val state: StateFlow<TemplateEditorUiState> = _state.asStateFlow()

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
            val content = templateRepository.getWithContent(templateId)
            if (content != null) {
                val order = content.categories.sortedBy { it.sortOrder }.map { it.categoryKey }
                _state.value = _state.value.copy(
                    template = content.template,
                    categoryOrder = order,
                    lines = content.lines,
                    loading = false,
                )
            } else {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }

    fun rename(name: String) {
        viewModelScope.launch {
            templateRepository.renameTemplate(templateId, name)
            reload()
        }
    }

    fun addServices(services: List<ServiceEntity>, categoryKey: Long) {
        if (services.isEmpty()) return
        viewModelScope.launch {
            templateRepository.addServices(templateId, categoryKey, services)
            reload()
        }
    }

    fun updateLine(line: EstimateTemplateLineEntity) {
        viewModelScope.launch {
            templateRepository.updateLine(line)
            reload()
        }
    }

    fun deleteLine(lineId: Long) {
        viewModelScope.launch {
            templateRepository.deleteLine(lineId)
            reload()
        }
    }

    fun reorderCategories(orderedKeys: List<Long>) {
        viewModelScope.launch {
            templateRepository.reorderCategories(templateId, orderedKeys)
            reload()
        }
    }

    class Factory(
        private val templateId: Long,
        private val templateRepository: TemplateRepository,
        private val priceRepository: PriceRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TemplateEditorViewModel(templateId, templateRepository, priceRepository) as T
        }
    }
}
