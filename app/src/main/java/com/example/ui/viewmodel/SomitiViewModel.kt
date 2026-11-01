package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.SomitiRepository
import com.example.util.BanglaUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class Screen {
    object Login : Screen()
    object AdminDashboard : Screen()
    object Members : Screen()
    data class MemberDetail(val memberId: String) : Screen()
    object Savings : Screen()
    object Loans : Screen()
    object IncomeExpense : Screen()
    object Accounts : Screen()
    object Reports : Screen()
    object Notices : Screen()
    object Settings : Screen()
    object Approvals : Screen()
    object ActivityLogs : Screen()
    object Notifications : Screen()
    object MemberDashboard : Screen()
}

data class DashboardStats(
    val totalMembers: Int = 0,
    val activeMembers: Int = 0,
    val inactiveMembers: Int = 0,
    val todaySavings: Double = 0.0,
    val todayIncome: Double = 0.0,
    val todayExpense: Double = 0.0,
    val thisMonthSavings: Double = 0.0,
    val totalSavingsLiability: Double = 0.0,
    val totalDisbursedLoans: Double = 0.0,
    val currentLoanDue: Double = 0.0,
    val thisMonthInstallments: Double = 0.0,
    val totalInstallmentDue: Double = 0.0,
    val cashBalance: Double = 0.0,
    val bankBalance: Double = 0.0,
    val mobileBankBalance: Double = 0.0,
    val totalNetBalance: Double = 0.0,
    val totalIncomeAllTime: Double = 0.0,
    val totalExpenseAllTime: Double = 0.0,
    val netProfitLoss: Double = 0.0
)

data class ChartPoint(val label: String, val value: Double)

class SomitiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SomitiDatabase.getDatabase(application)
    val repository = SomitiRepository(database.somitiDao(), application)

    // Current logged in user
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Navigation Stack / Screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenHistory = mutableListOf<Screen>()

    // Global App Settings
    val appSettings: StateFlow<AppSettingsEntity?> = repository.appSettings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Active Receipt for Modal Dialog
    private val _activeReceipt = MutableStateFlow<ReceiptEntity?>(null)
    val activeReceipt: StateFlow<ReceiptEntity?> = _activeReceipt.asStateFlow()

    // Snackbar / Feedback message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Force Password Change dialog
    private val _showPasswordChangeDialog = MutableStateFlow(false)
    val showPasswordChangeDialog: StateFlow<Boolean> = _showPasswordChangeDialog.asStateFlow()

    // Data streams from repository
    val members: StateFlow<List<MemberEntity>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsList: StateFlow<List<SavingsEntity>> = repository.allSavings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val withdrawals: StateFlow<List<SavingsWithdrawalEntity>> = repository.allWithdrawals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyDues: StateFlow<List<MonthlyDueEntity>> = repository.allMonthlyDues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loanApps: StateFlow<List<LoanApplicationEntity>> = repository.allLoanApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loans: StateFlow<List<LoanEntity>> = repository.allLoans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val installmentPayments: StateFlow<List<InstallmentPaymentEntity>> = repository.allInstallmentPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomes: StateFlow<List<IncomeEntity>> = repository.allIncomes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transfers: StateFlow<List<AccountTransferEntity>> = repository.allTransfers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receipts: StateFlow<List<ReceiptEntity>> = repository.allReceipts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notices: StateFlow<List<NoticeEntity>> = repository.allNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityLogs: StateFlow<List<ActivityLogEntity>> = repository.allActivityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memberMessages: StateFlow<List<MemberMessageEntity>> = repository.allMemberMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val admins: StateFlow<List<UserEntity>> = repository.allAdmins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unread notification count
    val unreadNotifCount: StateFlow<Int> = notifications.map { list ->
        val user = _currentUser.value
        if (user?.role == "MEMBER") {
            list.count { !it.isRead && (it.targetMemberId == null || it.targetMemberId == user.memberId) }
        } else {
            list.count { !it.isRead && (it.targetRole == "ALL" || it.targetRole == "ADMIN") }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Pending count for Approvals Badge
    val pendingApprovalsCount: StateFlow<Int> = combine(loanApps, withdrawals) { apps, wds ->
        val pendingLoans = apps.count { it.status == "APPLIED" || it.status == "UNDER_REVIEW" }
        val pendingWds = wds.count { it.status == "PENDING" }
        pendingLoans + pendingWds
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Aggregated Dashboard Stats
    val dashboardStats: StateFlow<DashboardStats> = combine(
        members, accounts, savingsList, withdrawals, loans, installmentPayments, incomes, expenses
    ) { params: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        val mems = params[0] as List<MemberEntity>
        @Suppress("UNCHECKED_CAST")
        val accs = params[1] as List<AccountEntity>
        @Suppress("UNCHECKED_CAST")
        val savs = params[2] as List<SavingsEntity>
        @Suppress("UNCHECKED_CAST")
        val withs = params[3] as List<SavingsWithdrawalEntity>
        @Suppress("UNCHECKED_CAST")
        val lns = params[4] as List<LoanEntity>
        @Suppress("UNCHECKED_CAST")
        val insts = params[5] as List<InstallmentPaymentEntity>
        @Suppress("UNCHECKED_CAST")
        val incs = params[6] as List<IncomeEntity>
        @Suppress("UNCHECKED_CAST")
        val exps = params[7] as List<ExpenseEntity>

        val todayDate = BanglaUtils.formatDate(System.currentTimeMillis(), false)
        val currentMonth = BanglaUtils.currentMonthYear()

        val validSavings = savs.filter { it.status == "VALID" }
        val approvedWithdrawals = withs.filter { it.status == "APPROVED" }

        val todaySav = validSavings.filter { it.date == todayDate }.sumOf { it.amount }
        val todayInc = incs.filter { it.date == todayDate && it.status == "VALID" }.sumOf { it.amount }
        val todayExp = exps.filter { it.date == todayDate && it.status == "VALID" }.sumOf { it.amount }

        val thisMonthSav = validSavings.filter { it.date.endsWith(currentMonth) }.sumOf { it.amount }
        val totalSavLiability = validSavings.sumOf { it.amount } - approvedWithdrawals.sumOf { it.amount }

        val totalDisbursed = lns.sumOf { it.principalAmount }
        val currentDue = lns.filter { it.status != "FULLY_REPAID" }.sumOf { it.remainingAmount }

        val thisMonthInst = insts.filter { it.paymentDate.endsWith(currentMonth) && it.status == "VALID" }.sumOf { it.amount }
        val totalInstDue = lns.filter { it.status == "OVERDUE" || it.status == "REPAYING" }.sumOf { it.remainingAmount }

        val cash = accs.filter { it.type == "CASH" }.sumOf { it.currentBalance }
        val bank = accs.filter { it.type == "BANK" }.sumOf { it.currentBalance }
        val mobile = accs.filter { it.type in listOf("BKASH", "NAGAD", "ROCKET", "UPAY") }.sumOf { it.currentBalance }
        val totalNet = accs.sumOf { it.currentBalance }

        val totalIncAll = incs.filter { it.status == "VALID" }.sumOf { it.amount }
        val totalExpAll = exps.filter { it.status == "VALID" }.sumOf { it.amount }
        val netProfit = totalIncAll - totalExpAll

        DashboardStats(
            totalMembers = mems.size,
            activeMembers = mems.count { it.status == "ACTIVE" },
            inactiveMembers = mems.count { it.status == "INACTIVE" },
            todaySavings = todaySav,
            todayIncome = todayInc,
            todayExpense = todayExp,
            thisMonthSavings = thisMonthSav,
            totalSavingsLiability = maxOf(0.0, totalSavLiability),
            totalDisbursedLoans = totalDisbursed,
            currentLoanDue = maxOf(0.0, currentDue),
            thisMonthInstallments = thisMonthInst,
            totalInstallmentDue = maxOf(0.0, totalInstDue),
            cashBalance = cash,
            bankBalance = bank,
            mobileBankBalance = mobile,
            totalNetBalance = totalNet,
            totalIncomeAllTime = totalIncAll,
            totalExpenseAllTime = totalExpAll,
            netProfitLoss = netProfit
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    init {
        viewModelScope.launch {
            repository.initDatabase()
        }
    }

    // Navigation
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenHistory.isNotEmpty()) {
            _currentScreen.value = screenHistory.removeAt(screenHistory.size - 1)
            return true
        }
        return false
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun openReceipt(receipt: ReceiptEntity) {
        _activeReceipt.value = receipt
    }

    fun closeReceipt() {
        _activeReceipt.value = null
    }

    // Authentication
    fun login(identifier: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.authenticate(identifier, pass)
            result.onSuccess { user ->
                _currentUser.value = user
                if (user.forcePasswordChange) {
                    _showPasswordChangeDialog.value = true
                }
                if (user.role == "MEMBER") {
                    _currentScreen.value = Screen.MemberDashboard
                } else {
                    _currentScreen.value = Screen.AdminDashboard
                }
                showSnackbar("স্বাগতম, ${user.name}!")
                onSuccess()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "লগইন ব্যর্থ হয়েছে")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        screenHistory.clear()
        _currentScreen.value = Screen.Login
        showSnackbar("সফলভাবে লগআউট হয়েছে")
    }

    fun changePassword(newPass: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.changePassword(user.id, newPass)
            res.onSuccess {
                _showPasswordChangeDialog.value = false
                showSnackbar("পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে!")
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "পাসওয়ার্ড পরিবর্তন ব্যর্থ হয়েছে")
            }
        }
    }

    // Member Actions
    fun createMember(
        name: String, father: String, mother: String, mobile: String, altMobile: String,
        address: String, village: String, upazila: String, district: String, joiningDate: String,
        nomineeName: String, nomineeRel: String, nomineeMobile: String, monthlySavings: Double,
        admissionFee: Double?, accountId: String?, onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val creator = _currentUser.value?.name ?: "Admin"
            val res = repository.createMember(
                name, father, mother, mobile, altMobile, address, village, upazila, district,
                joiningDate, nomineeName, nomineeRel, nomineeMobile, monthlySavings, admissionFee, accountId, creator
            )
            res.onSuccess { mem ->
                showSnackbar("নতুন সদস্য ${mem.name} (${mem.memberId}) তৈরি হয়েছে!")
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "সদস্য তৈরিতে সমস্যা হয়েছে")
            }
        }
    }

    fun updateMember(member: MemberEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            val res = repository.updateMember(member, _currentUser.value?.name ?: "Admin")
            res.onSuccess {
                showSnackbar("সদস্য তথ্য সফলভাবে আপডেট করা হয়েছে!")
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "আপডেট ব্যর্থ হয়েছে")
            }
        }
    }

    // Savings Deposit
    fun depositSavings(
        memberId: String, amount: Double, type: String, accountId: String,
        paymentMethod: String, collector: String, remarks: String, txnId: String, date: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.depositSavings(
                memberId, amount, type, accountId, paymentMethod, collector, remarks, txnId, date
            )
            res.onSuccess { receipt ->
                showSnackbar("সঞ্চয় জমা সফল! রসিদ নং: ${receipt.receiptNumber}")
                _activeReceipt.value = receipt
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "সঞ্চয় জমা ব্যর্থ হয়েছে")
            }
        }
    }

    // Savings Withdrawal
    fun requestSavingsWithdrawal(
        memberId: String, amount: Double, reason: String, accountId: String,
        paymentMethod: String, date: String, onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.requestSavingsWithdrawal(memberId, amount, reason, accountId, paymentMethod, date)
            res.onSuccess {
                showSnackbar("সঞ্চয় উত্তোলনের আবেদন জমা হয়েছে। অনুমোদনের অপেক্ষায় রয়েছে।")
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "আবেদন ব্যর্থ হয়েছে")
            }
        }
    }

    fun approveSavingsWithdrawal(withdrawal: SavingsWithdrawalEntity) {
        viewModelScope.launch {
            val approver = _currentUser.value?.name ?: "Admin"
            val res = repository.approveSavingsWithdrawal(withdrawal, approver)
            res.onSuccess { receipt ->
                showSnackbar("উত্তোলন অনুমোদিত ও রসিদ তৈরি হয়েছে!")
                _activeReceipt.value = receipt
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "উত্তোলন অনুমোদন ব্যর্থ হয়েছে")
            }
        }
    }

    // Loan Operations
    fun applyForLoan(
        memberId: String, amount: Double, purpose: String, duration: Int,
        installments: Int, guarantorName: String, guarantorMobile: String,
        guarantorRel: String, remarks: String, date: String, onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.applyForLoan(
                memberId, amount, purpose, duration, installments, guarantorName,
                guarantorMobile, guarantorRel, remarks, date
            )
            res.onSuccess { app ->
                showSnackbar("ঋণ আবেদন সফলভাবে দাখিল হয়েছে! (আইডি: ${app.applicationId})")
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "আবেদন ব্যর্থ হয়েছে")
            }
        }
    }

    fun reviewLoanApplication(appId: String, newStatus: String, notes: String) {
        viewModelScope.launch {
            val reviewer = _currentUser.value?.name ?: "Admin"
            val res = repository.reviewLoanApplication(appId, newStatus, notes, reviewer)
            res.onSuccess {
                showSnackbar("ঋণ আবেদন আপডেট করা হয়েছে!")
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "পর্যালোচনা ব্যর্থ হয়েছে")
            }
        }
    }

    fun disburseLoan(
        app: LoanApplicationEntity, approvedAmount: Double, interestRate: Double,
        serviceCharge: Double, accountId: String, paymentMethod: String,
        installments: Int, date: String, onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val disbursedBy = _currentUser.value?.name ?: "Admin"
            val res = repository.disburseLoan(
                app, approvedAmount, interestRate, serviceCharge, accountId, paymentMethod, installments, disbursedBy, date
            )
            res.onSuccess { pair ->
                showSnackbar("ঋণ বিতরণ সম্পন্ন! ঋণ আইডি: ${pair.first.loanId}")
                _activeReceipt.value = pair.second
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "ঋণ বিতরণ ব্যর্থ হয়েছে")
            }
        }
    }

    fun collectInstallment(
        loanId: String, installmentNumber: Int, amount: Double, accountId: String,
        paymentMethod: String, collector: String, remarks: String, txnId: String, date: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.collectInstallment(
                loanId, installmentNumber, amount, accountId, paymentMethod, collector, remarks, txnId, date
            )
            res.onSuccess { receipt ->
                showSnackbar("কিস্তি আদায় সফল! রসিদ নং: ${receipt.receiptNumber}")
                _activeReceipt.value = receipt
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "কিস্তি আদায় ব্যর্থ হয়েছে")
            }
        }
    }

    // Income & Expense
    fun addIncome(
        title: String, category: String, amount: Double, accountId: String,
        paymentMethod: String, ref: String, details: String, date: String, onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value?.name ?: "Admin"
            val res = repository.addIncome(title, category, amount, accountId, paymentMethod, ref, details, user, date)
            res.onSuccess { inc ->
                showSnackbar("আয় সফলভাবে যুক্ত হয়েছে! ৳ $amount")
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "আয় যোগ করতে সমস্যা হয়েছে")
            }
        }
    }

    fun addExpense(
        title: String, category: String, amount: Double, accountId: String,
        paymentMethod: String, voucher: String, remarks: String, date: String, onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value?.name ?: "Admin"
            val res = repository.addExpense(title, category, amount, accountId, paymentMethod, voucher, user, remarks, date)
            res.onSuccess { exp ->
                showSnackbar("ব্যয় সফলভাবে যুক্ত হয়েছে! ৳ $amount")
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "ব্যয় যোগ করতে সমস্যা হয়েছে")
            }
        }
    }

    // Fund Transfer
    fun transferFunds(
        fromId: String, toId: String, amount: Double, ref: String, remarks: String, onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value?.name ?: "Admin"
            val res = repository.transferFunds(fromId, toId, amount, ref, remarks, user)
            res.onSuccess {
                showSnackbar("ফান্ড ট্রান্সফার সফলভাবে সম্পন্ন হয়েছে!")
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "ট্রান্সফার ব্যর্থ হয়েছে")
            }
        }
    }

    // Notices & Messaging
    fun publishNotice(title: String, content: String, category: String, isPinned: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value?.name ?: "Admin"
            val res = repository.publishNotice(title, content, category, user, isPinned)
            res.onSuccess {
                showSnackbar("নোটিশ সফলভাবে প্রকাশিত হয়েছে!")
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "নোটিশ প্রকাশ ব্যর্থ হয়েছে")
            }
        }
    }

    fun deleteNotice(notice: NoticeEntity) {
        viewModelScope.launch {
            repository.deleteNotice(notice)
            showSnackbar("নোটিশ মুছে ফেলা হয়েছে")
        }
    }

    fun sendMemberMessage(subject: String, message: String, onDone: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.sendMemberMessage(user.memberId ?: user.username, user.name, subject, message)
            res.onSuccess {
                showSnackbar("আপনার বার্তা অ্যাডমিনের কাছে পাঠানো হয়েছে!")
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "বার্তা পাঠানো ব্যর্থ হয়েছে")
            }
        }
    }

    fun replyMemberMessage(msg: MemberMessageEntity, reply: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val res = repository.replyMemberMessage(msg, reply)
            res.onSuccess {
                showSnackbar("বার্তার উত্তর পাঠানো হয়েছে!")
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "উত্তর পাঠানো ব্যর্থ হয়েছে")
            }
        }
    }

    // Notifications
    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            showSnackbar("সব বিজ্ঞপ্তি পঠিত হিসেবে চিহ্নিত করা হয়েছে")
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    // Admin Creation & Settings
    fun createAdmin(username: String, name: String, phone: String, role: String, permissions: String, pass: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val res = repository.createAdmin(username, name, phone, role, permissions, pass)
            res.onSuccess {
                showSnackbar("নতুন অ্যাডমিন ${it.name} তৈরি হয়েছে!")
                onDone()
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "অ্যাডমিন তৈরিতে সমস্যা হয়েছে")
            }
        }
    }

    fun toggleAdminStatus(userId: String) {
        viewModelScope.launch {
            val res = repository.toggleAdminStatus(userId)
            res.onSuccess {
                showSnackbar("অ্যাডমিন স্ট্যাটাস পরিবর্তন করা হয়েছে")
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "স্ট্যাটাস পরিবর্তন ব্যর্থ")
            }
        }
    }

    fun updateSettings(settings: AppSettingsEntity) {
        viewModelScope.launch {
            val res = repository.updateAppSettings(settings)
            res.onSuccess {
                showSnackbar("সেটিংস সফলভাবে সংরক্ষণ করা হয়েছে!")
            }.onFailure { ex ->
                showSnackbar(ex.message ?: "সেটিংস সংরক্ষণ ব্যর্থ")
            }
        }
    }
}
