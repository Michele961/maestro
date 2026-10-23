# Comando /review-task

Esegue la revisione formale e qualitativa di un task o di una Pull Request senza modificare il codice.

Uso: `/review-task <TASK_ID>` (es. `/review-task F1-01`)

## Istruzioni operative per l'agente revisore

1. **Modalità solo lettura:**
   - Non modificare alcun file durante la revisione.
2. **Verifica della Definizione di Fatto (`CLAUDE.md` §6):**
   - [ ] Tutti i criteri di accettazione del task nel backlog sono pienamente soddisfatti.
   - [ ] `./gradlew check` (e su Android `./gradlew :app:android:assembleDebug` se applicabile) passa con successo.
   - [ ] Ogni nuova regola o comportamento ha test dedicati che lo nominano esplicitamente (vedi `docs/conventions.md`).
   - [ ] KDoc presente per tutte le nuove API pubbliche.
   - [ ] Nessun `TODO` orfano privo di ID task (es. `// TODO(F1-05): ...`).
   - [ ] La PR usa il template `.github/pull_request_template.md` con le sezioni "Fuori dal perimetro" e "Domande aperte" compilate.
3. **Controllo dei file protetti e perimetro (`CLAUDE.md` §3):**
   - Nessuna modifica non autorizzata a `gradle/libs.versions.toml` o `build.gradle.kts`.
   - Nessuna violazione dei confini tra moduli (`checkModuleBoundaries`).
   - Nessuna modifica a documenti architetturali approvati (`docs/decisions/`, `docs/rules/`, ecc.) non prevista dal perimetro.
   - Nessun indebolimento dei test esistenti.
4. **Verifica dei principi architetturali (`CLAUDE.md` §4):**
   - Purezza di `core:game` (nessuna dipendenza esterna, I/O, orologio o stato globale).
   - Determinismo garantito tramite `SeededRng`.
   - L'AI e la vista non devono mai avere accesso a carte nascoste o allo stato completo (`ScopaView` vs `ScopaState`).
   - Immutabilità di stati e modelli.
   - Nessuna eccezione lanciata per mosse non valide (uso di `PlayResult.Rejected`).
5. **Output della revisione:**
   - Emetti un report strutturato indicando esito positivo o eventuali rilievi bloccanti da correggere prima del merge.
