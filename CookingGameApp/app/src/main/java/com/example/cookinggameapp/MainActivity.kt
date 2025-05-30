package com.example.cookinggameapp

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val createRoomButton = findViewById<ImageButton>(R.id.buttonCreateRoom)
        createRoomButton.setOnClickListener {
            val intent = Intent(this, SelectDifficultyActivity::class.java)
            startActivity(intent)
        }

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
