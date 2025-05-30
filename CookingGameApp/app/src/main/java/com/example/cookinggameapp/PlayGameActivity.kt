package com.example.cookinggameapp

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

class PlayGameActivity : AppCompatActivity() {

    // Nearby API
    private lateinit var connectionsClient: ConnectionsClient
    private var endpointId: String? = null
    private val SERVICE_ID = "com.example.cookinggameapp.NEARBY"
    private val STRATEGY = Strategy.P2P_CLUSTER

    // Countdown
    private var timeLimit: Int = 60
    private var countdownTimer: CountDownTimer? = null
    private lateinit var countdownText: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playgame)

        // Get time limit from intent
        timeLimit = intent.getIntExtra("timeLimit", 60)

        // Setup countdown view
        countdownText = findViewById(R.id.countdownText)
        startCountdown(timeLimit)

        // Nearby API
        connectionsClient = Nearby.getConnectionsClient(this)
        startAdvertising()

        // Draggable items
        val chicken = findViewById<ImageView>(R.id.imageChicken)
        val knife = findViewById<ImageView>(R.id.imageKnife)
        val lemon = findViewById<ImageView>(R.id.imageLemon)
        val avocado = findViewById<ImageView>(R.id.imageAvocado)
        val cuttingboard = findViewById<ImageView>(R.id.imageCuttingboard)
        val stove = findViewById<ImageView>(R.id.imageStove)

        // Baskets
        val basketLeft = findViewById<ImageView>(R.id.imageBasketLeft)
        val basketRight = findViewById<ImageView>(R.id.imageBasketRight)

        // Enable drag
        listOf(chicken, knife, lemon, avocado, cuttingboard, stove).forEach {
            enableDrag(it, basketLeft, basketRight)
        }
    }

    private fun startCountdown(seconds: Int) {
        countdownTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                countdownText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                countdownText.text = "00:00"
                Toast.makeText(this@PlayGameActivity, "Game Over!", Toast.LENGTH_SHORT).show()
                // TODO: Add game over logic here
            }
        }.start()
    }

    private fun enableDrag(view: ImageView, basketLeft: View, basketRight: View) {
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
                sendChicken()
            }
            .start()
    }

    private fun sendChicken() {
        val payload = Payload.fromBytes("chicken_sent".toByteArray())
        endpointId?.let {
            connectionsClient.sendPayload(it, payload)
        }
    }

    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            "Phone A",
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        )
    }

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

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            // not used here
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
    }
}
