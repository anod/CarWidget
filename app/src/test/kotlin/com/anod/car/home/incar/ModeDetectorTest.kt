// Copyright (c) CarWidget contributors. Licensed under the project license.
package com.anod.car.home.incar

import info.anodsplace.carwidget.content.preferences.InCarInterface
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Exercises the thread-safety hardening around ModeDetector's shared state: prefState must hand
 * back an independent copy, and updatePrefState/forceState must map each preference flag to the
 * correct slot without leaking across flags.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], application = android.app.Application::class)
class ModeDetectorTest {

    @Test
    fun prefStateReturnsDefensiveCopy() {
        ModeDetector.updatePrefState(InCarInterface.NoOp(isPowerRequired = true))
        val snapshot = ModeDetector.prefState
        assertNotSame(snapshot, ModeDetector.prefState)
        assertTrue(snapshot[FLAG_POWER])

        snapshot[FLAG_POWER] = false

        assertTrue("mutating the returned array must not affect internal state", ModeDetector.prefState[FLAG_POWER])
    }

    @Test
    fun updatePrefStateMapsEachPreferenceFlag() {
        ModeDetector.updatePrefState(
            InCarInterface.NoOp(
                isPowerRequired = true,
                isHeadsetRequired = false,
                isBluetoothRequired = true,
                isActivityRequired = true,
                isCarDockRequired = false
            )
        )
        val state = ModeDetector.prefState
        assertTrue(state[FLAG_POWER])
        assertFalse(state[FLAG_HEADSET])
        assertTrue(state[FLAG_BLUETOOTH])
        assertTrue(state[FLAG_ACTIVITY])
        assertFalse(state[FLAG_CAR_DOCK])
    }

    @Test
    fun forceStateActivatesOnlyEnabledFlags() {
        ModeDetector.forceState(
            InCarInterface.NoOp(isPowerRequired = true, isBluetoothRequired = false),
            forceMode = true
        )
        val byFlag = ModeDetector.eventsState().associateBy { it.id }

        assertTrue(byFlag.getValue(FLAG_POWER).enabled)
        assertTrue(byFlag.getValue(FLAG_POWER).active)
        assertFalse(byFlag.getValue(FLAG_BLUETOOTH).enabled)
        assertFalse(byFlag.getValue(FLAG_BLUETOOTH).active)
    }

    private companion object {
        const val FLAG_POWER = 0
        const val FLAG_HEADSET = 1
        const val FLAG_BLUETOOTH = 2
        const val FLAG_ACTIVITY = 3
        const val FLAG_CAR_DOCK = 4
    }
}
