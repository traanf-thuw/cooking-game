package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.Manifest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Create Room button → SelectDifficultyActivity
        val createRoomButton = findViewById<ImageButton>(R.id.buttonCreateRoom)
        createRoomButton.setOnClickListener {
            val intent = Intent(this, SelectDifficultyActivity::class.java)
            startActivity(intent)
        }

        // Join button → JoinRoomActivity
        val joinButton = findViewById<ImageButton>(R.id.buttonJoin)
        joinButton.setOnClickListener {
            val intent = Intent(this, JoinRoomActivity::class.java)
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
}
