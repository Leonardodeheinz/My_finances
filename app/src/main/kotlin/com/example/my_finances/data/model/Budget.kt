package com.example.my_finances.data.model

import java.util.Date

data class Budget(
    val id: String = "",
    val userId: String = "",
    val categoryId: String = "",
    val amount: Double = 0.0,
    val spent: Double = 0.0,
    val month: Int = 0,
    val year: Int = 0,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    val remaining: Double
        get() = amount - spent

    val percentage: Float
        get() = if (amount > 0) (spent / amount * 100).toFloat() else 0f
}
