package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class ParticleType {
    RIBBON,
    BURST,
    STAR,
    SPARKLE
}

private data class CelebrationParticle(
    val type: ParticleType,
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val gravity: Float,
    val rotationSpeed: Float,
    val color: Color,
    val size: Float,
    val delayFraction: Float = 0f
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    val palette = remember {
        listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFF4081), // Pink
            Color(0xFF00E676), // Bright Green
            Color(0xFF00E5FF), // Cyan
            Color(0xFFFF9100), // Orange
            Color(0xFF7C4DFF), // Purple
            Color(0xFFFF1744), // Crimson
            Color(0xFFFFFFFF)  // White Sparkle
        )
    }

    val particles = remember {
        val random = Random(12345)
        val list = mutableListOf<CelebrationParticle>()

        // 1. Central explosive burst (60 particles radiating outwards in all directions)
        for (i in 0 until 60) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val speed = 350f + random.nextFloat() * 650f
            list.add(
                CelebrationParticle(
                    type = if (i % 3 == 0) ParticleType.STAR else ParticleType.BURST,
                    initialX = 0.5f,
                    initialY = 0.45f,
                    velocityX = cos(angle) * speed,
                    velocityY = sin(angle) * speed,
                    gravity = 500f,
                    rotationSpeed = (random.nextFloat() - 0.5f) * 1080f,
                    color = palette[random.nextInt(palette.size)],
                    size = 12f + random.nextFloat() * 14f
                )
            )
        }

        // 2. Falling fluttering ribbons from top (70 particles)
        for (i in 0 until 70) {
            list.add(
                CelebrationParticle(
                    type = ParticleType.RIBBON,
                    initialX = random.nextFloat(),
                    initialY = -0.1f - random.nextFloat() * 0.25f,
                    velocityX = (random.nextFloat() - 0.5f) * 220f,
                    velocityY = 380f + random.nextFloat() * 520f,
                    gravity = 80f,
                    rotationSpeed = (random.nextFloat() - 0.5f) * 720f,
                    color = palette[random.nextInt(palette.size)],
                    size = 14f + random.nextFloat() * 16f,
                    delayFraction = random.nextFloat() * 0.2f
                )
            )
        }

        // 3. Twinkling sparkling stars (30 particles)
        for (i in 0 until 30) {
            list.add(
                CelebrationParticle(
                    type = ParticleType.SPARKLE,
                    initialX = random.nextFloat(),
                    initialY = 0.15f + random.nextFloat() * 0.6f,
                    velocityX = (random.nextFloat() - 0.5f) * 80f,
                    velocityY = -40f + random.nextFloat() * 80f,
                    gravity = 20f,
                    rotationSpeed = (random.nextFloat() - 0.5f) * 360f,
                    color = Color(0xFFFFEB3B),
                    size = 16f + random.nextFloat() * 18f
                )
            )
        }

        list
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val t = progress.value

        // Shockwave ripple ring in first 600ms
        if (t < 0.25f) {
            val shockwaveProgress = t / 0.25f
            val shockwaveRadius = width * 0.6f * shockwaveProgress
            val alpha = (1f - shockwaveProgress) * 0.7f
            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = alpha),
                radius = shockwaveRadius,
                center = Offset(width * 0.5f, height * 0.45f),
                style = Stroke(width = 8f * (1f - shockwaveProgress))
            )
        }

        for (p in particles) {
            if (t < p.delayFraction) continue
            val particleT = (t - p.delayFraction) / (1f - p.delayFraction)

            // Physics calculation: x = x0 + v*t, y = y0 + v*t + 0.5*g*t^2
            val currentX = (p.initialX * width) + (p.velocityX * particleT) +
                    sin(particleT * 10f + p.initialX * 20f) * 35f
            val currentY = (p.initialY * height) + (p.velocityY * particleT) +
                    (0.5f * p.gravity * particleT * particleT)

            // Fade out near the end
            val alpha = (1f - (particleT * 0.85f)).coerceIn(0f, 1f)
            val currentRot = p.rotationSpeed * particleT

            when (p.type) {
                ParticleType.RIBBON -> {
                    // Fluttering 3D effect: scale width sinusoidally
                    val flutterScale = cos(particleT * 14f + p.initialX * 10f)
                    val ribbonWidth = p.size * flutterScale
                    val ribbonHeight = p.size * 0.45f

                    rotate(degrees = currentRot, pivot = Offset(currentX, currentY)) {
                        drawRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(currentX - ribbonWidth / 2, currentY - ribbonHeight / 2),
                            size = Size(ribbonWidth, ribbonHeight)
                        )
                    }
                }

                ParticleType.BURST -> {
                    rotate(degrees = currentRot, pivot = Offset(currentX, currentY)) {
                        drawCircle(
                            color = p.color.copy(alpha = alpha),
                            radius = p.size * 0.4f * (1f - particleT * 0.3f),
                            center = Offset(currentX, currentY)
                        )
                    }
                }

                ParticleType.STAR -> {
                    val starSize = p.size * (1f - particleT * 0.4f)
                    rotate(degrees = currentRot, pivot = Offset(currentX, currentY)) {
                        drawStar(
                            center = Offset(currentX, currentY),
                            radius = starSize,
                            color = p.color.copy(alpha = alpha)
                        )
                    }
                }

                ParticleType.SPARKLE -> {
                    // Pulsating sparkle
                    val pulse = (sin(particleT * 20f) + 1f) * 0.5f
                    val sparkleAlpha = alpha * pulse
                    drawSparkle(
                        center = Offset(currentX, currentY),
                        size = p.size * pulse,
                        color = p.color.copy(alpha = sparkleAlpha)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val points = 5
    val innerRadius = radius * 0.45f
    val step = PI / points

    for (i in 0 until 2 * points) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = i * step - PI / 2
        val x = center.x + (cos(angle) * r).toFloat()
        val y = center.y + (sin(angle) * r).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color)
}

private fun DrawScope.drawSparkle(center: Offset, size: Float, color: Color) {
    // 4-pointed sparkle star
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticTo(center.x, center.y, center.x + size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + size)
        quadraticTo(center.x, center.y, center.x - size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - size)
        close()
    }
    drawPath(path, color = color)
}
