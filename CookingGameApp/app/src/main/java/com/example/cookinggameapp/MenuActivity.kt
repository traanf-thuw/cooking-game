// MenuActivity.kt
package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu) // Assuming the layout filename is activity_menu.xml

        // Set up button actions
        findViewById<ImageButton>(R.id.buttonCreateRoom).setOnClickListener {
            // Launch your PlayGame activity or room creation
            startActivity(Intent(this, CreateRoomActivity::class.java))
        }

        findViewById<ImageButton>(R.id.buttonJoin).setOnClickListener {
            // Handle join logic or navigate
            startActivity(Intent(this, JoinRoomActivity::class.java))
        }

        findViewById<ImageButton>(R.id.buttonInstruction).setOnClickListener {
            // Show instructions
        }
    }
}
