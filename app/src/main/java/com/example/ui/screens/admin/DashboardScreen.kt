package com.example.ui.screens.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChartPoint
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.SomitiViewModel
import com.example.util.BanglaUtils

data class DashboardCardItem(
    val title: String,
    val value: String,
    val subtitle: String = "",
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconColor: Color,
    val targetScreen: Screen
)

data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val targetScreen: Screen
)

@Composable
fun DashboardScreen(viewModel: SomitiViewModel) {
    val stats by viewModel.dashboardStats.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val useBangla = appSettings?.useBengaliNumerals ?: true
    val currency = appSettings?.currencySymbol ?: "৳"

    val members by viewModel.members.collectAsState()
    val savings by viewModel.savingsList.collectAsState()
    val loans by viewModel.loans.collectAsState()
    val installmentPayments by viewModel.installmentPayments.collectAsState()
    val incomes by viewModel.incomes.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    // Dashboard Cards 1 to 15
    val cards = listOf(
        DashboardCardItem(
            title = "মোট সদস্য",
            value = BanglaUtils.formatNumber(stats.totalMembers, useBangla),
            subtitle = "নিবন্ধিত সকল সদস্য",
            icon = Icons.Default.Groups,
            iconBgColor = PaleGreenBg,
            iconColor = PrimaryDarkGreen,
            targetScreen = Screen.Members
        ),
        DashboardCardItem(
            title = "সক্রিয় সদস্য",
            value = BanglaUtils.formatNumber(stats.activeMembers, useBangla),
            subtitle = "নিয়মিত সঞ্চয়কারী",
            icon = Icons.Default.CheckCircle,
            iconBgColor = PaleGreenBg,
            iconColor = PrimaryGreen,
            targetScreen = Screen.Members
        ),
        DashboardCardItem(
            title = "নিষ্ক্রিয় সদস্য",
            value = BanglaUtils.formatNumber(stats.inactiveMembers, useBangla),
            subtitle = "স্থগিত সদস্য হিসাব",
            icon = Icons.Default.PauseCircle,
            iconBgColor = SoftGoldBg,
            iconColor = WarningOrange,
            targetScreen = Screen.Members
        ),
        DashboardCardItem(
            title = "আজকের সঞ্চয় জমা",
            value = BanglaUtils.formatCurrency(stats.todaySavings, useBangla, currency),
            subtitle = "দৈনিক সঞ্চয় আদায়",
            icon = Icons.Default.Savings,
            iconBgColor = PaleGreenBg,
            iconColor = PrimaryDarkGreen,
            targetScreen = Screen.Savings
        ),
        DashboardCardItem(
            title = "আজকের মোট আয়",
            value = BanglaUtils.formatCurrency(stats.todayIncome, useBangla, currency),
            subtitle = "ফি, চার্জ ও সুদ বাবদ",
            icon = Icons.Default.TrendingUp,
            iconBgColor = SoftBlueBg,
            iconColor = AccentBlue,
            targetScreen = Screen.IncomeExpense
        ),
        DashboardCardItem(
            title = "আজকের মোট ব্যয়",
            value = BanglaUtils.formatCurrency(stats.todayExpense, useBangla, currency),
            subtitle = "দৈনিক প্রাতিষ্ঠানিক খরচ",
            icon = Icons.Default.TrendingDown,
            iconBgColor = ErrorSoftBg,
            iconColor = ErrorRed,
            targetScreen = Screen.IncomeExpense
        ),
        DashboardCardItem(
            title = "চলতি মাসের মোট সঞ্চয়",
            value = BanglaUtils.formatCurrency(stats.thisMonthSavings, useBangla, currency),
            subtitle = "এই মাসের মোট আমানত",
            icon = Icons.Default.AccountBalanceWallet,
            iconBgColor = PaleGreenBg,
            iconColor = PrimaryGreen,
            targetScreen = Screen.Savings
        ),
        DashboardCardItem(
            title = "মোট সঞ্চয় দায় (Liability)",
            value = BanglaUtils.formatCurrency(stats.totalSavingsLiability, useBangla, currency),
            subtitle = "সদস্যদের সংরক্ষিত আমানত",
            icon = Icons.Default.Security,
            iconBgColor = SoftGoldBg,
            iconColor = AccentGold,
            targetScreen = Screen.Savings
        ),
        DashboardCardItem(
            title = "মোট বিতরণ করা ঋণ",
            value = BanglaUtils.formatCurrency(stats.totalDisbursedLoans, useBangla, currency),
            subtitle = "বিতরণকৃত আসল ঋণ",
            icon = Icons.Default.CreditScore,
            iconBgColor = SoftBlueBg,
            iconColor = AccentBlue,
            targetScreen = Screen.Loans
        ),
        DashboardCardItem(
            title = "বর্তমানে বকেয়া ঋণ",
            value = BanglaUtils.formatCurrency(stats.currentLoanDue, useBangla, currency),
            subtitle = "আদায়যোগ্য অবশিষ্ট আসল",
            icon = Icons.Default.PendingActions,
            iconBgColor = ErrorSoftBg,
            iconColor = ErrorRed,
            targetScreen = Screen.Loans
        ),
        DashboardCardItem(
            title = "চলতি মাসে আদায়কৃত কিস্তি",
            value = BanglaUtils.formatCurrency(stats.thisMonthInstallments, useBangla, currency),
            subtitle = "এই মাসে আদায়",
            icon = Icons.Default.PriceCheck,
            iconBgColor = PaleGreenBg,
            iconColor = PrimaryGreen,
            targetScreen = Screen.Loans
        ),
        DashboardCardItem(
            title = "মোট বকেয়া কিস্তি",
            value = BanglaUtils.formatCurrency(stats.totalInstallmentDue, useBangla, currency),
            subtitle = "অনাদায়ী ঋণের মোট পরিমাণ",
            icon = Icons.Default.ReportProblem,
            iconBgColor = ErrorSoftBg,
            iconColor = WarningOrange,
            targetScreen = Screen.Loans
        ),
        DashboardCardItem(
            title = "নগদ ব্যালেন্স (Cash)",
            value = BanglaUtils.formatCurrency(stats.cashBalance, useBangla, currency),
            subtitle = "অফিস ক্যাশ বক্স",
            icon = Icons.Default.AttachMoney,
            iconBgColor = PaleGreenBg,
            iconColor = PrimaryDarkGreen,
            targetScreen = Screen.Accounts
        ),
        DashboardCardItem(
            title = "ব্যাংক ব্যালেন্স (Bank)",
            value = BanglaUtils.formatCurrency(stats.bankBalance, useBangla, currency),
            subtitle = "ব্যাংক জমা স্থিতি",
            icon = Icons.Default.AccountBalance,
            iconBgColor = SoftBlueBg,
            iconColor = AccentBlue,
            targetScreen = Screen.Accounts
        ),
        DashboardCardItem(
            title = "বিকাশ ও অন্যান্য একাউন্ট",
            value = BanglaUtils.formatCurrency(stats.mobileBankBalance, useBangla, currency),
            subtitle = "bKash, Nagad, Rocket",
            icon = Icons.Default.Smartphone,
            iconBgColor = SoftGoldBg,
            iconColor = AccentGold,
            targetScreen = Screen.Accounts
        )
    )

    val quickActions = listOf(
        QuickActionItem("নতুন সদস্য", Icons.Default.PersonAdd, PrimaryDarkGreen, Screen.Members),
        QuickActionItem("সঞ্চয় জমা", Icons.Default.Savings, PrimaryGreen, Screen.Savings),
        QuickActionItem("ঋণ আবেদন", Icons.Default.PostAdd, AccentBlue, Screen.Loans),
        QuickActionItem("ঋণ বিতরণ", Icons.Default.Payment, AccentGold, Screen.Loans),
        QuickActionItem("কিস্তি আদায়", Icons.Default.PriceCheck, PrimaryDarkGreen, Screen.Loans),
        QuickActionItem("নতুন আয়", Icons.Default.TrendingUp, AccentBlue, Screen.IncomeExpense),
        QuickActionItem("নতুন ব্যয়", Icons.Default.TrendingDown, ErrorRed, Screen.IncomeExpense),
        QuickActionItem("রিপোর্ট দেখুন", Icons.Default.Assessment, PrimaryGreen, Screen.Reports)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scrollable content inside LazyVerticalGrid with header sections
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Net Total Balance Banner (Span full width)
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryDarkGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "সমিতির সর্বমোট নগদ ও ব্যাংক স্থিতি",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = BanglaUtils.formatCurrency(stats.totalNetBalance, useBangla, currency),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "সার্বিক লাভ/ক্ষতি: ${BanglaUtils.formatCurrency(stats.netProfitLoss, useBangla, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (stats.netProfitLoss >= 0) SoftGreen else ErrorSoftBg
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(28.dp)
                            )
                        }
                    }
                }
            }

            // Quick Actions Section Title
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "দ্রুত কার্যক্রম (Quick Actions)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkGreen,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Quick Actions Horizontal Row / Grid items
            items(quickActions) { action ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.navigateTo(action.targetScreen) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = action.color.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                action.icon,
                                contentDescription = action.title,
                                tint = action.color,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = action.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Dashboard Analytics & Cards Section Title
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "সমিতির সার্বিক আর্থিক অবস্থা (Dashboard Statistics)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkGreen,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }

            // 15 Dashboard Cards
            items(cards) { card ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.navigateTo(card.targetScreen) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = card.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = CircleShape,
                                color = card.iconBgColor,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    card.icon,
                                    contentDescription = null,
                                    tint = card.iconColor,
                                    modifier = Modifier
                                        .padding(7.dp)
                                        .size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = card.value,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (card.subtitle.isNotBlank()) {
                            Text(
                                text = card.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Charts Section Title
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "আর্থিক বিশ্লেষণ চার্ট (Analytics & Trends)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkGreen,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }

            // Chart 1: Loan Disbursed vs Collected (Custom Canvas)
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ঋণ বিতরণ বনাম ঋণ আদায় অনুপাত",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val totalDisbursed = stats.totalDisbursedLoans
                        val totalPaid = loans.sumOf { it.paidAmount }
                        val remaining = stats.currentLoanDue

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("মোট বিতরণ", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(
                                    BanglaUtils.formatCurrency(totalDisbursed, useBangla, currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("মোট আদায়", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(
                                    BanglaUtils.formatCurrency(totalPaid, useBangla, currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("বকেয়া", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(
                                    BanglaUtils.formatCurrency(remaining, useBangla, currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual Progress Bar
                        val recoveryRatio = if (totalDisbursed > 0) (totalPaid / totalDisbursed).toFloat().coerceIn(0f, 1f) else 0f
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ঋণ আদায়ের হার", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(
                                    "${BanglaUtils.formatNumber((recoveryRatio * 100).toInt(), useBangla)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDarkGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { recoveryRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = PrimaryGreen,
                                trackColor = PaleGreenBg
                            )
                        }
                    }
                }
            }

            // Chart 2: Income vs Expense Breakdown (Custom Canvas Bar Chart)
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "মোট আয় বনাম মোট ব্যয় অনুপাত",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val totalInc = stats.totalIncomeAllTime
                        val totalExp = stats.totalExpenseAllTime
                        val maxVal = maxOf(totalInc, totalExp, 1.0)

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                        ) {
                            val barHeight = 24.dp.toPx()
                            val spacing = 16.dp.toPx()
                            val cornerRadius = 6.dp.toPx()

                            val incWidth = ((totalInc / maxVal) * size.width).toFloat().coerceAtLeast(10f)
                            val expWidth = ((totalExp / maxVal) * size.width).toFloat().coerceAtLeast(10f)

                            // Income bar
                            drawRoundRect(
                                color = AccentBlue,
                                topLeft = Offset(0f, 10f),
                                size = Size(incWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                            )

                            // Expense bar
                            drawRoundRect(
                                color = ErrorRed,
                                topLeft = Offset(0f, 10f + barHeight + spacing),
                                size = Size(expWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(AccentBlue, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "মোট আয়: ${BanglaUtils.formatCurrency(totalInc, useBangla, currency)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(ErrorRed, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "মোট ব্যয়: ${BanglaUtils.formatCurrency(totalExp, useBangla, currency)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
