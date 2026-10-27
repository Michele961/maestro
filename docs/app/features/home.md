# Home — dossier

Stato: **approvato** (Michele, 2026-10-27) · 2026-10-27 · task collegati: F1R-19 (da suddividere dopo l'approvazione), F1R-24 (voce del Maestro), F1R-14
Procedimento: `docs/app/features/README.md`. Va approvato da Michele prima che i task partano.

## 1. Cosa fa per il giocatore

È la prima schermata di ogni sessione: deve far tornare in partita in un tocco, o far scegliere con serenità cosa fare oggi (giocare, allenarsi, la sfida del giorno). Deve far sentire il circolo e il Maestro, senza promettere cose che non ci sono.

## 2. Com'è oggi

**Codice**: `feature/home/ui/HomeScreen.kt` (466 righe), un solo file; navigazione decisa in `MaestroNavHost`.

**Screenshot** (matrice, `home_screen*.png`; il Pixel 10 Pro con partita in corso è stato rivisto): intestazione con avatar, nome, monete, gemme, busta e ingranaggio; riquadro del Maestro con fumetto; tre pulsanti grandi; due tessere di gioco; Sfida del giorno; Missioni e Amici; tre cerchi in basso.

**Problemi**
| # | Problema | Dove |
| --- | --- | --- |
| H1 | **Voci finte**: le **gemme** («280») sono un valore fisso, la **busta** (messaggi) e **Amici** non portano da nessuna parte; **Briscola** dice «Presto disponibile» in una tessera grande | `home_gems_placeholder`, `WoodIconButton` senza azione, tessera Amici |
| H2 | **Nome e livello finti**: «Giocatore · Liv. 1» è una stringa fissa; i livelli di giocatore non esistono (esiste la Maestria) | `home_player_name` |
| H3 | **Etichetta sbagliata**: il cerchio «Classifica» (trofeo) apre il Profilo | `BrassIconBadge(onClick = onProfileClicked)` |
| H4 | **Più chiamate all'azione**: «Riprendi partita», «Nuova partita» e «Gioca» (tessera Scopa) competono; non è chiaro quale sia la strada principale | tre pulsanti grandi |
| H5 | **Il Maestro dice sempre la stessa frase**: nessun legame con ciò che il giocatore ha fatto (partita in corso, streak, errori da rivedere) | `home_coach_welcome` |
| H6 | **Layout**: molto spazio per il riquadro del Maestro; la lista di accesso è lunga e compressa (6 destinazioni + 3 cerchi) | schermo |
| H7 | **Un solo file di 466 righe**: intestazione, riquadro, tessere e barra insieme | architettura |
| H8 | **Crediti e missioni** mostrati ma non collegati alla ricompensa visibile (le missioni a fine partita sono ora riscuotibili, F1R-15) | — |

## 3. Come deve diventare

1. **Una sola azione principale, sempre la stessa posizione.** Se c'è una partita in corso: «Riprendi partita» (grande, in evidenza) e sotto «Nuova partita» in piccolo. Se non c'è: «Gioca» (Scopa) è l'azione principale; la scelta di livello e modalità resta nella pre-partita.
2. **Niente voci finte** (F1R-19): via **gemme**, **busta** e **Amici** finché non esistono; **Briscola** non è una tessera grande: si accenna a «Briscola in arrivo» in una riga, oppure non compare. Se i titoli riempiono troppo, la Home è più corta e ariosa, non riempita.
3. **Nome e identità veri**: nome scelto dal giocatore (da decidere se si chiede; altrimenti solo l'avatar) e **Indice di Maestria** al posto di «Liv. 1» (è il dato vero, F1R-14).
4. **Tre destinazioni chiare**, con il loro nome reale: **Sfida del giorno**, **Missioni** (con la fiamma della serie), **Accademia**; **Profilo** si apre dall'avatar; **Bottega** dal portafoglio (monete). «Classifica» sparisce finché non c'è una classifica.
5. **Il Maestro parla dalla Home con la sua voce** (F1R-24): una battuta dinamica sullo stato del giocatore (partita in corso da riprendere, serie da non spezzare, un errore da rivedere, la sfida di oggi non ancora fatta), mai la stessa a ripetizione, scelta dal `MomentDirector`/dalla stessa memoria; senza crediti di partita (D19).
6. **Layout**: contenuto in colonna che occupa lo schermo senza zone vuote (ADR-0016), l'azione principale raggiungibile col pollice, misure dalla zona disponibile.

**Stati da coprire con gli screenshot** (matrice a 4 configurazioni): nuova installazione (senza partita), con partita in corso, con serie attiva e missione completata da riscuotere, sfida già fatta.

## 4. Contratti

- `HomeUiState` (presentazione): partita in corso, serie, monete, Maestria, stato della sfida del giorno e delle missioni; un `HomeViewModel` lo compone dai repository (oggi la schermata riceve tanti parametri sciolti da `MaestroNavHost`).
- Battuta del Maestro: `VoiceLine` scelta per la Home (nuovi momenti `HOME_*` nel `MomentDirector`, con le stesse regole di ritmo).
- Nessuna dipendenza nuova. Nessun credito dato dalla Home.

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-19a | Home senza voci finte | Gemme, busta, Amici, «Classifica» fuori; Briscola non è una tessera; ogni elemento toccabile porta a una funzione esistente |
| F1R-19b | `HomeViewModel` e stato | Stato unico; una sola azione principale (riprendi o gioca); file < 400 righe |
| F1R-19c | Identità vera | Maestria al posto del livello finto; Profilo dall'avatar, Bottega dal portafoglio |
| F1R-19d | Il Maestro in Home | Battuta dinamica sullo stato del giocatore con la voce del Maestro; test su stati noti |
| F1R-19e | Layout e screenshot | Nessuna zona vuota nella matrice; stati del §3 |

## 6. Domande per Michele

1. **Nome del giocatore**: si chiede al primo avvio, o la Home mostra solo l'avatar e la Maestria? Proposta: solo avatar e Maestria per ora.
2. **Briscola**: nascosta del tutto fino all'arrivo, oppure una riga «Briscola in arrivo» che dà la sensazione di un prodotto in crescita? Proposta: una riga piccola, non una tessera.
3. **Bottega e Profilo**: nel nuovo schema si aprono da portafoglio e avatar (non più dal cerchio in basso). Va bene?
4. **Classifica**: sparisce finché non esiste. Va bene, o preferisci mostrare una sezione «prossimamente» nel Profilo?
5. **Azione principale quando non c'è partita in corso**: «Gioca» parte subito con le ultime scelte (livello e punteggio), oppure apre sempre la pre-partita? Proposta: apre la pre-partita (è dove si sceglie il livello, D11).

## 7. Decisioni di Michele (2026-10-27)

1. **Nome del giocatore**: si **chiede** e resta **modificabile**; la gestione di utenti e accesso verrà più avanti.
2. **Briscola**: nessuna tessera ora; «Briscola in arrivo» si farà quando la situazione sarà stabile (quindi per ora non compare).
3. **Bottega e Profilo**: si aprono da **portafoglio** (monete) e **avatar**, come proposto.
4. **Classifica**: sparisce; verrà con il multiplayer e con un piano per le modalità, quindi per ora non è usata.
5. **«Gioca»** apre la **pre-partita**; Michele nota che potrebbe essere **ridondante** rispetto al resto: con Briscola e Classifica fuori, la Home ha **un solo «Gioca»** (niente tessera Scopa che duplichi il pulsante principale). Se la pre-partita risulta ridondante si rivede insieme dopo la prova.
