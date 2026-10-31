package it.maestro.app.domain.story

import it.maestro.app.R

private val MAE = Speaker.MAESTRO
private val GIG = Speaker.GIGI
private val ROS = Speaker.ROSA
private val TON = Speaker.TONINO
private val ALF = Speaker.ALFONSO

/** Le battute approvate di `docs/design/stagione-scene.md`, come dati: i testi stanno nelle risorse (ADR-0007). */
internal object StoryTexts {
    val OPEN: Map<Int, List<Beat>> = mapOf(
        1 to listOf(
            Beat(MAE, R.string.story_ch01_scene1_text),
            Beat(GIG, R.string.story_ch01_scene2_text),
            Beat(MAE, R.string.story_ch01_scene3_text),
            Beat(GIG, R.string.story_ch01_scene4_text),
        ),
        2 to listOf(
            Beat(GIG, R.string.story_ch02_scene1_text),
            Beat(MAE, R.string.story_ch02_scene2_text),
            Beat(GIG, R.string.story_ch02_scene3_text),
        ),
        3 to listOf(
            Beat(MAE, R.string.story_ch03_scene1_text),
            Beat(MAE, R.string.story_ch03_scene2_text),
            Beat(GIG, R.string.story_ch03_scene3_text),
            Beat(MAE, R.string.story_ch03_scene4_text),
        ),
        4 to listOf(
            Beat(MAE, R.string.story_ch04_scene1_text),
            Beat(GIG, R.string.story_ch04_scene2_text),
            Beat(MAE, R.string.story_ch04_scene3_text),
        ),
        5 to listOf(
            Beat(ROS, R.string.story_ch05_scene1_text),
            Beat(MAE, R.string.story_ch05_scene2_text),
            Beat(ROS, R.string.story_ch05_scene3_text),
            Beat(MAE, R.string.story_ch05_scene4_text),
        ),
        6 to listOf(
            Beat(MAE, R.string.story_ch06_scene1_text),
            Beat(ROS, R.string.story_ch06_scene2_text),
            Beat(MAE, R.string.story_ch06_scene3_text),
        ),
        7 to listOf(
            Beat(MAE, R.string.story_ch07_scene1_text),
            Beat(TON, R.string.story_ch07_scene2_text),
            Beat(MAE, R.string.story_ch07_scene3_text),
        ),
        8 to listOf(
            Beat(TON, R.string.story_ch08_scene1_text),
            Beat(MAE, R.string.story_ch08_scene2_text),
            Beat(TON, R.string.story_ch08_scene3_text),
            Beat(MAE, R.string.story_ch08_scene4_text),
        ),
        9 to listOf(
            Beat(MAE, R.string.story_ch09_scene1_text),
            Beat(ALF, R.string.story_ch09_scene2_text),
            Beat(MAE, R.string.story_ch09_scene3_text),
        ),
        10 to listOf(
            Beat(ALF, R.string.story_ch10_scene1_text),
            Beat(MAE, R.string.story_ch10_scene2_text),
            Beat(ALF, R.string.story_ch10_scene3_text),
            Beat(MAE, R.string.story_ch10_scene4_text),
        ),
    )

    val MID: Map<Int, List<Beat>> = mapOf(
        9 to listOf(
            Beat(MAE, R.string.story_ch09_mid_scene1_text),
        ),
        10 to listOf(
            Beat(ALF, R.string.story_ch10_mid_scene1_text),
            Beat(MAE, R.string.story_ch10_mid_scene2_text),
            Beat(MAE, R.string.story_ch10_mid_scene3_text),
            Beat(MAE, R.string.story_ch10_mid_scene4_text),
        ),
    )

    val CLOSE: Map<Int, List<Beat>> = mapOf(
        1 to listOf(
            Beat(MAE, R.string.story_ch01_close_scene1_text),
            Beat(MAE, R.string.story_ch01_close_scene2_text),
            Beat(GIG, R.string.story_ch01_close_scene3_text),
        ),
        2 to listOf(
            Beat(GIG, R.string.story_ch02_close_scene1_text),
            Beat(GIG, R.string.story_ch02_close_scene2_text),
            Beat(MAE, R.string.story_ch02_close_scene3_text),
        ),
        3 to listOf(
            Beat(GIG, R.string.story_ch03_close_scene1_text),
            Beat(MAE, R.string.story_ch03_close_scene2_text),
            Beat(GIG, R.string.story_ch03_close_scene3_text),
        ),
        4 to listOf(
            Beat(MAE, R.string.story_ch04_close_scene1_text),
            Beat(ROS, R.string.story_ch04_close_scene2_text),
            Beat(GIG, R.string.story_ch04_close_scene3_text),
        ),
        5 to listOf(
            Beat(ROS, R.string.story_ch05_close_scene1_text),
            Beat(ROS, R.string.story_ch05_close_scene2_text),
            Beat(MAE, R.string.story_ch05_close_scene3_text),
        ),
        6 to listOf(
            Beat(MAE, R.string.story_ch06_close_scene1_text),
            Beat(ROS, R.string.story_ch06_close_scene2_text),
            Beat(MAE, R.string.story_ch06_close_scene3_text),
        ),
        7 to listOf(
            Beat(MAE, R.string.story_ch07_close_scene1_text),
            Beat(MAE, R.string.story_ch07_close_scene2_text),
            Beat(TON, R.string.story_ch07_close_scene3_text),
        ),
        8 to listOf(
            Beat(TON, R.string.story_ch08_close_scene1_text),
            Beat(TON, R.string.story_ch08_close_scene2_text),
            Beat(MAE, R.string.story_ch08_close_scene3_text),
        ),
        9 to listOf(
            Beat(MAE, R.string.story_ch09_close_scene1_text),
            Beat(MAE, R.string.story_ch09_close_scene2_text),
            Beat(ALF, R.string.story_ch09_close_scene3_text),
        ),
        10 to listOf(
            Beat(MAE, R.string.story_ch10_close_scene1_text, R.string.story_ch10_close_scene1_text_named),
            Beat(MAE, R.string.story_ch10_close_scene2_text),
            Beat(GIG, R.string.story_ch10_close_scene3_text),
        ),
    )

    val WELCOME_BEATS: List<Beat> = listOf(
        Beat(MAE, R.string.story_welcome_scene1_text),
        Beat(MAE, R.string.story_welcome_scene2_text),
        Beat(MAE, R.string.story_welcome_scene3_text, R.string.story_welcome_scene3_text_named),
        Beat(MAE, R.string.story_welcome_scene4_text),
    )

    val EPILOGUE_BEATS: List<Beat> = listOf(
        Beat(MAE, R.string.story_epilogue_scene1_text),
        Beat(MAE, R.string.story_epilogue_scene2_text),
        Beat(MAE, R.string.story_epilogue_scene3_text, R.string.story_epilogue_scene3_text_named),
        Beat(GIG, R.string.story_epilogue_scene4_text),
        Beat(ROS, R.string.story_epilogue_scene5_text),
        Beat(TON, R.string.story_epilogue_scene6_text),
        Beat(ALF, R.string.story_epilogue_scene7_text),
    )
}
