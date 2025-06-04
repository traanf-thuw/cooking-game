package com.example.cookinggameapp

import android.os.Bundle
import android.view.View
import android.widget.*

class SettingsActivity : BaseActivity() {

    private lateinit var volumeSeekBar: SeekBar
    private lateinit var musicSpinner: Spinner

    private val musicTracks = mapOf(
        "chill-lofi-background-music-333347" to R.raw.track1,
        "sakura-lofi-ambient-lofi-music-340018" to R.raw.track2,
        "soulful-river-folk-tune-with-bamboo-flute-339826" to R.raw.track3
    )

    private val PREFS_NAME = "AppSettingsPrefs"
    private val KEY_VOLUME = "volume_level"
    private val KEY_MUSIC = "music_selection"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        musicSpinner = findViewById(R.id.musicSpinner)

        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        val savedVolume = sharedPrefs.getInt(KEY_VOLUME, 50)
        val savedMusic = sharedPrefs.getString(KEY_MUSIC, "chill-lofi-background-music-333347") ?: "chill-lofi-background-music-333347"

        volumeSeekBar.progress = savedVolume

        val musicNames = musicTracks.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, musicNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        musicSpinner.adapter = adapter
        musicSpinner.setSelection(musicNames.indexOf(savedMusic))

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sharedPrefs.edit().putInt(KEY_VOLUME, progress).apply()
                MusicManager.setVolume(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        musicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selectedTrack = musicNames[position]
                sharedPrefs.edit().putString(KEY_MUSIC, selectedTrack).apply()

                val resId = musicTracks[selectedTrack]
                if (resId != null) {
                    MusicManager.start(applicationContext, resId)
                    MusicManager.setVolume(volumeSeekBar.progress / 100f)
                }

                Toast.makeText(applicationContext, "Playing: $selectedTrack", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
