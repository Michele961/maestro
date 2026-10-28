# Suggerimenti e Check in partita — dossier

Stato: **bozza** · 2026-10-28 · task collegati: F1R-24 (suggerimenti, già fatti con la voce del Maestro), F1R-23
Procedimento: `docs/app/features/README.md`. Va approvato da Michele prima che i task partano.

## 1. Cosa fa per il giocatore

In partita (Sparring) il Maestro aiuta in due modi: **su richiesta** con il consiglio (due livelli: stimolo, poi soluzione) e **di sua iniziativa** con le battute; in più, se il giocatore ha scelto un obiettivo di allenamento, gli fa un **Check**: una domanda rapida sulla partita («Quanti Sette sono già passati?») per allenare memoria e attenzione, che è il cuore della promessa del prodotto (`docs/vision.md`).

## 2. Com'è oggi

**Suggerimenti**: fatti con `docs/app/features/maestro-voice.md` (F1R-24). Consiglio dinamico a due livelli, voce del Maestro, equilibrato secondo la prova di Michele (2026-10-27/28). Non serve altro qui.

**Check** (`TrainingQuestionDialog.kt`, 265 righe; `TrainingChecks.kt` in `core:game`; innesco in `GameReducer`):
- L'obiettivo si sceglie in pre-partita: Sette, Denari, Primiera, Settebello, Carte (+ «Nessun check»).
- A inizio del turno del giocatore, se la posizione è «critica» per quell'obiettivo, e al massimo **una volta per smazzata**, compare una **finestra modale** con 3–4 risposte.
- Dopo la risposta c'è un feedback «corretta/non corretta» e «Continua la partita»; a fine partita compare un punteggio di attenzione («Punteggio di attenzione: 70%»).

**Problemi**
| # | Problema | Dove |
| --- | --- | --- |
| C1 | **Voce fissa e fredda**: titolo «Check del Maestro» e feedback «Risposta non corretta» sono frasi fisse, senza la voce del Maestro, senza spiegare il perché | `strings.xml` `training_*` |
| C2 | **Non dice la risposta giusta né come arrivarci**: se sbagli resti con «non corretta» | `TrainingQuestionDialog` |
| C3 | **Interrompe con una finestra modale**: copre il tavolo proprio quando serve guardarlo per rispondere (le carte da contare sono a terra e nelle prese) | `TableScreen` |
| C4 | **Una sola scelta di obiettivo per partita**, scelta in pre-partita senza sapere cosa sia (nomi tecnici tra parentesi) | pre-partita |
| C5 | **Il punteggio di attenzione** compare a fine partita senza legame con la Maestria né con le missioni | post-partita |
| C6 | **Testi nel codice dei dati** (`TrainingChecks`): i nomi degli esiti sono corretti, ma gli argomenti delle risposte vanno rivisti per le due lingue | `core:game` / `strings.xml` |
| C7 | **Un file di 265 righe** con dialogo, opzioni, feedback e stati | architettura |

## 3. Come deve diventare (proposta)

1. **Il Check lo fa il Maestro, non un form.** La domanda compare come **fumetto del Maestro** nella zona del Maestro (come le battute) con le risposte in una fila di pulsanti sotto il fumetto, **senza coprire le carte**: si può guardare il tavolo e le prese mentre si risponde. Nessuna finestra modale, tranne il tutorial della prima volta.
2. **Feedback con la voce del Maestro** (stessi `MatchFacts`/`VoiceComposer` di F1R-24): se giusta, un incoraggiamento breve; se sbagliata, la risposta giusta **e perché** («Erano tre Sette: due nelle prese, uno a terra»), con le carte nominate.
3. **Scelta dell'obiettivo comprensibile**: in pre-partita ogni obiettivo ha un nome in italiano semplice e una riga che dice cosa allena («Conta i Sette — allena la memoria»); i nomi tecnici restano nella descrizione.
4. **Punteggio di attenzione collegato**: a fine partita il risultato dei Check entra nella scheda di fine partita (già c'è) e conta per le missioni (esistono missioni sul Check) e per il radar del Profilo; mai crediti di partita (D19).
5. **Ritmo**: al massimo un Check per smazzata (come oggi), mai nello stesso turno di una battuta spontanea, mai durante una scena della regia (già vero).
6. **Layout**: file < 400 righe (dialogo, opzioni, feedback separati), matrice a 4 configurazioni, stati: domanda, giusta, sbagliata, ultima risposta.

## 4. Contratti

- `TrainingQuestion` e `buildTrainingQuestion` (`core:game`): invariati; si aggiunge, se serve, la **spiegazione in codici** (quali carte, quale conto) calcolata dalla stessa vista, per `VoiceComposer`. Nessuna regola di gioco nuova.
- Presentazione: la domanda attiva diventa parte della `MaestroLine` del tavolo (stato già presente in `GameUiState`), non un dialogo.
- Nessuna dipendenza nuova.

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-25a | Il Check come fumetto del Maestro | Nessuna finestra modale; tavolo visibile; risposte toccabili; file < 400 righe |
| F1R-25b | Feedback con la voce del Maestro | Risposta giusta spiegata con le carte; testi it/en da approvare; test su posizioni note |
| F1R-25c | Scelta dell'obiettivo | Nomi semplici e una riga che dice cosa allena, in pre-partita |
| F1R-25d | Punteggio di attenzione collegato | Scheda di fine partita, missioni e radar; nessun credito |

## 6. Domande per Michele

1. **Fumetto al posto della finestra**: va bene che il Check non copra il tavolo? Proposta: sì, è il motivo per cui oggi non si riesce a contare.
2. **Se sbagli**: il Maestro dice subito la risposta giusta con la spiegazione, oppure prima dà un indizio e poi la risposta (come il consiglio a due livelli)? Proposta: indizio al primo errore della partita, risposta e spiegazione dal secondo.
3. **Obiettivi**: i cinque di oggi bastano o ne vuoi altri (es. «Quante carte restano», «Chi ha più Denari»)? Servirebbero solo se sono regole di conteggio già nella specifica.
4. **Frequenza**: un Check per smazzata è giusto, o meno (ogni due smazzate) per non pesare?
5. **Classica**: i Check restano solo in Sparring (come oggi) e mai in Classica? Proposta: sì.
