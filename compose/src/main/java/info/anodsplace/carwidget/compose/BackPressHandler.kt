package info.anodsplace.carwidget.compose

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticAmbientOf

/**
 * This [Ambient] is used to provide an [OnBackPressedDispatcherOwner]:
 *
 * ```
 * Providers(BackPressedDispatcherAmbient provides requireActivity()) { }
 * ```
 *
 * and setting up the callbacks with [backPressHandler].
 */
val BackPressedDispatcherAmbient =
        staticAmbientOf<OnBackPressedDispatcherOwner> { error("Ambient used without Provider") }

/**
 * This [Composable] can be used with a [BackPressedDispatcherAmbient] to intercept a back press (if
 * [enabled]).
 */
@Composable
fun backPressHandler(onBackPressed: () -> Unit, enabled: Boolean = true) {
    val dispatcher = BackPressedDispatcherAmbient.current.onBackPressedDispatcher

    // This callback is going to be remembered only if onBackPressed is referentially equal.
    val backCallback = remember(onBackPressed) {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
    }

    // Using onCommit guarantees that failed transactions don't incorrectly toggle the
    // remembered callback.
    onCommit(enabled) {
        backCallback.isEnabled = enabled
    }

    onCommit(dispatcher, onBackPressed) {
        // Whenever there's a new dispatcher set up the callback
        dispatcher.addCallback(backCallback)
        onDispose {
            backCallback.remove()
        }
    }
}