# Pre-partita e avversari — dossier

Stato: **bozza** · 2026-10-28 · task collegati: F1R-18 (avversari come personaggi), F1R-23; decisione D11 (`docs/roadmap.md`)
Procedimento: `docs/app/features/README.md`. Va approvato da Michele prima che i task partano.

## 1. Cosa fa per il giocatore

Prima di ogni partita sceglie contro chi giocare. Gli avversari sono personaggi del circolo, dal più debole al più forte: si parte dal primo e, battendolo, si **sblocca il successivo**. È una scala di sfida: il giocatore sa sempre qual è il prossimo avversario da battere.

## 2. Com'è oggi

- `feature/pregame/ui/PreGameScreen.kt` (284 righe): tre tessere di livello (Principiante, Esperto, Maestro) con avatar provvisori, scelta del Check e modalità; tutto sempre sbloccato.
- Il motore ha **cinque** livelli (`DifficultyLevel`: Principiante 800, Dilettante 1100, Intermedio 1400, Avanzato 1700, Maestro 2000 di Elo di riferimento), ma la UI ne mostra tre.
- Problemi: nessuna progressione, nessun nome né ritratto (D11 non è chiusa), tessere che dicono solo «Livello N».

## 3. Come deve diventare (proposta, da decisione di Michele del 2026-10-28)

1. **Cinque avversari, uno per livello**, ciascuno con nome, stemma, ritratto e una frase di presentazione (testi in `strings.xml` it/en, ADR-0007). Nomi, stemmi e ritratti li fornisce Michele; fino ad allora segnaposto riconoscibili.
2. **Sfida a scala.** Si parte dal primo avversario (Elo più basso). Battendolo in una partita **a punteggio** si sblocca il successivo; così fino al Maestro. Gli avversari battuti restano rigiocabili.
3. **Schermata della scala** al posto delle tre tessere: i cinque avversari in sequenza, con stato (da battere / sbloccato / battuto), Elo di riferimento in forma semplice e il prossimo da affrontare in evidenza. Un tocco su un avversario sbloccato apre la preparazione (modalità, obiettivo del Check solo in Sparring).
4. **Voce del Maestro**: commenta lo sblocco e incoraggia prima di una sfida difficile (testi [bozza] da approvare, stesso sistema di F1R-24).
5. **Ricompensa dello sblocco** solo tramite `RewardEngine` (nessuna ricompensa fuori dal motore), importi da decidere.
6. Layout adattivo con la matrice a 4 configurazioni; nessun elemento che richieda scorrimento nascosto per arrivare al prossimo avversario.

## 4. Contratti

- Nuovo stato di progressione «avversario più alto sbloccato» e «avversari battuti» nel dominio (`domain/...`), salvato come dato del giocatore senza migrazioni distruttive. Va deciso dove vive (DataStore o Room): non lo decido io (domanda 3).
- L'esito di una partita contro un avversario viene riconosciuto dalla stessa via con cui oggi si salva la partita; la regola «battuto» è una regola di prodotto e va scritta nel dossier approvato, non nel motore di gioco.
- Nessuna dipendenza nuova; nessun cambio al motore AI.

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-18a | Modello degli avversari e progressione | Cinque avversari, sblocco a scala, test su sblocco e salvataggio |
| F1R-18b | Schermata della scala | Stati da battere/sbloccato/battuto, matrice a 4 configurazioni |
| F1R-18c | Preparazione e avvio partita | Modalità e obiettivo del Check solo in Sparring |
| F1R-18d | Ritratti, stemmi e testi | Asset di Michele; testi it/en approvati |
| F1R-18e | Voce e ricompensa dello sblocco | Battute del Maestro, ricompensa da `RewardEngine` |

## 6. Domande per Michele

1. **Quando si «batte» un avversario?** Proposta: vincendo una partita completa (la partita a punteggio già esistente), in qualsiasi modalità tranne gli allenamenti guidati. Va bene, o serve una serie (es. due vittorie su tre)?
2. **Sparring e Classica**: lo sblocco vale in entrambe, o solo in Classica (dove il Maestro non aiuta)? Proposta: solo in **Classica**, perché la sfida deve essere senza aiuti; in Sparring si gioca contro qualunque avversario già sbloccato.
3. **Dove salvare** i progressi della scala (DataStore, come le impostazioni, oppure Room con la cronologia)? Proposta: DataStore, sono pochi valori.
4. **Cinque o tre avversari?** Il motore ha cinque livelli: ti propongo di mostrarli tutti.
5. **Ricompensa** allo sblocco di un avversario: nessuna, monete, o un oggetto della Bottega? Importi da te.
6. **Asset**: per ognuno dei cinque servono nome, stemma, ritratto (e un'espressione se vuoi vittoria/sconfitta). Formato e dimensioni te li do nel task F1R-18d.
