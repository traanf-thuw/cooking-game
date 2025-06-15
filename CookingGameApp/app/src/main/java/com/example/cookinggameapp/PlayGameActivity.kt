package com.example.cookinggameapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.core.view.doOnLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.random.Random

class PlayGameActivity : BaseActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var roomCode: String
    private var isHost: Boolean = false
    private var roomListener: ListenerRegistration? = null

    private lateinit var gameCanvas: FrameLayout
    private val dynamicItems = mutableListOf<ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playgame)

        db = FirebaseFirestore.getInstance()
        roomCode = intent.getStringExtra("roomCode") ?: return
        isHost = intent.getBooleanExtra("isHost", false)

        gameCanvas = findViewById(R.id.gameCanvas)

        // Add items after layout is ready
        gameCanvas.doOnLayout {
            addDynamicItem("dynamic_chicken", R.drawable.chicken)
            addDynamicItem("dynamic_lemon", R.drawable.lemon)
        }
    }

    /** Add dynamic item at random position */
    private fun addDynamicItem(tag: String, drawableRes: Int): ImageView {
        val item = ImageView(this).apply {
            setImageResource(drawableRes)
            this.tag = tag
            layoutParams = FrameLayout.LayoutParams(100, 100)
        }

        gameCanvas.addView(item)

        // Set position after added
        val maxX = gameCanvas.width - item.layoutParams.width
        val maxY = gameCanvas.height - item.layoutParams.height
        item.x = Random.nextInt(0, maxX).toFloat()
        item.y = Random.nextInt(0, maxY).toFloat()

        dynamicItems.add(item)

        enableDrag(item)

        return item
    }

    /** Remove dynamic item by tag */
    private fun removeDynamicItemByTag(tag: String) {
        val item = dynamicItems.find { it.tag == tag }
        if (item != null) {
            gameCanvas.removeView(item)
            dynamicItems.remove(item)
            Log.d("PlayGame", "Removed item with tag: $tag")
        } else {
            Log.w("PlayGame", "No dynamic item found with tag: $tag")
        }
    }

    /** Enable dragging for item */
    @SuppressLint("ClickableViewAccessibility")
    private fun enableDrag(item: ImageView) {
        var dX = 0f
        var dY = 0f

        item.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    v.x = (event.rawX + dX).coerceIn(0f, gameCanvas.width - v.width.toFloat())
                    v.y = (event.rawY + dY).coerceIn(0f, gameCanvas.height - v.height.toFloat())
                }
                MotionEvent.ACTION_UP -> {
                    Toast.makeText(this, "${v.tag} dropped!", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        roomListener?.remove()
    }
}
