package com.example.cookinggameapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

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

    private lateinit var sensorManager: SensorManager
    private var accel = 0f
    private var accelCurrent = 0f
    private var accelLast = 0f

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

        // 🔁 Start Firebase listener for state sync
        listenToRoomState()

        // ✅ Set drag for other items
        enableDrag(avocado)
        enableDrag(lemon)
        enableDrag(knife)
        enableDrag(cuttingBoard)

        if (isHost) {
            // ✅ Only host will detect shake and send update
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorManager.registerListener(
                sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI
            )

            accel = 10f
            accelCurrent = SensorManager.GRAVITY_EARTH
            accelLast = SensorManager.GRAVITY_EARTH
        } else {
            // ✅ Hide chicken on client phone
            chicken.visibility = View.INVISIBLE
        }
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
                            Toast.makeText(this, "Now shake the phone to drop the chicken!", Toast.LENGTH_SHORT).show()
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
            val manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        val duration = 1500L  // Vibrate for 1.5 seconds

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            accelLast = accelCurrent
            accelCurrent = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = accelCurrent - accelLast
            accel = accel * 0.9f + delta

            if (accel > 12 && isHost) {  // Shake threshold
                updateFirebaseForDrop()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        roomListener?.remove()
        if (isHost) {
            sensorManager.unregisterListener(sensorListener)
        }
    }
}
