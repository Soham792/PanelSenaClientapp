package com.panelsena.client.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.panelsena.client.data.model.AssignedDisplay
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun observeAssignedContent(clientId: String): Flow<AssignedDisplay?> = callbackFlow {
        val listener = firestore.collection("displays")
            .document(clientId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                try {
                    val display = snap?.toObject(AssignedDisplay::class.java)
                    trySend(display)
                } catch (e: Exception) {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }
}
