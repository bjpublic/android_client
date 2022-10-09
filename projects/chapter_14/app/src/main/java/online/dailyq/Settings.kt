package online.dailyq

import android.content.Context
import android.content.SharedPreferences

object Settings {
    const val THEME = "theme"

    lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    enum class Theme {
        Light, Dark
    }

    var theme: Theme
        get() = Theme.valueOf(prefs.getString(THEME, Theme.Light.name)!!)
        set(value) {
            prefs.edit().putString(THEME, value.name).apply()
        }

    fun clear() {
        AuthManager.prefs.edit().clear().apply()
    }
}
