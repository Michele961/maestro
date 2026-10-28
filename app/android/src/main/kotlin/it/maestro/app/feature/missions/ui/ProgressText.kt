package it.maestro.app.feature.missions.ui

import androidx.annotation.StringRes
import it.maestro.app.R
import it.maestro.app.domain.progress.AchievementId
import it.maestro.app.domain.progress.Experience

/** Nome dell'obiettivo: il dominio conosce gli identificatori, i testi stanno nelle risorse (ADR-0007). */
internal val AchievementId.nameRes: Int
    @StringRes get() = when (this) {
        AchievementId.FIRST_WIN -> R.string.achievement_first_win_name
        AchievementId.BEAT_GIGI -> R.string.achievement_beat_gigi_name
        AchievementId.BEAT_NONNA_ROSA -> R.string.achievement_beat_nonna_rosa_name
        AchievementId.BEAT_TONINO -> R.string.achievement_beat_tonino_name
        AchievementId.BEAT_DON_ALFONSO -> R.string.achievement_beat_don_alfonso_name
        AchievementId.BEAT_MAESTRO -> R.string.achievement_beat_maestro_name
        AchievementId.LADDER_COMPLETE -> R.string.achievement_ladder_complete_name
        AchievementId.FIRST_SWEEP -> R.string.achievement_first_sweep_name
        AchievementId.TEN_SWEEPS -> R.string.achievement_ten_sweeps_name
        AchievementId.PERFECT_MATCH -> R.string.achievement_perfect_match_name
        AchievementId.MATCHES_10 -> R.string.achievement_matches_10_name
        AchievementId.MATCHES_50 -> R.string.achievement_matches_50_name
        AchievementId.MATCHES_100 -> R.string.achievement_matches_100_name
        AchievementId.STREAK_7 -> R.string.achievement_streak_7_name
        AchievementId.STREAK_30 -> R.string.achievement_streak_30_name
        AchievementId.FIRST_SPARRING -> R.string.achievement_first_sparring_name
        AchievementId.PUZZLE_FULL_DAY -> R.string.achievement_puzzle_full_day_name
        AchievementId.PUZZLE_WEEK -> R.string.achievement_puzzle_week_name
    }

/** Riga che dice come si ottiene l'obiettivo. */
internal val AchievementId.descriptionRes: Int
    @StringRes get() = when (this) {
        AchievementId.FIRST_WIN -> R.string.achievement_first_win_desc
        AchievementId.BEAT_GIGI -> R.string.achievement_beat_gigi_desc
        AchievementId.BEAT_NONNA_ROSA -> R.string.achievement_beat_nonna_rosa_desc
        AchievementId.BEAT_TONINO -> R.string.achievement_beat_tonino_desc
        AchievementId.BEAT_DON_ALFONSO -> R.string.achievement_beat_don_alfonso_desc
        AchievementId.BEAT_MAESTRO -> R.string.achievement_beat_maestro_desc
        AchievementId.LADDER_COMPLETE -> R.string.achievement_ladder_complete_desc
        AchievementId.FIRST_SWEEP -> R.string.achievement_first_sweep_desc
        AchievementId.TEN_SWEEPS -> R.string.achievement_ten_sweeps_desc
        AchievementId.PERFECT_MATCH -> R.string.achievement_perfect_match_desc
        AchievementId.MATCHES_10 -> R.string.achievement_matches_10_desc
        AchievementId.MATCHES_50 -> R.string.achievement_matches_50_desc
        AchievementId.MATCHES_100 -> R.string.achievement_matches_100_desc
        AchievementId.STREAK_7 -> R.string.achievement_streak_7_desc
        AchievementId.STREAK_30 -> R.string.achievement_streak_30_desc
        AchievementId.FIRST_SPARRING -> R.string.achievement_first_sparring_desc
        AchievementId.PUZZLE_FULL_DAY -> R.string.achievement_puzzle_full_day_desc
        AchievementId.PUZZLE_WEEK -> R.string.achievement_puzzle_week_desc
    }

private val levelNameRes = intArrayOf(
    R.string.experience_level_1,
    R.string.experience_level_2,
    R.string.experience_level_3,
    R.string.experience_level_4,
    R.string.experience_level_5,
    R.string.experience_level_6,
    R.string.experience_level_7,
    R.string.experience_level_8,
    R.string.experience_level_9,
    R.string.experience_level_10,
)

/** Nome del [level] di esperienza (da 1 a [Experience.MAX_LEVEL]). */
@StringRes
internal fun experienceLevelNameRes(level: Int): Int = levelNameRes[(level - 1).coerceIn(0, Experience.MAX_LEVEL - 1)]
