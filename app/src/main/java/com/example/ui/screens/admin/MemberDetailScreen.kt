package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomitiViewModel
import com.example.util.BanglaUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    memberId: String,
    viewModel: SomitiViewModel,
    onBack: () -> Unit
) {
    val members by viewModel.members.collectAsState()
    val member = members.find { it.memberId == memberId }

    val savingsList by viewModel.savingsList.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()
    val loans by viewModel.loans.collectAsState()
    val installmentPayments by viewModel.installmentPayments.collectAsState()
    val receipts by viewModel.receipts.collectAsState()
    val monthlyDues by viewModel.monthlyDues.collectAsState()

    val appSettings by viewModel.appSettings.collectAsState()
    val useBangla = appSettings?.useBengaliNumerals ?: true
    val currency = appSettings?.currencySymbol ?: "৳"

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("ওভারভিউ", "সঞ্চয় হিসাব", "ঋণ ও কিস্তি", "ডিজিটাল পাসবই", "রসিদ")

    if (member == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("সদস্য তথ্য পাওয়া যায়নি")
        }
        return
    }

    // Calculations
    val memSavings = savingsList.filter { it.memberId == memberId && it.status == "VALID" }
    val memWds = withdrawals.filter { it.memberId == memberId && it.status == "APPROVED" }
    val totalNetSavings = maxOf(0.0, memSavings.sumOf { it.amount } - memWds.sumOf { it.amount })

    val memLoans = loans.filter { it.memberId == memberId }
    val activeLoans = memLoans.filter { it.status != "FULLY_REPAID" }
    val totalLoanDue = activeLoans.sumOf { it.remainingAmount }

    val memReceipts = receipts.filter { it.memberId == memberId }
    val memDues = monthlyDues.filter { it.memberId == memberId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${member.name} (${member.memberId})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পিছনে যান")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDarkGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Profile Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PaleGreenBg,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryDarkGreen,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "সদস্য আইডি: ${member.memberId} | মোবাইল: ${member.mobile}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "যোগদানের তারিখ: ${member.joiningDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = DividerColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Summary 3-Column
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মোট সঞ্চয় ব্যালেন্স", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                BanglaUtils.formatCurrency(totalNetSavings, useBangla, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDarkGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("বর্তমান ঋণ বকেয়া", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                BanglaUtils.formatCurrency(totalLoanDue, useBangla, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (totalLoanDue > 0) ErrorRed else TextSecondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("নির্ধারিত মাসিক সঞ্চয়", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                BanglaUtils.formatCurrency(member.monthlySavings, useBangla, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryDarkGreen,
                edgePadding = 16.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Overview
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("ব্যক্তিগত ও ঠিকানা বিবরণ", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                    DetailItem("পিতার নাম", member.fatherName.ifBlank { "প্রযোজ্য নয়" })
                                    DetailItem("মাতার নাম", member.motherName.ifBlank { "প্রযোজ্য নয়" })
                                    DetailItem("বিকল্প মোবাইল", member.altMobile.ifBlank { "প্রযোজ্য নয়" })
                                    DetailItem("ঠিকানা", member.address.ifBlank { "প্রযোজ্য নয়" })
                                    DetailItem("গ্রাম/মহল্লা", member.village.ifBlank { "প্রযোজ্য নয়" })
                                    DetailItem("উপজেলা", member.upazila.ifBlank { "প্রযোজ্য নয়" })
                                    DetailItem("জেলা", member.district.ifBlank { "প্রযোজ্য নয়" })
                                    DetailItem("সদস্য স্ট্যাটাস", if (member.status == "ACTIVE") "সক্রিয়" else "নিষ্ক্রিয়")
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("মনোনীত ব্যক্তি (Nominee) তথ্য", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                    DetailItem("নমিনির নাম", member.nomineeName.ifBlank { "প্রযোজ্য নয়" })
                                    DetailItem("সম্পর্ক", member.nomineeRelation.ifBlank { "প্রযোজ্য নয়" })
                                    DetailItem("নমিনির মোবাইল", member.nomineeMobile.ifBlank { "প্রযোজ্য নয়" })
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Savings statement
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (memSavings.isEmpty() && memWds.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("কোনো সঞ্চয় বা উত্তোলনের রেকর্ড নেই", color = TextSecondary)
                                }
                            }
                        }

                        items(memSavings) { sav ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("সঞ্চয় জমা (${sav.type})", fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("তারিখ: ${sav.date} | রসিদ: ${sav.receiptNumber}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Text("মাধ্যম: ${sav.paymentMethod} | সংগ্রাহক: ${sav.collectorName}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                    Text(
                                        "+ ${BanglaUtils.formatCurrency(sav.amount, useBangla, currency)}",
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )
                                }
                            }
                        }

                        items(memWds) { wd ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("সঞ্চয় উত্তোলন (${wd.status})", fontWeight = FontWeight.Bold, color = ErrorRed)
                                        Text("তারিখ: ${wd.date} | কারণ: ${wd.reason}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Text("অনুমোদনকারী: ${wd.approverName.ifBlank { "অপেক্ষমান" }}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                    Text(
                                        "- ${BanglaUtils.formatCurrency(wd.amount, useBangla, currency)}",
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorRed
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Loan & Installment History
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (memLoans.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("এই সদস্যের কোনো ঋণ রেকর্ড নেই", color = TextSecondary)
                                }
                            }
                        }

                        items(memLoans) { loan ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(loan.loanId, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (loan.status == "FULLY_REPAID") PaleGreenBg else SoftGoldBg
                                        ) {
                                            Text(
                                                text = if (loan.status == "FULLY_REPAID") "পরিশোধিত" else "চলমান",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (loan.status == "FULLY_REPAID") PrimaryDarkGreen else WarningOrange,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    DetailItem("আসল ঋণ", BanglaUtils.formatCurrency(loan.principalAmount, useBangla, currency))
                                    DetailItem("মোট পরিশোধযোগ্য", BanglaUtils.formatCurrency(loan.totalRepayable, useBangla, currency))
                                    DetailItem("পরিশোধ হয়েছে", BanglaUtils.formatCurrency(loan.paidAmount, useBangla, currency))
                                    DetailItem("অবশিষ্ট বকেয়া", BanglaUtils.formatCurrency(loan.remainingAmount, useBangla, currency))
                                    DetailItem("কিস্তির সংখ্যা", "${loan.installmentsCount} টি (${BanglaUtils.formatCurrency(loan.perInstallmentAmount, useBangla, currency)} / কিস্তি)")
                                    DetailItem("বিতরণের তারিখ", loan.disbursementDate)
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Digital Passbook (chronological unified ledger)
                    val passbookEntries = remember(memSavings, memWds, memLoans, installmentPayments) {
                        val list = mutableListOf<PassbookEntry>()
                        memSavings.forEach {
                            list.add(PassbookEntry(it.date, "সঞ্চয় জমা (${it.type})", it.amount, true, it.receiptNumber))
                        }
                        memWds.filter { it.status == "APPROVED" }.forEach {
                            list.add(PassbookEntry(it.date, "সঞ্চয় উত্তোলন (${it.reason})", it.amount, false, it.receiptNumber))
                        }
                        memLoans.forEach {
                            list.add(PassbookEntry(it.disbursementDate, "ঋণ গ্রহণ (${it.loanId})", it.principalAmount, true, it.receiptNumber))
                        }
                        installmentPayments.filter { it.memberId == memberId && it.status == "VALID" }.forEach {
                            list.add(PassbookEntry(it.paymentDate, "ঋণের কিস্তি প্রদান (#${it.installmentNumber})", it.amount, false, it.receiptNumber))
                        }
                        list.sortedByDescending { it.date }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (passbookEntries.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("ডিজিটাল পাসবইয়ে কোনো লেনদেন নেই", color = TextSecondary)
                                }
                            }
                        }

                        items(passbookEntries) { entry ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(entry.description, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("তারিখ: ${entry.date} | রসিদ: ${entry.receiptNo}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                    Text(
                                        text = (if (entry.isCredit) "+ " else "- ") + BanglaUtils.formatCurrency(entry.amount, useBangla, currency),
                                        fontWeight = FontWeight.Bold,
                                        color = if (entry.isCredit) PrimaryGreen else ErrorRed
                                    )
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // Receipts
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (memReceipts.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("কোনো রসিদ নেই", color = TextSecondary)
                                }
                            }
                        }

                        items(memReceipts) { rec ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.openReceipt(rec) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(rec.receiptNumber, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                        Text("তারিখ: ${rec.date} | ধরন: ${rec.details}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Text("মাধ্যম: ${rec.paymentMethod}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            BanglaUtils.formatCurrency(rec.amount, useBangla, currency),
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryDarkGreen
                                        )
                                        Text("রসিদ দেখুন >", style = MaterialTheme.typography.labelSmall, color = AccentBlue)
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

data class PassbookEntry(
    val date: String,
    val description: String,
    val amount: Double,
    val isCredit: Boolean,
    val receiptNo: String
)

@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}
