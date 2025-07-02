package com.example.cookinggameapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore

class DeleteRecipeActivity : BaseActivity() {

    private lateinit var recipeListView: ListView
    private lateinit var recipeNames: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_recipe)

        recipeListView = findViewById(R.id.recipeListView)
        recipeNames = mutableListOf()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, recipeNames)
        recipeListView.adapter = adapter

        fetchRecipes()

        recipeListView.setOnItemClickListener { _, _, position, _ ->
            val selectedRecipe = recipeNames[position]
            showDeleteDialog(selectedRecipe)
        }
    }

    private fun fetchRecipes() {
        db.collection("recipes").get().addOnSuccessListener { snapshot ->
            recipeNames.clear()
            for (doc in snapshot) {
                val name = doc.getString("name") ?: continue
                recipeNames.add(name)
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch recipes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteDialog(recipeName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Recipe")
            .setMessage("Are you sure you want to delete \"$recipeName\"?")
            .setPositiveButton("Delete") { _, _ ->
                deleteRecipe(recipeName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteRecipe(recipeName: String) {
        db.collection("recipes")
            .whereEqualTo("name", recipeName)
            .get()
            .addOnSuccessListener { docs ->
                for (doc in docs) {
                    db.collection("recipes").document(doc.id).delete()
                }
                Toast.makeText(this, "Recipe deleted", Toast.LENGTH_SHORT).show()
                fetchRecipes()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete recipe", Toast.LENGTH_SHORT).show()
            }
    }
}
