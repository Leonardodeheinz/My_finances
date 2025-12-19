package com.example.my_finances.data.model

import java.util.Date

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: String = "",
    val description: String = "",
    val date: Date = Date(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class TransactionType {
    INCOME,
    EXPENSE
}
