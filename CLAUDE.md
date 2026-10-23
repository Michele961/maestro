# CLAUDE.md — Istruzioni per gli agenti

Questo repository contiene **Maestro**, un gioco di carte italiano (Scopa, poi Briscola) per Android con un coach AI ("il Maestro").
Michele è il proprietario del prodotto: decide lui. Gli agenti eseguono task definiti, non progettano il prodotto.

Leggi questo file per intero prima di ogni task.

## 1. Regola d'oro

**Fai solo il task che ti è stato assegnato, nel modo descritto dai documenti. Se qualcosa non è scritto, fermati e chiedi.**

Non inventare regole di gioco, varianti, API, dipendenze, moduli, schermate o funzionalità. Un dubbio va scritto come domanda, non risolto con un'ipotesi.

## 2. Prima di scrivere codice

1. Leggi il task in `docs/backlog/` (ID tipo `F0-05`): obiettivo, perimetro, criteri di accettazione, dipendenze.
2. Controlla che i task da cui dipende siano `fatto`. Se non lo sono, non iniziare.
3. Leggi i documenti che il task cita. Sempre: `docs/architecture.md` e `docs/conventions.md`.
4. Se il task riguarda le regole, leggi `docs/rules/scopa.md` e `docs/rules/glossary.md`. **La specifica vince sul codice.**
5. Imposta lo stato del task a `in corso` nel file di backlog.

## 3. Cose vietate senza approvazione esplicita di Michele

- Aggiungere o aggiornare dipendenze, plugin o versioni (`gradle/libs.versions.toml`, file `build.gradle.kts`).
- Creare moduli, cambiare i confini tra moduli (tabella in `build.gradle.kts` root).
- Modificare i documenti approvati: `docs/decisions/` (ADR), `docs/rules/`, `docs/vision.md`, `docs/architecture.md`, `docs/ai/`, `docs/conventions.md`, `docs/workflow.md`, `.github/`, `.claude/`, questo file e `AGENTS.md`.
- Se il codice non torna con un documento approvato, è il codice a doversi adeguare: non "correggere" il documento per farlo combaciare.
- Cambiare l'API pubblica di un modulo diverso da quello del task.
- Cancellare, disattivare o indebolire test esistenti (`@Ignore`, asserzioni rimosse, tolleranze allargate).
- Rinominare package o spostare file fuori dal perimetro del task.
- Iniziare lavori di fasi successive (Android, server, grafica, monetizzazione) prima del task dedicato.
- "Migliorare" codice non toccato dal task: refactoring, riformattazioni, rinomine.

Se ti serve una di queste cose, **fermati** e scrivi la richiesta nella sezione "Domande aperte" (vedi §5).

## 4. Principi tecnici non negoziabili

- **Motore puro.** `core:game` è Kotlin comune senza dipendenze: niente I/O, orologio, rete, thread, stato globale.
- **Determinismo.** Ogni casualità passa dal generatore con seed del progetto (ADR-0009). Mai `kotlin.random.Random` senza seed, mai `System.currentTimeMillis()` nella logica.
- **Partita = seed + configurazione + lista di mosse** (ADR-0003). Il replay di un log deve dare sempre lo stesso stato finale.
- **Immutabilità.** Stato e modelli sono `data class`/`value class` immutabili; le funzioni di regola restituiscono un nuovo stato.
- **Mosse illegali non lanciano eccezioni:** restituiscono un risultato tipizzato (`sealed interface`). Le eccezioni sono solo per bug di programmazione.
- **L'AI vede solo la vista del giocatore**, mai lo stato completo. Violare questo punto è un bug grave.
- **Server autoritativo** (Fase 2): il client non riceve mai carte nascoste.

## 5. Quando fermarsi e chiedere

Fermati, non procedere per ipotesi, quando:

- una regola di gioco non è coperta dalla specifica, oppure servirebbe andare oltre il default provvisorio di un punto `DA CONFERMARE` (il default si implementa così com'è);
- il task è ambiguo o in conflitto con un ADR o con un altro documento;
- servirebbe una cosa della lista §3;
- un test esistente fallisce per motivi estranei al task;
- la soluzione supera chiaramente il perimetro o la dimensione del task (~400 righe di codice non di test);
- servirebbe un nome, un tipo o un formato pubblico che né il task né i documenti definiscono.

Un task può concedere eccezioni puntuali a §3 solo con una voce esplicita **Perimetro autorizzato**.

Come chiedere: aggiungi la domanda nella sezione **Domande aperte** del task nel backlog e nella descrizione della PR, imposta il task a `bloccato` e chiudi il lavoro con quello che è già verificato.

## 6. Definizione di fatto

Un task è `fatto` solo se:

- [ ] tutti i criteri di accettazione del task sono soddisfatti;
- [ ] `./gradlew check` è verde (test + confini tra moduli);
- [ ] ogni regola o comportamento nuovo ha almeno un test che lo nomina (vedi `docs/conventions.md`);
- [ ] il KDoc delle API pubbliche nuove è scritto (se cambierebbe la responsabilità di un modulo, fermati: §5);
- [ ] nessun `TODO` senza ID di task (`// TODO(F0-07): ...`);
- [ ] la PR usa il template e compila le sezioni "Fuori dal perimetro" e "Domande aperte".

## 7. Comandi

```
./gradlew check                                  # test + verifica confini tra moduli
./gradlew :core:game:jvmTest                     # test di un modulo KMP
./gradlew :sim:run --args="--games 100 --seed 1" # simulatore
```

Comandi per gli agenti (in `.claude/commands/`):

- `/start-task F0-05` — avvia un task seguendo questi guardrail.
- `/review-task F0-05` — revisione di una PR, senza modificare codice.

Le modifiche ai file protetti (§3) chiedono conferma tramite `.claude/settings.json`. Quel file è solo un aiuto: il vero controllo è la revisione della PR e la protezione del branch `main` su GitHub.

## 8. Git

- Un branch per task: `task/F0-05-scopa-prese`.
- Commit piccoli in formato Conventional Commits, con l'ID: `feat(game): F0-05 presa per somma`.
- Mai push su `main`, mai force-push su branch altrui, mai riscrivere la storia condivisa.

## 9. Mappa dei documenti

| Documento | Contenuto |
| --- | --- |
| `docs/vision.md` | Visione di prodotto (fonte: Michele). Solo contesto, non specifica |
| `docs/architecture.md` | Moduli, dipendenze, flussi, roadmap tecnica |
| `docs/decisions/` | ADR: decisioni prese e perché. Non si riaprono senza Michele |
| `docs/rules/scopa.md` | Specifica normativa della Scopa |
| `docs/rules/glossary.md` | Termini di gioco, nomi nel codice, traduzioni it/en |
| `docs/ai/maestro.md` | Design del motore AI e dell'analizzatore |
| `docs/conventions.md` | Stile del codice, test, naming |
| `docs/workflow.md` | Ciclo di vita dei task, ruoli, escalation |
| `docs/backlog/` | Task per fase, con stato e criteri di accettazione |
