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
import ru.hdlabs.electrosmeta.BuildConfig

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            animationSpeedPercent = prefs[Keys.ANIM_SPEED] ?: 100,
            animationEnabled = prefs[Keys.ANIM_ENABLED] ?: true,
            powerSaveMode = prefs[Keys.POWER_SAVE] ?: false,
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
            githubOwner = prefs[Keys.GH_OWNER] ?: BuildConfig.GITHUB_RELEASES_OWNER,
            githubRepo = prefs[Keys.GH_REPO] ?: BuildConfig.GITHUB_RELEASES_REPO,
            showComplexityInPdf = prefs[Keys.SHOW_COMPLEXITY] ?: true,
        )
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { prefs ->
            val current = AppSettings(
                animationSpeedPercent = prefs[Keys.ANIM_SPEED] ?: 100,
                animationEnabled = prefs[Keys.ANIM_ENABLED] ?: true,
                powerSaveMode = prefs[Keys.POWER_SAVE] ?: false,
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
                githubOwner = prefs[Keys.GH_OWNER] ?: BuildConfig.GITHUB_RELEASES_OWNER,
                githubRepo = prefs[Keys.GH_REPO] ?: BuildConfig.GITHUB_RELEASES_REPO,
                showComplexityInPdf = prefs[Keys.SHOW_COMPLEXITY] ?: true,
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
            prefs[Keys.GH_OWNER] = updated.githubOwner
            prefs[Keys.GH_REPO] = updated.githubRepo
            prefs[Keys.SHOW_COMPLEXITY] = updated.showComplexityInPdf
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
        val GH_OWNER = stringPreferencesKey("gh_owner")
        val GH_REPO = stringPreferencesKey("gh_repo")
        val SHOW_COMPLEXITY = booleanPreferencesKey("show_complexity")
    }
}
