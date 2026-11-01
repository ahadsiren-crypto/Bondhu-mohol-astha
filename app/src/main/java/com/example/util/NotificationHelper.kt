package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R

object NotificationHelper {

    const val CHANNEL_SAVINGS = "channel_savings"
    const val CHANNEL_LOANS = "channel_loans"
    const val CHANNEL_NOTICES = "channel_notices"
    const val CHANNEL_SYSTEM = "channel_system"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val chSavings = NotificationChannel(
                CHANNEL_SAVINGS,
                "সঞ্চয় ও কিস্তি (Savings & Installments)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "সঞ্চয় জমা, উত্তোলন ও কিস্তি পরিশোধ সংক্রান্ত বিজ্ঞপ্তি"
            }

            val chLoans = NotificationChannel(
                CHANNEL_LOANS,
                "ঋণ ও অনুমোদন (Loans & Approvals)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "ঋণ আবেদন, অনুমোদন ও বিতরণ সংক্রান্ত বিজ্ঞপ্তি"
            }

            val chNotices = NotificationChannel(
                CHANNEL_NOTICES,
                "নোটিশ ও ঘোষণা (Notices & Announcements)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "সমিতির সাধারণ ও জরুরি নোটিশ"
            }

            val chSystem = NotificationChannel(
                CHANNEL_SYSTEM,
                "সিস্টেম ও ব্যাকআপ (System & Reminders)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "মাসিক কিস্তি ও সঞ্চয় রিমাইন্ডার"
            }

            manager.createNotificationChannel(chSavings)
            manager.createNotificationChannel(chLoans)
            manager.createNotificationChannel(chNotices)
            manager.createNotificationChannel(chSystem)
        }
    }

    fun postNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                // Permission not yet granted, skip posting system notification
                return
            }
        }

        try {
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: Exception) {
            // Graceful handling
        }
    }
}
