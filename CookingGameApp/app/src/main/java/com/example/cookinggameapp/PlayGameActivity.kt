package com.example.cookinggameapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import android.os.Handler
import android.os.Looper
import android.content.Intent
import com.google.firebase.firestore.DocumentChange

class PlayGameActivity : BaseActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var roomCode: String
    private var roomListener: ListenerRegistration? = null
    private var isHost: Boolean = false
    var dropDirection: String? = null
    private lateinit var dbHandler: DatabaseHandler
    private lateinit var currentPlayerId: String
    private var playerPosition: Int = 0 // 0=Host, 1=Player1, 2=Player2, 3=Player3
    private lateinit var recipeStepText: TextView

    private lateinit var chicken: ImageView
    private lateinit var avocado: ImageView
    private lateinit var lemon: ImageView
    private lateinit var knife: ImageView
    private lateinit var cuttingBoard: ImageView

    private lateinit var fireSeekBar: SeekBar
    private lateinit var stove: ImageView
    private lateinit var pot: ImageView

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

    private var lastDroppedItemTag: String? = null

    // All draggable items
    private lateinit var allItems: List<ImageView>
    private lateinit var basketLeft: ImageView
    private lateinit var basketRight: ImageView
    private lateinit var spoon: ImageView

    private lateinit var currentRecipe: Recipe
    private var currentStepIndex = 0

    // Player management
    private val playerIds = mutableListOf<String>()
    private lateinit var leftNeighborId: String
    private lateinit var rightNeighborId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playgame)

        db = FirebaseFirestore.getInstance()
        roomCode = intent.getStringExtra("roomCode") ?: return
        isHost = intent.getBooleanExtra("isHost", false)
        dbHandler = DatabaseHandler()
        currentPlayerId = intent.getStringExtra("playerId") ?: "PlayerUnknown"

        // ADD THESE DEBUG LOGS
        Log.d("DEBUG_TRACE", "PlayGameActivity onCreate()")
        Log.d("DEBUG_TRACE", "roomCode: '$roomCode'")
        Log.d("DEBUG_TRACE", "currentPlayerId: '$currentPlayerId'")
        Log.d("DEBUG_TRACE", "isHost: $isHost")

        // Init references
        chicken = findViewById<ImageView>(R.id.imageChicken).apply { tag = "chicken" }
        avocado = findViewById<ImageView>(R.id.imageAvocado).apply { tag = "avocado" }
        lemon = findViewById<ImageView>(R.id.imageLemon).apply { tag = "lemon" }
        knife = findViewById<ImageView>(R.id.imageKnife).apply { tag = "knife" }
        cuttingBoard = findViewById<ImageView>(R.id.imageCuttingboard).apply { tag = "cuttingboard" }
        pot = findViewById<ImageView>(R.id.imagePot).apply { tag = "pot" }
        stove = findViewById<ImageView>(R.id.imageStove).apply { tag = "stove" }
        spoon = findViewById<ImageView>(R.id.imageSpoon).apply { tag = "spoon" }

        basketLeft = findViewById(R.id.imageBasketLeft)
        basketRight = findViewById(R.id.imageBasketRight)
        countdownText = findViewById(R.id.countdownText)

        fireSeekBar = findViewById(R.id.fireSeekBar)
        fireSeekBar.visibility = View.GONE
        redFillImage = findViewById(R.id.imageRedFill)

        allItems = listOf(chicken, avocado, lemon, knife, cuttingBoard, pot, stove, spoon)

        // Setup game timer
        setupGameTimer()

        // Listen to room state
        listenToRoomState()

        // Setup recipe
        recipeStepText = findViewById(R.id.textRecipeStep)
        currentRecipe = GameRecipes.allRecipes.random()
        currentStepIndex = 0
        showNextRecipeStep()

        // Only host handles shake detection
        if (isHost) {
            setupShakeDetection()
        }

        initializePlayerPositions()
    }

    private fun initializePlayerPositions() {
        Log.d("DEBUG_TRACE", "🔥 initializePlayerPositions() called")

        db.collection("rooms").document(roomCode).get()
            .addOnSuccessListener { document ->
                Log.d("DEBUG_TRACE", "🔥 Firebase document retrieved successfully")

                val players = document.get("players") as? List<String> ?: return@addOnSuccessListener
                Log.d("DEBUG_TRACE", "Players in room: $players")

                playerIds.clear()
                playerIds.addAll(players)

                playerPosition = playerIds.indexOf(currentPlayerId)
                if (playerPosition == -1) playerPosition = 0

                val totalPlayers = playerIds.size
                leftNeighborId = playerIds[(playerPosition - 1 + totalPlayers) % totalPlayers]
                rightNeighborId = playerIds[(playerPosition + 1) % totalPlayers]

                Log.d("DEBUG_TRACE", "Player $currentPlayerId at position $playerPosition")
                Log.d("DEBUG_TRACE", "Left neighbor: $leftNeighborId, Right neighbor: $rightNeighborId")

                setupPlayerSpecificContent()

            }
            .addOnFailureListener { e ->
                Log.e("DEBUG_TRACE", "❌ Failed to get player positions", e)
            }
    }

    // New method that runs after Firebase returns
    private fun setupPlayerSpecificContent() {
        Log.d("DEBUG_SETUP", "🔥 setupPlayerSpecificContent() for player $playerPosition")

        // Distribute items based on player role
        distributeItemsBasedOnRole()

        // Set up visibility and interactions for items
        allItems.forEach { item ->
            val itemTag = item.tag?.toString() ?: ""
            val shouldHave = shouldPlayerHaveItem(itemTag)

            Log.d("DEBUG_ITEMS", "Item $itemTag: player $playerPosition should have = $shouldHave")

            if (shouldHave) {
                enableDrag(item)
                item.visibility = View.VISIBLE
                Log.d("DEBUG_ITEMS", "Enabled drag and made visible: $itemTag")
            } else {
                item.visibility = View.INVISIBLE
                Log.d("DEBUG_ITEMS", "Made invisible: $itemTag")
            }
        }

        setupAdvancedStirring()
        setupChopping()

        Log.d("DEBUG_SETUP", "✅ All player-specific content setup complete")
    }

    private fun distributeItemsBasedOnRole() {
        when (playerPosition) {
            0 -> { // Host - has pot and ingredients
                scatterViewsWithoutOverlap(listOf(chicken, lemon, pot, spoon))
            }
            1 -> { // Player 1 - has knife and cutting board
                scatterViewsWithoutOverlap(listOf(knife, cuttingBoard))
            }
            2 -> { // Player 2 - has spoon (for stirring)
                scatterViewsWithoutOverlap(listOf(stove))
            }
            3 -> { // Player 3 - has stove
                scatterViewsWithoutOverlap(listOf(avocado))
            }
        }
    }

    private fun shouldPlayerHaveItem(itemTag: String): Boolean {
        val shouldHave = when (playerPosition) {
            0 -> itemTag in listOf("chicken", "lemon", "pot", "spoon") // Host has ingredients and knife
            1 -> itemTag in listOf("cuttingboard", "knife") // Player 1 has cutting board and spoon
            2 -> itemTag in listOf("stove") // Player 2 has stove
            3 -> itemTag in listOf("avocado") // Player 3 has additional items
            else -> false
        }

        Log.d("DEBUG_ITEMS", "shouldPlayerHaveItem($itemTag) for player $playerPosition = $shouldHave")
        return shouldHave
    }

    private fun setupGameTimer() {
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
            val remaining = totalTimeMillis - elapsed
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
    }

    private fun setupShakeDetection() {
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

    private fun startCountdown(seconds: Int) {
        countdownTimer = object : CountDownTimer((seconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                countdownText.text = String.format("00:%02d", secondsLeft)
            }

            override fun onFinish() {
                countdownText.text = "00:00"
                Toast.makeText(this@PlayGameActivity, "Time's up!", Toast.LENGTH_SHORT).show()
                vibrateDevice()

                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this@PlayGameActivity, CongratsActivity::class.java))
                }, 3000)
            }
        }.start()
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

                    // Check for cooking interaction (only if player has stove)
                    if (v != stove && shouldPlayerHaveItem("stove") && isViewOverlapping(v, stove)) {
                        currentCookingItem = v as ImageView
                        showFireSlider()
                    } else if (currentCookingItem == v && !isViewOverlapping(v, stove)) {
                        hideFireSlider()
                        currentCookingItem = null
                    }
                }

                MotionEvent.ACTION_UP -> {
                    handleItemDrop(v)
                }
            }
            true
        }
    }

    private fun handleItemDrop(view: View) {
        Log.d("DEBUG_TRACE", "🔥 handleItemDrop() called")

        val itemBox = Rect()
        val leftBox = Rect()
        val rightBox = Rect()

        view.getGlobalVisibleRect(itemBox)
        basketLeft.getGlobalVisibleRect(leftBox)
        basketRight.getGlobalVisibleRect(rightBox)

        val itemId = view.tag?.toString()
        if (itemId == null) {
            Log.w("DEBUG_TRACE", "⚠️ Warning: View with no tag dropped!")
            return
        }

        Log.d("DEBUG_TRACE", "Item dropped: $itemId")
        Log.d("DEBUG_TRACE", "Left neighbor ID: $leftNeighborId")
        Log.d("DEBUG_TRACE", "Right neighbor ID: $rightNeighborId")

        when {
            Rect.intersects(itemBox, rightBox) -> {
                Log.d("DEBUG_TRACE", "🔥 Item dropped on RIGHT basket")
                // Check if neighbors are initialized
                if (!::rightNeighborId.isInitialized || rightNeighborId.isBlank()) {
                    Log.e("DEBUG_TRACE", "❌ Right neighbor ID not initialized!")
                    return
                }

                // Pass to right neighbor
                passItemToPlayer(itemId, rightNeighborId, "right")
                animateIntoBasket(view)
                lastDroppedItemTag = itemId
                dropDirection = "right"
            }

            Rect.intersects(itemBox, leftBox) -> {
                Log.d("DEBUG_TRACE", "🔥 Item dropped on LEFT basket")
                // Check if neighbors are initialized
                if (!::leftNeighborId.isInitialized || leftNeighborId.isBlank()) {
                    Log.e("DEBUG_TRACE", "❌ Left neighbor ID not initialized!")
                    return
                }

                // Pass to left neighbor
                passItemToPlayer(itemId, leftNeighborId, "left")
                animateIntoBasket(view)
                lastDroppedItemTag = itemId
                dropDirection = "left"
            }

            else -> {
                Log.d("DEBUG_TRACE", "🔥 Item dropped outside baskets")
                Toast.makeText(this, "Drop on a basket to pass to neighbors!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun passItemToPlayer(itemId: String, receiverId: String, direction: String) {
        Log.d("DEBUG_TRACE", "🔥 passItemToPlayer() CALLED!")

        // Validate required fields FIRST
        if (currentPlayerId.isBlank()) {
            Log.e("DEBUG_TRACE", "❌ ERROR: currentPlayerId is blank!")
            Toast.makeText(this, "Player ID error!", Toast.LENGTH_SHORT).show()
            return
        }

        if (receiverId.isBlank()) {
            Log.e("DEBUG_TRACE", "❌ ERROR: receiverId is blank!")
            Toast.makeText(this, "Receiver ID error!", Toast.LENGTH_SHORT).show()
            return
        }

        if (roomCode.isBlank()) {
            Log.e("DEBUG_TRACE", "❌ ERROR: roomCode is blank!")
            Toast.makeText(this, "Room code error!", Toast.LENGTH_SHORT).show()
            return
        }

        // Update Firebase with the item transfer
        val transferData = mapOf<String, Any>(
            "from" to currentPlayerId,
            "to" to receiverId,
            "item" to itemId,
            "direction" to direction,
            "timestamp" to System.currentTimeMillis()
        )

        Log.d("DEBUG_TRACE", "=== TRANSFER DEBUG INFO ===")
        Log.d("DEBUG_TRACE", "currentPlayerId: '$currentPlayerId'")
        Log.d("DEBUG_TRACE", "receiverId: '$receiverId'")
        Log.d("DEBUG_TRACE", "itemId: '$itemId'")
        Log.d("DEBUG_TRACE", "direction: '$direction'")
        Log.d("DEBUG_TRACE", "roomCode: '$roomCode'")
        Log.d("DEBUG_TRACE", "transferData: $transferData")
        Log.d("DEBUG_TRACE", "=============================")

        val transfersRef = db.collection("rooms").document(roomCode).collection("transfers")
        Log.d("DEBUG_TRACE", "Writing to Firebase path: rooms/$roomCode/transfers")

        transfersRef.add(transferData)
            .addOnSuccessListener { documentReference ->
                Log.d("DEBUG_TRACE", "✅ SUCCESS: Item $itemId sent from $currentPlayerId to $receiverId")
                Log.d("DEBUG_TRACE", "Document ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("DEBUG_TRACE", "❌ FAILED to transfer item", e)
                Log.e("DEBUG_TRACE", "Error message: ${e.message}")
                Log.e("DEBUG_TRACE", "Error cause: ${e.cause}")
                Toast.makeText(this, "Transfer failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun animateIntoBasket(view: View) {
        view.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                view.visibility = View.INVISIBLE
            }.start()
    }

    private fun listenToRoomState() {
        // Listen for incoming transfers
        db.collection("rooms").document(roomCode)
            .collection("transfers")
            .whereEqualTo("to", currentPlayerId)
            .addSnapshotListener { snapshots, _ ->
                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val transfer = change.document.data
                        val itemId = transfer["item"] as? String ?: return@forEach
                        val fromPlayer = transfer["from"] as? String ?: return@forEach
                        val direction = transfer["direction"] as? String ?: return@forEach

                        receiveItem(itemId, fromPlayer, direction)

                        // Mark transfer as processed
//                        change.document.reference.delete()
                    }
                }
            }
        db.collection("rooms").document(roomCode)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val newStepIndex = snapshot.getLong("currentStepIndex")?.toInt() ?: 0

                if (newStepIndex != currentStepIndex) {
                    currentStepIndex = newStepIndex
                    showNextRecipeStep()
                }
            }
    }

    private fun receiveItem(itemId: String, fromPlayer: String, direction: String) {
        val view = allItems.find { it.tag == itemId } ?: return

        // Determine entry point based on direction
        val entryBasket = if (direction == "right") basketLeft else basketRight
        val basketX = entryBasket.x
        val basketY = entryBasket.y

        // Animate item flying in
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
                Toast.makeText(this, "$itemId received from $fromPlayer!", Toast.LENGTH_SHORT).show()
                vibrateDevice()

                // Make item draggable for this player
                enableDrag(view)

                // Scatter the item to avoid overlapping
                scatterSingleItem(view)
            }.start()
    }

    private fun scatterSingleItem(view: View) {
        val parent = findViewById<FrameLayout>(R.id.gameCanvas)

        parent.post {
            val parentWidth = parent.width
            val parentHeight = parent.height
            val reservedBottomSpace = 350

            val viewWidth = view.width
            val viewHeight = view.height

            // Find a random position that doesn't overlap with existing items
            var attempts = 0
            var placed = false

            while (!placed && attempts < 50) {
                val x = (50..(parentWidth - viewWidth - 50)).random()
                val y = (50..(parentHeight - reservedBottomSpace - viewHeight)).random()

                view.x = x.toFloat()
                view.y = y.toFloat()
                placed = true // Simplified - you could add overlap checking here
                attempts++
            }
        }
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
        val newStepIndex = currentStepIndex + 1
        db.collection("rooms").document(roomCode)
            .update("currentStepIndex", newStepIndex)
            .addOnSuccessListener {
                Log.d("GAME", "Advanced to step $newStepIndex")
            }
            .addOnFailureListener {
                Log.e("GAME", "Failed to update step", it)
            }
    }

    private fun showNextRecipeStep() {
        val step = currentRecipe.steps[currentStepIndex]
        recipeStepText.text = "Step ${currentStepIndex + 1}: ${step.step}"
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

        // Only setup stirring if this player should have the spoon
        if (!shouldPlayerHaveItem("spoon")) return

        var lastTouchX = 0f
        var lastTouchY = 0f
        var rotationAngle = 0f
        var stirStartTime: Long = 0
        var hasFilled = false

        spoon.setOnTouchListener { view, event ->
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
                        spoon.rotation = rotationAngle

                        if (elapsed >= 1000 && isCurrentStepInvolves("stirring")) {
                            triggerRedFill()
                            hasFilled = true
                            vibrateDevice()
                            advanceToNextStep()
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
        pot.getGlobalVisibleRect(potRect)
        spoon.getGlobalVisibleRect(spoonRect)
        return Rect.intersects(potRect, spoonRect)
    }

    private fun triggerRedFill() {
        if (redFillImage.visibility != View.VISIBLE) {
            redFillImage.visibility = View.VISIBLE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupChopping() {
        Log.d("DEBUG_CHOPPING", "🔥 Setting up chopping for player $playerPosition")

        // Check if this player should have the knife
        val shouldHaveKnife = shouldPlayerHaveItem("knife")
        Log.d("DEBUG_CHOPPING", "Player $playerPosition should have knife: $shouldHaveKnife")

        if (!shouldHaveKnife) {
            Log.d("DEBUG_CHOPPING", "Player $playerPosition should not have knife, skipping chopping setup")
            return
        }

        Log.d("DEBUG_CHOPPING", "✅ Player $playerPosition setting up chopping with knife")

        knife.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d("DEBUG_CHOPPING", "🔪 Knife touched down")
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val parent = view.parent as View
                    val maxX = parent.width - view.width
                    val maxY = parent.height - view.height
                    view.translationX = (event.rawX - view.width / 2).coerceIn(0f, maxX.toFloat())
                    view.translationY = (event.rawY - view.height / 2).coerceIn(0f, maxY.toFloat())
                    true
                }

                MotionEvent.ACTION_UP -> {
                    Log.d("DEBUG_CHOPPING", "🔪 Knife released - checking for targets")

                    // Find all visible choppable items
                    val targets = listOf(avocado, lemon, chicken).filter {
                        val isVisible = it.visibility == View.VISIBLE
                        Log.d("DEBUG_CHOPPING", "${it.tag} visibility: $isVisible")
                        isVisible
                    }

                    Log.d("DEBUG_CHOPPING", "Available choppable targets: ${targets.map { it.tag }}")

                    if (targets.isEmpty()) {
                        Log.w("DEBUG_CHOPPING", "⚠️ No visible targets found!")
                        return@setOnTouchListener true
                    }

                    currentChopTarget = targets.firstOrNull { target ->
                        val isOverlapping = isViewOverlapping(knife, target)
                        Log.d("DEBUG_CHOPPING", "Checking ${target.tag}: overlapping = $isOverlapping")
                        isOverlapping
                    }

                    if (currentChopTarget != null) {
                        chopCount++
                        Log.d("DEBUG_CHOPPING", "✅ Chopping ${currentChopTarget?.tag}, count: $chopCount/2")
                        vibrateDevice()

                        if (chopCount >= 1) {
                            Log.d("DEBUG_CHOPPING", "🎉 Chopping complete!")
                            currentChopTarget?.setImageResource(R.drawable.chickenleg)

                            if (isCurrentStepInvolves("chopping")) {
                                Log.d("DEBUG_CHOPPING", "Advancing to next recipe step")
                                advanceToNextStep()
                            }
                            chopCount = 0
                            currentChopTarget = null
                        }
                    } else {
                        Log.d("DEBUG_CHOPPING", "❌ No target found - knife not over any ingredient")
                        Toast.makeText(this@PlayGameActivity, "Place knife over an ingredient to chop", Toast.LENGTH_SHORT).show()
                        chopCount = 0
                    }
                    true
                }

                else -> false
            }
        }
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

            if (accel > 12 && lastDroppedItemTag != null) {
                confirmItemTransfer()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun confirmItemTransfer() {
        lastDroppedItemTag?.let { itemTag ->
            val receiverId = if (dropDirection == "right") rightNeighborId else leftNeighborId
            vibrateDevice()
            lastDroppedItemTag = null
            dropDirection = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
        roomListener?.remove()
        if (isHost) sensorManager.unregisterListener(sensorListener)
    }

    private fun scatterViewsWithoutOverlap(views: List<View>) {
        val parent = findViewById<FrameLayout>(R.id.gameCanvas)

        parent.post {
            val parentWidth = parent.width
            val parentHeight = parent.height

            val reservedBottomSpace = 350
            val reservedCenterWidth = 150
            val reservedCenterHeight = 300

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

                    val padding = 16
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