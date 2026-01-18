package com.example.my_finances.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Calendar
import java.util.Date

enum class BudgetPeriodType {
    MONTHLY,
    YEARLY,
    CUSTOM
}

@Keep
data class Budget(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val categoryIds: List<String> = emptyList(),
    val amount: Double = 0.0,
    val spent: Double = 0.0,
    val periodType: BudgetPeriodType = BudgetPeriodType.MONTHLY,
    val month: Int = 0,
    val year: Int = 0,
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    val remaining: Double
        get() = amount - spent

    val percentage: Float
        get() = if (amount > 0) (spent / amount * 100).toFloat().coerceIn(0f, 100f) else 0f

    val isOverBudget: Boolean
        get() = spent > amount

    val isActive: Boolean
        get() {
            val now = Date()
            return when (periodType) {
                BudgetPeriodType.MONTHLY -> {
                    val calendar = Calendar.getInstance()
                    val currentMonth = calendar.get(Calendar.MONTH) + 1
                    val currentYear = calendar.get(Calendar.YEAR)
                    month == currentMonth && year == currentYear
                }
                BudgetPeriodType.YEARLY -> {
                    val calendar = Calendar.getInstance()
                    val currentYear = calendar.get(Calendar.YEAR)
                    year == currentYear
                }
                BudgetPeriodType.CUSTOM -> {
                    now.after(startDate) && now.before(endDate) || now == startDate || now == endDate
                }
            }
        }

    val periodDisplayName: String
        get() = when (periodType) {
            BudgetPeriodType.MONTHLY -> {
                val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                "${monthNames.getOrElse(month - 1) { "Unknown" }} $year"
            }
            BudgetPeriodType.YEARLY -> "$year"
            BudgetPeriodType.CUSTOM -> {
                val dateFormat = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
                "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
            }
        }
}

