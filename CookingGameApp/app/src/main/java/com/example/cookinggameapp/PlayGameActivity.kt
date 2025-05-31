package com.example.cookinggameapp

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import android.widget.FrameLayout
import android.widget.SeekBar

class PlayGameActivity : AppCompatActivity() {

    // 🔌 Nearby API variables
    private lateinit var connectionsClient: ConnectionsClient
    private var endpointId: String? = null
    private val SERVICE_ID = "com.example.cookinggameapp.NEARBY"
    private val STRATEGY = Strategy.P2P_CLUSTER

    private lateinit var potImage: ImageView
    private lateinit var chickenImage: ImageView
    private lateinit var stoveImage: ImageView
    private lateinit var fireSeekBar: SeekBar
    private lateinit var spoonImage: ImageView
    private lateinit var knife: ImageView
    private lateinit var lemon: ImageView
    private lateinit var avocado: ImageView
    private lateinit var cuttingboard: ImageView

    private var chopCount = 0
    private var currentChopTarget: ImageView? = null

    private var currentCookingItem: ImageView? = null
    private var cookingStartTime: Long = 0L
    private var isCooking = false
    private var isCookingDone = false

    private lateinit var redFillImage: ImageView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playgame)

        // Initialize Nearby API
        connectionsClient = Nearby.getConnectionsClient(this)
        startAdvertising() //  Start advertising on Phone A

        // Get draggable items
        chickenImage = findViewById<ImageView>(R.id.imageChicken)
        knife = findViewById<ImageView>(R.id.imageKnife)
        lemon = findViewById<ImageView>(R.id.imageLemon)
        avocado = findViewById<ImageView>(R.id.imageAvocado)
        cuttingboard = findViewById<ImageView>(R.id.imageCuttingboard)
        stoveImage = findViewById<ImageView>(R.id.imageStove)
        potImage = findViewById<ImageView>(R.id.imagePot)
        spoonImage = findViewById(R.id.imageSpoon)
        fireSeekBar = findViewById(R.id.fireSeekBar)
        fireSeekBar.visibility = View.GONE

        // Get basket areas
        val basketLeft = findViewById<ImageView>(R.id.imageBasketLeft)
        val basketRight = findViewById<ImageView>(R.id.imageBasketRight)

        // Enable drag on all items
        val allItems = listOf(chickenImage, knife, lemon, avocado, cuttingboard, stoveImage, spoonImage, potImage)
        allItems.forEach { item ->
            enableDrag(item, basketLeft, basketRight)
        }

        setupAdvancedStirring()
        setupChopping()
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

                        if (elapsed >= 3000) { // 3 seconds
                            triggerRedFill()
                            hasFilled = true
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
        knife.setOnClickListener {
            // Find what the knife is overlapping
            val targets = listOf(avocado, lemon, chickenImage) // Add all items that can be chopped

            currentChopTarget = targets.firstOrNull { isViewOverlapping(knife, it) }

            if (currentChopTarget != null) {
                chopCount++
                if (chopCount >= 5) {
                    currentChopTarget?.setImageResource(R.drawable.chickenleg) // or other result image
                    chopCount = 0
                }
            } else {
                Toast.makeText(this, "Place knife over an item to chop", Toast.LENGTH_SHORT).show()
                chopCount = 0
            }
        }
    }

    // ✋ Drag-and-drop logic
    private fun enableDrag(view: ImageView, basketLeft: View, basketRight: View) {
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
                    v.performClick()
                    val itemBox = Rect()
                    val leftBox = Rect()
                    val rightBox = Rect()

                    v.getGlobalVisibleRect(itemBox)
                    basketLeft.getGlobalVisibleRect(leftBox)
                    basketRight.getGlobalVisibleRect(rightBox)

                    when {
                        Rect.intersects(itemBox, leftBox) -> animateIntoBasket(v, basketLeft)
                        Rect.intersects(itemBox, rightBox) -> animateIntoBasket(v, basketRight)
                    }
                }
            }
            true
        }
    }

    // 🧺 Drop item into basket + send payload
    private fun animateIntoBasket(view: View, basket: View) {
        val basketLocation = IntArray(2)
        basket.getLocationOnScreen(basketLocation)

        val viewLocation = IntArray(2)
        view.getLocationOnScreen(viewLocation)

        val deltaX = (basketLocation[0] + basket.width / 2) - (viewLocation[0] + view.width / 2)
        val deltaY = (basketLocation[1] + basket.height / 2) - (viewLocation[1] + view.height / 2)

        view.animate()
            .translationXBy(deltaX.toFloat())
            .translationYBy(deltaY.toFloat())
            .setDuration(300)
            .withEndAction {
                view.visibility = View.INVISIBLE
                Toast.makeText(this, "Item placed!", Toast.LENGTH_SHORT).show()
                sendChicken() // ✅ Send message to Phone B
            }
            .start()
    }

    // 📡 Start advertising (host)
    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            "Phone A",
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        )
    }

    // 🐔 Send payload to receiver
    private fun sendChicken() {
        val payload = Payload.fromBytes("chicken_sent".toByteArray())
        endpointId?.let {
            connectionsClient.sendPayload(it, payload)
        }
    }

    // 🔁 Handle connection lifecycle
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                this@PlayGameActivity.endpointId = endpointId
                Toast.makeText(
                    this@PlayGameActivity,
                    "Connected to $endpointId",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun onDisconnected(endpointId: String) {
            Toast.makeText(this@PlayGameActivity, "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    // 📦 Payload callback (receive on Phone A, not used here)
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            // Not expecting payloads here, but could handle replies
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }
}
