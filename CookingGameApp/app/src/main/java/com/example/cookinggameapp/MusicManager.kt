package com.example.cookinggameapp

import android.content.Context
import android.media.MediaPlayer

object MusicManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: Int? = null
    private var volume = 0.5f
    private var isPaused = false

    fun start(context: Context, trackResId: Int) {
        if (mediaPlayer != null && currentTrack == trackResId && !isPaused) return

        stop()
        mediaPlayer = MediaPlayer.create(context.applicationContext, trackResId).apply {
            isLooping = true
            setVolume(volume, volume)
            start()
        }
        currentTrack = trackResId
        isPaused = false
    }

    fun setVolume(vol: Float) {
        volume = vol
        mediaPlayer?.setVolume(volume, volume)
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPaused = true
        }
    }

    fun resume() {
        if (isPaused) {
            mediaPlayer?.start()
            isPaused = false
        }
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentTrack = null
        isPaused = false
    }
}
