package com.example.data

import com.example.model.VoucherWithChecklist
import kotlinx.coroutines.flow.Flow

class VoucherRepository(private val dao: VoucherDao) {
    val allVouchers: Flow<List<VoucherWithChecklist>> = dao.getAllVouchersWithChecklist()
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = dao.getUnreadNotificationCount()
    val allAuditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()

    fun getVoucherById(id: Long): Flow<VoucherWithChecklist?> = dao.getVoucherWithChecklistById(id)
    fun getAuditLogsForVoucher(voucherId: Long): Flow<List<AuditLogEntity>> = dao.getAuditLogsForVoucher(voucherId)

    suspend fun insertVoucherWithChecklist(
        voucher: VoucherEntity,
        checklistTitles: List<Pair<String, String>> // Pair<sectionGroup, title>
    ): Long {
        val voucherId = dao.insertVoucher(voucher)
        val checklistEntities = checklistTitles.mapIndexed { index, pair ->
            ChecklistItemEntity(
                voucherId = voucherId,
                sectionGroup = pair.first,
                requirementNumber = index + 1,
                title = pair.second,
                isChecked = false
            )
        }
        if (checklistEntities.isNotEmpty()) {
            dao.insertChecklistItems(checklistEntities)
        }
        dao.insertAuditLog(
            AuditLogEntity(
                voucherId = voucherId,
                voucherNo = voucher.voucherNo,
                action = "CREATED",
                performedByRole = "Originating Unit",
                performedByName = voucher.payeeName,
                details = "Voucher Route Slip created for ${voucher.payeeName} (₱${voucher.amount})"
            )
        )
        return voucherId
    }

    suspend fun updateVoucher(voucher: VoucherEntity, roleName: String, actionName: String, details: String) {
        dao.updateVoucher(voucher)
        dao.insertAuditLog(
            AuditLogEntity(
                voucherId = voucher.id,
                voucherNo = voucher.voucherNo,
                action = actionName,
                performedByRole = roleName,
                performedByName = roleName,
                details = details
            )
        )
    }

    suspend fun updateChecklistItem(item: ChecklistItemEntity, roleName: String, voucherNo: String) {
        dao.updateChecklistItem(item)
        val statusText = if (item.isChecked) "Verified / Completed" else "Unverified / Incomplete"
        dao.insertAuditLog(
            AuditLogEntity(
                voucherId = item.voucherId,
                voucherNo = voucherNo,
                action = "CHECKLIST_UPDATED",
                performedByRole = roleName,
                performedByName = roleName,
                details = "Checklist #${item.requirementNumber} (${item.title}) marked as $statusText"
            )
        )
    }

    suspend fun deleteVoucher(id: Long) {
        dao.deleteVoucherById(id)
    }

    suspend fun markNotificationAsRead(id: Long) = dao.markNotificationAsRead(id)
    suspend fun markAllNotificationsAsRead() = dao.markAllNotificationsAsRead()
    suspend fun deleteNotification(id: Long) = dao.deleteNotification(id)

    suspend fun addNotification(notification: NotificationEntity) {
        dao.insertNotification(notification)
    }
}
