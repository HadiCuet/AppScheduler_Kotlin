package com.example.appscheduler_kotlin.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.appscheduler_kotlin.ui.screens.AppSelectionCard
import com.example.appscheduler_kotlin.util.TimePresets
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditScheduleScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun saveButtonIsDisabledWhenNoAppSelected() {
        composeTestRule.setContent {
            BottomActionBar(
                onSave = { },
                onCancel = { },
                saveEnabled = false
            )
        }

        composeTestRule
            .onNodeWithText("Save")
            .assertIsNotEnabled()
    }

    @Test
    fun saveButtonIsEnabledWhenAppAndTimeSelected() {
        composeTestRule.setContent {
            BottomActionBar(
                onSave = { },
                onCancel = { },
                saveEnabled = true
            )
        }

        composeTestRule
            .onNodeWithText("Save")
            .assertIsEnabled()
    }

    @Test
    fun dateTimeSectionDisplaysPresets() {
        composeTestRule.setContent {
            DateTimeSection(
                triggerAtMillis = null,
                onPresetSelected = { },
                onDateClick = { },
                onTimeClick = { }
            )
        }

        // Check that preset chips are displayed
        composeTestRule
            .onNodeWithText("+5 min")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Morning 9:00")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("Evening 18:00")
            .assertIsDisplayed()
    }

    @Test
    fun dateTimeSectionShowsErrorForPastTime() {
        val pastTime = System.currentTimeMillis() - 60000 // 1 minute ago
        
        composeTestRule.setContent {
            DateTimeSection(
                triggerAtMillis = pastTime,
                onPresetSelected = { },
                onDateClick = { },
                onTimeClick = { }
            )
        }

        composeTestRule
            .onNodeWithText("Time must be in the future.")
            .assertIsDisplayed()
    }

    @Test
    fun appSelectionCardShowsPlaceholderWhenNoAppSelected() {
        composeTestRule.setContent {
            AppSelectionCard(
                selectedApp = null to null,
                onClick = { }
            )
        }

        composeTestRule
            .onNodeWithText("Pick app")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Select an app to schedule")
            .assertIsDisplayed()
    }

    @Test
    fun appSelectionCardShowsSelectedApp() {
        composeTestRule.setContent {
            AppSelectionCard(
                selectedApp = "com.example.app" to "Example App",
                onClick = { }
            )
        }

        composeTestRule
            .onNodeWithText("Example App")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("com.example.app")
            .assertIsDisplayed()
    }
}