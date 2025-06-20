package com.example.cookinggameapp

import android.content.Context
import android.media.MediaPlayer

object MusicManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackResId: Int? = null

    fun start(context: Context, resId: Int) {
        if (mediaPlayer?.isPlaying == true && resId == currentTrackResId) return

        stop()
        mediaPlayer = MediaPlayer.create(context.applicationContext, resId).apply {
            isLooping = true
            start()
        }
        currentTrackResId = resId
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        mediaPlayer?.start()
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentTrackResId = null
    }

    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }
}
