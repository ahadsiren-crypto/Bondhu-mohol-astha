package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.SomitiViewModel
import com.example.util.BanglaUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(viewModel: SomitiViewModel) {
    val members by viewModel.members.collectAsState()
    val savingsList by viewModel.savingsList.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()
    val loans by viewModel.loans.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val useBangla = appSettings?.useBengaliNumerals ?: true
    val currency = appSettings?.currencySymbol ?: "৳"

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, ACTIVE, INACTIVE, SAVINGS_DUE, LOAN_DUE
    var showCreateDialog by remember { mutableStateOf(false) }

    // Filter members
    val filteredMembers = remember(members, searchQuery, selectedFilter, savingsList, loans) {
        members.filter { m ->
            val matchQuery = searchQuery.isBlank() ||
                    m.name.contains(searchQuery, ignoreCase = true) ||
                    m.memberId.contains(searchQuery, ignoreCase = true) ||
                    m.mobile.contains(searchQuery, ignoreCase = true)

            val matchFilter = when (selectedFilter) {
                "ACTIVE" -> m.status == "ACTIVE"
                "INACTIVE" -> m.status == "INACTIVE"
                "LOAN_DUE" -> {
                    val mLoans = loans.filter { it.memberId == m.memberId && it.status != "FULLY_REPAID" }
                    mLoans.any { it.remainingAmount > 0 }
                }
                "SAVINGS_DUE" -> {
                    // Check if member has less savings than expected
                    true
                }
                else -> true
            }

            matchQuery && matchFilter
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = PrimaryDarkGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("নতুন সদস্য যুক্ত করুন", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar & Filter Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("নাম, সদস্য আইডি বা মোবাইল দিয়ে খুঁজুন...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryGreen) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "মুছুন")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("সব সদস্য (${BanglaUtils.formatNumber(members.size, useBangla)})") }
                        )
                        FilterChip(
                            selected = selectedFilter == "ACTIVE",
                            onClick = { selectedFilter = "ACTIVE" },
                            label = { Text("সক্রিয় (${BanglaUtils.formatNumber(members.count { it.status == "ACTIVE" }, useBangla)})") }
                        )
                        FilterChip(
                            selected = selectedFilter == "INACTIVE",
                            onClick = { selectedFilter = "INACTIVE" },
                            label = { Text("নিষ্ক্রিয় (${BanglaUtils.formatNumber(members.count { it.status == "INACTIVE" }, useBangla)})") }
                        )
                        FilterChip(
                            selected = selectedFilter == "LOAN_DUE",
                            onClick = { selectedFilter = "LOAN_DUE" },
                            label = { Text("ঋণের বকেয়া") }
                        )
                    }
                }
            }

            // Member List or Empty State
            if (filteredMembers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "কোনো সদস্য পাওয়া যায়নি" else "এখনো কোনো সদস্য যোগ করা হয়নি",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "নতুন সদস্য যুক্ত করতে নিচের বাটনে চাপ দিন",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredMembers, key = { it.id }) { member ->
                        // Calculate total savings for this member
                        val memSavings = savingsList.filter { it.memberId == member.memberId && it.status == "VALID" }.sumOf { it.amount }
                        val memWds = withdrawals.filter { it.memberId == member.memberId && it.status == "APPROVED" }.sumOf { it.amount }
                        val totalNetSavings = maxOf(0.0, memSavings - memWds)

                        // Calculate active loans for this member
                        val memLoans = loans.filter { it.memberId == member.memberId && it.status != "FULLY_REPAID" }
                        val totalLoanDue = memLoans.sumOf { it.remainingAmount }

                        MemberCard(
                            member = member,
                            totalSavings = totalNetSavings,
                            loanDue = totalLoanDue,
                            useBangla = useBangla,
                            currency = currency,
                            onDetailsClick = {
                                viewModel.navigateTo(Screen.MemberDetail(member.memberId))
                            }
                        )
                    }
                }
            }
        }
    }

    // Create Member Dialog
    if (showCreateDialog) {
        CreateMemberDialog(
            accounts = accounts,
            onDismiss = { showCreateDialog = false },
            onSubmit = { name, father, mother, mobile, alt, addr, vil, upa, dist, date, nomName, nomRel, nomMobile, monthlySav, fee, accId ->
                viewModel.createMember(
                    name = name,
                    father = father,
                    mother = mother,
                    mobile = mobile,
                    altMobile = alt,
                    address = addr,
                    village = vil,
                    upazila = upa,
                    district = dist,
                    joiningDate = date,
                    nomineeName = nomName,
                    nomineeRel = nomRel,
                    nomineeMobile = nomMobile,
                    monthlySavings = monthlySav,
                    admissionFee = fee,
                    accountId = accId,
                    onDone = { showCreateDialog = false }
                )
            }
        )
    }
}

@Composable
fun MemberCard(
    member: MemberEntity,
    totalSavings: Double,
    loanDue: Double,
    useBangla: Boolean,
    currency: String,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onDetailsClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Member ID badge & Status chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryDarkGreen
                ) {
                    Text(
                        text = member.memberId,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (member.status == "ACTIVE") PaleGreenBg else SoftGoldBg
                ) {
                    Text(
                        text = if (member.status == "ACTIVE") "সক্রিয়" else "নিষ্ক্রিয়",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (member.status == "ACTIVE") PrimaryDarkGreen else WarningOrange,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Member Info Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = PaleGreenBg,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryDarkGreen,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "মোবাইল: ${member.mobile}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "যোগদান: ${member.joiningDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(10.dp))

            // Financial Summary Row: Monthly Target, Total Savings, Loan Due
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("মাসিক সঞ্চয়", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        BanglaUtils.formatCurrency(member.monthlySavings, useBangla, currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("মোট সঞ্চয়", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        BanglaUtils.formatCurrency(totalSavings, useBangla, currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("ঋণ বকেয়া", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        BanglaUtils.formatCurrency(loanDue, useBangla, currency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (loanDue > 0) ErrorRed else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Action Button
            OutlinedButton(
                onClick = onDetailsClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("বিস্তারিত প্রোফাইল ও স্টেটমেন্ট দেখুন")
            }
        }
    }
}

@Composable
fun CreateMemberDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSubmit: (
        name: String, father: String, mother: String, mobile: String, alt: String,
        addr: String, vil: String, upa: String, dist: String, date: String,
        nomName: String, nomRel: String, nomMobile: String, monthlySav: Double,
        fee: Double?, accId: String?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var father by remember { mutableStateOf("") }
    var mother by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var altMobile by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var upazila by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var joiningDate by remember { mutableStateOf(BanglaUtils.formatDate(System.currentTimeMillis(), false)) }
    var nomineeName by remember { mutableStateOf("") }
    var nomineeRel by remember { mutableStateOf("") }
    var nomineeMobile by remember { mutableStateOf("") }
    var monthlySavingsStr by remember { mutableStateOf("1000") }
    var admissionFeeStr by remember { mutableStateOf("100") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "নতুন সদস্য নিবন্ধন",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkGreen
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ErrorSoftBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Text("সদস্যের প্রাথমিক তথ্য", fontWeight = FontWeight.Bold, color = PrimaryGreen)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("সদস্যের পুরো নাম *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = father,
                            onValueChange = { father = it },
                            label = { Text("পিতার নাম") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = mother,
                            onValueChange = { mother = it },
                            label = { Text("মাতার নাম") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("মোবাইল নম্বর *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = altMobile,
                            onValueChange = { altMobile = it },
                            label = { Text("বিকল্প মোবাইল") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }

                    Text("ঠিকানা ও অঞ্চল", fontWeight = FontWeight.Bold, color = PrimaryGreen, modifier = Modifier.padding(top = 6.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("পূর্ণ ঠিকানা / বাসা / রোড") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = village,
                            onValueChange = { village = it },
                            label = { Text("গ্রাম / পাড়া") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = upazila,
                            onValueChange = { upazila = it },
                            label = { Text("উপজেলা / থানা") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = { Text("জেলা") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text("নমিনি সংক্রান্ত তথ্য", fontWeight = FontWeight.Bold, color = PrimaryGreen, modifier = Modifier.padding(top = 6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nomineeName,
                            onValueChange = { nomineeName = it },
                            label = { Text("নমিনির নাম") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = nomineeRel,
                            onValueChange = { nomineeRel = it },
                            label = { Text("সম্পর্ক") },
                            modifier = Modifier.weight(0.8f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = nomineeMobile,
                        onValueChange = { nomineeMobile = it },
                        label = { Text("নমিনির মোবাইল নম্বর") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Text("সঞ্চয় ও পেমেন্ট সেটিংস", fontWeight = FontWeight.Bold, color = PrimaryGreen, modifier = Modifier.padding(top = 6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = monthlySavingsStr,
                            onValueChange = { monthlySavingsStr = it },
                            label = { Text("মাসিক সঞ্চয় (৳) *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = admissionFeeStr,
                            onValueChange = { admissionFeeStr = it },
                            label = { Text("ভর্তি ফি (৳)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    // Account select for Admission fee
                    if (accounts.isNotEmpty()) {
                        Text("ফি জমা গ্রহণের অ্যাকাউন্ট", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            accounts.forEach { acc ->
                                FilterChip(
                                    selected = selectedAccountId == acc.id,
                                    onClick = { selectedAccountId = acc.id },
                                    label = { Text(acc.name, maxLines = 1) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = joiningDate,
                        onValueChange = { joiningDate = it },
                        label = { Text("যোগদানের তারিখ (DD-MM-YYYY)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("বাতিল")
                    }
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "অনুগ্রহ করে সদস্যের পুরো নাম লিখুন"
                                return@Button
                            }
                            if (mobile.isBlank() || mobile.length < 10) {
                                errorMessage = "অনুগ্রহ করে সঠিক মোবাইল নম্বর দিন (কমপক্ষে ১০ ডিজিট)"
                                return@Button
                            }
                            val monthlySav = monthlySavingsStr.toDoubleOrNull() ?: 1000.0
                            val fee = admissionFeeStr.toDoubleOrNull()

                            onSubmit(
                                name, father, mother, mobile, altMobile, address, village,
                                upazila, district, joiningDate, nomineeName, nomineeRel, nomineeMobile,
                                monthlySav, fee, selectedAccountId
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                    ) {
                        Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
