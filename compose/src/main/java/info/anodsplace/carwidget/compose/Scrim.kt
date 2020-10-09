package info.anodsplace.carwidget.compose

import androidx.compose.animation.animate
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color

@Composable
fun OverlayScrim(
        color: Color,
        onDismiss: () -> Unit,
        visible: Boolean
) {
    if (color != Color.Transparent) {
        val alpha = animate(target = if (visible) 1f else 0f, animSpec = TweenSpec())
        val dismissModifier = if (visible) Modifier.tapGestureFilter { onDismiss() } else Modifier
        Canvas(
            Modifier
                .fillMaxSize()
                .then(dismissModifier)
        ) {
            drawRect(color = color, alpha = alpha)
        }
    }
}