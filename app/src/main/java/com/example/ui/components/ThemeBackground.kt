package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.model.VisualTheme
import kotlin.math.sin

@Composable
fun AnimatedVisualBackground(
    theme: VisualTheme,
    isPlaying: Boolean,
    progressSec: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "canvas_anim")

    // Slow pulsing motion
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Floating angle for gradient and shapes
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "float"
    )

    val primaryColor = Color(theme.primaryHex)
    val secondaryColor = Color(theme.secondaryHex)
    val accentColor = Color(theme.accentHex)
    val bgBase = Color(theme.backgroundHex)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Deep atmospheric gradient base
        val bgGradient = Brush.verticalGradient(
            colors = listOf(
                bgBase,
                Color(
                    red = ((primaryColor.red * 0.25f + bgBase.red * 0.75f)),
                    green = ((primaryColor.green * 0.25f + bgBase.green * 0.75f)),
                    blue = ((primaryColor.blue * 0.25f + bgBase.blue * 0.75f))
                ),
                bgBase
            ),
            startY = 0f,
            endY = h
        )
        drawRect(brush = bgGradient, size = size)

        // 2. Python-inspired accent shapes with dynamic glow:
        // Top accent orb (Python script: ellipse at (150, 140, 930, 880))
        val rad1 = (w * 0.42f) * pulse
        val center1X = w * 0.5f + (sin(Math.toRadians(floatOffset.toDouble())).toFloat() * (w * 0.08f))
        val center1Y = h * 0.28f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.55f),
                    primaryColor.copy(alpha = 0.20f),
                    Color.Transparent
                ),
                center = Offset(center1X, center1Y),
                radius = rad1
            ),
            radius = rad1,
            center = Offset(center1X, center1Y)
        )

        // Bottom accent orb (Python script: ellipse at (620, 980, 1000, 1360))
        val rad2 = (w * 0.38f) * (2f - pulse)
        val center2X = w * 0.65f - (sin(Math.toRadians(floatOffset.toDouble() + 45)).toFloat() * (w * 0.07f))
        val center2Y = h * 0.72f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.50f),
                    secondaryColor.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(center2X, center2Y),
                radius = rad2
            ),
            radius = rad2,
            center = Offset(center2X, center2Y)
        )

        // Subtle accent grid lines or digital texture
        val lineSpacing = h / 20f
        for (i in 0..20) {
            val y = i * lineSpacing
            val alpha = (0.03f + 0.02f * sin((i + progressSec).toDouble()).toFloat()).coerceIn(0.01f, 0.08f)
            drawLine(
                color = accentColor.copy(alpha = alpha),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.5f
            )
        }

        // 3. Audio visualizer soundwave bars at lower quarter
        val barCount = 28
        val barWidth = (w * 0.75f) / (barCount * 1.5f)
        val startX = (w - (barCount * barWidth * 1.5f)) / 2f
        val baseY = h * 0.58f

        for (i in 0 until barCount) {
            val wavePhase = (i * 0.4f) + (progressSec * 8f)
            val barAmp = if (isPlaying) {
                (sin(wavePhase.toDouble()).toFloat() * 0.5f + 0.5f) * (h * 0.05f) + 8f
            } else {
                8f + (sin((i * 0.5).toDouble()).toFloat() * 4f)
            }

            val bx = startX + i * (barWidth * 1.5f)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(accentColor.copy(alpha = 0.8f), primaryColor.copy(alpha = 0.6f)),
                    startY = baseY - barAmp,
                    endY = baseY + barAmp
                ),
                topLeft = Offset(bx, baseY - barAmp / 2f),
                size = Size(barWidth, barAmp),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }
}
