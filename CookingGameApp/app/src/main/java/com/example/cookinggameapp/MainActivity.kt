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

        setupAllMenuButtons()

        findViewById<ImageButton>(R.id.buttonSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }

    private fun setupAllMenuButtons() {
        val menuConfigs = listOf(
            R.drawable.createroom to Intent(this, SelectDifficultyActivity::class.java),
            R.drawable.join to Intent(this, JoinRoomActivity::class.java),
            R.drawable.instruction to Intent(this, InstructionActivity::class.java)
        )

        val row = findViewById<LinearLayout>(R.id.buttonRow)

        menuConfigs.forEachIndexed { index, (icon, intent) ->
            val frame = row.getChildAt(index) as FrameLayout
            val button = frame.findViewById<ImageButton>(R.id.menuImageButton)
            button.setImageResource(icon)
            button.setOnClickListener {
                startActivity(intent)
            }
        }
    }
}
