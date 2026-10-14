package ru.hdlabs.electrosmeta.domain.electrical

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        val r = cableSectionTz(10.0, ConductorMaterial.Copper, CableInstallMode.InConduit)
        assertNotNull(r)
        assertEquals(1.5, r!!.recommendedSectionMm2, 0.0)
    }

    @Test
    fun tzCableSection16AmpsCopperInConduit() {
        val r = cableSectionTz(16.0, ConductorMaterial.Copper, CableInstallMode.InConduit)
        assertNotNull(r)
        assertEquals(2.5, r!!.recommendedSectionMm2, 0.0)
    }

    @Test
    fun tzCableSection25AmpsCopperInConduit() {
        val r = cableSectionTz(25.0, ConductorMaterial.Copper, CableInstallMode.InConduit)
        assertNotNull(r)
        assertEquals(4.0, r!!.recommendedSectionMm2, 0.0)
    }

    @Test
    fun tzCableSection32AmpsCopperOpen() {
        val r = cableSectionTz(32.0, ConductorMaterial.Copper, CableInstallMode.OpenAir)
        assertNotNull(r)
        // По допустимому току в воздухе (≈38 А на 4 мм²) достаточно 4 мм²; 6 мм² — запас на объекте.
        assertEquals(4.0, r!!.recommendedSectionMm2, 0.0)
    }

    @Test
    fun cableSectionSameFor220And380WithoutLength() {
        val base = CableSectionInput(
            16.0,
            ConductorMaterial.Copper,
            CableInstallMode.InConduit,
            null,
            PhaseMode.Single,
            220.0,
        )
        val at380 = base.copy(phaseMode = PhaseMode.Three, voltageVolts = 380.0)
        val r220 = ElectricalCalculators.cableSection(base)
        val r380 = ElectricalCalculators.cableSection(at380)
        assertNotNull(r220)
        assertNotNull(r380)
        assertEquals(r220!!.recommendedSectionMm2, r380!!.recommendedSectionMm2, 0.0)
    }

    @Test
    fun tzCableSection16AmpsAluminumInConduit() {
        val r = cableSectionTz(16.0, ConductorMaterial.Aluminum, CableInstallMode.InConduit)
        assertNotNull(r)
        assertEquals(4.0, r!!.recommendedSectionMm2, 0.0)
    }

    @Test
    fun cableSectionIncreasesWithMoreCablesInConduit() {
        val fewer = ElectricalCalculators.cableSection(
            CableSectionInput(
                19.0,
                ConductorMaterial.Copper,
                CableInstallMode.InConduit,
                null,
                PhaseMode.Single,
                220.0,
                cablesInConduit = CablesInConduit.OneTwo,
            ),
        )
        val more = ElectricalCalculators.cableSection(
            CableSectionInput(
                19.0,
                ConductorMaterial.Copper,
                CableInstallMode.InConduit,
                null,
                PhaseMode.Single,
                220.0,
                cablesInConduit = CablesInConduit.ThreeFour,
            ),
        )
        assertNotNull(fewer)
        assertNotNull(more)
        assertTrue(more!!.recommendedSectionMm2 > fewer!!.recommendedSectionMm2)
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
    fun tzLedDcFeederDropExceedsOnThinCable() {
        val loadA = 5.0 * 14.4 / 12.0
        val drop = ElectricalCalculators.ledDcFeederDrop(
            loadAmps = loadA,
            supplyVolts = 12.0,
            feeder = LedDcFeederInput(15.0, 1.5, ConductorMaterial.Copper),
        )
        assertNotNull(drop)
        assertEquals(2.10, drop!!.dropVolts, 0.05)
        assertEquals(17.5, drop.dropPercent, 0.5)
        assertTrue(!drop.withinLimit)
        assertEquals(10.0, drop.minSectionMm2!!, 0.0)
    }

    @Test
    fun tzLedDcFeederDropWithinLimit() {
        val loadA = 5.0 * 14.4 / 12.0
        val drop = ElectricalCalculators.ledDcFeederDrop(
            loadAmps = loadA,
            supplyVolts = 12.0,
            feeder = LedDcFeederInput(3.0, 4.0, ConductorMaterial.Copper),
        )
        assertNotNull(drop)
        assertEquals(0.16, drop!!.dropVolts, 0.03)
        assertEquals(1.3, drop.dropPercent, 0.2)
        assertTrue(drop.withinLimit)
    }

    @Test
    fun ledStripAt5Volts() {
        val r = ElectricalCalculators.ledStrip(LedStripInput(2.0, 10.0, 5.0))
        assertNotNull(r)
        assertEquals(20.0, r!!.totalWatts, 0.01)
        assertEquals(4.0, r.loadAmps, 0.01)
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
        assertEquals(33, r!!.orderMeters)
        assertEquals(1, r.rollsCount)
    }

    @Test
    fun cableOrder12mThreeIdenticalTraces() {
        val r = ElectricalCalculators.cableOrder(
            CableOrderInput(listOf(12.0), runsCount = 3, reservePercent = 0.0, rollLengthM = null),
        )
        assertNotNull(r)
        assertEquals(36, r!!.orderMeters)
    }

    @Test
    fun cableOrderAcceptanceTestA() {
        val r = ElectricalCalculators.cableOrder(
            CableOrderInput(listOf(12.0), runsCount = 3, reservePercent = 10.0, rollLengthM = null),
        )
        assertNotNull(r)
        assertEquals(39.6, r!!.exactMetersWithReserve, 0.01)
        assertEquals(40, r.orderMeters)
    }

    @Test
    fun cableOrderDoesNotSumHiddenDemoSegments() {
        val wrong = ElectricalCalculators.cableOrder(
            CableOrderInput(listOf(12.0, 8.0, 5.0), runsCount = 3, reservePercent = 10.0, rollLengthM = null),
        )
        val right = ElectricalCalculators.cableOrder(
            CableOrderInput(listOf(12.0), runsCount = 3, reservePercent = 10.0, rollLengthM = null),
        )
        assertNotNull(wrong)
        assertNotNull(right)
        assertEquals(83, wrong!!.orderMeters)
        assertEquals(40, right!!.orderMeters)
    }

    @Test
    fun cableSectionLightingStricterDropAt30m() {
        val power = ElectricalCalculators.cableSection(
            CableSectionInput(
                16.0,
                ConductorMaterial.Copper,
                CableInstallMode.InConduit,
                30.0,
                PhaseMode.Single,
                220.0,
                lineKind = ElectricalLineKind.Power,
            ),
        )
        val light = ElectricalCalculators.cableSection(
            CableSectionInput(
                16.0,
                ConductorMaterial.Copper,
                CableInstallMode.InConduit,
                30.0,
                PhaseMode.Single,
                220.0,
                lineKind = ElectricalLineKind.Lighting,
            ),
        )
        assertNotNull(power)
        assertNotNull(light)
        assertEquals(2.5, power!!.recommendedSectionMm2, 0.0)
        assertEquals(4.0, light!!.recommendedSectionMm2, 0.0)
    }

    @Test
    fun ledStripMains220() {
        val r = ElectricalCalculators.ledStrip(LedStripInput(2.0, 10.0, 220.0))
        assertNotNull(r)
        assertTrue(r!!.isMainsAc)
        assertNull(r.dcFeeder)
    }

    @Test
    fun voltageDropSinglePhaseFormula() {
        val r = ElectricalCalculators.voltageDrop(
            VoltageDropInput(10.0, 20.0, 2.5, ConductorMaterial.Copper, PhaseMode.Single, 220.0),
        )
        assertNotNull(r)
        assertEquals(2.8, r!!.dropVolts, 0.05)
        assertEquals(1.27, r.dropPercent, 0.05)
        assertTrue(r.withinLimit)
    }

    @Test
    fun threeCablesInConduitDeratesAmpacity() {
        val oneTwo = ElectricalCalculators.cableSection(
            CableSectionInput(
                18.0,
                ConductorMaterial.Copper,
                CableInstallMode.InConduit,
                null,
                PhaseMode.Single,
                220.0,
                cablesInConduit = CablesInConduit.OneTwo,
            ),
        )
        val threeFour = ElectricalCalculators.cableSection(
            CableSectionInput(
                18.0,
                ConductorMaterial.Copper,
                CableInstallMode.InConduit,
                null,
                PhaseMode.Single,
                220.0,
                cablesInConduit = CablesInConduit.ThreeFour,
            ),
        )
        assertNotNull(oneTwo)
        assertNotNull(threeFour)
        assertEquals(2.5, oneTwo!!.recommendedSectionMm2, 0.0)
        assertEquals(4.0, threeFour!!.recommendedSectionMm2, 0.0)
    }

    @Test
    fun lightingPower() {
        val r = ElectricalCalculators.lightingPower(LightingPowerInput(20.0, 15.0))
        assertNotNull(r)
        assertEquals(300.0, r!!.totalWatts, 0.01)
        assertTrue(abs(r.approxCurrentSinglePhase220 - 300.0 / 220.0) < 0.01)
    }

    private fun cableSectionTz(
        amps: Double,
        material: ConductorMaterial,
        install: CableInstallMode,
    ) = ElectricalCalculators.cableSection(
        CableSectionInput(
            currentAmps = amps,
            material = material,
            installMode = install,
            lengthMeters = null,
            phaseMode = PhaseMode.Single,
            voltageVolts = 220.0,
        ),
    )
}
