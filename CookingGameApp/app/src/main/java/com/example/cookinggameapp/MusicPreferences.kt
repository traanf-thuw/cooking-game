package com.example.cookinggameapp

import android.content.Context

object MusicPreferences {
    private const val PREF_NAME = "AppSettingsPrefs"
    private const val KEY_VOLUME = "volume_level"
    private const val KEY_TRACK = "music_selection"
    private const val KEY_MUSIC_ENABLED = "music_enabled"

    fun getVolumeRaw(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_VOLUME, 50)
    }

    fun setVolumeRaw(context: Context, volume: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_VOLUME, volume.coerceIn(0, 100))
            .apply()
    }

    fun getVolume(context: Context): Float {
        return getVolumeRaw(context) / 100f
    }

    fun getSelectedTrack(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TRACK, "chill-lofi-background-music-333347")
    }

    fun setSelectedTrack(context: Context, trackName: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TRACK, trackName)
            .apply()
    }

    fun isMusicEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_MUSIC_ENABLED, true)
    }

    fun setMusicEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MUSIC_ENABLED, enabled)
            .apply()
    }
}
