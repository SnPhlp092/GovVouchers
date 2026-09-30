package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class ExpirationUrgency {
    EXPIRED,
    CRITICAL, // <= 3 days
    WARNING,  // <= 7 days
    GOOD,     // > 7 days
    COMPLETED
}

data class ExpirationInfo(
    val urgency: ExpirationUrgency,
    val remainingDays: Long,
    val formattedDate: String,
    val label: String
)

object ExpirationHelper {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val periodFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)

    fun calculate(expirationTimestamp: Long, isCompleted: Boolean = false): ExpirationInfo {
        val formattedDate = dateFormat.format(Date(expirationTimestamp))
        if (isCompleted) {
            return ExpirationInfo(
                urgency = ExpirationUrgency.COMPLETED,
                remainingDays = 0,
                formattedDate = formattedDate,
                label = "Settled / Paid"
            )
        }

        val now = System.currentTimeMillis()
        val diffMs = expirationTimestamp - now
        val days = TimeUnit.MILLISECONDS.toDays(diffMs)

        return when {
            diffMs < 0 -> {
                val overdueDays = TimeUnit.MILLISECONDS.toDays(-diffMs)
                ExpirationInfo(
                    urgency = ExpirationUrgency.EXPIRED,
                    remainingDays = -overdueDays,
                    formattedDate = formattedDate,
                    label = "Expired ${overdueDays}d ago"
                )
            }
            days <= 3 -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
                ExpirationInfo(
                    urgency = ExpirationUrgency.CRITICAL,
                    remainingDays = days,
                    formattedDate = formattedDate,
                    label = if (days <= 0) "${hours}h left" else "${days}d left (Urgent)"
                )
            }
            days <= 7 -> {
                ExpirationInfo(
                    urgency = ExpirationUrgency.WARNING,
                    remainingDays = days,
                    formattedDate = formattedDate,
                    label = "${days} days left"
                )
            }
            else -> {
                ExpirationInfo(
                    urgency = ExpirationUrgency.GOOD,
                    remainingDays = days,
                    formattedDate = formattedDate,
                    label = "${days} days left"
                )
            }
        }
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
}
