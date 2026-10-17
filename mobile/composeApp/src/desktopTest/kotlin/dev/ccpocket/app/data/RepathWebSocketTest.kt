package dev.ccpocket.app.data

import dev.ccpocket.app.net.FakeRelayServer
import dev.ccpocket.app.net.RepathController
import dev.ccpocket.app.pairing.PairedDaemon
import dev.ccpocket.app.pairing.Pairing
import dev.ccpocket.app.push.PushPlatform
import dev.ccpocket.app.push.PushAuthorization
import dev.ccpocket.app.push.PushFailureEvent
import dev.ccpocket.app.push.PushRegistrar
import dev.ccpocket.app.push.PushStateStore
import dev.ccpocket.app.push.PushToken
import dev.ccpocket.app.util.B64Url
import dev.ccpocket.protocol.OpenSession
import dev.ccpocket.protocol.e2e.E2ECrypto
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/** Real TCP, WebSocket, Noise and encrypted application frames; no dial/probe/receive test seams. */
class RepathWebSocketTest {
    private suspend fun eventually(check: () -> Boolean) = withTimeout(10_000) {
        while (!check()) delay(20)
    }

    private fun scenario(confirmDirect: Boolean = true, acknowledgePush: Boolean = true,
        test: suspend (PocketRepository, FakeRelayServer, FakeRelayServer, MutableStateFlow<PushToken?>) -> Unit) {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher().use { dispatcher ->
            runBlocking(dispatcher) {
                val keys = E2ECrypto.generateKeyPair()
                val device = Pairing.deviceKeys().publicRaw
                val tokens = MutableStateFlow<PushToken?>(null)
                val pushPlatform = object : PushPlatform {
                    override val token = tokens
                    override val failures = MutableSharedFlow<PushFailureEvent>()
                    override fun requestToken(prompt: Boolean, request: Long) {}
                    override fun readAuthorization(cb: (PushAuthorization) -> Unit) = cb(PushAuthorization.AUTHORIZED)
                }
                FakeRelayServer(keys, device, acknowledgePush = acknowledgePush).use { relay ->
                    FakeRelayServer(keys, device, confirmHandshake = confirmDirect).use { direct ->
                        val scope = CoroutineScope(SupervisorJob() + dispatcher)
                        val store = object : PushStateStore {
                            private val data = mutableMapOf<String, String>()
                            override fun get(key: String) = data[key]
                            override fun put(key: String, value: String) { data[key] = value }
                            override fun remove(key: String) { data.remove(key) }
                        }
                        val repo = PocketRepository(scope).apply {
                            paired.value = PairedDaemon(relay.url, "acct", B64Url.encode(keys.publicRaw), "dev", "cred")
                            sameMachineClient = true
                            repathEnabledOverride = true
                            notificationsOn.value = true
                            registrarOverride = PushRegistrar(scope, pushPlatform, store,
                                foreground = MutableStateFlow(true), jitter = { 0.0 })
                        }
                        try {
                            repo.startRelay()
                            eventually { repo.phase.value == ConnPhase.Ready }
                            assertTrue(repo.openSession("/test-work", "test-session"))
                            eventually { repo.convoId.value == "test-convo" && repo.repathIdle() }
                            // Give the initial history frame its turn after SessionLive so the cursor is real.
                            eventually { !repo.opening.value && !repo.switchingSession.value && repo.messages.isNotEmpty() }
                            test(repo, relay, direct, tokens)
                        } finally {
                            repo.disconnect()
                            scope.cancel()
                        }
                    }
                }
            }
        }
    }

    @Test fun idleRelaySwitchesToDirectAndReattachesWithTheHistoryCursor() = scenario { repo, relay, direct, _ ->
        repo.paired.value = repo.paired.value!!.copy(directUrl = direct.url)
        repo.onAppForeground()
        eventually { direct.received.filterIsInstance<OpenSession>().isNotEmpty() }
        eventually { repo.phase.value == ConnPhase.Ready && repo.repathIdle() && relay.activeConnections.get() == 0 }
        val reattach = direct.received.filterIsInstance<OpenSession>().single()
        assertEquals("test-session", reattach.resumeId)
        assertEquals(42L, reattach.lastEventSeq)
        assertEquals(1, relay.connections.get(), "the optimization must never open a second relay")
        assertEquals(1, direct.handshakes.get())
        assertEquals("test-convo", repo.convoId.value)
    }

    @Test fun reachableTcpWithoutKeyConfirmationFallsBackAndRestoresTheSession() = scenario(false) { repo, relay, direct, _ ->
        repo.paired.value = repo.paired.value!!.copy(directUrl = direct.url)
        repo.onAppForeground()
        eventually { relay.received.filterIsInstance<OpenSession>().size == 2 }
        eventually { repo.phase.value == ConnPhase.Ready && repo.repathIdle() }
        assertEquals(1, direct.handshakes.get())
        assertEquals(2, relay.connections.get())
        assertEquals(1, relay.maxActiveConnections.get(), "retire the old relay before falling back")
        assertEquals(42L, relay.received.filterIsInstance<OpenSession>().last().lastEventSeq)
        // A new foreground trigger inside the failure cooldown cannot try the broken address again.
        repo.onAppForeground()
        delay(RepathController.FOREGROUND_DELAY_MS + 300)
        assertEquals(1, direct.handshakes.get())
        assertEquals(2, relay.connections.get())
    }

    @Test fun busyConversationKeepsTheRelayUntilItBecomesIdle() = scenario { repo, relay, direct, _ ->
        repo.paired.value = repo.paired.value!!.copy(directUrl = direct.url)
        repo.streaming.value = true
        repo.onAppForeground()
        delay(RepathController.FOREGROUND_DELAY_MS + 300)
        assertEquals(0, direct.handshakes.get(), "a TCP probe must not become a live E2E connection during output")
        assertEquals(1, relay.activeConnections.get())
        repo.streaming.value = false
        repo.onAppForeground()
        eventually { direct.received.filterIsInstance<OpenSession>().isNotEmpty() }
        assertEquals(1, relay.connections.get())
    }

    @Test fun failedTcpProbeLeavesTheHealthyRelayUntouched() = scenario { repo, relay, direct, _ ->
        val port = ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { it.localPort }
        repo.paired.value = repo.paired.value!!.copy(directUrl = "ws://127.0.0.1:$port")
        repo.onAppForeground()
        delay(RepathController.FOREGROUND_DELAY_MS + RepathController.PROBE_TIMEOUT_MS + 300)
        assertEquals(1, relay.connections.get())
        assertEquals(1, relay.activeConnections.get())
        assertEquals(0, direct.handshakes.get())
        assertEquals(ConnPhase.Ready, repo.phase.value)
    }

    @Test fun tokenArrivingDuringFailedDirectAttemptUsesOnlyTheFallbackRelaySocket() =
        scenario(confirmDirect = false, acknowledgePush = false) { repo, relay, direct, tokens ->
            repo.paired.value = repo.paired.value!!.copy(directUrl = direct.url)
            repo.onAppForeground()
            eventually { direct.handshakes.get() == 1 }
            // A-3: an unacknowledged control-only deposit would survive the 3s direct timeout and
            // overlap the fallback relay. Do not replace either dial with a test seam.
            tokens.value = PushToken("fcm", "during-direct")
            eventually { relay.received.filterIsInstance<OpenSession>().size == 2 }
            eventually { relay.registrations.isNotEmpty() }
            assertEquals(2, relay.connections.get(), "initial + fallback only; no helper socket during the attempt")
            assertEquals(1, relay.maxActiveConnections.get())
            assertEquals(listOf("during-direct"), relay.registrations.map { it.token })
            assertEquals(ConnPhase.Ready, repo.phase.value)
        }
}
