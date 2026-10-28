package it.maestro.app.data.local.academy

import it.maestro.app.R
import it.maestro.app.designsystem.components.MaestroMood
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Suit

/** Capitolo 4 dell'Accademia: Visione da Maestro (Memoria & Endgame) (F1R-28f: un file per capitolo). */
internal val ACADEMY_CHAPTER_4: AcademyChapter = AcademyChapter(
    id = AcademyChapterId.CHAPTER_4_MAESTRIA,
    number = 4,
    titleRes = R.string.academy_ch4_title,
    subtitleRes = R.string.academy_ch4_subtitle,
    descriptionRes = R.string.academy_ch4_desc,
    iconRes = R.drawable.icon_trophy,
    badgeRes = R.drawable.badge_best,
    rankTitleRes = R.string.academy_ch4_rank,
    lessons = listOf(
        // 4.1: La Memoria dei Sette
        AcademyLesson(
            id = AcademyLessonId.LESSON_4_1_MEMORIA_SETTE,
            specRefs = listOf("6.2", "5.1", "4.4"),
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
            specRefs = listOf("4.3", "4.4"),
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
            specRefs = listOf("3.4", "3.5", "6.1", "4.2", "6.2"),
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
)
