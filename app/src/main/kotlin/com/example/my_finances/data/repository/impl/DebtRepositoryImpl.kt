package com.example.my_finances.data.repository.impl

import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.model.Debt
import com.example.my_finances.data.model.DebtStatus
import com.example.my_finances.data.repository.DebtRepository
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
class DebtRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) : FirebaseBaseRepository<Debt>(firestore, auth, "debts"), DebtRepository {

    override fun toModel(map: Map<String, Any>, id: String): Debt {
        return Debt(
            id = id,
            userId = map["userId"] as? String ?: "",
            creditor = map["creditor"] as? String ?: "",
            description = map["description"] as? String ?: "",
            amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
            repaymentRate = (map["repaymentRate"] as? Number)?.toDouble() ?: 0.0,
            paidAmount = (map["paidAmount"] as? Number)?.toDouble() ?: 0.0,
            status = DebtStatus.valueOf(
                map["status"] as? String ?: DebtStatus.OPEN.name
            ),
            dueDate = map["dueDate"] as? Timestamp,
            createdAt = (map["createdAt"] as? Timestamp)?.toDate() ?: Date(),
            updatedAt = (map["updatedAt"] as? Timestamp)?.toDate() ?: Date()
        )
    }

    override fun toMap(item: Debt): Map<String, Any> {
        val now = Date()
        val map = hashMapOf<String, Any>(
            "userId" to (currentUserId ?: ""),
            "creditor" to item.creditor,
            "description" to item.description,
            "amount" to item.amount,
            "repaymentRate" to item.repaymentRate,
            "paidAmount" to item.paidAmount,
            "status" to item.status.name,
            "createdAt" to Timestamp(item.createdAt.takeIf { item.id.isNotEmpty() } ?: now),
            "updatedAt" to Timestamp(now)
        )
        item.dueDate?.let { map["dueDate"] = it }
        return map
    }

    override fun getBaseQuery(): Query {
        return firestore.collection("debts")
            .whereEqualTo("userId", currentUserId)
            .orderBy("dueDate", Query.Direction.ASCENDING)
    }

    override suspend fun getByStatus(status: DebtStatus): Flow<AuthResult<List<Debt>>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = firestore.collection("debts")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", status.name)
                .get()
                .await()

            val debts = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { toModel(it, doc.id) }
            }
            emit(AuthResult.Success(debts))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch debts"))
        }
    }

    override suspend fun getOpenDebts(): Flow<AuthResult<List<Debt>>> {
        return getByStatus(DebtStatus.OPEN)
    }

    override suspend fun updateStatus(id: String, status: DebtStatus): Flow<AuthResult<Unit>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            firestore.collection("debts")
                .document(id)
                .update("status", status.name)
                .await()

            emit(AuthResult.Success(Unit))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to update debt status"))
        }
    }

    override suspend fun updatePaidAmount(id: String, paidAmount: Double): Flow<AuthResult<Unit>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            firestore.collection("debts")
                .document(id)
                .update("paidAmount", paidAmount)
                .await()

            emit(AuthResult.Success(Unit))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to update paid amount"))
        }
    }

    override suspend fun getTotalDebt(): Flow<AuthResult<Double>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = firestore.collection("debts")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", DebtStatus.OPEN.name)
                .get()
                .await()

            val totalDebt = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { toModel(it, doc.id) }
            }.sumOf { it.amount - it.paidAmount }

            emit(AuthResult.Success(totalDebt))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to calculate total debt"))
        }
    }

    override fun observeDebts(): Flow<AuthResult<List<Debt>>> = callbackFlow {
        if (currentUserId == null) {
            trySend(AuthResult.Error("User not authenticated"))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("debts")
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(AuthResult.Error(error.message ?: "Failed to observe debts"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val debts = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { toModel(it, doc.id) }
                    }
                    trySend(AuthResult.Success(debts))
                }
            }

        awaitClose { listener.remove() }
    }
}
