package com.qa.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate


class ProfileScreenLayoutTest {
    @get:Rule val rule = createComposeRule()

    private class FakeRepo : ProfileRepository {
        override fun save(profile: Profile) {}
        override fun load(): Profile? = null
    }
    private fun newVm() = ProfileViewModel(FakeRepo()) { LocalDate.of(2026, 10, 13) }

    // TC-PRF-036: layar sempit 320dp x 568dp, semua elemen tetap di dalam layar dan tombol Save terjangkau
    @Test fun narrowScreenKeepsAllElementsInsideAndSaveReachable() {
        val vm = newVm()
        rule.setContent {
            MaterialTheme {
                Box(Modifier.requiredSize(320.dp, 568.dp).testTag("phone")) { ProfileScreen(vm) }
            }
        }
        val phone = rule.onNodeWithTag("phone").getUnclippedBoundsInRoot()

        // Picu semua pesan error (teks error paling panjang) agar ikut diperiksa
        rule.onNodeWithTag(Tags.Save).performScrollTo().performClick()

        val tags = listOf(
            Tags.FirstName, Tags.MiddleName, Tags.LastName, Tags.Nickname,
            Tags.Male, Tags.Female, Tags.BirthDate, Tags.Save,
            Tags.error(Field.FirstName), Tags.error(Field.Nickname), Tags.error(Field.BirthDate),
        )
        for (tag in tags) {
            val b = rule.onNodeWithTag(tag).performScrollTo().getUnclippedBoundsInRoot()
            assertTrue("$tag keluar dari batas kiri layar", b.left >= phone.left)
            assertTrue("$tag keluar dari batas kanan layar", b.right <= phone.right)
        }
        rule.onNodeWithTag(Tags.Save).performScrollTo().assertIsDisplayed()
    }

    // TC-PRF-037: tombol back memanggil aksi kembali tepat satu kali
    @Test fun backButtonTriggersNavigationBack() {
        var backCount = 0
        val vm = newVm()
        rule.setContent { MaterialTheme { ProfileScreen(vm, onBack = { backCount++ }) } }
        rule.onNodeWithTag(Tags.Back).assertIsDisplayed().performClick()
        assertEquals(1, backCount)
    }
}
