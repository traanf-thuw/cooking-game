package com.example.cookinggameapp

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PlayGameActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playgame)

        // Get draggable items
        val chicken = findViewById<ImageView>(R.id.imageChicken)
        val knife = findViewById<ImageView>(R.id.imageKnife)
        val lemon = findViewById<ImageView>(R.id.imageLemon)
        val avocado = findViewById<ImageView>(R.id.imageAvocado)
        val cuttingboard = findViewById<ImageView>(R.id.imageCuttingboard)
        val stove = findViewById<ImageView>(R.id.imageStove)

        // Get baskets
        val basketLeft = findViewById<ImageView>(R.id.imageBasketLeft)
        val basketRight = findViewById<ImageView>(R.id.imageBasketRight)

        // Apply drag logic to all items
        val allItems = listOf(chicken, knife, lemon, avocado, cuttingboard, stove)
        allItems.forEach { item ->
            enableDrag(item, basketLeft, basketRight)
        }
    }

    // Drag-and-drop with basket drop detection
    private fun enableDrag(view: ImageView, basketLeft: View, basketRight: View) {
        view.setOnTouchListener { v, event ->
            val parent = v.parent as View
            val maxX = parent.width - v.width
            val maxY = parent.height - v.height

            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    v.translationX = (event.rawX - v.width / 2).coerceIn(0f, maxX.toFloat())
                    v.translationY = (event.rawY - v.height / 2).coerceIn(0f, maxY.toFloat())
                }

                MotionEvent.ACTION_UP -> {
                    v.performClick()

                    val itemBox = Rect()
                    val leftBox = Rect()
                    val rightBox = Rect()

                    v.getGlobalVisibleRect(itemBox)
                    basketLeft.getGlobalVisibleRect(leftBox)
                    basketRight.getGlobalVisibleRect(rightBox)

                    when {
                        Rect.intersects(itemBox, leftBox) -> {
                            animateIntoBasket(v, basketLeft)
                        }
                        Rect.intersects(itemBox, rightBox) -> {
                            animateIntoBasket(v, basketRight)
                        }
                    }
                }
            }
            true
        }
    }

    // Animate into basket and hide
    private fun animateIntoBasket(view: View, basket: View) {
        val basketLocation = IntArray(2)
        basket.getLocationOnScreen(basketLocation)

        val viewLocation = IntArray(2)
        view.getLocationOnScreen(viewLocation)

        val deltaX = (basketLocation[0] + basket.width / 2) - (viewLocation[0] + view.width / 2)
        val deltaY = (basketLocation[1] + basket.height / 2) - (viewLocation[1] + view.height / 2)

        view.animate()
            .translationXBy(deltaX.toFloat())
            .translationYBy(deltaY.toFloat())
            .setDuration(300)
            .withEndAction {
                view.visibility = View.INVISIBLE
                Toast.makeText(this, "Item placed!", Toast.LENGTH_SHORT).show()
            }
            .start()
    }
}
