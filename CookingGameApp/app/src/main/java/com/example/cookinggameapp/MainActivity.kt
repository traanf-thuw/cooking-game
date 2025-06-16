package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import android.Manifest


class MainActivity : BaseActivity() {

    private lateinit var currentPlayerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // ✅ Generate player ID once per app launch
        currentPlayerId = generatePlayerId()

        val createRoomButton = findViewById<ImageButton>(R.id.buttonCreateRoom)
        createRoomButton.setOnClickListener {
            val intent = Intent(this, SelectDifficultyActivity::class.java)
            intent.putExtra("playerId", currentPlayerId) // ✅ pass it to next screen
            startActivity(intent)
        }

        val joinButton = findViewById<ImageButton>(R.id.buttonJoin)
        joinButton.setOnClickListener {
            val intent = Intent(this, JoinRoomActivity::class.java)
            intent.putExtra("playerId", currentPlayerId) // ✅ fixed
            startActivity(intent)
        }

        // Request location permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )

        // Instruction button → InstructionActivity
        val instructionButton = findViewById<ImageButton>(R.id.buttonInstruction)
        instructionButton.setOnClickListener {
            val intent = Intent(this, InstructionActivity::class.java)
            startActivity(intent)
        }

        // Settings button → SettingsActivity
        findViewById<ImageButton>(R.id.buttonSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun generatePlayerId(): String {
        return "Player_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}
