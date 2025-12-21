package com.example.my_finances.data.repository

import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.model.Contract
import com.example.my_finances.data.model.ContractStatus
import kotlinx.coroutines.flow.Flow

interface ContractRepository : BaseRepository<Contract> {
    /**
     * Get contracts by status
     */
    suspend fun getByStatus(status: ContractStatus): Flow<AuthResult<List<Contract>>>

    /**
     * Get active contracts
     */
    suspend fun getActiveContracts(): Flow<AuthResult<List<Contract>>>

    /**
     * Update contract status
     */
    suspend fun updateStatus(id: String, status: ContractStatus): Flow<AuthResult<Unit>>

    /**
     * Observe contracts with real-time updates
     */
    fun observeContracts(): Flow<AuthResult<List<Contract>>>
}
