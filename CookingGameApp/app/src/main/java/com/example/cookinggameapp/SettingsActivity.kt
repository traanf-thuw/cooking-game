package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.cookinggameapp.MusicLibrary.trackMap

class SettingsActivity : BaseActivity() {

    private lateinit var volumeSeekBar: SeekBar
    private lateinit var musicSpinner: Spinner
    private lateinit var musicNames: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        musicSpinner = findViewById(R.id.musicSpinner)

        musicNames = trackMap.keys.toList()

        initVolumeControl()
        initMusicSpinner()

        // handle Add Recipe button click
        val addRecipeBtn: View = findViewById(R.id.addRecipeButton) // or R.id.addRecipeContainer
        addRecipeBtn.setOnClickListener {
            val intent = Intent(this, CreateRecipeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initVolumeControl() {
        val savedVolume = MusicPreferences.getVolumeRaw(this)
        volumeSeekBar.progress = savedVolume

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                MusicPreferences.setVolumeRaw(this@SettingsActivity, progress)
                MusicManager.setVolume(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initMusicSpinner() {
        val savedMusic = MusicPreferences.getSelectedTrack(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, musicNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        musicSpinner.adapter = adapter
        musicSpinner.setSelection(musicNames.indexOf(savedMusic))

        musicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val selectedTrack = musicNames[position]
                MusicPreferences.setSelectedTrack(this@SettingsActivity, selectedTrack)

                trackMap[selectedTrack]?.let { resId ->
                    MusicManager.start(applicationContext, resId)
                    MusicManager.setVolume(volumeSeekBar.progress / 100f)
                }

                Toast.makeText(applicationContext, "Playing: $selectedTrack", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
