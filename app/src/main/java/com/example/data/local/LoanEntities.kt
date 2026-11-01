package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "loan_applications",
    indices = [Index(value = ["applicationId"], unique = true), Index(value = ["memberId"])]
)
data class LoanApplicationEntity(
    @PrimaryKey
    val id: String,
    val applicationId: String, // e.g. "APP-0001"
    val memberId: String,
    val applyDate: String,
    val amount: Double,
    val purpose: String,
    val durationMonths: Int,
    val installmentsCount: Int,
    val guarantorName: String,
    val guarantorMobile: String,
    val guarantorRelation: String,
    val remarks: String = "",
    val status: String = "APPLIED", // "APPLIED", "UNDER_REVIEW", "APPROVED", "REJECTED", "DISBURSED"
    val reviewedBy: String = "",
    val reviewNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "loans",
    indices = [Index(value = ["loanId"], unique = true), Index(value = ["memberId"])]
)
data class LoanEntity(
    @PrimaryKey
    val id: String,
    val loanId: String, // e.g. "LN-0001"
    val applicationId: String,
    val memberId: String,
    val principalAmount: Double,
    val interestRate: Double = 0.0, // percentage e.g. 0% or 5%
    val serviceCharge: Double = 0.0,
    val totalRepayable: Double, // principal + interest
    val paidAmount: Double = 0.0,
    val remainingAmount: Double,
    val disbursementDate: String,
    val accountId: String,
    val paymentMethod: String,
    val installmentsCount: Int,
    val perInstallmentAmount: Double,
    val status: String = "DISBURSED", // "DISBURSED", "REPAYING", "FULLY_REPAID", "OVERDUE"
    val disbursedBy: String,
    val receiptNumber: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "loan_installments",
    indices = [Index(value = ["loanId", "installmentNumber"], unique = true), Index(value = ["memberId"])]
)
data class LoanInstallmentEntity(
    @PrimaryKey
    val id: String,
    val loanId: String,
    val memberId: String,
    val installmentNumber: Int,
    val scheduledAmount: Double,
    val collectedAmount: Double = 0.0,
    val dueDate: String,
    val paidDate: String? = null,
    val status: String = "DUE" // "PAID", "PARTIAL", "DUE", "LATE"
)

@Entity(
    tableName = "installment_payments",
    indices = [Index(value = ["loanId"]), Index(value = ["memberId"])]
)
data class InstallmentPaymentEntity(
    @PrimaryKey
    val id: String,
    val loanId: String,
    val memberId: String,
    val installmentNumber: Int,
    val amount: Double,
    val paymentDate: String,
    val accountId: String,
    val paymentMethod: String,
    val transactionId: String = "",
    val collectorName: String,
    val remarks: String = "",
    val receiptNumber: String,
    val status: String = "VALID", // "VALID", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis()
)
