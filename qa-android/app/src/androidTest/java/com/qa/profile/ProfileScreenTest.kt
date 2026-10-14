package com.qa.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.material3.MaterialTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


class ProfileScreenTest {
    @get:Rule val rule = createComposeRule()

    private val today = LocalDate.of(2026, 10, 13)
    private lateinit var repo: FakeRepo
    private lateinit var vm: ProfileViewModel

    private class FakeRepo : ProfileRepository {
        var calls = 0; var stored: Profile? = null
        override fun save(profile: Profile) { calls++; stored = profile }
        override fun load() = stored
    }

    @Before fun setUp() {
        repo = FakeRepo()
        vm = ProfileViewModel(repo) { today }
        rule.setContent { MaterialTheme { ProfileScreen(vm) } }
    }

    private fun type(tag: String, text: String) {
        rule.onNodeWithTag(tag).performScrollTo().performTextReplacement(text)
    }
    private fun fillValid(first: String = "Joel", nick: String = "Joe", birth: String = "15 Aug 1995") {
        type(Tags.FirstName, first); type(Tags.MiddleName, "K"); type(Tags.LastName, "Osteen")
        type(Tags.Nickname, nick); type(Tags.BirthDate, birth)
    }
    private fun save() { rule.onNodeWithTag(Tags.Save).performScrollTo().performClick() }
    private fun fmt(d: LocalDate) = d.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
    private fun errorOf(f: Field) = rule.onNodeWithTag(Tags.error(f))

    // TC-PRF-001
    @Test fun initialScreenMatchesDesign() {
        rule.onAllNodes(hasText("Input your profile")).onFirst().assertIsDisplayed()
        rule.onNodeWithContentDescription("Back").assertIsDisplayed()
        listOf("First Name", "Middle Name", "Last Name", "Nickname", "Gender", "Birth Date").forEach {
            rule.onAllNodes(hasText(it)).onFirst().assertExists()   // Nickname punya label + placeholder
        }
        listOf("e.g Joel", "e.g K", "e.g Osteen", "Nickname", "DD MMM YYYY").forEach {
            rule.onAllNodes(hasText(it)).onFirst().assertExists()
        }
        rule.onNodeWithTag(Tags.Save).performScrollTo().assertIsDisplayed().assertIsEnabled()
    }

    // TC-PRF-002
    @Test fun optionalTagOnlyOnMiddleAndLastName() {
        rule.onAllNodes(hasText("(Optional)")).assertCountEquals(2)
    }

    // TC-PRF-003, 020
    @Test fun genderDefaultsToMaleAndIsMutuallyExclusive() {
        rule.onNodeWithTag(Tags.Male).assertIsSelected()
        rule.onNodeWithTag(Tags.Female).assertIsNotSelected()
        rule.onNodeWithTag(Tags.Female).performClick()
        rule.onNodeWithTag(Tags.Female).assertIsSelected()
        rule.onNodeWithTag(Tags.Male).assertIsNotSelected()
    }

    // TC-PRF-004
    @Test fun saveWithValidDataShowsSuccess() {
        fillValid(); save()
        rule.onNodeWithTag(Tags.Message).assertTextContains("saved", substring = true, ignoreCase = true)
        assertEquals(1, repo.calls)
    }

    // TC-PRF-006, 018, 022, 029
    @Test fun emptyFormShowsRequiredErrors() {
        save()
        errorOf(Field.FirstName).assertTextEquals("First Name is required")
        errorOf(Field.Nickname).assertTextEquals("Nickname is required")
        errorOf(Field.BirthDate).assertTextEquals("Birth Date is required")
        errorOf(Field.MiddleName).assertDoesNotExist()
        errorOf(Field.LastName).assertDoesNotExist()
        assertEquals(0, repo.calls)
    }

    // TC-PRF-008, 009
    @Test fun invalidCharactersInFirstNameAreRejected() {
        fillValid(first = "Joel123"); save()
        errorOf(Field.FirstName).assertTextContains("invalid", substring = true, ignoreCase = true)
        assertEquals(0, repo.calls)
    }

    // TC-PRF-011
    @Test fun firstNameOver50CharactersIsRejected() {
        fillValid(first = "A".repeat(51)); save()
        errorOf(Field.FirstName).assertTextContains("at most 50", substring = true)
    }

    // TC-PRF-023
    @Test fun wrongBirthDateFormatIsRejected() {
        fillValid(birth = "1995-08-15"); save()
        errorOf(Field.BirthDate).assertTextContains("Invalid date format", substring = true)
    }

    // TC-PRF-026
    @Test fun futureBirthDateIsRejected() {
        fillValid(birth = fmt(today.plusYears(1))); save()
        errorOf(Field.BirthDate).assertTextContains("future", substring = true)
    }

    // TC-PRF-028
    @Test fun minimumAgeBoundary() {
        fillValid(birth = fmt(today.minusYears(5))); save()
        rule.onNodeWithTag(Tags.Message).assertExists()
        type(Tags.BirthDate, fmt(today.minusYears(5).plusDays(1))); save()
        errorOf(Field.BirthDate).assertTextContains("Minimum age", substring = true)
    }

    // TC-PRF-030
    @Test fun errorClearsAfterCorrection() {
        fillValid(first = ""); save()
        errorOf(Field.FirstName).assertIsDisplayed()
        type(Tags.FirstName, "Joel"); save()
        errorOf(Field.FirstName).assertDoesNotExist()
        rule.onNodeWithTag(Tags.Message).assertExists()
    }

    // TC-PRF-031
    @Test fun doubleTapSaveSubmitsOnce() {
        fillValid()
        rule.onNodeWithTag(Tags.Save).performScrollTo().performClick().performClick()
        assertEquals(1, repo.calls)
    }

    // TC-PRF-021
    @Test fun femaleGenderIsSaved() {
        rule.onNodeWithTag(Tags.Female).performScrollTo().performClick()
        fillValid(); save()
        assertEquals(Gender.Female, repo.stored?.gender)
    }

    // TC-PRF-033
    @Test fun xssPayloadIsRejected() {
        fillValid(first = "<script>alert(1)</script>"); save()
        errorOf(Field.FirstName).assertExists()
        assertEquals(0, repo.calls)
    }

    // TC-PRF-034
    @Test fun sqlInjectionInNicknameIsRejected() {
        fillValid(nick = "' OR 1=1 --"); save()
        errorOf(Field.Nickname).assertExists()
        assertEquals(0, repo.calls)
    }

    // TC-PRF-016
    @Test fun digitsInMiddleNameAreRejected() {
        fillValid(); type(Tags.MiddleName, "K9"); save()
        errorOf(Field.MiddleName).assertTextContains("invalid", substring = true, ignoreCase = true)
        assertEquals(0, repo.calls)
    }

    // TC-PRF-017
    @Test fun digitsInLastNameAreRejected() {
        fillValid(); type(Tags.LastName, "Osteen2"); save()
        errorOf(Field.LastName).assertTextContains("invalid", substring = true, ignoreCase = true)
        assertEquals(0, repo.calls)
    }
}
