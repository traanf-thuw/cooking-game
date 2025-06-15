package com.example.cookinggameapp

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val buttonRow = findViewById<LinearLayout>(R.id.buttonRow)

        // Get each reused menuImageButton and assign actions
        setupMenuButton(
            buttonRow.getChildAt(0) as FrameLayout,
            R.drawable.createroom
        ) {
            startActivity(Intent(this, SelectDifficultyActivity::class.java))
        }

        setupMenuButton(
            buttonRow.getChildAt(1) as FrameLayout,
            R.drawable.join
        ) {
            startActivity(Intent(this, JoinRoomActivity::class.java))
        }

        setupMenuButton(
            buttonRow.getChildAt(2) as FrameLayout,
            R.drawable.instruction
        ) {
            startActivity(Intent(this, InstructionActivity::class.java))
        }

        findViewById<ImageButton>(R.id.buttonSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }

    private fun setupMenuButton(container: FrameLayout, iconRes: Int, onClick: () -> Unit) {
        val button = container.findViewById<ImageButton>(R.id.menuImageButton)
        button.setImageResource(iconRes)
        button.setOnClickListener { onClick() }
    }
}
