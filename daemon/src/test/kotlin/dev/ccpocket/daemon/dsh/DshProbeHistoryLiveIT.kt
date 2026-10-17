package dev.ccpocket.daemon.dsh

import dev.ccpocket.protocol.ChatRole
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Replay the exact persisted session from scripts/probe-dsh-acp.py against a real model.
 * Point CC_POCKET_DSH_PROBE_FINDINGS at its findings.json; no fixture or other user session is read.
 * Run after the probe exits so the CLI has flushed/compressed its transcript.
 */
@EnabledIfEnvironmentVariable(named = "CC_POCKET_DSH_PROBE_FINDINGS", matches = ".+")
class DshProbeHistoryLiveIT {
    @Test
    fun a_real_probe_preserves_messages_and_completed_tool_results_in_history() {
        val findings = Json.parseToJsonElement(
            Path.of(System.getenv("CC_POCKET_DSH_PROBE_FINDINGS")).readText(),
        ).jsonObject
        val file = Path.of(findings.getValue("transcript_path").jsonPrimitive.content)
        val sid = findings.getValue("session/new").jsonObject.getValue("sessionId").jsonPrimitive.content
        val header = assertNotNull(DshTranscript.header(file))
        assertEquals(sid, header.id, "probe must select its own session transcript")
        val cwd = assertNotNull(header.cwd)
        val root = file.parent.parent.parent
        assertTrue(DshTranscriptScanner.scan(cwd, root).any { it.sessionId == sid })

        val history = DshTranscriptReplay.slice(file, null, maxMessages = 1000)
        assertEquals("complete", history.quality, history.readError)
        assertEquals(0L, history.failedRows)
        assertTrue(history.messages.any { it.role == ChatRole.USER && it.text.contains("hello-from-probe") })
        assertTrue(history.messages.any { it.role == ChatRole.ASSISTANT && it.text.contains("hello-from-probe") })
        val tools = history.messages.filter { it.role == ChatRole.TOOL }
        assertTrue(tools.any { it.ok == true && it.output?.contains("hello-from-probe") == true },
            "the real shell result was lost")
        assertTrue(tools.any { it.ok == true && it.output?.contains("probe.txt") == true },
            "the real file-write result was lost")
    }
}
