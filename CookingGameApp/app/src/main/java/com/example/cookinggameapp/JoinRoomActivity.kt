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

        val inputRoomCode = findViewById<EditText>(R.id.editRoomCode)
        findViewById<ImageButton>(R.id.buttonStart).setOnClickListener {
            val roomCode = inputRoomCode.text.toString().trim().uppercase()
            joinRoom(roomCode)
        }
    }

    private fun joinRoom(roomCode: String) {
        RoomManager.joinRoom(db, roomCode,
            onSuccess = { newPlayerId ->
                currentPlayerId = newPlayerId
                val intent = Intent(this, WaitingActivity::class.java).apply {
                    putExtra("roomCode", roomCode)
                    putExtra("playerId", currentPlayerId)
                    putExtra("isHost", false)
                }
                startActivity(intent)
                finish()
            },
            onFailure = { e ->
                Toast.makeText(this, e.message ?: "Error joining room", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
