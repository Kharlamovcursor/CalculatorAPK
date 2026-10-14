package ru.hdlabs.electrosmeta.backup

import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.hdlabs.electrosmeta.data.local.dao.CategoryDao
import ru.hdlabs.electrosmeta.data.local.dao.EstimateDao
import ru.hdlabs.electrosmeta.data.local.dao.ServiceDao
import ru.hdlabs.electrosmeta.data.local.dao.TemplateDao
import ru.hdlabs.electrosmeta.data.local.entities.CategoryEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateLineEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateCategoryEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateLineEntity
import ru.hdlabs.electrosmeta.data.local.entities.ServiceEntity
import ru.hdlabs.electrosmeta.data.settings.SettingsRepository

class BackupManager(
    private val categoryDao: CategoryDao,
    private val serviceDao: ServiceDao,
    private val estimateDao: EstimateDao,
    private val templateDao: TemplateDao,
    private val settingsRepository: SettingsRepository,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun buildPlainJson(): String {
        val settings = settingsRepository.settings.first().toBackup()
        val backup = BackupFileV1(
            exportedAtMillis = System.currentTimeMillis(),
            settings = settings,
            categories = categoryDao.observeAll().first().map {
                BackupCategory(it.id, it.name, it.sortOrder, it.archived)
            },
            services = serviceDao.getAllOnce().map {
                BackupService(
                    it.id, it.categoryId, it.name, it.unit, it.priceKopecks,
                    it.sortOrder, it.archived, it.isFavorite,
                )
            },
            estimates = estimateDao.getAllOnce().map {
                BackupEstimate(
                    it.id, it.title, it.type.name, it.status.name,
                    it.clientName, it.clientAddress, it.workTitle,
                    it.discountPercent, it.discountAmountKopecks, it.extraFeeKopecks,
                    it.complexityMultiplier, it.taxPercent, it.roundToWholeRubles,
                    it.comment, it.createdAtMillis, it.updatedAtMillis,
                )
            },
            lines = estimateDao.getAllLinesOnce().map {
                BackupLine(
                    it.id, it.estimateId, it.catalogServiceId, it.categoryKey, it.name, it.unit,
                    it.unitPriceKopecks, it.quantity, it.note, it.sortOrder,
                )
            },
            templates = templateDao.getAllTemplatesOnce().map {
                BackupTemplate(it.id, it.name, it.sortOrder, it.type.name)
            },
            templateCategories = templateDao.getAllTemplateCategoriesOnce().map {
                BackupTemplateCategory(it.id, it.templateId, it.categoryKey, it.sortOrder)
            },
            templateLines = templateDao.getAllTemplateLinesOnce().map {
                BackupTemplateLine(
                    it.id, it.templateId, it.categoryKey, it.catalogServiceId,
                    it.name, it.unit, it.unitPriceKopecks, it.quantity, it.sortOrder,
                )
            },
        )
        return json.encodeToString(backup)
    }

    suspend fun exportBytes(): ByteArray = buildPlainJson().toByteArray(Charsets.UTF_8)

    suspend fun preview(data: ByteArray): BackupSummary {
        val backup = parse(data)
        return summary(backup)
    }

    suspend fun import(data: ByteArray): BackupSummary {
        val backup = parse(data)
        applyBackup(backup)
        return summary(backup)
    }

    private fun parse(data: ByteArray): BackupFileV1 {
        val text = data.toString(Charsets.UTF_8).trimStart()
        if (!text.startsWith("{")) {
            throw IllegalArgumentException(
                "Этот файл сделан со старым паролем. Создайте новую копию в этой версии приложения.",
            )
        }
        val backup = json.decodeFromString<BackupFileV1>(text)
        require(backup.version == 1) { "Неподдерживаемая версия: ${backup.version}" }
        return backup
    }

    private fun summary(backup: BackupFileV1) = BackupSummary(
        categories = backup.categories.size,
        services = backup.services.size,
        estimates = backup.estimates.size,
        templates = backup.templates.size,
    )

    private suspend fun applyBackup(backup: BackupFileV1) {
        settingsRepository.update { backup.settings.toAppSettings() }
        estimateDao.getAllOnce().forEach { estimateDao.deleteEstimate(it.id) }
        templateDao.deleteAllLines()
        templateDao.deleteAllCategories()
        templateDao.deleteAllTemplates()
        serviceDao.deleteAll()
        categoryDao.deleteAll()
        backup.categories.forEach {
            categoryDao.insert(CategoryEntity(it.id, it.name, it.sortOrder, it.archived))
        }
        backup.services.forEach {
            serviceDao.insert(
                ServiceEntity(
                    it.id, it.categoryId, it.name, it.unit, it.priceKopecks,
                    it.sortOrder, it.archived, it.isFavorite,
                ),
            )
        }
        backup.estimates.forEach {
            estimateDao.insertEstimate(
                EstimateEntity(
                    it.id, it.title, it.type.toEstimateType(), it.status.toEstimateStatus(),
                    it.clientName, it.clientAddress, it.workTitle,
                    it.discountPercent, it.discountAmountKopecks, it.extraFeeKopecks,
                    it.complexityMultiplier, it.taxPercent, it.roundToWholeRubles,
                    it.comment, it.createdAtMillis, it.updatedAtMillis,
                ),
            )
        }
        backup.lines.forEach {
            estimateDao.insertLine(
                EstimateLineEntity(
                    it.id, it.estimateId, it.catalogServiceId, it.categoryKey, it.name, it.unit,
                    it.unitPriceKopecks, it.quantity, it.note, it.sortOrder,
                ),
            )
        }
        backup.templates.forEach {
            templateDao.insertTemplate(
                EstimateTemplateEntity(it.id, it.name, it.sortOrder, it.type.toEstimateType()),
            )
        }
        backup.templateCategories.forEach {
            templateDao.insertCategory(
                EstimateTemplateCategoryEntity(it.id, it.templateId, it.categoryKey, it.sortOrder),
            )
        }
        backup.templateLines.forEach {
            templateDao.insertLine(
                EstimateTemplateLineEntity(
                    it.id, it.templateId, it.categoryKey, it.catalogServiceId,
                    it.name, it.unit, it.unitPriceKopecks, it.quantity, it.sortOrder,
                ),
            )
        }
    }
}

data class BackupSummary(
    val categories: Int,
    val services: Int,
    val estimates: Int,
    val templates: Int = 0,
)
