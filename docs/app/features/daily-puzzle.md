# Sfida del Giorno — dossier

Stato: **bozza** (da approvare) · 2026-10-28 · task collegati: F1R-23 (dossier), F1R-27 (da creare dopo l'approvazione), F1R-14, F1R-16
Procedimento: `docs/app/features/README.md`. Va approvato da Michele prima che i task partano. Le proposte non approvate sono segnate **[proposta]**.

## 1. Cosa fa per il giocatore

Ogni giorno, in cinque minuti, risolve un piccolo problema di Scopa: una posizione con una sola mossa giusta. È l'abitudine quotidiana che tiene in esercizio e fa tornare ogni giorno (`docs/vision.md`: «Sfida del Giorno»). Deve far provare la soddisfazione di aver capito qualcosa, non la frustrazione di aver sbagliato.

## 2. Com'è oggi

**Codice** (`feature/puzzle`, `data/local/DailyPuzzleProvider.kt`, `DailyPuzzleRepository.kt`): `DailyPuzzleScreen.kt` 726 righe, `DailyPuzzleViewModel.kt` 333, `DailyPuzzleProvider.kt` 158, `PuzzleShareFormatter.kt` 59. Si apre dalla Home («Sfida del giorno») e dalla missione omonima.

**Come funziona**: la data del giorno dà un seme uguale per tutti; da quel seme si generano **3 livelli** (Riscaldamento: presa di Scopa; Tattica: massimizzare i punti; Maestro: partita decisiva) con una sola soluzione. Ogni livello dà crediti (15, 20, 25) una sola volta, tramite `RewardEngine.onPuzzleSolved`. Dopo un errore il Maestro dà un indizio (due livelli); a livello risolto si vedono «concetto tattico» e «regola d'oro», si può condividere il risultato senza spoiler o rigiocare. I progressi stanno in DataStore.

**Test**: `DailyPuzzleViewModelTest`, `DailyPuzzleMultiTierTest`, `DailyPuzzleRepositoryTest`, `DayRolloverTest`, `DailyPuzzleScreenshotTest` (matrice a 4 configurazioni).

**Problemi**

| # | Problema | Dove |
| --- | --- | --- |
| D1 | **Testi italiani scritti nel codice**, contro ADR-0007: il «concetto tattico», la «regola d'oro» e il testo di condivisione («Maestro Scopa #14 — Risolto al 1° tentativo!») sono stringhe Kotlin. In inglese l'app mostrerebbe italiano | `DailyPuzzleProvider.getLogicalConcept/getGoldenRule`, `PuzzleShareFormatter` |
| D2 | **Il Maestro dice cose generiche**: gli indizi sono pochi testi fissi per obiettivo e non nominano le carte della posizione; non usano la voce del Maestro e le spiegazioni localizzate di F1R-16/F1R-24 | `puzzle_coach_hint_*` |
| D3 | **La spiegazione dopo la soluzione è lunga e astratta**: «concetto» e «regola d'oro» sono due blocchi di testo che non indicano le carte giuste | `SolvedCelebrationBanner` |
| D4 | **Gli errori contano come tentativi in vista**: il numero dei tentativi sta nel testo condiviso («Risolto al 3° tentativo»), il che spinge a nascondere gli errori invece di imparare | `PuzzleShareFormatter` |
| D5 | **Cosa succede tornando il giorno dopo non è chiaro**: la sfida di ieri sparisce senza traccia; nessun archivio né calendario dei giorni risolti | — |
| D6 | **Nessun legame con le lacune del giocatore**: i tre livelli sono uguali per tutti, anche se il giocatore è debole su un asse del radar | — |
| D7 | **Un file di 726 righe** (soglia 400): intestazione, obiettivo, area del Maestro, mano, selettore dei livelli, banner di errore e di vittoria | architettura |
| D8 | **Tempi e regole sul tavolo**: la schermata ha il suo mini-tavolo e non passa dalla regia (`TableDirector`/`MotionSpec`) | `PlayerHandSection` |
| D9 | **Crediti nei tre livelli**: sono gli unici crediti «facili» dell'app; la decisione D19 vieta i crediti di partita, ma qui sono ricompense di una sfida, non di una partita | `rewardCredits` |

## 3. Come deve diventare **[proposta]**

1. **Un solo flusso chiaro**: dalla Home si entra sul primo livello non ancora risolto di oggi. In alto il numero del giorno e i tre livelli (stato: da fare, in corso, risolto). Il resto è la posizione: tavolo, mano, e il Maestro che presenta l'obiettivo con una frase che nomina le carte in gioco.
2. **Errore → indizio → soluzione** (come il Check, `coach-in-game.md` §6): dopo il primo errore un indizio che nomina le carte («guarda il Sette di Denari sul tavolo»), dopo il secondo una seconda indicazione più forte; chi vuole può chiedere la soluzione e il Maestro la spiega. L'errore non toglie nulla (nessun limite ai tentativi).
3. **La spiegazione a livello risolto usa la voce del Maestro**: una frase che dice **perché** la mossa vince, con le carte nominate (da `MatchFacts`/`VoiceComposer` di F1R-24), sostituendo «concetto» e «regola d'oro» con testi in `strings.xml`.
4. **Condivisione senza spoiler e senza il numero degli errori**: «Maestro Scopa #14 — Sfida risolta!» più i tre livelli in simboli (✔ ✔ ✔); il testo viene dalle risorse (it/en).
5. **Traccia dei giorni**: una riga «Serie di sfide» (giorni consecutivi con almeno un livello risolto) legata alla serie di giorni esistente; **nessun archivio dei giorni passati** in questa feature: l'archivio è del piano a pagamento (`docs/roadmap.md`, «archivio puzzle»).
6. **Livelli sensati per il giocatore** (D6): nessun cambio ai tre livelli generati dal seme, uguali per tutti; la scelta del livello da cui partire può suggerire quello che allena l'asse più debole del radar. Il generatore non cambia.
7. **Il mini-tavolo passa dalla regia** (D8): stessi tempi e animazioni del tavolo (`MotionSpec`), nessun `delay` sparso.
8. **Crediti**: restano 15, 20, 25 per livello, assegnati solo da `RewardEngine`, una volta per giorno e livello; resta fuori la possibilità di raddoppiarli con un video (Fase 2, monetizzazione).

**Layout e stati da coprire con screenshot** (matrice a 4 configurazioni): primo ingresso del giorno, dopo un errore (indizio 1 e 2), livello risolto con spiegazione, tre livelli risolti con condivisione, giorno già risolto, font al 130%.

## 4. Contratti

- I testi del «concetto» e della «regola d'oro» diventano **codici** (per esempio `PuzzleLesson.SWEEP`, `TAKES_SETTEBELLO`, …) calcolati dal provider e mostrati con testi in `strings.xml` it/en; il provider non restituisce più stringhe italiane (D1).
- `PuzzleShareFormatter` riceve le stringhe dalle risorse tramite un'interfaccia di presentazione, non le contiene (D1, D4).
- Gli indizi e la spiegazione usano `VoiceComposer`/`MatchFacts` (F1R-24); se servissero fatti che oggi mancano, si aggiungono lì, non in questa feature.
- Il mini-tavolo legge i tempi da `MotionSpec` (F1R-09); la presentazione non decide durate.
- `DailyPuzzleViewModel` resta l'unico stato della schermata; `RewardEngine.onPuzzleSolved` resta l'unico punto dei crediti.
- Nessuna dipendenza nuova; nessun cambio a `core:*` (il generatore di puzzle non cambia).

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-27a | Testi fuori dal codice | Concetto, regola d'oro e testo di condivisione in `strings.xml` it/en; nessuna stringa italiana in Kotlin; test su tutti i codici |
| F1R-27b | Indizio e soluzione con la voce del Maestro | Indizio in due livelli e soluzione spiegata con le carte nominate; testi [bozza] da approvare; test su posizioni note |
| F1R-27c | Condivisione senza spoiler né errori | Testo con i tre livelli in simboli, da risorse; test del testo |
| F1R-27d | Mini-tavolo sulla regia | Tempi da `MotionSpec`; nessun `delay` nuovo; test di sequenza |
| F1R-27e | Flusso e stati della schermata | Primo livello da fare in evidenza, stato dei tre livelli, serie di sfide; file sotto 400 righe; matrice a 4 configurazioni |

## 6. Domande per Michele

1. **Errori**: ti torna che l'errore non tolga nulla e che il numero dei tentativi **non** compaia nel testo condiviso? Oggi compare («Risolto al 3° tentativo»).
2. **Indizi e soluzione**: vuoi un pulsante «Mostrami la soluzione» (con spiegazione), oppure basta l'indizio in due livelli e poi si riprova all'infinito?
3. **Archivio dei giorni passati**: confermi che resta fuori da questa feature (piano a pagamento)?
4. **Livello consigliato dal radar** (punto 6): lo vuoi, o i tre livelli restano un percorso fisso Riscaldamento → Tattica → Maestro?
5. **Serie di sfide**: la vuoi distinta dalla serie di giorni dell'app, o basta quella già esistente?
6. **Crediti** 15/20/25 per livello: restano così? Le monete hanno ancora «un valore blando» (decisione 5 di `pre-game.md`) e la Bottega non è solida.
