# Input Profile - replika aplikasi Android + tes

Replika native layar **"Input your profile"** dari soal QA Test (Kotlin + Jetpack Compose),
beserta unit test dan UI test. Dipakai sebagai target automation Bagian 1.

## Struktur
```
app/src/main/java/com/qa/profile/
  Profile.kt             model data (Profile, Gender, Field)
  ProfileValidator.kt    aturan validasi (ASUMSI, lihat bawah)
  ProfileRepository.kt   penyimpanan (SharedPreferences)
  ProfileViewModel.kt    state form + logika Save
  ProfileScreen.kt       UI Compose (testTag di object Tags)
  MainActivity.kt
app/src/test/            unit test JVM   (ProfileValidatorTest, ProfileViewModelTest)
app/src/androidTest/     UI test Compose (ProfileScreenTest) - butuh emulator/perangkat
```

## Aturan yang diasumsikan (soal hanya memberi gambar)
First Name, Nickname, Birth Date wajib; Middle/Last Name opsional; nama hanya huruf, spasi,
`'` `.` `-` (maks. 50), nickname maks. 20; Birth Date format `DD MMM YYYY`, tidak boleh masa depan,
usia minimal 5 tahun; Gender default Male; tap Save berulang tanpa perubahan tidak menyimpan ulang.
Ubah di `ProfileValidator.kt` bila spesifikasi berbeda.

## Catatan
- Birth Date berupa kolom ketik (ikon kalender hanya hiasan), bukan date picker. Perilaku date picker
  (TC-PRF-039) dicatat sebagai tes manual.
- Tangkapan layar soal tidak memberi aturan error/warna error; tampilan error adalah rancangan sendiri.
