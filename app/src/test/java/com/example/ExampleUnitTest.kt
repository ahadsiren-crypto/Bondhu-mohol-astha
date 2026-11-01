package com.example

import com.example.util.BanglaUtils
import com.example.util.SecurityUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBanglaDigitConversion() {
        val english = "12345"
        val bangla = BanglaUtils.toBanglaDigit(english)
        assertEquals("১২৩৪৫", bangla)

        val convertedBack = BanglaUtils.toEnglishDigit(bangla)
        assertEquals("12345", convertedBack)
    }

    @Test
    fun testCurrencyFormatting() {
        val amount = 1500.0
        val formattedBn = BanglaUtils.formatCurrency(amount, useBangla = true, symbol = "৳")
        assertTrue(formattedBn.startsWith("৳"))
        assertTrue(formattedBn.contains("১,৫০০"))

        val formattedEn = BanglaUtils.formatCurrency(amount, useBangla = false, symbol = "৳")
        assertEquals("৳ 1,500.00", formattedEn)
    }

    @Test
    fun testMemberIdGeneration() {
        val id1 = BanglaUtils.generateMemberId(1)
        assertEquals("BMS-0001", id1)

        val id42 = BanglaUtils.generateMemberId(42)
        assertEquals("BMS-0042", id42)
    }

    @Test
    fun testSecurityPasswordHashingAndVerification() {
        val rawPassword = "SuperSecretPassword123!"
        val salt = SecurityUtils.generateSalt()
        assertNotNull(salt)
        assertTrue(salt.isNotBlank())

        val hash = SecurityUtils.hashPassword(rawPassword, salt)
        assertNotNull(hash)
        assertNotEquals(rawPassword, hash)

        // Verification with correct password
        val isCorrect = SecurityUtils.verifyPassword(rawPassword, salt, hash)
        assertTrue(isCorrect)

        // Verification with wrong password
        val isWrong = SecurityUtils.verifyPassword("WrongPassword", salt, hash)
        assertFalse(isWrong)
    }

    @Test
    fun testAmountInBengaliWords() {
        val words1000 = BanglaUtils.amountInBengaliWords(1000.0)
        assertTrue(words1000.contains("এক হাজার"))
        assertTrue(words1000.endsWith("টাকা মাত্র"))

        val wordsZero = BanglaUtils.amountInBengaliWords(0.0)
        assertEquals("শূন্য টাকা মাত্র", wordsZero)
    }
}
