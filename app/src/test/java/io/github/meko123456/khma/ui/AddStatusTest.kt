package io.github.meko123456.khma.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The line under the app bar that says how adding a podcast went. TalkBack's speech can't be
 * observed from a test, so this checks what TalkBack acts on: the line is a polite live region,
 * and each step is a change of the same node's words, not a node appearing.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class AddStatusTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun eachStepOfAnAddIsANewWordingOfOnePoliteLiveRegion() {
        var status by mutableStateOf<String?>(null)
        rule.setContent { AddStatus(status, onClear = { status = null }) }
        val line = rule.onNodeWithTag(ADD_STATUS_TAG, useUnmergedTree = true)
        line.assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        line.assertHeightIsEqualTo(0.dp)
        val node = line.fetchSemanticsNode().id

        status = "Adding…"
        line.assertTextEquals("Adding…")
        assertEquals(node, line.fetchSemanticsNode().id)

        status = "Couldn't reach that address. Check the link and your connection."
        line.assertTextEquals("Couldn't reach that address. Check the link and your connection.")
        assertEquals(node, line.fetchSemanticsNode().id)

        status = "Adding…"
        status = "Added"
        line.assertTextEquals("Added")
        assertEquals(node, line.fetchSemanticsNode().id)

        // Tapping it clears it, back to no words and no height, still the same node.
        line.performClick()
        line.assertTextEquals("")
        line.assertHeightIsEqualTo(0.dp)
        assertEquals(node, line.fetchSemanticsNode().id)
        line.assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }
}
