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

- **Stato:** fatto
- **Riferimenti:** ADR-0014 (Migrazione), `docs/app/layers.md` §1
- **Obiettivo:** spostare i file esistenti nei package `shell`, `domain`, `data`, `designsystem`, `feature.<nome>.{ui,presentation}` senza cambiare comportamento.
- **Criteri di accettazione:**
    - [x] Solo spostamenti e import: nessuna modifica di logica (diff verificabile).
    - [x] Tutti i test verdi; screenshot identici.
- **Domande aperte:** —

## F1R-08 — `MatchSession` nel dominio

- **Stato:** fatto (parte 1 e parte 2)
- **Riferimenti:** ADR-0015, `docs/app/layers.md` §3, `docs/app/table-director.md` §1–2
- **Obiettivo:** `MatchSession` sostituisce la logica di partita di `GameReducer`/`GameViewModel`: applica le mosse, produce `Beat`, salva, chiama il bot solo dopo `PresentationGate.awaitIdle`, con tempo di riflessione da `MotionSpec`. Le costanti `BOT_*_DELAY_MS` spariscono.
- **Criteri di accettazione:**
    - [x] Test con gate finto: il bot non muove prima di `awaitIdle`.
    - [x] Test: una partita completa su seed fisso produce lo stesso `MatchLog` di oggi.
    - [x] La presentazione non riceve mai `ScopaState` (test F1R-01 senza eccezioni per questo punto).
- **Suddivisione (2026-10-27):** il task supera le 400 righe e si divide in due parti.
    - **Parte 1:** il bot aspetta la fine della scena tramite `PresentationGate`, alimentato dal tavolo attuale (`UiPresentationGate`), e la riflessione da `MotionSpec`. Le costanti `BOT_*_DELAY_MS` spariscono. Test `BotWaitsForPresentationTest`.
    - **Parte 2 (F1R-08b nel dossier `docs/app/features/table.md`):** `MatchSession` con le `Beat` e il reducer che lavora solo sulla vista. Chiude i criteri "stesso `MatchLog`" e "nessuno `ScopaState` in presentazione".
- **Nota parte 2:** `MatchSession` (domain/match) è l'unico a vedere `ScopaState`; il reducer lavora su `MatchSession` e viste. Le `Beat` vanno alla regia (`TableDirector`), che è anche il cancello del bot (F1R-12a).
- **Domande aperte:** —

## F1R-09 — `MotionSpec` e `GameSpeed`

- **Stato:** in revisione
- **Riferimenti:** `docs/app/game-feel.md` §2
- **Obiettivo:** tabella unica dei tempi nel design system e impostazione "Velocità di gioco" (1×, 1,5×, 2×) in DataStore e nelle impostazioni.
- **Criteri di accettazione:**
    - [ ] Ogni durata dell'app viene da `MotionSpec` (test F1R-01).
- **Domande aperte:** —

## F1R-10 — `TableDirector` e `TableScene`

- **Stato:** in revisione
- **Riferimenti:** ADR-0015, `docs/app/table-director.md`
- **Obiettivo:** regia in presentazione: coda di battute, sequenze di passi come dati, scena pubblicata, salto, velocità, ricostruzione dopo ricreazione, suoni e vibrazioni dai passi.
- **Criteri di accettazione:**
    - [x] Test con tempo virtuale per ogni tipo di battuta (§3 della specifica).
    - [x] Due battute ravvicinate vengono mostrate per intero e in ordine (test).
    - [x] Salto e velocità portano alla stessa scena finale (test).
- **Domande aperte:** —

## F1R-11 — `TableGeometry` e layout a zone

- **Stato:** fatto
- **Riferimenti:** ADR-0016, `docs/app/responsive-ui.md` §2–3, `docs/app/game-feel.md` §4
- **Obiettivo:** geometria pura del tavolo con le zone di `game-feel.md` §4 (pile davanti a ciascun giocatore, mazzo a lato, mano intera).
- **Criteri di accettazione:**
    - [x] Test puri sulle 4 configurazioni: nessuna zona fuori dai bordi, nessuna sovrapposizione mano/terra, carte a terra ≥ 56 dp.
- **Domande aperte:** —

## F1R-12 — Tavolo disegnato dalla scena

- **Stato:** in revisione (12a, 12b, 12c fatte; manca il playtest di Michele sul Pixel)
- **Riferimenti:** `docs/app/table-director.md`, `docs/app/game-feel.md` §3–4
- **Obiettivo:** `TableScreen` riscritto come insieme di composable piccoli che disegnano `TableScene` con `TableGeometry`; nessun `delay`, nessuna logica di sequenza nei composable. Il vecchio `FeltLayout` viene rimosso.
- **Criteri di accettazione:**
    - [x] Nessun file della feature tavolo sopra 400 righe.
    - [x] Screenshot della matrice per: inizio smazzata, turno umano, presa in volo, scopa, riepilogo smazzata.
    - [ ] Playtest di Michele sul Pixel: nessuna animazione interrotta o fuori ordine.
- **Suddivisione (2026-10-27, dossier `docs/app/features/table.md` approvato):**
    - **F1R-12a — Tavolo a riposo (fatto, 2026-10-27):** layout a zone di `TableGeometry` che disegna `TableScene` senza animazioni; via "Ultima giocata"; mano a ventaglio leggero con carte intere; avversario con nome e ritratto. Screenshot della matrice senza D3–D5.
    - **F1R-12b — Passi animati (fatto, 2026-10-27):** ogni `SceneStep` animato con la durata di `MotionSpec`; suoni e vibrazioni dai passi; salto con un tocco.
    - **F1R-12c — Dialog e pulizia (fatto, 2026-10-27):** scelta presa, riepilogo, domanda, pausa sulla scena; `FeltLayout` rimosso; nessun file > 400 righe.
- **Domande aperte:** —

## F1R-13 — Corsa ai punti e Maestro al tavolo

- **Stato:** in revisione (corsa ai punti e fumetto fatti; le battute di §5 aspettano i testi di Michele)
- **Riferimenti:** `docs/app/game-feel.md` P4, §5
- **Obiettivo:** indicatori Carte, Denari, Settebello, Primiera calcolati dalla vista (numeri in Sparring, solo icone in Classica); Maestro a posto fisso con le regole di §5.
- **Criteri di accettazione:**
    - [x] I valori della corsa ai punti sono calcolati da funzioni pure sulla vista, testate.
    - [x] Il fumetto non si sovrappone mai a mano o carte a terra (test sulla geometria).
- **Domande aperte:** le battute automatiche del Maestro di `game-feel.md` §5 (saluto a inizio partita, promemoria prima dell'ultima distribuzione, battuta sulla scopa subita) hanno bisogno dei testi: li approva Michele (§8). Per ora il Maestro ha solo le pose e il consiglio su richiesta.

## F1R-14 — `RewardEngine`

- **Stato:** fatto (14a e 14b)
- **Riferimenti:** `docs/app/layers.md` §3
- **Obiettivo:** un solo punto per crediti, missioni, streak e maestria a fine partita, puzzle e lezione; rimuovere la stessa logica da `MainActivity`, `DailyPuzzleViewModel`, `DailyMissionsViewModel`, `ShopViewModel`.
- **Criteri di accettazione:**
    - [x] Test: una fine partita produce un `RewardSummary` coerente con wallet e missioni.
    - [x] Nessuna chiamata a `WalletRepository.addCredits` fuori da `RewardEngine` (test F1R-01).
- **Suddivisione (2026-10-27):** il task tocca più feature e supera le 400 righe.
    - **14a (fatto):** `domain/rewards/RewardEngine` e `RewardSummary`; fine partita, puzzle, lezione, riscossione missioni e bonus di benvenuto passano da qui (`ShellViewModel`, `DailyPuzzleViewModel`, `AcademyViewModel`, `DailyMissionsViewModel`). Regola `CREDITS_OUTSIDE_REWARD_ENGINE` nei guardrail. Nessun credito per la partita: la spec non ne assegna (`game-feel.md` §6 parla di crediti che volano, ma non dice quanti: vedi domanda aperta).
    - **14b (fatto, 2026-10-27):** interfacce e modelli di wallet, partita (`MatchRepository`), maestria, streak e missioni spostati in `domain/wallet`, `domain/match`, `domain/mastery`, `domain/retention` (le implementazioni restano in `data`); i titoli delle missioni passano dal dominio alla UI (`MissionText.kt`). `RewardEngine`, missioni, profilo e tavolo non importano più `data`. **Restano fuori** i cataloghi di contenuto legati a risorse e al design system: cosmetici e impostazioni (revisione Bottega e Impostazioni), sfida del giorno (revisione della sfida), Accademia (F1R-21); le eccezioni `UI_IMPORTS_DATA` di quelle feature sono ri-etichettate col task che le toglie.
- **Decisione di Michele (2026-10-27):** le partite contro i bot non danno crediti per ora; un sistema monetario verrà più avanti. La sequenza di fine partita (F1R-15) non mostra crediti di partita: solo missioni, streak e maestria. I crediti da missioni, sfida e lezioni restano.
- **Domande aperte:** —

## F1R-15 — Sequenza di fine partita

- **Stato:** in revisione (dossier `docs/app/features/post-match.md` approvato il 2026-10-27; 15a–d fatte, attende la prova sul dispositivo di Michele). Fatti: 15a `PostMatchViewModel` e sequenza, 15b schermata a passi, 15c missioni avanzate e riscossione con un tocco, 15d contenuti centrati che seguono i passi e velocità di gioco. I suoni della sequenza vanno con F1R-20 (audio)
- **Riferimenti:** `docs/app/game-feel.md` §6
- **Obiettivo:** schermata unica di fine partita che anima `RewardSummary` (targa, precisione, missioni, streak, maestria; **senza crediti di partita**, vedi D19) con "Rivedi partita" come azione primaria.
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

- **Stato:** in revisione (dossier `docs/app/features/review.md` approvato il 2026-10-27). Fatti: 17a la revisione sul tavolo vero (`StageBoard`) pilotato da `ReviewStage`/`TableDirector`, con la mossa giocata e quella migliore animate; 17b caricamento intero, «Perché?» al posto dello scorrimento, «Indietro», fine partita con caricamento unico; 17c le spiegazioni sono `ReviewCoach` (codici, vere nella posizione) nel dialogo «Perché?», senza decimali; 17d «Riprova la mano» a livelli (stimolo, poi indizio, soluzione a richiesta). Attende la prova di Michele sul telefono.
- **Riferimenti:** `docs/app/table-director.md` §5
- **Obiettivo:** Game Review e "Rigioca l'errore" usano `TableDirector` e `TableGeometry`; navigazione tra mosse animata; spiegazioni da `CoachService`.
- **Criteri di accettazione:**
    - [ ] La review mostra solo ciò che il giocatore sapeva più le carte uscite dopo (test esistente conservato).
    - [ ] File della feature review ≤ 400 righe ciascuno.
- **Domande aperte:** 17a (regia animata) supera il perimetro di una singola PR: `TableStage`/`PlayArea` sono interni al tavolo e legati a `GameUiState`. Proposta (da approvare): spezzare 17a in (1) estrarre dal tavolo uno `TableStage` di sola lettura che accetti `StageModel` + `TableGeometry` senza `GameUiState` (tocca `feature/table`, API di un altro modulo/feature), (2) costruire i `Beat` dalla revisione (`viewBefore`/`viewAfter`/eventi) e pilotare un `TableDirector` dedicato. Nel frattempo la revisione resta statica. Michele ha approvato la suddivisione (2026-10-27): fatta.

## F1R-18 — Avversari come personaggi

- **Stato:** in corso (dossier `docs/app/features/pre-game.md` approvato il 2026-10-28; sotto-task 18a–e). Fatto: 18a modello (`Opponent`, `OpponentProgress`, `OpponentRepository` su DataStore), 18b scala degli avversari (nuova schermata dopo «Gioca»; la preparazione mostra l'avversario scelto al posto dei livelli; testi [bozza]) e 18d asset, 18c vittoria in Classica registrata dal `RewardEngine` (partita finita, vinta dall'umano, nessun fondamentale: sblocca il successivo; Sparring non conta). 18e voce e titolo allo sblocco in fine partita (riquadro ricompensa + battuta del Maestro) e titolo sulla scala; titoli e battute [bozza]. Resta aperto per il Profilo/esperienza/obiettivi (dossier Profilo) e l'incoraggiamento prima di una sfida difficile. Resta 18e
- **Riferimenti:** decisione D11 (`docs/roadmap.md` §3), `docs/app/features/pre-game.md`, prompt asset in `docs/art/asset-prompts.md` (Set Avversari)
- **Obiettivo:** cinque avversari con nome, titolo e ritratto in una scala di sfida: si sblocca il successivo battendo il precedente in una partita completa in Classica. Progressi in DataStore; ricompense: titoli (esperienza e obiettivi quando esisteranno), nessuna moneta.
- **Domande aperte:** nomi e titoli definitivi (proposte nel prompt degli asset); asset di Michele per F1R-18d: **consegnati il 2026-10-28** (ritratti neutri, vittoria/sconfitta, stemmi, lucchetti, in `res/drawable-nodpi`; i ritratti hanno la cornice in ottone già inclusa) e completati lo stesso giorno con le due espressioni rigenerate (Set 5 bis): asset al completo.

## F1R-19 — Home senza voci finte

- **Stato:** in revisione (dossier `docs/app/features/home.md` approvato il 2026-10-27; suddivisione in 19a…e). Fatti: 19a voci finte fuori (gemme, busta, Amici, Briscola, Classifica), 19b un solo «Gioca» e destinazioni vere, avatar → Profilo e portafoglio → Bottega, 19c nome e Maestria veri: il nome si chiede al primo avvio (dialogo, «Più tardi» non lo richiede) e si cambia toccandolo nella Home. 19d il Maestro in Home parla dello stato del giocatore (partita in corso, serie, benvenuto; 3 varianti al giorno, testi [bozza] da approvare). 19e messa a punto dello spazio: nessun difetto segnalato dopo la prova del 2026-10-28, chiuso salvo nuove segnalazioni
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

## F1R-25 — Suggerimenti e Check in partita

- **Stato:** in corso (dossier `docs/app/features/coach-in-game.md` approvato il 2026-10-28). Fatto: 25f momento del Check per obiettivo e partita (Perimetro autorizzato da Michele il 2026-10-28 per `isCriticalTrainingMoment` in `core:game`): mai prima della 12ª carta, comunque dalla 24ª, nel mezzo solo se la posizione è significativa per l'obiettivo; una regola per obiettivo, pronta per le varianti. 25e (prima parte, approvata da Michele il 2026-10-28) obiettivo consigliato dal radar: l'asse più basso tra Sette, Denari, Primiera (almeno 3 partite; parità: Sette, Denari, Primiera), preselezionato in preparazione con «Il Maestro consiglia». «Conta le Scope» scartato da Michele; «carte nel mazzo» non si fa senza nuova indicazione (nessuna modifica a `core:game`). Testi [bozza] di 18e, 25b, 25c approvati. Momento del Check: proposta con i dati di simulazione nel dossier, da approvare. 25d punteggio di attenzione collegato: era già in scheda di fine partita, nel radar (Maestria) e nella missione sui Check dei Sette; aggiunti i test che lo fissano, nessun credito. 25c scelta dell'obiettivo in preparazione (Sparring): cinque obiettivi con nome semplice e riga «cosa allena» (testi [bozza]); la schermata ora scorre e «Gioca» resta fisso. 25b indizio + risposta (sui conteggi un indizio e un secondo tentativo, sulle altre domande risposta e spiegazione subito; punteggio sul primo tentativo; testi [bozza] da approvare), 25a Check come fumetto del Maestro, nella zona del Maestro sopra la mano, senza finestra modale (il momento nella smazzata resta quello di oggi, da rivedere con i dati). Sotto-task: 25a Check come fumetto (con scelta del momento nella smazzata), 25b feedback a indizio + risposta con la voce del Maestro, 25c scelta obiettivo con nomi semplici, 25d punteggio di attenzione collegato, 25e obiettivi adattivi (elenco da approvare prima del codice). Check solo in Sparring, uno per smazzata
- **Riferimenti:** `docs/app/features/coach-in-game.md`, F1R-24, F1R-16
- **Domande aperte:** momento del Check nella smazzata; elenco degli obiettivi adattivi (25e)

## F1R-24 — La voce del Maestro

- **Stato:** in corso (dossier approvato il 2026-10-27; viene prima di F1R-15). Fatti: 24a `MatchFacts`, 24b `MomentDirector`, 24c prontuario (bozza testi it/en da approvare) e `VoiceComposer`. 24d fumetto al tavolo, impostazione «Chiacchiere» e suggerimento ricordato. 24f trascrizioni (test che scrive `app/android/build/maestro-transcripts/*.md`). 24e consiglio dinamico (`HintComposer`: nomina le carte e la ragione; livelli maieutici da decidere con Michele). 24g parte 1: `ReviewCoach` (spiegazioni in codici, vere nella posizione, che ammettono quando la mano non basta; «Riprova la mano» non si propone con 1–2 scelte) e simulazione delle spiegazioni (`ReviewSimulationTest` → `app/android/build/review-simulation/`). 24g parte 2 (in parte): le differenze trascurabili (perdita ≤ 0,10) non sono errori né «mossa migliore» ma «Buona mossa» (`ReviewVerdict`). Dopo la prova di Michele (2026-10-27): consiglio a **due livelli** (stimolo, poi soluzione al secondo tocco) e Maestro più proattivo e vario (nuovi momenti: Settebello in mano, banco vuoto, grossa presa, avanti/indietro, «colore»; più battute per smazzata; varianti non ripetute). Da fare: altre simulazioni e messa a punto, punteggi senza decimali nella schermata con F1R-17
- **Riferimenti:** `docs/app/features/maestro-voice.md`, `docs/design/maestro_persona.md`, `docs/app/game-feel.md` §5, F1R-13, F1R-16
- **Obiettivo:** battute spontanee del Maestro dinamiche (fatti → momenti → battuta componibile), nei momenti chiave e con ritmo controllato, più consiglio dinamico e offerta d'aiuto, e la stessa voce dinamica con tuning nella revisione (Game Review). Il dossier propone la suddivisione in F1R-24a…g.
- **Criteri di accettazione:** definiti dal dossier dopo l'approvazione di Michele.
- **Domande aperte:** vedi §6 del dossier.

## F1R-26 — Profilo, Maestria e Radar

- **Stato:** da fare (dossier `docs/app/features/profile.md` approvato il 2026-10-28)
- **Riferimenti:** `docs/app/features/profile.md` §3–§6, F1R-14, F1R-18e
- **Sotto-task:** 26a storico con avversario e modalità, 26b scheda giocatore e titolo scelto dall'utente, 26c la scala nel Profilo, 26d radar leggibile e consiglio che porta all'allenamento, 26e statistiche e tab «Partite», 26f divisione del file e matrice finale. Ordine delle sezioni: opzione B (radar dopo la scheda), modificabile.
- **Dipendenze:** esperienza e obiettivi arrivano da F1R-29.
- **Stato avanzamento:** 26a…26f fatti (ProfileScreen sotto le 400 righe; matrice con tutti gli stati del dossier).
- **Domande aperte:** (26c) il dossier chiede, al tocco su uno stemma, anche «quando è stato battuto la prima volta», ma `profile.md` §4 dice di non salvare dati nuovi sulla scala e oggi `OpponentProgress` ricorda solo chi è battuto, non la data. Per ora il dettaglio mostra nome e titolo. Serve una decisione di Michele: aggiungere la data (un campo in più nel salvataggio degli avversari battuti) oppure togliere la data dal dossier.

## F1R-27 — Sfida del Giorno

- **Stato:** da fare (dossier `docs/app/features/daily-puzzle.md` approvato il 2026-10-28)
- **Riferimenti:** `docs/app/features/daily-puzzle.md`, F1R-02, F1R-10
- **Sotto-task:** 27a testi fuori dal codice, 27b indizio e «Mostrami la soluzione» con la voce del Maestro, 27c condivisione senza spoiler né errori, 27d mini-tavolo sulla regia, 27e flusso e stati con livello consigliato dal radar e serie di sfide distinta dalla serie di giorni. Archivio dei giorni passati: fuori (a pagamento).
- **Domande aperte:** —

## F1R-28 — Accademia

- **Stato:** da fare (dossier `docs/app/features/academy.md` approvato il 2026-10-28)
- **Riferimenti:** `docs/app/features/academy.md`, F1R-21
- **Sotto-task:** 28a contenuti coerenti con la specifica (revisione F1R-21 a cura dell'agente), 28b percorso libero dal capitolo 2 e capitolo consigliato dal radar, 28c ripasso lampo, 28d titoli di capitolo, 28e mini-tavolo sulla regia, 28f divisione dei file e matrice finale. Moduli a pagamento: fuori.
- **Stato avanzamento:** 28a fatto: corretti gli errori certi (articoli inesistenti, Settebello a +3 punti, «Re dei Denari», seme mancante come «0 punti», scopa all'ultima giocata, nomi inglesi Clubs/Knight); ogni lezione dichiara i paragrafi della specifica (`specRefs`) e `AcademyContentTest` li controlla. 28b fatto: il capitolo 1 resta sequenziale, i capitoli 2–4 si aprono liberi a capitolo 1 finito (`AcademyPath`, condiviso da DataStore e in-memory); il Maestro consiglia un capitolo dal radar (Primiera→2, Banco→3, Sette e Finale→4, Denari→2; con meno di 3 partite nessun consiglio; con il capitolo 1 da finire consiglia sempre l'1) e la mappa mostra «Consigliato dal Maestro». 28c fatto: «Ripasso lampo» sulle lezioni finite (la domanda di verifica e la regola d'oro, senza crediti; `AcademyRepository.markReviewed` salva il giorno e la tessera mostra «Ripassata oggi»). 28d fatto: ogni capitolo finito dà un titolo (Allievo del Circolo, Cacciatore di Primiere, Custode del Banco, Occhio del Maestro; testi miei [bozza]) mostrato sulla mappa e, via `RewardEngine.onChapterCompleted`, 50 PX e gli obiettivi «Studente» e «Allievo del Maestro» (nuovi: `student`, `academy_complete`); si assegna solo quando l'ultima lezione mancante viene completata, quindi rigiocare non ripaga. 28e fatto: le carte del mini-tavolo delle lezioni arrivano una alla volta con `MovingCard` (la stessa del tavolo) e tempi di `MotionSpec` (`DEAL_CARD`) alla velocità scelta nelle Impostazioni; nessun `delay` nuovo (solo `MotionSpec.pause`). Misure dallo spazio disponibile. 28f in parte: i contenuti sono divisi in un file per capitolo (`AcademyChapter1…4.kt`, `AcademyContent.kt` resta l'indice). **Non fatta** la divisione di `AcademyScreen.kt` (1188 righe): in 5 file da meno di 400 righe compila, ma `ArchitectureRulesTest` chiede un'eccezione per ogni nuovo file che importa i tipi dei contenuti (`data.local.academy`), e la lista «può solo accorciarsi». Serve una decisione di Michele (vedi Domande aperte).
- **Domande aperte (28f):** per dividere `AcademyScreen.kt` posso (a) aggiungere a `ArchitectureRulesTest` quattro eccezioni uguali a quella già esistente (stesso debito F1R-21, solo spostato), oppure (b) spostare prima i modelli dell'Accademia (`AcademyLesson`, `AcademyStep`, …) nel dominio, che è la migrazione F1R-21 e tocca anche i tipi con risorse Android. Quale preferisci?
- **Da rileggere (Michele, testi [bozza]):** le lezioni 4.1 e 4.2 parlano di deduzioni «con certezza» (il giocatore può giocare qualunque carta, §4.4); la 4.3 dice che il Fante di Coppe «assicura l'ultima presa» (l'Asso resta a terra); i titoli «La Regola del Dispari» (3.3) e «L'Ultima Mano del Mazziere» (3.4) non corrispondono a regole della specifica; alcuni «sempre» sono consigli, non regole (1.4, 2.4, 3.2).
- **Domande aperte:** —

## F1R-29 — Missioni, serie, esperienza e obiettivi

- **Stato:** in corso (dossier `docs/app/features/missions.md` approvato il 2026-10-28). Fatto: 29a missioni vere (ambito per partita o per giorno, estrazione uniforme con seme proprio), 29b missione dei Check su ogni obiettivo (solo a chi ha giocato Sparring negli ultimi 14 giorni) e una missione al giorno che segue l'asse più debole del radar (con almeno 3 partite; nuova missione «Re della Primiera»), 29c Gettone Riposo che si ricarica (uno ogni 7 giorni di serie, massimo 2; `RewardSummary.freezeTokenEarned`; il messaggio senza rimproveri a serie spezzata arriva con la schermata «Oggi», 29f), 29d esperienza (contatore a 10 livelli ogni 250 PX; fonti: prima vittoria su un avversario 100×posto, livello della Sfida 10/15/20, missione riscossa 10, tre su tre +25; il capitolo dell'Accademia arriva con 28d; nomi dei livelli e vista nel Profilo con 29f/26b), 29e obiettivi (18 con identificatori stabili per Google Play Games, `AchievementRepository` con contatori, sblocco da `RewardEngine` una volta sola, `RewardSummary.achievementsUnlocked`; quelli di Accademia, Bottega, titoli e «occhio di lince» arrivano con le rispettive feature; nomi e descrizioni in 29f), 29f schermata «Oggi» (missioni, serie con Gettoni Riposo, esperienza con livello, obiettivi sbloccati; sostituisce il dialogo della Home; l'avviso di sblocco in fine partita arriva con la feature «fine partita»)
- **Riferimenti:** `docs/app/features/missions.md`, F1R-14
- **Sotto-task (29f in due parti: f1 testi di livelli e obiettivi, fatta; f2 schermata «Oggi» con avviso di sblocco):** 29a missioni vere, 29b missioni dei Check e adattive, 29c Gettone Riposo e serie, 29d esperienza (livelli del §6), 29e obiettivi (elenco esteso del §6, identificatori stabili per Google Play Games, aggancio futuro), 29f schermata «Oggi».
- **Domande aperte:** —

## F1R-30 — Bottega e wallet

- **Stato:** da fare (dossier `docs/app/features/shop.md` approvato il 2026-10-28)
- **Riferimenti:** `docs/app/features/shop.md`, `docs/app/features/shop-assets.md`
- **Sotto-task:** 30a catalogo nel dominio e stile dal catalogo, 30b anteprime vere e «Provalo», 30c acquisto con conferma sempre e eventi tipizzati, 30d movimenti (da valutare), 30e premio dall'ultimo avversario come stemma o titolo, 30f divisione del file e matrice finale. Acquisti a pagamento e annunci: fuori.
- **Domande aperte:** nuovi contenuti grafici (li genera Michele dalla lista di `shop-assets.md`).

## F1R-31 — Impostazioni

- **Stato:** da fare (dossier `docs/app/features/settings.md` approvato il 2026-10-28)
- **Riferimenti:** `docs/app/features/settings.md`, F1R-20, `docs/app/features/audio-prompts.md`
- **Sotto-task:** 31a schermata a sezioni e `SettingsViewModel` (con accesso rapido a «Chiacchiere» dal tavolo, se fattibile), 31b volumi e vibrazione, 31c lingua, 31d informazioni, privacy e segnalazioni, 31e divisione dei file e matrice finale. Musica e ambiente: F1R-20, con asset generati da Michele.
- **Domande aperte:** asset audio di Michele.
