package com.example.my_finances.data.repository

import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository : BaseRepository<Budget> {
    /**
     * Get budget for a specific month and year
     */
    suspend fun getByMonth(month: Int, year: Int): Flow<AuthResult<List<Budget>>>

    /**
     * Get budget for a specific category and month
     */
    suspend fun getByCategoryAndMonth(categoryId: String, month: Int, year: Int): Flow<AuthResult<Budget?>>

    /**
     * Update spent amount for a budget
     */
    suspend fun updateSpent(id: String, spent: Double): Flow<AuthResult<Unit>>

    /**
     * Get all budgets that are over limit
     */
    suspend fun getOverBudget(): Flow<AuthResult<List<Budget>>>

    /**
     * Observe budgets for a specific month with real-time updates
     */
    fun observeByMonth(month: Int, year: Int): Flow<AuthResult<List<Budget>>>
}
