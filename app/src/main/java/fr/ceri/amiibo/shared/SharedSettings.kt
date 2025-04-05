package fr.ceri.amiibo.shared

import android.content.Context
import android.content.SharedPreferences

object SharedSettings {

    private const val PREF_NAME = "amiibo_settings"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_SCORE = "score"
    private const val KEY_FIRST_LAUNCH = "first_launch"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Langue sélectionnée
    var selectedLanguage: String?
        get() = prefs.getString(KEY_LANGUAGE, null)
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    // Score (par exemple pour Game Over)
    var lastScore: Int
        get() = prefs.getInt(KEY_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_SCORE, value).apply()

    // Indique si l'utilisateur a déjà vu les règles
    var firstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
