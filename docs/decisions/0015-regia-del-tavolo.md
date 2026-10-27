# ADR-0015 — Regia del tavolo guidata dagli eventi

Stato: accettato · 2026-10-27 · approvato da Michele in chat

## Contesto
Oggi lo stato della partita salta subito al risultato finale e la UI ricostruisce le animazioni con ritardi scritti nei composable (`delay(160L)`, `while (…) delay(40L)`). Il bot muove dopo tempi fissi decisi nel reducer (`BOT_NORMAL_MOVE_DELAY_MS = 1400L`, `BOT_HAND_DEAL_DELAY_MS = 2600L`) e sperati più lunghi dell'animazione. Quando non lo sono, un'animazione interrompe l'altra; su dispositivi più lenti o con l'utente che trascina una carta il problema peggiora. Le PR #63–#69 correggono sintomi dello stesso difetto.

## Decisione
1. **Il motore produce battute, la regia le mette in scena.** Ogni mossa applicata produce una `Beat`: chi ha mosso, gli eventi di `core:game` (`CardPlayed`, `Captured`, `Sweep`, `CardsDealt`, `DealEnded`, `MatchEnded`) e la vista del giocatore umano prima e dopo. Nessuna carta nascosta entra in una `Beat`.
2. **Un solo `TableDirector` per tavolo** riproduce le battute in coda, una alla volta, e pubblica la **scena** visibile (`TableScene`): cosa mostrare, dove, in quale fase dell'animazione. La UI disegna solo la scena.
3. **Il bot aspetta la regia, non un timer.** `MatchSession` (dominio) chiede la mossa al bot solo dopo che la regia ha confermato di aver finito la battuta precedente (`PresentationGate.awaitIdle()`), poi aggiunge un tempo di riflessione preso dalla tabella dei tempi.
4. **Tutti i tempi stanno in una tabella sola** (`MotionSpec` in `app:designsystem`), moltiplicata dalla velocità di gioco scelta dall'utente. È vietato scrivere durate o `delay()` altrove.
5. **Ogni scena si può saltare.** Un tocco porta la regia alla fine della battuta corrente; lo stato della partita non cambia.
6. **La regia si testa senza Compose**, con tempo virtuale (`runTest`, `advanceTimeBy`).

## Conseguenze
- Impossibile che un'animazione parta "quando non dovrebbe": c'è una sola coda e un solo orologio.
- Rotazione o ritorno in app: la scena si ricostruisce dall'ultima vista, senza rigiocare animazioni a metà.
- La stessa regia serve a Review, "Rigioca l'errore", Sfida del giorno e, in Fase 3, alle partite online (gli eventi arriveranno dal server).
- Specifica completa: `docs/app/table-director.md`.
