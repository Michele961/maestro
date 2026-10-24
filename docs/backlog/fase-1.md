# Backlog — Fase 1: MVP offline Android

Approvato da Michele il 2026-10-23. Le decisioni a cui i task rimandano (D1–D9) sono in `docs/roadmap.md` §3.

Obiettivo della fase: un'app Android installabile in cui si gioca a Scopa contro il Maestro, a più livelli, con anteprima post-partita, Game Review e Check di allenamento, tutto offline. Nessun server, nessun pagamento.

**Gate di uscita:** partita completa giocata da un tester esterno contro ogni livello; Elo di ogni livello vicino al suo obiettivo e ogni livello più forte del precedente (D3); p95 del tempo per mossa del livello Maestro sotto 200 ms sul Pixel 10 Pro e sotto 800 ms sul profilo di fascia bassa (D4); zero crash su almeno 50 partite di test; etichette della review giudicate sensate nel playtest umano (F1-16).

Lo stato di ogni task è **solo** nella sua riga **Stato** (`da fare` · `in corso` · `in revisione` · `fatto` · `bloccato`, vedi `docs/workflow.md`). Gli agenti modificano solo **Stato** e **Domande aperte** del proprio task.

| ID | Titolo | Dipende da |
| --- | --- | --- |
| F1-00 | Guardrail mancanti per gli agenti | — |
| F1-01 | Target Android nei moduli `core:*` | F1-00 |
| F1-02 | Modulo `app:android` e CI con APK | F1-01 |
| F1-03 | Livelli di difficoltà del Maestro | F1-00 |
| F1-04 | Misura delle prestazioni su dispositivo | F1-02, F1-03 |
| F1-05 | Stato della partita nella UI (MVI) | F1-02 |
| F1-06 | Schermata del tavolo con carte segnaposto | F1-05 |
| F1-07 | Flusso completo della partita | F1-06 |
| F1-08 | Salvataggio, ripresa e impostazioni | F1-07 |
| F1-09 | Anteprima post-partita | F1-07 |
| F1-10 | Game Review mossa per mossa | F1-09 |
| F1-11 | Suggerimento su richiesta in Sparring | F1-07 |
| F1-12 | Check di allenamento | F1-07 |
| F1-13 | Guida di stile e asset definitivi | F1-06 |
| F1-14 | Audio, animazioni del Maestro, Crashlytics e build di rilascio | F1-13 |
| F1-15 | Test interno su Play Console | F1-08, F1-10, F1-14 |
| F1-16 | Playtest con giocatori umani | F1-15 |
| F1-17 | Report di fine fase | F1-16 |
| F1-18 | Componenti UI del menu con gli asset definitivi | F1-13 |
| F1-19 | Fondamenta UI: tipografia, sfondi e componenti condivisi | F1-18 |
| F1-20 | Modale "Preparati a giocare" | F1-19 |
| F1-21 | Tavolo da gioco con gli asset definitivi | F1-19 |
| F1-22 | Fine partita e Game Review con gli asset definitivi | F1-19 |
| F1-23 | Impostazioni, pausa e check didattici | F1-19 |
| F1-24 | Movimento e feedback (UX) | F1-20, F1-21, F1-22, F1-23 |

Si possono lavorare in parallelo: F1-03 con F1-01/F1-02; F1-09…F1-12 tra loro dopo F1-07; F1-13 (soprattutto lavoro di Michele) dal momento in cui esiste il tavolo.

---

## F1-00 — Guardrail mancanti per gli agenti

- **Stato:** fatto
- **Modalità:** sessione interattiva con Michele (tutti i file sono protetti).
- **Obiettivo:** creare i file che `CLAUDE.md` cita ma che non esistono: template della PR (con sezioni "Fuori dal perimetro" e "Domande aperte"), `.claude/commands/start-task.md`, `.claude/commands/review-task.md`, `.claude/settings.json` con conferma sui file protetti. Aggiornare `README.md` (stato: Fase 1) e la tabella "Stack per fase" di `docs/architecture.md` con le librerie approvate in D5.
- **Perimetro autorizzato:** `.github/`, `.claude/`, `README.md`, tabella "Stack per fase" di `docs/architecture.md`.
- **Criteri di accettazione:**
    - [ ] I quattro file esistono e corrispondono a quanto descritto in `CLAUDE.md` §6–7.
    - [ ] Michele ha approvato il testo dei comandi.
- **Domande aperte:** —

## F1-01 — Target Android nei moduli `core:*`

- **Stato:** fatto
- **Riferimenti:** `docs/architecture.md` (Target), ADR-0001, ADR-0010
- **Obiettivo:** aggiungere `androidTarget()` a `core:game`, `core:ai`, `core:protocol` senza cambiare codice comune. I test comuni girano anche sul target Android (unit test locali).
- **Perimetro autorizzato:** Android Gradle Plugin stabile (D5) nel version catalog e nei `build.gradle.kts` dei moduli `core:*`; Android SDK nel workflow di CI.
- **Criteri di accettazione:**
    - [ ] `./gradlew check` verde in CI con il target Android.
    - [ ] `core:game` continua a dipendere solo dalla stdlib (`checkModuleBoundaries`).
    - [ ] Nessun file in `commonMain` modificato.
- **Domande aperte:** —

## F1-02 — Modulo `app:android` e CI con APK

- **Stato:** fatto
- **Riferimenti:** ADR-0012, ADR-0007, `docs/architecture.md` (Moduli)
- **Obiettivo:** creare `app:android` (una sola Activity, Compose, Koin, `minSdk 26`, `applicationId = "it.maestro.app"`), tema base, risorse stringhe `it` ed `en`, schermata iniziale vuota con il nome dell'app ("Maestro – Scopa e Briscola", D8). La CI produce l'APK di debug.
- **Perimetro autorizzato:** nuovo modulo `app:android` in `settings.gradle.kts` e nella tabella dei confini del `build.gradle.kts` root; Compose, Koin, Navigation Compose, kotlinx-coroutines, AndroidX Lifecycle/ViewModel per Compose, Compose UI test (D5) nel version catalog.
- **Criteri di accettazione:**
    - [ ] `./gradlew check` e `./gradlew :app:android:assembleDebug` verdi in CI.
    - [ ] `app:android` dipende solo da `core:*` tra i moduli del progetto.
    - [ ] Nessuna stringa per l'utente scritta nel codice (ADR-0007): un test o un controllo lint lo verifica.
- **Domande aperte:** —

## F1-03 — Livelli di difficoltà del Maestro

- **Stato:** fatto
- **Riferimenti:** `docs/ai/maestro.md` (Livelli di difficoltà), `docs/reports/fase-0.md` §5–6, decisioni D2 e D3
- **Obiettivo:** in `core:ai`, 5 livelli di difficoltà (Principiante, Dilettante, Intermedio, Avanzato, Maestro) come configurazioni di `PimcBot`: campioni, nodi, rumore nella scelta, memoria imperfetta (D3). Il simulatore accetta `--p1 level:<nome>` e calcola l'Elo dei livelli facendoli giocare tra loro. `PRIMIERA_DIFF` passa a 0,025 (D2). Parametri ed Elo finali si fissano con Michele sui dati del task.
- **Perimetro autorizzato:** aggiungere livelli ed Elo al simulatore; aggiornare in `docs/ai/maestro.md` il peso della primiera e la sezione "Livelli di difficoltà" con i valori approvati.
- **Criteri di accettazione:**
    - [x] Ogni livello è deterministico a parità di seed (test).
    - [x] La "memoria imperfetta" usa solo la vista: il bot non riceve mai lo stato completo (test).
    - [x] Report nella PR: Elo misurato di ogni livello (obiettivo di partenza 800 / 1100 / 1400 / 1700 / 2000), win-rate di ogni livello contro il successivo su 1000 partite, precisione media con `MatchAnalyzer`.
    - [x] Ogni livello batte il precedente con margine significativo.
    - [x] Confronto `PRIMIERA_DIFF` 0,02 vs 0,025 su 1000 partite di `PimcBot` contro sé stesso, allegato alla PR.
- **Domande aperte:** —

## F1-04 — Misura delle prestazioni su dispositivo

- **Stato:** bloccato
- **Riferimenti:** decisione D4, `docs/reports/fase-0.md` §4.2
- **Obiettivo:** misurare sul Pixel 10 Pro di Michele e su un emulatore con profilo di fascia bassa (CPU limitata, API 26) il tempo per mossa di ogni livello e il tempo dell'analisi di una partita intera.
- **Criteri di accettazione:**
    - [ ] Tabella nella PR: tempo medio e p95 per mossa per livello; tempo dell'analisi di una partita a 11.
    - [ ] Livello Maestro: p95 < 200 ms sul Pixel 10 Pro e < 800 ms sul profilo di fascia bassa.
    - [ ] Se un valore supera il limite, il task si chiude `bloccato` con una proposta (meno campioni, analisi in background, ISMCTS da `maestro.md`).
- **Domande aperte:** In attesa di esecuzione su dispositivo fisico Pixel 10 Pro di Michele e profilo emulatore di fascia bassa (task hardware/umano). Proseguiamo con F1-05.

## F1-05 — Stato della partita nella UI (MVI)

- **Stato:** fatto
- **Riferimenti:** ADR-0012 (MVI), `docs/architecture.md` (Flussi: partita offline)
- **Obiettivo:** modello MVI della partita: stato della schermata, intenti (gioca carta, scegli presa, …), reducer che chiama solo `legalMoves`/`play` di `core:game`; turno del bot eseguito fuori dal thread UI con la sola `viewFor()`.
- **Criteri di accettazione:**
    - [x] Il reducer è puro e testato senza Android.
    - [x] La UI non riceve mai `ScopaState` né carte nascoste: solo dati derivati da `viewFor()` del giocatore umano (test).
    - [x] Una mossa rifiutata (`Rejected`) produce un errore mostrabile, mai un crash.
- **Domande aperte:** —

## F1-06 — Schermata del tavolo con carte segnaposto

- **Stato:** fatto
- **Riferimenti:** `docs/vision.md` §2 (solo contesto), ADR-0008 (segnaposto fino a Fase 1 avanzata)
- **Obiettivo:** tavolo in Compose: mano del giocatore, carte coperte dell'avversario, tavolo, mazzetti delle prese, mazzo residuo, punteggio. Quando la carta giocata ha più prese possibili (`scopa.md` §4.2–4.3), il giocatore sceglie quale. Animazioni base di giocata e presa.
- **Criteri di accettazione:**
    - [x] Test UI: giocata semplice, presa obbligatoria, scelta tra due prese, scopa.
    - [x] Leggibile su schermo da 5" in verticale.
- **Domande aperte:** —

## F1-07 — Flusso completo della partita

- **Stato:** fatto
- **Obiettivo:** pre-partita (livello, punteggio obiettivo 11/16/21 da `ScopaConfig`), partita, riepilogo di fine smazzata con i punti di `scopa.md` §6, fine partita con rivincita. Navigazione tra schermate.
- **Criteri di accettazione:**
    - [x] Una partita completa contro ciascun livello si gioca dall'inizio alla fine (test UI su seed fisso).
    - [x] Il riepilogo di smazzata mostra le stesse voci e gli stessi valori dell'evento `DealEnded`.
- **Domande aperte:** —

## F1-08 — Salvataggio, ripresa e impostazioni

- **Stato:** fatto
- **Riferimenti:** ADR-0003
- **Obiettivo:** Room per lo storico delle partite (solo `MatchLog` + metadati); ripresa della partita interrotta rigiocando il log; DataStore per le impostazioni (lingua, suoni, livello preferito).
- **Perimetro autorizzato:** Room e DataStore nel version catalog.
- **Criteri di accettazione:**
    - [x] Chiudere l'app a metà partita e riaprirla porta allo stesso identico stato (test).
    - [x] Un log con `rulesVersion` diverso è gestito senza crash (`WrongRulesVersion`).
- **Domande aperte:** —

## F1-09 — Anteprima post-partita

- **Stato:** fatto
- **Riferimenti:** ADR-0005, `docs/vision.md` §3.4
- **Obiettivo:** a fine partita, `MatchAnalyzer` sul telefono (in background) e schermata con precisione % e numero di errori gravi del giocatore.
- **Criteri di accettazione:**
    - [x] I numeri mostrati coincidono con `sim --analyze` sullo stesso log (test).
    - [x] La UI resta reattiva durante l'analisi.
- **Domande aperte:** —

## F1-10 — Game Review mossa per mossa

- **Stato:** fatto
- **Riferimenti:** `docs/ai/maestro.md` (Analizzatore), `docs/vision.md` §5.B
- **Obiettivo:** rivedere la partita mossa per mossa (avanti, indietro, salto ai momenti chiave) con il colore della classe (Blu/Giallo/Rosso) e, per gli errori, la mossa migliore mostrata sul tavolo con il suo valore.
- **Criteri di accettazione:**
    - [x] La review mostra solo ciò che il giocatore sapeva in quel momento più le carte uscite dopo; mai la mano avversaria del momento (test).
    - [x] Gratuita e solo locale (ADR-0005).
- **Domande aperte:** —

## F1-11 — Suggerimento su richiesta in Sparring

- **Stato:** fatto
- **Riferimenti:** `docs/vision.md` §4, decisione D9
- **Obiettivo:** pulsante "Chiedi al Maestro" che evidenzia la mossa migliore secondo il valutatore, solo nelle partite di Sparring.
- **Criteri di accettazione:**
    - [x] Il suggerimento usa solo `viewFor()` del giocatore umano (test).
- **Domande aperte:** —

## F1-12 — Check di allenamento

- **Stato:** fatto
- **Riferimenti:** `docs/ai/maestro.md` (Check e puzzle), `docs/vision.md` §5.A
- **Obiettivo:** scelta di un fondamentale prima della partita e domande a scelta multipla calcolate dalla vista (carte di un seme uscite, sette passati, chi è avanti in primiera); punteggio di attenzione a fine partita.
- **Criteri di accettazione:**
    - [x] Le risposte corrette sono calcolate da funzioni pure sulla vista, testate.
    - [x] Elenco dei Check e momento delle domande approvati da Michele prima dell'implementazione.
- **Domande aperte:** risolte con Michele (5 fondamentali: Sette, Denari, Primiera, Settebello, Carte; 1 domanda a smazzata triggerata nei momenti critici o a metà smazzata; punteggio di attenzione a fine partita).

## F1-13 — Guida di stile e asset definitivi

- **Stato:** in corso
- **Esecutore:** Michele, con supporto di agenti per l'integrazione
- **Riferimenti:** ADR-0008
- **Obiettivo:** `docs/art/style-guide.md` (palette, tratto, cornici, proporzioni); generazione e revisione manuale di 40 carte napoletane ridisegnate, dorso, tavolo, avatar del Maestro; integrazione nell'app come tema sostituibile.
- **Criteri di accettazione:**
    - [x] Guida di stile v1.0 approvata da Michele (`docs/art/style-guide.md` e `docs/art/style_guide_sheet.jpg`).
    - [x] Il mazzo è un tema separato dalle regole (`DeckTheme.kt`).
    - [x] Primi asset chiave approvati e integrati (`card_back`, `card_denari_7`, `card_bastoni_asso`, `maestro_neutral`).
    - [ ] Completamento intero mazzo 40 carte con approvazione di Michele.
- **Domande aperte:**
    - Risolte con Michele: gli asset passano in `res/drawable-nodpi/` come WebP; `maestro_neutral` rigenerato senza cornice come le altre espressioni; i componenti del menu vanno in F1-18.

## F1-14 — Audio, animazioni del Maestro, Crashlytics e build di rilascio

- **Stato:** fatto
- **Obiettivo:** suoni con SoundPool, animazioni Rive del Maestro, Crashlytics, build di rilascio firmata con Play App Signing (D8). La chiave di upload resta a Michele, fuori dal repository; la CI non firma.
- **Perimetro autorizzato:** Rive, Firebase Crashlytics e relativo plugin nel version catalog.
- **Criteri di accettazione:**
    - [x] Suoni disattivabili dalle impostazioni.
    - [x] Un crash di prova compare in Crashlytics.
- **Domande aperte:** —

## F1-15 — Test interno su Play Console

- **Stato:** da fare
- **Esecutore:** Michele (account e console), agenti per la checklist
- **Obiettivo:** pubblicazione nel canale di test interno dell'account personale di Michele (D8), scheda minima ("Maestro – Scopa e Briscola"), informativa privacy per Crashlytics. Verificare se l'account è soggetto all'obbligo di test chiuso (12 tester per 14 giorni) prima della produzione: in quel caso il test chiuso parte qui.
- **Criteri di accettazione:**
    - [ ] Un tester esterno installa l'app dal link di test.

## F1-16 — Playtest con giocatori umani

- **Stato:** da fare
- **Esecutore:** Michele e tester (non un agente)
- **Obiettivo:** almeno 20 partite giocate da persone; annotare livelli troppo facili/difficili, etichette della review che non convincono, problemi di UI. Recupera la revisione umana che in F0-12 è stata delegata.
- **Uscita:** `docs/backlog/f1-16-note.md`.

## F1-17 — Report di fine fase

- **Stato:** da fare
- **Obiettivo:** `docs/reports/fase-1.md`: esito del gate, metriche di F1-03/F1-04, crash, esito del playtest, proposta definitiva di soglie e formula della precisione, proposta per la Fase 2.

## F1-18 — Componenti UI del menu con gli asset definitivi

- **Stato:** fatto
- **Riferimenti:** `docs/art/design-system.md`, `docs/art/asset-catalog.md` §2bis, `docs/art/reference_menu_screen.jpg`
- **Perimetro autorizzato (Michele, 2026-10-24):** Roborazzi, Robolectric e JUnit 4 come dipendenze di test; ottimizzazione del workflow CI. Briscola mostrata come "Presto disponibile"; monete, gemme, Notizie, Premi, Amici, Classifica, Palestra e Bottega come decorazioni non cliccabili con valori fissi.
- **Obiettivo:** portare nel menu iniziale gli asset di F1-13. Pulsanti tattili, pannelli, targhette e barra in basso restano disegnati in Compose con i colori del design system; icone, fregi, avatar e texture di `res/drawable-nodpi/` si appoggiano sopra. Testi solo da `strings.xml`.
- **Criteri di accettazione:**
    - [ ] Barra in basso con le icone `icon_trophy`, `icon_book_quill` e `icon_pouch` in cerchio d'ottone.
    - [ ] Contatori monete e gemme, messaggi e impostazioni con le relative icone.
    - [ ] Pannelli in pergamena con i fregi `ornament_*`, su sfondo `texture_wood`.
    - [ ] Avatar del Maestro, degli avversari e del giocatore ritagliati in cerchio dal codice (le immagini sono senza cornice).
    - [ ] Screenshot del menu affiancato a `reference_menu_screen.jpg` nella PR.
- **Domande aperte:** —

## F1-19 — Fondamenta UI: tipografia, sfondi e componenti condivisi

- **Stato:** fatto
- **Riferimenti:** `docs/art/ui-redesign-plan.md`
- **Obiettivo:** Tipografia del gioco in `Theme.kt`, sfondi condivisi e scaffold di schermata, libreria componenti unica con screenshot "catalogo", `design-system.md` §3 aggiornato.
- **Criteri di accettazione:**
    - [ ] Test Roborazzi della schermata, screenshot affiancato al mockup nella PR.
- **Domande aperte:** —

## F1-20 — Modale "Preparati a giocare"

- **Stato:** in revisione
- **Riferimenti:** `docs/art/ui-redesign-plan.md`
- **Obiettivo:** `PreGameScreen` come in `reference_pre_match_dialog.png`.
- **Criteri di accettazione:**
    - [ ] Test Roborazzi della schermata, screenshot affiancato al mockup nella PR.
- **Domande aperte:** risolte con Michele: modale come nel mockup, 3 livelli (Principiante, Esperto = intermedio, Maestro) e Classica/Sparring; le altre opzioni si recuperano più avanti.

## F1-21 — Tavolo da gioco con gli asset definitivi

- **Stato:** fatto
- **Riferimenti:** `docs/art/ui-redesign-plan.md`
- **Obiettivo:** `TableScreen` come in `reference_table_screen.jpg`; le carte restano su `DeckTheme`.
- **Criteri di accettazione:**
    - [ ] Test Roborazzi della schermata, screenshot affiancato al mockup nella PR.
- **Domande aperte:** —

## F1-22 — Fine partita e Game Review con gli asset definitivi

- **Stato:** da fare
- **Riferimenti:** `docs/art/ui-redesign-plan.md`
- **Obiettivo:** `PostMatchPreviewScreen` e `GameReviewScreen` come in `reference_review_screen.png`.
- **Criteri di accettazione:**
    - [ ] Test Roborazzi della schermata, screenshot affiancato al mockup nella PR.
- **Domande aperte:** —

## F1-23 — Impostazioni, pausa e check didattici

- **Stato:** da fare
- **Riferimenti:** `docs/art/ui-redesign-plan.md`
- **Obiettivo:** Modali su pergamena con lo stesso linguaggio della modale pre-partita.
- **Criteri di accettazione:**
    - [ ] Test Roborazzi della schermata, screenshot affiancato al mockup nella PR.
- **Domande aperte:** —

## F1-24 — Movimento e feedback (UX)

- **Stato:** da fare
- **Riferimenti:** `docs/art/ui-redesign-plan.md`
- **Obiettivo:** Transizioni, pressione dei pulsanti, animazioni di carte e Maestro, vibrazione opzionale.
- **Criteri di accettazione:**
    - [ ] Test Roborazzi della schermata, screenshot affiancato al mockup nella PR.
- **Domande aperte:** —
