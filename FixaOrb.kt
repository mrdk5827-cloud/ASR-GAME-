package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.FixaState
import com.example.ui.theme.FixaPrimary
import com.example.ui.theme.FixaSecondary
import com.example.ui.theme.FixaTertiary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FixaOrb(
    state: FixaState,
    size: Dp = 140.dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_anim")

    // Rotation angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    FixaState.THINKING -> 1500
                    FixaState.LISTENING -> 2500
                    FixaState.SPEAKING -> 2000
                    FixaState.IDLE -> 6000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulse scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    FixaState.LISTENING -> 600
                    FixaState.SPEAKING -> 800
                    FixaState.THINKING -> 500
                    FixaState.IDLE -> 1800
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2f) * 0.72f * pulseScale

            val primaryColor = when (state) {
                FixaState.LISTENING -> FixaTertiary
                FixaState.THINKING -> FixaSecondary
                FixaState.SPEAKING -> FixaPrimary
                FixaState.IDLE -> FixaPrimary
            }

            val secondaryColor = when (state) {
                FixaState.LISTENING -> FixaSecondary
                FixaState.THINKING -> FixaPrimary
                FixaState.SPEAKING -> FixaTertiary
                FixaState.IDLE -> FixaSecondary
            }

            // 1. Ambient outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.45f),
                        secondaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.35f
                ),
                radius = baseRadius * 1.35f,
                center = center
            )

            // 2. Outer revolving dashed ring
            val outerRingRadius = baseRadius * 1.15f
            drawCircle(
                color = primaryColor.copy(alpha = 0.65f),
                radius = outerRingRadius,
                center = center,
                style = Stroke(
                    width = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f), rotationAngle * 2f)
                )
            )

            // 3. Counter-rotating inner dashed ring
            val innerRingRadius = baseRadius * 0.95f
            drawCircle(
                color = secondaryColor.copy(alpha = 0.75f),
                radius = innerRingRadius,
                center = center,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 20f), -rotationAngle * 3f)
                )
            )

            // 4. Core energetic sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryColor,
                        secondaryColor,
                        Color(0xFF0A0E1A)
                    ),
                    center = Offset(center.x - baseRadius * 0.2f, center.y - baseRadius * 0.2f),
                    radius = baseRadius * 0.85f
                ),
                radius = baseRadius * 0.85f,
                center = center
            )

            // 5. Orbiting energy sparks
            val sparkCount = 4
            for (i in 0 until sparkCount) {
                val sparkAngle = Math.toRadians((rotationAngle + i * (360f / sparkCount)).toDouble())
                val sparkDistance = baseRadius * 1.08f
                val sparkCenter = Offset(
                    x = center.x + (sparkDistance * cos(sparkAngle)).toFloat(),
                    y = center.y + (sparkDistance * sin(sparkAngle)).toFloat()
                )
                drawCircle(
                    color = if (i % 2 == 0) Color.White else primaryColor,
                    radius = 3.5.dp.toPx(),
                    center = sparkCenter
                )
            }
        }
    }
}
