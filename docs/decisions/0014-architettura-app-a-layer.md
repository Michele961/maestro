# ADR-0014 — Architettura dell'app a layer

Stato: accettato · 2026-10-27 · approvato da Michele in chat

## Contesto
`app:android` è cresciuta in fretta (16.500 righe in 4 giorni). Navigazione, missioni, wallet e analisi sono in `MainActivity`; `TableScreen.kt` supera le 2.000 righe; la UI chiama direttamente i repository. Ogni nuova funzione tocca gli stessi file e le correzioni si rincorrono (`docs/reports/audit-2026-10-27.md` §2).

## Decisione
L'app è divisa in cinque layer con dipendenze in una sola direzione:

```
UI  →  Presentazione  →  Dominio app  →  core:game / core:ai
                    ↘        ↑
                      Dati ──┘ (implementa le interfacce del dominio)
```

| Layer | Contiene | Non può |
| --- | --- | --- |
| **UI** | Composable, layout, input, risorse | Chiamare repository, `core:*` per calcoli, `delay()`, leggere l'orologio |
| **Presentazione** | ViewModel, stato di schermata, navigazione, `TableDirector` (ADR-0015) | Contenere regole di gioco o di ricompensa |
| **Dominio app** | Casi d'uso puri: `MatchSession`, `RewardEngine`, `CoachService`, `Clock`; interfacce dei repository | Dipendere da Android, Compose, Room, stringhe localizzate (restituisce codici, ADR-0007) |
| **Dati** | Room, DataStore, asset; implementazioni dei repository | Contenere logica di business |
| **core:\*** | Regole, bot, analisi, puzzle, maestria | Cambia solo con i task di `core` |

Struttura dei moduli Gradle di arrivo (l'ADR autorizza la loro creazione, un task per volta):

| Modulo | Layer | Può dipendere da |
| --- | --- | --- |
| `app:domain` | Dominio app, Kotlin Multiplatform (`jvm`, `android`; iOS in Fase 4) | `core:*`, kotlinx-coroutines |
| `app:data` | Dati | `app:domain`, Room, DataStore |
| `app:designsystem` | UI condivisa: tema, token di movimento, componenti, geometria adattiva (ADR-0016) | Compose |
| `app:feature:<nome>` | UI + Presentazione di una funzione (`table`, `review`, `home`, `academy`, `puzzle`, `profile`, `shop`) | `app:domain`, `app:designsystem` |
| `app:android` | Guscio: Activity, navigazione, DI, risorse condivise | tutti i moduli `app:*` |

Una feature non dipende da un'altra feature: si parlano solo tramite rotte di navigazione.

## Migrazione
1. Prima i **package** dentro `app:android` con gli stessi nomi dei moduli futuri (`it.maestro.app.domain`, `…data`, `…designsystem`, `…feature.table`, …), con un test automatico che verifica gli import tra layer (F1R-01).
2. Poi l'estrazione in moduli Gradle, una feature per task, aggiornando la tabella di `checkModuleBoundaries`.

## Conseguenze
- Le regole di dipendenza sono verificate da `./gradlew check`, non dalla buona volontà degli agenti.
- La logica di fine partita, missioni e crediti vive in un solo punto testabile (`RewardEngine`).
- `app:domain` in Kotlin Multiplatform riduce il lavoro per iOS (Fase 4).
- Dettagli operativi e contratti: `docs/app/layers.md`.
