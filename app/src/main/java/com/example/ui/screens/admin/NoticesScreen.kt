package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.MemberMessageEntity
import com.example.data.local.NoticeEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomitiViewModel
import com.example.util.BanglaUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen(viewModel: SomitiViewModel) {
    val notices by viewModel.notices.collectAsState()
    val memberMessages by viewModel.memberMessages.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val useBangla = appSettings?.useBengaliNumerals ?: true

    var selectedTab by remember { mutableIntStateOf(0) }
    var showPublishDialog by remember { mutableStateOf(false) }
    var replyingMessage by remember { mutableStateOf<MemberMessageEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showPublishDialog = true },
                    containerColor = PrimaryDarkGreen,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                    text = { Text("নতুন নোটিশ প্রকাশ", fontWeight = FontWeight.Bold) }
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryDarkGreen
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("নোটিশ বোর্ড (${BanglaUtils.formatNumber(notices.size, useBangla)})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("সদস্যদের বার্তা ও অভিযোগ (${BanglaUtils.formatNumber(memberMessages.size, useBangla)})", fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedTab) {
                0 -> {
                    if (notices.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("কোনো প্রকাশিত নোটিশ নেই। নিচে 'নতুন নোটিশ প্রকাশ' চাপুন।", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(notices) { notice ->
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (notice.isPinned) {
                                                    Icon(Icons.Default.PushPin, contentDescription = "পিন করা", tint = AccentGold, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(notice.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            }
                                            IconButton(onClick = { viewModel.deleteNotice(notice) }) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "মুছুন", tint = ErrorRed)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(notice.content, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)

                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = DividerColor)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("প্রকাশক: ${notice.publishedBy}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                            Text("তারিখ: ${notice.date}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (memberMessages.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("সদস্যদের কাছ থেকে কোনো অভিযোগ বা বার্তা নেই।", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(memberMessages) { msg ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(msg.subject, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = if (msg.status == "REPLIED") PaleGreenBg else SoftGoldBg
                                            ) {
                                                Text(
                                                    text = if (msg.status == "REPLIED") "উত্তর দেওয়া হয়েছে" else "অপেক্ষমান",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (msg.status == "REPLIED") PrimaryDarkGreen else WarningOrange,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("প্রেরক: ${msg.memberName} (${msg.memberId}) | তারিখ: ${msg.date}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(msg.message, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)

                                        if (msg.reply.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(shape = RoundedCornerShape(8.dp), color = PaleGreenBg, modifier = Modifier.fillMaxWidth()) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text("অ্যাডমিনের উত্তর:", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen, style = MaterialTheme.typography.labelSmall)
                                                    Text(msg.reply, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                                }
                                            }
                                        }

                                        if (msg.status != "REPLIED") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedButton(
                                                onClick = { replyingMessage = msg },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("উত্তর দিন")
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

    if (showPublishDialog) {
        PublishNoticeDialog(
            onDismiss = { showPublishDialog = false },
            onSubmit = { title, content, cat, pinned ->
                viewModel.publishNotice(title, content, cat, pinned) {
                    showPublishDialog = false
                }
            }
        )
    }

    if (replyingMessage != null) {
        val targetMsg = replyingMessage!!
        ReplyMessageDialog(
            msg = targetMsg,
            onDismiss = { replyingMessage = null },
            onSubmit = { reply ->
                viewModel.replyMemberMessage(targetMsg, reply) {
                    replyingMessage = null
                }
            }
        )
    }
}

@Composable
fun PublishNoticeDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, content: String, category: String, pinned: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("সাধারণ ঘোষণা") }
    val categories = listOf("সাধারণ ঘোষণা", "মাসিক সঞ্চয়ের নির্দেশনা", "কিস্তি সংক্রান্ত", "সভার তারিখ", "জরুরি বিজ্ঞপ্তি")
    var isPinned by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("নতুন নোটিশ প্রকাশ করুন", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                HorizontalDivider()

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("নোটিশের শিরোনাম *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Text("নোটিশের ধরন:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.take(3).forEach { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                    }
                }

                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("নোটিশের বিস্তারিত বিবরণ *") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 6)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("নোটিশটি শীর্ষে পিন করে রাখুন")
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onSubmit(title, content, category, isPinned)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                    ) { Text("প্রকাশ করুন", color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun ReplyMessageDialog(
    msg: MemberMessageEntity,
    onDismiss: () -> Unit,
    onSubmit: (reply: String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("সদস্যের বার্তার উত্তর দিন", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                Text("সদস্য: ${msg.memberName} (${msg.memberId})\nবিষয়: ${msg.subject}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                HorizontalDivider()

                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("আপনার উত্তর লিখুন *") },
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    maxLines = 5
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onSubmit(replyText)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                    ) { Text("উত্তর পাঠান", color = Color.White) }
                }
            }
        }
    }
}
