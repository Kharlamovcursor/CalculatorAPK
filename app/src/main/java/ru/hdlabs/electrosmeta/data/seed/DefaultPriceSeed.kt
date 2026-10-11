package ru.hdlabs.electrosmeta.data.seed

import ru.hdlabs.electrosmeta.data.local.dao.CategoryDao
import ru.hdlabs.electrosmeta.data.local.dao.ServiceDao
import ru.hdlabs.electrosmeta.data.local.entities.CategoryEntity
import ru.hdlabs.electrosmeta.data.local.entities.ServiceEntity

/**
 * Ориентировочный усреднённый прайс по Санкт-Петербургу (только работы, без материалов).
 */
object DefaultPriceSeed {
    suspend fun seedIfEmpty(categoryDao: CategoryDao, serviceDao: ServiceDao) {
        if (categoryDao.count() > 0) return
        seed(categoryDao, serviceDao)
    }

    suspend fun seed(categoryDao: CategoryDao, serviceDao: ServiceDao) {
        var categoryOrder = 0
        categories.forEach { (categoryName, services) ->
            val categoryId = categoryDao.insert(
                CategoryEntity(name = categoryName, sortOrder = categoryOrder++),
            )
            var serviceOrder = 0
            services.forEach { (name, unit, rub) ->
                serviceDao.insert(
                    ServiceEntity(
                        categoryId = categoryId,
                        name = name,
                        unit = unit,
                        priceKopecks = rub * 100,
                        sortOrder = serviceOrder++,
                    ),
                )
            }
        }
    }

    private val categories: List<Pair<String, List<Triple<String, String, Long>>>> = listOf(
        "Демонтаж" to listOf(
            Triple("Демонтаж розетки/выключателя", "шт.", 150),
            Triple("Демонтаж светильника", "шт.", 250),
            Triple("Демонтаж электропроводки", "м.п.", 80),
            Triple("Демонтаж электрощита", "шт.", 1200),
        ),
        "Разметка и подготовка" to listOf(
            Triple("Разметка трасс под кабель", "м.п.", 60),
            Triple("Пробивка отверстия под кабель (до 20 мм)", "шт.", 350),
            Triple("Уборка и вывоз строительного мусора (электрика)", "компл.", 2500),
        ),
        "Штробление" to listOf(
            Triple("Штроба в гипсокартоне (до 40×40 мм)", "м.п.", 180),
            Triple("Штроба в кирпиче (до 40×40 мм)", "м.п.", 320),
            Triple("Штроба в бетоне (до 40×40 мм)", "м.п.", 450),
            Triple("Штроба в пенобетоне", "м.п.", 240),
        ),
        "Отверстия и подрозетники" to listOf(
            Triple("Отверстие под подрозетник в гипсокартоне", "шт.", 220),
            Triple("Отверстие под подрозетник в кирпиче", "шт.", 350),
            Triple("Отверстие под подрозетник в бетоне", "шт.", 500),
            Triple("Установка подрозетника", "шт.", 180),
        ),
        "Прокладка кабеля" to listOf(
            Triple("Прокладка кабеля в штробе (до 3×2,5)", "м.п.", 130),
            Triple("Прокладка кабеля в гофре", "м.п.", 120),
            Triple("Прокладка кабеля открыто по стене", "м.п.", 100),
            Triple("Прокладка силового кабеля (до 16 мм²)", "м.п.", 220),
        ),
        "Кабель-каналы, гофра, лотки" to listOf(
            Triple("Монтаж кабель-канала (до 40 мм)", "м.п.", 180),
            Triple("Монтаж гофры ПВХ", "м.п.", 90),
            Triple("Монтаж металлического лотка", "м.п.", 350),
        ),
        "Розетки и выключатели" to listOf(
            Triple("Установка розетки (готовая точка)", "шт.", 280),
            Triple("Установка выключателя одноклавишного", "шт.", 300),
            Triple("Установка выключателя двухклавишного", "шт.", 320),
            Triple("Установка диммера", "шт.", 380),
            Triple("Установка проходного выключателя", "шт.", 650),
        ),
        "Распаечные коробки" to listOf(
            Triple("Монтаж распаечной коробки", "шт.", 450),
            Triple("Распайка в коробке (до 5 жил)", "шт.", 550),
        ),
        "Освещение" to listOf(
            Triple("Установка люстры", "шт.", 900),
            Triple("Установка точечного светильника", "шт.", 350),
            Triple("Установка бра/настенного светильника", "шт.", 450),
            Triple("Монтаж LED-ленты с блоком питания", "м.п.", 280),
        ),
        "Электрощиты" to listOf(
            Triple("Монтаж накладного щита (корпус)", "шт.", 800),
            Triple("Монтаж встраиваемого щита в готовую нишу", "шт.", 1200),
            Triple("Устройство ниши под встраиваемый щит", "шт.", 2400),
            Triple("Сборка и расключение щита «под ключ» (квартира)", "компл.", 6000),
            Triple("Сборка щита в частном доме", "компл.", 12000),
        ),
        "Автоматы, УЗО, дифавтоматы" to listOf(
            Triple("Установка автомата 1P", "шт.", 400),
            Triple("Установка УЗО", "шт.", 700),
            Triple("Установка дифавтомата", "шт.", 850),
            Triple("Замена автомата", "шт.", 350),
        ),
        "Подключение оборудования" to listOf(
            Triple("Подключение электроплиты", "шт.", 1500),
            Triple("Подключение варочной панели", "шт.", 1200),
            Triple("Подключение стиральной машины", "шт.", 900),
            Triple("Подключение бойлера", "шт.", 1100),
        ),
        "Заземление" to listOf(
            Triple("Монтаж контура заземления (базовый комплект)", "компл.", 15000),
            Triple("Прокладка шины заземления", "м.п.", 250),
        ),
        "Слаботочные линии" to listOf(
            Triple("Прокладка UTP (интернет)", "м.п.", 90),
            Triple("Прокладка коаксиального кабеля (ТВ)", "м.п.", 90),
            Triple("Установка розетки RJ-45", "шт.", 350),
        ),
        "Диагностика и доп. работы" to listOf(
            Triple("Диагностика неисправности электрики", "час", 1200),
            Triple("Выезд мастера по СПб", "компл.", 1500),
            Triple("Работа на высоте (лестница/стремянка)", "час", 500),
        ),
    )
}
