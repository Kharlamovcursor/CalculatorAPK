package ru.hdlabs.electrosmeta.navigation

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.ui.draw.scale
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import kotlinx.coroutines.delay
import ru.hdlabs.electrosmeta.R
import ru.hdlabs.electrosmeta.backup.BackupSummary
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateEntity
import ru.hdlabs.electrosmeta.data.local.entities.EstimateLineEntity
import ru.hdlabs.electrosmeta.data.local.entities.ServiceEntity
import ru.hdlabs.electrosmeta.data.settings.AppSettings
import ru.hdlabs.electrosmeta.domain.EstimateLineCategoryKeys
import ru.hdlabs.electrosmeta.domain.parseRublesToKopecks
import ru.hdlabs.electrosmeta.domain.formatRubles
import ru.hdlabs.electrosmeta.feature.estimate.EstimateEditorUiState
import ru.hdlabs.electrosmeta.feature.estimate.EstimateEditorViewModel
import ru.hdlabs.electrosmeta.feature.template.TemplateEditorUiState
import ru.hdlabs.electrosmeta.feature.template.TemplateEditorViewModel
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateLineEntity
import ru.hdlabs.electrosmeta.ui.components.ReorderDragHandle
import ru.hdlabs.electrosmeta.ui.components.HdReorderableState
import ru.hdlabs.electrosmeta.ui.components.hdReorderableItem
import ru.hdlabs.electrosmeta.ui.components.rememberHdReorderableState
import ru.hdlabs.electrosmeta.update.ApkDownloadHelper
import ru.hdlabs.electrosmeta.update.ReleaseUpdateConfig
import ru.hdlabs.electrosmeta.update.UpdateInfo
import ru.hdlabs.electrosmeta.pdf.PdfStorageHelper
import ru.hdlabs.electrosmeta.ui.background.SpaceBackground
import ru.hdlabs.electrosmeta.data.settings.CategoryHighlightStyle
import ru.hdlabs.electrosmeta.ui.motion.LocalHdMotion
import ru.hdlabs.electrosmeta.ui.motion.resolveHdMotion
import ru.hdlabs.electrosmeta.ui.components.HdCard
import ru.hdlabs.electrosmeta.ui.components.categoryHighlightColors
import ru.hdlabs.electrosmeta.ui.components.HdPrimaryButton
import ru.hdlabs.electrosmeta.ui.components.HdQuickUnits
import ru.hdlabs.electrosmeta.ui.components.HdUnitChoiceRow
import ru.hdlabs.electrosmeta.ui.components.HdSaveButton
import ru.hdlabs.electrosmeta.ui.components.HdSavePhase
import ru.hdlabs.electrosmeta.ui.components.HdSecondaryButton
import ru.hdlabs.electrosmeta.ui.components.HdDialogFieldColumn
import ru.hdlabs.electrosmeta.ui.components.HdTextField
import ru.hdlabs.electrosmeta.ui.components.HdThumbFriendlyBody
import androidx.compose.foundation.background
import ru.hdlabs.electrosmeta.ui.theme.HdBlack
import ru.hdlabs.electrosmeta.ui.theme.HdPanel
import ru.hdlabs.electrosmeta.ui.theme.HdPanelElevated
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
    const val ESTIMATE_HUB = "estimate_hub"
    const val TEMPLATE_EDIT = "template_edit/{id}"
    const val ESTIMATE = "estimate/{id}"
    fun estimate(id: Long) = "estimate/$id"
    fun templateEdit(id: Long) = "template_edit/$id"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost() {
    val context = LocalContext.current
    val app = context.applicationContext as ElectrosmetaApplication
    val settings by app.container.settingsRepository.settings.collectAsStateWithLifecycle(AppSettings())
    val motion = remember(settings) { resolveHdMotion(settings) }
    CompositionLocalProvider(LocalHdMotion provides motion) {
        val nav = rememberNavController()
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            enterTransition = {
                val slide = motion.screenSlideDivisor
                fadeIn(tween(motion.screenEnterMs)) +
                    slideInHorizontally(tween(motion.screenEnterMs)) { width ->
                        if (slide <= 0) 0 else width / slide
                    }
            },
            exitTransition = { fadeOut(tween(motion.screenExitMs)) },
            popEnterTransition = {
                val slide = motion.screenSlideDivisor
                fadeIn(tween(motion.screenEnterMs)) +
                    slideInHorizontally(tween(motion.screenEnterMs)) { width ->
                        if (slide <= 0) 0 else -width / slide
                    }
            },
            popExitTransition = {
                val slide = motion.screenSlideDivisor
                fadeOut(tween(motion.screenExitMs)) +
                    slideOutHorizontally(tween(motion.screenExitMs)) { width ->
                        if (slide <= 0) 0 else width / slide
                    }
            },
        ) {
            composable(Routes.HOME) { HomeScreen(nav) }
            composable(Routes.ESTIMATE_HUB) { EstimateHubScreen(nav) }
            composable(
                route = Routes.TEMPLATE_EDIT,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: return@composable
                TemplateEditScreen(nav, id)
            }
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
    val motion = LocalHdMotion.current
    Box(Modifier.fillMaxSize()) {
        SpaceBackground()
        Scaffold(
            containerColor = HdBlack.copy(alpha = motion.scaffoldOverlayAlpha),
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
                .imePadding()
            if (scroll) {
                Column(mod.verticalScroll(rememberScrollState())) { content() }
            } else {
                Column(mod.fillMaxSize()) { content() }
            }
        }
    }
}

@Composable
private fun HomeScreen(nav: NavHostController) {
    HdScaffold(title = "", scroll = false, showTopBar = false) {
        HdThumbFriendlyBody(
            footer = {
                Text(
                    "Версия ${BuildConfig.VERSION_NAME}",
                    color = HdTextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    textAlign = TextAlign.Center,
                )
            },
        ) {
            Image(
                painter = painterResource(R.drawable.calc_home_logo),
                contentDescription = "Calc by HDLabs",
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .padding(bottom = 12.dp),
                contentScale = ContentScale.Fit,
            )
            HdPrimaryButton("Расчёт сметы") { nav.navigate(Routes.ESTIMATE_HUB) }
            Spacer(Modifier.height(12.dp))
            HdSecondaryButton("История", onClick = { nav.navigate(Routes.HISTORY) })
            Spacer(Modifier.height(12.dp))
            HdSecondaryButton("Прайс-лист", onClick = { nav.navigate(Routes.PRICE) })
            Spacer(Modifier.height(12.dp))
            HdSecondaryButton("Настройки", onClick = { nav.navigate(Routes.SETTINGS) })
        }
    }
}

@Composable
private fun EstimateHubScreen(nav: NavHostController) {
    val app = LocalContext.current.applicationContext as ElectrosmetaApplication
    val scope = rememberCoroutineScope()
    val templates by app.container.templateRepository.observeTemplates()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var orderedIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    val templateReorderState = rememberHdReorderableState()
    LaunchedEffect(templates) {
        if (templateReorderState.isDragging) return@LaunchedEffect
        val ids = templates.map { it.id }
        orderedIds = (orderedIds.filter { ids.contains(it) } + ids.filter { !orderedIds.contains(it) })
    }
    val orderedTemplates = orderedIds.mapNotNull { id -> templates.find { it.id == id } }
    var deleteTemplateTarget by remember { mutableStateOf<EstimateTemplateEntity?>(null) }

    HdScaffold(title = "Расчёт сметы", onBack = { nav.popBackStack() }, scroll = false) {
        HdThumbFriendlyBody {
                HdPrimaryButton("Новый расчёт") {
                    scope.launch {
                        val tax = app.container.settingsRepository.settings.first().defaultTaxPercent
                        val id = app.container.estimateRepository.createDraft(EstimateType.MAIN, tax)
                        nav.navigate(Routes.estimate(id))
                    }
                }
                Text(
                    "Пустая смета. Типовые работы — шаблоны ниже.",
                    color = HdTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                )
                if (orderedTemplates.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Порядок: удерживайте ⋮⋮ справа и перетаскивайте между пунктами.",
                        color = HdTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    )
                }
                orderedTemplates.forEachIndexed { index, template ->
                    key(template.id) {
                    TemplateHubRow(
                        template = template,
                        reorderState = templateReorderState,
                        index = index,
                        itemCount = orderedTemplates.size,
                        onMove = { from, to ->
                            val next = orderedIds.toMutableList()
                            val id = next.removeAt(from)
                            next.add(to, id)
                            orderedIds = next
                        },
                        onDragEnd = {
                            scope.launch { app.container.templateRepository.reorderTemplates(orderedIds) }
                        },
                        onOpenEstimate = {
                            scope.launch {
                                val tax = app.container.settingsRepository.settings.first().defaultTaxPercent
                                val estimateId = app.container.templateRepository.createEstimateFromTemplate(template.id, tax)
                                if (estimateId != null) {
                                    nav.navigate(Routes.estimate(estimateId))
                                }
                            }
                        },
                        onEdit = { nav.navigate(Routes.templateEdit(template.id)) },
                        onDelete = { deleteTemplateTarget = template },
                    )
                    }
                }
                HdSecondaryButton("Добавить шаблон") {
                    scope.launch {
                        val id = app.container.templateRepository.createTemplate("", EstimateType.MAIN)
                        nav.navigate(Routes.templateEdit(id))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "В шаблоне — те же кнопки «Своя позиция» и «Добавить из прайса», что в расчёте.",
                    color = HdTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
        }
    }

    deleteTemplateTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTemplateTarget = null },
            title = { Text("Удалить шаблон?") },
            text = {
                Text(
                    "«${target.name}» и все его работы будут удалены без восстановления.",
                    color = HdTextSecondary,
                )
            },
            confirmButton = {
                HdPrimaryButton("Удалить") {
                    scope.launch {
                        app.container.templateRepository.deleteTemplate(target.id)
                        deleteTemplateTarget = null
                    }
                }
            },
            dismissButton = { HdSecondaryButton("Отмена") { deleteTemplateTarget = null } },
        )
    }
}

@Composable
private fun TemplateEditScreen(nav: NavHostController, templateId: Long) {
    val context = LocalContext.current
    val app = context.applicationContext as ElectrosmetaApplication
    val vm: TemplateEditorViewModel = viewModel(
        factory = TemplateEditorViewModel.Factory(
            templateId,
            app.container.templateRepository,
            app.container.priceRepository,
        ),
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val settings by app.container.settingsRepository.settings.collectAsStateWithLifecycle(AppSettings())
    val motion = LocalHdMotion.current
    val scope = rememberCoroutineScope()
    val template = state.template
    val snackbarHostState = remember { SnackbarHostState() }
    var title by remember(template?.id) { mutableStateOf(template?.name ?: "") }
    val templateLineReorderState = rememberHdReorderableState()
    var showDeleteTemplateConfirm by remember { mutableStateOf(false) }
    var showServicePicker by remember { mutableStateOf(false) }
    var showCustomLineForm by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customUnit by remember { mutableStateOf("шт.") }
    var customPrice by remember { mutableStateOf("") }
    var customQty by remember { mutableStateOf("1") }
    var expandedKeys by remember { mutableStateOf(setOf<Long>()) }
    var highlightedTemplateCategoryKey by remember { mutableStateOf<Long?>(null) }
    var categoryOrder by remember(state.categoryOrder) { mutableStateOf(state.categoryOrder) }
    LaunchedEffect(highlightedTemplateCategoryKey) {
        if (highlightedTemplateCategoryKey != null) {
            delay(4000)
            highlightedTemplateCategoryKey = null
        }
    }
    LaunchedEffect(state.categoryOrder) {
        if (!templateLineReorderState.isDragging) categoryOrder = state.categoryOrder
    }
    LaunchedEffect(title, template?.id) {
        if (state.loading || template == null) return@LaunchedEffect
        delay(800)
        val trimmed = title.trim()
        if (trimmed.isEmpty() || trimmed == template.name) return@LaunchedEffect
        vm.rename(trimmed)
        snackbarHostState.showSnackbar("Название сохранено", duration = SnackbarDuration.Short)
    }
    val saveAndLeave: () -> Unit = {
        val currentName = template?.name
        val trimmed = title.trim()
        if (currentName != null && trimmed.isNotEmpty() && trimmed != currentName) {
            vm.rename(trimmed)
        }
        nav.popBackStack()
    }
    BackHandler(onBack = saveAndLeave)

    Box(Modifier.fillMaxSize()) {
    HdScaffold(title = template?.name ?: "Шаблон", onBack = saveAndLeave) {
        if (state.loading || template == null) {
            Text("Загрузка…", color = HdTextSecondary)
            return@HdScaffold
        }
        HdTextField(title, { title = it }, "Название шаблона")
        Text(
            "Название сохраняется автоматически",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Spacer(Modifier.height(4.dp))
        val groups = buildTemplateLineGroups(state, categoryOrder)
        HdCard(Modifier.padding(bottom = 12.dp)) {
            Text("Состав шаблона", color = HdTextPrimary, modifier = Modifier.padding(bottom = 8.dp))
            if (groups.isEmpty()) {
                Text(
                    "Пока пусто. Добавьте работы из прайса или свою позицию — как в расчёте сметы.",
                    color = HdTextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
            HdSecondaryButton(if (showCustomLineForm) "Скрыть форму" else "Своя позиция") {
                showCustomLineForm = !showCustomLineForm
                if (showCustomLineForm) showServicePicker = false
            }
            AnimatedVisibility(
                visible = showCustomLineForm,
                enter = expandVertically(tween(motion.expandDurationMs)) + fadeIn(tween(motion.expandDurationMs)),
                exit = shrinkVertically(tween(motion.expandDurationMs - 40)) + fadeOut(tween(motion.expandDurationMs - 60)),
            ) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    HdTextField(customName, { customName = it }, "Название работы")
                    HdUnitChoiceRow(customUnit, { customUnit = it })
                    Spacer(Modifier.height(8.dp))
                    HdTextField(customPrice, { customPrice = it }, "Цена, ₽")
                    HdTextField(customQty, { customQty = it }, "Количество")
                    HdSecondaryButton("Добавить в шаблон") {
                        vm.addCustomLine(customName, customUnit, customPrice, customQty)
                        customName = ""
                        expandedKeys = expandedKeys + EstimateLineCategoryKeys.UNCATEGORIZED
                    }
                }
            }
            HdSecondaryButton(if (showServicePicker) "Скрыть прайс" else "Добавить из прайса") {
                showServicePicker = !showServicePicker
                if (showServicePicker) showCustomLineForm = false
            }
            AnimatedVisibility(
                visible = showServicePicker,
                enter = expandVertically(tween(motion.expandDurationMs)) + fadeIn(tween(motion.expandDurationMs)),
                exit = shrinkVertically(tween(motion.expandDurationMs - 40)) + fadeOut(tween(motion.expandDurationMs - 60)),
            ) {
                MainServicePickerPanel(
                    state = state.toPricePickerState(),
                    highlightStyle = settings.categoryHighlightStyle,
                    animationEnabled = settings.animationEnabled && !settings.powerSaveMode,
                    onAddServices = { services, key ->
                        val keys = services.map { it.categoryId }.toSet()
                        val highlight = keys.firstOrNull() ?: key
                        vm.addServices(services, key)
                        expandedKeys = expandedKeys + keys + highlight
                        highlightedTemplateCategoryKey = highlight
                        showServicePicker = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "✓ Добавлено в «${categoryTitleFromPrice(state.categories, state.favorites, highlight)}»: ${services.size} поз.",
                                duration = SnackbarDuration.Short,
                            )
                        }
                    },
                )
            }
            groups.forEachIndexed { index, group ->
                key(group.categoryKey) {
                MainCategoryLinesSection(
                    group = group,
                    expanded = expandedKeys.contains(group.categoryKey),
                    highlighted = highlightedTemplateCategoryKey == group.categoryKey,
                    highlightStyle = settings.categoryHighlightStyle,
                    animationEnabled = settings.animationEnabled && !settings.powerSaveMode,
                    onToggleExpand = {
                        expandedKeys = if (expandedKeys.contains(group.categoryKey)) {
                            expandedKeys - group.categoryKey
                        } else {
                            expandedKeys + group.categoryKey
                        }
                    },
                    onUpdateLine = { line -> vm.updateLine(line.toTemplateLine(templateId, group.categoryKey)) },
                    onUpdateFailed = {},
                    onDeleteLine = { vm.deleteLine(it) },
                    reorderState = templateLineReorderState,
                    reorderIndex = index,
                    reorderItemCount = groups.size,
                    onReorder = { from, to ->
                        val keys = categoryOrder.toMutableList()
                        val moved = keys.removeAt(from)
                        keys.add(to, moved)
                        categoryOrder = keys
                    },
                    onReorderSettled = { vm.reorderCategories(categoryOrder) },
                )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        HdSecondaryButton("Сохранить шаблон", onClick = saveAndLeave)
        Spacer(Modifier.height(8.dp))
        HdSecondaryButton("Удалить шаблон") { showDeleteTemplateConfirm = true }
    }
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp),
        snackbar = { data ->
            Snackbar(
                snackbarData = data,
                containerColor = HdPanelElevated,
                contentColor = HdTextPrimary,
                actionColor = HdOrange,
            )
        },
    )
    }

    if (showDeleteTemplateConfirm && template != null) {
        AlertDialog(
            onDismissRequest = { showDeleteTemplateConfirm = false },
            title = { Text("Удалить шаблон?") },
            text = {
                Text(
                    "«${template.name}» и все работы в нём будут удалены.",
                    color = HdTextSecondary,
                )
            },
            confirmButton = {
                HdPrimaryButton("Удалить") {
                    vm.deleteTemplate { nav.popBackStack() }
                    showDeleteTemplateConfirm = false
                }
            },
            dismissButton = { HdSecondaryButton("Отмена") { showDeleteTemplateConfirm = false } },
        )
    }
}

@Composable
private fun TemplateHubRow(
    template: EstimateTemplateEntity,
    reorderState: HdReorderableState,
    index: Int,
    itemCount: Int,
    onMove: (from: Int, to: Int) -> Unit,
    onDragEnd: () -> Unit,
    onOpenEstimate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .hdReorderableItem(reorderState, index),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .weight(1f)
                .clip(shape)
                .background(HdPanelElevated.copy(alpha = 0.65f))
                .border(1.dp, HdBlue.copy(alpha = 0.35f), shape)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenEstimate),
            ) {
                Text(template.name, color = HdTextPrimary)
                Text("Нажмите — новый расчёт из шаблона", color = HdTextSecondary)
            }
            Text(
                "Изменить",
                color = HdOrange,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable(onClick = onEdit),
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить шаблон", tint = HdTextSecondary)
            }
        }
        ReorderDragHandle(
            state = reorderState,
            index = index,
            itemCount = itemCount,
            onMove = onMove,
            onDragEnd = onDragEnd,
        )
    }
}

private data class PricePickerState(
    val categories: List<CategoryEntity>,
    val servicesByCategory: Map<Long, List<ServiceEntity>>,
    val favorites: List<ServiceEntity>,
)

private fun EstimateEditorUiState.toPricePickerState() = PricePickerState(
    categories = categories,
    servicesByCategory = servicesByCategory,
    favorites = favorites,
)

private fun TemplateEditorUiState.toPricePickerState() = PricePickerState(
    categories = categories,
    servicesByCategory = servicesByCategory,
    favorites = favorites,
)

private fun EstimateLineEntity.toTemplateLine(templateId: Long, categoryKey: Long): EstimateTemplateLineEntity =
    EstimateTemplateLineEntity(
        id = id,
        templateId = templateId,
        categoryKey = categoryKey,
        catalogServiceId = catalogServiceId,
        name = name,
        unit = unit,
        unitPriceKopecks = unitPriceKopecks,
        quantity = quantity,
        sortOrder = sortOrder,
    )

private fun buildTemplateLineGroups(
    state: TemplateEditorUiState,
    categoryOrder: List<Long>,
): List<MainLineGroup> {
    val serviceToCategory = mutableMapOf<Long, Long>()
    state.servicesByCategory.forEach { (catId, services) ->
        services.forEach { serviceToCategory[it.id] = catId }
    }
    state.favorites.forEach { serviceToCategory[it.id] = it.categoryId }
    val grouped = state.lines.groupBy { it.categoryKey }
    val keys = (categoryOrder.filter { grouped.containsKey(it) } +
        grouped.keys.filter { !categoryOrder.contains(it) }).distinct()
    return keys.map { key ->
        val lines = grouped[key]?.sortedBy { it.sortOrder }?.map { templateLine ->
            EstimateLineEntity(
                id = templateLine.id,
                estimateId = 0,
                catalogServiceId = templateLine.catalogServiceId,
                categoryKey = templateLine.categoryKey,
                name = templateLine.name,
                unit = templateLine.unit,
                unitPriceKopecks = templateLine.unitPriceKopecks,
                quantity = templateLine.quantity,
                note = null,
                sortOrder = templateLine.sortOrder,
            )
        } ?: emptyList()
        MainLineGroup(
            categoryKey = key,
            title = categoryTitleFromPrice(state.categories, state.favorites, key),
            lines = lines,
        )
    }
}

private fun categoryTitleFromPrice(
    categories: List<CategoryEntity>,
    favorites: List<ServiceEntity>,
    categoryKey: Long,
): String = when (categoryKey) {
    FAVORITES_PICKER_CATEGORY_ID -> "Избранное"
    EstimateLineCategoryKeys.UNCATEGORIZED -> "Прочее"
    EstimateLineCategoryKeys.ORPHAN_CATALOG -> "Снято с прайса"
    else -> categories.find { it.id == categoryKey }?.name ?: "Категория"
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
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var confirmBulkDelete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val list = if (query.isBlank()) estimates else {
        estimates.filter {
            it.title.contains(query, true) ||
                (it.clientName?.contains(query, true) == true) ||
                (it.clientAddress?.contains(query, true) == true)
        }
    }
    val visibleIds = list.map { it.id }.toSet()
    val allVisibleSelected = visibleIds.isNotEmpty() && visibleIds.all { it in selectedIds }
    LaunchedEffect(estimates) {
        val alive = estimates.map { it.id }.toSet()
        selectedIds = selectedIds.intersect(alive)
        if (estimates.isEmpty()) selectionMode = false
    }
    fun toggleSelected(id: Long) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }
    HdScaffold(title = "История", onBack = { nav.popBackStack() }) {
        HdTextField(query, { query = it }, "Поиск")
        Spacer(Modifier.height(8.dp))
        if (list.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selectionMode) {
                    Text(
                        if (allVisibleSelected) "Снять все" else "Выбрать все",
                        color = HdOrange,
                        modifier = Modifier.clickable {
                            selectedIds = if (allVisibleSelected) {
                                selectedIds - visibleIds
                            } else {
                                selectedIds + visibleIds
                            }
                        },
                    )
                    Text(
                        "Готово",
                        color = HdTextPrimary,
                        modifier = Modifier.clickable {
                            selectionMode = false
                            selectedIds = emptySet()
                        },
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Выбрать",
                        color = HdOrange,
                        modifier = Modifier.clickable { selectionMode = true },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (selectionMode && selectedIds.isNotEmpty()) {
            HdSecondaryButton("Удалить выбранные (${selectedIds.size})") {
                confirmBulkDelete = true
            }
            Spacer(Modifier.height(8.dp))
        }
        list.forEach { item ->
            val selected = item.id in selectedIds
            HdCard(
                Modifier
                    .padding(bottom = 8.dp)
                    .then(
                        if (selectionMode) {
                            Modifier.clickable { toggleSelected(item.id) }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectionMode) {
                        HistorySelectCircle(selected = selected)
                        Spacer(Modifier.width(12.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(item.title, color = HdTextPrimary)
                        Text(item.status.name, color = HdTextSecondary)
                        if (!selectionMode) {
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

    if (confirmBulkDelete && selectedIds.isNotEmpty()) {
        val count = selectedIds.size
        AlertDialog(
            onDismissRequest = { confirmBulkDelete = false },
            title = { Text("Удалить выбранные?") },
            text = {
                Text(
                    "Будет удалено: $count. Восстановить расчёты нельзя.",
                    color = HdTextSecondary,
                )
            },
            confirmButton = {
                HdPrimaryButton("Удалить") {
                    val ids = selectedIds.toList()
                    scope.launch {
                        ids.forEach { app.container.estimateRepository.deleteEstimate(it) }
                        selectedIds = emptySet()
                        selectionMode = false
                        confirmBulkDelete = false
                    }
                }
            },
            dismissButton = {
                HdSecondaryButton("Отмена") { confirmBulkDelete = false }
            },
        )
    }

    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Переименовать") },
            text = {
                HdDialogFieldColumn {
                    HdTextField(renameTitle, { renameTitle = it }, "Название")
                }
            },
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
private fun HistorySelectCircle(selected: Boolean) {
    Box(
        Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (selected) HdOrange else Color.Transparent)
            .border(2.dp, if (selected) HdOrange else HdTextSecondary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Text("✓", color = HdBlack)
        }
    }
}

@Composable
private fun PriceListScreen(nav: NavHostController) {
    val app = LocalContext.current.applicationContext as ElectrosmetaApplication
    val categories by app.container.priceRepository.observeCategories()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val services by app.container.priceRepository.observeAllServices()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val archivedCategories by app.container.priceRepository.observeArchivedCategories()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var showArchivedCategories by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var editingService by remember { mutableStateOf<ServiceEntity?>(null) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var archiveCategoryTarget by remember { mutableStateOf<CategoryEntity?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    var expandedCategoryIds by remember { mutableStateOf(setOf<Long>()) }
    var orderedCategoryIds by remember(categories) { mutableStateOf(categories.map { it.id }) }
    var serviceOrderByCategory by remember { mutableStateOf(mapOf<Long, List<Long>>()) }
    val scope = rememberCoroutineScope()
    val categoryReorderState = rememberHdReorderableState()

    LaunchedEffect(categories) {
        if (categoryReorderState.isDragging) return@LaunchedEffect
        val ids = categories.map { it.id }
        orderedCategoryIds = orderedCategoryIds.filter { ids.contains(it) } + ids.filter { !orderedCategoryIds.contains(it) }
    }
    LaunchedEffect(services, categories) {
        val next = serviceOrderByCategory.toMutableMap()
        categories.forEach { cat ->
            val serviceIds = services.filter { it.categoryId == cat.id }.map { it.id }
            val prev = next[cat.id] ?: emptyList()
            next[cat.id] = prev.filter { serviceIds.contains(it) } + serviceIds.filter { !prev.contains(it) }
        }
        serviceOrderByCategory = next
    }

    HdScaffold(title = "Прайс-лист", onBack = { nav.popBackStack() }) {
        Text(
            "Порядок: удерживайте ⋮⋮ справа и перетаскивайте между пунктами. Нажмите на категорию, чтобы развернуть.",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        HdSecondaryButton("Добавить категорию") { showAddCategory = true }
        Spacer(Modifier.height(8.dp))
        HdSecondaryButton("Сбросить к стартовому прайсу") { confirmReset = true }
        orderedCategoryIds.mapNotNull { id -> categories.find { it.id == id } }.forEachIndexed { catIndex, cat ->
            key(cat.id) {
            val orderedServiceIds = serviceOrderByCategory[cat.id] ?: emptyList()
            val orderedServices = orderedServiceIds.mapNotNull { sid ->
                services.find { it.id == sid && it.categoryId == cat.id }
            }
            val expanded = expandedCategoryIds.contains(cat.id)
            val serviceCount = services.count { it.categoryId == cat.id }
            Spacer(Modifier.height(12.dp))
            val catShape = RoundedCornerShape(14.dp)
            val toggleExpand = {
                expandedCategoryIds = if (expanded) {
                    expandedCategoryIds - cat.id
                } else {
                    expandedCategoryIds + cat.id
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .hdReorderableItem(categoryReorderState, catIndex),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier
                        .weight(1f)
                        .clip(catShape)
                        .background(HdPanelElevated.copy(alpha = 0.65f))
                        .border(1.dp, HdTextSecondary.copy(alpha = 0.35f), catShape)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                Column(
                    Modifier
                        .weight(1f)
                        .clickable(onClick = toggleExpand),
                ) {
                    Text(cat.name, color = HdTextPrimary)
                    Text("$serviceCount поз.", color = HdTextSecondary)
                }
                Text(
                    "Изменить",
                    color = HdOrange,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable { editingCategory = cat },
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Свернуть" else "Развернуть",
                    tint = HdOrange,
                    modifier = Modifier.clickable(onClick = toggleExpand),
                )
                }
                ReorderDragHandle(
                    state = categoryReorderState,
                    index = catIndex,
                    itemCount = orderedCategoryIds.size,
                    onMove = { from, to ->
                        val next = orderedCategoryIds.toMutableList()
                        val movedId = next.removeAt(from)
                        next.add(to, movedId)
                        orderedCategoryIds = next
                    },
                    onDragEnd = {
                        scope.launch { app.container.priceRepository.reorderCategories(orderedCategoryIds) }
                    },
                )
            }
            val serviceReorderState = remember(cat.id) { HdReorderableState() }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(280)) + fadeIn(tween(240)),
                exit = shrinkVertically(animationSpec = tween(220)) + fadeOut(tween(200)),
            ) {
                Column {
                    orderedServices.forEachIndexed { svcIndex, svc ->
                        key(svc.id) {
                        val onServiceMove: (Int, Int) -> Unit = { from, to ->
                            val current = serviceOrderByCategory[cat.id] ?: orderedServiceIds
                            val next = current.toMutableList()
                            val movedId = next.removeAt(from)
                            next.add(to, movedId)
                            serviceOrderByCategory = serviceOrderByCategory + (cat.id to next)
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .hdReorderableItem(serviceReorderState, svcIndex),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            HdCard(Modifier.weight(1f)) {
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
                                Text("  ·  ★", color = if (svc.isFavorite) HdOrange else HdTextSecondary, modifier = Modifier.clickable {
                                    scope.launch { app.container.priceRepository.toggleFavorite(svc.id, !svc.isFavorite) }
                                })
                                Text("  ·  Архив", color = HdTextPrimary, modifier = Modifier.clickable {
                                    scope.launch { app.container.priceRepository.archiveService(svc.id) }
                                })
                            }
                            }
                            ReorderDragHandle(
                                state = serviceReorderState,
                                index = svcIndex,
                                itemCount = orderedServices.size,
                                onMove = onServiceMove,
                                onDragEnd = {
                                    val ids = serviceOrderByCategory[cat.id] ?: return@ReorderDragHandle
                                    scope.launch { app.container.priceRepository.reorderServices(cat.id, ids) }
                                },
                            )
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
                    HdSecondaryButton("Скрыть категорию") {
                        archiveCategoryTarget = cat
                    }
                }
            }
            }
        }
        if (archivedCategories.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            HdSecondaryButton(
                if (showArchivedCategories) {
                    "Свернуть скрытые"
                } else {
                    "Скрытые категории (${archivedCategories.size})"
                },
            ) {
                showArchivedCategories = !showArchivedCategories
            }
            AnimatedVisibility(visible = showArchivedCategories) {
                Column {
                    archivedCategories.forEach { cat ->
                        HdCard(Modifier.padding(top = 8.dp)) {
                            Text(cat.name, color = HdTextPrimary)
                            Text("Не показывается в прайсе и при добавлении услуг", color = HdTextSecondary)
                            HdSecondaryButton("Вернуть в прайс") {
                                scope.launch {
                                    app.container.priceRepository.restoreCategoryWithServices(cat.id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCategory) {
        AlertDialog(
            onDismissRequest = { showAddCategory = false },
            title = { Text("Новая категория") },
            text = {
                HdDialogFieldColumn {
                    HdTextField(newCategoryName, { newCategoryName = it }, "Название")
                }
            },
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
                        "Идентификаторы услуг изменятся — ссылки из шаблонов на старый прайс могут перестать совпадать. " +
                        "Уже созданные сметы (снимки цен в строках) не пересчитаются.",
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
                HdDialogFieldColumn {
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

    editingCategory?.let { cat ->
        var name by remember(cat.id) { mutableStateOf(cat.name) }
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Категория") },
            text = {
                HdDialogFieldColumn {
                    HdTextField(name, { name = it }, "Название")
                }
            },
            confirmButton = {
                HdPrimaryButton("Сохранить") {
                    scope.launch {
                        app.container.priceRepository.upsertCategory(cat.copy(name = name))
                        editingCategory = null
                    }
                }
            },
        )
    }

    archiveCategoryTarget?.let { cat ->
        AlertDialog(
            onDismissRequest = { archiveCategoryTarget = null },
            title = { Text("Скрыть категорию?") },
            text = {
                Text(
                    "Категория «${cat.name}» и её услуги не будут показаны в прайсе и при добавлении из прайса. " +
                        "Расчёты не изменятся.",
                    color = HdTextSecondary,
                )
            },
            confirmButton = {
                HdPrimaryButton("Скрыть") {
                    scope.launch {
                        services.filter { it.categoryId == cat.id }.forEach {
                            app.container.priceRepository.archiveService(it.id)
                        }
                        app.container.priceRepository.archiveCategory(cat.id)
                        archiveCategoryTarget = null
                    }
                }
            },
            dismissButton = { HdSecondaryButton("Отмена") { archiveCategoryTarget = null } },
        )
    }
}

@Composable
private fun SettingsScreen(nav: NavHostController) {
    val app = LocalContext.current.applicationContext as ElectrosmetaApplication
    val settings by app.container.settingsRepository.settings.collectAsStateWithLifecycle(AppSettings())
    val scope = rememberCoroutineScope()
    var pendingImport by remember { mutableStateOf<ByteArray?>(null) }
    var importPreview by remember { mutableStateOf<BackupSummary?>(null) }
    var importMessage by remember { mutableStateOf("") }
    var confirmClear by remember { mutableStateOf(false) }
    var taxLabel by remember(settings) { mutableStateOf(settings.taxLabel) }
    var taxPercent by remember(settings) { mutableStateOf(settings.defaultTaxPercent.toString()) }
    var taxSavePhase by remember { mutableStateOf(HdSavePhase.Idle) }
    var showAppearance by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val bytes = app.container.backupManager.exportBytes()
                app.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                importMessage = "Резервная копия сохранена"
            } catch (e: Exception) {
                importMessage = e.message ?: "Не удалось экспортировать"
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bytes = app.contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
            try {
                importPreview = app.container.backupManager.preview(bytes)
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
            HdSaveButton("Сохранить налог", taxSavePhase) {
                scope.launch {
                    taxSavePhase = HdSavePhase.Saving
                    app.container.settingsRepository.update {
                        it.copy(
                            taxLabel = taxLabel,
                            defaultTaxPercent = taxPercent.replace(',', '.').toDoubleOrNull() ?: 0.0,
                        )
                    }
                    taxSavePhase = HdSavePhase.Saved
                    delay(1400)
                    taxSavePhase = HdSavePhase.Idle
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        HdSecondaryButton("Реквизиты и PDF") { nav.navigate(Routes.ABOUT) }
        Spacer(Modifier.height(8.dp))
        HdSecondaryButton("Конфиденциальность") { nav.navigate(Routes.PRIVACY) }
        Spacer(Modifier.height(16.dp))
        Text(
            "Обновление приложения сохраняет прайс, шаблоны, сметы и настройки. " +
                "Устанавливайте новую версию поверх старой, не удаляя приложение.",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
            "Файл без пароля: прайс, шаблоны, сметы и настройки. Храните его у себя.",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
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
        HdSecondaryButton("Удалить все сметы из истории") { confirmClear = true }
        Spacer(Modifier.height(16.dp))
        HdSecondaryButton(if (showAppearance) "Скрыть оформление" else "Оформление") {
            showAppearance = !showAppearance
        }
        AnimatedVisibility(
            visible = showAppearance,
            enter = expandVertically(tween(220)) + fadeIn(tween(180)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(140)),
        ) {
            Column {
                Spacer(Modifier.height(12.dp))
                AppearancePreview(highlightStyle = settings.categoryHighlightStyle)
                Spacer(Modifier.height(16.dp))
                Text("Подсветка категории в смете", color = HdTextPrimary)
                Text(
                    "Как выделяется категория после добавления позиции или в выборе из прайса.",
                    color = HdTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                listOf(
                    CategoryHighlightStyle.SoftGlow to "Мягкое свечение (синий, спокойно)",
                    CategoryHighlightStyle.PulseBorder to "Пульсирующая рамка",
                    CategoryHighlightStyle.GradientRing to "Градиент синий → оранжевый",
                    CategoryHighlightStyle.Noticeable to "Заметнее (оранжевая рамка)",
                ).forEach { (style, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                scope.launch {
                                    app.container.settingsRepository.update { it.copy(categoryHighlightStyle = style) }
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = settings.categoryHighlightStyle == style,
                            onCheckedChange = {
                                scope.launch {
                                    app.container.settingsRepository.update { it.copy(categoryHighlightStyle = style) }
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = HdBlue),
                        )
                        Text(label, color = HdTextPrimary)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        UpdateCheckSection()
    }

    importPreview?.let { preview ->
        AlertDialog(
            onDismissRequest = { importPreview = null; pendingImport = null },
            title = { Text("Импорт резервной копии") },
            text = {
                Text(
                    "Будет заменено: категорий ${preview.categories}, услуг ${preview.services}, " +
                        "шаблонов ${preview.templates}, расчётов ${preview.estimates}. " +
                        "Старые копии без шаблонов восстановят пустой список шаблонов.",
                    color = HdTextSecondary,
                )
            },
            confirmButton = {
                HdPrimaryButton("Заменить данные") {
                    scope.launch {
                        pendingImport?.let {
                            app.container.backupManager.import(it)
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
            title = { Text("Удалить все сметы?") },
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
private fun AppearancePreview(highlightStyle: CategoryHighlightStyle) {
    val motion = LocalHdMotion.current
    val highlight = categoryHighlightColors(
        active = true,
        style = highlightStyle,
        animationEnabled = true,
        expanded = false,
        pulseStrength = motion.highlightPulseStrength,
    )
    val shape = RoundedCornerShape(12.dp)
    Text("Так выглядит подсветка категории", color = HdTextSecondary, modifier = Modifier.padding(bottom = 8.dp))
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(highlight.background)
            .border(highlight.borderWidth, highlight.borderBrush, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text("Категория", color = highlight.titleColor)
        Text("3 поз.", color = HdTextSecondary)
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
    var settingsSavePhase by remember { mutableStateOf(HdSavePhase.Idle) }

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
        HdSaveButton("Сохранить настройки", settingsSavePhase) {
            scope.launch {
                settingsSavePhase = HdSavePhase.Saving
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
                settingsSavePhase = HdSavePhase.Saved
                delay(1400)
                settingsSavePhase = HdSavePhase.Idle
            }
        }
    }
}

@Composable
private fun UpdateCheckSection() {
    val context = LocalContext.current
    val app = context.applicationContext as ElectrosmetaApplication
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    var pendingUpdate by remember { mutableStateOf<UpdateInfo?>(null) }

    fun downloadUpdate(info: UpdateInfo) {
        scope.launch {
            checking = true
            try {
                val file = withContext(Dispatchers.IO) {
                    val target = File(context.cacheDir, info.apkFileName)
                    ApkDownloadHelper.download(info.apkUrl, target)
                    target
                }
                InstallHelper.promptInstall(context, file)
            } catch (e: Exception) {
                dialogMessage = e.message ?: "Не удалось скачать обновление"
                pendingUpdate = null
            } finally {
                checking = false
            }
        }
    }

    HdSecondaryButton(
        text = if (checking) "Проверка…" else "Проверить обновления",
        enabled = !checking,
    ) {
        scope.launch {
            checking = true
            pendingUpdate = null
            dialogMessage = null
            if (!ReleaseUpdateConfig.isConfigured()) {
                dialogMessage = "Репозиторий обновлений не настроен в сборке приложения"
                checking = false
                return@launch
            }
            try {
                val source = app.container.updateSource(
                    ReleaseUpdateConfig.githubOwner,
                    ReleaseUpdateConfig.githubRepo,
                )
                val info = withContext(Dispatchers.IO) {
                    source.checkForUpdate(BuildConfig.VERSION_CODE)
                }
                if (info == null) {
                    dialogMessage = "У вас установлена последняя версия"
                } else {
                    pendingUpdate = info
                }
            } catch (e: Exception) {
                dialogMessage = e.message ?: "Не удалось проверить обновления"
            } finally {
                checking = false
            }
        }
    }

    pendingUpdate?.let { info ->
        AlertDialog(
            onDismissRequest = { pendingUpdate = null },
            title = { Text("Доступно обновление") },
            text = {
                Text(
                    "Версия ${info.versionName} уже на GitHub. Скачать и установить?",
                    color = HdTextSecondary,
                )
            },
            confirmButton = {
                HdPrimaryButton("Скачать") {
                    pendingUpdate = null
                    downloadUpdate(info)
                }
            },
            dismissButton = {
                HdSecondaryButton("Не скачивать") { pendingUpdate = null }
            },
        )
    }

    dialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { dialogMessage = null },
            title = { Text("Обновление") },
            text = { Text(message, color = HdTextSecondary) },
            confirmButton = {
                HdPrimaryButton("Понятно") { dialogMessage = null }
            },
        )
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
    val motion = LocalHdMotion.current

    var title by remember(estimate?.id) { mutableStateOf(estimate?.title ?: "") }
    var clientName by remember(estimate?.id) { mutableStateOf(estimate?.clientName ?: "") }
    var clientAddress by remember(estimate?.id) { mutableStateOf(estimate?.clientAddress ?: "") }
    var workTitle by remember(estimate?.id) { mutableStateOf(estimate?.workTitle ?: "") }
    var showServicePicker by remember { mutableStateOf(false) }
    var showCustomLineForm by remember { mutableStateOf(false) }
    var expandedMainCategoryKeys by remember { mutableStateOf(setOf<Long>()) }
    var highlightedMainCategoryKey by remember { mutableStateOf<Long?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    var lastLineCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(highlightedMainCategoryKey) {
        if (highlightedMainCategoryKey != null) {
            delay(4000)
            highlightedMainCategoryKey = null
        }
    }

    suspend fun showSnack(message: String) {
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short,
        )
    }
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
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var generatedPdfDisplayName by remember { mutableStateOf("") }
    var generatedPdfSizeLabel by remember { mutableStateOf("") }
    var pdfDownloadsPath by remember { mutableStateOf<String?>(null) }
    var showSaveResultDialog by remember { mutableStateOf(false) }
    var showSaveAsTemplateDialog by remember { mutableStateOf(false) }
    var templateSaveName by remember { mutableStateOf("") }
    var manualSavePhase by remember { mutableStateOf(ManualSavePhase.Idle) }

    suspend fun persistForm(forceDefaults: Boolean): Boolean {
        val discountParsed = discountPct.replace(',', '.').toDoubleOrNull()
        val discountAmountParsed = parseRublesToKopecks(discountAmt)
        val extraFeeParsed = parseRublesToKopecks(extraFee)
        val complexityParsed = complexity.replace(',', '.').toDoubleOrNull()
        if (!forceDefaults) {
            if (discountParsed == null || discountAmountParsed == null ||
                extraFeeParsed == null || complexityParsed == null
            ) {
                return false
            }
        }
        return vm.persistEstimateFields(
            title,
            clientName,
            clientAddress,
            workTitle,
            discountParsed ?: 0.0,
            discountAmountParsed ?: 0L,
            extraFeeParsed ?: 0L,
            complexityParsed ?: 1.0,
            settings.defaultTaxPercent,
            true,
            comment,
        )
    }

    fun saveFields(forceDefaults: Boolean = false) {
        scope.launch { persistForm(forceDefaults) }
    }

    LaunchedEffect(manualSavePhase) {
        if (manualSavePhase == ManualSavePhase.Success) {
            delay(650)
            if (manualSavePhase == ManualSavePhase.Success) {
                manualSavePhase = ManualSavePhase.Idle
            }
        }
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
        saveFields(forceDefaults = false)
    }

    Box(Modifier.fillMaxSize()) {
    HdScaffold(title = estimate?.title ?: "Расчёт", onBack = { nav.popBackStack() }) {
        if (state.loading) {
            Text("Загрузка…", color = HdTextSecondary)
            return@HdScaffold
        }
        HdCard(Modifier.padding(bottom = 12.dp)) {
            Text("Данные расчёта", color = HdTextPrimary)
            Spacer(Modifier.height(8.dp))
            HdTextField(title, { title = it }, "Название расчёта")
            HdTextField(clientName, { clientName = it }, "Имя клиента (необяз.)")
            HdTextField(clientAddress, { clientAddress = it }, "Адрес объекта (необяз.)")
            HdTextField(workTitle, { workTitle = it }, "Название работ")
        }
        val mainLineGroups = remember(state.lines, state.categories, state.servicesByCategory) {
            buildMainLineGroups(state)
        }
        LaunchedEffect(state.lines.size, mainLineGroups.map { it.categoryKey }) {
            if (state.lines.size > lastLineCount) {
                expandedMainCategoryKeys = expandedMainCategoryKeys + mainLineGroups.map { it.categoryKey }
                highlightedMainCategoryKey?.let { key ->
                    expandedMainCategoryKeys = expandedMainCategoryKeys + key
                }
            }
            lastLineCount = state.lines.size
        }
        HdCard(Modifier.padding(bottom = 12.dp)) {
            Text("Услуги", color = HdTextPrimary, modifier = Modifier.padding(bottom = 8.dp))
            mainLineGroups.forEach { group ->
                    MainCategoryLinesSection(
                        group = group,
                        expanded = expandedMainCategoryKeys.contains(group.categoryKey),
                        highlighted = highlightedMainCategoryKey == group.categoryKey,
                        highlightStyle = settings.categoryHighlightStyle,
                        animationEnabled = settings.animationEnabled && !settings.powerSaveMode,
                        onToggleExpand = {
                        expandedMainCategoryKeys = if (expandedMainCategoryKeys.contains(group.categoryKey)) {
                            expandedMainCategoryKeys - group.categoryKey
                        } else {
                            expandedMainCategoryKeys + group.categoryKey
                        }
                    },
                    onUpdateLine = { line -> vm.updateLine(line) },
                    onUpdateFailed = {
                        scope.launch { showSnack("Не удалось обновить — проверьте количество и цену") }
                    },
                    onDeleteLine = { vm.removeLine(it) },
                )
            }
            val customInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            val customPressed by customInteraction.collectIsPressedAsState()
            val customScale by animateFloatAsState(
                if (customPressed) 0.97f else 1f,
                label = "customScale",
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .scale(customScale)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        BorderStroke(1.5.dp, HdOrange.copy(alpha = 0.75f)),
                        RoundedCornerShape(14.dp),
                    )
                    .clickable(
                        interactionSource = customInteraction,
                        indication = ripple(color = HdOrange),
                        onClick = {
                            showCustomLineForm = !showCustomLineForm
                            if (showCustomLineForm) showServicePicker = false
                        },
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (showCustomLineForm) "Скрыть форму" else "Своя позиция",
                    color = HdTextPrimary,
                )
                Icon(Icons.Default.Add, contentDescription = null, tint = HdOrange)
            }
            AnimatedVisibility(
                visible = showCustomLineForm,
                enter = expandVertically(animationSpec = tween(motion.expandDurationMs)) +
                    fadeIn(animationSpec = tween(motion.expandDurationMs)),
                exit = shrinkVertically(animationSpec = tween(motion.expandDurationMs - 40)) +
                    fadeOut(animationSpec = tween(motion.expandDurationMs - 60)),
            ) {
                Column(Modifier.padding(bottom = 12.dp)) {
                    HdTextField(quickName, { quickName = it }, "Название работы")
                    HdUnitChoiceRow(quickUnit, { quickUnit = it })
                    Spacer(Modifier.height(8.dp))
                    HdTextField(quickPrice, { quickPrice = it }, "Цена, ₽")
                    HdTextField(quickQty, { quickQty = it }, "Количество")
                    HdSecondaryButton("Добавить в смету") {
                        val priceOk = quickPrice.replace(',', '.').toDoubleOrNull() != null
                        val qtyOk = quickQty.replace(',', '.').toDoubleOrNull() != null
                        if (quickName.isBlank() || !priceOk || !qtyOk) {
                            scope.launch { showSnack("Заполните название, цену и количество") }
                        } else {
                            vm.addQuickLine(quickName, quickUnit, quickPrice, quickQty)
                            quickName = ""
                            expandedMainCategoryKeys = expandedMainCategoryKeys + EstimateLineCategoryKeys.UNCATEGORIZED
                            highlightedMainCategoryKey = EstimateLineCategoryKeys.UNCATEGORIZED
                            scope.launch { showSnack("✓ Добавлено в «Прочее»") }
                        }
                    }
                }
            }
            val addInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            val addPressed by addInteraction.collectIsPressedAsState()
            val addScale by animateFloatAsState(
                if (addPressed) 0.97f else 1f,
                label = "addScale",
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .scale(addScale)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        BorderStroke(
                            1.5.dp,
                            Brush.horizontalGradient(listOf(HdBlue, HdOrange)),
                        ),
                        RoundedCornerShape(14.dp),
                    )
                    .clickable(
                        interactionSource = addInteraction,
                        indication = ripple(color = HdOrange),
                        onClick = {
                            showServicePicker = !showServicePicker
                            if (showServicePicker) showCustomLineForm = false
                        },
                    )
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
                enter = expandVertically(animationSpec = tween(motion.expandDurationMs)) +
                    fadeIn(animationSpec = tween(motion.expandDurationMs)),
                exit = shrinkVertically(animationSpec = tween(motion.expandDurationMs - 40)) +
                    fadeOut(animationSpec = tween(motion.expandDurationMs - 60)),
            ) {
                MainServicePickerPanel(
                    state = state.toPricePickerState(),
                    highlightStyle = settings.categoryHighlightStyle,
                    animationEnabled = settings.animationEnabled && !settings.powerSaveMode,
                    onAddServices = { services, pickerCategoryKey ->
                        val keys = services.map { it.categoryId }.toSet()
                        val highlight = keys.firstOrNull() ?: pickerCategoryKey
                        expandedMainCategoryKeys = expandedMainCategoryKeys + keys + highlight
                        highlightedMainCategoryKey = highlight
                        showServicePicker = false
                        vm.addServices(services)
                        scope.launch {
                            showSnack(
                                "✓ Добавлено в «${categoryTitle(state, highlight)}»: ${services.size} поз.",
                            )
                        }
                    },
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Итог", color = HdTextPrimary)
        HdTextField(discountPct, { discountPct = it }, "Скидка, %")
        HdTextField(discountAmt, { discountAmt = it }, "Скидка, ₽")
        Text(
            "Скидка в процентах и в рублях суммируются.",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 6.dp),
        )
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

        ManualSaveEstimateButton(
            phase = manualSavePhase,
            onClick = {
                scope.launch {
                    manualSavePhase = ManualSavePhase.Saving
                    val ok = persistForm(forceDefaults = true)
                    if (!ok) {
                        manualSavePhase = ManualSavePhase.Idle
                        showSnack("Проверьте скидки, доплату и коэффициент сложности")
                        return@launch
                    }
                    manualSavePhase = ManualSavePhase.Success
                    delay(320)
                    showSaveResultDialog = true
                }
            },
        )
        Spacer(Modifier.height(8.dp))
        HdSecondaryButton("Сохранить как шаблон") {
            if (state.lines.isEmpty()) {
                scope.launch { showSnack("Добавьте позиции в смету") }
            } else {
                templateSaveName = title
                showSaveAsTemplateDialog = true
            }
        }
        Spacer(Modifier.height(8.dp))
        HdPrimaryButton("Сформировать PDF") { showPdfPreview = true }
    }
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp),
        snackbar = { data ->
            Snackbar(
                snackbarData = data,
                containerColor = HdPanelElevated,
                contentColor = HdTextPrimary,
                actionColor = HdOrange,
            )
        },
    )
    }

    if (showSaveAsTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showSaveAsTemplateDialog = false },
            title = { Text("Сохранить как шаблон") },
            text = {
                HdDialogFieldColumn {
                    HdTextField(templateSaveName, { templateSaveName = it }, "Название шаблона")
                    Text(
                        "Сохранятся все позиции и категории этой сметы.",
                        color = HdTextSecondary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            },
            confirmButton = {
                HdPrimaryButton("Сохранить") {
                    scope.launch {
                        if (!persistForm(forceDefaults = true)) {
                            showSnack("Сначала исправьте поля сметы")
                            return@launch
                        }
                        val id = app.container.templateRepository.createFromEstimate(
                            estimateId,
                            templateSaveName,
                        )
                        if (id == null) {
                            showSnack("Не удалось создать шаблон")
                        } else {
                            showSnack("✓ Шаблон «${templateSaveName.trim()}» сохранён")
                            showSaveAsTemplateDialog = false
                        }
                    }
                }
            },
            dismissButton = { HdSecondaryButton("Отмена") { showSaveAsTemplateDialog = false } },
        )
    }

    if (showSaveResultDialog) {
        AlertDialog(
            onDismissRequest = { showSaveResultDialog = false },
            title = { Text("Сохранено на устройстве") },
            text = {
                Column {
                    Text(
                        "Расчёт «${title.ifBlank { "Без названия" }}» записан в локальную базу приложения.",
                        color = HdTextPrimary,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Где найти", color = HdTextPrimary)
                    Text(
                        "Главный экран → «История». Там список всех смет на этом телефоне.",
                        color = HdTextSecondary,
                    )
                    Text(
                        "В интернет данные не отправляются автоматически.",
                        color = HdTextSecondary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            },
            confirmButton = {
                HdPrimaryButton("Открыть «История»") {
                    showSaveResultDialog = false
                    nav.navigate(Routes.HISTORY)
                }
            },
            dismissButton = {
                HdSecondaryButton("Остаться в расчёте") { showSaveResultDialog = false }
            },
        )
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
                        if (!persistForm(forceDefaults = true)) return@launch
                        val data = vm.getWithLines() ?: return@launch
                        val calc = vm.state.value.calculation ?: return@launch
                        val displayName = PdfStorageHelper.buildDisplayName(data.estimate.title)
                        val file = File(context.cacheDir, "export_${estimateId}_${System.currentTimeMillis()}.pdf")
                        withContext(Dispatchers.IO) {
                            app.container.pdfGenerator.generate(data, settings, calc, file)
                        }
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file,
                        )
                        generatedPdfUri = uri
                        generatedPdfFile = file
                        generatedPdfDisplayName = displayName
                        pdfDownloadsPath = null
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
                Column {
                    Text(
                        "Размер: $generatedPdfSizeLabel. Логотип HDLabs в PDF не добавляется.",
                        color = HdTextSecondary,
                    )
                    pdfDownloadsPath?.let { path ->
                        Text(
                            "Сохранено на телефоне: $path",
                            color = HdBlue,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
            },
            confirmButton = {
                Column(Modifier.fillMaxWidth()) {
                    HdPrimaryButton("Открыть") {
                        if (uri != null) {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    HdSecondaryButton("Сохранить на телефон") {
                        scope.launch {
                            val file = generatedPdfFile
                            if (file == null) {
                                showSnack("Файл PDF не найден — создайте PDF заново")
                                return@launch
                            }
                            val result = withContext(Dispatchers.IO) {
                                PdfStorageHelper.saveToDownloads(
                                    context,
                                    file,
                                    generatedPdfDisplayName,
                                )
                            }
                            result.fold(
                                onSuccess = { path ->
                                    pdfDownloadsPath = path
                                    showSnack("✓ PDF: $path")
                                },
                                onFailure = {
                                    showSnack("Не удалось сохранить: ${it.message ?: "ошибка"}")
                                },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    HdSecondaryButton("Отправить") {
                        if (uri != null) {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Отправить смету"))
                        }
                    }
                }
            },
            dismissButton = {
                HdSecondaryButton("Закрыть") { showPdfReady = false }
            },
        )
    }
}

private enum class ManualSavePhase { Idle, Saving, Success }

@Composable
private fun ManualSaveEstimateButton(
    phase: ManualSavePhase,
    onClick: () -> Unit,
) {
    val motion = LocalHdMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when (phase) {
            ManualSavePhase.Success -> 1.06f
            ManualSavePhase.Saving -> motion.buttonPressScale
            else -> if (pressed) motion.buttonPressScale else 1f
        },
        animationSpec = if (phase == ManualSavePhase.Success) {
            spring(dampingRatio = 0.55f, stiffness = 380f)
        } else {
            tween(motion.buttonPressDurationMs)
        },
        label = "manualSaveScale",
    )
    val label = when (phase) {
        ManualSavePhase.Saving -> "Сохранение…"
        ManualSavePhase.Success -> "Сохранено"
        ManualSavePhase.Idle -> "Сохранить сейчас"
    }
    val containerColor = when (phase) {
        ManualSavePhase.Success -> HdBlue.copy(alpha = 0.35f)
        ManualSavePhase.Saving -> HdPanel.copy(alpha = 0.85f)
        else -> if (pressed) HdOrange.copy(alpha = 0.35f) else HdPanel
    }
    val border = when (phase) {
        ManualSavePhase.Success -> BorderStroke(2.5.dp, HdBlue)
        ManualSavePhase.Saving -> BorderStroke(1.dp, HdTextSecondary.copy(alpha = 0.35f))
        else -> BorderStroke(
            if (pressed) 2.5.dp else 1.dp,
            if (pressed) HdOrange else HdBlue.copy(alpha = 0.4f),
        )
    }
    Button(
        onClick = onClick,
        enabled = phase != ManualSavePhase.Saving,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = HdPanel,
        ),
        border = border,
    ) {
        if (phase == ManualSavePhase.Success) {
            Icon(Icons.Default.Check, contentDescription = null, tint = HdBlue, modifier = Modifier.padding(end = 8.dp))
        }
        Text(label, color = HdTextPrimary)
    }
}

private const val FAVORITES_PICKER_CATEGORY_ID = -1L

private fun lineCategoryKey(line: EstimateLineEntity, serviceToCategory: Map<Long, Long>): Long {
    line.categoryKey?.let { return it }
    val catalogId = line.catalogServiceId
    if (catalogId != null) {
        return serviceToCategory[catalogId] ?: EstimateLineCategoryKeys.ORPHAN_CATALOG
    }
    return EstimateLineCategoryKeys.UNCATEGORIZED
}

private data class MainLineGroup(
    val categoryKey: Long,
    val title: String,
    val lines: List<EstimateLineEntity>,
)

private fun buildMainLineGroups(state: EstimateEditorUiState): List<MainLineGroup> {
    val serviceToCategory = mutableMapOf<Long, Long>()
    state.servicesByCategory.forEach { (catId, services) ->
        services.forEach { serviceToCategory[it.id] = catId }
    }
    state.favorites.forEach { serviceToCategory[it.id] = it.categoryId }
    val grouped = state.lines.groupBy { line -> lineCategoryKey(line, serviceToCategory) }
    return grouped.map { (key, lines) ->
        MainLineGroup(
            categoryKey = key,
            title = categoryTitle(state, key),
            lines = lines.sortedBy { it.sortOrder },
        )
    }.sortedBy { it.lines.minOfOrNull { it.sortOrder } ?: 0 }
}

private fun categoryTitle(state: EstimateEditorUiState, categoryKey: Long): String = when (categoryKey) {
    FAVORITES_PICKER_CATEGORY_ID -> "Избранное"
    EstimateLineCategoryKeys.UNCATEGORIZED -> "Прочее"
    EstimateLineCategoryKeys.ORPHAN_CATALOG -> "Снято с прайса"
    else -> state.categories.find { it.id == categoryKey }?.name ?: "Категория"
}

private enum class ServicePickerStep { Categories, Services, Confirming }

@Composable
private fun MainCategoryLinesSection(
    group: MainLineGroup,
    expanded: Boolean,
    highlighted: Boolean,
    highlightStyle: CategoryHighlightStyle,
    animationEnabled: Boolean,
    onToggleExpand: () -> Unit,
    onUpdateLine: (EstimateLineEntity) -> Unit,
    onUpdateFailed: () -> Unit,
    onDeleteLine: (Long) -> Unit,
    headerModifier: Modifier = Modifier,
    reorderState: HdReorderableState? = null,
    reorderIndex: Int = 0,
    reorderItemCount: Int = 0,
    onReorder: ((from: Int, to: Int) -> Unit)? = null,
    onReorderSettled: (() -> Unit)? = null,
) {
    val motion = LocalHdMotion.current
    val shape = RoundedCornerShape(12.dp)
    val highlightColors = categoryHighlightColors(
        active = highlighted,
        style = highlightStyle,
        animationEnabled = animationEnabled,
        expanded = expanded && !highlighted,
        pulseStrength = motion.highlightPulseStrength,
    )
    val headerInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val headerPressed by headerInteraction.collectIsPressedAsState()
    val headerScale by animateFloatAsState(if (headerPressed) 0.98f else 1f, label = "catHeaderScale")
    val reorderModifier = if (reorderState != null && onReorder != null) {
        Modifier.hdReorderableItem(reorderState, reorderIndex)
    } else {
        Modifier
    }
    Column(Modifier.padding(bottom = 10.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .then(reorderModifier),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .weight(1f)
                    .then(headerModifier)
                    .scale(headerScale)
                    .clip(shape)
                    .background(highlightColors.background)
                    .border(
                        width = highlightColors.borderWidth,
                        brush = highlightColors.borderBrush,
                        shape = shape,
                    )
                    .clickable(
                        interactionSource = headerInteraction,
                        indication = ripple(color = HdOrange),
                        onClick = onToggleExpand,
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(group.title, color = highlightColors.titleColor)
                    Text("${group.lines.size} поз.", color = HdTextSecondary)
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Свернуть" else "Развернуть",
                    tint = HdOrange.copy(alpha = 0.85f),
                )
            }
            if (reorderState != null && onReorder != null) {
                ReorderDragHandle(
                    state = reorderState,
                    index = reorderIndex,
                    itemCount = reorderItemCount,
                    onMove = onReorder,
                    onDragEnd = { onReorderSettled?.invoke() },
                )
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(motion.expandDurationMs)) +
                fadeIn(tween(motion.expandDurationMs)),
            exit = shrinkVertically(animationSpec = tween(motion.expandDurationMs - 40)) +
                fadeOut(tween(motion.expandDurationMs - 60)),
        ) {
            Column(Modifier.padding(top = 6.dp)) {
                group.lines.forEach { line ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(motion.lineEnterMs)) +
                            expandVertically(tween(motion.lineEnterMs)),
                    ) {
                        LineEditor(
                            line = line,
                            allowUnitChoice = line.catalogServiceId == null,
                            onUpdate = onUpdateLine,
                            onUpdateFailed = onUpdateFailed,
                            onDelete = { onDeleteLine(line.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainServicePickerPanel(
    state: PricePickerState,
    highlightStyle: CategoryHighlightStyle,
    animationEnabled: Boolean,
    onAddServices: (List<ServiceEntity>, Long) -> Unit,
) {
    val motion = LocalHdMotion.current
    val pickerSlideMs = (motion.expandDurationMs * 1.2f).toInt()
    var step by remember { mutableStateOf(ServicePickerStep.Categories) }
    var activeCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedServiceIds by remember { mutableStateOf(setOf<Long>()) }
    var pendingConfirm by remember { mutableStateOf<Pair<List<ServiceEntity>, Long>?>(null) }

    LaunchedEffect(step, pendingConfirm) {
        if (step != ServicePickerStep.Confirming || pendingConfirm == null) return@LaunchedEffect
        delay(motion.lineEnterMs.coerceAtLeast(480).toLong())
        val (picked, key) = pendingConfirm!!
        onAddServices(picked, key)
        pendingConfirm = null
        selectedServiceIds = emptySet()
        activeCategoryId = null
        step = ServicePickerStep.Categories
    }

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
                when {
                    targetState == ServicePickerStep.Confirming ->
                        fadeIn(tween(pickerSlideMs)) togetherWith fadeOut(tween(pickerSlideMs / 2))
                    initialState == ServicePickerStep.Confirming ->
                        fadeIn(tween(pickerSlideMs / 2)) togetherWith fadeOut(tween(pickerSlideMs))
                    targetState == ServicePickerStep.Services ->
                        slideInHorizontally(animationSpec = tween(pickerSlideMs)) { it / 4 } +
                            fadeIn(tween(pickerSlideMs)) togetherWith
                            slideOutHorizontally(animationSpec = tween(pickerSlideMs)) { -it / 4 } +
                            fadeOut(tween(pickerSlideMs - 40))
                    else ->
                        slideInHorizontally(animationSpec = tween(pickerSlideMs)) { -it / 4 } +
                            fadeIn(tween(pickerSlideMs)) togetherWith
                            slideOutHorizontally(animationSpec = tween(pickerSlideMs)) { it / 4 } +
                            fadeOut(tween(pickerSlideMs - 40))
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
                                highlightStyle = highlightStyle,
                                animationEnabled = animationEnabled,
                                onClick = {
                                    activeCategoryId = FAVORITES_PICKER_CATEGORY_ID
                                    selectedServiceIds = emptySet()
                                    step = ServicePickerStep.Services
                                },
                            )
                        }
                        state.categories.forEach { cat ->
                            val count = state.servicesByCategory[cat.id]?.size ?: 0
                            val label = if (count > 0) "${cat.name} ($count)" else cat.name
                            ServiceCategoryChip(
                                name = label,
                                selected = activeCategoryId == cat.id,
                                highlightStyle = highlightStyle,
                                animationEnabled = animationEnabled,
                                onClick = {
                                    activeCategoryId = cat.id
                                    selectedServiceIds = emptySet()
                                    step = ServicePickerStep.Services
                                },
                            )
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
                            highlightStyle = highlightStyle,
                            animationEnabled = animationEnabled,
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
                            enabled = selectedServiceIds.isNotEmpty() && step != ServicePickerStep.Confirming,
                        ) {
                            val picked = servicesInCategory.filter { selectedServiceIds.contains(it.id) }
                            val key = activeCategoryId ?: EstimateLineCategoryKeys.UNCATEGORIZED
                            pendingConfirm = Pair(picked, key)
                            step = ServicePickerStep.Confirming
                        }
                    }
                }
                ServicePickerStep.Confirming -> {
                    val count = pendingConfirm?.first?.size ?: selectedServiceIds.size
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = HdOrange,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Добавлено в «$activeCategoryLabel»",
                            color = HdTextPrimary,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            "$count поз. — возврат к составу…",
                            color = HdTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        Spacer(Modifier.height(14.dp))
                        ServiceCategoryChip(
                            name = activeCategoryLabel,
                            selected = true,
                            highlightStyle = highlightStyle,
                            animationEnabled = animationEnabled,
                            onClick = {},
                        )
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
    highlightStyle: CategoryHighlightStyle,
    animationEnabled: Boolean,
    onClick: () -> Unit,
) {
    val motion = LocalHdMotion.current
    val shape = RoundedCornerShape(12.dp)
    val colors = categoryHighlightColors(
        active = selected,
        style = highlightStyle,
        animationEnabled = animationEnabled,
        pulseStrength = motion.highlightPulseStrength,
    )
    val chipInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val chipPressed by chipInteraction.collectIsPressedAsState()
    val chipScale by animateFloatAsState(if (chipPressed) 0.97f else 1f, label = "chipScale")
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(chipScale)
            .clip(shape)
            .background(colors.background)
            .border(
                width = colors.borderWidth,
                brush = colors.borderBrush,
                shape = shape,
            )
            .clickable(
                interactionSource = chipInteraction,
                indication = ripple(color = HdOrange),
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name, color = colors.titleColor)
    }
}

@Composable
private fun LineEditor(
    line: EstimateLineEntity,
    allowUnitChoice: Boolean,
    onUpdate: (EstimateLineEntity) -> Unit,
    onUpdateFailed: () -> Unit,
    onDelete: () -> Unit,
) {
    var qty by remember(line.id) { mutableStateOf(line.quantity.toString()) }
    var price by remember(line.id) { mutableStateOf((line.unitPriceKopecks / 100.0).toString()) }
    var note by remember(line.id) { mutableStateOf(line.note ?: "") }
    var unit by remember(line.id) { mutableStateOf(line.unit) }
    val unitFromQuickList = unit in HdQuickUnits
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
            if (unitFromQuickList) {
                HdUnitChoiceRow(unit, { unit = it })
            } else {
                Text("Единица: $unit", color = HdTextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                HdTextField(unit, { unit = it }, "Единица")
            }
        } else {
            Text(line.unit, color = HdTextSecondary)
        }
        HdTextField(qty, { qty = it }, "Кол-во")
        HdTextField(price, { price = it }, "Цена, ₽")
        HdTextField(note, { note = it }, "Примечание")
        Text(
            "Количество и цена сохраняются автоматически",
            color = HdTextSecondary,
            modifier = Modifier.padding(top = 4.dp),
        )
    }

    LaunchedEffect(qty, price, note, unit, line.id) {
        delay(750)
        val q = qty.replace(',', '.').toDoubleOrNull()
        val p = parseRublesToKopecks(price)
        if (q == null || q <= 0.0 || p == null) return@LaunchedEffect
        val u = if (allowUnitChoice) unit else line.unit
        val updated = line.copy(
            quantity = q,
            unitPriceKopecks = p,
            unit = u,
            note = note.ifBlank { null },
        )
        if (updated.quantity == line.quantity &&
            updated.unitPriceKopecks == line.unitPriceKopecks &&
            updated.unit == line.unit &&
            updated.note == line.note
        ) {
            return@LaunchedEffect
        }
        onUpdate(updated)
    }
}
