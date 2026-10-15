package ru.hdlabs.electrosmeta.ui.background

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import ru.hdlabs.electrosmeta.ElectrosmetaApplication
import ru.hdlabs.electrosmeta.data.settings.AppSettings
import ru.hdlabs.electrosmeta.ui.motion.resolveHdMotion

data class BackgroundAnimState(
    val animEnabled: Boolean,
    val speedFactor: Float,
    val powerSave: Boolean,
    val phase: Float,
)

@Composable
fun rememberBackgroundAnim(cycleMillis: Int = 28_000): BackgroundAnimState {
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
    val motion = resolveHdMotion(settings)
    val speedFactor = (settings.animationSpeedPercent / 100f).coerceAtLeast(0.1f) * motion.backgroundSpeedMultiplier

    val transition = rememberInfiniteTransition(label = "bgAnim")
    val phase = if (animEnabled) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (cycleMillis / speedFactor).toInt().coerceAtLeast(4_000),
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "phase",
        ).value
    } else {
        0.35f
    }

    return BackgroundAnimState(
        animEnabled = animEnabled,
        speedFactor = speedFactor,
        powerSave = settings.powerSaveMode,
        phase = phase,
    )
}
