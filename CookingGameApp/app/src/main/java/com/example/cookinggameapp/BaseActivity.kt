package com.example.cookinggameapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    companion object {
        private var activityCount = 0
    }

    override fun onResume() {
        super.onResume()
        if (MusicPreferences.isMusicEnabled(this)) {
            MusicManager.resume()
        } else {
            MusicManager.pause()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeMusicIfNeeded()
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

    private fun initializeMusicIfNeeded() {
        val volume = MusicPreferences.getVolume(this)
        val selectedTrack = MusicPreferences.getSelectedTrack(this)
        val resId = MusicLibrary.trackMap[selectedTrack]

        if (resId != null) {
            MusicManager.setVolume(volume)
            MusicManager.start(this, resId)
        }
    }
}
