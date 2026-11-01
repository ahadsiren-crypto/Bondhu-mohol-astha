package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.SomitiViewModel
import com.example.util.BanglaUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(viewModel: SomitiViewModel) {
    val notifications by viewModel.notifications.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val useBangla = appSettings?.useBengaliNumerals ?: true

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, UNREAD

    // Filter by role & target
    val relevantNotifs = remember(notifications, currentUser, selectedFilter) {
        notifications.filter { n ->
            val matchRole = if (currentUser?.role == "MEMBER") {
                n.targetRole == "ALL" || n.targetRole == "MEMBER" || n.targetMemberId == currentUser?.memberId
            } else {
                n.targetRole == "ALL" || n.targetRole == "ADMIN"
            }
            val matchRead = if (selectedFilter == "UNREAD") !n.isRead else true
            matchRole && matchRead
        }
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বিজ্ঞপ্তি ও রিমাইন্ডার সেন্টার",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkGreen
                        )
                        TextButton(onClick = { viewModel.markAllNotificationsRead() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("সব পঠিত করুন")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("সব বিজ্ঞপ্তি (${BanglaUtils.formatNumber(notifications.size, useBangla)})") }
                        )
                        FilterChip(
                            selected = selectedFilter == "UNREAD",
                            onClick = { selectedFilter = "UNREAD" },
                            label = { Text("অপঠিত (${BanglaUtils.formatNumber(notifications.count { !it.isRead }, useBangla)})") }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (relevantNotifs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("কোনো নতুন বিজ্ঞপ্তি নেই", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(relevantNotifs, key = { it.id }) { notif ->
                    val icon = when (notif.type) {
                        "SAVINGS" -> Icons.Default.Savings
                        "LOAN" -> Icons.Default.CreditScore
                        "NOTICE" -> Icons.Default.Campaign
                        "MEMBER" -> Icons.Default.PersonAdd
                        else -> Icons.Default.Notifications
                    }
                    val iconColor = when (notif.type) {
                        "SAVINGS" -> PrimaryDarkGreen
                        "LOAN" -> AccentBlue
                        "NOTICE" -> AccentGold
                        else -> PrimaryGreen
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.markNotificationRead(notif.id)
                                // Navigate if appropriate
                                when (notif.type) {
                                    "SAVINGS" -> viewModel.navigateTo(Screen.Savings)
                                    "LOAN" -> viewModel.navigateTo(Screen.Loans)
                                    "NOTICE" -> viewModel.navigateTo(Screen.Notices)
                                    "MEMBER" -> viewModel.navigateTo(Screen.Members)
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!notif.isRead) PaleGreenBg.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (!notif.isRead) 2.dp else 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = iconColor.copy(alpha = 0.12f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.padding(10.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    if (!notif.isRead) {
                                        Box(modifier = Modifier.size(8.dp).background(PrimaryDarkGreen, CircleShape))
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (!notif.isRead) TextPrimary else TextSecondary
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = BanglaUtils.formatDateTime(notif.createdAt, useBangla),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
