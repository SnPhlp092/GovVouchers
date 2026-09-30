package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpirationHelper
import com.example.model.ExpirationUrgency
import com.example.model.VoucherWithChecklist
import com.example.ui.components.ExpirationStatusBadge
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun ExpirationMonitorScreen(
    vouchers: List<VoucherWithChecklist>,
    onSelectVoucher: (Long) -> Unit,
    onExtendExpiration: (voucherId: Long, voucherNo: String, currentExp: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "CRITICAL", "WARNING", "EXPIRED", "SETTLED"
    val now = System.currentTimeMillis()

    val filteredList = remember(vouchers, selectedFilter) {
        vouchers.filter { item ->
            val v = item.voucher
            val isCompleted = v.status == "PAID_COMPLETED"
            val info = ExpirationHelper.calculate(v.expirationDate, isCompleted)
            when (selectedFilter) {
                "CRITICAL" -> info.urgency == ExpirationUrgency.CRITICAL
                "WARNING" -> info.urgency == ExpirationUrgency.WARNING
                "EXPIRED" -> info.urgency == ExpirationUrgency.EXPIRED
                "SETTLED" -> info.urgency == ExpirationUrgency.COMPLETED
                else -> true
            }
        }.sortedBy { it.voucher.expirationDate }
    }

    val criticalCount = vouchers.count {
        ExpirationHelper.calculate(it.voucher.expirationDate, it.voucher.status == "PAID_COMPLETED").urgency == ExpirationUrgency.CRITICAL
    }
    val expiredCount = vouchers.count {
        ExpirationHelper.calculate(it.voucher.expirationDate, it.voucher.status == "PAID_COMPLETED").urgency == ExpirationUrgency.EXPIRED
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Expiration Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (criticalCount > 0 || expiredCount > 0) GovUrgentContainer else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (criticalCount > 0 || expiredCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (criticalCount > 0 || expiredCount > 0) GovUrgentRed else GovSuccessGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Voucher Expiration & Liquidation Monitor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (criticalCount > 0 || expiredCount > 0) GovUrgentRed else GovNavyPrimary
                    )
                    Text(
                        text = if (expiredCount > 0) "$expiredCount expired/lapsed vouchers require justification or re-obligation."
                        else if (criticalCount > 0) "$criticalCount vouchers expiring within 72 hours! Prioritize routing."
                        else "All active vouchers are currently within regulatory deadlines.",
                        fontSize = 11.5.sp,
                        color = SlateTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${vouchers.size})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedFilter == "CRITICAL",
                onClick = { selectedFilter = "CRITICAL" },
                label = { Text("Critical ($criticalCount)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GovUrgentContainer,
                    selectedLabelColor = GovUrgentRed
                )
            )
            FilterChip(
                selected = selectedFilter == "WARNING",
                onClick = { selectedFilter = "WARNING" },
                label = { Text("Warning", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedFilter == "EXPIRED",
                onClick = { selectedFilter = "EXPIRED" },
                label = { Text("Expired ($expiredCount)", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Vouchers Expiration List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No vouchers found matching filter.",
                    color = SlateTextMuted,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.voucher.id }) { item ->
                    val voucher = item.voucher
                    val isCompleted = voucher.status == "PAID_COMPLETED"
                    val expInfo = ExpirationHelper.calculate(voucher.expirationDate, isCompleted)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectVoucher(voucher.id) }
                            .testTag("voucher_exp_${voucher.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = if (expInfo.urgency == ExpirationUrgency.CRITICAL || expInfo.urgency == ExpirationUrgency.EXPIRED)
                            BorderStroke(1.dp, GovUrgentRed) else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = voucher.voucherNo,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = GovNavyPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Text(
                                            text = voucher.currentSection,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                ExpirationStatusBadge(
                                    expirationTimestamp = voucher.expirationDate,
                                    isCompleted = isCompleted
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = voucher.payeeName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = voucher.natureOfExpenses,
                                fontSize = 11.sp,
                                color = SlateTextSecondary,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Expiration date timeline & amount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Expires: ${expInfo.formattedDate}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (expInfo.urgency == ExpirationUrgency.CRITICAL || expInfo.urgency == ExpirationUrgency.EXPIRED)
                                            GovUrgentRed else SlateTextSecondary
                                    )
                                    Text(
                                        text = "Period: ${voucher.periodCovered}",
                                        fontSize = 10.sp,
                                        color = SlateTextMuted
                                    )
                                }

                                Text(
                                    text = "₱${String.format(Locale.US, "%,.2f", voucher.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        onExtendExpiration(voucher.id, voucher.voucherNo, voucher.expirationDate)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Extend Deadline", fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { onSelectVoucher(voucher.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GovNavyPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Open Slip", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
