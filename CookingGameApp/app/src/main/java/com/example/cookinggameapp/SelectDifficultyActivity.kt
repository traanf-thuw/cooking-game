package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class SelectDifficultyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_difficulty)

        val easyButton = findViewById<ImageButton>(R.id.easyButton)
        val mediumButton = findViewById<ImageButton>(R.id.mediumButton)
        val hardButton = findViewById<ImageButton>(R.id.hardButton)

        easyButton.setOnClickListener { goToCreateRoom("easy") }
        mediumButton.setOnClickListener { goToCreateRoom("medium") }
        hardButton.setOnClickListener { goToCreateRoom("hard") }
    }

    private fun goToCreateRoom(difficulty: String) {
        val intent = Intent(this, CreateRoomActivity::class.java)
        intent.putExtra("difficulty", difficulty)
        startActivity(intent)
    }
}
