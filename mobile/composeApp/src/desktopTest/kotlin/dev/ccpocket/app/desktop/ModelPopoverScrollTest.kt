package dev.ccpocket.app.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.ccpocket.app.resources.Res
import dev.ccpocket.app.resources.model_custom_label
import dev.ccpocket.app.str
import dev.ccpocket.app.theme.PocketTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ModelPopoverScrollTest {
    @Test
    fun long_gateway_catalog_can_reach_custom_input_and_select_the_last_model_in_a_short_host() =
        runDesktopComposeUiTest(420, 600) {
            var picked: String? = null
            var dismissed = false
            val model = object : SeedDesktopModel() {
                override val gatewayBaseUrl = "https://gateway.example/v1"
                override val gatewayModels = (1..60).map { "vendor-model-$it" }
                override fun switchModel(name: String) { picked = name }
            }
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                    PocketTheme {
                        Box(Modifier.height(320.dp)) { ModelPopover(model) { dismissed = true } }
                    }
                }
            }
            onNodeWithText(str(Res.string.model_custom_label).uppercase()).performScrollTo().assertIsDisplayed()
            onNodeWithText("vendor-model-60").performScrollTo().assertIsDisplayed().performClick()
            assertEquals("vendor-model-60", picked)
            assertTrue(dismissed)
        }
}
