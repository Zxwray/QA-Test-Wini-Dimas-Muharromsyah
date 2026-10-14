package com.qa.profile

import android.content.Context

interface ProfileRepository {
    fun save(profile: Profile)
    fun load(): Profile?
}

/** Penyimpanan lokal sederhana (SharedPreferences) agar data tampil kembali saat layar dibuka ulang. */
class SharedPrefsProfileRepository(context: Context) : ProfileRepository {
    private val prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE)

    override fun save(profile: Profile) {
        prefs.edit()
            .putString("firstName", profile.firstName)
            .putString("middleName", profile.middleName)
            .putString("lastName", profile.lastName)
            .putString("nickname", profile.nickname)
            .putString("gender", profile.gender.name)
            .putString("birthDate", profile.birthDate)
            .apply()
    }

    override fun load(): Profile? {
        val first = prefs.getString("firstName", null) ?: return null
        return Profile(
            firstName = first,
            middleName = prefs.getString("middleName", "") ?: "",
            lastName = prefs.getString("lastName", "") ?: "",
            nickname = prefs.getString("nickname", "") ?: "",
            gender = runCatching { Gender.valueOf(prefs.getString("gender", "Male")!!) }.getOrDefault(Gender.Male),
            birthDate = prefs.getString("birthDate", "") ?: "",
        )
    }
}
