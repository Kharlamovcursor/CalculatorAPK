package ru.hdlabs.electrosmeta.domain.electrical

import kotlin.math.ceil
import kotlin.math.sqrt

enum class PhaseMode { Single, Three }

enum class ConductorMaterial { Copper, Aluminum }

/** Способ прокладки (упрощённо по ПУЭ: в трубе / открыто). */
enum class CableInstallMode { InConduit, OpenAir }

/** Количество кабелей в одной трубе (коэффициенты ПУЭ для снижения допустимого тока). */
enum class CablesInConduit { OneTwo, ThreeFour }

enum class RcdLocationHint { DryRooms, WetRooms }

/** Назначение линии для выбора лимита ΔU (упрощённая модель, ТЗ 2.2.0). */
enum class ElectricalLineKind {
    /** Силовая / розеточная — 5% в MVP */
    Power,
    /** Освещение — 3% в MVP */
    Lighting,
}

enum class RcdLoadKind {
    /** Нагреватели, лампы накаливания без электроники */
    MostlyClassic,
    /** LED, БП, зарядки, электроника — тип A */
    WithElectronics,
}

enum class RcdSensitivityContext {
    /** Групповые линии квартиры */
    GroupCircuit,
    /** Повышенный риск поражения (ванная и т.п.) — только как подсказка */
    HigherShockRisk,
}

data class CableSectionInput(
    val currentAmps: Double,
    val material: ConductorMaterial,
    val installMode: CableInstallMode,
    val lengthMeters: Double?,
    val phaseMode: PhaseMode,
    val voltageVolts: Double,
    val maxVoltageDropPercent: Double? = null,
    val cablesInConduit: CablesInConduit = CablesInConduit.OneTwo,
    val lineKind: ElectricalLineKind = ElectricalLineKind.Power,
)

enum class CableSectionLimitingFactor {
    /** Допустимый длительный ток по таблице ПУЭ (упрощённо). */
    Ampacity,
    /** Сечение увеличено из‑за падения напряжения на длине линии. */
    VoltageDrop,
    /** Практический минимум для силовых/розеточных линий. */
    PracticalMinimum,
}

data class CableSectionResult(
    val recommendedSectionMm2: Double,
    val allowableAmpsAtSection: Double,
    val voltageDropPercent: Double?,
    val limitingFactor: CableSectionLimitingFactor,
    /** Краткая итоговая рекомендация для монтажника. */
    val recommendation: String,
    val notes: List<String>,
)

data class LedDcFeederInput(
    /** Длина трассы в одну сторону (от БП в щите до ленты), м. */
    val lengthOneWayMeters: Double,
    val sectionMm2: Double,
    val material: ConductorMaterial = ConductorMaterial.Copper,
    /** Допустимое падение на линии постоянного тока, % от напряжения БП (для LED обычно 3%). */
    val maxDropPercent: Double = 3.0,
)

data class LedDcFeederResult(
    val dropVolts: Double,
    val dropPercent: Double,
    val voltageAtStrip: Double,
    val withinLimit: Boolean,
    val maxDropPercent: Double,
    /** Минимальное стандартное сечение при этой длине и токе. */
    val minSectionMm2: Double?,
    val recommendation: String,
)

data class LedStripInput(
    val lengthMeters: Double,
    val wattsPerMeter: Double,
    val supplyVolts: Double,
    val reserveFactor: Double = 1.2,
    val dcFeeder: LedDcFeederInput? = null,
)

data class LedStripResult(
    val totalWatts: Double,
    val loadAmps: Double,
    val psuWattsMin: Double,
    val psuAmpsMin: Double,
    val suggestedPsuLabel: String,
    val feederHint: String,
    val recommendation: String,
    val dcFeeder: LedDcFeederResult? = null,
    /** Лента/модуль на сеть 220 В — без расчёта DC-линии */
    val isMainsAc: Boolean = false,
)

data class BreakerInput(
    val powerKw: Double,
    val phaseMode: PhaseMode,
    val voltageVolts: Double,
    val cosPhi: Double = 1.0,
)

data class BreakerResult(
    val loadAmps: Double,
    val designAmps: Double,
    val recommendedBreakerAmps: Int,
    val recommendation: String,
    val notes: List<String>,
)

data class VoltageDropInput(
    val currentAmps: Double,
    val lengthMeters: Double,
    val sectionMm2: Double,
    val material: ConductorMaterial,
    val phaseMode: PhaseMode,
    val voltageVolts: Double,
    val lineKind: ElectricalLineKind = ElectricalLineKind.Power,
    val maxDropPercent: Double? = null,
)

data class VoltageDropResult(
    val dropVolts: Double,
    val dropPercent: Double,
    val maxDropPercent: Double,
    val withinLimit: Boolean,
    val recommendation: String,
)

data class LightingPowerInput(
    val areaSqM: Double,
    val wattsPerSqM: Double,
)

data class LightingPowerResult(
    val totalWatts: Double,
    val approxCurrentSinglePhase220: Double,
    val recommendation: String,
)

data class RcdHintInput(
    val breakerAmps: Int,
    val loadKind: RcdLoadKind = RcdLoadKind.WithElectronics,
    val sensitivityContext: RcdSensitivityContext = RcdSensitivityContext.GroupCircuit,
)

data class RcdHintResult(
    val rcdRatedAmps: Int,
    val leakageMa: Int,
    val recommendation: String,
    val notes: List<String>,
)

data class CableOrderInput(
    val segmentLengthsM: List<Double>,
    val runsCount: Int,
    val reservePercent: Double,
    val rollLengthM: Double?,
)

data class CableOrderResult(
    /** Метраж с запасом до округления, м */
    val exactMetersWithReserve: Double,
    /** Заказ, м (округление вверх до целого) */
    val orderMeters: Int,
    val rollsCount: Int?,
    /** Суммарная длина купленных бухт, м */
    val purchasedMeters: Double?,
    /** Остаток после работ, м */
    val remainderMeters: Double?,
    val recommendation: String,
    val notes: List<String> = emptyList(),
) {
    /** Для совместимости: заказ к целым метрам */
    val totalMeters: Double get() = orderMeters.toDouble()
}

object ElectricalCalculators {
    private val standardSectionsMm2 = listOf(1.5, 2.5, 4.0, 6.0, 10.0, 16.0, 25.0, 35.0, 50.0, 70.0, 95.0)
    private val standardBreakers = listOf(6, 10, 16, 20, 25, 32, 40, 50, 63, 80, 100, 125)
    private val standardPsuAmps = listOf(2, 3, 5, 6, 8, 10, 12, 15, 20, 25, 30)

    private const val RHO_COPPER = 0.0175
    private const val RHO_ALUMINUM = 0.028

    fun currentFromPower(
        powerKw: Double,
        phaseMode: PhaseMode,
        voltageVolts: Double,
        cosPhi: Double,
    ): Double {
        require(powerKw > 0 && voltageVolts > 0 && cosPhi in 0.1..1.0)
        val pW = powerKw * 1000.0
        return when (phaseMode) {
            PhaseMode.Single -> pW / (voltageVolts * cosPhi)
            PhaseMode.Three -> pW / (sqrt(3.0) * voltageVolts * cosPhi)
        }
    }

    fun defaultMaxVoltageDropPercent(lineKind: ElectricalLineKind): Double = when (lineKind) {
        ElectricalLineKind.Power -> 5.0
        ElectricalLineKind.Lighting -> 3.0
    }

    fun cableSection(input: CableSectionInput): CableSectionResult? {
        if (input.currentAmps <= 0 || input.voltageVolts <= 0) return null
        val maxDropPct = input.maxVoltageDropPercent ?: defaultMaxVoltageDropPercent(input.lineKind)
        val notes = mutableListOf<String>()
        notes += "Режим: предварительный подбор (упрощённые таблицы, cos φ = 1)."
        val byAmpacity = minSectionForCurrent(
            input.currentAmps,
            input.material,
            input.installMode,
            input.cablesInConduit,
        ) ?: return null
        var chosen = byAmpacity.section
        var ampsAt = byAmpacity.maxAmps
        var limiting = CableSectionLimitingFactor.Ampacity
        val length = input.lengthMeters
        if (length != null && length > 0) {
            val minForDrop = minSectionForVoltageDrop(
                amps = input.currentAmps,
                lengthM = length,
                material = input.material,
                phaseMode = input.phaseMode,
                voltage = input.voltageVolts,
                maxPercent = maxDropPct,
            )
            if (minForDrop != null && minForDrop > chosen) {
                chosen = minForDrop
                limiting = CableSectionLimitingFactor.VoltageDrop
                notes += "Сечение увеличено из‑за падения напряжения (≤ ${maxDropPct}%)."
            }
        }
        if (input.material == ConductorMaterial.Copper && chosen < 1.5) {
            chosen = 1.5
            notes += "Минимальное сечение меди в силовых цепях — 1,5 мм²."
        }
        if (input.material == ConductorMaterial.Aluminum && chosen < 2.5) {
            chosen = 2.5
            notes += "Минимум для алюминия в бытовых цепях — 2,5 мм²."
        }
        val beforePractical = chosen
        chosen = applyPracticalMinimumSection(chosen, input)
        if (chosen > beforePractical) {
            limiting = CableSectionLimitingFactor.PracticalMinimum
            if (input.material == ConductorMaterial.Copper && input.currentAmps >= 16.0) {
                notes += "Для розеточных и силовых линий около 16 А на объекте обычно не менее 2,5 мм² меди."
            }
        }
        ampsAt = ampacityAtSection(chosen, input.material, input.installMode, input.cablesInConduit) ?: ampsAt
        val dropPct = if (length != null && length > 0) {
            voltageDrop(
                VoltageDropInput(
                    input.currentAmps,
                    length,
                    chosen,
                    input.material,
                    input.phaseMode,
                    input.voltageVolts,
                    lineKind = input.lineKind,
                    maxDropPercent = maxDropPct,
                ),
            )?.dropPercent
        } else {
            null
        }
        notes += "Длительно допустимый ток для ${formatSection(chosen)} мм² ≈ ${ampsAt.toInt()} А (ориентир по ПУЭ, упрощённо)."
        val recommendation = buildCableSectionRecommendation(input, chosen, limiting, dropPct, maxDropPct)
        return CableSectionResult(
            recommendedSectionMm2 = chosen,
            allowableAmpsAtSection = ampsAt,
            voltageDropPercent = dropPct,
            limitingFactor = limiting,
            recommendation = recommendation,
            notes = notes,
        )
    }

    /** Розеточные/силовые линии: на практике не уменьшают сечение ниже типовых значений при 16 А и выше. */
    private fun applyPracticalMinimumSection(section: Double, input: CableSectionInput): Double {
        if (input.material == ConductorMaterial.Copper && input.currentAmps >= 16.0 && section < 2.5) {
            return 2.5
        }
        return section
    }

    private fun buildCableSectionRecommendation(
        input: CableSectionInput,
        section: Double,
        limiting: CableSectionLimitingFactor,
        dropPct: Double?,
        maxDropPct: Double,
    ): String {
        val phaseLabel = when (input.phaseMode) {
            PhaseMode.Single -> "1 фаза, ${input.voltageVolts.toInt()} В"
            PhaseMode.Three -> "3 фазы, ${input.voltageVolts.toInt()} В (межфазное)"
        }
        val length = input.lengthMeters
        return when {
            length == null || length <= 0 -> buildString {
                append("Итог: ${formatSection(section)} мм² по току ${"%.0f".format(input.currentAmps)} А. ")
                append("Напряжение ($phaseLabel) на сечение по нагреву не влияет — ")
                append("укажите длину линии, чтобы проверить падение напряжения.")
            }
            limiting == CableSectionLimitingFactor.VoltageDrop -> buildString {
                append("Итог: ${formatSection(section)} мм² — упёрлись в ΔU на ${"%.0f".format(length)} м ($phaseLabel). ")
                dropPct?.let { append("Падение ≈ ${"%.1f".format(it)}%. ") }
                append("Если линия короче или нагрузка меньше номинала — можно пересчитать.")
            }
            limiting == CableSectionLimitingFactor.PracticalMinimum -> buildString {
                append("Итог: ${formatSection(section)} мм² — практический минимум для такого тока; ")
                append("по таблице допустимого тока иногда хватает меньшего сечения при открытой прокладке.")
            }
            else -> buildString {
                append("Итог: ${formatSection(section)} мм² при ${"%.0f".format(input.currentAmps)} А ($phaseLabel). ")
                dropPct?.let {
                    append("ΔU на ${"%.0f".format(length)} м ≈ ${"%.1f".format(it)}% — в пределах ${maxDropPct}%. ")
                }
                append("Сверьте с автоматом и способом прокладки на объекте.")
            }
        }
    }

    fun ledStrip(input: LedStripInput): LedStripResult? {
        if (input.lengthMeters <= 0 || input.wattsPerMeter <= 0 || input.supplyVolts <= 0) return null
        if (input.supplyVolts >= 100.0) {
            return ledStripMainsAc(input)
        }
        val factor = input.reserveFactor.coerceIn(1.05, 1.5)
        val totalW = input.lengthMeters * input.wattsPerMeter
        val loadA = totalW / input.supplyVolts
        val psuW = totalW * factor
        val psuA = loadA * factor
        val suggestedA = standardPsuAmps.firstOrNull { it >= psuA } ?: standardPsuAmps.last()
        val label = "${input.supplyVolts.toInt()} В, не менее ${"%.0f".format(psuW)} Вт / ${suggestedA} А"
        val feederDrop = input.dcFeeder?.let { feeder ->
            ledDcFeederDrop(loadA, input.supplyVolts, feeder)
        }
        val feeder = buildLedFeederHint(input, loadA, feederDrop)
        val recommendation = buildLedStripRecommendation(loadA, input.supplyVolts, feederDrop)
        return LedStripResult(totalW, loadA, psuW, psuA, label, feeder, recommendation, feederDrop)
    }

    private fun ledStripMainsAc(input: LedStripInput): LedStripResult {
        val totalW = input.lengthMeters * input.wattsPerMeter
        val loadA = totalW / input.supplyVolts
        val recommendation = buildString {
            append("Итог: лента/модуль на сеть ${input.supplyVolts.toInt()} В — ")
            append("монтаж и защита по инструкции производителя; DC-формулы не применяются. ")
            append("Проверьте автомат, УЗО тип A и сечение вводной линии в калькуляторах сечения/ΔU.")
        }
        val feeder = "Сеть 220 В: не используйте расчёт DC-кабеля от щита; отдельная линия по паспорту изделия."
        return LedStripResult(
            totalWatts = totalW,
            loadAmps = loadA,
            psuWattsMin = totalW,
            psuAmpsMin = loadA,
            suggestedPsuLabel = "Питание от сети ${input.supplyVolts.toInt()} В",
            feederHint = feeder,
            recommendation = recommendation,
            dcFeeder = null,
            isMainsAc = true,
        )
    }

    private fun buildLedStripRecommendation(
        loadA: Double,
        supplyVolts: Double,
        feederDrop: LedDcFeederResult?,
    ): String = buildString {
        append("Итог: БП ${supplyVolts.toInt()} В с запасом по мощности; DC-линия — ")
        if (feederDrop == null) {
            append("укажите длину и сечение от щита для проверки напряжения на ленте.")
        } else if (feederDrop.withinLimit) {
            append("напряжение на ленте ≈ ${"%.2f".format(feederDrop.voltageAtStrip)} В, падение в норме (≤${feederDrop.maxDropPercent.toInt()}%).")
        } else {
            append("падение ${"%.1f".format(feederDrop.dropPercent)}% — увеличьте сечение")
            feederDrop.minSectionMm2?.let { append(" (ориентир ≥ $it мм²)") }
            append(" или укоротите трассу.")
        }
        if (loadA > 8) append(" При токе >8 А рассмотрите питание ленты с двух сторон.")
    }

    /**
     * Падение на линии DC (+ и −) от БП до ленты: ΔU = 2·I·L·ρ/S.
     */
    fun ledDcFeederDrop(
        loadAmps: Double,
        supplyVolts: Double,
        feeder: LedDcFeederInput,
    ): LedDcFeederResult? {
        if (loadAmps <= 0 || supplyVolts <= 0 || feeder.lengthOneWayMeters <= 0 || feeder.sectionMm2 <= 0) {
            return null
        }
        val maxPct = feeder.maxDropPercent.coerceIn(1.0, 10.0)
        val rho = if (feeder.material == ConductorMaterial.Copper) RHO_COPPER else RHO_ALUMINUM
        val dropV = 2.0 * loadAmps * feeder.lengthOneWayMeters * rho / feeder.sectionMm2
        val pct = 100.0 * dropV / supplyVolts
        val atStrip = (supplyVolts - dropV).coerceAtLeast(0.0)
        val maxDropV = supplyVolts * maxPct / 100.0
        val requiredS = 2.0 * loadAmps * feeder.lengthOneWayMeters * rho / maxDropV
        val minSection = standardSectionsMm2.firstOrNull { it >= requiredS }
        val recommendation = assessLedDropRecommendation(pct, atStrip, supplyVolts, minSection, pct <= maxPct)
        return LedDcFeederResult(
            dropVolts = dropV,
            dropPercent = pct,
            voltageAtStrip = atStrip,
            withinLimit = pct <= maxPct,
            maxDropPercent = maxPct,
            minSectionMm2 = minSection,
            recommendation = recommendation,
        )
    }

    private fun assessLedDropRecommendation(
        dropPercent: Double,
        voltageAtStrip: Double,
        supplyVolts: Double,
        minSection: Double?,
        withinLimit: Boolean,
    ): String = when {
        withinLimit ->
            "Падение ${"%.1f".format(dropPercent)}% — норма для LED. На ленте ≈ ${"%.2f".format(voltageAtStrip)} В."
        dropPercent <= 10.0 ->
            "Падение ${"%.1f".format(dropPercent)}% — на пределе. Лента ≈ ${"%.2f".format(voltageAtStrip)} В; усильте кабель" +
                (minSection?.let { " (от $it мм²)" } ?: "") + "."
        else ->
            "Падение ${"%.1f".format(dropPercent)}% — критично для ${supplyVolts.toInt()} В. Лента ≈ ${"%.2f".format(voltageAtStrip)} В; " +
                "обязательно увеличьте сечение" + (minSection?.let { " до ≥$it мм²" } ?: "") + "."
    }

    private fun buildLedFeederHint(
        input: LedStripInput,
        loadA: Double,
        feederDrop: LedDcFeederResult?,
    ): String {
        val acFromPanel =
            "Для питания БП от щита обычно 2,5 мм² (медь) при токе до 16 А. 1,5 мм² — только для короткой открытой линии."
        val dcHint = when {
            feederDrop != null -> ""
            input.lengthMeters > 8 && loadA > 4 ->
                "Длинная лента: питание с двух сторон или усилить линию от БП."
            input.lengthMeters > 5 && loadA > 2.5 ->
                "Укажите длину и сечение кабеля от щита — проверим падение напряжения."
            else -> "БП в щите: укажите длину и сечение DC-линии для проверки ΔU на ленте."
        }
        return listOf(dcHint, acFromPanel).filter { it.isNotBlank() }.joinToString("\n")
    }

    fun breakerSizing(input: BreakerInput): BreakerResult? {
        val load = currentFromPower(input.powerKw, input.phaseMode, input.voltageVolts, input.cosPhi)
        val design = load * 1.25
        val breaker = standardBreakers.firstOrNull { it >= design } ?: standardBreakers.last()
        val notes = mutableListOf<String>()
        notes += "Номинал автомата ≥ 1,25× рабочий ток (длительная нагрузка)."
        notes += "Защита кабеля (Iном ≤ Iдоп), пусковые токи, характеристики B/C/D и отключение при КЗ в приложении не проверяются."
        if (breaker == standardBreakers.last() && design > breaker) {
            notes += "Нагрузка высокая — нужен расчёт по проекту."
        }
        val cableHint = typicalCopperCableHint(breaker)
        val recommendation = buildString {
            append("Итог: автомат ${breaker} А на ток ≈ ${"%.1f".format(load)} А. ")
            append("Ориентир кабеля медь: $cableHint — уточните в калькуляторе сечения с длиной линии.")
        }
        return BreakerResult(load, design, breaker, recommendation, notes)
    }

    private fun typicalCopperCableHint(breakerAmps: Int): String = when {
        breakerAmps <= 10 -> "1,5 мм²"
        breakerAmps <= 16 -> "2,5 мм²"
        breakerAmps <= 25 -> "4 мм²"
        breakerAmps <= 32 -> "6 мм²"
        breakerAmps <= 40 -> "6–10 мм²"
        breakerAmps <= 63 -> "10–16 мм²"
        else -> "по проекту"
    }

    fun voltageDrop(input: VoltageDropInput): VoltageDropResult? {
        if (input.currentAmps <= 0 || input.lengthMeters <= 0 || input.sectionMm2 <= 0 || input.voltageVolts <= 0) {
            return null
        }
        val rho = if (input.material == ConductorMaterial.Copper) RHO_COPPER else RHO_ALUMINUM
        val drop = when (input.phaseMode) {
            PhaseMode.Single -> 2.0 * input.currentAmps * input.lengthMeters * rho / input.sectionMm2
            PhaseMode.Three -> sqrt(3.0) * input.currentAmps * input.lengthMeters * rho / input.sectionMm2
        }
        val pct = 100.0 * drop / input.voltageVolts
        val limit = input.maxDropPercent ?: defaultMaxVoltageDropPercent(input.lineKind)
        val ok = pct <= limit
        val phaseLabel = when (input.phaseMode) {
            PhaseMode.Single -> "1 фаза"
            PhaseMode.Three -> "3 фазы"
        }
        val kindLabel = when (input.lineKind) {
            ElectricalLineKind.Power -> "силовая/розеточная"
            ElectricalLineKind.Lighting -> "освещение"
        }
        val recommendation = if (ok) {
            "Итог: ΔU ${"%.1f".format(pct)}% — в норме (≤${"%.0f".format(limit)}%, $kindLabel) для $phaseLabel, ${input.voltageVolts.toInt()} В."
        } else {
            "Итог: ΔU ${"%.1f".format(pct)}% — выше ${"%.0f".format(limit)}% ($kindLabel); увеличьте сечение или укоротите линию ($phaseLabel, ${input.voltageVolts.toInt()} В)."
        }
        return VoltageDropResult(drop, pct, limit, ok, recommendation)
    }

    fun lightingPower(input: LightingPowerInput): LightingPowerResult? {
        if (input.areaSqM <= 0 || input.wattsPerSqM <= 0) return null
        val totalW = input.areaSqM * input.wattsPerSqM
        val i = totalW / 220.0
        val recommendation = buildString {
            append("Итог (ориентир, не расчёт освещённости по СП): ≈${"%.0f".format(totalW)} Вт. ")
            when {
                i <= 10 -> append("Для LED обычно 1,5 мм² меди на группу; автомат 10 А.")
                i <= 16 -> append("Проверьте автомат 10–16 А и кабель 1,5–2,5 мм²; при длинной линии — расчёт ΔU.")
                else -> append("Ток высокий — разбейте на несколько линий или расчёт сечения по длине.")
            }
        }
        return LightingPowerResult(totalW, i, recommendation)
    }

    fun rcdHint(input: RcdHintInput): RcdHintResult {
        val leakage = when (input.sensitivityContext) {
            RcdSensitivityContext.HigherShockRisk -> 10
            RcdSensitivityContext.GroupCircuit -> 30
        }
        val rated = listOf(16, 25, 40, 63, 80)
            .firstOrNull { it >= input.breakerAmps } ?: 80
        val notes = mutableListOf<String>()
        notes += "Справочная подсказка, не проектный подбор. Номинал УЗО ≥ номинал автомата линии."
        notes += when (input.sensitivityContext) {
            RcdSensitivityContext.HigherShockRisk ->
                "Контекст повышенного риска: часто рассматривают 10 мА на отдельную линию — уточните по проекту и ПУЭ."
            RcdSensitivityContext.GroupCircuit ->
                "Групповые линии квартиры — типично 30 мА (вводной или групповой диф); не единственный критерий."
        }
        notes += when (input.loadKind) {
            RcdLoadKind.WithElectronics ->
                "Нагрузка с электроникой (LED, БП, зарядки): предпочтительен тип A; тип AC может не срабатывать на пульсирующий DC."
            RcdLoadKind.MostlyClassic ->
                "Преимущественно «классическая» нагрузка: тип AC допустим чаще, но для смешанных линий всё равно рассмотрите тип A."
        }
        val typeHint = if (input.loadKind == RcdLoadKind.WithElectronics) "A" else "A или AC (по нагрузке)"
        val recommendation =
            "Итог: ориентир УЗО ${rated} А / ${leakage} мА, тип $typeHint; номинал не ниже автомата ${input.breakerAmps} А."
        return RcdHintResult(rated, leakage, recommendation, notes)
    }

    fun cableOrder(input: CableOrderInput): CableOrderResult? {
        if (input.runsCount <= 0 || input.segmentLengthsM.isEmpty()) return null
        val sum = input.segmentLengthsM.sum()
        if (sum <= 0) return null
        val reservePct = input.reservePercent.coerceIn(0.0, 50.0)
        val reserve = reservePct / 100.0
        val exact = sum * input.runsCount * (1.0 + reserve)
        val orderM = ceil(exact).toInt()
        val rollLen = input.rollLengthM?.takeIf { it > 0 }
        val rolls = rollLen?.let { roll -> ceil(orderM.toDouble() / roll).toInt() }
        val purchased = rolls?.let { r -> r * rollLen!! }
        val remainder = purchased?.let { it - orderM }
        val notes = mutableListOf<String>()
        if (input.segmentLengthsM.size > 1) {
            val parts = input.segmentLengthsM.joinToString(" + ") { "%.0f".format(it) }
            notes += "Участки одной трассы: $parts = ${"%.1f".format(sum)} м."
        }
        notes += "Расчёт: ${"%.1f".format(sum)} м × ${input.runsCount} трассы × (1 + ${"%.0f".format(reservePct)}% запас) = ${"%.1f".format(exact)} м."
        notes += "Заказ (округление вверх до 1 м): $orderM м."
        if (rolls != null && rollLen != null) {
            notes += "Бухт: $rolls × ${rollLen.toInt()} м = ${"%.0f".format(purchased!!)} м; остаток ≈ ${"%.0f".format(remainder!!)} м."
        }
        val recommendation = buildString {
            append("Итог: закажите $orderM м (с запасом ${"%.1f".format(exact)} м до округления). ")
            if (rolls != null) {
                append("Купить $rolls бухт(и); остаток после работ — не прибавляется к заказу.")
            } else {
                append("Добавьте длину бухты, чтобы посчитать число отрезков.")
            }
        }
        return CableOrderResult(
            exactMetersWithReserve = exact,
            orderMeters = orderM,
            rollsCount = rolls,
            purchasedMeters = purchased,
            remainderMeters = remainder,
            recommendation = recommendation,
            notes = notes,
        )
    }

    private data class Ampacity(val section: Double, val maxAmps: Double)

    private fun minSectionForCurrent(
        amps: Double,
        material: ConductorMaterial,
        mode: CableInstallMode,
        cablesInConduit: CablesInConduit,
    ): Ampacity? {
        return ampacityTable(material, mode)
            .map { Ampacity(it.section, effectiveAmps(it.baseAmps, mode, cablesInConduit)) }
            .firstOrNull { it.maxAmps >= amps }
    }

    private fun effectiveAmps(
        baseAmps: Double,
        mode: CableInstallMode,
        cablesInConduit: CablesInConduit,
    ): Double = baseAmps * installDerating(mode, cablesInConduit)

    /**
     * Для открытой прокладки — коэффициент 1,0 к табл. 1.3.6 / 1.3.7 (3 жилы под нагрузкой).
     * Для трубы базовые токи уже снижены (ориентир по прокладке в коробах/трубах); при 3–4 кабелях — доп. ×0,8/0,9.
     */
    private fun installDerating(mode: CableInstallMode, cablesInConduit: CablesInConduit): Double =
        when (mode) {
            CableInstallMode.OpenAir -> 1.0
            CableInstallMode.InConduit -> when (cablesInConduit) {
                CablesInConduit.OneTwo -> 1.0
                CablesInConduit.ThreeFour -> 0.8
            }
        }

    private fun minSectionForVoltageDrop(
        amps: Double,
        lengthM: Double,
        material: ConductorMaterial,
        phaseMode: PhaseMode,
        voltage: Double,
        maxPercent: Double,
    ): Double? {
        val rho = if (material == ConductorMaterial.Copper) RHO_COPPER else RHO_ALUMINUM
        val maxDropV = voltage * maxPercent / 100.0
        val factor = when (phaseMode) {
            PhaseMode.Single -> 2.0
            PhaseMode.Three -> sqrt(3.0)
        }
        val requiredS = factor * amps * lengthM * rho / maxDropV
        return standardSectionsMm2.firstOrNull { it >= requiredS }
    }

    private fun ampacityAtSection(
        section: Double,
        material: ConductorMaterial,
        mode: CableInstallMode,
        cablesInConduit: CablesInConduit,
    ): Double? {
        val row = ampacityTable(material, mode).lastOrNull { it.section == section }
            ?: ampacityTable(material, mode).firstOrNull { it.section >= section }
        return row?.let { effectiveAmps(it.baseAmps, mode, cablesInConduit) }
    }

    private data class AmpacityRow(val section: Double, val baseAmps: Double)

    /** Базовые длительные токи: открыто — табл. 1.3.6 / 1.3.7; в трубе — ориентир по прокладке в коробах/трубах. */
    private fun ampacityTable(material: ConductorMaterial, mode: CableInstallMode): List<AmpacityRow> = when (material) {
        ConductorMaterial.Copper -> when (mode) {
            CableInstallMode.InConduit -> listOf(
                AmpacityRow(1.5, 15.0), AmpacityRow(2.5, 21.0), AmpacityRow(4.0, 27.0), AmpacityRow(6.0, 34.0),
                AmpacityRow(10.0, 46.0), AmpacityRow(16.0, 62.0), AmpacityRow(25.0, 80.0), AmpacityRow(35.0, 96.0),
                AmpacityRow(50.0, 115.0), AmpacityRow(70.0, 140.0), AmpacityRow(95.0, 170.0),
            )
            CableInstallMode.OpenAir -> listOf(
                AmpacityRow(1.5, 19.0), AmpacityRow(2.5, 27.0), AmpacityRow(4.0, 38.0), AmpacityRow(6.0, 50.0),
                AmpacityRow(10.0, 70.0), AmpacityRow(16.0, 90.0), AmpacityRow(25.0, 115.0), AmpacityRow(35.0, 140.0),
                AmpacityRow(50.0, 175.0), AmpacityRow(70.0, 215.0), AmpacityRow(95.0, 260.0),
            )
        }
        ConductorMaterial.Aluminum -> when (mode) {
            CableInstallMode.InConduit -> listOf(
                AmpacityRow(2.5, 15.0), AmpacityRow(4.0, 21.0), AmpacityRow(6.0, 26.0), AmpacityRow(10.0, 36.0),
                AmpacityRow(16.0, 48.0), AmpacityRow(25.0, 63.0), AmpacityRow(35.0, 75.0), AmpacityRow(50.0, 90.0),
                AmpacityRow(70.0, 110.0), AmpacityRow(95.0, 130.0),
            )
            CableInstallMode.OpenAir -> listOf(
                AmpacityRow(2.5, 21.0), AmpacityRow(4.0, 29.0), AmpacityRow(6.0, 38.0), AmpacityRow(10.0, 55.0),
                AmpacityRow(16.0, 70.0), AmpacityRow(25.0, 90.0), AmpacityRow(35.0, 105.0), AmpacityRow(50.0, 130.0),
                AmpacityRow(70.0, 165.0), AmpacityRow(95.0, 200.0),
            )
        }
    }

    private fun formatSection(s: Double): String =
        if (s == s.toLong().toDouble()) s.toLong().toString() else "%.1f".format(s)
}

fun parseElectricalDouble(raw: String): Double? = raw.trim().replace(',', '.').toDoubleOrNull()
