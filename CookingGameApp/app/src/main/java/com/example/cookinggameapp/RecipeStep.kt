package com.example.cookinggameapp

data class RecipeStep(
    val step: String,
    val involves: List<String> // e.g., ["chopping", "cooking"]
)