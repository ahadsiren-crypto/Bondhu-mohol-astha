package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomitiViewModel
import com.example.util.BanglaUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(viewModel: SomitiViewModel) {
    val members by viewModel.members.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val loanApps by viewModel.loanApps.collectAsState()
    val loans by viewModel.loans.collectAsState()
    val installmentPayments by viewModel.installmentPayments.collectAsState()
    val savingsList by viewModel.savingsList.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val useBangla = appSettings?.useBengaliNumerals ?: true
    val currency = appSettings?.currencySymbol ?: "৳"

    var selectedTab by remember { mutableIntStateOf(0) }
    var showApplyDialog by remember { mutableStateOf(false) }
    var showDisburseDialog by remember { mutableStateOf<LoanApplicationEntity?>(null) }
    var showCollectInstallmentDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showApplyDialog = true },
                    containerColor = PrimaryDarkGreen,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.PostAdd, contentDescription = null) },
                    text = { Text("নতুন ঋণ আবেদন", fontWeight = FontWeight.Bold) }
                )
            } else if (selectedTab == 1 || selectedTab == 2) {
                ExtendedFloatingActionButton(
                    onClick = { showCollectInstallmentDialog = true },
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.PriceCheck, contentDescription = null) },
                    text = { Text("কিস্তি আদায় করুন", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryDarkGreen
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("ঋণ আবেদন (${BanglaUtils.formatNumber(loanApps.size, useBangla)})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("বিতরণকৃত ঋণ (${BanglaUtils.formatNumber(loans.size, useBangla)})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("কিস্তি আদায় (${BanglaUtils.formatNumber(installmentPayments.size, useBangla)})", fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Loan Applications
                    if (loanApps.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("কোনো ঋণের আবেদন নেই। নতুন ঋণ দিতে আবেদন যোগ করুন।", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(loanApps) { app ->
                                val member = members.find { it.memberId == app.memberId }
                                val memSavings = savingsList.filter { it.memberId == app.memberId && it.status == "VALID" }.sumOf { it.amount }
                                val memExistingLoans = loans.filter { it.memberId == app.memberId && it.status != "FULLY_REPAID" }
                                val existingDue = memExistingLoans.sumOf { it.remainingAmount }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${app.applicationId} — ${member?.name ?: "সদস্য"} (${app.memberId})",
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = when (app.status) {
                                                    "APPROVED" -> PaleGreenBg
                                                    "DISBURSED" -> SoftBlueBg
                                                    "REJECTED" -> ErrorSoftBg
                                                    else -> SoftGoldBg
                                                }
                                            ) {
                                                Text(
                                                    text = when (app.status) {
                                                        "APPROVED" -> "অনুমোদিত"
                                                        "DISBURSED" -> "বিতরণ সম্পন্ন"
                                                        "REJECTED" -> "প্রত্যাখ্যাত"
                                                        else -> "পর্যালোচনাধীন"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (app.status) {
                                                        "APPROVED" -> PrimaryDarkGreen
                                                        "DISBURSED" -> AccentBlue
                                                        "REJECTED" -> ErrorRed
                                                        else -> WarningOrange
                                                    },
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("আবেদনের পরিমাণ: ${BanglaUtils.formatCurrency(app.amount, useBangla, currency)}", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                        Text("উদ্দেশ্য: ${app.purpose} | মেয়াদ: ${app.durationMonths} মাস (${app.installmentsCount} টি কিস্তি)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        Text("জামিনদার: ${app.guarantorName} (${app.guarantorRelation}) — মোবাইল: ${app.guarantorMobile}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(shape = RoundedCornerShape(6.dp), color = PaleGreenBg.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = "অনুমোদনের পূর্বে তথ্য: সদস্যের বর্তমান সঞ্চয়: ${BanglaUtils.formatCurrency(memSavings, useBangla, currency)} | পূর্বের ঋণ বকেয়া: ${BanglaUtils.formatCurrency(existingDue, useBangla, currency)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PrimaryDarkGreen,
                                                modifier = Modifier.padding(6.dp)
                                            )
                                        }

                                        // Actions if APPLIED or APPROVED
                                        if (app.status == "APPLIED" || app.status == "UNDER_REVIEW") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedButton(
                                                    onClick = { viewModel.reviewLoanApplication(app.id, "REJECTED", "শর্ত পূরণ হয়নি") },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                                                ) {
                                                    Text("প্রত্যাখ্যান")
                                                }
                                                Button(
                                                    onClick = { viewModel.reviewLoanApplication(app.id, "APPROVED", "অনুমোদিত") },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                                ) {
                                                    Text("অনুমোদন করুন")
                                                }
                                            }
                                        } else if (app.status == "APPROVED") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = { showDisburseDialog = app },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                                            ) {
                                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("অনুমোদিত ঋণ বিতরণ ও অর্থ প্রদান করুন")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Disbursed Loans
                    if (loans.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("এখনো কোনো ঋণ বিতরণ করা হয়নি।", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(loans) { loan ->
                                val member = members.find { it.memberId == loan.memberId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${loan.loanId} — ${member?.name ?: "সদস্য"} (${loan.memberId})",
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryDarkGreen
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = if (loan.status == "FULLY_REPAID") PaleGreenBg else SoftGoldBg
                                            ) {
                                                Text(
                                                    text = if (loan.status == "FULLY_REPAID") "সম্পূর্ণ পরিশোধিত" else "চলমান",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (loan.status == "FULLY_REPAID") PrimaryDarkGreen else WarningOrange,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("মূল ঋণ: ${BanglaUtils.formatCurrency(loan.principalAmount, useBangla, currency)}", style = MaterialTheme.typography.bodySmall)
                                            Text("মোট প্রদেয়: ${BanglaUtils.formatCurrency(loan.totalRepayable, useBangla, currency)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("আদায় হয়েছে: ${BanglaUtils.formatCurrency(loan.paidAmount, useBangla, currency)}", style = MaterialTheme.typography.bodySmall, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                                            Text("অবশিষ্ট বকেয়া: ${BanglaUtils.formatCurrency(loan.remainingAmount, useBangla, currency)}", style = MaterialTheme.typography.bodySmall, color = ErrorRed, fontWeight = FontWeight.Bold)
                                        }
                                        Text("প্রতি কিস্তির পরিমাণ: ${BanglaUtils.formatCurrency(loan.perInstallmentAmount, useBangla, currency)} (${loan.installmentsCount} টি কিস্তি)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Text("বিতরণের তারিখ: ${loan.disbursementDate} | রসিদ নং: ${loan.receiptNumber}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Installment Payments
                    if (installmentPayments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("এখনো কোনো কিস্তি আদায় করা হয়নি। নিচে 'কিস্তি আদায় করুন' চাপুন।", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(installmentPayments) { inst ->
                                val member = members.find { it.memberId == inst.memberId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${member?.name ?: "সদস্য"} (${inst.memberId})",
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "ঋণ নং: ${inst.loanId} — কিস্তি #${inst.installmentNumber}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = PrimaryDarkGreen,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "তারিখ: ${inst.paymentDate} | রসিদ: ${inst.receiptNumber}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = BanglaUtils.formatCurrency(inst.amount, useBangla, currency),
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryGreen
                                            )
                                            Text("আদায়কারী: ${inst.collectorName}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
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

    // Apply for Loan Dialog
    if (showApplyDialog) {
        ApplyLoanDialog(
            members = members,
            onDismiss = { showApplyDialog = false },
            onSubmit = { mId, amt, purpose, duration, installments, gName, gMobile, gRel, remarks, date ->
                viewModel.applyForLoan(
                    mId, amt, purpose, duration, installments, gName, gMobile, gRel, remarks, date,
                    onDone = { showApplyDialog = false }
                )
            }
        )
    }

    // Disburse Loan Dialog
    if (showDisburseDialog != null) {
        val app = showDisburseDialog!!
        DisburseLoanDialog(
            app = app,
            accounts = accounts,
            onDismiss = { showDisburseDialog = null },
            onSubmit = { amt, rate, srvCharge, accId, method, installments, date ->
                viewModel.disburseLoan(
                    app, amt, rate, srvCharge, accId, method, installments, date,
                    onDone = { showDisburseDialog = null }
                )
            }
        )
    }

    // Collect Installment Dialog
    if (showCollectInstallmentDialog) {
        CollectInstallmentDialog(
            loans = loans.filter { it.status != "FULLY_REPAID" },
            members = members,
            accounts = accounts,
            onDismiss = { showCollectInstallmentDialog = false },
            onSubmit = { lId, instNum, amt, accId, method, collector, remarks, txnId, date ->
                viewModel.collectInstallment(
                    lId, instNum, amt, accId, method, collector, remarks, txnId, date,
                    onDone = { showCollectInstallmentDialog = false }
                )
            }
        )
    }
}

@Composable
fun ApplyLoanDialog(
    members: List<MemberEntity>,
    onDismiss: () -> Unit,
    onSubmit: (
        mId: String, amt: Double, purpose: String, duration: Int,
        installments: Int, gName: String, gMobile: String, gRel: String, remarks: String, date: String
    ) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.memberId ?: "") }
    var amountStr by remember { mutableStateOf("10000") }
    var purpose by remember { mutableStateOf("ক্ষুদ্র ব্যবসা সম্প্রসারণ") }
    var durationMonthsStr by remember { mutableStateOf("6") }
    var installmentsCountStr by remember { mutableStateOf("6") }
    var guarantorName by remember { mutableStateOf("") }
    var guarantorMobile by remember { mutableStateOf("") }
    var guarantorRel by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var applyDate by remember { mutableStateOf(BanglaUtils.formatDate(System.currentTimeMillis(), false)) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.92f).padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Text("নতুন ঋণ আবেদন পত্র", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (errorMsg != null) Text(errorMsg!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)

                    Text("সদস্য নির্বাচন করুন *", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    Column {
                        members.take(10).forEach { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedMemberId == m.memberId) PaleGreenBg else Color.Transparent)
                                    .clickable { selectedMemberId = m.memberId }
                                    .padding(8.dp)
                            ) {
                                Text("${m.name} (${m.memberId})")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("প্রস্তাবিত ঋণের পরিমাণ (৳) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = purpose,
                        onValueChange = { purpose = it },
                        label = { Text("ঋণের উদ্দেশ্য / ব্যবহারের খাত *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = durationMonthsStr,
                            onValueChange = { durationMonthsStr = it },
                            label = { Text("মেয়াদ (মাস)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = installmentsCountStr,
                            onValueChange = { installmentsCountStr = it },
                            label = { Text("কিস্তির সংখ্যা") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Text("জামিনদার (Guarantor) সংক্রান্ত তথ্য", fontWeight = FontWeight.Bold, color = PrimaryGreen, modifier = Modifier.padding(top = 4.dp))
                    OutlinedTextField(
                        value = guarantorName,
                        onValueChange = { guarantorName = it },
                        label = { Text("জামিনদারের নাম") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = guarantorMobile,
                            onValueChange = { guarantorMobile = it },
                            label = { Text("মোবাইল নম্বর") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = guarantorRel,
                            onValueChange = { guarantorRel = it },
                            label = { Text("সম্পর্ক") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = applyDate,
                        onValueChange = { applyDate = it },
                        label = { Text("আবেদনের তারিখ (DD-MM-YYYY)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                errorMsg = "সঠিক ঋণের পরিমাণ দিন"
                                return@Button
                            }
                            val duration = durationMonthsStr.toIntOrNull() ?: 6
                            val installments = installmentsCountStr.toIntOrNull() ?: 6
                            onSubmit(selectedMemberId, amt, purpose, duration, installments, guarantorName, guarantorMobile, guarantorRel, remarks, applyDate)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                    ) {
                        Text("আবেদন দাখিল করুন", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DisburseLoanDialog(
    app: LoanApplicationEntity,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSubmit: (amt: Double, rate: Double, srvCharge: Double, accId: String, method: String, installments: Int, date: String) -> Unit
) {
    var approvedAmountStr by remember { mutableStateOf(app.amount.toString()) }
    var interestRateStr by remember { mutableStateOf("0") } // Admin determines interest
    var serviceChargeStr by remember { mutableStateOf("0") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var paymentMethod by remember { mutableStateOf("নগদ") }
    val methods = listOf("নগদ", "ব্যাংক", "বিকাশ", "নগদ", "অন্যান্য")
    var installmentsCountStr by remember { mutableStateOf(app.installmentsCount.toString()) }
    var disburseDate by remember { mutableStateOf(BanglaUtils.formatDate(System.currentTimeMillis(), false)) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("অনুমোদিত ঋণ বিতরণ ও অর্থ প্রদান", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                HorizontalDivider()

                OutlinedTextField(
                    value = approvedAmountStr,
                    onValueChange = { approvedAmountStr = it },
                    label = { Text("বিতরণকৃত ঋণের পরিমাণ (৳) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = interestRateStr,
                        onValueChange = { interestRateStr = it },
                        label = { Text("সুদের হার (% - ঐচ্ছিক)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = serviceChargeStr,
                        onValueChange = { serviceChargeStr = it },
                        label = { Text("সার্ভিস চার্জ (৳)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Text("অর্থ প্রদানের অ্যাকাউন্ট:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    accounts.forEach { acc ->
                        FilterChip(
                            selected = selectedAccountId == acc.id,
                            onClick = { selectedAccountId = acc.id },
                            label = { Text(acc.name) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            val amt = approvedAmountStr.toDoubleOrNull() ?: app.amount
                            val rate = interestRateStr.toDoubleOrNull() ?: 0.0
                            val srv = serviceChargeStr.toDoubleOrNull() ?: 0.0
                            val inst = installmentsCountStr.toIntOrNull() ?: app.installmentsCount
                            onSubmit(amt, rate, srv, selectedAccountId, paymentMethod, inst, disburseDate)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                    ) {
                        Text("বিতরণ সম্পন্ন করুন", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CollectInstallmentDialog(
    loans: List<LoanEntity>,
    members: List<MemberEntity>,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSubmit: (
        lId: String, instNum: Int, amt: Double, accId: String,
        method: String, collector: String, remarks: String, txnId: String, date: String
    ) -> Unit
) {
    var selectedLoanId by remember { mutableStateOf(loans.firstOrNull()?.loanId ?: "") }
    val currentLoan = loans.find { it.loanId == selectedLoanId }

    var installmentNumStr by remember { mutableStateOf("1") }
    var amountStr by remember { mutableStateOf(currentLoan?.perInstallmentAmount?.toInt()?.toString() ?: "1000") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var paymentMethod by remember { mutableStateOf("নগদ") }
    var collectorName by remember { mutableStateOf("অ্যাডমিন") }
    var remarks by remember { mutableStateOf("") }
    var txnId by remember { mutableStateOf("") }
    var paymentDate by remember { mutableStateOf(BanglaUtils.formatDate(System.currentTimeMillis(), false)) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("ঋণের কিস্তি আদায় রসিদ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                HorizontalDivider()

                Text("ঋণ নির্বাচন করুন *", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                Column {
                    loans.forEach { l ->
                        val m = members.find { it.memberId == l.memberId }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedLoanId == l.loanId) PaleGreenBg else Color.Transparent)
                                .clickable {
                                    selectedLoanId = l.loanId
                                    amountStr = l.perInstallmentAmount.toInt().toString()
                                }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${l.loanId} — ${m?.name ?: ""} (${l.memberId})")
                            Text("বকেয়া: ৳ ${l.remainingAmount}", fontWeight = FontWeight.Bold, color = ErrorRed)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = installmentNumStr,
                        onValueChange = { installmentNumStr = it },
                        label = { Text("কিস্তি নম্বর *") },
                        modifier = Modifier.weight(0.8f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("আদায়কৃত টাকা (৳) *") },
                        modifier = Modifier.weight(1.2f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = collectorName,
                    onValueChange = { collectorName = it },
                    label = { Text("আদায়কারীর নাম") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("আদায়ের তারিখ (DD-MM-YYYY)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            val inst = installmentNumStr.toIntOrNull() ?: 1
                            onSubmit(selectedLoanId, inst, amt, selectedAccountId, paymentMethod, collectorName, remarks, txnId, paymentDate)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                    ) {
                        Text("কিস্তি জমা ও রসিদ তৈরি", color = Color.White)
                    }
                }
            }
        }
    }
}
