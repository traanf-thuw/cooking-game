package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InstructionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction)

        findViewById<TextView>(R.id.instructionTitle).text = "How to Play?"
        findViewById<TextView>(R.id.instructionSteps).text = """
            1. Host player creates a room
            2. Other players enter the game code
            3. Host player starts the game
            4. Play together and have fun!
        """.trimIndent()

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
