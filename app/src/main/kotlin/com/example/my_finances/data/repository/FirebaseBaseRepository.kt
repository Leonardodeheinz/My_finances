package com.example.my_finances.data.repository

import com.example.my_finances.data.model.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Abstract base repository implementation for Firestore
 * Provides common CRUD operations that all repositories can inherit
 */
abstract class FirebaseBaseRepository<T>(
    protected val firestore: FirebaseFirestore,
    protected val auth: FirebaseAuth,
    private val collectionPath: String
) : BaseRepository<T> {

    /**
     * Get current user ID
     */
    protected val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Convert Firestore document to data model
     * Must be implemented by child classes
     */
    protected abstract fun toModel(map: Map<String, Any>, id: String): T

    /**
     * Convert data model to Firestore document
     * Must be implemented by child classes
     */
    protected abstract fun toMap(item: T): Map<String, Any>

    /**
     * Get the base query for this collection
     * Override to add custom filters (e.g., filter by userId)
     */
    protected open fun getBaseQuery(): Query {
        return firestore.collection(collectionPath)
    }

    override suspend fun getAll(): Flow<AuthResult<List<T>>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = getBaseQuery().get().await()
            val items = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data?.let { toModel(it, doc.id) }
                } catch (e: Exception) {
                    null
                }
            }
            emit(AuthResult.Success(items))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch items"))
        }
    }

    override suspend fun getById(id: String): Flow<AuthResult<T>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val doc = firestore.collection(collectionPath).document(id).get().await()
            if (doc.exists() && doc.data != null) {
                val item = toModel(doc.data!!, doc.id)
                emit(AuthResult.Success(item))
            } else {
                emit(AuthResult.Error("Item not found"))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch item"))
        }
    }

    override suspend fun insert(item: T): Flow<AuthResult<String>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val data = toMap(item)
            val docRef = firestore.collection(collectionPath).add(data).await()
            emit(AuthResult.Success(docRef.id))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to insert item"))
        }
    }

    override suspend fun update(id: String, item: T): Flow<AuthResult<Unit>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val data = toMap(item)
            firestore.collection(collectionPath).document(id).set(data).await()
            emit(AuthResult.Success(Unit))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to update item"))
        }
    }

    override suspend fun delete(id: String): Flow<AuthResult<Unit>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            firestore.collection(collectionPath).document(id).delete().await()
            emit(AuthResult.Success(Unit))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to delete item"))
        }
    }

    override fun observeAll(): Flow<AuthResult<List<T>>> = callbackFlow {
        if (currentUserId == null) {
            trySend(AuthResult.Error("User not authenticated"))
            close()
            return@callbackFlow
        }

        val listener = getBaseQuery().addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(AuthResult.Error(error.message ?: "Failed to observe items"))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val items = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data?.let { toModel(it, doc.id) }
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(AuthResult.Success(items))
            }
        }

        awaitClose { listener.remove() }
    }

    override fun observeById(id: String): Flow<AuthResult<T>> = callbackFlow {
        if (currentUserId == null) {
            trySend(AuthResult.Error("User not authenticated"))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(collectionPath).document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(AuthResult.Error(error.message ?: "Failed to observe item"))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists() && snapshot.data != null) {
                    try {
                        val item = toModel(snapshot.data!!, snapshot.id)
                        trySend(AuthResult.Success(item))
                    } catch (e: Exception) {
                        trySend(AuthResult.Error(e.message ?: "Failed to parse item"))
                    }
                } else {
                    trySend(AuthResult.Error("Item not found"))
                }
            }

        awaitClose { listener.remove() }
    }
}
