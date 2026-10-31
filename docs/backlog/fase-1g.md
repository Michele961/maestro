# Backlog — Fase 1G: game loop, storia e regole della casa

Aperto il 2026-10-30 su richiesta di Michele (decisioni D20…D26 in `docs/roadmap.md`). Fonte: `docs/research/game-loop-e-modalita.md`.

Obiettivo: dare al giocatore un primo giorno guidato, un percorso (la Stagione), regole della casa che si sbloccano, mazzi regionali e un motivo per tornare, tutto offline. Il multiplayer (Fase 3) parte dopo il rilascio (D21) e non ha task qui.

Lo stato di ogni task è **solo** nella sua riga **Stato** (`docs/workflow.md`). Regole come in `fase-1r.md`: nessuna regola di gioco inventata, testi in `strings.xml` it/en (ADR-0007), ricompense solo da `RewardEngine`, nessuna migrazione distruttiva, PR < 400 righe di codice non di test. Le grafiche, i ritratti, i mazzi e la musica nuovi li genera o li procura Michele (D14): ogni task li elenca in **Per Michele**.

**Macro issue su GitHub** (una per filone, aggiornata a ogni task chiuso):

| Macro | Filone | Task |
| --- | --- | --- |
| M1 | Il primo giorno | F1G-01, F1G-02, F1G-03 |
| M2 | Regole della casa | F1G-04, F1G-05, F1G-06 |
| M3 | La Stagione | F1G-07, F1G-08, F1G-09 |
| M4 | Mazzi regionali | F1G-10, F1G-11 |
| M5 | Ritorno quotidiano e misure | F1G-12, F1G-13, F1G-14, F1G-15 |
| M6 | Online (dopo il rilascio) | nessun task: promemoria delle preparazioni di D21 |

| ID | Titolo | Macro | Dipende da |
| --- | --- | --- | --- |
| F1G-01 | Primo avvio e saluto del Maestro | M1 | F1R-19, F1R-24 |
| F1G-02 | Mano d'esordio guidata | M1 | F1G-01, F1R-21, F1R-25 |
| F1G-03 | La Home che si accende | M1 | F1G-01 |
| F1G-04 | Regole della casa già nel motore: re bello, tre re, punteggio 16/21 | M2 | — |
| F1G-05 | Ricerca e bozza di specifica: napola e asso che taglia | M2 | — |
| F1G-06 | Napola e asso che taglia nel motore | M2 | F1G-05 approvato da Michele, F1G-04 |
| F1G-07 | Modello e schermata della Stagione | M3 | F1R-18, F1R-27, F1R-28, F1R-29 |
| F1G-08 | Testi e voce della Stagione | M3 | F1G-07 |
| F1G-09 | Regole della casa che si sbloccano con gli avversari | M3 | F1G-04, F1G-07 |
| F1G-10 | Schede fonte e licenza dei mazzi regionali | M4 | — |
| F1G-11 | Integrazione di un mazzo regionale | M4 | F1G-10, ADR-0013 |
| F1G-12 | Chiusura del giorno | M5 | F1R-29 |
| F1G-13 | Promemoria facoltativo | M5 | F1G-03 |
| F1G-14 | Misure minime del ritorno | M5 | — |
| F1G-15 | Partita del giorno offline | M5 | F1R-02, F1R-14 |

---

## F1G-01 — Primo avvio e saluto del Maestro

- **Stato:** da fare
- **Obiettivo:** un utente nuovo non trova la Home piena: trova il Maestro che saluta, dà il nome ed entra nella prima mano.
- **Perimetro:** schermata di primo avvio (riusa il dialogo del nome); flag «primo avvio completato» in DataStore con valore di default (nessuna migrazione distruttiva); chi aggiorna l'app con dati esistenti non vede il primo avvio.
- **Criteri di accettazione:** utente nuovo → saluto → nome → ingresso nella prima mano; utente esistente → Home com'è; il flag sopravvive a chiusura del processo e rotazione; test sul flag e screenshot nella matrice.
- **Per Michele:** testi del saluto (D16): bozza rivista e approvata per delega in `docs/design/testi-primo-avvio-e-home.md`.
- **Domande aperte:** Q5 (la prima partita di Gigi è in Sparring o in Classica?); proposta confermata da Michele (2026-10-30): mano d'esordio in Sparring, prima partita vera in Classica.

## F1G-02 — Mano d'esordio guidata

- **Stato:** da fare
- **Obiettivo:** insegnare presa singola e somma in una mano con seme fisso, saltabile («So già giocare»).
- **Perimetro:** partita guidata sul tavolo esistente con i suggerimenti a due livelli di Sparring; testi dal capitolo 1 dell'Accademia; **nessuna regola nuova**; il seme fisso passa dal generatore del progetto (ADR-0009).
- **Criteri di accettazione:** la mano è identica a ogni avvio; si può saltare; fine mano porta a Gigi; test di determinismo e di salto.
- **Per Michele:** approvazione dei testi didattici (D16).
- **Domande aperte:** nessuna.

## F1G-03 — La Home che si accende

- **Stato:** da fare
- **Obiettivo:** mostrare le funzioni a mano a mano che servono (Sfida del giorno dopo la prima vittoria, Missioni dopo la sfida, Accademia dopo la prima prova, ecc.).
- **Perimetro:** stato «funzioni presentate» in DataStore; la Home lo legge; nessuna voce finta; chi ha già giocato vede tutto.
- **Criteri di accettazione:** utente nuovo vede solo Gioca; sblocchi come da tabella nel documento di ricerca §6; utente esistente invariato; screenshot.
- **Per Michele:** niente.
- **Domande aperte:** nessuna. Ordine degli sblocchi confermato da Michele (2026-10-30), vedi `docs/design/testi-primo-avvio-e-home.md`.

## F1G-04 — Regole della casa già nel motore

- **Stato:** fatto
- **Obiettivo:** esporre all'utente `reBello`, `redealOnThreeKings` e il punteggio obiettivo 16/21, già in specifica (`docs/rules/scopa.md` §6–7).
- **Perimetro:** scelta nelle impostazioni di partita (Sparring e partita libera); i valori entrano in `GameConfig` e nel `MatchLog`; **nessuna regola nuova**. Le scelte valgono per la partita che parte (non sono salvate come preferenza); la ripresa usa la configurazione del `MatchLog`. Resta com'è ora lo sblocco degli avversari: non cambia con le regole della casa finché Michele non decide (domanda aperta).
- **Criteri di accettazione:** configurazione salvata e ripresa con la partita; replay del log invariato; bot e Check funzionano con le varianti già coperte dai test del motore; test di UI.
- **Per Michele:** niente.
- **Domande aperte:** le vittorie con regola della casa sbloccano l'avversario successivo? (proposta: sì).

## F1G-05 — Ricerca e bozza di specifica: napola e asso che taglia

- **Stato:** fatto
- **Obiettivo:** raccogliere da fonti pubbliche le regole di napola e asso pigliatutto e preparare la bozza di modifica a `docs/rules/scopa.md` per l'approvazione di Michele.
- **Perimetro:** solo documenti (`docs/research/`); **non** si modifica `docs/rules/` senza approvazione esplicita (CLAUDE.md §3). La bozza elenca le varianti trovate, le fonti, dove divergono e una proposta per ogni punto aperto: carta di partenza della sequenza, punti della napola, somma ai punti normali, valore per il punteggio obiettivo; per l'asso: cosa prende se c'è già un asso, prima mossa, scopa, tavolo vuoto.
- **Criteri di accettazione:** ogni regola ha almeno due fonti o è segnata come non verificata; Michele approva punto per punto.
- **Per Michele:** scegliere la variante per ogni punto.
- **Domande aperte:** nessuna: Michele ha approvato le proposte il 2026-10-30.

## F1G-06 — Napola e asso che taglia nel motore

- **Stato:** fatto
- **Obiettivo:** implementare le due varianti dopo l'approvazione della specifica.
- **Perimetro:** `core:game` con test; aumento di `RULES_VERSION`; verifica che bot e analizzatore non rompano; lezioni e Check solo se Michele li chiede.
- **Criteri di accettazione:** da scrivere dopo F1G-05.
- **Domande aperte:** Michele ha approvato le regole (2026-10-30); specifica aggiornata alla versione 2 in `docs/rules/scopa.md`. `RULES_VERSION` resta 1 (uguaglianza stretta nel replay e nell'analizzatore: salirla renderebbe illeggibili partite e storico salvati; le varianti nuove sono spente di default). Ci sono due letture da confermare: con un Asso sul tavolo l'Asso giocato prende **un** Asso a scelta (regola 4.2 invariata, non «tutti gli assi»).

## F1G-07 — Modello e schermata della Stagione

- **Stato:** in corso (schermata e modello consegnati; restano le voci in «Resta» e la conferma di Michele sul telefono delle dipendenze F1R-18/27/28/29)
- **Obiettivo:** la «Scalata dei Circoli» come dati: capitoli che richiamano avversario, modalità, Check, livello di sfida, lezione; schermata del capitolo corrente.
- **Perimetro:** modello nel dominio, progresso in DataStore, ricompense solo da `RewardEngine`; capitoli come dati, non codice; nessun blocco a tempo.
- **Criteri di accettazione:** progresso salvato e sopravvive a chiusura; capitolo completato assegna ricompensa una sola volta; screenshot; test del modello.
- **Decisioni di Michele (2026-10-30):** Q2 forma «Scalata dei Circoli» con tono da apprendista confermata; Q10 abbinamento Gigi–Tavolino del Bar, Nonna Rosa–Circolo Rionale, Tonino–Salotto Nobile, Don Alfonso–Gran Casinò, il Maestro–Olimpo confermato; se un personaggio non calza, Michele ne genera altri.
- **Per Michele:** niente.
- **Domande aperte:** nessuna. Dipendenze tecniche ancora aperte: F1R-18, F1R-27, F1R-28, F1R-29.
- **Esito (2026-10-31, procede per autorizzazione di Michele con le dipendenze ancora aperte):** modello in `domain/season/` (10 capitoli come dati, capitolo corrente derivato da avversari battuti, Sfida del giorno, Accademia e storico Sparring; in DataStore solo i capitoli con ricompensa riscossa); ricompensa solo da `RewardEngine.onSeasonChapterCompleted` (20 crediti a capitolo, 50 al decimo, una volta sola); schermata `feature/season`, raggiungibile da «La Scalata» in Home; stringhe `story_*` copiate dalla storia approvata.
- **Resta:** cap. 1 (mano d'esordio guidata, F1G-02) e cap. 7 (revisione: nessun segnale registrato) restano nascosti e non completabili finché la funzione non c'è; il cap. 4 si completa solo se la Sfida del giorno è risolta nel giorno in cui si apre la schermata (la Sfida non conserva lo storico). I titoli dei capitoli in inglese (`story_chNN_title`) sono bozza dell'agente: da rivedere (D16).

## F1G-08 — Testi e voce della Stagione

- **Stato:** da fare
- **Obiettivo:** battute di apertura e chiusura dei capitoli con la voce del Maestro (D18).
- **Perimetro:** `strings.xml` it/en; bozze dell'agente, approvazione di Michele (D16).
- **Criteri di accettazione:** parità it/en; Michele ha approvato.
- **Testi:** storia e battute dei 10 capitoli, rivedute e approvate per delega, in `docs/design/stagione-storia.md`; le stringhe `story_*` entrano in `strings.xml` con F1G-07 (nessuna stringa inutilizzata prima).
- **Domande aperte:** nessuna.

## F1G-09 — Regole della casa che si sbloccano con gli avversari

- **Stato:** da fare
- **Obiettivo:** dopo ogni avversario battuto, una regola della casa nuova (C2 re bello, C3 tre re o 16, poi C4 napola e C5 asso quando esisteranno), spiegata dal Maestro con una mini-lezione.
- **Perimetro:** sblocchi in `RewardEngine`; mini-lezioni nell'Accademia; le regole non ancora implementate restano nascoste.
- **Criteri di accettazione:** sblocco una sola volta; regole disponibili solo dopo lo sblocco; test.
- **Domande aperte:** abbinamento definitivo regola ↔ circolo.

## F1G-10 — Schede fonte e licenza dei mazzi regionali

- **Stato:** in corso (schede scritte in `docs/art/regionali/<mazzo>/SOURCE.md`; mancano la verifica a vista delle Siciliane e le 24 numeriche + dorso, vedi «Esito» sotto)
- **Obiettivo:** per ogni mazzo candidato, scheda con fonte, licenza, autore, link, 40 carte, semi latini, leggibilità, peso.
- **Perimetro:** solo documenti (`docs/art/`); nessun mazzo incluso senza scheda approvata. Modello: ADR-0013.
- **Criteri di accettazione:** una scheda per mazzo; licenze compatibili con la distribuzione in app.
- **Per Michele:** procurare o generare le carte di Piacentine e Siciliane (D14).
- **Decisione di Michele (2026-10-30):** N5 = Bergamasche, Piacentine, Siciliane. Licenza CC BY-SA 3.0 delle Bergamasche **accettata** (attribuzione a «Poulpy» e immagini adattate rilasciate con la stessa licenza). Le Siciliane del Commons hanno solo 16 carte: servono le altre 24.
- **Esito (2026-10-30), schede in `docs/art/regionali/`:**
  - Bergamasche: 40 carte ritagliabili in automatico dal foglio `Carte bergamasche.jpg` (pagina: `{{PD-old}}`, «Public domain»; il testo «Public Domain Mark» non c'è sulla pagina); qualità a 532x868 discreta; dubbio forte sulla datazione (Asso di denari con «MADE IN ITALY» e marchio Masenghini). Ripiego: serie Poulpy CC BY-SA 3.0, ritagliata dallo stesso foglio. Manca il dorso.
  - Piacentine: 40 carte ritagliabili dal foglio di Florixc (`PD-self`); qualità sfocata (disegno sotto i 250 px, ingrandimento 1,7-2,5x); Asso di denari con il centro bianco da ritoccare; edizione scansionata non identificata; manca il dorso.
  - Siciliane: solo 16 carte su 40 (OppidumNissenae CC BY-SA 4.0); mancano 24 numeriche e il dorso: da generare o procurare.
  - Triestine e Sarde (CC BY-SA 4.0, GattoFigaro): foto a carte a contatto, non ritagliabili in automatico, sfocate a 868 px. Romagnole (CC0, Gallica 1850): 38 carte su 40 + dorso, nitide ma di stile antico; mancano Re di denari e di bastoni.
  - `scripts/crop_sheet.py` ritaglia i fogli con sfondo chiaro (Piacentine, Bergamasche).
  - **Verifica a vista delle anteprime (2026-10-31):** le Bergamasche ritagliate sono leggibili (le figure doppie sono autentiche del mazzo); le Piacentine **non** sono pulite: nel ritaglio automatico l'Asso di denari è tagliato e alcune carte hanno i bordi sbagliati, quindi servono ritaglio a mano e ingrandimento o ridisegno (grafica di Michele, D14). Nessun mazzo è pronto per `res`.
  - **Decisione per delega (2026-10-31):** per le Bergamasche si userà il ripiego Poulpy (CC BY-SA 3.0, già accettata da Michele) con attribuzione e immagini adattate rilasciate con la stessa licenza; la dicitura `PD-old` della pagina del foglio non basta da sola. Piacentine come secondo mazzo.
  - **Nota sul mazzo base:** l'Asso di denari della napoletana in `res` riporta «T. Dal Negro Treviso» e «Made in Italy». ADR-0013 (approvato da Michele) lo dichiara pubblico dominio (Trocche100, `PD-user-it`); la sola presenza di un marchio commerciale sulla carta merita un controllo di Michele prima del rilascio.
- **Domande aperte:** N6. Verifica fatta in `docs/art/mazzi-regionali.md` (2026-10-30): solo le bergamasche hanno 40 carte singole (CC BY-SA 3.0, accettata); romagnole CC0 ma incomplete; gli altri senza carte singole.

## F1G-11 — Integrazione di un mazzo regionale

- **Stato:** bloccato (attende le 40 immagini pulite di un mazzo, vedi F1G-10)
- **Obiettivo:** includere nell'APK un mazzo con scheda approvata e renderlo scegliibile come il napoletano.
- **Perimetro:** asset in `res`, nessuna regola cambiata; ripetibile per mazzo.
- **Criteri di accettazione:** leggibilità nella matrice screenshot; peso verificato.
- **Per Michele:** fornire le 40 carte e il dorso, ritagliate o ridisegnate, del primo mazzo (Bergamasche o Piacentine).
- **Parte tecnica (già prevista, non iniziata):** `DeckTheme` esiste (`designsystem/theme/DeckTheme.kt`); servono un tema per mazzo con prefisso dei drawable, una scelta salvata nelle impostazioni, un `CompositionLocal` che la porti alle carte, la pagina dei crediti con l'attribuzione CC BY-SA e il controllo del peso dell'APK. Si fa quando le immagini ci sono, per non costruire codice senza asset.
- **Domande aperte:** nessuna.

## F1G-12 — Chiusura del giorno

- **Stato:** da fare
- **Obiettivo:** quando sfida, partita e missioni del giorno sono fatte, il Maestro saluta («per oggi basta») e annuncia il domani.
- **Perimetro:** riga/schermata in Home, stato da missioni e serie esistenti; nessun invito a continuare; nessuna perdita se si salta.
- **Criteri di accettazione:** compare una volta al giorno; screenshot; test della condizione.
- **Per Michele:** testi.
- **Domande aperte:** nessuna.

## F1G-13 — Promemoria facoltativo

- **Stato:** da fare (dopo F1G-03)
- **Obiettivo:** una notifica al giorno al massimo, chiesta solo dopo la prima vittoria, solo con consenso.
- **Perimetro:** notifica locale giornaliera (nessun server, nessun dato in uscita); richiesta del permesso `POST_NOTIFICATIONS` (Android 13+) nel momento giusto e mai prima della prima vittoria; interruttore nelle Impostazioni; testo della notifica da `strings.xml` it/en, approvato come gli altri (D16); nessun promemoria se l'utente ha già giocato oggi.
- **Perimetro autorizzato (Michele, 2026-10-30, Q7):** il promemoria giornaliero è accettato; sono concessi il permesso `POST_NOTIFICATIONS` e, per la sola pianificazione, `RECEIVE_BOOT_COMPLETED` nel manifest. **Nessuna nuova dipendenza** è autorizzata: si usano le API della piattaforma (`AlarmManager` inesatto, `NotificationManager`). Se non bastano, il task si ferma e chiede (CLAUDE.md §5).
- **Criteri di accettazione:** il permesso si chiede una sola volta, dopo la prima vittoria, con spiegazione; rifiutato → nessuna notifica e nessuna insistenza; l'interruttore spegne e riaccende la pianificazione; al riavvio del telefono la pianificazione riparte; test sulla logica (quando notificare, mai due volte nello stesso giorno, mai se ha già giocato).
- **Domande aperte:** nessuna.

## F1G-14 — Misure minime del ritorno

- **Stato:** da fare (rimandato: manca una superficie dove leggere i dati)
- **Obiettivo:** sapere se il giocatore torna il giorno dopo e dopo 7 giorni, senza dati personali.
- **Perimetro:** contatori locali anonimi con consenso, oppure strumento esterno (dipendenza nuova → decisione di Michele).
- **Decisione per delega (2026-10-31, Q4):** solo contatori **locali e anonimi** (data del primo avvio e giorni di apertura, in DataStore, che non lasciano mai il telefono); nessuno strumento esterno e nessuna nuova dipendenza. Si implementa quando esiste dove leggerli (strumenti di sviluppo o fase online con consenso, Q13); fino ad allora non serve e non si costruisce.
- **Domande aperte:** nessuna.

## F1G-15 — Partita del giorno offline

- **Stato:** fatto
- **Obiettivo:** una partita a seme comune (seme dalla data) contro un bot, con risultato condivisibile come testo senza rivelare le mosse.
- **Perimetro:** dominio puro; classifica solo locale; verifica se il bot è deterministico dal seme e riportarla; nessun server.
- **Criteri di accettazione:** stesso giorno → stessa mano di partenza; testo di condivisione senza spoiler; test.
- **Domande aperte:** nessuna.
