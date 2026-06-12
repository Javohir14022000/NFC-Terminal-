package dev.mobile.nfctest

import dev.mobile.nfctest.model.CardData
import org.junit.Assert.assertEquals
import org.junit.Test

class CardDataTest {

    @Test
    fun `maskedPan shows only last 4 digits`() {
        val card = CardData(pan = "4111111111111111", expiry = "12/27")
        assertEquals("**** **** **** 1111", card.maskedPan)
    }

    @Test
    fun `maskedPan works for any 16 digit pan`() {
        val card = CardData(pan = "5500005555555559", expiry = "01/28")
        assertEquals("**** **** **** 5559", card.maskedPan)
    }

    @Test
    fun `cardholderName is null by default`() {
        val card = CardData(pan = "4111111111111111", expiry = "12/27")
        assertEquals(null, card.cardholderName)
    }

    @Test
    fun `cardholderName is stored correctly`() {
        val card = CardData(pan = "4111111111111111", expiry = "12/27", cardholderName = "JOHN DOE")
        assertEquals("JOHN DOE", card.cardholderName)
    }

    @Test
    fun `expiry is stored as given`() {
        val card = CardData(pan = "4111111111111111", expiry = "06/30")
        assertEquals("06/30", card.expiry)
    }
}
