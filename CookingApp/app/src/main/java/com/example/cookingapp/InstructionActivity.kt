package com.example.cookingapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class InstructionActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instruction)

        val btBack = findViewById<Button>(R.id.btnBack)

        btBack.setOnClickListener {
            // back to another screen
        }
    }
}