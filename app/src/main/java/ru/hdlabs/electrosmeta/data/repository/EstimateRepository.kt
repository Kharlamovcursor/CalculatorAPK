package ru.hdlabs.electrosmeta.data.repository

import kotlinx.coroutines.flow.Flow
import ru.hdlabs.electrosmeta.data.local.EstimateStatus
import ru.hdlabs.electrosmeta.data.local.EstimateType
import ru.hdlabs.electrosmeta.data.local.dao.EstimateDao
import ru.hdlabs.electrosmeta.data.local.dao.EstimateWithLines
import ru.hdlabs.electrosmeta.data.local.entities.EstimateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateLineEntity
import ru.hdlabs.electrosmeta.data.local.entities.ServiceEntity
import ru.hdlabs.electrosmeta.domain.EstimateCalculator

class EstimateRepository(
    private val estimateDao: EstimateDao,
) {
    fun observeEstimates(): Flow<List<EstimateEntity>> = estimateDao.observeAll()

    fun searchEstimates(query: String): Flow<List<EstimateEntity>> = estimateDao.search(query)

    suspend fun getWithLines(id: Long): EstimateWithLines? = estimateDao.getWithLines(id)

    suspend fun createDraft(type: EstimateType, taxPercent: Double): Long {
        val now = System.currentTimeMillis()
        val title = when (type) {
            EstimateType.MAIN -> "Основной расчёт"
            EstimateType.QUICK -> "Быстрый расчёт"
        }
        return estimateDao.insertEstimate(
            EstimateEntity(
                title = title,
                type = type,
                status = EstimateStatus.DRAFT,
                clientName = null,
                clientAddress = null,
                workTitle = null,
                discountPercent = 0.0,
                discountAmountKopecks = 0,
                extraFeeKopecks = 0,
                complexityMultiplier = 1.0,
                taxPercent = taxPercent,
                roundToWholeRubles = true,
                comment = null,
                createdAtMillis = now,
                updatedAtMillis = now,
            ),
        )
    }

    suspend fun saveEstimate(estimate: EstimateEntity) {
        estimateDao.updateEstimate(estimate.copy(updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun addLineFromService(estimateId: Long, service: ServiceEntity, sortOrder: Int) {
        estimateDao.insertLine(
            EstimateLineEntity(
                estimateId = estimateId,
                catalogServiceId = service.id,
                name = service.name,
                unit = service.unit,
                unitPriceKopecks = service.priceKopecks,
                quantity = 1.0,
                note = null,
                sortOrder = sortOrder,
            ),
        )
        touchEstimate(estimateId)
    }

    suspend fun addCustomLine(
        estimateId: Long,
        name: String,
        unit: String,
        priceKopecks: Long,
        quantity: Double,
        sortOrder: Int,
    ) {
        estimateDao.insertLine(
            EstimateLineEntity(
                estimateId = estimateId,
                catalogServiceId = null,
                name = name,
                unit = unit,
                unitPriceKopecks = priceKopecks,
                quantity = quantity,
                note = null,
                sortOrder = sortOrder,
            ),
        )
        touchEstimate(estimateId)
    }

    suspend fun updateLine(line: EstimateLineEntity) {
        estimateDao.updateLine(line)
        touchEstimate(line.estimateId)
    }

    suspend fun deleteLine(lineId: Long, estimateId: Long) {
        estimateDao.deleteLine(lineId)
        touchEstimate(estimateId)
    }

    suspend fun deleteEstimate(id: Long) = estimateDao.deleteEstimate(id)

    suspend fun clearAllEstimates() {
        estimateDao.getAllOnce().forEach { estimateDao.deleteEstimate(it.id) }
    }

    suspend fun duplicateEstimate(id: Long): Long? {
        val original = estimateDao.getWithLines(id) ?: return null
        val now = System.currentTimeMillis()
        val newId = estimateDao.insertEstimate(
            original.estimate.copy(
                id = 0,
                title = original.estimate.title + " (копия)",
                status = EstimateStatus.DRAFT,
                createdAtMillis = now,
                updatedAtMillis = now,
            ),
        )
        original.lines.forEachIndexed { index, line ->
            estimateDao.insertLine(line.copy(id = 0, estimateId = newId, sortOrder = index))
        }
        return newId
    }

    suspend fun linesSubtotalKopecks(estimateId: Long): Long {
        val lines = estimateDao.getLines(estimateId)
        return lines.sumOf { EstimateCalculator.lineTotalKopecks(it.quantity, it.unitPriceKopecks) }
    }

    suspend fun getAllForBackup(): Pair<List<EstimateEntity>, List<EstimateLineEntity>> {
        return estimateDao.getAllOnce() to estimateDao.getAllLinesOnce()
    }

    suspend fun replaceAllFromBackup(
        estimates: List<EstimateEntity>,
        lines: List<EstimateLineEntity>,
    ) {
        estimateDao.getAllOnce().forEach { estimateDao.deleteEstimate(it.id) }
        estimates.forEach { estimateDao.insertEstimate(it) }
        lines.forEach { estimateDao.insertLine(it) }
    }

    private suspend fun touchEstimate(estimateId: Long) {
        val estimate = estimateDao.getById(estimateId) ?: return
        estimateDao.updateEstimate(estimate.copy(updatedAtMillis = System.currentTimeMillis()))
    }
}
