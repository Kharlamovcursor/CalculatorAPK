package ru.hdlabs.electrosmeta.backup

import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.hdlabs.electrosmeta.data.local.dao.CategoryDao
import ru.hdlabs.electrosmeta.data.local.dao.EstimateDao
import ru.hdlabs.electrosmeta.data.local.dao.ServiceDao
import ru.hdlabs.electrosmeta.data.local.entities.CategoryEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateLineEntity
import ru.hdlabs.electrosmeta.data.local.entities.ServiceEntity
import ru.hdlabs.electrosmeta.data.settings.SettingsRepository
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class BackupManager(
    private val categoryDao: CategoryDao,
    private val serviceDao: ServiceDao,
    private val estimateDao: EstimateDao,
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
                    it.id, it.estimateId, it.catalogServiceId, it.name, it.unit,
                    it.unitPriceKopecks, it.quantity, it.note, it.sortOrder,
                )
            },
        )
        return json.encodeToString(backup)
    }

    suspend fun exportEncrypted(password: CharArray): ByteArray {
        val plain = buildPlainJson().toByteArray(Charsets.UTF_8)
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(plain)
        return salt + iv + encrypted
    }

    suspend fun previewEncrypted(data: ByteArray, password: CharArray): BackupSummary {
        val backup = decryptBackup(data, password)
        return BackupSummary(
            categories = backup.categories.size,
            services = backup.services.size,
            estimates = backup.estimates.size,
        )
    }

    suspend fun importEncrypted(data: ByteArray, password: CharArray): BackupSummary {
        val backup = decryptBackup(data, password)
        applyBackup(backup)
        return BackupSummary(
            categories = backup.categories.size,
            services = backup.services.size,
            estimates = backup.estimates.size,
        )
    }

    private fun decryptBackup(data: ByteArray, password: CharArray): BackupFileV1 {
        require(data.size > 28) { "Неверный файл резервной копии" }
        val salt = data.copyOfRange(0, 16)
        val iv = data.copyOfRange(16, 28)
        val payload = data.copyOfRange(28, data.size)
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        val plain = cipher.doFinal(payload)
        val backup = json.decodeFromString<BackupFileV1>(plain.decodeToString())
        require(backup.version == 1) { "Неподдерживаемая версия: ${backup.version}" }
        return backup
    }

    private suspend fun applyBackup(backup: BackupFileV1) {
        settingsRepository.update { backup.settings.toAppSettings() }
        estimateDao.getAllOnce().forEach { estimateDao.deleteEstimate(it.id) }
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
                    it.id, it.estimateId, it.catalogServiceId, it.name, it.unit,
                    it.unitPriceKopecks, it.quantity, it.note, it.sortOrder,
                ),
            )
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, 120_000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val key = factory.generateSecret(spec).encoded
        return SecretKeySpec(key, "AES")
    }
}

data class BackupSummary(
    val categories: Int,
    val services: Int,
    val estimates: Int,
)
