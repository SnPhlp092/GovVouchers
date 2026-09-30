package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checklist_items",
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
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val voucherId: Long,
    val sectionGroup: String, // "General Requirements", "Salaries of Individuals Hired under Contract of Service", etc.
    val requirementNumber: Int,
    val title: String,
    val isChecked: Boolean = false,
    val note: String = ""
)
