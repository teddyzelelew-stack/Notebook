package com.example.util

import android.icu.util.Calendar
import android.icu.util.ULocale

object EthiopianDateHelper {

    val MONTH_NAMES_AMHARIC = listOf(
        "መስከረም",  // Meskerem (1)
        "ጥቅምት",   // Tekemt (2)
        "ሕዳር",    // Hidar (3)
        "ታኅሣሥ",   // Tahsas (4)
        "ጥር",     // Tir (5)
        "የካቲት",   // Yakatit (6)
        "መጋቢት",   // Megabit (7)
        "ሚያዝያ",   // Miyazya (8)
        "ግንቦት",   // Ginbot (9)
        "ሰኔ",     // Sene (10)
        "ሐምሌ",    // Hamle (11)
        "ነሐሴ",    // Nehasse (12)
        "ጳጉሜን"    // Pagumen (13)
    )

    val WEEKDAY_NAMES_AMHARIC = listOf(
        "እሑድ",   // Sunday
        "ሰኞ",    // Monday
        "ማክሰኞ",  // Tuesday
        "ረቡዕ",   // Wednesday
        "ሐሙስ",   // Thursday
        "ዓርብ",   // Friday
        "ቅዳሜ"    // Saturday
    )

    val WEEKDAY_SHORT_NAMES_AMHARIC = listOf(
        "እ", "ሰ", "ማ", "ረ", "ሐ", "ዓ", "ቅ"
    )

    data class EthDate(
        val year: Int,
        val month: Int, // 0-indexed (0 to 12)
        val day: Int
    ) {
        val monthNameAmharic: String
            get() = MONTH_NAMES_AMHARIC.getOrElse(month) { "" }

        fun formatFullAmharic(): String {
            return "$monthNameAmharic $day ቀን $year ዓ.ም"
        }
    }

    /**
     * Convert standard timestamp in milliseconds to Ethiopian Date
     */
    fun fromMillis(millis: Long): EthDate {
        val cal = Calendar.getInstance(ULocale("am_ET@calendar=ethiopian"))
        cal.timeInMillis = millis
        return EthDate(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH), // 0-indexed
            day = cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Convert Ethiopian Date back to standard milliseconds
     */
    fun toMillis(year: Int, month0Indexed: Int, day: Int): Long {
        val cal = Calendar.getInstance(ULocale("am_ET@calendar=ethiopian"))
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month0Indexed)
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Get the number of days in a given Ethiopian month (30 for months 0-11, 5 or 6 for Pagume)
     */
    fun getDaysInMonth(year: Int, month0Indexed: Int): Int {
        if (month0Indexed < 12) return 30
        val cal = Calendar.getInstance(ULocale("am_ET@calendar=ethiopian"))
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month0Indexed)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * Get the 1-indexed day of week (1 = Sunday, 7 = Saturday) for the first day of an Ethiopian month
     */
    fun getStartDayOfWeek(year: Int, month0Indexed: Int): Int {
        val cal = Calendar.getInstance(ULocale("am_ET@calendar=ethiopian"))
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month0Indexed)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.get(Calendar.DAY_OF_WEEK)
    }

    /**
     * Get the current date as EthDate
     */
    fun currentEthDate(): EthDate {
        return fromMillis(System.currentTimeMillis())
    }
}
