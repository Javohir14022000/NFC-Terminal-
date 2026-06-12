package dev.mobile.nfctest

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import dev.mobile.nfctest.model.TerminalData
import dev.mobile.nfctest.nfc.NfcManager
import dev.mobile.nfctest.nfc.NfcSender
import dev.mobile.nfctest.ui.TerminalScreen
import dev.mobile.nfctest.ui.TerminalUiState
import dev.mobile.nfctest.ui.theme.NFCTestTheme
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var nfcManager: NfcManager

    private var merchantId by mutableStateOf("M001")
    private var terminalId by mutableStateOf("T001")
    private var amount by mutableStateOf("")
    private var uiState by mutableStateOf<TerminalUiState>(TerminalUiState.Waiting)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcManager = NfcManager(this)

        if (!nfcManager.isSupported()) {
            uiState = TerminalUiState.Error("Bu qurilma NFC ni qo'llab-quvvatlamaydi")
        } else if (!nfcManager.isEnabled()) {
            uiState = TerminalUiState.Error("NFC o'chiq. Sozlamalardan yoqing")
        }

        enableEdgeToEdge()
        setContent {
            NFCTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TerminalScreen(
                        merchantId = merchantId,
                        terminalId = terminalId,
                        amount = amount,
                        state = uiState,
                        onMerchantIdChange = { merchantId = it },
                        onTerminalIdChange = { terminalId = it },
                        onAmountChange = { amount = it },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (nfcManager.isSupported() && nfcManager.isEnabled()) {
            nfcManager.enable()
        }
    }

    override fun onPause() {
        super.onPause()
        nfcManager.disable()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val action = intent.action
        val isNfcIntent = action == NfcAdapter.ACTION_TECH_DISCOVERED ||
                action == NfcAdapter.ACTION_TAG_DISCOVERED ||
                action == NfcAdapter.ACTION_NDEF_DISCOVERED
        if (!isNfcIntent) return

        if (merchantId.isBlank() || terminalId.isBlank() || amount.isBlank()) {
            uiState = TerminalUiState.Error("Barcha maydonlarni to'ldiring")
            return
        }

        @Suppress("DEPRECATION")
        val tag = intent.getParcelableExtra<android.nfc.Tag>(NfcAdapter.EXTRA_TAG) ?: return
        val isoDep = IsoDep.get(tag) ?: run {
            uiState = TerminalUiState.Error("Qurilma qabul qilmadi")
            return
        }

        val data = TerminalData(merchantId, terminalId, amount)
        uiState = TerminalUiState.Sending

        lifecycleScope.launch {
            uiState = withContext(Dispatchers.IO) {
                try {
                    NfcSender(isoDep).send(data)
                    TerminalUiState.Success
                } catch (e: IllegalStateException) {
                    TerminalUiState.Error(e.message ?: "Yuborib bo'lmadi")
                } catch (e: Exception) {
                    TerminalUiState.Error("Qayta tekkizing")
                }
            }
        }
    }
}
