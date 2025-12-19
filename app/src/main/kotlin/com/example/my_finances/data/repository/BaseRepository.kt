package com.example.my_finances.data.repository

import com.example.my_finances.data.model.AuthResult
import kotlinx.coroutines.flow.Flow

/**
 * Base repository interface with common CRUD operations
 * @param T The data model type
 */
interface BaseRepository<T> {
    /**
     * Get all items for the current user
     */
    suspend fun getAll(): Flow<AuthResult<List<T>>>

    /**
     * Get a single item by ID
     */
    suspend fun getById(id: String): Flow<AuthResult<T>>

    /**
     * Insert a new item
     * @return The ID of the created item
     */
    suspend fun insert(item: T): Flow<AuthResult<String>>

    /**
     * Update an existing item
     */
    suspend fun update(id: String, item: T): Flow<AuthResult<Unit>>

    /**
     * Delete an item by ID
     */
    suspend fun delete(id: String): Flow<AuthResult<Unit>>

    /**
     * Get items with real-time updates
     */
    fun observeAll(): Flow<AuthResult<List<T>>>

    /**
     * Observe a single item with real-time updates
     */
    fun observeById(id: String): Flow<AuthResult<T>>
}
