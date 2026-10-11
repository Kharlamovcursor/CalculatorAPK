package ru.hdlabs.electrosmeta.navigation

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ru.hdlabs.electrosmeta.ui.theme.HdBlue
import ru.hdlabs.electrosmeta.ui.theme.HdOrange
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
import ru.hdlabs.electrosmeta.feature.estimate.EstimateEditorUiState
import ru.hdlabs.electrosmeta.feature.estimate.EstimateEditorViewModel
import ru.hdlabs.electrosmeta.update.ApkDownloadHelper
import ru.hdlabs.electrosmeta.update.ReleaseUpdateConfig
import ru.hdlabs.electrosmeta.update.UpdateInfo
import ru.hdlabs.electrosmeta.ui.background.SpaceBackground
import ru.hdlabs.electrosmeta.ui.components.HdCard
import ru.hdlabs.electrosmeta.ui.components.HdPrimaryButton
import ru.hdlabs.electrosmeta.ui.components.HdQuickUnits
import ru.hdlabs.electrosmeta.ui.components.HdUnitChoiceRow
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
    showTopBar: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        SpaceBackground()
        Scaffold(
            containerColor = HdBlack.copy(alpha = 0.55f),
            topBar = {
                if (showTopBar) {
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
                }
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
    val app = LocalContext.current.applicationContext as ElectrosmetaApplication
    val scope = rememberCoroutineScope()
    var showNewEstimateOptions by remember { mutableStateOf(false) }

    HdScaffold(title = "", scroll = false, showTopBar = false) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.calc_home_logo),
                    contentDescription = "Calc by HDLabs",
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .padding(bottom = 20.dp),
                    contentScale = ContentScale.Fit,
                )
                HdPrimaryButton(
                    text = if (showNewEstimateOptions) "Скрыть варианты" else "Новый расчёт",
                    onClick = { showNewEstimateOptions = !showNewEstimateOptions },
                )
                AnimatedVisibility(
                    visible = showNewEstimateOptions,
                    enter = expandVertically(animationSpec = tween(280)) + fadeIn(tween(240)),
                    exit = shrinkVertically(animationSpec = tween(220)) + fadeOut(tween(200)),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        HdPrimaryButton("Основной расчёт") {
                            showNewEstimateOptions = false
                            scope.launch {
                                val tax = app.container.settingsRepository.settings.first().defaultTaxPercent
                                val id = app.container.estimateRepository.createDraft(EstimateType.MAIN, tax)
                                nav.navigate(Routes.estimate(id))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        HdSecondaryButton("Быстрый расчёт") {
                            showNewEstimateOptions = false
                            scope.launch {
                                val tax = app.container.settingsRepository.settings.first().defaultTaxPercent
                                val id = app.container.estimateRepository.createDraft(EstimateType.QUICK, tax)
                                nav.navigate(Routes.estimate(id))
                            }
                        }
                    }
                }
                if (!showNewEstimateOptions) {
                    Spacer(Modifier.height(12.dp))
                    HdSecondaryButton("История", onClick = { nav.navigate(Routes.HISTORY) })
                    Spacer(Modifier.height(12.dp))
                    HdSecondaryButton("Прайс-лист", onClick = { nav.navigate(Routes.PRICE) })
                    Spacer(Modifier.height(12.dp))
                    HdSecondaryButton("Настройки", onClick = { nav.navigate(Routes.SETTINGS) })
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Версия ${BuildConfig.VERSION_NAME}",
                color = HdTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun NewEstimateTypeScreen(nav: NavHostController) {
    val app = LocalContext.current.applicationContext as ElectrosmetaApplication
    val scope = rememberCoroutineScope()
    HdScaffold(title = "Новый расчёт", onBack = { nav.popBackStack() }, scroll = false) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
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
            text = {
                Text(
                    "Текущий активный прайс будет архивирован и заменён стартовым списком услуг. " +
                        "Уже созданные расчёты не изменятся.",
                    color = HdTextSecondary,
                )
            },
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
    var taxLabel by remember(settings) { mutableStateOf(settings.taxLabel) }
    var taxPercent by remember(settings) { mutableStateOf(settings.defaultTaxPercent.toString()) }
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
        HdCard {
            Text("Налог в сметах", color = HdTextPrimary)
            Text(
                "Задаётся один раз — применяется ко всем новым расчётам и PDF.",
                color = HdTextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            HdTextField(taxLabel, { taxLabel = it }, "Подпись (например «Без НДС»)")
            HdTextField(taxPercent, { taxPercent = it }, "Налог, % (0 = без налога)")
            HdSecondaryButton("Сохранить налог") {
                scope.launch {
                    app.container.settingsRepository.update {
                        it.copy(
                            taxLabel = taxLabel,
                            defaultTaxPercent = taxPercent.replace(',', '.').toDoubleOrNull() ?: 0.0,
                        )
                    }
                }
            }
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
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        HdSecondaryButton("Проверить обновления") {
            scope.launch {
                updateMessage = "Проверка…"
                updateInfo = null
                if (!ReleaseUpdateConfig.isConfigured()) {
                    updateMessage = "Репозиторий обновлений не настроен в сборке приложения"
                    return@launch
                }
                try {
                    val source = app.container.updateSource(
                        ReleaseUpdateConfig.githubOwner,
                        ReleaseUpdateConfig.githubRepo,
                    )
                    updateInfo = withContext(Dispatchers.IO) {
                        source.checkForUpdate(BuildConfig.VERSION_CODE)
                    }
                    updateMessage = if (updateInfo == null) {
                        "У вас актуальная версия ${BuildConfig.VERSION_NAME}. " +
                            "Или в Releases нет APK новее текущей."
                    } else {
                        "Доступно: ${updateInfo?.versionName}"
                    }
                } catch (e: Exception) {
                    updateInfo = null
                    updateMessage = e.message ?: "Не удалось проверить обновления"
                }
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
    var workTitle by remember(estimate?.id) { mutableStateOf(estimate?.workTitle ?: "") }
    var showServicePicker by remember { mutableStateOf(false) }
    var discountPct by remember(estimate?.id) { mutableStateOf(estimate?.discountPercent?.toString() ?: "0") }
    var discountAmt by remember(estimate?.id) { mutableStateOf(((estimate?.discountAmountKopecks ?: 0) / 100).toString()) }
    var extraFee by remember(estimate?.id) { mutableStateOf(((estimate?.extraFeeKopecks ?: 0) / 100).toString()) }
    var complexity by remember(estimate?.id) { mutableStateOf(estimate?.complexityMultiplier?.toString() ?: "1.0") }
    var comment by remember(estimate?.id) { mutableStateOf(estimate?.comment ?: "") }
    var quickName by remember { mutableStateOf("") }
    var quickUnit by remember { mutableStateOf("шт.") }
    var quickPrice by remember { mutableStateOf("") }
    var quickQty by remember { mutableStateOf("1") }
    var showPdfPreview by remember { mutableStateOf(false) }
    var showPdfReady by remember { mutableStateOf(false) }
    var generatedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var generatedPdfSizeLabel by remember { mutableStateOf("") }

    fun saveFields() {
        vm.saveEstimateFields(
            title, clientName, clientAddress, workTitle,
            discountPct.replace(',', '.').toDoubleOrNull() ?: 0.0,
            parseRublesToKopecks(discountAmt) ?: 0L,
            parseRublesToKopecks(extraFee) ?: 0L,
            complexity.replace(',', '.').toDoubleOrNull() ?: 1.0,
            settings.defaultTaxPercent,
            true,
            comment,
        )
    }

    LaunchedEffect(
        title,
        clientName,
        clientAddress,
        workTitle,
        discountPct,
        discountAmt,
        extraFee,
        complexity,
        settings.defaultTaxPercent,
        settings.taxLabel,
        comment,
    ) {
        if (state.loading || estimate == null) return@LaunchedEffect
        delay(900)
        saveFields()
    }

    HdScaffold(title = estimate?.title ?: "Расчёт", onBack = { nav.popBackStack() }) {
        if (state.loading) {
            Text("Загрузка…", color = HdTextSecondary)
            return@HdScaffold
        }
        if (estimate?.type == EstimateType.MAIN) {
            HdCard(Modifier.padding(bottom = 12.dp)) {
                Text("Данные расчёта", color = HdTextPrimary)
                Spacer(Modifier.height(8.dp))
                HdTextField(title, { title = it }, "Название расчёта")
                HdTextField(clientName, { clientName = it }, "Имя клиента (необяз.)")
                HdTextField(clientAddress, { clientAddress = it }, "Адрес объекта (необяз.)")
                HdTextField(workTitle, { workTitle = it }, "Название работ")
            }
            HdCard(Modifier.padding(bottom = 12.dp)) {
                Text("Услуги", color = HdTextPrimary, modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(HdBlue, HdOrange)),
                            ),
                            RoundedCornerShape(14.dp),
                        )
                        .clickable { showServicePicker = !showServicePicker }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (showServicePicker) "Скрыть выбор из прайса" else "Добавить из прайса",
                        color = HdTextPrimary,
                    )
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = HdOrange,
                    )
                }
                AnimatedVisibility(
                    visible = showServicePicker,
                    enter = expandVertically(animationSpec = tween(320)) + fadeIn(animationSpec = tween(280)),
                    exit = shrinkVertically(animationSpec = tween(260)) + fadeOut(animationSpec = tween(220)),
                ) {
                    MainServicePickerPanel(
                        state = state,
                        onAddServices = vm::addServices,
                    )
                }
            }
        } else {
            HdCard(Modifier.padding(bottom = 12.dp)) {
                Text("Данные расчёта", color = HdTextPrimary)
                Spacer(Modifier.height(8.dp))
                HdTextField(title, { title = it }, "Название расчёта")
                HdTextField(clientName, { clientName = it }, "Имя клиента (необяз.)")
                HdTextField(clientAddress, { clientAddress = it }, "Адрес объекта (необяз.)")
            }
            HdCard(Modifier.padding(bottom = 12.dp)) {
                Text("Новая позиция", color = HdTextPrimary)
                Spacer(Modifier.height(8.dp))
                HdTextField(quickName, { quickName = it }, "Название работы")
                HdUnitChoiceRow(quickUnit, { quickUnit = it })
                Spacer(Modifier.height(8.dp))
                HdTextField(quickPrice, { quickPrice = it }, "Цена, ₽")
                HdTextField(quickQty, { quickQty = it }, "Количество")
                HdSecondaryButton("Добавить в смету") {
                    vm.addQuickLine(quickName, quickUnit, quickPrice, quickQty)
                    quickName = ""
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Позиции", color = HdTextPrimary)
        val isQuick = estimate?.type == EstimateType.QUICK
        state.lines.forEach { line ->
            LineEditor(
                line,
                allowUnitChoice = isQuick,
                onUpdate = vm::updateLine,
                onDelete = { vm.removeLine(line.id) },
            )
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
            title = { Text("Проверка перед PDF") },
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
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file,
                        )
                        generatedPdfUri = uri
                        generatedPdfSizeLabel = when {
                            file.length() < 1024 -> "${file.length()} Б"
                            else -> "${file.length() / 1024} КБ"
                        }
                        showPdfPreview = false
                        showPdfReady = true
                    }
                }
            },
            dismissButton = { HdSecondaryButton("Отмена") { showPdfPreview = false } },
        )
    }

    if (showPdfReady) {
        val uri = generatedPdfUri
        AlertDialog(
            onDismissRequest = { showPdfReady = false },
            title = { Text("PDF готов") },
            text = {
                Text(
                    "Размер файла: $generatedPdfSizeLabel. Логотип HDLabs в PDF не добавляется.",
                    color = HdTextSecondary,
                )
            },
            confirmButton = {
                HdPrimaryButton("Открыть") {
                    if (uri != null) {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    }
                    showPdfReady = false
                }
            },
            dismissButton = {
                HdSecondaryButton("Отправить") {
                    if (uri != null) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Отправить смету"))
                    }
                    showPdfReady = false
                }
            },
        )
    }
}

private const val FAVORITES_PICKER_CATEGORY_ID = -1L

private enum class ServicePickerStep { Categories, Services }

@Composable
private fun MainServicePickerPanel(
    state: EstimateEditorUiState,
    onAddServices: (List<ServiceEntity>) -> Unit,
) {
    var step by remember { mutableStateOf(ServicePickerStep.Categories) }
    var activeCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedServiceIds by remember { mutableStateOf(setOf<Long>()) }

    val servicesInCategory: List<ServiceEntity> = when (activeCategoryId) {
        FAVORITES_PICKER_CATEGORY_ID -> state.favorites
        null -> emptyList()
        else -> state.servicesByCategory[activeCategoryId] ?: emptyList()
    }
    val activeCategoryLabel = when (activeCategoryId) {
        FAVORITES_PICKER_CATEGORY_ID -> "Избранное"
        null -> ""
        else -> state.categories.find { it.id == activeCategoryId }?.name ?: ""
    }

    Column(Modifier.padding(top = 12.dp)) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState == ServicePickerStep.Services) {
                    slideInHorizontally(animationSpec = tween(300)) { it / 3 } + fadeIn(tween(280)) togetherWith
                        slideOutHorizontally(animationSpec = tween(300)) { -it / 3 } + fadeOut(tween(220))
                } else {
                    slideInHorizontally(animationSpec = tween(300)) { -it / 3 } + fadeIn(tween(280)) togetherWith
                        slideOutHorizontally(animationSpec = tween(300)) { it / 3 } + fadeOut(tween(220))
                }
            },
            label = "servicePickerStep",
        ) { currentStep ->
            when (currentStep) {
                ServicePickerStep.Categories -> {
                    Column {
                        Text("Выберите категорию из прайса", color = HdTextSecondary)
                        Spacer(Modifier.height(8.dp))
                        if (state.favorites.isNotEmpty()) {
                            ServiceCategoryChip(
                                name = "Избранное",
                                selected = activeCategoryId == FAVORITES_PICKER_CATEGORY_ID,
                                onClick = {
                                    activeCategoryId = FAVORITES_PICKER_CATEGORY_ID
                                    selectedServiceIds = emptySet()
                                    step = ServicePickerStep.Services
                                },
                            )
                        }
                        state.categories.forEach { cat ->
                            val count = state.servicesByCategory[cat.id]?.size ?: 0
                            if (count > 0) {
                                ServiceCategoryChip(
                                    name = cat.name,
                                    selected = activeCategoryId == cat.id,
                                    onClick = {
                                        activeCategoryId = cat.id
                                        selectedServiceIds = emptySet()
                                        step = ServicePickerStep.Services
                                    },
                                )
                            }
                        }
                    }
                }
                ServicePickerStep.Services -> {
                    Column {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    step = ServicePickerStep.Categories
                                    selectedServiceIds = emptySet()
                                    activeCategoryId = null
                                }
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "К категориям",
                                tint = HdOrange,
                            )
                            Text("К категориям", color = HdOrange, modifier = Modifier.padding(start = 6.dp))
                        }
                        Text("Категория", color = HdTextSecondary)
                        ServiceCategoryChip(
                            name = activeCategoryLabel,
                            selected = true,
                            onClick = {},
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Отметьте работы в этой категории", color = HdTextSecondary)
                        Spacer(Modifier.height(6.dp))
                        servicesInCategory.forEach { svc ->
                            val checked = selectedServiceIds.contains(svc.id)
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedServiceIds = if (checked) {
                                            selectedServiceIds - svc.id
                                        } else {
                                            selectedServiceIds + svc.id
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        selectedServiceIds = if (isChecked) {
                                            selectedServiceIds + svc.id
                                        } else {
                                            selectedServiceIds - svc.id
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = HdOrange,
                                        checkmarkColor = HdBlack,
                                        uncheckedColor = HdTextSecondary,
                                    ),
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(svc.name, color = HdTextPrimary)
                                    Text(
                                        "${formatRubles(svc.priceKopecks)} / ${svc.unit}",
                                        color = HdTextSecondary,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        HdPrimaryButton(
                            text = if (selectedServiceIds.isEmpty()) {
                                "Выберите работы"
                            } else {
                                "Подтвердить (${selectedServiceIds.size})"
                            },
                            enabled = selectedServiceIds.isNotEmpty(),
                        ) {
                            val picked = servicesInCategory.filter { selectedServiceIds.contains(it.id) }
                            onAddServices(picked)
                            selectedServiceIds = emptySet()
                            step = ServicePickerStep.Categories
                            activeCategoryId = null
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceCategoryChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(shape)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) HdOrange else HdTextSecondary.copy(alpha = 0.35f),
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            name,
            color = if (selected) HdOrange else HdTextPrimary,
        )
    }
}

@Composable
private fun LineEditor(
    line: EstimateLineEntity,
    allowUnitChoice: Boolean,
    onUpdate: (EstimateLineEntity) -> Unit,
    onDelete: () -> Unit,
) {
    var qty by remember(line.id) { mutableStateOf(line.quantity.toString()) }
    var price by remember(line.id) { mutableStateOf((line.unitPriceKopecks / 100.0).toString()) }
    var note by remember(line.id) { mutableStateOf(line.note ?: "") }
    var unit by remember(line.id) {
        mutableStateOf(line.unit.takeIf { it in HdQuickUnits } ?: HdQuickUnits.first())
    }
    HdCard(Modifier.padding(vertical = 4.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(line.name, color = HdTextPrimary, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = HdTextSecondary)
            }
        }
        if (allowUnitChoice) {
            HdUnitChoiceRow(unit, { unit = it })
        } else {
            Text(line.unit, color = HdTextSecondary)
        }
        HdTextField(qty, { qty = it }, "Кол-во")
        HdTextField(price, { price = it }, "Цена, ₽")
        HdTextField(note, { note = it }, "Примечание")
        Text(
            "Обновить",
            color = HdTextPrimary,
            modifier = Modifier.clickable {
                val q = qty.replace(',', '.').toDoubleOrNull() ?: line.quantity
                val p = parseRublesToKopecks(price) ?: line.unitPriceKopecks
                val u = if (allowUnitChoice) unit else line.unit
                onUpdate(line.copy(quantity = q, unitPriceKopecks = p, unit = u, note = note.ifBlank { null }))
            },
        )
    }
}
