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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomitiViewModel
import com.example.util.BanglaUtils
import com.example.util.ReceiptExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: SomitiViewModel) {
    val context = LocalContext.current
    val stats by viewModel.dashboardStats.collectAsState()
    val receipts by viewModel.receipts.collectAsState()
    val members by viewModel.members.collectAsState()
    val savings by viewModel.savingsList.collectAsState()
    val loans by viewModel.loans.collectAsState()
    val incomes by viewModel.incomes.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    val appSettings by viewModel.appSettings.collectAsState()
    val useBangla = appSettings?.useBengaliNumerals ?: true
    val currency = appSettings?.currencySymbol ?: "৳"
    val somitiName = appSettings?.somitiNameBn ?: "বন্ধু মহল সমিতি"

    var selectedPeriod by remember { mutableStateOf("THIS_MONTH") } // TODAY, THIS_WEEK, THIS_MONTH, ALL_TIME
    var selectedReportType by remember { mutableStateOf("CASH_BOOK") } // CASH_BOOK, PROFIT_LOSS, SAVINGS, LOANS, TRANSACTIONS

    val periodLabel = when (selectedPeriod) {
        "TODAY" -> "আজকের রিপোর্ট"
        "THIS_WEEK" -> "সাপ্তাহিক রিপোর্ট"
        "THIS_MONTH" -> "মাসিক রিপোর্ট"
        else -> "সার্বিক / বাৎসরিক রিপোর্ট"
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val csv = buildString {
                        append("তারিখ,রসিদ নং,সদস্য/খাত,লেনদেনের ধরন,টাকার পরিমাণ (৳),পেমেন্ট মাধ্যম,সংগ্রহকারী\n")
                        receipts.forEach { r ->
                            append("${r.date},${r.receiptNumber},\"${r.memberName ?: "N/A"}\",\"${r.details}\",${r.amount},\"${r.paymentMethod}\",\"${r.collectorName}\"\n")
                        }
                    }
                    ReceiptExporter.shareReportCsv(context, "$somitiName - $periodLabel", csv)
                },
                containerColor = PrimaryDarkGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                text = { Text("Excel / CSV Export", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "আর্থিক রিপোর্ট ও অডিট সিস্টেম",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkGreen
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Period Filter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedPeriod == "TODAY",
                            onClick = { selectedPeriod = "TODAY" },
                            label = { Text("দৈনিক") }
                        )
                        FilterChip(
                            selected = selectedPeriod == "THIS_WEEK",
                            onClick = { selectedPeriod = "THIS_WEEK" },
                            label = { Text("সাপ্তাহিক") }
                        )
                        FilterChip(
                            selected = selectedPeriod == "THIS_MONTH",
                            onClick = { selectedPeriod = "THIS_MONTH" },
                            label = { Text("মাসিক") }
                        )
                        FilterChip(
                            selected = selectedPeriod == "ALL_TIME",
                            onClick = { selectedPeriod = "ALL_TIME" },
                            label = { Text("সব সময়") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Report Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedReportType == "CASH_BOOK",
                            onClick = { selectedReportType = "CASH_BOOK" },
                            label = { Text("Cash Book") }
                        )
                        FilterChip(
                            selected = selectedReportType == "PROFIT_LOSS",
                            onClick = { selectedReportType = "PROFIT_LOSS" },
                            label = { Text("লাভ-ক্ষতি") }
                        )
                        FilterChip(
                            selected = selectedReportType == "SAVINGS",
                            onClick = { selectedReportType = "SAVINGS" },
                            label = { Text("সঞ্চয় রিপোর্ট") }
                        )
                        FilterChip(
                            selected = selectedReportType == "LOANS",
                            onClick = { selectedReportType = "LOANS" },
                            label = { Text("ঋণ রিপোর্ট") }
                        )
                    }
                }
            }

            // Report Details Body
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedReportType) {
                    "CASH_BOOK" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("ক্যাশ ও ব্যাংক বুক সামারি ($periodLabel)", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                    HorizontalDivider()

                                    accounts.forEach { acc ->
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(acc.name, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                            Text(
                                                BanglaUtils.formatCurrency(acc.currentBalance, useBangla, currency),
                                                fontWeight = FontWeight.Bold,
                                                color = if (acc.currentBalance >= 0) PrimaryDarkGreen else ErrorRed
                                            )
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("সর্বমোট নগদ ও ব্যাংক স্থিতি", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                        Text(
                                            BanglaUtils.formatCurrency(stats.totalNetBalance, useBangla, currency),
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryDarkGreen
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text("সর্বশেষ লেনদেন রসিদ সমূহ", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                        }

                        items(receipts.take(25)) { r ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${r.details} — ${r.memberName ?: ""}", fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("রসিদ নং: ${r.receiptNumber} | তারিখ: ${r.date}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                    Text(
                                        BanglaUtils.formatCurrency(r.amount, useBangla, currency),
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryDarkGreen
                                    )
                                }
                            }
                        }
                    }

                    "PROFIT_LOSS" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("লাভ ও ক্ষতির হিসাব স্টেটমেন্ট", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                    HorizontalDivider()

                                    Text("আয়ের খাতসমূহ:", fontWeight = FontWeight.Bold, color = AccentBlue)
                                    incomes.groupBy { it.category }.forEach { (cat, list) ->
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(cat, style = MaterialTheme.typography.bodySmall)
                                            Text(BanglaUtils.formatCurrency(list.sumOf { it.amount }, useBangla, currency), fontWeight = FontWeight.Bold, color = AccentBlue)
                                        }
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("মোট আয়:", fontWeight = FontWeight.Bold)
                                        Text(BanglaUtils.formatCurrency(stats.totalIncomeAllTime, useBangla, currency), fontWeight = FontWeight.Bold, color = AccentBlue)
                                    }

                                    HorizontalDivider()

                                    Text("ব্যয়ের খাতসমূহ:", fontWeight = FontWeight.Bold, color = ErrorRed)
                                    expenses.groupBy { it.category }.forEach { (cat, list) ->
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(cat, style = MaterialTheme.typography.bodySmall)
                                            Text(BanglaUtils.formatCurrency(list.sumOf { it.amount }, useBangla, currency), fontWeight = FontWeight.Bold, color = ErrorRed)
                                        }
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("মোট ব্যয়:", fontWeight = FontWeight.Bold)
                                        Text(BanglaUtils.formatCurrency(stats.totalExpenseAllTime, useBangla, currency), fontWeight = FontWeight.Bold, color = ErrorRed)
                                    }

                                    HorizontalDivider()

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("নীট লাভ / ক্ষতি:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(
                                            BanglaUtils.formatCurrency(stats.netProfitLoss, useBangla, currency),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (stats.netProfitLoss >= 0) PrimaryGreen else ErrorRed
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "SAVINGS" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("সঞ্চয় রিপোর্ট সারাংশ", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                    HorizontalDivider()
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("আজকের সঞ্চয় আদায়:")
                                        Text(BanglaUtils.formatCurrency(stats.todaySavings, useBangla, currency), fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("চলতি মাসের সঞ্চয়:")
                                        Text(BanglaUtils.formatCurrency(stats.thisMonthSavings, useBangla, currency), fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("সর্বমোট সঞ্চয় দায় (সংরক্ষিত আমানত):")
                                        Text(BanglaUtils.formatCurrency(stats.totalSavingsLiability, useBangla, currency), fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                    }
                                }
                            }
                        }

                        items(members) { m ->
                            val sTotal = savings.filter { it.memberId == m.memberId && it.status == "VALID" }.sumOf { it.amount }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${m.name} (${m.memberId})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                    Text(BanglaUtils.formatCurrency(sTotal, useBangla, currency), fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                }
                            }
                        }
                    }

                    "LOANS" -> {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("ঋণ ও কিস্তি আদায় রিপোর্ট সারাংশ", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                    HorizontalDivider()
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("মোট বিতরণ করা ঋণ:")
                                        Text(BanglaUtils.formatCurrency(stats.totalDisbursedLoans, useBangla, currency), fontWeight = FontWeight.Bold, color = AccentBlue)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("বর্তমানে বকেয়া ঋণ:")
                                        Text(BanglaUtils.formatCurrency(stats.currentLoanDue, useBangla, currency), fontWeight = FontWeight.Bold, color = ErrorRed)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("চলতি মাসে আদায়কৃত কিস্তি:")
                                        Text(BanglaUtils.formatCurrency(stats.thisMonthInstallments, useBangla, currency), fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                    }
                                }
                            }
                        }

                        items(loans) { l ->
                            val m = members.find { it.memberId == l.memberId }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${l.loanId} — ${m?.name ?: ""} (${l.memberId})", fontWeight = FontWeight.Bold)
                                        Text(if (l.status == "FULLY_REPAID") "পরিশোধিত" else "চলমান", color = if (l.status == "FULLY_REPAID") PrimaryGreen else WarningOrange)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("বিতরণ: ${BanglaUtils.formatCurrency(l.principalAmount, useBangla, currency)}", style = MaterialTheme.typography.bodySmall)
                                        Text("বকেয়া: ${BanglaUtils.formatCurrency(l.remainingAmount, useBangla, currency)}", style = MaterialTheme.typography.bodySmall, color = ErrorRed, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
