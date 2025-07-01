package com.example.cookinggameapp

import android.animation.ValueAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.*
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

class OpeningScreen : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_animation)

        animateEggDrop()
        initTeeterAnimations()
        animateGreenBaby()
        animatePuddingBaby()
        animateTitleAndNavigate()
    }

    private fun animateEggDrop() {
        val egg = findViewById<ImageView>(R.id.fried_egg)
        val dropAnim = AnimationUtils.loadAnimation(this, R.anim.egg_drop)

        dropAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                playSound(R.raw.fall_down)
            }

            override fun onAnimationEnd(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })

        egg.startAnimation(dropAnim)
    }

    private fun playSound(soundResId: Int) {
        val mp = MediaPlayer.create(this, soundResId)
        mp.start()
    }

    private fun initTeeterAnimations() {
        val views = listOf(
            R.id.imageBreakfast,
            R.id.friedRice,
            R.id.imageDinner,
            R.id.imageSalad,
            R.id.imageDish
        ).map { findViewById<ImageView>(it) }

        val teeter = AnimationUtils.loadAnimation(this, R.anim.teeter)
        views.forEach { it.startAnimation(teeter) }
    }

    private fun animateGreenBaby() {
        val greenBaby = findViewById<ImageView>(R.id.green_baby)
        greenBaby.post {
            val offsetPx = 90 * resources.displayMetrics.density
            val startX = -greenBaby.width.toFloat()
            val endX = offsetPx
            applyWaveMotion(
                view = greenBaby,
                startX = startX,
                endX = endX,
                amplitude = 100,
                waveLength = 3 * Math.PI,
                duration = 3500
            )
        }
    }

    private fun animatePuddingBaby() {
        val pudding = findViewById<ImageView>(R.id.puddingBay)
        pudding.post {
            val parent = findViewById<View>(R.id.startPageLayout)
            val parentWidth = parent.width
            val startX = parentWidth.toFloat()
            val endX = (parentWidth / 7f) - (pudding.width / 1f)

            applyWaveMotion(
                view = pudding,
                startX = startX,
                endX = endX,
                amplitude = 100,
                waveLength = 3 * Math.PI,
                duration = 4000
            )
        }
    }

    private fun applyWaveMotion(
        view: View,
        startX: Float,
        endX: Float,
        amplitude: Int,
        waveLength: Double,
        duration: Long
    ) {
        view.translationX = startX
        view.translationY = 0f

        ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val fraction = animator.animatedFraction
                val x = startX + (endX - startX) * fraction
                val y = amplitude * sin(waveLength * fraction).toFloat()
                view.translationX = x
                view.translationY = y
            }
            start()
        }
    }

    private fun animateTitleAndNavigate() {
        val title = findViewById<TextView>(R.id.titleText)
        title.post {
            title.translationX = -resources.displayMetrics.widthPixels.toFloat()
            title.translationY = -200f

            title.animate()
                .translationX(0f)
                .translationY(0f)
                .setInterpolator(OvershootInterpolator(2f))
                .setDuration(3000)
                .start()

            lifecycleScope.launch {
                delay(5000)
                startActivity(Intent(this@OpeningScreen, MainActivity::class.java))
                finish()
            }
        }
    }
}
