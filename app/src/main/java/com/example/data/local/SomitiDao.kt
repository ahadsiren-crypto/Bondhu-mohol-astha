package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SomitiDao {

    // --- Users & RBAC ---
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE memberId = :memberId LIMIT 1")
    suspend fun getUserByMemberId(memberId: String): UserEntity?

    @Query("SELECT * FROM users WHERE role IN ('SUPER_ADMIN', 'ADMIN') ORDER BY createdAt ASC")
    fun getAllAdmins(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    // --- Members ---
    @Query("SELECT * FROM members ORDER BY memberId ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: String): MemberEntity?

    @Query("SELECT * FROM members WHERE memberId = :memberId LIMIT 1")
    suspend fun getMemberByMemberId(memberId: String): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Query("SELECT COUNT(*) FROM members")
    suspend fun getMemberCount(): Int

    // --- Accounts ---
    @Query("SELECT * FROM accounts ORDER BY createdAt ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountCount(): Int

    // --- Savings ---
    @Query("SELECT * FROM savings ORDER BY createdAt DESC")
    fun getAllSavings(): Flow<List<SavingsEntity>>

    @Query("SELECT * FROM savings WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getSavingsForMember(memberId: String): Flow<List<SavingsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavings(savings: SavingsEntity)

    @Update
    suspend fun updateSavings(savings: SavingsEntity)

    // --- Savings Withdrawals ---
    @Query("SELECT * FROM savings_withdrawals ORDER BY createdAt DESC")
    fun getAllSavingsWithdrawals(): Flow<List<SavingsWithdrawalEntity>>

    @Query("SELECT * FROM savings_withdrawals WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getSavingsWithdrawalsForMember(memberId: String): Flow<List<SavingsWithdrawalEntity>>

    @Query("SELECT * FROM savings_withdrawals WHERE status = 'PENDING'")
    fun getPendingSavingsWithdrawals(): Flow<List<SavingsWithdrawalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsWithdrawal(withdrawal: SavingsWithdrawalEntity)

    @Update
    suspend fun updateSavingsWithdrawal(withdrawal: SavingsWithdrawalEntity)

    // --- Monthly Dues ---
    @Query("SELECT * FROM monthly_dues ORDER BY monthYear DESC")
    fun getAllMonthlyDues(): Flow<List<MonthlyDueEntity>>

    @Query("SELECT * FROM monthly_dues WHERE memberId = :memberId")
    fun getMonthlyDuesForMember(memberId: String): Flow<List<MonthlyDueEntity>>

    @Query("SELECT * FROM monthly_dues WHERE memberId = :memberId AND monthYear = :monthYear LIMIT 1")
    suspend fun getMonthlyDue(memberId: String, monthYear: String): MonthlyDueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyDue(due: MonthlyDueEntity)

    @Update
    suspend fun updateMonthlyDue(due: MonthlyDueEntity)

    // --- Loans & Applications ---
    @Query("SELECT * FROM loan_applications ORDER BY createdAt DESC")
    fun getAllLoanApplications(): Flow<List<LoanApplicationEntity>>

    @Query("SELECT * FROM loan_applications WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getLoanApplicationsForMember(memberId: String): Flow<List<LoanApplicationEntity>>

    @Query("SELECT * FROM loan_applications WHERE status IN ('APPLIED', 'UNDER_REVIEW')")
    fun getPendingLoanApplications(): Flow<List<LoanApplicationEntity>>

    @Query("SELECT * FROM loan_applications WHERE id = :id LIMIT 1")
    suspend fun getLoanApplicationById(id: String): LoanApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanApplication(app: LoanApplicationEntity)

    @Update
    suspend fun updateLoanApplication(app: LoanApplicationEntity)

    @Query("SELECT * FROM loans ORDER BY createdAt DESC")
    fun getAllLoans(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getLoansForMember(memberId: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE loanId = :loanId LIMIT 1")
    suspend fun getLoanById(loanId: String): LoanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity)

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    // --- Loan Installments Schedule ---
    @Query("SELECT * FROM loan_installments WHERE loanId = :loanId ORDER BY installmentNumber ASC")
    fun getInstallmentsForLoan(loanId: String): Flow<List<LoanInstallmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanInstallment(inst: LoanInstallmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanInstallments(list: List<LoanInstallmentEntity>)

    @Update
    suspend fun updateLoanInstallment(inst: LoanInstallmentEntity)

    // --- Installment Payments ---
    @Query("SELECT * FROM installment_payments ORDER BY createdAt DESC")
    fun getAllInstallmentPayments(): Flow<List<InstallmentPaymentEntity>>

    @Query("SELECT * FROM installment_payments WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getInstallmentPaymentsForMember(memberId: String): Flow<List<InstallmentPaymentEntity>>

    @Query("SELECT * FROM installment_payments WHERE loanId = :loanId ORDER BY installmentNumber ASC")
    fun getInstallmentPaymentsForLoan(loanId: String): Flow<List<InstallmentPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallmentPayment(payment: InstallmentPaymentEntity)

    // --- Incomes ---
    @Query("SELECT * FROM incomes ORDER BY createdAt DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity)

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    // --- Transfers ---
    @Query("SELECT * FROM account_transfers ORDER BY createdAt DESC")
    fun getAllTransfers(): Flow<List<AccountTransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: AccountTransferEntity)

    // --- Receipts ---
    @Query("SELECT * FROM receipts ORDER BY createdAt DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getReceiptsForMember(memberId: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE receiptNumber = :receiptNumber LIMIT 1")
    suspend fun getReceiptByNumber(receiptNumber: String): ReceiptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    // --- Notices ---
    @Query("SELECT * FROM notices ORDER BY isPinned DESC, createdAt DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Delete
    suspend fun deleteNotice(notice: NoticeEntity)

    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE targetRole IN ('ALL', 'MEMBER') AND (targetMemberId IS NULL OR targetMemberId = :memberId) ORDER BY createdAt DESC")
    fun getNotificationsForMember(memberId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    // --- Member Messages ---
    @Query("SELECT * FROM member_messages ORDER BY createdAt DESC")
    fun getAllMemberMessages(): Flow<List<MemberMessageEntity>>

    @Query("SELECT * FROM member_messages WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getMessagesForMember(memberId: String): Flow<List<MemberMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemberMessage(message: MemberMessageEntity)

    @Update
    suspend fun updateMemberMessage(message: MemberMessageEntity)

    // --- Activity Logs ---
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 500")
    fun getAllActivityLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLogEntity)

    // --- App Settings ---
    @Query("SELECT * FROM app_settings WHERE id = 'GLOBAL_CONFIG' LIMIT 1")
    fun getAppSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 'GLOBAL_CONFIG' LIMIT 1")
    suspend fun getAppSettingsSync(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAppSettings(settings: AppSettingsEntity)
}
