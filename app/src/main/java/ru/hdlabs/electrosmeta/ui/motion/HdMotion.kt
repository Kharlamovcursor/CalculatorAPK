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
    val screenSlideDivisor: Int,
) {
    companion object {
        fun balanced(): HdMotionSpec = resolveHdMotion(AppSettings())
    }
}

val LocalHdMotion = compositionLocalOf { HdMotionSpec.balanced() }

fun resolveHdMotion(settings: AppSettings): HdMotionSpec {
    if (!settings.animationEnabled || settings.powerSaveMode) {
        return HdMotionSpec(
            buttonPressScale = 1f,
            buttonPressDurationMs = 40,
            expandDurationMs = 80,
            highlightPulseStrength = 0.2f,
            scaffoldOverlayAlpha = 0.62f,
            screenEnterMs = 50,
            screenExitMs = 40,
            lineEnterMs = 60,
            backgroundSpeedMultiplier = 0.4f,
            screenSlideDivisor = 0,
        )
    }
    return when (settings.uiMotionLevel) {
        UiMotionLevel.Subtle -> HdMotionSpec(
            buttonPressScale = 1f,
            buttonPressDurationMs = 40,
            expandDurationMs = 90,
            highlightPulseStrength = 0.15f,
            scaffoldOverlayAlpha = 0.62f,
            screenEnterMs = 70,
            screenExitMs = 50,
            lineEnterMs = 80,
            backgroundSpeedMultiplier = 0.4f,
            screenSlideDivisor = 0,
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
            screenSlideDivisor = 12,
        )
        UiMotionLevel.Expressive -> HdMotionSpec(
            buttonPressScale = 0.82f,
            buttonPressDurationMs = 240,
            expandDurationMs = 760,
            highlightPulseStrength = 2.2f,
            scaffoldOverlayAlpha = 0.2f,
            screenEnterMs = 480,
            screenExitMs = 340,
            lineEnterMs = 540,
            backgroundSpeedMultiplier = 2.2f,
            screenSlideDivisor = 4,
        )
    }
}
