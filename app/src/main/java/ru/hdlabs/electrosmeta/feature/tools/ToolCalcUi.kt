package ru.hdlabs.electrosmeta.feature.tools

import ru.hdlabs.electrosmeta.domain.electrical.ToolCalcReport

internal const val ToolsResidentialHint =
    "Для квартиры, дома и коттеджа — быстрый подбор на объекте (230/400 В)."

internal fun toolStandardWarnings(): List<String> = listOf(
    "Предварительный расчёт, не проект.",
    "КЗ, пусковые токи и полная защита кабеля не проверяются.",
)

internal fun toolStandardNormativeNotes(): List<String> = listOf(
    "Упрощённые таблицы бытовой прокладки; ПУЭ — уточняйте по проекту.",
)

internal fun formatCalcVoltageLabel(selectedUiVolts: Int): String = when (selectedUiVolts) {
    220 -> "230 В (в расчёте 220)"
    380 -> "400 В (в расчёте 380)"
    else -> "$selectedUiVolts В"
}

internal fun baseToolReport(
    headline: String,
    inputs: List<String>,
    methodLines: List<String>,
    substitutionLines: List<String> = emptyList(),
    checks: List<ru.hdlabs.electrosmeta.domain.electrical.ToolCalcCheck> = emptyList(),
    recommendations: List<String> = emptyList(),
): ToolCalcReport = ToolCalcReport(
    headline = headline,
    inputs = inputs,
    methodLines = methodLines,
    substitutionLines = substitutionLines,
    checks = checks,
    warnings = toolStandardWarnings(),
    normativeNotes = toolStandardNormativeNotes(),
    recommendations = recommendations,
)
