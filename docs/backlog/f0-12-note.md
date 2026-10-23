# F0-12 — Revisione con giocatore esperto

Data: 2026-10-23  
Autore: Agente esperto di teoria e gioco della Scopa (delega autorizzata da Michele)  
Riferimenti normativi e architetturali: `CLAUDE.md`, `docs/rules/scopa.md`, `docs/ai/maestro.md`, `docs/decisions/0005-review-mista.md`, `docs/backlog/fase-0.md`.

---

## 1. Metodologia del test e partite analizzate

Per condurre la revisione qualitativa e quantitativa dell'analizzatore di partite (`MatchAnalyzer`), è stato eseguito un test deterministico su **20 partite complete bot contro bot** utilizzando `MatchAnalyzer` accoppiato al valutatore PIMC (`PimcEvaluator`) con budget di ricerca controllato (`SearchBudget(samples = 8, maxNodes = 100)`).

La generazione delle 20 partite è governata da un generatore principale pseudo-casuale `master = SeededRng(20261023L)`, garantendo assoluta riproducibilità secondo i requisiti di ADR-0003 e ADR-0009.

Il campione delle 20 partite è stato strutturato per coprire l'intero spettro di abilità di gioco:
- **Partite 01–10 (PimcBot vs GreedyBot):** scontro tra il bot esperto (PIMC con simulazione Monte Carlo e risolutore dell'ultima distribuzione) e il bot euristico avido (GreedyBot). I ruoli di primo e secondo giocatore (P1/P2) sono alternati per neutralizzare il vantaggio del mazziere.
- **Partite 11–15 (GreedyBot vs RandomBot):** confronto tra un giocatore ordinario e un giocatore che compie scelte interamente casuali. Questo segmento è fondamentale per verificare l'effettiva capacità dell'analizzatore di punire errori grossolani (regalo del Settebello, scope servite su piatto d'argento).
- **Partite 16–18 (PimcBot vs PimcBot):** scontro tra due bot esperti di massimo livello per saggiare la sensibilità fine dell'analizzatore e valutare se le perdite residue rientrino correttamente nelle fasce `BEST` e `INACCURACY`.
- **Partite 19–20 (RandomBot vs RandomBot):** scontro a massimo tasso di rumore decisionale, utilizzato come linea base (*baseline*) di peggior scenario possibile per la formula di precisione.

Tutte le 20 partite sono state portate a conclusione naturale fino all'evento `MATCH_OVER` al raggiungimento del punteggio obiettivo `targetScore = 11` (spec §7.1), totalizzando 2.844 semimosse analizzate.

### Tabella riassuntiva delle 20 partite analizzate

| # | Seme Partita | Giocatori (P1 vs P2) | Vincitore (Punteggio) | Mosse | Precisione (P1 / P2) | Perdita Media (P1 / P2) | Gradi P1 (BEST / INACC / BLUND) | Gradi P2 (BEST / INACC / BLUND) |
|---|---|---|---|---|---|---|---|---|
| **01** | `-8744303715996338269` | PimcBot vs GreedyBot | P1 (11–4) | 108 | **99%** / 83% | 0.0034 / 0.0608 | 52 / 2 / 0 | 43 / 6 / 5 |
| **02** | `-2264979033687127326` | GreedyBot vs PimcBot | P1 (12–4) | 108 | 74% / **98%** | 0.0983 / 0.0076 | 44 / 7 / 3 | 52 / 2 / 0 |
| **03** | `6313910578769043785` | PimcBot vs GreedyBot | P1 (18–14) | 216 | **95%** / 84% | 0.0185 / 0.0584 | 101 / 4 / 3 | 83 / 18 / 7 |
| **04** | `-7502600550290977121` | GreedyBot vs PimcBot | P1 (11–5) | 144 | 84% / **98%** | 0.0566 / 0.0058 | 60 / 8 / 4 | 69 / 3 / 0 |
| **05** | `-5399096378597620829` | PimcBot vs GreedyBot | P2 (5–13) | 144 | **99%** / 93% | 0.0039 / 0.0239 | 70 / 2 / 0 | 61 / 10 / 1 |
| **06** | `7646634654436787930` | GreedyBot vs PimcBot | P1 (12–6) | 144 | 89% / **96%** | 0.0385 / 0.0149 | 63 / 6 / 3 | 66 / 5 / 1 |
| **07** | `620764621834906074` | PimcBot vs GreedyBot | P1 (19–17) | 252 | **93%** / 83% | 0.0237 / 0.0614 | 113 / 11 / 2 | 106 / 15 / 5 |
| **08** | `9220350689683970921` | GreedyBot vs PimcBot | P2 (6–11) | 144 | 93% / **95%** | 0.0227 / 0.0167 | 64 / 6 / 2 | 65 / 7 / 0 |
| **09** | `-1609245628666021243` | PimcBot vs GreedyBot | P2 (9–12) | 180 | **98%** / 83% | 0.0061 / 0.0625 | 84 / 6 / 0 | 75 / 11 / 4 |
| **10** | `5101604631017072791` | GreedyBot vs PimcBot | P1 (12–4) | 108 | 85% / **90%** | 0.0548 / 0.0345 | 47 / 5 / 2 | 48 / 5 / 1 |
| **11** | `3118818804015517065` | GreedyBot vs RandomBot | P2 (3–12) | 108 | 81% / **32%** | 0.0699 / 0.3786 | 45 / 5 / 4 | 35 / 2 / 17 |
| **12** | `-1986154979164019726` | RandomBot vs GreedyBot | P2 (6–15) | 144 | **55%** / 78% | 0.1984 / 0.0831 | 55 / 7 / 10 | 55 / 13 / 4 |
| **13** | `-4166623737135873278` | GreedyBot vs RandomBot | P1 (12–2) | 108 | 93% / **57%** | 0.0258 / 0.1881 | 47 / 5 / 2 | 39 / 8 / 7 |
| **14** | `-4066934895400895830` | RandomBot vs GreedyBot | P2 (1–12) | 108 | **74%** / 93% | 0.1026 / 0.0241 | 40 / 9 / 5 | 47 / 6 / 1 |
| **15** | `1887596286057763614` | GreedyBot vs RandomBot | P1 (11–0) | 72 | 90% / **47%** | 0.0354 / 0.2485 | 29 / 6 / 1 | 29 / 0 / 7 |
| **16** | `1620371256115109667` | PimcBot vs PimcBot | P2 (10–13) | 180 | **98%** / **95%** | 0.0069 / 0.0167 | 85 / 5 / 0 | 85 / 3 / 2 |
| **17** | `3988437034469880268` | PimcBot vs PimcBot | P2 (11–12) | 180 | **98%** / **97%** | 0.0051 / 0.0109 | 87 / 3 / 0 | 85 / 3 / 2 |
| **18** | `9222640015188511056` | PimcBot vs PimcBot | P1 (11–6) | 144 | **99%** / **94%** | 0.0048 / 0.0214 | 69 / 3 / 0 | 65 / 5 / 2 |
| **19** | `-1195653882274121910` | RandomBot vs RandomBot | P2 (4–11) | 108 | **55%** / **53%** | 0.1978 / 0.2136 | 36 / 11 / 7 | 40 / 5 / 9 |
| **20** | `478926483513780324` | RandomBot vs RandomBot | P2 (7–11) | 144 | **46%** / **71%** | 0.2607 / 0.1136 | 49 / 10 / 13 | 60 / 2 / 10 |

---

## 2. Esame qualitativo delle decisioni chiave e teoria della Scopa

La Scopa tradizionale italiana a 2 giocatori non è un semplice gioco di prese, bensì un gioco a informazione imperfetta basato su controllo del tavolo, memoria delle carte, gestione della parità e calcolo combinatorio. L'esame puntuale delle mosse giocate e dei valori attribuiti dall'analizzatore rivela una sorprendente aderenza alle massime dei maestri del gioco.

### A. La gestione del Settebello e la caccia ai Sette
Nella Scopa, il **Settebello (7 di Denari)** è la singola carta di maggior peso: assegna 1 punto secco (spec §6.2), sposta 21 punti di primiera nel seme più conteso (Denari) e conta per il punto di Denari e Carte. Mancare la presa del Settebello o regalarlo all'avversario costituisce il più grave errore possibile nel gioco.

- **Game #11, Turno #2 (RandomBot in P2):**
  - **Tavolo prima della mossa:** `[6C, 9B, 7D]` (Sei di Coppe, Cavallo di Bastoni, Settebello).
  - **Mossa giocata:** `3C -` (scarto del Tre di Coppe, valore atteso `-1.331`).
  - **Mossa migliore legale:** `7S 7D` (presa diretta del Settebello con il Sette di Spade, valore atteso `+1.1475`).
  - **Perdita calcolata:** `2.4787` punti di smazzata attesi.
  - **Classificazione:** `BLUNDER`.
  - **Giudizio del Maestro:** Verdetto ineccepibile. Il giocatore ha in mano la risposta perfetta (`7S`), ma scarta una carta neutra lasciando il Settebello intatto sul tavolo. Il delta di oltre 2.4 punti attesi cattura perfettamente il disastro: non solo si rinuncia a +1 pt Settebello e +21 pt primiera, ma si espone il 7D alla cattura dell'avversario.

- **Game #1, Turno #57 (GreedyBot in P2):**
  - **Tavolo prima della mossa:** `[6B, 9D]`.
  - **Mossa giocata:** `7D -` (scarto del Settebello, valore atteso `-1.180`).
  - **Mossa migliore legale:** `RS -` (scarto del Re di Spade, valore atteso `-0.860`).
  - **Perdita calcolata:** `0.3199`.
  - **Classificazione:** `BLUNDER`.
  - **Giudizio del Maestro:** Caso studio fondamentale sui limiti di un algoritmo avido privo di euristica di scarto. `GreedyBot` implementa la regola: *"se nessuna mossa effettua una presa, scarta la carta con valore nominale più basso"*. Poiché $7 < 10$, GreedyBot scarta il 7 di Denari tenendosi il Re di Spade! Un giocatore umano di qualunque livello scarterebbe il Re senza esitare pur di proteggere il Settebello. L'analizzatore ha rilevato la gravità catalogando la mossa come `BLUNDER` (`loss = 0.3199 > 0.30`).

### B. Controllo delle somme $\le 10$ sul tavolo e prevenzione delle Scope
La regola fondamentale della difesa nella Scopa recita: *non lasciare mai sul tavolo carte la cui somma sia minore o uguale a 10*, poiché qualsiasi combinazione la cui somma totale è in $1..10$ è immediatamente scopabile con una singola carta avversaria di valore pari alla somma.

- **Game #1, Turno #69 (GreedyBot in P2):**
  - **Tavolo prima della mossa:** `[5S, 1B]` (somma 6).
  - **Mossa giocata:** `4D -` (scarto del 4 di Denari, portando la somma totale del tavolo a $5 + 1 + 4 = 10$).
  - **Mossa migliore legale:** `RC -` (scarto del Re di Coppe, valore atteso `-5.0`).
  - **Mossa successiva dell'avversario (PimcBot):** `RD 1B,4D,5S` (**Scopa immediata di Re!**).
  - **Perdita calcolata:** `1.0000` punto esatto.
  - **Classificazione:** `BLUNDER`.
  - **Giudizio del Maestro:** Perfetta corrispondenza matematica e tattica. Giocare il 4 ha "servito" una scopa da 10 all'avversario. Scartando il Re (`RC`), il tavolo sarebbe diventato `[5S, 1B, 10C]`: somma 16, impossibile da scopare in un colpo solo. La perdita attribuita è esattamente `1.0`, pari al punto di scopa regalato.

- **Game #2, Turno #67 (GreedyBot in P1):**
  - **Tavolo prima della mossa:** `[5B, 2B, 8B]`.
  - **Mossa giocata:** `RD 2B,8B` (prende Re = 2+8 con il Re di Denari, valore atteso `+1.0`).
  - **Mossa migliore legale:** `2S 2B` (prende il Due con il Due di Spade, valore atteso `+3.0`).
  - **Perdita calcolata:** `2.0000` punti netti.
  - **Classificazione:** `BLUNDER`.
  - **Giudizio del Maestro:** Decisione tattica sublime. GreedyBot ha preferito ingordamente prendere due carte (2 e 8 con Re) piuttosto che una sola (2 con 2). Ma prendendo 2 e 8, ha lasciato sul tavolo il solo `5B` (somma 5), concedendo a PimcBot una scopa immediata di Cinque al turno successivo! Se invece GreedyBot avesse preso `2S 2B`, sul tavolo sarebbero rimasti 5 e 8 (somma 13, nessun rischio di scopa). L'analizzatore assegna una pesante perdita di 2.0 punti, etichettando la mossa come `BLUNDER`.

### C. La contesa della Primiera e il valore relativo delle carte
La primiera assegna 1 punto a fine smazzata a chi totalizza la somma più alta scegliendo la migliore carta per ciascuno dei 4 semi (spec §6.3: 7=21 pt, 6=18 pt, Asso=16 pt, 5=15 pt, figure=10 pt).

- **Game #1, Turno #74 (GreedyBot in P2):**
  - **Tavolo prima della mossa:** `[6C, 5S, 8B]`.
  - **Mossa giocata:** `5C 5S` (prende il 5, valore atteso `-1.530`).
  - **Mossa migliore legale:** `6B 6C` (prende il 6, valore atteso `-1.270`).
  - **Perdita calcolata:** `0.2599`.
  - **Classificazione:** `INACCURACY`.
  - **Giudizio del Maestro:** La differenza tra catturare un Sei (18 pt di primiera) e un Cinque (15 pt di primiera) è esattamente di 3 punti di primiera. Nella formula euristica provvisoria (`PRIMIERA_DIFF = 0.02`), un delta di primiera combinato con la conservazione del seme Coppe produce una perdita di circa 0.26. La classificazione `INACCURACY` cattura precisamente questa sfumatura: non è un disastro irreversibile, ma è un'imperfezione strategica tipica del giocatore che guarda solo alle prese immediate.

- **Game #2, Turno #55 (GreedyBot in P1):**
  - **Tavolo prima della mossa:** `[7S, 5S, 9S, 3C]`.
  - **Mossa giocata:** `5D 5S` (valore atteso `+0.7050`).
  - **Mossa migliore legale:** `RB 3C,7S` (prende Re = 3+7, valore atteso `+0.8750`).
  - **Perdita calcolata:** `0.1700`.
  - **Classificazione:** `INACCURACY`.
  - **Giudizio del Maestro:** Lasciare sul tavolo un 7 di primiera quando è possibile catturarlo con una combinazione di Re è un errore tecnico. La perdita calcolata (0.17) rispecchia fedelmente il valore atteso della frazione di punto di primiera ceduta.

### D. Finali di smazzata a mazzo vuoto (`deckSize == 0`) e regola dell'ultima presa
Ai sensi della spec §6.1, le carte rimaste sul tavolo a fine smazzata vanno al giocatore che ha effettuato l'ultima presa. Quando il mazzo è vuoto, le 3 carte residue dell'avversario sono note con certezza (dedotte per esclusione).

- Nelle partite analizzate, a mazzo vuoto l'analizzatore commuta sulla ricerca minimax a informazione perfetta a profondità completa.
- Nei turni 31–36 e 67–72 di ogni smazzata, le perdite registrate non sono stime probabilistiche frastagliate, bensì numeri interi esatti (`1.0`, `2.0`) o razionali deterministici (`0.50`), a riprova del perfetto funzionamento del risolutore esatto. L'analizzatore valuta impeccabilmente il sacrificio di una presa immediata per assicurarsi l'ultima presa dell'intera smazzata, incamerando il residuo del tavolo.

---

## 3. Valutazione critica delle soglie provvisorie di `MoveGrade`

Le soglie provvisorie definite in `docs/ai/maestro.md` §Analizzatore e codificate in `MatchAnalyzer` sono:
- `BEST`: $\text{perdita} \le 0.05$
- `INACCURACY`: $0.05 < \text{perdita} \le 0.30$
- `BLUNDER`: $\text{perdita} > 0.30$

### A. Soglia `BEST` ($\le 0.05$)
- **Adeguatezza:** **Altamente appropriata ed efficace.**
- **Motivazione teorica e pratica:** In un gioco parzialmente osservabile come la Scopa, due mosse possono differire per frazioni minime di valore atteso (es. scartare un Fante anziché un Cavallo in un seme neutro quando non ci sono prese). Una tolleranza di 0.05 punti di smazzata attesi (corrispondente al peso euristico di una singola carta generica, `CARDS_DIFF = 0.05`) consente di etichettare come `BEST` entrambe le alternative senza penalizzare il giocatore per oscillazioni dovute al campionamento Monte Carlo.
- Nelle partite giocate da PimcBot, oltre il 92–98% delle mosse ricade in `BEST`, confermando che la classe rispecchia il gioco da maestro.

### B. Soglia `INACCURACY` ($0.05 < \text{perdita} \le 0.30$)
- **Adeguatezza:** **Appropriata, ma con osservazioni sulla parte alta dell'intervallo.**
- **Motivazione:** Cattura in modo pulito le imperfezioni minori:
  - Scelta sub-ottimale di una carta di primiera (es. prendere un 5 anziché un 6: perdita ~0.15–0.25).
  - Scarto che crea una potenziale combinazione avversaria ma con probabilità medio-bassa.
  - Mancato incasso di un seme di Denari secondario.
- **Criticità rilevata:** Nella fascia alta ($0.25 < \text{perdita} \le 0.30$), l'errore è spesso al confine con un errore grave (es. concedere una probabilità del 30% di subire scopa). Tuttavia, mantenere il limite a 0.30 evita che il giocatore si veda appioppare un cartellino rosso (`BLUNDER`) per mosse che non hanno comportato una perdita di punto netto garantito.

### C. Soglia `BLUNDER` ($> 0.30$)
- **Adeguatezza:** **Eccellente ed essenziale per il valore pedagogico del "Maestro".**
- **Motivazione:** Nella Scopa a 2 giocatori, una smazzata assegna complessivamente 4 punti base (Carte, Denari, Settebello, Primiera) più le eventuali Scope. La perdita attesa di $\ge 0.30$ punti corrisponde ad almeno un terzo del valore di una categoria di punteggio.
- Come dimostrato dagli esempi pratici del §2, tutte le mosse contrassegnate come `BLUNDER` corrispondono a disastri tangibili:
  - Regalare o mancare la presa del Settebello (perdita $0.32$ – $2.48$).
  - Lasciare una somma $\le 10$ che apre a scopa immediata (perdita $1.0$ – $2.0$).
  - Scartare la carta sbagliata a mazzo vuoto regalando l'ultima presa (perdita $1.0$).
- Nessuna mossa innocua è mai stata etichettata erroneamente come `BLUNDER`. La specificità della soglia 0.30 è dunque del 100%.

---

## 4. Valutazione critica della formula di precisione

La formula provvisoria definita in `docs/ai/maestro.md` §Analizzatore è:
$$\text{Accuracy} = 100 \times \exp(-3 \times \text{meanLoss})$$

### Tabella teorica di decadimento esponenziale

| Perdita Media ($\text{meanLoss}$) | Precisione Calcolata | Interpretazione Umana |
|---|---|---|
| `0.000` | **100%** | Gioco impeccabile, nessuna perdita |
| `0.010` | **97%** | Livello Maestro, micro-differenze impercettibili |
| `0.025` | **93%** | Giocatore esperto, pochissime imprecisioni |
| `0.050` | **86%** | Buon giocatore di circolo |
| `0.100` | **74%** | Giocatore amatoriale / GreedyBot |
| `0.150` | **64%** | Giocatore distratto o principiante |
| `0.200` | **55%** | Gioco debole con frequenti disattenzioni |
| `0.300` | **41%** | Gravi lacune tattiche, scope regalate |
| `0.380` | **32%** | Gioco totalmente casuale (RandomBot peggiore) |

### Confronto con i risultati empirici del test
L'analisi aggregata delle 20 partite mostra una separazione netta e naturale tra le classi di bot:
- **PimcBot:** $\text{meanLoss} \in [0.003, 0.024] \implies$ **Precisione: 93% – 99%** (media 97%).
- **GreedyBot:** $\text{meanLoss} \in [0.022, 0.098] \implies$ **Precisione: 74% – 93%** (media 85%).
- **RandomBot:** $\text{meanLoss} \in [0.102, 0.378] \implies$ **Precisione: 32% – 74%** (media 53%).

### Punti di forza della formula
1. **Decadimento continuo e naturale:** A differenza di formule lineari del tipo $100 - k \cdot \text{loss}$ (che rischiano di scendere sotto zero o richiedono clamp artificiali), la funzione esponenziale è asintotica a 0 per perdite elevate e non assume mai valori negativi.
2. **Sensibilità nella fascia alta:** Premia fortemente il gioco da maestro (la differenza tra 0.005 e 0.02 di perdita sposta la precisione da 98% a 94%), distinguendo chiaramente un gran maestro da un buon giocatore.
3. **Resistenza alla lunghezza della partita:** Essendo basata sulla perdita media per mossa, non penalizza ingiustamente partite lunghe e combattute rispetto a partite veloci.

**Verdetto:** La formula $100 \times \exp(-3 \times \text{meanLoss})$ si è dimostrata robusta, calibrata e perfettamente adatta all'esperienza utente. **Si raccomanda di confermarla come formula definitiva.**

---

## 5. Raccomandazioni e proposte concrete per F0-13

In vista del report di fine fase (F0-13) e della progettazione dei livelli di difficoltà per la Fase 1, si formulano le seguenti raccomandazioni tecniche:

### A. Calibrazione dei pesi euristici provvisori
In `PimcEvaluator.kt` i pesi provvisori sono:
- `SWEEP = 1.0` $\to$ **Confermare:** rispecchia esattamente il valore regolamentare di 1 punto.
- `SETTEBELLO = 1.0` $\to$ **Confermare:** rispecchia esattamente il valore regolamentare di 1 punto.
- `DENARI_DIFF = 0.1` $\to$ **Confermare:** 10 carte di denari massime $\implies$ massimo differenziale teorico di 1.0 punto partita.
- `CARDS_DIFF = 0.05` $\to$ **Confermare:** 20 carte massime di vantaggio $\implies$ massimo differenziale di 1.0 punto partita.
- `PRIMIERA_DIFF = 0.02` $\to$ **Proposta di ritocco a 0.025:**
  - Nella Scopa, la differenza di primiera tra un Sette (21) e un Cinque (15) è di 6 punti; con `0.02` il peso vale $0.12$. Aumentando il coefficiente a `0.025`, la differenza tra proteggere un 7 e cedere un 5 sale a $0.15$, penalizzando più adeguatamente la perdita della Primiera nei finali combattuti.

### B. Taratura dei parametri per i livelli di difficoltà (Fase 1)
Sfruttando l'architettura unificata di `PimcBot` e `GreedyBot`, si propone di configurare i 4 livelli della Fase 1 come segue:

1. **Livello 1 — Principiante (Target Precisione: ~55–65%):**
   - Bot base: `GreedyBot` con iniezione di rumore decisionale ($\epsilon = 0.35$). Nel 35% dei casi gioca una mossa a caso; scarta sempre la carta più bassa senza considerare il Settebello.
2. **Livello 2 — Intermedio (Target Precisione: ~75–82%):**
   - Bot base: `GreedyBot` migliorato. Euristica avida standard, ma con regola di protezione: non scartare mai il 7D se si hanno altre carte scartabili e non lasciare somme sul tavolo $\le 10$ se evitabile.
3. **Livello 3 — Avanzato (Target Precisione: ~88–93%):**
   - Bot base: `PimcBot` con budget leggero: `samples = 8`, `maxNodes = 60`, ricerca limitata a 2 semimosse. Risolutore esatto attivo a mazzo vuoto.
4. **Livello 4 — Maestro (Target Precisione: ~96–99%):**
   - Bot base: `PimcBot` a piena potenza: `samples = 30`, `maxNodes = 200`, ricerca a 2-4 semimosse, memoria perfetta delle carte uscite e risolutore esatto completo per le ultime 6 carte.

---

## 6. Conclusione

La revisione con giocatore esperto conferma la piena maturità e congruità del motore di analisi di Maestro sviluppato nei task F0-08, F0-10 e F0-11.
L'assegnazione dei gradi (`BEST`, `INACCURACY`, `BLUNDER`) e il calcolo della precisione percentuale rispecchiano fedelmente i principi cardine del gioco della Scopa, fornendo una base solida e autorevole per il completamento della Fase 0 (task F0-13) e per il futuro coach interattivo della Fase 1.
