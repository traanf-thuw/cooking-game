package com.example.cookinggameapp

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager


class PlayGameActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var roomCode: String
    private var roomListener: ListenerRegistration? = null

    private var isHost: Boolean = false

    private lateinit var chicken: ImageView
    private lateinit var avocado: ImageView
    private lateinit var lemon: ImageView
    private lateinit var knife: ImageView
    private lateinit var cuttingBoard: ImageView
    private lateinit var basketLeft: ImageView
    private lateinit var basketRight: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playgame)

        // Get Firestore and intent info
        db = FirebaseFirestore.getInstance()
        roomCode = intent.getStringExtra("roomCode") ?: return
        isHost = intent.getBooleanExtra("isHost", false)

        // Grab UI
        chicken = findViewById(R.id.imageChicken)
        avocado = findViewById(R.id.imageAvocado)
        lemon = findViewById(R.id.imageLemon)
        knife = findViewById(R.id.imageKnife)
        cuttingBoard = findViewById(R.id.imageCuttingboard)
        basketLeft = findViewById(R.id.imageBasketLeft)
        basketRight = findViewById(R.id.imageBasketRight)

        // Sync logic for chicken
        if (isHost) {
            enableDrag(chicken, isChicken = true)
        } else {
            chicken.visibility = View.INVISIBLE
        }

        // Enable drag for other items
        enableDrag(avocado)
        enableDrag(lemon)
        enableDrag(knife)
        enableDrag(cuttingBoard)

        // Firebase listener
        listenToRoomState()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableDrag(view: ImageView, isChicken: Boolean = false) {
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
                    val itemBox = Rect()
                    val leftBox = Rect()
                    val rightBox = Rect()

                    v.getGlobalVisibleRect(itemBox)
                    basketLeft.getGlobalVisibleRect(leftBox)
                    basketRight.getGlobalVisibleRect(rightBox)

                    if (Rect.intersects(itemBox, leftBox) || Rect.intersects(itemBox, rightBox)) {
                        animateIntoBasket(v)

                        if (isChicken && isHost) {
                            updateFirebaseForDrop()
                        }
                    }
                }
            }
            true
        }
    }

    private fun animateIntoBasket(view: View) {
        view.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                view.visibility = View.INVISIBLE
                Toast.makeText(this, "Item dropped!", Toast.LENGTH_SHORT).show()
            }.start()
    }

    private fun updateFirebaseForDrop() {
        db.collection("rooms").document(roomCode)
            .update("chickenDropped", true)
    }

    private fun listenToRoomState() {
        roomListener = db.collection("rooms").document(roomCode)
            .addSnapshotListener { snapshot, _ ->
                val chickenDropped = snapshot?.getBoolean("chickenDropped") ?: false
                if (!isHost && chickenDropped) {
                    chicken.visibility = View.VISIBLE
                    chicken.alpha = 1f
                    Toast.makeText(this, "Chicken appeared!", Toast.LENGTH_SHORT).show()
                    vibrateDevice()
                }
            }
    }
    private fun vibrateDevice() {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as android.os.Vibrator
        }

        val duration = 1000L  // 1000 milliseconds = 1 second

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(
                android.os.VibrationEffect.createOneShot(
                    duration,
                    android.os.VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        roomListener?.remove()
    }
}
