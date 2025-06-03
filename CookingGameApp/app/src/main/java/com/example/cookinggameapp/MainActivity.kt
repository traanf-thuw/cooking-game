package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import android.Manifest


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Create Room button → CreateRoomActivity
        val createRoomButton = findViewById<ImageButton>(R.id.buttonCreateRoom)
        createRoomButton.setOnClickListener {
            val intent = Intent(this, CreateRoomActivity::class.java)
            startActivity(intent)
        }

        // Join button → JoinActivity
        val joinButton = findViewById<ImageButton>(R.id.buttonJoin)
        joinButton.setOnClickListener {
            val intent = Intent(this, JoinRoomActivity::class.java)
            startActivity(intent)
        }

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

        findViewById<ImageButton>(R.id.buttonSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
