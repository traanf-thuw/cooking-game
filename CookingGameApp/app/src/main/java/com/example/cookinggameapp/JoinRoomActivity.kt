package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class JoinRoomActivity : BaseActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var currentPlayerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_room)

        db = FirebaseFirestore.getInstance()
        currentPlayerId = intent.getStringExtra("playerId") ?: "ddbie"

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
            if (doc.exists()) {
                val players = doc.get("players") as? List<String> ?: emptyList()

                if (players.size < 4) {
                    // Add this player to the room
                    roomRef.update("players", players + currentPlayerId)

                    val intent = Intent(this, WaitingActivity::class.java)
                    intent.putExtra("roomCode", roomCode)
                    intent.putExtra("playerId", currentPlayerId)
                    intent.putExtra("isHost", false)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Room is full", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Room not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error joining room", Toast.LENGTH_SHORT).show()
        }
    }
}
