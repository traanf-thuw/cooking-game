package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import android.Manifest


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val buttonNavigationMap = mapOf(
            R.id.buttonCreateRoom to SelectDifficultyActivity::class.java,
            R.id.buttonJoin to JoinRoomActivity::class.java,
            R.id.buttonInstruction to InstructionActivity::class.java,
            R.id.buttonSettings to SettingsActivity::class.java
        )

        buttonNavigationMap.forEach { (buttonId, activityClass) ->
            findViewById<ImageButton>(buttonId).setOnClickListener {
                startActivity(Intent(this, activityClass))
            }
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }
}
