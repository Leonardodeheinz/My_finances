package com.example.my_finances.data.repository.impl

import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.model.Transaction
import com.example.my_finances.data.model.TransactionType
import com.example.my_finances.data.repository.FirebaseBaseRepository
import com.example.my_finances.data.repository.TransactionRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) : FirebaseBaseRepository<Transaction>(firestore, auth, "transactions"), TransactionRepository {

    override fun toModel(map: Map<String, Any>, id: String): Transaction {
        return Transaction(
            id = id,
            userId = map["userId"] as? String ?: "",
            amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
            type = TransactionType.valueOf(map["type"] as? String ?: "EXPENSE"),
            categoryId = map["categoryId"] as? String ?: "",
            description = map["description"] as? String ?: "",
            date = (map["date"] as? Timestamp)?.toDate() ?: Date(),
            createdAt = (map["createdAt"] as? Timestamp)?.toDate() ?: Date(),
            updatedAt = (map["updatedAt"] as? Timestamp)?.toDate() ?: Date()
        )
    }

    override fun toMap(item: Transaction): Map<String, Any> {
        return hashMapOf(
            "userId" to (currentUserId ?: ""),
            "amount" to item.amount,
            "type" to item.type.name,
            "categoryId" to item.categoryId,
            "description" to item.description,
            "date" to Timestamp(item.date),
            "createdAt" to Timestamp(item.createdAt),
            "updatedAt" to Timestamp(Date())
        )
    }

    override fun getBaseQuery(): Query {
        return firestore.collection("transactions")
            .whereEqualTo("userId", currentUserId)
            .orderBy("date", Query.Direction.DESCENDING)
    }

    override suspend fun getByDateRange(startDate: Date, endDate: Date): Flow<AuthResult<List<Transaction>>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", currentUserId)
                .whereGreaterThanOrEqualTo("date", Timestamp(startDate))
                .whereLessThanOrEqualTo("date", Timestamp(endDate))
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val transactions = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { toModel(it, doc.id) }
            }
            emit(AuthResult.Success(transactions))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch transactions"))
        }
    }

    override suspend fun getByCategory(categoryId: String): Flow<AuthResult<List<Transaction>>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("categoryId", categoryId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val transactions = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { toModel(it, doc.id) }
            }
            emit(AuthResult.Success(transactions))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch transactions"))
        }
    }

    override suspend fun getByType(type: TransactionType): Flow<AuthResult<List<Transaction>>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("type", type.name)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val transactions = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { toModel(it, doc.id) }
            }
            emit(AuthResult.Success(transactions))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch transactions"))
        }
    }

    override suspend fun getByMonth(month: Int, year: Int): Flow<AuthResult<List<Transaction>>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, 1, 0, 0, 0)
            val startDate = calendar.time

            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.SECOND, -1)
            val endDate = calendar.time

            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", currentUserId)
                .whereGreaterThanOrEqualTo("date", Timestamp(startDate))
                .whereLessThanOrEqualTo("date", Timestamp(endDate))
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val transactions = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { toModel(it, doc.id) }
            }
            emit(AuthResult.Success(transactions))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch transactions"))
        }
    }

    override suspend fun getTotalByDateRange(
        startDate: Date,
        endDate: Date,
        type: TransactionType
    ): Flow<AuthResult<Double>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("type", type.name)
                .whereGreaterThanOrEqualTo("date", Timestamp(startDate))
                .whereLessThanOrEqualTo("date", Timestamp(endDate))
                .get()
                .await()

            val total = snapshot.documents.sumOf { doc ->
                (doc.data?.get("amount") as? Number)?.toDouble() ?: 0.0
            }
            emit(AuthResult.Success(total))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to calculate total"))
        }
    }

    override fun observeByDateRange(startDate: Date, endDate: Date): Flow<AuthResult<List<Transaction>>> = callbackFlow {
        if (currentUserId == null) {
            trySend(AuthResult.Error("User not authenticated"))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("transactions")
            .whereEqualTo("userId", currentUserId)
            .whereGreaterThanOrEqualTo("date", Timestamp(startDate))
            .whereLessThanOrEqualTo("date", Timestamp(endDate))
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(AuthResult.Error(error.message ?: "Failed to observe transactions"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val transactions = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { toModel(it, doc.id) }
                    }
                    trySend(AuthResult.Success(transactions))
                }
            }

        awaitClose { listener.remove() }
    }
}
