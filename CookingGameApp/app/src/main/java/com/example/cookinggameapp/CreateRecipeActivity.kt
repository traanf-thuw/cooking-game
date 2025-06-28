package com.example.cookinggameapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class CreateRecipeActivity : AppCompatActivity() {

    private lateinit var recipeNameEditText: EditText
    private lateinit var involvesSpinners: List<Spinner>
    private lateinit var stepEditTexts: List<EditText>
    private lateinit var saveRecipeButton: Button

    private val involvesOptions = listOf("Select step type...", "chopping", "cooking", "stirring", "none")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_recipe)

        recipeNameEditText = findViewById(R.id.editRecipeName)
        saveRecipeButton = findViewById(R.id.saveRecipeButton)

        involvesSpinners = listOf(
            findViewById(R.id.spinnerStep1Involves),
            findViewById(R.id.spinnerStep2Involves),
            findViewById(R.id.spinnerStep3Involves),
            findViewById(R.id.spinnerStep4Involves)
        )

        stepEditTexts = listOf(
            findViewById(R.id.editStep1Text),
            findViewById(R.id.editStep2Text),
            findViewById(R.id.editStep3Text),
            findViewById(R.id.editStep4Text)
        )

        // Set up spinners
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, involvesOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        involvesSpinners.forEach { it.adapter = adapter }

        saveRecipeButton.setOnClickListener {
            saveRecipeToFirestore()
        }
    }

    private fun saveRecipeToFirestore() {
        val recipeName = recipeNameEditText.text.toString().trim()
        if (recipeName.isEmpty()) {
            Toast.makeText(this, "Please enter a recipe name", Toast.LENGTH_SHORT).show()
            return
        }

        val steps = mutableListOf<Map<String, Any>>()
        for (i in 0 until 4) {
            val involves = involvesSpinners[i].selectedItem.toString()
            val stepText = stepEditTexts[i].text.toString().trim()

            if (stepText.isEmpty()) {
                Toast.makeText(this, "Please fill in all step descriptions", Toast.LENGTH_SHORT).show()
                return
            }

            val stepData = mapOf(
                "involves" to involves,
                "step" to stepText
            )
            steps.add(stepData)
        }

        val recipeData = mapOf(
            "name" to recipeName,
            "steps" to steps
        )

        FirebaseFirestore.getInstance().collection("recipes")
            .document(recipeName) // use recipe name as the document ID
            .set(recipeData)
            .addOnSuccessListener {
                Toast.makeText(this, "Recipe saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save recipe: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
