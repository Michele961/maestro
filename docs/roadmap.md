# Roadmap — dopo la Fase 0

Stato: **approvata da Michele** il 2026-10-23, con le decisioni del §3.
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

Sono emersi rileggendo il report di F0-13 rispetto ai documenti approvati. Nessuno blocca la chiusura della Fase 0; ciascuno ha una risposta nella Fase 1.

1. **F0-12 non è stata fatta da un giocatore umano.** Il backlog indicava Michele come esecutore; le note (`docs/backlog/f0-12-note.md`) sono state scritte da un agente "su delega". Il gate è accettato in via provvisoria (D1); la revisione umana vera, su partite giocate da persone, è F1-16.
2. **Livelli di difficoltà.** Il report proponeva livelli basati anche su `GreedyBot` e un parametro "profondità in semimosse" che `SearchBudget` non ha. Deciso (D3): tutti i livelli derivano da `PimcBot`, come in `docs/ai/maestro.md`.
3. **Pesi e formula.** `PRIMIERA_DIFF` passa a 0,025 (D2), in F1-03. Soglie e formula della precisione restano provvisorie fino a F1-16.
4. **Tempi misurati.** Le latenze del report (p95 da 0,25 a 1,2 ms) sono molto basse per una ricerca con 200 nodi × 100 campioni. Si rimisurano su dispositivo in F1-04 (limiti in D4).
5. **Guardrail mancanti.** `CLAUDE.md` cita un template di PR, `.claude/commands/` (`/start-task`, `/review-task`) e `.claude/settings.json`, ma nel repository non esistono. Si creano in F1-00, con Michele.

## 2. Fasi

| Fase | Obiettivo | Risultato visibile | Stack (da `architecture.md`) |
| --- | --- | --- | --- |
| **1 — MVP offline Android** | Scopa contro il Maestro, con review locale gratuita | App in test interno su Play Console | Compose, Koin, Room, DataStore, Rive, SoundPool, Crashlytics, i18n it/en |
| **2 — Didattica, Retention & Economia** | "L'Accademia del Maestro": rigioca l'errore, puzzle del giorno, moduli didattici, abbonamento e monetizzazione | Lancio pubblico su Play Store come trainer & coach | Play Billing, AdMob rewarded con SSV, UMP |
| **3 — Online** | "Il Circolo": partite 1v1 in tempo reale, account, classificata, leghe | Ranked e Tavolo amichevole 1v1 | Ktor server + client WebSocket, Postgres, Redis, Google Sign-In, Play Integrity |
| **Briscola** | Secondo gioco sullo stesso motore e sulla stessa app | Briscola in tutte le modalità | — |
| **4 — iOS** | Stessa app su iPhone | App Store | Compose Multiplatform iOS |

La **Briscola** entra solo quando la Scopa è completa come esperienza, non solo come motore: app, didattica, economia e lancio pubblico. Prima serve `docs/rules/briscola.md` scritto o approvato da Michele (ADR-0004). Il numero della fase si decide allora; ADR-0012 colloca iOS in "Fase 4" e quel nome non cambia senza Michele.

### Fase 1 — MVP offline Android

Dettaglio dei task in `docs/backlog/fase-1.md`. In sintesi:

- **Fondamenta:** `androidTarget()` nei `core:*`, modulo `app:android` (Compose, una sola Activity, MVI, `minSdk 26`, ADR-0012), CI che compila l'APK.
- **AI sul telefono:** 5 livelli di difficoltà (Principiante → Maestro), misura dei tempi e della precisione su dispositivo.
- **Gioco:** tavolo con carte segnaposto, scelta della presa quando ce n'è più d'una, flusso completo pre-partita → smazzate → fine partita, salvataggio e ripresa tramite `MatchLog`.
- **Coach:** anteprima post-partita (precisione, errori gravi), Game Review mossa per mossa con i tre colori e l'alternativa migliore sul tavolo, Check di allenamento in Sparring (`docs/ai/maestro.md`: "task in Fase 1").
- **Rifinitura:** guida di stile e asset generati (ADR-0008), audio, animazioni del Maestro, Crashlytics, build di rilascio.
- **Validazione:** playtest con persone vere, report di fine fase.

**Fuori dalla Fase 1:** tutto ciò che richiede un server (account, classificata, amici, classifiche, leghe, emote verso altri giocatori), crediti e pagamenti, pubblicità, Accademia, puzzle, Briscola, Scopone.

**Gate di uscita:**

- un tester esterno installa l'app dal canale di test e gioca una partita completa contro ciascun livello;
- l'Elo di ogni livello, misurato con il simulatore, è vicino al suo obiettivo e ogni livello batte il precedente (D3);
- p95 del tempo per mossa del livello Maestro sotto 200 ms sul Pixel 10 Pro e sotto 800 ms sul profilo di fascia bassa (D4);
- zero crash in Crashlytics su almeno 50 partite di test;
- il playtest umano (F1-16) giudica sensate le etichette della review.

### Fase 2 — Didattica, Retention & Economia: "L'Accademia del Maestro"

Focalizzazione sull'USP del prodotto, sulla retention quotidiana e sulla monetizzazione etica prima del multiplayer (decisione D10):

- **Clinica degli errori:** "Rigioca l'errore" nella Game Review (interactive blunder fixing: il giocatore rigioca la mano direttamente sul tavolo per trovare la mossa corretta con feedback del Maestro).
- **Sfida del Giorno (Puzzle Tattici):** Generatore offline di puzzle tattici da posizioni a mazzo vuoto con soluzione unica vincente; interfaccia dedicata con condivisione social del risultato senza spoiler (stile Wordle).
- **Accademia a Moduli:** Micro-lezioni teorico-pratiche interattive (Basi, Primiera rapida, Regola del dispari, Gestione delle uscite, Mnemotecniche da bar).
- **Indice di Maestria & Abitudini:** Tracciamento della precisione media ponderata (Elo cognitivo indipendente dalla fortuna), Grafico Radar delle abilità (Memoria, Calcolo, Banco, Primiera, Endgame), streak giornaliera di allenamento ("Fiamma").
- **Economia e Monetizzazione:** Wallet locale "Crediti Maestro" (Room); integrazione AdMob Rewarded Ads (video opzionali per review extra / tentativi puzzle); Google Play Billing (pacchetti crediti e cosmetici regionali); Abbonamento "Circolo dei Maestri" (review illimitate, zero ads, accesso a tutta l'Accademia e archivio puzzle).
- **Lancio pubblico:** Rilascio su Google Play Store come app di allenamento e sfida mentale definitiva per la Scopa.

### Fase 3 — Online: "Il Circolo" (alto livello)

Una volta costituita una base utenti fidelizzata e attiva giornalmente:

- `core:protocol` con i messaggi di `architecture.md` (`PlayCard`, `Emote`, `Resync`, `Event`, `Snapshot`, `TurnTimer`).
- Modulo `server` Ktor con stanze WebSocket autoritative (ADR-0002), timer di turno, riconnessione, abbandono = sconfitta.
- Account con Google Sign-In; matchmaking su Redis con code separate per varianti (ADR-0011).
- Glicko-2 nascosto + trofei; Tavolo amichevole 1v1 via link; emote e frasi rapide; Play Integrity.
- Leghe e classifiche territoriali/regionali (D7).

### Fase 4 — iOS

Target iOS nei `core:*` e UI in Compose Multiplatform (ADR-0012).

## 3. Decisioni di Michele (2026-10-23…2026-10-26)

| ID | Domanda | Decisione |
| --- | --- | --- |
| D1 | Chiusura della Fase 0 | Approvata. Il gate F0-12 vale in via provvisoria; revisione umana in F1-16 |
| D2 | `PRIMIERA_DIFF` | Da 0,02 a 0,025, in F1-03. Soglie e formula della precisione restano provvisorie fino a F1-16 |
| D3 | Livelli di difficoltà | **5 livelli**, per coprire tutte le fasce Elo: Principiante, Dilettante, Intermedio, Avanzato, Maestro. Tutti da `PimcBot` (campioni, nodi, rumore nella scelta, memoria imperfetta), come in `docs/ai/maestro.md`. Ogni livello ha un **Elo obiettivo**; proposta di partenza 800 / 1100 / 1400 / 1700 / 2000, misurati facendo giocare i livelli tra loro nel simulatore. Parametri ed Elo finali si fissano insieme a Michele con i dati di F1-03 |
| D4 | Prestazioni | p95 del tempo per mossa del livello Maestro **< 200 ms sul Pixel 10 Pro** di Michele e **< 800 ms su un emulatore con profilo di fascia bassa** (CPU limitata, API 26) |
| D5 | Librerie Fase 1 non elencate in `architecture.md` | Approvate: Android Gradle Plugin stabile, Navigation Compose, kotlinx-coroutines, AndroidX Lifecycle/ViewModel per Compose, Compose UI test. Ognuna entra nel version catalog solo dal **Perimetro autorizzato** del suo task; la tabella "Stack per fase" si aggiorna in F1-00 |
| D6 | Quando entra la Briscola | Dopo la Fase 3: prima la Scopa completa come esperienza (app, online, economia, lancio). Serve prima `docs/rules/briscola.md` |
| D7 | Leghe e classifiche regionali | In Fase 3 (online) |
| D8 | Identità dell'app | Nome sullo store **"Maestro – Scopa e Briscola"**; ID applicazione **`it.maestro.app`** (definitivo dopo la prima pubblicazione); account Play Console **personale già esistente** di Michele. Firma: Play App Signing, chiave di upload custodita da Michele fuori dal repository |
| D9 | Suggerimento su richiesta in Sparring | Sì, in Fase 1 (F1-11) |
| D10 | Inversione Fase 2 (Didattica/Economia) e Fase 3 (Online) | **Approvata il 2026-10-26.** In assenza iniziale di una massa critica di utenti per il matchmaking sincrono, la priorità va all'USP unica (Coaching, Puzzle del Giorno, Accademia) e alla monetizzazione/lancio su Play Store. L'infrastruttura online in tempo reale (server Ktor/WebSocket) diventa Fase 3, da lanciare su una base utenti già attiva |
