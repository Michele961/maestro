# Missioni, serie, esperienza e obiettivi — dossier

Stato: **bozza** (da approvare) · 2026-10-28 · task collegati: F1R-23 (dossier), F1R-29 (da creare dopo l'approvazione), F1R-14 (`RewardEngine`), F1R-18e (titoli), `docs/app/features/pre-game.md` (decisione 5)
Procedimento: `docs/app/features/README.md`. Va approvato da Michele prima che i task partano. Le proposte non approvate sono segnate **[proposta]**; i numeri sono **[da calibrare]**.

## 1. Cosa fa per il giocatore

Gli dà un motivo per tornare ogni giorno e un senso di crescita: tre piccole missioni del giorno, una serie di giorni da non spezzare, e (nuovi) un'**esperienza** che sale e degli **obiettivi** da collezionare. Tutto ciò che si guadagna passa dal `RewardEngine`. Deve motivare senza far sentire in colpa chi salta un giorno.

## 2. Com'è oggi

**Codice**: `feature/missions` (`DailyMissionsDialog.kt` 385 righe, `DailyMissionsViewModel` 78), `domain/retention` (`DailyRetentionModels.kt` 207, `MatchMissionStatsExtractor` 82), `data/local/datastore/RetentionRepository.kt` 321. Si apre da un **dialogo** dalla Home (badge «N gg» e missioni da riscuotere).

**Come funziona**: ogni giorno tre missioni scelte dalla data fra sei tipi (sfida del giorno, partita con precisione ≥ 80%, due Check sui Sette corretti, due scope, sei Denari catturati, partita senza errori gravi). Le missioni avanzano a fine partita o a sfida risolta e si riscuotono con un tocco (10–15 crediti ciascuna). La **serie** conta i giorni consecutivi con almeno un'attività e ha un solo **Gettone Riposo** che salva la serie una volta.

**Test**: `DailyMissionsViewModelTest`, `RetentionRepositoryTest`, `MatchMissionStatsExtractorTest`, `RewardEngineTest`, `DailyMissionsScreenshotTest`.

**Problemi**

| # | Problema | Dove |
| --- | --- | --- |
| M1 | **Missione e descrizione non coincidono**: «Cattura almeno 6 carte di Denari **in una partita**» avanza invece sommando fra partite diverse; lo stesso vale per «due scope durante le partite» (qui la descrizione è corretta) | `CAPTURE_DENARI` in `RewardEngine.onMatchFinished` |
| M2 | **Le missioni dei Check valgono solo per i Sette**: chi si allena su Denari, Primiera o Settebello non fa mai avanzare nulla, anche se risponde bene; la missione «Occhio ai Sette» può capitare a chi gioca in Classica e non potrà mai completarla | `ANSWER_SEVENS_CHECKS` |
| M3 | **Estrazione casuale poco robusta**: l'indice dello scambio usa una maschera di bit invece di un resto, quindi la scelta non è uniforme; la scelta usa lo stesso seme della Sfida del Giorno (stessa data) | `DailyMissionGenerator` |
| M4 | **Nessun bonus per le tre completate** (`allCompleted` esiste ma nessuno lo usa) | `DailyMissionsState.allCompleted` |
| M5 | **Il Gettone Riposo non si ricarica mai**: è uno solo per sempre; dopo il primo uso la serie è fragile e il giocatore lo scopre quando perde 30 giorni | `StreakState` |
| M6 | **Le missioni non seguono il giocatore**: uguali per tutti, indipendenti dal radar e dall'obiettivo consigliato; chi non ha mai giocato in Sparring riceve missioni sui Check | — |
| M7 | **Un dialogo, non una schermata**: le missioni stanno in un riquadro sopra la Home; non c'è dove vedere il passato né ciò che si è già ottenuto | `DailyMissionsDialog` |
| M8 | **Testi di crediti** («+10 Crediti») mentre le monete hanno «valore blando» e la Bottega non è solida (decisione 5 di `pre-game.md`) | `retention_missions_reward` |
| M9 | **Esperienza e obiettivi non esistono**, benché Michele li abbia chiesti come ricompense dei progressi (decisione 5 di `pre-game.md`); il Profilo e la Home mostrano «Liv. 1» finto (H2 del dossier Home, già corretto con la Maestria) | — |

## 3. Come deve diventare **[proposta]**

### 3.1 Missioni del giorno

1. **Descrizione vera, avanzamento vero** (M1): le missioni dicono ciò che misurano. «Cattura sei Denari **in una partita**» si misura per partita; «due scope» per giorno (somma delle partite del giorno). Ogni missione dichiara il suo ambito (`PER_MATCH` o `PER_DAY`) nel modello e un test lo verifica.
2. **Missioni sui Check per ogni obiettivo** (M2): la missione dei Check diventa «Rispondi bene a N Check» sull'obiettivo giocato (qualunque), e **appare solo a chi ha giocato Sparring negli ultimi giorni**.
3. **Missioni adattive** (M6): una delle tre missioni segue il radar (asse più debole → missione che lo allena: Primiera → «vinci la Primiera in una partita»; Sette → Check sui Sette); le altre due restano casuali dalla data. Ogni missione resta una regola che il motore sa già contare.
4. **Estrazione corretta** (M3): resto invece di maschera, seme diverso da quello della Sfida del Giorno (per esempio con un prefisso fisso), con test sull'uniformità.
5. **Bonus «tre su tre»** (M4): completare tutte e tre dà **esperienza** (non crediti), segnata da `RewardEngine`.
6. **Schermata, non dialogo** (M7): una pagina «Oggi» con le tre missioni, la serie, e sotto l'esperienza e gli ultimi obiettivi ottenuti. Nella Home resta il badge.

### 3.2 Serie di giorni

7. **Gettone Riposo che si ricarica** (M5): uno ogni 7 giorni consecutivi di serie, fino a un massimo di 2, assegnato da `RewardEngine`. Il giocatore vede quanti ne ha e quando arriva il prossimo.
8. **Nessuna colpa**: se la serie si spezza, il Maestro lo dice senza rimproveri e mostra il record.

### 3.3 Esperienza (nuovo)

9. **Un solo contatore, «Esperienza»** (PX), che sale con le cose che già succedono e solo tramite `RewardEngine`. Fonti **[da calibrare]**:

   | Evento | PX |
   | --- | --- |
   | Prima vittoria su un avversario (in Classica, `LadderVictory.firstTime`) | 100 × numero d'ordine (100, 200, … 500) |
   | Capitolo dell'Accademia finito | 50 |
   | Livello della Sfida del Giorno risolto | 10 / 15 / 20 |
   | Missione riscossa | 10 |
   | «Tre su tre» | 25 |

   I livelli di esperienza hanno nomi nello stile del circolo (da [bozza] vostra), per esempio ogni 250 PX. L'esperienza **non** si compra e non si spende: segna solo la strada fatta. Il livello si mostra nel Profilo accanto al titolo (`profile.md`) e sostituisce il «Liv. 1» finto.

### 3.4 Obiettivi (achievements)

10. **Elenco iniziale di una dozzina di obiettivi**, ciascuno legato a un evento già esistente e con un identificatore stabile (`first_win`, `beat_gigi`, …) pensato per essere agganciato in futuro agli obiettivi di Google Play Games (decisione 3 di `pre-game.md`):

    | Obiettivo | Evento |
    | --- | --- |
    | Prima vittoria | prima partita vinta in Classica |
    | Un avversario battuto (×5) | `LadderVictory.firstTime`, uno per avversario |
    | Scala completa | tutti e cinque battuti |
    | Prima scopa / dieci scope | scope totali |
    | Partita perfetta | precisione ≥ 90 e nessun errore grave |
    | Una settimana | serie di 7 giorni |
    | Studente | primo capitolo dell'Accademia finito |
    | Allievo del Maestro | tutta l'Accademia |
    | Occhio di lince | 10 Check corretti di fila |
    | Sfida piena | tre livelli della Sfida del Giorno nello stesso giorno |

    Ogni obiettivo ha nome, riga che dice come si ottiene, e un segno ottenuto/da ottenere; compare un avviso breve quando si sblocca (fine partita o schermata «Oggi»). **Nessuna moneta** come premio (D19/decisione 5).

11. **Archivio nel Profilo**: l'elenco completo degli obiettivi sta nel Profilo (ordine e posizione si decidono nel dossier `profile.md`).

**Layout e stati da coprire con screenshot** (matrice a 4 configurazioni): «Oggi» con missioni da fare, in corso, completate e riscosse; serie con e senza Gettone; esperienza a inizio e a metà livello; elenco obiettivi (nuovo, alcuni ottenuti, tutti); avviso di sblocco; font al 130%.

## 4. Contratti

- `DailyMission` guadagna l'ambito (`MissionScope.PER_MATCH | PER_DAY`); `RewardEngine` lo rispetta nel calcolare l'avanzamento. Le missioni adattive prendono il radar da `MasteryRepository`, senza che la UI decida.
- `StreakState` guadagna la ricarica del Gettone Riposo (regola nel dominio, testata con `FakeClock`).
- **`Experience`** nuovo nel dominio (`domain/progress`): valore, livello, soglie; **`AchievementId`** (enum con identificatore stabile) e `AchievementRepository` (DataStore, solo l'insieme degli ottenuti); entrambi scritti **solo** da `RewardEngine`, che espone in `RewardSummary` i campi `experienceGained` e `achievementsUnlocked`.
- Gli eventi ascoltati sono quelli già emessi: `LadderVictory.firstTime` (F1R-18c), lezione/capitolo (Accademia), livello del puzzle, missione riscossa, fine partita.
- La UI legge solo lo stato (`ProgressViewModel`) e non assegna nulla; i testi di nome e descrizione degli obiettivi sono in `strings.xml` it/en (ADR-0007).
- Persistenza con campi aggiuntivi e valori di default, nessuna migrazione distruttiva (regola 10 di `docs/app/README.md`).
- Nessuna dipendenza nuova; nessun cambio a `core:*`.

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-29a | Missioni vere | Ambito per partita o per giorno, descrizioni coerenti, estrazione uniforme con seme proprio; test su ambito e uniformità |
| F1R-29b | Missioni dei Check e adattive | Check su qualunque obiettivo, solo a chi gioca Sparring; una missione dal radar; test |
| F1R-29c | Gettone Riposo e serie | Ricarica ogni 7 giorni, massimo 2; nessun rimprovero a serie spezzata; test con `FakeClock` |
| F1R-29d | Esperienza | `Experience` nel dominio, fonti da `RewardEngine`, livello nel Profilo; test su ogni fonte e una volta sola |
| F1R-29e | Obiettivi | `AchievementId`, repository, sblocco da `RewardEngine`, avviso breve; test su ogni obiettivo |
| F1R-29f | Schermata «Oggi» | Missioni, serie, esperienza e ultimi obiettivi in una pagina; badge in Home; matrice a 4 configurazioni |

## 6. Domande per Michele

1. **Esperienza**: ti torna l'idea di un solo contatore che sale con le cose già esistenti (vittorie sugli avversari, capitoli, sfida, missioni)? I numeri di §3.3 sono solo un punto di partenza.
2. **Nome e livelli** dell'esperienza: li scrivo io come [bozza] nello stile degli avversari («Novellino del bar»…), o hai un'idea?
3. **Obiettivi**: l'elenco di §3.4 va bene come inizio? Cosa aggiungeresti o toglieresti? Gli identificatori restano stabili per Google Play Games.
4. **Missioni adattive** dal radar: le vuoi, o preferisci missioni uguali per tutti?
5. **Gettone Riposo**: uno ogni 7 giorni di serie, massimo 2: ti torna?
6. **Crediti delle missioni** (10–15): restano? Con la Bottega ancora da definire potrebbero perdere senso; in alternativa si dà solo esperienza.
7. **Dialogo o schermata «Oggi»**: confermi che le missioni diventano una pagina?
