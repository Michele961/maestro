# Backlog — Fase 2: Didattica, Retention & Economia ("L'Accademia del Maestro")

Approvato da Michele il 2026-10-26 (decisione D10 in `docs/roadmap.md` §3).

Obiettivo della fase: trasformare Maestro nel punto di riferimento educativo per la Scopa, focalizzandosi sull'USP (coaching, puzzle quotidiani, Accademia a moduli) e introducendo il modello economico (abbonamento "Circolo dei Maestri", crediti e rewarded ads) per il **lancio pubblico su Google Play Store**. L'infrastruttura online in tempo reale è demandata alla Fase 3.

**Gate di uscita:**
- Funzione "Rigioca l'errore" attiva e testata nella Game Review con feedback del Maestro;
- "Sfida del Giorno" (Tactical Puzzle) con selezione deterministica per data e condivisione social del risultato senza spoiler;
- Almeno 3 moduli interattivi dell'Accademia completabili con prova pratica finale;
- Indice di Maestria (Elo Cognitivo) e Grafico Radar delle Abilità calcolati e visualizzati nel profilo;
- Streak giornaliera ("Fiamma") e missioni didattiche a rotazione;
- Wallet locale dei Crediti Maestro in Room (append-only);
- Integrazione AdMob Rewarded Ads funzionante con consenso GDPR/UMP (zero banner invasivi);
- Integrazione Google Play Billing con acquisti in-app (pacchetti crediti, cosmetici) e abbonamento "Circolo dei Maestri" testati con account licenza;
- Lancio ufficiale su Google Play Store in produzione con scheda completa e informativa privacy.

Lo stato di ogni task è **solo** nella sua riga **Stato** (`da fare` · `in corso` · `in revisione` · `fatto` · `bloccato`, vedi `docs/workflow.md`). Gli agenti modificano solo **Stato** e **Domande aperte** del proprio task.

| ID | Titolo | Dipende da |
| --- | --- | --- |
| F2-01 | "Rigioca l'errore" nella Game Review | F1-10 |
| F2-02 | Generatore e risolutore di puzzle tattici | — |
| F2-03 | Schermata "Sfida del Giorno" & Social Share | F2-02, F1-08 |
| F2-04 | Indice di Maestria & Radar delle Abilità | F1-09, F1-12 |
| F2-05 | Streak quotidiana ("Fiamma") & Missioni didattiche | F2-03, F2-04 |
| F2-06 | L'Accademia del Maestro: primi 3 moduli interattivi | F2-01, F2-02 |
| F2-07 | Wallet locale "Crediti Maestro" (Room) | F1-08 |
| F2-08 | Integrazione AdMob Rewarded Ads con UMP | F2-07 |
| F2-09 | Google Play Billing (IAP per Crediti & Cosmetici) | F2-07 |
| F2-10 | Abbonamento "Circolo dei Maestri" | F2-08, F2-09 |
| F2-11 | Shop e personalizzazione cosmetica (tavoli e carte) | F1-13, F2-07 |
| F2-12 | Preparazione e lancio pubblico su Google Play Store | F1-15, F1-16, F2-01…F2-11 |

---

## F2-01 — "Rigioca l'errore" nella Game Review

- **Stato:** in revisione
- **Riferimenti:** `docs/vision.md` §5.B, `learning_coaching_retention_monetization_strategy.md` §2
- **Obiettivo:** Nella `GameReviewScreen`, in corrispondenza di un errore (`BLUNDER` rosso o `INACCURACY` giallo), offrire il pulsante "Riprova la mano". La schermata ricrea la disposizione delle carte sul tavolo per quella mano specifica (carte in mano al giocatore e carte a terra, con carte avversarie coperte). L'utente tocca una carta: se sceglie la mossa migliore (o di pari valore), il Maestro si congratula con una posa positiva ed effetti visivi; se sceglie una mossa ancora imperfetta, lo stimola a riprovare mostrando un suggerimento.
- **Perimetro autorizzato:** `app:android` (GameReviewViewModel, GameReviewScreen, TableScreen). Nessuna nuova dipendenza esterna.
- **Criteri di accettazione:**
    - [x] Il test UI/Roborazzi verifica l'attivazione della modalità "Riprova la mano".
    - [x] Non vengono rivelate le carte coperte dell'avversario prima del gioco della carta.
    - [x] Risolvere correttamente l'errore aggiorna lo stato visivo della mossa nella timeline.
- **Domande aperte:** —

---

## F2-02 — Generatore e risolutore di puzzle tattici

- **Stato:** da fare
- **Riferimenti:** `docs/ai/maestro.md` (Check e puzzle), `docs/rules/scopa.md` §6
- **Obiettivo:** In `core:ai`, implementare un generatore e validatore di puzzle tattici basato sul risolutore esatto (`PimcBot` / endgame solver). Un puzzle tattico è definito da uno stato di smazzata (es. ultime 2-3 mani a mazzo vuoto o banco critico) in cui esiste **un'unica sequenza vincente** (o una sola mossa che massimizza i punti garantendo la vittoria o una scopa decisiva).
- **Perimetro autorizzato:** `core:ai` (nuovo package `it.maestro.ai.puzzle`). Test Kotlin puri senza Android.
- **Criteri di accettazione:**
    - [ ] Modello dati puro `TacticalPuzzle(id, title, initialView, goal, solutionMoves)`.
    - [ ] Generazione deterministica a partire da un seed.
    - [ ] Suite di test unitari con almeno 10 puzzle verificati e risolvibili.
- **Domande aperte:** —

---

## F2-03 — Schermata "Sfida del Giorno" & Social Share

- **Stato:** da fare
- **Riferimenti:** `docs/vision.md` §4, `learning_coaching_retention_monetization_strategy.md` §2
- **Obiettivo:** Schermata dedicata accessibile dalla Home per il puzzle tattico quotidiano. Il puzzle del giorno è selezionato o generato in modo deterministico a partire dalla data locale (uguale per tutti gli utenti nel medesimo giorno). Al completamento: feedback celebrativo, accredito ricompensa e pulsante "Condividi" che genera una stringa negli appunti formattata per WhatsApp/social senza spoiler (stile Wordle: *"Maestro Scopa #14 — Risolto al 1° tentativo! 🧠✨"*).
- **Perimetro autorizzato:** `app:android` (navigazione, PuzzleViewModel, PuzzleScreen, formattazione stringa di share).
- **Criteri di accettazione:**
    - [ ] DataStore registra lo stato del puzzle del giorno (risolto/non risolto, data ultimo completamento).
    - [ ] La condivisione usa l'Intent standard di Android (`ACTION_SEND`).
    - [ ] Test UI con screenshot Roborazzi della schermata del puzzle.
- **Domande aperte:** —

---

## F2-04 — Indice di Maestria & Radar delle Abilità

- **Stato:** da fare
- **Riferimenti:** `docs/vision.md` §5.A, `learning_coaching_retention_monetization_strategy.md` §3
- **Obiettivo:** Calcolo e persistenza dell'**Indice di Maestria** (Elo Cognitivo): media mobile ponderata della precisione delle partite giocate, slegata dal semplice vincere o perdere per sfortuna delle carte. Calcolo dei 5 parametri del Radar delle Abilità (Memoria Sette, Controllo Denari, Primiera, Gestione Banco, Endgame) basati sulle scelte della partita e sui Check di allenamento. Grafico radar renderizzato con Canvas Compose nel profilo/home.
- **Perimetro autorizzato:** `app:android`, `core:ai` (funzioni di aggregazione metriche). Nessuna libreria grafica esterna (Canvas nativo Compose).
- **Criteri di accettazione:**
    - [ ] Il profilo utente mostra l'Indice di Maestria numerico e il grafico radar a 5 assi.
    - [ ] La formula di aggiornamento della precisione è deterministica e coperta da test unitari.
- **Domande aperte:** —

---

## F2-05 — Streak quotidiana ("Fiamma") & Missioni didattiche

- **Stato:** da fare
- **Riferimenti:** `learning_coaching_retention_monetization_strategy.md` §3
- **Obiettivo:** Sistema di retention giornaliero:
  1. Conteggio giorni consecutivi di gioco ("Streak/Fiamma") con 1 "Gettone Riposo" (Freeze) salvavita per non perdere la sequenza se si salta un giorno.
  2. Tre missioni didattiche giornaliere a rotazione (es. *"Risolvi il puzzle del giorno"*, *"Chiudi una partita con precisione > 80%"*, *"Rispondi correttamente a 2 check sui Sette"*).
- **Perimetro autorizzato:** `app:android` (DataStore per tracking date/streak, componenti grafici fiamma e badge missioni).
- **Criteri di accettazione:**
    - [ ] Test su passaggio di data: incremento streak se giocato, consumo gettone riposo se non giocato, azzeramento se saltati due giorni consecutivi senza gettoni.
- **Domande aperte:** —

---

## F2-06 — L'Accademia del Maestro: primi 3 moduli interattivi

- **Stato:** da fare
- **Riferimenti:** `docs/vision.md` §5.C, `learning_coaching_retention_monetization_strategy.md` §2
- **Obiettivo:** Schermata "Accademia del Maestro" con percorso a tappe. Ogni modulo è una breve lezione interattiva (2 minuti): testo sintetico del Maestro + tavolo interattivo con 2-3 quesiti pratici per sbloccare la spilla del modulo:
  - Modulo 1: *L'Abbecedario del Circolo* (Regole formali, presa obbligatoria con carta singola vs somma, valore delle combinazioni).
  - Modulo 2: *Il Segreto della Primiera* (Calcolo rapido dei valori 7=21, 6=18, Asso=16, 5=15; priorità semi).
  - Modulo 3: *La Regola del Dispari* (Come contare le carte a terra e mantenere l'iniziativa).
- **Perimetro autorizzato:** `app:android` (AcademyScreen, AcademyViewModel, dati lezioni).
- **Criteri di accettazione:**
    - [ ] Navigazione progressiva: il modulo successivo si sblocca solo superando il precedente.
    - [ ] Salvare il completamento delle lezioni in Room o DataStore.
- **Domande aperte:** —

---

## F2-07 — Wallet locale "Crediti Maestro" (Room)

- **Stato:** da fare
- **Riferimenti:** `docs/vision.md` §8.1, ADR-0006
- **Obiettivo:** Registro transazioni del wallet locale in Room (append-only: ogni movimento ha id, timestamp, causale e importo con segno). Calcolo saldo corrente (`balance = sum(amount)`). Assegnazione crediti gratuiti giornalieri, vincite puzzle/missioni e acquisti. Saldo sempre visibile nella barra superiore dell'app.
- **Perimetro autorizzato:** `app:android` (estensione entità/DAO Room esistenti).
- **Criteri di accettazione:**
    - [ ] Test Room: accrediti, addebiti, verifica saldo, impossibilità di andare in negativo su addebito non consentito.
- **Domande aperte:** —

---

## F2-08 — Integrazione AdMob Rewarded Ads con UMP

- **Stato:** da fare
- **Riferimenti:** `docs/vision.md` §8.2, `docs/architecture.md` (Stack per fase)
- **Obiettivo:** Integrazione SDK Google Mobile Ads per formati **esclusivamente Rewarded** (video con premio su esplicita richiesta dell'utente; nessun banner invasivo o interstitial a sorpresa). Integrazione del modulo Google User Messaging Platform (UMP) per la gestione conforme del consenso GDPR/privacy in Europa. Ricompensa: sblocco di una Game Review approfondita o crediti bonus.
- **Perimetro autorizzato:** Aggiunta di `play-services-ads` e `user-messaging-platform` in `gradle/libs.versions.toml` e in `app/build.gradle.kts`. Wrapper `AdManager` con fallback offline trasparente.
- **Criteri di accettazione:**
    - [ ] Test con ID di test ufficiali di Google AdMob.
    - [ ] Mostrare il prompt UMP al primo avvio in area SEE/UK.
    - [ ] L'accredito della ricompensa avviene solo ad annuncio completato (`onUserEarnedReward`).
- **Domande aperte:** —

---

## F2-09 — Google Play Billing (IAP per Crediti & Cosmetici)

- **Stato:** da fare
- **Riferimenti:** `docs/vision.md` §8.1 e §8.3, `docs/architecture.md` (Stack per fase)
- **Obiettivo:** Integrazione ufficiale di Google Play Billing Library. Configurazione e gestione acquisti in-app una tantum (consumabili per pacchetti Crediti Maestro, non consumabili per temi grafici e dorsi premium). Gestione del flusso di acquisto, ripristino acquisti e persistenza sicura sul wallet locale.
- **Perimetro autorizzato:** Aggiunta di `billing-ktx` in `gradle/libs.versions.toml` e `app/build.gradle.kts`. Wrapper `BillingManager` in `app:android`.
- **Criteri di accettazione:**
    - [ ] Test di acquisto con account tester di licenza su Play Console.
    - [ ] Ripristino acquisti all'avvio o da impostazioni.
- **Domande aperte:** —

---

## F2-10 — Abbonamento "Circolo dei Maestri"

- **Stato:** da fare
- **Riferimenti:** `docs/vision.md` §8.4, `learning_coaching_retention_monetization_strategy.md` §4
- **Obiettivo:** Sottoscrizione in abbonamento mensile/annuale tramite Play Billing:
  - Game Review approfondite illimitate senza spendere crediti né guardare pubblicità;
  - Accesso illimitato all'Accademia del Maestro e a tutto l'archivio dei puzzle storici;
  - Rimozione totale di qualsiasi richiesta di annunci pubblicitari nell'app;
  - Distintivo estetico dorato "Membro del Circolo" visibile nel profilo e al tavolo.
- **Perimetro autorizzato:** `app:android` (estensione di `BillingManager`, gestione stato abbonamento attivo/scaduto).
- **Criteri di accettazione:**
    - [ ] Se l'abbonamento è attivo, la Game Review e l'Accademia si aprono direttamente senza dialoghi di sblocco.
- **Domande aperte:** —

---

## F2-11 — Shop e personalizzazione cosmetica (tavoli e carte)

- **Stato:** da fare
- **Riferimenti:** `docs/vision.md` §8.3
- **Obiettivo:** Schermata "La Bottega del Maestro" per visionare, acquistare ed equipaggiare elementi cosmetici:
  - Dorsi di carte alternativi (Classico, Dorato, Vintage);
  - Texture del tavolo (Panno Verde Tradizionale, Salotto Nobile in Velluto Rosso, Noce Massello);
  - Avatar e reazioni sonore personalizzate del Maestro.
- **Perimetro autorizzato:** `app:android` (ShopScreen, persistenza selezione tema in DataStore).
- **Criteri di accettazione:**
    - [ ] Il tema del tavolo o del dorso selezionato nello shop si riflette immediatamente nella partita di gioco.
- **Domande aperte:** —

---

## F2-12 — Preparazione e lancio pubblico su Google Play Store

- **Stato:** da fare
- **Esecutore:** Michele con supporto agenti per la documentazione
- **Riferimenti:** `docs/roadmap.md` §2, decisione D8
- **Obiettivo:** Rilascio ufficiale della versione 1.0 di Maestro sul canale di produzione di Google Play Store:
  - Scheda store rifinita: titolo, descrizione breve focalizzata su *"Il tuo Coach AI di Scopa"*, descrizione completa, screenshot grafici con mockup di gioco e review;
  - Icona applicazione finale ad alta risoluzione (512x512) e grafica promozionale (1024x500);
  - Modulo di classificazione dei contenuti IARC e questionario privacy/dati;
  - Build di rilascio App Bundle (`.aab`) firmata tramite Play App Signing (D8).
- **Criteri di accettazione:**
    - [ ] L'applicazione è approvata da Google e disponibile al download pubblico su Play Store.
- **Domande aperte:** —
