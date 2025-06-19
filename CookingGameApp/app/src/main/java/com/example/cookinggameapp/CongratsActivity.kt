package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CongratsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_congrats)

        applySystemInsets(R.id.backButton)

        initializeButtons()
    }

    private fun initializeButtons() {
        val buttonNavigationMap = mapOf(
            R.id.backButton to MainActivity::class.java,
            R.id.takeSelfieBtn to EndscreenActivity::class.java
        )

        buttonNavigationMap.forEach { (buttonId, activityClass) ->
            findViewById<ImageButton>(buttonId).setOnClickListener {
                startActivity(Intent(this, activityClass))
            }
        }
    }

    private fun applySystemInsets(viewId: Int) {
        val view = findViewById<ImageButton>(viewId)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
