package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String, // "Ahad" or member mobile/ID
    val passwordHash: String,
    val salt: String,
    val role: String, // "SUPER_ADMIN", "ADMIN", "MEMBER"
    val name: String,
    val phone: String,
    val memberId: String? = null,
    val isActive: Boolean = true,
    val forcePasswordChange: Boolean = false,
    val permissions: String = "", // Comma-separated permissions: e.g. "MEMBER_CREATE,SAVINGS_DEPOSIT,LOAN_MANAGE,INCOME_EXPENSE,REPORTS"
    val failedLoginAttempts: Int = 0,
    val lockedUntil: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
