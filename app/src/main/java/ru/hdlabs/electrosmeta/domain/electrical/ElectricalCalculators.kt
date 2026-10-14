package ru.hdlabs.electrosmeta.domain.electrical

import kotlin.math.ceil
import kotlin.math.sqrt

enum class PhaseMode { Single, Three }

enum class ConductorMaterial { Copper, Aluminum }

/** Способ прокладки (упрощённо по ПУЭ: в трубе / открыто). */
enum class CableInstallMode { InConduit, OpenAir }

enum class RcdLocationHint { DryRooms, WetRooms }

data class CableSectionInput(
    val currentAmps: Double,
    val material: ConductorMaterial,
    val installMode: CableInstallMode,
    val lengthMeters: Double?,
    val phaseMode: PhaseMode,
    val voltageVolts: Double,
    val maxVoltageDropPercent: Double = 5.0,
)

data class CableSectionResult(
    val recommendedSectionMm2: Double,
    val allowableAmpsAtSection: Double,
    val voltageDropPercent: Double?,
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
    val dcFeeder: LedDcFeederResult? = null,
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
    val notes: List<String>,
)

data class VoltageDropInput(
    val currentAmps: Double,
    val lengthMeters: Double,
    val sectionMm2: Double,
    val material: ConductorMaterial,
    val phaseMode: PhaseMode,
    val voltageVolts: Double,
)

data class VoltageDropResult(
    val dropVolts: Double,
    val dropPercent: Double,
    val withinLimit: Boolean,
)

data class LightingPowerInput(
    val areaSqM: Double,
    val wattsPerSqM: Double,
)

data class LightingPowerResult(
    val totalWatts: Double,
    val approxCurrentSinglePhase220: Double,
)

data class RcdHintInput(
    val breakerAmps: Int,
    val location: RcdLocationHint,
)

data class RcdHintResult(
    val rcdRatedAmps: Int,
    val leakageMa: Int,
    val notes: List<String>,
)

data class CableOrderInput(
    val segmentLengthsM: List<Double>,
    val runsCount: Int,
    val reservePercent: Double,
    val rollLengthM: Double?,
)

data class CableOrderResult(
    val totalMeters: Double,
    val rollsCount: Int?,
)

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

    fun cableSection(input: CableSectionInput): CableSectionResult? {
        if (input.currentAmps <= 0 || input.voltageVolts <= 0) return null
        val notes = mutableListOf<String>()
        val byAmpacity = minSectionForCurrent(input.currentAmps, input.material, input.installMode)
            ?: return null
        var chosen = byAmpacity.section
        var ampsAt = byAmpacity.maxAmps
        val length = input.lengthMeters
        if (length != null && length > 0) {
            val minForDrop = minSectionForVoltageDrop(
                amps = input.currentAmps,
                lengthM = length,
                material = input.material,
                phaseMode = input.phaseMode,
                voltage = input.voltageVolts,
                maxPercent = input.maxVoltageDropPercent,
            )
            if (minForDrop != null && minForDrop > chosen) {
                chosen = minForDrop
                notes += "Сечение увеличено из‑за падения напряжения (≤ ${input.maxVoltageDropPercent}%)."
            }
        }
        if (input.material == ConductorMaterial.Copper && chosen < 1.5) {
            chosen = 1.5
            notes += "Минимум для силовых цепей меди — 1,5 мм² (освещение); для розеток обычно от 2,5 мм²."
        }
        if (input.material == ConductorMaterial.Aluminum && chosen < 2.5) {
            chosen = 2.5
            notes += "Минимум для алюминия в бытовых цепях — 2,5 мм²."
        }
        ampsAt = ampacityAtSection(chosen, input.material, input.installMode) ?: ampsAt
        val dropPct = if (length != null && length > 0) {
            voltageDrop(
                VoltageDropInput(
                    input.currentAmps,
                    length,
                    chosen,
                    input.material,
                    input.phaseMode,
                    input.voltageVolts,
                ),
            )?.dropPercent
        } else {
            null
        }
        notes += "Длительно допустимый ток для ${formatSection(chosen)} мм² ≈ ${ampsAt.toInt()} А (ориентир по ПУЭ, упрощённо)."
        return CableSectionResult(
            recommendedSectionMm2 = chosen,
            allowableAmpsAtSection = ampsAt,
            voltageDropPercent = dropPct,
            notes = notes,
        )
    }

    fun ledStrip(input: LedStripInput): LedStripResult? {
        if (input.lengthMeters <= 0 || input.wattsPerMeter <= 0 || input.supplyVolts <= 0) return null
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
        return LedStripResult(totalW, loadA, psuW, psuA, label, feeder, feederDrop)
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
        return LedDcFeederResult(
            dropVolts = dropV,
            dropPercent = pct,
            voltageAtStrip = atStrip,
            withinLimit = pct <= maxPct,
            maxDropPercent = maxPct,
            minSectionMm2 = minSection,
        )
    }

    private fun buildLedFeederHint(
        input: LedStripInput,
        loadA: Double,
        feederDrop: LedDcFeederResult?,
    ): String {
        if (feederDrop != null) return ""
        return when {
            input.lengthMeters > 8 && loadA > 4 ->
                "Длинная лента: питание с двух сторон или усилить линию от БП."
            input.lengthMeters > 5 && loadA > 2.5 ->
                "Укажите длину и сечение кабеля от щита — проверим падение напряжения."
            else -> "БП в щите: укажите длину и сечение DC-линии для проверки ΔU на ленте."
        }
    }

    fun breakerSizing(input: BreakerInput): BreakerResult? {
        val load = currentFromPower(input.powerKw, input.phaseMode, input.voltageVolts, input.cosPhi)
        val design = load * 1.25
        val breaker = standardBreakers.firstOrNull { it >= design } ?: standardBreakers.last()
        val notes = mutableListOf<String>()
        notes += "Номинал автомата ≥ 1,25× рабочий ток (длительная нагрузка)."
        if (breaker == standardBreakers.last() && design > breaker) {
            notes += "Нагрузка высокая — нужен расчёт по проекту."
        }
        return BreakerResult(load, design, breaker, notes)
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
        return VoltageDropResult(drop, pct, pct <= 5.0)
    }

    fun lightingPower(input: LightingPowerInput): LightingPowerResult? {
        if (input.areaSqM <= 0 || input.wattsPerSqM <= 0) return null
        val totalW = input.areaSqM * input.wattsPerSqM
        val i = totalW / 220.0
        return LightingPowerResult(totalW, i)
    }

    fun rcdHint(input: RcdHintInput): RcdHintResult {
        val leakage = when (input.location) {
            RcdLocationHint.WetRooms -> 10
            RcdLocationHint.DryRooms -> 30
        }
        val rated = listOf(16, 25, 40, 63, 80)
            .firstOrNull { it >= input.breakerAmps } ?: 80
        val notes = mutableListOf<String>()
        notes += "Номинал УЗО ≥ номинал автомата линии."
        notes += if (input.location == RcdLocationHint.WetRooms) {
            "Ванная, душ — чувствительность 10 мА (отдельная линия)."
        } else {
            "Групповые линии квартиры — часто 30 мА (вводной или групповой диф)."
        }
        return RcdHintResult(rated, leakage, notes)
    }

    fun cableOrder(input: CableOrderInput): CableOrderResult? {
        if (input.runsCount <= 0 || input.segmentLengthsM.isEmpty()) return null
        val sum = input.segmentLengthsM.sum()
        if (sum <= 0) return null
        val reserve = input.reservePercent.coerceIn(0.0, 50.0) / 100.0
        val total = sum * input.runsCount * (1.0 + reserve)
        val rolls = input.rollLengthM?.takeIf { it > 0 }?.let { roll ->
            ceil(total / roll).toInt()
        }
        return CableOrderResult(total, rolls)
    }

    private data class Ampacity(val section: Double, val maxAmps: Double)

    private fun minSectionForCurrent(
        amps: Double,
        material: ConductorMaterial,
        mode: CableInstallMode,
    ): Ampacity? {
        return ampacityTable(material, mode)
            .firstOrNull { it.maxAmps >= amps }
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

    private fun ampacityAtSection(section: Double, material: ConductorMaterial, mode: CableInstallMode): Double? =
        ampacityTable(material, mode).lastOrNull { it.section == section }?.maxAmps
            ?: ampacityTable(material, mode).firstOrNull { it.section >= section }?.maxAmps

    private fun ampacityTable(material: ConductorMaterial, mode: CableInstallMode): List<Ampacity> = when (material) {
        ConductorMaterial.Copper -> when (mode) {
            CableInstallMode.InConduit -> listOf(
                Ampacity(1.5, 15.0), Ampacity(2.5, 21.0), Ampacity(4.0, 27.0), Ampacity(6.0, 34.0),
                Ampacity(10.0, 46.0), Ampacity(16.0, 62.0), Ampacity(25.0, 80.0), Ampacity(35.0, 96.0),
                Ampacity(50.0, 115.0), Ampacity(70.0, 140.0), Ampacity(95.0, 170.0),
            )
            CableInstallMode.OpenAir -> listOf(
                Ampacity(1.5, 19.0), Ampacity(2.5, 27.0), Ampacity(4.0, 38.0), Ampacity(6.0, 50.0),
                Ampacity(10.0, 70.0), Ampacity(16.0, 90.0), Ampacity(25.0, 115.0), Ampacity(35.0, 140.0),
                Ampacity(50.0, 175.0), Ampacity(70.0, 215.0), Ampacity(95.0, 260.0),
            )
        }
        ConductorMaterial.Aluminum -> when (mode) {
            CableInstallMode.InConduit -> listOf(
                Ampacity(2.5, 15.0), Ampacity(4.0, 21.0), Ampacity(6.0, 26.0), Ampacity(10.0, 36.0),
                Ampacity(16.0, 48.0), Ampacity(25.0, 63.0), Ampacity(35.0, 75.0), Ampacity(50.0, 90.0),
                Ampacity(70.0, 110.0), Ampacity(95.0, 130.0),
            )
            CableInstallMode.OpenAir -> listOf(
                Ampacity(2.5, 21.0), Ampacity(4.0, 29.0), Ampacity(6.0, 38.0), Ampacity(10.0, 55.0),
                Ampacity(16.0, 70.0), Ampacity(25.0, 90.0), Ampacity(35.0, 105.0), Ampacity(50.0, 130.0),
                Ampacity(70.0, 165.0), Ampacity(95.0, 200.0),
            )
        }
    }

    private fun formatSection(s: Double): String =
        if (s == s.toLong().toDouble()) s.toLong().toString() else "%.1f".format(s)
}

fun parseElectricalDouble(raw: String): Double? = raw.trim().replace(',', '.').toDoubleOrNull()
