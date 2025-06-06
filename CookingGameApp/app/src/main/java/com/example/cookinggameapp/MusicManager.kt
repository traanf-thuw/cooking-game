package com.example.cookinggameapp

import android.content.Context
import android.media.MediaPlayer

object MusicManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: Int? = null
    private var volume = 0.5f

    fun start(context: Context, trackResId: Int) {
        if (mediaPlayer != null && currentTrack == trackResId) return

        stop()
        mediaPlayer = MediaPlayer.create(context.applicationContext, trackResId).apply {
            isLooping = true
            setVolume(volume, volume)
            start()
        }
        currentTrack = trackResId
    }

    fun setVolume(vol: Float) {
        volume = vol
        mediaPlayer?.setVolume(volume, volume)
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
        currentTrack = null
    }
}
