// MenuActivity.kt
package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Set up button actions
        findViewById<ImageButton>(R.id.buttonCreateRoom).setOnClickListener {
            startActivity(Intent(this, CreateRoomActivity::class.java))
        }

        findViewById<ImageButton>(R.id.buttonJoin).setOnClickListener {
            startActivity(Intent(this, JoinRoomActivity::class.java))
        }

        findViewById<ImageButton>(R.id.buttonInstruction).setOnClickListener {
            startActivity(Intent(this, InstructionActivity::class.java))
        }

        findViewById<ImageButton>(R.id.buttonSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
