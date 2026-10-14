package ru.hdlabs.electrosmeta.update

import org.junit.Assert.assertEquals
import org.junit.Test

class ReleaseVersionParserTest {
    @Test
    fun vCodeTag() {
        assertEquals(14, ReleaseVersionParser.parseVersionCode("v14", 0))
    }

    @Test
    fun semanticAppVersion() {
        assertEquals(15, ReleaseVersionParser.parseVersionCode("1.0.15", 0))
    }

    @Test
    fun displayUsesReleaseName() {
        assertEquals(
            "1.1.0",
            ReleaseVersionParser.displayVersionName("v36", 36, "1.1.0"),
        )
    }

    @Test
    fun displayV36WithoutReleaseName() {
        assertEquals(
            "1.1.0",
            ReleaseVersionParser.displayVersionName("v36", 36, null),
        )
    }

    @Test
    fun displayV35Legacy() {
        assertEquals(
            "1.0.35",
            ReleaseVersionParser.displayVersionName("v35", 35, null),
        )
    }
}
