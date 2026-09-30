package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        VoucherEntity::class,
        ChecklistItemEntity::class,
        AuditLogEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun voucherDao(): VoucherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gov_voucher_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.voucherDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: VoucherDao) {
            val now = System.currentTimeMillis()
            val dayMs = 86_400_000L

            // 1. Joyce L. Galea (Exact from image.png!)
            val voucher1 = VoucherEntity(
                voucherNo = "26-809-09I",
                payeeName = "JOYCE L. GALEA",
                natureOfExpenses = "To payment for cash advance of salaries of Contract of Service personnel",
                periodCovered = "September 16–30, 2026",
                amount = 218227.11,
                createdTimestamp = now - (3 * dayMs),
                expirationDate = now + (4 * dayMs), // Expiring in 4 days!
                category = "Salaries of COS Personnel",
                fundCluster = "Fund Cluster 01 - Regular Agency Fund",
                status = "ACCOUNTING_PROCESSING",
                currentSection = "ACCOUNTING",
                sectionEntryTimestamp = now - (2 * dayMs + 3600_000L * 4), // At accounting for 52 hours (Bottleneck!)
                budgetReceivingClerkInitial = "RC-1",
                budgetReceivingDate = "Sep 17, 2026",
                budgetOfficerInitial = "BO-Santos",
                budgetOfficerDate = "Sep 18, 2026",
                budgetCertifiedAmount = 218227.11,
                accountingReceivingClerkInitial = "AC-Cruz",
                accountingReceivingDate = "Sep 19, 2026",
                accountingProcessingInitial = "PR-Mendoza",
                accountingProcessingDate = "Sep 20, 2026",
                accountingIndexInitial = "",
                accountingIndexDate = "",
                accountingReleasedDate = "",
                accountingReceivedBy = "",
                accountingReturnedDate = "",
                accountantInitial = "",
                accountantDate = "",
                orderOfPaymentLddapNo = "",
                remarks = "Chargeable against Personnel Services (PS) / Sub-allotment Advice SAA-2026-09-041. Subject to submission of complete Daily Time Record."
            )
            val v1Id = dao.insertVoucher(voucher1)

            // General requirements for V1
            dao.insertChecklistItems(
                listOf(
                    ChecklistItemEntity(voucherId = v1Id, sectionGroup = "General Requirements", requirementNumber = 1, title = "Disbursement Voucher", isChecked = true),
                    ChecklistItemEntity(voucherId = v1Id, sectionGroup = "General Requirements", requirementNumber = 2, title = "Obligation Request and Status (ORS)", isChecked = true),
                    ChecklistItemEntity(voucherId = v1Id, sectionGroup = "Salaries of Individuals Hired under Contract of Service", requirementNumber = 1, title = "Notarized Contract", isChecked = true),
                    ChecklistItemEntity(voucherId = v1Id, sectionGroup = "Salaries of Individuals Hired under Contract of Service", requirementNumber = 2, title = "Duly Accomplished Daily Time Record (DTR)", isChecked = false, note = "Pending 3 branch submissions"),
                    ChecklistItemEntity(voucherId = v1Id, sectionGroup = "Salaries of Individuals Hired under Contract of Service", requirementNumber = 3, title = "Duly Approved Accomplishment Report", isChecked = true),
                    ChecklistItemEntity(voucherId = v1Id, sectionGroup = "Salaries of Individuals Hired under Contract of Service", requirementNumber = 4, title = "Travel Order, if applicable", isChecked = true),
                    ChecklistItemEntity(voucherId = v1Id, sectionGroup = "Salaries of Individuals Hired under Contract of Service", requirementNumber = 5, title = "Certificate of Completion and Acceptance", isChecked = true),
                    ChecklistItemEntity(voucherId = v1Id, sectionGroup = "Salaries of Individuals Hired under Contract of Service", requirementNumber = 6, title = "S.O, if applicable", isChecked = true),
                    ChecklistItemEntity(voucherId = v1Id, sectionGroup = "Salaries of Individuals Hired under Contract of Service", requirementNumber = 7, title = "MCLL Certification, if lawyer", isChecked = false),
                    ChecklistItemEntity(voucherId = v1Id, sectionGroup = "Salaries of Individuals Hired under Contract of Service", requirementNumber = 8, title = "Written Concurrence from COA, if lawyer", isChecked = false),
                    ChecklistItemEntity(voucherId = v1Id, sectionGroup = "Salaries of Individuals Hired under Contract of Service", requirementNumber = 9, title = "Deputation of OSG, if lawyer", isChecked = false),
                    ChecklistItemEntity(voucherId = v1Id, sectionGroup = "Salaries of Individuals Hired under Contract of Service", requirementNumber = 10, title = "Acquiescence from OSG, if lawyer", isChecked = false),
                    ChecklistItemEntity(voucherId = v1Id, sectionGroup = "Salaries of Individuals Hired under Contract of Service", requirementNumber = 11, title = "Certificate of Availability of Funds", isChecked = true)
                )
            )

            dao.insertAuditLog(
                AuditLogEntity(
                    voucherId = v1Id,
                    voucherNo = "26-809-09I",
                    action = "BUDGET_OFFICER_SIGN",
                    performedByRole = "Budget Officer / Clerk",
                    performedByName = "Ricardo Santos",
                    timestamp = now - (2 * dayMs),
                    details = "Certified ORS availability in amount ₱218,227.11"
                )
            )
            dao.insertAuditLog(
                AuditLogEntity(
                    voucherId = v1Id,
                    voucherNo = "26-809-09I",
                    action = "ACCOUNTING_RECEIVED",
                    performedByRole = "Accounting Section",
                    performedByName = "Maria Cruz",
                    timestamp = now - (1 * dayMs + 3600_000L * 10),
                    details = "Received by Accounting section for pre-audit and processing"
                )
            )

            // 2. Second Voucher: Hardware / IT Infrastructure Procurement (Urgent Expiration in 1 day!)
            val voucher2 = VoucherEntity(
                voucherNo = "26-810-15B",
                payeeName = "TECHWORKS SOLUTIONS CORP.",
                natureOfExpenses = "Procurement of Server Infrastructure & Backup Power Supply for Disaster Recovery",
                periodCovered = "September 01–30, 2026",
                amount = 845600.00,
                createdTimestamp = now - (6 * dayMs),
                expirationDate = now + (1 * dayMs), // 1 day left!
                category = "Capital Outlay & Equipment",
                fundCluster = "Fund Cluster 02 - Foreign Assisted Projects",
                status = "BUDGET_RECEIVING",
                currentSection = "BUDGET",
                sectionEntryTimestamp = now - (1 * dayMs),
                budgetReceivingClerkInitial = "RC-1",
                budgetReceivingDate = "Sep 28, 2026",
                budgetOfficerInitial = "",
                budgetOfficerDate = "",
                budgetCertifiedAmount = 845600.00,
                remarks = "Purchase Order PO-2026-08-991 attached with Inspection and Acceptance Report."
            )
            val v2Id = dao.insertVoucher(voucher2)
            dao.insertChecklistItems(
                listOf(
                    ChecklistItemEntity(voucherId = v2Id, sectionGroup = "General Requirements", requirementNumber = 1, title = "Disbursement Voucher", isChecked = true),
                    ChecklistItemEntity(voucherId = v2Id, sectionGroup = "General Requirements", requirementNumber = 2, title = "Obligation Request and Status (ORS)", isChecked = true),
                    ChecklistItemEntity(voucherId = v2Id, sectionGroup = "Procurement Documents", requirementNumber = 1, title = "Purchase Request & Approved Budget for Contract", isChecked = true),
                    ChecklistItemEntity(voucherId = v2Id, sectionGroup = "Procurement Documents", requirementNumber = 2, title = "BAC Resolution of Award & Notice to Proceed", isChecked = true),
                    ChecklistItemEntity(voucherId = v2Id, sectionGroup = "Procurement Documents", requirementNumber = 3, title = "Inspection and Acceptance Report (IAR)", isChecked = true),
                    ChecklistItemEntity(voucherId = v2Id, sectionGroup = "Procurement Documents", requirementNumber = 4, title = "Certificate of Availability of Funds", isChecked = true)
                )
            )

            // 3. Third Voucher: Completed / Paid with LDDAP-ADA No.
            val voucher3 = VoucherEntity(
                voucherNo = "26-795-02A",
                payeeName = "MANILA ELECTRIC COMPANY",
                natureOfExpenses = "Payment of Electric Utility Consumption for Central Office Building",
                periodCovered = "August 15 – September 15, 2026",
                amount = 312540.80,
                createdTimestamp = now - (12 * dayMs),
                expirationDate = now + (18 * dayMs),
                category = "Utility Expenses",
                fundCluster = "Fund Cluster 01 - Regular Agency Fund",
                status = "PAID_COMPLETED",
                currentSection = "COMPLETED",
                sectionEntryTimestamp = now - (3 * dayMs),
                budgetReceivingClerkInitial = "RC-1",
                budgetReceivingDate = "Sep 18, 2026",
                budgetOfficerInitial = "BO-Santos",
                budgetOfficerDate = "Sep 19, 2026",
                budgetCertifiedAmount = 312540.80,
                accountingReceivingClerkInitial = "AC-Cruz",
                accountingReceivingDate = "Sep 20, 2026",
                accountingProcessingInitial = "PR-Mendoza",
                accountingProcessingDate = "Sep 21, 2026",
                accountingIndexInitial = "IX-Tan",
                accountingIndexDate = "Sep 22, 2026",
                accountingReleasedDate = "Sep 24, 2026",
                accountingReceivedBy = "Cashier-Division",
                accountantInitial = "ACC-Chief",
                accountantDate = "Sep 23, 2026",
                orderOfPaymentLddapNo = "LDDAP-ADA-2026-09-0412",
                remarks = "Paid via Electronic Modified Disbursement System (eMDS). OR #9928172 filed."
            )
            val v3Id = dao.insertVoucher(voucher3)
            dao.insertChecklistItems(
                listOf(
                    ChecklistItemEntity(voucherId = v3Id, sectionGroup = "General Requirements", requirementNumber = 1, title = "Disbursement Voucher", isChecked = true),
                    ChecklistItemEntity(voucherId = v3Id, sectionGroup = "General Requirements", requirementNumber = 2, title = "Obligation Request and Status (ORS)", isChecked = true),
                    ChecklistItemEntity(voucherId = v3Id, sectionGroup = "Utility Requirements", requirementNumber = 1, title = "Original Billing Statement", isChecked = true),
                    ChecklistItemEntity(voucherId = v3Id, sectionGroup = "Utility Requirements", requirementNumber = 2, title = "Certificate of Meter Reading", isChecked = true)
                )
            )

            // 4. Fourth Voucher: Expired Voucher needing re-obligation
            val voucher4 = VoucherEntity(
                voucherNo = "26-750-11E",
                payeeName = "ENGR. ROLANDO B. CASTILLO",
                natureOfExpenses = "Reimbursement of Regional Field Monitoring & Evaluation Travel Expenses",
                periodCovered = "August 01–15, 2026",
                amount = 45890.00,
                createdTimestamp = now - (35 * dayMs),
                expirationDate = now - (5 * dayMs), // Expired 5 days ago!
                category = "Travel Reimbursement",
                fundCluster = "Fund Cluster 01 - Regular Agency Fund",
                status = "RETURNED",
                currentSection = "RETURNED",
                sectionEntryTimestamp = now - (5 * dayMs),
                budgetReceivingClerkInitial = "RC-1",
                budgetReceivingDate = "Aug 20, 2026",
                budgetOfficerInitial = "BO-Santos",
                budgetOfficerDate = "Aug 21, 2026",
                budgetCertifiedAmount = 45890.00,
                accountingReceivingClerkInitial = "AC-Cruz",
                accountingReceivingDate = "Aug 25, 2026",
                accountingReturnedDate = "Sep 25, 2026",
                remarks = "Returned to claimant: Period exceeded 30-day travel liquidation policy under COA Circular 2012-004. Needs justification letter."
            )
            val v4Id = dao.insertVoucher(voucher4)
            dao.insertChecklistItems(
                listOf(
                    ChecklistItemEntity(voucherId = v4Id, sectionGroup = "General Requirements", requirementNumber = 1, title = "Disbursement Voucher", isChecked = true),
                    ChecklistItemEntity(voucherId = v4Id, sectionGroup = "General Requirements", requirementNumber = 2, title = "Obligation Request and Status (ORS)", isChecked = true),
                    ChecklistItemEntity(voucherId = v4Id, sectionGroup = "Travel Requirements", requirementNumber = 1, title = "Approved Travel Order", isChecked = true),
                    ChecklistItemEntity(voucherId = v4Id, sectionGroup = "Travel Requirements", requirementNumber = 2, title = "Certificate of Appearance", isChecked = false, note = "Missing certificate from Region IV office"),
                    ChecklistItemEntity(voucherId = v4Id, sectionGroup = "Travel Requirements", requirementNumber = 3, title = "Official Receipts / Boarding Passes", isChecked = true)
                )
            )

            // Notifications
            dao.insertNotification(
                NotificationEntity(
                    voucherId = v1Id,
                    voucherNo = "26-809-09I",
                    title = "Bottleneck Alert: Accounting Processing",
                    message = "Voucher 26-809-09I (Joyce L. Galea) has been at Accounting Processing for over 48 hours.",
                    type = "BOTTLENECK",
                    urgency = "HIGH",
                    timestamp = now - (3600_000L * 4)
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    voucherId = v2Id,
                    voucherNo = "26-810-15B",
                    title = "Critical Expiration Notice",
                    message = "Voucher 26-810-15B (TechWorks Solutions) expires in less than 24 hours (Oct 01, 2026). Action required.",
                    type = "EXPIRATION",
                    urgency = "HIGH",
                    timestamp = now - (3600_000L * 2)
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    voucherId = v1Id,
                    voucherNo = "26-809-09I",
                    title = "Checklist Incomplete: DTR Missing",
                    message = "Duly Accomplished Daily Time Record (DTR) unchecked for COS salary disbursement.",
                    type = "CHECKLIST",
                    urgency = "MEDIUM",
                    timestamp = now - (3600_000L * 14)
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    voucherId = v3Id,
                    voucherNo = "26-795-02A",
                    title = "Payment Advice Issued (LDDAP-ADA)",
                    message = "Voucher 26-795-02A (Meralco) completed with LDDAP-ADA-2026-09-0412.",
                    type = "STATUS_CHANGE",
                    urgency = "LOW",
                    timestamp = now - (dayMs * 2)
                )
            )
        }
    }
}
