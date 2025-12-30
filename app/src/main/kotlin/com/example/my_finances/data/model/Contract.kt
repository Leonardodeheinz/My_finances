package com.example.my_finances.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import java.util.Date

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

enum class ContractStatus{
    OPEN,
    CLOSED,
}