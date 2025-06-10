package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OpeningScreen : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_animation)

        // Start the egg drop animation
        val egg = findViewById<ImageView>(R.id.fried_egg)
        val dropAnim = AnimationUtils.loadAnimation(this, R.anim.animation_action)
        egg.startAnimation(dropAnim)

        lifecycleScope.launch {
            delay(5000) // 5 seconds
            startActivity(Intent(this@OpeningScreen, MainActivity::class.java))
            finish()
        }
    }
}
