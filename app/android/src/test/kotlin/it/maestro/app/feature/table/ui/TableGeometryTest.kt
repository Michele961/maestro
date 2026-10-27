package it.maestro.app.feature.table.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Task F1R-11: la geometria del tavolo regge le configurazioni di ADR-0016 senza tagli né sovrapposizioni.
 * Gli schermi qui sono lo spazio utile (dimensioni della matrice di screenshot).
 */
class TableGeometryTest {

    private val screens = mapOf(
        "small" to (360.dp to 640.dp),
        "medium" to (393.dp to 852.dp),
        "pixel10pro" to (410.dp to 914.dp),
        // Telefono alto e stretto, con barra di navigazione a gesti già sottratta.
        "tall" to (360.dp to 760.dp),
    )

    private fun eachScreen(block: (String, Dp, Dp, TableGeometry) -> Unit) =
        screens.forEach { (name, size) -> block(name, size.first, size.second, TableGeometry.from(size.first, size.second)) }

    @Test
    fun everyZoneStaysInsideTheScreen() = eachScreen { name, w, h, g ->
        g.zones.all.forEach { z ->
            assertTrue(z.x >= 0.dp && z.y >= 0.dp && z.right <= w && z.bottom <= h, "$name: zona fuori schermo $z")
        }
    }

    @Test
    fun zonesNeverOverlap() = eachScreen { name, _, _, g ->
        val zones = g.zones.all
        zones.forEachIndexed { i, a ->
            zones.drop(i + 1).forEach { b -> assertFalse(a.overlaps(b), "$name: $a si sovrappone a $b") }
        }
    }

    @Test
    fun maestroBubbleZoneNeverCoversHandOrTableCards() = eachScreen { name, _, _, g ->
        // Il fumetto del Maestro sta nella zona della corsa ai punti (F1R-13, `game-feel.md` §5).
        val bubble = g.zones.pointsRace
        assertFalse(bubble.overlaps(g.zones.hand), "$name: il fumetto copre la mano")
        assertFalse(bubble.overlaps(g.zones.playZone), "$name: il fumetto copre le carte a terra")
        assertFalse(bubble.overlaps(g.zones.opponentHand), "$name: il fumetto copre la mano avversaria")
        assertTrue(bubble.height >= 44.dp, "$name: fumetto troppo basso per due righe: ${bubble.height}")
    }

    @Test
    fun tableCardsStayReadableAndFitTwoRows() = eachScreen { name, _, _, g ->
        assertTrue(g.cardWidth >= TableGeometry.MIN_TABLE_CARD, "$name: carte a terra ${g.cardWidth}")
        assertTrue(g.cardHeight * 2 <= g.zones.playZone.height, "$name: due file non stanno nella zona di gioco")
    }

    @Test
    fun handCardsAreWholeAndBiggerThanTableCards() = eachScreen { name, _, _, g ->
        assertTrue(g.handCardHeight <= g.zones.hand.height, "$name: carte in mano tagliate")
        assertTrue(g.handCardWidth > g.cardWidth, "$name: la mano deve essere più grande del tavolo")
    }

    @Test
    fun opponentCardsFitTheirZone() = eachScreen { name, _, _, g ->
        assertTrue(g.opponentCardHeight <= g.zones.opponentHand.height, "$name: mano avversaria tagliata")
    }

    @Test
    fun biggerScreensGetBiggerCards() {
        val small = TableGeometry.from(360.dp, 640.dp)
        val pixel = TableGeometry.from(410.dp, 914.dp)
        assertTrue(pixel.cardWidth > small.cardWidth)
        assertTrue(pixel.handCardWidth > small.handCardWidth)
    }
}
