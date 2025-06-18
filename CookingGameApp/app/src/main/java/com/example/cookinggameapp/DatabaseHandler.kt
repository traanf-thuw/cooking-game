package com.example.cookinggameapp

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class DatabaseHandler {

    private val db = FirebaseFirestore.getInstance()

    fun roomCreate(roomCode: String, difficulty: String, hostId: String) {
        val roomData = mapOf(
            "room_id" to roomCode,
            "created" to System.currentTimeMillis(),
            "difficulty" to difficulty,
            "startTime" to null,
            "players" to mapOf(hostId to emptyList<String>()),
            "recipe_id" to null,
            "step_id" to null
        )

        db.collection("rooms").document(roomCode).set(roomData)
    }

    fun addPlayerToRoom(roomCode: String, joinId: String) {
        db.collection("rooms")
            .document(roomCode)
            .update("players.$joinId", emptyList<String>())
    }

    fun assignItemsAndInitialize(roomCode: String, items: List<String>) {
        val roomRef = db.collection("rooms").document(roomCode)

        getPlayersMap(roomCode) { updatedPlayers ->
            val playerIds = updatedPlayers.keys.toList()

            val cookwareItems = items.filter { it.startsWith("c") }.toMutableList()
            val ingredientItems = items.filter { it.startsWith("i") }.toMutableList()

            updatedPlayers[playerIds[0]]?.add("c00001")

            if (playerIds.size == 1) {
                updatedPlayers[playerIds[0]]?.addAll(cookwareItems + ingredientItems)
            } else {
                var index = 1
                for (item in cookwareItems) {
                    val targetIndex = if (index % playerIds.size == 0) 1 else index % playerIds.size
                    updatedPlayers[playerIds[targetIndex]]?.add(item)
                    index++
                }

                val random = java.util.Random()
                for (ingredient in ingredientItems) {
                    val randomPlayer = playerIds[random.nextInt(playerIds.size)]
                    updatedPlayers[randomPlayer]?.add(ingredient)
                }
            }

            updatePlayers(roomCode, updatedPlayers)

            db.collection("recipes").get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) return@addOnSuccessListener

                    val randomRecipe = result.documents.random()
                    val recipeId = randomRecipe.getLong("recipe_id") ?: return@addOnSuccessListener

                    val updates = mapOf(
                        "startTime" to System.currentTimeMillis(),
                        "recipe_id" to recipeId,
                        "step_id" to 0
                    )

                    roomRef.update(updates)
                }
        }
    }


    fun moveIngredientToNextPlayer(roomCode: String, currentPlayerId: String, ingredientId: String) {
        if (!ingredientId.startsWith("i")) return

        getPlayersMap(roomCode) { updatedPlayers ->
            val playerIds = updatedPlayers.keys.toList()

            val currentList = updatedPlayers[currentPlayerId]
            if (currentList?.contains(ingredientId) != true) return@getPlayersMap

            currentList.remove(ingredientId)

            val nextIndex = (playerIds.indexOf(currentPlayerId) + 1) % playerIds.size
            updatedPlayers[playerIds[nextIndex]]?.add(ingredientId)

            updatePlayers(roomCode, updatedPlayers)
        }
    }

    fun moveIngredientToPreviousPlayer(roomCode: String, currentPlayerId: String, ingredientId: String) {
        if (!ingredientId.startsWith("i")) return

        getPlayersMap(roomCode) { updatedPlayers ->
            val playerIds = updatedPlayers.keys.toList()

            val currentList = updatedPlayers[currentPlayerId]
            if (currentList?.contains(ingredientId) != true) return@getPlayersMap

            currentList.remove(ingredientId)

            val prevIndex = if (playerIds.indexOf(currentPlayerId) - 1 < 0)
                playerIds.lastIndex else playerIds.indexOf(currentPlayerId) - 1

            updatedPlayers[playerIds[prevIndex]]?.add(ingredientId)

            updatePlayers(roomCode, updatedPlayers)
        }
    }

    fun switchIngredient(roomCode: String, playerId: String, ingredientIdIn: String, ingredientIdOut: String?) {
        if (!ingredientIdIn.startsWith("i")) return
        if (ingredientIdOut != null && ingredientIdOut.isNotEmpty() && !ingredientIdOut.startsWith("i")) return

        getPlayersMap(roomCode) { updatedPlayers ->
            val playerItems = updatedPlayers[playerId]
            if (playerItems?.contains(ingredientIdIn) != true) return@getPlayersMap

            playerItems.remove(ingredientIdIn)

            if (!ingredientIdOut.isNullOrEmpty()) {
                playerItems.add(ingredientIdOut)
            }

            updatePlayers(roomCode, updatedPlayers)
        }
    }

    private fun getPlayersMap(roomCode: String, onSuccess: (MutableMap<String, MutableList<String>>) -> Unit) {
        db.collection("rooms").document(roomCode).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) return@addOnSuccessListener

                val playersMap = document.get("players") as? Map<String, List<String>>
                if (playersMap == null) {
                    Log.e("Firebase", "Players field missing or invalid.")
                    return@addOnSuccessListener
                }

                val updated = playersMap.mapValues { it.value.toMutableList() }.toMutableMap()
                onSuccess(updated)
            }
            .addOnFailureListener {
                Log.e("Firebase", "Failed to get room: ${it.message}")
            }
    }

    fun transferItem(roomCode: String, fromPlayerId: String, toPlayerId: String, itemId: String) {
        getPlayersMap(roomCode) { updatedPlayers ->
            val fromItems = updatedPlayers[fromPlayerId]
            val toItems = updatedPlayers[toPlayerId]

            if (fromItems?.contains(itemId) == true) {
                fromItems.remove(itemId)
                toItems?.add(itemId)
                updatePlayers(roomCode, updatedPlayers)
            }
        }
    }

    fun getAdjacentPlayers(
        roomCode: String,
        playerId: String,
        onResult: (leftPlayer: String, rightPlayer: String) -> Unit
    ) {
        getPlayersMap(roomCode) { players ->
            val playerList = players.keys.toList()
            val index = playerList.indexOf(playerId)
            if (index == -1 || playerList.size < 2) return@getPlayersMap

            val leftIndex = if (index - 1 < 0) playerList.lastIndex else index - 1
            val rightIndex = (index + 1) % playerList.size

            onResult(playerList[leftIndex], playerList[rightIndex])
        }
    }

    private fun updatePlayers(roomCode: String, players: Map<String, List<String>>) {
        db.collection("rooms").document(roomCode)
            .update("players", players)
            .addOnSuccessListener {
                Log.d("Firebase", "Players updated.")
            }
            .addOnFailureListener {
                Log.e("Firebase", "Failed to update players: ${it.message}")
            }
    }

    fun removeRoom(roomCode: String) {
        db.collection("rooms").document(roomCode)
            .delete()
            .addOnSuccessListener {
                Log.d("Firebase", "Room $roomCode successfully deleted.")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error deleting room $roomCode: ${e.message}")
            }
    }
}
