package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

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
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        // Instruction button → InstructionActivity
        val instructionButton = findViewById<ImageButton>(R.id.buttonInstruction)
        instructionButton.setOnClickListener {
            val intent = Intent(this, InstructionActivity::class.java)
            startActivity(intent)
        }
    }
}