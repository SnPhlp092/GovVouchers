package com.example.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.ChecklistItemEntity
import com.example.data.VoucherEntity

data class VoucherWithChecklist(
    @Embedded val voucher: VoucherEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "voucherId"
    )
    val checklistItems: List<ChecklistItemEntity> = emptyList()
)
