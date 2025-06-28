package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import android.Manifest
import androidx.appcompat.widget.SwitchCompat


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val musicSwitch = findViewById<SwitchCompat>(R.id.switchMusic)
        val menuMusicResId = R.raw.track1

        // Restore saved preference
        val isMusicOn = MusicPreferences.isMusicEnabled(this)
        musicSwitch.isChecked = isMusicOn

        if (isMusicOn) {
            MusicManager.start(this, menuMusicResId)
        }

        musicSwitch.setOnCheckedChangeListener { _, isChecked ->
            MusicPreferences.setMusicEnabled(this, isChecked)
            if (isChecked) {
                MusicManager.start(this, menuMusicResId)
            } else {
                MusicManager.pause()
            }
        }

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
