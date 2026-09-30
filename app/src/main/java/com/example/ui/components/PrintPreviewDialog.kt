package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.VoucherWithChecklist
import com.example.ui.theme.GovNavyPrimary
import com.example.ui.theme.GovPaperWhite
import com.example.ui.theme.GovTableBorder
import java.util.Locale

@Composable
fun PrintPreviewDialog(
    voucherWithChecklist: VoucherWithChecklist,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val voucher = voucherWithChecklist.voucher
    val checklist = voucherWithChecklist.checklistItems

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GovNavyPrimary)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Printable Route Slip • ${voucher.voucherNo}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        """
                                        --- VOUCHER ROUTE SLIP ---
                                        Voucher No: ${voucher.voucherNo}
                                        Name: ${voucher.payeeName}
                                        Nature of Expenses: ${voucher.natureOfExpenses}
                                        Period: ${voucher.periodCovered}
                                        Amount: ₱${String.format(Locale.US, "%,.2f", voucher.amount)}
                                        Status: ${voucher.status} (Current: ${voucher.currentSection})
                                        Budget Section: Clerk: ${voucher.budgetReceivingClerkInitial} (${voucher.budgetReceivingDate}), Officer: ${voucher.budgetOfficerInitial} (${voucher.budgetOfficerDate})
                                        Accounting Section: Clerk: ${voucher.accountingReceivingClerkInitial}, Processing: ${voucher.accountingProcessingInitial}, Index: ${voucher.accountingIndexInitial}
                                        LDDAP-ADA No: ${voucher.orderOfPaymentLddapNo}
                                        Remarks: ${voucher.remarks}
                                        """.trimIndent()
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Voucher Slip"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Document Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFE2E8F0))
                        .padding(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        shape = RoundedCornerShape(2.dp),
                        colors = CardDefaults.cardColors(containerColor = GovPaperWhite),
                        border = BorderStroke(1.5.dp, GovTableBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "REPUBLIC OF THE PHILIPPINES",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Black,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "VOUCHER ROUTE SLIP",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Black,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "Voucher No.: ",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = voucher.voucherNo,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Simplified preview table
                            Column(modifier = Modifier.border(1.dp, GovTableBorder)) {
                                RouteSlipPreviewRow("Name", voucher.payeeName)
                                RouteSlipPreviewRow("Nature of Expenses", voucher.natureOfExpenses)
                                RouteSlipPreviewRow("Period Covered", voucher.periodCovered)
                                RouteSlipPreviewRow("Amount", "₱" + String.format(Locale.US, "%,.2f", voucher.amount))
                                RouteSlipPreviewRow(
                                    "Budget Section",
                                    "Receiving: ${voucher.budgetReceivingClerkInitial.ifBlank { "—" }} (${voucher.budgetReceivingDate}) • Officer: ${voucher.budgetOfficerInitial.ifBlank { "—" }} (${voucher.budgetOfficerDate})"
                                )
                                RouteSlipPreviewRow(
                                    "Accounting Section",
                                    "Clerk: ${voucher.accountingReceivingClerkInitial.ifBlank { "—" }} • Processing: ${voucher.accountingProcessingInitial.ifBlank { "—" }} • Index: ${voucher.accountingIndexInitial.ifBlank { "—" }}"
                                )
                                RouteSlipPreviewRow("Order of Payment / LDDAP-ADA", voucher.orderOfPaymentLddapNo.ifBlank { "Pending Issuance" })
                                RouteSlipPreviewRow("Remarks", voucher.remarks.ifBlank { "None" })
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "CHECKLIST VERIFICATION SUMMARY:",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            checklist.forEach { item ->
                                val mark = if (item.isChecked) "[✓]" else "[ ]"
                                Text(
                                    text = "$mark ${item.requirementNumber}. ${item.title}",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 9.sp,
                                    color = if (item.isChecked) Color.Black else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteSlipPreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, GovTableBorder)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 9.5.sp,
            color = Color.Black,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            fontFamily = FontFamily.Serif,
            fontSize = 9.5.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}
