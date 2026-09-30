package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.model.VoucherWithChecklist
import kotlinx.coroutines.flow.Flow

@Dao
interface VoucherDao {
    @Transaction
    @Query("SELECT * FROM vouchers ORDER BY createdTimestamp DESC")
    fun getAllVouchersWithChecklist(): Flow<List<VoucherWithChecklist>>

    @Transaction
    @Query("SELECT * FROM vouchers WHERE id = :id")
    fun getVoucherWithChecklistById(id: Long): Flow<VoucherWithChecklist?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: VoucherEntity): Long

    @Update
    suspend fun updateVoucher(voucher: VoucherEntity)

    @Query("DELETE FROM vouchers WHERE id = :id")
    suspend fun deleteVoucherById(id: Long)

    // Checklist operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistItemEntity>)

    @Update
    suspend fun updateChecklistItem(item: ChecklistItemEntity)

    @Query("SELECT * FROM checklist_items WHERE voucherId = :voucherId ORDER BY id ASC")
    fun getChecklistForVoucher(voucherId: Long): Flow<List<ChecklistItemEntity>>

    // Audit logs
    @Query("SELECT * FROM audit_logs WHERE voucherId = :voucherId ORDER BY timestamp DESC")
    fun getAuditLogsForVoucher(voucherId: Long): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadNotificationCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)
}
