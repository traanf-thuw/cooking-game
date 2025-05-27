package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class WaitingActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var roomCode: String
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        db = FirebaseFirestore.getInstance()

        roomCode = intent.getStringExtra("roomCode") ?: return
        val roomRef = db.collection("rooms").document(roomCode)

        listener = roomRef.addSnapshotListener { snapshot, _ ->
            val gameStarted = snapshot?.getBoolean("gameStarted") ?: false
            if (gameStarted) {
                listener?.remove()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}
