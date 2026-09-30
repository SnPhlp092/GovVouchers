package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AuditLogEntity
import com.example.data.ChecklistItemEntity
import com.example.data.NotificationEntity
import com.example.data.VoucherEntity
import com.example.data.VoucherRepository
import com.example.model.ExpirationHelper
import com.example.model.ExpirationUrgency
import com.example.model.UserRole
import com.example.model.VoucherWithChecklist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BudgetStats(
    val totalAllotment: Double = 5000000.0, // ₱5.0M allocation
    val totalObligated: Double = 0.0,
    val totalDisbursed: Double = 0.0,
    val pendingProcessing: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val utilizationPercentage: Float = 0f
)

data class SectionBottleneckInfo(
    val sectionName: String,
    val pendingCount: Int,
    val bottleneckCount: Int, // Delayed > 48 hours
    val averageTurnaroundHours: Float
)

class VoucherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VoucherRepository

    val currentRole = MutableStateFlow(UserRole.ADMIN_AUDITOR)
    val searchQuery = MutableStateFlow("")
    val filterSection = MutableStateFlow("ALL") // "ALL", "BUDGET", "ACCOUNTING", "COMPLETED", "EXPIRING_SOON", "BOTTLENECK"
    val selectedVoucherId = MutableStateFlow<Long?>(null)
    val selectedTab = MutableStateFlow(0) // 0: Route Slips, 1: Dashboard, 2: Expiration Monitor, 3: Notifications, 4: Audit Logs

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = VoucherRepository(database.voucherDao())
    }

    val allVouchers: StateFlow<List<VoucherWithChecklist>> = repository.allVouchers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Vouchers
    val filteredVouchers: StateFlow<List<VoucherWithChecklist>> = combine(
        allVouchers,
        searchQuery,
        filterSection,
        currentRole
    ) { list, query, filter, role ->
        list.filter { item ->
            val v = item.voucher
            val matchesQuery = query.isBlank() ||
                v.voucherNo.contains(query, ignoreCase = true) ||
                v.payeeName.contains(query, ignoreCase = true) ||
                v.natureOfExpenses.contains(query, ignoreCase = true) ||
                v.orderOfPaymentLddapNo.contains(query, ignoreCase = true)

            val now = System.currentTimeMillis()
            val isExpiringSoon = (v.expirationDate - now) <= (3 * 86_400_000L) && v.status != "PAID_COMPLETED"
            val isBottleneck = (now - v.sectionEntryTimestamp) > (48 * 3600_000L) && v.status != "PAID_COMPLETED"

            val matchesFilter = when (filter) {
                "ALL" -> true
                "BUDGET" -> v.currentSection == "BUDGET"
                "ACCOUNTING" -> v.currentSection == "ACCOUNTING"
                "COMPLETED" -> v.currentSection == "COMPLETED"
                "RETURNED" -> v.currentSection == "RETURNED"
                "EXPIRING_SOON" -> isExpiringSoon
                "BOTTLENECK" -> isBottleneck
                else -> true
            }

            // If user is Claimant, only show vouchers they are related to or all if viewing as demo
            val matchesRole = if (role == UserRole.CLAIMANT) {
                v.payeeName.contains(role.defaultName, ignoreCase = true) || true // Allow switching
            } else {
                true
            }

            matchesQuery && matchesFilter && matchesRole
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected voucher details
    val selectedVoucher: StateFlow<VoucherWithChecklist?> = combine(
        allVouchers,
        selectedVoucherId
    ) { list, id ->
        if (id == null) {
            list.firstOrNull() // Default to first voucher (Joyce L. Galea)
        } else {
            list.find { it.voucher.id == id } ?: list.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Budget & Utilization calculations
    val budgetStats: StateFlow<BudgetStats> = allVouchers.combine(currentRole) { vouchers, _ ->
        val totalAllotment = 5000000.0 // ₱5,000,000 baseline budget
        var obligated = 0.0
        var disbursed = 0.0
        var pending = 0.0

        for (item in vouchers) {
            val v = item.voucher
            when (v.status) {
                "PAID_COMPLETED" -> {
                    disbursed += v.amount
                    obligated += v.amount
                }
                "RETURNED" -> {
                    // Not obligated
                }
                else -> {
                    pending += v.amount
                    if (v.budgetCertifiedAmount > 0) {
                        obligated += v.budgetCertifiedAmount
                    }
                }
            }
        }

        val remaining = (totalAllotment - obligated).coerceAtLeast(0.0)
        val percent = if (totalAllotment > 0) ((obligated / totalAllotment) * 100).toFloat() else 0f
        BudgetStats(
            totalAllotment = totalAllotment,
            totalObligated = obligated,
            totalDisbursed = disbursed,
            pendingProcessing = pending,
            remainingBalance = remaining,
            utilizationPercentage = percent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetStats())

    fun setRole(role: UserRole) {
        currentRole.value = role
    }

    fun selectVoucher(id: Long?) {
        selectedVoucherId.value = id
    }

    fun setTab(index: Int) {
        selectedTab.value = index
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setFilterSection(filter: String) {
        filterSection.value = filter
    }

    // Role Actions on the Route Slip Form
    fun signBudgetReceiving(voucherId: Long, initial: String, date: String) {
        viewModelScope.launch {
            val current = allVouchers.value.find { it.voucher.id == voucherId }?.voucher ?: return@launch
            val updated = current.copy(
                budgetReceivingClerkInitial = initial,
                budgetReceivingDate = date,
                status = "BUDGET_RECEIVING",
                currentSection = "BUDGET",
                sectionEntryTimestamp = System.currentTimeMillis()
            )
            repository.updateVoucher(
                updated,
                currentRole.value.title,
                "BUDGET_RECEIVED",
                "Receiving Clerk ($initial) acknowledged voucher slip on $date"
            )
        }
    }

    fun signBudgetOfficer(voucherId: Long, initial: String, date: String, certifiedAmount: Double) {
        viewModelScope.launch {
            val current = allVouchers.value.find { it.voucher.id == voucherId }?.voucher ?: return@launch
            val updated = current.copy(
                budgetOfficerInitial = initial,
                budgetOfficerDate = date,
                budgetCertifiedAmount = certifiedAmount,
                status = "BUDGET_CERTIFIED",
                currentSection = "ACCOUNTING", // Routes to Accounting Section!
                sectionEntryTimestamp = System.currentTimeMillis()
            )
            repository.updateVoucher(
                updated,
                currentRole.value.title,
                "BUDGET_OFFICER_SIGN",
                "Budget Officer ($initial) certified funds available ₱${String.format(Locale.US, "%,.2f", certifiedAmount)}. Routed to Accounting."
            )
            repository.addNotification(
                NotificationEntity(
                    voucherId = voucherId,
                    voucherNo = current.voucherNo,
                    title = "Voucher Routed to Accounting",
                    message = "Voucher ${current.voucherNo} certified by Budget Section and transmitted to Accounting.",
                    type = "STATUS_CHANGE",
                    urgency = "LOW"
                )
            )
        }
    }

    fun signAccountingReceiving(voucherId: Long, initial: String, date: String) {
        viewModelScope.launch {
            val current = allVouchers.value.find { it.voucher.id == voucherId }?.voucher ?: return@launch
            val updated = current.copy(
                accountingReceivingClerkInitial = initial,
                accountingReceivingDate = date,
                status = "ACCOUNTING_RECEIVING",
                currentSection = "ACCOUNTING",
                sectionEntryTimestamp = System.currentTimeMillis()
            )
            repository.updateVoucher(
                updated,
                currentRole.value.title,
                "ACCOUNTING_RECEIVED",
                "Accounting Receiving Clerk ($initial) recorded entry on $date"
            )
        }
    }

    fun signAccountingProcessing(voucherId: Long, initial: String, date: String) {
        viewModelScope.launch {
            val current = allVouchers.value.find { it.voucher.id == voucherId }?.voucher ?: return@launch
            val updated = current.copy(
                accountingProcessingInitial = initial,
                accountingProcessingDate = date,
                status = "ACCOUNTING_PROCESSING",
                currentSection = "ACCOUNTING",
                sectionEntryTimestamp = System.currentTimeMillis()
            )
            repository.updateVoucher(
                updated,
                currentRole.value.title,
                "ACCOUNTING_PROCESSING",
                "Processing clerk ($initial) completed pre-audit review on $date"
            )
        }
    }

    fun signAccountingIndex(voucherId: Long, initial: String, date: String) {
        viewModelScope.launch {
            val current = allVouchers.value.find { it.voucher.id == voucherId }?.voucher ?: return@launch
            val updated = current.copy(
                accountingIndexInitial = initial,
                accountingIndexDate = date,
                status = "ACCOUNTING_INDEX",
                currentSection = "ACCOUNTING",
                sectionEntryTimestamp = System.currentTimeMillis()
            )
            repository.updateVoucher(
                updated,
                currentRole.value.title,
                "ACCOUNTING_INDEXED",
                "Index clerk ($initial) entered into index of payments on $date"
            )
        }
    }

    fun signAccountingRelease(voucherId: Long, releasedDate: String, receivedBy: String) {
        viewModelScope.launch {
            val current = allVouchers.value.find { it.voucher.id == voucherId }?.voucher ?: return@launch
            val updated = current.copy(
                accountingReleasedDate = releasedDate,
                accountingReceivedBy = receivedBy,
                status = "RELEASED",
                currentSection = "CASHIER",
                sectionEntryTimestamp = System.currentTimeMillis()
            )
            repository.updateVoucher(
                updated,
                currentRole.value.title,
                "ACCOUNTING_RELEASED",
                "Released to $receivedBy on $releasedDate for payment processing"
            )
        }
    }

    fun signAccountantApproval(voucherId: Long, initial: String, date: String, lddapNo: String) {
        viewModelScope.launch {
            val current = allVouchers.value.find { it.voucher.id == voucherId }?.voucher ?: return@launch
            val isFinalPayment = lddapNo.isNotBlank()
            val updated = current.copy(
                accountantInitial = initial,
                accountantDate = date,
                orderOfPaymentLddapNo = lddapNo,
                status = if (isFinalPayment) "PAID_COMPLETED" else "ACCOUNTING_APPROVED",
                currentSection = if (isFinalPayment) "COMPLETED" else "CASHIER",
                sectionEntryTimestamp = System.currentTimeMillis()
            )
            repository.updateVoucher(
                updated,
                currentRole.value.title,
                if (isFinalPayment) "PAYMENT_ISSUED" else "ACCOUNTANT_APPROVED",
                "Accountant ($initial) signed off on $date with Order of Payment / LDDAP-ADA No: $lddapNo"
            )
            if (isFinalPayment) {
                repository.addNotification(
                    NotificationEntity(
                        voucherId = voucherId,
                        voucherNo = current.voucherNo,
                        title = "Payment Processed: ${current.voucherNo}",
                        message = "LDDAP-ADA $lddapNo issued for ${current.payeeName} (₱${String.format(Locale.US, "%,.2f", current.amount)}). Process completed.",
                        type = "STATUS_CHANGE",
                        urgency = "LOW"
                    )
                )
            }
        }
    }

    fun returnVoucher(voucherId: Long, reason: String, returnDate: String) {
        viewModelScope.launch {
            val current = allVouchers.value.find { it.voucher.id == voucherId }?.voucher ?: return@launch
            val updated = current.copy(
                accountingReturnedDate = returnDate,
                remarks = if (current.remarks.isBlank()) "Returned: $reason" else "${current.remarks}\n[Returned $returnDate]: $reason",
                status = "RETURNED",
                currentSection = "RETURNED",
                sectionEntryTimestamp = System.currentTimeMillis()
            )
            repository.updateVoucher(
                updated,
                currentRole.value.title,
                "RETURNED",
                "Voucher returned to claimant/originating unit: $reason"
            )
            repository.addNotification(
                NotificationEntity(
                    voucherId = voucherId,
                    voucherNo = current.voucherNo,
                    title = "Voucher Returned for Correction",
                    message = "Voucher ${current.voucherNo} returned: $reason",
                    type = "CHECKLIST",
                    urgency = "HIGH"
                )
            )
        }
    }

    fun updateRemarks(voucherId: Long, remarks: String) {
        viewModelScope.launch {
            val current = allVouchers.value.find { it.voucher.id == voucherId }?.voucher ?: return@launch
            val updated = current.copy(remarks = remarks)
            repository.updateVoucher(
                updated,
                currentRole.value.title,
                "REMARKS_UPDATED",
                "Remarks updated: ${remarks.take(60)}..."
            )
        }
    }

    fun toggleChecklistItem(item: ChecklistItemEntity, isChecked: Boolean, voucherNo: String) {
        viewModelScope.launch {
            val updated = item.copy(isChecked = isChecked)
            repository.updateChecklistItem(updated, currentRole.value.title, voucherNo)
        }
    }

    fun createVoucher(
        voucherNo: String,
        payeeName: String,
        natureOfExpenses: String,
        periodCovered: String,
        amount: Double,
        category: String,
        fundCluster: String,
        expirationDays: Int,
        selectedChecklist: List<Pair<String, String>>,
        remarks: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val expirationTimestamp = now + (expirationDays * 86_400_000L)
            val voucher = VoucherEntity(
                voucherNo = voucherNo,
                payeeName = payeeName,
                natureOfExpenses = natureOfExpenses,
                periodCovered = periodCovered,
                amount = amount,
                expirationDate = expirationTimestamp,
                category = category,
                fundCluster = fundCluster,
                status = "BUDGET_RECEIVING",
                currentSection = "BUDGET",
                sectionEntryTimestamp = now,
                remarks = remarks
            )
            val id = repository.insertVoucherWithChecklist(voucher, selectedChecklist)
            selectedVoucherId.value = id
            selectedTab.value = 0 // Show the newly created route slip!

            repository.addNotification(
                NotificationEntity(
                    voucherId = id,
                    voucherNo = voucherNo,
                    title = "New Voucher Route Slip Created",
                    message = "Route Slip $voucherNo for $payeeName (₱${String.format(Locale.US, "%,.2f", amount)}) registered in Budget Section.",
                    type = "STATUS_CHANGE",
                    urgency = "LOW"
                )
            )
        }
    }

    fun extendExpiration(voucherId: Long, additionalDays: Int, reason: String) {
        viewModelScope.launch {
            val current = allVouchers.value.find { it.voucher.id == voucherId }?.voucher ?: return@launch
            val newExp = current.expirationDate + (additionalDays * 86_400_000L)
            val updated = current.copy(
                expirationDate = newExp,
                remarks = if (current.remarks.isBlank()) "Extended by $additionalDays days: $reason" else "${current.remarks}\n[Expiration Extended by $additionalDays days]: $reason"
            )
            repository.updateVoucher(
                updated,
                currentRole.value.title,
                "EXPIRATION_EXTENDED",
                "Expiration extended by $additionalDays days. Reason: $reason"
            )
            repository.addNotification(
                NotificationEntity(
                    voucherId = voucherId,
                    voucherNo = current.voucherNo,
                    title = "Expiration Date Extended",
                    message = "Voucher ${current.voucherNo} expiration extended to ${ExpirationHelper.formatDate(newExp)}.",
                    type = "EXPIRATION",
                    urgency = "MEDIUM"
                )
            )
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    // Trigger an automated scan for impending expirations & bottlenecks
    fun runAutomatedMonitoringScan() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val vouchers = allVouchers.value
            for (item in vouchers) {
                val v = item.voucher
                if (v.status != "PAID_COMPLETED") {
                    val daysLeft = (v.expirationDate - now) / 86_400_000L
                    if (daysLeft in 0..2) {
                        repository.addNotification(
                            NotificationEntity(
                                voucherId = v.id,
                                voucherNo = v.voucherNo,
                                title = "Automated Expiration Alert",
                                message = "Voucher ${v.voucherNo} (${v.payeeName}) has only $daysLeft days remaining before expiration!",
                                type = "EXPIRATION",
                                urgency = "HIGH"
                            )
                        )
                    }
                    val hoursAtStation = (now - v.sectionEntryTimestamp) / 3600_000L
                    if (hoursAtStation >= 48) {
                        repository.addNotification(
                            NotificationEntity(
                                voucherId = v.id,
                                voucherNo = v.voucherNo,
                                title = "SLA Breach / Bottleneck Alert",
                                message = "Voucher ${v.voucherNo} has been queued at ${v.currentSection} section for $hoursAtStation hours (>48h limit).",
                                type = "BOTTLENECK",
                                urgency = "HIGH"
                            )
                        )
                    }
                }
            }
        }
    }
}
