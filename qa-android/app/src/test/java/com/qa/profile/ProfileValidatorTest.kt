package com.qa.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/** Unit test JVM untuk aturan validasi. ID TC mengacu ke QA_Test_Cases.xlsx (sheet 1-UI Form Profil). */
class ProfileValidatorTest {
    private val today = LocalDate.of(2026, 10, 13)
    private fun name(v: String, required: Boolean = true, max: Int = 50) =
        ProfileValidator.validateName("First Name", v, required, max)

    // TC-PRF-006, 007
    @Test fun requiredNameEmptyOrBlankIsRejected() {
        assertEquals("First Name is required", name(""))
        assertEquals("First Name is required", name("     "))
    }

    // Middle/Last Name opsional
    @Test fun optionalNameEmptyIsAccepted() {
        assertNull(name("", required = false))
    }

    // TC-PRF-008, 009
    @Test fun digitsAndSymbolsAreRejected() {
        assertEquals("First Name contains invalid characters", name("Joel123"))
        assertEquals("First Name contains invalid characters", name("Joel@#$"))
        assertEquals("First Name contains invalid characters", name("<script>alert(1)</script>")) // TC-PRF-033
        assertEquals("First Name contains invalid characters", name("' OR 1=1 --"))               // TC-PRF-034
    }

    // TC-PRF-010, 011 (boundary)
    @Test fun nameLengthBoundary() {
        assertNull(name("A".repeat(50)))
        assertEquals("First Name must be at most 50 characters", name("A".repeat(51)))
    }

    // TC-PRF-019 (boundary nickname)
    @Test fun nicknameLengthBoundary() {
        assertNull(name("N".repeat(20), max = 20))
        assertEquals("First Name must be at most 20 characters", name("N".repeat(21), max = 20))
    }

    // TC-PRF-012..014
    @Test fun legitimateNamesAreAccepted() {
        listOf("O'Brien", "Jean-Luc", "José", "Mary Ann", "St. John").forEach { assertNull(it, name(it)) }
    }

    @Test fun nameCannotStartWithSymbol() {
        assertNotNull(name("-Joel"))
        assertNotNull(name("'Joel"))
    }

    // TC-PRF-022
    @Test fun birthDateRequired() {
        assertEquals("Birth Date is required", ProfileValidator.validateBirthDate("", today))
        assertEquals("Birth Date is required", ProfileValidator.validateBirthDate("   ", today))
    }

    // TC-PRF-023
    @Test fun wrongDateFormatsAreRejected() {
        listOf("1995-08-15", "15/08/1995", "abc", "32 Jan 2000", "15 Foo 1995", "5 Aug 1995", "15 August 1995").forEach {
            assertEquals(it, "Invalid date format, use DD MMM YYYY", ProfileValidator.validateBirthDate(it, today))
        }
    }

    // TC-PRF-024, 025
    @Test fun leapDayHandling() {
        assertEquals("Invalid date format, use DD MMM YYYY", ProfileValidator.validateBirthDate("29 Feb 2001", today))
        assertNull(ProfileValidator.validateBirthDate("29 Feb 2000", today))
    }

    @Test fun monthIsCaseInsensitive() {
        assertNull(ProfileValidator.validateBirthDate("15 aug 1995", today))
        assertNull(ProfileValidator.validateBirthDate("15 AUG 1995", today))
    }

    // TC-PRF-026, 027
    @Test fun futureAndTodayAreRejected() {
        assertEquals("Birth Date cannot be in the future", ProfileValidator.validateBirthDate("14 Oct 2026", today))
        assertEquals("Minimum age is 5 years", ProfileValidator.validateBirthDate("13 Oct 2026", today))
    }

    // TC-PRF-028 (boundary usia 5 tahun)
    @Test fun minimumAgeBoundary() {
        assertNull(ProfileValidator.validateBirthDate("13 Oct 2021", today))                       // tepat 5 tahun
        assertEquals("Minimum age is 5 years", ProfileValidator.validateBirthDate("14 Oct 2021", today)) // kurang sehari
    }

    @Test fun veryOldDateIsRejected() {
        assertEquals("Birth Date is not valid", ProfileValidator.validateBirthDate("01 Jan 1899", today))
        assertNull(ProfileValidator.validateBirthDate("01 Jan 1900", today))
    }

    // TC-PRF-016, 017
    @Test fun middleAndLastNameWithDigitsAreRejected() {
        assertEquals("Middle Name contains invalid characters", ProfileValidator.validateName("Middle Name", "K9", false, 50))
        assertEquals("Last Name contains invalid characters", ProfileValidator.validateName("Last Name", "Osteen2", false, 50))
    }
}
