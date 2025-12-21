package com.example.my_finances.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date

@Keep
data class Budget(
    @DocumentId val id: String = "",
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

@Keep
data class FinancialEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val categoryId: String = "",
    val amount: Double = 0.0,
    val spent: Double = 0.0,
    val month: Int = 0,
    val year: Int = 0,
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val renewalDate: Date = Date()
) {
    val remaining: Double
        get() = amount - spent

    val percentage: Float
        get() = if (amount > 0) (spent / amount * 100).toFloat() else 0f
}

enum class DebtStatus {
    OPEN,
    PAID,
    OVERDUE,
    PARTIALLY_PAID
}

enum class ContractStatus{
    OPEN,
    CLOSED,
}


@Keep
data class Contract(
    @DocumentId val id: String = "",
    val userid: String = "",
    val categoryId: String = "",
    val name: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val month: Int = 0,
    val year : Int = 0,
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val status: ContractStatus = ContractStatus.OPEN,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

@Keep
data class Debt(
    @DocumentId val id: String = "",
    val userId: String = "",
    val creditor: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val repaymentRate: Double = 0.0,
    val paidAmount: Double = 0.0,
    val status: DebtStatus = DebtStatus.OPEN,
    val dueDate: Timestamp? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)