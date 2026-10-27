# Layer, moduli e contratti

Fonte: ADR-0014. Stato: approvata da Michele il 2026-10-27.

## 1. Mappa

```
app:android (guscio)
 ├─ MainActivity · MaestroNavHost · moduli Koin
 │
 ├─ app:feature:table     ─┐
 ├─ app:feature:review     │   UI + Presentazione
 ├─ app:feature:home       │   (una cartella `ui/` e una `presentation/` per feature)
 ├─ app:feature:puzzle     │
 ├─ app:feature:academy    │
 ├─ app:feature:profile    │
 ├─ app:feature:shop      ─┘
 │        │
 │        ├──► app:designsystem   tema, componenti, MotionSpec, geometrie, suoni (interfaccia)
 │        └──► app:domain         MatchSession, RewardEngine, CoachService, Clock, repository (interfacce)
 │                   │
 └─ app:data ────────┘            Room, DataStore, implementazioni dei repository
                     │
               core:game · core:ai
```

Fino all'estrazione in moduli (ADR-0014, "Migrazione") la stessa mappa vale come package dentro `app:android`:
`it.maestro.app.{shell, domain, data, designsystem, feature.<nome>.{ui, presentation}}`.

## 2. Dove va ogni cosa

| Ho bisogno di… | Va in | Esempio |
| --- | --- | --- |
| Una regola della Scopa, un bot, un'analisi | `core:*` (task dedicato) | `legalMoves`, `PimcBot`, `MatchAnalyzer` |
| Far avanzare una partita, chiamare il bot, salvare | `domain/match` | `MatchSession` |
| Assegnare crediti, avanzare missioni o streak | `domain/rewards` | `RewardEngine.onMatchFinished(…)` |
| Un consiglio, una spiegazione, un indizio del Maestro | `domain/coach` | `CoachService.explain(move) → ExplanationCode` |
| La data di oggi, l'ora, un seed nuovo | `domain/time` | `Clock.today()`, `Clock.newSeed()` |
| Leggere o scrivere dati | interfaccia in `domain/…`, implementazione in `data/…` | `WalletRepository` / `RoomWalletRepository` |
| Cosa mostrare a schermo e quando | `feature/<x>/presentation` | `TableViewModel`, `TableDirector` |
| Come disegnarlo | `feature/<x>/ui` | `TableScreen`, `HandFan` |
| Un colore, una durata, un componente riusabile | `designsystem` | `MotionSpec.capture`, `ParchmentPanel` |
| Un testo per l'utente | `res/values*/strings.xml`, scelto in UI da un codice | `ExplanationCode.SETTEBELLO_LEFT → R.string.…` |

## 3. Contratti principali (dominio)

Forme indicative: i task che le introducono possono raffinare i nomi dei parametri, non la responsabilità.

```kotlin
/** Fonte unica di tempo e casualità iniziale per l'app (mai System.currentTimeMillis o LocalDate.now altrove). */
interface Clock {
    fun now(): Instant
    fun today(): LocalDate          // fuso orario dell'utente
    fun newSeed(): Long             // seed per una nuova partita (ADR-0003)
}

/** Una partita in corso: unico punto che tocca ScopaState. */
class MatchSession(
    config: MatchSetup,             // livello, punteggio obiettivo, modalità, fondamentale
    gate: PresentationGate,         // implementato dalla regia (ADR-0015)
    repository: MatchRepository,
    clock: Clock,
) {
    val beats: Flow<Beat>                         // una per mossa applicata, in ordine
    val view: StateFlow<ScopaView>                // vista del giocatore umano
    fun playCard(card: Card, capture: Set<Card>?) : PlayOutcome   // tipizzato, mai eccezioni
    fun requestHint()                             // passa da CoachService
    fun resume(log: MatchLog)                     // ripresa, ADR-0003
}

/** Implementata dalla presentazione: il dominio aspetta che la scena sia finita. */
interface PresentationGate {
    suspend fun awaitIdle(beatId: Long)
}

/** Unico punto in cui nascono crediti, missioni, streak e maestria. */
class RewardEngine(wallet: WalletRepository, missions: MissionRepository, mastery: MasteryRepository, clock: Clock) {
    suspend fun onMatchFinished(log: MatchLog, analysis: MatchAnalysis, setup: MatchSetup): RewardSummary
    suspend fun onPuzzleSolved(puzzleId: String, attempts: Int): RewardSummary
    suspend fun onLessonCompleted(lessonId: String): RewardSummary
}

/** Il Maestro che spiega: produce codici e parametri, la UI li traduce. */
class CoachService {
    fun hint(view: ScopaView, legal: List<ScopaMove>, rng: SeededRng, level: HintLevel): Hint
    fun explain(analysis: MoveAnalysis, viewAtMove: ScopaView): Explanation   // codice + argomenti
}
```

`RewardSummary` è ciò che la fine partita anima (crediti guadagnati, missioni avanzate, streak, variazione di maestria), vedi `game-feel.md` §6.

## 4. Navigazione

- Navigation Compose con rotte tipizzate (`@Serializable`) e back stack del sistema.
- Gli argomenti sono identificativi, mai oggetti grandi: `Table(matchId)`, `Review(matchId)`; i dati si rileggono dal repository.
- Lo stato di schermata sopravvive a rotazione e chiusura del processo (`SavedStateHandle` per gli id, repository per i dati).
- Nessuna decisione di navigazione nei composable di feature: espongono callback (`onOpenReview(matchId)`), il guscio decide.

## 5. Dependency injection

- Koin, un modulo per layer (`domainModule`, `dataModule`, un modulo per feature). Il guscio li compone.
- I ViewModel ricevono interfacce del dominio, mai DAO o DataStore.

## 6. Divieti (verificati da test dove possibile, F1R-01)

| Divieto | Perché |
| --- | --- |
| `import …data…` da `ui` o `presentation` | La UI non parla con il database |
| `import androidx.…` / `android.…` in `domain` | Il dominio è puro e multipiattaforma |
| `delay(`, `withTimeout(`, `Thread.sleep` fuori da `TableDirector` e `MotionSpec` | Un solo orologio di scena (ADR-0015) |
| `System.currentTimeMillis`, `LocalDate.now`, `Random(` fuori da `Clock` | Determinismo e testabilità |
| Letterali di testo per l'utente nel codice | ADR-0007 |
| `ScopaState` fuori da `domain/match` | Nessuna informazione nascosta in presentazione |
| `fallbackToDestructiveMigration` | ADR-0017 |
| Strumenti di sviluppo fuori da `BuildConfig.DEBUG` | Build di rilascio pulita |
| Misure fisse in `dp` per carte, mano, pile, avatar | ADR-0016 |
