# Game feel: tempi, feedback, Maestro, ricompense

Stato: proposta (PR #77). I valori numerici sono **iniziali**: si tarano nei playtest e si cambiano solo qui.

## 1. Pilastri

| # | Pilastro | In una frase |
| --- | --- | --- |
| P1 | Il tavolo è il palcoscenico | Zone fisse e grandi; ogni giocatore ha davanti la sua pila |
| P2 | Ritmo e regia | Una cosa alla volta, con tempi coerenti; il bot "pensa" in proporzione alla mossa |
| P3 | Feedback | Ogni gesto ha un suono, un movimento e, se importante, una vibrazione |
| P4 | Il gioco si legge | La corsa ai punti della smazzata è sempre visibile |
| P5 | Il Maestro è un personaggio | Posto fisso, poche battute al momento giusto, mai sopra le carte |
| P6 | Ogni sessione premia | La fine partita è una sequenza di ricompensa, non tre schermate |

## 2. `MotionSpec` (tabella unica dei tempi, ms, a velocità 1×)

| Token | Valore | Uso |
| --- | --- | --- |
| `cardToPlayZone` | 260 | Carta umana dalla mano alla zona di gioco |
| `opponentReveal` | 220 | Carta avversaria che si gira |
| `opponentReadPause` | 550 | Pausa per leggere la carta avversaria |
| `captureHighlight` | 420 | Evidenza delle carte prese |
| `captureFlight` | 380 | Volo verso la pila (+40 per ogni carta successiva) |
| `dropToTable` | 240 | Calata senza presa |
| `dealCard` | 140 | Ogni carta distribuita |
| `shuffle` | 400 | Mescolata a inizio smazzata |
| `sweepCelebration` | 1400 | Overlay Scopa! umano (saltabile) |
| `sweepOpponent` | 900 | Overlay Scopa! avversario |
| `dealSummaryRow` | 260 | Ogni voce del riepilogo che si accende |
| `botThinkMin` / `botThinkMax` | 450 / 1300 | Riflessione del bot: minimo con una sola mossa legale, massimo con più prese possibili |
| `screenTransition` | 300 | Passaggio tra schermate |

Curve: ingressi `EaseOutBack` leggero per le carte, `FastOutSlowIn` per voli e pannelli; niente rimbalzi sui testi.

## 3. Catalogo dei feedback

| Momento | Movimento | Suono | Vibrazione |
| --- | --- | --- | --- |
| Tocco carta in mano | Carta si alza di 12% della sua altezza | click leggero | — |
| Calata | Scivola e si posa con leggera rotazione casuale (± 3°, dal seed) | `card_play` | — |
| Presa singola | Evidenza + volo | `card_capture` | tick |
| Presa per somma | Evidenza a catena, una carta dopo l'altra | `card_capture` più ricco | tick |
| Settebello preso | Cornice d'oro e bagliore | suono dedicato | medio |
| Scopa | Overlay, foglie d'oro (umano), carta di traverso sulla pila | `sweep` | forte |
| Scopa subita | Banner sobrio, posa del Maestro dispiaciuta | tonfo leggero | — |
| Fine smazzata | Riepilogo con punti accesi in sequenza | un "ding" per punto | — |
| Vittoria / sconfitta | Targa, Maestro con posa | jingle breve | medio (vittoria) |

Vibrazione disattivabile nelle impostazioni; tutti i suoni passano dal livello effetti (§7).

## 4. Layout del tavolo (P1)

```
┌─────────────────────────────────┐
│ Avatar avv. · nome · punteggio  │  ← barra avversario
│      [mano coperta avv.]   [P]  │  ← P = pila prese avversario (spessore)
│                                 │
│ [M]      carte a terra          │  ← M = mazzo; carte a ventaglio libero, grandi
│          (zona di gioco)        │
│                                 │
│ Maestro (mezzo busto, a lato)   │
│ Corsa ai punti: 🂠 ◆ 7◆ P        │  ← P4
│   [P]     [ mano del giocatore ]│  ← mano grande, intera, sollevata al tuo turno
└─────────────────────────────────┘
```

Proporzioni e minimi in `responsive-ui.md` §3.

## 5. Il Maestro al tavolo (P5)

| Quando | Cosa fa | Modalità |
| --- | --- | --- |
| Inizio partita | Saluto breve | tutte |
| Scopa del giocatore | Applauso | tutte |
| Scopa subita | Finta disperazione + battuta | Sparring |
| Prima dell'ultima distribuzione | Promemoria ("Conta i sette…") | Sparring |
| Su richiesta | Indizio a livelli (`maestro_persona.md` §3) | Sparring |
| Check di allenamento | Domanda tra due battute, mai durante | Sparring |
| In partita classica | Solo pose, nessun testo | Classica |

Al massimo una battuta di testo ogni 3 mosse del giocatore; il fumetto non copre mai mano o carte a terra.

## 6. Sequenza di fine partita (P6)

1. Targa Vittoria/Sconfitta con punteggio finale (1,2 s).
2. Precisione e numero di errori, con il commento del Maestro.
3. `RewardSummary` animato: crediti che volano nel contatore, missioni che avanzano, fiamma della streak, variazione dell'indice di maestria.
4. Azioni: **Rivedi partita** (primaria), Rivincita, Menu.

Ogni passo è saltabile con un tocco; nessun dialog Material.

## 7. Audio

Tre livelli con volume separato nelle impostazioni: **musica** (circolo, chitarra/mandolino, loop lungo), **ambiente** (brusio del bar, tazzine), **effetti**. La musica si abbassa durante le battute del Maestro. Gli asset li prepara Michele (generati o CC0) e passano da `scripts/prepare_assets.py` o equivalente.

## 8. Testi e numeri per l'utente

- Niente decimali di punteggio interno ("−1,11 pt"): si dice cosa si è perso ("Hai lasciato il Settebello", "Scopa regalata").
- Plurali con `plurals.xml` ("1 carta", "3 carte").
- I testi didattici (Accademia, spiegazioni) li approva Michele prima di ogni rilascio.
