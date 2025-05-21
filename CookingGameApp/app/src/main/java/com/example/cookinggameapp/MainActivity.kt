package com.example.cookinggameapp

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var dX = 0f
    private var dY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playgame)

        // Optional: Toast to confirm screen loaded
        Toast.makeText(this, "Game Screen Loaded", Toast.LENGTH_SHORT).show()

        tryMakeDraggable(R.id.imageAvocado)
        tryMakeDraggable(R.id.imageChicken)
        tryMakeDraggable(R.id.imageKnife)
        tryMakeDraggable(R.id.imageCuttingboard)
        tryMakeDraggable(R.id.imageLemon)
        tryMakeDraggable(R.id.imageStove)
    }

    private fun tryMakeDraggable(viewId: Int) {
        val view = findViewById<View?>(viewId)
        if (view != null) {
            makeDraggable(view)
        } else {
            Toast.makeText(this, "Missing view: $viewId", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeDraggable(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    v.performClick() // Accessibility
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    v.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                    true
                }
                else -> false
            }
        }
    }
}
