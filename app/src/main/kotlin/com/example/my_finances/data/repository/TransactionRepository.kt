package com.example.my_finances.data.repository

import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.model.Transaction
import com.example.my_finances.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface TransactionRepository : BaseRepository<Transaction> {
    /**
     * Get transactions by date range
     */
    suspend fun getByDateRange(startDate: Date, endDate: Date): Flow<AuthResult<List<Transaction>>>

    /**
     * Get transactions by category
     */
    suspend fun getByCategory(categoryId: String): Flow<AuthResult<List<Transaction>>>

    /**
     * Get transactions by type (income/expense)
     */
    suspend fun getByType(type: TransactionType): Flow<AuthResult<List<Transaction>>>

    /**
     * Get transactions for a specific month
     */
    suspend fun getByMonth(month: Int, year: Int): Flow<AuthResult<List<Transaction>>>

    /**
     * Get total amount for a date range
     */
    suspend fun getTotalByDateRange(startDate: Date, endDate: Date, type: TransactionType): Flow<AuthResult<Double>>

    /**
     * Observe transactions with real-time updates for a date range
     */
    fun observeByDateRange(startDate: Date, endDate: Date): Flow<AuthResult<List<Transaction>>>
}
