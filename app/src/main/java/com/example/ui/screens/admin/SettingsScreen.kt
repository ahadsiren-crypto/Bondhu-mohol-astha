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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AppSettingsEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.SomitiViewModel
import com.example.util.BanglaUtils
import com.example.util.ReceiptExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SomitiViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appSettings by viewModel.appSettings.collectAsState()
    val admins by viewModel.admins.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isSuperAdmin = currentUser?.role == "SUPER_ADMIN"

    var somitiNameBn by remember(appSettings) { mutableStateOf(appSettings?.somitiNameBn ?: "বন্ধু মহল সমিতি") }
    var somitiNameEn by remember(appSettings) { mutableStateOf(appSettings?.somitiNameEn ?: "Bondhu Mohol Somiti") }
    var address by remember(appSettings) { mutableStateOf(appSettings?.address ?: "ঢাকা, বাংলাদেশ") }
    var phone by remember(appSettings) { mutableStateOf(appSettings?.phone ?: "০১৭০০-০০০০০০") }
    var defaultSavingsStr by remember(appSettings) { mutableStateOf((appSettings?.defaultMonthlySavings ?: 1000.0).toInt().toString()) }
    var useBanglaNumerals by remember(appSettings) { mutableStateOf(appSettings?.useBengaliNumerals ?: true) }
    var isDarkTheme by remember(appSettings) { mutableStateOf(appSettings?.isDarkTheme ?: false) }

    var showCreateAdminDialog by remember { mutableStateOf(false) }
    var integrityIssues by remember { mutableStateOf<List<String>?>(null) }
    var isCheckingIntegrity by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "গ্লোবাল সেটিংস ও সুপার অ্যাডমিন কন্ট্রোল প্যানেল",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryDarkGreen
            )
            Text(
                text = "সমিতির পরিচয়, হিসাবের নিয়মাবলী, নিরাপত্তা এবং ডাটাবেজ ব্যাকআপ পরিচালনা করুন।",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Section 1: Somiti Basic Profile
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("সমিতির সাধারণ তথ্য", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)

                    OutlinedTextField(
                        value = somitiNameBn,
                        onValueChange = { somitiNameBn = it },
                        label = { Text("সমিতির বাংলা নাম") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = somitiNameEn,
                        onValueChange = { somitiNameEn = it },
                        label = { Text("সমিতির ইংরেজি নাম") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("সমিতির কার্যালয়ের পূর্ণ ঠিকানা") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("অফিসিয়াল মোবাইল নম্বর") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = defaultSavingsStr,
                        onValueChange = { defaultSavingsStr = it },
                        label = { Text("ডিফল্ট মাসিক সঞ্চয় লক্ষ্য (৳)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Button(
                        onClick = {
                            val targetSav = defaultSavingsStr.toDoubleOrNull() ?: 1000.0
                            val updated = (appSettings ?: AppSettingsEntity()).copy(
                                somitiNameBn = somitiNameBn,
                                somitiNameEn = somitiNameEn,
                                address = address,
                                phone = phone,
                                defaultMonthlySavings = targetSav,
                                useBengaliNumerals = useBanglaNumerals,
                                isDarkTheme = isDarkTheme
                            )
                            viewModel.updateSettings(updated)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সাধারণ তথ্য সংরক্ষণ করুন", color = Color.White)
                    }
                }
            }
        }

        // Section 2: Appearance & Numerals
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ভাষা ও প্রদর্শনী সেটিংস", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("বাংলা সংখ্যা প্রদর্শন", fontWeight = FontWeight.SemiBold)
                            Text("টাকা ও তারিখের সংখ্যা বাংলায় রূপান্তর (১, ২, ৩)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Switch(
                            checked = useBanglaNumerals,
                            onCheckedChange = {
                                useBanglaNumerals = it
                                val updated = (appSettings ?: AppSettingsEntity()).copy(useBengaliNumerals = it)
                                viewModel.updateSettings(updated)
                            }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ডার্ক মোড (Dark Theme)", fontWeight = FontWeight.SemiBold)
                            Text("গাঢ় ব্যাকগ্রাউন্ড ব্যবহার করুন", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = {
                                isDarkTheme = it
                                val updated = (appSettings ?: AppSettingsEntity()).copy(isDarkTheme = it)
                                viewModel.updateSettings(updated)
                            }
                        )
                    }
                }
            }
        }

        // Section 3: Admin Management & RBAC
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("অ্যাডমিন ও ইউজার রোল ব্যবস্থাপনা", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                        if (isSuperAdmin) {
                            TextButton(onClick = { showCreateAdminDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("নতুন অ্যাডমিন")
                            }
                        }
                    }

                    admins.forEach { adm ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${adm.name} (${adm.username})", fontWeight = FontWeight.Bold)
                                Text("রোল: ${adm.role} | পারমিশন: ${adm.permissions.ifBlank { "ALL" }}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (adm.isActive) PaleGreenBg else ErrorSoftBg
                            ) {
                                Text(
                                    text = if (adm.isActive) "সক্রিয়" else "স্থগিত",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (adm.isActive) PrimaryDarkGreen else ErrorRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Database Integrity Checker
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ডাটাবেজ ও হিসাবের সামঞ্জস্য যাচাই (Integrity Checker)", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                    Text("সঞ্চয়, ঋণ, কিস্তি ও ক্যাশ ব্যালেন্সে কোনো গরমিল বা অসামঞ্জস্য রয়েছে কি না তা স্বয়ংক্রিয়ভাবে অডিট করবে।", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                    OutlinedButton(
                        onClick = {
                            isCheckingIntegrity = true
                            scope.launch {
                                integrityIssues = viewModel.repository.verifyDatabaseIntegrity()
                                isCheckingIntegrity = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isCheckingIntegrity) "যাচাই করা হচ্ছে..." else "হিসাবের সামঞ্জস্য যাচাই করুন")
                    }

                    if (integrityIssues != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PaleGreenBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("ফলাফল বিবরণী:", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen, style = MaterialTheme.typography.bodySmall)
                                integrityIssues!!.forEach { issue ->
                                    Text("• $issue", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Data Backup & Export
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ডাটাবেজ ব্যাকআপ ও এক্সপোর্ট (Data Backup)", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                    Text("আপনার সমিতির সকল সদস্য ও অ্যাকাউন্টের তথ্যের একটি নিরাপদ JSON ব্যাকআপ ফাইল সংরক্ষণ করুন।", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                    Button(
                        onClick = {
                            scope.launch {
                                val json = viewModel.repository.exportBackupJson()
                                ReceiptExporter.shareReportCsv(context, "বন্ধু মহল সমিতি ডাটাবেজ ব্যাকআপ.json", json)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সম্পূর্ণ ডাটাবেজ ব্যাকআপ এক্সপোর্ট করুন", color = Color.White)
                    }
                }
            }
        }

        // Section 6: Activity Logs Navigation
        item {
            OutlinedButton(
                onClick = { viewModel.navigateTo(Screen.ActivityLogs) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("অ্যাক্টিভিটি ও অডিট লগ দেখুন (Activity Logs)")
            }
        }
    }

    if (showCreateAdminDialog) {
        CreateAdminDialog(
            onDismiss = { showCreateAdminDialog = false },
            onSubmit = { u, n, p, role, perms, pass ->
                viewModel.createAdmin(u, n, p, role, perms, pass) {
                    showCreateAdminDialog = false
                }
            }
        )
    }
}

@Composable
fun CreateAdminDialog(
    onDismiss: () -> Unit,
    onSubmit: (username: String, name: String, phone: String, role: String, permissions: String, pass: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var tempPassword by remember { mutableStateOf("") }

    var pMember by remember { mutableStateOf(true) }
    var pSavings by remember { mutableStateOf(true) }
    var pLoan by remember { mutableStateOf(true) }
    var pIncomeExp by remember { mutableStateOf(true) }
    var pReports by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("নতুন ম্যানেজার / অ্যাডমিন তৈরি", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                HorizontalDivider()

                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("ইউজারনেম *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("পূর্ণ নাম *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("মোবাইল নম্বর *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = tempPassword, onValueChange = { tempPassword = it }, label = { Text("অস্থায়ী পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর) *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Text("অ্যাডমিন পারমিশন নির্ধারণ করুন:", fontWeight = FontWeight.Bold, color = PrimaryGreen, modifier = Modifier.padding(top = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = pMember, onCheckedChange = { pMember = it })
                    Text("সদস্য তৈরি ও সম্পাদনা")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = pSavings, onCheckedChange = { pSavings = it })
                    Text("সঞ্চয় জমা ও হিসাব")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = pLoan, onCheckedChange = { pLoan = it })
                    Text("ঋণ ও কিস্তি আদায়")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = pIncomeExp, onCheckedChange = { pIncomeExp = it })
                    Text("আয় ও ব্যয় এন্ট্রি")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = pReports, onCheckedChange = { pReports = it })
                    Text("আর্থিক রিপোর্ট দেখা")
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            val permsList = mutableListOf<String>()
                            if (pMember) permsList.add("MEMBER")
                            if (pSavings) permsList.add("SAVINGS")
                            if (pLoan) permsList.add("LOAN")
                            if (pIncomeExp) permsList.add("INCOME_EXPENSE")
                            if (pReports) permsList.add("REPORTS")

                            if (username.isNotBlank() && name.isNotBlank() && tempPassword.length >= 6) {
                                onSubmit(username, name, phone, "ADMIN", permsList.joinToString(","), tempPassword)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                    ) { Text("অ্যাডমিন তৈরি করুন", color = Color.White) }
                }
            }
        }
    }
}
