package com.example.cookingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class JoinStatusHostActivity : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.join_status_host)

        // Button functionality
        findViewById<Button>(R.id.btnRoomCode).setOnClickListener {
            val intent = Intent(this, CodeHostActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnStartGame).setOnClickListener {
//            val intent = Intent(this, ) navigate the game playing page
            startActivity(intent)
        }
    }
}