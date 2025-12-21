package com.example.my_finances.data.repository.impl

import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.model.Contract
import com.example.my_finances.data.model.ContractStatus
import com.example.my_finances.data.repository.ContractRepository
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
class ContractRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) : FirebaseBaseRepository<Contract>(firestore, auth, "contracts"), ContractRepository {

    override fun toModel(map: Map<String, Any>, id: String): Contract {
        return Contract(
            id = id,
            userid = map["userId"] as? String ?: "",
            categoryId = map["categoryId"] as? String ?: "",
            name = map["name"] as? String ?: "",
            description = map["description"] as? String ?: "",
            amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
            month = (map["month"] as? Number)?.toInt() ?: 0,
            year = (map["year"] as? Number)?.toInt() ?: 0,
            startDate = (map["startDate"] as? Timestamp)?.toDate() ?: Date(),
            endDate = (map["endDate"] as? Timestamp)?.toDate() ?: Date(),
            status = ContractStatus.valueOf(
                map["status"] as? String ?: ContractStatus.OPEN.name
            ),
            createdAt = (map["createdAt"] as? Timestamp)?.toDate() ?: Date(),
            updatedAt = (map["updatedAt"] as? Timestamp)?.toDate() ?: Date()
        )
    }

    override fun toMap(item: Contract): Map<String, Any> {
        val now = Date()
        return hashMapOf(
            "userId" to (currentUserId ?: ""),
            "categoryId" to item.categoryId,
            "name" to item.name,
            "description" to item.description,
            "amount" to item.amount,
            "month" to item.month,
            "year" to item.year,
            "startDate" to Timestamp(item.startDate),
            "endDate" to Timestamp(item.endDate),
            "status" to item.status.name,
            "createdAt" to Timestamp(item.createdAt.takeIf { item.id.isNotEmpty() } ?: now),
            "updatedAt" to Timestamp(now)
        )
    }

    override fun getBaseQuery(): Query {
        return firestore.collection("contracts")
            .whereEqualTo("userId", currentUserId)
            .orderBy("startDate", Query.Direction.DESCENDING)
    }

    override suspend fun getByStatus(status: ContractStatus): Flow<AuthResult<List<Contract>>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            val snapshot = firestore.collection("contracts")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("status", status.name)
                .get()
                .await()

            val contracts = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { toModel(it, doc.id) }
            }
            emit(AuthResult.Success(contracts))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to fetch contracts"))
        }
    }

    override suspend fun getActiveContracts(): Flow<AuthResult<List<Contract>>> {
        return getByStatus(ContractStatus.OPEN)
    }

    override suspend fun updateStatus(id: String, status: ContractStatus): Flow<AuthResult<Unit>> = flow {
        try {
            emit(AuthResult.Loading)

            if (currentUserId == null) {
                emit(AuthResult.Error("User not authenticated"))
                return@flow
            }

            firestore.collection("contracts")
                .document(id)
                .update("status", status.name)
                .await()

            emit(AuthResult.Success(Unit))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Failed to update contract"))
        }
    }

    override fun observeContracts(): Flow<AuthResult<List<Contract>>> = callbackFlow {
        if (currentUserId == null) {
            trySend(AuthResult.Error("User not authenticated"))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("contracts")
            .whereEqualTo("userId", currentUserId)
            .orderBy("startDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(AuthResult.Error(error.message ?: "Failed to observe contracts"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val contracts = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { toModel(it, doc.id) }
                    }
                    trySend(AuthResult.Success(contracts))
                }
            }

        awaitClose { listener.remove() }
    }
}
