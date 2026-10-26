package it.maestro.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.ui.theme.MaestroDisplayFont
import it.maestro.app.ui.theme.BrassRivetGold
import it.maestro.app.ui.theme.ButtonBlueBottom
import it.maestro.app.ui.theme.ButtonGoldBorder
import it.maestro.app.ui.theme.WoodSurfaceDark
import it.maestro.app.ui.theme.WoodSurfaceLight

private val BrassShadow = Color(0xFF8C6D1F)

/**
 * Sfondo del circolo: legno a tutto schermo e, in basso, il panno verde per [feltFraction] dell'altezza
 * (0 = solo legno, 1 = solo panno). Base comune delle schermate (piano UI, F1-18/F1-19).
 */
@Composable
public fun ClubBackground(
    modifier: Modifier = Modifier,
    feltFraction: Float = 0f,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().background(WoodSurfaceDark)) {
        Image(
            painter = painterResource(R.drawable.texture_wood),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (feltFraction > 0f) {
            Image(
                painter = painterResource(R.drawable.table_felt_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(feltFraction).align(Alignment.BottomCenter),
            )
        }
        content()
    }
}

/**
 * Pannello su pergamena con bordo in ottone e fregi dorati (asset `ornament_*`, F1-13).
 * Il pannello si adatta a qualsiasi misura: i fregi si appoggiano sopra, non vengono stirati.
 */
@Composable
public fun ParchmentPanel(
    modifier: Modifier = Modifier,
    showTopOrnament: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier) {
        // Pelle generata (set 2): cornice d'ottone con angolari, pergamena all'interno.
        NineSliceBox(
            skin = R.drawable.skin_panel_parchment,
            slice = 0.24f,
            corner = 44.dp,
            modifier = Modifier.matchParentSize(),
        )
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp), content = content)
        if (showTopOrnament) {
            Image(
                painter = painterResource(R.drawable.ornament_top),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-22).dp)
                    .fillMaxWidth(0.55f),
            )
        }
    }
}

@Composable
private fun BoxScope.Corners() {
    val size = 30.dp
    val corner = painterResource(R.drawable.ornament_corner)
    // L'asset è l'angolo in alto a destra: gli altri tre si ottengono ribaltandolo.
    Image(corner, null, Modifier.align(Alignment.TopEnd).size(size))
    Image(corner, null, Modifier.align(Alignment.TopStart).size(size).scale(scaleX = -1f, scaleY = 1f))
    Image(corner, null, Modifier.align(Alignment.BottomEnd).size(size).scale(scaleX = 1f, scaleY = -1f))
    Image(corner, null, Modifier.align(Alignment.BottomStart).size(size).scale(-1f))
}

/**
 * Pannello in legno con filo d'ottone, per righe e riquadri secondari del menu.
 */
@Composable
public fun WoodPanel(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    NineSliceBox(
        skin = R.drawable.skin_plaque_wood,
        slice = 0.3f,
        corner = 20.dp,
        modifier = modifier,
        contentAlignment = Alignment.TopStart,
        content = content,
    )
}

/**
 * Ritratto senza cornice (avatar o Maestro) ritagliato in un cerchio d'ottone.
 * Le immagini sono generate senza cornice (F1-13): cerchio e bordo li aggiunge il codice.
 */
@Composable
public fun FramedPortrait(
    @DrawableRes portrait: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    background: Color = ButtonBlueBottom,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(BrassRivetGold, ButtonGoldBorder, BrassShadow)))
            .padding(size * 0.05f)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Image(
            painter = painterResource(portrait),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(0.92f),
        )
    }
}

/**
 * Icona illustrata (asset `icon_*`) dentro un cerchio blu con anello d'ottone, con etichetta sotto.
 * Senza [onClick] è solo decorativa.
 */
@Composable
public fun BrassIconBadge(
    @DrawableRes icon: Int,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(BrassRivetGold, ButtonGoldBorder, BrassShadow)))
                .padding(4.dp)
                .clip(CircleShape)
                .background(ButtonBlueBottom),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(icon), contentDescription = null, modifier = Modifier.fillMaxSize(0.72f))
        }
        Text(
            text = label.uppercase(),
            color = BrassRivetGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MaestroDisplayFont,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/**
 * Contatore a pillola con icona (monete, gemme) per l'intestazione del menu.
 */
@Composable
public fun CurrencyPill(
    @DrawableRes icon: Int,
    value: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50)
    Box(modifier = modifier.height(36.dp), contentAlignment = Alignment.CenterStart) {
        Box(
            modifier = Modifier
                .padding(start = 14.dp)
                .height(30.dp)
                .clip(shape)
                .background(Color(0xFF2A170C))
                .border(2.dp, ButtonGoldBorder, shape)
                .padding(start = 26.dp, end = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = MaestroDisplayFont)
        }
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(36.dp))
    }
}

/**
 * Pulsante quadrato in legno e ottone con un'icona illustrata (messaggi, impostazioni).
 */
@Composable
public fun WoodIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(10.dp)
    Surface(
        modifier = modifier
            .size(44.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = WoodSurfaceLight,
        border = BorderStroke(2.dp, ButtonGoldBorder),
        shadowElevation = 4.dp,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            modifier = Modifier.padding(6.dp),
        )
    }
}

/**
 * Riga in legno con icona e titolo (es. "Notizie dal circolo"); decorativa finché la funzione non esiste.
 */
@Composable
public fun WoodRowTile(
    @DrawableRes icon: Int?,
    title: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
    onClick: (() -> Unit)? = null,
) {
    WoodPanel(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(iconSize))
            }
            Text(
                text = title.uppercase(),
                color = BrassRivetGold,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MaestroDisplayFont,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            )
        }
    }
}
