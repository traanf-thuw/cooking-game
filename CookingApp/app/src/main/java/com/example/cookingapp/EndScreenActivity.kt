package com.example.cookingapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class EndScreenActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.end_screen)

        val btSave = findViewById<Button>(R.id.btnSave)
        val btnHome = findViewById<Button>(R.id.btnHome)

        btSave.setOnClickListener {
            //add save photo logic here
        }

        btnHome.setOnClickListener {
            // go back to game home page
        }
    }
}