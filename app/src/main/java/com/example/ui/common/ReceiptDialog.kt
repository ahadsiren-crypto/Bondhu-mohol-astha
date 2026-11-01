package com.example.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.AppSettingsEntity
import com.example.data.local.ReceiptEntity
import com.example.ui.theme.*
import com.example.util.BanglaUtils
import com.example.util.ReceiptExporter

@Composable
fun ReceiptDialog(
    receipt: ReceiptEntity,
    appSettings: AppSettingsEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val useBangla = appSettings?.useBengaliNumerals ?: true
    val somitiName = appSettings?.somitiNameBn ?: "বন্ধু মহল সমিতি"
    val somitiPhone = appSettings?.phone ?: "০১৭০০-০০০০০০"
    val somitiAddress = appSettings?.address ?: "ঢাকা, বাংলাদেশ"

    val typeTitle = when (receipt.type) {
        "SAVINGS_DEPOSIT" -> "সঞ্চয় জমা রসিদ"
        "SAVINGS_WITHDRAWAL" -> "সঞ্চয় উত্তোলন ভাউচার"
        "LOAN_DISBURSEMENT" -> "ঋণ বিতরণ রসিদ"
        "INSTALLMENT_PAYMENT" -> "ঋণের কিস্তি আদায় রসিদ"
        "INCOME_PAYMENT" -> "জমা/ফি আদায় রসিদ"
        else -> "আর্থিক লেনদেন রসিদ"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ডিজিটাল রসিদ",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Receipt Slip Card with decorative border
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PaleGreenBg.copy(alpha = 0.5f))
                        .border(1.dp, PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo and Somiti Info
                    Image(
                        painter = painterResource(id = R.drawable.bondhu_mohol_logo),
                        contentDescription = "সমিতি লোগো",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = somitiName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkGreen,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = somitiAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "মোবাইল: $somitiPhone",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = PrimaryGreen.copy(alpha = 0.3f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Type Badge
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PrimaryDarkGreen
                    ) {
                        Text(
                            text = typeTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Receipt Info Grid
                    ReceiptRow(label = "রসিদ নম্বর:", value = receipt.receiptNumber)
                    ReceiptRow(label = "তারিখ:", value = receipt.date)
                    if (!receipt.memberName.isNullOrBlank()) {
                        ReceiptRow(label = "সদস্যের নাম:", value = receipt.memberName)
                    }
                    if (!receipt.memberId.isNullOrBlank()) {
                        ReceiptRow(label = "সদস্য আইডি:", value = receipt.memberId)
                    }
                    ReceiptRow(label = "পেমেন্ট মাধ্যম:", value = receipt.paymentMethod)
                    if (receipt.transactionId.isNotBlank()) {
                        ReceiptRow(label = "ট্রানজেকশন আইডি:", value = receipt.transactionId)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DividerColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Amount Display
                    Text(
                        text = "পরিশোধিত টাকার পরিমাণ",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = BanglaUtils.formatCurrency(receipt.amount, useBangla),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkGreen
                    )
                    Text(
                        text = "(${BanglaUtils.amountInBengaliWords(receipt.amount)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = DividerColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Balances
                    ReceiptRow(
                        label = "পূর্বের ব্যালেন্স:",
                        value = BanglaUtils.formatCurrency(receipt.previousBalance, useBangla)
                    )
                    ReceiptRow(
                        label = "বর্তমান ব্যালেন্স:",
                        value = BanglaUtils.formatCurrency(receipt.currentBalance, useBangla)
                    )
                    ReceiptRow(label = "সংগ্রহকারী / আদায়কারী:", value = receipt.collectorName)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Signatures
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "-------------------", color = TextMuted)
                            Text(text = "সদস্যের স্বাক্ষর", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "-------------------", color = TextMuted)
                            Text(text = "অনুমোদিত স্বাক্ষর", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Share to WhatsApp / Print
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            ReceiptExporter.shareReceipt(
                                context = context,
                                somitiNameBn = somitiName,
                                somitiPhone = somitiPhone,
                                receiptNumber = receipt.receiptNumber,
                                memberName = receipt.memberName ?: "N/A",
                                memberId = receipt.memberId ?: "N/A",
                                typeTitle = typeTitle,
                                date = receipt.date,
                                amount = receipt.amount,
                                previousBalance = receipt.previousBalance,
                                currentBalance = receipt.currentBalance,
                                paymentMethod = receipt.paymentMethod,
                                transactionId = receipt.transactionId,
                                collectorName = receipt.collectorName
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "শেয়ার / WhatsApp")
                    }

                    Button(
                        onClick = {
                            ReceiptExporter.shareReceipt(
                                context = context,
                                somitiNameBn = somitiName,
                                somitiPhone = somitiPhone,
                                receiptNumber = receipt.receiptNumber,
                                memberName = receipt.memberName ?: "N/A",
                                memberId = receipt.memberId ?: "N/A",
                                typeTitle = typeTitle,
                                date = receipt.date,
                                amount = receipt.amount,
                                previousBalance = receipt.previousBalance,
                                currentBalance = receipt.currentBalance,
                                paymentMethod = receipt.paymentMethod,
                                transactionId = receipt.transactionId,
                                collectorName = receipt.collectorName
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "প্রিন্ট / ডাউনলোড")
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}
