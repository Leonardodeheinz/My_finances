package com.example.my_finances.data.repository

import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.model.Budget
import com.example.my_finances.data.model.BudgetPeriodType
import kotlinx.coroutines.flow.Flow

interface BudgetRepository : BaseRepository<Budget> {
    /**
     * Get budgets for a specific month and year
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

    /**
     * Get all active budgets (currently within their time period)
     */
    suspend fun getActiveBudgets(): Flow<AuthResult<List<Budget>>>

    /**
     * Get budgets by period type
     */
    suspend fun getByPeriodType(periodType: BudgetPeriodType): Flow<AuthResult<List<Budget>>>

    /**
     * Observe all budgets with real-time updates
     */
    fun observeAllBudgets(): Flow<AuthResult<List<Budget>>>

    /**
     * Calculate and update spent amount for a budget based on transactions
     */
    suspend fun recalculateSpent(budgetId: String, spent: Double): Flow<AuthResult<Unit>>
}
