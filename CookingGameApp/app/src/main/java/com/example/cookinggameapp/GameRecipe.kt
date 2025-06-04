package com.example.cookinggameapp

object GameRecipes {

    val allRecipes = listOf(
        Recipe(
            name = "Chicken Avocado Stir-Fry",
            steps = listOf(
                RecipeStep("Chop chicken and avocado on cutting board", listOf("chopping")),
                RecipeStep("Cook chicken on stove until golden", listOf("cooking")),
                RecipeStep("Add avocado and stir together", listOf("stirring")),
                RecipeStep("Serve hot in basket", listOf("none"))
            )
        ),
        Recipe(
            name = "Lemon Chicken Soup",
            steps = listOf(
                RecipeStep("Chop chicken and lemon slices", listOf("chopping")),
                RecipeStep("Boil chicken in pot for 3 minutes", listOf("cooking")),
                RecipeStep("Add lemon juice and stir slowly", listOf("stirring")),
                RecipeStep("Pour into bowl and serve", listOf("none"))
            )
        ),
        Recipe(
            name = "Avocado Lemon Mash",
            steps = listOf(
                RecipeStep("Chop avocado and lemon pieces", listOf("chopping")),
                RecipeStep("Cook avocado briefly on stove", listOf("cooking")),
                RecipeStep("Mash and stir lemon into mixture", listOf("stirring")),
                RecipeStep("Transfer mash into serving basket", listOf("none"))
            )
        ),
        Recipe(
            name = "Chicken Stew Deluxe",
            steps = listOf(
                RecipeStep("Chop chicken thoroughly", listOf("chopping")),
                RecipeStep("Add chicken to pot and cook", listOf("cooking")),
                RecipeStep("Stir in vegetables and broth", listOf("stirring")),
                RecipeStep("Let simmer before serving", listOf("none"))
            )
        )
    )
}