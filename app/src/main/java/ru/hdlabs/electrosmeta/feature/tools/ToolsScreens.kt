@file:OptIn(ExperimentalFoundationApi::class)

package ru.hdlabs.electrosmeta.feature.tools

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.navigation.NavHostController
import ru.hdlabs.electrosmeta.domain.electrical.BreakerInput
import ru.hdlabs.electrosmeta.domain.electrical.CableInstallMode
import ru.hdlabs.electrosmeta.domain.electrical.CablesInConduit
import ru.hdlabs.electrosmeta.domain.electrical.CableOrderInput
import ru.hdlabs.electrosmeta.domain.electrical.ElectricalLineKind
import ru.hdlabs.electrosmeta.domain.electrical.RcdLoadKind
import ru.hdlabs.electrosmeta.domain.electrical.RcdSensitivityContext
import ru.hdlabs.electrosmeta.domain.electrical.ToolCalcCheck
import ru.hdlabs.electrosmeta.domain.electrical.ToolCalcReport
import ru.hdlabs.electrosmeta.domain.electrical.CableSectionInput
import ru.hdlabs.electrosmeta.domain.electrical.ConductorMaterial
import ru.hdlabs.electrosmeta.domain.electrical.ElectricalCalculators
import ru.hdlabs.electrosmeta.domain.electrical.LedDcFeederInput
import ru.hdlabs.electrosmeta.domain.electrical.LedStripInput
import ru.hdlabs.electrosmeta.domain.electrical.LightingPowerInput
import ru.hdlabs.electrosmeta.domain.electrical.PhaseMode
import ru.hdlabs.electrosmeta.domain.electrical.RcdHintInput
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

const val ToolsCalculatorsDisclaimer =
    "Расчёт ориентировочный. Упрощённые модели могут не совпадать с ПУЭ и СП. " +
        "Ответственность за безопасность несёт исполнитель и проектировщик."

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
            ToolsCalculatorsDisclaimer,
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 16.dp),
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
        var cablesInDuct by remember { mutableStateOf(CablesInConduit.OneTwo) }
        var phase by remember { mutableStateOf(PhaseMode.Single) }
        var lineKind by remember { mutableStateOf(ElectricalLineKind.Power) }
        LaunchedEffect(phase) {
            if (phase == PhaseMode.Three && lineVoltage == 220) lineVoltage = 380
            if (phase == PhaseMode.Single && lineVoltage == 380) lineVoltage = 220
        }
        var resultText by remember { mutableStateOf<String?>(null) }
        var notes by remember { mutableStateOf<List<String>>(emptyList()) }
        var report by remember { mutableStateOf<ToolCalcReport?>(null) }
        var liveRecalc by remember { mutableStateOf(false) }
        var scrollToResultNonce by remember { mutableIntStateOf(0) }

        fun performCalc() {
            val i = parseElectricalDouble(current)
            val len = parseElectricalDouble(length)
            if (i == null || i <= 0) {
                resultText = null
                report = null
                notes = listOf("Укажите ток больше 0.")
            } else {
                val maxDu = ElectricalCalculators.defaultMaxVoltageDropPercent(lineKind)
                val r = ElectricalCalculators.cableSection(
                    CableSectionInput(
                        i,
                        material,
                        install,
                        len,
                        phase,
                        lineVoltage.toDouble(),
                        cablesInConduit = cablesInDuct,
                        lineKind = lineKind,
                    ),
                )
                if (r == null) {
                    resultText = "Не удалось рассчитать"
                    notes = emptyList()
                } else {
                    val drop = r.voltageDropPercent?.let { " Падение ≈ ${"%.1f".format(it)}%." } ?: ""
                    resultText = "Рекомендуемое сечение: ${r.recommendedSectionMm2} мм².$drop"
                    notes = listOf(r.recommendation) + r.notes
                    report = ToolCalcReport(
                        headline = resultText!!,
                        inputs = listOf(
                            "I = $i А",
                            "L = ${len ?: "—"} м",
                            "U = ${formatCalcVoltageLabel(lineVoltage)}, ${if (phase == PhaseMode.Single) "1 фаза" else "3 фазы"}",
                            "Линия: ${if (lineKind == ElectricalLineKind.Power) "силовая" else "освещение"} (ΔU ≤ $maxDu%)",
                        ),
                        methodLines = listOf(
                            "Предварительный подбор: Iдоп по таблице, ΔU = 2·L·I·ρ/S (1ф) или √3·L·I·ρ/S (3ф), cos φ = 1.",
                        ),
                        substitutionLines = r.voltageDropPercent?.let {
                            listOf("ΔU ≈ ${"%.2f".format(it)}% при S = ${r.recommendedSectionMm2} мм²")
                        } ?: emptyList(),
                        checks = buildList {
                            add(ToolCalcCheck("I ≤ Iдоп", "≈ ${r.allowableAmpsAtSection.toInt()} А", true))
                            r.voltageDropPercent?.let {
                                add(ToolCalcCheck("ΔU ≤ $maxDu%", "${"%.2f".format(it)}%", it <= maxDu))
                            }
                        },
                        warnings = toolStandardWarnings(),
                        normativeNotes = toolStandardNormativeNotes(),
                        recommendations = listOf(
                            r.recommendation,
                            "При проверке только ΔU откройте калькулятор «Падение напряжения».",
                        ),
                    )
                }
            }
        }

        val inputKey = remember(current, length, lineVoltage, material, install, cablesInDuct, phase, lineKind) {
            listOf(current, length, lineVoltage, material, install, cablesInDuct, phase, lineKind)
        }
        ToolAutoRecalcEffect(liveRecalc, inputKey) { performCalc() }

        ToolTaskPresets(
            listOf("Розетки 16 А", "Освещение 10 А", "Плита 32 А"),
        ) { label ->
            when (label) {
                "Розетки 16 А" -> {
                    current = "16"
                    lineKind = ElectricalLineKind.Power
                }
                "Освещение 10 А" -> {
                    current = "10"
                    lineKind = ElectricalLineKind.Lighting
                }
                "Плита 32 А" -> {
                    current = "32"
                    lineKind = ElectricalLineKind.Power
                    phase = PhaseMode.Three
                    lineVoltage = 380
                }
            }
            liveRecalc = true
            performCalc()
        }
        HdTextField(current, { current = it }, "Ток нагрузки, А (фазный / линейный)")
        HdTextField(length, { length = it }, "Длина линии, м (необяз., в одну сторону)")
        Text(
            "Без длины — только по току. Номинал 230/400 В на сечение по нагреву не влияет; для ΔU укажите длину.",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        HdAcVoltagePicker(lineVoltage, onSelect = { lineVoltage = it })
        HdChoiceLabel("Материал")
        HdTwoChoice("Медь", "Алюминий", material == ConductorMaterial.Copper) {
            material = if (it) ConductorMaterial.Copper else ConductorMaterial.Aluminum
        }
        HdChoiceLabel("Прокладка")
        HdTwoChoice("В трубе", "Открыто", install == CableInstallMode.InConduit) {
            install = if (it) CableInstallMode.InConduit else CableInstallMode.OpenAir
        }
        if (install == CableInstallMode.InConduit) {
            HdChoiceLabel("Кабелей в трубе")
            HdTwoChoice("1–2", "3–4", cablesInDuct == CablesInConduit.OneTwo) {
                cablesInDuct = if (it) CablesInConduit.OneTwo else CablesInConduit.ThreeFour
            }
        }
        HdChoiceLabel("Назначение линии")
        HdTwoChoice("Силовая / розетки", "Освещение", lineKind == ElectricalLineKind.Power) {
            lineKind = if (it) ElectricalLineKind.Power else ElectricalLineKind.Lighting
        }
        HdChoiceLabel("Сеть")
        HdTwoChoice("1 фаза", "3 фазы", phase == PhaseMode.Single) {
            phase = if (it) PhaseMode.Single else PhaseMode.Three
        }
        ToolCalcButton(
            "Рассчитать",
            onRequestScrollToResult = { scrollToResultNonce++ },
        ) {
            liveRecalc = true
            performCalc()
        }
        ToolResultBlock(resultText, notes, report, scrollToResultNonce)
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
        var report by remember { mutableStateOf<ToolCalcReport?>(null) }
        var liveRecalc by remember { mutableStateOf(false) }
        var scrollToResultNonce by remember { mutableIntStateOf(0) }

        fun performCalc() {
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
                report = null
                notes = listOf("Проверьте введённые значения.")
            } else {
                resultText = buildString {
                    append("Мощность ленты: ${"%.1f".format(r.totalWatts)} Вт, ток ≈ ${"%.2f".format(r.loadAmps)} А.\n")
                    if (r.isMainsAc) {
                        append(r.suggestedPsuLabel)
                        append("\n${r.feederHint}")
                    } else {
                        append("Блок питания: ${r.suggestedPsuLabel} (запас 20%).")
                        append("\nЗапас нужен, чтобы БП не работал на пределе.")
                    }
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
                        append("\n${fd.recommendation}")
                    }
                }
                notes = buildList {
                    add(r.recommendation)
                    if (r.feederHint.isNotBlank()) add(r.feederHint)
                    if (supplyVolts <= 5) {
                        add("На 5 В ток выше — нужен толстый кабель до ленты и БП с запасом по амперам.")
                    }
                    r.dcFeeder?.let {
                        add("При большом падении лента тускнеет на конце; для низкого напряжения часто целятся в ≤3% ΔU.")
                    }
                }
                report = baseToolReport(
                    headline = resultText!!,
                    inputs = listOf(
                        "L = $l м, $wpm Вт/м",
                        if (r.isMainsAc) "Питание: сеть ${supplyVolts} В" else "БП: ${supplyVolts} В",
                    ),
                    methodLines = if (r.isMainsAc) {
                        listOf("Сетевая лента: без DC-формулы ΔU.")
                    } else {
                        listOf("P = L·Pуд; I = P/U; БП с запасом; DC: ΔU = 2·I·L·ρ/S, лимит 3%.")
                    },
                    substitutionLines = r.dcFeeder?.let { fd ->
                        listOf("ΔU DC ≈ ${"%.1f".format(fd.dropPercent)}%")
                    } ?: emptyList(),
                    checks = r.dcFeeder?.let { fd ->
                        listOf(ToolCalcCheck("ΔU DC ≤ ${fd.maxDropPercent.toInt()}%", "${"%.1f".format(fd.dropPercent)}%", fd.withinLimit))
                    } ?: emptyList(),
                    recommendations = listOf(r.recommendation),
                )
            }
        }

        val inputKey = remember(length, wpm, supplyVolts, feederLen, feederSection, feederMaterial) {
            listOf(length, wpm, supplyVolts, feederLen, feederSection, feederMaterial)
        }
        ToolAutoRecalcEffect(liveRecalc, inputKey) { performCalc() }

        ToolTaskPresets(listOf("5 м × 14,4", "3 м × 9,6", "220 В сеть")) { label ->
            when (label) {
                "5 м × 14,4" -> {
                    length = "5"
                    wpm = "14.4"
                    supplyVolts = 12
                }
                "3 м × 9,6" -> {
                    length = "3"
                    wpm = "9.6"
                    supplyVolts = 12
                }
                "220 В сеть" -> {
                    length = "5"
                    wpm = "14.4"
                    supplyVolts = 220
                }
            }
            liveRecalc = true
            performCalc()
        }
        HdTextField(length, { length = it }, "Длина ленты, м")
        HdTextField(wpm, { wpm = it }, "Мощность, Вт/м")
        HdLedVoltagePicker(supplyVolts, onSelect = { supplyVolts = it })
        if (supplyVolts >= 100) {
            Text(
                "Лента на сеть 220 В: расчёт DC-кабеля от щита не выполняется. Используйте калькуляторы сечения и ΔU для линии 220 В.",
                color = HdTextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )
        } else {
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
        }
        ToolCalcButton(
            "Рассчитать",
            onRequestScrollToResult = { scrollToResultNonce++ },
        ) {
            liveRecalc = true
            performCalc()
        }
        ToolResultBlock(resultText, notes, report, scrollToResultNonce)
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
        var report by remember { mutableStateOf<ToolCalcReport?>(null) }
        var liveRecalc by remember { mutableStateOf(false) }
        var scrollToResultNonce by remember { mutableIntStateOf(0) }

        fun performCalc() {
            val p = parseElectricalDouble(power)
            val c = parseElectricalDouble(cos) ?: 1.0
            val r = if (p != null) {
                ElectricalCalculators.breakerSizing(
                    BreakerInput(p, phase, lineVoltage.toDouble(), c),
                )
            } else null
            if (r == null) {
                resultText = null
                report = null
                notes = listOf("Укажите мощность.")
            } else {
                resultText = buildString {
                    append("Ток нагрузки ≈ ${"%.1f".format(r.loadAmps)} А.\n")
                    append("С запасом 1,25: ${"%.1f".format(r.designAmps)} А → автомат ${r.recommendedBreakerAmps} А.")
                }
                notes = listOf(r.recommendation) + r.notes
                report = baseToolReport(
                    headline = resultText!!,
                    inputs = listOf(
                        "P = $p кВт, cos φ = $c",
                        formatCalcVoltageLabel(lineVoltage) + ", " + if (phase == PhaseMode.Single) "1 фаза" else "3 фазы",
                    ),
                    methodLines = listOf("I = P/(U·cos φ) или P/(√3·U·cos φ); автомат ≥ 1,25·I."),
                    substitutionLines = listOf(
                        "I ≈ ${"%.2f".format(r.loadAmps)} А → ${r.recommendedBreakerAmps} А",
                    ),
                    recommendations = listOf(r.recommendation, "Дальше: «Сечение кабеля» с этим током и длиной линии."),
                )
            }
        }

        val inputKey = remember(power, lineVoltage, cos, phase) {
            listOf(power, lineVoltage, cos, phase)
        }
        ToolAutoRecalcEffect(liveRecalc, inputKey) { performCalc() }

        ToolTaskPresets(listOf("1,5 кВт", "3 кВт", "5 кВт", "7 кВт")) { label ->
            power = label.replace(" кВт", "").replace(',', '.')
            liveRecalc = true
            performCalc()
        }
        HdTextField(power, { power = it }, "Мощность, кВт")
        HdAcVoltagePicker(lineVoltage, onSelect = { lineVoltage = it })
        HdTextField(cos, { cos = it }, "cos φ")
        HdChoiceLabel("Сеть")
        HdTwoChoice("1 фаза", "3 фазы", phase == PhaseMode.Single) {
            phase = if (it) PhaseMode.Single else PhaseMode.Three
        }
        ToolCalcButton(
            "Рассчитать",
            onRequestScrollToResult = { scrollToResultNonce++ },
        ) {
            liveRecalc = true
            performCalc()
        }
        ToolResultBlock(resultText, notes, report, scrollToResultNonce)
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
        var lineKind by remember { mutableStateOf(ElectricalLineKind.Power) }
        var resultText by remember { mutableStateOf<String?>(null) }
        var notes by remember { mutableStateOf<List<String>>(emptyList()) }
        var report by remember { mutableStateOf<ToolCalcReport?>(null) }
        var liveRecalc by remember { mutableStateOf(false) }
        var scrollToResultNonce by remember { mutableIntStateOf(0) }
        LaunchedEffect(phase) {
            if (phase == PhaseMode.Three && lineVoltage == 220) lineVoltage = 380
            if (phase == PhaseMode.Single && lineVoltage == 380) lineVoltage = 220
        }

        fun performCalc() {
            val i = parseElectricalDouble(current)
            val l = parseElectricalDouble(length)
            val s = parseElectricalDouble(section)
            val r = if (i != null && l != null && s != null) {
                ElectricalCalculators.voltageDrop(
                    VoltageDropInput(
                        i,
                        l,
                        s,
                        material,
                        phase,
                        lineVoltage.toDouble(),
                        lineKind = lineKind,
                    ),
                )
            } else null
            if (r == null) {
                resultText = null
                report = null
                notes = listOf("Заполните ток, длину и сечение.")
            } else {
                val ok = if (r.withinLimit) "в норме (≤${r.maxDropPercent.toInt()}%)" else "выше ${r.maxDropPercent.toInt()}%"
                resultText = "ΔU ≈ ${"%.2f".format(r.dropVolts)} В (${"%.1f".format(r.dropPercent)}%), $ok"
                notes = listOf(r.recommendation)
                val formula = if (phase == PhaseMode.Single) "ΔU = 2·L·I·ρ/S" else "ΔU = √3·L·I·ρ/S"
                report = baseToolReport(
                    headline = resultText!!,
                    inputs = listOf(
                        "I = $i А, L = $l м, S = $s мм²",
                        formatCalcVoltageLabel(lineVoltage),
                        if (lineKind == ElectricalLineKind.Power) "Силовая (≤${r.maxDropPercent.toInt()}%)" else "Освещение (≤${r.maxDropPercent.toInt()}%)",
                    ),
                    methodLines = listOf("$formula, cos φ = 1."),
                    substitutionLines = listOf("ΔU = ${"%.2f".format(r.dropVolts)} В (${"%.1f".format(r.dropPercent)}%)"),
                    checks = listOf(
                        ToolCalcCheck("ΔU ≤ ${r.maxDropPercent.toInt()}%", "${"%.1f".format(r.dropPercent)}%", r.withinLimit),
                    ),
                    recommendations = listOf(r.recommendation),
                )
            }
        }

        val inputKey = remember(current, length, section, lineVoltage, material, phase, lineKind) {
            listOf(current, length, section, lineVoltage, material, phase, lineKind)
        }
        ToolAutoRecalcEffect(liveRecalc, inputKey) { performCalc() }

        ToolTaskPresets(listOf("16 А розетки", "10 А свет")) { label ->
            when (label) {
                "16 А розетки" -> {
                    current = "16"
                    lineKind = ElectricalLineKind.Power
                }
                "10 А свет" -> {
                    current = "10"
                    lineKind = ElectricalLineKind.Lighting
                }
            }
            liveRecalc = true
            performCalc()
        }
        HdTextField(current, { current = it }, "Ток, А")
        HdTextField(length, { length = it }, "Длина, м")
        HdTextField(section, { section = it }, "Сечение, мм²")
        HdAcVoltagePicker(lineVoltage, onSelect = { lineVoltage = it })
        HdChoiceLabel("Материал")
        HdTwoChoice("Медь", "Алюминий", material == ConductorMaterial.Copper) {
            material = if (it) ConductorMaterial.Copper else ConductorMaterial.Aluminum
        }
        HdChoiceLabel("Назначение линии")
        HdTwoChoice("Силовая / розетки", "Освещение", lineKind == ElectricalLineKind.Power) {
            lineKind = if (it) ElectricalLineKind.Power else ElectricalLineKind.Lighting
        }
        HdChoiceLabel("Сеть")
        HdTwoChoice("1 фаза", "3 фазы", phase == PhaseMode.Single) {
            phase = if (it) PhaseMode.Single else PhaseMode.Three
        }
        ToolCalcButton(
            "Рассчитать",
            onRequestScrollToResult = { scrollToResultNonce++ },
        ) {
            liveRecalc = true
            performCalc()
        }
        ToolResultBlock(resultText, notes, report, scrollToResultNonce)
    }
}

@Composable
fun LightingToolScreen(onBack: () -> Unit) {
    ToolScreenScaffold("Освещение", onBack) {
        var area by remember { mutableStateOf("18") }
        var wsq by remember { mutableStateOf("15") }
        var resultText by remember { mutableStateOf<String?>(null) }
        var notes by remember { mutableStateOf<List<String>>(emptyList()) }
        var report by remember { mutableStateOf<ToolCalcReport?>(null) }
        var liveRecalc by remember { mutableStateOf(false) }
        var scrollToResultNonce by remember { mutableIntStateOf(0) }

        fun performCalc() {
            val a = parseElectricalDouble(area)
            val w = parseElectricalDouble(wsq)
            val r = if (a != null && w != null) ElectricalCalculators.lightingPower(LightingPowerInput(a, w)) else null
            if (r == null) {
                resultText = null
                report = null
                notes = emptyList()
            } else {
                resultText = "Суммарная мощность ≈ ${"%.0f".format(r.totalWatts)} Вт.\n" +
                    "Ток одной фазы 230 В ≈ ${"%.1f".format(r.approxCurrentSinglePhase220)} А."
                notes = listOf(r.recommendation)
                report = baseToolReport(
                    headline = resultText!!,
                    inputs = listOf("S = $a м², $w Вт/м²"),
                    methodLines = listOf("P = S·Вт/м²; ориентир тока на 220 В — не расчёт люкс/СП."),
                    substitutionLines = listOf("P ≈ ${"%.0f".format(r.totalWatts)} Вт"),
                    recommendations = listOf(r.recommendation),
                )
            }
        }

        val inputKey = remember(area, wsq) { listOf(area, wsq) }
        ToolAutoRecalcEffect(liveRecalc, inputKey) { performCalc() }

        ToolTaskPresets(listOf("Комната 12", "Кухня 15", "Ванная 15")) { label ->
            wsq = when (label) {
                "Комната 12" -> "12"
                "Кухня 15" -> "15"
                else -> "15"
            }
            liveRecalc = true
            performCalc()
        }
        HdTextField(area, { area = it }, "Площадь, м²")
        HdTextField(wsq, { wsq = it }, "Удельная мощность, Вт/м²")
        Text(
            "Ориентир по Вт/м² (LED), не норма освещённости по СП.",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        ToolCalcButton(
            "Рассчитать",
            onRequestScrollToResult = { scrollToResultNonce++ },
        ) {
            liveRecalc = true
            performCalc()
        }
        ToolResultBlock(resultText, notes, report, scrollToResultNonce)
    }
}

@Composable
fun RcdToolScreen(onBack: () -> Unit) {
    ToolScreenScaffold("УЗО / диф", onBack) {
        var breaker by remember { mutableStateOf("16") }
        var electronicsLoad by remember { mutableStateOf(true) }
        var higherShockRisk by remember { mutableStateOf(false) }
        var resultText by remember { mutableStateOf<String?>(null) }
        var notes by remember { mutableStateOf<List<String>>(emptyList()) }
        var report by remember { mutableStateOf<ToolCalcReport?>(null) }
        var liveRecalc by remember { mutableStateOf(false) }
        var scrollToResultNonce by remember { mutableIntStateOf(0) }

        fun performCalc() {
            val b = parseElectricalDouble(breaker)?.toInt()
            if (b == null || b <= 0) {
                resultText = null
                report = null
                notes = listOf("Укажите номинал автомата.")
            } else {
                val r = ElectricalCalculators.rcdHint(
                    RcdHintInput(
                        b,
                        if (electronicsLoad) RcdLoadKind.WithElectronics else RcdLoadKind.MostlyClassic,
                        if (higherShockRisk) RcdSensitivityContext.HigherShockRisk else RcdSensitivityContext.GroupCircuit,
                    ),
                )
                resultText = "УЗО: ${r.rcdRatedAmps} А, чувствительность ${r.leakageMa} мА."
                notes = listOf(r.recommendation) + r.notes
                report = baseToolReport(
                    headline = resultText!!,
                    inputs = listOf(
                        "Автомат линии: $b А",
                        if (electronicsLoad) "Нагрузка с электроникой" else "Классическая нагрузка",
                    ),
                    methodLines = listOf("Справочный подбор; не заменяет проект и схему УЗО/дифавтомата."),
                    recommendations = listOf(r.recommendation),
                )
            }
        }

        val inputKey = remember(breaker, electronicsLoad, higherShockRisk) {
            listOf(breaker, electronicsLoad, higherShockRisk)
        }
        ToolAutoRecalcEffect(liveRecalc, inputKey) { performCalc() }

        HdTextField(breaker, { breaker = it }, "Номинал автомата линии, А")
        HdChoiceLabel("Характер нагрузки")
        HdTwoChoice("С электроникой (LED, БП)", "Классическая", electronicsLoad) {
            electronicsLoad = it
        }
        HdChoiceLabel("Контекст чувствительности")
        HdTwoChoice("Групповая линия (30 мА)", "Повышенный риск (10 мА)", !higherShockRisk) {
            higherShockRisk = !it
        }
        Text(
            "Тип A — для линий с LED, БП и зарядками; 10 мА — только как подсказка для зон повышенного риска.",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        ToolCalcButton(
            "Подсказка",
            onRequestScrollToResult = { scrollToResultNonce++ },
        ) {
            liveRecalc = true
            performCalc()
        }
        ToolResultBlock(resultText, notes, report, scrollToResultNonce)
    }
}

@Composable
fun CableOrderToolScreen(onBack: () -> Unit) {
    ToolScreenScaffold("Кабель на заказ", onBack) {
        var lengths by remember { mutableStateOf("") }
        var runs by remember { mutableStateOf("") }
        var reserve by remember { mutableStateOf("") }
        var roll by remember { mutableStateOf("") }
        var resultText by remember { mutableStateOf<String?>(null) }
        var notes by remember { mutableStateOf<List<String>>(emptyList()) }
        var report by remember { mutableStateOf<ToolCalcReport?>(null) }
        var liveRecalc by remember { mutableStateOf(false) }
        var scrollToResultNonce by remember { mutableIntStateOf(0) }

        fun performCalc() {
            val segs = lengths.lines()
                .mapNotNull { parseElectricalDouble(it) }
                .filter { it > 0 }
            val rCount = parseElectricalDouble(runs)?.toInt() ?: 0
            val res = parseElectricalDouble(reserve) ?: 0.0
            val rollM = parseElectricalDouble(roll)
            val r = ElectricalCalculators.cableOrder(CableOrderInput(segs, rCount, res, rollM))
            resultText = when {
                r == null -> null
                r.rollsCount != null ->
                    "Заказ: ${r.orderMeters} м (до округления ${"%.1f".format(r.exactMetersWithReserve)} м), " +
                        "бухт ${r.rollsCount} × ${rollM?.toInt()} м"
                else -> "Заказ: ${r.orderMeters} м (до округления ${"%.1f".format(r.exactMetersWithReserve)} м)."
            }
            notes = when {
                r != null -> r.notes + r.recommendation
                segs.isEmpty() -> listOf("Введите длину участков трассы, м (каждый участок — с новой строки).")
                rCount <= 0 -> listOf("Укажите число одинаковых трасс (целое больше 0).")
                else -> emptyList()
            }
            report = r?.let {
                baseToolReport(
                    headline = resultText ?: "",
                    inputs = listOf(
                        "Участки: ${segs.joinToString(", ")} м",
                        "Трасс: $rCount, запас $res%",
                    ),
                    methodLines = listOf(
                        "L = Σучастков × Nтрасс × (1+З%); заказ = округление вверх до 1 м; бухты не прибавляются к метражу.",
                    ),
                    substitutionLines = listOf(
                        "Точно ${"%.1f".format(it.exactMetersWithReserve)} м → заказ ${it.orderMeters} м",
                    ),
                    recommendations = listOf(it.recommendation),
                )
            }
        }

        val inputKey = remember(lengths, runs, reserve, roll) { listOf(lengths, runs, reserve, roll) }
        ToolAutoRecalcEffect(liveRecalc, inputKey) { performCalc() }

        ToolTaskPresets(listOf("12 м × 3 +10%", "Одна 25 м")) { label ->
            when (label) {
                "12 м × 3 +10%" -> {
                    lengths = "12"
                    runs = "3"
                    reserve = "10"
                }
                "Одна 25 м" -> {
                    lengths = "25"
                    runs = "1"
                    reserve = "10"
                }
            }
            liveRecalc = true
            performCalc()
        }
        Text(
            "Строки суммируются в одну трассу. Три линии по 12 м → одна строка «12» и «3 трассы», не три строки 12+12+12.",
            color = HdTextSecondary,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        HdTextField(
            lengths,
            { lengths = it },
            "Участки одной трассы, м (с новой строки)",
        )
        HdTextField(runs, { runs = it }, "Сколько таких одинаковых трасс")
        HdTextField(reserve, { reserve = it }, "Запас, % (пусто = 0)")
        HdTextField(roll, { roll = it }, "Длина бухты, м (необяз.)")
        ToolCalcButton(
            "Рассчитать",
            onRequestScrollToResult = { scrollToResultNonce++ },
        ) {
            liveRecalc = true
            performCalc()
        }
        ToolResultBlock(resultText, notes, report, scrollToResultNonce)
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
            val scroll = rememberScrollState()
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(scroll)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                content()
                Spacer(Modifier.height(20.dp))
                Text(ToolsCalculatorsDisclaimer, color = HdTextSecondary)
            }
        }
    }
}

/** После первого «Рассчитать» пересчитывает при изменении любого параметра. */
@Composable
private fun ToolAutoRecalcEffect(enabled: Boolean, inputKey: Any?, onRecalc: () -> Unit) {
    LaunchedEffect(enabled, inputKey) {
        if (enabled) onRecalc()
    }
}

/** Скрывает клавиатуру перед расчётом, чтобы был виден блок «Результат». */
@Composable
private fun ToolCalcButton(
    text: String,
    onRequestScrollToResult: () -> Unit = {},
    onClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    HdPrimaryButton(text) {
        focusManager.clearFocus()
        keyboard?.hide()
        onRequestScrollToResult()
        onClick()
    }
}

@Composable
private fun ToolResultBlock(
    result: String?,
    notes: List<String>,
    report: ToolCalcReport? = null,
    scrollToResultNonce: Int = 0,
) {
    val bringIntoView = remember { BringIntoViewRequester() }
    LaunchedEffect(scrollToResultNonce) {
        if (scrollToResultNonce > 0 && (result != null || notes.isNotEmpty())) {
            delay(120)
            bringIntoView.bringIntoView()
        }
    }
    if (result != null || notes.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        HdCard(Modifier.bringIntoViewRequester(bringIntoView)) {
            Text(if (result != null) "Результат" else "Подсказка", color = HdOrange)
            if (result != null) {
                Text(result, color = HdTextPrimary, modifier = Modifier.padding(top = 8.dp))
            }
            notes.forEachIndexed { index, note ->
                val color = if (index == 0 && note.startsWith("Итог:")) HdOrange else HdTextSecondary
                Text(
                    if (index == 0 && note.startsWith("Итог:")) note else "• $note",
                    color = color,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
    if (report != null) {
        ToolCalcReportBlock(report)
    }
}

@Composable
private fun ToolTaskPresets(labels: List<String>, onPick: (String) -> Unit) {
    HdChoiceLabel("Быстрый выбор")
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEach { label ->
            ToolPresetChip(label) { onPick(label) }
        }
    }
}

@Composable
private fun ToolPresetChip(label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HdPanel),
        border = BorderStroke(1.dp, HdBlue.copy(alpha = 0.45f)),
    ) {
        Text(
            label,
            color = HdTextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun HdAcVoltagePicker(selected: Int, onSelect: (Int) -> Unit) {
    HdChoiceLabel("Напряжение, В")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HdChipToggle("230 (~220)", selected == 220, Modifier.weight(1f)) { onSelect(220) }
        HdChipToggle("400 (~380)", selected == 380, Modifier.weight(1f)) { onSelect(380) }
    }
    Text(
        "В формулах — выбранное значение; номинал бытовой сети в РФ: 230/400 В.",
        color = HdTextSecondary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun HdLedVoltagePicker(selected: Int, onSelect: (Int) -> Unit) {
    HdChoiceLabel("Питание ленты, В")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HdChipToggle("5", selected == 5, Modifier.weight(1f)) { onSelect(5) }
        HdChipToggle("12", selected == 12, Modifier.weight(1f)) { onSelect(12) }
        HdChipToggle("24", selected == 24, Modifier.weight(1f)) { onSelect(24) }
        HdChipToggle("220", selected == 220, Modifier.weight(1f)) { onSelect(220) }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun ToolCalcReportBlock(report: ToolCalcReport) {
    var expanded by remember { mutableStateOf(false) }
    Spacer(Modifier.height(12.dp))
    HdCard(Modifier.clickable { expanded = !expanded }) {
        Text(
            if (expanded) "Подробный расчёт ▲" else "Подробный расчёт ▼",
            color = HdOrange,
        )
        if (!expanded) {
            Text(
                "Нажмите, чтобы открыть формулы и проверки.",
                color = HdTextSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
            return@HdCard
        }
        if (report.inputs.isNotEmpty()) {
            Text("Исходные данные", color = HdTextSecondary, modifier = Modifier.padding(top = 8.dp))
            report.inputs.forEach { Text("• $it", color = HdTextPrimary, modifier = Modifier.padding(top = 4.dp)) }
        }
        if (report.methodLines.isNotEmpty()) {
            Text("Метод", color = HdTextSecondary, modifier = Modifier.padding(top = 8.dp))
            report.methodLines.forEach { Text(it, color = HdTextPrimary, modifier = Modifier.padding(top = 4.dp)) }
        }
        if (report.substitutionLines.isNotEmpty()) {
            Text("Подстановка", color = HdTextSecondary, modifier = Modifier.padding(top = 8.dp))
            report.substitutionLines.forEach { Text(it, color = HdTextPrimary, modifier = Modifier.padding(top = 4.dp)) }
        }
        if (report.checks.isNotEmpty()) {
            Text("Проверки", color = HdTextSecondary, modifier = Modifier.padding(top = 8.dp))
            report.checks.forEach { c ->
                val mark = when (c.passed) {
                    true -> "OK"
                    false -> "не OK"
                    null -> "—"
                }
                Text("• ${c.criterion}: ${c.value} ($mark)", color = HdTextPrimary, modifier = Modifier.padding(top = 4.dp))
            }
        }
        report.warnings.forEach { Text("⚠ $it", color = HdOrange, modifier = Modifier.padding(top = 6.dp)) }
        report.normativeNotes.forEach { Text("Норматив: $it", color = HdTextSecondary, modifier = Modifier.padding(top = 4.dp)) }
        report.recommendations.forEach { rec ->
            Text(rec, color = HdOrange, modifier = Modifier.padding(top = 6.dp))
        }
    }
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
