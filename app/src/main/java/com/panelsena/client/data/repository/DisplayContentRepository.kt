package com.panelsena.client.data.repository

import com.panelsena.client.core.utils.ClientIdManager
import com.panelsena.client.core.utils.NetworkMonitor
import com.panelsena.client.data.firebase.FirestoreDataSource
import com.panelsena.client.data.model.AssignedDisplay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisplayContentRepository @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val clientIdManager: ClientIdManager,
    private val networkMonitor: NetworkMonitor
) {
    val isOnline: Flow<Boolean> = networkMonitor.isOnline

    suspend fun getClientId(): String = clientIdManager.getOrCreateClientId()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeDisplayContent(): Flow<AssignedDisplay?> = networkMonitor.isOnline.flatMapLatest { online ->
        if (online) {
            val clientId = clientIdManager.getOrCreateClientId()
            firestoreDataSource.observeAssignedContent(clientId)
        } else {
            flowOf(null)
        }
    }.catch { emit(null) }
}
