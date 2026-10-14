# QA Test - Automation Playwright (Bagian 2 dan 3)

Bagian 1 (aplikasi mobile "Input your profile") ada di proyek terpisah: `qa-android` (Kotlin + Compose).
Proyek ini berisi automation website (Bagian 2) dan alur POS (Bagian 3).

## Struktur
```
mock/pos.html                        mock POS sesuai flow chart (target Bagian 3)
pages/PosPage.ts                     Page Object POS
tests/02-borobudur-website.spec.ts   Bagian 2 (TC-WEB-xxx, situs live)
tests/03-pos-flow.spec.ts            Bagian 3 (TC-POS-xxx)
```

## Catatan
- Tes `@bug-candidate` (TC-WEB-019, 020, 026) diharapkan GAGAL selama BUG-001..003 belum diperbaiki.
- Selector situs live disusun dari HTML yang dibaca; kalibrasi pada run pertama.
