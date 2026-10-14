package ru.hdlabs.electrosmeta.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            animationSpeedPercent = prefs[Keys.ANIM_SPEED] ?: 115,
            animationEnabled = prefs[Keys.ANIM_ENABLED] ?: true,
            powerSaveMode = prefs[Keys.POWER_SAVE] ?: false,
            uiMotionLevel = UiMotionLevel.fromStorage(prefs[Keys.UI_MOTION_LEVEL]),
            companyName = prefs[Keys.COMPANY] ?: "HDLabs",
            performerTitle = prefs[Keys.PERFORMER] ?: "Электромонтажные работы",
            inn = prefs[Keys.INN] ?: "",
            ogrnip = prefs[Keys.OGRNIP] ?: "",
            phone = prefs[Keys.PHONE] ?: "",
            telegram = prefs[Keys.TELEGRAM] ?: "",
            maxContact = prefs[Keys.MAX] ?: "",
            pdfTitle = prefs[Keys.PDF_TITLE] ?: "Предварительная смета",
            pdfIntro = prefs[Keys.PDF_INTRO] ?: AppSettings().pdfIntro,
            pdfFooter = prefs[Keys.PDF_FOOTER] ?: AppSettings().pdfFooter,
            offerValidityDays = prefs[Keys.VALIDITY] ?: 14,
            defaultTaxPercent = prefs[Keys.TAX_PERCENT] ?: 0.0,
            taxLabel = prefs[Keys.TAX_LABEL] ?: "Без НДС",
            showComplexityInPdf = prefs[Keys.SHOW_COMPLEXITY] ?: false,
            categoryHighlightStyle = CategoryHighlightStyle.fromStorage(prefs[Keys.CATEGORY_HIGHLIGHT]),
        )
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { prefs ->
            val current = AppSettings(
                animationSpeedPercent = prefs[Keys.ANIM_SPEED] ?: 115,
                animationEnabled = prefs[Keys.ANIM_ENABLED] ?: true,
                powerSaveMode = prefs[Keys.POWER_SAVE] ?: false,
                uiMotionLevel = UiMotionLevel.fromStorage(prefs[Keys.UI_MOTION_LEVEL]),
                companyName = prefs[Keys.COMPANY] ?: "HDLabs",
                performerTitle = prefs[Keys.PERFORMER] ?: "Электромонтажные работы",
                inn = prefs[Keys.INN] ?: "",
                ogrnip = prefs[Keys.OGRNIP] ?: "",
                phone = prefs[Keys.PHONE] ?: "",
                telegram = prefs[Keys.TELEGRAM] ?: "",
                maxContact = prefs[Keys.MAX] ?: "",
                pdfTitle = prefs[Keys.PDF_TITLE] ?: "Предварительная смета",
                pdfIntro = prefs[Keys.PDF_INTRO] ?: AppSettings().pdfIntro,
                pdfFooter = prefs[Keys.PDF_FOOTER] ?: AppSettings().pdfFooter,
                offerValidityDays = prefs[Keys.VALIDITY] ?: 14,
                defaultTaxPercent = prefs[Keys.TAX_PERCENT] ?: 0.0,
                taxLabel = prefs[Keys.TAX_LABEL] ?: "Без НДС",
                showComplexityInPdf = prefs[Keys.SHOW_COMPLEXITY] ?: false,
                categoryHighlightStyle = CategoryHighlightStyle.fromStorage(prefs[Keys.CATEGORY_HIGHLIGHT]),
            )
            val updated = transform(current)
            prefs[Keys.ANIM_SPEED] = updated.animationSpeedPercent
            prefs[Keys.ANIM_ENABLED] = updated.animationEnabled
            prefs[Keys.POWER_SAVE] = updated.powerSaveMode
            prefs[Keys.COMPANY] = updated.companyName
            prefs[Keys.PERFORMER] = updated.performerTitle
            prefs[Keys.INN] = updated.inn
            prefs[Keys.OGRNIP] = updated.ogrnip
            prefs[Keys.PHONE] = updated.phone
            prefs[Keys.TELEGRAM] = updated.telegram
            prefs[Keys.MAX] = updated.maxContact
            prefs[Keys.PDF_TITLE] = updated.pdfTitle
            prefs[Keys.PDF_INTRO] = updated.pdfIntro
            prefs[Keys.PDF_FOOTER] = updated.pdfFooter
            prefs[Keys.VALIDITY] = updated.offerValidityDays
            prefs[Keys.TAX_PERCENT] = updated.defaultTaxPercent
            prefs[Keys.TAX_LABEL] = updated.taxLabel
            prefs[Keys.SHOW_COMPLEXITY] = updated.showComplexityInPdf
            prefs[Keys.CATEGORY_HIGHLIGHT] = updated.categoryHighlightStyle.name
            prefs[Keys.UI_MOTION_LEVEL] = updated.uiMotionLevel.name
        }
    }

    suspend fun pinFixedAppearance() {
        context.dataStore.edit { prefs ->
            if (prefs[Keys.APPEARANCE_PINNED] == true) return@edit
            prefs[Keys.UI_MOTION_LEVEL] = UiMotionLevel.Balanced.name
            prefs[Keys.CATEGORY_HIGHLIGHT] = CategoryHighlightStyle.GradientRing.name
            prefs[Keys.APPEARANCE_PINNED] = true
        }
    }

    private object Keys {
        val ANIM_SPEED = intPreferencesKey("anim_speed")
        val ANIM_ENABLED = booleanPreferencesKey("anim_enabled")
        val POWER_SAVE = booleanPreferencesKey("power_save")
        val COMPANY = stringPreferencesKey("company")
        val PERFORMER = stringPreferencesKey("performer")
        val INN = stringPreferencesKey("inn")
        val OGRNIP = stringPreferencesKey("ogrnip")
        val PHONE = stringPreferencesKey("phone")
        val TELEGRAM = stringPreferencesKey("telegram")
        val MAX = stringPreferencesKey("max")
        val PDF_TITLE = stringPreferencesKey("pdf_title")
        val PDF_INTRO = stringPreferencesKey("pdf_intro")
        val PDF_FOOTER = stringPreferencesKey("pdf_footer")
        val VALIDITY = intPreferencesKey("validity")
        val TAX_PERCENT = doublePreferencesKey("tax_percent")
        val TAX_LABEL = stringPreferencesKey("tax_label")
        val SHOW_COMPLEXITY = booleanPreferencesKey("show_complexity")
        val CATEGORY_HIGHLIGHT = stringPreferencesKey("category_highlight")
        val UI_MOTION_LEVEL = stringPreferencesKey("ui_motion_level")
        val APPEARANCE_PINNED = booleanPreferencesKey("appearance_pinned_v32")
    }
}
