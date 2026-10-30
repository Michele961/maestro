# Architettura

Stato: approvata da Michele il 2026-10-23. Le motivazioni sono negli ADR in `docs/decisions/`.

## In una riga

Regole e AI vivono in un modulo Kotlin Multiplatform puro. App Android, server di gioco, simulatore e (più avanti) iOS sono strati sottili sopra di esso.

## Moduli

| Modulo | Fase | Responsabilità | Può dipendere da |
| --- | --- | --- | --- |
| `core:game` | 0 | Regole, stato, mosse legali, punteggi, log, replay, viste per giocatore | solo stdlib |
| `core:ai` | 0 | Bot (casuale, avido, PIMC), risolutore finale, analizzatore | `core:game` |
| `core:protocol` | 2 | Messaggi client-server serializzabili | `core:game`, kotlinx-serialization |
| `sim` | 0 | CLI headless: partite bot contro bot, report | `core:game`, `core:ai` |
| `app:android` | 1 | App Android: oggi tutto in un modulo, organizzato a layer secondo ADR-0014 e `docs/app/` | `core:*` |
| `app:domain`, `app:data`, `app:designsystem`, `app:feature:*` | 1R | Moduli di arrivo dell'app (ADR-0014), creati da F1R-22 | vedi `docs/app/layers.md` |
| `server` | 2 | Ktor: stanze WebSocket, matchmaking, ranking, economia | `core:*` |

`core:protocol` esiste già come scaffold vuoto; il suo contenuto arriva con l'online (Fase 3, D10). `server` **non esiste ancora** e non va creato fuori dal task dedicato. L'app Android segue ADR-0014…0017: layer, regia del tavolo, UI adattiva e migrazioni; la guida operativa è `docs/app/`.
La tabella dei confini è applicata da `./gradlew check` (task `checkModuleBoundaries`, definito nel `build.gradle.kts` root).

## Target

- `core:*`: `jvm()` e `androidTarget()` (F1-01).
- `app:domain` (quando estratto, F1R-22): `jvm()` e `androidTarget()`.
- Fase 4: si aggiungono i target iOS.

## Package

Prefisso `it.maestro`. Un package per modulo (`it.maestro.game`, `it.maestro.ai`, ...), con sotto-package per area:

```
it.maestro.game.model    Card, Suit, Seat, ...
it.maestro.game.rng      SeededRng (ADR-0009)
it.maestro.game.scopa    stato, mosse, regole, punteggio della Scopa
it.maestro.game.log      MatchLog, replay
it.maestro.ai.bot        Bot e implementazioni
it.maestro.ai.analysis   analizzatore, precisione, classificazione
```

## Forma dell'API di `core:game` (vincolante)

Questa è la forma vincolante. I task F0-03…F0-07 la completano con i nomi che elencano; non introdurre API alternative né nomi pubblici non elencati.

```kotlin
// Una partita nasce da configurazione + seed, sempre.
fun newMatch(config: ScopaConfig, seed: Long): ScopaState

// Mosse legali del giocatore di turno. Ordine deterministico (vedi conventions.md).
fun legalMoves(state: ScopaState): List<ScopaMove>

// Applica una mossa: mai eccezioni per mosse illegali.
fun play(state: ScopaState, move: ScopaMove): PlayResult

sealed interface PlayResult {
    data class Applied(val state: ScopaState, val events: List<GameEvent>) : PlayResult
    data class Rejected(val reason: RejectReason) : PlayResult
}

// Cosa vede un giocatore: unica cosa che riceve l'AI e, in Fase 3, il client.
fun viewFor(state: ScopaState, seat: Seat): ScopaView

// Log e replay (ADR-0003).
data class MatchLog(val rulesVersion: Int, val config: ScopaConfig, val seed: Long, val moves: List<ScopaMove>)
fun replay(log: MatchLog): ReplayResult   // Ok(state) | WrongRulesVersion | IllegalMove(index)

// Solo per l'AI (PIMC): ricostruisce uno stato completo da una vista più le carte nascoste ipotizzate.
// Rifiuta ipotesi incompatibili con la vista (carte già viste, dimensioni sbagliate).
fun determinize(view: ScopaView, opponentHand: List<Card>, deckOrder: List<Card>): ScopaState
```

Tutte queste funzioni stanno in `it.maestro.game.scopa`. I tipi e i valori non elencati qui sono definiti nei task F0-04…F0-07, che li nominano esplicitamente.

## Flussi

**Partita offline:** UI → ViewModel → `MatchSession` (dominio) → `play()` → `Beat` (eventi + vista) → `TableDirector` → scena → UI. Il bot riceve `viewFor()` e muove solo quando la regia ha finito la scena precedente (ADR-0015). A fine partita, l'analizzatore lavora sul `MatchLog` e `RewardEngine` assegna le ricompense.

**Partita online (Fase 3):** il client invia `PlayCard`; il server chiama `play()` sullo stato autoritativo e invia a ciascun client gli eventi filtrati e, alla riconnessione, `viewFor()`. Il server conserva il `MatchLog` e lo rigioca per verificarlo.

**Review (ADR-0005):** l'anteprima è calcolata sul telefono. Sblocco e risultato ufficiale passano dal server (dalla Fase 3). Nell'MVP la review è gratuita e solo locale.

## Multiplayer (riferimento per la Fase 3)

- Server autoritativo (ADR-0002): distribuisce le carte, valida, invia solo la vista di ciascun giocatore.
- Messaggi: `PlayCard`, `Emote`, `Resync(lastSeq)` dal client; `Event(seq)`, `Snapshot`, `TurnTimer` dal server.
- Timer di turno con mossa automatica, finestra di riconnessione, abbandono = sconfitta.
- Ranking Glicko-2 nascosto + trofei visibili; classifiche su sorted set Redis per chiave (globale, regione, provincia, amici).

## Stack per fase

| Fase | Aggiunte |
| --- | --- |
| 0 | Kotlin 2.2, Gradle 8.14, JDK 21, kotlin.test |
| 1 | Android Gradle Plugin, Jetpack Compose, Koin, Room, DataStore, Rive, SoundPool, Crashlytics, i18n it/en, Navigation Compose, kotlinx-coroutines, AndroidX Lifecycle/ViewModel per Compose, Compose UI test |
| 2 | Ktor server + client WebSocket, Postgres, Redis, Google Sign-In, Play Integrity |
| 3 | Play Billing (verifica server, RTDN), AdMob rewarded con SSV, consenso UMP |
| 4 | Compose Multiplatform iOS |

Ogni libreria entra nel version catalog solo con un task che la nomina esplicitamente nel suo **Perimetro autorizzato**.
