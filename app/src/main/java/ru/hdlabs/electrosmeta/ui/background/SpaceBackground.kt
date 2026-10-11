package ru.hdlabs.electrosmeta.ui.background

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import ru.hdlabs.electrosmeta.ElectrosmetaApplication
import ru.hdlabs.electrosmeta.data.settings.AppSettings
import ru.hdlabs.electrosmeta.ui.theme.HdBlack
import ru.hdlabs.electrosmeta.ui.theme.HdBlue
import ru.hdlabs.electrosmeta.ui.theme.HdOrange
import kotlin.math.sin
import kotlin.random.Random

private data class Star(val x: Float, val y: Float, val radius: Float, val layer: Int)

@Composable
fun SpaceBackground(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as ElectrosmetaApplication
    val settings by app.container.settingsRepository.settings.collectAsStateWithLifecycle(
        initialValue = AppSettings(),
    )
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val reduceMotion = remember {
        try {
            android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
            ) == 0f
        } catch (_: Exception) {
            false
        }
    }

    val animEnabled = settings.animationEnabled &&
        !reduceMotion &&
        lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    val speedFactor = settings.animationSpeedPercent / 100f
    val powerSave = settings.powerSaveMode
    val starCount = if (powerSave) 60 else 140

    val stars = remember(starCount) {
        List(starCount) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 2f + 0.5f,
                layer = Random.nextInt(3),
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "space")
    val phase = if (animEnabled) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (24000 / speedFactor.coerceAtLeast(0.1f)).toInt(),
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "phase",
        ).value
    } else {
        0f
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(HdBlack)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(HdBlue.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.15f),
                radius = size.width * 0.7f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(HdOrange.copy(alpha = 0.06f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.75f),
                radius = size.width * 0.55f,
            ),
        )

        stars.forEach { star ->
            val drift = (star.layer + 1) * 0.02f * speedFactor
            val y = (star.y + phase * drift) % 1f
            val alpha = when (star.layer) {
                0 -> 0.35f
                1 -> 0.55f
                else -> 0.85f
            }
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = star.radius,
                center = Offset(star.x * size.width, y * size.height),
            )
        }

        if (animEnabled && !powerSave) {
            val pulsarAlpha = 0.15f + 0.1f * sin(phase * 6.28f).toFloat()
            drawCircle(
                color = HdBlue.copy(alpha = pulsarAlpha),
                radius = size.minDimension * 0.12f,
                center = Offset(size.width * 0.12f, size.height * 0.35f),
            )
            drawCircle(
                color = HdOrange.copy(alpha = pulsarAlpha * 0.9f),
                radius = size.minDimension * 0.08f,
                center = Offset(size.width * 0.88f, size.height * 0.22f),
            )
            val streakY = (phase * 1.2f) % 1f
            drawLine(
                color = HdBlue.copy(alpha = 0.35f),
                start = Offset(size.width * 0.1f, streakY * size.height),
                end = Offset(size.width * 0.35f, (streakY + 0.05f) * size.height),
                strokeWidth = 2f,
            )
        }
    }
}
