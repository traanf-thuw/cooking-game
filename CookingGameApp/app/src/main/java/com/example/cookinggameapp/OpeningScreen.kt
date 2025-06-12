package com.example.cookinggameapp

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
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
        val dropAnim = AnimationUtils.loadAnimation(this, R.anim.egg_drop)
        egg.startAnimation(dropAnim)

        // Start the teeter animation
        val breakfast = findViewById<ImageView>(R.id.imageBreakfast)
        val friedRice = findViewById<ImageView>(R.id.friedRice)
        val dinner = findViewById<ImageView>(R.id.imageDinner)
        val salad = findViewById<ImageView>(R.id.imageSalad)
        val dish = findViewById<ImageView>(R.id.imageDish)

        val teeter = AnimationUtils.loadAnimation(this, R.anim.teeter)
        breakfast.startAnimation(teeter)
        friedRice.startAnimation(teeter)
        dinner.startAnimation(teeter)
        salad.startAnimation(teeter)
        dish.startAnimation(teeter)

        // For green_baby (left to center, wave)

        val greenBaby = findViewById<ImageView>(R.id.green_baby)
        greenBaby.post {

            // Option 2: Stop at custom dp offset from left
             val offsetPx = (120 * resources.displayMetrics.density)
             val endX = offsetPx

            val startX = -greenBaby.width.toFloat()
            val amplitude = 80
            val waveLength = 2 * Math.PI

            greenBaby.translationX = startX
            greenBaby.translationY = 0f

            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 2500
                interpolator = LinearInterpolator()
                addUpdateListener { animator ->
                    val fraction = animator.animatedFraction
                    val x = startX + (endX - startX) * fraction
                    val y = amplitude * Math.sin(waveLength * fraction).toFloat()
                    greenBaby.translationX = x
                    greenBaby.translationY = y
                }
                start()
            }
        }


        // For pudding_baby (right to center, wave)

        val pudding_Baby = findViewById<ImageView>(R.id.puddingBay)
        pudding_Baby.post {
            val parent = findViewById<View>(R.id.startPageLayout)
            val parentWidth = parent.width

            val startX = parentWidth.toFloat() // Start just off the right edge
            val endX = (parentWidth / 15f) - (pudding_Baby.width / 1f) // Middle of the screen

            val amplitude = 100
            val waveLength = 2 * Math.PI

            pudding_Baby.translationX = startX
            pudding_Baby.translationY = 6f

            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 2000
                interpolator = LinearInterpolator()
                addUpdateListener { animator ->
                    val fraction = animator.animatedFraction
                    val x = startX + (endX - startX) * fraction
                    val y = amplitude * Math.sin(waveLength * fraction).toFloat()
                    pudding_Baby.translationX = x
                    pudding_Baby.translationY = y
                }
                start()
            }
        }


        lifecycleScope.launch {
            delay(5000) // 5 seconds
            startActivity(Intent(this@OpeningScreen, MainActivity::class.java))
            finish()
        }
    }
}
