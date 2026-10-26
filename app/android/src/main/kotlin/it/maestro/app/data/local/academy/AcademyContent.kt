package it.maestro.app.data.local.academy

import it.maestro.app.R
import it.maestro.app.ui.components.MaestroMood
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit

/**
 * Contenuti didattici ufficiali dei primi 3 moduli interattivi dell'Accademia del Maestro (task F2-06).
 */
public object AcademyContent {

    public val MODULES: List<AcademyModule> = listOf(
        // =========================================================================
        // MODULO 1: L'Abbecedario del Circolo
        // =========================================================================
        AcademyModule(
            id = AcademyModuleId.MODULE_1_ABBECEDARIO,
            number = 1,
            titleRes = R.string.academy_module_1_title,
            subtitleRes = R.string.academy_module_1_subtitle,
            descriptionRes = R.string.academy_module_1_desc,
            iconRes = R.drawable.icon_book_quill,
            goldenRuleRes = R.string.academy_module_1_rule,
            steps = listOf(
                AcademyStep(
                    id = "m1_s1_single_rule",
                    stepNumber = 1,
                    type = AcademyStepType.INTERACTIVE_QUESTION,
                    titleRes = R.string.academy_m1_step1_title,
                    maestroMood = MaestroMood.THINKING,
                    maestroSpeechRes = R.string.academy_m1_step1_speech,
                    tableCards = listOf(
                        Card(Rank.SETTE, Suit.SPADE),
                        Card(Rank.QUATTRO, Suit.COPPE),
                        Card(Rank.TRE, Suit.BASTONI),
                    ),
                    handCards = listOf(
                        Card(Rank.SETTE, Suit.DENARI),
                    ),
                    tableBadgeRes = R.string.academy_m1_step1_badge,
                    options = listOf(
                        AcademyOption(
                            id = "m1_s1_opt1",
                            textRes = R.string.academy_m1_step1_opt1,
                            feedbackRes = R.string.academy_m1_step1_fb1,
                            isCorrect = true,
                        ),
                        AcademyOption(
                            id = "m1_s1_opt2",
                            textRes = R.string.academy_m1_step1_opt2,
                            feedbackRes = R.string.academy_m1_step1_fb2,
                            isCorrect = false,
                        ),
                    ),
                ),
                AcademyStep(
                    id = "m1_s2_settebello",
                    stepNumber = 2,
                    type = AcademyStepType.CARD_ACTION,
                    titleRes = R.string.academy_m1_step2_title,
                    maestroMood = MaestroMood.ENCOURAGING,
                    maestroSpeechRes = R.string.academy_m1_step2_speech,
                    tableCards = listOf(
                        Card(Rank.SETTE, Suit.DENARI),
                        Card(Rank.CINQUE, Suit.SPADE),
                        Card(Rank.DUE, Suit.COPPE),
                    ),
                    handCards = listOf(
                        Card(Rank.SETTE, Suit.BASTONI),
                        Card(Rank.QUATTRO, Suit.COPPE),
                    ),
                    cardBadges = mapOf(
                        Card(Rank.SETTE, Suit.DENARI) to R.string.academy_badge_plus_3_points,
                    ),
                    tableBadgeRes = R.string.academy_m1_step2_badge,
                    targetCard = Card(Rank.SETTE, Suit.BASTONI),
                ),
                AcademyStep(
                    id = "m1_s3_sums_choice",
                    stepNumber = 3,
                    type = AcademyStepType.INTERACTIVE_QUESTION,
                    titleRes = R.string.academy_m1_step3_title,
                    maestroMood = MaestroMood.NEUTRAL,
                    maestroSpeechRes = R.string.academy_m1_step3_speech,
                    tableCards = listOf(
                        Card(Rank.CINQUE, Suit.SPADE),
                        Card(Rank.DUE, Suit.SPADE),
                        Card(Rank.QUATTRO, Suit.DENARI),
                        Card(Rank.TRE, Suit.DENARI),
                    ),
                    handCards = listOf(
                        Card(Rank.SETTE, Suit.COPPE),
                    ),
                    tableBadgeRes = R.string.academy_m1_step3_badge,
                    options = listOf(
                        AcademyOption(
                            id = "m1_s3_opt1",
                            textRes = R.string.academy_m1_step3_opt1,
                            feedbackRes = R.string.academy_m1_step3_fb1,
                            isCorrect = true,
                        ),
                        AcademyOption(
                            id = "m1_s3_opt2",
                            textRes = R.string.academy_m1_step3_opt2,
                            feedbackRes = R.string.academy_m1_step3_fb2,
                            isCorrect = false,
                        ),
                    ),
                ),
                AcademyStep(
                    id = "m1_s4_summary",
                    stepNumber = 4,
                    type = AcademyStepType.SUMMARY_AND_BADGE,
                    titleRes = R.string.academy_m1_step4_title,
                    maestroMood = MaestroMood.TRIUMPHANT,
                    maestroSpeechRes = R.string.academy_m1_step4_speech,
                    goldenRuleRes = R.string.academy_module_1_rule,
                ),
            ),
        ),

        // =========================================================================
        // MODULO 2: Il Segreto della Primiera
        // =========================================================================
        AcademyModule(
            id = AcademyModuleId.MODULE_2_PRIMIERA,
            number = 2,
            titleRes = R.string.academy_module_2_title,
            subtitleRes = R.string.academy_module_2_subtitle,
            descriptionRes = R.string.academy_module_2_desc,
            iconRes = R.drawable.icon_shield_fleur,
            goldenRuleRes = R.string.academy_module_2_rule,
            steps = listOf(
                AcademyStep(
                    id = "m2_s1_king_paradox",
                    stepNumber = 1,
                    type = AcademyStepType.INTERACTIVE_QUESTION,
                    titleRes = R.string.academy_m2_step1_title,
                    maestroMood = MaestroMood.THINKING,
                    maestroSpeechRes = R.string.academy_m2_step1_speech,
                    tableCards = listOf(
                        Card(Rank.RE, Suit.BASTONI),
                        Card(Rank.SEI, Suit.COPPE),
                    ),
                    cardBadges = mapOf(
                        Card(Rank.RE, Suit.BASTONI) to R.string.academy_badge_king_10_pt,
                        Card(Rank.SEI, Suit.COPPE) to R.string.academy_badge_six_18_pt,
                    ),
                    tableBadgeRes = R.string.academy_m2_step1_badge,
                    options = listOf(
                        AcademyOption(
                            id = "m2_s1_opt1",
                            textRes = R.string.academy_m2_step1_opt1,
                            feedbackRes = R.string.academy_m2_step1_fb1,
                            isCorrect = true,
                        ),
                        AcademyOption(
                            id = "m2_s1_opt2",
                            textRes = R.string.academy_m2_step1_opt2,
                            feedbackRes = R.string.academy_m2_step1_fb2,
                            isCorrect = false,
                        ),
                    ),
                ),
                AcademyStep(
                    id = "m2_s2_close_suit",
                    stepNumber = 2,
                    type = AcademyStepType.CARD_ACTION,
                    titleRes = R.string.academy_m2_step2_title,
                    maestroMood = MaestroMood.ENCOURAGING,
                    maestroSpeechRes = R.string.academy_m2_step2_speech,
                    tableCards = listOf(
                        Card(Rank.CINQUE, Suit.BASTONI),
                        Card(Rank.RE, Suit.DENARI),
                    ),
                    handCards = listOf(
                        Card(Rank.CINQUE, Suit.SPADE),
                        Card(Rank.RE, Suit.COPPE),
                    ),
                    cardBadges = mapOf(
                        Card(Rank.CINQUE, Suit.BASTONI) to R.string.academy_badge_plus_15_pt,
                        Card(Rank.RE, Suit.DENARI) to R.string.academy_badge_plus_10_pt,
                    ),
                    tableBadgeRes = R.string.academy_m2_step2_badge,
                    targetCard = Card(Rank.CINQUE, Suit.SPADE),
                ),
                AcademyStep(
                    id = "m2_s3_sevens_count",
                    stepNumber = 3,
                    type = AcademyStepType.INTERACTIVE_QUESTION,
                    titleRes = R.string.academy_m2_step3_title,
                    maestroMood = MaestroMood.NEUTRAL,
                    maestroSpeechRes = R.string.academy_m2_step3_speech,
                    tableCards = listOf(
                        Card(Rank.SETTE, Suit.SPADE),
                        Card(Rank.SETTE, Suit.COPPE),
                        Card(Rank.SETTE, Suit.BASTONI),
                        Card(Rank.SEI, Suit.DENARI),
                    ),
                    tableBadgeRes = R.string.academy_m2_step3_badge,
                    options = listOf(
                        AcademyOption(
                            id = "m2_s3_opt1",
                            textRes = R.string.academy_m2_step3_opt1,
                            feedbackRes = R.string.academy_m2_step3_fb1,
                            isCorrect = true,
                        ),
                        AcademyOption(
                            id = "m2_s3_opt2",
                            textRes = R.string.academy_m2_step3_opt2,
                            feedbackRes = R.string.academy_m2_step3_fb2,
                            isCorrect = false,
                        ),
                    ),
                ),
                AcademyStep(
                    id = "m2_s4_summary",
                    stepNumber = 4,
                    type = AcademyStepType.SUMMARY_AND_BADGE,
                    titleRes = R.string.academy_m2_step4_title,
                    maestroMood = MaestroMood.TRIUMPHANT,
                    maestroSpeechRes = R.string.academy_m2_step4_speech,
                    goldenRuleRes = R.string.academy_module_2_rule,
                ),
            ),
        ),

        // =========================================================================
        // MODULO 3: La Regola del Dispari
        // =========================================================================
        AcademyModule(
            id = AcademyModuleId.MODULE_3_DISPARI,
            number = 3,
            titleRes = R.string.academy_module_3_title,
            subtitleRes = R.string.academy_module_3_subtitle,
            descriptionRes = R.string.academy_module_3_desc,
            iconRes = R.drawable.icon_trophy,
            goldenRuleRes = R.string.academy_module_3_rule,
            steps = listOf(
                AcademyStep(
                    id = "m3_s1_ten_threshold",
                    stepNumber = 1,
                    type = AcademyStepType.INTERACTIVE_QUESTION,
                    titleRes = R.string.academy_m3_step1_title,
                    maestroMood = MaestroMood.THINKING,
                    maestroSpeechRes = R.string.academy_m3_step1_speech,
                    tableCards = listOf(
                        Card(Rank.QUATTRO, Suit.SPADE),
                    ),
                    handCards = listOf(
                        Card(Rank.TRE, Suit.COPPE),
                        Card(Rank.RE, Suit.BASTONI),
                    ),
                    tableBadgeRes = R.string.academy_m3_step1_badge,
                    options = listOf(
                        AcademyOption(
                            id = "m3_s1_opt1",
                            textRes = R.string.academy_m3_step1_opt1,
                            feedbackRes = R.string.academy_m3_step1_fb1,
                            isCorrect = true,
                        ),
                        AcademyOption(
                            id = "m3_s1_opt2",
                            textRes = R.string.academy_m3_step1_opt2,
                            feedbackRes = R.string.academy_m3_step1_fb2,
                            isCorrect = false,
                        ),
                    ),
                ),
                AcademyStep(
                    id = "m3_s2_fortify_board",
                    stepNumber = 2,
                    type = AcademyStepType.CARD_ACTION,
                    titleRes = R.string.academy_m3_step2_title,
                    maestroMood = MaestroMood.WARNING,
                    maestroSpeechRes = R.string.academy_m3_step2_speech,
                    tableCards = listOf(
                        Card(Rank.SEI, Suit.BASTONI),
                    ),
                    handCards = listOf(
                        Card(Rank.CAVALLO, Suit.COPPE),
                        Card(Rank.DUE, Suit.SPADE),
                    ),
                    cardBadges = mapOf(
                        Card(Rank.CAVALLO, Suit.COPPE) to R.string.academy_badge_sum_15_safe,
                        Card(Rank.DUE, Suit.SPADE) to R.string.academy_badge_sum_8_risk,
                    ),
                    tableBadgeRes = R.string.academy_m3_step2_badge,
                    targetCard = Card(Rank.CAVALLO, Suit.COPPE),
                ),
                AcademyStep(
                    id = "m3_s3_dealers_hand",
                    stepNumber = 3,
                    type = AcademyStepType.INTERACTIVE_QUESTION,
                    titleRes = R.string.academy_m3_step3_title,
                    maestroMood = MaestroMood.NEUTRAL,
                    maestroSpeechRes = R.string.academy_m3_step3_speech,
                    tableCards = listOf(
                        Card(Rank.ASSO, Suit.DENARI),
                        Card(Rank.FANTE, Suit.COPPE),
                    ),
                    tableBadgeRes = R.string.academy_m3_step3_badge,
                    options = listOf(
                        AcademyOption(
                            id = "m3_s3_opt1",
                            textRes = R.string.academy_m3_step3_opt1,
                            feedbackRes = R.string.academy_m3_step3_fb1,
                            isCorrect = true,
                        ),
                        AcademyOption(
                            id = "m3_s3_opt2",
                            textRes = R.string.academy_m3_step3_opt2,
                            feedbackRes = R.string.academy_m3_step3_fb2,
                            isCorrect = false,
                        ),
                    ),
                ),
                AcademyStep(
                    id = "m3_s4_summary",
                    stepNumber = 4,
                    type = AcademyStepType.SUMMARY_AND_BADGE,
                    titleRes = R.string.academy_m3_step4_title,
                    maestroMood = MaestroMood.CELEBRATING,
                    maestroSpeechRes = R.string.academy_m3_step4_speech,
                    goldenRuleRes = R.string.academy_module_3_rule,
                ),
            ),
        ),
    )
}
