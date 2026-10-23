# Roadmap — dopo la Fase 0

Stato: **proposta** · 2026-10-23 · da approvare da Michele.
Questo documento è un piano, non una specifica: non autorizza nessun lavoro. Un task si esegue solo quando compare nel backlog della sua fase con stato `da fare` e Michele lo assegna.

Fonti: `docs/vision.md`, `docs/architecture.md` (stack per fase), ADR-0001…0012, `docs/ai/maestro.md`, `docs/reports/fase-0.md`.

## 1. Dove siamo

La Fase 0 (motore headless) è chiusa: F0-01…F0-13 sono `fatto` su `main` (PR #2–#15).

| Area | Consegnato |
| --- | --- |
| `core:game` | Regole della Scopa a 2 complete (`scopa.md` §1–7), varianti `targetScore` 11/16/21, `redealOnThreeKings`, `reBello`; `viewFor`, `MatchLog`, `replay`, `determinize` |
| `core:ai` | `RandomBot`, `GreedyBot`, `PimcBot` con risolutore esatto a mazzo vuoto, `MatchAnalyzer` con `MoveGrade` e precisione |
| `sim` | CLI bot contro bot, `--samples`, `--analyze` |
| Gate | Pimc vs Greedy 63–66% su 1000 partite, p95 ≪ 1 s, etichette della review valutate in F0-12 |

### Punti da chiarire prima di andare avanti

Sono emersi rileggendo il report di F0-13 rispetto ai documenti approvati. Nessuno blocca la chiusura della Fase 0, ma ciascuno influenza la Fase 1.

1. **F0-12 non è stata fatta da un giocatore umano.** Il backlog indicava Michele come esecutore; le note (`docs/backlog/f0-12-note.md`) sono state scritte da un agente "su delega". Il gate chiedeva etichette "giudicate sensate da un giocatore esperto". Proposta: considerare il gate superato in via provvisoria e prevedere una revisione umana vera, su partite giocate da persone, in Fase 1 (F1-16).
2. **Livelli di difficoltà.** `docs/ai/maestro.md` dice che i livelli si ricavano **dallo stesso `PimcBot`** variando campioni, profondità, rumore e memoria imperfetta. Il report propone invece livelli 1–2 basati su `GreedyBot` e regole nuove ("non scartare il 7D", "evitare somme ≤ 10 sul tavolo") e un parametro "profondità in semimosse" che `SearchBudget` non ha. Serve una decisione di Michele (vedi D3).
3. **Pesi e formula.** Il report propone `PRIMIERA_DIFF` da 0,02 a 0,025 e dichiara "definitive" soglie e formula della precisione. Nel codice e in `docs/ai/maestro.md` sono ancora "provvisori". Se Michele approva, `docs/ai/maestro.md` va aggiornato (file protetto: lo fa lui o lo chiede esplicitamente).
4. **Tempi misurati.** Le latenze del report (p95 da 0,25 a 1,2 ms) sono molto basse per una ricerca con 200 nodi × 100 campioni. Vanno rimisurate su un telefono reale di fascia bassa (F1-04), che è comunque il vincolo che conta.
5. **Guardrail mancanti.** `CLAUDE.md` cita un template di PR, `.claude/commands/` (`/start-task`, `/review-task`) e `.claude/settings.json`, ma nel repository non esistono. Vanno creati (file protetti: serve l'ok di Michele) prima che la Fase 1 parta con più agenti.

## 2. Fasi

| Fase | Obiettivo | Risultato visibile | Stack (da `architecture.md`) |
| --- | --- | --- | --- |
| **1 — MVP offline Android** | Scopa contro il Maestro, con review locale gratuita | App in test interno su Play Console | Compose, Koin, Room, DataStore, Rive, SoundPool, Crashlytics, i18n it/en |
| **2 — Online** | Partite 1v1 in tempo reale, account, classificata | Ranked e Tavolo amichevole 1v1 | Ktor server + client WebSocket, Postgres, Redis, Google Sign-In, Play Integrity |
| **3 — Economia** | Crediti, review a pagamento, pubblicità con premio, puzzle | Lancio pubblico | Play Billing, AdMob rewarded con SSV, UMP |
| **4 — iOS** | Stessa app su iPhone | App Store | Compose Multiplatform iOS |

La **Briscola** non ha ancora una fase: ADR-0004 la mette "dopo, sullo stesso motore" e richiede prima `docs/rules/briscola.md` (decisione D6).

### Fase 1 — MVP offline Android

Dettaglio dei task in `docs/backlog/fase-1.md` (bozza). In sintesi:

- **Fondamenta:** `androidTarget()` nei `core:*`, modulo `app:android` (Compose, una sola Activity, MVI, `minSdk 26`, ADR-0012), CI che compila l'APK.
- **AI sul telefono:** livelli di difficoltà (Principiante → Maestro), misura dei tempi e della precisione su dispositivo.
- **Gioco:** tavolo con carte segnaposto, scelta della presa quando ce n'è più d'una, flusso completo pre-partita → smazzate → fine partita, salvataggio e ripresa tramite `MatchLog`.
- **Coach:** anteprima post-partita (precisione, errori gravi), Game Review mossa per mossa con i tre colori e l'alternativa migliore sul tavolo, Check di allenamento in Sparring (`docs/ai/maestro.md`: "task in Fase 1").
- **Rifinitura:** guida di stile e asset generati (ADR-0008), audio, animazioni del Maestro, Crashlytics, build di rilascio.
- **Validazione:** playtest con persone vere, report di fine fase.

**Fuori dalla Fase 1:** tutto ciò che richiede un server (account, classificata, amici, classifiche, leghe, emote verso altri giocatori), crediti e pagamenti, pubblicità, Accademia, puzzle, Briscola, Scopone.

**Gate di uscita proposto:**

- un tester esterno installa l'app dal canale di test interno e gioca una partita completa contro ciascun livello;
- la precisione media di ogni livello, misurata con il simulatore, cade nella fascia scelta da Michele (D3);
- p95 del tempo per mossa del livello più forte sotto 500 ms su un dispositivo di riferimento di fascia bassa (D4);
- zero crash in Crashlytics su almeno 50 partite di test;
- il playtest umano (F1-16) giudica sensate le etichette della review.

### Fase 2 — Online (alto livello)

`core:protocol` con i messaggi di `architecture.md` (`PlayCard`, `Emote`, `Resync`, `Event`, `Snapshot`, `TurnTimer`); modulo `server` Ktor con stanze WebSocket autoritative (ADR-0002), timer di turno, riconnessione, abbandono = sconfitta; account con Google Sign-In (Supabase solo per auth, se conviene: ADR-0006); matchmaking su Redis con code separate per varianti (ADR-0011); Glicko-2 nascosto + trofei; Tavolo amichevole 1v1 via link; emote e frasi rapide; Play Integrity. Leghe e classifiche regionali/provinciali possono stare a fine Fase 2 o in Fase 3 (D7).

### Fase 3 — Economia (alto livello)

Wallet append-only sul server (ADR-0006), Crediti Maestro, token firmato per la review completa (ADR-0005), Play Billing con verifica server e RTDN, AdMob rewarded con SSV e consenso UMP, Sfida del Giorno con puzzle generati dal risolutore (`docs/ai/maestro.md`), cosmetici. Accademia e "Pass del Maestro" sono "fase successiva" nella visione.

### Fase 4 — iOS

Target iOS nei `core:*` e UI in Compose Multiplatform (ADR-0012).

## 3. Decisioni che servono a Michele

Per aprire la Fase 1 servono almeno D1–D5.

| ID | Domanda | Proposta |
| --- | --- | --- |
| D1 | Approvi il report di Fase 0 e chiudi la fase? Accetti il gate F0-12 come provvisorio (punto 1.1)? | Sì, con revisione umana in F1-16 |
| D2 | `PRIMIERA_DIFF` 0,02 → 0,025? Soglie e formula della precisione diventano definitive? | Ritocco sì, ma solo dopo un confronto con il simulatore (F1-03). Soglie e formula restano provvisorie fino a F1-16 |
| D3 | Livelli di difficoltà: quanti, come si costruiscono, che fasce di precisione? | 4 livelli (Principiante, Intermedio, Avanzato, Maestro), tutti da `PimcBot` come da `maestro.md` (campioni, rumore, memoria imperfetta), fasce del report come obiettivo da verificare |
| D4 | Dispositivo di riferimento per le prestazioni e limite di tempo per mossa | Un telefono Android 8 (API 26) di fascia bassa; p95 < 500 ms |
| D5 | Librerie Fase 1 non elencate in `architecture.md`: navigazione Compose, coroutine/lifecycle, Android Gradle Plugin, test UI | Navigation Compose, kotlinx-coroutines, AGP stabile, Compose UI test; ognuna nel **Perimetro autorizzato** del suo task |
| D6 | Quando entra la Briscola? | Dopo la Fase 1, prima della Fase 2: chi gioca online si aspetta entrambi i giochi. Serve prima `docs/rules/briscola.md` scritto o approvato da Michele |
| D7 | Leghe e classifiche regionali: Fase 2 o 3? | Fine Fase 2 (servono solo ranking e profilo con regione) |
| D8 | Nome dell'app, package Android (`it.maestro.app`?), account Play Console e firma | Da decidere prima di F1-14 |
| D9 | Il coach in Sparring può dare suggerimenti a richiesta (visione §4) già in Fase 1? | Sì, è poco codice in più rispetto alla review (F1-11) |
