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

- **Stato:** da fare
- **Obiettivo:** la «Scalata dei Circoli» come dati: capitoli che richiamano avversario, modalità, Check, livello di sfida, lezione; schermata del capitolo corrente.
- **Perimetro:** modello nel dominio, progresso in DataStore, ricompense solo da `RewardEngine`; capitoli come dati, non codice; nessun blocco a tempo.
- **Criteri di accettazione:** progresso salvato e sopravvive a chiusura; capitolo completato assegna ricompensa una sola volta; screenshot; test del modello.
- **Per Michele:** conferma capitoli e abbinamento Circoli ↔ avversari (Q10).
- **Domande aperte:** Q2, Q10.

## F1G-08 — Testi e voce della Stagione

- **Stato:** da fare
- **Obiettivo:** battute di apertura e chiusura dei capitoli con la voce del Maestro (D18).
- **Perimetro:** `strings.xml` it/en; bozze dell'agente, approvazione di Michele (D16).
- **Criteri di accettazione:** parità it/en; Michele ha approvato.
- **Domande aperte:** tono definitivo.

## F1G-09 — Regole della casa che si sbloccano con gli avversari

- **Stato:** da fare
- **Obiettivo:** dopo ogni avversario battuto, una regola della casa nuova (C2 re bello, C3 tre re o 16, poi C4 napola e C5 asso quando esisteranno), spiegata dal Maestro con una mini-lezione.
- **Perimetro:** sblocchi in `RewardEngine`; mini-lezioni nell'Accademia; le regole non ancora implementate restano nascoste.
- **Criteri di accettazione:** sblocco una sola volta; regole disponibili solo dopo lo sblocco; test.
- **Domande aperte:** abbinamento definitivo regola ↔ circolo.

## F1G-10 — Schede fonte e licenza dei mazzi regionali

- **Stato:** in revisione
- **Obiettivo:** per ogni mazzo candidato, scheda con fonte, licenza, autore, link, 40 carte, semi latini, leggibilità, peso.
- **Perimetro:** solo documenti (`docs/art/`); nessun mazzo incluso senza scheda approvata. Modello: ADR-0013.
- **Criteri di accettazione:** una scheda per mazzo; licenze compatibili con la distribuzione in app.
- **Per Michele:** scegliere le prime tre regioni (N5).
- **Domande aperte:** N5, N6. Verifica fatta in `docs/art/mazzi-regionali.md` (2026-10-30): solo le bergamasche hanno 40 carte singole (CC BY-SA 3.0, da accettare); romagnole CC0 ma incomplete; gli altri senza carte singole.

## F1G-11 — Integrazione di un mazzo regionale

- **Stato:** bloccato
- **Obiettivo:** includere nell'APK un mazzo con scheda approvata e renderlo scegliibile come il napoletano.
- **Perimetro:** asset in `res`, nessuna regola cambiata; ripetibile per mazzo.
- **Criteri di accettazione:** leggibilità nella matrice screenshot; peso verificato.
- **Per Michele:** fornire o confermare le immagini scaricate.
- **Domande aperte:** blocca su F1G-10.

## F1G-12 — Chiusura del giorno

- **Stato:** da fare
- **Obiettivo:** quando sfida, partita e missioni del giorno sono fatte, il Maestro saluta («per oggi basta») e annuncia il domani.
- **Perimetro:** riga/schermata in Home, stato da missioni e serie esistenti; nessun invito a continuare; nessuna perdita se si salta.
- **Criteri di accettazione:** compare una volta al giorno; screenshot; test della condizione.
- **Per Michele:** testi.
- **Domande aperte:** nessuna.

## F1G-13 — Promemoria facoltativo

- **Stato:** bloccato
- **Obiettivo:** una notifica al giorno al massimo, chiesta solo dopo la prima vittoria, solo con consenso.
- **Perimetro:** può richiedere permesso Android 13+ e una libreria nuova: **serve Perimetro autorizzato di Michele** (CLAUDE.md §3).
- **Domande aperte:** Q7; serve una dipendenza?

## F1G-14 — Misure minime del ritorno

- **Stato:** bloccato
- **Obiettivo:** sapere se il giocatore torna il giorno dopo e dopo 7 giorni, senza dati personali.
- **Perimetro:** contatori locali anonimi con consenso, oppure strumento esterno (dipendenza nuova → decisione di Michele).
- **Domande aperte:** Q4.

## F1G-15 — Partita del giorno offline

- **Stato:** fatto
- **Obiettivo:** una partita a seme comune (seme dalla data) contro un bot, con risultato condivisibile come testo senza rivelare le mosse.
- **Perimetro:** dominio puro; classifica solo locale; verifica se il bot è deterministico dal seme e riportarla; nessun server.
- **Criteri di accettazione:** stesso giorno → stessa mano di partenza; testo di condivisione senza spoiler; test.
- **Domande aperte:** nessuna.
