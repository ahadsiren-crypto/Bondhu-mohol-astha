package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val category: String = "GENERAL", // "GENERAL", "SAVINGS_DIRECTIVE", "MEETING", "EMERGENCY"
    val publishedBy: String,
    val date: String,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val targetRole: String = "ALL", // "ALL", "ADMIN", "MEMBER"
    val targetMemberId: String? = null,
    val title: String,
    val message: String,
    val type: String, // "MEMBER", "SAVINGS", "LOAN", "INSTALLMENT", "INCOME", "EXPENSE", "NOTICE", "REMINDER", "SYSTEM"
    val relatedId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val userName: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "member_messages")
data class MemberMessageEntity(
    @PrimaryKey
    val id: String,
    val memberId: String,
    val memberName: String,
    val subject: String,
    val message: String,
    val date: String,
    val status: String = "UNREAD", // "UNREAD", "REPLIED"
    val reply: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: String = "GLOBAL_CONFIG",
    val somitiNameBn: String = "বন্ধু মহল সমিতি",
    val somitiNameEn: String = "Bondhu Mohol Somiti",
    val address: String = "ঢাকা, বাংলাদেশ",
    val phone: String = "০১৭০০-০০০০০০",
    val currencySymbol: String = "৳",
    val defaultMonthlySavings: Double = 1000.0,
    val useBengaliNumerals: Boolean = true,
    val isDarkTheme: Boolean = false,
    val pushNotificationEnabled: Boolean = true,
    val logoAsset: String = "bondhu_mohol_logo",
    val updatedAt: Long = System.currentTimeMillis()
)
