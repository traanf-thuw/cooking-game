package com.example.cookinggameapp

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class PlayerPositionManager(
    private val context: Context,
    private val db: FirebaseFirestore,
    private val roomCode: String,
    private val currentPlayerId: String,
    private val onSuccess: (
        playerPosition: Int,
        playerIds: List<String>,
        leftNeighborId: String,
        rightNeighborId: String
    ) -> Unit,
    private val onFailure: () -> Unit
) {

    fun initializePlayerPositions() {
        Log.d("DEBUG_TRACE", "🔥 initializePlayerPositions() called")

        db.collection("rooms").document(roomCode).get()
            .addOnSuccessListener { document ->
                try {
                    if (!document.exists()) {
                        Log.e("DEBUG_TRACE", "❌ Room document does not exist!")
                        onFailure()
                        return@addOnSuccessListener
                    }

                    val players = document.get("players") as? List<String> ?: run {
                        Log.e("DEBUG_TRACE", "❌ No players found in room!")
                        onFailure()
                        return@addOnSuccessListener
                    }

                    val playerPosition = players.indexOf(currentPlayerId)
                    if (playerPosition == -1) {
                        Log.e("DEBUG_TRACE", "❌ Current player not found in room!")
                        onFailure()
                        return@addOnSuccessListener
                    }

                    val totalPlayers = players.size
                    val leftNeighborId = players[(playerPosition - 1 + totalPlayers) % totalPlayers]
                    val rightNeighborId = players[(playerPosition + 1) % totalPlayers]

                    Log.d("DEBUG_TRACE", "Player $currentPlayerId at position $playerPosition")
                    Log.d("DEBUG_TRACE", "Left: $leftNeighborId, Right: $rightNeighborId")

                    onSuccess(playerPosition, players, leftNeighborId, rightNeighborId)

                } catch (e: Exception) {
                    Log.e("DEBUG_TRACE", "❌ Error processing player positions", e)
                    onFailure()
                }
            }
            .addOnFailureListener {
                Log.e("DEBUG_TRACE", "❌ Failed to get player positions", it)
                onFailure()
            }
    }
}
