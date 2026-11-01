package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

object BanglaUtils {

    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    fun toBanglaDigit(number: String): String {
        val sb = StringBuilder()
        for (ch in number) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toEnglishDigit(number: String): String {
        val sb = StringBuilder()
        for (ch in number) {
            val idx = banglaDigits.indexOf(ch)
            if (idx != -1) {
                sb.append(englishDigits[idx])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatNumber(number: Number, useBangla: Boolean = true): String {
        val df = DecimalFormat("#,##0.##")
        val formatted = df.format(number)
        return if (useBangla) toBanglaDigit(formatted) else formatted
    }

    fun formatCurrency(amount: Double, useBangla: Boolean = true, symbol: String = "৳"): String {
        val df = DecimalFormat("#,##0.00")
        val formatted = df.format(amount)
        val text = if (useBangla) toBanglaDigit(formatted) else formatted
        return "$symbol $text"
    }

    fun formatDate(millis: Long = System.currentTimeMillis(), useBangla: Boolean = true): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val dateStr = sdf.format(Date(millis))
        return if (useBangla) toBanglaDigit(dateStr) else dateStr
    }

    fun formatDateTime(millis: Long = System.currentTimeMillis(), useBangla: Boolean = true): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US)
        val dateStr = sdf.format(Date(millis))
        return if (useBangla) toBanglaDigit(dateStr) else dateStr
    }

    fun currentMonthYear(): String {
        val sdf = SimpleDateFormat("MM-yyyy", Locale.US)
        return sdf.format(Date())
    }

    fun generateMemberId(serial: Int): String {
        return "BMS-%04d".format(serial)
    }

    fun generateReceiptNumber(typePrefix: String = "REC"): String {
        val datePart = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val randomPart = Random().nextInt(9000) + 1000
        return "$typePrefix-$datePart-$randomPart"
    }

    fun generateLoanId(serial: Int): String {
        return "LN-%04d".format(serial)
    }

    fun generateAppId(serial: Int): String {
        return "APP-%04d".format(serial)
    }

    private val units = arrayOf(
        "", "এক", "দুই", "তিন", "চার", "পাঁচ", "ছয়", "সাত", "আট", "নয়", "দশ",
        "এগারো", "বারো", "তেরো", "চোদ্দ", "পনেরো", "ষোল", "সতেরো", "আঠারো", "উনিশ", "বিশ"
    )

    fun amountInBengaliWords(amountDouble: Double): String {
        val amount = amountDouble.toLong()
        if (amount == 0L) return "শূন্য টাকা মাত্র"
        var remaining = amount
        val sb = StringBuilder()

        val crore = remaining / 10000000L
        if (crore > 0) {
            sb.append(numberToWords(crore)).append(" কোটি ")
            remaining %= 10000000L
        }

        val lakh = remaining / 100000L
        if (lakh > 0) {
            sb.append(numberToWords(lakh)).append(" লক্ষ ")
            remaining %= 100000L
        }

        val thousand = remaining / 1000L
        if (thousand > 0) {
            sb.append(numberToWords(thousand)).append(" হাজার ")
            remaining %= 1000L
        }

        val hundred = remaining / 100L
        if (hundred > 0) {
            sb.append(numberToWords(hundred)).append(" শত ")
            remaining %= 100L
        }

        if (remaining > 0) {
            sb.append(numberToWords(remaining)).append(" ")
        }

        sb.append("টাকা মাত্র")
        return sb.toString().trim()
    }

    private fun numberToWords(num: Long): String {
        if (num <= 20) return units[num.toInt()]
        val tens = num / 10
        val rem = num % 10
        val tensStr = when (tens) {
            2L -> "কুড়ি"
            3L -> "ত্রিশ"
            4L -> "চল্লিশ"
            5L -> "পঞ্চাশ"
            6L -> "ষাট"
            7L -> "সত্তর"
            8L -> "আশি"
            9L -> "নব্বই"
            else -> ""
        }
        return if (rem == 0L) tensStr else "$tensStr ${units[rem.toInt()]}"
    }
}
