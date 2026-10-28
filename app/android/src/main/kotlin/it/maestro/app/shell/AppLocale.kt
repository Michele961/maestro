package it.maestro.app.shell

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import it.maestro.app.domain.settings.AppLanguage
import java.util.Locale

/**
 * Applica la lingua scelta nelle Impostazioni (task F1R-31c), senza dipendenze aggiuntive.
 * Da Android 13 (API 33) lo fa il sistema con `LocaleManager`; sotto, la lingua si legge da un
 * piccolo riflesso locale all'avvio e si applica avvolgendo il contesto dell'Activity.
 */
public object AppLocale {
    private const val PREFS = "maestro_locale"
    private const val KEY_TAG = "language_tag"

    /** Avvolge [base] con la lingua scelta; da usare in `attachBaseContext` (solo sotto API 33). */
    public fun wrap(base: Context): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return base
        val language = AppLanguage.fromTag(savedTag(base))
        if (language == AppLanguage.SYSTEM) return base
        val locale = Locale.forLanguageTag(language.tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }

    /** Applica [language]; se la lingua cambia sotto API 33 ricrea l'Activity per rileggere le risorse. */
    public fun apply(activity: Activity, language: AppLanguage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val manager = activity.getSystemService(LocaleManager::class.java)
            val wanted = if (language == AppLanguage.SYSTEM) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(language.tag)
            if (manager.applicationLocales != wanted) manager.applicationLocales = wanted
            return
        }
        if (savedTag(activity) == language.tag) return
        activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_TAG, language.tag).apply()
        activity.recreate()
    }

    private fun savedTag(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TAG, AppLanguage.SYSTEM.tag) ?: AppLanguage.SYSTEM.tag
}
