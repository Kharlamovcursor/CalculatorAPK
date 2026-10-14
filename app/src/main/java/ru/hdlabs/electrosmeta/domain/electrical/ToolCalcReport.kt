package ru.hdlabs.electrosmeta.domain.electrical

/** Единый структурированный результат расчёта (ТЗ 2.2.0, §2). */
data class ToolCalcCheck(
    val criterion: String,
    val value: String,
    /** null — проверка не выполнялась */
    val passed: Boolean?,
)

data class ToolCalcReport(
    val headline: String,
    val inputs: List<String> = emptyList(),
    val methodLines: List<String> = emptyList(),
    val substitutionLines: List<String> = emptyList(),
    val checks: List<ToolCalcCheck> = emptyList(),
    val warnings: List<String> = emptyList(),
    val normativeNotes: List<String> = emptyList(),
    /** Итоговые рекомендации (строки с «Итог:») */
    val recommendations: List<String> = emptyList(),
)
