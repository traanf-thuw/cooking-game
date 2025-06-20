package com.example.cookinggameapp

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class RoomStateListener(
    private val db: FirebaseFirestore,
    private val roomCode: String,
    private val currentPlayerId: String,
    private val onStepAdvanced: (Int) -> Unit,
    private val onItemReceived: (String, String, String) -> Unit
) {
    private var stepListener: ListenerRegistration? = null
    private var transferListener: ListenerRegistration? = null
    private var lastStepIndex: Int = -1

    fun startListening() {
        stepListener = db.collection("rooms").document(roomCode)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    Log.w("RoomStateListener", "❌ Failed to listen to room document")
                    return@addSnapshotListener
                }

                val newStepIndex = snapshot.getLong("currentStepIndex")?.toInt() ?: return@addSnapshotListener

                if (newStepIndex != lastStepIndex) {
                    lastStepIndex = newStepIndex
                    onStepAdvanced(newStepIndex)
                }
            }

        transferListener = db.collection("rooms").document(roomCode)
            .collection("transfers")
            .whereEqualTo("to", currentPlayerId)
            .addSnapshotListener { snapshots, _ ->
                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val data = change.document.data
                        val itemId = data["item"] as? String ?: return@forEach
                        val fromPlayer = data["from"] as? String ?: return@forEach
                        val direction = data["direction"] as? String ?: return@forEach

                        onItemReceived(itemId, fromPlayer, direction)

                        // Mark transfer as processed
                        change.document.reference.delete()
                    }
                }
            }
    }

    fun stopListening() {
        stepListener?.remove()
        transferListener?.remove()
    }
}
