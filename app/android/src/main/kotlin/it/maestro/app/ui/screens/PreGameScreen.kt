package it.maestro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.R

private val GoldAccentColor = Color(0xFFE9C46A)
private val SelectedCardColor = Color(0xFF264653)
private val UnselectedCardColor = Color(0xFF1E293B)

/**
 * Schermata di configurazione pre-partita (task F1-07).
 * Permette di selezionare il livello del bot e il punteggio obiettivo (11, 16, 21).
 * Tutti i testi provengono rigorosamente dalle risorse stringhe (ADR-0007).
 */
@Composable
public fun PreGameScreen(
    modifier: Modifier = Modifier,
    initialLevel: DifficultyLevel = DifficultyLevel.INTERMEDIO,
    initialTargetScore: Int = 11,
    onStartGame: (DifficultyLevel, Int) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit = {},
) {
    var selectedLevel by remember { mutableStateOf(initialLevel) }
    var selectedTargetScore by remember { mutableIntStateOf(initialTargetScore) }

    val levels = listOf(
        DifficultyLevel.PRINCIPIANTE to R.string.level_principiante,
        DifficultyLevel.DILETTANTE to R.string.level_dilettante,
        DifficultyLevel.INTERMEDIO to R.string.level_intermedio,
        DifficultyLevel.AVANZATO to R.string.level_avanzato,
        DifficultyLevel.MAESTRO to R.string.level_maestro,
    )

    val targetScores = listOf(
        11 to R.string.target_score_11,
        16 to R.string.target_score_16,
        21 to R.string.target_score_21,
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("pre_game_screen"),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.pre_game_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sezione Livello di Difficoltà
            Text(
                text = stringResource(id = R.string.pre_game_difficulty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("level_selector"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for ((level, stringResId) in levels) {
                    val isSelected = selectedLevel == level
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLevel = level }
                            .testTag("level_option_${level.name}")
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) GoldAccentColor else Color.DarkGray,
                                shape = RoundedCornerShape(8.dp),
                            ),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) SelectedCardColor else UnselectedCardColor,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(id = stringResId),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GoldAccentColor else Color.White,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sezione Punteggio Obiettivo
            Text(
                text = stringResource(id = R.string.pre_game_target_score_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("target_score_selector"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for ((target, stringResId) in targetScores) {
                    val isSelected = selectedTargetScore == target
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTargetScore = target }
                            .testTag("target_score_option_$target")
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) GoldAccentColor else Color.DarkGray,
                                shape = RoundedCornerShape(8.dp),
                            ),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) SelectedCardColor else UnselectedCardColor,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(id = stringResId),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GoldAccentColor else Color.White,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pulsanti di azione
            Button(
                onClick = { onStartGame(selectedLevel, selectedTargetScore) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("start_game_button"),
            ) {
                Text(
                    text = stringResource(id = R.string.pre_game_start_button),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("back_to_home_button"),
            ) {
                Text(
                    text = stringResource(id = R.string.pre_game_back_button),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
