package com.example.cookinggameapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PlayGameActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var roomCode: String
    private var roomListener: ListenerRegistration? = null
    private var isHost: Boolean = false

    private lateinit var sensorManager: SensorManager
    private var accel = 0f
    private var accelCurrent = 0f
    private var accelLast = 0f

    private var lastDroppedItemTag: String? = null

    private lateinit var allItems: List<ImageView>
    private lateinit var basketLeft: ImageView
    private lateinit var basketRight: ImageView
    private lateinit var spoon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playgame)

        db = FirebaseFirestore.getInstance()
        roomCode = intent.getStringExtra("roomCode") ?: return
        isHost = intent.getBooleanExtra("isHost", false)

        // Initialize item views and tags
        val chicken = findViewById<ImageView>(R.id.imageChicken).apply { tag = "chicken" }
        val avocado = findViewById<ImageView>(R.id.imageAvocado).apply { tag = "avocado" }
        val lemon = findViewById<ImageView>(R.id.imageLemon).apply { tag = "lemon" }
        val knife = findViewById<ImageView>(R.id.imageKnife).apply { tag = "knife" }
        val cuttingBoard = findViewById<ImageView>(R.id.imageCuttingboard).apply { tag = "cuttingboard" }
        val pot = findViewById<ImageView>(R.id.imagePot).apply { tag = "pot" }
        val stove = findViewById<ImageView>(R.id.imageStove).apply { tag = "stove" }
        val spoon = findViewById<ImageView>(R.id.imageSpoon).apply { tag = "spoon" }

        basketLeft = findViewById(R.id.imageBasketLeft)
        basketRight = findViewById(R.id.imageBasketRight)

        allItems = listOf(chicken, avocado, lemon, knife, cuttingBoard, pot, stove, spoon)

        allItems.forEach { item ->
            enableDrag(item)
            if (!isHost) item.visibility = View.INVISIBLE
        }

        listenToMyLogic()

        if (isHost) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sensorManager.registerListener(
                sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI
            )
            accel = 10f
            accelCurrent = SensorManager.GRAVITY_EARTH
            accelLast = SensorManager.GRAVITY_EARTH
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableDrag(view: ImageView) {
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

                    when {
                        Rect.intersects(itemBox, rightBox) -> {
                            animateIntoBasket(v)
                            lastDroppedItemTag = v.tag?.toString()
                            Toast.makeText(this, "Shake to send $lastDroppedItemTag!", Toast.LENGTH_SHORT).show()
                            Log.d("MyGameLogic", "Dropped on RIGHT: $lastDroppedItemTag")
                        }

                        Rect.intersects(itemBox, leftBox) -> {
                            animateIntoBasket(v)
                            lastDroppedItemTag = v.tag?.toString()
                            Toast.makeText(this, "Shake to send $lastDroppedItemTag!", Toast.LENGTH_SHORT).show()
                            Log.d("MyGameLogic", "Dropped on LEFT: $lastDroppedItemTag")
                        }

                        else -> {
                            Toast.makeText(this, "Drop it on a basket!", Toast.LENGTH_SHORT).show()
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

    private fun saveMyLogicToFirestore() {
        val item = lastDroppedItemTag ?: run {
            Toast.makeText(this, "No item was dropped yet!", Toast.LENGTH_SHORT).show()
            Log.w("MyGameLogic", "Skipped saving: no item dropped")
            return
        }

        val data = hashMapOf(
            "roomID" to roomCode,
            "playerID" to "player1",
            "itemTag" to item,
            "actionType" to "sendItem",
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("myGameLogic")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Saved and sent: $item", Toast.LENGTH_SHORT).show()
                Log.d("MyGameLogic", "✅ Saved & triggered: $item")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
                Log.e("MyGameLogic", "❌ Firestore save failed", e)
            }
    }

    private fun listenToMyLogic() {
        roomListener = db.collection("myGameLogic")
            .whereEqualTo("roomID", roomCode)
            .addSnapshotListener { snapshots, _ ->
                for (change in snapshots?.documentChanges ?: return@addSnapshotListener) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        val itemTag = change.document.getString("itemTag") ?: continue
                        Log.d("MyGameLogic", "📩 New item received: $itemTag")
                        if (!isHost) animateItemFromTag(itemTag)
                    }
                }
            }
    }

    private fun animateItemFromTag(tag: String) {
        val view = allItems.find { it.tag == tag } ?: return

        val basketX = basketLeft.x
        val basketY = basketLeft.y

        view.translationX = basketX
        view.translationY = -300f
        view.alpha = 0f
        view.scaleX = 0.3f
        view.scaleY = 0.3f
        view.rotation = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .translationY(basketY)
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .rotationBy(1440f)
            .setDuration(1000)
            .withEndAction {
                Toast.makeText(this, "$tag flew in!", Toast.LENGTH_SHORT).show()
                vibrateDevice()
            }.start()
    }

    private fun vibrateDevice() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        val duration = 1500L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
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

            if (accel > 4 && isHost) {
                Log.d("Sensor", "Shake detected! accel = $accel")
                saveMyLogicToFirestore()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        roomListener?.remove()
        if (isHost) sensorManager.unregisterListener(sensorListener)
    }
}
