package it.maestro.app.data.local.academy

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import it.maestro.app.R
import it.maestro.app.ui.components.MaestroMood
import it.maestro.game.model.Card

/**
 * Identificativo univoco dei moduli dell'Accademia del Maestro (task F2-06).
 */
public enum class AcademyModuleId {
    MODULE_1_ABBECEDARIO,
    MODULE_2_PRIMIERA,
    MODULE_3_DISPARI,
}

/**
 * Tipologia di step all'interno di una lezione dell'Accademia.
 */
public enum class AcademyStepType {
    THEORY_EXPLANATION,
    INTERACTIVE_QUESTION,
    CARD_ACTION,
    SUMMARY_AND_BADGE,
}

/**
 * Opzione di risposta per un quesito didattico a scelta multipla.
 */
public data class AcademyOption(
    public val id: String,
    @StringRes public val textRes: Int,
    @StringRes public val feedbackRes: Int,
    public val isCorrect: Boolean,
)

/**
 * Singolo step didattico interattivo al tavolo del Maestro.
 */
public data class AcademyStep(
    public val id: String,
    public val stepNumber: Int,
    public val type: AcademyStepType,
    @StringRes public val titleRes: Int,
    public val maestroMood: MaestroMood,
    @StringRes public val maestroSpeechRes: Int,
    public val tableCards: List<Card> = emptyList(),
    public val handCards: List<Card> = emptyList(),
    public val cardBadges: Map<Card, Int> = emptyMap(),
    @StringRes public val tableBadgeRes: Int? = null,
    public val options: List<AcademyOption> = emptyList(),
    public val targetCard: Card? = null,
    @StringRes public val goldenRuleRes: Int? = null,
)

/**
 * Modulo formativo completo dell'Accademia con percorso di step progressivi.
 */
public data class AcademyModule(
    public val id: AcademyModuleId,
    public val number: Int,
    @StringRes public val titleRes: Int,
    @StringRes public val subtitleRes: Int,
    @StringRes public val descriptionRes: Int,
    @DrawableRes public val iconRes: Int,
    @StringRes public val goldenRuleRes: Int,
    public val steps: List<AcademyStep>,
)

/**
 * Stato di avanzamento dell'utente per un modulo dell'Accademia (task F2-06).
 */
public data class AcademyProgress(
    public val moduleId: AcademyModuleId,
    public val isUnlocked: Boolean = false,
    public val isCompleted: Boolean = false,
    public val completedAt: Long = 0L,
)
