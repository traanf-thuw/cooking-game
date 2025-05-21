package com.example.cookingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RecipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipe)

        // Back button functionality
        findViewById<Button>(R.id.btnBack).setOnClickListener {
//            val intent = Intent() navigate to game playing page
            startActivity(intent)
        }
    }
}