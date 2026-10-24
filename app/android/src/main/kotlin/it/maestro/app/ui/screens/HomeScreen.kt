package it.maestro.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.maestro.app.R

/**
 * Schermata iniziale di Maestro.
 * Tutti i testi visibili provengono rigorosamente dalle risorse stringhe (ADR-0007).
 */
@Composable
public fun HomeScreen(
    modifier: Modifier = Modifier,
    hasActiveMatch: Boolean = false,
    onResumeGameClicked: () -> Unit = {},
    onStartGameClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(id = R.string.home_welcome_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (hasActiveMatch) {
                Button(
                    onClick = onResumeGameClicked,
                    modifier = Modifier.testTag("resume_game_button"),
                ) {
                    Text(text = stringResource(id = R.string.home_resume_game))
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onStartGameClicked,
                    modifier = Modifier.testTag("new_game_button"),
                ) {
                    Text(text = stringResource(id = R.string.home_start_game))
                }
            } else {
                Button(
                    onClick = onStartGameClicked,
                    modifier = Modifier.testTag("new_game_button"),
                ) {
                    Text(text = stringResource(id = R.string.home_start_game))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onSettingsClicked,
                modifier = Modifier.testTag("home_settings_button"),
            ) {
                Text(text = stringResource(id = R.string.home_settings_button))
            }
        }
    }
}
