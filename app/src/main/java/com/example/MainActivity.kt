package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.UserRole
import com.example.ui.VoucherViewModel
import com.example.ui.components.*
import com.example.ui.screens.AuditLogsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpirationMonitorScreen
import com.example.ui.screens.NotificationCenterScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: VoucherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: VoucherViewModel) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val allVouchers by viewModel.allVouchers.collectAsStateWithLifecycle()
    val filteredVouchers by viewModel.filteredVouchers.collectAsStateWithLifecycle()
    val selectedVoucher by viewModel.selectedVoucher.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val budgetStats by viewModel.budgetStats.collectAsStateWithLifecycle()
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val auditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterSection by viewModel.filterSection.collectAsStateWithLifecycle()

    var showRoleSheet by remember { mutableStateOf(false) }
    var showNewVoucherDialog by remember { mutableStateOf(false) }
    var showPrintPreviewDialog by remember { mutableStateOf(false) }
    var showRemarksDialog by remember { mutableStateOf(false) }
    var showReturnDialog by remember { mutableStateOf(false) }
    var showExtendDialog by remember { mutableStateOf(false) }
    var activeSignSection by remember { mutableStateOf<String?>(null) }

    // Dialogs state tracking
    var extendVoucherTarget by remember { mutableStateOf<Triple<Long, String, Long>?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GovVoucher",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1E466F)
                            ) {
                                Text(
                                    text = "ROUTE SLIP",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF90CAF9),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        // Current role subtitle clickable
                        Text(
                            text = "${currentRole.title} (${currentRole.defaultName})",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.clickable { showRoleSheet = true }
                        )
                    }
                },
                actions = {
                    // Role switch button
                    IconButton(
                        onClick = { showRoleSheet = true },
                        modifier = Modifier.testTag("action_switch_role")
                    ) {
                        Icon(
                            Icons.Default.ManageAccounts,
                            contentDescription = "Switch Role",
                            tint = Color.White
                        )
                    }

                    // Notification bell with badge
                    IconButton(
                        onClick = { viewModel.setTab(3) },
                        modifier = Modifier.testTag("action_notifications")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(containerColor = GovUrgentRed) {
                                        Text("$unreadNotificationsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GovNavyPrimary)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Route Slip") },
                    label = { Text("Route Slip", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_route_slip")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Default.HourglassBottom, contentDescription = "Expirations") },
                    label = { Text("Expirations", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_expirations")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(containerColor = GovUrgentRed) {
                                        Text("$unreadNotificationsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                        }
                    },
                    label = { Text("Alerts", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_alerts")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { viewModel.setTab(4) },
                    icon = { Icon(Icons.Default.HistoryEdu, contentDescription = "Audit Trail") },
                    label = { Text("COA Audit", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_audit")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 || selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showNewVoucherDialog = true },
                    containerColor = GovNavyPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_new_voucher")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Voucher Slip")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    // ROUTE SLIP MAIN VIEW
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Quick Voucher Horizontal Selector & Search
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "VOUCHERS:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextMuted
                                )

                                allVouchers.forEach { item ->
                                    val isSelected = selectedVoucher?.voucher?.id == item.voucher.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.selectVoucher(item.voucher.id) },
                                        label = {
                                            Text(
                                                text = "${item.voucher.voucherNo} • ${item.voucher.payeeName.take(12)}",
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        modifier = Modifier.testTag("chip_voucher_${item.voucher.id}")
                                    )
                                }
                            }
                        }

                        if (selectedVoucher != null) {
                            VoucherRouteSlipView(
                                voucherWithChecklist = selectedVoucher!!,
                                currentRole = currentRole,
                                onSignSection = { sectionKey ->
                                    activeSignSection = sectionKey
                                },
                                onToggleChecklist = { item, isChecked ->
                                    viewModel.toggleChecklistItem(item, isChecked, selectedVoucher!!.voucher.voucherNo)
                                },
                                onEditRemarks = { showRemarksDialog = true },
                                onReturnVoucher = { showReturnDialog = true },
                                onExtendExpiration = {
                                    val v = selectedVoucher!!.voucher
                                    extendVoucherTarget = Triple(v.id, v.voucherNo, v.expirationDate)
                                    showExtendDialog = true
                                },
                                onExportPrint = { showPrintPreviewDialog = true }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
                1 -> {
                    DashboardScreen(
                        currentRole = currentRole,
                        vouchers = allVouchers,
                        budgetStats = budgetStats,
                        onSwitchRole = { showRoleSheet = true },
                        onNavigateToRouteSlip = { id ->
                            viewModel.selectVoucher(id)
                            viewModel.setTab(0)
                        },
                        onNavigateToExpirations = { viewModel.setTab(2) },
                        onNavigateToNotifications = { viewModel.setTab(3) },
                        onTriggerScan = { viewModel.runAutomatedMonitoringScan() }
                    )
                }
                2 -> {
                    ExpirationMonitorScreen(
                        vouchers = allVouchers,
                        onSelectVoucher = { id ->
                            viewModel.selectVoucher(id)
                            viewModel.setTab(0)
                        },
                        onExtendExpiration = { id, no, exp ->
                            extendVoucherTarget = Triple(id, no, exp)
                            showExtendDialog = true
                        }
                    )
                }
                3 -> {
                    NotificationCenterScreen(
                        notifications = notifications,
                        onSelectVoucher = { id ->
                            viewModel.selectVoucher(id)
                            viewModel.setTab(0)
                        },
                        onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
                        onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
                        onDeleteNotification = { id -> viewModel.deleteNotification(id) },
                        onTriggerScan = { viewModel.runAutomatedMonitoringScan() }
                    )
                }
                4 -> {
                    AuditLogsScreen(
                        auditLogs = auditLogs,
                        onSelectVoucher = { id ->
                            viewModel.selectVoucher(id)
                            viewModel.setTab(0)
                        }
                    )
                }
            }
        }
    }

    // Role Switcher Bottom Sheet
    if (showRoleSheet) {
        RoleSwitcherBottomSheet(
            currentRole = currentRole,
            onRoleSelected = { role -> viewModel.setRole(role) },
            onDismiss = { showRoleSheet = false }
        )
    }

    // New Voucher Dialog
    if (showNewVoucherDialog) {
        NewVoucherDialog(
            onDismiss = { showNewVoucherDialog = false },
            onCreate = { no, name, nature, period, amount, cat, fund, days, checklist, remarks ->
                viewModel.createVoucher(no, name, nature, period, amount, cat, fund, days, checklist, remarks)
                showNewVoucherDialog = false
            }
        )
    }

    // Print / Export Preview Dialog
    if (showPrintPreviewDialog && selectedVoucher != null) {
        PrintPreviewDialog(
            voucherWithChecklist = selectedVoucher!!,
            onDismiss = { showPrintPreviewDialog = false }
        )
    }

    // Edit Remarks Dialog
    if (showRemarksDialog && selectedVoucher != null) {
        EditRemarksDialog(
            initialRemarks = selectedVoucher!!.voucher.remarks,
            onDismiss = { showRemarksDialog = false },
            onConfirm = { remarks ->
                viewModel.updateRemarks(selectedVoucher!!.voucher.id, remarks)
                showRemarksDialog = false
            }
        )
    }

    // Return Voucher Dialog
    if (showReturnDialog && selectedVoucher != null) {
        ReturnVoucherDialog(
            voucherNo = selectedVoucher!!.voucher.voucherNo,
            onDismiss = { showReturnDialog = false },
            onConfirm = { reason, date ->
                viewModel.returnVoucher(selectedVoucher!!.voucher.id, reason, date)
                showReturnDialog = false
            }
        )
    }

    // Extend Expiration Dialog
    if (showExtendDialog && extendVoucherTarget != null) {
        val target = extendVoucherTarget!!
        ExtendExpirationDialog(
            voucherNo = target.second,
            currentExpirationDate = target.third,
            onDismiss = {
                showExtendDialog = false
                extendVoucherTarget = null
            },
            onConfirm = { additionalDays, reason ->
                viewModel.extendExpiration(target.first, additionalDays, reason)
                showExtendDialog = false
                extendVoucherTarget = null
            }
        )
    }

    // Section Sign-off Dialog
    if (activeSignSection != null && selectedVoucher != null) {
        val sectionKey = activeSignSection!!
        SignOffDialog(
            sectionKey = sectionKey,
            voucher = selectedVoucher!!,
            currentRole = currentRole,
            onDismiss = { activeSignSection = null },
            onConfirm = { initial, date, extra ->
                val vId = selectedVoucher!!.voucher.id
                when (sectionKey) {
                    "BUDGET_RECEIVING" -> viewModel.signBudgetReceiving(vId, initial, date)
                    "BUDGET_OFFICER" -> {
                        val amount = extra.toDoubleOrNull() ?: selectedVoucher!!.voucher.amount
                        viewModel.signBudgetOfficer(vId, initial, date, amount)
                    }
                    "ACCOUNTING_RECEIVING" -> viewModel.signAccountingReceiving(vId, initial, date)
                    "ACCOUNTING_PROCESSING" -> viewModel.signAccountingProcessing(vId, initial, date)
                    "ACCOUNTING_INDEX" -> viewModel.signAccountingIndex(vId, initial, date)
                    "ACCOUNTING_RELEASED" -> viewModel.signAccountingRelease(vId, date, extra)
                    "ACCOUNTANT", "ORDER_OF_PAYMENT" -> viewModel.signAccountantApproval(vId, initial, date, extra)
                }
                activeSignSection = null
            }
        )
    }
}
