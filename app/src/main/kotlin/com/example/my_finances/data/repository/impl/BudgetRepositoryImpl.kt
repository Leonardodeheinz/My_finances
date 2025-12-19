package com.example.my_finances.data.repository.impl

import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.model.Budget
import com.example.my_finances.data.repository.BudgetRepository
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
class BudgetRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) : FirebaseBaseRepository<Budget>(firestore, auth, "budgets"), BudgetRepository {

    override fun toModel(map: Map<String, Any>, id: String): Budget {
        return Budget(
            id = id,
            userId = map["userId"] as? String ?: "",
            categoryId = map["categoryId"] as? String ?: "",
            amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
            spent = (map["spent"] as? Number)?.toDouble() ?: 0.0,
            month = (map["month"] as? Number)?.toInt() ?: 0,
            year = (map["year"] as? Number)?.toInt() ?: 0,
            createdAt = (map["createdAt"] as? Timestamp)?.toDate() ?: Date(),
            updatedAt = (map["updatedAt"] as? Timestamp)?.toDate() ?: Date()
        )
    }

    override fun toMap(item: Budget): Map<String, Any> {
        return hashMapOf(
            "userId" to (currentUserId ?: ""),
            "categoryId" to item.categoryId,
            "amount" to item.amount,
            "spent" to item.spent,
            "month" to item.month,
            "year" to item.year,
            "createdAt" to Timestamp(item.createdAt),
            "updatedAt" to Timestamp(Date())
        )
    }

    override fun getBaseQuery(): Query {
        return firestore.collection("budgets")
            .whereEqualTo("userId", currentUserId)
            .orderBy("year", Query.Direction.DESCENDING)
            .orderBy("month", Query.Direction.DESCENDING)
    }

    override suspend fun getByMonth(month: Int, year: Int): Flow<AuthResult<List<Budget>>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = firestore.collection("budgets")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .get()
                .await()

            val budgets = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { toModel(it, doc.id) }
            }
            emit(AuthResult.Success(budgets))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch budgets"))
        }
    }

    override suspend fun getByCategoryAndMonth(
        categoryId: String,
        month: Int,
        year: Int
    ): Flow<AuthResult<Budget?>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = firestore.collection("budgets")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("categoryId", categoryId)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .limit(1)
                .get()
                .await()

            val budget = snapshot.documents.firstOrNull()?.data?.let {
                toModel(it, snapshot.documents.first().id)
            }
            emit(AuthResult.Success(budget))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch budget"))
        }
    }

    override suspend fun updateSpent(id: String, spent: Double): Flow<AuthResult<Unit>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            firestore.collection("budgets")
                .document(id)
                .update(
                    mapOf(
                        "spent" to spent,
                        "updatedAt" to Timestamp(Date())
                    )
                )
                .await()

            emit(AuthResult.Success(Unit))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to update budget"))
        }
    }

    override suspend fun getOverBudget(): Flow<AuthResult<List<Budget>>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = firestore.collection("budgets")
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()

            val budgets = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { toModel(it, doc.id) }
            }.filter { it.spent > it.amount }

            emit(AuthResult.Success(budgets))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch over-budget items"))
        }
    }

    override fun observeByMonth(month: Int, year: Int): Flow<AuthResult<List<Budget>>> = callbackFlow {
        if (currentUserId == null) {
            trySend(AuthResult.Error("User not authenticated"))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("budgets")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("month", month)
            .whereEqualTo("year", year)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(AuthResult.Error(error.message ?: "Failed to observe budgets"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val budgets = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { toModel(it, doc.id) }
                    }
                    trySend(AuthResult.Success(budgets))
                }
            }

        awaitClose { listener.remove() }
    }
}
