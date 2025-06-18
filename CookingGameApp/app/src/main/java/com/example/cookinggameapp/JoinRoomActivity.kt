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

        val joinButton = findViewById<ImageButton>(R.id.buttonStart)
        val inputRoomCode = findViewById<EditText>(R.id.editRoomCode)

        joinButton.setOnClickListener {
            val roomCode = inputRoomCode.text.toString().trim().uppercase()
            joinRoom(roomCode)
        }
    }

    private fun joinRoom(roomCode: String) {
        val roomRef = db.collection("rooms").document(roomCode)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(roomRef)

            if (!snapshot.exists()) {
                throw Exception("Room not found")
            }

            val players = snapshot.get("players") as? List<String> ?: emptyList()

            if (players.size >= 4) {
                throw Exception("Room is full")
            }

            val newPlayerId = "Player ${players.size + 1}"
            val updatedPlayers = players + newPlayerId
            transaction.update(roomRef, "players", updatedPlayers)

            newPlayerId // Return the new player ID for success callback
        }.addOnSuccessListener { newPlayerId ->
            currentPlayerId = newPlayerId
            val intent = Intent(this, WaitingActivity::class.java)
            intent.putExtra("roomCode", roomCode)
            intent.putExtra("playerId", currentPlayerId)
            intent.putExtra("isHost", false)
            startActivity(intent)
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(this, e.message ?: "Error joining room", Toast.LENGTH_SHORT).show()
        }
    }
}
