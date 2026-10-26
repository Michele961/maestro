package it.maestro.app.ui.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.alpha
import it.maestro.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.ui.theme.MaestroDisplayFont
import it.maestro.app.ui.theme.ButtonAmberBottom
import it.maestro.app.ui.theme.ButtonAmberTop
import it.maestro.app.ui.theme.ButtonBlueBottom
import it.maestro.app.ui.theme.ButtonBlueTop
import it.maestro.app.ui.theme.ButtonGoldBorder
import it.maestro.app.ui.theme.ButtonGreenBottom
import it.maestro.app.ui.theme.ButtonGreenTop
import it.maestro.app.ui.theme.ModalBackgroundWarm
import it.maestro.app.ui.theme.WoodSurfaceDark

public enum class PillButtonVariant {
    HERO_RED,
    PRIMARY_GREEN,
    DARK_GREEN,
    EDUCATIONAL_BLUE,
    SECONDARY_AMBER,
    WOOD,
    CREAM,
}

/**
 * Pulsante tattile a pillola 3D con finitura satinata e rilievo morbido (Guida di Stile v1.0).
 */
@Composable
public fun MaestroPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PillButtonVariant = PillButtonVariant.PRIMARY_GREEN,
    enabled: Boolean = true,
    testTag: String? = null,
    height: Dp = 52.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
    fontSize: TextUnit = 17.sp,
) {
    // Pelli generate in F1-13 (docs/art/asset-prompts.md, set 1): rilievo, ottone e rivetti dei mockup.
    val skin = when (variant) {
        PillButtonVariant.HERO_RED -> R.drawable.skin_button_red
        PillButtonVariant.PRIMARY_GREEN, PillButtonVariant.DARK_GREEN -> R.drawable.skin_button_green
        PillButtonVariant.EDUCATIONAL_BLUE -> R.drawable.skin_button_blue
        PillButtonVariant.SECONDARY_AMBER -> R.drawable.skin_button_amber
        PillButtonVariant.WOOD -> R.drawable.skin_button_wood
        PillButtonVariant.CREAM -> R.drawable.skin_button_cream
    }
    val colorFilter = when (variant) {
        PillButtonVariant.DARK_GREEN -> ColorFilter.tint(Color(0xFF386638), BlendMode.Modulate)
        else -> null
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "pill_scale")
    val pressY by animateDpAsState(targetValue = if (isPressed) 2.dp else 0.dp, label = "pill_press_y")

    NineSliceBox(
        skin = skin,
        slice = 0.42f,
        corner = height * 0.42f,
        colorFilter = colorFilter,
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                translationY = pressY.toPx()
            }
            .alpha(if (enabled) 1f else 0.6f)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentPadding = contentPadding,
    ) {
        Text(
            text = text,
            color = if (variant == PillButtonVariant.CREAM) WoodSurfaceDark else ButtonTextCream,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = MaestroDisplayFont,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(shadow = Shadow(Color(0x99000000), Offset(0f, 3f), 4f)),
        )
    }
}

/** Testo crema dei pulsanti, come nei mockup. */
private val ButtonTextCream = Color(0xFFFFF4D6)

/**
 * Riquadro di dialogo del Maestro su cartoncino avorio caldo con cornice in ottone (Guida v1.0).
 */
@Composable
public fun MaestroDialogueBox(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Il Maestro",
    actionContent: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, shape),
        shape = shape,
        color = ModalBackgroundWarm,
        border = BorderStroke(2.dp, ButtonGoldBorder),
    ) {
        Box(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5A2B),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    Text(
                        text = message,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                    if (actionContent != null) {
                        Box(modifier = Modifier.padding(top = 8.dp)) {
                            actionContent()
                        }
                    }
                }
            }
        }
    }
}

/** Lato da cui esce la codina del [MaestroSpeechBubble]. */
public enum class BubbleTail { BOTTOM_START, BOTTOM_CENTER, BOTTOM_END, TOP_START, START }

/**
 * Fumetto compatto del Maestro come nei mockup (F1-26): carta avorio, testo centrato,
 * codina verso il ritratto, nessuna intestazione. Ingombro minimo: la larghezza va limitata
 * dal chiamante.
 */
@Composable
public fun MaestroSpeechBubble(
    message: String,
    modifier: Modifier = Modifier,
    tail: BubbleTail = BubbleTail.BOTTOM_START,
    fontSize: TextUnit = 14.sp,
    maxLines: Int = 6,
) {
    if (tail == BubbleTail.START) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .size(width = 10.dp, height = 16.dp)
                    .offset(x = 1.dp)
                    .zIndex(1f),
            ) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width, 0f)
                    lineTo(0f, size.height * 0.5f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(path, BubblePaper)
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BubblePaper,
                border = BorderStroke(1.dp, Color(0x55603813)),
                shadowElevation = 4.dp,
                modifier = Modifier.weight(1f, fill = false),
            ) {
                Box(
                    modifier = Modifier
                        .heightIn(max = 140.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = message,
                        color = WoodSurfaceDark,
                        fontSize = fontSize,
                        lineHeight = fontSize * 1.22f,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MaestroDisplayFont,
                        textAlign = TextAlign.Center,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
        return
    }

    val isTop = tail == BubbleTail.TOP_START
    val tailAlign = when (tail) {
        BubbleTail.BOTTOM_START, BubbleTail.TOP_START, BubbleTail.START -> Alignment.Start
        BubbleTail.BOTTOM_CENTER -> Alignment.CenterHorizontally
        BubbleTail.BOTTOM_END -> Alignment.End
    }
    val tailCanvas: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit = {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .align(tailAlign)
                .padding(horizontal = 22.dp)
                .size(width = 16.dp, height = 10.dp),
        ) {
            val path = androidx.compose.ui.graphics.Path().apply {
                if (isTop) {
                    moveTo(size.width * 0.25f, 0f)
                    lineTo(0f, size.height)
                    lineTo(size.width, size.height)
                } else {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width * 0.25f, size.height)
                }
                close()
            }
            drawPath(path, BubblePaper)
        }
    }

    androidx.compose.foundation.layout.Column(modifier = modifier) {
        if (isTop) {
            tailCanvas()
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = BubblePaper,
            border = BorderStroke(1.dp, Color(0x55603813)),
            shadowElevation = 4.dp,
        ) {
            Box(
                modifier = Modifier
                    .heightIn(max = 140.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = message,
                    color = WoodSurfaceDark,
                    fontSize = fontSize,
                    lineHeight = fontSize * 1.22f,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MaestroDisplayFont,
                    textAlign = TextAlign.Center,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
        if (!isTop) {
            tailCanvas()
        }
    }
}

private val BubblePaper = Color(0xFFF7EEDC)

/**
 * Targhetta orizzontale in legno/metallo divisa a scomparti per il punteggio: Noi [7] | Voi [3] (Guida v1.0).
 */
@Composable
public fun ScorePlaque(
    p1Score: Int,
    p2Score: Int,
    modifier: Modifier = Modifier,
    p1Label: String = "Noi",
    p2Label: String = "Voi",
) {
    // Targa d'ottone con rivetti (pelle generata), come "TU 4 – 2 AVV" del mockup del tavolo.
    NineSliceBox(
        skin = R.drawable.skin_score_plaque,
        slice = 0.4f,
        corner = 18.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScoreText(p1Label.uppercase(), ButtonTextCream, 14.sp)
            ScoreText(p1Score.toString(), Color(0xFF42A5F5), 22.sp, Modifier.padding(start = 6.dp))
            ScoreText("–", ButtonTextCream, 18.sp, Modifier.padding(horizontal = 6.dp))
            ScoreText(p2Score.toString(), Color(0xFFEF5350), 22.sp, Modifier.padding(end = 6.dp))
            ScoreText(p2Label.uppercase(), ButtonTextCream, 14.sp)
        }
    }
}

@Composable
private fun ScoreText(text: String, color: Color, size: TextUnit, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = color,
        fontSize = size,
        fontWeight = FontWeight.Bold,
        fontFamily = MaestroDisplayFont,
        modifier = modifier,
        style = TextStyle(shadow = Shadow(Color(0x99000000), Offset(0f, 2f), 3f)),
    )
}

/**
 * Selettore a pillola per opzioni multiple (es. 11/16/21 punti, modalità di gioco).
 */
@Composable
public fun TactileSegmentedPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    NineSliceBox(
        skin = if (selected) R.drawable.skin_button_green else R.drawable.skin_button_cream,
        slice = 0.42f,
        corner = 18.dp,
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = text,
            color = if (selected) ButtonTextCream else WoodSurfaceDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MaestroDisplayFont,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Targhetta orizzontale in ottone/legno noce con rivetti dorati per le intestazioni (es. PREPARATI A GIOCARE).
 */
@Composable
public fun BrassHeaderPlaque(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    NineSliceBox(
        skin = R.drawable.skin_plaque_wood,
        slice = 0.3f,
        corner = 22.dp,
        modifier = modifier.padding(horizontal = 0.dp),
    ) {
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    color = Color(0xFFFFE082),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MaestroDisplayFont,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Color(0xFFD7CCC8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}




