package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GovNavyPrimary
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewVoucherDialog(
    onDismiss: () -> Unit,
    onCreate: (
        voucherNo: String,
        payeeName: String,
        natureOfExpenses: String,
        periodCovered: String,
        amount: Double,
        category: String,
        fundCluster: String,
        expirationDays: Int,
        checklist: List<Pair<String, String>>,
        remarks: String
    ) -> Unit
) {
    val randomSuffix = remember { Random.nextInt(10, 99).toString() + ('A'..'Z').random() }
    var voucherNo by remember { mutableStateOf("26-809-$randomSuffix") }
    var payeeName by remember { mutableStateOf("") }
    var natureOfExpenses by remember { mutableStateOf("To payment for cash advance of salaries of Contract of Service personnel") }
    var periodCovered by remember { mutableStateOf("October 01–15, 2026") }
    var amountText by remember { mutableStateOf("185000.00") }
    var category by remember { mutableStateOf("Salaries of COS Personnel") }
    var fundCluster by remember { mutableStateOf("Fund Cluster 01 - Regular Agency Fund") }
    var expirationDaysText by remember { mutableStateOf("15") }
    var remarks by remember { mutableStateOf("Subject to complete submission of Daily Time Records (DTR) and accomplishment reports.") }

    val categories = listOf(
        "Salaries of COS Personnel",
        "Procurement of Goods & Supplies",
        "Utility & Communication Expenses",
        "Travel Reimbursement & Per Diem",
        "Consultancy & Professional Services"
    )

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GovNavyPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Voucher Route Slip", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = voucherNo,
                    onValueChange = { voucherNo = it },
                    label = { Text("Voucher No.") },
                    modifier = Modifier.fillMaxWidth().testTag("input_voucher_no"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = payeeName,
                    onValueChange = { payeeName = it },
                    label = { Text("Payee / Claimant Name") },
                    placeholder = { Text("e.g., JOYCE L. GALEA") },
                    modifier = Modifier.fillMaxWidth().testTag("input_payee_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = natureOfExpenses,
                    onValueChange = { natureOfExpenses = it },
                    label = { Text("Nature of Expenses") },
                    modifier = Modifier.fillMaxWidth().height(80.dp).testTag("input_nature_expenses"),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = periodCovered,
                        onValueChange = { periodCovered = it },
                        label = { Text("Period Covered") },
                        modifier = Modifier.weight(1.2f).testTag("input_period_covered"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (₱)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("input_amount"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expense Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    category = item
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = expirationDaysText,
                    onValueChange = { expirationDaysText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Expiration / Liquidation Days") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Initial Remarks / Notes") },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val days = expirationDaysText.toIntOrNull() ?: 15
                    if (voucherNo.isNotBlank() && payeeName.isNotBlank() && amount > 0) {
                        // Standard checklist items
                        val checklist = listOf(
                            Pair("General Requirements", "Disbursement Voucher"),
                            Pair("General Requirements", "Obligation Request and Status (ORS)"),
                            Pair("Salaries of Individuals Hired under Contract of Service", "Notarized Contract"),
                            Pair("Salaries of Individuals Hired under Contract of Service", "Duly Accomplished Daily Time Record (DTR)"),
                            Pair("Salaries of Individuals Hired under Contract of Service", "Duly Approved Accomplishment Report"),
                            Pair("Salaries of Individuals Hired under Contract of Service", "Travel Order, if applicable"),
                            Pair("Salaries of Individuals Hired under Contract of Service", "Certificate of Completion and Acceptance"),
                            Pair("Salaries of Individuals Hired under Contract of Service", "S.O, if applicable"),
                            Pair("Salaries of Individuals Hired under Contract of Service", "MCLL Certification, if lawyer"),
                            Pair("Salaries of Individuals Hired under Contract of Service", "Written Concurrence from COA, if lawyer"),
                            Pair("Salaries of Individuals Hired under Contract of Service", "Deputation of OSG, if lawyer"),
                            Pair("Salaries of Individuals Hired under Contract of Service", "Acquiescence from OSG, if lawyer"),
                            Pair("Salaries of Individuals Hired under Contract of Service", "Certificate of Availability of Funds")
                        )
                        onCreate(
                            voucherNo.trim(),
                            payeeName.trim().uppercase(Locale.US),
                            natureOfExpenses.trim(),
                            periodCovered.trim(),
                            amount,
                            category,
                            fundCluster,
                            days,
                            checklist,
                            remarks.trim()
                        )
                    }
                },
                modifier = Modifier.testTag("btn_submit_voucher")
            ) {
                Text("Register Voucher")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
