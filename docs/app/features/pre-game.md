# Pre-partita e avversari — dossier

Stato: **approvato** (decisioni di Michele, 2026-10-28) · 2026-10-28 · task collegati: F1R-18 (avversari come personaggi), F1R-23; decisione D11 (`docs/roadmap.md`)
Procedimento: `docs/app/features/README.md`. Decisioni in §6.

## 1. Cosa fa per il giocatore

Prima di ogni partita sceglie contro chi giocare. Gli avversari sono personaggi del circolo, dal più debole al più forte: si parte dal primo e, battendolo, si **sblocca il successivo**. È una scala di sfida: il giocatore sa sempre qual è il prossimo avversario da battere.

## 2. Com'è oggi

- `feature/pregame/ui/PreGameScreen.kt` (284 righe): tre tessere di livello (Principiante, Esperto, Maestro) con avatar provvisori, scelta del Check e modalità; tutto sempre sbloccato.
- Il motore ha **cinque** livelli (`DifficultyLevel`: Principiante 800, Dilettante 1100, Intermedio 1400, Avanzato 1700, Maestro 2000 di Elo di riferimento), ma la UI ne mostra tre.
- Problemi: nessuna progressione, nessun nome né ritratto (D11 non è chiusa), tessere che dicono solo «Livello N».

## 3. Come deve diventare (proposta, da decisione di Michele del 2026-10-28)

1. **Cinque avversari, uno per livello**, con un titolo scherzoso al posto dell'Elo, ciascuno con nome, stemma, ritratto e una frase di presentazione (testi in `strings.xml` it/en, ADR-0007). Nomi, stemmi e ritratti li fornisce Michele; fino ad allora segnaposto riconoscibili.
2. **Sfida a scala.** Si parte dal primo avversario (Elo più basso). Battendolo in una partita **a punteggio** si sblocca il successivo; così fino al Maestro. Gli avversari battuti restano rigiocabili.
3. **Schermata della scala** al posto delle tre tessere: i cinque avversari in sequenza, con stato (da battere / sbloccato / battuto), titolo (niente Elo e niente indicatori di difficoltà: i pallini non si capivano, 2026-10-28) e il prossimo da affrontare in evidenza. Un tocco su un avversario sbloccato apre la preparazione (modalità; l'obiettivo del Check solo in Sparring). Lo sblocco si ottiene solo vincendo in Classica.
4. **Voce del Maestro**: commenta lo sblocco e incoraggia prima di una sfida difficile (testi [bozza] da approvare, stesso sistema di F1R-24).
5. **Ricompense dello sblocco** (titolo; esperienza e obiettivi quando esisteranno) solo tramite `RewardEngine`. Nessuna moneta.
6. Layout adattivo con la matrice a 4 configurazioni; nessun elemento che richieda scorrimento nascosto per arrivare al prossimo avversario.

## 4. Contratti

- Nuovo stato di progressione «avversario più alto sbloccato» e «avversari battuti» nel dominio (`domain/...`), salvato come dato del giocatore senza migrazioni distruttive. Vive in DataStore (decisione 3), dietro un'interfaccia nel dominio.
- L'esito di una partita contro un avversario viene riconosciuto dalla stessa via con cui oggi si salva la partita; la regola «battuto» è una regola di prodotto e va scritta nel dossier approvato, non nel motore di gioco.
- Nessuna dipendenza nuova; nessun cambio al motore AI.

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-18a | Modello degli avversari e progressione | Cinque avversari, sblocco a scala, test su sblocco e salvataggio |
| F1R-18b | Schermata della scala | Stati da battere/sbloccato/battuto, matrice a 4 configurazioni |
| F1R-18c | Preparazione e avvio partita | Modalità e obiettivo del Check solo in Sparring |
| F1R-18d | Ritratti, stemmi e testi | Asset di Michele; testi it/en approvati |
| F1R-18e | Voce e titoli dello sblocco | Battute del Maestro; titolo assegnato da `RewardEngine`; evento «battuto la prima volta» pronto per esperienza e obiettivi |

## 6. Decisioni di Michele (2026-10-28)

1. **Avversario battuto**: vincendo **una partita completa**; non contano gli allenamenti guidati (Accademia, sfida del giorno, Rigioca l'errore).
2. **Sblocco**: vale **solo in Classica** (senza aiuti). In Sparring si gioca contro qualunque avversario già sbloccato.
3. **Salvataggio**: DataStore. In futuro questa parte passerà al server e si aggancerà agli obiettivi di Google Play; per questo il modello resta piccolo e separato dalla UI (interfaccia in `domain`, implementazione in `data`).
4. **Cinque avversari**, tutti visibili. Meno Elo, più **difficoltà ed esperienza**, con **titoli** divertenti (esempio di Michele: «Il migliore del bar»). L'Elo di riferimento non compare nella UI.
5. **Ricompense**: titoli; esperienza alla **prima** sconfitta di un avversario; obiettivi (achievements). Le monete restano fuori: la Bottega non è ancora solida e il loro valore è blando.
6. **Asset**: li genera Michele con i prompt di `docs/art/asset-prompts.md` (Set Avversari).

### Cosa è nel perimetro e cosa no

- **Titoli**: nel perimetro (F1R-18e). Un titolo per avversario battuto, mostrato sulla scala e nel Profilo; assegnato solo da `RewardEngine`.
- **Esperienza** e **obiettivi/achievements**: oggi non esistono come sistemi (la Maestria è un Elo cognitivo, non un'esperienza). Definirli è compito dei dossier Profilo e Missioni; qui si prevede solo l'evento «avversario battuto per la prima volta» che quei sistemi potranno ascoltare. Nessun sistema nuovo viene inventato in F1R-18.
- **Nomi e titoli** dei cinque avversari: nel prompt degli asset sono **proposte**; Michele può cambiarli prima dei testi finali.
