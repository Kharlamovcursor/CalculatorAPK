package ru.hdlabs.electrosmeta.navigation

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import ru.hdlabs.electrosmeta.feature.template.TemplateEditorUiState
import ru.hdlabs.electrosmeta.feature.template.TemplateEditorViewModel
import ru.hdlabs.electrosmeta.data.local.entities.EstimateTemplateLineEntity
import ru.hdlabs.electrosmeta.ui.components.reorderOnLongPressDrag
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

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
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
                        .fillMaxWidth(0.58f)
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
private fun EstimateHubScreen(nav: NavHostController) {
    val app = LocalContext.current.applicationContext as ElectrosmetaApplication
    val scope = rememberCoroutineScope()
    val templates by app.container.templateRepository.observeTemplates()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var orderedIds by remember(templates) { mutableStateOf(templates.map { it.id }) }
    LaunchedEffect(templates) {
        val ids = templates.map { it.id }
        orderedIds = (orderedIds.filter { ids.contains(it) } + ids.filter { !orderedIds.contains(it) })
    }
    val orderedTemplates = orderedIds.mapNotNull { id -> templates.find { it.id == id } }

    HdScaffold(title = "Расчёт сметы", onBack = { nav.popBackStack() }, scroll = true) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
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
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )
            Spacer(Modifier.height(12.dp))
            orderedTemplates.forEachIndexed { index, template ->
                HdSecondaryButton(
                    text = template.name,
                    modifier = Modifier.reorderOnLongPressDrag(
                        enabled = true,
                        onMoveUp = {
                            if (index > 0) {
                                val next = orderedIds.toMutableList()
                                next[index - 1] = next[index].also { next[index] = next[index - 1] }
                                orderedIds = next
                                scope.launch { app.container.templateRepository.reorderTemplates(next) }
                            }
                        },
                        onMoveDown = {
                            if (index < orderedIds.lastIndex) {
                                val next = orderedIds.toMutableList()
                                next[index + 1] = next[index].also { next[index] = next[index + 1] }
                                orderedIds = next
                                scope.launch { app.container.templateRepository.reorderTemplates(next) }
                            }
                        },
                    ),
                    onClick = {
                        scope.launch {
                            val tax = app.container.settingsRepository.settings.first().defaultTaxPercent
                            val estimateId = app.container.templateRepository.createEstimateFromTemplate(template.id, tax)
                            if (estimateId != null) {
                                nav.navigate(Routes.estimate(estimateId))
                            }
                        }
                    },
                )
                Spacer(Modifier.height(12.dp))
            }
            HdSecondaryButton("Добавить шаблон") {
                scope.launch {
                    val id = app.container.templateRepository.createTemplate("Шаблон", EstimateType.MAIN)
                    nav.navigate(Routes.templateEdit(id))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
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
    val template = state.template
    var title by remember(template?.id) { mutableStateOf(template?.name ?: "") }
    var showServicePicker by remember { mutableStateOf(false) }
    var expandedKeys by remember { mutableStateOf(setOf<Long>()) }
    var categoryOrder by remember(state.categoryOrder) { mutableStateOf(state.categoryOrder) }
    LaunchedEffect(state.categoryOrder) { categoryOrder = state.categoryOrder }

    HdScaffold(title = "Шаблон", onBack = { nav.popBackStack() }) {
        if (state.loading || template == null) {
            Text("Загрузка…", color = HdTextSecondary)
            return@HdScaffold
        }
        HdTextField(title, { title = it }, "Название шаблона")
        HdSecondaryButton("Сохранить название") { vm.rename(title) }
        Spacer(Modifier.height(12.dp))
        Text("Категории и работы", color = HdTextPrimary)
        val groups = buildTemplateLineGroups(state, categoryOrder)
        groups.forEachIndexed { index, group ->
            MainCategoryLinesSection(
                group = group,
                expanded = expandedKeys.contains(group.categoryKey),
                highlighted = false,
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
                headerModifier = Modifier.reorderOnLongPressDrag(
                    enabled = true,
                    onMoveUp = {
                        val keys = categoryOrder.toMutableList()
                        val pos = keys.indexOf(group.categoryKey)
                        if (pos > 0) {
                            keys[pos - 1] = keys[pos].also { keys[pos] = keys[pos - 1] }
                            categoryOrder = keys
                            vm.reorderCategories(keys)
                        }
                    },
                    onMoveDown = {
                        val keys = categoryOrder.toMutableList()
                        val pos = keys.indexOf(group.categoryKey)
                        if (pos >= 0 && pos < keys.lastIndex) {
                            keys[pos + 1] = keys[pos].also { keys[pos] = keys[pos + 1] }
                            categoryOrder = keys
                            vm.reorderCategories(keys)
                        }
                    },
                ),
            )
        }
        HdSecondaryButton(if (showServicePicker) "Скрыть прайс" else "Добавить из прайса") {
            showServicePicker = !showServicePicker
        }
        AnimatedVisibility(visible = showServicePicker) {
            MainServicePickerPanel(
                state = state.toPricePickerState(),
                onAddServices = { services, key ->
                    vm.addServices(services, key)
                    expandedKeys = expandedKeys + key
                    showServicePicker = false
                },
            )
        }
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
    UNCATEGORIZED_LINES_KEY -> "Прочее"
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
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var archiveCategoryTarget by remember { mutableStateOf<CategoryEntity?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    var expandedCategoryIds by remember { mutableStateOf(setOf<Long>()) }
    var orderedCategoryIds by remember(categories) { mutableStateOf(categories.map { it.id }) }
    var serviceOrderByCategory by remember { mutableStateOf(mapOf<Long, List<Long>>()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(categories) {
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
            "Удерживайте категорию или услугу и перетащите для смены порядка. Нажмите на категорию, чтобы развернуть.",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        HdSecondaryButton("Добавить категорию") { showAddCategory = true }
        Spacer(Modifier.height(8.dp))
        HdSecondaryButton("Сбросить к стартовому прайсу") { confirmReset = true }
        orderedCategoryIds.mapNotNull { id -> categories.find { it.id == id } }.forEachIndexed { catIndex, cat ->
            val orderedServiceIds = serviceOrderByCategory[cat.id] ?: emptyList()
            val orderedServices = orderedServiceIds.mapNotNull { sid ->
                services.find { it.id == sid && it.categoryId == cat.id }
            }
            val expanded = expandedCategoryIds.contains(cat.id)
            val serviceCount = services.count { it.categoryId == cat.id }
            Spacer(Modifier.height(12.dp))
            val catShape = RoundedCornerShape(14.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .reorderOnLongPressDrag(
                        enabled = true,
                        onMoveUp = {
                            if (catIndex > 0) {
                                val next = orderedCategoryIds.toMutableList()
                                next[catIndex - 1] = next[catIndex].also { next[catIndex] = next[catIndex - 1] }
                                orderedCategoryIds = next
                                scope.launch { app.container.priceRepository.reorderCategories(next) }
                            }
                        },
                        onMoveDown = {
                            if (catIndex < orderedCategoryIds.lastIndex) {
                                val next = orderedCategoryIds.toMutableList()
                                next[catIndex + 1] = next[catIndex].also { next[catIndex] = next[catIndex + 1] }
                                orderedCategoryIds = next
                                scope.launch { app.container.priceRepository.reorderCategories(next) }
                            }
                        },
                    )
                    .clip(catShape)
                    .background(HdPanelElevated.copy(alpha = 0.65f))
                    .border(1.dp, HdTextSecondary.copy(alpha = 0.35f), catShape)
                    .clickable {
                        expandedCategoryIds = if (expanded) {
                            expandedCategoryIds - cat.id
                        } else {
                            expandedCategoryIds + cat.id
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(cat.name, color = HdTextPrimary)
                    Text("$serviceCount поз.", color = HdTextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(280)) + fadeIn(tween(240)),
                exit = shrinkVertically(animationSpec = tween(220)) + fadeOut(tween(200)),
            ) {
                Column {
                    orderedServices.forEachIndexed { svcIndex, svc ->
                        HdCard(
                            Modifier
                                .padding(top = 6.dp)
                                .reorderOnLongPressDrag(
                                    enabled = true,
                                    onMoveUp = {
                                        if (svcIndex > 0) {
                                            val next = orderedServiceIds.toMutableList()
                                            next[svcIndex - 1] = next[svcIndex].also { next[svcIndex] = next[svcIndex - 1] }
                                            serviceOrderByCategory = serviceOrderByCategory + (cat.id to next)
                                            scope.launch {
                                                app.container.priceRepository.reorderServices(cat.id, next)
                                            }
                                        }
                                    },
                                    onMoveDown = {
                                        if (svcIndex < orderedServiceIds.lastIndex) {
                                            val next = orderedServiceIds.toMutableList()
                                            next[svcIndex + 1] = next[svcIndex].also { next[svcIndex] = next[svcIndex + 1] }
                                            serviceOrderByCategory = serviceOrderByCategory + (cat.id to next)
                                            scope.launch {
                                                app.container.priceRepository.reorderServices(cat.id, next)
                                            }
                                        }
                                    },
                                ),
                        ) {
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

    editingCategory?.let { cat ->
        var name by remember(cat.id) { mutableStateOf(cat.name) }
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Категория") },
            text = { HdTextField(name, { name = it }, "Название") },
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
        Text(
            "Обновление приложения сохраняет прайс, шаблоны, сметы и настройки. " +
                "Устанавливайте новую версию поверх старой, не удаляя приложение.",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
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
        Spacer(Modifier.height(24.dp))
        UpdateCheckSection()
    }

    importPreview?.let { preview ->
        AlertDialog(
            onDismissRequest = { importPreview = null; pendingImport = null },
            title = { Text("Импорт резервной копии") },
            text = {
                Text(
                    "Будет заменено: категорий ${preview.categories}, услуг ${preview.services}, " +
                        "шаблонов ${preview.templates}, расчётов ${preview.estimates}.",
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
                    onToggleExpand = {
                        expandedMainCategoryKeys = if (expandedMainCategoryKeys.contains(group.categoryKey)) {
                            expandedMainCategoryKeys - group.categoryKey
                        } else {
                            expandedMainCategoryKeys + group.categoryKey
                        }
                    },
                    onUpdateLine = { line ->
                        vm.updateLine(line)
                        scope.launch { showSnack("✓ Позиция обновлена") }
                    },
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
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(260)),
                exit = shrinkVertically(animationSpec = tween(240)) + fadeOut(animationSpec = tween(200)),
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
                            expandedMainCategoryKeys = expandedMainCategoryKeys + UNCATEGORIZED_LINES_KEY
                            highlightedMainCategoryKey = UNCATEGORIZED_LINES_KEY
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
                enter = expandVertically(animationSpec = tween(320)) + fadeIn(animationSpec = tween(280)),
                exit = shrinkVertically(animationSpec = tween(260)) + fadeOut(animationSpec = tween(220)),
            ) {
                MainServicePickerPanel(
                    state = state.toPricePickerState(),
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
private const val UNCATEGORIZED_LINES_KEY = -2L

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
    val grouped = state.lines.groupBy { line ->
        line.catalogServiceId?.let { serviceToCategory[it] } ?: UNCATEGORIZED_LINES_KEY
    }
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
    UNCATEGORIZED_LINES_KEY -> "Прочее"
    else -> state.categories.find { it.id == categoryKey }?.name ?: "Категория"
}

private enum class ServicePickerStep { Categories, Services }

@Composable
private fun MainCategoryLinesSection(
    group: MainLineGroup,
    expanded: Boolean,
    highlighted: Boolean,
    onToggleExpand: () -> Unit,
    onUpdateLine: (EstimateLineEntity) -> Unit,
    onUpdateFailed: () -> Unit,
    onDeleteLine: (Long) -> Unit,
    headerModifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val headerBg = when {
        highlighted -> HdOrange.copy(alpha = 0.45f)
        expanded -> HdOrange.copy(alpha = 0.22f)
        else -> HdPanel.copy(alpha = 0.6f)
    }
    val headerInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val headerPressed by headerInteraction.collectIsPressedAsState()
    val headerScale by animateFloatAsState(if (headerPressed) 0.98f else 1f, label = "catHeaderScale")
    Column(Modifier.padding(bottom = 10.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .then(headerModifier)
                .scale(headerScale)
                .clip(shape)
                .background(headerBg)
                .border(
                    width = if (highlighted) 2.5.dp else if (expanded) 1.5.dp else 1.dp,
                    color = when {
                        highlighted -> HdOrange
                        expanded -> HdOrange.copy(alpha = 0.85f)
                        else -> HdTextSecondary.copy(alpha = 0.35f)
                    },
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
                Text(group.title, color = if (highlighted) HdOrange else HdTextPrimary)
                Text("${group.lines.size} поз.", color = HdTextSecondary)
            }
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Свернуть" else "Развернуть",
                tint = HdOrange,
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(280)) + fadeIn(tween(240)),
            exit = shrinkVertically(animationSpec = tween(220)) + fadeOut(tween(200)),
        ) {
            Column(Modifier.padding(top = 6.dp)) {
                group.lines.forEach { line ->
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

@Composable
private fun MainServicePickerPanel(
    state: PricePickerState,
    onAddServices: (List<ServiceEntity>, Long) -> Unit,
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
                            val label = if (count > 0) "${cat.name} ($count)" else cat.name
                            ServiceCategoryChip(
                                name = label,
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
                            val key = activeCategoryId ?: UNCATEGORIZED_LINES_KEY
                            onAddServices(picked, key)
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
    val bg = if (selected) HdOrange.copy(alpha = 0.28f) else HdPanel.copy(alpha = 0.5f)
    val chipInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val chipPressed by chipInteraction.collectIsPressedAsState()
    val chipScale by animateFloatAsState(if (chipPressed) 0.97f else 1f, label = "chipScale")
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(chipScale)
            .clip(shape)
            .background(bg)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) HdOrange else HdTextSecondary.copy(alpha = 0.35f),
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
    onUpdateFailed: () -> Unit,
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
        Spacer(Modifier.height(6.dp))
        HdSecondaryButton("Обновить позицию") {
            val q = qty.replace(',', '.').toDoubleOrNull()
            val p = parseRublesToKopecks(price)
            if (q == null || q <= 0.0 || p == null) {
                onUpdateFailed()
            } else {
                val u = if (allowUnitChoice) unit else line.unit
                onUpdate(line.copy(quantity = q, unitPriceKopecks = p, unit = u, note = note.ifBlank { null }))
            }
        }
    }
}
