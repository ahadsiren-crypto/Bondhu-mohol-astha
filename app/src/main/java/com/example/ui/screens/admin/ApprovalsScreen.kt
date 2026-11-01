package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.LoanApplicationEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomitiViewModel
import com.example.util.BanglaUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalsScreen(viewModel: SomitiViewModel) {
    val loanApps by viewModel.loanApps.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()
    val members by viewModel.members.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val useBangla = appSettings?.useBengaliNumerals ?: true
    val currency = appSettings?.currencySymbol ?: "৳"

    val pendingLoans = loanApps.filter { it.status == "APPLIED" || it.status == "UNDER_REVIEW" }
    val pendingWithdrawals = withdrawals.filter { it.status == "PENDING" }

    var disburseLoanApp by remember { mutableStateOf<LoanApplicationEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "অনুমোদন কেন্দ্র (Pending Approval Center)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryDarkGreen
            )
            Text(
                text = "অপেক্ষমান ঋণ আবেদন ও সঞ্চয় উত্তোলন এক নজরে পর্যালোচনা ও অনুমোদন করুন।",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Section: Pending Loans
        item {
            Text(
                text = "অপেক্ষমান ঋণ আবেদনসমূহ (${BanglaUtils.formatNumber(pendingLoans.size, useBangla)})",
                fontWeight = FontWeight.Bold,
                color = AccentBlue,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (pendingLoans.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Text("কোনো ঋণ আবেদন অপেক্ষমান নেই।", modifier = Modifier.padding(16.dp), color = TextSecondary)
                }
            }
        } else {
            items(pendingLoans) { app ->
                val member = members.find { it.memberId == app.memberId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${app.applicationId} — ${member?.name ?: ""} (${app.memberId})", fontWeight = FontWeight.Bold)
                            Text(BanglaUtils.formatCurrency(app.amount, useBangla, currency), fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                        }
                        Text("উদ্দেশ্য: ${app.purpose} | মেয়াদ: ${app.durationMonths} মাস", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("জামিনদার: ${app.guarantorName} (${app.guarantorMobile})", style = MaterialTheme.typography.labelSmall, color = TextMuted)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.reviewLoanApplication(app.id, "REJECTED", "বাতিল") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                            ) { Text("প্রত্যাখ্যান") }

                            Button(
                                onClick = { viewModel.reviewLoanApplication(app.id, "APPROVED", "অনুমোদিত") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) { Text("অনুমোদন করুন") }
                        }
                    }
                }
            }
        }

        // Section: Pending Withdrawals
        item {
            Text(
                text = "অপেক্ষমান সঞ্চয় উত্তোলন আবেদনসমূহ (${BanglaUtils.formatNumber(pendingWithdrawals.size, useBangla)})",
                fontWeight = FontWeight.Bold,
                color = ErrorRed,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (pendingWithdrawals.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Text("কোনো সঞ্চয় উত্তোলন অপেক্ষমান নেই।", modifier = Modifier.padding(16.dp), color = TextSecondary)
                }
            }
        } else {
            items(pendingWithdrawals) { wd ->
                val member = members.find { it.memberId == wd.memberId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${member?.name ?: ""} (${wd.memberId})", fontWeight = FontWeight.Bold)
                            Text("- ${BanglaUtils.formatCurrency(wd.amount, useBangla, currency)}", fontWeight = FontWeight.Bold, color = ErrorRed)
                        }
                        Text("কারণ: ${wd.reason} | তারিখ: ${wd.date}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.approveSavingsWithdrawal(wd) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("উত্তোলন অনুমোদন ও রসিদ তৈরি")
                        }
                    }
                }
            }
        }
    }
}
