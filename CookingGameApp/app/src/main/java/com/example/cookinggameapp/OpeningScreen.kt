package com.example.cookinggameapp

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.media.MediaPlayer
import android.view.animation.Animation

class OpeningScreen : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_animation)

        // Start the egg drop animation
        val egg = findViewById<ImageView>(R.id.fried_egg)
        val dropAnim = AnimationUtils.loadAnimation(this, R.anim.egg_drop)

        dropAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // Play sound here
                val mp = MediaPlayer.create(this@OpeningScreen, R.raw.fall_down)
                mp.start()
            }
            override fun onAnimationEnd(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })

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
            val amplitude = 100
            val waveLength = 3 * Math.PI

            greenBaby.translationX = startX
            greenBaby.translationY = 0f

            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 3500
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
            val waveLength = 3 * Math.PI

            pudding_Baby.translationX = startX
            pudding_Baby.translationY = 6f

            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 4000
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


        val title = findViewById<TextView>(R.id.titleText)

        title.post {
            // Move it to the corner (off-screen, top left)
            val originalX = title.x
            val originalY = title.y

            // Start position: way off top-left (adjust as needed)
            title.translationX = -resources.displayMetrics.widthPixels.toFloat()
            title.translationY = -200f

            // Animate X to center (boomerang with overshoot)
            title.animate()
                .translationX(0f)
                .translationY(0f)
                .setInterpolator(OvershootInterpolator(2f)) // "spring" effect
                .setDuration(3000)
                .start()


            lifecycleScope.launch {
                delay(5000) // 5 seconds
                startActivity(Intent(this@OpeningScreen, MainActivity::class.java))
                finish()
            }
        }
    }
}
