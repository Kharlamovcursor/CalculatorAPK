package ru.hdlabs.electrosmeta.backup

import kotlinx.serialization.Serializable
import ru.hdlabs.electrosmeta.data.local.EstimateStatus
import ru.hdlabs.electrosmeta.data.local.EstimateType
import ru.hdlabs.electrosmeta.data.settings.AppSettings
import ru.hdlabs.electrosmeta.update.ReleaseUpdateConfig

@Serializable
data class BackupFileV1(
    val version: Int = 1,
    val exportedAtMillis: Long,
    val settings: BackupSettings,
    val categories: List<BackupCategory>,
    val services: List<BackupService>,
    val estimates: List<BackupEstimate>,
    val lines: List<BackupLine>,
)

@Serializable
data class BackupSettings(
    val animationSpeedPercent: Int,
    val animationEnabled: Boolean,
    val powerSaveMode: Boolean,
    val companyName: String,
    val performerTitle: String,
    val inn: String,
    val ogrnip: String,
    val phone: String,
    val telegram: String,
    val maxContact: String,
    val pdfTitle: String,
    val pdfIntro: String,
    val pdfFooter: String,
    val offerValidityDays: Int,
    val defaultTaxPercent: Double,
    val taxLabel: String,
    val githubOwner: String,
    val githubRepo: String,
    val showComplexityInPdf: Boolean,
)

@Serializable
data class BackupCategory(
    val id: Long,
    val name: String,
    val sortOrder: Int,
    val archived: Boolean,
)

@Serializable
data class BackupService(
    val id: Long,
    val categoryId: Long,
    val name: String,
    val unit: String,
    val priceKopecks: Long,
    val sortOrder: Int,
    val archived: Boolean,
    val isFavorite: Boolean,
)

@Serializable
data class BackupEstimate(
    val id: Long,
    val title: String,
    val type: String,
    val status: String,
    val clientName: String?,
    val clientAddress: String?,
    val workTitle: String? = null,
    val discountPercent: Double,
    val discountAmountKopecks: Long,
    val extraFeeKopecks: Long,
    val complexityMultiplier: Double,
    val taxPercent: Double,
    val roundToWholeRubles: Boolean,
    val comment: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

@Serializable
data class BackupLine(
    val id: Long,
    val estimateId: Long,
    val catalogServiceId: Long?,
    val name: String,
    val unit: String,
    val unitPriceKopecks: Long,
    val quantity: Double,
    val note: String?,
    val sortOrder: Int,
)

fun AppSettings.toBackup() = BackupSettings(
    animationSpeedPercent = animationSpeedPercent,
    animationEnabled = animationEnabled,
    powerSaveMode = powerSaveMode,
    companyName = companyName,
    performerTitle = performerTitle,
    inn = inn,
    ogrnip = ogrnip,
    phone = phone,
    telegram = telegram,
    maxContact = maxContact,
    pdfTitle = pdfTitle,
    pdfIntro = pdfIntro,
    pdfFooter = pdfFooter,
    offerValidityDays = offerValidityDays,
    defaultTaxPercent = defaultTaxPercent,
    taxLabel = taxLabel,
    githubOwner = ReleaseUpdateConfig.githubOwner,
    githubRepo = ReleaseUpdateConfig.githubRepo,
    showComplexityInPdf = showComplexityInPdf,
)

fun BackupSettings.toAppSettings() = AppSettings(
    animationSpeedPercent = animationSpeedPercent,
    animationEnabled = animationEnabled,
    powerSaveMode = powerSaveMode,
    companyName = companyName,
    performerTitle = performerTitle,
    inn = inn,
    ogrnip = ogrnip,
    phone = phone,
    telegram = telegram,
    maxContact = maxContact,
    pdfTitle = pdfTitle,
    pdfIntro = pdfIntro,
    pdfFooter = pdfFooter,
    offerValidityDays = offerValidityDays,
    defaultTaxPercent = defaultTaxPercent,
    taxLabel = taxLabel,
    showComplexityInPdf = showComplexityInPdf,
)

fun String.toEstimateType(): EstimateType = EstimateType.valueOf(this)
fun String.toEstimateStatus(): EstimateStatus = EstimateStatus.valueOf(this)
