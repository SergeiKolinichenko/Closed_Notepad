package info.sergeikolinichenko.closednotepad.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasImeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.ImeAction
import android.os.Looper
import kotlinx.coroutines.runBlocking
import org.robolectric.Shadows.shadowOf
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "en-rUS-w411dp-h891dp")
class AppFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val repository
        get() = (compose.activity.application as NotesApp).component.notesRepository()

    private fun waitForText(text: String) {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    // The editor saves a moment after the last change and when it is closed
    private fun waitForSave() {
        compose.mainClock.advanceTimeBy(1_000)
        Thread.sleep(300)
        compose.waitForIdle()
    }

    @Test
    fun createTextNote_isSavedAndShownOnHome() {
        waitForText("Write down your first note")
        compose.onNodeWithContentDescription("Create note").performClick()

        waitForText("Title")
        compose.onNode(hasSetTextAction() and hasText("Title")).performTextInput("Meeting")
        compose.onNode(hasSetTextAction() and hasText("Note")).performTextInput("Prepare the demo")
        waitForSave()
        compose.onNodeWithContentDescription("Back").performClick()
        waitForSave()

        waitForText("Meeting")
        compose.onNodeWithText("Prepare the demo").assertIsDisplayed()
        val notes = runBlocking { repository.getNote(1) }
        assertEquals("Meeting", notes?.title)
        assertEquals("Prepare the demo", notes?.body)
    }

    @Test
    fun typing_isSavedWithoutLeavingTheEditor() {
        waitForText("Write down your first note")
        compose.onNodeWithContentDescription("Create note").performClick()
        waitForText("Title")
        compose.onNode(hasSetTextAction() and hasText("Note")).performTextInput("Draft")

        // Let the save delay pass on the main looper
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(2))
        compose.waitUntil(5_000) { runBlocking { repository.getNote(1)?.body } == "Draft" }
        waitForText("Saved")
    }

    @Test
    fun emptyNote_isNotKept() {
        waitForText("Write down your first note")
        compose.onNodeWithContentDescription("New to-do list").performClick()
        waitForText("Add item")
        compose.onNodeWithContentDescription("Back").performClick()
        waitForSave()
        waitForText("Write down your first note")
    }

    @Test
    fun shoppingList_parsesQuantityAndChecksItem() {
        waitForText("Write down your first note")
        compose.onNodeWithContentDescription("New shopping list").performClick()

        waitForText("Add a product")
        val input = compose.onNode(hasSetTextAction() and hasImeAction(ImeAction.Done))
        input.performTextInput("eggs 10 pcs")
        input.performImeAction()
        waitForText("Eggs")
        compose.onNodeWithText("10 pcs").assertIsDisplayed()
        compose.onNodeWithText("0 of 1 bought").assertIsDisplayed()

        compose.onNodeWithContentDescription("Eggs").performClick()
        waitForText("Everything is bought")
        compose.onNodeWithText("In the cart (1)").assertIsDisplayed()

        compose.onNodeWithContentDescription("In the store").performClick()
        waitForText("Finish shopping")
        compose.onNodeWithText("1 / 1").assertIsDisplayed()
        compose.onNodeWithText("Finish shopping").performClick()
        waitForText("In the cart (1)")
        compose.onNodeWithContentDescription("Back").performClick()

        waitForSave()
        val note = runBlocking { repository.getNote(1) }
        assertEquals(listOf("Eggs"), note?.items?.map { it.text })
        assertEquals(true, note?.items?.single()?.isChecked)
    }

    @Test
    fun movingToTrash_canBeUndone() {
        waitForText("Write down your first note")
        compose.onNodeWithContentDescription("Create note").performClick()
        waitForText("Title")
        compose.onNode(hasSetTextAction() and hasText("Title")).performTextInput("Shopping ideas")
        waitForSave()

        compose.onNodeWithContentDescription("More options").performClick()
        compose.onNodeWithText("Move to trash").performClick()
        waitForText("Note moved to trash")
        compose.onNodeWithText("Undo").performClick()
        waitForText("Shopping ideas")
        compose.onAllNodesWithText("Shopping ideas").onFirst().assertIsDisplayed()
    }
}
