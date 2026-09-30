package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpirationHelper
import com.example.model.ExpirationUrgency
import com.example.ui.theme.GovAccentAmber
import com.example.ui.theme.GovAmberContainer
import com.example.ui.theme.GovNavyDark
import com.example.ui.theme.GovNavyPrimary
import com.example.ui.theme.GovPaperWhite
import com.example.ui.theme.GovSuccessContainer
import com.example.ui.theme.GovSuccessGreen
import com.example.ui.theme.GovTableBorder
import com.example.ui.theme.GovTableLightBorder
import com.example.ui.theme.GovUrgentContainer
import com.example.ui.theme.GovUrgentRed

@Composable
fun RouteSlipCell(
    modifier: Modifier = Modifier,
    text: String,
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: Int = 11,
    fontStyle: FontStyle = FontStyle.Normal,
    textAlign: TextAlign = TextAlign.Start,
    textColor: Color = Color.Black,
    backgroundColor: Color = Color.Transparent,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .border(0.5.dp, GovTableBorder)
            .background(backgroundColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        contentAlignment = when (textAlign) {
            TextAlign.Center -> Alignment.Center
            TextAlign.End -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        }
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Serif,
            fontWeight = fontWeight,
            fontSize = fontSize.sp,
            fontStyle = fontStyle,
            textAlign = textAlign,
            color = textColor,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RouteSlipSignOffRow(
    label: String,
    initial: String,
    date: String,
    canSign: Boolean = false,
    onSignClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Label cell
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight()
                .border(0.5.dp, GovTableBorder)
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp,
                color = Color.Black
            )
        }

        // Initial cell
        Box(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .border(0.5.dp, GovTableBorder)
                .background(if (initial.isBlank() && canSign) Color(0xFFFEF9C3) else Color.White)
                .clickable(enabled = canSign) { onSignClick() }
                .padding(horizontal = 4.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (initial.isNotBlank()) {
                Text(
                    text = initial,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = GovNavyPrimary
                )
            } else if (canSign) {
                Text(
                    text = "Tap to Sign",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    color = Color(0xFF854D0E)
                )
            } else {
                Text(
                    text = "—",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }

        // Date cell
        Box(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxHeight()
                .border(0.5.dp, GovTableBorder)
                .background(if (date.isBlank() && canSign) Color(0xFFFEF9C3) else Color.White)
                .clickable(enabled = canSign) { onSignClick() }
                .padding(horizontal = 4.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (date.isNotBlank()) {
                Text(
                    text = date,
                    fontFamily = FontFamily.Serif,
                    fontSize = 10.5.sp,
                    color = Color.Black
                )
            } else if (canSign) {
                Text(
                    text = "Set Date",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 9.sp,
                    color = Color(0xFF854D0E)
                )
            } else {
                Text(
                    text = "—",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun RouteSlipChecklistItemRow(
    number: Int,
    title: String,
    isChecked: Boolean,
    note: String = "",
    canToggle: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, GovTableBorder)
            .background(if (isChecked) Color(0xFFF0FDF4) else Color.White)
            .clickable(enabled = canToggle) { onCheckedChange(!isChecked) }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Square Checkbox matching government paper form
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(1.dp, GovTableBorder, RoundedCornerShape(2.dp))
                .background(if (isChecked) GovNavyPrimary else Color.White)
                .testTag("checklist_box_$number"),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Checked",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$number. $title",
                fontFamily = FontFamily.Serif,
                fontSize = 10.5.sp,
                fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isChecked) Color.Black else Color(0xFF334155),
                lineHeight = 13.sp
            )
            if (note.isNotBlank()) {
                Text(
                    text = "Note: $note",
                    fontSize = 9.sp,
                    color = GovUrgentRed,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun ExpirationStatusBadge(
    expirationTimestamp: Long,
    isCompleted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val info = ExpirationHelper.calculate(expirationTimestamp, isCompleted)
    val (bgColor, textColor, borderColor) = when (info.urgency) {
        ExpirationUrgency.EXPIRED -> Triple(GovUrgentContainer, GovUrgentRed, GovUrgentRed)
        ExpirationUrgency.CRITICAL -> Triple(GovUrgentContainer, GovUrgentRed, GovUrgentRed)
        ExpirationUrgency.WARNING -> Triple(GovAmberContainer, Color(0xFF92400E), GovAccentAmber)
        ExpirationUrgency.GOOD -> Triple(GovSuccessContainer, GovSuccessGreen, GovSuccessGreen)
        ExpirationUrgency.COMPLETED -> Triple(Color(0xFFE2E8F0), Color(0xFF334155), Color(0xFF94A3B8))
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (info.urgency == ExpirationUrgency.CRITICAL || info.urgency == ExpirationUrgency.EXPIRED) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alert",
                    tint = textColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = info.label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
