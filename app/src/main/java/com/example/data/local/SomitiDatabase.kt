package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        MemberEntity::class,
        AccountEntity::class,
        SavingsEntity::class,
        SavingsWithdrawalEntity::class,
        MonthlyDueEntity::class,
        LoanApplicationEntity::class,
        LoanEntity::class,
        LoanInstallmentEntity::class,
        InstallmentPaymentEntity::class,
        IncomeEntity::class,
        ExpenseEntity::class,
        AccountTransferEntity::class,
        ReceiptEntity::class,
        NoticeEntity::class,
        NotificationEntity::class,
        ActivityLogEntity::class,
        MemberMessageEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SomitiDatabase : RoomDatabase() {

    abstract fun somitiDao(): SomitiDao

    companion object {
        @Volatile
        private var INSTANCE: SomitiDatabase? = null

        fun getDatabase(context: Context): SomitiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SomitiDatabase::class.java,
                    "bondhu_mohol_somiti.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
