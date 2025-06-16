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

        // 🔗 Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // 🔐 Get room code and host info from previous screen
        roomCode = intent.getStringExtra("roomCode") ?: return
        isHost = intent.getBooleanExtra("isHost", false)
        currentPlayerId = intent.getStringExtra("playerId") ?: "Unknown"

        // 👂 Start listening to game start signal from Firebase
        val roomRef = db.collection("rooms").document(roomCode)
        listener = roomRef.addSnapshotListener { snapshot, _ ->
            val gameStarted = snapshot?.getBoolean("gameStarted") ?: false

            if (gameStarted) {
                listener?.remove()

                // 🚀 Launch game and pass room + host info
                val intent = Intent(this, PlayGameActivity::class.java)
                intent.putExtra("roomCode", roomCode)
                intent.putExtra("isHost", isHost)
                intent.putExtra("playerId", currentPlayerId)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 🧹 Remove listener to avoid memory leaks
        listener?.remove()
    }
}
