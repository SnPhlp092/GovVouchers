package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpirationHelper
import com.example.model.ExpirationUrgency
import com.example.model.UserRole
import com.example.model.VoucherWithChecklist
import com.example.ui.BudgetStats
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun DashboardScreen(
    currentRole: UserRole,
    vouchers: List<VoucherWithChecklist>,
    budgetStats: BudgetStats,
    onSwitchRole: () -> Unit,
    onNavigateToRouteSlip: (voucherId: Long) -> Unit,
    onNavigateToExpirations: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onTriggerScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val now = System.currentTimeMillis()
    val activeVouchers = vouchers.filter { it.voucher.status != "PAID_COMPLETED" && it.voucher.status != "RETURNED" }
    val expiredCount = vouchers.count {
        it.voucher.status != "PAID_COMPLETED" && (it.voucher.expirationDate < now)
    }
    val expiringCount = vouchers.count {
        it.voucher.status != "PAID_COMPLETED" &&
            (it.voucher.expirationDate >= now) &&
            ((it.voucher.expirationDate - now) <= (3 * 86_400_000L))
    }
    val bottleneckCount = vouchers.count {
        it.voucher.status != "PAID_COMPLETED" && ((now - it.voucher.sectionEntryTimestamp) > (48 * 3600_000L))
    }

    val completedCount = vouchers.count { it.voucher.status == "PAID_COMPLETED" }

    // Overall checklist compliance
    val totalChecklistItems = vouchers.sumOf { it.checklistItems.size }
    val checkedItems = vouchers.sumOf { it.checklistItems.count { item -> item.isChecked } }
    val complianceRate = if (totalChecklistItems > 0) ((checkedItems.toFloat() / totalChecklistItems) * 100).toInt() else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Role & Office Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = GovNavyPrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SECURE REPORTING DASHBOARD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF90CAF9),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentRole.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "${currentRole.defaultName} • ${currentRole.department}",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }

                Button(
                    onClick = onSwitchRole,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E466F)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_switch_role")
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Role", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Executive Metric Grid
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Active Vouchers",
                value = "${activeVouchers.size}",
                subtitle = "₱${String.format(Locale.US, "%,.0f", budgetStats.totalObligated)} committed",
                icon = Icons.Default.Description,
                iconColor = GovSecondaryBlue,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            MetricCard(
                title = "Bottlenecks (>48h)",
                value = "$bottleneckCount",
                subtitle = if (bottleneckCount > 0) "SLA Delay Alert" else "Flow optimal",
                icon = Icons.Default.HourglassTop,
                iconColor = if (bottleneckCount > 0) GovUrgentRed else GovSuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                title = "Expiring Soon",
                value = "$expiringCount",
                subtitle = if (expiredCount > 0) "$expiredCount lapsed/expired" else "Within 3 days",
                icon = Icons.Default.WarningAmber,
                iconColor = if (expiringCount > 0 || expiredCount > 0) GovUrgentRed else GovSuccessGreen,
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_expiring_soon"),
                onClick = onNavigateToExpirations
            )
            Spacer(modifier = Modifier.width(10.dp))
            MetricCard(
                title = "COA Compliance",
                value = "$complianceRate%",
                subtitle = "$checkedItems of $totalChecklistItems docs checked",
                icon = Icons.Default.FactCheck,
                iconColor = GovSuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // REAL-TIME BUDGET UTILIZATION GAUGE
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Real-Time Fund Utilization",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Fund Cluster 01 (General Fund FY 2026)",
                            fontSize = 11.sp,
                            color = SlateTextSecondary
                        )
                    }

                    Surface(
                        color = if (budgetStats.utilizationPercentage > 85f) GovUrgentContainer else GovSecondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", budgetStats.utilizationPercentage)}% Obligated",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (budgetStats.utilizationPercentage > 85f) GovUrgentRed else GovSecondaryBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Multi-stage progress bar
                val progressFrac = (budgetStats.utilizationPercentage / 100f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressFrac)
                            .fillMaxHeight()
                            .background(
                                if (budgetStats.utilizationPercentage > 85f) GovUrgentRed else GovNavyPrimary
                            )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Allotment", fontSize = 10.sp, color = SlateTextMuted)
                        Text(
                            "₱${String.format(Locale.US, "%,.2f", budgetStats.totalAllotment)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Obligated", fontSize = 10.sp, color = SlateTextMuted)
                        Text(
                            "₱${String.format(Locale.US, "%,.2f", budgetStats.totalObligated)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = GovSecondaryBlue
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Remaining Balance", fontSize = 10.sp, color = SlateTextMuted)
                        Text(
                            "₱${String.format(Locale.US, "%,.2f", budgetStats.remainingBalance)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = GovSuccessGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // REAL-TIME SECTION ROUTING & BOTTLENECK MONITOR
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Section Workflow Status & Turnaround",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Real-time queue tracking across government units",
                    fontSize = 11.sp,
                    color = SlateTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                val sections = listOf(
                    Triple("Budget Section", vouchers.count { it.voucher.currentSection == "BUDGET" }, "Certifying ORS & availability"),
                    Triple("Accounting Section", vouchers.count { it.voucher.currentSection == "ACCOUNTING" }, "Pre-audit, indexing & processing"),
                    Triple("Cashier / Payment", vouchers.count { it.voucher.currentSection == "CASHIER" }, "LDDAP-ADA & check release"),
                    Triple("Settled / Paid", completedCount, "Complete disbursement")
                )

                sections.forEach { (name, count, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (count > 0) GovNavyPrimary else Color(0xFFE2E8F0),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$count",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (count > 0) Color.White else SlateTextMuted
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(desc, fontSize = 10.5.sp, color = SlateTextSecondary)
                        }
                    }
                    if (name != "Settled / Paid") {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // QUICK ACTIONS & AUTOMATED MONITORING TRIGGER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onTriggerScan,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_trigger_scan"),
                colors = ButtonDefaults.buttonColors(containerColor = GovNavyPrimary)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Scan Alerts", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = onNavigateToNotifications,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Alerts", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.testTag("metric_$title") else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick ?: {}
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlateTextSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = SlateTextMuted
            )
        }
    }
}
