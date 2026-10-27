# Regia del tavolo (`TableDirector`)

Fonte: ADR-0015. Stato: proposta (PR #77).

## 1. Flusso

```
Giocatore tocca una carta
   │
   ▼
TableViewModel ──playCard──► MatchSession (dominio)
                               │  play() di core:game
                               │  ↳ Beat(id, actor, events, viewBefore, viewAfter)
                               ▼
                         beats (Flow) ──► TableDirector ──scene──► TableScreen (disegna)
                                               │
                                awaitIdle(id) ◄┘ quando la battuta è finita
                               │
MatchSession: tocca al bot? ──► attende awaitIdle ──► tempo di riflessione ──► mossa del bot ──► nuova Beat
```

Il bot non ha timer propri: il suo turno parte **dopo** la fine della scena precedente.

## 2. Modelli

```kotlin
/** Una mossa applicata, vista dal giocatore umano. Nessuna carta nascosta. */
data class Beat(
    val id: Long,
    val actor: Seat,
    val move: ScopaMove,                 // pubblica: carta giocata e presa
    val events: List<GameEvent>,         // CardPlayed, Captured, Sweep, CardsDealt, DealEnded, MatchEnded
    val viewBefore: ScopaView,
    val viewAfter: ScopaView,
)

/** Cosa si vede ora. È l'unico input della UI del tavolo. */
data class TableScene(
    val table: List<CardSlot>,           // carte a terra con posizione logica e stato (normale, evidenziata, in volo)
    val hand: List<CardSlot>,
    val opponentHandCount: Int,
    val deckCount: Int,
    val piles: Map<Seat, PileState>,     // spessore, carte di scopa visibili
    val flying: List<FlyingCard>,        // carte in movimento: da → a, progresso gestito dalla UI con la durata data
    val overlay: Overlay?,               // Scopa!, riepilogo smazzata, fine partita
    val maestro: MaestroCue?,            // posa e battuta del Maestro
    val pointsRace: PointsRace,          // corsa a Carte, Denari, Settebello, Primiera (game-feel.md P4)
    val inputEnabled: Boolean,           // la mano è giocabile solo a scena ferma e turno umano
)
```

## 3. Da eventi a scene

Ogni battuta si traduce in una sequenza fissa di **passi**; ogni passo ha una durata presa da `MotionSpec` (`game-feel.md` §2).

| Evento / situazione | Passi |
| --- | --- |
| `CardPlayed` (umano) | carta dalla mano alla zona di gioco |
| `CardPlayed` (avversario) | carta coperta → girata nella zona di gioco → pausa di lettura |
| `Captured` | evidenzia le carte prese → volo insieme verso la pila di chi prende → la pila cresce |
| Calata senza presa | la carta scivola nel suo posto a terra |
| `Sweep` | come `Captured`, poi overlay Scopa! (umano o avversario, `game-feel.md` §4) e carta di scopa di traverso sulla pila |
| `CardsDealt` | 3 + 3 carte una alla volta, alternate, dal mazzo; il contatore scende a ogni carta |
| Nuova smazzata | mescolata → 4 carte a terra → distribuzione |
| `DealEnded` | carte residue all'ultimo che ha preso → overlay riepilogo con punti accesi in sequenza |
| `MatchEnded` | overlay di fine partita → navigazione alla sequenza di ricompensa |

Le sequenze sono **dati** (una lista di passi per tipo di battuta), non codice sparso: si leggono e si testano.

## 4. Regole

1. Una battuta alla volta, nell'ordine di `MatchSession`. Le battute successive aspettano in coda.
2. `inputEnabled` è vero solo a coda vuota e turno umano: niente tocchi durante le animazioni.
3. **Salta:** un tocco sul tavolo durante una scena porta all'ultimo passo della battuta corrente; un secondo tocco svuota la coda fino alla prossima scelta del giocatore.
4. **Velocità:** ogni durata è moltiplicata da `GameSpeed` (impostazione: 1×, 1,5×, 2×; "istantanea" per i test).
5. **Ricostruzione:** dopo una rotazione o un ritorno in app la regia mostra la scena di riposo di `viewAfter` dell'ultima battuta, senza rigiocare animazioni.
6. **Il Maestro** parla solo nei punti previsti da `game-feel.md` §5 e mai sopra la mano o le carte a terra.
7. **Suoni e vibrazione** partono dai passi, non dai composable: ogni passo può dichiarare un `SoundCue` e un `HapticCue`.

## 5. Riuso

- **Review e "Rigioca l'errore":** la regia riceve le battute ricostruite dal `MatchLog` (avanti/indietro = battuta successiva/precedente a velocità alta).
- **Sfida del giorno e Accademia:** stessa regia, con battute generate dalla soluzione.
- **Online (Fase 3):** le battute arrivano dal server; la regia non cambia.

## 6. Test

- `TableDirectorTest` con tempo virtuale: per ogni tipo di battuta, la sequenza di scene e la durata totale attesa.
- Coda: due battute ravvicinate vengono mostrate per intero, nell'ordine.
- Gate: `MatchSession` non chiede la mossa al bot prima di `awaitIdle` (test con gate finto).
- Salta e velocità: stessa scena finale, durata ridotta.
