# Revisione della partita (Game Review) e «Rigioca l'errore» — dossier

Stato: **bozza** · 2026-10-27 · task collegati: F1R-17, F1R-16, F1R-24g (spiegazioni dinamiche e tuning)
Procedimento: `docs/app/features/README.md`. Va approvato da Michele prima che i task partano.
Ordine deciso da Michele: dopo la voce del Maestro (F1R-24) e la fine partita (F1R-15).

## 1. Cosa fa per il giocatore

È il luogo in cui si impara: dopo la partita il Maestro ripercorre le mosse, segna dove si è perso e fa **riprovare** la mano. È la parte che rende il prodotto diverso dagli altri giochi di carte. Michele: la parte va **migliorata notevolmente** (sia la schermata sia la precisione delle spiegazioni).

## 2. Com'è oggi

**Codice**
- `feature/review/ui/GameReviewScreen.kt`: **1.008 righe** (banner della mossa, banner di «Riprova la mano», tavolo di revisione, navigazione, linea del tempo).
- `feature/review/presentation/GameReviewViewModel.kt` (300) e `ReviewExplanationEngine.kt` (199): la spiegazione è un `if` a cascata che **scrive testi italiani direttamente nel codice** (viola ADR-0007); non guarda la partita, solo la differenza tra mossa giocata e mossa migliore.
- La schermata ricostruisce lo stato del tavolo da sola, senza passare dalla regia (`TableDirector`): la mossa **non si anima**.

**Screenshot** (matrice, Pixel 10 Pro: `game_review.png`, `game_review_retry.png`)
- Posizione statica, nessuna animazione della mossa giocata né della mossa migliore.
- «−1,11 pt»: **punteggio interno con decimali**, vietato da `game-feel.md` §8 (si deve dire cosa si è perso).
- Titolo e pulsanti si sovrappongono («Torna a…» tagliato, «RIPROVA LA MANO» schiacciato dai pulsanti).
- «Tuo turno» in alto a destra, piccolo e senza significato per chi guarda.
- Linea del tempo di 18 puntini senza etichette; due frecce «Errore» e due «Prec./Succ.» ravvicinate; sotto un grande pulsante verde «Torna all'anteprima» (il nome non corrisponde a quello della schermata precedente).
- Tavolo di revisione poco leggibile: carte a terra e mano del giocatore a stessa grandezza, la mossa giocata si riconosce solo da una piccola X.

**Problemi**
| # | Problema |
| --- | --- |
| R1 | Nessuna regia: la mossa e la risposta migliore non si vedono muovere |
| R2 | Spiegazioni generiche e fisse, testi nel codice; a volte imprecise (Michele) |
| R3 | Punteggio con decimali invece di «cosa hai perso» |
| R4 | Layout: sovrapposizioni, testi tagliati, gerarchia dei pulsanti |
| R5 | File di 1.000 righe: ViewModel, schermata e logica mescolate |
| R6 | «Riprova la mano»: il flusso non dice cosa si impara né premia chi trova la mossa |

## 3. Come deve diventare

1. **La regia anche qui.** Ogni mossa della revisione si mette in scena con `TableDirector`/`BeatScript` (come al tavolo): si vede la mossa giocata, poi — a richiesta — la mossa migliore che si anima sul tavolo. Stessa `TableGeometry`, stessi colori e carte del tavolo.
2. **Una mossa alla volta, un solo messaggio.** Il Maestro dice **una cosa** per mossa, con la sua voce dinamica (F1R-24): cosa è successo, cosa è andato perso («Hai lasciato il Settebello»), il principio da ricordare. Mai decimali.
3. **Gli errori prima.** Si apre sul primo errore grave; la linea del tempo è la lista dei momenti chiave con una breve etichetta, non 18 puntini identici.
4. **«Riprova la mano»** (maieutico, `maestro_persona.md` §3): il giocatore prova, il Maestro risponde a livelli (stimolo → indizio → soluzione), si festeggia chi trova la mossa. La mossa trovata mostra la differenza con un'animazione.
5. **Tuning della precisione.** Elenco delle spiegazioni imprecise (da Michele e dai test), corretto uno a uno con test su posizioni note: parte di F1R-24g.
6. Layout con la matrice di ADR-0016 (4 configurazioni), nessun file oltre 400 righe.

**Stati da coprire con gli screenshot**: mossa corretta, errore grave, errore lieve, «Riprova la mano» (iniziale, sbagliato, trovato), mossa migliore animata, ultima mossa.

## 4. Contratti

- `ReviewSession` (presentazione): sequenza di battute della partita, con analisi, per il giocatore umano; riusa `Beat` e `BeatScript` (F1R-17).
- Spiegazione = `VoiceLine` del Maestro (F1R-24), non più `ReviewExplanation` con testi fissi.
- `GameReviewScreen` si spezza in file piccoli (banner, tavolo, navigazione, tempo).

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-17a | Revisione sulla regia | La mossa giocata e quella migliore passano da `TableDirector`; test come al tavolo |
| F1R-17b | Schermata e layout | Banner, tavolo, navigazione senza sovrapposizioni nella matrice; file < 400 righe |
| F1R-17c | Spiegazioni sulla voce del Maestro | Il banner usa `VoiceLine`; niente decimali; testi in `strings.xml` (dopo F1R-24) |
| F1R-17d | «Riprova la mano» | Livelli di aiuto, festeggiamento, differenza animata |
| F1R-24g | Tuning della precisione | Elenco dei casi imprecisi e test su posizioni note |

## 6. Domande per Michele

1. **Casi imprecisi**: ricordi partite o mosse in cui la spiegazione ti è sembrata sbagliata? Anche solo «il Maestro diceva X ma in realtà…»: partiamo da lì per i test.
2. **Mossa migliore**: va mostrata sempre dopo un errore, oppure solo a richiesta («Svela soluzione», come oggi)? Proposta: a richiesta, per non togliere il ragionamento.
3. **Quanto è lunga la revisione**: tutte le mosse, o solo i momenti chiave (errori e mosse brillanti)? Proposta: i momenti chiave, con la possibilità di scorrere tutte.
4. **Classica**: in Classica (senza aiuti, con l'Elo) la revisione resta disponibile dopo la partita? Proposta: sì, è fuori partita.
