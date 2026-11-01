package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.common.NotificationCenterScreen
import com.example.ui.common.ReceiptDialog
import com.example.ui.screens.admin.*
import com.example.ui.screens.member.MemberDashboardScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.SomitiViewModel

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val screen: Screen,
    val badgeCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: SomitiViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeReceipt by viewModel.activeReceipt.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val showPasswordChangeDialog by viewModel.showPasswordChangeDialog.collectAsState()
    val unreadNotifCount by viewModel.unreadNotifCount.collectAsState()
    val pendingApprovalsCount by viewModel.pendingApprovalsCount.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Hardware back press handling
    BackHandler(enabled = currentScreen != Screen.Login && currentScreen != Screen.AdminDashboard && currentScreen != Screen.MemberDashboard) {
        viewModel.navigateBack()
    }

    if (currentUser == null || currentScreen == Screen.Login) {
        LoginScreen(viewModel = viewModel)
        return
    }

    val isMember = currentUser?.role == "MEMBER"

    val adminNavItems = listOf(
        NavItem("ড্যাশবোর্ড", Icons.Default.Dashboard, Screen.AdminDashboard),
        NavItem("সদস্য", Icons.Default.Groups, Screen.Members),
        NavItem("সঞ্চয়", Icons.Default.Savings, Screen.Savings),
        NavItem("ঋণ", Icons.Default.CreditScore, Screen.Loans),
        NavItem("আয়/ব্যয়", Icons.Default.AccountBalanceWallet, Screen.IncomeExpense),
        NavItem("অ্যাকাউন্ট", Icons.Default.AccountBalance, Screen.Accounts),
        NavItem("রিপোর্ট", Icons.Default.Assessment, Screen.Reports),
        NavItem("নোটিশ", Icons.Default.Campaign, Screen.Notices),
        NavItem("সেটিংস", Icons.Default.Settings, Screen.Settings)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.bondhu_mohol_logo),
                            contentDescription = "সমিতির লোগো",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = appSettings?.somitiNameBn ?: "বন্ধু মহল সমিতি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isMember) "সদস্য প্যানেল" else "অ্যাডমিন প্যানেল (${currentUser?.name})",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    // Pending Approvals Icon for Admin
                    if (!isMember) {
                        IconButton(onClick = { viewModel.navigateTo(Screen.Approvals) }) {
                            BadgedBox(
                                badge = {
                                    if (pendingApprovalsCount > 0) {
                                        Badge(containerColor = ErrorRed) {
                                            Text(pendingApprovalsCount.toString(), color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.PendingActions, contentDescription = "অনুমোদন কেন্দ্র", tint = Color.White)
                            }
                        }
                    }

                    // Notification Bell Icon with Badge
                    IconButton(onClick = { viewModel.navigateTo(Screen.Notifications) }) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifCount > 0) {
                                    Badge(containerColor = ErrorRed) {
                                        Text(unreadNotifCount.toString(), color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "বিজ্ঞপ্তি", tint = Color.White)
                        }
                    }

                    // Logout Button
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "লগআউট", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDarkGreen,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            if (!isMember) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    val displayedNavItems = listOf(
                        adminNavItems[0], // ড্যাশবোর্ড
                        adminNavItems[1], // সদস্য
                        adminNavItems[2], // সঞ্চয়
                        adminNavItems[3], // ঋণ
                        adminNavItems[4], // আয়/ব্যয়
                        adminNavItems[6]  // রিপোর্ট
                    )
                    displayedNavItems.forEach { item ->
                        val isSelected = currentScreen == item.screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(item.screen) },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryDarkGreen,
                                selectedTextColor = PrimaryDarkGreen,
                                indicatorColor = PaleGreenBg
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentScreen) {
                is Screen.AdminDashboard -> DashboardScreen(viewModel = viewModel)
                is Screen.Members -> MembersScreen(viewModel = viewModel)
                is Screen.MemberDetail -> MemberDetailScreen(
                    memberId = (currentScreen as Screen.MemberDetail).memberId,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
                is Screen.Savings -> SavingsScreen(viewModel = viewModel)
                is Screen.Loans -> LoansScreen(viewModel = viewModel)
                is Screen.IncomeExpense -> IncomeExpenseScreen(viewModel = viewModel)
                is Screen.Accounts -> AccountsScreen(viewModel = viewModel)
                is Screen.Reports -> ReportsScreen(viewModel = viewModel)
                is Screen.Notices -> NoticesScreen(viewModel = viewModel)
                is Screen.Settings -> SettingsScreen(viewModel = viewModel)
                is Screen.Approvals -> ApprovalsScreen(viewModel = viewModel)
                is Screen.ActivityLogs -> ActivityLogScreen(viewModel = viewModel)
                is Screen.Notifications -> NotificationCenterScreen(viewModel = viewModel)
                is Screen.MemberDashboard -> MemberDashboardScreen(viewModel = viewModel)
                else -> DashboardScreen(viewModel = viewModel)
            }
        }
    }

    // Active Receipt Dialog
    if (activeReceipt != null) {
        ReceiptDialog(
            receipt = activeReceipt!!,
            appSettings = appSettings,
            onDismiss = { viewModel.closeReceipt() }
        )
    }

    // Mandatory Password Change Dialog (e.g. for initial Super Admin Ahad on first setup)
    if (showPasswordChangeDialog) {
        ForcePasswordChangeDialog(
            onSubmit = { newPass -> viewModel.changePassword(newPass) }
        )
    }
}

@Composable
fun ForcePasswordChangeDialog(onSubmit: (newPass: String) -> Unit) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "নতুন পাসওয়ার্ড নির্ধারণ আবশ্যক",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkGreen
                )
                Text(
                    text = "নিরাপত্তার স্বার্থে প্রাথমিক সেটআপ বা প্রথমবার লগইনের পর আপনার ব্যক্তিগত গোপন পাসওয়ার্ড পরিবর্তন বাধ্যতামূলক।",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                HorizontalDivider()

                if (errorMsg != null) {
                    Text(errorMsg!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("নতুন পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("নতুন পাসওয়ার্ড পুনরায় লিখুন *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (newPassword.length < 6) {
                            errorMsg = "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে"
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            errorMsg = "উভয় পাসওয়ার্ড মেলেনি! পুনরায় লিখুন।"
                            return@Button
                        }
                        onSubmit(newPassword)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                ) {
                    Text("পাসওয়ার্ড সংরক্ষণ করুন", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
