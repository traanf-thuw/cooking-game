package com.example.cookinggameapp

import com.google.firebase.firestore.FirebaseFirestore

object RoomManager {

    fun generateRoomCode(length: Int = 6): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..length).map { chars.random() }.joinToString("")
    }

    fun createRoom(
        db: FirebaseFirestore,
        code: String,
        hostId: String,
        recipe: Recipe,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val roomData = hashMapOf(
            "createdAt" to System.currentTimeMillis(),
            "players" to listOf(hostId),
            "host" to hostId,
            "gameStarted" to false,
            "chickenDropped" to false,
            "currentStepIndex" to 0,
            "recipe" to mapOf(
                "name" to recipe.name,
                "steps" to recipe.steps.map { step ->
                    mapOf(
                        "step" to step.step,
                        "involves" to step.involves
                    )
                }
            )
        )

        db.collection("rooms").document(code)
            .set(roomData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun listenForPlayers(
        db: FirebaseFirestore,
        code: String,
        onPlayersChanged: (List<String>) -> Unit
    ) {
        db.collection("rooms").document(code)
            .addSnapshotListener { snapshot, _ ->
                val players = snapshot?.get("players") as? List<String> ?: return@addSnapshotListener
                onPlayersChanged(players)
            }
    }

    fun joinRoom(
        db: FirebaseFirestore,
        roomCode: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
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

            newPlayerId
        }.addOnSuccessListener { newPlayerId ->
            onSuccess(newPlayerId)
        }.addOnFailureListener { e ->
            onFailure(e)
        }
    }
}
