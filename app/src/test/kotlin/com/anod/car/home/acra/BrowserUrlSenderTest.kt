package com.anod.car.home.acra

import org.junit.Assert.assertEquals
import org.junit.Test

class BrowserUrlSenderTest {
    @Test
    fun keepsRootCauseAfterNestedInflationExceptions() {
        val trace = """
            android.view.InflateException: Error inflating preference
                at com.anod.car.home.SettingsActivity.onCreate(SettingsActivity.kt:10)
                at com.anod.car.home.SettingsActivity.create(SettingsActivity.kt:20)
                at com.anod.car.home.SettingsActivity.inflate(SettingsActivity.kt:30)
                at com.anod.car.home.SettingsActivity.bind(SettingsActivity.kt:40)
            Caused by: android.view.InflateException: Error inflating SwitchCompat
                at android.view.LayoutInflater.createView(LayoutInflater.java:1)
            Caused by: java.lang.reflect.InvocationTargetException
                at java.lang.reflect.Constructor.newInstance(Constructor.java:1)
            Caused by: android.content.res.Resources.NotFoundException: Missing drawable
                at android.content.res.Resources.getDrawable(Resources.java:1)
        """.trimIndent()

        assertEquals(
            """
                android.view.InflateException: Error inflating preference
                    at com.anod.car.home.SettingsActivity.onCreate(SettingsActivity.kt:10)
                    at com.anod.car.home.SettingsActivity.create(SettingsActivity.kt:20)
                    at com.anod.car.home.SettingsActivity.inflate(SettingsActivity.kt:30)
                Caused by: android.view.InflateException: Error inflating SwitchCompat
                Caused by: java.lang.reflect.InvocationTargetException
                Caused by: android.content.res.Resources.NotFoundException: Missing drawable
            """.trimIndent(),
            minifyCrashTrace(trace)
        )
    }

    @Test
    fun keepsCauseChainWithoutApplicationFrames() {
        val trace = """
            android.view.InflateException: Error inflating preference
                at android.view.LayoutInflater.inflate(LayoutInflater.java:1)
            Caused by: android.view.InflateException: Error inflating SwitchCompat
            Caused by: java.lang.reflect.InvocationTargetException
            Caused by: java.lang.IllegalArgumentException: Invalid theme
        """.trimIndent()

        assertEquals(
            trace.lineSequence().filterNot { it.trimStart().startsWith("at ") }.joinToString("\n"),
            minifyCrashTrace(trace)
        )
    }

    @Test
    fun handlesEmptyTrace() {
        assertEquals("", minifyCrashTrace(""))
    }
}
