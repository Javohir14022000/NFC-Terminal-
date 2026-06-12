# NFC Terminal Sender — Design Spec
**Date:** 2026-06-11
**Status:** Approved
**Project:** NFCTest (Terminal tomon)

## Overview

Terminal Android ilovasi. Foydalanuvchi `merchantId`, `terminalId`, `amount` kiritadi. NFC yoqilgan telefon tekkizilganda bu ma'lumotlar HCE protokoli orqali telefonning `NFCmobile` ilovasiga yuboriladi.

## Protocol

Custom APDU over HCE (ISO 14443-4):
- **AID:** `F0010203040506`
- **SELECT AID** → telefon `9000 OK` javob beradi
- **PUT DATA** (JSON payload) → telefon `9000 OK` javob beradi

JSON format:
```json
{"merchantId":"M001","terminalId":"T001","amount":"50000"}
```

## Architecture

```
NFCTest/
├── model/
│   └── TerminalData.kt       ← data class: merchantId, terminalId, amount
├── nfc/
│   └── NfcSender.kt          ← IsoDep connect, SELECT AID, PUT DATA APDU
└── ui/
    ├── TerminalScreen.kt     ← Asosiy ekran: forma + holat
    └── StatusView.kt         ← Waiting / Sending / Success / Error holatlari
```

## Data Flow

```
[Forma to'ldiriladi]
    → [Telefon tekkiziladi] → onNewIntent
    → NfcSender.IsoDep.connect()
    → SELECT AID (F0010203040506)
    → PUT DATA (JSON bytes)
    → 9000 OK → UiState.Success
```

## UI

Bitta ekran, ikki bo'lim:
1. **Forma**: merchantId, terminalId, amount (TextField)
2. **Holat**:
   - `Waiting` — "Telefonni yaqinlashtiring 📶"
   - `Sending` — CircularProgressIndicator
   - `Success` — "Muvaffaqiyatli yuborildi"
   - `Error(message)` — xato matni + "Qayta" tugma

## Error Handling

| Holat | Sabab | UI |
|-------|-------|-----|
| NFC o'chiq | adapter null/disabled | "NFC yoqing" |
| HCE yo'q | SELECT AID FAILED | "Qurilma qabul qilmadi" |
| Ulanish uzildi | IOException | "Qayta tekkizing" |
| Bo'sh maydon | validation | maydon chegarasi qizaradi |

## Permissions

```xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" android:required="true" />
```
