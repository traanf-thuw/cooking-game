package com.example.cookinggameapp

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StartingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animation)

        val friedEgg: ImageView = findViewById(R.id.fried_egg)
        val longPan: ImageView = findViewById(R.id.long_pan)
        val imageSalad: ImageView = findViewById(R.id.imageSalad)
        val imageBreakfast: ImageView = findViewById(R.id.imageBreakfast)
        val friedRice: ImageView = findViewById(R.id.friedRice)
        val imageDinner: ImageView = findViewById(R.id.imageDinner)
        val imageDish: ImageView = findViewById(R.id.imageDish)

        // Drop friedEgg into pan
        friedEgg.post {
            val eggY = friedEgg.y
            val panY = longPan.y
            val dropDistance = panY - eggY - 30

            val dropAnimation = TranslateAnimation(0f, 0f, 0f, dropDistance)
            dropAnimation.duration = 1000
            dropAnimation.fillAfter = true
            friedEgg.startAnimation(dropAnimation)
        }

        // Continuous rotation
        val rotateForever = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 2000
            repeatCount = Animation.INFINITE
            interpolator = LinearInterpolator()
        }
        imageSalad.startAnimation(rotateForever)
        imageBreakfast.startAnimation(rotateForever)

        // 🤸‍♂️ Teetering animation (side-to-side)
        val teeter = RotateAnimation(
            -10f, 10f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 500
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
        }

        // Let me cook boomerang
        friedRice.startAnimation(teeter)
        imageDinner.startAnimation(teeter)
        imageDish.startAnimation(teeter)

        val titleText: TextView = findViewById(R.id.titleText)
        val fatBaby: ImageView = findViewById(R.id.fatBaby)

        titleText.post {
            val titleY = titleText.y
            val fatY = fatBaby.y
            val distance = (fatY - titleY) / 2  // Move halfway toward fatBaby

            val boomerang = TranslateAnimation(0f, 0f, 0f, distance).apply {
                duration = 1000
                repeatCount = Animation.INFINITE
                repeatMode = Animation.REVERSE
            }

            titleText.startAnimation(boomerang)
        }
    }
}
