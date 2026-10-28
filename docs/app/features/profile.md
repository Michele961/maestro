# Profilo, Maestria e Radar — dossier

Stato: **approvato** (Michele, 2026-10-28) · bozza del 2026-10-28 · task collegati: F1R-23 (dossier), F1R-26 (da creare dopo l'approvazione), F1R-18e (titoli), F1R-25e (obiettivo consigliato), F1R-14
Procedimento: `docs/app/features/README.md`. Approvato da Michele il 2026-10-28 con le decisioni del §6; i punti segnati **[proposta]** nel §3 valgono come approvati salvo diversa indicazione del §6.

## 1. Cosa fa per il giocatore

Gli fa vedere chi è diventato al circolo: il suo titolo, quanto è bravo e dove è debole, quali avversari ha battuto e cosa conviene allenare adesso. Lo apre di tanto in tanto (dopo qualche partita, per vedere i progressi), non a ogni sessione. Deve fargli provare orgoglio per ciò che ha ottenuto e voglia di tornare ad allenarsi.

## 2. Com'è oggi

**Codice** (`feature/profile`): `ProfileScreen.kt` 616 righe, `AbilityRadarChart.kt` 208, `ProfileViewModel.kt` 103. Si apre dall'avatar della Home. Due tab: «Maestria & Radar» e «Cronologia Partite».

- Tab Maestria: scheda giocatore (nome, titolo di grado), Indice di Maestria («1218 ELO» e precisione media), Radar a 5 assi, «consiglio» del Maestro, statistiche (partite e precisione media).
- Tab Cronologia: una scheda per partita con esito, precisione, variazione «ELO» e «Rivedi analisi».
- Test: `ProfileScreenshotTest` (stato iniziale e stato avanzato), matrice a 4 configurazioni.

**Problemi**

| # | Problema | Dove |
| --- | --- | --- |
| P1 | **Si parla di «ELO» ovunque** (badge, cronologia, spiegazione), contro la decisione di Michele del 2026-10-28 di parlare meno di Elo e più di difficoltà ed esperienza | `profile_elo_badge`, `profile_history_elo_delta_*` |
| P2 | **Il titolo di grado viene dall'Indice** (Principiante … «Maestro del Circolo») e non dalla scala degli avversari: i titoli guadagnati in F1R-18e non si vedono da nessuna parte e «Maestro» è anche il nome dell'ultimo avversario | `mapEloToRankTitle` |
| P3 | **I progressi sulla scala non si vedono**: nessuno stemma, nessun avversario battuto | — |
| P4 | **La cronologia dice sempre «Partita vs Maestro»**: non registra né mostra l'avversario né la modalità (Classica o Sparring e obiettivo) | `MatchHistoryEntry`, `profile_history_vs_bot` |
| P5 | **Il consiglio non porta da nessuna parte**: è uno di 5 testi fissi sull'asse più basso, ma l'obiettivo consigliato in preparazione (F1R-25e) considera solo Sette, Denari, Primiera; i due non coincidono se l'asse più basso è Banco o Finale. Non c'è modo di «allenare questo» con un tocco | `computeMaestroAdvice` |
| P6 | **Radar poco leggibile**: l'asse «Endgame» è in inglese; toccando un asse non succede niente, quindi il giocatore non sa cosa significhi né come migliorarlo | `radar_axis_*` |
| P7 | **Precisione media mostrata due volte** (scheda Indice e statistiche) | `MasteryIndexCard`, `PlayerStatsCard` |
| P8 | **Il nome non si cambia qui** (solo dalla Home) | — |
| P9 | **Un file di 616 righe** (soglia di convenzione 400): identità, indice, radar, consiglio, statistiche e cronologia insieme | architettura |
| P10 | **Serie di giorni (streak) assente**, benché sia un dato del giocatore che fa piacere rivedere | — |

## 3. Come deve diventare **[proposta]**

Una pagina che scorre, con due tab: **«Io»** e **«Partite»**.

**Tab «Io»**, dall'alto:

1. **Scheda giocatore.** Nome (si cambia toccandolo, come in Home), ritratto, **titolo scelto dal giocatore tra quelli guadagnati** (di default l'ultimo avversario battuto: «Promosso al bar», …; senza vittorie: «Nuovo al circolo»). Un tocco sul titolo apre l'elenco dei titoli posseduti; lo stesso elenco accoglierà i titoli futuri (sfide, classifiche, altri). Sotto, la **Maestria** (il nome dell'Indice, deciso da Michele) come numero e una freccia di tendenza sulle ultime partite; la parola «ELO» non compare più (P1, P2).
2. **La scala.** I cinque stemmi in fila: battuto (stemma e titolo guadagnato), prossimo da battere, bloccato (lucchetto). Un tocco su uno stemma mostra nome, titolo guadagnato e quando è stato battuto la prima volta (P3).
3. **Radar delle Abilità.** Cinque assi con nomi italiani (Sette, Denari, Primiera, Banco, **Finale**), l'asse più debole evidenziato. Un tocco su un asse apre una riga: cosa significa e come si allena (P6).
4. **Il Maestro consiglia.** Il consiglio parte dall'asse più debole. Se quell'asse ha un Check (Sette, Denari, Primiera) c'è un pulsante **«Allena questo»** che apre la scala degli avversari con l'obiettivo già scelto; se è Banco o Finale, il Maestro spiega cosa curare ma non offre il pulsante, perché non esiste ancora un Check per quell'asse (P5).
5. **Statistiche.** Un solo blocco: partite, vittorie e sconfitte, precisione media, **serie di giorni** (P7, P10).

**Tab «Partite».** Lo storico, una scheda per partita: ritratto e nome dell'avversario, **Classica o Sparring** (con l'obiettivo), esito, punteggio, precisione, variazione dell'Indice in forma neutra («+18»), «Rivedi analisi». Le partite senza avversario registrato (per esempio quelle di test) mostrano «Avversario non registrato». L'app non è uscita: non serve alcuna gestione dedicata di dati vecchi (P4).

**Esperienza e obiettivi.** Michele ha chiesto titoli, **esperienza alla prima sconfitta di un avversario** e **obiettivi (achievements)** (decisione 5 di `pre-game.md`). Qui si decide solo cosa mostra il Profilo:
- *Titoli*: sì, vedi sopra.
- *Esperienza*: **non si inventa un sistema in questa feature**. Il Profilo prevede un posto per il livello di esperienza accanto al titolo, ma il suo calcolo (che cosa dà punti, quanti) si decide nel dossier **Missioni**, dove vivono anche gli obiettivi. L'evento «avversario battuto per la prima volta» (`LadderVictory.firstTime`) è già pronto per ascoltarlo.
- *Obiettivi (achievements)*: nel dossier Missioni; il Profilo li mostrerà quando esistono.

**Layout e stati da coprire con screenshot** (matrice a 4 configurazioni): giocatore nuovo (nessuna partita, nessun avversario battuto), giocatore intermedio, giocatore che ha battuto tutti, asse toccato aperto, storico vuoto, storico con e senza avversario, font al 130%.

## 4. Contratti

- `ProfileViewModel` combina `MasteryRepository`, `OpponentRepository` (progressi sulla scala), `RetentionRepository` (serie) e il nome del giocatore; nessun import di `data.*` nella UI.
- **Titolo attuale** e **titoli guadagnati**: derivati da `OpponentProgress` (nessun dato nuovo da salvare); i testi sono quelli di F1R-18e.
- **`MatchHistoryEntry`** guadagna campi opzionali `opponent` e `sparringObjective` (nomi dei rispettivi enum), scritti alla registrazione della partita; i campi sono opzionali, quindi nessuna migrazione distruttiva (regola 10 di `docs/app/README.md`). L'app non è uscita, perciò non si scrivono casi speciali per dati precedenti.
- **Obiettivo consigliato**: la stessa regola di `ObjectiveAdvisor` (F1R-25e), così consiglio in Profilo e consiglio in preparazione non si contraddicono.
- **«Allena questo»**: nuova rotta tipizzata verso la scala con l'obiettivo suggerito come parametro; la preparazione lo preseleziona (oggi lo calcola da sola).
- File: identità, scala, radar, consiglio, statistiche e storico in file separati, ciascuno sotto le 400 righe.
- Nessuna dipendenza nuova; nessun cambio a `core:*`.

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-26a | Storico con avversario e modalità | `MatchHistoryEntry` con `opponent` e `sparringObjective`, scritti alla registrazione; test sulla lettura con e senza avversario |
| F1R-26b | Scheda giocatore e titolo | Titolo scelto dall'utente tra quelli guadagnati (salvato in DataStore), Maestria senza «ELO», nome modificabile, freccia di tendenza; matrice a 4 configurazioni |
| F1R-26c | La scala nel Profilo | Cinque stemmi con stato e titolo guadagnato; dettaglio al tocco; test su nuovo giocatore e scala completata |
| F1R-26d | Radar leggibile e consiglio che porta all'allenamento | Assi in italiano, asse debole evidenziato, spiegazione al tocco, «Allena questo» verso la preparazione con obiettivo preselezionato |
| F1R-26e | Statistiche e tab «Partite» | Statistiche senza doppioni con serie di giorni; storico con ritratto, modalità e obiettivo |
| F1R-26f | Divisione del file e matrice finale | Nessun file sopra 400 righe; screenshot degli stati elencati in §3 |

## 6. Decisioni di Michele (2026-10-28)

Decise il 2026-10-28:
1. **Titolo** selezionabile dall'utente tra quelli guadagnati (anche in futuro per sfide, classifiche e altri titoli).
2. **Nome dell'Indice**: «Maestria».
3. **Esperienza e obiettivi**: nel dossier Missioni (approvato).
4. **Partite senza avversario**: «Avversario non registrato»; nessuna gestione di dati precedenti (app non uscita).
5. **Ordine delle sezioni**: lascia la scelta all'agente, si cambia dopo. Scelta: **opzione B** — scheda giocatore, **radar**, il Maestro consiglia, la scala, statistiche (e a seguire esperienza e obiettivi). Motivo: il radar è il dato più personale e porta direttamente al consiglio e all'allenamento.
6. **Asse «Finale» e «Banco»**: nomi italiani e assenza di «Allena questo» confermati (non contestati).
