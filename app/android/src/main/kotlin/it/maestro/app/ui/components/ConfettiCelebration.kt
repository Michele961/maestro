package it.maestro.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.maestro.app.R
import kotlin.math.cos
import kotlin.math.sin

private val CONFETTI_DRAWABLES = listOf(
    R.drawable.particle_confetti_1,
    R.drawable.particle_confetti_2,
    R.drawable.particle_confetti_3,
    R.drawable.particle_confetti_4,
    R.drawable.particle_confetti_5,
    R.drawable.particle_confetti_6,
    R.drawable.particle_confetti_7,
    R.drawable.particle_confetti_8,
    R.drawable.particle_confetti_9,
    R.drawable.particle_confetti_10,
)

private data class ConfettiPiece(
    val drawableRes: Int,
    val vx: Float,
    val vy: Float,
    val initialRot: Float,
    val rotSpeed: Float,
    val scale: Float,
    val sizeDp: Float,
)

/**
 * Pioggia e cascata di coriandoli celebrativi per la vittoria della partita (task F1-25).
 */
@Composable
public fun ConfettiCelebration(
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }

    val confetti = remember {
        val count = 40
        List(count) { i ->
            val angle = -Math.PI.toFloat() * 0.9f + (i.toFloat() / count) * Math.PI.toFloat() * 0.8f
            val speed = 180f + (i * 23 % 260)
            ConfettiPiece(
                drawableRes = CONFETTI_DRAWABLES[i % CONFETTI_DRAWABLES.size],
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 120f,
                initialRot = (i * 31 % 360).toFloat(),
                rotSpeed = -240f + (i * 47 % 480),
                scale = 0.6f + ((i * 17 % 40) / 100f),
                sizeDp = 24f + (i * 11 % 16),
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val t = progress.value * 2.4f
        val alpha = (1f - (progress.value * 1.1f)).coerceIn(0f, 1f)
        val gravity = 280f

        confetti.forEach { c ->
            val px = c.vx * t
            val py = c.vy * t + 0.5f * gravity * t * t + 60f
            val rot = c.initialRot + c.rotSpeed * t

            Box(
                modifier = Modifier
                    .offset(x = px.dp, y = py.dp)
                    .size(c.sizeDp.dp)
                    .graphicsLayer {
                        rotationZ = rot
                        scaleX = c.scale
                        scaleY = c.scale
                        this.alpha = alpha
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = c.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
