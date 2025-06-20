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
import android.widget.ImageButton
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

    private lateinit var player1Label: TextView
    private lateinit var player2Label: TextView
    private lateinit var player3Label: TextView

    private val choppedImageMap = mapOf(
        "chicken" to Pair("chicken_meat", R.drawable.chickenbreast),
        "lemon" to Pair("lemon_sliced", R.drawable.lemonslide),
        "avocado" to Pair("avocado_sliced", R.drawable.avocadoslide)
    )

    private var isGameInitialized = false
    private var isRecipeLoaded = false
    private var isPlayersLoaded = false

    private lateinit var itemTransferHandler: ItemTransferHandler
    private lateinit var gameTimerHandler: GameTimerHandler
    private lateinit var roomStateListener: RoomStateListener
    private lateinit var recipeLoader: RecipeLoader
    private lateinit var viewScatterer: ViewScatterer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playgame)

        try {
            db = FirebaseFirestore.getInstance()
            roomCode = intent.getStringExtra("roomCode") ?: run {
                Log.e("DEBUG_TRACE", "❌ No room code provided!")
                finish()
                return
            }

            isHost = intent.getBooleanExtra("isHost", false)
            dbHandler = DatabaseHandler()
            currentPlayerId = intent.getStringExtra("playerId") ?: run {
                Log.e("DEBUG_TRACE", "❌ No player ID provided!")
                finish()
                return
            }

            Log.d("DEBUG_TRACE", "PlayGameActivity onCreate()")
            Log.d("DEBUG_TRACE", "roomCode: '$roomCode'")
            Log.d("DEBUG_TRACE", "currentPlayerId: '$currentPlayerId'")
            Log.d("DEBUG_TRACE", "isHost: $isHost")

            initializeViews()

            // Load recipe and players in parallel, then initialize game
            recipeLoader = RecipeLoader(
                context = this,
                db = db,
                roomCode = roomCode,
                onLoaded = { recipe ->
                    currentRecipe = recipe
                    isRecipeLoaded = true
                    checkAndInitializeGame()
                },
                onFailure = { finish() }
            )
            recipeLoader.load()
            loadPlayerPositions()

        } catch (e: Exception) {
            Log.e("DEBUG_TRACE", "❌ Error in onCreate", e)
            finish()
        }
    }

    private fun initializeViews() {
        chicken = findViewById<ImageView>(R.id.imageChicken).apply { tag = "chicken" }
        avocado = findViewById<ImageView>(R.id.imageAvocado).apply { tag = "avocado" }
        lemon = findViewById<ImageView>(R.id.imageLemon).apply { tag = "lemon" }
        knife = findViewById<ImageView>(R.id.imageKnife).apply { tag = "knife" }
        cuttingBoard =
            findViewById<ImageView>(R.id.imageCuttingboard).apply { tag = "cuttingboard" }
        pot = findViewById<ImageView>(R.id.imagePot).apply { tag = "pot" }
        stove = findViewById<ImageView>(R.id.imageStove).apply { tag = "stove" }
        spoon = findViewById<ImageView>(R.id.imageSpoon).apply { tag = "spoon" }

        val chickenMeat = findViewById<ImageView>(R.id.imageChickenMeat).apply {
            tag = "chicken_meat"
            visibility = View.INVISIBLE
        }
        val lemonSliced = findViewById<ImageView>(R.id.imageLemonSliced).apply {
            tag = "lemon_sliced"
            visibility = View.INVISIBLE
        }
        val avocadoSliced = findViewById<ImageView>(R.id.imageAvocadoSliced).apply {
            tag = "avocado_sliced"
            visibility = View.INVISIBLE
        }

        basketLeft = findViewById(R.id.imageBasketLeft)
        basketRight = findViewById(R.id.imageBasketRight)
        countdownText = findViewById(R.id.countdownText)
        recipeStepText = findViewById(R.id.textRecipeStep)

        player1Label = findViewById(R.id.player1Label)
        player2Label = findViewById(R.id.player2Label)
        player3Label = findViewById(R.id.player3Label)


        fireSeekBar = findViewById(R.id.fireSeekBar)
        fireSeekBar.visibility = View.GONE
        redFillImage = findViewById(R.id.imageRedFill)

        allItems = listOf(
            chicken, avocado, lemon,
            chickenMeat, lemonSliced, avocadoSliced,
            knife, cuttingBoard, pot, stove, spoon
        )

        val recipeButton = findViewById<ImageButton>(R.id.buttonRecipe)
        recipeButton.setOnClickListener {
            val intent = Intent(this, RecipeGameActivity::class.java)
            startActivity(intent)
        }
    }


    private fun loadPlayerPositions() {
        val manager = PlayerPositionManager(
            context = this,
            db = db,
            roomCode = roomCode,
            currentPlayerId = currentPlayerId,
            onSuccess = { position, ids, leftId, rightId ->
                playerPosition = position
                playerIds.clear()
                playerIds.addAll(ids)
                leftNeighborId = leftId
                rightNeighborId = rightId
                isPlayersLoaded = true

                when (playerIds.size) {
                    1 -> {
                        player1Label.text = "You"
                        player2Label.text = "You"
                    }

                    2 -> {
                        val neighborId =
                            if (leftNeighborId == currentPlayerId) rightNeighborId else leftNeighborId
                        val label = "Player ${playerIds.indexOf(neighborId) + 1}"
                        player1Label.text = label
                        player2Label.text = label
                    }

                    else -> {
                        val leftLabel = "Player ${playerIds.indexOf(leftNeighborId) + 1}"
                        val rightLabel = "Player ${playerIds.indexOf(rightNeighborId) + 1}"
                        player1Label.text = leftLabel
                        player2Label.text = rightLabel
                    }
                }

                val currentPlayerNumber = playerIds.indexOf(currentPlayerId) + 1
                player3Label.text = "You (Player $currentPlayerNumber)"

                checkAndInitializeGame()
            },
            onFailure = {
                finish()
            }
        )
        manager.initializePlayerPositions()
    }


    private fun checkAndInitializeGame() {
        Log.d(
            "DEBUG_INIT",
            "Checking initialization: recipe=$isRecipeLoaded, players=$isPlayersLoaded, game=$isGameInitialized"
        )

        if (isRecipeLoaded && isPlayersLoaded && !isGameInitialized) {
            Log.d("DEBUG_INIT", "✅ All data loaded, initializing game...")
            isGameInitialized = true

            try {
                setupPlayerSpecificContent()
                showNextRecipeStep()
                setupGameTimer()

                viewScatterer = ViewScatterer(findViewById(R.id.gameCanvas))

                itemTransferHandler = ItemTransferHandler(
                    context = this,
                    db = db,
                    roomCode = roomCode,
                    currentPlayerId = currentPlayerId,
                    playerIds = playerIds,
                    getBasketLeft = { basketLeft },
                    getBasketRight = { basketRight },
                    allItems = allItems,
                    onItemReceived = { itemView ->
                        enableDrag(itemView)
                        viewScatterer.scatter(listOf(itemView))
                    },
                    vibrate = ::vibrateDevice
                )
                itemTransferHandler.startListening()

                roomStateListener = RoomStateListener(
                    db = db,
                    roomCode = roomCode,
                    currentPlayerId = currentPlayerId,
                    onStepAdvanced = { newStepIndex ->
                        currentStepIndex = newStepIndex
                        showNextRecipeStep()
                    },
                    onItemReceived = { itemId, fromPlayer, direction ->
                        itemTransferHandler.receiveItem(itemId, fromPlayer, direction)
                    }
                )
                roomStateListener.startListening()

                if (isHost) {
                    setupShakeDetection()

                    db.collection("rooms").document(roomCode).get().addOnSuccessListener { doc ->
                        if (!doc.contains("start_time")) {
                            val startTime = System.currentTimeMillis()
                            db.collection("rooms").document(roomCode).update("start_time", startTime)
                        }
                    }
                }

                Log.d("DEBUG_INIT", "🎉 Game initialization complete!")

            } catch (e: Exception) {
                Log.e("DEBUG_INIT", "❌ Error during game initialization", e)
                finish()
            }
        }
    }


    // Update showNextRecipeStep to be safer
    private fun showNextRecipeStep() {
        if (!::currentRecipe.isInitialized) {
            Log.w("RECIPE_SYNC", "Recipe not loaded yet, skipping step display")
            return
        }

        if (currentStepIndex < currentRecipe.steps.size) {
            val step = currentRecipe.steps[currentStepIndex]
            recipeStepText.text = "Step ${currentStepIndex + 1}: ${step.step}"
        } else {
            recipeStepText.text = "Recipe Complete!"
        }
    }

    // Update setupPlayerSpecificContent to be safer
    private fun setupPlayerSpecificContent() {
        if (!isPlayersLoaded) {
            Log.w("DEBUG_SETUP", "Players not loaded yet, skipping setup")
            return
        }

        Log.d("DEBUG_SETUP", "🔥 setupPlayerSpecificContent() for player $playerPosition")

        try {
            // Distribute items based on player role
            distributeItemsBasedOnRole()

            // Set up visibility and interactions for items
            allItems.forEach { item ->
                val itemTag = item.tag?.toString() ?: ""
                val shouldHave = shouldPlayerHaveItem(itemTag)

                Log.d(
                    "DEBUG_ITEMS",
                    "Item $itemTag: player $playerPosition should have = $shouldHave"
                )

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

        } catch (e: Exception) {
            Log.e("DEBUG_SETUP", "❌ Error in setupPlayerSpecificContent", e)
            finish()
        }
    }

    private fun distributeItemsBasedOnRole() {
        when (playerPosition) {
            0 -> { // Host - has pot and ingredients
                pot.visibility = View.VISIBLE
                spoon.visibility = View.VISIBLE
                scatterViewsWithoutOverlap(listOf(chicken, lemon))
            }
            1 -> { // Player 1 - has knife and cutting board
                scatterViewsWithoutOverlap(listOf(knife, cuttingBoard))
            }
            2 -> { // Player 2 - has stove (or stirring tools)
                scatterViewsWithoutOverlap(listOf(stove))
            }
            3 -> { // Player 3 - has avocado or other tools
                scatterViewsWithoutOverlap(listOf(avocado))
            }
        }
    }

    private fun shouldPlayerHaveItem(itemTag: String): Boolean {
        val shouldHave = when (playerPosition) {
            0 -> itemTag in listOf(
                "chicken",
                "lemon",
                "pot",
                "spoon"
            ) // Host has ingredients and knife
            1 -> itemTag in listOf("cuttingboard", "knife") // Player 1 has cutting board and spoon
            2 -> itemTag in listOf("stove") // Player 2 has stove
            3 -> itemTag in listOf("avocado") // Player 3 has additional items
            else -> false
        }

        Log.d(
            "DEBUG_ITEMS",
            "shouldPlayerHaveItem($itemTag) for player $playerPosition = $shouldHave"
        )
        return shouldHave
    }

    private fun setupGameTimer() {
        gameTimerHandler = GameTimerHandler(
            context = this,
            db = db,
            roomCode = roomCode,
            countdownText = countdownText
        ) {
            vibrateDevice()
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this@PlayGameActivity, CongratsActivity::class.java))
            }, 3000)
        }

        gameTimerHandler.start()
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


        setupAdvancedStirring()
        setupChopping()

        val recipeButton = findViewById<ImageButton>(R.id.buttonRecipe)
        recipeButton.setOnClickListener {
            val intent = Intent(this, RecipeGameActivity::class.java)
            startActivity(intent)
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

                    // Check for cooking interaction (only if player has stove)
                    if (v != stove && shouldPlayerHaveItem("stove") && isViewOverlapping(
                            v,
                            stove
                        )
                    ) {
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
        val itemId = view.tag?.toString() ?: return
        itemTransferHandler.handleItemDrop(view, itemId)
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

    private fun isViewOverlapping(view1: View, view2: View): Boolean {
        val rect1 = Rect()
        val rect2 = Rect()
        view1.getGlobalVisibleRect(rect1)
        view2.getGlobalVisibleRect(rect2)
        return Rect.intersects(rect1, rect2)
    }

    private fun setupAdvancedStirring() {
        val stirringHandler = StirringHandler(
            context = this,
            spoon = spoon,
            pot = pot,
            redFillImage = redFillImage,
            shouldPlayerHaveSpoon = shouldPlayerHaveItem("spoon"),
            vibrate = ::vibrateDevice,
            isCurrentStepInvolves = ::isCurrentStepInvolves,
            onStirComplete = ::advanceToNextStep
        )

        stirringHandler.setup()
    }

    private fun setupChopping() {
        val choppingHandler = ChoppingHandler(
            context = this,
            knife = knife,
            choppingTargets = listOf(avocado, lemon, chicken),
            choppedImageMap = choppedImageMap,
            shouldPlayerHaveKnife = shouldPlayerHaveItem("knife"),
            isViewOverlapping = ::isViewOverlapping,
            vibrate = ::vibrateDevice,
            isCurrentStepInvolves = ::isCurrentStepInvolves,
            onChopComplete = ::advanceToNextStep
        )

        choppingHandler.setup()
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
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    duration,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
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
        itemTransferHandler.stopListening()
        gameTimerHandler.cancel()
        roomStateListener.stopListening()
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