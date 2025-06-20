package com.example.cookinggameapp

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.firestore.*

class ItemTransferHandler(
    private val context: Context,
    private val db: FirebaseFirestore,
    private val roomCode: String,
    private val currentPlayerId: String,
    private val playerIds: List<String>,
    private val getBasketLeft: () -> View,
    private val getBasketRight: () -> View,
    private val allItems: List<ImageView>,
    private val onItemReceived: (ImageView) -> Unit,
    private val vibrate: () -> Unit
) {

    private var listenerRegistration: ListenerRegistration? = null

    fun startListening() {
        val transfersRef = db.collection("rooms").document(roomCode).collection("transfers")

        listenerRegistration = transfersRef
            .whereEqualTo("to", currentPlayerId)
            .addSnapshotListener { snapshots, _ ->
                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val transfer = change.document.data
                        val itemId = transfer["item"] as? String ?: return@forEach
                        val fromPlayer = transfer["from"] as? String ?: return@forEach
                        val direction = transfer["direction"] as? String ?: return@forEach

                        receiveItem(itemId, fromPlayer, direction)
                    }
                }
            }
    }

    fun handleItemDrop(view: View, itemId: String) {
        val itemBox = Rect()
        val leftBox = Rect()
        val rightBox = Rect()

        view.getGlobalVisibleRect(itemBox)
        getBasketLeft().getGlobalVisibleRect(leftBox)
        getBasketRight().getGlobalVisibleRect(rightBox)

        val direction: String? = when {
            Rect.intersects(itemBox, rightBox) -> "right"
            Rect.intersects(itemBox, leftBox) -> "left"
            else -> null
        }

        if (direction == null) {
            return
        }

        val receiverId = getNeighborId(currentPlayerId, direction)

        // Animate item fading out before triggering Firebase transfer
        view.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                view.visibility = View.INVISIBLE

                val transferData = mapOf(
                    "from" to currentPlayerId,
                    "to" to receiverId,
                    "item" to itemId,
                    "direction" to direction,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("rooms").document(roomCode).collection("transfers")
                    .add(transferData)
                    .addOnSuccessListener {
                        if (receiverId == currentPlayerId) {
                            view.postDelayed({
                                receiveItem(itemId, "yourself", direction)
                            }, 500)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Transfer failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .start()
    }

    fun receiveItem(itemId: String, fromPlayer: String, direction: String) {
        val view = allItems.find { it.tag == itemId } ?: return

        val entryBasket = if (direction == "right") getBasketLeft() else getBasketRight()
        val basketX = entryBasket.x
        val basketY = entryBasket.y

        view.translationX = basketX
        view.translationY = -300f
        view.alpha = 0f
        view.scaleX = 0.3f
        view.scaleY = 0.3f
        view.rotation = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .translationY(basketY)
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .rotationBy(1440f)
            .setDuration(1000)
            .withEndAction {
                Toast.makeText(context, "$itemId received from $fromPlayer!", Toast.LENGTH_SHORT).show()
                vibrate()
                onItemReceived(view)
            }
            .start()
    }

    private fun getNeighborId(currentId: String, direction: String): String {
        val index = playerIds.indexOf(currentId)
        if (index == -1 || playerIds.isEmpty()) return currentId

        return when {
            playerIds.size == 1 -> currentId
            direction == "left" -> playerIds[(index - 1 + playerIds.size) % playerIds.size]
            direction == "right" -> playerIds[(index + 1) % playerIds.size]
            else -> currentId
        }
    }

    fun stopListening() {
        listenerRegistration?.remove()
    }
}
