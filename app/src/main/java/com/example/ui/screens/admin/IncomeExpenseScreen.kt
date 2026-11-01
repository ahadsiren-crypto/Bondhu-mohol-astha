package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AccountEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.IncomeEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomitiViewModel
import com.example.util.BanglaUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeExpenseScreen(viewModel: SomitiViewModel) {
    val incomes by viewModel.incomes.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val useBangla = appSettings?.useBengaliNumerals ?: true
    val currency = appSettings?.currencySymbol ?: "৳"

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    val totalIncome = incomes.filter { it.status == "VALID" }.sumOf { it.amount }
    val totalExpense = expenses.filter { it.status == "VALID" }.sumOf { it.amount }
    val netProfit = totalIncome - totalExpense

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showAddIncomeDialog = true },
                    containerColor = AccentBlue,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("নতুন আয় যোগ করুন", fontWeight = FontWeight.Bold) }
                )
            } else {
                ExtendedFloatingActionButton(
                    onClick = { showAddExpenseDialog = true },
                    containerColor = ErrorRed,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("নতুন ব্যয় যোগ করুন", fontWeight = FontWeight.Bold) }
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
            // Strict Accounting Overview Banner
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "আর্থিক লাভ-ক্ষতি লেজার (Accounting Ledger)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkGreen
                    )
                    Text(
                        text = "নিয়ম: সঞ্চয় ও ঋণের আসল টাকা কখনই প্রাতিষ্ঠানিক আয় বা ব্যয় হিসেবে গণ্য হয় না।",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("মোট আয়", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                BanglaUtils.formatCurrency(totalIncome, useBangla, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মোট ব্যয়", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                BanglaUtils.formatCurrency(totalExpense, useBangla, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("নীট লাভ / ক্ষতি", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                BanglaUtils.formatCurrency(netProfit, useBangla, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (netProfit >= 0) PrimaryGreen else ErrorRed
                            )
                        }
                    }
                }
            }

            // Tab Row: Income vs Expense
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryDarkGreen
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("আয়সমূহ (${BanglaUtils.formatNumber(incomes.size, useBangla)})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("ব্যয়সমূহ (${BanglaUtils.formatNumber(expenses.size, useBangla)})", fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Incomes List
                    if (incomes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("কোনো আয়ের রেকর্ড নেই। নিচে 'নতুন আয় যোগ করুন' চাপুন।", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(incomes) { inc ->
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
                                            Text(inc.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text("খাত: ${inc.category} | তারিখ: ${inc.date}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                            Text("পদ্ধতি: ${inc.paymentMethod} | এন্ট্রি: ${inc.entryBy}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        }
                                        Text(
                                            "+ ${BanglaUtils.formatCurrency(inc.amount, useBangla, currency)}",
                                            fontWeight = FontWeight.Bold,
                                            color = AccentBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Expenses List
                    if (expenses.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("কোনো ব্যয়ের রেকর্ড নেই। নিচে 'নতুন ব্যয় যোগ করুন' চাপুন।", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(expenses) { exp ->
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
                                            Text(exp.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text("খাত: ${exp.category} | তারিখ: ${exp.date}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                            Text("ভাউচার: ${exp.receiptVoucherNote.ifBlank { "N/A" }} | অনুমোদনকারী: ${exp.approverName}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        }
                                        Text(
                                            "- ${BanglaUtils.formatCurrency(exp.amount, useBangla, currency)}",
                                            fontWeight = FontWeight.Bold,
                                            color = ErrorRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Income Dialog
    if (showAddIncomeDialog) {
        AddIncomeDialog(
            accounts = accounts,
            onDismiss = { showAddIncomeDialog = false },
            onSubmit = { title, cat, amt, accId, method, ref, details, date ->
                viewModel.addIncome(title, cat, amt, accId, method, ref, details, date) {
                    showAddIncomeDialog = false
                }
            }
        )
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            accounts = accounts,
            onDismiss = { showAddExpenseDialog = false },
            onSubmit = { title, cat, amt, accId, method, voucher, remarks, date ->
                viewModel.addExpense(title, cat, amt, accId, method, voucher, remarks, date) {
                    showAddExpenseDialog = false
                }
            }
        )
    }
}

@Composable
fun AddIncomeDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSubmit: (title: String, cat: String, amt: Double, accId: String, method: String, ref: String, details: String, date: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ভর্তি ফি") }
    val categories = listOf("ভর্তি ফি", "নবায়ন ফি", "সার্ভিস চার্জ", "ঋণের সুদ", "অন্যান্য আয়")
    var amountStr by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var paymentMethod by remember { mutableStateOf("নগদ") }
    val methods = listOf("নগদ", "ব্যাংক", "বিকাশ", "নগদ", "অন্যান্য")
    var refNumber by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(BanglaUtils.formatDate(System.currentTimeMillis(), false)) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("নতুন আয় এন্ট্রি", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AccentBlue)
                HorizontalDivider()

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("আয়ের শিরোনাম / নাম *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Text("আয়ের খাত নির্বাচন করুন:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { c ->
                        FilterChip(selected = selectedCategory == c, onClick = { selectedCategory = c }, label = { Text(c) })
                    }
                }

                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("টাকার পরিমাণ (৳) *") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Text("জমা গ্রহণের অ্যাকাউন্ট:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    accounts.forEach { a ->
                        FilterChip(selected = selectedAccountId == a.id, onClick = { selectedAccountId = a.id }, label = { Text(a.name) })
                    }
                }

                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("তারিখ (DD-MM-YYYY)") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && amt > 0) {
                                onSubmit(title, selectedCategory, amt, selectedAccountId, paymentMethod, refNumber, details, date)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) { Text("সংরক্ষণ করুন", color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSubmit: (title: String, cat: String, amt: Double, accId: String, method: String, voucher: String, remarks: String, date: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("অফিস ভাড়া") }
    val categories = listOf("অফিস ভাড়া", "কর্মচারীর বেতন", "যাতায়াত", "বিদ্যুৎ বিল", "কাগজ ও প্রিন্টিং", "অন্যান্য ব্যয়")
    var amountStr by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var paymentMethod by remember { mutableStateOf("নগদ") }
    var voucherNote by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(BanglaUtils.formatDate(System.currentTimeMillis(), false)) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("নতুন ব্যয় এন্ট্রি", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ErrorRed)
                HorizontalDivider()

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("ব্যয়ের বিবরণ / নাম *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Text("ব্যয়ের খাত নির্বাচন করুন:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { c ->
                        FilterChip(selected = selectedCategory == c, onClick = { selectedCategory = c }, label = { Text(c) })
                    }
                }

                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("টাকার পরিমাণ (৳) *") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Text("ব্যয় পরিষদের অ্যাকাউন্ট:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    accounts.forEach { a ->
                        FilterChip(selected = selectedAccountId == a.id, onClick = { selectedAccountId = a.id }, label = { Text(a.name) })
                    }
                }

                OutlinedTextField(value = voucherNote, onValueChange = { voucherNote = it }, label = { Text("ভাউচার বা বিল নম্বর") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("তারিখ (DD-MM-YYYY)") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && amt > 0) {
                                onSubmit(title, selectedCategory, amt, selectedAccountId, paymentMethod, voucherNote, remarks, date)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) { Text("সংরক্ষণ করুন", color = Color.White) }
                }
            }
        }
    }
}
