package com.example.cookinggameapp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


open class BaseActivity : AppCompatActivity() {

    companion object {
        private var activityCount = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeMusicIfNeeded(this)
    }

    override fun onStart() {
        super.onStart()
        if (activityCount == 0) {
            MusicManager.resume()
        }
        activityCount++
    }

    override fun onStop() {
        super.onStop()
        activityCount--
        if (activityCount == 0) {
            MusicManager.pause()
        }
    }

    private fun initializeMusicIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)
        val volume = prefs.getInt("volume_level", 50) / 100f
        val track = prefs.getString("music_selection", "chill-lofi-background-music-333347")

        val musicMap = mapOf(
            "chill-lofi-background-music-333347" to R.raw.track1,
            "sakura-lofi-ambient-lofi-music-340018" to R.raw.track2,
            "soulful-river-folk-tune-with-bamboo-flute-339826" to R.raw.track3
        )

        track?.let { selectedTrack ->
            musicMap[selectedTrack]?.let { resId ->
                MusicManager.setVolume(volume)
                MusicManager.start(context, resId)
            }
        }
    }
}
