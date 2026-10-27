# Partita al tavolo — dossier

Stato: **bozza** · 2026-10-27 · task collegati: F1R-08 (parte 2), F1R-12, F1R-13
Procedimento: `docs/app/features/README.md`. Va approvato da Michele prima che i task partano.

## 1. Cosa fa per il giocatore

È la schermata in cui si passa quasi tutto il tempo: ogni sessione è una partita. Deve sembrare di essere seduti al tavolo di un circolo. Si capisce subito cosa è successo, cosa si può fare e chi sta vincendo la smazzata, e ogni presa dà soddisfazione.

## 2. Com'è oggi

**Codice**
- `feature/table/ui/TableScreen.kt`: 2.180 righe. Disegno, sequenza delle animazioni (`FeltLayout` con ~20 `delay`), dialog, colori dei temi.
- `feature/table/presentation/GameReducer.kt` (389) e `GameViewModel.kt`. Lo stato contiene `ScopaState`, cioè la partita completa, carte nascoste comprese.
- Già pronti e non ancora usati dallo schermo: `TableDirector` (F1R-10), `TableGeometry` (F1R-11), `MotionSpec` (F1R-09).
- Già in uso: il bot aspetta la fine della scena (F1R-08 parte 1).

**Problemi** (screenshot della matrice, `layout-defects-f1r06.md`)

| # | Problema |
| --- | --- |
| D3 | Su schermi piccoli la mano copre le pile e la carta giocata copre la mano avversaria |
| D4 | Su schermi grandi carte piccole e molto panno vuoto |
| D5 | Con il testo al 130% "GIOCA" e "Ultima giocata" invadono il tavolo |
| G2 | Prese dell'avversario, mazzo e prese tue sono tre pile uguali attaccate a destra |
| G3 | Maestro piccolo e flottante; il fumetto copre le carte |
| G4 | Nessuna indicazione di chi va verso Carte, Denari, Settebello, Primiera |
| G5 | "Ultima giocata" è un chip da app, non un gesto sul tavolo |

## 3. Come deve diventare

**Layout** (zone di `TableGeometry`, `game-feel.md` §4)

```
┌───────────────────────────────────────┐
│ [avatar] Avversario · Maestro   11–7  ‖ │ barra: chi, livello, punteggio, pausa
│        [▯ ▯ ▯]                  [pila] │ mano avversaria coperta + sue prese
│ [mazzo]   carte a terra, grandi        │ zona di gioco: ventaglio libero,
│  24        in due file al massimo      │ la carta giocata entra qui
│                                        │
│ ◆ Denari 4–2  🂠 Carte 12–9  7◆  P     │ corsa ai punti (F1R-13)   [Maestro]
│ [pila]   [ mano grande, intera ]       │ mano sollevata al tuo turno
└───────────────────────────────────────┘
```

- Ogni giocatore ha la **sua pila davanti** (avversario in alto, tu in basso); lo spessore cresce con le carte. Le carte di scopa stanno di traverso sulla pila.
- **"Ultima giocata" sparisce**: la carta avversaria si gira nella zona di gioco e resta il tempo di leggerla (passi `OpponentReveal` e `OpponentReadPause`).
- **Maestro** a posto fisso, accanto alla corsa ai punti. Il fumetto si apre verso l'alto, dentro la zona di gioco, solo quando non ci sono carte da guardare (F1R-13).

**Ritmo e feedback**: i passi di `BeatScript` con i tempi di `MotionSpec`, i suoni e le vibrazioni di `game-feel.md` §3. Un tocco salta la scena.

**Stati da coprire con gli screenshot** (matrice ADR-0016): inizio smazzata (distribuzione), turno del giocatore, carta avversaria girata, presa in volo, Scopa!, riepilogo di smazzata, fine partita, scelta tra due prese, domanda del Maestro, pausa.

## 4. Contratti

- `MatchSession` (dominio, F1R-08 parte 2): applica le mosse, produce `Beat`, salva, fa giocare il bot dopo `PresentationGate`. È l'unico punto che vede `ScopaState`.
- `TableDirector` → `TableScene` (presentazione, F1R-10).
- `TableGeometry` (UI, F1R-11).
- `TableViewModel`: stato di schermata (scelta presa, domanda del Maestro, pausa, suggerimento) + scena.

## 5. Task proposti

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-08b | `MatchSession` e reducer sulla sola vista | Stesso `MatchLog` di oggi su seed fisso; nessuno `ScopaState` in presentazione; il bot usa il cancello della regia |
| F1R-12a | Tavolo a riposo con `TableGeometry` | Nuovo layout a zone che disegna `TableScene` senza animazioni; screenshot della matrice senza D3–D5 |
| F1R-12b | Passi animati | Ogni `SceneStep` animato con la durata di `MotionSpec`; suoni e vibrazioni dai passi; salto con un tocco |
| F1R-12c | Dialog e rimozione del vecchio tavolo | Scelta presa, riepilogo, domanda, pausa portati sulla scena; `FeltLayout` rimosso; nessun file > 400 righe |
| F1R-13 | Corsa ai punti e Maestro | Come da backlog |

## 6. Domande per Michele

1. **Layout**: va bene lo schema del §3? In particolare le pile davanti a ciascun giocatore e il mazzo a sinistra.
2. **"Ultima giocata"**: la togliamo e lasciamo che sia la carta girata sul tavolo a raccontare la mossa avversaria?
3. **Mano**: ventaglio come oggi o fila dritta con le carte intere? Proposta: ventaglio leggero, ma con le carte sempre interamente visibili.
4. **Avversario**: in alto nome e ritratto del livello (es. "Maestro") invece del solo testo? Questo si lega a D11 (avversari come personaggi).
