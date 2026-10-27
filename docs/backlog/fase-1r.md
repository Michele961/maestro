# Backlog — Fase 1R: rifondazione dell'app

Approvato da Michele il 2026-10-27 (ADR-0014…0017, decisioni D12–D17).

Obiettivo: portare l'app all'architettura di `docs/app/` senza perdere funzioni, e trasformare il tavolo in un gioco (regia, ritmo, feedback, loop di sessione). Le funzioni esistenti restano disponibili per tutta la fase; ogni task lascia l'app giocabile.

**Gate di uscita:**
- `./gradlew check` include i test d'architettura (F1R-01) e passa senza eccezioni;
- nessun `delay()` né durata fuori da `TableDirector` e `MotionSpec`; il bot muove solo dopo la fine della scena;
- tutte le schermate passano la matrice di ADR-0016 (4 configurazioni);
- rotazione, cambio di tema o lingua e chiusura del processo non perdono la partita né la schermata;
- nessuna migrazione distruttiva; test di migrazione verdi;
- playtest interno di Michele: tavolo fluido e leggibile, nessuna animazione fuori posto.

**Revisione per feature.** Le ondate A e B costruiscono le fondamenta comuni. Da lì ogni feature si rivede e migliora **singolarmente**, con un dossier approvato da Michele prima dei task (`docs/app/features/README.md`, task F1R-23). I task C e D di questo file restano validi come base e vengono rifiniti dal dossier della loro feature.

Dopo questa fase: alpha su Play Console (F1-15), playtest (F1-16), report (F1-17), poi monetizzazione (F2-08…F2-10) e lancio (F2-12).

Regole per tutti i task: leggere `docs/app/README.md` e i documenti che il task cita; nessuna funzione nuova oltre quanto scritto; PR < 400 righe di codice non di test (un task troppo grande si divide e si segnala).

| ID | Titolo | Ondata | Dipende da |
| --- | --- | --- | --- |
| F1R-01 | Guardrail automatici d'architettura | A | — |
| F1R-02 | `Clock`, seed e data iniettati | A | F1R-01 |
| F1R-03 | Room con schema esportato e migrazioni | A | F1R-01 |
| F1R-04 | Navigazione con rotte tipizzate e stato salvabile | A | F1R-01 |
| F1R-05 | Build di rilascio pulita | A | — |
| F1R-06 | Matrice screenshot multi-dispositivo | A | — |
| F1R-07 | Package a layer dentro `app:android` | A | F1R-01 |
| F1R-08 | `MatchSession` nel dominio | B | F1R-02, F1R-07 |
| F1R-09 | `MotionSpec` e `GameSpeed` | B | F1R-07 |
| F1R-10 | `TableDirector` e `TableScene` | B | F1R-08, F1R-09 |
| F1R-11 | `TableGeometry` e layout a zone | B | F1R-06, F1R-07 |
| F1R-12 | Tavolo disegnato dalla scena | B | F1R-10, F1R-11 |
| F1R-13 | Corsa ai punti e Maestro al tavolo | B | F1R-12 |
| F1R-14 | `RewardEngine` | C | F1R-02, F1R-07 |
| F1R-15 | Sequenza di fine partita | C | F1R-09, F1R-14 |
| F1R-16 | `CoachService` e spiegazioni localizzate | C | F1R-07 |
| F1R-17 | Review sulla regia del tavolo | C | F1R-12, F1R-16 |
| F1R-18 | Avversari come personaggi | C | F1R-12 |
| F1R-19 | Home senza voci finte | C | F1R-04 |
| F1R-20 | Audio a tre livelli | D | F1R-09 |
| F1R-21 | Revisione dei contenuti didattici | D | F1R-16 |
| F1R-22 | Estrazione in moduli Gradle | D | F1R-08…F1R-17 |
| F1R-23 | Dossier di una feature (ripetuto per ogni feature) | B–D | prerequisiti in `docs/app/features/README.md` |

Parallelismo: F1R-05 e F1R-06 subito; F1R-02, F1R-03, F1R-04 in parallelo dopo F1R-01; F1R-14 e F1R-16 in parallelo con l'ondata B.

---

## F1R-01 — Guardrail automatici d'architettura

- **Stato:** fatto
- **Riferimenti:** `docs/app/layers.md` §6, `docs/app/testing.md`
- **Obiettivo:** un test JVM nell'app (eseguito da `./gradlew check`) che legge i sorgenti e fallisce se viola i divieti di `layers.md` §6. Le violazioni già presenti vanno in una **lista di eccezioni** esplicita nel test, con il task che le eliminerà; la lista può solo accorciarsi.
- **Perimetro autorizzato:** nuovo test in `app:android/src/test`; nessuna libreria nuova.
- **Criteri di accettazione:**
    - [x] Il test copre: import tra layer, `delay`/`Thread.sleep`, `System.currentTimeMillis`/`LocalDate.now`/`Random(`, `fallbackToDestructiveMigration`, `ScopaState` fuori da `domain/match`.
    - [x] Ogni eccezione elencata cita il task F1R che la rimuove.
    - [x] Un esempio di violazione nuova fa fallire il test (test del test).
- **Domande aperte:** —

## F1R-02 — `Clock`, seed e data iniettati

- **Stato:** fatto
- **Riferimenti:** `docs/app/layers.md` §3
- **Obiettivo:** interfaccia `Clock` nel dominio e implementazione di sistema; tutti gli usi di `System.currentTimeMillis`, `LocalDate.now` e i seed `42L`/`12345L` passano da `Clock`. La data si rilegge al ritorno in primo piano (cambio giorno con app aperta).
- **Criteri di accettazione:**
    - [x] Nessuna eccezione residua di questo tipo nel test di F1R-01.
    - [x] Test: streak e puzzle del giorno cambiano giorno con un `Clock` finto che passa la mezzanotte.
- **Domande aperte:** —

## F1R-03 — Room con schema esportato e migrazioni

- **Stato:** fatto
- **Riferimenti:** ADR-0017
- **Obiettivo:** `exportSchema = true`, schema versionato, rimozione di `fallbackToDestructiveMigration()`, prima migrazione di prova e test con `MigrationTestHelper`.
- **Perimetro autorizzato:** `room-testing` come dipendenza di test; opzione `room.schemaLocation` nel `build.gradle.kts` dell'app.
- **Criteri di accettazione:**
    - [x] Aggiornare l'app da `main` attuale alla nuova versione conserva storico, partita in corso e wallet (test).
    - [x] Lo schema JSON è nel repository.
- **Domande aperte:** —

## F1R-04 — Navigazione con rotte tipizzate e stato salvabile

- **Stato:** fatto
- **Riferimenti:** `docs/app/layers.md` §4
- **Obiettivo:** `MaestroNavHost` con Navigation Compose e rotte `@Serializable`; argomenti solo identificativi (`matchId`); `MainActivity` diventa un guscio senza logica di dominio (missioni e crediti restano dove sono fino a F1R-14, ma escono dall'Activity in un ViewModel di guscio).
- **Perimetro autorizzato:** plugin `kotlin-serialization` nell'app (già nel catalog).
- **Criteri di accettazione:**
    - [x] Test: rotazione e ricreazione dell'Activity in partita, in review e nella Sfida del giorno tornano sulla stessa schermata con lo stesso stato.
    - [x] Il tasto indietro segue il back stack in ogni schermata.
    - [x] `MainActivity.kt` ≤ 120 righe.
- **Domande aperte:** —

## F1R-05 — Build di rilascio pulita

- **Stato:** fatto
- **Obiettivo:** strumenti di sviluppo (crash di prova, partita all'ultima mano, dialog con lo stack trace) visibili solo con `BuildConfig.DEBUG`; orientamento verticale (ADR-0016).
- **Criteri di accettazione:**
    - [x] Test: nella variante release le voci non compaiono.
    - [x] `screenOrientation="portrait"` nel manifest.
- **Domande aperte:** —

## F1R-06 — Matrice screenshot multi-dispositivo

- **Stato:** fatto
- **Riferimenti:** ADR-0016, `docs/app/responsive-ui.md` §4
- **Obiettivo:** un'utilità di test che esegue ogni screenshot sulle 4 configurazioni; tutti i test screenshot esistenti la usano. Si misurano le dimensioni reali del Pixel 10 Pro di Michele (`adb shell wm size`, `wm density`) e si aggiorna la tabella.
- **Criteri di accettazione:**
    - [x] La CI pubblica gli screenshot divisi per configurazione.
    - [x] Elenco dei difetti di layout trovati, allegato alla PR (si correggono nei task delle schermate, non qui).
- **Domande aperte:** misure reali del Pixel 10 Pro (`adb shell wm size`, `wm density`): fornite da Michele quando servono; per ora `w410dp-h914dp-xxxhdpi`.

## F1R-07 — Package a layer dentro `app:android`

- **Stato:** in revisione
- **Riferimenti:** ADR-0014 (Migrazione), `docs/app/layers.md` §1
- **Obiettivo:** spostare i file esistenti nei package `shell`, `domain`, `data`, `designsystem`, `feature.<nome>.{ui,presentation}` senza cambiare comportamento.
- **Criteri di accettazione:**
    - [x] Solo spostamenti e import: nessuna modifica di logica (diff verificabile).
    - [x] Tutti i test verdi; screenshot identici.
- **Domande aperte:** —

## F1R-08 — `MatchSession` nel dominio

- **Stato:** da fare
- **Riferimenti:** ADR-0015, `docs/app/layers.md` §3, `docs/app/table-director.md` §1–2
- **Obiettivo:** `MatchSession` sostituisce la logica di partita di `GameReducer`/`GameViewModel`: applica le mosse, produce `Beat`, salva, chiama il bot solo dopo `PresentationGate.awaitIdle`, con tempo di riflessione da `MotionSpec`. Le costanti `BOT_*_DELAY_MS` spariscono.
- **Criteri di accettazione:**
    - [ ] Test con gate finto: il bot non muove prima di `awaitIdle`.
    - [ ] Test: una partita completa su seed fisso produce lo stesso `MatchLog` di oggi.
    - [ ] La presentazione non riceve mai `ScopaState` (test F1R-01 senza eccezioni per questo punto).
- **Domande aperte:** —

## F1R-09 — `MotionSpec` e `GameSpeed`

- **Stato:** da fare
- **Riferimenti:** `docs/app/game-feel.md` §2
- **Obiettivo:** tabella unica dei tempi nel design system e impostazione "Velocità di gioco" (1×, 1,5×, 2×) in DataStore e nelle impostazioni.
- **Criteri di accettazione:**
    - [ ] Ogni durata dell'app viene da `MotionSpec` (test F1R-01).
- **Domande aperte:** —

## F1R-10 — `TableDirector` e `TableScene`

- **Stato:** da fare
- **Riferimenti:** ADR-0015, `docs/app/table-director.md`
- **Obiettivo:** regia in presentazione: coda di battute, sequenze di passi come dati, scena pubblicata, salto, velocità, ricostruzione dopo ricreazione, suoni e vibrazioni dai passi.
- **Criteri di accettazione:**
    - [ ] Test con tempo virtuale per ogni tipo di battuta (§3 della specifica).
    - [ ] Due battute ravvicinate vengono mostrate per intero e in ordine (test).
    - [ ] Salto e velocità portano alla stessa scena finale (test).
- **Domande aperte:** —

## F1R-11 — `TableGeometry` e layout a zone

- **Stato:** da fare
- **Riferimenti:** ADR-0016, `docs/app/responsive-ui.md` §2–3, `docs/app/game-feel.md` §4
- **Obiettivo:** geometria pura del tavolo con le zone di `game-feel.md` §4 (pile davanti a ciascun giocatore, mazzo a lato, mano intera).
- **Criteri di accettazione:**
    - [ ] Test puri sulle 4 configurazioni: nessuna zona fuori dai bordi, nessuna sovrapposizione mano/terra, carte a terra ≥ 56 dp.
- **Domande aperte:** —

## F1R-12 — Tavolo disegnato dalla scena

- **Stato:** da fare
- **Riferimenti:** `docs/app/table-director.md`, `docs/app/game-feel.md` §3–4
- **Obiettivo:** `TableScreen` riscritto come insieme di composable piccoli che disegnano `TableScene` con `TableGeometry`; nessun `delay`, nessuna logica di sequenza nei composable. Il vecchio `FeltLayout` viene rimosso.
- **Criteri di accettazione:**
    - [ ] Nessun file della feature tavolo sopra 400 righe.
    - [ ] Screenshot della matrice per: inizio smazzata, turno umano, presa in volo, scopa, riepilogo smazzata.
    - [ ] Playtest di Michele sul Pixel: nessuna animazione interrotta o fuori ordine.
- **Domande aperte:** —

## F1R-13 — Corsa ai punti e Maestro al tavolo

- **Stato:** da fare
- **Riferimenti:** `docs/app/game-feel.md` P4, §5
- **Obiettivo:** indicatori Carte, Denari, Settebello, Primiera calcolati dalla vista (numeri in Sparring, solo icone in Classica); Maestro a posto fisso con le regole di §5.
- **Criteri di accettazione:**
    - [ ] I valori della corsa ai punti sono calcolati da funzioni pure sulla vista, testate.
    - [ ] Il fumetto non si sovrappone mai a mano o carte a terra (test sulla geometria).
- **Domande aperte:** —

## F1R-14 — `RewardEngine`

- **Stato:** da fare
- **Riferimenti:** `docs/app/layers.md` §3
- **Obiettivo:** un solo punto per crediti, missioni, streak e maestria a fine partita, puzzle e lezione; rimuovere la stessa logica da `MainActivity`, `DailyPuzzleViewModel`, `DailyMissionsViewModel`, `ShopViewModel`.
- **Criteri di accettazione:**
    - [ ] Test: una fine partita produce un `RewardSummary` coerente con wallet e missioni.
    - [ ] Nessuna chiamata a `WalletRepository.addCredits` fuori da `RewardEngine` (test F1R-01).
- **Domande aperte:** —

## F1R-15 — Sequenza di fine partita

- **Stato:** da fare
- **Riferimenti:** `docs/app/game-feel.md` §6
- **Obiettivo:** schermata unica di fine partita che anima `RewardSummary` (targa, precisione, crediti, missioni, streak, maestria) con "Rivedi partita" come azione primaria.
- **Criteri di accettazione:**
    - [ ] Ogni passo saltabile; screenshot della matrice per vittoria e sconfitta.
- **Domande aperte:** —

## F1R-16 — `CoachService` e spiegazioni localizzate

- **Stato:** da fare
- **Riferimenti:** ADR-0007, `docs/design/maestro_persona.md`, `docs/app/game-feel.md` §8
- **Obiettivo:** spostare la logica di `ReviewExplanationEngine` e dei suggerimenti nel dominio come codici con argomenti; testi in `strings.xml` it/en; niente decimali di punteggio interno; plurali.
- **Criteri di accettazione:**
    - [ ] Nessun testo per l'utente nel codice Kotlin dell'app (test F1R-01 o lint).
    - [ ] Il suggerimento usa un `SeededRng` della sessione, non lo stato nascosto.
- **Domande aperte:** —

## F1R-17 — Review sulla regia del tavolo

- **Stato:** da fare
- **Riferimenti:** `docs/app/table-director.md` §5
- **Obiettivo:** Game Review e "Rigioca l'errore" usano `TableDirector` e `TableGeometry`; navigazione tra mosse animata; spiegazioni da `CoachService`.
- **Criteri di accettazione:**
    - [ ] La review mostra solo ciò che il giocatore sapeva più le carte uscite dopo (test esistente conservato).
    - [ ] File della feature review ≤ 400 righe ciascuno.
- **Domande aperte:** —

## F1R-18 — Avversari come personaggi

- **Stato:** da fare
- **Riferimenti:** decisione D11 (`docs/roadmap.md` §3)
- **Obiettivo:** ogni livello di difficoltà è un avversario con nome, ritratto e frase; scelta nella modale pre-partita secondo D11.
- **Domande aperte:** nomi e ritratti dei personaggi (Michele).

## F1R-19 — Home senza voci finte

- **Stato:** da fare
- **Obiettivo:** nascondere Amici, Classifica, messaggi e gemme finché non esistono; una sola chiamata all'azione principale; partita in corso in evidenza.
- **Criteri di accettazione:**
    - [ ] Ogni elemento toccabile della Home porta a una funzione esistente.
- **Domande aperte:** —

## F1R-20 — Audio a tre livelli

- **Stato:** da fare
- **Riferimenti:** `docs/app/game-feel.md` §7
- **Obiettivo:** musica, ambiente ed effetti con volumi separati; la musica si abbassa quando parla il Maestro.
- **Domande aperte:** asset audio forniti da Michele.

## F1R-21 — Revisione dei contenuti didattici

- **Stato:** da fare
- **Esecutore:** Michele, con un agente che prepara l'elenco dei testi
- **Obiettivo:** rivedere testi di Accademia, Check e spiegazioni della review (es. lezione 1.3: il Settebello vale 1 punto, non 3; "Articolo 3" non esiste in `docs/rules/scopa.md`).
- **Uscita:** testi corretti in `strings.xml`.

## F1R-22 — Estrazione in moduli Gradle

- **Stato:** da fare
- **Riferimenti:** ADR-0014
- **Obiettivo:** creare `app:domain`, `app:data`, `app:designsystem` e i moduli `app:feature:*`, spostando i package corrispondenti; aggiornare la tabella di `checkModuleBoundaries`. Si può dividere in un task per modulo.
- **Perimetro autorizzato:** nuovi moduli in `settings.gradle.kts` e relativi `build.gradle.kts`, tabella dei confini.
- **Criteri di accettazione:**
    - [ ] `./gradlew check` verifica i confini dei nuovi moduli.
    - [ ] Nessun cambio di comportamento; screenshot identici.
- **Domande aperte:** —

## F1R-23 — Dossier di una feature

- **Stato:** da fare (uno per feature, nell'ordine di `docs/app/features/README.md`)
- **Riferimenti:** `docs/app/features/README.md`
- **Obiettivo:** scrivere `docs/app/features/<feature>.md` con il modello indicato: stato attuale con screenshot della matrice, problemi, comportamento desiderato, contratti, task proposti, domande.
- **Perimetro autorizzato:** solo il file del dossier; nessun codice.
- **Criteri di accettazione:**
    - [ ] Ogni problema cita il file o lo screenshot che lo mostra.
    - [ ] I task proposti sono < 400 righe ciascuno e hanno criteri verificabili.
    - [ ] Michele ha approvato il dossier (stato `approvato`) prima che i task entrino nel backlog.
- **Domande aperte:** —
