package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class WaitingActivity : BaseActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var roomCode: String
    private var listener: ListenerRegistration? = null
    private var isHost: Boolean = false
    private lateinit var currentPlayerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        db = FirebaseFirestore.getInstance()

        roomCode = intent.getStringExtra("roomCode") ?: return
        isHost = intent.getBooleanExtra("isHost", false)
        currentPlayerId = intent.getStringExtra("playerId") ?: "Unknown"

        listenForGameStart()
    }

    private fun listenForGameStart() {
        val roomRef = db.collection("rooms").document(roomCode)
        listener = roomRef.addSnapshotListener { snapshot, _ ->
            val gameStarted = snapshot?.getBoolean("gameStarted") ?: false
            if (gameStarted) {
                listener?.remove()
                startGame()
            }
        }
    }

    private fun startGame() {
        val intent = Intent(this, PlayGameActivity::class.java).apply {
            putExtra("roomCode", roomCode)
            putExtra("isHost", isHost)
            putExtra("playerId", currentPlayerId)
        }
        startActivity(intent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        listener?.remove()
        listener = null
    }
}
