package dev.mobile.nfctest.nfc

import android.nfc.tech.IsoDep
import dev.mobile.nfctest.model.TerminalData

private val AID = byteArrayOf(0xF0.toByte(), 0x01, 0x02, 0x03, 0x04, 0x05, 0x06)

class NfcSender(private val isoDep: IsoDep) {

    fun send(data: TerminalData) {
        isoDep.connect()
        isoDep.timeout = 5000
        try {
            selectAid()
            putData(data.toJson())
        } finally {
            if (isoDep.isConnected) isoDep.close()
        }
    }

    private fun selectAid() {
        val apdu = buildSelectApdu(AID)
        val response = isoDep.transceive(apdu)
        if (!response.endsWith9000()) throw IllegalStateException("AID tanlanmadi")
    }

    private fun putData(json: String) {
        val payload = json.toByteArray(Charsets.UTF_8)
        val apdu = buildPutDataApdu(payload)
        val response = isoDep.transceive(apdu)
        if (!response.endsWith9000()) throw IllegalStateException("Ma'lumot yuborilmadi")
    }

    private fun buildSelectApdu(aid: ByteArray): ByteArray =
        byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, aid.size.toByte()) + aid

    private fun buildPutDataApdu(data: ByteArray): ByteArray =
        byteArrayOf(0x00, 0xDA.toByte(), 0x00, 0x00, data.size.toByte()) + data

    private fun ByteArray.endsWith9000(): Boolean =
        size >= 2 && this[size - 2] == 0x90.toByte() && this[size - 1] == 0x00.toByte()
}
