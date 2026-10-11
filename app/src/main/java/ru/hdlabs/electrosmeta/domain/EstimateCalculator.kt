package ru.hdlabs.electrosmeta.domain

import kotlin.math.roundToLong

data class CalculationInput(
    val linesSubtotalKopecks: Long,
    val discountPercent: Double = 0.0,
    val discountAmountKopecks: Long = 0,
    val extraFeeKopecks: Long = 0,
    val complexityMultiplier: Double = 1.0,
    val taxPercent: Double = 0.0,
    val roundToWholeRubles: Boolean = true,
)

data class CalculationResult(
    val subtotalKopecks: Long,
    val discountKopecks: Long,
    val afterDiscountKopecks: Long,
    val afterComplexityKopecks: Long,
    val taxKopecks: Long,
    val totalKopecks: Long,
)

object EstimateCalculator {
    fun calculate(input: CalculationInput): CalculationResult {
        val subtotal = input.linesSubtotalKopecks.coerceAtLeast(0)
        val percentDiscount = (subtotal * input.discountPercent / 100.0).roundToLong()
        val discount = (percentDiscount + input.discountAmountKopecks).coerceAtMost(subtotal)
        val afterDiscount = (subtotal - discount + input.extraFeeKopecks).coerceAtLeast(0)
        val afterComplexity = (afterDiscount * input.complexityMultiplier).roundToLong().coerceAtLeast(0)
        val tax = (afterComplexity * input.taxPercent / 100.0).roundToLong()
        var total = afterComplexity + tax
        if (input.roundToWholeRubles) {
            total = ((total + 50) / 100) * 100
        }
        return CalculationResult(
            subtotalKopecks = subtotal,
            discountKopecks = discount,
            afterDiscountKopecks = afterDiscount,
            afterComplexityKopecks = afterComplexity,
            taxKopecks = tax,
            totalKopecks = total,
        )
    }

    fun lineTotalKopecks(quantity: Double, unitPriceKopecks: Long): Long {
        return (quantity * unitPriceKopecks).roundToLong().coerceAtLeast(0)
    }
}

fun formatRubles(kopecks: Long): String {
    val rub = kopecks / 100
    val kop = kotlin.math.abs(kopecks % 100)
    return if (kop == 0L) {
        "$rub ₽"
    } else {
        "$rub,$kop ₽"
    }
}

fun parseRublesToKopecks(text: String): Long? {
    val cleaned = text.trim().replace("₽", "").replace(" ", "").replace(',', '.')
    if (cleaned.isEmpty()) return null
    val value = cleaned.toDoubleOrNull() ?: return null
    return (value * 100).roundToLong()
}
