package com.example.my_finances.data.repository.impl

import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.model.Category
import com.example.my_finances.data.model.TransactionType
import com.example.my_finances.data.repository.CategoryRepository
import com.example.my_finances.data.repository.FirebaseBaseRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) : FirebaseBaseRepository<Category>(firestore, auth, "categories"), CategoryRepository {

    override fun toModel(map: Map<String, Any>, id: String): Category {
        return Category(
            id = id,
            userId = map["userId"] as? String ?: "",
            name = map["name"] as? String ?: "",
            icon = map["icon"] as? String ?: "",
            color = map["color"] as? String ?: "#000000",
            type = TransactionType.valueOf(map["type"] as? String ?: "EXPENSE"),
            isDefault = map["isDefault"] as? Boolean ?: false,
            createdAt = (map["createdAt"] as? Timestamp)?.toDate() ?: Date()
        )
    }

    override fun toMap(item: Category): Map<String, Any> {
        return hashMapOf(
            "userId" to (currentUserId ?: ""),
            "name" to item.name,
            "icon" to item.icon,
            "color" to item.color,
            "type" to item.type.name,
            "isDefault" to item.isDefault,
            "createdAt" to Timestamp(item.createdAt)
        )
    }

    override fun getBaseQuery(): Query {
        return firestore.collection("categories")
            .whereEqualTo("userId", currentUserId)
            .orderBy("name", Query.Direction.ASCENDING)
    }

    override suspend fun getByType(type: TransactionType): Flow<AuthResult<List<Category>>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = firestore.collection("categories")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("type", type.name)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()

            val categories = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { toModel(it, doc.id) }
            }
            emit(AuthResult.Success(categories))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch categories"))
        }
    }

    override suspend fun getDefaultCategories(): Flow<AuthResult<List<Category>>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = firestore.collection("categories")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isDefault", true)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()

            val categories = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { toModel(it, doc.id) }
            }
            emit(AuthResult.Success(categories))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch default categories"))
        }
    }

    override suspend fun getUserCategories(): Flow<AuthResult<List<Category>>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = firestore.collection("categories")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isDefault", false)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()

            val categories = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { toModel(it, doc.id) }
            }
            emit(AuthResult.Success(categories))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch user categories"))
        }
    }

    override fun observeByType(type: TransactionType): Flow<AuthResult<List<Category>>> = callbackFlow {
        if (currentUserId == null) {
            trySend(AuthResult.Error("User not authenticated"))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("categories")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("type", type.name)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(AuthResult.Error(error.message ?: "Failed to observe categories"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val categories = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { toModel(it, doc.id) }
                    }
                    trySend(AuthResult.Success(categories))
                }
            }

        awaitClose { listener.remove() }
    }
}
