package it.maestro.app.feature.table.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import it.maestro.app.designsystem.haptics.rememberGameHaptics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import it.maestro.app.R
import it.maestro.app.feature.table.presentation.UiGameEvent
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private val LEAF_DRAWABLES = listOf(
    R.drawable.particle_leaf_1,
    R.drawable.particle_leaf_2,
    R.drawable.particle_leaf_3,
    R.drawable.particle_leaf_4,
    R.drawable.particle_leaf_5,
    R.drawable.particle_leaf_6,
    R.drawable.particle_leaf_7,
    R.drawable.particle_leaf_8,
)

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

private data class Particle(
    val drawableRes: Int,
    val vx: Float,
    val vy: Float,
    val initialRot: Float,
    val rotSpeed: Float,
    val baseScale: Float,
    val sizeDp: Float,
)

/**
 * Overlay celebrativo d'impatto per la Scopa (task F1-25).
 * - Scopa Giocatore: banner dorato con rimbalzo elastico, alloro, esplosione di foglie d'oro e vibrazione.
 * - Scope Consecutive: banner dinamico "ANCORA!" o "DOPPIA!".
 * - Scopa Avversario: banner rosso sobrio con piccolo tonfo a schermo.
 * - Dura quanto il passo della regia che la mostra (`MotionSpec`); il tocco ovunque sullo schermo salta la scena.
 */
@Composable
public fun SweepCelebrationOverlay(
    event: UiGameEvent.Sweep,
    isHuman: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    val haptic = rememberGameHaptics()

    val bannerRes = remember(event, isHuman) {
        if (isHuman) {
            when {
                event.sweepCountThisDeal == 1 -> R.drawable.text_scopa_gold
                event.sweepCountThisDeal == 2 -> R.drawable.text_ancora_gold
                else -> R.drawable.text_doppia_gold
            }
        } else {
            R.drawable.text_scopa_red
        }
    }

    // Animazione di ingresso con rimbalzo elastico
    val scale = remember { Animatable(0.2f) }
    val rotation = remember { Animatable(if (isHuman) -14f else 4f) }
    val alpha = remember { Animatable(0f) }
    val particleProgress = remember { Animatable(0f) }

    // Generazione particelle (28 foglie per il giocatore umano)
    val particles = remember(isHuman) {
        if (!isHuman) emptyList()
        else {
            val count = 28
            List(count) { i ->
                val angle = (i.toFloat() / count) * 2f * Math.PI.toFloat() + ((i * 37 % 100) / 100f - 0.5f) * 0.4f
                val speed = 140f + (i * 29 % 160)
                Particle(
                    drawableRes = LEAF_DRAWABLES[i % LEAF_DRAWABLES.size],
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 90f,
                    initialRot = (i * 47 % 360).toFloat(),
                    rotSpeed = -180f + (i * 53 % 360),
                    baseScale = 0.55f + ((i * 19 % 40) / 100f),
                    sizeDp = 28f + (i * 13 % 14),
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        // Feedback aptico all'impatto
        haptic(HapticFeedbackType.LongPress)

        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        launch {
            rotation.animateTo(
                targetValue = if (isHuman) -5f else 0f,
                animationSpec = tween(400, easing = FastOutSlowInEasing),
            )
        }
        launch {
            alpha.animateTo(1f, tween(120))
        }
        if (particles.isNotEmpty()) {
            launch {
                particleProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1600, easing = FastOutSlowInEasing),
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("sweep_celebration_overlay")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Bagliore radiale sullo sfondo
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val glowRadius = size.minDimension * 0.55f
            val colors = if (isHuman) {
                listOf(
                    Color(0x99FFD700),
                    Color(0x44D4AF37),
                    Color.Transparent,
                )
            } else {
                listOf(
                    Color(0x778E1F1F),
                    Color(0x333B2213),
                    Color.Transparent,
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = colors,
                    center = center,
                    radius = glowRadius * scale.value.coerceIn(0.2f, 1.2f),
                ),
                radius = glowRadius * scale.value.coerceIn(0.2f, 1.2f),
                center = center,
            )
        }

        // Particelle: foglie d'oro in esplosione e caduta fisica
        val t = particleProgress.value * 1.6f
        val particleAlpha = (1f - (particleProgress.value * 1.15f)).coerceIn(0f, 1f)
        val gravity = 320f

        particles.forEach { p ->
            val px = p.vx * t
            val py = p.vy * t + 0.5f * gravity * t * t
            val rot = p.initialRot + p.rotSpeed * t

            Box(
                modifier = Modifier
                    .offset(x = px.dp, y = py.dp)
                    .size(p.sizeDp.dp)
                    .graphicsLayer {
                        rotationZ = rot
                        scaleX = p.baseScale
                        scaleY = p.baseScale
                        this.alpha = particleAlpha
                    },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = p.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        // Banner centrale con il lettering Scopa
        Image(
            painter = painterResource(id = bannerRes),
            contentDescription = if (isHuman) "Scopa!" else "Scopa dell'avversario",
            modifier = Modifier
                .width(320.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = rotation.value
                    this.alpha = alpha.value
                }
                .testTag("sweep_banner_image"),
            contentScale = ContentScale.Fit,
        )
    }
}
