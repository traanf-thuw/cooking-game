package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton

class SelectDifficultyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_difficulty)

        findViewById<ImageButton>(R.id.easyButton).setOnClickListener {
            startCreateRoomActivity(60)
        }

        findViewById<ImageButton>(R.id.mediumButton).setOnClickListener {
            startCreateRoomActivity(40)
        }

        findViewById<ImageButton>(R.id.hardButton).setOnClickListener {
            startCreateRoomActivity(30)
        }
    }

    private fun startCreateRoomActivity(seconds: Int) {
        val intent = Intent(this, CreateRoomActivity::class.java)
        intent.putExtra("timeLimit", seconds)
        startActivity(intent)
    }
}
