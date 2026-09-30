package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuditLogEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditLogsScreen(
    auditLogs: List<AuditLogEntity>,
    onSelectVoucher: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US) }
    var searchLog by remember { mutableStateOf("") }

    val filteredLogs = remember(auditLogs, searchLog) {
        if (searchLog.isBlank()) auditLogs
        else auditLogs.filter {
            it.voucherNo.contains(searchLog, ignoreCase = true) ||
            it.action.contains(searchLog, ignoreCase = true) ||
            it.performedByName.contains(searchLog, ignoreCase = true) ||
            it.details.contains(searchLog, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "COA / Internal Audit Trail",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GovNavyPrimary
                )
                Text(
                    text = "Tamper-evident logs of all voucher transitions and sign-offs",
                    fontSize = 12.sp,
                    color = SlateTextSecondary
                )
            }
            Surface(
                color = GovNavyPrimary,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "${filteredLogs.size} Events",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchLog,
            onValueChange = { searchLog = it },
            placeholder = { Text("Filter audit trail by voucher #, officer, action...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlateTextMuted) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No audit log entries found.",
                    color = SlateTextMuted,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFE2E8F0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        log.action.contains("SIGN") || log.action.contains("APPROVED") -> Icons.Default.Verified
                                        log.action.contains("RETURN") -> Icons.Default.Reply
                                        log.action.contains("CHECKLIST") -> Icons.Default.PlaylistAddCheck
                                        else -> Icons.Default.History
                                    },
                                    contentDescription = null,
                                    tint = GovNavyPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = log.voucherNo,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = GovNavyPrimary
                                    )
                                    Text(
                                        text = timeFormat.format(Date(log.timestamp)),
                                        fontSize = 10.sp,
                                        color = SlateTextMuted
                                    )
                                }

                                Text(
                                    text = log.details,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Actor: ${log.performedByName} (${log.performedByRole})",
                                    fontSize = 10.sp,
                                    color = SlateTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
