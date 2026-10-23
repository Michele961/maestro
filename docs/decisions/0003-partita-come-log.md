# ADR-0003 — Una partita è seed + configurazione + mosse

Stato: accettato · 2026-10-23

## Contesto
Replay, Game Review, riconnessione, test e anti-cheat richiedono di ricostruire esattamente una partita.

## Decisione
Il `MatchLog` contiene `rulesVersion`, configurazione, seed e lista ordinata delle mosse. Lo stato si ottiene sempre rigiocando il log. Il motore è una funzione pura `stato + mossa → nuovo stato + eventi`.

## Conseguenze
- Ogni cambiamento di regole che altera l'esito di un log incrementa `CoreGame.RULES_VERSION`.
- I log salvati diventano fixture di regressione.
- Nessuna casualità fuori dal seed (ADR-0009).
