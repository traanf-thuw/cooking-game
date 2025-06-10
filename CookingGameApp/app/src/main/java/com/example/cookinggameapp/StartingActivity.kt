package com.example.cookinggameapp

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class StartingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation)

        // TEST ANIMATION on another view (fatBaby) to confirm animations work
        val testView = findViewById<ImageView>(R.id.fatBaby)
        testView.post {
            // simple property animation for testing
            testView.animate()
                .translationYBy(200f)
                .setDuration(500)
                .start()
        }

        // Actual egg drop using view animation
        val egg = findViewById<ImageView>(R.id.fried_egg)
        val dropAnim = AnimationUtils.loadAnimation(this, R.anim.animation_action)
        egg.startAnimation(dropAnim)
    }
}