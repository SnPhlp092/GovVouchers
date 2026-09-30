package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NotificationEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationCenterScreen(
    notifications: List<NotificationEntity>,
    onSelectVoucher: (Long) -> Unit,
    onMarkAsRead: (Long) -> Unit,
    onMarkAllRead: () -> Unit,
    onDeleteNotification: (Long) -> Unit,
    onTriggerScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "EXPIRATION", "BOTTLENECK", "CHECKLIST", "STATUS"
    val timeFormat = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.US) }

    val filteredList = remember(notifications, selectedFilter) {
        if (selectedFilter == "ALL") notifications
        else notifications.filter { it.type == selectedFilter }
    }

    val unreadCount = notifications.count { !it.isRead }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Notification Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Automated Notifications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GovNavyPrimary
                )
                Text(
                    text = if (unreadCount > 0) "$unreadCount unread automated alerts" else "All alerts caught up",
                    fontSize = 12.sp,
                    color = SlateTextSecondary
                )
            }

            Row {
                FilledTonalButton(
                    onClick = onTriggerScan,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_scan_alerts")
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan Now", fontSize = 11.sp)
                }

                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(
                        onClick = onMarkAllRead,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("Mark Read", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${notifications.size})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedFilter == "EXPIRATION",
                onClick = { selectedFilter = "EXPIRATION" },
                label = { Text("Expirations", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedFilter == "BOTTLENECK",
                onClick = { selectedFilter = "BOTTLENECK" },
                label = { Text("Bottlenecks", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedFilter == "CHECKLIST",
                onClick = { selectedFilter = "CHECKLIST" },
                label = { Text("Checklist", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = SlateTextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No notifications in this category",
                        color = SlateTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    val (icon, iconBg, iconColor) = when (item.type) {
                        "EXPIRATION" -> Triple(Icons.Default.HourglassEmpty, GovUrgentContainer, GovUrgentRed)
                        "BOTTLENECK" -> Triple(Icons.Default.AccessTime, GovUrgentContainer, GovUrgentRed)
                        "CHECKLIST" -> Triple(Icons.Default.AssignmentLate, GovAmberContainer, GovAccentAmber)
                        else -> Triple(Icons.Default.CheckCircle, GovSuccessContainer, GovSuccessGreen)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onMarkAsRead(item.id)
                                item.voucherId?.let { onSelectVoucher(it) }
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isRead) MaterialTheme.colorScheme.surface else Color(0xFFF8FAFC)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isRead) 1.dp else 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(iconBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = if (item.isRead) MaterialTheme.colorScheme.onSurface else GovNavyPrimary
                                    )
                                    Text(
                                        text = timeFormat.format(Date(item.timestamp)),
                                        fontSize = 10.sp,
                                        color = SlateTextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = item.message,
                                    fontSize = 11.5.sp,
                                    color = SlateTextSecondary,
                                    lineHeight = 15.sp
                                )

                                if (item.voucherNo.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Voucher #${item.voucherNo} • Tap to view route slip",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = GovSecondaryBlue
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteNotification(item.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = SlateTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
