# Audio — prompt per generare gli asset

Per Michele (`settings.md`, decisione 4; `docs/app/game-feel.md` §7). Formato: `ogg`/`wav` 44,1 kHz; musica e ambiente in **loop senza giunture**; a volume medio-basso (il mixer applica i volumi). Se il generatore ha limiti di durata, creare 30–60 s e fare il loop.

## Musica (circolo)

1. **Circolo, giorno**: «Warm Italian cafe background, nylon-string guitar and soft mandolin, slow 70 BPM, gentle fingerpicking, nostalgic, no vocals, no drums, seamless loop, 90 seconds.»
2. **Circolo, sera**: «Slow Italian folk, acoustic guitar with light accordion pad, relaxed 60 BPM, intimate smoky club mood, no vocals, seamless loop.»
3. **Partita tesa** (ultima mano): «Same instruments, slightly faster 85 BPM, sparse mandolin tremolo, subtle tension, no percussion, seamless loop.»
4. **Vittoria**: «Short 6-second Italian guitar and mandolin flourish, joyful, resolves on a major chord, no vocals.»
5. **Sconfitta**: «Short 6-second gentle guitar phrase, wistful but kind, resolves softly, no vocals.»

## Ambiente

6. **Brusio del bar**: «Quiet Italian bar ambience, distant chatter in Italian (indistinct), espresso cups clinking, soft chair movements, no music, seamless loop, 60 seconds.»
7. **Tavolo da gioco**: «Room tone of a wooden card table, very low, occasional subtle wood creaks, no voices, seamless loop.»

## Effetti (da rifare o integrare quelli esistenti in `res/raw/`)

8. Carta giocata sul tavolo (colpo secco su feltro), 9. presa (due carte raccolte), 10. scopa (swish + colpo più pieno), 11. Check giusto (campanello breve caldo), 12. Check sbagliato (tono basso morbido, non punitivo), 13. click di pulsante (legno leggero), 14. sblocco obiettivo (due note ascendenti di mandolino), 15. acquisto nella Bottega (monete leggere + chiusura di cassetto).

Ogni file: nome in minuscolo con underscore (`music_circle_day.ogg`, `ambient_bar.ogg`, `sfx_card_play.wav`…). Me li passi nella cartella `app/android/src/main/res/raw/` e li collego io nel task F1R-20.
