package com.example.cookinggameapp

import android.content.Context
import android.os.CountDownTimer
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
                "easy" -> 300_000L    // 5 minutes
                "medium" -> 240_000L  // 4 minutes
                "hard" -> 120_000L    // 2 minutes
                else -> 300_000L
            }

            val now = System.currentTimeMillis()
            val elapsed = now - startTime
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
                val minutes = secondsLeft / 60
                val seconds = secondsLeft % 60
                countdownText.text = String.format("%02d:%02d", minutes, seconds)
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
