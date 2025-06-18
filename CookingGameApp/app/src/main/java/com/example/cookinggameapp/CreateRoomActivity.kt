package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class CreateRoomActivity : BaseActivity() {

    private lateinit var roomCode: String
    private lateinit var db: FirebaseFirestore
    private lateinit var selectedDifficulty: String
    private lateinit var currentPlayerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hostplayerpage)

        currentPlayerId = intent.getStringExtra("playerId") ?: "Player 1"
        db = FirebaseFirestore.getInstance()

        // Get difficulty from previous screen
        selectedDifficulty = intent.getStringExtra("difficulty") ?: "easy"

        // 1. Generate unique room code
        roomCode = generateRoomCode()
        createRoomInFirestore(roomCode)

        // 2. Listen for joined players
        listenForPlayerUpdates(roomCode)

        // 3. Start game when host clicks start
        findViewById<ImageButton>(R.id.buttonStart).setOnClickListener {
            startGame()
        }
    }

    private fun generateRoomCode(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..6).map { chars.random() }.joinToString("")
    }

    private fun createRoomInFirestore(code: String) {
        val roomData = hashMapOf(
            "createdAt" to System.currentTimeMillis(),
            "players" to listOf(currentPlayerId),  // add host playerId here
            "host" to currentPlayerId,             // store host id explicitly
            "gameStarted" to false,
            "chickenDropped" to false
        )

        db.collection("rooms").document(code)
            .set(roomData)
            .addOnSuccessListener {
                findViewById<TextView>(R.id.textRoomCode).text = code
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "❌ Failed to create room", e)
            }
    }

//    private fun generatePlayerId(): String {
//        // Generate a unique ID for this player
//        return "Player_${(1000..9999).random()}"
//    }

    private fun listenForPlayerUpdates(code: String) {
        db.collection("rooms").document(code)
            .addSnapshotListener { snapshot, _ ->
                val players = snapshot?.get("players") as? List<*> ?: return@addSnapshotListener
                updatePlayersUI(players)
            }
    }

    private fun updatePlayersUI(players: List<*>) {
        findViewById<TextView>(R.id.textPlayersJoined).text = "${players.size} players joined"

        val playerViews = listOf(
            findViewById<TextView>(R.id.player1Button),
            findViewById<TextView>(R.id.player2Button),
            findViewById<TextView>(R.id.player3Button),
            findViewById<TextView>(R.id.player4Button)
        )

        playerViews.forEachIndexed { index, view ->
            if (index < players.size) {
                view.text = players[index].toString()
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.INVISIBLE
            }
        }
    }

    private fun startGame() {
        val ref = db.collection("rooms").document(roomCode)
        val startTime = System.currentTimeMillis()

        ref.update(
            mapOf(
                "gameStarted" to true,
                "start_time" to startTime,
                "difficulty" to selectedDifficulty
            )
        ).addOnSuccessListener {
            val intent = Intent(this, PlayGameActivity::class.java)
            intent.putExtra("roomCode", roomCode)
            intent.putExtra("isHost", true)
            intent.putExtra("playerId", currentPlayerId)
            intent.putExtra("difficulty", selectedDifficulty)
            startActivity(intent)
            finish()
        }.addOnFailureListener {
            Log.e("CreateRoom", " Failed to start game", it)
        }
    }

}