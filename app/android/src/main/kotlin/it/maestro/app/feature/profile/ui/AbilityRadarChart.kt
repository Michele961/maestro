package it.maestro.app.feature.profile.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.ai.mastery.AbilityRadar
import it.maestro.app.R
import it.maestro.app.designsystem.theme.BrassRivetGold
import it.maestro.app.designsystem.theme.MaestroDisplayFont
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val AXIS_COUNT = 5
private const val GRID_LEVELS = 5
private const val START_ANGLE_RAD = -PI / 2.0 // Punta in alto

private val WebGridColor = Color(0x66D4AF37)
private val SpokeColor = Color(0x55D4AF37)
private val PolygonFillColor = Color(0x552A9D8F)
private val PolygonStrokeColor = Color(0xFFFFD54F)
private val NodeDotColor = Color(0xFFE9C46A)
private val NodeBorderColor = Color(0xFFFFFFFF)

/**
 * Grafico Radar a 5 assi renderizzato con Canvas Compose nativo (task F2-04, `docs/vision.md` §5.A).
 * Visualizza i parametri: Memoria Sette, Controllo Denari, Primiera, Gestione Banco, Endgame.
 */
@Composable
public fun AbilityRadarChart(
    radar: AbilityRadar,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .testTag("ability_radar_chart")
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        val sizeDp = min(maxWidth.value, maxHeight.value).dp
        val chartSize = if (sizeDp > 0.dp) sizeDp else 280.dp

        Box(
            modifier = Modifier.size(chartSize),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize().testTag("radar_canvas")) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (min(size.width, size.height) / 2f) * 0.65f

                // 1. Disegna i 5 pentagoni concentrici della griglia di riferimento (20%, 40%, 60%, 80%, 100%)
                for (level in 1..GRID_LEVELS) {
                    val levelRadius = radius * (level.toFloat() / GRID_LEVELS)
                    val gridPath = Path()
                    for (i in 0 until AXIS_COUNT) {
                        val angle = START_ANGLE_RAD + i * (2 * PI / AXIS_COUNT)
                        val x = center.x + (levelRadius * cos(angle)).toFloat()
                        val y = center.y + (levelRadius * sin(angle)).toFloat()
                        if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                    }
                    gridPath.close()
                    drawPath(
                        path = gridPath,
                        color = WebGridColor,
                        style = Stroke(width = if (level == GRID_LEVELS) 2.dp.toPx() else 1.dp.toPx()),
                    )
                }

                // 2. Raggi radiali (Spokes) dal centro ai vertici esterni
                for (i in 0 until AXIS_COUNT) {
                    val angle = START_ANGLE_RAD + i * (2 * PI / AXIS_COUNT)
                    val endX = center.x + (radius * cos(angle)).toFloat()
                    val endY = center.y + (radius * sin(angle)).toFloat()
                    drawLine(
                        color = SpokeColor,
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                }

                // 3. Poligono dei dati del giocatore
                val values = listOf(
                    radar.sevens.toFloat() / 100f,
                    radar.denari.toFloat() / 100f,
                    radar.primiera.toFloat() / 100f,
                    radar.board.toFloat() / 100f,
                    radar.endgame.toFloat() / 100f,
                )

                val dataPath = Path()
                val nodePoints = mutableListOf<Offset>()

                for (i in 0 until AXIS_COUNT) {
                    val angle = START_ANGLE_RAD + i * (2 * PI / AXIS_COUNT)
                    val valueRadius = radius * values[i].coerceIn(0.05f, 1f)
                    val px = center.x + (valueRadius * cos(angle)).toFloat()
                    val py = center.y + (valueRadius * sin(angle)).toFloat()
                    val point = Offset(px, py)
                    nodePoints.add(point)
                    if (i == 0) dataPath.moveTo(px, py) else dataPath.lineTo(px, py)
                }
                dataPath.close()

                // Riempimento semi-trasparente
                drawPath(path = dataPath, color = PolygonFillColor, style = Fill)
                // Bordo dorato del poligono
                drawPath(
                    path = dataPath,
                    color = PolygonStrokeColor,
                    style = Stroke(width = 3.dp.toPx()),
                )

                // 4. Rivetti/nodi dorati sui vertici dei dati
                for (pt in nodePoints) {
                    drawCircle(color = NodeBorderColor, radius = 5.dp.toPx(), center = pt)
                    drawCircle(color = NodeDotColor, radius = 3.5.dp.toPx(), center = pt)
                }
            }

            // Etichette dei 5 assi posizionate attorno al Canvas
            AxisLabel(
                title = stringResource(R.string.radar_axis_sevens),
                value = "${radar.sevens}%",
                testTag = "radar_axis_sevens",
                modifier = Modifier.align(Alignment.TopCenter).offset(y = (-4).dp),
            )
            AxisLabel(
                title = stringResource(R.string.radar_axis_denari),
                value = "${radar.denari}%",
                testTag = "radar_axis_denari",
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = 46.dp),
            )
            AxisLabel(
                title = stringResource(R.string.radar_axis_primiera),
                value = "${radar.primiera}%",
                testTag = "radar_axis_primiera",
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = 2.dp),
            )
            AxisLabel(
                title = stringResource(R.string.radar_axis_board),
                value = "${radar.board}%",
                testTag = "radar_axis_board",
                modifier = Modifier.align(Alignment.BottomStart).offset(x = 4.dp, y = 2.dp),
            )
            AxisLabel(
                title = stringResource(R.string.radar_axis_endgame),
                value = "${radar.endgame}%",
                testTag = "radar_axis_endgame",
                modifier = Modifier.align(Alignment.TopStart).offset(x = (-6).dp, y = 46.dp),
            )
        }
    }
}

@Composable
private fun AxisLabel(
    title: String,
    value: String,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title.uppercase(),
            color = BrassRivetGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MaestroDisplayFont,
            textAlign = TextAlign.Center,
        )
        Text(
            text = value,
            color = Color(0xFFFFD54F),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
    }
}
