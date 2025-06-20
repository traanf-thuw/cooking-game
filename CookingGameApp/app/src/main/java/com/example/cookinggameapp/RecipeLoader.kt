package com.example.cookinggameapp

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class RecipeLoader(
    private val context: Context,
    private val db: FirebaseFirestore,
    private val roomCode: String,
    private val onLoaded: (Recipe) -> Unit,
    private val onFailure: () -> Unit
) {
    fun load() {
        db.collection("rooms").document(roomCode).get()
            .addOnSuccessListener { document ->
                try {
                    if (!document.exists()) {
                        Log.e("RecipeLoader", "❌ Room document not found")
                        onFailure()
                        return@addOnSuccessListener
                    }

                    val recipeData = document.get("recipe") as? Map<String, Any>
                    if (recipeData != null) {
                        val name = recipeData["name"] as? String ?: "Unnamed Recipe"
                        val stepsData = recipeData["steps"] as? List<Map<String, Any>> ?: emptyList()

                        val steps = stepsData.map {
                            RecipeStep(
                                step = it["step"] as? String ?: "",
                                involves = it["involves"] as? List<String> ?: emptyList()
                            )
                        }

                        val recipe = Recipe(name, steps)
                        Log.d("RecipeLoader", "✅ Loaded recipe: ${recipe.name}")
                        onLoaded(recipe)
                    } else {
                        Log.e("RecipeLoader", "❌ No recipe field in room document")
                        onFailure()
                    }
                } catch (e: Exception) {
                    Log.e("RecipeLoader", "❌ Error loading recipe", e)
                    onFailure()
                }
            }
            .addOnFailureListener {
                Log.e("RecipeLoader", "❌ Firebase failure: $it")
                onFailure()
            }
    }

    fun loadRandom() {
        db.collection("recipes").get()
            .addOnSuccessListener { snapshot ->
                try {
                    val documents = snapshot.documents
                    if (documents.isEmpty()) {
                        Log.e("RecipeLoader", "❌ No recipes available")
                        onFailure()
                        return@addOnSuccessListener
                    }

                    val randomDoc = documents.random()
                    val name = randomDoc.getString("name") ?: "Unnamed Recipe"
                    val stepsData = randomDoc.get("steps") as? List<Map<String, Any>> ?: emptyList()

                    val steps = stepsData.map {
                        RecipeStep(
                            step = it["step"] as? String ?: "",
                            involves = it["involves"] as? List<String> ?: emptyList()
                        )
                    }

                    val recipe = Recipe(name, steps)
                    Log.d("RecipeLoader", "✅ Loaded random recipe: ${recipe.name}")
                    onLoaded(recipe)
                } catch (e: Exception) {
                    Log.e("RecipeLoader", "❌ Error loading random recipe", e)
                    onFailure()
                }
            }
            .addOnFailureListener {
                Log.e("RecipeLoader", "❌ Firebase failure in loadRandom: $it")
                onFailure()
            }
    }
}
