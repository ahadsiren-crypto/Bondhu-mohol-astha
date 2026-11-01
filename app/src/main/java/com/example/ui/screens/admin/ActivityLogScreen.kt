package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomitiViewModel
import com.example.util.BanglaUtils

@Composable
fun ActivityLogScreen(viewModel: SomitiViewModel) {
    val logs by viewModel.activityLogs.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val useBangla = appSettings?.useBengaliNumerals ?: true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "অ্যাক্টিভিটি ও অডিট লগ (Audit Trail)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryDarkGreen
            )
            Text(
                text = "সিস্টেমে কে কখন কী পরিবর্তন করেছে তার পূর্ণাঙ্গ অপরিবর্তনীয় ইতিহাস।",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        if (logs.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Text("কোনো লগ রেকর্ড নেই।", modifier = Modifier.padding(16.dp), color = TextSecondary)
                }
            }
        } else {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.userName, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                            Text(BanglaUtils.formatDateTime(log.timestamp, useBangla), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(log.action, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
                        if (log.details.isNotBlank()) {
                            Text(log.details, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
