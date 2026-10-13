package ru.hdlabs.electrosmeta.data.settings

enum class CategoryHighlightStyle {
    SoftGlow,
    PulseBorder,
    GradientRing,
    Noticeable,
    ;

    companion object {
        fun fromStorage(value: String?): CategoryHighlightStyle = when (value) {
            PulseBorder.name -> PulseBorder
            GradientRing.name -> GradientRing
            Noticeable.name -> Noticeable
            else -> SoftGlow
        }
    }
}
