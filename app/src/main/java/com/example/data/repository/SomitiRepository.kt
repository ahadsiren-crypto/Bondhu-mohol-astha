package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import com.example.util.BanglaUtils
import com.example.util.NotificationHelper
import com.example.util.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SomitiRepository(
    private val dao: SomitiDao,
    private val context: Context
) {

    // Streams
    val allMembers: Flow<List<MemberEntity>> = dao.getAllMembers()
    val allAccounts: Flow<List<AccountEntity>> = dao.getAllAccounts()
    val allSavings: Flow<List<SavingsEntity>> = dao.getAllSavings()
    val allWithdrawals: Flow<List<SavingsWithdrawalEntity>> = dao.getAllSavingsWithdrawals()
    val allMonthlyDues: Flow<List<MonthlyDueEntity>> = dao.getAllMonthlyDues()
    val allLoanApps: Flow<List<LoanApplicationEntity>> = dao.getAllLoanApplications()
    val allLoans: Flow<List<LoanEntity>> = dao.getAllLoans()
    val allInstallmentPayments: Flow<List<InstallmentPaymentEntity>> = dao.getAllInstallmentPayments()
    val allIncomes: Flow<List<IncomeEntity>> = dao.getAllIncomes()
    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val allTransfers: Flow<List<AccountTransferEntity>> = dao.getAllTransfers()
    val allReceipts: Flow<List<ReceiptEntity>> = dao.getAllReceipts()
    val allNotices: Flow<List<NoticeEntity>> = dao.getAllNotices()
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()
    val allActivityLogs: Flow<List<ActivityLogEntity>> = dao.getAllActivityLogs()
    val allMemberMessages: Flow<List<MemberMessageEntity>> = dao.getAllMemberMessages()
    val allAdmins: Flow<List<UserEntity>> = dao.getAllAdmins()
    val appSettings: Flow<AppSettingsEntity?> = dao.getAppSettings()

    suspend fun initDatabase() = withContext(Dispatchers.IO) {
        NotificationHelper.initNotificationChannels(context)

        // Ensure default settings
        val settings = dao.getAppSettingsSync()
        if (settings == null) {
            dao.insertOrUpdateAppSettings(
                AppSettingsEntity(
                    id = "GLOBAL_CONFIG",
                    somitiNameBn = "বন্ধু মহল সমিতি",
                    somitiNameEn = "Bondhu Mohol Somiti",
                    address = "ঢাকা, বাংলাদেশ",
                    phone = "০১৭০০-০০০০০০",
                    currencySymbol = "৳",
                    defaultMonthlySavings = 1000.0,
                    useBengaliNumerals = true,
                    isDarkTheme = false
                )
            )
        }

        // Ensure Super Admin Ahad exists
        val adminUser = dao.getUserByUsername("Ahad")
        if (adminUser == null) {
            val salt = SecurityUtils.generateSalt()
            val tempPassword = "Ahad@2025"
            val hash = SecurityUtils.hashPassword(tempPassword, salt)
            dao.insertUser(
                UserEntity(
                    id = UUID.randomUUID().toString(),
                    username = "Ahad",
                    passwordHash = hash,
                    salt = salt,
                    role = "SUPER_ADMIN",
                    name = "Ahad (সুপার অ্যাডমিন)",
                    phone = "01711000000",
                    memberId = null,
                    isActive = true,
                    forcePasswordChange = true,
                    permissions = "ALL"
                )
            )
            logActivity("SYSTEM", "System", "সুপার অ্যাডমিন 'Ahad' সফলভাবে তৈরি হয়েছে")
        }

        // Ensure default accounts exist
        if (dao.getAccountCount() == 0) {
            val cashAcc = AccountEntity(
                id = "acc_cash",
                name = "নগদ তহবিল (Cash Box)",
                type = "CASH",
                accountNumber = "CASH-01",
                openingBalance = 5000.0,
                currentBalance = 5000.0,
                isDefault = true
            )
            val bankAcc = AccountEntity(
                id = "acc_bank",
                name = "সোনালী ব্যাংক একাউন্ট (Bank)",
                type = "BANK",
                accountNumber = "12034981293",
                openingBalance = 50000.0,
                currentBalance = 50000.0
            )
            val bkashAcc = AccountEntity(
                id = "acc_bkash",
                name = "বিকাশ মার্চেন্ট (bKash)",
                type = "BKASH",
                accountNumber = "01700112233",
                openingBalance = 15000.0,
                currentBalance = 15000.0
            )
            val nagadAcc = AccountEntity(
                id = "acc_nagad",
                name = "নগদ একাউন্ট (Nagad)",
                type = "NAGAD",
                accountNumber = "01800112233",
                openingBalance = 10000.0,
                currentBalance = 10000.0
            )
            dao.insertAccount(cashAcc)
            dao.insertAccount(bankAcc)
            dao.insertAccount(bkashAcc)
            dao.insertAccount(nagadAcc)
        }

        // Ensure initial notice exists
        val notices = dao.getAllNotices().firstOrNull()
        if (notices.isNullOrEmpty()) {
            dao.insertNotice(
                NoticeEntity(
                    id = UUID.randomUUID().toString(),
                    title = "বন্ধু মহল সমিতিতে স্বাগতম",
                    content = "বন্ধু মহল সমিতির সকল সম্মানিত সদস্যদের জানানো যাচ্ছে যে, সমিতির নতুন ডিজিটাল প্ল্যাটফর্ম চালু হয়েছে। আপনারা এখন খুব সহজেই আপনাদের সঞ্চয়, ঋণ ও লেনদেনের রসিদ দেখতে পারবেন। প্রতি মাসের ১০ তারিখের মধ্যে নিয়মিত মাসিক সঞ্চয় জমা দেওয়ার জন্য অনুরোধ করা হলো।",
                    category = "GENERAL",
                    publishedBy = "Ahad (সুপার অ্যাডমিন)",
                    date = BanglaUtils.formatDate(System.currentTimeMillis(), false),
                    isPinned = true
                )
            )
        }
    }

    // --- Authentication ---
    suspend fun authenticate(identifier: String, passwordAttempt: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val trimmed = identifier.trim()
        val user = dao.getUserByUsername(trimmed)
            ?: dao.getUserByPhone(trimmed)
            ?: dao.getUserByMemberId(trimmed)

        if (user == null) {
            return@withContext Result.failure(Exception("ব্যবহারকারী বা মোবাইল নম্বর পাওয়া যায়নি"))
        }

        if (!user.isActive) {
            return@withContext Result.failure(Exception("এই অ্যাকাউন্টটি বর্তমানে নিষ্ক্রিয় রয়েছে। অনুগ্রহ করে অ্যাডমিনের সাথে যোগাযোগ করুন।"))
        }

        val currentTime = System.currentTimeMillis()
        if (user.lockedUntil > currentTime) {
            val remainingMins = ((user.lockedUntil - currentTime) / 60000) + 1
            return@withContext Result.failure(Exception("অতিরিক্ত ভুল চেষ্টার কারণে অ্যাকাউন্ট সাময়িকভাবে বন্ধ আছে। আরো $remainingMins মিনিট পর চেষ্টা করুন।"))
        }

        val isValid = SecurityUtils.verifyPassword(passwordAttempt, user.salt, user.passwordHash)
        if (!isValid) {
            val failed = user.failedLoginAttempts + 1
            val lockTime = if (failed >= 5) currentTime + (15 * 60 * 1000) else 0L
            dao.updateUser(user.copy(failedLoginAttempts = failed, lockedUntil = lockTime))
            val msg = if (failed >= 5) "ভুল পাসওয়ার্ড! ৫ বার ভুল চেষ্টার কারণে অ্যাকাউন্ট ১৫ মিনিটের জন্য লক করা হয়েছে।"
            else "ভুল পাসওয়ার্ড! সঠিক পাসওয়ার্ড দিন। (ভুল চেষ্টা: $failed/৫)"
            return@withContext Result.failure(Exception(msg))
        }

        // Reset failed attempts on success
        val updatedUser = user.copy(failedLoginAttempts = 0, lockedUntil = 0L)
        dao.updateUser(updatedUser)
        logActivity(user.id, user.name, "লগইন সফল হয়েছে")
        Result.success(updatedUser)
    }

    suspend fun changePassword(userId: String, newPass: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = dao.getUserById(userId) ?: return@withContext Result.failure(Exception("ব্যবহারকারী খুঁজে পাওয়া যায়নি"))
        if (newPass.length < 6) {
            return@withContext Result.failure(Exception("পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে"))
        }
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(newPass, salt)
        dao.updateUser(user.copy(passwordHash = hash, salt = salt, forcePasswordChange = false, updatedAt = System.currentTimeMillis()))
        logActivity(user.id, user.name, "পাসওয়ার্ড পরিবর্তন করা হয়েছে")
        Result.success(Unit)
    }

    // --- Member Operations ---
    suspend fun createMember(
        name: String,
        fatherName: String,
        motherName: String,
        mobile: String,
        altMobile: String,
        address: String,
        village: String,
        upazila: String,
        district: String,
        joiningDate: String,
        nomineeName: String,
        nomineeRelation: String,
        nomineeMobile: String,
        monthlySavings: Double,
        initialAdmissionFee: Double?,
        accountId: String?,
        creatorName: String
    ): Result<MemberEntity> = withContext(Dispatchers.IO) {
        val count = dao.getMemberCount()
        val memberId = BanglaUtils.generateMemberId(count + 1)

        val member = MemberEntity(
            id = UUID.randomUUID().toString(),
            memberId = memberId,
            name = name.trim(),
            fatherName = fatherName.trim(),
            motherName = motherName.trim(),
            mobile = mobile.trim(),
            altMobile = altMobile.trim(),
            address = address.trim(),
            village = village.trim(),
            upazila = upazila.trim(),
            district = district.trim(),
            joiningDate = joiningDate.ifBlank { BanglaUtils.formatDate(System.currentTimeMillis(), false) },
            nomineeName = nomineeName.trim(),
            nomineeRelation = nomineeRelation.trim(),
            nomineeMobile = nomineeMobile.trim(),
            monthlySavings = monthlySavings,
            status = "ACTIVE"
        )
        dao.insertMember(member)

        // Create Member User Account so member can login
        val memberSalt = SecurityUtils.generateSalt()
        val defaultMemberPass = if (mobile.length >= 6) mobile.takeLast(6) else "123456"
        val memberHash = SecurityUtils.hashPassword(defaultMemberPass, memberSalt)
        val memberUser = UserEntity(
            id = UUID.randomUUID().toString(),
            username = memberId,
            passwordHash = memberHash,
            salt = memberSalt,
            role = "MEMBER",
            name = name.trim(),
            phone = mobile.trim(),
            memberId = memberId,
            isActive = true,
            forcePasswordChange = false
        )
        dao.insertUser(memberUser)

        // Process Admission Fee if provided
        if (initialAdmissionFee != null && initialAdmissionFee > 0 && !accountId.isNullOrBlank()) {
            val acc = dao.getAccountById(accountId)
            if (acc != null) {
                val receiptNo = BanglaUtils.generateReceiptNumber("FEE")
                val inc = IncomeEntity(
                    id = UUID.randomUUID().toString(),
                    title = "ভর্তি ফি: ${member.name} ($memberId)",
                    category = "ADMISSION_FEE",
                    date = joiningDate,
                    amount = initialAdmissionFee,
                    accountId = accountId,
                    paymentMethod = acc.type,
                    refNumber = memberId,
                    details = "নতুন সদস্য ভর্তি ফি",
                    entryBy = creatorName,
                    receiptNumber = receiptNo
                )
                dao.insertIncome(inc)
                dao.updateAccount(
                    acc.copy(
                        currentBalance = acc.currentBalance + initialAdmissionFee,
                        totalDeposit = acc.totalDeposit + initialAdmissionFee,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                dao.insertReceipt(
                    ReceiptEntity(
                        id = UUID.randomUUID().toString(),
                        receiptNumber = receiptNo,
                        memberId = memberId,
                        memberName = name,
                        type = "INCOME_PAYMENT",
                        date = joiningDate,
                        amount = initialAdmissionFee,
                        previousBalance = acc.currentBalance,
                        currentBalance = acc.currentBalance + initialAdmissionFee,
                        paymentMethod = acc.name,
                        collectorName = creatorName,
                        details = "সদস্য ভর্তি ফি বাবদ গ্রহণ"
                    )
                )
            }
        }

        // Post Notification
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetRole = "ALL",
            targetMemberId = memberId,
            title = "নতুন সদস্য যুক্ত হয়েছে",
            message = "সমিতিতে নতুন সদস্য ${member.name} ($memberId) যোগদান করেছেন।",
            type = "MEMBER",
            relatedId = memberId
        )
        dao.insertNotification(notif)
        NotificationHelper.postNotification(context, NotificationHelper.CHANNEL_NOTICES, notif.title, notif.message)
        logActivity("USER", creatorName, "নতুন সদস্য তৈরি: ${member.name} ($memberId)")

        Result.success(member)
    }

    suspend fun updateMember(member: MemberEntity, updatedBy: String): Result<Unit> = withContext(Dispatchers.IO) {
        dao.updateMember(member.copy(updatedAt = System.currentTimeMillis()))
        logActivity("USER", updatedBy, "সদস্য তথ্য আপডেট: ${member.name} (${member.memberId})")
        Result.success(Unit)
    }

    // --- Savings Operations ---
    suspend fun depositSavings(
        memberId: String,
        amount: Double,
        type: String,
        accountId: String,
        paymentMethod: String,
        collectorName: String,
        remarks: String,
        transactionId: String,
        date: String
    ): Result<ReceiptEntity> = withContext(Dispatchers.IO) {
        val member = dao.getMemberByMemberId(memberId)
            ?: return@withContext Result.failure(Exception("সদস্য খুঁজে পাওয়া যায়নি"))
        val account = dao.getAccountById(accountId)
            ?: return@withContext Result.failure(Exception("হিসাব বা অ্যাকাউন্ট পাওয়া যায়নি"))

        val receiptNumber = BanglaUtils.generateReceiptNumber("SAV")
        val effectiveDate = date.ifBlank { BanglaUtils.formatDate(System.currentTimeMillis(), false) }

        val savings = SavingsEntity(
            id = UUID.randomUUID().toString(),
            memberId = memberId,
            date = effectiveDate,
            type = type,
            amount = amount,
            paymentMethod = paymentMethod,
            accountId = accountId,
            transactionId = transactionId,
            collectorName = collectorName,
            remarks = remarks,
            receiptNumber = receiptNumber,
            status = "VALID"
        )
        dao.insertSavings(savings)

        // Update Account Balance
        val updatedAcc = account.copy(
            currentBalance = account.currentBalance + amount,
            totalDeposit = account.totalDeposit + amount,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateAccount(updatedAcc)

        // If Monthly Savings, update or create MonthlyDue
        val currentMonth = BanglaUtils.currentMonthYear()
        val existingDue = dao.getMonthlyDue(memberId, currentMonth)
        if (existingDue != null) {
            val newPaid = existingDue.paidAmount + amount
            val newDue = maxOf(0.0, existingDue.targetAmount - newPaid)
            val newStatus = when {
                newPaid >= existingDue.targetAmount -> "PAID"
                newPaid > 0.0 -> "PARTIAL"
                else -> "UNPAID"
            }
            dao.updateMonthlyDue(existingDue.copy(paidAmount = newPaid, dueAmount = newDue, status = newStatus, updatedAt = System.currentTimeMillis()))
        } else {
            val dueAmt = maxOf(0.0, member.monthlySavings - amount)
            val st = if (amount >= member.monthlySavings) "PAID" else "PARTIAL"
            dao.insertMonthlyDue(
                MonthlyDueEntity(
                    id = UUID.randomUUID().toString(),
                    memberId = memberId,
                    monthYear = currentMonth,
                    targetAmount = member.monthlySavings,
                    paidAmount = amount,
                    dueAmount = dueAmt,
                    status = st
                )
            )
        }

        // Calculate member total savings so far
        val allMemberSavings = dao.getSavingsForMember(memberId).firstOrNull().orEmpty()
        val totalValidDeposits = allMemberSavings.filter { it.status == "VALID" }.sumOf { it.amount }
        val allMemberWithdrawals = dao.getSavingsWithdrawalsForMember(memberId).firstOrNull().orEmpty()
        val totalApprovedWithdrawals = allMemberWithdrawals.filter { it.status == "APPROVED" }.sumOf { it.amount }
        val currentMemberSavings = totalValidDeposits - totalApprovedWithdrawals

        val receipt = ReceiptEntity(
            id = UUID.randomUUID().toString(),
            receiptNumber = receiptNumber,
            memberId = memberId,
            memberName = member.name,
            type = "SAVINGS_DEPOSIT",
            date = effectiveDate,
            amount = amount,
            previousBalance = currentMemberSavings - amount,
            currentBalance = currentMemberSavings,
            paymentMethod = "${account.name} ($paymentMethod)",
            transactionId = transactionId,
            collectorName = collectorName,
            details = "সঞ্চয় জমা ($type)"
        )
        dao.insertReceipt(receipt)

        // Notification
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetRole = "ALL",
            targetMemberId = memberId,
            title = "সঞ্চয় জমা সম্পন্ন হয়েছে",
            message = "${member.name} ($memberId)-এর ৳ ${BanglaUtils.formatNumber(amount, true)} সঞ্চয় জমা হয়েছে। রসিদ নং: $receiptNumber",
            type = "SAVINGS",
            relatedId = receiptNumber
        )
        dao.insertNotification(notif)
        NotificationHelper.postNotification(context, NotificationHelper.CHANNEL_SAVINGS, notif.title, notif.message)
        logActivity("USER", collectorName, "সঞ্চয় জমা: ৳ $amount — ${member.name} ($memberId)")

        Result.success(receipt)
    }

    suspend fun requestSavingsWithdrawal(
        memberId: String,
        amount: Double,
        reason: String,
        accountId: String,
        paymentMethod: String,
        date: String
    ): Result<SavingsWithdrawalEntity> = withContext(Dispatchers.IO) {
        val member = dao.getMemberByMemberId(memberId) ?: return@withContext Result.failure(Exception("সদস্য পাওয়া যায়নি"))
        val allMemberSavings = dao.getSavingsForMember(memberId).firstOrNull().orEmpty()
        val totalValidDeposits = allMemberSavings.filter { it.status == "VALID" }.sumOf { it.amount }
        val allMemberWithdrawals = dao.getSavingsWithdrawalsForMember(memberId).firstOrNull().orEmpty()
        val totalApprovedWithdrawals = allMemberWithdrawals.filter { it.status == "APPROVED" }.sumOf { it.amount }
        val currentSavings = totalValidDeposits - totalApprovedWithdrawals

        if (amount > currentSavings) {
            return@withContext Result.failure(Exception("উত্তোলনের পরিমাণ বর্তমান সঞ্চয়ের চেয়ে বেশি হতে পারে না! বর্তমান সঞ্চয়: ৳ ${BanglaUtils.formatNumber(currentSavings, true)}"))
        }

        val withdrawal = SavingsWithdrawalEntity(
            id = UUID.randomUUID().toString(),
            memberId = memberId,
            amount = amount,
            reason = reason,
            date = date.ifBlank { BanglaUtils.formatDate(System.currentTimeMillis(), false) },
            accountId = accountId,
            paymentMethod = paymentMethod,
            refNumber = BanglaUtils.generateReceiptNumber("WD"),
            status = "PENDING"
        )
        dao.insertSavingsWithdrawal(withdrawal)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetRole = "ADMIN",
            title = "সঞ্চয় উত্তোলনের আবেদন",
            message = "${member.name} ($memberId) ৳ ${BanglaUtils.formatNumber(amount, true)} উত্তোলনের আবেদন করেছেন।",
            type = "SAVINGS",
            relatedId = withdrawal.id
        )
        dao.insertNotification(notif)
        NotificationHelper.postNotification(context, NotificationHelper.CHANNEL_SAVINGS, notif.title, notif.message)
        Result.success(withdrawal)
    }

    suspend fun approveSavingsWithdrawal(withdrawal: SavingsWithdrawalEntity, approverName: String): Result<ReceiptEntity> = withContext(Dispatchers.IO) {
        val account = dao.getAccountById(withdrawal.accountId) ?: return@withContext Result.failure(Exception("অ্যাকাউন্ট পাওয়া যায়নি"))
        if (account.currentBalance < withdrawal.amount) {
            return@withContext Result.failure(Exception("নির্বাচিত অ্যাকাউন্টে পর্যাপ্ত ব্যালেন্স নেই! বর্তমান ব্যালেন্স: ৳ ${BanglaUtils.formatNumber(account.currentBalance, true)}"))
        }

        val member = dao.getMemberByMemberId(withdrawal.memberId) ?: return@withContext Result.failure(Exception("সদস্য পাওয়া যায়নি"))
        val receiptNumber = BanglaUtils.generateReceiptNumber("REC-WD")

        val updatedWithdrawal = withdrawal.copy(
            status = "APPROVED",
            approverName = approverName,
            receiptNumber = receiptNumber,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateSavingsWithdrawal(updatedWithdrawal)

        // Deduct from account
        dao.updateAccount(
            account.copy(
                currentBalance = account.currentBalance - withdrawal.amount,
                totalWithdrawal = account.totalWithdrawal + withdrawal.amount,
                updatedAt = System.currentTimeMillis()
            )
        )

        // Member savings balance calculation
        val allMemberSavings = dao.getSavingsForMember(withdrawal.memberId).firstOrNull().orEmpty()
        val totalValidDeposits = allMemberSavings.filter { it.status == "VALID" }.sumOf { it.amount }
        val allMemberWithdrawals = dao.getSavingsWithdrawalsForMember(withdrawal.memberId).firstOrNull().orEmpty()
        val totalApprovedWithdrawals = allMemberWithdrawals.filter { it.status == "APPROVED" }.sumOf { it.amount }
        val currentSavings = totalValidDeposits - totalApprovedWithdrawals

        val receipt = ReceiptEntity(
            id = UUID.randomUUID().toString(),
            receiptNumber = receiptNumber,
            memberId = withdrawal.memberId,
            memberName = member.name,
            type = "SAVINGS_WITHDRAWAL",
            date = withdrawal.date,
            amount = withdrawal.amount,
            previousBalance = currentSavings + withdrawal.amount,
            currentBalance = currentSavings,
            paymentMethod = "${account.name} (${withdrawal.paymentMethod})",
            collectorName = approverName,
            details = "সঞ্চয় উত্তোলন অনুমোদন"
        )
        dao.insertReceipt(receipt)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetRole = "ALL",
            targetMemberId = withdrawal.memberId,
            title = "সঞ্চয় উত্তোলন অনুমোদিত হয়েছে",
            message = "${member.name} ($withdrawal.memberId)-এর ৳ ${BanglaUtils.formatNumber(withdrawal.amount, true)} সঞ্চয় উত্তোলন অনুমোদিত ও প্রদান করা হয়েছে।",
            type = "SAVINGS",
            relatedId = receiptNumber
        )
        dao.insertNotification(notif)
        NotificationHelper.postNotification(context, NotificationHelper.CHANNEL_SAVINGS, notif.title, notif.message)
        logActivity("ADMIN", approverName, "সঞ্চয় উত্তোলন অনুমোদন: ৳ ${withdrawal.amount} — ${member.name}")

        Result.success(receipt)
    }

    // --- Loan Operations ---
    suspend fun applyForLoan(
        memberId: String,
        amount: Double,
        purpose: String,
        durationMonths: Int,
        installmentsCount: Int,
        guarantorName: String,
        guarantorMobile: String,
        guarantorRelation: String,
        remarks: String,
        applyDate: String
    ): Result<LoanApplicationEntity> = withContext(Dispatchers.IO) {
        val member = dao.getMemberByMemberId(memberId) ?: return@withContext Result.failure(Exception("সদস্য পাওয়া যায়নি"))
        val count = dao.getAllLoanApplications().firstOrNull()?.size ?: 0
        val appId = BanglaUtils.generateAppId(count + 1)

        val app = LoanApplicationEntity(
            id = UUID.randomUUID().toString(),
            applicationId = appId,
            memberId = memberId,
            applyDate = applyDate.ifBlank { BanglaUtils.formatDate(System.currentTimeMillis(), false) },
            amount = amount,
            purpose = purpose.trim(),
            durationMonths = durationMonths,
            installmentsCount = installmentsCount,
            guarantorName = guarantorName.trim(),
            guarantorMobile = guarantorMobile.trim(),
            guarantorRelation = guarantorRelation.trim(),
            remarks = remarks.trim(),
            status = "APPLIED"
        )
        dao.insertLoanApplication(app)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetRole = "ADMIN",
            title = "নতুন ঋণ আবেদন এসেছে",
            message = "${member.name} ($memberId) ৳ ${BanglaUtils.formatNumber(amount, true)} ঋণের জন্য আবেদন করেছেন। (অ্যাপ্লিকেশন: $appId)",
            type = "LOAN",
            relatedId = app.id
        )
        dao.insertNotification(notif)
        NotificationHelper.postNotification(context, NotificationHelper.CHANNEL_LOANS, notif.title, notif.message)
        logActivity("MEMBER", member.name, "ঋণ আবেদন তৈরি: ৳ $amount ($appId)")

        Result.success(app)
    }

    suspend fun reviewLoanApplication(
        appId: String,
        newStatus: String, // "APPROVED", "REJECTED", "UNDER_REVIEW"
        notes: String,
        reviewer: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val app = dao.getLoanApplicationById(appId) ?: return@withContext Result.failure(Exception("আবেদন খুঁজে পাওয়া যায়নি"))
        val updated = app.copy(status = newStatus, reviewNotes = notes, reviewedBy = reviewer)
        dao.updateLoanApplication(updated)

        val member = dao.getMemberByMemberId(app.memberId)
        val statusBangla = when (newStatus) {
            "APPROVED" -> "অনুমোদিত হয়েছে"
            "REJECTED" -> "প্রত্যাখ্যাত হয়েছে"
            else -> "পর্যালোচনায় রয়েছে"
        }
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetRole = "ALL",
            targetMemberId = app.memberId,
            title = "ঋণ আবেদন $statusBangla",
            message = "${member?.name ?: app.memberId}-এর ৳ ${BanglaUtils.formatNumber(app.amount, true)} ঋণের আবেদন $statusBangla।",
            type = "LOAN",
            relatedId = app.applicationId
        )
        dao.insertNotification(notif)
        NotificationHelper.postNotification(context, NotificationHelper.CHANNEL_LOANS, notif.title, notif.message)
        logActivity("ADMIN", reviewer, "ঋণ আবেদন পর্যালোচনা: ${app.applicationId} -> $newStatus")
        Result.success(Unit)
    }

    suspend fun disburseLoan(
        app: LoanApplicationEntity,
        approvedAmount: Double,
        interestRate: Double,
        serviceCharge: Double,
        accountId: String,
        paymentMethod: String,
        installmentsCount: Int,
        disbursedBy: String,
        disburseDate: String
    ): Result<Pair<LoanEntity, ReceiptEntity>> = withContext(Dispatchers.IO) {
        val account = dao.getAccountById(accountId) ?: return@withContext Result.failure(Exception("অ্যাকাউন্ট পাওয়া যায়নি"))
        if (account.currentBalance < approvedAmount) {
            return@withContext Result.failure(Exception("নির্বাচিত অ্যাকাউন্টে পর্যাপ্ত ব্যালেন্স নেই! বর্তমান ব্যালেন্স: ৳ ${BanglaUtils.formatNumber(account.currentBalance, true)}"))
        }

        val member = dao.getMemberByMemberId(app.memberId) ?: return@withContext Result.failure(Exception("সদস্য পাওয়া যায়নি"))
        val totalRepayable = approvedAmount + (approvedAmount * interestRate / 100.0)
        val perInstallment = if (installmentsCount > 0) totalRepayable / installmentsCount else totalRepayable

        val allLoansCount = dao.getAllLoans().firstOrNull()?.size ?: 0
        val loanId = BanglaUtils.generateLoanId(allLoansCount + 1)
        val receiptNumber = BanglaUtils.generateReceiptNumber("LN-DIS")
        val effectiveDate = disburseDate.ifBlank { BanglaUtils.formatDate(System.currentTimeMillis(), false) }

        val loan = LoanEntity(
            id = UUID.randomUUID().toString(),
            loanId = loanId,
            applicationId = app.applicationId,
            memberId = app.memberId,
            principalAmount = approvedAmount,
            interestRate = interestRate,
            serviceCharge = serviceCharge,
            totalRepayable = totalRepayable,
            paidAmount = 0.0,
            remainingAmount = totalRepayable,
            disbursementDate = effectiveDate,
            accountId = accountId,
            paymentMethod = paymentMethod,
            installmentsCount = installmentsCount,
            perInstallmentAmount = perInstallment,
            status = "DISBURSED",
            disbursedBy = disbursedBy,
            receiptNumber = receiptNumber
        )
        dao.insertLoan(loan)

        // Deduct disbursed loan principal from account (cash outflow)
        dao.updateAccount(
            account.copy(
                currentBalance = account.currentBalance - approvedAmount,
                totalWithdrawal = account.totalWithdrawal + approvedAmount,
                updatedAt = System.currentTimeMillis()
            )
        )

        // Service charge is Income if > 0
        if (serviceCharge > 0) {
            val incReceipt = BanglaUtils.generateReceiptNumber("SRV")
            val inc = IncomeEntity(
                id = UUID.randomUUID().toString(),
                title = "ঋণ সার্ভিস চার্জ: ${member.name} ($loanId)",
                category = "SERVICE_CHARGE",
                date = effectiveDate,
                amount = serviceCharge,
                accountId = accountId,
                paymentMethod = paymentMethod,
                refNumber = loanId,
                details = "ঋণ বিতরণ সার্ভিস চার্জ",
                entryBy = disbursedBy,
                receiptNumber = incReceipt
            )
            dao.insertIncome(inc)
            // Add service charge to account
            dao.updateAccount(
                account.copy(
                    currentBalance = account.currentBalance - approvedAmount + serviceCharge,
                    totalDeposit = account.totalDeposit + serviceCharge,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        // Generate Loan Installment Schedule rows
        val installmentList = mutableListOf<LoanInstallmentEntity>()
        for (i in 1..installmentsCount) {
            installmentList.add(
                LoanInstallmentEntity(
                    id = UUID.randomUUID().toString(),
                    loanId = loanId,
                    memberId = app.memberId,
                    installmentNumber = i,
                    scheduledAmount = perInstallment,
                    collectedAmount = 0.0,
                    dueDate = effectiveDate,
                    status = "DUE"
                )
            )
        }
        dao.insertLoanInstallments(installmentList)

        // Mark application as DISBURSED
        dao.updateLoanApplication(app.copy(status = "DISBURSED"))

        // Create Receipt
        val receipt = ReceiptEntity(
            id = UUID.randomUUID().toString(),
            receiptNumber = receiptNumber,
            memberId = app.memberId,
            memberName = member.name,
            type = "LOAN_DISBURSEMENT",
            date = effectiveDate,
            amount = approvedAmount,
            previousBalance = 0.0,
            currentBalance = totalRepayable,
            paymentMethod = "${account.name} ($paymentMethod)",
            collectorName = disbursedBy,
            details = "অনুমোদিত ঋণ বিতরণ ($loanId)"
        )
        dao.insertReceipt(receipt)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetRole = "ALL",
            targetMemberId = app.memberId,
            title = "ঋণ বিতরণ করা হয়েছে",
            message = "${member.name} ($app.memberId)-কে ৳ ${BanglaUtils.formatNumber(approvedAmount, true)} ঋণ বিতরণ করা হয়েছে। ঋণ নং: $loanId",
            type = "LOAN",
            relatedId = loanId
        )
        dao.insertNotification(notif)
        NotificationHelper.postNotification(context, NotificationHelper.CHANNEL_LOANS, notif.title, notif.message)
        logActivity("ADMIN", disbursedBy, "ঋণ বিতরণ: ৳ $approvedAmount ($loanId) -> ${member.name}")

        Result.success(Pair(loan, receipt))
    }

    suspend fun collectInstallment(
        loanId: String,
        installmentNumber: Int,
        amount: Double,
        accountId: String,
        paymentMethod: String,
        collectorName: String,
        remarks: String,
        transactionId: String,
        date: String
    ): Result<ReceiptEntity> = withContext(Dispatchers.IO) {
        val loan = dao.getLoanById(loanId) ?: return@withContext Result.failure(Exception("ঋণ খুঁজে পাওয়া যায়নি"))
        val account = dao.getAccountById(accountId) ?: return@withContext Result.failure(Exception("অ্যাকাউন্ট পাওয়া যায়নি"))
        val member = dao.getMemberByMemberId(loan.memberId) ?: return@withContext Result.failure(Exception("সদস্য পাওয়া যায়নি"))

        val receiptNumber = BanglaUtils.generateReceiptNumber("INS")
        val effectiveDate = date.ifBlank { BanglaUtils.formatDate(System.currentTimeMillis(), false) }

        val payment = InstallmentPaymentEntity(
            id = UUID.randomUUID().toString(),
            loanId = loanId,
            memberId = loan.memberId,
            installmentNumber = installmentNumber,
            amount = amount,
            paymentDate = effectiveDate,
            accountId = accountId,
            paymentMethod = paymentMethod,
            transactionId = transactionId,
            collectorName = collectorName,
            remarks = remarks,
            receiptNumber = receiptNumber,
            status = "VALID"
        )
        dao.insertInstallmentPayment(payment)

        // Update Account Balance (installment collection is cash inflow)
        dao.updateAccount(
            account.copy(
                currentBalance = account.currentBalance + amount,
                totalDeposit = account.totalDeposit + amount,
                updatedAt = System.currentTimeMillis()
            )
        )

        // Update Loan paid and remaining
        val newPaid = loan.paidAmount + amount
        val newRemaining = maxOf(0.0, loan.totalRepayable - newPaid)
        val newStatus = if (newRemaining <= 0.0) "FULLY_REPAID" else "REPAYING"
        dao.updateLoan(loan.copy(paidAmount = newPaid, remainingAmount = newRemaining, status = newStatus))

        // Create Receipt
        val receipt = ReceiptEntity(
            id = UUID.randomUUID().toString(),
            receiptNumber = receiptNumber,
            memberId = loan.memberId,
            memberName = member.name,
            type = "INSTALLMENT_PAYMENT",
            date = effectiveDate,
            amount = amount,
            previousBalance = loan.remainingAmount,
            currentBalance = newRemaining,
            paymentMethod = "${account.name} ($paymentMethod)",
            transactionId = transactionId,
            collectorName = collectorName,
            details = "ঋণের কিস্তি নং $installmentNumber আদায় ($loanId)"
        )
        dao.insertReceipt(receipt)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetRole = "ALL",
            targetMemberId = loan.memberId,
            title = "কিস্তি জমা হয়েছে",
            message = "${member.name} ($loan.memberId)-এর ঋণের কিস্তি নং $installmentNumber বাবদ ৳ ${BanglaUtils.formatNumber(amount, true)} জমা হয়েছে।",
            type = "INSTALLMENT",
            relatedId = receiptNumber
        )
        dao.insertNotification(notif)
        NotificationHelper.postNotification(context, NotificationHelper.CHANNEL_SAVINGS, notif.title, notif.message)
        logActivity("USER", collectorName, "কিস্তি আদায়: ৳ $amount ($loanId - কিস্তি $installmentNumber)")

        Result.success(receipt)
    }

    // --- Income & Expense Operations ---
    suspend fun addIncome(
        title: String,
        category: String,
        amount: Double,
        accountId: String,
        paymentMethod: String,
        refNumber: String,
        details: String,
        entryBy: String,
        date: String
    ): Result<IncomeEntity> = withContext(Dispatchers.IO) {
        val account = dao.getAccountById(accountId) ?: return@withContext Result.failure(Exception("অ্যাকাউন্ট পাওয়া যায়নি"))
        val receiptNumber = BanglaUtils.generateReceiptNumber("INC")
        val effectiveDate = date.ifBlank { BanglaUtils.formatDate(System.currentTimeMillis(), false) }

        val income = IncomeEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            category = category,
            date = effectiveDate,
            amount = amount,
            accountId = accountId,
            paymentMethod = paymentMethod,
            refNumber = refNumber.trim(),
            details = details.trim(),
            entryBy = entryBy,
            receiptNumber = receiptNumber
        )
        dao.insertIncome(income)

        // Update account
        dao.updateAccount(
            account.copy(
                currentBalance = account.currentBalance + amount,
                totalDeposit = account.totalDeposit + amount,
                updatedAt = System.currentTimeMillis()
            )
        )

        dao.insertReceipt(
            ReceiptEntity(
                id = UUID.randomUUID().toString(),
                receiptNumber = receiptNumber,
                memberId = null,
                memberName = title,
                type = "INCOME_PAYMENT",
                date = effectiveDate,
                amount = amount,
                previousBalance = account.currentBalance,
                currentBalance = account.currentBalance + amount,
                paymentMethod = "${account.name} ($paymentMethod)",
                collectorName = entryBy,
                details = "আয়: $title ($category)"
            )
        )

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetRole = "ADMIN",
            title = "নতুন আয় যুক্ত হয়েছে",
            message = "$title বাবদ ৳ ${BanglaUtils.formatNumber(amount, true)} জমা হয়েছে।",
            type = "INCOME",
            relatedId = receiptNumber
        )
        dao.insertNotification(notif)
        logActivity("USER", entryBy, "আয় যোগ: ৳ $amount — $title")
        Result.success(income)
    }

    suspend fun addExpense(
        title: String,
        category: String,
        amount: Double,
        accountId: String,
        paymentMethod: String,
        voucherNote: String,
        approverName: String,
        remarks: String,
        date: String
    ): Result<ExpenseEntity> = withContext(Dispatchers.IO) {
        val account = dao.getAccountById(accountId) ?: return@withContext Result.failure(Exception("অ্যাকাউন্ট পাওয়া যায়নি"))
        if (account.currentBalance < amount) {
            return@withContext Result.failure(Exception("নির্বাচিত অ্যাকাউন্টে পর্যাপ্ত ব্যালেন্স নেই! বর্তমান ব্যালেন্স: ৳ ${BanglaUtils.formatNumber(account.currentBalance, true)}"))
        }

        val effectiveDate = date.ifBlank { BanglaUtils.formatDate(System.currentTimeMillis(), false) }

        val expense = ExpenseEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            category = category,
            date = effectiveDate,
            amount = amount,
            accountId = accountId,
            paymentMethod = paymentMethod,
            receiptVoucherNote = voucherNote.trim(),
            approverName = approverName,
            remarks = remarks.trim()
        )
        dao.insertExpense(expense)

        dao.updateAccount(
            account.copy(
                currentBalance = account.currentBalance - amount,
                totalWithdrawal = account.totalWithdrawal + amount,
                updatedAt = System.currentTimeMillis()
            )
        )

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetRole = "ADMIN",
            title = "নতুন ব্যয় যোগ হয়েছে",
            message = "$title বাবদ ৳ ${BanglaUtils.formatNumber(amount, true)} ব্যয় হয়েছে।",
            type = "EXPENSE",
            relatedId = expense.id
        )
        dao.insertNotification(notif)
        logActivity("USER", approverName, "ব্যয় এন্ট্রি: ৳ $amount — $title")
        Result.success(expense)
    }

    // --- Fund Transfer ---
    suspend fun transferFunds(
        fromAccountId: String,
        toAccountId: String,
        amount: Double,
        reference: String,
        remarks: String,
        performedBy: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (fromAccountId == toAccountId) {
            return@withContext Result.failure(Exception("একই অ্যাকাউন্টে ফান্ড ট্রান্সফার করা যাবে না"))
        }
        val fromAcc = dao.getAccountById(fromAccountId) ?: return@withContext Result.failure(Exception("উৎস অ্যাকাউন্ট পাওয়া যায়নি"))
        val toAcc = dao.getAccountById(toAccountId) ?: return@withContext Result.failure(Exception("গন্তব্য অ্যাকাউন্ট পাওয়া যায়নি"))

        if (fromAcc.currentBalance < amount) {
            return@withContext Result.failure(Exception("উৎস অ্যাকাউন্টে পর্যাপ্ত টাকা নেই! ব্যালেন্স: ৳ ${BanglaUtils.formatNumber(fromAcc.currentBalance, true)}"))
        }

        // Deduct from source and add to destination
        dao.updateAccount(fromAcc.copy(currentBalance = fromAcc.currentBalance - amount, updatedAt = System.currentTimeMillis()))
        dao.updateAccount(toAcc.copy(currentBalance = toAcc.currentBalance + amount, updatedAt = System.currentTimeMillis()))

        dao.insertTransfer(
            AccountTransferEntity(
                id = UUID.randomUUID().toString(),
                fromAccountId = fromAccountId,
                toAccountId = toAccountId,
                amount = amount,
                date = BanglaUtils.formatDate(System.currentTimeMillis(), false),
                reference = reference.trim(),
                remarks = remarks.trim(),
                performedBy = performedBy
            )
        )

        logActivity("ADMIN", performedBy, "ফান্ড ট্রান্সফার: ৳ $amount (${fromAcc.name} -> ${toAcc.name})")
        Result.success(Unit)
    }

    // --- Notice & Communication ---
    suspend fun publishNotice(
        title: String,
        content: String,
        category: String,
        publishedBy: String,
        isPinned: Boolean
    ): Result<NoticeEntity> = withContext(Dispatchers.IO) {
        val notice = NoticeEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            content = content.trim(),
            category = category,
            publishedBy = publishedBy,
            date = BanglaUtils.formatDate(System.currentTimeMillis(), false),
            isPinned = isPinned
        )
        dao.insertNotice(notice)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetRole = "ALL",
            title = "নতুন নোটিশ প্রকাশিত হয়েছে",
            message = title,
            type = "NOTICE",
            relatedId = notice.id
        )
        dao.insertNotification(notif)
        NotificationHelper.postNotification(context, NotificationHelper.CHANNEL_NOTICES, notif.title, notif.message)
        logActivity("ADMIN", publishedBy, "নোটিশ প্রকাশ: $title")
        Result.success(notice)
    }

    suspend fun deleteNotice(notice: NoticeEntity): Result<Unit> = withContext(Dispatchers.IO) {
        dao.deleteNotice(notice)
        Result.success(Unit)
    }

    suspend fun sendMemberMessage(
        memberId: String,
        memberName: String,
        subject: String,
        message: String
    ): Result<MemberMessageEntity> = withContext(Dispatchers.IO) {
        val msg = MemberMessageEntity(
            id = UUID.randomUUID().toString(),
            memberId = memberId,
            memberName = memberName,
            subject = subject.trim(),
            message = message.trim(),
            date = BanglaUtils.formatDate(System.currentTimeMillis(), false)
        )
        dao.insertMemberMessage(msg)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetRole = "ADMIN",
            title = "সদস্যের নতুন বার্তা/অভিযোগ",
            message = "$memberName ($memberId): $subject",
            type = "SYSTEM",
            relatedId = msg.id
        )
        dao.insertNotification(notif)
        NotificationHelper.postNotification(context, NotificationHelper.CHANNEL_SYSTEM, notif.title, notif.message)
        Result.success(msg)
    }

    suspend fun replyMemberMessage(message: MemberMessageEntity, replyText: String): Result<Unit> = withContext(Dispatchers.IO) {
        dao.updateMemberMessage(message.copy(status = "REPLIED", reply = replyText))
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetRole = "MEMBER",
            targetMemberId = message.memberId,
            title = "আপনার বার্তার উত্তর দেওয়া হয়েছে",
            message = "বিষয়: ${message.subject} — উত্তর: $replyText",
            type = "SYSTEM",
            relatedId = message.id
        )
        dao.insertNotification(notif)
        Result.success(Unit)
    }

    // --- Admin Management ---
    suspend fun createAdmin(
        username: String,
        name: String,
        phone: String,
        role: String, // "ADMIN"
        permissions: String,
        tempPassword: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val existing = dao.getUserByUsername(username.trim())
        if (existing != null) {
            return@withContext Result.failure(Exception("এই ইউজারনেমে ইতোমধ্যে একটি অ্যাকাউন্ট রয়েছে"))
        }
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(tempPassword, salt)
        val admin = UserEntity(
            id = UUID.randomUUID().toString(),
            username = username.trim(),
            passwordHash = hash,
            salt = salt,
            role = role,
            name = name.trim(),
            phone = phone.trim(),
            isActive = true,
            forcePasswordChange = true,
            permissions = permissions
        )
        dao.insertUser(admin)
        logActivity("SUPER_ADMIN", "সুপার অ্যাডমিন", "নতুন অ্যাডমিন তৈরি: $username ($name)")
        Result.success(admin)
    }

    suspend fun updateAdminPermissions(userId: String, newPermissions: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = dao.getUserById(userId) ?: return@withContext Result.failure(Exception("ব্যবহারকারী খুঁজে পাওয়া যায়নি"))
        dao.updateUser(user.copy(permissions = newPermissions, updatedAt = System.currentTimeMillis()))
        logActivity("SUPER_ADMIN", "সুপার অ্যাডমিন", "পারমিশন পরিবর্তন: ${user.username}")
        Result.success(Unit)
    }

    suspend fun toggleAdminStatus(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = dao.getUserById(userId) ?: return@withContext Result.failure(Exception("ব্যবহারকারী খুঁজে পাওয়া যায়নি"))
        if (user.role == "SUPER_ADMIN") return@withContext Result.failure(Exception("সুপার অ্যাডমিন নিষ্ক্রিয় করা যাবে না"))
        dao.updateUser(user.copy(isActive = !user.isActive, updatedAt = System.currentTimeMillis()))
        Result.success(Unit)
    }

    // --- Notifications & Activity Log ---
    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        dao.markAllNotificationsAsRead()
    }

    suspend fun markNotificationAsRead(id: String) = withContext(Dispatchers.IO) {
        dao.markNotificationAsRead(id)
    }

    suspend fun logActivity(userId: String, userName: String, action: String, details: String = "") = withContext(Dispatchers.IO) {
        dao.insertActivityLog(
            ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                userName = userName,
                action = action,
                details = details
            )
        )
    }

    // --- Settings ---
    suspend fun updateAppSettings(settings: AppSettingsEntity): Result<Unit> = withContext(Dispatchers.IO) {
        dao.insertOrUpdateAppSettings(settings.copy(updatedAt = System.currentTimeMillis()))
        Result.success(Unit)
    }

    // --- Integrity Checker ---
    suspend fun verifyDatabaseIntegrity(): List<String> = withContext(Dispatchers.IO) {
        val issues = mutableListOf<String>()
        val members = dao.getAllMembers().firstOrNull().orEmpty()
        val accounts = dao.getAllAccounts().firstOrNull().orEmpty()
        val savings = dao.getAllSavings().firstOrNull().orEmpty()
        val loans = dao.getAllLoans().firstOrNull().orEmpty()
        val expenses = dao.getAllExpenses().firstOrNull().orEmpty()

        if (members.isEmpty()) {
            issues.add("সমিতিতে কোনো সক্রিয় সদস্য নিবন্ধিত নেই।")
        }
        if (accounts.isEmpty()) {
            issues.add("কোনো ক্যাশ বা ব্যাংক একাউন্ট নেই!")
        }

        // Check if any loan has paidAmount > totalRepayable
        loans.forEach { loan ->
            if (loan.paidAmount > loan.totalRepayable + 0.01) {
                issues.add("সতর্কতা: ঋণ নং ${loan.loanId} এ পরিশোধের পরিমাণ (৳ ${loan.paidAmount}) মোট প্রাপ্য (৳ ${loan.totalRepayable}) অপেক্ষা বেশি!")
            }
        }

        // Check account balances
        accounts.forEach { acc ->
            if (acc.currentBalance < 0.0) {
                issues.add("সতর্কতা: ${acc.name} একাউন্টে নেগেটিভ ব্যালেন্স (৳ ${acc.currentBalance}) রয়েছে!")
            }
        }

        if (issues.isEmpty()) {
            issues.add("সব ডেটাবেজ রেকর্ড ও হিসাবের সামঞ্জস্য সম্পূর্ণ নির্ভুল রয়েছে। কোনো অমিল পাওয়া যায়নি।")
        }
        issues
    }

    // --- Backup & Restore JSON ---
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())
        root.put("appName", "Bondhu Mohol Somiti")

        val membersArr = JSONArray()
        dao.getAllMembers().firstOrNull().orEmpty().forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("memberId", it.memberId)
            obj.put("name", it.name)
            obj.put("mobile", it.mobile)
            obj.put("joiningDate", it.joiningDate)
            obj.put("monthlySavings", it.monthlySavings)
            obj.put("status", it.status)
            membersArr.put(obj)
        }
        root.put("members", membersArr)

        val accountsArr = JSONArray()
        dao.getAllAccounts().firstOrNull().orEmpty().forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("type", it.type)
            obj.put("currentBalance", it.currentBalance)
            accountsArr.put(obj)
        }
        root.put("accounts", accountsArr)

        root.toString(2)
    }
}
