package com.qa.profile

/** Repository palsu untuk tes: menghitung berapa kali save dipanggil. */
class FakeProfileRepository(private var stored: Profile? = null) : ProfileRepository {
    var saveCalls = 0
        private set
    val saved: Profile? get() = stored

    override fun save(profile: Profile) { saveCalls++; stored = profile }
    override fun load(): Profile? = stored
}
