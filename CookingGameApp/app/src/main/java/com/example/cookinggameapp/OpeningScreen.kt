package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OpeningScreen : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_animation)

        lifecycleScope.launch {
            delay(5000) // 5000 milliseconds = 5 seconds
            startActivity(Intent(this@OpeningScreen, MainActivity::class.java))
            finish()
        }
    }
}