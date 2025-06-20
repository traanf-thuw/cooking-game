package com.example.cookinggameapp

import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast

class StirringHandler(
    private val context: Context,
    private val spoon: ImageView,
    private val pot: ImageView,
    private val redFillImage: ImageView,
    private val shouldPlayerHaveSpoon: Boolean,
    private val vibrate: () -> Unit,
    private val isCurrentStepInvolves: (String) -> Boolean,
    private val onStirComplete: () -> Unit
) {

    fun setup() {
        if (!shouldPlayerHaveSpoon) return

        var lastTouchX = 0f
        var lastTouchY = 0f
        var rotationAngle = 0f
        var stirStartTime: Long = 0
        var hasFilled = false

        spoon.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.rawX - view.x
                    lastTouchY = event.rawY - view.y
                }

                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX - lastTouchX
                    val newY = event.rawY - lastTouchY
                    view.x = newX
                    view.y = newY

                    if (!hasFilled && isSpoonInsidePot()) {
                        if (stirStartTime == 0L) stirStartTime = System.currentTimeMillis()

                        val elapsed = System.currentTimeMillis() - stirStartTime
                        rotationAngle += 10f
                        spoon.rotation = rotationAngle

                        if (elapsed >= 1000 && isCurrentStepInvolves("stirring")) {
                            triggerRedFill()
                            hasFilled = true
                            vibrate()
                            onStirComplete()
                        }
                    } else {
                        stirStartTime = 0
                        if (!hasFilled) redFillImage.visibility = View.INVISIBLE
                    }
                }
            }
            true
        }
    }

    private fun isSpoonInsidePot(): Boolean {
        val potRect = Rect()
        val spoonRect = Rect()
        pot.getGlobalVisibleRect(potRect)
        spoon.getGlobalVisibleRect(spoonRect)
        return Rect.intersects(potRect, spoonRect)
    }

    private fun triggerRedFill() {
        if (redFillImage.visibility != View.VISIBLE) {
            redFillImage.visibility = View.VISIBLE
        }
    }
}
