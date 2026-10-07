package tachiyomi.presentation.core.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun LightningProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = color.copy(alpha = 0.2f),
    progress: (() -> Float)? = null,
) {
    val fraction = progress?.invoke()?.let { if (it.isFinite()) it.coerceIn(0f, 1f) else 0f }
    val pulse = if (fraction == null) {
        val transition = rememberInfiniteTransition(label = "lightningLoading")
        val alpha by transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
            label = "lightningPulse",
        )
        alpha
    } else {
        1f
    }
    Box(
        modifier = modifier.size(40.dp).semantics {
            progressBarRangeInfo = if (fraction == null) {
                ProgressBarRangeInfo.Indeterminate
            } else {
                ProgressBarRangeInfo(fraction, 0f..1f)
            }
        },
    ) {
        if (fraction != null) {
            Icon(Icons.Filled.Bolt, contentDescription = null, tint = trackColor, modifier = Modifier.matchParentSize())
        }
        Icon(
            Icons.Filled.Bolt,
            contentDescription = null,
            tint = color,
            modifier = Modifier.matchParentSize().alpha(pulse).drawWithContent {
                if (fraction == null) {
                    drawContent()
                } else {
                    clipRect(top = size.height * (1f - fraction)) { this@drawWithContent.drawContent() }
                }
            },
        )
    }
}

@Composable
fun CombinedLightningProgressIndicator(progress: () -> Float, modifier: Modifier = Modifier) {
    val value = progress()
    LightningProgressIndicator(modifier = modifier, progress = if (value <= 0f) null else ({ value }))
}
