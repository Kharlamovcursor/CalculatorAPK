package ru.hdlabs.electrosmeta.update

internal object ReleaseVersionParser {
    fun parseVersionCode(tag: String, fallback: Int): Int {
        val stripped = tag.trim().removePrefix("v").removePrefix("V")
        stripped.toIntOrNull()?.let { return it }
        val parts = stripped.split('.').filter { it.isNotEmpty() }
        if (parts.size >= 3) {
            val major = parts[0].toIntOrNull() ?: 0
            val minor = parts[1].toIntOrNull() ?: 0
            val patch = parts[2].toIntOrNull() ?: 0
            if (major == 1 && minor == 0) return patch
            return major * 10000 + minor * 100 + patch
        }
        if (parts.isEmpty()) return fallback
        return parts.take(3).fold(0) { acc, part ->
            acc * 1000 + (part.toIntOrNull()?.coerceIn(0, 999) ?: 0)
        }
    }
}
