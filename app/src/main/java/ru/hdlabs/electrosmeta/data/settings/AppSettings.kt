package ru.hdlabs.electrosmeta.data.settings

data class AppSettings(
    val animationSpeedPercent: Int = 115,
    val animationEnabled: Boolean = true,
    val powerSaveMode: Boolean = false,
    val uiMotionLevel: UiMotionLevel = UiMotionLevel.Balanced,
    val companyName: String = "HDLabs",
    val performerTitle: String = "Электромонтажные работы",
    val inn: String = "",
    val ogrnip: String = "",
    val phone: String = "",
    val telegram: String = "",
    val maxContact: String = "",
    val pdfTitle: String = "Предварительная смета",
    val pdfIntro: String =
        "Ниже приведён перечень планируемых электромонтажных работ и ориентировочная стоимость.",
    val pdfFooter: String =
        "Расчёт является предварительным, не является публичной офертой и может быть уточнён после осмотра объекта.",
    val offerValidityDays: Int = 14,
    val defaultTaxPercent: Double = 0.0,
    val taxLabel: String = "Без НДС",
    val showComplexityInPdf: Boolean = false,
    val categoryHighlightStyle: CategoryHighlightStyle = CategoryHighlightStyle.SoftGlow,
)
