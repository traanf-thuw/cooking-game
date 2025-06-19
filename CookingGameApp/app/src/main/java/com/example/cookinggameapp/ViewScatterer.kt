package com.example.cookinggameapp

import android.graphics.Rect
import android.util.Log
import android.view.View
import android.widget.FrameLayout

class ViewScatterer(private val parent: FrameLayout) {

    fun scatter(views: List<View>){
        parent.post {
            val parentWidth = parent.width
            val parentHeight = parent.height

            val reservedBottomSpace = 350
            val reservedCenterWidth = 150
            val reservedCenterHeight = 300

            val centerX = parentWidth / 2
            val centerY = parentHeight / 2
            val centerRect = Rect(
                centerX - reservedCenterWidth / 2,
                centerY - reservedCenterHeight / 2,
                centerX + reservedCenterWidth / 2,
                centerY + reservedCenterHeight / 2
            )

            val placedRects = mutableListOf<Rect>()

            views.forEach { view ->
                val viewWidth = view.width
                val viewHeight = view.height

                var attempts = 0
                var placed = false

                while (!placed && attempts < 100) {
                    val x = (0..(parentWidth - viewWidth)).random()
                    val y = (0..(parentHeight - reservedBottomSpace - viewHeight)).random()

                    val padding = 16
                    val newRect = Rect(
                        x - padding,
                        y - padding,
                        x + viewWidth + padding,
                        y + viewHeight + padding
                    )

                    val overlapsPlaced = placedRects.any { it.intersect(newRect) }
                    val overlapsCenter = Rect.intersects(newRect, centerRect)

                    if (!overlapsPlaced && !overlapsCenter) {
                        view.x = x.toFloat()
                        view.y = y.toFloat()
                        placedRects.add(newRect)
                        placed = true
                    }

                    attempts++
                }

                if (!placed) {
                    Log.w("ViewScatterer", "⚠ Could not place ${view.contentDescription}")
                }
            }
        }
    }
}
