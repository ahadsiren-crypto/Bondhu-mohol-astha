package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "members",
    indices = [Index(value = ["memberId"], unique = true), Index(value = ["mobile"])]
)
data class MemberEntity(
    @PrimaryKey
    val id: String,
    val memberId: String, // e.g. "BMS-0001"
    val name: String,
    val fatherName: String = "",
    val motherName: String = "",
    val mobile: String,
    val altMobile: String = "",
    val address: String = "",
    val village: String = "",
    val upazila: String = "",
    val district: String = "",
    val joiningDate: String, // DD-MM-YYYY
    val nomineeName: String = "",
    val nomineeRelation: String = "",
    val nomineeMobile: String = "",
    val photoUri: String? = null,
    val monthlySavings: Double = 1000.0,
    val status: String = "ACTIVE", // "ACTIVE", "INACTIVE"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
