package ru.hdlabs.electrosmeta.navigation

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import ru.hdlabs.electrosmeta.R
import ru.hdlabs.electrosmeta.backup.BackupSummary
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hdlabs.electrosmeta.BuildConfig
import ru.hdlabs.electrosmeta.ElectrosmetaApplication
import ru.hdlabs.electrosmeta.data.local.EstimateType
import ru.hdlabs.electrosmeta.data.local.entities.CategoryEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateLineEntity
import ru.hdlabs.electrosmeta.data.local.entities.ServiceEntity
import ru.hdlabs.electrosmeta.data.settings.AppSettings
import ru.hdlabs.electrosmeta.domain.parseRublesToKopecks
import ru.hdlabs.electrosmeta.domain.formatRubles
import ru.hdlabs.electrosmeta.feature.estimate.EstimateEditorViewModel
import ru.hdlabs.electrosmeta.update.ApkDownloadHelper
import ru.hdlabs.electrosmeta.update.UpdateInfo
import ru.hdlabs.electrosmeta.ui.background.SpaceBackground
import ru.hdlabs.electrosmeta.ui.components.HdCard
import ru.hdlabs.electrosmeta.ui.components.HdPrimaryButton
import ru.hdlabs.electrosmeta.ui.components.HdSecondaryButton
import ru.hdlabs.electrosmeta.ui.components.HdTextField
import ru.hdlabs.electrosmeta.ui.theme.HdBlack
import ru.hdlabs.electrosmeta.ui.theme.HdTextPrimary
import ru.hdlabs.electrosmeta.ui.theme.HdTextSecondary
import ru.hdlabs.electrosmeta.update.InstallHelper
import java.io.File

object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val PRICE = "price"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    const val NEW_TYPE = "new_type"
    const val ESTIMATE = "estimate/{id}"
    fun estimate(id: Long) = "estimate/$id"
}

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(nav) }
        composable(Routes.NEW_TYPE) { NewEstimateTypeScreen(nav) }
        composable(Routes.HISTORY) { HistoryScreen(nav) }
        composable(Routes.PRICE) { PriceListScreen(nav) }
        composable(Routes.SETTINGS) { SettingsScreen(nav) }
        composable(Routes.ABOUT) { AboutScreen(nav) }
        composable(Routes.PRIVACY) { PrivacyScreen(nav) }
        composable(
            route = Routes.ESTIMATE,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            EstimateScreen(nav, id)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HdScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    scroll: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        SpaceBackground()
        Scaffold(
            containerColor = HdBlack.copy(alpha = 0.55f),
            topBar = {
                TopAppBar(
                    title = { Text(title, color = HdTextPrimary) },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = HdTextPrimary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = HdBlack.copy(alpha = 0.7f)),
                )
            },
        ) { padding ->
            val mod = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
            if (scroll) {
                Column(mod.verticalScroll(rememberScrollState())) { content() }
            } else {
                Column(mod) { content() }
            }
        }
    }
}

@Composable
private fun HomeScreen(nav: NavHostController) {
    HdScaffold(title = "HDLabs ЭлектроСмета", scroll = false) {
        Image(
            painter = painterResource(R.drawable.hdlabs_logo),
            contentDescription = "HDLabs",
            modifier = Modifier
                .width(220.dp)
                .padding(bottom = 12.dp),
            contentScale = ContentScale.Fit,
        )
        Text("Калькулятор электромонтажных работ", color = HdTextSecondary, modifier = Modifier.padding(bottom = 16.dp))
        HdPrimaryButton("Новый расчёт", onClick = { nav.navigate(Routes.NEW_TYPE) })
        Spacer(Modifier.height(12.dp))
        HdSecondaryButton("История", onClick = { nav.navigate(Routes.HISTORY) })
        Spacer(Modifier.height(12.dp))
        HdSecondaryButton("Прайс-лист", onClick = { nav.navigate(Routes.PRICE) })
        Spacer(Modifier.height(12.dp))
        HdSecondaryButton("Настройки", onClick = { nav.navigate(Routes.SETTINGS) })
        Spacer(Modifier.height(24.dp))
        Text("Версия ${BuildConfig.VERSION_NAME}", color = HdTextSecondary)
    }
}

@Composable
private fun NewEstimateTypeScreen(nav: NavHostController) {
    val app = LocalContext.current.applicationContext as ElectrosmetaApplication
    val scope = rememberCoroutineScope()
    HdScaffold(title = "Новый расчёт", onBack = { nav.popBackStack() }) {
        HdPrimaryButton("Основной расчёт") {
            scope.launch {
                val tax = app.container.settingsRepository.settings.first().defaultTaxPercent
                val id = app.container.estimateRepository.createDraft(EstimateType.MAIN, tax)
                nav.navigate(Routes.estimate(id)) { popUpTo(Routes.NEW_TYPE) { inclusive = true } }
            }
        }
        Spacer(Modifier.height(12.dp))
        HdSecondaryButton("Быстрый расчёт") {
            scope.launch {
                val tax = app.container.settingsRepository.settings.first().defaultTaxPercent
                val id = app.container.estimateRepository.createDraft(EstimateType.QUICK, tax)
                nav.navigate(Routes.estimate(id)) { popUpTo(Routes.NEW_TYPE) { inclusive = true } }
            }
        }
    }
}

@Composable
private fun HistoryScreen(nav: NavHostController) {
    val app = LocalContext.current.applicationContext as ElectrosmetaApplication
    val estimates by app.container.estimateRepository.observeEstimates()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var query by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<EstimateEntity?>(null) }
    var renameTarget by remember { mutableStateOf<EstimateEntity?>(null) }
    var renameTitle by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val list = if (query.isBlank()) estimates else {
        estimates.filter {
            it.title.contains(query, true) ||
                (it.clientName?.contains(query, true) == true) ||
                (it.clientAddress?.contains(query, true) == true)
        }
    }
    HdScaffold(title = "История", onBack = { nav.popBackStack() }) {
        HdTextField(query, { query = it }, "Поиск")
        Spacer(Modifier.height(8.dp))
        list.forEach { item ->
            HdCard(Modifier.padding(bottom = 8.dp)) {
                Text(item.title, color = HdTextPrimary)
                Text(item.status.name, color = HdTextSecondary)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("Открыть", color = HdTextPrimary, modifier = Modifier.clickable {
                        nav.navigate(Routes.estimate(item.id))
                    })
                    Text("  ·  ", color = HdTextSecondary)
                    Text("Копия", color = HdTextPrimary, modifier = Modifier.clickable {
                        scope.launch {
                            app.container.estimateRepository.duplicateEstimate(item.id)?.let { newId ->
                                nav.navigate(Routes.estimate(newId))
                            }
                        }
                    })
                    Text("  ·  ", color = HdTextSecondary)
                    Text("Имя", color = HdTextPrimary, modifier = Modifier.clickable {
                        renameTarget = item
                        renameTitle = item.title
                    })
                    Text("  ·  ", color = HdTextSecondary)
                    Text("Удалить", color = HdTextPrimary, modifier = Modifier.clickable {
                        deleteTarget = item
                    })
                }
            }
        }
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Удалить расчёт?") },
            text = { Text("«${target.title}» будет удалён без возможности восстановления.", color = HdTextSecondary) },
            confirmButton = {
                HdPrimaryButton("Удалить") {
                    scope.launch {
                        app.container.estimateRepository.deleteEstimate(target.id)
                        deleteTarget = null
                    }
                }
            },
            dismissButton = {
                HdSecondaryButton("Отмена") { deleteTarget = null }
            },
        )
    }

    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Переименовать") },
            text = { HdTextField(renameTitle, { renameTitle = it }, "Название") },
            confirmButton = {
                HdPrimaryButton("Сохранить") {
                    scope.launch {
                        app.container.estimateRepository.saveEstimate(
                            target.copy(title = renameTitle.trim().ifBlank { target.title }),
                        )
                        renameTarget = null
                    }
                }
            },
        )
    }
}

@Composable
private fun PriceListScreen(nav: NavHostController) {
    val app = LocalContext.current.applicationContext as ElectrosmetaApplication
    val categories by app.container.priceRepository.observeCategories()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val services by app.container.priceRepository.observeAllServices()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var editingService by remember { mutableStateOf<ServiceEntity?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    HdScaffold(title = "Прайс-лист", onBack = { nav.popBackStack() }) {
        Text("Ориентировочные цены СПб. Изменения не затрагивают старые сметы.", color = HdTextSecondary)
        Spacer(Modifier.height(8.dp))
        HdSecondaryButton("Добавить категорию") { showAddCategory = true }
        Spacer(Modifier.height(8.dp))
        HdSecondaryButton("Сбросить к стартовому прайсу") { confirmReset = true }
        categories.forEach { cat ->
            Spacer(Modifier.height(12.dp))
            Text(cat.name, color = HdTextPrimary)
            services.filter { it.categoryId == cat.id }.forEach { svc ->
                HdCard(Modifier.padding(top = 6.dp)) {
                    Text(svc.name, color = HdTextPrimary)
                    Text("${formatRubles(svc.priceKopecks)} / ${svc.unit}", color = HdTextSecondary)
                    Row {
                        Text("Изменить", color = HdTextPrimary, modifier = Modifier.clickable { editingService = svc })
                        Text("  ·  Дубль", color = HdTextPrimary, modifier = Modifier.clickable {
                            scope.launch {
                                app.container.priceRepository.upsertService(
                                    svc.copy(id = 0, name = svc.name + " (копия)", sortOrder = svc.sortOrder + 1),
                                )
                            }
                        })
                        Text("  ·  ★", color = if (svc.isFavorite) HdTextPrimary else HdTextSecondary, modifier = Modifier.clickable {
                            scope.launch { app.container.priceRepository.toggleFavorite(svc.id, !svc.isFavorite) }
                        })
                        Text("  ·  Архив", color = HdTextPrimary, modifier = Modifier.clickable {
                            scope.launch { app.container.priceRepository.archiveService(svc.id) }
                        })
                    }
                }
            }
            HdSecondaryButton("+ Услуга в «${cat.name}»") {
                editingService = ServiceEntity(
                    categoryId = cat.id,
                    name = "",
                    unit = "шт.",
                    priceKopecks = 0,
                    sortOrder = services.count { it.categoryId == cat.id },
                )
            }
        }
    }

    if (showAddCategory) {
        AlertDialog(
            onDismissRequest = { showAddCategory = false },
            title = { Text("Новая категория") },
            text = { HdTextField(newCategoryName, { newCategoryName = it }, "Название") },
            confirmButton = {
                HdPrimaryButton("Сохранить") {
                    scope.launch {
                        app.container.priceRepository.upsertCategory(
                            CategoryEntity(name = newCategoryName, sortOrder = categories.size),
                        )
                        showAddCategory = false
                        newCategoryName = ""
                    }
                }
            },
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Сбросить прайс?") },
            text = { Text("Текущий активный прайс будет архивирован и загружен стартовый список СПб.", color = HdTextSecondary) },
            confirmButton = {
                HdPrimaryButton("Сбросить") {
                    scope.launch {
                        app.container.priceRepository.resetToDefaults()
                        confirmReset = false
                    }
                }
            },
            dismissButton = { HdSecondaryButton("Отмена") { confirmReset = false } },
        )
    }

    editingService?.let { svc ->
        var name by remember(svc.id) { mutableStateOf(svc.name) }
        var unit by remember(svc.id) { mutableStateOf(svc.unit) }
        var price by remember(svc.id) { mutableStateOf((svc.priceKopecks / 100).toString()) }
        AlertDialog(
            onDismissRequest = { editingService = null },
            title = { Text("Услуга") },
            text = {
                Column {
                    HdTextField(name, { name = it }, "Название")
                    HdTextField(unit, { unit = it }, "Единица")
                    HdTextField(price, { price = it }, "Цена, ₽")
                }
            },
            confirmButton = {
                HdPrimaryButton("Сохранить") {
                    scope.launch {
                        val kopecks = parseRublesToKopecks(price) ?: 0L
                        app.container.priceRepository.upsertService(
                            svc.copy(name = name, unit = unit, priceKopecks = kopecks),
                        )
                        editingService = null
                    }
                }
            },
        )
    }
}

@Composable
private fun SettingsScreen(nav: NavHostController) {
    val app = LocalContext.current.applicationContext as ElectrosmetaApplication
    val settings by app.container.settingsRepository.settings.collectAsStateWithLifecycle(AppSettings())
    val scope = rememberCoroutineScope()
    var backupPassword by remember { mutableStateOf("") }
    var pendingImport by remember { mutableStateOf<ByteArray?>(null) }
    var importPreview by remember { mutableStateOf<BackupSummary?>(null) }
    var importMessage by remember { mutableStateOf("") }
    var confirmClear by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bytes = app.container.backupManager.exportEncrypted(backupPassword.toCharArray())
            app.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bytes = app.contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
            try {
                importPreview = app.container.backupManager.previewEncrypted(bytes, backupPassword.toCharArray())
                pendingImport = bytes
            } catch (e: Exception) {
                importMessage = e.message ?: "Ошибка чтения копии"
            }
        }
    }

    HdScaffold(title = "Настройки", onBack = { nav.popBackStack() }) {
        Text("Анимация фона", color = HdTextPrimary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Включена", color = HdTextSecondary, modifier = Modifier.weight(1f))
            Switch(settings.animationEnabled, onCheckedChange = {
                scope.launch { app.container.settingsRepository.update { s -> s.copy(animationEnabled = it) } }
            })
        }
        Text("Скорость: ${settings.animationSpeedPercent}%", color = HdTextSecondary)
        Slider(
            value = settings.animationSpeedPercent.toFloat(),
            onValueChange = {
                scope.launch {
                    app.container.settingsRepository.update { s -> s.copy(animationSpeedPercent = it.toInt()) }
                }
            },
            valueRange = 0f..200f,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Экономичный режим", color = HdTextSecondary, modifier = Modifier.weight(1f))
            Switch(settings.powerSaveMode, onCheckedChange = {
                scope.launch { app.container.settingsRepository.update { s -> s.copy(powerSaveMode = it) } }
            })
        }
        Spacer(Modifier.height(16.dp))
        HdSecondaryButton("Реквизиты и PDF") { nav.navigate(Routes.ABOUT) }
        Spacer(Modifier.height(8.dp))
        HdSecondaryButton("Конфиденциальность") { nav.navigate(Routes.PRIVACY) }
        Spacer(Modifier.height(16.dp))
        HdTextField(backupPassword, { backupPassword = it }, "Пароль резервной копии")
        Spacer(Modifier.height(8.dp))
        HdSecondaryButton("Экспорт резервной копии") {
            exportLauncher.launch("hdlabs-electrosmeta-backup.hdeb")
        }
        Spacer(Modifier.height(8.dp))
        HdSecondaryButton("Импорт резервной копии") {
            importLauncher.launch(arrayOf("*/*"))
        }
        if (importMessage.isNotBlank()) {
            Text(importMessage, color = HdTextSecondary, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(16.dp))
        HdSecondaryButton("Удалить все расчёты") { confirmClear = true }
    }

    importPreview?.let { preview ->
        AlertDialog(
            onDismissRequest = { importPreview = null; pendingImport = null },
            title = { Text("Импорт резервной копии") },
            text = {
                Text(
                    "Будет заменено: категорий ${preview.categories}, услуг ${preview.services}, расчётов ${preview.estimates}.",
                    color = HdTextSecondary,
                )
            },
            confirmButton = {
                HdPrimaryButton("Заменить данные") {
                    scope.launch {
                        pendingImport?.let {
                            app.container.backupManager.importEncrypted(it, backupPassword.toCharArray())
                        }
                        importPreview = null
                        pendingImport = null
                        importMessage = "Импорт выполнен"
                    }
                }
            },
            dismissButton = {
                HdSecondaryButton("Отмена") {
                    importPreview = null
                    pendingImport = null
                }
            },
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Удалить все расчёты?") },
            text = { Text("Прайс и настройки сохранятся.", color = HdTextSecondary) },
            confirmButton = {
                HdPrimaryButton("Удалить") {
                    scope.launch {
                        app.container.estimateRepository.clearAllEstimates()
                        confirmClear = false
                    }
                }
            },
            dismissButton = { HdSecondaryButton("Отмена") { confirmClear = false } },
        )
    }
}

@Composable
private fun AboutScreen(nav: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as ElectrosmetaApplication
    val settings by app.container.settingsRepository.settings.collectAsStateWithLifecycle(AppSettings())
    val scope = rememberCoroutineScope()
    var company by remember(settings) { mutableStateOf(settings.companyName) }
    var performer by remember(settings) { mutableStateOf(settings.performerTitle) }
    var inn by remember(settings) { mutableStateOf(settings.inn) }
    var ogrnip by remember(settings) { mutableStateOf(settings.ogrnip) }
    var phone by remember(settings) { mutableStateOf(settings.phone) }
    var telegram by remember(settings) { mutableStateOf(settings.telegram) }
    var maxContact by remember(settings) { mutableStateOf(settings.maxContact) }
    var validity by remember(settings) { mutableStateOf(settings.offerValidityDays.toString()) }
    var pdfTitle by remember(settings) { mutableStateOf(settings.pdfTitle) }
    var pdfIntro by remember(settings) { mutableStateOf(settings.pdfIntro) }
    var pdfFooter by remember(settings) { mutableStateOf(settings.pdfFooter) }
    var taxPercent by remember(settings) { mutableStateOf(settings.defaultTaxPercent.toString()) }
    var taxLabel by remember(settings) { mutableStateOf(settings.taxLabel) }
    var ghOwner by remember(settings) { mutableStateOf(settings.githubOwner) }
    var ghRepo by remember(settings) { mutableStateOf(settings.githubRepo) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var updateMessage by remember { mutableStateOf("") }

    HdScaffold(title = "О приложении", onBack = { nav.popBackStack() }) {
        HdTextField(company, { company = it }, "Название / мастер")
        HdTextField(performer, { performer = it }, "Вид работ (подзаголовок)")
        HdTextField(inn, { inn = it }, "ИНН")
        HdTextField(ogrnip, { ogrnip = it }, "ОГРНИП")
        HdTextField(phone, { phone = it }, "Телефон")
        HdTextField(telegram, { telegram = it }, "Telegram")
        HdTextField(maxContact, { maxContact = it }, "MAX")
        HdTextField(validity, { validity = it }, "Срок действия сметы, дней")
        HdTextField(pdfTitle, { pdfTitle = it }, "Заголовок PDF")
        HdTextField(pdfIntro, { pdfIntro = it }, "Вступление PDF")
        HdTextField(pdfFooter, { pdfFooter = it }, "Подвал PDF")
        HdTextField(taxLabel, { taxLabel = it }, "Подпись налога")
        HdTextField(taxPercent, { taxPercent = it }, "Налог, % (0 = без НДС)")
        HdTextField(ghOwner, { ghOwner = it }, "GitHub owner (релизы)")
        HdTextField(ghRepo, { ghRepo = it }, "GitHub repo (релизы)")
        Spacer(Modifier.height(8.dp))
        HdPrimaryButton("Сохранить настройки") {
            scope.launch {
                app.container.settingsRepository.update {
                    it.copy(
                        companyName = company,
                        performerTitle = performer,
                        inn = inn,
                        ogrnip = ogrnip,
                        phone = phone,
                        telegram = telegram,
                        maxContact = maxContact,
                        offerValidityDays = validity.toIntOrNull() ?: it.offerValidityDays,
                        pdfTitle = pdfTitle,
                        pdfIntro = pdfIntro,
                        pdfFooter = pdfFooter,
                        taxLabel = taxLabel,
                        defaultTaxPercent = taxPercent.replace(',', '.').toDoubleOrNull() ?: 0.0,
                        githubOwner = ghOwner,
                        githubRepo = ghRepo,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        HdSecondaryButton("Проверить обновления") {
            scope.launch {
                updateMessage = "Проверка…"
                val source = app.container.updateSource(ghOwner, ghRepo)
                updateInfo = withContext(Dispatchers.IO) {
                    source.checkForUpdate(BuildConfig.VERSION_CODE)
                }
                updateMessage = if (updateInfo == null) "Обновлений нет" else "Доступно: ${updateInfo?.versionName}"
            }
        }
        if (updateMessage.isNotBlank()) Text(updateMessage, color = HdTextSecondary)
        updateInfo?.let { info ->
            Spacer(Modifier.height(8.dp))
            HdPrimaryButton("Скачать ${info.versionName}") {
                scope.launch(Dispatchers.IO) {
                    val file = File(context.cacheDir, info.apkFileName)
                    ApkDownloadHelper.download(info.apkUrl, file)
                    val hash = ApkDownloadHelper.sha256(file)
                    withContext(Dispatchers.Main) {
                        updateMessage = "SHA-256: $hash"
                        InstallHelper.promptInstall(context, file)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyScreen(nav: NavHostController) {
    HdScaffold(title = "Конфиденциальность", onBack = { nav.popBackStack() }) {
        Text(
            "Данные клиента (имя, адрес) и расчёты хранятся только на устройстве. " +
                "HDLabs не получает эти данные автоматически. При отправке PDF вы сами выбираете получателя. " +
                "Ответственность за соблюдение 152-ФЗ при работе с данными клиентов несёт исполнитель (мастер/ИП).",
            color = HdTextSecondary,
        )
    }
}

@Composable
private fun EstimateScreen(nav: NavHostController, estimateId: Long) {
    val context = LocalContext.current
    val app = context.applicationContext as ElectrosmetaApplication
    val vm: EstimateEditorViewModel = viewModel(
        factory = EstimateEditorViewModel.Factory(
            estimateId,
            app.container.estimateRepository,
            app.container.priceRepository,
        ),
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val estimate = state.estimate
    val scope = rememberCoroutineScope()
    val settings by app.container.settingsRepository.settings.collectAsStateWithLifecycle(AppSettings())

    var title by remember(estimate?.id) { mutableStateOf(estimate?.title ?: "") }
    var clientName by remember(estimate?.id) { mutableStateOf(estimate?.clientName ?: "") }
    var clientAddress by remember(estimate?.id) { mutableStateOf(estimate?.clientAddress ?: "") }
    var discountPct by remember(estimate?.id) { mutableStateOf(estimate?.discountPercent?.toString() ?: "0") }
    var discountAmt by remember(estimate?.id) { mutableStateOf(((estimate?.discountAmountKopecks ?: 0) / 100).toString()) }
    var extraFee by remember(estimate?.id) { mutableStateOf(((estimate?.extraFeeKopecks ?: 0) / 100).toString()) }
    var complexity by remember(estimate?.id) { mutableStateOf(estimate?.complexityMultiplier?.toString() ?: "1.0") }
    var taxPct by remember(estimate?.id) { mutableStateOf(estimate?.taxPercent?.toString() ?: "0") }
    var comment by remember(estimate?.id) { mutableStateOf(estimate?.comment ?: "") }
    var quickName by remember { mutableStateOf("") }
    var quickUnit by remember { mutableStateOf("шт.") }
    var quickPrice by remember { mutableStateOf("") }
    var quickQty by remember { mutableStateOf("1") }
    var search by remember { mutableStateOf("") }
    var showPdfPreview by remember { mutableStateOf(false) }

    fun saveFields() {
        vm.saveEstimateFields(
            title, clientName, clientAddress,
            discountPct.replace(',', '.').toDoubleOrNull() ?: 0.0,
            parseRublesToKopecks(discountAmt) ?: 0L,
            parseRublesToKopecks(extraFee) ?: 0L,
            complexity.replace(',', '.').toDoubleOrNull() ?: 1.0,
            taxPct.replace(',', '.').toDoubleOrNull() ?: 0.0,
            true,
            comment,
        )
    }

    LaunchedEffect(title, clientName, clientAddress, discountPct, discountAmt, extraFee, complexity, taxPct, comment) {
        if (state.loading || estimate == null) return@LaunchedEffect
        delay(900)
        saveFields()
    }

    HdScaffold(title = estimate?.title ?: "Расчёт", onBack = { nav.popBackStack() }) {
        if (state.loading) {
            Text("Загрузка…", color = HdTextSecondary)
            return@HdScaffold
        }
        HdTextField(title, { title = it }, "Название расчёта")
        HdTextField(clientName, { clientName = it }, "Имя клиента (необяз.)")
        HdTextField(clientAddress, { clientAddress = it }, "Адрес объекта (необяз.)")

        if (estimate?.type == EstimateType.MAIN) {
            if (state.favorites.isNotEmpty()) {
                Text("Избранное", color = HdTextPrimary, modifier = Modifier.padding(top = 8.dp))
                state.favorites.forEach { svc ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(svc.name, color = HdTextSecondary, modifier = Modifier.weight(1f))
                        Text("+", color = HdTextPrimary, modifier = Modifier.clickable { vm.addService(svc) })
                    }
                }
            }
            HdTextField(search, { search = it; vm.search(it) }, "Поиск услуги")
            state.searchResults.forEach { svc ->
                HdCard(Modifier.padding(vertical = 4.dp)) {
                    Text(svc.name, color = HdTextPrimary)
                    Text("${formatRubles(svc.priceKopecks)} / ${svc.unit}", color = HdTextSecondary)
                    Text("Добавить", modifier = Modifier.clickable { vm.addService(svc) })
                }
            }
            state.categories.forEach { cat ->
                val items = state.servicesByCategory[cat.id] ?: emptyList()
                if (items.isNotEmpty()) {
                    Text(cat.name, color = HdTextPrimary, modifier = Modifier.padding(top = 8.dp))
                    items.take(5).forEach { svc ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(svc.name, color = HdTextSecondary, modifier = Modifier.weight(1f))
                            Text("+", color = HdTextPrimary, modifier = Modifier.clickable { vm.addService(svc) })
                        }
                    }
                }
            }
        } else {
            HdTextField(quickName, { quickName = it }, "Название работы")
            HdTextField(quickUnit, { quickUnit = it }, "Единица")
            HdTextField(quickPrice, { quickPrice = it }, "Цена, ₽")
            HdTextField(quickQty, { quickQty = it }, "Количество")
            HdSecondaryButton("Добавить строку") {
                vm.addQuickLine(quickName, quickUnit, quickPrice, quickQty)
                quickName = ""
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Позиции", color = HdTextPrimary)
        state.lines.forEach { line ->
            LineEditor(line, onUpdate = vm::updateLine, onDelete = { vm.removeLine(line.id) })
        }

        Spacer(Modifier.height(12.dp))
        Text("Итог", color = HdTextPrimary)
        HdTextField(discountPct, { discountPct = it }, "Скидка, %")
        HdTextField(discountAmt, { discountAmt = it }, "Скидка, ₽")
        HdTextField(extraFee, { extraFee = it }, "Доплата, ₽")
        HdTextField(complexity, { complexity = it }, "Коэффициент сложности")
        Row {
            listOf("1.0", "1.1", "1.2", "1.3", "1.5").forEach { preset ->
                Text(
                    preset,
                    color = HdTextPrimary,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable { complexity = preset },
                )
            }
        }
        HdTextField(taxPct, { taxPct = it }, "Налог, %")
        HdTextField(comment, { comment = it }, "Комментарий")

        state.calculation?.let {
            Text("Итого: ${formatRubles(it.totalKopecks)}", color = HdTextPrimary, modifier = Modifier.padding(vertical = 8.dp))
        }

        HdSecondaryButton("Сохранить сейчас") { saveFields() }
        Spacer(Modifier.height(8.dp))
        HdPrimaryButton("Сформировать PDF") { showPdfPreview = true }
    }

    if (showPdfPreview) {
        val total = state.calculation?.totalKopecks
        AlertDialog(
            onDismissRequest = { showPdfPreview = false },
            title = { Text("Предпросмотр сметы") },
            text = {
                Column {
                    Text("Клиент: ${clientName.ifBlank { "—" }}", color = HdTextSecondary)
                    Text("Адрес: ${clientAddress.ifBlank { "—" }}", color = HdTextSecondary)
                    Text("Позиций: ${state.lines.size}", color = HdTextSecondary)
                    Text("Итого: ${total?.let { formatRubles(it) } ?: "—"}", color = HdTextPrimary)
                    Text(settings.pdfFooter, color = HdTextSecondary, modifier = Modifier.padding(top = 8.dp))
                }
            },
            confirmButton = {
                HdPrimaryButton("Создать PDF") {
                    scope.launch {
                        saveFields()
                        val data = vm.getWithLines() ?: return@launch
                        val calc = vm.state.value.calculation ?: return@launch
                        val file = File(context.cacheDir, "smeta_${estimateId}.pdf")
                        app.container.pdfGenerator.generate(data, settings, calc, file)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Отправить смету"))
                        showPdfPreview = false
                    }
                }
            },
            dismissButton = { HdSecondaryButton("Отмена") { showPdfPreview = false } },
        )
    }
}

@Composable
private fun LineEditor(
    line: EstimateLineEntity,
    onUpdate: (EstimateLineEntity) -> Unit,
    onDelete: () -> Unit,
) {
    var qty by remember(line.id) { mutableStateOf(line.quantity.toString()) }
    var price by remember(line.id) { mutableStateOf((line.unitPriceKopecks / 100.0).toString()) }
    var note by remember(line.id) { mutableStateOf(line.note ?: "") }
    HdCard(Modifier.padding(vertical = 4.dp)) {
        Text(line.name, color = HdTextPrimary)
        Text(line.unit, color = HdTextSecondary)
        HdTextField(qty, { qty = it }, "Кол-во")
        HdTextField(price, { price = it }, "Цена, ₽")
        HdTextField(note, { note = it }, "Примечание")
        Row {
            Text("Обновить", modifier = Modifier.clickable {
                val q = qty.replace(',', '.').toDoubleOrNull() ?: line.quantity
                val p = parseRublesToKopecks(price) ?: line.unitPriceKopecks
                onUpdate(line.copy(quantity = q, unitPriceKopecks = p, note = note.ifBlank { null }))
            })
            Text("  ·  Удалить", modifier = Modifier.clickable(onClick = onDelete))
        }
    }
}
