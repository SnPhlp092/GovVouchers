package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vouchers")
data class VoucherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val voucherNo: String,
    val payeeName: String,
    val natureOfExpenses: String,
    val periodCovered: String,
    val amount: Double,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val expirationDate: Long, // Epoch timestamp in ms
    val category: String = "Salaries of COS Personnel",
    val fundCluster: String = "Fund Cluster 01 - Regular Agency Fund",
    val status: String = "BUDGET_RECEIVING",
    val currentSection: String = "BUDGET", // "BUDGET", "ACCOUNTING", "CASHIER", "COMPLETED", "RETURNED"
    val sectionEntryTimestamp: Long = System.currentTimeMillis(),

    // BUDGET SECTION Sign-offs
    val budgetReceivingClerkInitial: String = "",
    val budgetReceivingDate: String = "",
    val budgetOfficerInitial: String = "",
    val budgetOfficerDate: String = "",
    val budgetCertifiedAmount: Double = 0.0,

    // ACCOUNTING SECTION Sign-offs
    val accountingReceivingClerkInitial: String = "",
    val accountingReceivingDate: String = "",
    val accountingProcessingInitial: String = "",
    val accountingProcessingDate: String = "",
    val accountingIndexInitial: String = "",
    val accountingIndexDate: String = "",
    val accountingReleasedDate: String = "",
    val accountingReceivedBy: String = "",
    val accountingReturnedDate: String = "",
    val accountantInitial: String = "",
    val accountantDate: String = "",
    val orderOfPaymentLddapNo: String = "",

    // Remarks
    val remarks: String = ""
)
