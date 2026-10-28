# Profilo, Maestria e Radar — dossier

Stato: **bozza** (da approvare) · 2026-10-28 · task collegati: F1R-23 (dossier), F1R-26 (da creare dopo l'approvazione), F1R-18e (titoli), F1R-25e (obiettivo consigliato), F1R-14
Procedimento: `docs/app/features/README.md`. Va approvato da Michele prima che i task partano. Le proposte non approvate sono segnate **[proposta]**.

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

1. **Scheda giocatore.** Nome (si cambia toccandolo, come in Home), ritratto, **titolo attuale = il titolo dell'ultimo avversario battuto** («Promosso al bar», …; senza vittorie: «Nuovo al circolo»). Sotto, l'**Indice di Maestria** come numero con il suo nome italiano e una freccia di tendenza sulle ultime partite; la parola «ELO» non compare più (P1, P2).
2. **La scala.** I cinque stemmi in fila: battuto (stemma e titolo guadagnato), prossimo da battere, bloccato (lucchetto). Un tocco su uno stemma mostra nome, titolo guadagnato e quando è stato battuto la prima volta (P3).
3. **Radar delle Abilità.** Cinque assi con nomi italiani (Sette, Denari, Primiera, Banco, **Finale**), l'asse più debole evidenziato. Un tocco su un asse apre una riga: cosa significa e come si allena (P6).
4. **Il Maestro consiglia.** Il consiglio parte dall'asse più debole. Se quell'asse ha un Check (Sette, Denari, Primiera) c'è un pulsante **«Allena questo»** che apre la scala degli avversari con l'obiettivo già scelto; se è Banco o Finale, il Maestro spiega cosa curare ma non offre il pulsante, perché non esiste ancora un Check per quell'asse (P5).
5. **Statistiche.** Un solo blocco: partite, vittorie e sconfitte, precisione media, **serie di giorni** (P7, P10).

**Tab «Partite».** Lo storico, una scheda per partita: ritratto e nome dell'avversario, **Classica o Sparring** (con l'obiettivo), esito, punteggio, precisione, variazione dell'Indice in forma neutra («+18»), «Rivedi analisi». Le partite vecchie, senza avversario registrato, restano visibili come «Avversario non registrato» (P4).

**Esperienza e obiettivi.** Michele ha chiesto titoli, **esperienza alla prima sconfitta di un avversario** e **obiettivi (achievements)** (decisione 5 di `pre-game.md`). Qui si decide solo cosa mostra il Profilo:
- *Titoli*: sì, vedi sopra.
- *Esperienza*: **non si inventa un sistema in questa feature**. Il Profilo prevede un posto per il livello di esperienza accanto al titolo, ma il suo calcolo (che cosa dà punti, quanti) si decide nel dossier **Missioni**, dove vivono anche gli obiettivi. L'evento «avversario battuto per la prima volta» (`LadderVictory.firstTime`) è già pronto per ascoltarlo.
- *Obiettivi (achievements)*: nel dossier Missioni; il Profilo li mostrerà quando esistono.

**Layout e stati da coprire con screenshot** (matrice a 4 configurazioni): giocatore nuovo (nessuna partita, nessun avversario battuto), giocatore intermedio, giocatore che ha battuto tutti, asse toccato aperto, storico vuoto, storico con partite vecchie e nuove, font al 130%.

## 4. Contratti

- `ProfileViewModel` combina `MasteryRepository`, `OpponentRepository` (progressi sulla scala), `RetentionRepository` (serie) e il nome del giocatore; nessun import di `data.*` nella UI.
- **Titolo attuale** e **titoli guadagnati**: derivati da `OpponentProgress` (nessun dato nuovo da salvare); i testi sono quelli di F1R-18e.
- **`MatchHistoryEntry`** guadagna campi opzionali `opponent` e `sparringObjective` (nomi dei rispettivi enum), scritti alla registrazione della partita; le partite già salvate restano valide (nessuna migrazione distruttiva, regola 10 di `docs/app/README.md`).
- **Obiettivo consigliato**: la stessa regola di `ObjectiveAdvisor` (F1R-25e), così consiglio in Profilo e consiglio in preparazione non si contraddicono.
- **«Allena questo»**: nuova rotta tipizzata verso la scala con l'obiettivo suggerito come parametro; la preparazione lo preseleziona (oggi lo calcola da sola).
- File: identità, scala, radar, consiglio, statistiche e storico in file separati, ciascuno sotto le 400 righe.
- Nessuna dipendenza nuova; nessun cambio a `core:*`.

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-26a | Storico con avversario e modalità | `MatchHistoryEntry` con `opponent` e `sparringObjective`, scritti alla registrazione; test su partite vecchie e nuove |
| F1R-26b | Scheda giocatore e titolo | Titolo dalla scala, Indice senza «ELO», nome modificabile, freccia di tendenza; matrice a 4 configurazioni |
| F1R-26c | La scala nel Profilo | Cinque stemmi con stato e titolo guadagnato; dettaglio al tocco; test su nuovo giocatore e scala completata |
| F1R-26d | Radar leggibile e consiglio che porta all'allenamento | Assi in italiano, asse debole evidenziato, spiegazione al tocco, «Allena questo» verso la preparazione con obiettivo preselezionato |
| F1R-26e | Statistiche e tab «Partite» | Statistiche senza doppioni con serie di giorni; storico con ritratto, modalità e obiettivo |
| F1R-26f | Divisione del file e matrice finale | Nessun file sopra 400 righe; screenshot degli stati elencati in §3 |

## 6. Domande per Michele

1. **Titolo del giocatore**: ti torna che sia il titolo dell'ultimo avversario battuto (e non più il grado dell'Indice)? Il grado dell'Indice («Dilettante», …) sparirebbe dalla UI.
2. **Indice di Maestria**: serve un nome per il giocatore diverso da «Elo», e la proposta è proprio «Indice di Maestria» con il numero. Va bene, o preferisci un altro nome (per esempio «Livello di Maestria»)?
3. **Asse «Finale»** (oggi «Endgame») e **«Banco»**: confermi i nomi italiani? Per questi due assi non c'è un Check: il Profilo spiega cosa curare ma non offre «Allena questo». Va bene così per ora?
4. **Esperienza e obiettivi** rimandati al dossier Missioni: confermi?
5. **Partite vecchie** senza avversario registrato: ti torna «Avversario non registrato», o preferisci nasconderle dalla cronologia?
6. Vuoi l'**ordine** delle sezioni di §3 (scheda, scala, radar, consiglio, statistiche), o mettere il radar più in alto?
