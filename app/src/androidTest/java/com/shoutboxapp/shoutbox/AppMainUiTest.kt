package com.shoutboxapp.shoutbox
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import androidx.test.uiautomator.UiDevice
import androidx.test.platform.app.InstrumentationRegistry

class AppMainUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>() // Ensure your MainActivity is tested.

    @Test
    fun testSendMessageFlow() {
        // Grant permission if the dialog appears
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            .findObject(androidx.test.uiautomator.By.text("While using the app"))
            ?.click()
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            .findObject(androidx.test.uiautomator.By.text("Allow"))
            ?.click()

        // Test entering name on the first screen
        composeTestRule.onNodeWithTag("NameInputField") // Assume you added test tags
            .performTextInput("John Doe")

        // Simulate click on 'Next' button to go to second screen
       composeTestRule.onNodeWithTag("ShoutButton")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            // returns an empty list and does wait for timeout unlike
            // onNodeWithTag which will fail immediately
            composeTestRule.onAllNodesWithTag("ChatScreen").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("ChatMessage")
            .performTextInput("Hello")
        composeTestRule.onNodeWithTag("SendChatButton").performClick()

        Thread.sleep(5000)
        var displayed_mess = composeTestRule.onNodeWithText("Message").isDisplayed()

        // TODO: need to figure out way to assert New Message
        Thread.sleep(5000)

    }
}
