package com.example.sonicflow.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun WaveformVisualizer(
    currentPosition: Long,
    duration: Long,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFFFFC107),
    barWidth: Float = 4f,
    barSpacing: Float = 2f,
    numberOfBars: Int = 50
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_transition")

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    val barHeights = remember {
        FloatArray(numberOfBars) {
            Random.nextFloat() * 0.8f + 0.2f
        }
    }

    val animatedBarHeights = barHeights.mapIndexed { index, baseHeight ->
        val progressRatio = if (duration > 0) (currentPosition.toFloat() / duration) else 0f
        val distanceFromPlayhead = kotlin.math.abs(index.toFloat() / numberOfBars - progressRatio)

        if (isPlaying) {
            val sineWave = (sin((index + waveOffset) * 0.05f) + 1) / 2
            val distanceEffect = kotlin.math.max(0f, 1f - distanceFromPlayhead * 2f)
            (baseHeight * 0.5f + sineWave * 0.3f + distanceEffect * 0.2f).coerceIn(0.1f, 1f)
        } else {
            val pulse = (sin(waveOffset * 0.02f) + 1) / 4
            (baseHeight * pulse).coerceIn(0.1f, 1f)
        }
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val totalBarWidth = numberOfBars * (barWidth + barSpacing)
        val startX = (canvasWidth - totalBarWidth) / 2

        val progressRatio = if (duration > 0) (currentPosition.toFloat() / duration) else 0f

        for (i in 0 until numberOfBars) {
            val x = startX + i * (barWidth + barSpacing)
            val barHeight = animatedBarHeights[i] * canvasHeight
            val y = (canvasHeight - barHeight) / 2

            val barProgressRatio = i.toFloat() / numberOfBars
            val color = if (barProgressRatio < progressRatio) {
                barColor.copy(alpha = 0.9f)
            } else {
                barColor.copy(alpha = 0.4f)
            }

            drawLine(
                color = color,
                start = Offset(x + barWidth / 2, y),
                end = Offset(x + barWidth / 2, y + barHeight),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SimpleWaveformBar(
    currentPosition: Long,
    duration: Long,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFFFFC107)
) {
    val progressRatio = if (duration > 0) (currentPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "progress_transition")

    val animatedProgress by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = progressRatio,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "progress_animation"
        )
    } else {
        remember { mutableStateOf(progressRatio) }
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Barre de fond
        drawRect(
            color = barColor.copy(alpha = 0.2f),
            size = Size(canvasWidth, canvasHeight)
        )

        // Barre de progression
        drawRect(
            color = barColor,
            size = Size(canvasWidth * animatedProgress, canvasHeight)
        )
    }
}