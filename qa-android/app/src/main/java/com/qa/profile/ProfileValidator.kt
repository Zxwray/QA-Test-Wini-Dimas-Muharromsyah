package com.qa.profile

import java.time.DateTimeException
import java.time.LocalDate

/**
 * Aturan validasi layar "Input your profile".
 *
 * CATATAN: soal tidak memberi spesifikasi validasi. Angka dan pesan di bawah adalah ASUMSI
 */
object ProfileValidator {
    const val MAX_NAME = 50
    const val MAX_NICKNAME = 20
    const val MIN_AGE_YEARS = 5L

    // huruf (Unicode), spasi, apostrof, titik, tanda hubung; harus diawali huruf
    private val NAME_REGEX = Regex("^\\p{L}[\\p{L}\\s'.\\-]*$")
    private val DATE_REGEX = Regex("^(\\d{2}) ([A-Za-z]{3}) (\\d{4})$")
    private val MONTHS = listOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")

    fun validateName(label: String, raw: String, required: Boolean, max: Int): String? {
        val v = raw.trim()
        if (v.isEmpty()) return if (required) "$label is required" else null
        if (v.length > max) return "$label must be at most $max characters"
        if (!NAME_REGEX.matches(v)) return "$label contains invalid characters"
        return null
    }

    /** Mengurai "DD MMM YYYY" (mis. "15 Aug 1995"); null bila format salah atau tanggal tidak ada. */
    fun parseDate(raw: String): LocalDate? {
        val m = DATE_REGEX.matchEntire(raw.trim()) ?: return null
        val month = MONTHS.indexOf(m.groupValues[2].lowercase()) + 1
        if (month == 0) return null
        return try {
            LocalDate.of(m.groupValues[3].toInt(), month, m.groupValues[1].toInt())
        } catch (e: DateTimeException) {
            null // mis. 29 Feb 2001 atau 32 Jan 2000
        }
    }

    fun validateBirthDate(raw: String, today: LocalDate): String? {
        if (raw.isBlank()) return "Birth Date is required"
        val date = parseDate(raw) ?: return "Invalid date format, use DD MMM YYYY"
        if (date.isAfter(today)) return "Birth Date cannot be in the future"
        if (date.isAfter(today.minusYears(MIN_AGE_YEARS))) return "Minimum age is $MIN_AGE_YEARS years"
        if (date.year < 1900) return "Birth Date is not valid"
        return null
    }
}
