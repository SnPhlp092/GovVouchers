package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audit_logs",
    foreignKeys = [
        ForeignKey(
            entity = VoucherEntity::class,
            parentColumns = ["id"],
            childColumns = ["voucherId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["voucherId"])]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val voucherId: Long,
    val voucherNo: String,
    val action: String, // "CREATED", "BUDGET_RECEIVED", "BUDGET_OFFICER_SIGN", "ACCOUNTING_RECEIVED", "PROCESSING_COMPLETED", "INDEXED", "RELEASED", "RETURNED", "CHECKLIST_UPDATED"
    val performedByRole: String,
    val performedByName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)
