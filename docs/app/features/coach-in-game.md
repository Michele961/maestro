# Suggerimenti e Check in partita — dossier

Stato: **approvato** (Michele, 2026-10-28) · 2026-10-28 · task collegati: F1R-24 (suggerimenti, già fatti con la voce del Maestro), F1R-23
Procedimento: `docs/app/features/README.md`. Decisioni in §6.

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
| F1R-25e | Obiettivi adattivi | Nuovi obiettivi scelti da livello e lacune; elenco approvato prima del codice |

## 6. Decisioni di Michele (2026-10-28)

1. **Fumetto al posto della finestra**: approvato.
2. **Se sbagli**: la forma può dipendere dalla domanda; si struttura come **indizio + risposta** (indizio prima, poi risposta con spiegazione).
3. **Obiettivi**: oltre ai cinque di oggi, se ne aggiungono altri **scelti in base al livello del giocatore e alle sue lacune** (dati di Maestria/radar), per spronarlo a migliorare e tenerlo in esercizio. Ogni nuovo obiettivo deve essere una regola di conteggio già nella specifica.
4. **Frequenza**: **per smazzata**; resta da definire il momento giusto in cui proporre il Check (vedi «Aperto»).
5. **Classica**: i Check restano **solo in Sparring**.

### Aperto

- **Momento del Check nella smazzata** (a quale mano/turno): da proporre nel task F1R-25a con i dati di una simulazione, senza coprire scene della regia né battute spontanee.
- **Scelta degli obiettivi adattivi**: elenco dei nuovi obiettivi e regola con cui si sceglie in base alle lacune (F1R-25e): proposta da approvare prima del codice.
