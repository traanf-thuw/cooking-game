package com.example.cookinggameapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
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
        selectedDifficulty = intent.getStringExtra("difficulty") ?: "easy"
        db = FirebaseFirestore.getInstance()

        roomCode = RoomManager.generateRoomCode()
        val recipe = GameRecipes.allRecipes.random()

        RoomManager.createRoom(
            db,
            roomCode,
            currentPlayerId,
            recipe,
            onSuccess = { findViewById<TextView>(R.id.textRoomCode).text = roomCode },
            onFailure = { Log.e("Firestore", "❌ Failed to create room", it) }
        )

        RoomManager.listenForPlayers(db, roomCode) { players ->
            updatePlayersUI(players)
        }

        findViewById<ImageButton>(R.id.buttonStart).setOnClickListener {
            startGame()
        }
    }

    private fun updatePlayersUI(players: List<String>) {
        findViewById<TextView>(R.id.textPlayersJoined).text = "${players.size} players joined"

        val playerViews = listOf(
            R.id.player1Button, R.id.player2Button, R.id.player3Button, R.id.player4Button
        ).map { findViewById<TextView>(it) }

        playerViews.forEachIndexed { index, view ->
            if (index < players.size) {
                view.text = players[index]
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.INVISIBLE
            }
        }
    }

    private fun startGame() {
        db.collection("rooms").document(roomCode).update(
            mapOf(
                "gameStarted" to true,
                "start_time" to System.currentTimeMillis(),
                "difficulty" to selectedDifficulty
            )
        ).addOnSuccessListener {
            startActivity(Intent(this, PlayGameActivity::class.java).apply {
                putExtra("roomCode", roomCode)
                putExtra("isHost", true)
                putExtra("playerId", currentPlayerId)
                putExtra("difficulty", selectedDifficulty)
            })
            finish()
        }.addOnFailureListener {
            Log.e("CreateRoom", "Failed to start game", it)
        }
    }
}
