package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WaitingActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        // Launch a coroutine that waits for 5 seconds, then starts MainActivity
        lifecycleScope.launch {
            delay(5000) // 5000 milliseconds = 5 seconds
            startActivity(Intent(this@WaitingActivity, MainActivity::class.java))
            finish()
        }
    }
}