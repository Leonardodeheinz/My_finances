package com.example.my_finances.data.model

import java.util.Date

data class Category(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val icon: String = "",
    val color: String = "#000000",
    val type: TransactionType = TransactionType.EXPENSE,
    val isDefault: Boolean = false,
    val createdAt: Date = Date()
)
