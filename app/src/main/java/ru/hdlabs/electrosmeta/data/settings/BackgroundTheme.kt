package ru.hdlabs.electrosmeta.data.settings

enum class BackgroundTheme {
    Space,
    Spring,
    Summer,
    Autumn,
    Winter,
    ;

    val label: String
        get() = when (this) {
            Space -> "Космос"
            Spring -> "Весна"
            Summer -> "Лето"
            Autumn -> "Осень"
            Winter -> "Зима"
        }

    companion object {
        fun fromStorage(value: String?): BackgroundTheme = when (value) {
            Spring.name -> Spring
            Summer.name -> Summer
            Autumn.name -> Autumn
            Winter.name -> Winter
            else -> Space
        }

        val selectable: List<BackgroundTheme> = listOf(Space, Spring, Summer, Autumn, Winter)
    }
}
