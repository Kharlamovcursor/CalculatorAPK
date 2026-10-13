package ru.hdlabs.electrosmeta.ui.motion

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.Immutable
import ru.hdlabs.electrosmeta.data.settings.AppSettings
import ru.hdlabs.electrosmeta.data.settings.UiMotionLevel

@Immutable
data class HdMotionSpec(
    val buttonPressScale: Float,
    val buttonPressDurationMs: Int,
    val expandDurationMs: Int,
    val highlightPulseStrength: Float,
    val scaffoldOverlayAlpha: Float,
    val screenEnterMs: Int,
    val screenExitMs: Int,
    val lineEnterMs: Int,
    val backgroundSpeedMultiplier: Float,
) {
    companion object {
        fun balanced(): HdMotionSpec = resolveHdMotion(AppSettings())
    }
}

val LocalHdMotion = compositionLocalOf { HdMotionSpec.balanced() }

fun resolveHdMotion(settings: AppSettings): HdMotionSpec {
    if (!settings.animationEnabled || settings.powerSaveMode) {
        return HdMotionSpec(
            buttonPressScale = 0.98f,
            buttonPressDurationMs = 60,
            expandDurationMs = 180,
            highlightPulseStrength = 0.6f,
            scaffoldOverlayAlpha = 0.58f,
            screenEnterMs = 120,
            screenExitMs = 100,
            lineEnterMs = 150,
            backgroundSpeedMultiplier = 0.85f,
        )
    }
    return when (settings.uiMotionLevel) {
        UiMotionLevel.Subtle -> HdMotionSpec(
            buttonPressScale = 0.96f,
            buttonPressDurationMs = 100,
            expandDurationMs = 280,
            highlightPulseStrength = 1f,
            scaffoldOverlayAlpha = 0.50f,
            screenEnterMs = 180,
            screenExitMs = 140,
            lineEnterMs = 220,
            backgroundSpeedMultiplier = 1f,
        )
        UiMotionLevel.Balanced -> HdMotionSpec(
            buttonPressScale = 0.93f,
            buttonPressDurationMs = 130,
            expandDurationMs = 340,
            highlightPulseStrength = 1.35f,
            scaffoldOverlayAlpha = 0.42f,
            screenEnterMs = 220,
            screenExitMs = 160,
            lineEnterMs = 280,
            backgroundSpeedMultiplier = 1.15f,
        )
        UiMotionLevel.Expressive -> HdMotionSpec(
            buttonPressScale = 0.90f,
            buttonPressDurationMs = 160,
            expandDurationMs = 400,
            highlightPulseStrength = 1.7f,
            scaffoldOverlayAlpha = 0.34f,
            screenEnterMs = 280,
            screenExitMs = 200,
            lineEnterMs = 320,
            backgroundSpeedMultiplier = 1.3f,
        )
    }
}
