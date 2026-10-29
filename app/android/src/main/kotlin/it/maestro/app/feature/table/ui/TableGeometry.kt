package it.maestro.app.feature.table.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min

/** Rettangolo in dp, con origine in alto a sinistra dello spazio utile del tavolo. */
public data class ZoneRect(val x: Dp, val y: Dp, val width: Dp, val height: Dp) {
    val right: Dp get() = x + width
    val bottom: Dp get() = y + height

    /** `true` se i due rettangoli hanno un'area in comune. */
    public fun overlaps(other: ZoneRect): Boolean =
        x < other.right && other.x < right && y < other.bottom && other.y < bottom
}

/** Zone fisse del tavolo (`docs/app/game-feel.md` §4). */
public data class TableZones(
    val opponentBar: ZoneRect,
    val opponentHand: ZoneRect,
    val opponentPile: ZoneRect,
    val deck: ZoneRect,
    val playZone: ZoneRect,
    val pointsRace: ZoneRect,
    val maestro: ZoneRect,
    val humanPile: ZoneRect,
    val hand: ZoneRect,
) {
    /** Tutte le zone, per i controlli di geometria. */
    val all: List<ZoneRect>
        get() = listOf(opponentBar, opponentHand, opponentPile, deck, playZone, pointsRace, maestro, humanPile, hand)
}

/**
 * Tutte le misure del tavolo, ricavate dallo spazio utile (ADR-0016, `docs/app/responsive-ui.md` §2–3,
 * task F1R-11). I composable del tavolo le ricevono e non inventano numeri.
 */
public data class TableGeometry(
    val cardWidth: Dp,
    val handCardWidth: Dp,
    val opponentCardWidth: Dp,
    val pileWidth: Dp,
    val maestroSize: Dp,
    val zones: TableZones,
) {
    val cardHeight: Dp get() = cardWidth * CARD_RATIO
    val handCardHeight: Dp get() = handCardWidth * CARD_RATIO
    val opponentCardHeight: Dp get() = opponentCardWidth * CARD_RATIO

    public companion object {
        /** Rapporto altezza/larghezza delle carte napoletane (ADR-0013). */
        public const val CARD_RATIO: Float = 1.72f

        /** Larghezza minima leggibile di una carta a terra. */
        public val MIN_TABLE_CARD: Dp = 56.dp

        private val GAP = 8.dp

        /** Nella revisione la fascia sopra la mano è più stretta: lì non si trascina, si tocca. */
        private val REVIEW_CONTROLS_STRIP = 28.dp

        /** Fascia sopra le carte della mano, sempre riservata: ci stanno «Gioca» e «Trascina sul tavolo» senza muovere nulla. */
        public val CONTROLS_STRIP: Dp = 48.dp

        /** Geometria per uno spazio utile di [width] × [height] (già al netto degli inset di sistema). */
        public fun from(width: Dp, height: Dp, review: Boolean = false): TableGeometry {
            val opponentCard = (width * 0.13f).coerceIn(40.dp, 64.dp)
            val pile = (width * 0.15f).coerceIn(48.dp, 72.dp)
            val maestro = (width * 0.14f).coerceIn(44.dp, 72.dp)
            val barHeight = if (review) 0.dp else (height * 0.07f).coerceIn(40.dp, 64.dp)
            val opponentHandHeight = opponentCard * CARD_RATIO

            // La mano occupa il 24% dell'altezza e le carte restano intere al suo interno.
            val cardsHeight = height * (if (review) 0.22f else 0.24f)
            val handHeight = cardsHeight + (if (review) REVIEW_CONTROLS_STRIP else CONTROLS_STRIP)
            val handCard = min((width * 0.26f).coerceIn(84.dp, 128.dp), (cardsHeight - GAP) / CARD_RATIO)
            // La zona del Maestro è più alta del ritratto quando c'è spazio: il fumetto ci sta per intero (4-5 righe).
            val statusHeight = if (review) 0.dp else (height * 0.11f).coerceIn(maestro, 92.dp)

            // La zona di gioco prende lo spazio che resta (al massimo il 42% dell'altezza).
            val fixed = barHeight + opponentHandHeight + statusHeight + handHeight + GAP * 5
            val playHeight = min(height * (if (review) 0.5f else 0.42f), height - fixed)
            val playWidth = width - pile - GAP
            // Due file di carte a terra devono stare nella zona di gioco.
            val tableCard = min(
                (width * 0.19f).coerceIn(MIN_TABLE_CARD, 96.dp),
                (playHeight - GAP) / (2 * CARD_RATIO),
            )

            // Spazio verticale avanzato: distribuito tra le zone, così il tavolo resta centrato.
            val spare = (height - fixed - playHeight).coerceAtLeast(0.dp) / 5
            var y = 0.dp
            val bar = ZoneRect(0.dp, y, width, barHeight)
            y += barHeight + GAP + spare
            val opponentHand = ZoneRect(0.dp, y, width - pile - GAP, opponentHandHeight)
            val opponentPile = ZoneRect(width - pile, y, pile, opponentHandHeight)
            y += opponentHandHeight + GAP + spare
            val deck = ZoneRect(0.dp, y, pile, playHeight)
            val play = ZoneRect(pile + GAP, y, playWidth, playHeight)
            y += playHeight + GAP + spare
            val race = ZoneRect(0.dp, y, width - maestro - GAP, statusHeight)
            val maestroZone = ZoneRect(width - maestro, y, maestro, statusHeight)
            y += statusHeight + GAP + spare
            val humanPile = ZoneRect(0.dp, y, pile, handHeight)
            val hand = ZoneRect(pile + GAP, y, width - pile - GAP, handHeight)

            return TableGeometry(
                cardWidth = tableCard,
                handCardWidth = handCard,
                opponentCardWidth = opponentCard,
                pileWidth = pile,
                maestroSize = maestro,
                zones = TableZones(bar, opponentHand, opponentPile, deck, play, race, maestroZone, humanPile, hand),
            )
        }
    }
}
