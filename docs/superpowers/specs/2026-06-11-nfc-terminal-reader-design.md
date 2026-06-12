# NFC Terminal Reader — Design Spec
**Date:** 2026-06-11
**Status:** Approved

## Overview

Android terminal app that reads EMV card data (PAN, expiry, cardholder name) from NFC-enabled phones (Google Pay / HCE). Data is displayed on screen only — no server or local storage.

## Architecture

```
NFCTest/
├── MainActivity              ← NFC intent qabul qiladi, foreground dispatch
├── nfc/
│   ├── NfcManager.kt         ← NFC adapter lifecycle, foreground dispatch on/off
│   └── EmvReader.kt          ← IsoDep APDU commands + TLV response parsing
├── model/
│   └── CardData.kt           ← Data class: pan, expiry, cardholderName (nullable)
└── ui/
    ├── CardScreen.kt         ← Asosiy ekran, uch holat: Waiting / Result / Error
    └── CardResultCard.kt     ← O'qilgan karta UI komponenti
```

### Data Flow

```
Telefon tekkiziladi
    → NfcAdapter tag aniqlaydi (ACTION_TAG_DISCOVERED)
    → IsoDep ulanadi
    → EmvReader: SELECT PPSE → AID ro'yxati
    → EmvReader: SELECT AID → GET PROCESSING OPTIONS
    → EmvReader: READ RECORD → TLV parse
    → CardData (pan, expiry, name)
    → CardScreen: Result holatiga o'tadi
```

### Dependency

- `io.github.devnied:emv-nfc-reader` — APDU + TLV parsing kutubxonasi

## UI

Bitta ekran, uch holat:

| Holat | Ko'rinish |
|-------|-----------|
| Waiting | "Telefonni yaqinlashtiring" + NFC ikonkasi |
| Result | Karta kartochkasi: `**** **** **** 4242`, `12/27`, `JOHN DOE` |
| Error | Xato xabari + "Qayta urinish" tugmasi |

PAN oxirgi 4 raqamdan tashqari yashiriladi: `**** **** **** XXXX`

## Error Handling

| Holat | Sabab | UI xabari |
|-------|-------|-----------|
| NFC o'chiq | NFC adapter null | "NFC ni yoqing" + sozlamalarga o'tish |
| Tag ulanmadi | IsoDep null | "Qayta tekkizing" |
| AID topilmadi | HCE app yo'q | "Karta ma'lumoti topilmadi" |
| APDU xato | Noto'g'ri javob | "O'qib bo'lmadi, qayta urinib ko'ring" |

## Permissions (AndroidManifest)

```xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" android:required="true" />
```

## Testing Strategy

- **Unit:** `EmvReader` TLV parsing — mock APDU byte array bilan
- **Integration:** Real qurilmada Google Pay yoqilgan telefon bilan manual test
- Emulator NFC ni qo'llab-quvvatlamaydi — real device majburiy

## Constraints

- Google Pay va boshqa payment applar haqiqiy PAN emas, tokenized DPAN beradi — bu kutilgan xatti-harakat
- Minimal UI — hozircha soddalik maqsad
