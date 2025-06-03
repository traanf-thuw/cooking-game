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

class PlayGameActivity : BaseActivity() {

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

    private lateinit var fireSeekBar: SeekBar
    private lateinit var spoonImage: ImageView
    private lateinit var stoveImage: ImageView
    private lateinit var potImage: ImageView

    private var chopCount = 0
    private var currentChopTarget: ImageView? = null
    private var countdownTimer: CountDownTimer? = null
    private lateinit var countdownText: TextView

    private var currentCookingItem: ImageView? = null
    private var cookingStartTime: Long = 0L
    private var isCooking = false
    private var isCookingDone = false

    private lateinit var redFillImage: ImageView

    private lateinit var sensorManager: SensorManager
    private var accel = 0f
    private var accelCurrent = 0f
    private var accelLast = 0f

    private lateinit var currentRecipe: Recipe
    private var currentStepIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playgame)

        // Get Firestore and intent info
        db = FirebaseFirestore.getInstance()
        roomCode = intent.getStringExtra("roomCode") ?: return
        isHost = intent.getBooleanExtra("isHost", false)


        countdownText = findViewById(R.id.countdownText)

        scatterViewsWithoutOverlap(
            listOf(
                findViewById(R.id.imageChicken),
                findViewById(R.id.imageAvocado),
                findViewById(R.id.imageKnife),
                findViewById(R.id.imageLemon),
                findViewById(R.id.imageCuttingboard),
                findViewById(R.id.imageStove)
            )
        )


        db.collection("rooms").document(roomCode).get().addOnSuccessListener { document ->
            val startTime = document.getLong("start_time") ?: return@addOnSuccessListener
            val difficulty = document.getString("difficulty") ?: "easy"

            val totalTimeMillis = when (difficulty.lowercase()) {
                "easy" -> 60_000L
                "medium" -> 45_000L
                "hard" -> 30_000L
                else -> 60_000L
            }

            val elapsed = System.currentTimeMillis() - startTime
            val remaining = totalTimeMillis - elapsed //This is the calculated remaining time
            val clampedRemaining = remaining.coerceAtLeast(0L)
            val remainingSeconds = kotlin.math.ceil(clampedRemaining / 1000.0).toInt()

            if (remainingSeconds > 0) {
                Log.d("PlayGame", "Starting countdown: $remainingSeconds seconds left")
                startCountdown(remainingSeconds)
            } else {
                countdownText.text = "00:00"
                Toast.makeText(this, "Time's up!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Log.e("PlayGame", "Failed to fetch room data", it)
        }

        // Grab UI
        chicken = findViewById(R.id.imageChicken)
        avocado = findViewById(R.id.imageAvocado)
        lemon = findViewById(R.id.imageLemon)
        knife = findViewById(R.id.imageKnife)
        cuttingBoard = findViewById(R.id.imageCuttingboard)
        basketLeft = findViewById(R.id.imageBasketLeft)
        basketRight = findViewById(R.id.imageBasketRight)
        stoveImage = findViewById<ImageView>(R.id.imageStove)
        potImage = findViewById<ImageView>(R.id.imagePot)
        spoonImage = findViewById(R.id.imageSpoon)
        fireSeekBar = findViewById(R.id.fireSeekBar)
        fireSeekBar.visibility = View.GONE

        // 🔁 Start Firebase listener for state sync
        listenToRoomState()

        // ✅ Set drag for other items
        enableDrag(avocado)
        enableDrag(lemon)
        enableDrag(knife)
        enableDrag(cuttingBoard)
        enableDrag(chicken)
        enableDrag(potImage)
        enableDrag(stoveImage)
        enableDrag(spoonImage)

        currentRecipe = GameRecipes.allRecipes.random()
        currentStepIndex = 0
        showNextRecipeStep()

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

        setupAdvancedStirring()
        setupChopping()
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
                            if (isCurrentStepInvolves("cooking")) {
                                advanceToNextStep()
                            }
                            currentCookingItem?.setImageResource(R.drawable.carrot)
                            vibrateDevice()
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

    private fun isCurrentStepInvolves(action: String): Boolean {
        return currentRecipe.steps.getOrNull(currentStepIndex)?.involves?.contains(action) == true
    }

    private fun advanceToNextStep() {
        currentStepIndex++
        if (currentStepIndex >= currentRecipe.steps.size) {
            Toast.makeText(this, "🎉 Recipe complete!", Toast.LENGTH_LONG).show()
        } else {
            showNextRecipeStep()
        }
    }

    private fun showNextRecipeStep() {
        val step = currentRecipe.steps[currentStepIndex]
        Toast.makeText(this, "Next: ${step.step}", Toast.LENGTH_SHORT).show()
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

                        if (elapsed >= 1000 && isCurrentStepInvolves("stirring")) { // 3 seconds
                            triggerRedFill()
                            hasFilled = true
                            vibrateDevice()
                            advanceToNextStep()
                        }
                    } else {
                        stirStartTime = 0 // reset if not inside
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
        knife.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val parent = view.parent as View
                    val maxX = parent.width - view.width
                    val maxY = parent.height - view.height
                    view.translationX = (event.rawX - view.width / 2).coerceIn(0f, maxX.toFloat())
                    view.translationY = (event.rawY - view.height / 2).coerceIn(0f, maxY.toFloat())
                }

                MotionEvent.ACTION_UP -> {
                    val targets = listOf(avocado, lemon, chicken)
                    currentChopTarget = targets.firstOrNull { isViewOverlapping(knife, it) }

                    if (currentChopTarget != null) {
                        chopCount++
                        if (chopCount >= 2) {
                            currentChopTarget?.setImageResource(R.drawable.chickenleg)
                            vibrateDevice()
                            if (isCurrentStepInvolves("chopping")) {
                                advanceToNextStep()
                            }
                            chopCount = 0
                        }
                    } else {
                        Toast.makeText(this, "Place knife over an item to chop", Toast.LENGTH_SHORT).show()
                        chopCount = 0
                    }
                }
            }
            true
        }
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

    private fun listenToRoomState() {
        roomListener = db.collection("rooms").document(roomCode)
            .addSnapshotListener { snapshot, _ ->
                val chickenDropped = snapshot?.getBoolean("chickenDropped") ?: false
                if (!isHost && chickenDropped) {
                    // Get basket position
                    val basketX = basketLeft.x
                    val basketY = basketLeft.y

// Start from above screen, invisible and small
                    chicken.translationX = basketX
                    chicken.translationY = -300f
                    chicken.alpha = 0f
                    chicken.scaleX = 0.3f
                    chicken.scaleY = 0.3f
                    chicken.rotation = 0f
                    chicken.visibility = View.VISIBLE

// Animate: spin + fly in
                    chicken.animate()
                        .translationY(basketY)
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .rotationBy(1440f) // 4 full spins (360 * 4)
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
        countdownTimer?.cancel()
        roomListener?.remove()
        if (isHost) {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    private fun scatterViewsWithoutOverlap(views: List<View>) {
        val parent = findViewById<FrameLayout>(R.id.gameCanvas)

        parent.post {
            val parentWidth = parent.width
            val parentHeight = parent.height

            val reservedBottomSpace = 350  // bottom off-limits
            val reservedCenterWidth = 150  // width of center exclusion zone
            val reservedCenterHeight = 300 // height of center exclusion zone

            val centerX = parentWidth / 2
            val centerY = parentHeight / 2
            val centerRect = android.graphics.Rect(
                centerX - reservedCenterWidth / 2,
                centerY - reservedCenterHeight / 2,
                centerX + reservedCenterWidth / 2,
                centerY + reservedCenterHeight / 2
            )

            val placedRects = mutableListOf<android.graphics.Rect>()

            views.forEach { view ->
                val viewWidth = view.width
                val viewHeight = view.height

                var attempts = 0
                var placed = false

                while (!placed && attempts < 100) {
                    val x = (0..(parentWidth - viewWidth)).random()
                    val y = (0..(parentHeight - reservedBottomSpace - viewHeight)).random()

                    val padding = 16  // Minimum distance between items in pixels
                    val newRect = android.graphics.Rect(
                        x - padding,
                        y - padding,
                        x + viewWidth + padding,
                        y + viewHeight + padding
                    )

                    val overlapsPlaced = placedRects.any { it.intersect(newRect) }
                    val overlapsCenter = android.graphics.Rect.intersects(newRect, centerRect)

                    if (!overlapsPlaced && !overlapsCenter) {
                        view.x = x.toFloat()
                        view.y = y.toFloat()
                        placedRects.add(newRect)
                        placed = true
                    }

                    attempts++
                }

                if (!placed) {
                    Log.w("Scatter", "⚠ Could not place ${view.contentDescription}")
                }
            }
        }
    }
}
