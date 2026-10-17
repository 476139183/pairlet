package dev.ccpocket.app.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.ccpocket.app.theme.PocketTheme
import java.awt.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class WindowChromeDragTest {
    @Test fun aSingleMovementMovesTheWindowFromThePressAnchorAtBothDensities() {
        for (density in listOf(1f, 2f)) runDesktopComposeUiTest(600, 240) {
            var mouse = Point(150, 130)
            var window = Point(100, 100)
            var zooms = 0
            val modifier = windowDragAndZoom({ mouse }, { window }, { window = it }, { zooms++ })
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(density)) {
                    Box(Modifier.size(250.dp, 80.dp).testTag("handle").then(modifier))
                }
            }
            val handle = onNodeWithTag("handle")
            handle.performMouseInput { moveTo(Offset(50f, 30f)); press() }
            waitForIdle()
            mouse = Point(154, 132)
            handle.performMouseInput { moveBy(Offset(4f * density, 2f * density)) }
            waitForIdle()
            handle.performMouseInput { release() }
            waitForIdle()
            assertEquals(Point(104, 102), window, "small first movement must count, in AWT points at density $density")
            assertEquals(0, zooms)
        }
    }

    @Test fun doubleClickStillZoomsWithoutMovingTheWindow() = runDesktopComposeUiTest(400, 160) {
        var moves = 0
        var zooms = 0
        val modifier = windowDragAndZoom({ Point(150, 130) }, { Point(100, 100) }, { moves++ }, { zooms++ })
        setContent { Box(Modifier.size(300.dp, 80.dp).testTag("handle").then(modifier)) }
        onNodeWithTag("handle").performMouseInput { doubleClick(center) }
        waitForIdle()
        assertEquals(1, zooms)
        assertEquals(0, moves)
    }

    @Test fun windowsChatHasAFullHeightHandleAndItsCloseButtonRemainsClickable() = runDesktopComposeUiTest(1180, 798) {
        var closed = false
        val chrome = DesktopWindowChrome(onClose = { closed = true }, dragAndZoomModifier = Modifier.testTag("window-drag"))
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f), LocalWindowChrome provides chrome) {
                PocketTheme { DesktopApp(SeedDesktopModel()) }
            }
        }
        val handle = onNodeWithTag("window-drag").fetchSemanticsNode()
        assertTrue(handle.size.height >= 38, "the drag hit area must not shrink to the 15sp title line")
        val root = onRoot().fetchSemanticsNode()
        // Windows close cell is the trailing 30dp control, inside the header's 18dp outer inset.
        onRoot().performMouseInput { click(Offset(root.size.width - 33f, handle.positionInRoot.y + handle.size.height / 2f)) }
        waitForIdle()
        assertTrue(closed, "the drag handle must not swallow the adjacent close button")
    }
}
