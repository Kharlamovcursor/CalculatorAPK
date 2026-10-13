package ru.hdlabs.electrosmeta.data.repository

import kotlinx.coroutines.flow.Flow
import ru.hdlabs.electrosmeta.data.local.EstimateType
import ru.hdlabs.electrosmeta.data.local.dao.TemplateDao
import ru.hdlabs.electrosmeta.data.local.dao.TemplateWithContent
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateCategoryEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateLineEntity
import ru.hdlabs.electrosmeta.data.local.entities.ServiceEntity
import ru.hdlabs.electrosmeta.domain.EstimateLineCategoryKeys

class TemplateRepository(
    private val templateDao: TemplateDao,
    private val estimateRepository: EstimateRepository,
) {
    fun observeTemplates(): Flow<List<EstimateTemplateEntity>> = templateDao.observeAll()

    suspend fun getWithContent(id: Long): TemplateWithContent? = templateDao.getWithContent(id)

    suspend fun createTemplate(name: String, type: EstimateType): Long {
        val sortOrder = templateDao.nextTemplateSortOrder()
        val displayName = name.ifBlank { defaultNewTemplateName() }
        return templateDao.insertTemplate(
            EstimateTemplateEntity(
                name = displayName,
                sortOrder = sortOrder,
                type = type,
            ),
        )
    }

    private suspend fun defaultNewTemplateName(): String {
        val existing = templateDao.getAllTemplatesOnce()
        val base = "Шаблон"
        if (existing.none { it.name == base }) return base
        var n = 2
        while (existing.any { it.name == "$base $n" }) n++
        return "$base $n"
    }

    suspend fun renameTemplate(id: Long, name: String) {
        val template = templateDao.getById(id) ?: return
        templateDao.updateTemplate(template.copy(name = name))
    }

    suspend fun deleteTemplate(id: Long) = templateDao.deleteTemplateWithContent(id)

    suspend fun reorderTemplates(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            val template = templateDao.getById(id) ?: return@forEachIndexed
            if (template.sortOrder != index) {
                templateDao.updateTemplate(template.copy(sortOrder = index))
            }
        }
    }

    suspend fun ensureCategory(templateId: Long, categoryKey: Long, sortOrder: Int? = null) {
        val existing = templateDao.getCategories(templateId).find { it.categoryKey == categoryKey }
        if (existing != null) return
        val order = sortOrder ?: templateDao.getCategories(templateId).size
        templateDao.insertCategory(
            EstimateTemplateCategoryEntity(
                templateId = templateId,
                categoryKey = categoryKey,
                sortOrder = order,
            ),
        )
    }

    suspend fun reorderCategories(templateId: Long, orderedCategoryKeys: List<Long>) {
        orderedCategoryKeys.forEachIndexed { index, key ->
            val row = templateDao.getCategories(templateId).find { it.categoryKey == key } ?: return@forEachIndexed
            if (row.sortOrder != index) {
                templateDao.updateCategory(row.copy(sortOrder = index))
            }
        }
    }

    suspend fun addServices(templateId: Long, categoryKey: Long, services: List<ServiceEntity>) {
        if (services.isEmpty()) return
        ensureCategory(templateId, categoryKey)
        var sort = templateDao.getLines(templateId).maxOfOrNull { it.sortOrder }?.plus(1) ?: 0
        services.forEach { service ->
            templateDao.insertLine(
                EstimateTemplateLineEntity(
                    templateId = templateId,
                    categoryKey = categoryKey,
                    catalogServiceId = service.id,
                    name = service.name,
                    unit = service.unit,
                    unitPriceKopecks = service.priceKopecks,
                    quantity = 1.0,
                    sortOrder = sort++,
                ),
            )
        }
    }

    suspend fun updateLine(line: EstimateTemplateLineEntity) {
        templateDao.updateLine(line)
    }

    suspend fun deleteLine(lineId: Long) {
        templateDao.deleteLine(lineId)
    }

    suspend fun addCustomLine(
        templateId: Long,
        name: String,
        unit: String,
        priceKopecks: Long,
        quantity: Double,
    ) {
        val key = EstimateLineCategoryKeys.UNCATEGORIZED
        ensureCategory(templateId, key)
        val sort = templateDao.getLines(templateId).maxOfOrNull { it.sortOrder }?.plus(1) ?: 0
        templateDao.insertLine(
            EstimateTemplateLineEntity(
                templateId = templateId,
                categoryKey = key,
                catalogServiceId = null,
                name = name,
                unit = unit,
                unitPriceKopecks = priceKopecks,
                quantity = quantity,
                sortOrder = sort,
            ),
        )
    }

    suspend fun createFromEstimate(estimateId: Long, preferredName: String? = null): Long? {
        val content = estimateRepository.getWithLines(estimateId) ?: return null
        if (content.lines.isEmpty()) return null
        val name = preferredName?.trim()?.takeIf { it.isNotEmpty() } ?: content.estimate.title
        val templateId = createTemplate(name, EstimateType.MAIN)
        val categoryKeys = content.lines.map { line ->
            line.categoryKey ?: EstimateLineCategoryKeys.UNCATEGORIZED
        }.distinct()
        categoryKeys.forEachIndexed { index, key ->
            ensureCategory(templateId, key, sortOrder = index)
        }
        content.lines.sortedBy { it.sortOrder }.forEachIndexed { index, line ->
            val key = line.categoryKey ?: EstimateLineCategoryKeys.UNCATEGORIZED
            templateDao.insertLine(
                EstimateTemplateLineEntity(
                    templateId = templateId,
                    categoryKey = key,
                    catalogServiceId = line.catalogServiceId,
                    name = line.name,
                    unit = line.unit,
                    unitPriceKopecks = line.unitPriceKopecks,
                    quantity = line.quantity,
                    sortOrder = index,
                ),
            )
        }
        return templateId
    }

    suspend fun createEstimateFromTemplate(templateId: Long, taxPercent: Double): Long? {
        val content = templateDao.getWithContent(templateId) ?: return null
        val estimateId = estimateRepository.createDraft(EstimateType.MAIN, taxPercent)
        val title = content.template.name
        estimateRepository.getWithLines(estimateId)?.estimate?.let { estimate ->
            estimateRepository.saveEstimate(estimate.copy(title = title))
        }
        content.lines.sortedBy { it.sortOrder }.forEachIndexed { index, line ->
            estimateRepository.addLineSnapshot(
                estimateId = estimateId,
                catalogServiceId = line.catalogServiceId,
                name = line.name,
                unit = line.unit,
                priceKopecks = line.unitPriceKopecks,
                quantity = line.quantity,
                sortOrder = index,
                categoryKey = line.categoryKey,
            )
        }
        return estimateId
    }
}
