package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class JoinRoomActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_room) // Create this layout

        db = FirebaseFirestore.getInstance()

        val joinButton = findViewById<ImageButton>(R.id.buttonStart)
        val inputRoomCode = findViewById<EditText>(R.id.editRoomCode)

        joinButton.setOnClickListener {
            val roomCode = inputRoomCode.text.toString().trim().uppercase()

            joinRoom(roomCode)
        }
    }

    private fun joinRoom(roomCode: String) {
        val roomRef = db.collection("rooms").document(roomCode)
        roomRef.get().addOnSuccessListener { doc ->
            val players = doc.get("players") as? List<String> ?: emptyList()
            val assignedSlot = "Player ${players.size + 1}"

            if (players.size < 4) {
                roomRef.update("players", players + assignedSlot)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
