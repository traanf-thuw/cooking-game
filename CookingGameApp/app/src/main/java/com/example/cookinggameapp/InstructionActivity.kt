package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cookinggameapp.view.InstructionCardView

class InstructionActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction)

        // Set "How to Play?" content
        findViewById<TextView>(R.id.instructionTitle).text = "How to Play?"
        findViewById<TextView>(R.id.instructionSteps).text = """
            1. Host player creates a room
            2. Other players enter the game code
            3. Host player starts the game
            4. Play together and have fun!
        """.trimIndent()

        // Back button functionality
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val container = findViewById<LinearLayout>(R.id.instructionCardContainer)

        // Shared layout params for centering each card
        val centeredParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Cook card
        val cookCard = InstructionCardView(this)
        cookCard.layoutParams = centeredParams
        cookCard.setupCard(
            R.drawable.how_to_cook,
            listOf(
                R.drawable.stove to "Step 1: Drag the raw chicken onto the stove.",
                R.drawable.uncooked_chicken to "Step 2: Drag the dot to the right.",
                R.drawable.cooked_chicken to "Step 3: Done! The chicken is now cooked."
            )
        )
        container.addView(cookCard)

        // Chop card
        val chopCard = InstructionCardView(this)
        chopCard.layoutParams = centeredParams
        chopCard.setupCard(
            R.drawable.how_to_chop,
            listOf(
                R.drawable.cuttingboard to "Step 1: Find the chopping board.",
                R.drawable.step2_chop to "Step 2: Place the chicken on the board.",
                R.drawable.step3_chop to "Step 3: Place the knife on the top of the chicken and tap it.",
                R.drawable.step4_chop to "Step 4: The chicken transformed into fillet slices."
            )
        )
        container.addView(chopCard)

        // Stir card
        val stirCard = InstructionCardView(this)
        stirCard.layoutParams = centeredParams
        stirCard.setupCard(
            R.drawable.how_to_stir,
            listOf(
                R.drawable.pot to "Step 1: Place the pot",
                R.drawable.step2_stir to "Step 2: Add vegetables or food into the pot.",
                R.drawable.step3_stir to "Step 3: Stir the pot",
                R.drawable.step4_stir to "Step 4: The food is ready."
            )
        )
        container.addView(stirCard)
    }
}
