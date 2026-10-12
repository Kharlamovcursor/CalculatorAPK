package ru.hdlabs.electrosmeta.data.settings

enum class CategoryHighlightStyle {
    SoftGlow,
    PulseBorder,
    GradientRing,
    ;

    companion object {
        fun fromStorage(value: String?): CategoryHighlightStyle = when (value) {
            PulseBorder.name -> PulseBorder
            GradientRing.name -> GradientRing
            else -> SoftGlow
        }
    }
}
