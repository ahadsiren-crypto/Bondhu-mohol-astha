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
fun SavingsScreen(viewModel: SomitiViewModel) {
    val members by viewModel.members.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val savingsList by viewModel.savingsList.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()
    val monthlyDues by viewModel.monthlyDues.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val useBangla = appSettings?.useBengaliNumerals ?: true
    val currency = appSettings?.currencySymbol ?: "৳"

    var selectedTab by remember { mutableIntStateOf(0) }
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawalDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showDepositDialog = true },
                    containerColor = PrimaryDarkGreen,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("সঞ্চয় জমা করুন", fontWeight = FontWeight.Bold) }
                )
            } else if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showWithdrawalDialog = true },
                    containerColor = ErrorRed,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.RemoveCircleOutline, contentDescription = null) },
                    text = { Text("উত্তোলন আবেদন", fontWeight = FontWeight.Bold) }
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
            // Tabs: 1. Savings List 2. Withdrawals 3. Monthly Dues Tracker
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryDarkGreen
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("সঞ্চয় জমা (${BanglaUtils.formatNumber(savingsList.size, useBangla)})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("সঞ্চয় উত্তোলন (${BanglaUtils.formatNumber(withdrawals.size, useBangla)})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("মাসিক বকেয়া", fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Savings list
                    if (savingsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("এখনো কোনো সঞ্চয় জমা হয়নি। নিচে 'সঞ্চয় জমা করুন' চাপুন।", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(savingsList) { sav ->
                                val member = members.find { it.memberId == sav.memberId }
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
                                                text = "${member?.name ?: "সদস্য"} (${sav.memberId})",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "ধরন: ${sav.type} | তারিখ: ${sav.date}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = "রসিদ নং: ${sav.receiptNumber} | মাধ্যম: ${sav.paymentMethod}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "+ ${BanglaUtils.formatCurrency(sav.amount, useBangla, currency)}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryGreen
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = PaleGreenBg,
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Text(
                                                    text = "বৈধ জমা",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = PrimaryDarkGreen,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Withdrawals List
                    if (withdrawals.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("কোনো সঞ্চয় উত্তোলনের রেকর্ড নেই।", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(withdrawals) { wd ->
                                val member = members.find { it.memberId == wd.memberId }
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
                                                text = "${member?.name ?: "সদস্য"} (${wd.memberId})",
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = when (wd.status) {
                                                    "APPROVED" -> PaleGreenBg
                                                    "PENDING" -> SoftGoldBg
                                                    else -> ErrorSoftBg
                                                }
                                            ) {
                                                Text(
                                                    text = when (wd.status) {
                                                        "APPROVED" -> "অনুমোদিত"
                                                        "PENDING" -> "অনুমোদনের অপেক্ষায়"
                                                        else -> "প্রত্যাখ্যাত"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (wd.status) {
                                                        "APPROVED" -> PrimaryDarkGreen
                                                        "PENDING" -> WarningOrange
                                                        else -> ErrorRed
                                                    },
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("উত্তোলনের পরিমাণ: - ${BanglaUtils.formatCurrency(wd.amount, useBangla, currency)}", fontWeight = FontWeight.Bold, color = ErrorRed)
                                        Text("কারণ: ${wd.reason} | তারিখ: ${wd.date}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                                        if (wd.status == "PENDING") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = { viewModel.approveSavingsWithdrawal(wd) },
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("উত্তোলন অনুমোদন ও অর্থ প্রদান করুন")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Monthly Dues tracker
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(members) { member ->
                            val currentMonth = BanglaUtils.currentMonthYear()
                            val due = monthlyDues.find { it.memberId == member.memberId && it.monthYear == currentMonth }
                            val target = member.monthlySavings
                            val paid = due?.paidAmount ?: 0.0
                            val remaining = maxOf(0.0, target - paid)

                            val status = when {
                                paid >= target -> "পরিশোধিত (Paid)"
                                paid > 0.0 -> "আংশিক (Partial)"
                                else -> "বকেয়া (Unpaid)"
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${member.name} (${member.memberId})", fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (paid >= target) PaleGreenBg else if (paid > 0) SoftBlueBg else ErrorSoftBg
                                        ) {
                                            Text(
                                                text = status,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (paid >= target) PrimaryDarkGreen else if (paid > 0) AccentBlue else ErrorRed,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("নির্ধারিত লক্ষ্য: ${BanglaUtils.formatCurrency(target, useBangla, currency)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        Text("জমা: ${BanglaUtils.formatCurrency(paid, useBangla, currency)}", style = MaterialTheme.typography.bodySmall, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                                        Text("বাকি: ${BanglaUtils.formatCurrency(remaining, useBangla, currency)}", style = MaterialTheme.typography.bodySmall, color = if (remaining > 0) ErrorRed else TextSecondary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Deposit Dialog
    if (showDepositDialog) {
        DepositSavingsDialog(
            members = members,
            accounts = accounts,
            onDismiss = { showDepositDialog = false },
            onSubmit = { mId, amt, type, accId, method, collector, remarks, txnId, date ->
                viewModel.depositSavings(
                    memberId = mId,
                    amount = amt,
                    type = type,
                    accountId = accId,
                    paymentMethod = method,
                    collector = collector,
                    remarks = remarks,
                    txnId = txnId,
                    date = date,
                    onDone = { showDepositDialog = false }
                )
            }
        )
    }

    // Withdrawal Dialog
    if (showWithdrawalDialog) {
        WithdrawalSavingsDialog(
            members = members,
            accounts = accounts,
            onDismiss = { showWithdrawalDialog = false },
            onSubmit = { mId, amt, reason, accId, method, date ->
                viewModel.requestSavingsWithdrawal(
                    memberId = mId,
                    amount = amt,
                    reason = reason,
                    accountId = accId,
                    paymentMethod = method,
                    date = date,
                    onDone = { showWithdrawalDialog = false }
                )
            }
        )
    }
}

@Composable
fun DepositSavingsDialog(
    members: List<MemberEntity>,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSubmit: (
        mId: String, amt: Double, type: String, accId: String,
        method: String, collector: String, remarks: String, txnId: String, date: String
    ) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.memberId ?: "") }
    var selectedSavingsType by remember { mutableStateOf("মাসিক সঞ্চয়") }
    val savingsTypes = listOf("মাসিক সঞ্চয়", "সাপ্তাহিক সঞ্চয়", "বিশেষ সঞ্চয়", "অতিরিক্ত সঞ্চয়", "এককালীন সঞ্চয়")

    var amountStr by remember { mutableStateOf("1000") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var paymentMethod by remember { mutableStateOf("নগদ") }
    val methods = listOf("নগদ", "ব্যাংক", "বিকাশ", "রকেট", "উপায়", "অন্যান্য")

    var collectorName by remember { mutableStateOf("অ্যাডমিন") }
    var remarks by remember { mutableStateOf("") }
    var txnId by remember { mutableStateOf("") }
    var depositDate by remember { mutableStateOf(BanglaUtils.formatDate(System.currentTimeMillis(), false)) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f).padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("সঞ্চয় জমা রসিদ এন্ট্রি", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "বন্ধ") }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (errorMsg != null) {
                        Text(errorMsg!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    }

                    Text("সদস্য নির্বাচন করুন *", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    // Member selector chips or scroll
                    Column {
                        members.take(15).forEach { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedMemberId == m.memberId) PaleGreenBg else Color.Transparent)
                                    .clickable { selectedMemberId = m.memberId }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${m.name} (${m.memberId})", fontWeight = if (selectedMemberId == m.memberId) FontWeight.Bold else FontWeight.Normal)
                                if (selectedMemberId == m.memberId) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryDarkGreen, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Text("সঞ্চয়ের ধরন *", fontWeight = FontWeight.Bold, color = PrimaryGreen, modifier = Modifier.padding(top = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        savingsTypes.forEach { type ->
                            FilterChip(
                                selected = selectedSavingsType == type,
                                onClick = { selectedSavingsType = type },
                                label = { Text(type, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("টাকার পরিমাণ (৳) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Text("পেমেন্ট মাধ্যম ও অ্যাকাউন্ট", fontWeight = FontWeight.Bold, color = PrimaryGreen, modifier = Modifier.padding(top = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        methods.forEach { m ->
                            FilterChip(
                                selected = paymentMethod == m,
                                onClick = { paymentMethod = m },
                                label = { Text(m) }
                            )
                        }
                    }

                    if (accounts.isNotEmpty()) {
                        Text("জমা গ্রহণের অ্যাকাউন্ট:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            accounts.forEach { acc ->
                                FilterChip(
                                    selected = selectedAccountId == acc.id,
                                    onClick = { selectedAccountId = acc.id },
                                    label = { Text(acc.name) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = txnId,
                        onValueChange = { txnId = it },
                        label = { Text("ট্রানজেকশন আইডি (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = collectorName,
                        onValueChange = { collectorName = it },
                        label = { Text("সংগ্রহকারী / আদায়কারীর নাম *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = depositDate,
                        onValueChange = { depositDate = it },
                        label = { Text("জমার তারিখ (DD-MM-YYYY)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("মন্তব্য (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                errorMsg = "অনুগ্রহ করে সঠিক টাকার পরিমাণ লিখুন"
                                return@Button
                            }
                            if (selectedMemberId.isBlank()) {
                                errorMsg = "অনুগ্রহ করে সদস্য নির্বাচন করুন"
                                return@Button
                            }
                            onSubmit(selectedMemberId, amt, selectedSavingsType, selectedAccountId, paymentMethod, collectorName, remarks, txnId, depositDate)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                    ) {
                        Text("জমা করুন ও রসিদ বানান", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun WithdrawalSavingsDialog(
    members: List<MemberEntity>,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSubmit: (mId: String, amt: Double, reason: String, accId: String, method: String, date: String) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.memberId ?: "") }
    var amountStr by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var paymentMethod by remember { mutableStateOf("নগদ") }
    var date by remember { mutableStateOf(BanglaUtils.formatDate(System.currentTimeMillis(), false)) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("সঞ্চয় উত্তোলনের আবেদন", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ErrorRed)
                HorizontalDivider()

                if (errorMsg != null) {
                    Text(errorMsg!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }

                Text("সদস্য নির্বাচন:", fontWeight = FontWeight.Bold, color = TextPrimary)
                Column {
                    members.take(10).forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedMemberId == m.memberId) ErrorSoftBg else Color.Transparent)
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
                    label = { Text("উত্তোলনের পরিমাণ (৳) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("উত্তোলনের কারণ *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                errorMsg = "অনুগ্রহ করে সঠিক উত্তোলনের পরিমাণ লিখুন"
                                return@Button
                            }
                            if (reason.isBlank()) {
                                errorMsg = "অনুগ্রহ করে উত্তোলনের কারণ লিখুন"
                                return@Button
                            }
                            onSubmit(selectedMemberId, amt, reason, selectedAccountId, paymentMethod, date)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("আবেদন জমা দিন", color = Color.White)
                    }
                }
            }
        }
    }
}
