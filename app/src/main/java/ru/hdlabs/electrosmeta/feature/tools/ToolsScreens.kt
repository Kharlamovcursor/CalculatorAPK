package ru.hdlabs.electrosmeta.feature.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ru.hdlabs.electrosmeta.domain.electrical.BreakerInput
import ru.hdlabs.electrosmeta.domain.electrical.CableInstallMode
import ru.hdlabs.electrosmeta.domain.electrical.CableOrderInput
import ru.hdlabs.electrosmeta.domain.electrical.CableSectionInput
import ru.hdlabs.electrosmeta.domain.electrical.ConductorMaterial
import ru.hdlabs.electrosmeta.domain.electrical.ElectricalCalculators
import ru.hdlabs.electrosmeta.domain.electrical.LedDcFeederInput
import ru.hdlabs.electrosmeta.domain.electrical.LedStripInput
import ru.hdlabs.electrosmeta.domain.electrical.LightingPowerInput
import ru.hdlabs.electrosmeta.domain.electrical.PhaseMode
import ru.hdlabs.electrosmeta.domain.electrical.RcdHintInput
import ru.hdlabs.electrosmeta.domain.electrical.RcdLocationHint
import ru.hdlabs.electrosmeta.domain.electrical.VoltageDropInput
import ru.hdlabs.electrosmeta.domain.electrical.parseElectricalDouble
import ru.hdlabs.electrosmeta.ui.background.SpaceBackground
import ru.hdlabs.electrosmeta.ui.components.HdCard
import ru.hdlabs.electrosmeta.ui.components.HdPrimaryButton
import ru.hdlabs.electrosmeta.ui.components.HdTextField
import ru.hdlabs.electrosmeta.ui.motion.LocalHdMotion
import ru.hdlabs.electrosmeta.ui.theme.HdBlack
import ru.hdlabs.electrosmeta.ui.theme.HdBlue
import ru.hdlabs.electrosmeta.ui.theme.HdOrange
import ru.hdlabs.electrosmeta.ui.theme.HdPanel
import ru.hdlabs.electrosmeta.ui.theme.HdTextPrimary
import ru.hdlabs.electrosmeta.ui.theme.HdTextSecondary

object ToolRoutes {
    const val CABLE = "tool/cable"
    const val LED = "tool/led"
    const val BREAKER = "tool/breaker"
    const val VOLTAGE_DROP = "tool/voltage_drop"
    const val LIGHTING = "tool/lighting"
    const val RCD = "tool/rcd"
    const val CABLE_ORDER = "tool/cable_order"

    const val HOME_TAB_KEY = "homeTab"
    const val HOME_TAB_TOOLS = "Tools"

    fun navigateToTool(nav: NavHostController, route: String, homeRoute: String) {
        nav.getBackStackEntry(homeRoute).savedStateHandle[HOME_TAB_KEY] = HOME_TAB_TOOLS
        nav.navigate(route)
    }
}

private data class ToolCard(
    val title: String,
    val subtitle: String,
    val route: String,
)

private val toolCards = listOf(
    ToolCard("Сечение кабеля", "Ток, прокладка, длина → мм²", ToolRoutes.CABLE),
    ToolCard("Светодиодная лента", "Длина, Вт/м → блок питания", ToolRoutes.LED),
    ToolCard("Ток и автомат", "Мощность → А и номинал", ToolRoutes.BREAKER),
    ToolCard("Падение напряжения", "ΔU по длине и сечению", ToolRoutes.VOLTAGE_DROP),
    ToolCard("Освещение", "Площадь и Вт/м² → мощность", ToolRoutes.LIGHTING),
    ToolCard("УЗО / диф", "Подсказка по номиналу", ToolRoutes.RCD),
    ToolCard("Кабель на заказ", "Длины и запас → метраж", ToolRoutes.CABLE_ORDER),
)

@Composable
fun ToolsHubContent(
    nav: NavHostController,
    homeRoute: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            "Ориентировочные расчёты для монтажа. Проверяйте по проекту и ПУЭ.",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp),
            textAlign = TextAlign.Center,
        )
        toolCards.forEach { card ->
            ToolHubCard(card, onClick = { ToolRoutes.navigateToTool(nav, card.route, homeRoute) })
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ToolHubCard(card: ToolCard, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = HdPanel),
        border = BorderStroke(1.dp, HdBlue.copy(alpha = 0.35f)),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(card.title, color = HdTextPrimary)
            Text(card.subtitle, color = HdTextSecondary, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun CableSectionToolScreen(onBack: () -> Unit) {
    ToolScreenScaffold("Сечение кабеля", onBack) {
        var current by remember { mutableStateOf("16") }
        var length by remember { mutableStateOf("") }
        var lineVoltage by remember { mutableIntStateOf(220) }
        var material by remember { mutableStateOf(ConductorMaterial.Copper) }
        var install by remember { mutableStateOf(CableInstallMode.InConduit) }
        var phase by remember { mutableStateOf(PhaseMode.Single) }
        LaunchedEffect(phase) {
            if (phase == PhaseMode.Three && lineVoltage == 220) lineVoltage = 380
            if (phase == PhaseMode.Single && lineVoltage == 380) lineVoltage = 220
        }
        var resultText by remember { mutableStateOf<String?>(null) }
        var notes by remember { mutableStateOf<List<String>>(emptyList()) }

        HdTextField(current, { current = it }, "Ток нагрузки, А")
        HdTextField(length, { length = it }, "Длина линии, м (необяз.)")
        HdAcVoltagePicker(lineVoltage, onSelect = { lineVoltage = it })
        HdChoiceLabel("Материал")
        HdTwoChoice("Медь", "Алюминий", material == ConductorMaterial.Copper) {
            material = if (it) ConductorMaterial.Copper else ConductorMaterial.Aluminum
        }
        HdChoiceLabel("Прокладка")
        HdTwoChoice("В трубе", "Открыто", install == CableInstallMode.InConduit) {
            install = if (it) CableInstallMode.InConduit else CableInstallMode.OpenAir
        }
        HdChoiceLabel("Сеть")
        HdTwoChoice("1 фаза", "3 фазы", phase == PhaseMode.Single) {
            phase = if (it) PhaseMode.Single else PhaseMode.Three
        }
        HdPrimaryButton("Рассчитать") {
            val i = parseElectricalDouble(current)
            val len = parseElectricalDouble(length)
            if (i == null || i <= 0) {
                resultText = null
                notes = listOf("Укажите ток больше 0.")
            } else {
                val r = ElectricalCalculators.cableSection(
                    CableSectionInput(i, material, install, len, phase, lineVoltage.toDouble()),
                )
                if (r == null) {
                    resultText = "Не удалось рассчитать"
                    notes = emptyList()
                } else {
                    val drop = r.voltageDropPercent?.let { " Падение ≈ ${"%.1f".format(it)}%." } ?: ""
                    resultText = "Рекомендуемое сечение: ${r.recommendedSectionMm2} мм².$drop"
                    notes = r.notes
                }
            }
        }
        ToolResultBlock(resultText, notes)
    }
}

@Composable
fun LedStripToolScreen(onBack: () -> Unit) {
    ToolScreenScaffold("Светодиодная лента", onBack) {
        var length by remember { mutableStateOf("5") }
        var wpm by remember { mutableStateOf("14.4") }
        var supplyVolts by remember { mutableIntStateOf(12) }
        var feederLen by remember { mutableStateOf("15") }
        var feederSection by remember { mutableStateOf("1.5") }
        var feederMaterial by remember { mutableStateOf(ConductorMaterial.Copper) }
        var resultText by remember { mutableStateOf<String?>(null) }
        var notes by remember { mutableStateOf<List<String>>(emptyList()) }

        HdTextField(length, { length = it }, "Длина ленты, м")
        HdTextField(wpm, { wpm = it }, "Мощность, Вт/м")
        HdLedVoltagePicker(supplyVolts, onSelect = { supplyVolts = it })
        Text(
            "БП в щите — линия постоянного тока до ленты",
            color = HdOrange,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        )
        HdTextField(feederLen, { feederLen = it }, "Длина кабеля, м (в одну сторону)")
        HdTextField(feederSection, { feederSection = it }, "Сечение кабеля, мм²")
        HdChoiceLabel("Материал кабеля")
        HdTwoChoice("Медь", "Алюминий", feederMaterial == ConductorMaterial.Copper) {
            feederMaterial = if (it) ConductorMaterial.Copper else ConductorMaterial.Aluminum
        }
        Text(
            "Допустимое падение на DC-линии: 3% (типично для LED). Считаем ΔU = 2·I·L·ρ/S.",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        HdPrimaryButton("Рассчитать") {
            val l = parseElectricalDouble(length)
            val w = parseElectricalDouble(wpm)
            val v = supplyVolts.toDouble()
            val fLen = parseElectricalDouble(feederLen)
            val fS = parseElectricalDouble(feederSection)
            val dcFeeder = if (fLen != null && fS != null && fLen > 0 && fS > 0) {
                LedDcFeederInput(fLen, fS, feederMaterial)
            } else {
                null
            }
            val r = if (l != null && w != null) {
                ElectricalCalculators.ledStrip(LedStripInput(l, w, v, dcFeeder = dcFeeder))
            } else null
            if (r == null) {
                resultText = null
                notes = listOf("Проверьте введённые значения.")
            } else {
                resultText = buildString {
                    append("Мощность ленты: ${"%.1f".format(r.totalWatts)} Вт, ток ≈ ${"%.2f".format(r.loadAmps)} А.\n")
                    append("Блок питания: ${r.suggestedPsuLabel} (запас 20%).")
                    r.dcFeeder?.let { fd ->
                        append("\n\nЛиния от щита до ленты:\n")
                        append("Падение на кабеле ≈ ${"%.2f".format(fd.dropVolts)} В (${"%.1f".format(fd.dropPercent)}%).\n")
                        append("Напряжение у ленты ≈ ${"%.2f".format(fd.voltageAtStrip)} В.")
                        append(
                            if (fd.withinLimit) {
                                " В пределах нормы (≤ ${fd.maxDropPercent.toInt()}%)."
                            } else {
                                " Превышает норму (>${fd.maxDropPercent.toInt()}%) — увеличьте сечение или укоротите трассу."
                            },
                        )
                        fd.minSectionMm2?.let { minS ->
                            if (!fd.withinLimit) {
                                append("\nОриентир сечения при этой длине: не меньше $minS мм².")
                            }
                        }
                    }
                }
                notes = buildList {
                    if (r.feederHint.isNotBlank()) add(r.feederHint)
                    if (supplyVolts <= 5) {
                        add("На 5 В ток выше — нужен толстый кабель до ленты и БП с запасом по амперам.")
                    }
                    r.dcFeeder?.let {
                        add("При большом падении лента тускнеет на конце; для низкого напряжения часто целятся в ≤3% ΔU.")
                    }
                }
            }
        }
        ToolResultBlock(resultText, notes)
    }
}

@Composable
fun BreakerToolScreen(onBack: () -> Unit) {
    ToolScreenScaffold("Ток и автомат", onBack) {
        var power by remember { mutableStateOf("3") }
        var lineVoltage by remember { mutableIntStateOf(220) }
        var cos by remember { mutableStateOf("1") }
        var phase by remember { mutableStateOf(PhaseMode.Single) }
        LaunchedEffect(phase) {
            if (phase == PhaseMode.Three && lineVoltage == 220) lineVoltage = 380
            if (phase == PhaseMode.Single && lineVoltage == 380) lineVoltage = 220
        }
        var resultText by remember { mutableStateOf<String?>(null) }
        var notes by remember { mutableStateOf<List<String>>(emptyList()) }

        HdTextField(power, { power = it }, "Мощность, кВт")
        HdAcVoltagePicker(lineVoltage, onSelect = { lineVoltage = it })
        HdTextField(cos, { cos = it }, "cos φ")
        HdChoiceLabel("Сеть")
        HdTwoChoice("1 фаза", "3 фазы", phase == PhaseMode.Single) {
            phase = if (it) PhaseMode.Single else PhaseMode.Three
        }
        HdPrimaryButton("Рассчитать") {
            val p = parseElectricalDouble(power)
            val c = parseElectricalDouble(cos) ?: 1.0
            val r = if (p != null) {
                ElectricalCalculators.breakerSizing(
                    BreakerInput(p, phase, lineVoltage.toDouble(), c),
                )
            } else null
            if (r == null) {
                resultText = null
                notes = listOf("Укажите мощность.")
            } else {
                resultText = buildString {
                    append("Ток нагрузки ≈ ${"%.1f".format(r.loadAmps)} А.\n")
                    append("С запасом 1,25: ${"%.1f".format(r.designAmps)} А → автомат ${r.recommendedBreakerAmps} А.")
                }
                notes = r.notes
            }
        }
        ToolResultBlock(resultText, notes)
    }
}

@Composable
fun VoltageDropToolScreen(onBack: () -> Unit) {
    ToolScreenScaffold("Падение напряжения", onBack) {
        var current by remember { mutableStateOf("10") }
        var length by remember { mutableStateOf("25") }
        var section by remember { mutableStateOf("2.5") }
        var lineVoltage by remember { mutableIntStateOf(220) }
        var material by remember { mutableStateOf(ConductorMaterial.Copper) }
        var phase by remember { mutableStateOf(PhaseMode.Single) }
        var resultText by remember { mutableStateOf<String?>(null) }
        var notes by remember { mutableStateOf<List<String>>(emptyList()) }
        LaunchedEffect(phase) {
            if (phase == PhaseMode.Three && lineVoltage == 220) lineVoltage = 380
            if (phase == PhaseMode.Single && lineVoltage == 380) lineVoltage = 220
        }

        HdTextField(current, { current = it }, "Ток, А")
        HdTextField(length, { length = it }, "Длина, м")
        HdTextField(section, { section = it }, "Сечение, мм²")
        HdAcVoltagePicker(lineVoltage, onSelect = { lineVoltage = it })
        HdChoiceLabel("Материал")
        HdTwoChoice("Медь", "Алюминий", material == ConductorMaterial.Copper) {
            material = if (it) ConductorMaterial.Copper else ConductorMaterial.Aluminum
        }
        HdChoiceLabel("Сеть")
        HdTwoChoice("1 фаза", "3 фазы", phase == PhaseMode.Single) {
            phase = if (it) PhaseMode.Single else PhaseMode.Three
        }
        HdPrimaryButton("Рассчитать") {
            val i = parseElectricalDouble(current)
            val l = parseElectricalDouble(length)
            val s = parseElectricalDouble(section)
            val r = if (i != null && l != null && s != null) {
                ElectricalCalculators.voltageDrop(
                    VoltageDropInput(i, l, s, material, phase, lineVoltage.toDouble()),
                )
            } else null
            if (r == null) {
                resultText = null
                notes = listOf("Заполните ток, длину и сечение.")
            } else {
                val ok = if (r.withinLimit) "в норме (≤5%)" else "выше 5% — увеличьте сечение или укоротите линию"
                resultText = "ΔU ≈ ${"%.2f".format(r.dropVolts)} В (${"%.1f".format(r.dropPercent)}%), $ok"
                notes = emptyList()
            }
        }
        ToolResultBlock(resultText, notes)
    }
}

@Composable
fun LightingToolScreen(onBack: () -> Unit) {
    ToolScreenScaffold("Освещение", onBack) {
        var area by remember { mutableStateOf("18") }
        var wsq by remember { mutableStateOf("15") }
        var resultText by remember { mutableStateOf<String?>(null) }

        HdTextField(area, { area = it }, "Площадь, м²")
        HdTextField(wsq, { wsq = it }, "Удельная мощность, Вт/м²")
        Text(
            "Ориентир: жилые комнаты ~10–15, кухня ~15–20, ванная ~15 Вт/м² (LED).",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        HdPrimaryButton("Рассчитать") {
            val a = parseElectricalDouble(area)
            val w = parseElectricalDouble(wsq)
            val r = if (a != null && w != null) ElectricalCalculators.lightingPower(LightingPowerInput(a, w)) else null
            resultText = r?.let {
                "Суммарная мощность ≈ ${"%.0f".format(it.totalWatts)} Вт.\n" +
                    "Ток одной фазы 220 В ≈ ${"%.1f".format(it.approxCurrentSinglePhase220)} А."
            }
        }
        ToolResultBlock(resultText, emptyList())
    }
}

@Composable
fun RcdToolScreen(onBack: () -> Unit) {
    ToolScreenScaffold("УЗО / диф", onBack) {
        var breaker by remember { mutableStateOf("16") }
        var wet by remember { mutableStateOf(false) }
        var resultText by remember { mutableStateOf<String?>(null) }
        var notes by remember { mutableStateOf<List<String>>(emptyList()) }

        HdTextField(breaker, { breaker = it }, "Номинал автомата линии, А")
        HdChoiceLabel("Зона")
        HdTwoChoice("Сухие помещения", "Ванная / душ", !wet) { dry -> wet = !dry }
        Text(
            "Для квартир и новой проводки предпочтительны УЗО типа A (чувствительны к пульсирующему постоянному току). Тип AC — в основном для старых линий без электроники; для дома с LED, зарядками и БП часто недостаточен.",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        HdPrimaryButton("Подсказка") {
            val b = parseElectricalDouble(breaker)?.toInt()
            if (b == null || b <= 0) {
                resultText = null
                notes = listOf("Укажите номинал автомата.")
            } else {
                val r = ElectricalCalculators.rcdHint(
                    RcdHintInput(b, if (wet) RcdLocationHint.WetRooms else RcdLocationHint.DryRooms),
                )
                resultText = "УЗО: ${r.rcdRatedAmps} А, чувствительность ${r.leakageMa} мА."
                notes = r.notes
            }
        }
        ToolResultBlock(resultText, notes)
    }
}

@Composable
fun CableOrderToolScreen(onBack: () -> Unit) {
    ToolScreenScaffold("Кабель на заказ", onBack) {
        var lengths by remember { mutableStateOf("12\n8\n5") }
        var runs by remember { mutableStateOf("1") }
        var reserve by remember { mutableStateOf("10") }
        var roll by remember { mutableStateOf("100") }
        var resultText by remember { mutableStateOf<String?>(null) }

        HdTextField(
            lengths,
            { lengths = it },
            "Длины участков, м (каждая с новой строки)",
        )
        HdTextField(runs, { runs = it }, "Число одинаковых трасс")
        HdTextField(reserve, { reserve = it }, "Запас, %")
        HdTextField(roll, { roll = it }, "Длина бухты, м (необяз.)")
        HdPrimaryButton("Рассчитать") {
            val segs = lengths.lines()
                .mapNotNull { parseElectricalDouble(it) }
                .filter { it > 0 }
            val rCount = parseElectricalDouble(runs)?.toInt() ?: 0
            val res = parseElectricalDouble(reserve) ?: 10.0
            val rollM = parseElectricalDouble(roll)
            val r = ElectricalCalculators.cableOrder(CableOrderInput(segs, rCount, res, rollM))
            resultText = when {
                r == null -> null
                r.rollsCount != null ->
                    "Итого ≈ ${"%.1f".format(r.totalMeters)} м, бухт по ${rollM?.toInt()} м: ${r.rollsCount} шт."
                else -> "Итого ≈ ${"%.1f".format(r.totalMeters)} м."
            }
        }
        ToolResultBlock(resultText, emptyList())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ToolScreenScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val motion = LocalHdMotion.current
    Box(Modifier.fillMaxSize()) {
        SpaceBackground()
        Scaffold(
            containerColor = HdBlack.copy(alpha = motion.scaffoldOverlayAlpha),
            topBar = {
                TopAppBar(
                    title = { Text(title, color = HdTextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = HdTextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = HdBlack.copy(alpha = 0.7f)),
                )
            },
        ) { padding ->
            BoxWithConstraints(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding(),
            ) {
                val scroll = rememberScrollState()
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = maxHeight)
                        .verticalScroll(scroll)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        content()
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Расчёт ориентировочный. Ответственность за проектные решения несёт исполнитель.",
                        color = HdTextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolResultBlock(result: String?, notes: List<String>) {
    if (result != null) {
        Spacer(Modifier.height(16.dp))
        HdCard {
            Text("Результат", color = HdOrange)
            Text(result, color = HdTextPrimary, modifier = Modifier.padding(top = 8.dp))
            notes.forEach { note ->
                Text("• $note", color = HdTextSecondary, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun HdAcVoltagePicker(selected: Int, onSelect: (Int) -> Unit) {
    HdChoiceLabel("Напряжение, В")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HdChipToggle("220", selected == 220, Modifier.weight(1f)) { onSelect(220) }
        HdChipToggle("380", selected == 380, Modifier.weight(1f)) { onSelect(380) }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun HdLedVoltagePicker(selected: Int, onSelect: (Int) -> Unit) {
    HdChoiceLabel("Напряжение БП (на ленту), В")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HdChipToggle("5", selected == 5, Modifier.weight(1f)) { onSelect(5) }
        HdChipToggle("12", selected == 12, Modifier.weight(1f)) { onSelect(12) }
        HdChipToggle("24", selected == 24, Modifier.weight(1f)) { onSelect(24) }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun HdChoiceLabel(text: String) {
    Text(text, color = HdTextSecondary, modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))
}

@Composable
private fun HdTwoChoice(left: String, right: String, leftSelected: Boolean, onSelectLeft: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HdChipToggle(left, leftSelected, Modifier.weight(1f)) { onSelectLeft(true) }
        HdChipToggle(right, !leftSelected, Modifier.weight(1f)) { onSelectLeft(false) }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun HdChipToggle(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) HdBlue.copy(alpha = 0.35f) else HdPanel,
        ),
        border = BorderStroke(
            1.dp,
            if (selected) HdOrange.copy(alpha = 0.8f) else HdTextSecondary.copy(alpha = 0.35f),
        ),
    ) {
        Text(
            label,
            color = if (selected) HdTextPrimary else HdTextSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
        )
    }
}
