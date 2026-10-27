package it.maestro.app.feature.missions.ui

import androidx.annotation.StringRes
import it.maestro.app.R
import it.maestro.app.domain.retention.DailyMissionType

/** Titolo della missione: il dominio conosce i tipi, i testi stanno nella UI (ADR-0007, `layers.md`). */
internal val DailyMissionType.titleRes: Int
    @StringRes get() = when (this) {
        DailyMissionType.SOLVE_DAILY_PUZZLE -> R.string.mission_daily_puzzle_title
        DailyMissionType.COMPLETE_MATCH_WITH_ACCURACY_80 -> R.string.mission_accuracy_80_title
        DailyMissionType.ANSWER_SEVENS_CHECKS -> R.string.mission_sevens_check_title
        DailyMissionType.MAKE_SWEEPS -> R.string.mission_sweeps_title
        DailyMissionType.CAPTURE_DENARI -> R.string.mission_denari_title
        DailyMissionType.PLAY_MATCH_WITHOUT_BLUNDERS -> R.string.mission_no_blunders_title
    }

/** Descrizione della missione. */
internal val DailyMissionType.descriptionRes: Int
    @StringRes get() = when (this) {
        DailyMissionType.SOLVE_DAILY_PUZZLE -> R.string.mission_daily_puzzle_desc
        DailyMissionType.COMPLETE_MATCH_WITH_ACCURACY_80 -> R.string.mission_accuracy_80_desc
        DailyMissionType.ANSWER_SEVENS_CHECKS -> R.string.mission_sevens_check_desc
        DailyMissionType.MAKE_SWEEPS -> R.string.mission_sweeps_desc
        DailyMissionType.CAPTURE_DENARI -> R.string.mission_denari_desc
        DailyMissionType.PLAY_MATCH_WITHOUT_BLUNDERS -> R.string.mission_no_blunders_desc
    }
