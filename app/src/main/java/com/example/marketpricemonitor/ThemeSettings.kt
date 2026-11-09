package com.example.marketpricemonitor

import android.content.Context

enum class ThemeMode { SYSTEM, LIGHT, DARK }

object ThemeSettings {
    private const val PREFS = "theme_prefs"
    private const val KEY_MODE = "theme_mode"

    fun getMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    fun setMode(context: Context, mode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }
}