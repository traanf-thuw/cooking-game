package com.example.cookingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class CodeHostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.code_host)

        // Button functionality
        findViewById<Button>(R.id.btnStatus).setOnClickListener {
            val intent = Intent(this,JoinStatusHostActivity::class.java)
            startActivity(intent)
        }
    }
}