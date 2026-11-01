package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.data.local.AccountEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomitiViewModel
import com.example.util.BanglaUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(viewModel: SomitiViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val transfers by viewModel.transfers.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val useBangla = appSettings?.useBengaliNumerals ?: true
    val currency = appSettings?.currencySymbol ?: "৳"

    var showTransferDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showTransferDialog = true },
                containerColor = PrimaryDarkGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                text = { Text("ফান্ড ট্রান্সফার করুন", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Text(
                    text = "নগদ ও ব্যাংক অ্যাকাউন্ট ব্যবস্থাপনা",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkGreen
                )
                Text(
                    text = "প্রতিটি লেনদেন সংশ্লিষ্ট অ্যাকাউন্টের সাথে যুক্ত থাকে এবং এক অ্যাকাউন্ট থেকে অন্য অ্যাকাউন্টে তহবিল স্থানান্তর করা যায়।",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Account Cards
            items(accounts) { acc ->
                val icon = when (acc.type) {
                    "CASH" -> Icons.Default.AttachMoney
                    "BANK" -> Icons.Default.AccountBalance
                    else -> Icons.Default.Smartphone
                }
                val iconColor = when (acc.type) {
                    "CASH" -> PrimaryDarkGreen
                    "BANK" -> AccentBlue
                    else -> AccentGold
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = iconColor.copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
                                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.padding(10.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(acc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("হিসাব নং: ${acc.accountNumber.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("বর্তমান ব্যালেন্স", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(
                                    BanglaUtils.formatCurrency(acc.currentBalance, useBangla, currency),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (acc.currentBalance >= 0) PrimaryDarkGreen else ErrorRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = DividerColor)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("মোট জমা: ${BanglaUtils.formatCurrency(acc.totalDeposit, useBangla, currency)}", style = MaterialTheme.typography.bodySmall, color = PrimaryGreen)
                            Text("মোট খরচ/উত্তোলন: ${BanglaUtils.formatCurrency(acc.totalWithdrawal, useBangla, currency)}", style = MaterialTheme.typography.bodySmall, color = ErrorRed)
                        }
                    }
                }
            }

            // Transfer History
            item {
                Text(
                    text = "ফান্ড ট্রান্সফার ইতিহাস (${BanglaUtils.formatNumber(transfers.size, useBangla)})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkGreen,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }

            if (transfers.isEmpty()) {
                item {
                    Text("কোনো ফান্ড স্থানান্তরের রেকর্ড নেই।", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            } else {
                items(transfers) { tr ->
                    val fromAcc = accounts.find { it.id == tr.fromAccountId }?.name ?: "উৎস"
                    val toAcc = accounts.find { it.id == tr.toAccountId }?.name ?: "গন্তব্য"

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
                            Column {
                                Text("$fromAcc  ➔  $toAcc", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("তারিখ: ${tr.date} | সম্পাদিত: ${tr.performedBy}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Text(
                                BanglaUtils.formatCurrency(tr.amount, useBangla, currency),
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTransferDialog) {
        FundTransferDialog(
            accounts = accounts,
            onDismiss = { showTransferDialog = false },
            onSubmit = { fromId, toId, amt, ref, remarks ->
                viewModel.transferFunds(fromId, toId, amt, ref, remarks) {
                    showTransferDialog = false
                }
            }
        )
    }
}

@Composable
fun FundTransferDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSubmit: (fromId: String, toId: String, amt: Double, ref: String, remarks: String) -> Unit
) {
    var fromAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var toAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: "") }
    var amountStr by remember { mutableStateOf("") }
    var ref by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("অভ্যন্তরীণ ফান্ড ট্রান্সফার", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                Text("নোট: ট্রান্সফারকে আয় বা ব্যয় হিসেবে গণনা করা হয় না। এটি শুধু অ্যাকাউন্ট ব্যালেন্স স্থানান্তর করে।", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                HorizontalDivider()

                if (errorMsg != null) Text(errorMsg!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)

                Text("উৎস অ্যাকাউন্ট (From):", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    accounts.forEach { a ->
                        FilterChip(selected = fromAccountId == a.id, onClick = { fromAccountId = a.id }, label = { Text(a.name) })
                    }
                }

                Text("গন্তব্য অ্যাকাউন্ট (To):", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    accounts.forEach { a ->
                        FilterChip(selected = toAccountId == a.id, onClick = { toAccountId = a.id }, label = { Text(a.name) })
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("স্থানান্তরের পরিমাণ (৳) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("মন্তব্য / কারণ") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (amt <= 0) {
                                errorMsg = "সঠিক পরিমাণ লিখুন"
                                return@Button
                            }
                            if (fromAccountId == toAccountId) {
                                errorMsg = "উৎস ও গন্তব্য অ্যাকাউন্ট ভিন্ন হতে হবে"
                                return@Button
                            }
                            onSubmit(fromAccountId, toAccountId, amt, ref, remarks)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                    ) { Text("ট্রান্সফার করুন", color = Color.White) }
                }
            }
        }
    }
}
