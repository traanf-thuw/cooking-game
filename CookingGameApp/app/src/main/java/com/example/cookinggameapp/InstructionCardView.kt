package com.example.cookinggameapp.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.cookinggameapp.R

class InstructionCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val labelImage: ImageView
    private val contentLayout: LinearLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.instruction_card, this, true)
        labelImage = findViewById(R.id.cardLabel)
        contentLayout = findViewById(R.id.cardContent)
    }

    fun setupCard(labelRes: Int, steps: List<Pair<Int, String>>) {
        labelImage.setImageResource(labelRes)

        val imagesLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 2f
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val textsLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        for ((index, pair) in steps.withIndex()) {
            val (imageRes, labelText) = pair

            val image = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 140.dp, 1f)
                setImageResource(imageRes)
                contentDescription = labelText
            }

            val text = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = labelText
                textSize = 14f
                setTextColor(0xFF000000.toInt())
                gravity = android.view.Gravity.CENTER
                typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.baloo)
            }

            imagesLayout.addView(image)
            textsLayout.addView(text)

            if (index < steps.lastIndex) {
                imagesLayout.addView(spaceBetweenSteps())
                textsLayout.addView(spaceBetweenSteps())
            }
        }

        contentLayout.removeAllViews()
        contentLayout.addView(imagesLayout)
        contentLayout.addView(textsLayout)
    }

    private fun spaceBetweenSteps(): View {
        return Space(context).apply {
            layoutParams = LinearLayout.LayoutParams(24.dp, LinearLayout.LayoutParams.MATCH_PARENT)
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
