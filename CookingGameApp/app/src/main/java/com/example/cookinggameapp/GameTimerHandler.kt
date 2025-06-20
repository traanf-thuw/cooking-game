package com.example.cookinggameapp

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.ceil

class GameTimerHandler(
    private val context: Context,
    private val db: FirebaseFirestore,
    private val roomCode: String,
    private val countdownText: TextView,
    private val onTimeUp: () -> Unit
) {

    private var countdownTimer: CountDownTimer? = null

    fun start() {
        db.collection("rooms").document(roomCode).get().addOnSuccessListener { document ->
            val startTime = document.getLong("start_time") ?: return@addOnSuccessListener
            val difficulty = document.getString("difficulty") ?: "easy"

            val totalTimeMillis = when (difficulty.lowercase()) {
                "easy" -> 60_000L
                "medium" -> 45_000L
                "hard" -> 30_000L
                else -> 60_000L
            }

            val elapsed = System.currentTimeMillis() - startTime
            val remaining = totalTimeMillis - elapsed
            val clampedRemaining = remaining.coerceAtLeast(0L)
            val remainingSeconds = ceil(clampedRemaining / 1000.0).toInt()

            if (remainingSeconds > 0) {
                startCountdown(remainingSeconds)
            } else {
                countdownText.text = "00:00"
                Toast.makeText(context, "Time's up!", Toast.LENGTH_SHORT).show()
                onTimeUp()
            }
        }
    }

    private fun startCountdown(seconds: Int) {
        countdownTimer = object : CountDownTimer((seconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                countdownText.text = String.format("00:%02d", secondsLeft)
            }

            override fun onFinish() {
                countdownText.text = "00:00"
                Toast.makeText(context, "Time's up!", Toast.LENGTH_SHORT).show()
                onTimeUp()
            }
        }.start()
    }

    fun cancel() {
        countdownTimer?.cancel()
    }
}
