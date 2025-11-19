package com.example.weatherapp.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesManager(context : Context) {

    private val prefs : SharedPreferences = context.getSharedPreferences(
        "weather_app_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_WELCOME_SEEN = "welcome_seen"
    }

    fun isWelcomeSeen(): Boolean {
        return prefs.getBoolean(KEY_WELCOME_SEEN, false)
    }

    fun setWelcomeSeen() {
        prefs.edit { putBoolean(KEY_WELCOME_SEEN, true) }
    }

}