package com.example.my_finances.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date

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


enum class DebtStatus {
    OPEN,
    PAID,
    OVERDUE,
    PARTIALLY_PAID
}