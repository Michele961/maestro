# Backlog — Fase 0: motore headless

Obiettivo della fase: regole della Scopa complete e testate, bot di riferimento, bot PIMC, simulatore e prima versione dell'analizzatore. Nessuna UI.

**Gate di uscita:** `PimcBot` vince più del 60% delle partite contro `GreedyBot` su 1000 partite (valore provvisorio, da confermare in F0-13), p95 del tempo per mossa sotto 1 s sul runner `ubuntu-latest` di GitHub Actions al budget scelto, etichette della review giudicate sensate da un giocatore esperto (F0-12).

Lo stato di ogni task è **solo** nella sua riga **Stato** (`da fare` · `in corso` · `in revisione` · `fatto` · `bloccato`, vedi `docs/workflow.md`). Gli agenti modificano solo **Stato** e **Domande aperte** del proprio task.

| ID | Titolo | Dipende da |
| --- | --- | --- |
| F0-01 | Build verde in CI | — |
| F0-02 | Generatore con seed e mescolamento | F0-01 |
| F0-03 | Modello delle carte e notazione di test | F0-01 |
| F0-04 | Configurazione, nuova partita e distribuzione iniziale | F0-02, F0-03 |
| F0-05 | Mosse legali, giocata e ridistribuzioni | F0-04 |
| F0-06 | Fine smazzata, punteggio e fine partita | F0-05 |
| F0-07 | Vista del giocatore, log, replay e determinizzazione | F0-06 |
| F0-08 | Bot casuale e avido | F0-07 |
| F0-09 | Simulatore da riga di comando | F0-08 |
| F0-10 | Bot PIMC e risolutore dell'ultima distribuzione | F0-09 |
| F0-11 | Analizzatore delle mosse | F0-10 |
| F0-12 | Revisione con giocatore esperto (Michele) | F0-11 |
| F0-13 | Report di fine fase | F0-12 |

---

## F0-01 — Build verde in CI

- **Stato:** fatto
- **Modalità:** sessione interattiva con Michele (le modifiche ai file di build chiedono conferma).
- **Riferimenti:** `docs/architecture.md`, `build.gradle.kts`, `.github/workflows/ci.yml`
- **Obiettivo:** lo scaffold esistente compila e `./gradlew check` passa su GitHub Actions e in locale (JDK 21 obbligatorio, nessun toolchain resolver).
- **Perimetro autorizzato:**
    - Correzioni necessarie a far compilare i file di build esistenti (`**/build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `gradle.properties`).
    - Attivare `explicitApi()` nei moduli `core:*`.
    - Versioni nel catalog: solo se quelle attuali non si risolvono, scegliendo la più recente stabile della stessa minor.
    - `.github/workflows/ci.yml`: solo se il workflow attuale non parte; nessun passo nuovo.
- **Fuori perimetro:** codice di dominio, nuove dipendenze o plugin, ktlint/detekt.
- **Criteri di accettazione:**
    - [ ] `./gradlew check` verde in CI su `main`.
    - [ ] Aggiungendo a mano `implementation(libs.kotlinx.serialization.json)` a `core:game`, `./gradlew :core:game:checkModuleBoundaries` fallisce citando la libreria (verificato in locale, modifica rimossa, descritto nella PR).
    - [ ] `./gradlew :sim:run --args="--games 10 --seed 1"` stampa il messaggio di placeholder.
- **Domande aperte:** —

## F0-02 — Generatore con seed e mescolamento

- **Stato:** fatto
- **Riferimenti:** ADR-0009 (dettagli vincolanti), spec §3.1
- **Obiettivo:** `SeededRng` in `it.maestro.game.rng` con `nextLong()`, `nextInt(bound)`, `fork()`, `state` esportabile e ripristinabile, e `shuffle` Fisher–Yates che restituisce una nuova lista. Algoritmi esattamente come in ADR-0009.
- **Fuori perimetro:** uso di `kotlin.random`.
- **Criteri di accettazione:**
    - [ ] Vettori di riferimento SplitMix64 (primi 5 `nextLong()`, come unsigned esadecimale):
        - seed 0: `e220a8397b1dcdaf`, `6e789e6aa1b965f4`, `06c45d188009454f`, `f88bb8a8724c81ec`, `1b39896a51a8749b`
        - seed 42: `bdd732262feb6e95`, `28efe333b266f103`, `47526757130f9f52`, `581ce1ff0e4ae394`, `09bc585a244823f2`
    - [ ] `nextInt(bound)` rifiuta `bound <= 0` con `require`; rejection sampling come da ADR-0009.
    - [ ] `shuffle` è una permutazione ed è identico a parità di seed.
    - [ ] Uniformità: 100.000 mescolamenti di 40 elementi con seed 1; ogni elemento compare in ogni posizione con frequenza entro ±20% dell'attesa (2.500).
- **Domande aperte:** —

## F0-03 — Modello delle carte e notazione di test

- **Stato:** fatto
- **Riferimenti:** spec §1, `docs/rules/glossary.md`, `docs/conventions.md` (ordine canonico)
- **Obiettivo:** in `it.maestro.game.model`: `Suit`, `Rank` (con `value` 1–10), `Card`, `Seat` (`P1`, `P2`, `other()`), `fullDeck()` in ordine canonico, `CardOrder` (comparator canonico). Helper **solo di test** `card("7D")` / `cards("AD", "3C")`.
- **Notazione:** simbolo del valore `A`, `2`…`7`, `F`, `C`, `R` + seme `D`, `C`, `S`, `B`. Ogni altro simbolo (es. `8D`, `10D`, `KD`) è invalido.
- **Fuori perimetro:** valori di primiera (F0-06), testi per l'utente.
- **Criteri di accettazione:**
    - [ ] Il mazzo ha 40 carte distinte, in ordine canonico.
    - [ ] `Rank.value` corrisponde alla spec §1.2.
    - [ ] Il parser di test rifiuta notazioni non valide con messaggio chiaro.
- **Domande aperte:** —

## F0-04 — Configurazione, nuova partita e distribuzione iniziale

- **Stato:** fatto
- **Riferimenti:** spec §2, §3.1–3.3, §8; ADR-0009 (ordine di consumo), ADR-0011
- **Obiettivo:** in `it.maestro.game.scopa`: `ScopaConfig` (solo i campi di §8, validati), `ScopaState` immutabile (contiene lo stato di `SeededRng`), `newMatch(config, seed)` con primo mazziere, mescolamento, distribuzione iniziale e regola dei tre Re. Porta `CoreGame.RULES_VERSION` a `1` e aggiorna il relativo test (autorizzato).
- **Perimetro autorizzato:** helper di test che costruisce uno stato da un ordine di mazzo dato, per forzare i casi di §3.3.
- **Fuori perimetro:** giocate e ridistribuzioni (F0-05).
- **Criteri di accettazione:**
    - [ ] Stesso seed → stato iniziale identico; 100 seed diversi → almeno 99 stati diversi.
    - [ ] Dopo `newMatch`: 3 carte a testa, 4 sul tavolo, 30 nel mazzo; di turno l'avversario del mazziere; distribuzione come in §3.2.
    - [ ] §3.3 testato con tavolo a 2, 3 e 4 Re, anche con `redealOnThreeKings = false`.
    - [ ] Invariante delle 40 carte su 500 seed.
- **Domande aperte:** —

## F0-05 — Mosse legali, giocata e ridistribuzioni

- **Stato:** fatto
- **Riferimenti:** spec §3.4, §4, §5; `docs/architecture.md` (forma dell'API)
- **Obiettivo:** `ScopaMove(card, capture: Set<Card>)`, `legalMoves(state)`, `play(state, move): PlayResult`, ridistribuzione automatica (§3.4).
- **Nomi da usare:**
    - `GameEvent`: `CardPlayed(seat, card)`, `Captured(seat, cards)`, `Sweep(seat)`, `CardsDealt(distribution)`.
    - `RejectReason`: `NOT_IN_LEGAL_MOVES`, `DEAL_OVER`, `MATCH_OVER`.
    - Fase della smazzata in `ScopaState`: `DealPhase.PLAYING`, `DealPhase.DEAL_OVER`.
- **Comportamento di confine:** dopo l'ultima giocata della smazzata lo stato passa a `DEAL_OVER` (nessun punteggio: arriva in F0-06), `legalMoves()` è vuota e `play()` restituisce `Rejected(DEAL_OVER)`.
- **Fuori perimetro:** carte residue, punteggio, nuova smazzata (F0-06).
- **Criteri di accettazione:**
    - [ ] Tutti i casi di §10 relativi a §3.4, §4 e §5 hanno un test dedicato.
    - [ ] Esempio §4.8 riprodotto esattamente, con l'ordine di §4.9.
    - [ ] Una mossa non presente in `legalMoves` → `Rejected(NOT_IN_LEGAL_MOVES)`, stato invariato.
    - [ ] Su 500 seed, giocando sempre la prima mossa legale, ogni smazzata arriva a `DEAL_OVER` con mani e mazzo vuoti e invariante delle 40 carte rispettata.
- **Domande aperte:** —

## F0-06 — Fine smazzata, punteggio e fine partita

- **Stato:** fatto
- **Riferimenti:** spec §6, §7
- **Obiettivo:** carte residue all'ultima presa, punti per categoria (primiera e `reBello` inclusi), totali, nuova smazzata con cambio mazziere, fine partita, tutto dentro `play()` come da §7.5. `DealPhase.DEAL_OVER` diventa uno stato di passaggio non osservabile dall'esterno, a meno che la partita sia finita.
- **Nomi da usare:** `DealScore` (punti per `ScoreCategory` e per `Seat`, più le somme di primiera di entrambi), eventi `DealEnded(score: DealScore)` e `MatchEnded(winner: Seat, totals)`, `DealPhase.MATCH_OVER`.
- **Perimetro autorizzato:** modificare il comportamento di confine di F0-05 (da `DEAL_OVER` a nuova smazzata) e aggiornare i relativi test.
- **Criteri di accettazione:**
    - [ ] Tutti i casi di §10 relativi a §6 e §7 hanno un test dedicato.
    - [ ] §7.6: a partita finita `legalMoves()` è vuota e `play()` dà `Rejected(MATCH_OVER)`.
    - [ ] Su 500 seed, la partita termina sempre e il vincitore rispetta §7.2–7.3.
- **Domande aperte:** —

## F0-07 — Vista del giocatore, log, replay e determinizzazione

- **Stato:** fatto
- **Riferimenti:** ADR-0002, ADR-0003, `docs/architecture.md`
- **Obiettivo:** `viewFor(state, seat)`, `MatchLog`, `replay(log): ReplayResult`, `determinize(view, opponentHand, deckOrder)`, formato testuale del log.
- **Campi di `ScopaView`:** `seat`, `config`, `dealer`, `toMove`, `phase`, `hand` (propria), `table`, `capturedBy` (prese di entrambi), `playedCards` (carte uscite nella smazzata, in ordine), `opponentHandSize`, `deckSize`, `sweeps` (per giocatore), `totals` (punteggi di partita). **Mai** la mano avversaria, l'ordine del mazzo o lo stato del generatore.
- **`ReplayResult`:** `Ok(state)`, `WrongRulesVersion(found, expected)`, `IllegalMove(index)`.
- **Formato testuale del log** (UTF-8, una voce per riga):
    ```
    maestro-log 1
    rules 1
    config targetScore=11 redealOnThreeKings=true reBello=false
    seed 42
    move 7D AD,6C
    move 3S -
    ```
    `move <carta> <presa separata da virgole in ordine canonico | ->`. Funzioni `MatchLog.toText()` e `MatchLog.parse(text)`, con parse che rifiuta righe sconosciute.
- **Criteri di accettazione:**
    - [ ] Su 500 seed, a ogni turno, la vista non contiene la mano avversaria né l'ordine del mazzo (test sul contenuto, non solo sul tipo).
    - [ ] `replay(log)` dà lo stesso stato finale della partita originale su 500 seed; `toText` → `parse` è un'identità.
    - [ ] `rulesVersion` diversa → `WrongRulesVersion`; mossa illegale → `IllegalMove(index)`.
    - [ ] `determinize` rifiuta ipotesi incompatibili con la vista e, con le carte vere, ricostruisce uno stato equivalente all'originale.
    - [ ] Almeno 3 log testuali come fixture di regressione con stato finale atteso.
- **Domande aperte:** —

## F0-08 — Bot casuale e avido

- **Stato:** fatto
- **Riferimenti:** `docs/ai/maestro.md` (Bot)
- **Obiettivo:** `Bot`, `RandomBot`, `GreedyBot` in `it.maestro.ai.bot`, con le regole di scelta esattamente come nella tabella dei bot.
- **Criteri di accettazione:**
    - [ ] I bot ricevono solo `ScopaView` (firma).
    - [ ] Deterministici a parità di vista e seed.
    - [ ] `GreedyBot`: test costruiti a mano per scopa, settebello, scarto della carta più bassa.
- **Domande aperte:** —

## F0-09 — Simulatore da riga di comando

- **Stato:** fatto
- **Riferimenti:** `sim/README.md`, `docs/ai/maestro.md` (Metriche)
- **Obiettivo:** `./gradlew :sim:run --args="--games N --seed S --p1 <random|greedy> --p2 <random|greedy> [--out file.json] [--save-logs dir]"`.
- **Regole del simulatore:**
    - `master = SeededRng(S)`; per la partita `i` (da 0): seed della partita = `master.nextLong()`, seed del bot di P1 e di P2 = i due `nextLong()` successivi.
    - Nelle partite pari il bot `--p1` siede in `P1`, nelle dispari in `P2`.
    - Report su stdout: vittorie per bot, punti medi per categoria, tempo medio e p95 per mossa.
    - JSON (scritto a mano, senza librerie): `{"games":N,"seed":S,"bots":{"p1":"...","p2":"..."},"wins":{"p1":x,"p2":y},"avgPoints":{"p1":{"CARTE":..,...},"p2":{...}},"msPerMove":{"mean":..,"p95":..}}`.
- **Perimetro autorizzato:** in `.github/workflows/ci.yml`, solo gli argomenti del passo "Smoke test del simulatore".
- **Criteri di accettazione:**
    - [ ] Stessi argomenti → stesso report (escluso il tempo).
    - [ ] Argomenti non validi → messaggio d'uso ed exit code ≠ 0.
    - [ ] `GreedyBot` batte `RandomBot` in più del 50% di 1000 partite (valore nella PR).
    - [ ] `--save-logs` scrive i log nel formato di F0-07.
- **Domande aperte:** —

## F0-10 — Bot PIMC e risolutore dell'ultima distribuzione

- **Stato:** fatto
- **Riferimenti:** `docs/ai/maestro.md` (PIMC)
- **Obiettivo:** `MoveEvaluator` PIMC con `determinize()`, `PimcBot`, ricerca esatta a mazzo vuoto, `SearchBudget(samples, maxNodes)`, valutazione provvisoria come da documento. Il simulatore accetta `--p1 pimc` / `--p2 pimc` con `--samples K`.
- **Perimetro autorizzato:** aggiungere `pimc` e `--samples` al simulatore.
- **Criteri di accettazione:**
    - [ ] Le distribuzioni campionate sono compatibili con la vista (nessuna carta già vista, dimensioni corrette).
    - [ ] Il risolutore esatto trova la mossa ottima su almeno 5 posizioni finali costruite a mano con soluzione calcolata a mano e spiegata nel test.
    - [ ] Report allegato alla PR: `PimcBot` vs `GreedyBot` su 1000 partite, tempo medio e p95 per `samples` = 20, 50, 100.
- **Domande aperte:** —

## F0-11 — Analizzatore delle mosse

- **Stato:** fatto
- **Riferimenti:** `docs/ai/maestro.md` (Analizzatore), ADR-0005
- **Obiettivo:** in `it.maestro.ai.analysis`: analisi di un `MatchLog` con valore delle alternative, perdita, `MoveGrade`, precisione per giocatore. Il simulatore accetta `--analyze <file di log>`.
- **Perimetro autorizzato:** aggiungere `--analyze` al simulatore.
- **Criteri di accettazione:**
    - [ ] Il valutatore riceve solo `viewFor(stato, chi muove)` (test).
    - [ ] Soglie e formula della precisione come costanti nominate in un solo punto.
    - [ ] Output leggibile per mossa: numero, carta e presa giocate, migliori 3 alternative con valore, classe.
- **Domande aperte:** —

## F0-12 — Revisione con giocatore esperto

- **Stato:** fatto
- **Esecutore:** Michele (non un agente)
- **Obiettivo:** analizzare 20 partite con l'output di F0-11 e annotare dove le classi non convincono.
- **Uscita:** note in `docs/backlog/f0-12-note.md`, usate per tarare soglie, pesi e formula della precisione.

## F0-13 — Report di fine fase

- **Stato:** fatto
- **Obiettivo:** `docs/reports/fase-0.md` con le metriche di `docs/ai/maestro.md`, l'esito del gate, proposte di pesi, soglie e parametri per i livelli di difficoltà. Michele decide il passaggio alla Fase 1 e conferma o cambia i valori provvisori.
