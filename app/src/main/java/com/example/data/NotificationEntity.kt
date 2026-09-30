package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val voucherId: Long? = null,
    val voucherNo: String = "",
    val title: String,
    val message: String,
    val type: String, // "EXPIRATION", "BOTTLENECK", "CHECKLIST", "STATUS_CHANGE"
    val urgency: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
