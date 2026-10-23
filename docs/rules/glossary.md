# Glossario

Fonte unica per termini di gioco, nomi nel codice e traduzioni. Se un termine manca, **non inventarlo**: chiedi (`CLAUDE.md` §5).

## Regola dei nomi nel codice

- Concetti generici di programmazione in inglese: `ScopaState`, `ScopaMove`, `ScopaView`, `Seat`, `PlayResult`, `MatchLog`.
- Termini di gioco senza un equivalente inglese preciso in italiano, come nella colonna "Nome nel codice": `Suit.DENARI`, `Scope`, `Primiera`, `Settebello`.
- Nessun testo per l'utente in `core:*`: solo enum/codici che la UI traduce (ADR-0007).

## Carte

| Italiano | Nome nel codice | English (UI) | Note |
| --- | --- | --- | --- |
| Denari | `Suit.DENARI` | Coins | Seme che conta per il punto "denari" |
| Coppe | `Suit.COPPE` | Cups | |
| Spade | `Suit.SPADE` | Swords | |
| Bastoni | `Suit.BASTONI` | Clubs | |
| Asso | `Rank.ASSO` (valore 1) | Ace | |
| Due … Sette | `Rank.DUE` … `Rank.SETTE` (2–7) | Two … Seven | |
| Fante | `Rank.FANTE` (valore 8) | Jack | |
| Cavallo | `Rank.CAVALLO` (valore 9) | Knight | Nelle piacentine si chiama anche "donna" o "cavallo" a seconda del mazzo |
| Re | `Rank.RE` (valore 10) | King | |
| Mazzo napoletano | `DeckTheme.NAPOLETANE` | Neapolitan deck | Tema grafico, non influisce sulle regole |

## Partita

| Italiano | Nome nel codice | English (UI) | Definizione |
| --- | --- | --- | --- |
| Partita | `Match` | Match | Insieme di smazzate fino al punteggio obiettivo |
| Smazzata | `Deal` | Deal / Round | Dal mescolamento all'esaurimento del mazzo e delle mani |
| Mano (di carte) | `Hand` | Hand | Le carte in mano a un giocatore |
| Distribuzione | `Distribution` | Deal of cards | Ogni volta che si danno 3 carte a testa |
| Mazziere | `dealer: Seat` | Dealer | Chi distribuisce; alterna a ogni smazzata |
| Tavolo | `table` | Table | Carte scoperte al centro |
| Giocata | `ScopaMove` | Play | Una carta dalla mano, con la presa scelta |
| Presa | `Capture` | Capture | Le carte prese dal tavolo con una giocata |
| Scopa | `Sweep` (evento) / `Scope` (conteggio) | Sweep (scopa) | Presa che svuota il tavolo (vedi spec §5) |
| Ultima presa | `lastCapturer: Seat` | Last capture | Chi prende le carte rimaste a fine smazzata |
| Posto | `Seat` | Seat | `Seat.P1`, `Seat.P2` nella partita a 2 |

## Punteggio

| Italiano | Nome nel codice | English (UI) | Definizione |
| --- | --- | --- | --- |
| Carte | `ScoreCategory.CARTE` | Cards | Più carte prese |
| Denari | `ScoreCategory.DENARI` | Coins | Più carte di denari prese |
| Settebello | `ScoreCategory.SETTEBELLO` | Settebello (7 of coins) | Aver preso il 7 di denari |
| Primiera | `ScoreCategory.PRIMIERA` | Primiera | Vedi spec §6.2 |
| Scope | `ScoreCategory.SCOPE` | Sweeps | Un punto per ogni scopa |
| Re bello | `ScoreCategory.RE_BELLO` | Re bello (king of coins) | Solo se la variante è attiva |

## Maestro (AI)

| Italiano | Nome nel codice | English (UI) | Definizione |
| --- | --- | --- | --- |
| Mossa Magistrale | `MoveGrade.BEST` | Brilliant / Best | Mossa con perdita entro la soglia minima |
| Imprecisione | `MoveGrade.INACCURACY` | Inaccuracy | Perdita piccola |
| Errore grave | `MoveGrade.BLUNDER` | Blunder | Perdita grave |
| Precisione | `accuracy` | Accuracy | Percentuale calcolata dall'analizzatore |
| Check | `TrainingCheck` | Check | Quiz durante la partita di allenamento |
