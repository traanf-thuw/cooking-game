package com.example.cookinggameapp

import android.content.Context
import android.media.MediaPlayer

object MusicManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackResId: Int? = null

    var isMusicOn: Boolean = true
    var volumeLevel: Int = 100  // Range: 0 to 100

    fun start(context: Context, resId: Int) {
        if (!isMusicOn) return
        if (mediaPlayer?.isPlaying == true && resId == currentTrackResId) return

        stop()
        mediaPlayer = MediaPlayer.create(context.applicationContext, resId).apply {
            isLooping = true
            setVolume(volumeLevel / 100f, volumeLevel / 100f)
            start()
        }
        currentTrackResId = resId
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        if (isMusicOn) {
            mediaPlayer?.start()
        }
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentTrackResId = null
    }

    fun toggleMusic(context: Context, resId: Int, enabled: Boolean) {
        isMusicOn = enabled
        if (enabled) {
            start(context, resId)
        } else {
            pause()
        }
    }

    fun setVolume(volume: Int) {
        volumeLevel = volume.coerceIn(0, 100)
        mediaPlayer?.setVolume(volumeLevel / 100f, volumeLevel / 100f)
    }
}
