package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class CreateRoomActivity : AppCompatActivity() {

    private lateinit var roomCode: String
    private lateinit var db: FirebaseFirestore
    private var timeLimit: Int = 60 // Default value if not passed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hostplayerpage)

        // Required to receive insets if you're using edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.imageBackground)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Receive selected difficulty time limit from SelectDifficultyActivity
        timeLimit = intent.getIntExtra("timeLimit", 60)

        db = FirebaseFirestore.getInstance()

        // 1. Generate Room Code and Create Room
        roomCode = generateRoomCode()
        createRoomInFirestore(roomCode)

        // 2. Start listening for player joins
        listenForPlayerUpdates(roomCode)

        // 3. Start the game when all players have joined
        val joinButton = findViewById<ImageButton>(R.id.buttonStart)
        joinButton.setOnClickListener {
            this.startGame(roomCode)
        }
    }

    private fun generateRoomCode(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..6).map { chars.random() }.joinToString("")
    }

    private fun createRoomInFirestore(code: String) {
        val roomData = hashMapOf(
            "createdAt" to System.currentTimeMillis(),
            "players" to listOf("Player 1"),
            "gameStarted" to false
        )

        db.collection("rooms").document(code)
            .set(roomData)
            .addOnSuccessListener {
                findViewById<TextView>(R.id.textRoomCode).text = code
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error creating room", e)
            }
    }

    private fun listenForPlayerUpdates(roomCode: String) {
        db.collection("rooms").document(roomCode)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val players = snapshot.get("players") as? List<*> ?: emptyList<String>()
                    updatePlayersUI(players)
                }
            }
    }

    private fun updatePlayersUI(players: List<*>) {
        val playersJoinedText = findViewById<TextView>(R.id.textPlayersJoined)
        val playerButtons = listOf(
            findViewById<TextView>(R.id.player1Button),
            findViewById<TextView>(R.id.player2Button),
            findViewById<TextView>(R.id.player3Button),
            findViewById<TextView>(R.id.player4Button)
        )

        playersJoinedText.text = "${players.size} players\n  joined"

        playerButtons.forEachIndexed { index, button ->
            if (index < players.size) {
                button.text = players[index].toString()
                button.visibility = View.VISIBLE
            } else {
                button.visibility = View.INVISIBLE
            }
        }
    }

    private fun startGame(roomCode: String) {
        val roomRef = db.collection("rooms").document(roomCode)
        roomRef.update("gameStarted", true).addOnSuccessListener {
            val intent = Intent(this, PlayGameActivity::class.java)
            intent.putExtra("roomCode", roomCode)
            intent.putExtra("timeLimit", timeLimit) // Pass the selected difficulty
            startActivity(intent)
            finish()
        }
    }
}
