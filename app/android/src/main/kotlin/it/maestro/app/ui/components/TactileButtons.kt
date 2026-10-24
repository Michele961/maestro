package it.maestro.app.ui.components

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
    EDUCATIONAL_BLUE,
    SECONDARY_AMBER,
    WOOD,
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
        PillButtonVariant.PRIMARY_GREEN -> R.drawable.skin_button_green
        PillButtonVariant.EDUCATIONAL_BLUE -> R.drawable.skin_button_blue
        PillButtonVariant.SECONDARY_AMBER -> R.drawable.skin_button_amber
        PillButtonVariant.WOOD -> R.drawable.skin_button_wood
    }
    NineSliceBox(
        skin = skin,
        slice = 0.42f,
        corner = height * 0.42f,
        modifier = modifier
            .height(height)
            .alpha(if (enabled) 1f else 0.6f)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(contentPadding),
    ) {
        Text(
            text = text,
            color = ButtonTextCream,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = MaestroDisplayFont,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp,
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
        corner = 26.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 30.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScoreText(p1Label.uppercase(), ButtonTextCream, 18.sp)
            ScoreText(p1Score.toString(), Color(0xFF42A5F5), 30.sp, Modifier.padding(start = 8.dp))
            ScoreText("–", ButtonTextCream, 24.sp, Modifier.padding(horizontal = 8.dp))
            ScoreText(p2Score.toString(), Color(0xFFEF5350), 30.sp, Modifier.padding(end = 8.dp))
            ScoreText(p2Label.uppercase(), ButtonTextCream, 18.sp)
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
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
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




