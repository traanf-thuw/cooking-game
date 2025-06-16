package com.example.cookinggameapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.math.ceil
import kotlin.random.Random

class PlayGameActivity : BaseActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var roomCode: String
    private var roomListener: ListenerRegistration? = null
    private var isHost: Boolean = false

    private lateinit var gameCanvas: FrameLayout
    private lateinit var countdownText: TextView
    private lateinit var fireSeekBar: SeekBar
    private lateinit var basketLeft: ImageView
    private lateinit var basketRight: ImageView

    private lateinit var pot: ImageView
    private lateinit var spoon: ImageView
    private lateinit var redFillImage: ImageView

    private var lastDroppedItemTag: String? = null
    private val dynamicItems = mutableListOf<ImageView>()

    private lateinit var sensorManager: SensorManager
    private var accel = 0f
    private var accelCurrent = 0f
    private var accelLast = 0f

    private var countdownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playgame)

        db = FirebaseFirestore.getInstance()
        roomCode = intent.getStringExtra("roomCode") ?: return
        isHost = intent.getBooleanExtra("isHost", false)

        gameCanvas = findViewById(R.id.gameCanvas)
        countdownText = findViewById(R.id.countdownText)
        fireSeekBar = findViewById(R.id.fireSeekBar)
        basketLeft = findViewById(R.id.imageBasketLeft)
        basketRight = findViewById(R.id.imageBasketRight)
        pot = findViewById(R.id.imagePot)
        spoon = findViewById(R.id.imageSpoon)
        redFillImage = findViewById(R.id.imageRedFill)

        fireSeekBar.visibility = View.GONE

        setupGameTimer()
        listenToRoomState()
        setupStirring()

        if (isHost) {
            addInitialItems()
            setupShakeDetection()
        }
    }

    private fun addInitialItems() {
        gameCanvas.post {
            createDynamicItem("chicken", R.drawable.chicken)
            createDynamicItem("avocado", R.drawable.avocado)
            createDynamicItem("lemon", R.drawable.lemon)
            createDynamicItem("knife", R.drawable.knife)
            createDynamicItem("cuttingboard", R.drawable.cuttingboard)
            createDynamicItem("stove", R.drawable.stove)
        }
    }

    private fun createDynamicItem(tag: String, drawableRes: Int): ImageView {
        val item = ImageView(this).apply {
            setImageResource(drawableRes)
            this.tag = tag
            val size = (150 * resources.displayMetrics.density).toInt()
            layoutParams = FrameLayout.LayoutParams(size, size)
        }

        val maxX = gameCanvas.width - 150
        val maxY = gameCanvas.height - 150

        item.x = Random.nextInt(0, maxX).toFloat()
        item.y = Random.nextInt(0, maxY).toFloat()

        gameCanvas.addView(item)
        dynamicItems.add(item)
        enableDrag(item)
        return item
    }

    private fun removeDynamicItemByTag(tag: String) {
        val item = dynamicItems.find { it.tag == tag }
        if (item != null) {
            gameCanvas.removeView(item)
            dynamicItems.remove(item)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableDrag(item: ImageView) {
        item.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val maxX = gameCanvas.width - v.width
                    val maxY = gameCanvas.height - v.height
                    v.translationX = (event.rawX - v.width / 2).coerceIn(0f, maxX.toFloat())
                    v.translationY = (event.rawY - v.height / 2).coerceIn(0f, maxY.toFloat())
                }
                MotionEvent.ACTION_UP -> {
                    val itemRect = Rect()
                    val leftRect = Rect()
                    val rightRect = Rect()

                    v.getGlobalVisibleRect(itemRect)
                    basketLeft.getGlobalVisibleRect(leftRect)
                    basketRight.getGlobalVisibleRect(rightRect)

                    if (Rect.intersects(itemRect, leftRect) || Rect.intersects(itemRect, rightRect)) {
                        v.animate().alpha(0f).setDuration(300).withEndAction {
                            v.visibility = View.INVISIBLE
                            Toast.makeText(this, "Dropped!", Toast.LENGTH_SHORT).show()
                            lastDroppedItemTag = v.tag.toString()
                            if (isHost) Toast.makeText(this, "Shake to send $lastDroppedItemTag", Toast.LENGTH_SHORT).show()
                        }.start()
                    }
                }
            }
            true
        }
    }

    private fun listenToRoomState() {
        roomListener = db.collection("rooms").document(roomCode)
            .addSnapshotListener { snapshot, _ ->
                val tag = snapshot?.getString("droppedItem") ?: return@addSnapshotListener
                if (!isHost) {
                    val item = createDynamicItem(tag, getDrawableResForTag(tag))
                    item.alpha = 0f
                    item.animate().alpha(1f).setDuration(500).start()
                }
            }
    }

    private fun getDrawableResForTag(tag: String): Int {
        return when (tag) {
            "chicken" -> R.drawable.chicken
            "avocado" -> R.drawable.avocado
            "lemon" -> R.drawable.lemon
            "knife" -> R.drawable.knife
            "cuttingboard" -> R.drawable.cuttingboard
            "stove" -> R.drawable.stove
            else -> R.drawable.chicken
        }
    }

    private fun setupShakeDetection() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI)
        accel = 10f
        accelCurrent = SensorManager.GRAVITY_EARTH
        accelLast = SensorManager.GRAVITY_EARTH
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            accelLast = accelCurrent
            accelCurrent = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = accelCurrent - accelLast
            accel = accel * 0.9f + delta

            if (accel > 12 && lastDroppedItemTag != null) {
                db.collection("rooms").document(roomCode).update("droppedItem", lastDroppedItemTag)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun setupGameTimer() {
        db.collection("rooms").document(roomCode).get().addOnSuccessListener { doc ->
            val startTime = doc.getLong("start_time") ?: return@addOnSuccessListener
            val difficulty = doc.getString("difficulty") ?: "easy"

            val totalTime = when (difficulty.lowercase()) {
                "easy" -> 60000L
                "medium" -> 45000L
                "hard" -> 30000L
                else -> 60000L
            }

            val elapsed = System.currentTimeMillis() - startTime
            val remaining = (totalTime - elapsed).coerceAtLeast(0L)

            startCountdown(ceil(remaining / 1000.0).toInt())
        }
    }

    private fun startCountdown(seconds: Int) {
        countdownTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secs = millisUntilFinished / 1000
                countdownText.text = String.format("00:%02d", secs)
            }

            override fun onFinish() {
                countdownText.text = "00:00"
                Toast.makeText(this@PlayGameActivity, "Time's up!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@PlayGameActivity, CongratsActivity::class.java))
                finish()
            }
        }.start()
    }

    private fun setupStirring() {
        spoon.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    v.x = (event.rawX - v.width / 2).coerceIn(0f, (gameCanvas.width - v.width).toFloat())
                    v.y = (event.rawY - v.height / 2).coerceIn(0f, (gameCanvas.height - v.height).toFloat())
                }
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
        roomListener?.remove()
        if (isHost) sensorManager.unregisterListener(sensorListener)
    }
}
