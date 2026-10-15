package ru.hdlabs.electrosmeta.ui.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.sin
import kotlin.random.Random

private data class DriftParticle(
    val x: Float,
    val yStart: Float,
    val speed: Float,
    val size: Float,
    val swayAmp: Float,
    val swayPhase: Float,
    val spin: Float,
    val spinRate: Float,
    val color: Color,
    val alpha: Float,
)

private fun driftY(anim: BackgroundAnimState, particle: DriftParticle): Float {
    val travel = if (anim.animEnabled) {
        ((anim.phase * particle.speed + particle.yStart) % 1f + 1f) % 1f
    } else {
        particle.yStart
    }
    return travel
}

private fun DrawScope.particlePosition(
    anim: BackgroundAnimState,
    particle: DriftParticle,
): Offset {
    val travel = driftY(anim, particle)
    val y = travel * size.height * 1.08f - size.height * 0.04f
    val sway = if (anim.animEnabled) {
        sin((anim.phase * 2f + particle.swayPhase) * 6.28318f) * particle.swayAmp
    } else {
        0f
    }
    val x = particle.x * size.width + sway * size.width * 0.06f
    return Offset(x, y)
}

private fun mapleLeafPath(size: Float): Path = Path().apply {
    val s = size
    moveTo(0f, -s * 0.52f)
    lineTo(s * 0.14f, -s * 0.08f)
    lineTo(s * 0.48f, -s * 0.32f)
    lineTo(s * 0.18f, 0.02f)
    lineTo(s * 0.42f, s * 0.48f)
    lineTo(0f, s * 0.18f)
    lineTo(-s * 0.42f, s * 0.48f)
    lineTo(-s * 0.18f, 0.02f)
    lineTo(-s * 0.48f, -s * 0.32f)
    lineTo(-s * 0.14f, -s * 0.08f)
    close()
}

@Composable
fun SpringBackground(modifier: Modifier = Modifier) {
    val anim = rememberBackgroundAnim(cycleMillis = 34_000)
    val count = if (anim.powerSave) 14 else 26
    val petals = remember(count) {
        val random = Random(31)
        val colors = listOf(
            Color(0xFFF8BBD9),
            Color(0xFFFFE0EC),
            Color(0xFFFFF3E0),
            Color(0xFFE1BEE7),
        )
        List(count) {
            DriftParticle(
                x = random.nextFloat(),
                yStart = random.nextFloat(),
                speed = random.nextFloat() * 0.35f + 0.55f,
                size = random.nextFloat() * 5f + 4f,
                swayAmp = random.nextFloat() * 0.8f + 0.4f,
                swayPhase = random.nextFloat(),
                spin = random.nextFloat() * 360f,
                spinRate = random.nextFloat() * 40f - 20f,
                color = colors[random.nextInt(colors.size)],
                alpha = random.nextFloat() * 0.18f + 0.14f,
            )
        }
    }
    Canvas(modifier.fillMaxSize()) {
        drawRect(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF0E1418), Color(0xFF121C22), Color(0xFF1A2830)),
            ),
        )
        petals.forEach { petal ->
            val center = particlePosition(anim, petal)
            val rot = petal.spin + if (anim.animEnabled) anim.phase * petal.spinRate else 0f
            rotate(rot, center) {
                drawOval(
                    color = petal.color.copy(alpha = petal.alpha),
                    topLeft = Offset(center.x - petal.size, center.y - petal.size * 0.35f),
                    size = androidx.compose.ui.geometry.Size(petal.size * 2f, petal.size * 0.7f),
                )
            }
        }
    }
}

@Composable
fun SummerBackground(modifier: Modifier = Modifier) {
    val anim = rememberBackgroundAnim(cycleMillis = 36_000)
    val count = if (anim.powerSave) 8 else 14
    val flies = remember(count) {
        val random = Random(52)
        List(count) {
            DriftParticle(
                x = random.nextFloat(),
                yStart = random.nextFloat(),
                speed = random.nextFloat() * 0.25f + 0.35f,
                size = random.nextFloat() * 2.5f + 2f,
                swayAmp = random.nextFloat() * 1.2f + 0.6f,
                swayPhase = random.nextFloat(),
                spin = 0f,
                spinRate = 0f,
                color = if (random.nextBoolean()) Color(0xFFFFF59D) else Color(0xFFC5E1A5),
                alpha = random.nextFloat() * 0.2f + 0.35f,
            )
        }
    }
    Canvas(modifier.fillMaxSize()) {
        drawRect(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF0A0E18), Color(0xFF101828), Color(0xFF1A2030)),
            ),
        )
        flies.forEach { fly ->
            val center = particlePosition(anim, fly)
            val pulse = if (anim.animEnabled) {
                val wave = sin((anim.phase * 3f + fly.swayPhase) * 6.28318f)
                (0.45f + 0.55f * wave).coerceIn(0.2f, 1f)
            } else {
                0.7f
            }
            drawCircle(
                color = fly.color.copy(alpha = fly.alpha * 0.25f * pulse),
                radius = fly.size * 4.5f,
                center = center,
            )
            drawCircle(
                color = fly.color.copy(alpha = fly.alpha * pulse),
                radius = fly.size,
                center = center,
            )
        }
    }
}

@Composable
fun AutumnBackground(modifier: Modifier = Modifier) {
    val anim = rememberBackgroundAnim(cycleMillis = 38_000)
    val count = if (anim.powerSave) 12 else 22
    val leaves = remember(count) {
        val random = Random(19)
        val colors = listOf(
            Color(0xFFE65100),
            Color(0xFFD84315),
            Color(0xFFBF360C),
            Color(0xFF6D4C41),
            Color(0xFF558B2F),
        )
        List(count) {
            DriftParticle(
                x = random.nextFloat(),
                yStart = random.nextFloat(),
                speed = random.nextFloat() * 0.3f + 0.45f,
                size = random.nextFloat() * 7f + 8f,
                swayAmp = random.nextFloat() * 1f + 0.5f,
                swayPhase = random.nextFloat(),
                spin = random.nextFloat() * 360f,
                spinRate = random.nextFloat() * 55f - 25f,
                color = colors[random.nextInt(colors.size)],
                alpha = random.nextFloat() * 0.14f + 0.16f,
            )
        }
    }
    Canvas(modifier.fillMaxSize()) {
        drawRect(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF100C0A), Color(0xFF16120E), Color(0xFF1C1820)),
            ),
        )
        leaves.forEach { leaf ->
            val center = particlePosition(anim, leaf)
            val rot = leaf.spin + if (anim.animEnabled) anim.phase * leaf.spinRate else 0f
            translate(center.x, center.y) {
                rotate(rot) {
                    drawPath(
                        path = mapleLeafPath(leaf.size),
                        color = leaf.color.copy(alpha = leaf.alpha),
                    )
                }
            }
        }
    }
}

@Composable
fun WinterBackground(modifier: Modifier = Modifier) {
    val anim = rememberBackgroundAnim(cycleMillis = 32_000)
    val count = if (anim.powerSave) 22 else 42
    val flakes = remember(count) {
        val random = Random(88)
        List(count) {
            val large = random.nextFloat() > 0.82f
            DriftParticle(
                x = random.nextFloat(),
                yStart = random.nextFloat(),
                speed = random.nextFloat() * 0.35f + 0.4f,
                size = if (large) random.nextFloat() * 1.8f + 2.2f else random.nextFloat() * 1.2f + 0.8f,
                swayAmp = random.nextFloat() * 0.7f + 0.3f,
                swayPhase = random.nextFloat(),
                spin = random.nextFloat() * 360f,
                spinRate = random.nextFloat() * 30f - 15f,
                color = Color.White,
                alpha = if (large) random.nextFloat() * 0.2f + 0.35f else random.nextFloat() * 0.12f + 0.18f,
            )
        }
    }
    Canvas(modifier.fillMaxSize()) {
        drawRect(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF080C14), Color(0xFF0E1420), Color(0xFF141C28)),
            ),
        )
        flakes.forEach { flake ->
            val center = particlePosition(anim, flake)
            val rot = flake.spin + if (anim.animEnabled) anim.phase * flake.spinRate else 0f
            drawCircle(
                color = flake.color.copy(alpha = flake.alpha),
                radius = flake.size,
                center = center,
            )
            if (flake.size > 2.5f) {
                rotate(rot, center) {
                    val arm = flake.size * 1.8f
                    repeat(3) { i ->
                        rotate(i * 60f, center) {
                            drawLine(
                                color = flake.color.copy(alpha = flake.alpha * 0.65f),
                                start = Offset(center.x - arm, center.y),
                                end = Offset(center.x + arm, center.y),
                                strokeWidth = 0.8f,
                            )
                        }
                    }
                }
            }
        }
    }
}
