# Accademia — dossier

Stato: **approvato** (Michele, 2026-10-28) · bozza del 2026-10-28 · task collegati: F1R-23 (dossier), F1R-28 (da creare dopo l'approvazione), F1R-21 (revisione dei contenuti, di Michele), F1R-14, F1R-16
Procedimento: `docs/app/features/README.md`. Approvato da Michele il 2026-10-28 con le decisioni del §6; i punti segnati **[proposta]** nel §3 valgono come approvati salvo diversa indicazione del §6.

## 1. Cosa fa per il giocatore

Gli insegna le tecniche della Scopa con brevi lezioni interattive guidate dal Maestro («La regola del dispari», «Conta i Sette», `docs/vision.md` §C), a percorso: una lezione sblocca la successiva. Lo apre quando vuole capire meglio, o quando il Maestro gli dice che una lacuna del radar si cura lì. Deve sembrare una lezione al bar con un amico bravo, non un manuale.

## 2. Com'è oggi

**Codice** (`feature/academy`, `data/local/academy`): `AcademyScreen.kt` **1117 righe**, `AcademyViewModel.kt` 234, `AcademyContent.kt` 819 (i contenuti), `AcademyModels.kt` 122, `AcademyRepository.kt` 140. Si apre dalla Home.

**Struttura**: 4 capitoli, 15 lezioni — Fondamenti (4), La scienza della Primiera (4), Difesa del banco e antiscopa (4), Visione da Maestro (3). Ogni lezione ha passi di quattro tipi: teoria, domanda interattiva, giocata di carta, riepilogo con la «regola d'oro». Le lezioni si sbloccano **in sequenza** (la prima è sempre aperta); il progresso sta in DataStore; ogni lezione superata dà 5 crediti una sola volta (`RewardEngine.onLessonCompleted`). Quasi tutti i testi stanno in `strings.xml` (193 voci `academy_*`).

**Test**: `AcademyViewModelTest`, `AcademyRepositoryTest`, `AcademyScreenshotTest` (matrice a 4 configurazioni).

**Problemi**

| # | Problema | Dove |
| --- | --- | --- |
| A1 | **Contenuti sbagliati rispetto alla specifica**: la lezione 1.3 dice che il Settebello «vale da solo fino a 3 punti di mazzo su 4» (vale 1 punto, `docs/rules/scopa.md` §7); la 1.1 cita «l'Articolo 3 del regolamento» e la 3.4 «l'articolo finale», che non esistono. È il caso di F1R-21 | `academy_l1_1_*`, `academy_l1_3_*`, `academy_l3_4_*` |
| A2 | **Nessun controllo che le lezioni dicano il vero**: i testi non sono collegati alla specifica e nulla fa fallire la build se si scrive una regola inventata | — |
| A3 | **Percorso rigido e non rivedibile**: si procede solo in sequenza e una lezione finita si può solo rifare dall'inizio; non c'è una scelta «salta a quello che mi serve» né un modo di ripassare solo la regola d'oro | `AcademyRepository` (sblocco per indice) |
| A4 | **Nessun legame con il giocatore**: il capitolo consigliato non dipende dal radar; il Maestro non dice «ti serve la lezione sulla Primiera» | — |
| A5 | **Un solo ritmo**: le lezioni sono tutte della stessa lunghezza; nessun «ripasso lampo» da 30 secondi | — |
| A6 | **Il mini-tavolo delle lezioni non passa dalla regia** (stesso problema di D8 nel dossier della Sfida del Giorno) | `LessonTableArea` |
| A7 | **Un file di 1117 righe** (soglia 400): mappa dei capitoli, schede, tessere, lezione, domande, giocata, riepilogo | architettura |
| A8 | **Crediti per lezione** (5) senza altro riconoscimento: finire un capitolo non dà un titolo né un segno visibile; il giocatore non vede quanto ha imparato | — |
| A9 | **File di contenuti da 819 righe**: dati e testi delle 15 lezioni in un unico file | `AcademyContent.kt` |

## 3. Come deve diventare **[proposta]**

1. **Contenuti veri, in prima battuta.** Prima di ogni altra cosa si correggono gli errori noti (A1). I testi della revisione (F1R-21) li approva Michele; qui si fissa il *metodo*: ogni lezione dichiara quali regole di `docs/rules/scopa.md` usa (per esempio «§7, Settebello»), e un **test** controlla che nessun testo citi «Articolo», «articolo», «Art.» né numeri di punti diversi da quelli della specifica per le categorie (A2).
2. **Mappa chiara.** I quattro capitoli come percorso con lo stato di ogni lezione (da fare, in corso, fatta); il capitolo **consigliato dal Maestro** in evidenza, scelto dall'asse più debole del radar: Primiera → capitolo 2, Banco → 3, Sette e Finale → 4, Denari → 1 e 2 (lezione sul Settebello) (A4).
3. **Percorso libero dopo il primo giro.** Le lezioni restano in sequenza solo per il capitolo 1 (le basi); dal capitolo 2 in poi si può aprire qualunque lezione, perché il giocatore sa già giocare e sceglie cosa gli serve (A3).
4. **Ripasso lampo.** Per ogni lezione finita, un tocco su «Ripasso» mostra solo la regola d'oro e una domanda di verifica (30 secondi), che non dà crediti ma aggiorna il segno «ripassata oggi» (A5).
5. **Mini-tavolo sulla regia**: stessi tempi e animazioni del tavolo (`MotionSpec`), come per la Sfida del Giorno (A6).
6. **Riconoscimento**: finire un capitolo assegna un **titolo** tramite `RewardEngine` (stesso meccanismo dei titoli degli avversari, F1R-18e) e un segno sulla mappa; i 5 crediti per lezione restano (A8).
7. **Voce del Maestro**: le battute di apertura e chiusura di ogni lezione e di ogni capitolo usano `VoiceComposer` (F1R-24) per variare in base a ciò che il giocatore ha già fatto; i testi delle lezioni restano quelli approvati in F1R-21.

**Layout e stati da coprire con screenshot** (matrice a 4 configurazioni): mappa a inizio percorso, mappa a metà con capitolo consigliato, capitolo finito con titolo, lezione (teoria, domanda giusta, domanda sbagliata, giocata, riepilogo), ripasso lampo, font al 130%.

## 4. Contratti

- `AcademyLesson` dichiara le regole usate (elenco di riferimenti alla specifica, per il test di A2); nessun cambio ai tipi dei passi.
- Lo sblocco diventa una regola nel dominio (`AcademyProgress`): capitolo 1 in sequenza, dal 2 libero; `AcademyRepository` salva solo cosa è stato completato (senza migrazioni distruttive: l'insieme delle lezioni già completate resta valido).
- **Capitolo consigliato**: funzione pura del radar (`MasteryProfile.radar`), con test su ogni asse; usa lo stesso criterio dell'obiettivo consigliato di F1R-25e (asse più basso).
- **Titoli di capitolo**: assegnati solo da `RewardEngine` (nuovo `onChapterCompleted`), derivati dalle lezioni completate (nessun dato in più da salvare).
- Il mini-tavolo legge i tempi da `MotionSpec`; la presentazione non decide durate.
- `AcademyContent` si divide per capitolo (un file per capitolo, sotto le 400 righe).
- Nessuna dipendenza nuova; nessun cambio a `core:*`.

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-28a | Contenuti coerenti con la specifica | Correzione degli errori A1 con testi approvati da Michele (F1R-21); riferimenti alla specifica per lezione; test che vieta «Articolo» e numeri di punti sbagliati |
| F1R-28b | Sblocco libero e capitolo consigliato | Regola nel dominio (capitolo 1 in sequenza, dal 2 libero); capitolo consigliato dal radar; test su ogni asse |
| F1R-28c | Ripasso lampo | Regola d'oro e domanda di verifica per lezione finita; senza crediti; test |
| F1R-28d | Titoli di capitolo | `RewardEngine.onChapterCompleted`, titolo sulla mappa; test su una volta sola |
| F1R-28e | Mini-tavolo sulla regia | Tempi da `MotionSpec`, nessun `delay` nuovo |
| F1R-28f | Divisione dei file e matrice finale | Schermata e contenuti sotto 400 righe per file; screenshot degli stati di §3 |

## 6. Decisioni di Michele (2026-10-28)

1. **Revisione dei contenuti (F1R-21)**: la fa l'agente con un agente ad hoc (non Michele); si correggono gli errori certi contro la specifica (`docs/rules/scopa.md`) e i testi nuovi restano marcati per una rilettura finale.
2. **Percorso libero dal capitolo 2**: sì.
3. **Capitolo consigliato dal radar**: sì, con la corrispondenza asse → capitolo di §3.
4. **Ripasso lampo** senza crediti: va bene.
5. **Titoli di capitolo**: li scrive e gestisce l'agente; si ritoccano dopo.
6. **Moduli a pagamento**: fuori da questa feature per ora.
