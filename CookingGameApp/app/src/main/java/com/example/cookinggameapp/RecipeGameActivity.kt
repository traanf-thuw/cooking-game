package com.example.cookinggameapp

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class RecipeGameActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_game)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        loadRecipeFromIntent()
    }

    private fun loadRecipeFromIntent() {
        val recipeName = intent.getStringExtra("recipe_name")
        val recipeSteps = intent.getStringArrayListExtra("recipe_steps")

        if (recipeName == null || recipeSteps == null) {
            Log.e("RecipeGameActivity", "Missing intent data")
            Toast.makeText(this, "No recipe data provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("RecipeGameActivity", "Recipe name: $recipeName")
        Log.d("RecipeGameActivity", "Recipe steps: $recipeSteps")

        val recipe = Recipe(
            name = recipeName,
            steps = recipeSteps.map { RecipeStep(step = it, involves = emptyList()) }
        )

        displayRecipe(recipe)
    }

    private fun displayRecipe(recipe: Recipe) {
        val titleText = findViewById<TextView>(R.id.instructionTitle)
        val stepsText = findViewById<TextView>(R.id.instructionSteps)

        titleText.text = recipe.name
        stepsText.text = recipe.steps.mapIndexed { index, step ->
            "${index + 1}. ${step.step}"
        }.joinToString("\n")
    }
}
