package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChecklistItemEntity
import com.example.model.ExpirationHelper
import com.example.model.UserRole
import com.example.model.VoucherWithChecklist
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun VoucherRouteSlipView(
    voucherWithChecklist: VoucherWithChecklist,
    currentRole: UserRole,
    onSignSection: (sectionKey: String) -> Unit,
    onToggleChecklist: (item: ChecklistItemEntity, isChecked: Boolean) -> Unit,
    onEditRemarks: () -> Unit,
    onReturnVoucher: () -> Unit,
    onExtendExpiration: () -> Unit,
    onExportPrint: () -> Unit,
    modifier: Modifier = Modifier
) {
    val voucher = voucherWithChecklist.voucher
    val checklist = voucherWithChecklist.checklistItems
    val scrollState = rememberScrollState()

    val now = System.currentTimeMillis()
    val hoursAtStation = (now - voucher.sectionEntryTimestamp) / (3600_000L)
    val isBottleneck = hoursAtStation >= 48 && voucher.status != "PAID_COMPLETED"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp)
    ) {
        // Operational Header / Status Toolbar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Station: ${voucher.currentSection}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = GovNavyPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isBottleneck) {
                                Surface(
                                    color = GovUrgentContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.HourglassTop,
                                            contentDescription = null,
                                            tint = GovUrgentRed,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "BOTTLENECK (${hoursAtStation}h)",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GovUrgentRed
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "(${hoursAtStation}h at station)",
                                    fontSize = 11.sp,
                                    color = SlateTextSecondary
                                )
                            }
                        }
                        Text(
                            text = "Fund: ${voucher.fundCluster}",
                            fontSize = 10.sp,
                            color = SlateTextMuted
                        )
                    }

                    // Expiration Badge
                    ExpirationStatusBadge(
                        expirationTimestamp = voucher.expirationDate,
                        isCompleted = voucher.status == "PAID_COMPLETED"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditRemarks,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_remarks"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remarks", fontSize = 11.sp)
                    }

                    if (currentRole.canSignBudget || currentRole.canSignAccounting || currentRole.canAudit) {
                        OutlinedButton(
                            onClick = onReturnVoucher,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_return"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GovUrgentRed),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Return", fontSize = 11.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onExtendExpiration,
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("btn_extend"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Extend", fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = onExportPrint,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_print"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // THE OFFICIAL GOVERNMENT ROUTE SLIP FORM (matches image.png layout)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(2.dp),
            colors = CardDefaults.cardColors(containerColor = GovPaperWhite),
            border = BorderStroke(1.5.dp, GovTableBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Republic / Header
                Text(
                    text = "REPUBLIC OF THE PHILIPPINES",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF475569),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "FINANCIAL MANAGEMENT & ACCOUNTING DIVISION",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 8.5.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF64748B),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // TOP ROW: Title & Voucher No.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VOUCHER ROUTE SLIP",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        modifier = Modifier.weight(1.3f)
                    )

                    // Voucher No & CHECKLIST Column Header
                    Column(
                        modifier = Modifier.weight(1.1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Voucher No.: ",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Black
                            )
                            Text(
                                text = voucher.voucherNo,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color.Black,
                                modifier = Modifier.border(
                                    BorderStroke(1.dp, GovTableBorder),
                                    shape = RoundedCornerShape(2.dp)
                                ).padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "CHECKLIST",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, GovTableBorder)
                                .background(Color(0xFFF1F5F9))
                                .padding(vertical = 2.dp)
                        )
                    }
                }

                // MAIN TWO-COLUMN BODY
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GovTableBorder)
                ) {
                    // LEFT COLUMN (Slip Details & Signatures)
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .border(0.5.dp, GovTableBorder)
                    ) {
                        // Name Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, GovTableBorder)
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Name : ",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Black
                            )
                            Text(
                                text = voucher.payeeName,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Nature of Expenses
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .border(0.5.dp, GovTableBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(0.9f)
                                    .fillMaxHeight()
                                    .background(Color(0xFFF8FAFC))
                                    .border(0.5.dp, GovTableBorder)
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "NATURE OF\nEXPENSES",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.Black
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .fillMaxHeight()
                                    .background(Color.White)
                                    .border(0.5.dp, GovTableBorder)
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = voucher.natureOfExpenses,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color.Black
                                )
                            }
                        }

                        // Period Covered
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, GovTableBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(0.9f)
                                    .background(Color(0xFFF8FAFC))
                                    .border(0.5.dp, GovTableBorder)
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "PERIOD COVERED",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    color = Color.Black
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .background(Color.White)
                                    .border(0.5.dp, GovTableBorder)
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = voucher.periodCovered,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = Color.Black
                                )
                            }
                        }

                        // Header: Initial | Date
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE2E8F0))
                                .border(0.5.dp, GovTableBorder)
                        ) {
                            Box(modifier = Modifier.weight(1.3f))
                            Box(
                                modifier = Modifier
                                    .weight(0.9f)
                                    .border(0.5.dp, GovTableBorder)
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Initial",
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 9.5.sp,
                                    color = Color.Black
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1.0f)
                                    .border(0.5.dp, GovTableBorder)
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Date",
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 9.5.sp,
                                    color = Color.Black
                                )
                            }
                        }

                        // AMOUNT ROW
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF9C3))
                                .border(0.5.dp, GovTableBorder)
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AMOUNT",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = Color.Black,
                                modifier = Modifier.weight(0.9f)
                            )
                            Text(
                                text = String.format(Locale.US, "%,.2f", voucher.amount),
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                textAlign = TextAlign.End,
                                color = Color.Black,
                                modifier = Modifier.weight(1.5f)
                            )
                        }

                        // BUDGET SECTION HEADER
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE2E8F0))
                                .border(0.5.dp, GovTableBorder)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "BUDGET SECTION",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = Color.Black
                            )
                        }

                        // Budget Receiving Clerk
                        RouteSlipSignOffRow(
                            label = "RECEIVING CLERK",
                            initial = voucher.budgetReceivingClerkInitial,
                            date = voucher.budgetReceivingDate,
                            canSign = currentRole.canSignBudget,
                            onSignClick = { onSignSection("BUDGET_RECEIVING") }
                        )

                        // Budget Officer
                        RouteSlipSignOffRow(
                            label = "BUDGET OFFICER",
                            initial = voucher.budgetOfficerInitial,
                            date = voucher.budgetOfficerDate,
                            canSign = currentRole.canSignBudget,
                            onSignClick = { onSignSection("BUDGET_OFFICER") }
                        )

                        // Budget Section Amount
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, GovTableBorder)
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AMOUNT",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = Color.Black,
                                modifier = Modifier.weight(1.3f)
                            )
                            Text(
                                text = if (voucher.budgetCertifiedAmount > 0)
                                    String.format(Locale.US, "%,.2f", voucher.budgetCertifiedAmount)
                                else "Pending Obligation",
                                fontFamily = FontFamily.Serif,
                                fontWeight = if (voucher.budgetCertifiedAmount > 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                color = if (voucher.budgetCertifiedAmount > 0) Color.Black else Color.Gray,
                                modifier = Modifier.weight(1.9f)
                            )
                        }

                        // ACCOUNTING SECTION HEADER
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE2E8F0))
                                .border(0.5.dp, GovTableBorder)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ACCOUNTING SECTION",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = Color.Black
                            )
                        }

                        // Accounting Receiving Clerk
                        RouteSlipSignOffRow(
                            label = "RECEIVING CLERK",
                            initial = voucher.accountingReceivingClerkInitial,
                            date = voucher.accountingReceivingDate,
                            canSign = currentRole.canSignAccounting,
                            onSignClick = { onSignSection("ACCOUNTING_RECEIVING") }
                        )

                        // Processing
                        RouteSlipSignOffRow(
                            label = "PROCESSING",
                            initial = voucher.accountingProcessingInitial,
                            date = voucher.accountingProcessingDate,
                            canSign = currentRole.canSignAccounting,
                            onSignClick = { onSignSection("ACCOUNTING_PROCESSING") }
                        )

                        // Index
                        RouteSlipSignOffRow(
                            label = "INDEX",
                            initial = voucher.accountingIndexInitial,
                            date = voucher.accountingIndexDate,
                            canSign = currentRole.canSignAccounting,
                            onSignClick = { onSignSection("ACCOUNTING_INDEX") }
                        )

                        // Released Date
                        RouteSlipSignOffRow(
                            label = "RELEASED DATE",
                            initial = if (voucher.accountingReleasedDate.isNotBlank()) "OK" else "",
                            date = voucher.accountingReleasedDate,
                            canSign = currentRole.canSignAccounting,
                            onSignClick = { onSignSection("ACCOUNTING_RELEASED") }
                        )

                        // Received By
                        RouteSlipSignOffRow(
                            label = "RECEIVED BY",
                            initial = voucher.accountingReceivedBy,
                            date = voucher.accountingReleasedDate,
                            canSign = currentRole.canSignAccounting,
                            onSignClick = { onSignSection("ACCOUNTING_RELEASED") }
                        )

                        // Returned Date
                        RouteSlipSignOffRow(
                            label = "RETURNED DATE",
                            initial = if (voucher.accountingReturnedDate.isNotBlank()) "RET" else "",
                            date = voucher.accountingReturnedDate,
                            canSign = currentRole.canSignAccounting,
                            onSignClick = { onSignSection("RETURNED") }
                        )

                        // Accountant
                        RouteSlipSignOffRow(
                            label = "ACCOUNTANT",
                            initial = voucher.accountantInitial,
                            date = voucher.accountantDate,
                            canSign = currentRole.canSignAccounting,
                            onSignClick = { onSignSection("ACCOUNTANT") }
                        )

                        // Order of Payment / LDDAP-ADA No
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, GovTableBorder)
                                .clickable(enabled = currentRole.canSignAccounting) {
                                    onSignSection("ORDER_OF_PAYMENT")
                                }
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ORDER OF PAYMENT / LDDAP-ADA NO.",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color.Black,
                                modifier = Modifier.weight(1.3f)
                            )
                            Text(
                                text = voucher.orderOfPaymentLddapNo.ifBlank {
                                    if (currentRole.canSignAccounting) "[Set LDDAP-ADA]" else "—"
                                },
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                color = if (voucher.orderOfPaymentLddapNo.isNotBlank()) GovNavyPrimary else Color.LightGray,
                                modifier = Modifier.weight(1.9f)
                            )
                        }
                    }

                    // RIGHT COLUMN (Checklist & Remarks)
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .border(0.5.dp, GovTableBorder)
                    ) {
                        // General Requirements Group
                        val generalItems = checklist.filter { it.sectionGroup == "General Requirements" }
                        if (generalItems.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9))
                                    .border(0.5.dp, GovTableBorder)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "General Requirements",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.Black
                                )
                            }
                            generalItems.forEach { item ->
                                RouteSlipChecklistItemRow(
                                    number = item.requirementNumber,
                                    title = item.title,
                                    isChecked = item.isChecked,
                                    note = item.note,
                                    canToggle = currentRole.canSignAccounting || currentRole.canSignBudget || currentRole.canAudit,
                                    onCheckedChange = { isChecked ->
                                        onToggleChecklist(item, isChecked)
                                    }
                                )
                            }
                        }

                        // Expense Specific Group (e.g. Salaries of Individuals Hired under Contract of Service)
                        val otherGroups = checklist.filter { it.sectionGroup != "General Requirements" }
                            .groupBy { it.sectionGroup }

                        otherGroups.forEach { (groupName, items) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9))
                                    .border(0.5.dp, GovTableBorder)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = groupName,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    color = Color.Black
                                )
                            }
                            items.forEach { item ->
                                RouteSlipChecklistItemRow(
                                    number = item.requirementNumber,
                                    title = item.title,
                                    isChecked = item.isChecked,
                                    note = item.note,
                                    canToggle = currentRole.canSignAccounting || currentRole.canSignBudget || currentRole.canAudit,
                                    onCheckedChange = { isChecked ->
                                        onToggleChecklist(item, isChecked)
                                    }
                                )
                            }
                        }

                        // REMARKS SECTION
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE2E8F0))
                                .border(0.5.dp, GovTableBorder)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "REMARKS",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Remarks",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onEditRemarks() },
                                    tint = GovNavyPrimary
                                )
                            }
                        }

                        // Remarks content box with underlined paper lines
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 120.dp)
                                .background(Color.White)
                                .border(0.5.dp, GovTableBorder)
                                .clickable { onEditRemarks() }
                                .padding(8.dp)
                        ) {
                            if (voucher.remarks.isNotBlank()) {
                                Text(
                                    text = voucher.remarks,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 10.sp,
                                    color = Color(0xFF1E293B),
                                    lineHeight = 15.sp
                                )
                            } else {
                                Text(
                                    text = "No remarks entered. Tap to add auditor observations, missing item reasons, or compliance notes.",
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 9.5.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
