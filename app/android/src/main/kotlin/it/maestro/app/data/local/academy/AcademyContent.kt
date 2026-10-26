package it.maestro.app.data.local.academy

import it.maestro.app.R
import it.maestro.app.ui.components.MaestroMood
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit

/**
 * Contenuti formativi completi dell'Accademia del Maestro: 4 Macro-Moduli (Capitoli) e 15 Sotto-Moduli (Lezioni).
 */
public object AcademyContent {

    public val CHAPTERS: List<AcademyChapter> = listOf(
        // =========================================================================
        // CAPITOLO 1: I Fondamenti del Circolo
        // =========================================================================
        AcademyChapter(
            id = AcademyChapterId.CHAPTER_1_FONDAMENTI,
            number = 1,
            titleRes = R.string.academy_ch1_title,
            subtitleRes = R.string.academy_ch1_subtitle,
            descriptionRes = R.string.academy_ch1_desc,
            iconRes = R.drawable.icon_book_quill,
            badgeRes = R.drawable.badge_best,
            lessons = listOf(
                // 1.1: La Presa Singola
                AcademyLesson(
                    id = AcademyLessonId.LESSON_1_1_PRESA_SINGOLA,
                    chapterId = AcademyChapterId.CHAPTER_1_FONDAMENTI,
                    lessonNumber = 1,
                    titleRes = R.string.academy_l1_1_title,
                    subtitleRes = R.string.academy_l1_1_subtitle,
                    descriptionRes = R.string.academy_l1_1_desc,
                    iconRes = R.drawable.icon_book_quill,
                    goldenRuleRes = R.string.academy_l1_1_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch1_l1_s1",
                            stepNumber = 1,
                            type = AcademyStepType.INTERACTIVE_QUESTION,
                            titleRes = R.string.academy_l1_1_title,
                            maestroMood = MaestroMood.THINKING,
                            maestroSpeechRes = R.string.academy_l1_1_speech_q,
                            tableCards = listOf(
                                Card(Rank.SETTE, Suit.SPADE),
                                Card(Rank.QUATTRO, Suit.COPPE),
                                Card(Rank.TRE, Suit.BASTONI),
                            ),
                            handCards = listOf(
                                Card(Rank.SETTE, Suit.DENARI),
                            ),
                            tableBadgeRes = R.string.academy_l1_1_badge_q,
                            options = listOf(
                                AcademyOption(
                                    id = "c1_l1_opt1",
                                    textRes = R.string.academy_l1_1_opt1,
                                    feedbackRes = R.string.academy_l1_1_fb1,
                                    isCorrect = true,
                                ),
                                AcademyOption(
                                    id = "c1_l1_opt2",
                                    textRes = R.string.academy_l1_1_opt2,
                                    feedbackRes = R.string.academy_l1_1_fb2,
                                    isCorrect = false,
                                ),
                            ),
                        ),
                        AcademyStep(
                            id = "ch1_l1_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l1_1_title,
                            maestroMood = MaestroMood.TRIUMPHANT,
                            maestroSpeechRes = R.string.academy_l1_1_speech_sum,
                            goldenRuleRes = R.string.academy_l1_1_rule,
                        ),
                    ),
                ),

                // 1.2: I Quattro Punti di Mazzo
                AcademyLesson(
                    id = AcademyLessonId.LESSON_1_2_PUNTI_MAZZO,
                    chapterId = AcademyChapterId.CHAPTER_1_FONDAMENTI,
                    lessonNumber = 2,
                    titleRes = R.string.academy_l1_2_title,
                    subtitleRes = R.string.academy_l1_2_subtitle,
                    descriptionRes = R.string.academy_l1_2_desc,
                    iconRes = R.drawable.icon_coin,
                    goldenRuleRes = R.string.academy_l1_2_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch1_l2_s1",
                            stepNumber = 1,
                            type = AcademyStepType.INTERACTIVE_QUESTION,
                            titleRes = R.string.academy_l1_2_title,
                            maestroMood = MaestroMood.NEUTRAL,
                            maestroSpeechRes = R.string.academy_l1_2_speech_q,
                            tableCards = listOf(
                                Card(Rank.ASSO, Suit.DENARI),
                                Card(Rank.RE, Suit.DENARI),
                                Card(Rank.SETTE, Suit.SPADE),
                            ),
                            tableBadgeRes = R.string.academy_l1_2_badge_q,
                            options = listOf(
                                AcademyOption(
                                    id = "c1_l2_opt1",
                                    textRes = R.string.academy_l1_2_opt1,
                                    feedbackRes = R.string.academy_l1_2_fb1,
                                    isCorrect = true,
                                ),
                                AcademyOption(
                                    id = "c1_l2_opt2",
                                    textRes = R.string.academy_l1_2_opt2,
                                    feedbackRes = R.string.academy_l1_2_fb2,
                                    isCorrect = false,
                                ),
                            ),
                        ),
                        AcademyStep(
                            id = "ch1_l2_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l1_2_title,
                            maestroMood = MaestroMood.PLEASED,
                            maestroSpeechRes = R.string.academy_l1_2_speech_sum,
                            goldenRuleRes = R.string.academy_l1_2_rule,
                        ),
                    ),
                ),

                // 1.3: Il Re dei Denari
                AcademyLesson(
                    id = AcademyLessonId.LESSON_1_3_SETTEBELLO,
                    chapterId = AcademyChapterId.CHAPTER_1_FONDAMENTI,
                    lessonNumber = 3,
                    titleRes = R.string.academy_l1_3_title,
                    subtitleRes = R.string.academy_l1_3_subtitle,
                    descriptionRes = R.string.academy_l1_3_desc,
                    iconRes = R.drawable.icon_trophy,
                    goldenRuleRes = R.string.academy_l1_3_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch1_l3_s1",
                            stepNumber = 1,
                            type = AcademyStepType.CARD_ACTION,
                            titleRes = R.string.academy_l1_3_title,
                            maestroMood = MaestroMood.ENCOURAGING,
                            maestroSpeechRes = R.string.academy_l1_3_speech_act,
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
                            tableBadgeRes = R.string.academy_l1_3_badge_act,
                            targetCard = Card(Rank.SETTE, Suit.BASTONI),
                        ),
                        AcademyStep(
                            id = "ch1_l3_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l1_3_title,
                            maestroMood = MaestroMood.TRIUMPHANT,
                            maestroSpeechRes = R.string.academy_l1_3_speech_sum,
                            goldenRuleRes = R.string.academy_l1_3_rule,
                        ),
                    ),
                ),

                // 1.4: La Scelta tra Somme
                AcademyLesson(
                    id = AcademyLessonId.LESSON_1_4_SCELTA_SOMME,
                    chapterId = AcademyChapterId.CHAPTER_1_FONDAMENTI,
                    lessonNumber = 4,
                    titleRes = R.string.academy_l1_4_title,
                    subtitleRes = R.string.academy_l1_4_subtitle,
                    descriptionRes = R.string.academy_l1_4_desc,
                    iconRes = R.drawable.icon_pouch,
                    goldenRuleRes = R.string.academy_l1_4_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch1_l4_s1",
                            stepNumber = 1,
                            type = AcademyStepType.INTERACTIVE_QUESTION,
                            titleRes = R.string.academy_l1_4_title,
                            maestroMood = MaestroMood.NEUTRAL,
                            maestroSpeechRes = R.string.academy_l1_4_speech_q,
                            tableCards = listOf(
                                Card(Rank.CINQUE, Suit.SPADE),
                                Card(Rank.DUE, Suit.SPADE),
                                Card(Rank.QUATTRO, Suit.DENARI),
                                Card(Rank.TRE, Suit.DENARI),
                            ),
                            handCards = listOf(
                                Card(Rank.SETTE, Suit.COPPE),
                            ),
                            tableBadgeRes = R.string.academy_l1_4_badge_q,
                            options = listOf(
                                AcademyOption(
                                    id = "c1_l4_opt1",
                                    textRes = R.string.academy_l1_4_opt1,
                                    feedbackRes = R.string.academy_l1_4_fb1,
                                    isCorrect = true,
                                ),
                                AcademyOption(
                                    id = "c1_l4_opt2",
                                    textRes = R.string.academy_l1_4_opt2,
                                    feedbackRes = R.string.academy_l1_4_fb2,
                                    isCorrect = false,
                                ),
                            ),
                        ),
                        AcademyStep(
                            id = "ch1_l4_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l1_4_title,
                            maestroMood = MaestroMood.CELEBRATING,
                            maestroSpeechRes = R.string.academy_l1_4_speech_sum,
                            goldenRuleRes = R.string.academy_l1_4_rule,
                        ),
                    ),
                ),
            ),
        ),

        // =========================================================================
        // CAPITOLO 2: La Scienza della Primiera
        // =========================================================================
        AcademyChapter(
            id = AcademyChapterId.CHAPTER_2_PRIMIERA,
            number = 2,
            titleRes = R.string.academy_ch2_title,
            subtitleRes = R.string.academy_ch2_subtitle,
            descriptionRes = R.string.academy_ch2_desc,
            iconRes = R.drawable.icon_shield_fleur,
            badgeRes = R.drawable.badge_best,
            lessons = listOf(
                // 2.1: Il Paradosso del Re
                AcademyLesson(
                    id = AcademyLessonId.LESSON_2_1_SCALA_VALORI,
                    chapterId = AcademyChapterId.CHAPTER_2_PRIMIERA,
                    lessonNumber = 1,
                    titleRes = R.string.academy_l2_1_title,
                    subtitleRes = R.string.academy_l2_1_subtitle,
                    descriptionRes = R.string.academy_l2_1_desc,
                    iconRes = R.drawable.icon_shield_fleur,
                    goldenRuleRes = R.string.academy_l2_1_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch2_l1_s1",
                            stepNumber = 1,
                            type = AcademyStepType.INTERACTIVE_QUESTION,
                            titleRes = R.string.academy_l2_1_title,
                            maestroMood = MaestroMood.THINKING,
                            maestroSpeechRes = R.string.academy_l2_1_speech_q,
                            tableCards = listOf(
                                Card(Rank.RE, Suit.BASTONI),
                                Card(Rank.SEI, Suit.COPPE),
                            ),
                            cardBadges = mapOf(
                                Card(Rank.RE, Suit.BASTONI) to R.string.academy_badge_king_10_pt,
                                Card(Rank.SEI, Suit.COPPE) to R.string.academy_badge_six_18_pt,
                            ),
                            tableBadgeRes = R.string.academy_l2_1_badge_q,
                            options = listOf(
                                AcademyOption(
                                    id = "c2_l1_opt1",
                                    textRes = R.string.academy_l2_1_opt1,
                                    feedbackRes = R.string.academy_l2_1_fb1,
                                    isCorrect = true,
                                ),
                                AcademyOption(
                                    id = "c2_l1_opt2",
                                    textRes = R.string.academy_l2_1_opt2,
                                    feedbackRes = R.string.academy_l2_1_fb2,
                                    isCorrect = false,
                                ),
                            ),
                        ),
                        AcademyStep(
                            id = "ch2_l1_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l2_1_title,
                            maestroMood = MaestroMood.PLEASED,
                            maestroSpeechRes = R.string.academy_l2_1_speech_sum,
                            goldenRuleRes = R.string.academy_l2_1_rule,
                        ),
                    ),
                ),

                // 2.2: La Formula dei 4 Semi
                AcademyLesson(
                    id = AcademyLessonId.LESSON_2_2_FORMULA_SEMI,
                    chapterId = AcademyChapterId.CHAPTER_2_PRIMIERA,
                    lessonNumber = 2,
                    titleRes = R.string.academy_l2_2_title,
                    subtitleRes = R.string.academy_l2_2_subtitle,
                    descriptionRes = R.string.academy_l2_2_desc,
                    iconRes = R.drawable.icon_book_quill,
                    goldenRuleRes = R.string.academy_l2_2_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch2_l2_s1",
                            stepNumber = 1,
                            type = AcademyStepType.INTERACTIVE_QUESTION,
                            titleRes = R.string.academy_l2_2_title,
                            maestroMood = MaestroMood.NEUTRAL,
                            maestroSpeechRes = R.string.academy_l2_2_speech_q,
                            tableCards = listOf(
                                Card(Rank.SETTE, Suit.SPADE),
                                Card(Rank.SETTE, Suit.COPPE),
                                Card(Rank.SETTE, Suit.DENARI),
                            ),
                            tableBadgeRes = R.string.academy_l2_2_badge_q,
                            options = listOf(
                                AcademyOption(
                                    id = "c2_l2_opt1",
                                    textRes = R.string.academy_l2_2_opt1,
                                    feedbackRes = R.string.academy_l2_2_fb1,
                                    isCorrect = true,
                                ),
                                AcademyOption(
                                    id = "c2_l2_opt2",
                                    textRes = R.string.academy_l2_2_opt2,
                                    feedbackRes = R.string.academy_l2_2_fb2,
                                    isCorrect = false,
                                ),
                            ),
                        ),
                        AcademyStep(
                            id = "ch2_l2_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l2_2_title,
                            maestroMood = MaestroMood.TRIUMPHANT,
                            maestroSpeechRes = R.string.academy_l2_2_speech_sum,
                            goldenRuleRes = R.string.academy_l2_2_rule,
                        ),
                    ),
                ),

                // 2.3: La Trappola del Seme Vuoto
                AcademyLesson(
                    id = AcademyLessonId.LESSON_2_3_TRAPPOLA_SEME_VUOTO,
                    chapterId = AcademyChapterId.CHAPTER_2_PRIMIERA,
                    lessonNumber = 3,
                    titleRes = R.string.academy_l2_3_title,
                    subtitleRes = R.string.academy_l2_3_subtitle,
                    descriptionRes = R.string.academy_l2_3_desc,
                    iconRes = R.drawable.icon_shield_fleur,
                    goldenRuleRes = R.string.academy_l2_3_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch2_l3_s1",
                            stepNumber = 1,
                            type = AcademyStepType.CARD_ACTION,
                            titleRes = R.string.academy_l2_3_title,
                            maestroMood = MaestroMood.ENCOURAGING,
                            maestroSpeechRes = R.string.academy_l2_3_speech_act,
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
                            tableBadgeRes = R.string.academy_l2_3_badge_act,
                            targetCard = Card(Rank.CINQUE, Suit.SPADE),
                        ),
                        AcademyStep(
                            id = "ch2_l3_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l2_3_title,
                            maestroMood = MaestroMood.TRIUMPHANT,
                            maestroSpeechRes = R.string.academy_l2_3_speech_sum,
                            goldenRuleRes = R.string.academy_l2_3_rule,
                        ),
                    ),
                ),

                // 2.4: La Conta dei Sette e dei Sei
                AcademyLesson(
                    id = AcademyLessonId.LESSON_2_4_CONTA_SETTE_SEI,
                    chapterId = AcademyChapterId.CHAPTER_2_PRIMIERA,
                    lessonNumber = 4,
                    titleRes = R.string.academy_l2_4_title,
                    subtitleRes = R.string.academy_l2_4_subtitle,
                    descriptionRes = R.string.academy_l2_4_desc,
                    iconRes = R.drawable.icon_trophy,
                    goldenRuleRes = R.string.academy_l2_4_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch2_l4_s1",
                            stepNumber = 1,
                            type = AcademyStepType.INTERACTIVE_QUESTION,
                            titleRes = R.string.academy_l2_4_title,
                            maestroMood = MaestroMood.NEUTRAL,
                            maestroSpeechRes = R.string.academy_l2_4_speech_q,
                            tableCards = listOf(
                                Card(Rank.SETTE, Suit.SPADE),
                                Card(Rank.SETTE, Suit.COPPE),
                                Card(Rank.SETTE, Suit.BASTONI),
                                Card(Rank.SEI, Suit.DENARI),
                            ),
                            tableBadgeRes = R.string.academy_l2_4_badge_q,
                            options = listOf(
                                AcademyOption(
                                    id = "c2_l4_opt1",
                                    textRes = R.string.academy_l2_4_opt1,
                                    feedbackRes = R.string.academy_l2_4_fb1,
                                    isCorrect = true,
                                ),
                                AcademyOption(
                                    id = "c2_l4_opt2",
                                    textRes = R.string.academy_l2_4_opt2,
                                    feedbackRes = R.string.academy_l2_4_fb2,
                                    isCorrect = false,
                                ),
                            ),
                        ),
                        AcademyStep(
                            id = "ch2_l4_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l2_4_title,
                            maestroMood = MaestroMood.CELEBRATING,
                            maestroSpeechRes = R.string.academy_l2_4_speech_sum,
                            goldenRuleRes = R.string.academy_l2_4_rule,
                        ),
                    ),
                ),
            ),
        ),

        // =========================================================================
        // CAPITOLO 3: Difesa del Banco & Antiscopa
        // =========================================================================
        AcademyChapter(
            id = AcademyChapterId.CHAPTER_3_DIFESA,
            number = 3,
            titleRes = R.string.academy_ch3_title,
            subtitleRes = R.string.academy_ch3_subtitle,
            descriptionRes = R.string.academy_ch3_desc,
            iconRes = R.drawable.icon_book_closed,
            badgeRes = R.drawable.badge_best,
            lessons = listOf(
                // 3.1: La Soglia del Dieci
                AcademyLesson(
                    id = AcademyLessonId.LESSON_3_1_SOGLIA_DIECI,
                    chapterId = AcademyChapterId.CHAPTER_3_DIFESA,
                    lessonNumber = 1,
                    titleRes = R.string.academy_l3_1_title,
                    subtitleRes = R.string.academy_l3_1_subtitle,
                    descriptionRes = R.string.academy_l3_1_desc,
                    iconRes = R.drawable.icon_book_closed,
                    goldenRuleRes = R.string.academy_l3_1_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch3_l1_s1",
                            stepNumber = 1,
                            type = AcademyStepType.INTERACTIVE_QUESTION,
                            titleRes = R.string.academy_l3_1_title,
                            maestroMood = MaestroMood.THINKING,
                            maestroSpeechRes = R.string.academy_l3_1_speech_q,
                            tableCards = listOf(
                                Card(Rank.QUATTRO, Suit.SPADE),
                            ),
                            handCards = listOf(
                                Card(Rank.TRE, Suit.COPPE),
                                Card(Rank.RE, Suit.BASTONI),
                            ),
                            tableBadgeRes = R.string.academy_l3_1_badge_q,
                            options = listOf(
                                AcademyOption(
                                    id = "c3_l1_opt1",
                                    textRes = R.string.academy_l3_1_opt1,
                                    feedbackRes = R.string.academy_l3_1_fb1,
                                    isCorrect = true,
                                ),
                                AcademyOption(
                                    id = "c3_l1_opt2",
                                    textRes = R.string.academy_l3_1_opt2,
                                    feedbackRes = R.string.academy_l3_1_fb2,
                                    isCorrect = false,
                                ),
                            ),
                        ),
                        AcademyStep(
                            id = "ch3_l1_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l3_1_title,
                            maestroMood = MaestroMood.PLEASED,
                            maestroSpeechRes = R.string.academy_l3_1_speech_sum,
                            goldenRuleRes = R.string.academy_l3_1_rule,
                        ),
                    ),
                ),

                // 3.2: Blindare il Banco
                AcademyLesson(
                    id = AcademyLessonId.LESSON_3_2_BLINDARE_BANCO,
                    chapterId = AcademyChapterId.CHAPTER_3_DIFESA,
                    lessonNumber = 2,
                    titleRes = R.string.academy_l3_2_title,
                    subtitleRes = R.string.academy_l3_2_subtitle,
                    descriptionRes = R.string.academy_l3_2_desc,
                    iconRes = R.drawable.icon_shield_fleur,
                    goldenRuleRes = R.string.academy_l3_2_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch3_l2_s1",
                            stepNumber = 1,
                            type = AcademyStepType.CARD_ACTION,
                            titleRes = R.string.academy_l3_2_title,
                            maestroMood = MaestroMood.WARNING,
                            maestroSpeechRes = R.string.academy_l3_2_speech_act,
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
                            tableBadgeRes = R.string.academy_l3_2_badge_act,
                            targetCard = Card(Rank.CAVALLO, Suit.COPPE),
                        ),
                        AcademyStep(
                            id = "ch3_l2_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l3_2_title,
                            maestroMood = MaestroMood.TRIUMPHANT,
                            maestroSpeechRes = R.string.academy_l3_2_speech_sum,
                            goldenRuleRes = R.string.academy_l3_2_rule,
                        ),
                    ),
                ),

                // 3.3: La Regola del Dispari
                AcademyLesson(
                    id = AcademyLessonId.LESSON_3_3_REGOLA_DISPARI,
                    chapterId = AcademyChapterId.CHAPTER_3_DIFESA,
                    lessonNumber = 3,
                    titleRes = R.string.academy_l3_3_title,
                    subtitleRes = R.string.academy_l3_3_subtitle,
                    descriptionRes = R.string.academy_l3_3_desc,
                    iconRes = R.drawable.icon_book_quill,
                    goldenRuleRes = R.string.academy_l3_3_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch3_l3_s1",
                            stepNumber = 1,
                            type = AcademyStepType.INTERACTIVE_QUESTION,
                            titleRes = R.string.academy_l3_3_title,
                            maestroMood = MaestroMood.NEUTRAL,
                            maestroSpeechRes = R.string.academy_l3_3_speech_q,
                            tableCards = listOf(
                                Card(Rank.ASSO, Suit.COPPE),
                                Card(Rank.TRE, Suit.SPADE),
                            ),
                            tableBadgeRes = R.string.academy_l3_3_badge_q,
                            options = listOf(
                                AcademyOption(
                                    id = "c3_l3_opt1",
                                    textRes = R.string.academy_l3_3_opt1,
                                    feedbackRes = R.string.academy_l3_3_fb1,
                                    isCorrect = true,
                                ),
                                AcademyOption(
                                    id = "c3_l3_opt2",
                                    textRes = R.string.academy_l3_3_opt2,
                                    feedbackRes = R.string.academy_l3_3_fb2,
                                    isCorrect = false,
                                ),
                            ),
                        ),
                        AcademyStep(
                            id = "ch3_l3_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l3_3_title,
                            maestroMood = MaestroMood.PLEASED,
                            maestroSpeechRes = R.string.academy_l3_3_speech_sum,
                            goldenRuleRes = R.string.academy_l3_3_rule,
                        ),
                    ),
                ),

                // 3.4: L'Ultima Mano del Mazziere
                AcademyLesson(
                    id = AcademyLessonId.LESSON_3_4_ULTIMA_MANO,
                    chapterId = AcademyChapterId.CHAPTER_3_DIFESA,
                    lessonNumber = 4,
                    titleRes = R.string.academy_l3_4_title,
                    subtitleRes = R.string.academy_l3_4_subtitle,
                    descriptionRes = R.string.academy_l3_4_desc,
                    iconRes = R.drawable.icon_trophy,
                    goldenRuleRes = R.string.academy_l3_4_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch3_l4_s1",
                            stepNumber = 1,
                            type = AcademyStepType.INTERACTIVE_QUESTION,
                            titleRes = R.string.academy_l3_4_title,
                            maestroMood = MaestroMood.NEUTRAL,
                            maestroSpeechRes = R.string.academy_l3_4_speech_q,
                            tableCards = listOf(
                                Card(Rank.ASSO, Suit.DENARI),
                                Card(Rank.FANTE, Suit.COPPE),
                            ),
                            tableBadgeRes = R.string.academy_l3_4_badge_q,
                            options = listOf(
                                AcademyOption(
                                    id = "c3_l4_opt1",
                                    textRes = R.string.academy_l3_4_opt1,
                                    feedbackRes = R.string.academy_l3_4_fb1,
                                    isCorrect = true,
                                ),
                                AcademyOption(
                                    id = "c3_l4_opt2",
                                    textRes = R.string.academy_l3_4_opt2,
                                    feedbackRes = R.string.academy_l3_4_fb2,
                                    isCorrect = false,
                                ),
                            ),
                        ),
                        AcademyStep(
                            id = "ch3_l4_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l3_4_title,
                            maestroMood = MaestroMood.CELEBRATING,
                            maestroSpeechRes = R.string.academy_l3_4_speech_sum,
                            goldenRuleRes = R.string.academy_l3_4_rule,
                        ),
                    ),
                ),
            ),
        ),

        // =========================================================================
        // CAPITOLO 4: Visione da Maestro (Memoria & Endgame)
        // =========================================================================
        AcademyChapter(
            id = AcademyChapterId.CHAPTER_4_MAESTRIA,
            number = 4,
            titleRes = R.string.academy_ch4_title,
            subtitleRes = R.string.academy_ch4_subtitle,
            descriptionRes = R.string.academy_ch4_desc,
            iconRes = R.drawable.icon_trophy,
            badgeRes = R.drawable.badge_best,
            lessons = listOf(
                // 4.1: La Memoria dei Sette
                AcademyLesson(
                    id = AcademyLessonId.LESSON_4_1_MEMORIA_SETTE,
                    chapterId = AcademyChapterId.CHAPTER_4_MAESTRIA,
                    lessonNumber = 1,
                    titleRes = R.string.academy_l4_1_title,
                    subtitleRes = R.string.academy_l4_1_subtitle,
                    descriptionRes = R.string.academy_l4_1_desc,
                    iconRes = R.drawable.icon_newspaper,
                    goldenRuleRes = R.string.academy_l4_1_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch4_l1_s1",
                            stepNumber = 1,
                            type = AcademyStepType.INTERACTIVE_QUESTION,
                            titleRes = R.string.academy_l4_1_title,
                            maestroMood = MaestroMood.THINKING,
                            maestroSpeechRes = R.string.academy_l4_1_speech_q,
                            tableCards = listOf(
                                Card(Rank.QUATTRO, Suit.COPPE),
                                Card(Rank.TRE, Suit.BASTONI),
                            ),
                            tableBadgeRes = R.string.academy_l4_1_badge_q,
                            options = listOf(
                                AcademyOption(
                                    id = "c4_l1_opt1",
                                    textRes = R.string.academy_l4_1_opt1,
                                    feedbackRes = R.string.academy_l4_1_fb1,
                                    isCorrect = true,
                                ),
                                AcademyOption(
                                    id = "c4_l1_opt2",
                                    textRes = R.string.academy_l4_1_opt2,
                                    feedbackRes = R.string.academy_l4_1_fb2,
                                    isCorrect = false,
                                ),
                            ),
                        ),
                        AcademyStep(
                            id = "ch4_l1_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l4_1_title,
                            maestroMood = MaestroMood.PLEASED,
                            maestroSpeechRes = R.string.academy_l4_1_speech_sum,
                            goldenRuleRes = R.string.academy_l4_1_rule,
                        ),
                    ),
                ),

                // 4.2: La Lettura delle Carte Nascoste
                AcademyLesson(
                    id = AcademyLessonId.LESSON_4_2_DEDUZIONE_MANO,
                    chapterId = AcademyChapterId.CHAPTER_4_MAESTRIA,
                    lessonNumber = 2,
                    titleRes = R.string.academy_l4_2_title,
                    subtitleRes = R.string.academy_l4_2_subtitle,
                    descriptionRes = R.string.academy_l4_2_desc,
                    iconRes = R.drawable.icon_book_quill,
                    goldenRuleRes = R.string.academy_l4_2_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch4_l2_s1",
                            stepNumber = 1,
                            type = AcademyStepType.INTERACTIVE_QUESTION,
                            titleRes = R.string.academy_l4_2_title,
                            maestroMood = MaestroMood.NEUTRAL,
                            maestroSpeechRes = R.string.academy_l4_2_speech_q,
                            tableCards = listOf(
                                Card(Rank.SEI, Suit.COPPE),
                                Card(Rank.DUE, Suit.BASTONI),
                            ),
                            tableBadgeRes = R.string.academy_l4_2_badge_q,
                            options = listOf(
                                AcademyOption(
                                    id = "c4_l2_opt1",
                                    textRes = R.string.academy_l4_2_opt1,
                                    feedbackRes = R.string.academy_l4_2_fb1,
                                    isCorrect = true,
                                ),
                                AcademyOption(
                                    id = "c4_l2_opt2",
                                    textRes = R.string.academy_l4_2_opt2,
                                    feedbackRes = R.string.academy_l4_2_fb2,
                                    isCorrect = false,
                                ),
                            ),
                        ),
                        AcademyStep(
                            id = "ch4_l2_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l4_2_title,
                            maestroMood = MaestroMood.TRIUMPHANT,
                            maestroSpeechRes = R.string.academy_l4_2_speech_sum,
                            goldenRuleRes = R.string.academy_l4_2_rule,
                        ),
                    ),
                ),

                // 4.3: Il Finale a Mazzo Vuoto
                AcademyLesson(
                    id = AcademyLessonId.LESSON_4_3_ENDGAME_MAZZO_VUOTO,
                    chapterId = AcademyChapterId.CHAPTER_4_MAESTRIA,
                    lessonNumber = 3,
                    titleRes = R.string.academy_l4_3_title,
                    subtitleRes = R.string.academy_l4_3_subtitle,
                    descriptionRes = R.string.academy_l4_3_desc,
                    iconRes = R.drawable.icon_trophy,
                    goldenRuleRes = R.string.academy_l4_3_rule,
                    steps = listOf(
                        AcademyStep(
                            id = "ch4_l3_s1",
                            stepNumber = 1,
                            type = AcademyStepType.CARD_ACTION,
                            titleRes = R.string.academy_l4_3_title,
                            maestroMood = MaestroMood.ENCOURAGING,
                            maestroSpeechRes = R.string.academy_l4_3_speech_act,
                            tableCards = listOf(
                                Card(Rank.FANTE, Suit.BASTONI),
                                Card(Rank.ASSO, Suit.DENARI),
                            ),
                            handCards = listOf(
                                Card(Rank.FANTE, Suit.COPPE),
                                Card(Rank.DUE, Suit.SPADE),
                            ),
                            tableBadgeRes = R.string.academy_l4_3_badge_act,
                            targetCard = Card(Rank.FANTE, Suit.COPPE),
                        ),
                        AcademyStep(
                            id = "ch4_l3_s2",
                            stepNumber = 2,
                            type = AcademyStepType.SUMMARY_AND_BADGE,
                            titleRes = R.string.academy_l4_3_title,
                            maestroMood = MaestroMood.CELEBRATING,
                            maestroSpeechRes = R.string.academy_l4_3_speech_sum,
                            goldenRuleRes = R.string.academy_l4_3_rule,
                        ),
                    ),
                ),
            ),
        ),
    )

    /** Lista piatta di tutte le lezioni ordinate per progressione sequenziale. */
    public val ALL_LESSONS: List<AcademyLesson> = CHAPTERS.flatMap { it.lessons }
}
