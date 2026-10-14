package ru.hdlabs.electrosmeta.update

internal object ReleaseVersionParser {
    /** Первый `versionCode` линейки 1.1.x в APK (см. `app/build.gradle.kts`). */
    const val VERSION_1_1_BASE_CODE = 36

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

    /**
     * Подпись версии для диалога обновления: сначала [releaseName] с GitHub, иначе по тегу и [versionCode].
     */
    fun displayVersionName(tagName: String, versionCode: Int, releaseName: String?): String {
        releaseName?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        val stripped = tagName.trim().removePrefix("v").removePrefix("V")
        stripped.toIntOrNull()?.let { code ->
            return if (code >= VERSION_1_1_BASE_CODE) {
                "1.1.${code - VERSION_1_1_BASE_CODE}"
            } else {
                "1.0.$code"
            }
        }
        return tagName
    }
}
