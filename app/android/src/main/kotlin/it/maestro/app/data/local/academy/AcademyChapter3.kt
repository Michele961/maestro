package it.maestro.app.data.local.academy

import it.maestro.app.R
import it.maestro.app.designsystem.components.MaestroMood
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit

/** Capitolo 3 dell'Accademia: Difesa del Banco & Antiscopa (F1R-28f: un file per capitolo). */
internal val ACADEMY_CHAPTER_3: AcademyChapter = AcademyChapter(
    id = AcademyChapterId.CHAPTER_3_DIFESA,
    number = 3,
    titleRes = R.string.academy_ch3_title,
    subtitleRes = R.string.academy_ch3_subtitle,
    descriptionRes = R.string.academy_ch3_desc,
    iconRes = R.drawable.icon_book_closed,
    badgeRes = R.drawable.badge_best,
    rankTitleRes = R.string.academy_ch3_rank,
    lessons = listOf(
        // 3.1: La Soglia del Dieci
        AcademyLesson(
            id = AcademyLessonId.LESSON_3_1_SOGLIA_DIECI,
            specRefs = listOf("5.1", "1.2", "4.2", "4.3"),
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
            specRefs = listOf("5.1", "4.5", "1.2"),
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
            specRefs = listOf("4.5", "5.1", "6.2", "6.3"),
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
            specRefs = listOf("5.2", "6.1", "3.5", "4.1"),
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
)
