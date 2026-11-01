package com.example.util

import android.content.Context
import android.content.Intent

object ReceiptExporter {

    fun shareReceipt(
        context: Context,
        somitiNameBn: String,
        somitiPhone: String,
        receiptNumber: String,
        memberName: String,
        memberId: String,
        typeTitle: String,
        date: String,
        amount: Double,
        previousBalance: Double,
        currentBalance: Double,
        paymentMethod: String,
        transactionId: String,
        collectorName: String
    ) {
        val amountWords = BanglaUtils.amountInBengaliWords(amount)
        val text = buildString {
            append("================================\n")
            append("      $somitiNameBn\n")
            append("  আর্থিক লেনদেনের ডিজিটাল রসিদ\n")
            append("================================\n")
            append("রসিদ নং: $receiptNumber\n")
            append("তারিখ: $date\n")
            append("সদস্যের নাম: $memberName\n")
            append("সদস্য আইডি: $memberId\n")
            append("লেনদেনের ধরন: $typeTitle\n")
            append("টাকার পরিমাণ: ৳ ${BanglaUtils.formatNumber(amount, true)}\n")
            append("কথায়: $amountWords\n")
            append("পেমেন্ট পদ্ধতি: $paymentMethod\n")
            if (transactionId.isNotBlank()) {
                append("ট্রানজেকশন আইডি: $transactionId\n")
            }
            append("পূর্বের ব্যালেন্স: ৳ ${BanglaUtils.formatNumber(previousBalance, true)}\n")
            append("বর্তমান ব্যালেন্স: ৳ ${BanglaUtils.formatNumber(currentBalance, true)}\n")
            append("সংগ্রহকারী: $collectorName\n")
            append("--------------------------------\n")
            append("যোগাযোগ: $somitiPhone\n")
            append("বন্ধু মহল সমিতি — বিশ্বস্ত সঞ্চয় ও সেবায় নিয়োজিত\n")
            append("================================")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "রসিদ শেয়ার করুন")
        context.startActivity(shareIntent)
    }

    fun shareReportCsv(context: Context, reportTitle: String, csvContent: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, reportTitle)
            putExtra(Intent.EXTRA_TEXT, csvContent)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "রিপোর্ট এক্সপোর্ট করুন")
        context.startActivity(shareIntent)
    }
}
