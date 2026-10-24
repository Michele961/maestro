package it.maestro.app.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.maestro.app.R

/**
 * Stati d'animo ed espressioni visive del Maestro (task F1-14).
 */
public enum class MaestroMood {
    NEUTRAL,
    THINKING,
    PLEASED,
    WARNING,
    CELEBRATING,
}

/**
 * Avatar dinamico e animato del Maestro (task F1-14, F1-13).
 * Se è presente un asset drawable dedicato (`maestro_<mood>`), viene mostrato all'interno
 * del medaglione circolare; altrimenti viene renderizzata la versione procedurale.
 */
@SuppressLint("DiscouragedApi")
@Composable
public fun MaestroAvatarView(
    modifier: Modifier = Modifier,
    mood: MaestroMood = MaestroMood.NEUTRAL,
    size: Dp = 48.dp,
) {
    val auraColor by animateColorAsState(
        targetValue = when (mood) {
            MaestroMood.NEUTRAL -> Color(0xFF4A5568)
            MaestroMood.THINKING -> Color(0xFFE9C46A)
            MaestroMood.PLEASED -> Color(0xFF2A9D8F)
            MaestroMood.WARNING -> Color(0xFFE76F51)
            MaestroMood.CELEBRATING -> Color(0xFFFFD166)
        },
        animationSpec = tween(durationMillis = 400),
        label = "maestro_aura_color",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "maestro_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (mood == MaestroMood.THINKING || mood == MaestroMood.CELEBRATING) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (mood == MaestroMood.THINKING) 800 else 1400,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "maestro_pulse_scale",
    )

    val contentDesc = stringResource(id = R.string.maestro_avatar_description)
    val context = LocalContext.current
    val customAvatarResId = remember(mood) {
        val resName = "maestro_${mood.name.lowercase()}"
        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
        if (resId != 0) resId else null
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("maestro_avatar_${mood.name.lowercase()}")
            .semantics { this.contentDescription = contentDesc },
        contentAlignment = Alignment.Center,
    ) {
        // Aura pulsante esterna
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2f) - 2.dp.toPx()
            drawCircle(
                color = auraColor.copy(alpha = 0.35f),
                radius = baseRadius * pulseScale,
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )
        }

        // Cerchio base avatar
        Box(
            modifier = Modifier
                .size(size - 8.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
                .border(2.dp, auraColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (customAvatarResId != null) {
                Image(
                    painter = painterResource(id = customAvatarResId),
                    contentDescription = contentDesc,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                ProceduralMaestroFace(mood = mood, auraColor = auraColor, size = size)
            }
        }
    }
}

@Composable
private fun ProceduralMaestroFace(
    mood: MaestroMood,
    auraColor: Color,
    size: Dp,
) {
    Canvas(modifier = Modifier.size(size - 14.dp)) {
        val w = this.size.width
        val h = this.size.height

        // Occhi / sguardo
        val eyeColor = Color.White
        val eyeRadius = 2.dp.toPx()
        val leftEye = Offset(w * 0.35f, h * 0.42f)
        val rightEye = Offset(w * 0.65f, h * 0.42f)

        drawCircle(color = eyeColor, radius = eyeRadius, center = leftEye)
        drawCircle(color = eyeColor, radius = eyeRadius, center = rightEye)

        // Sopracciglia sagge / espressive
        val browColor = auraColor
        val browStroke = Stroke(width = 2.dp.toPx())
        when (mood) {
            MaestroMood.THINKING -> {
                drawLine(browColor, Offset(w * 0.28f, h * 0.32f), Offset(w * 0.42f, h * 0.36f), browStroke.width)
                drawLine(browColor, Offset(w * 0.72f, h * 0.32f), Offset(w * 0.58f, h * 0.36f), browStroke.width)
            }
            MaestroMood.WARNING -> {
                drawLine(browColor, Offset(w * 0.28f, h * 0.34f), Offset(w * 0.42f, h * 0.38f), browStroke.width)
                drawLine(browColor, Offset(w * 0.72f, h * 0.34f), Offset(w * 0.58f, h * 0.38f), browStroke.width)
            }
            else -> {
                drawLine(browColor, Offset(w * 0.28f, h * 0.34f), Offset(w * 0.42f, h * 0.34f), browStroke.width)
                drawLine(browColor, Offset(w * 0.58f, h * 0.34f), Offset(w * 0.72f, h * 0.34f), browStroke.width)
            }
        }

        // Barba caratteristica del Maestro
        val beardColor = Color(0xFFD1D5DB)
        drawLine(
            color = beardColor,
            start = Offset(w * 0.30f, h * 0.68f),
            end = Offset(w * 0.50f, h * 0.88f),
            strokeWidth = 2.5f.dp.toPx(),
        )
        drawLine(
            color = beardColor,
            start = Offset(w * 0.70f, h * 0.68f),
            end = Offset(w * 0.50f, h * 0.88f),
            strokeWidth = 2.5f.dp.toPx(),
        )
    }
}
