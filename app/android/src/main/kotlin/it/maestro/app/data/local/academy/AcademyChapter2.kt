package it.maestro.app.data.local.academy

import it.maestro.app.domain.academy.AcademyChapterId
import it.maestro.app.domain.academy.AcademyLessonId
import it.maestro.app.domain.academy.AcademyStepType
import it.maestro.app.domain.academy.AcademyOption
import it.maestro.app.domain.academy.AcademyStep
import it.maestro.app.domain.academy.AcademyLesson
import it.maestro.app.domain.academy.AcademyChapter

import it.maestro.app.R
import it.maestro.app.designsystem.components.MaestroMood
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit

/** Capitolo 2 dell'Accademia: La Scienza della Primiera (F1R-28f: un file per capitolo). */
internal val ACADEMY_CHAPTER_2: AcademyChapter = AcademyChapter(
    id = AcademyChapterId.CHAPTER_2_PRIMIERA,
    number = 2,
    titleRes = R.string.academy_ch2_title,
    subtitleRes = R.string.academy_ch2_subtitle,
    descriptionRes = R.string.academy_ch2_desc,
    iconRes = R.drawable.icon_shield_fleur,
    badgeRes = R.drawable.badge_best,
    rankTitleRes = R.string.academy_ch2_rank,
    lessons = listOf(
        // 2.1: Il Paradosso del Re
        AcademyLesson(
            id = AcademyLessonId.LESSON_2_1_SCALA_VALORI,
            specRefs = listOf("6.3", "1.2"),
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
            specRefs = listOf("6.3"),
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
            specRefs = listOf("6.3", "4.2"),
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
            specRefs = listOf("6.3"),
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
)
