package com.qa.profile

enum class Gender { Male, Female }

/** Data profil yang sudah divalidasi dan siap disimpan. */
data class Profile(
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val nickname: String,
    val gender: Gender,
    val birthDate: String,
)

/** Nama field, dipakai sebagai kunci error. */
enum class Field { FirstName, MiddleName, LastName, Nickname, BirthDate }
