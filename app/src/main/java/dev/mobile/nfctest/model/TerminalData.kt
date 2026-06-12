package dev.mobile.nfctest.model

data class TerminalData(
    val merchantId: String,
    val terminalId: String,
    val amount: String
) {
    fun toJson(): String =
        """{"merchantId":"$merchantId","terminalId":"$terminalId","amount":"$amount"}"""
}
