package it.maestro.app.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
) {
    val gradient = when (variant) {
        PillButtonVariant.PRIMARY_GREEN -> Brush.verticalGradient(
            listOf(ButtonGreenTop, ButtonGreenBottom),
        )
        PillButtonVariant.EDUCATIONAL_BLUE -> Brush.verticalGradient(
            listOf(ButtonBlueTop, ButtonBlueBottom),
        )
        PillButtonVariant.SECONDARY_AMBER -> Brush.verticalGradient(
            listOf(ButtonAmberTop, ButtonAmberBottom),
        )
        PillButtonVariant.WOOD -> Brush.verticalGradient(
            listOf(Color(0xFF5A3821), Color(0xFF3B2213)),
        )
    }

    val borderColor = when (variant) {
        PillButtonVariant.PRIMARY_GREEN, PillButtonVariant.SECONDARY_AMBER -> ButtonGoldBorder
        PillButtonVariant.EDUCATIONAL_BLUE -> Color(0xFF64B5F6)
        PillButtonVariant.WOOD -> ButtonGoldBorder.copy(alpha = 0.6f)
    }

    val shape = RoundedCornerShape(26.dp)

    Surface(
        modifier = modifier
            .height(height)
            .shadow(elevation = if (enabled) 6.dp else 1.dp, shape = shape)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = shape,
        border = BorderStroke(width = 2.dp, color = if (enabled) borderColor else Color.Gray),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

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
    val plaqueShape = RoundedCornerShape(20.dp)
    val scoreBadgeShape = RoundedCornerShape(6.dp)

    Surface(
        modifier = modifier.shadow(4.dp, plaqueShape),
        shape = plaqueShape,
        color = WoodSurfaceDark,
        border = BorderStroke(1.5.dp, ButtonGoldBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = p1Label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp),
            )
            Box(
                modifier = Modifier
                    .clip(scoreBadgeShape)
                    .background(Color(0xFF1E88E5))
                    .padding(horizontal = 10.dp, vertical = 2.dp),
            ) {
                Text(
                    text = p1Score.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }

            Box(modifier = Modifier.padding(horizontal = 10.dp)) {
                Text(text = "•", color = ButtonGoldBorder, fontSize = 16.sp)
            }

            Text(
                text = p2Label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp),
            )
            Box(
                modifier = Modifier
                    .clip(scoreBadgeShape)
                    .background(Color(0xFFD32F2F))
                    .padding(horizontal = 10.dp, vertical = 2.dp),
            ) {
                Text(
                    text = p2Score.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}
