package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.app.R
import it.maestro.app.ui.components.BrassHeaderPlaque
import it.maestro.app.ui.components.BrassIconBadge
import it.maestro.app.ui.components.ClubBackground
import it.maestro.app.ui.components.CurrencyPill
import it.maestro.app.ui.components.FramedPortrait
import it.maestro.app.ui.components.MaestroDialogueBox
import it.maestro.app.ui.components.MaestroPillButton
import it.maestro.app.ui.components.ParchmentPanel
import it.maestro.app.ui.components.PillButtonVariant
import it.maestro.app.ui.components.ScorePlaque
import it.maestro.app.ui.components.TactileSegmentedPill
import it.maestro.app.ui.components.WoodIconButton
import it.maestro.app.ui.components.WoodRowTile
import it.maestro.app.ui.theme.MaestroTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Catalogo dei componenti UI condivisi (F1-19): un'unica immagine con tutti i componenti
 * della libreria, per verificarne l'aspetto in un colpo d'occhio a ogni modifica.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "it-w411dp-h960dp-xxhdpi", application = Application::class)
class ComponentCatalogScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun component_catalog_screenshot() {
        composeRule.setContent {
            MaestroTheme {
                ClubBackground(feltFraction = 0.4f) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Titolo display", style = MaterialTheme.typography.headlineMedium)
                        PillButtonVariant.entries.forEach { variant ->
                            MaestroPillButton(text = variant.name, onClick = {}, variant = variant, modifier = Modifier.fillMaxWidth())
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TactileSegmentedPill(text = "11 punti", selected = true, onClick = {})
                            TactileSegmentedPill(text = "16 punti", selected = false, onClick = {})
                            ScorePlaque(p1Score = 4, p2Score = 2)
                        }
                        BrassHeaderPlaque(title = "PREPARATI A GIOCARE", subtitle = "Sottotitolo")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CurrencyPill(icon = R.drawable.icon_coin, value = "12.450")
                            CurrencyPill(icon = R.drawable.icon_gem, value = "280")
                            WoodIconButton(icon = R.drawable.icon_gear, contentDescription = "")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                R.drawable.maestro_neutral,
                                R.drawable.maestro_thinking,
                                R.drawable.maestro_pleased,
                                R.drawable.maestro_warning,
                                R.drawable.maestro_celebrating,
                            ).forEach { FramedPortrait(portrait = it, size = 64.dp) }
                        }
                        ParchmentPanel(showTopOrnament = true, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                            MaestroDialogueBox(message = "Fumetto del Maestro su pergamena con fregi.")
                        }
                        WoodRowTile(icon = R.drawable.icon_newspaper, title = "Riga in legno", modifier = Modifier.fillMaxWidth().height(52.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            BrassIconBadge(icon = R.drawable.icon_trophy, label = "Badge")
                            BrassIconBadge(icon = R.drawable.icon_book_quill, label = "Badge")
                            BrassIconBadge(icon = R.drawable.icon_pouch, label = "Badge")
                        }
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/component_catalog.png")
    }
}
