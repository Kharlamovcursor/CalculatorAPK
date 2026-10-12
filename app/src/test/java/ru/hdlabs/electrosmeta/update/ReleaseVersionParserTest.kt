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
}
