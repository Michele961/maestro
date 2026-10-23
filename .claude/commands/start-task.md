# Comando /start-task

Avvia l'esecuzione di un task del backlog seguendo rigorosamente i guardrail definiti in `CLAUDE.md`.

Uso: `/start-task <TASK_ID>` (es. `/start-task F1-01`)

## Istruzioni operative per l'agente

1. **Lettura del task:**
   - Cerca `<TASK_ID>` nei file di backlog (es. `docs/backlog/fase-1.md` o la fase attiva).
   - Leggi con attenzione: obiettivo, perimetro autorizzato, criteri di accettazione e dipendenze.
2. **Verifica dipendenze:**
   - Controlla che tutti i task da cui `<TASK_ID>` dipende abbiano stato `fatto`. Se uno o più non lo sono, fermati immediatamente e segnalalo a Michele.
3. **Lettura della documentazione vincolante:**
   - Leggi sempre `docs/architecture.md` e `docs/conventions.md`.
   - Se il task tocca le regole o il modello di gioco, leggi `docs/rules/scopa.md` e `docs/rules/glossary.md` ("la specifica vince sul codice").
   - Se tocca l'AI o l'analisi, leggi `docs/ai/maestro.md`.
   - Se tocca decisioni architetturali, leggi gli ADR pertinenti in `docs/decisions/`.
4. **Aggiornamento stato backlog:**
   - Imposta lo stato del task a `in corso` nel file di backlog corrispondente.
5. **Creazione del branch Git:**
   - Crea e passa a un nuovo branch denominato `task/<TASK_ID>-<descrizione-breve-kebab-case>` partendo da `main` aggiornato.
6. **Promemoria sui vincoli non negoziabili:**
   - Modifica solo ciò che è esplicitamente nel perimetro del task.
   - Non toccare file protetti (`build.gradle.kts`, `gradle/libs.versions.toml`, `docs/decisions/`, `docs/rules/`, `CLAUDE.md`, ecc.) a meno di esplicita voce "Perimetro autorizzato".
   - Non aggiungere dipendenze o plugin senza approvazione esplicita.
   - Tutti i commit devono seguire il formato Conventional Commits includendo l'ID del task (es. `feat(scope): <TASK_ID> descrizione`).
