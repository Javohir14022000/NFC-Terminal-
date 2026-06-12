# NFC Terminal Reader — Implementation Plan
**Date:** 2026-06-11
**Spec:** `2026-06-11-nfc-terminal-reader-design.md`

## Phase 1: Dependencies & Permissions

### Task 1.1 — `libs.versions.toml` ga kutubxona qo'shish
```toml
[versions]
emvNfcReader = "3.0.1"

[libraries]
emv-nfc-reader = { group = "io.github.devnied", name = "emv-nfc-reader", version.ref = "emvNfcReader" }
```

### Task 1.2 — `app/build.gradle.kts` ga dependency qo'shish
```kotlin
implementation(libs.emv.nfc.reader)
```

### Task 1.3 — `AndroidManifest.xml` ga NFC permission qo'shish
```xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" android:required="true" />
```
MainActivity intent-filter va meta-data qo'shish (foreground dispatch uchun).

### Task 1.4 — `res/xml/nfc_tech_filter.xml` yaratish
IsoDep tech filter XML fayli.

---

## Phase 2: Model

### Task 2.1 — `model/CardData.kt` yaratish
```kotlin
data class CardData(
    val pan: String,
    val expiry: String,
    val cardholderName: String? = null
) {
    val maskedPan: String get() = "**** **** **** ${pan.takeLast(4)}"
}
```

---

## Phase 3: NFC Logic

### Task 3.1 — `nfc/EmvReader.kt` yaratish
`IProvider` implement qiladi, `IsoDep` orqali APDU yuboradi, `EmvParser` bilan karta ma'lumotlari olinadi.

### Task 3.2 — `nfc/NfcManager.kt` yaratish
`NfcAdapter` lifecycle boshqaradi, foreground dispatch yoqadi/o'chiradi.

---

## Phase 4: UI

### Task 4.1 — `ui/CardScreen.kt` yaratish
`CardUiState` sealed class: `Waiting`, `Result(card)`, `Error(message)`.

### Task 4.2 — `ui/CardResultCard.kt` yaratish
Material3 Card: maskedPan, expiry, cardholderName.

---

## Phase 5: MainActivity ulash

### Task 5.1 — `MainActivity.kt` yangilash
- `NfcManager` init, onResume/onPause lifecycle
- `onNewIntent` da IsoDep tag olish
- `EmvReader` IO coroutine da ishga tushirish
- `CardUiState` yangilab `CardScreen` ga uzatish

---

## Phase 6: Tests

### Task 6.1 — `EmvReaderTest.kt` unit test
Mock APDU byte array bilan CardData parsing tekshirish.

---

## Build Order

```
1.1 → 1.2 → 1.3+1.4 (parallel)
    ↓
2.1
    ↓
3.1 + 3.2 (parallel)
    ↓
4.1 + 4.2 (parallel)
    ↓
5.1
    ↓
6.1
```
