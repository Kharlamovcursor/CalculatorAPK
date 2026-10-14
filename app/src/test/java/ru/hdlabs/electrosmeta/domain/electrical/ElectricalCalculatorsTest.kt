package ru.hdlabs.electrosmeta.domain.electrical

import ru.hdlabs.electrosmeta.domain.electrical.ConductorMaterial
import ru.hdlabs.electrosmeta.domain.electrical.LedDcFeederInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ElectricalCalculatorsTest {
    @Test
    fun singlePhaseCurrentFromPower() {
        val i = ElectricalCalculators.currentFromPower(2.2, PhaseMode.Single, 220.0, 1.0)
        assertEquals(10.0, i, 0.1)
    }

    @Test
    fun threePhaseCurrentFromPower() {
        val i = ElectricalCalculators.currentFromPower(5.5, PhaseMode.Three, 380.0, 0.95)
        assertTrue(i > 8.0 && i < 9.5)
    }

    @Test
    fun cableSectionFor10Amps() {
        val r = ElectricalCalculators.cableSection(
            CableSectionInput(
                currentAmps = 10.0,
                material = ConductorMaterial.Copper,
                installMode = CableInstallMode.InConduit,
                lengthMeters = null,
                phaseMode = PhaseMode.Single,
                voltageVolts = 220.0,
            ),
        )
        assertNotNull(r)
        assertEquals(1.5, r!!.recommendedSectionMm2, 0.0)
    }

    @Test
    fun cableSectionIncreasesWithLongLength() {
        val short = ElectricalCalculators.cableSection(
            CableSectionInput(16.0, ConductorMaterial.Copper, CableInstallMode.InConduit, 5.0, PhaseMode.Single, 220.0),
        )
        val long = ElectricalCalculators.cableSection(
            CableSectionInput(16.0, ConductorMaterial.Copper, CableInstallMode.InConduit, 40.0, PhaseMode.Single, 220.0),
        )
        assertNotNull(short)
        assertNotNull(long)
        assertTrue(long!!.recommendedSectionMm2 >= short!!.recommendedSectionMm2)
    }

    @Test
    fun ledStripPsuReserve() {
        val r = ElectricalCalculators.ledStrip(LedStripInput(5.0, 14.4, 12.0, 1.2))
        assertNotNull(r)
        assertEquals(72.0, r!!.totalWatts, 0.01)
        assertEquals(6.0, r.loadAmps, 0.01)
        assertTrue(r.psuWattsMin >= 86.0)
    }

    @Test
    fun ledDcFeederDropTooHighOnThinCable() {
        val drop = ElectricalCalculators.ledDcFeederDrop(
            loadAmps = 6.0,
            supplyVolts = 12.0,
            feeder = LedDcFeederInput(15.0, 1.5, ConductorMaterial.Copper),
        )
        assertNotNull(drop)
        assertTrue(drop!!.dropVolts > 0.35)
        assertTrue(!drop.withinLimit)
        assertTrue(drop.minSectionMm2!! >= 2.5)
    }

    @Test
    fun ledStripWithDcFeeder() {
        val r = ElectricalCalculators.ledStrip(
            LedStripInput(
                5.0,
                14.4,
                12.0,
                dcFeeder = LedDcFeederInput(5.0, 4.0, ConductorMaterial.Copper),
            ),
        )
        assertNotNull(r?.dcFeeder)
        assertTrue(r!!.dcFeeder!!.withinLimit)
    }

    @Test
    fun breakerRoundsUp() {
        val r = ElectricalCalculators.breakerSizing(BreakerInput(3.0, PhaseMode.Single, 220.0, 1.0))
        assertNotNull(r)
        assertEquals(20, r!!.recommendedBreakerAmps)
    }

    @Test
    fun voltageDropSinglePhase() {
        val r = ElectricalCalculators.voltageDrop(
            VoltageDropInput(10.0, 20.0, 2.5, ConductorMaterial.Copper, PhaseMode.Single, 220.0),
        )
        assertNotNull(r)
        assertTrue(r!!.dropPercent > 1.0)
    }

    @Test
    fun cableOrderWithReserve() {
        val r = ElectricalCalculators.cableOrder(
            CableOrderInput(listOf(10.0, 5.0), runsCount = 2, reservePercent = 10.0, rollLengthM = 100.0),
        )
        assertNotNull(r)
        assertEquals(33.0, r!!.totalMeters, 0.01)
        assertEquals(1, r.rollsCount)
    }

    @Test
    fun lightingPower() {
        val r = ElectricalCalculators.lightingPower(LightingPowerInput(20.0, 15.0))
        assertNotNull(r)
        assertEquals(300.0, r!!.totalWatts, 0.01)
        assertTrue(abs(r.approxCurrentSinglePhase220 - 300.0 / 220.0) < 0.01)
    }
}
