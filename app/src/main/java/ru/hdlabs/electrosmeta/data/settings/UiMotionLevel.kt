package ru.hdlabs.electrosmeta.data.settings

enum class UiMotionLevel {
    Subtle,
    Balanced,
    Expressive,
    ;

    companion object {
        fun fromStorage(value: String?): UiMotionLevel = when (value) {
            Subtle.name -> Subtle
            Expressive.name -> Expressive
            else -> Balanced
        }
    }
}
