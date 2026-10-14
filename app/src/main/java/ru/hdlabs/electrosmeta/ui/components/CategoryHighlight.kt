package ru.hdlabs.electrosmeta.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.hdlabs.electrosmeta.data.settings.CategoryHighlightStyle
import ru.hdlabs.electrosmeta.ui.theme.HdBlue
import ru.hdlabs.electrosmeta.ui.theme.HdOrange
import ru.hdlabs.electrosmeta.ui.theme.HdPanel
import ru.hdlabs.electrosmeta.ui.theme.HdTextPrimary
import ru.hdlabs.electrosmeta.ui.theme.HdTextSecondary

@Immutable
data class CategoryHighlightColors(
    val background: Color,
    val borderBrush: Brush,
    val borderWidth: Dp,
    val titleColor: Color,
)

@Composable
fun categoryHighlightColors(
    active: Boolean,
    style: CategoryHighlightStyle,
    animationEnabled: Boolean,
    expanded: Boolean = false,
    pulseStrength: Float = 1f,
): CategoryHighlightColors {
    if (!active) {
        return CategoryHighlightColors(
            background = HdPanel.copy(alpha = if (expanded) 0.65f else 0.6f),
            borderBrush = SolidColor(
                if (expanded) HdOrange.copy(alpha = 0.45f) else HdTextSecondary.copy(alpha = 0.35f),
            ),
            borderWidth = if (expanded) 1.5.dp else 1.dp,
            titleColor = HdTextPrimary,
        )
    }
    val pulse = if (animationEnabled) {
        val transition = rememberInfiniteTransition(label = "catHighlight")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "catHighlightPulse",
        ).value
    } else {
        0.5f
    }
    val amp = pulseStrength.coerceIn(0f, 2.4f)
    val p = (pulse * amp).coerceIn(0f, 1f)
    return when (style) {
        CategoryHighlightStyle.SoftGlow -> {
            val glow = (0.12f + p * 0.14f) * amp.coerceAtMost(1.5f)
            CategoryHighlightColors(
                background = HdBlue.copy(alpha = glow.coerceIn(0f, 0.45f)),
                borderBrush = SolidColor(HdBlue.copy(alpha = (0.45f + p * 0.22f).coerceIn(0f, 1f))),
                borderWidth = 1.5.dp,
                titleColor = HdTextPrimary,
            )
        }
        CategoryHighlightStyle.PulseBorder -> {
            CategoryHighlightColors(
                background = HdPanel.copy(alpha = 0.72f),
                borderBrush = SolidColor(HdOrange.copy(alpha = (0.35f + p * 0.32f).coerceIn(0f, 1f))),
                borderWidth = (1f + p * 1.5f).dp,
                titleColor = HdTextPrimary,
            )
        }
        CategoryHighlightStyle.GradientRing -> {
            CategoryHighlightColors(
                background = HdPanel.copy(alpha = 0.68f),
                borderBrush = Brush.horizontalGradient(
                    listOf(
                        HdBlue.copy(alpha = (0.5f + p * 0.2f).coerceIn(0f, 1f)),
                        HdOrange.copy(alpha = (0.45f + p * 0.18f).coerceIn(0f, 1f)),
                    ),
                ),
                borderWidth = 1.5.dp,
                titleColor = HdTextPrimary,
            )
        }
        CategoryHighlightStyle.Noticeable -> {
            CategoryHighlightColors(
                background = HdBlue.copy(alpha = (0.18f + p * 0.2f).coerceIn(0f, 0.55f)),
                borderBrush = SolidColor(HdOrange.copy(alpha = (0.5f + p * 0.35f).coerceIn(0f, 1f))),
                borderWidth = (1.5f + p * 1f).dp,
                titleColor = HdTextPrimary,
            )
        }
    }
}
