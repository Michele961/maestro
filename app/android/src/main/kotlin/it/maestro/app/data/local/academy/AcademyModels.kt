package it.maestro.app.data.local.academy

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import it.maestro.app.ui.components.MaestroMood
import it.maestro.game.model.Card

/**
 * Identificativo univoco dei capitoli (macro-moduli) dell'Accademia del Maestro.
 */
public enum class AcademyChapterId {
    CHAPTER_1_FONDAMENTI,
    CHAPTER_2_PRIMIERA,
    CHAPTER_3_DIFESA,
    CHAPTER_4_MAESTRIA,
}

/**
 * Identificativo univoco di ciascuna lezione (sotto-modulo) dell'Accademia.
 */
public enum class AcademyLessonId {
    // Capitolo 1: I Fondamenti del Circolo
    LESSON_1_1_PRESA_SINGOLA,
    LESSON_1_2_PUNTI_MAZZO,
    LESSON_1_3_SETTEBELLO,
    LESSON_1_4_SCELTA_SOMME,

    // Capitolo 2: La Scienza della Primiera
    LESSON_2_1_SCALA_VALORI,
    LESSON_2_2_FORMULA_SEMI,
    LESSON_2_3_TRAPPOLA_SEME_VUOTO,
    LESSON_2_4_CONTA_SETTE_SEI,

    // Capitolo 3: Difesa del Banco & Antiscopa
    LESSON_3_1_SOGLIA_DIECI,
    LESSON_3_2_BLINDARE_BANCO,
    LESSON_3_3_REGOLA_DISPARI,
    LESSON_3_4_ULTIMA_MANO,

    // Capitolo 4: Visione da Maestro (Memoria & Endgame)
    LESSON_4_1_MEMORIA_SETTE,
    LESSON_4_2_DEDUZIONE_MANO,
    LESSON_4_3_ENDGAME_MAZZO_VUOTO,
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
 * Lezione (sotto-modulo) formativa dell'Accademia.
 */
public data class AcademyLesson(
    public val id: AcademyLessonId,
    public val chapterId: AcademyChapterId,
    public val lessonNumber: Int,
    @StringRes public val titleRes: Int,
    @StringRes public val subtitleRes: Int,
    @StringRes public val descriptionRes: Int,
    @DrawableRes public val iconRes: Int,
    @StringRes public val goldenRuleRes: Int,
    public val steps: List<AcademyStep>,
)

/**
 * Capitolo (macro-modulo) formativo dell'Accademia contenente più lezioni tematiche.
 */
public data class AcademyChapter(
    public val id: AcademyChapterId,
    public val number: Int,
    @StringRes public val titleRes: Int,
    @StringRes public val subtitleRes: Int,
    @StringRes public val descriptionRes: Int,
    @DrawableRes public val iconRes: Int,
    @DrawableRes public val badgeRes: Int,
    public val lessons: List<AcademyLesson>,
)

/**
 * Stato di avanzamento dell'utente per una lezione dell'Accademia.
 */
public data class AcademyLessonProgress(
    public val lessonId: AcademyLessonId,
    public val isUnlocked: Boolean = false,
    public val isCompleted: Boolean = false,
    public val completedAt: Long = 0L,
)
