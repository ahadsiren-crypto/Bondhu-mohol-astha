package com.example.ui.screens.member

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.MemberMessageEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomitiViewModel
import com.example.util.BanglaUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDashboardScreen(viewModel: SomitiViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val memberId = currentUser?.memberId ?: currentUser?.username ?: ""

    val members by viewModel.members.collectAsState()
    val member = members.find { it.memberId == memberId }

    val savingsList by viewModel.savingsList.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()
    val loans by viewModel.loans.collectAsState()
    val installmentPayments by viewModel.installmentPayments.collectAsState()
    val receipts by viewModel.receipts.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val monthlyDues by viewModel.monthlyDues.collectAsState()
    val memberMessages by viewModel.memberMessages.collectAsState()

    val appSettings by viewModel.appSettings.collectAsState()
    val useBangla = appSettings?.useBengaliNumerals ?: true
    val currency = appSettings?.currencySymbol ?: "৳"
    val somitiName = appSettings?.somitiNameBn ?: "বন্ধু মহল সমিতি"

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("সারসংক্ষেপ", "পাসবই", "ঋণ ও কিস্তি", "রসিদ", "নোটিশ", "মেসেজ")

    var showSendMessageDialog by remember { mutableStateOf(false) }

    // Member data isolation (Strict security: Member ONLY sees own data!)
    val mySavings = savingsList.filter { it.memberId == memberId && it.status == "VALID" }
    val myWds = withdrawals.filter { it.memberId == memberId && it.status == "APPROVED" }
    val myTotalSavings = maxOf(0.0, mySavings.sumOf { it.amount } - myWds.sumOf { it.amount })

    val myLoans = loans.filter { it.memberId == memberId }
    val myActiveLoans = myLoans.filter { it.status != "FULLY_REPAID" }
    val myTotalLoanDue = myActiveLoans.sumOf { it.remainingAmount }

    val myReceipts = receipts.filter { it.memberId == memberId }
    val myMessages = memberMessages.filter { it.memberId == memberId }

    val currentMonth = BanglaUtils.currentMonthYear()
    val myCurrentDue = monthlyDues.find { it.memberId == memberId && it.monthYear == currentMonth }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 5) {
                ExtendedFloatingActionButton(
                    onClick = { showSendMessageDialog = true },
                    containerColor = PrimaryDarkGreen,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Send, contentDescription = null) },
                    text = { Text("অ্যাডমিনকে বার্তা পাঠান") }
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
            // Member Top Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryDarkGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(50.dp)) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = member?.name ?: currentUser?.name ?: "সম্মানিত সদস্য",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "সদস্য আইডি: $memberId | $somitiName",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("আমার মোট সঞ্চয়", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                BanglaUtils.formatCurrency(myTotalSavings, useBangla, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("আমার অবশিষ্ট ঋণ বকেয়া", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                BanglaUtils.formatCurrency(myTotalLoanDue, useBangla, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (myTotalLoanDue > 0) SoftGoldBg else Color.White
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
                tabTitles.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Tabs Content
            when (selectedTab) {
                0 -> {
                    // Summary Overview
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Monthly Savings Due Status Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("চলতি মাসের সঞ্চয় অবস্থা ($currentMonth)", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("নির্ধারিত লক্ষ্য:")
                                        Text(BanglaUtils.formatCurrency(member?.monthlySavings ?: 1000.0, useBangla, currency), fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("জমা হয়েছে:")
                                        Text(BanglaUtils.formatCurrency(myCurrentDue?.paidAmount ?: 0.0, useBangla, currency), fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("বকেয়া:")
                                        Text(
                                            BanglaUtils.formatCurrency(myCurrentDue?.dueAmount ?: (member?.monthlySavings ?: 1000.0), useBangla, currency),
                                            fontWeight = FontWeight.Bold,
                                            color = if ((myCurrentDue?.dueAmount ?: 1.0) > 0) ErrorRed else PrimaryGreen
                                        )
                                    }
                                }
                            }
                        }

                        // Nominee and Contact
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("আমার প্রোফাইল বিবরণ", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                    Text("মোবাইল: ${member?.mobile ?: currentUser?.phone ?: ""}", style = MaterialTheme.typography.bodySmall)
                                    Text("ঠিকানা: ${member?.address ?: ""}, ${member?.village ?: ""}, ${member?.upazila ?: ""}", style = MaterialTheme.typography.bodySmall)
                                    Text("মনোনীত ব্যক্তি: ${member?.nomineeName ?: ""} (${member?.nomineeRelation ?: ""})", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Passbook
                    if (mySavings.isEmpty() && myWds.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("পাসবইয়ে এখনো কোনো লেনদেন নেই", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(mySavings) { s ->
                                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("সঞ্চয় জমা (${s.type})", fontWeight = FontWeight.Bold)
                                            Text("তারিখ: ${s.date} | রসিদ: ${s.receiptNumber}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        }
                                        Text("+ ${BanglaUtils.formatCurrency(s.amount, useBangla, currency)}", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Loans & Installments
                    if (myLoans.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("আপনার কোনো সক্রিয় বা পূর্ববর্তী ঋণ নেই", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(myLoans) { l ->
                                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("ঋণ আইডি: ${l.loanId}", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("মোট প্রদেয়:")
                                            Text(BanglaUtils.formatCurrency(l.totalRepayable, useBangla, currency), fontWeight = FontWeight.Bold)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("পরিশোধ হয়েছে:")
                                            Text(BanglaUtils.formatCurrency(l.paidAmount, useBangla, currency), fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("অবশিষ্ট বকেয়া:")
                                            Text(BanglaUtils.formatCurrency(l.remainingAmount, useBangla, currency), fontWeight = FontWeight.Bold, color = ErrorRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Receipts
                    if (myReceipts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("আপনার কোনো রসিদ পাওয়া যায়নি", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(myReceipts) { r ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { viewModel.openReceipt(r) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(r.receiptNumber, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                            Text("${r.details} | তারিখ: ${r.date}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(BanglaUtils.formatCurrency(r.amount, useBangla, currency), fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                            Text("রসিদ দেখুন >", style = MaterialTheme.typography.labelSmall, color = AccentBlue)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // Notice Board
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notices) { n ->
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(n.title, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(n.content, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("তারিখ: ${n.date} | ${n.publishedBy}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                            }
                        }
                    }
                }
                5 -> {
                    // Messages to Admin
                    if (myMessages.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("আপনি এখনো কোনো বার্তা বা অভিযোগ পাঠাননি। নিচে বাটন চাপুন।", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(myMessages) { msg ->
                                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(msg.subject, fontWeight = FontWeight.Bold)
                                        Text("তারিখ: ${msg.date}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(msg.message, style = MaterialTheme.typography.bodyMedium)

                                        if (msg.reply.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(shape = RoundedCornerShape(8.dp), color = PaleGreenBg, modifier = Modifier.fillMaxWidth()) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Text("অ্যাডমিনের উত্তর:", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen, style = MaterialTheme.typography.labelSmall)
                                                    Text(msg.reply, style = MaterialTheme.typography.bodySmall)
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
        }
    }

    if (showSendMessageDialog) {
        SendMessageToAdminDialog(
            onDismiss = { showSendMessageDialog = false },
            onSubmit = { sub, msg ->
                viewModel.sendMemberMessage(sub, msg) {
                    showSendMessageDialog = false
                }
            }
        )
    }
}

@Composable
fun SendMessageToAdminDialog(
    onDismiss: () -> Unit,
    onSubmit: (subject: String, message: String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("অ্যাডমিনকে বার্তা / অভিযোগ পাঠান", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                HorizontalDivider()

                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("বিষয় *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("বার্তার বিবরণ *") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 6)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            if (subject.isNotBlank() && message.isNotBlank()) {
                                onSubmit(subject, message)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                    ) { Text("পাঠান", color = Color.White) }
                }
            }
        }
    }
}
