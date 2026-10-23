package it.maestro.protocol

import kotlin.test.Test
import kotlin.test.assertEquals

class CoreProtocolTest {
    @Test
    fun protocolVersionStartsAtZero() {
        assertEquals(0, CoreProtocol.PROTOCOL_VERSION)
    }
}
