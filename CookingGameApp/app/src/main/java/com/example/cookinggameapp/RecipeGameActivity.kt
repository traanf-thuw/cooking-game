package com.example.cookinggameapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView

class RecipeGameActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_game)

        val recipeName = intent.getStringExtra("recipe_name") ?: "Unknown Recipe"
        val recipeSteps = intent.getStringArrayListExtra("recipe_steps") ?: arrayListOf()

        // Update instruction block
        findViewById<TextView>(R.id.instructionTitle).text = recipeName
        findViewById<TextView>(R.id.instructionSteps).text = recipeSteps.mapIndexed { index, step ->
            "${index + 1}. $step"
        }.joinToString("\n")

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
}
