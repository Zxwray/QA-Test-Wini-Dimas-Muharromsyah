package com.qa.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.LocalDate

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    var firstName by mutableStateOf("");  private set
    var middleName by mutableStateOf(""); private set
    var lastName by mutableStateOf("");   private set
    var nickname by mutableStateOf("");   private set
    var gender by mutableStateOf(Gender.Male); private set
    var birthDate by mutableStateOf("");  private set

    /** Pesan error per field; field valid tidak ada di map. */
    var errors by mutableStateOf<Map<Field, String>>(emptyMap()); private set

    /** Pesan sukses setelah Save; null bila belum/ setelah ada perubahan. */
    var message by mutableStateOf<String?>(null); private set

    /** Berapa kali data benar-benar dikirim ke repository (dipakai tes anti double-submit). */
    var saveCount by mutableStateOf(0); private set

    private var lastSaved: Profile? = null

    init {
        repository.load()?.let {
            firstName = it.firstName; middleName = it.middleName; lastName = it.lastName
            nickname = it.nickname; gender = it.gender; birthDate = it.birthDate
            lastSaved = it
        }
    }

    fun onFirstName(v: String) { firstName = v; message = null }
    fun onMiddleName(v: String) { middleName = v; message = null }
    fun onLastName(v: String) { lastName = v; message = null }
    fun onNickname(v: String) { nickname = v; message = null }
    fun onGender(v: Gender) { gender = v; message = null }
    fun onBirthDate(v: String) { birthDate = v; message = null }

    fun onSave() {
        val found = buildMap<Field, String> {
            ProfileValidator.validateName("First Name", firstName, true, ProfileValidator.MAX_NAME)?.let { put(Field.FirstName, it) }
            ProfileValidator.validateName("Middle Name", middleName, false, ProfileValidator.MAX_NAME)?.let { put(Field.MiddleName, it) }
            ProfileValidator.validateName("Last Name", lastName, false, ProfileValidator.MAX_NAME)?.let { put(Field.LastName, it) }
            ProfileValidator.validateName("Nickname", nickname, true, ProfileValidator.MAX_NICKNAME)?.let { put(Field.Nickname, it) }
            ProfileValidator.validateBirthDate(birthDate, today())?.let { put(Field.BirthDate, it) }
        }
        errors = found
        message = null
        if (found.isNotEmpty()) return

        val profile = Profile(firstName.trim(), middleName.trim(), lastName.trim(), nickname.trim(), gender, birthDate.trim())
        // Idempoten: tap Save berulang tanpa perubahan data tidak mengirim ulang (cegah double-submit)
        if (profile != lastSaved) {
            repository.save(profile)
            lastSaved = profile
            saveCount++
        }
        message = "Profile saved successfully"
    }
}
