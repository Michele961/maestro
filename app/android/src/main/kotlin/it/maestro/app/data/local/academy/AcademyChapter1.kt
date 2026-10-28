package it.maestro.app.data.local.academy

import it.maestro.app.R
import it.maestro.app.designsystem.components.MaestroMood
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit

/** Capitolo 1 dell'Accademia: I Fondamenti del Circolo (F1R-28f: un file per capitolo). */
internal val ACADEMY_CHAPTER_1: AcademyChapter = AcademyChapter(
    id = AcademyChapterId.CHAPTER_1_FONDAMENTI,
    number = 1,
    titleRes = R.string.academy_ch1_title,
    subtitleRes = R.string.academy_ch1_subtitle,
    descriptionRes = R.string.academy_ch1_desc,
    iconRes = R.drawable.icon_book_quill,
    badgeRes = R.drawable.badge_best,
    rankTitleRes = R.string.academy_ch1_rank,
    lessons = listOf(
        // 1.1: La Presa Singola
        AcademyLesson(
            id = AcademyLessonId.LESSON_1_1_PRESA_SINGOLA,
            specRefs = listOf("4.2", "4.3", "4.4"),
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
            specRefs = listOf("6.2"),
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
            specRefs = listOf("6.2", "6.3", "4.2"),
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
            specRefs = listOf("4.3", "4.8"),
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
)
