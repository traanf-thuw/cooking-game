package com.example.cookinggameapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class EndscreenActivity :AppCompatActivity () {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_endscreen)

        // Button: Save
        val btnSave = findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            // TODO: Implement save functionality
        }

        // Button: Home
        val btnHome = findViewById<Button>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        findViewById<ImageButton>(R.id.btnShareWhatsapp).setOnClickListener {
            openUrl("https://wa.me/")
        }

        findViewById<ImageButton>(R.id.btnShareInstagram).setOnClickListener {
            openUrl("https://www.instagram.com/")
        }

        findViewById<ImageButton>(R.id.btnShareTwitter).setOnClickListener {
            openUrl("https://x.com/home")
        }

        findViewById<ImageButton>(R.id.btnShareFacebook).setOnClickListener {
            openUrl("https://www.facebook.com/")
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}