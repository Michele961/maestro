package it.maestro.app.domain.guide

import it.maestro.app.R

/** Le stringhe della guida (`guide_*`, ADR-0007): quale testo dice il Maestro per ogni passo e tono. */
public object GuideTexts {
    /** La risorsa della battuta [line], o `null` se per quel passo e tono non c'è nulla da dire (nessuna mossa sbagliata). */
    public fun res(line: GuideLine): Int? = when (line.step) {
        GuideStep.PICK -> choose(line.tone, R.string.guide_pick_instr, R.string.guide_pick_ok, null)
        GuideStep.SINGLE -> choose(line.tone, R.string.guide_single_instr, R.string.guide_single_ok, R.string.guide_single_wrong)
        GuideStep.SUM -> choose(line.tone, R.string.guide_sum_instr, R.string.guide_sum_ok, R.string.guide_sum_wrong)
        GuideStep.LAY -> choose(line.tone, R.string.guide_lay_instr, R.string.guide_lay_ok, null)
        GuideStep.SCOPA -> choose(line.tone, R.string.guide_scopa_instr, R.string.guide_scopa_ok, R.string.guide_scopa_wrong)
        GuideStep.SETTE -> choose(line.tone, R.string.guide_sette_instr, R.string.guide_sette_ok, R.string.guide_sette_wrong)
        GuideStep.END -> if (line.tone == GuideTone.INSTRUCTION) R.string.guide_end_instr else R.string.guide_end_ok
    }

    private fun choose(tone: GuideTone, instruction: Int, ok: Int, wrong: Int?): Int? = when (tone) {
        GuideTone.INSTRUCTION -> instruction
        GuideTone.OK -> ok
        GuideTone.WRONG -> wrong
    }
}
