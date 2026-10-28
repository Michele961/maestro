package it.maestro.app.designsystem.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/** Vibrazione attiva? Fornito dalla radice dell'app dalle impostazioni (task F1R-31b); attiva di default. */
public val LocalHapticsEnabled: androidx.compose.runtime.ProvidableCompositionLocal<Boolean> =
    compositionLocalOf { true }

/** Restituisce l'azione che fa vibrare solo se la vibrazione è attiva nelle impostazioni. */
@Composable
public fun rememberGameHaptics(): (HapticFeedbackType) -> Unit {
    val enabled = LocalHapticsEnabled.current
    val haptic = LocalHapticFeedback.current
    return { type -> if (enabled) haptic.performHapticFeedback(type) }
}
