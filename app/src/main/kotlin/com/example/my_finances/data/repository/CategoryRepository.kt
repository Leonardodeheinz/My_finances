package com.example.my_finances.data.repository

import com.example.my_finances.data.model.AuthResult
import com.example.my_finances.data.model.Category
import com.example.my_finances.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository : BaseRepository<Category> {
    /**
     * Get categories by type (income/expense)
     */
    suspend fun getByType(type: TransactionType): Flow<AuthResult<List<Category>>>

    /**
     * Get default categories
     */
    suspend fun getDefaultCategories(): Flow<AuthResult<List<Category>>>

    /**
     * Get user-created categories
     */
    suspend fun getUserCategories(): Flow<AuthResult<List<Category>>>

    /**
     * Observe categories by type with real-time updates
     */
    fun observeByType(type: TransactionType): Flow<AuthResult<List<Category>>>
}
