package ru.hdlabs.electrosmeta.di

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.hdlabs.electrosmeta.backup.BackupManager
import ru.hdlabs.electrosmeta.data.local.AppDatabase
import ru.hdlabs.electrosmeta.data.repository.EstimateRepository
import ru.hdlabs.electrosmeta.data.repository.PriceRepository
import ru.hdlabs.electrosmeta.data.repository.TemplateRepository
import ru.hdlabs.electrosmeta.data.seed.DefaultPriceSeed
import ru.hdlabs.electrosmeta.data.settings.SettingsRepository
import ru.hdlabs.electrosmeta.pdf.PdfGenerator
import ru.hdlabs.electrosmeta.update.GitHubUpdateSource
import ru.hdlabs.electrosmeta.update.UpdateSource

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database = AppDatabase.getInstance(appContext)

    val settingsRepository = SettingsRepository(appContext)
    val priceRepository = PriceRepository(database.categoryDao(), database.serviceDao())
    val estimateRepository = EstimateRepository(database.estimateDao())
    val templateRepository = TemplateRepository(database.templateDao(), estimateRepository)

    val backupManager = BackupManager(
        database.categoryDao(),
        database.serviceDao(),
        database.estimateDao(),
        database.templateDao(),
        settingsRepository,
    )

    val pdfGenerator = PdfGenerator(appContext)

    fun updateSource(owner: String, repo: String): UpdateSource =
        GitHubUpdateSource(owner, repo)

    init {
        scope.launch {
            DefaultPriceSeed.seedIfEmpty(database.categoryDao(), database.serviceDao())
            settingsRepository.pinFixedAppearance()
        }
    }
}
