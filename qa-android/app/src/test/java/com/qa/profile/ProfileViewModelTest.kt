package com.qa.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Unit test JVM untuk perilaku tombol Save. */
class ProfileViewModelTest {
    private val today = LocalDate.of(2026, 10, 13)
    private fun vm(repo: FakeProfileRepository = FakeProfileRepository()) = ProfileViewModel(repo) { today }

    private fun ProfileViewModel.fillValid() {
        onFirstName("Joel"); onMiddleName("K"); onLastName("Osteen"); onNickname("Joe")
        onGender(Gender.Male); onBirthDate("15 Aug 1995")
    }

    // TC-PRF-003
    @Test fun defaultGenderIsMale() = assertEquals(Gender.Male, vm().gender)

    // TC-PRF-004
    @Test fun saveWithValidDataStoresProfile() {
        val repo = FakeProfileRepository(); val v = vm(repo)
        v.fillValid(); v.onSave()
        assertTrue(v.errors.isEmpty())
        assertEquals("Profile saved successfully", v.message)
        assertEquals(Profile("Joel", "K", "Osteen", "Joe", Gender.Male, "15 Aug 1995"), repo.saved)
    }

    // TC-PRF-005
    @Test fun saveWithOnlyRequiredFields() {
        val repo = FakeProfileRepository(); val v = vm(repo)
        v.onFirstName("Joel"); v.onNickname("Joe"); v.onBirthDate("15 Aug 1995"); v.onSave()
        assertTrue(v.errors.isEmpty()); assertEquals(1, repo.saveCalls)
    }

    // TC-PRF-029
    @Test fun emptyFormShowsAllRequiredErrorsAtOnce() {
        val repo = FakeProfileRepository(); val v = vm(repo)
        v.onSave()
        assertEquals(setOf(Field.FirstName, Field.Nickname, Field.BirthDate), v.errors.keys)
        assertEquals(0, repo.saveCalls); assertNull(v.message)
    }

    // TC-PRF-030
    @Test fun errorDisappearsAfterCorrection() {
        val v = vm(); v.fillValid(); v.onFirstName(""); v.onSave()
        assertEquals("First Name is required", v.errors[Field.FirstName])
        v.onFirstName("Joel"); v.onSave()
        assertTrue(v.errors.isEmpty()); assertEquals("Profile saved successfully", v.message)
    }

    // TC-PRF-031 (anti double submit)
    @Test fun repeatedSaveWithoutChangesSavesOnce() {
        val repo = FakeProfileRepository(); val v = vm(repo)
        v.fillValid(); v.onSave(); v.onSave(); v.onSave()
        assertEquals(1, repo.saveCalls); assertEquals(1, v.saveCount)
    }

    @Test fun editAfterSaveSavesAgain() {
        val repo = FakeProfileRepository(); val v = vm(repo)
        v.fillValid(); v.onSave(); v.onNickname("Joey"); v.onSave()
        assertEquals(2, repo.saveCalls)
    }

    // TC-PRF-015
    @Test fun valuesAreTrimmedWhenSaved() {
        val repo = FakeProfileRepository(); val v = vm(repo)
        v.fillValid(); v.onFirstName("   Joel   "); v.onNickname("  Joe "); v.onSave()
        assertEquals("Joel", repo.saved?.firstName); assertEquals("Joe", repo.saved?.nickname)
    }

    // TC-PRF-021
    @Test fun femaleGenderIsSaved() {
        val repo = FakeProfileRepository(); val v = vm(repo)
        v.fillValid(); v.onGender(Gender.Female); v.onSave()
        assertEquals(Gender.Female, repo.saved?.gender)
    }

    // TC-PRF-032 (data tampil kembali)
    @Test fun savedDataIsRestoredOnNewInstance() {
        val repo = FakeProfileRepository(); val first = vm(repo)
        first.fillValid(); first.onGender(Gender.Female); first.onSave()
        val reopened = vm(repo)
        assertEquals("Joel", reopened.firstName); assertEquals("Joe", reopened.nickname)
        assertEquals(Gender.Female, reopened.gender); assertEquals("15 Aug 1995", reopened.birthDate)
    }

    @Test fun invalidDataIsNeverStored() {
        val repo = FakeProfileRepository(); val v = vm(repo)
        v.fillValid(); v.onFirstName("Joel123"); v.onSave()
        assertEquals(0, repo.saveCalls)
    }

    // TC-PRF-016, 017
    @Test fun digitsInMiddleOrLastNameShowErrorOnThatField() {
        val repo = FakeProfileRepository(); val v = vm(repo)
        v.fillValid(); v.onMiddleName("K9"); v.onLastName("Osteen2"); v.onSave()
        assertEquals(setOf(Field.MiddleName, Field.LastName), v.errors.keys)
        assertEquals(0, repo.saveCalls)
    }
}
