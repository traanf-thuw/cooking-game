package com.example.cookinggameapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PlayGameActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var roomCode: String
    private var roomListener: ListenerRegistration? = null

    private var isHost: Boolean = false
    private var countdownTimer: CountDownTimer? = null

    private lateinit var countdownText: TextView
    private lateinit var chicken: ImageView
    private lateinit var avocado: ImageView
    private lateinit var lemon: ImageView
    private lateinit var knife: ImageView
    private lateinit var cuttingBoard: ImageView
    private lateinit var basketLeft: ImageView
    private lateinit var basketRight: ImageView

    private lateinit var fireSeekBar: SeekBar
    private lateinit var spoonImage: ImageView
    private lateinit var stoveImage: ImageView
    private lateinit var potImage: ImageView

    private var chopCount = 0
    private var currentChopTarget: ImageView? = null

    private var currentCookingItem: ImageView? = null
    private var cookingStartTime: Long = 0L
    private var isCooking = false
    private var isCookingDone = false

    private lateinit var redFillImage: ImageView

    private lateinit var sensorManager: SensorManager
    private var accel = 0f
    private var accelCurrent = 0f
    private var accelLast = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playgame)

        db = FirebaseFirestore.getInstance()
        roomCode = intent.getStringExtra("roomCode") ?: return
        isHost = intent.getBooleanExtra("isHost", false)

        // 👇 Get difficulty and set timer
        val difficulty = intent.getStringExtra("difficulty") ?: "easy"
        val countdownSeconds = when (difficulty.lowercase()) {
            "easy" -> 60
            "medium" -> 40
            "hard" -> 30
            else -> 60
        }
        Log.d("PlayGame", "Starting game with $countdownSeconds seconds for difficulty: $difficulty")

        // 🔔 Start countdown
        countdownText = findViewById(R.id.countdownText)
        startCountdown(countdownSeconds)

        // Grab UI
        chicken = findViewById(R.id.imageChicken)
        avocado = findViewById(R.id.imageAvocado)
        lemon = findViewById(R.id.imageLemon)
        knife = findViewById(R.id.imageKnife)
        cuttingBoard = findViewById(R.id.imageCuttingboard)
        basketLeft = findViewById(R.id.imageBasketLeft)
        basketRight = findViewById(R.id.imageBasketRight)
        stoveImage = findViewById(R.id.imageStove)
        potImage = findViewById(R.id.imagePot)
        spoonImage = findViewById(R.id.imageSpoon)
        fireSeekBar = findViewById(R.id.fireSeekBar)
        redFillImage = findViewById(R.id.imageRedFill)

        fireSeekBar.visibility = View.GONE

        listenToRoomState()

        // Enable dragging
        enableDrag(avocado)
        enableDrag(lemon)
        enableDrag(knife)
        enableDrag(cuttingBoard)
        enableDrag(chicken, isChicken = true)
        enableDrag(potImage)
        enableDrag(stoveImage)
        enableDrag(spoonImage)

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
        } else {
            chicken.visibility = View.INVISIBLE
        }

        setupAdvancedStirring()
        setupChopping()
    }

    private fun startCountdown(seconds: Int) {
        countdownTimer = object : CountDownTimer((seconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                countdownText.text = String.format("00:%02d", secondsLeft)
            }

            override fun onFinish() {
                countdownText.text = "00:00"
                Toast.makeText(this@PlayGameActivity, "Time's up!", Toast.LENGTH_SHORT).show()
            }
        }.start()
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

                    if (v != stoveImage && isViewOverlapping(v, stoveImage)) {
                        currentCookingItem = v as ImageView
                        showFireSlider()
                    } else if (currentCookingItem == v) {
                        hideFireSlider()
                        currentCookingItem = null
                    }
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

    private fun showFireSlider() {
        if (fireSeekBar.visibility == View.VISIBLE) return

        fireSeekBar.visibility = View.VISIBLE
        fireSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 2 && !isCooking && currentCookingItem != null) {
                    isCooking = true
                    cookingStartTime = System.currentTimeMillis()

                    fireSeekBar.postDelayed({
                        if (fireSeekBar.progress == 2 && isCooking && currentCookingItem != null) {
                            isCookingDone = true
                            Toast.makeText(this@PlayGameActivity, "Cooking done!", Toast.LENGTH_SHORT).show()
                            currentCookingItem?.setImageResource(R.drawable.carrot)
                            hideFireSlider()
                        }
                    }, 3000)
                } else {
                    isCooking = false
                    cookingStartTime = 0L
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun hideFireSlider() {
        fireSeekBar.visibility = View.GONE
        fireSeekBar.progress = 0
        isCooking = false
        isCookingDone = false
    }

    private fun isViewOverlapping(view1: View, view2: View): Boolean {
        val rect1 = Rect()
        val rect2 = Rect()
        view1.getGlobalVisibleRect(rect1)
        view2.getGlobalVisibleRect(rect2)
        return Rect.intersects(rect1, rect2)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupAdvancedStirring() {
        redFillImage = findViewById(R.id.imageRedFill)
        spoonImage = findViewById(R.id.imageSpoon)
        val potContainer = findViewById<FrameLayout>(R.id.potContainer)

        var lastTouchX = 0f
        var lastTouchY = 0f
        var rotationAngle = 0f
        var stirStartTime: Long = 0
        var hasFilled = false

        spoonImage.setOnTouchListener { view, event ->
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
                        spoonImage.rotation = rotationAngle

                        if (elapsed >= 3000) {
                            triggerRedFill()
                            hasFilled = true
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
        potImage.getGlobalVisibleRect(potRect)
        spoonImage.getGlobalVisibleRect(spoonRect)
        return Rect.intersects(potRect, spoonRect)
    }

    private fun triggerRedFill() {
        if (redFillImage.visibility != View.VISIBLE) {
            redFillImage.visibility = View.VISIBLE
            Toast.makeText(this, "Stirring with spoon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupChopping() {
        knife.setOnClickListener {
            val targets = listOf(avocado, lemon, chicken)
            currentChopTarget = targets.firstOrNull { isViewOverlapping(knife, it) }

            if (currentChopTarget != null) {
                chopCount++
                if (chopCount >= 5) {
                    currentChopTarget?.setImageResource(R.drawable.chickenleg)
                    chopCount = 0
                }
            } else {
                Toast.makeText(this, "Place knife over an item to chop", Toast.LENGTH_SHORT).show()
                chopCount = 0
            }
        }
    }

    private fun listenToRoomState() {
        roomListener = db.collection("rooms").document(roomCode)
            .addSnapshotListener { snapshot, _ ->
                val chickenDropped = snapshot?.getBoolean("chickenDropped") ?: false
                if (!isHost && chickenDropped) {
                    val basketX = basketLeft.x
                    val basketY = basketLeft.y

                    chicken.translationX = basketX
                    chicken.translationY = -300f
                    chicken.alpha = 0f
                    chicken.scaleX = 0.3f
                    chicken.scaleY = 0.3f
                    chicken.rotation = 0f
                    chicken.visibility = View.VISIBLE

                    chicken.animate()
                        .translationY(basketY)
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .rotationBy(1440f)
                        .setDuration(1000)
                        .withEndAction {
                            Toast.makeText(this, "🐔 Chicken flew in with style!", Toast.LENGTH_SHORT).show()
                            vibrateDevice()
                        }
                        .start()
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

        val duration = 1500L
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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

            if (accel > 12 && isHost) {
                updateFirebaseForDrop()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
        roomListener?.remove()
        if (isHost) {
            sensorManager.unregisterListener(sensorListener)
        }
    }
}
