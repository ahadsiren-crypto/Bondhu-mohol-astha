package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val name: String, // e.g. "নগদ তহবিল (Cash)", "সোনালী ব্যাংক (Bank)", "বিকাশ (bKash)"
    val type: String, // "CASH", "BANK", "BKASH", "NAGAD", "ROCKET", "UPAY", "OTHER"
    val accountNumber: String = "",
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val totalDeposit: Double = 0.0,
    val totalWithdrawal: Double = 0.0,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "savings",
    indices = [Index(value = ["memberId"]), Index(value = ["receiptNumber"])]
)
data class SavingsEntity(
    @PrimaryKey
    val id: String,
    val memberId: String, // BMS-0001
    val date: String, // DD-MM-YYYY
    val type: String, // "MONTHLY", "WEEKLY", "SPECIAL", "EXTRA", "ONE_TIME"
    val amount: Double,
    val paymentMethod: String, // "CASH", "BANK", "BKASH", "NAGAD", "ROCKET", "UPAY", "OTHER"
    val accountId: String,
    val transactionId: String = "",
    val collectorName: String,
    val remarks: String = "",
    val receiptNumber: String,
    val status: String = "VALID", // "VALID", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "savings_withdrawals")
data class SavingsWithdrawalEntity(
    @PrimaryKey
    val id: String,
    val memberId: String,
    val amount: Double,
    val reason: String,
    val date: String,
    val accountId: String,
    val paymentMethod: String,
    val approverName: String = "",
    val refNumber: String = "",
    val receiptNumber: String = "",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "monthly_dues",
    indices = [Index(value = ["memberId", "monthYear"], unique = true)]
)
data class MonthlyDueEntity(
    @PrimaryKey
    val id: String,
    val memberId: String,
    val monthYear: String, // "11-2026"
    val targetAmount: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val status: String, // "PAID", "PARTIAL", "UNPAID", "ADVANCE"
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String, // "ADMISSION_FEE", "RENEWAL_FEE", "SERVICE_CHARGE", "LOAN_INTEREST", "OTHER"
    val date: String,
    val amount: Double,
    val accountId: String,
    val paymentMethod: String,
    val refNumber: String = "",
    val details: String = "",
    val entryBy: String,
    val receiptNumber: String = "",
    val status: String = "VALID", // "VALID", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String, // "OFFICE_RENT", "STAFF_SALARY", "TRAVEL", "ELECTRICITY", "PRINTING", "OTHER"
    val date: String,
    val amount: Double,
    val accountId: String,
    val paymentMethod: String,
    val receiptVoucherNote: String = "",
    val approverName: String = "",
    val remarks: String = "",
    val status: String = "VALID", // "VALID", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "account_transfers")
data class AccountTransferEntity(
    @PrimaryKey
    val id: String,
    val fromAccountId: String,
    val toAccountId: String,
    val amount: Double,
    val date: String,
    val reference: String = "",
    val remarks: String = "",
    val performedBy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "receipts",
    indices = [Index(value = ["receiptNumber"], unique = true), Index(value = ["memberId"])]
)
data class ReceiptEntity(
    @PrimaryKey
    val id: String,
    val receiptNumber: String,
    val memberId: String? = null,
    val memberName: String? = null,
    val type: String, // "SAVINGS_DEPOSIT", "SAVINGS_WITHDRAWAL", "LOAN_DISBURSEMENT", "INSTALLMENT_PAYMENT", "INCOME_PAYMENT"
    val date: String,
    val amount: Double,
    val previousBalance: Double,
    val currentBalance: Double,
    val paymentMethod: String,
    val transactionId: String = "",
    val collectorName: String,
    val details: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
