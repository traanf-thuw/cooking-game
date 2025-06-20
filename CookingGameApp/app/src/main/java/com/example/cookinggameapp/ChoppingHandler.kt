package com.example.cookinggameapp

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast

class ChoppingHandler(
    private val context: Context,
    private val knife: ImageView,
    private val choppingTargets: List<ImageView>,
    private val choppedImageMap: Map<String, Pair<String, Int>>,
    private val shouldPlayerHaveKnife: Boolean,
    private val isViewOverlapping: (View, View) -> Boolean,
    private val vibrate: () -> Unit,
    private val isCurrentStepInvolves: (String) -> Boolean,
    private val onChopComplete: () -> Unit
) {

    private var chopCount = 0
    private var currentChopTarget: ImageView? = null

    fun setup() {
        if (!shouldPlayerHaveKnife) {
            Log.d("ChoppingHandler", "Skipping chopping setup: player has no knife")
            return
        }

        knife.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> true

                MotionEvent.ACTION_MOVE -> {
                    val parent = view.parent as View
                    val maxX = parent.width - view.width
                    val maxY = parent.height - view.height
                    view.translationX = (event.rawX - view.width / 2).coerceIn(0f, maxX.toFloat())
                    view.translationY = (event.rawY - view.height / 2).coerceIn(0f, maxY.toFloat())
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val visibleTargets = choppingTargets.filter { it.visibility == View.VISIBLE }

                    currentChopTarget = visibleTargets.firstOrNull { isViewOverlapping(knife, it) }

                    if (currentChopTarget != null) {
                        chopCount++
                        vibrate()

                        if (chopCount >= 1) {
                            val tag = currentChopTarget?.tag as? String
                            val chopData = choppedImageMap[tag]
                            chopData?.let { (newTag, newImageRes) ->
                                currentChopTarget?.setImageResource(newImageRes)
                                currentChopTarget?.tag = newTag
                            }

                            if (isCurrentStepInvolves("chopping")) {
                                onChopComplete()
                            }

                            chopCount = 0
                            currentChopTarget = null
                        }

                    } else {
                        Toast.makeText(context, "Place knife over an ingredient to chop", Toast.LENGTH_SHORT).show()
                        chopCount = 0
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun getChoppedTag(originalTag: String?): String? {
        return when (originalTag) {
            "chicken" -> "chicken_meat"
            "avocado" -> "avocado_sliced"
            "lemon" -> "lemon_sliced"
            else -> null
        }
    }
}
