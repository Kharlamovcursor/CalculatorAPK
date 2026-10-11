package ru.hdlabs.electrosmeta.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class EstimateCalculatorTest {
    @Test
    fun subtotalAndComplexity() {
        val result = EstimateCalculator.calculate(
            CalculationInput(
                linesSubtotalKopecks = 100_000,
                complexityMultiplier = 1.2,
                roundToWholeRubles = false,
            ),
        )
        assertEquals(100_000L, result.subtotalKopecks)
        assertEquals(120_000L, result.afterComplexityKopecks)
        assertEquals(120_000L, result.totalKopecks)
    }

    @Test
    fun discountTaxAndRounding() {
        val result = EstimateCalculator.calculate(
            CalculationInput(
                linesSubtotalKopecks = 10_050,
                discountPercent = 10.0,
                taxPercent = 20.0,
                roundToWholeRubles = true,
            ),
        )
        assertEquals(1_005L, result.discountKopecks)
        assertEquals(9_045L, result.afterDiscountKopecks)
        assertEquals(1_809L, result.taxKopecks)
        assertEquals(10_900L, result.totalKopecks)
    }

    @Test
    fun lineTotal() {
        assertEquals(2_750L, EstimateCalculator.lineTotalKopecks(2.5, 1_100))
    }
}
