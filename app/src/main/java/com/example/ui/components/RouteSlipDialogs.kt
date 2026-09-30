package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.model.VoucherWithChecklist
import com.example.ui.theme.GovNavyPrimary
import com.example.ui.theme.GovUrgentRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SignOffDialog(
    sectionKey: String,
    voucher: VoucherWithChecklist,
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onConfirm: (initial: String, date: String, extraValue: String) -> Unit
) {
    val todayDate = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
    }
    var initial by remember { mutableStateOf(currentRole.defaultInitial) }
    var date by remember { mutableStateOf(todayDate) }
    var extraValue by remember {
        mutableStateOf(
            when (sectionKey) {
                "BUDGET_OFFICER" -> String.format(Locale.US, "%.2f", voucher.voucher.amount)
                "ACCOUNTING_RELEASED" -> "Cashier-Division"
                "ACCOUNTANT", "ORDER_OF_PAYMENT" -> voucher.voucher.orderOfPaymentLddapNo.ifBlank { "LDDAP-2026-09-" }
                else -> ""
            }
        )
    }

    val (title, extraLabel) = when (sectionKey) {
        "BUDGET_RECEIVING" -> Pair("Sign Budget Receiving", null)
        "BUDGET_OFFICER" -> Pair("Certify Budget & Fund Availability", "Certified Amount (₱)")
        "ACCOUNTING_RECEIVING" -> Pair("Sign Accounting Receiving", null)
        "ACCOUNTING_PROCESSING" -> Pair("Complete Accounting Processing", null)
        "ACCOUNTING_INDEX" -> Pair("Record in Index of Payments", null)
        "ACCOUNTING_RELEASED" -> Pair("Release Voucher to Cashier", "Received By")
        "ACCOUNTANT" -> Pair("Chief Accountant Approval", "LDDAP-ADA / Order of Payment No.")
        "ORDER_OF_PAYMENT" -> Pair("Set Order of Payment / LDDAP-ADA", "LDDAP-ADA No.")
        else -> Pair("Sign Route Slip Stage", null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GovNavyPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Voucher: ${voucher.voucher.voucherNo} (${voucher.voucher.payeeName})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = initial,
                    onValueChange = { initial = it },
                    label = { Text("Officer Initial / Stamp") },
                    modifier = Modifier.fillMaxWidth().testTag("input_initial"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Sign-off Date") },
                    modifier = Modifier.fillMaxWidth().testTag("input_date"),
                    singleLine = true
                )

                if (extraLabel != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = extraValue,
                        onValueChange = { extraValue = it },
                        label = { Text(extraLabel) },
                        modifier = Modifier.fillMaxWidth().testTag("input_extra"),
                        keyboardOptions = if (sectionKey == "BUDGET_OFFICER") KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default,
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (initial.isNotBlank() && date.isNotBlank()) {
                        onConfirm(initial.trim(), date.trim(), extraValue.trim())
                    }
                },
                modifier = Modifier.testTag("btn_confirm_sign")
            ) {
                Text("Confirm & Sign")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReturnVoucherDialog(
    voucherNo: String,
    onDismiss: () -> Unit,
    onConfirm: (reason: String, date: String) -> Unit
) {
    val todayDate = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
    }
    var reason by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(todayDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Reply, contentDescription = null, tint = GovUrgentRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Return Voucher to Originator", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GovUrgentRed)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Voucher $voucherNo will be returned. A justification remark is required per COA regulations.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Return / Incomplete Items") },
                    placeholder = { Text("e.g., Lacking DTR attachment, period exceeds 30-day liquidation rule...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("input_return_reason"),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Returned Date") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isNotBlank()) {
                        onConfirm(reason.trim(), date.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GovUrgentRed),
                modifier = Modifier.testTag("btn_confirm_return")
            ) {
                Text("Return Voucher")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExtendExpirationDialog(
    voucherNo: String,
    currentExpirationDate: Long,
    onDismiss: () -> Unit,
    onConfirm: (additionalDays: Int, reason: String) -> Unit
) {
    var additionalDays by remember { mutableStateOf("15") }
    var reason by remember { mutableStateOf("Extension requested by department head due to regional office submission delay.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = GovNavyPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Extend Expiration Deadline", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Extend validity for Voucher $voucherNo.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = additionalDays,
                    onValueChange = { additionalDays = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Additional Days (e.g. 7, 15, 30)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_extend_days"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Justification / Authority") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = additionalDays.toIntOrNull() ?: 15
                    if (days > 0 && reason.isNotBlank()) {
                        onConfirm(days, reason.trim())
                    }
                },
                modifier = Modifier.testTag("btn_confirm_extend")
            ) {
                Text("Apply Extension")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditRemarksDialog(
    initialRemarks: String,
    onDismiss: () -> Unit,
    onConfirm: (newRemarks: String) -> Unit
) {
    var remarks by remember { mutableStateOf(initialRemarks) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Route Slip Remarks", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Remarks appear on the official Voucher Route Slip form.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks & Auditor Notes") },
                    modifier = Modifier.fillMaxWidth().height(140.dp).testTag("input_remarks_dialog"),
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(remarks.trim()) },
                modifier = Modifier.testTag("btn_save_remarks")
            ) {
                Text("Save Remarks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSwitcherBottomSheet(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Switch User Role & Permissions",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = GovNavyPrimary
            )
            Text(
                text = "Test the system across different authorized government personnel stations:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            UserRole.values().forEach { role ->
                val isSelected = role == currentRole
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) BorderStroke(1.5.dp, GovNavyPrimary) else BorderStroke(0.5.dp, Color.LightGray),
                    onClick = {
                        onRoleSelected(role)
                        onDismiss()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onRoleSelected(role)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = role.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isSelected) GovNavyPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${role.defaultName} • ${role.department}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (role) {
                                    UserRole.ADMIN_AUDITOR -> "Permissions: Full COA audit, sign any stage, view logs, export reports"
                                    UserRole.BUDGET_OFFICER -> "Permissions: Sign Budget receiving & officer certification, verify ORS"
                                    UserRole.ACCOUNTANT -> "Permissions: Sign Accounting receiving, processing, index, release & LDDAP"
                                    UserRole.CLAIMANT -> "Permissions: Track own voucher, view checklist status, submit requirements"
                                },
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
