package dev.ccpocket.app.net

import dev.ccpocket.app.pairing.PairedDaemon
import dev.ccpocket.protocol.e2e.E2ECrypto
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.value
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import platform.posix.AF_INET
import platform.posix.IPPROTO_TCP
import platform.posix.SOCK_STREAM
import platform.posix.bind
import platform.posix.close
import platform.posix.getsockname
import platform.posix.listen
import platform.posix.sockaddr
import platform.posix.sockaddr_in
import platform.posix.socket
import platform.posix.socklen_tVar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.TimeSource

/** Exercise the actual Darwin engine, not the JVM CIO engine. Cellular eligibility still needs a device. */
@OptIn(ExperimentalForeignApi::class)
class DirectEstablishBudgetIosTest {
    private val paired = PairedDaemon("wss://127.0.0.1:9", "test", "unused", "device", "credential")

    // The TCP listen queue completes connect, but nobody accepts/answers the HTTP upgrade.
    // This is exactly the phase BEFORE the old inner WebSocket handshake timeout starts.
    private suspend fun withBlackHole(block: suspend (String) -> Unit) {
        val fd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)
        check(fd >= 0)
        try {
            val port = memScoped {
                val addr = alloc<sockaddr_in>()
                addr.sin_len = sizeOf<sockaddr_in>().convert()
                addr.sin_family = AF_INET.convert()
                addr.sin_port = 0u
                addr.sin_addr.s_addr = 0x0100007fu
                check(bind(fd, addr.ptr.reinterpret<sockaddr>(), sizeOf<sockaddr_in>().convert()) == 0)
                check(listen(fd, 8) == 0)
                val len = alloc<socklen_tVar>()
                len.value = sizeOf<sockaddr_in>().convert()
                check(getsockname(fd, addr.ptr.reinterpret<sockaddr>(), len.ptr) == 0)
                val raw = addr.sin_port.toInt()
                ((raw and 0xff) shl 8) or (raw ushr 8)
            }
            block("ws://127.0.0.1:$port")
        } finally { close(fd) }
    }

    @Test fun darwinUpgradeBlackHoleIsBoundedBeforeTheHandshakeStarts() = runBlocking {
        withBlackHole { url ->
            val conn = DirectE2EConnection(establishBudgetMs = 400)
            val keys = E2ECrypto.generateKeyPair()
            val started = TimeSource.Monotonic.markNow()
            val failure = assertFailsWith<DirectUnreachableException> { conn.connect(url, paired, keys) }
            val elapsed = started.elapsedNow().inWholeMilliseconds
            assertEquals(DirectFallbackReason.BUDGET_EXPIRED, failure.reason)
            assertTrue(elapsed in 350..2_500, "Darwin budget elapsed ${elapsed}ms")
            assertEquals(false, conn.connected)
        }
    }

    @Test fun callerCancellationDoesNotBecomeADirectFailureOnDarwin() = runBlocking {
        withBlackHole { url ->
            val conn = DirectE2EConnection(establishBudgetMs = 10_000)
            val keys = E2ECrypto.generateKeyPair()
            var failure: Throwable? = null
            val job = launch { try { conn.connect(url, paired, keys) } catch (t: Throwable) { failure = t; throw t } }
            delay(200)
            val started = TimeSource.Monotonic.markNow()
            job.cancel(); job.join()
            assertTrue(failure is CancellationException)
            assertTrue(started.elapsedNow().inWholeMilliseconds < 2_000)
        }
    }
}
