package com.example.my_finances.data.repository

import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.model.Debt
import com.example.my_finances.data.model.DebtStatus
import kotlinx.coroutines.flow.Flow

interface DebtRepository : BaseRepository<Debt> {
    /**
     * Get debts by status
     */
    suspend fun getByStatus(status: DebtStatus): Flow<AuthResult<List<Debt>>>

    /**
     * Get all open debts
     */
    suspend fun getOpenDebts(): Flow<AuthResult<List<Debt>>>

    /**
     * Update debt status
     */
    suspend fun updateStatus(id: String, status: DebtStatus): Flow<AuthResult<Unit>>

    /**
     * Update paid amount
     */
    suspend fun updatePaidAmount(id: String, paidAmount: Double): Flow<AuthResult<Unit>>

    /**
     * Get total debt amount
     */
    suspend fun getTotalDebt(): Flow<AuthResult<Double>>

    /**
     * Observe debts with real-time updates
     */
    fun observeDebts(): Flow<AuthResult<List<Debt>>>
}
