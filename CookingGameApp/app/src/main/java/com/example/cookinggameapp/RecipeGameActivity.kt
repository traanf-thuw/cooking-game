package com.example.cookinggameapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_game)

        findViewById<TextView>(R.id.instructionTitle).text = "Boiled Chicken"
        findViewById<TextView>(R.id.instructionSteps).text = """
            1. Chop the chicken
            2. Put it in the pot
            3. Boil it
            4. Put a little salt
        """.trimIndent()

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
}
