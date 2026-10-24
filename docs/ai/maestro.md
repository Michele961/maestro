# Il Maestro — design del motore AI

Stato: approvato a livello di architettura · 2026-10-23. Soglie e parametri numerici sono **provvisori** finché la Fase 0 non li misura.

## Principi

1. **Solo informazione lecita.** Bot e valutatore ricevono `ScopaView` (la vista del giocatore), mai `ScopaState`. Le carte nascoste si stimano, non si leggono. L'analizzatore può rigiocare il log per ricostruire la partita, ma chiama il valutatore solo con `viewFor(stato, chi muove)`. Gli stati ipotetici del PIMC si costruiscono con `determinize()` di `core:game`.
2. **Deterministico.** Ogni scelta usa un `SeededRng` ricevuto come parametro. Stesso input + stesso seed = stessa mossa.
3. **Un motore, più usi.** Lo stesso valutatore serve a bot, review, Check e puzzle.
4. **Budget di tempo esplicito.** Ogni chiamata accetta un budget in campioni/nodi, non in millisecondi (il tempo reale non è deterministico).

## Interfacce attese (`core:ai`)

```kotlin
interface Bot {
    fun chooseMove(view: ScopaView, legal: List<ScopaMove>, rng: SeededRng): ScopaMove
}

interface MoveEvaluator {
    // Valore atteso di ogni mossa legale per il giocatore di turno, nella sua vista.
    fun evaluate(view: ScopaView, legal: List<ScopaMove>, rng: SeededRng, budget: SearchBudget): List<MoveValue>
}
```

I nomi esatti si fissano nei task F0-08 e F0-10; non introdurre interfacce alternative.

## Bot

| Bot | Task | Comportamento |
| --- | --- | --- |
| `RandomBot` | F0-08 | Mossa legale a caso (con `rng`) |
| `GreedyBot` | F0-08 | Tra le mosse con presa sceglie in ordine lessicografico: fa scopa > prende il settebello > più denari presi > più carte prese; parità → prima in `legalMoves`. Se nessuna mossa prende, scarta la carta con valore di presa più basso; parità → prima in `legalMoves` |
| `PimcBot` | F0-10 | Campionamento delle mani nascoste (PIMC) + ricerca a informazione perfetta |

### PIMC (Perfect Information Monte Carlo)

1. Dalla vista si ricava l'insieme delle carte non viste (mano avversaria + mazzo).
2. Si generano N distribuzioni compatibili: la mano avversaria ha la dimensione nota, il resto va nel mazzo in ordine casuale.
3. Per ogni distribuzione si cerca la mossa migliore a informazione perfetta (minimax con potatura alfa-beta), fino a fine smazzata se il budget di nodi lo consente, altrimenti fino alla profondità data.
   - A fine smazzata il valore è esatto: punti di smazzata del giocatore − punti dell'avversario (spec §6).
   - A profondità limitata vale la **valutazione provvisoria**: scope fatte × 1 + settebello preso × 1 + 0,1 × (differenza denari presi) + 0,05 × (differenza carte prese) + 0,025 × (differenza delle somme di primiera parziali), sempre dal punto di vista di chi muove (decisione D2, task F1-03). I pesi sono costanti nominate in un solo punto (`PimcEvaluator.kt`).
4. Valore di una mossa = media sui campioni. Si sceglie il massimo; parità → ordine canonico.

**Ultima distribuzione:** quando il mazzo è vuoto, la mano avversaria si deduce per esclusione: la ricerca diventa esatta, senza campionamento (F0-10).

**Se PIMC è troppo lento o debole** sulla Scopa, l'alternativa prevista è ISMCTS. Il passaggio richiede l'approvazione di Michele, con i dati del simulatore.

## Livelli di difficoltà (Fase 1)

Ricavati dallo stesso `PimcBot` (decisione D3) variando i parametri: numero di campioni, nodi, rumore nella scelta e memoria imperfetta (il bot "dimentica" stocasticamente una frazione delle carte passate, trattandole come ancora incognite).

| Livello | Target Elo | Campioni ($s$) | Max Nodi | Rumore ($\epsilon$) | Forget Rate |
| --- | --- | --- | --- | --- | --- |
| `PRINCIPIANTE` | 800 | 2 | 10 | 0.35 | 0.60 |
| `DILETTANTE` | 1100 | 5 | 25 | 0.18 | 0.35 |
| `INTERMEDIO` | 1400 | 12 | 60 | 0.05 | 0.15 |
| `AVANZATO` | 1700 | 25 | 120 | 0.00 | 0.00 |
| `MAESTRO` | 2000 | 50 | 200 | 0.00 | 0.00 |

## Analizzatore (F0-11)

- Per ogni mossa del log: valore di tutte le mosse legali dal punto di vista di chi muove, **con la sola informazione che aveva in quel momento**.
- Perdita = valore della mossa migliore − valore della mossa giocata (≥ 0).
- Classificazione provvisoria (unità: punti di smazzata attesi):

| Classe | Condizione provvisoria |
| --- | --- |
| `BEST` (Blu) | perdita ≤ 0,05 |
| `INACCURACY` (Giallo) | 0,05 < perdita ≤ 0,30 |
| `BLUNDER` (Rosso) | perdita > 0,30 |

- Precisione % della partita, formula provvisoria: `100 × exp(−3 × perdita media)`, arrotondata all'intero, con test a valori fissi. La formula definitiva si decide dopo la revisione con un giocatore esperto (F0-12).

## Check e puzzle (fasi successive)

- Check: domande calcolate dalla vista (carte di un seme uscite, sette passati, chi è avanti in primiera). Nessuna AI necessaria; task in Fase 1.
- Puzzle: generati offline dal risolutore esatto, cercando posizioni con un'unica sequenza vincente. Task in Fase 3.

## Metriche da misurare in Fase 0

- Percentuale di vittorie su 1000 partite: `PimcBot` vs `GreedyBot`, `GreedyBot` vs `RandomBot`.
- Tempo medio e al 95° percentile per mossa su JVM, con diversi numeri di campioni.
- Distribuzione delle perdite e delle classi su partite bot contro bot.
