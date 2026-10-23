# Backlog — Fase 1: MVP offline Android

> **BOZZA — non eseguibile.** Proposta del 2026-10-23, in attesa di approvazione di Michele (vedi `docs/roadmap.md` §3, decisioni D1–D9). Finché questa nota c'è, nessun agente avvia task di questo file.

Obiettivo della fase: un'app Android installabile in cui si gioca a Scopa contro il Maestro, a più livelli, con anteprima post-partita, Game Review e Check di allenamento, tutto offline. Nessun server, nessun pagamento.

**Gate di uscita (proposta):** partita completa giocata da un tester esterno contro ogni livello; precisione di ogni livello nella fascia decisa in D3; p95 del tempo per mossa del livello più forte sotto 500 ms sul dispositivo di riferimento (D4); zero crash su almeno 50 partite di test; etichette della review giudicate sensate nel playtest umano (F1-16).

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

Si possono lavorare in parallelo: F1-03 con F1-01/F1-02; F1-09…F1-12 tra loro dopo F1-07; F1-13 (soprattutto lavoro di Michele) dal momento in cui esiste il tavolo.

---

## F1-00 — Guardrail mancanti per gli agenti

- **Stato:** da fare
- **Modalità:** sessione interattiva con Michele (tutti i file sono protetti).
- **Obiettivo:** creare i file che `CLAUDE.md` cita ma che non esistono: template della PR (con sezioni "Fuori dal perimetro" e "Domande aperte"), `.claude/commands/start-task.md`, `.claude/commands/review-task.md`, `.claude/settings.json` con conferma sui file protetti. Aggiornare `README.md` (stato: Fase 1).
- **Perimetro autorizzato:** `.github/`, `.claude/`, `README.md`.
- **Criteri di accettazione:**
    - [ ] I quattro file esistono e corrispondono a quanto descritto in `CLAUDE.md` §6–7.
    - [ ] Michele ha approvato il testo dei comandi.
- **Domande aperte:** —

## F1-01 — Target Android nei moduli `core:*`

- **Stato:** da fare
- **Riferimenti:** `docs/architecture.md` (Target), ADR-0001, ADR-0010
- **Obiettivo:** aggiungere `androidTarget()` a `core:game`, `core:ai`, `core:protocol` senza cambiare codice comune. I test comuni girano anche sul target Android (unit test locali).
- **Perimetro autorizzato:** Android Gradle Plugin (versione da D5) nel version catalog e nei `build.gradle.kts` dei moduli `core:*`; Android SDK nel workflow di CI.
- **Criteri di accettazione:**
    - [ ] `./gradlew check` verde in CI con il target Android.
    - [ ] `core:game` continua a dipendere solo dalla stdlib (`checkModuleBoundaries`).
    - [ ] Nessun file in `commonMain` modificato.
- **Domande aperte:** —

## F1-02 — Modulo `app:android` e CI con APK

- **Stato:** da fare
- **Riferimenti:** ADR-0012, ADR-0007, `docs/architecture.md` (Moduli)
- **Obiettivo:** creare `app:android` (una sola Activity, Compose, Koin, `minSdk 26`), tema base, risorse stringhe `it` ed `en`, schermata iniziale vuota con il nome dell'app. La CI produce l'APK di debug.
- **Perimetro autorizzato:** nuovo modulo `app:android` in `settings.gradle.kts` e nella tabella dei confini del `build.gradle.kts` root; Compose, Koin e le librerie di D5 nel version catalog.
- **Criteri di accettazione:**
    - [ ] `./gradlew check` e `./gradlew :app:android:assembleDebug` verdi in CI.
    - [ ] `app:android` dipende solo da `core:*` tra i moduli del progetto.
    - [ ] Nessuna stringa per l'utente scritta nel codice (ADR-0007): un test o un controllo lint lo verifica.
- **Domande aperte:** package Android e nome dell'app (D8).

## F1-03 — Livelli di difficoltà del Maestro

- **Stato:** da fare
- **Riferimenti:** `docs/ai/maestro.md` (Livelli di difficoltà), `docs/reports/fase-0.md` §5–6, decisioni D2 e D3
- **Obiettivo:** in `core:ai`, livelli di difficoltà come configurazione di `PimcBot` (campioni, nodi, rumore nella scelta, memoria imperfetta), nella forma decisa in D3. Il simulatore accetta `--p1 level:<nome>`. Se D2 lo approva, confronto `PRIMIERA_DIFF` 0,02 vs 0,025.
- **Perimetro autorizzato:** aggiungere l'opzione dei livelli al simulatore.
- **Criteri di accettazione:**
    - [ ] Ogni livello è deterministico a parità di seed (test).
    - [ ] La "memoria imperfetta" usa solo la vista: il bot non riceve mai lo stato completo (test).
    - [ ] Report nella PR: precisione media (con `MatchAnalyzer`) e win-rate di ogni livello contro il successivo su 1000 partite; ogni livello nella sua fascia di D3.
- **Domande aperte:** —

## F1-04 — Misura delle prestazioni su dispositivo

- **Stato:** da fare
- **Riferimenti:** decisione D4, `docs/reports/fase-0.md` §4.2
- **Obiettivo:** misurare su dispositivo reale (o emulatore con profilo del dispositivo di riferimento) il tempo per mossa di ogni livello e il tempo dell'analisi di una partita intera.
- **Criteri di accettazione:**
    - [ ] Tabella nella PR: tempo medio e p95 per mossa per livello; tempo dell'analisi di una partita a 11.
    - [ ] Se un valore supera il limite di D4, il task si chiude `bloccato` con una proposta (meno campioni, analisi in background, ISMCTS da `maestro.md`).
- **Domande aperte:** —

## F1-05 — Stato della partita nella UI (MVI)

- **Stato:** da fare
- **Riferimenti:** ADR-0012 (MVI), `docs/architecture.md` (Flussi: partita offline)
- **Obiettivo:** modello MVI della partita: stato della schermata, intenti (gioca carta, scegli presa, …), reducer che chiama solo `legalMoves`/`play` di `core:game`; turno del bot eseguito fuori dal thread UI con la sola `viewFor()`.
- **Criteri di accettazione:**
    - [ ] Il reducer è puro e testato senza Android.
    - [ ] La UI non riceve mai `ScopaState` né carte nascoste: solo dati derivati da `viewFor()` del giocatore umano (test).
    - [ ] Una mossa rifiutata (`Rejected`) produce un errore mostrabile, mai un crash.
- **Domande aperte:** —

## F1-06 — Schermata del tavolo con carte segnaposto

- **Stato:** da fare
- **Riferimenti:** `docs/vision.md` §2 (solo contesto), ADR-0008 (segnaposto fino a Fase 1 avanzata)
- **Obiettivo:** tavolo in Compose: mano del giocatore, carte coperte dell'avversario, tavolo, mazzetti delle prese, mazzo residuo, punteggio. Quando la carta giocata ha più prese possibili (`scopa.md` §4.2–4.3), il giocatore sceglie quale. Animazioni base di giocata e presa.
- **Criteri di accettazione:**
    - [ ] Test UI: giocata semplice, presa obbligatoria, scelta tra due prese, scopa.
    - [ ] Leggibile su schermo da 5" in verticale.
- **Domande aperte:** —

## F1-07 — Flusso completo della partita

- **Stato:** da fare
- **Obiettivo:** pre-partita (livello, punteggio obiettivo 11/16/21 da `ScopaConfig`), partita, riepilogo di fine smazzata con i punti di `scopa.md` §6, fine partita con rivincita. Navigazione tra schermate.
- **Criteri di accettazione:**
    - [ ] Una partita completa contro ciascun livello si gioca dall'inizio alla fine (test UI su seed fisso).
    - [ ] Il riepilogo di smazzata mostra le stesse voci e gli stessi valori dell'evento `DealEnded`.
- **Domande aperte:** —

## F1-08 — Salvataggio, ripresa e impostazioni

- **Stato:** da fare
- **Riferimenti:** ADR-0003
- **Obiettivo:** Room per lo storico delle partite (solo `MatchLog` + metadati); ripresa della partita interrotta rigiocando il log; DataStore per le impostazioni (lingua, suoni, livello preferito).
- **Perimetro autorizzato:** Room e DataStore nel version catalog.
- **Criteri di accettazione:**
    - [ ] Chiudere l'app a metà partita e riaprirla porta allo stesso identico stato (test).
    - [ ] Un log con `rulesVersion` diverso è gestito senza crash (`WrongRulesVersion`).
- **Domande aperte:** —

## F1-09 — Anteprima post-partita

- **Stato:** da fare
- **Riferimenti:** ADR-0005, `docs/vision.md` §3.4
- **Obiettivo:** a fine partita, `MatchAnalyzer` sul telefono (in background) e schermata con precisione % e numero di errori gravi del giocatore.
- **Criteri di accettazione:**
    - [ ] I numeri mostrati coincidono con `sim --analyze` sullo stesso log (test).
    - [ ] La UI resta reattiva durante l'analisi.
- **Domande aperte:** —

## F1-10 — Game Review mossa per mossa

- **Stato:** da fare
- **Riferimenti:** `docs/ai/maestro.md` (Analizzatore), `docs/vision.md` §5.B
- **Obiettivo:** rivedere la partita mossa per mossa (avanti, indietro, salto ai momenti chiave) con il colore della classe (Blu/Giallo/Rosso) e, per gli errori, la mossa migliore mostrata sul tavolo con il suo valore.
- **Criteri di accettazione:**
    - [ ] La review mostra solo ciò che il giocatore sapeva in quel momento più le carte uscite dopo; mai la mano avversaria del momento (test).
    - [ ] Gratuita e solo locale (ADR-0005).
- **Domande aperte:** —

## F1-11 — Suggerimento su richiesta in Sparring

- **Stato:** da fare
- **Riferimenti:** `docs/vision.md` §4, decisione D9
- **Obiettivo:** pulsante "Chiedi al Maestro" che evidenzia la mossa migliore secondo il valutatore, solo nelle partite di Sparring.
- **Criteri di accettazione:**
    - [ ] Il suggerimento usa solo `viewFor()` del giocatore umano (test).
- **Domande aperte:** —

## F1-12 — Check di allenamento

- **Stato:** da fare
- **Riferimenti:** `docs/ai/maestro.md` (Check e puzzle), `docs/vision.md` §5.A
- **Obiettivo:** scelta di un fondamentale prima della partita e domande a scelta multipla calcolate dalla vista (carte di un seme uscite, sette passati, chi è avanti in primiera); punteggio di attenzione a fine partita.
- **Criteri di accettazione:**
    - [ ] Le risposte corrette sono calcolate da funzioni pure sulla vista, testate.
    - [ ] Elenco dei Check e momento delle domande approvati da Michele prima dell'implementazione.
- **Domande aperte:** quali Check per primi, quante domande per partita, quando vengono poste.

## F1-13 — Guida di stile e asset definitivi

- **Stato:** da fare
- **Esecutore:** Michele, con supporto di agenti per l'integrazione
- **Riferimenti:** ADR-0008
- **Obiettivo:** `docs/art/style-guide.md` (palette, tratto, cornici, proporzioni); generazione e revisione manuale di 40 carte napoletane ridisegnate, dorso, tavolo, avatar del Maestro; integrazione nell'app come tema sostituibile.
- **Criteri di accettazione:**
    - [ ] Ogni asset ha superato la revisione manuale di Michele.
    - [ ] Il mazzo è un tema separato dalle regole.
- **Domande aperte:** —

## F1-14 — Audio, animazioni del Maestro, Crashlytics e build di rilascio

- **Stato:** da fare
- **Obiettivo:** suoni con SoundPool, animazioni Rive del Maestro, Crashlytics, build di rilascio firmata.
- **Perimetro autorizzato:** Rive, Firebase Crashlytics e relativo plugin nel version catalog.
- **Criteri di accettazione:**
    - [ ] Suoni disattivabili dalle impostazioni.
    - [ ] Un crash di prova compare in Crashlytics.
- **Domande aperte:** firma e account (D8).

## F1-15 — Test interno su Play Console

- **Stato:** da fare
- **Esecutore:** Michele (account e console), agenti per la checklist
- **Obiettivo:** pubblicazione nel canale di test interno, scheda minima, informativa privacy per Crashlytics.
- **Criteri di accettazione:**
    - [ ] Un tester esterno installa l'app dal link di test.

## F1-16 — Playtest con giocatori umani

- **Stato:** da fare
- **Esecutore:** Michele e tester (non un agente)
- **Obiettivo:** almeno 20 partite giocate da persone; annotare livelli troppo facili/difficili, etichette della review che non convincono, problemi di UI. Recupera la revisione umana che in F0-12 è stata delegata.
- **Uscita:** `docs/backlog/f1-16-note.md`.

## F1-17 — Report di fine fase

- **Stato:** da fare
- **Obiettivo:** `docs/reports/fase-1.md`: esito del gate, metriche di F1-03/F1-04, crash, esito del playtest, proposta definitiva di soglie e formula della precisione, proposta per la Fase 2 (o per la Briscola, secondo D6).
