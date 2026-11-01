package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.SomitiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: SomitiViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Admin, 1: Member
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val appSettings by viewModel.appSettings.collectAsState()
    val somitiName = appSettings?.somitiNameBn ?: "বন্ধু মহল সমিতি"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.bondhu_mohol_logo),
                contentDescription = "সমিতির লোগো",
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(3.dp, PrimaryDarkGreen, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = somitiName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryDarkGreen,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Bondhu Mohol Somiti Management System",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "সঞ্চয়, ঋণ ও সার্বিক সমবায় আর্থিক সমাধান",
                style = MaterialTheme.typography.labelMedium,
                color = AccentBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Login Mode Tabs: Admin vs Member
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = PaleGreenBg,
                contentColor = PrimaryDarkGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        identifier = ""
                        password = ""
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("অ্যাডমিন লগইন", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        identifier = ""
                        password = ""
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সদস্য লগইন", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (selectedTab == 0) "ইউজারনেম অথবা মোবাইল নম্বর" else "সদস্য আইডি (BMS-XXXX) অথবা মোবাইল নম্বর",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = identifier,
                        onValueChange = { identifier = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(if (selectedTab == 0) "উদাহরণ: Ahad অথবা 01711XXXXXX" else "উদাহরণ: BMS-0001 অথবা 01711XXXXXX")
                        },
                        leadingIcon = {
                            Icon(
                                if (selectedTab == 0) Icons.Default.Badge else Icons.Default.AccountBox,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (selectedTab == 1) KeyboardType.Text else KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "পাসওয়ার্ড / পিন",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("গোপন পাসওয়ার্ড লিখুন") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryGreen)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "লুকান" else "দেখান"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (identifier.isNotBlank() && password.isNotBlank()) {
                                    viewModel.login(identifier, password)
                                }
                            }
                        )
                    )

                    // Default admin hint helper
                    if (selectedTab == 0) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = PaleGreenBg
                        ) {
                            Text(
                                text = "প্রাথমিক সুপার অ্যাডমিন: Username: Ahad | Password: প্রথম সেটআপের পাসওয়ার্ড Ahad@2025",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryDarkGreen,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = SoftBlueBg
                        ) {
                            Text(
                                text = "টিপ: সদস্য আইডি (যেমন: BMS-0001) এবং নিবন্ধনের সময় দেওয়া মোবাইল নম্বর দিয়ে লগইন করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentBlue,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (identifier.isBlank()) {
                                viewModel.showSnackbar("অনুগ্রহ করে ইউজারনেম বা আইডি লিখুন")
                                return@Button
                            }
                            if (password.isBlank()) {
                                viewModel.showSnackbar("অনুগ্রহ করে পাসওয়ার্ড লিখুন")
                                return@Button
                            }
                            viewModel.login(identifier, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDarkGreen)
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedTab == 0) "অ্যাডমিন প্যানেলে প্রবেশ" else "সদস্য প্যানেলে প্রবেশ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Forgot Password Link
                    TextButton(
                        onClick = { showForgotPasswordDialog = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "পাসওয়ার্ড ভুলে গেছেন?",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text(
                    text = "পাসওয়ার্ড রিসেট ও সহায়তা",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkGreen
                )
            },
            text = {
                Column {
                    Text(
                        text = "নিরাপত্তাজনিত কারণে অ্যাপ থেকে সরাসরি পাসওয়ার্ড রিসেট সীমিত করা হয়েছে।",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. সাধারণ সদস্যদের পাসওয়ার্ড রিসেটের জন্য সমিতির সুপার অ্যাডমিনের সাথে যোগাযোগ করুন।\n" +
                                "২. সুপার অ্যাডমিন ইউজারদের জন্য সিস্টেম ডাটাবেজ ব্যাকআপ অথবা সমিতির কার্যালয়ে উপস্থিত হয়ে পাসওয়ার্ড রিসেট করতে হবে।\n\n" +
                                "হেল্পলাইন: ${appSettings?.phone ?: "০১৭০০-০০০০০০"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("ঠিক আছে", fontWeight = FontWeight.Bold, color = PrimaryDarkGreen)
                }
            }
        )
    }
}
