# Game loop e modalità di gioco — ricerca

Stato: **ricerca e proposte** · 2026-10-30 · le decisioni di Michele sono in `docs/roadmap.md` (D20…D26); tutto il resto qui è **proposta** finché non è nel backlog (`docs/backlog/fase-1g.md`).

Questo è un riassunto del documento prodotto dalla sessione di ricerca (2026-10-29) e delle risposte di Michele. Non contiene regole di gioco: le regole stanno solo in `docs/rules/`.

Legenda: **[Fatto]** verificato nel repository o in una fonte; **[Ipotesi]** ragionamento da misurare o decidere.

## 1. Direzione

Strada **C: offline prima (storia, tutorial, prime partite, ritorno quotidiano), multiplayer dopo** il rilascio, in base all'utenza (D20, D21). È coerente con D10. Il multiplayer è la parte più forte nel lungo periodo; le priorità di adesso sono offline.

## 2. Cosa c'è già (non si reinventa)

Scala Gigi → Nonna Rosa → Tonino → Don Alfonso → Il Maestro; Classica e Sparring; Check; Accademia; Sfida del giorno; missioni «Oggi» con serie e Gettone Riposo; esperienza e obiettivi; `RewardEngine`; Bottega; Profilo, Maestria, Radar; revisione con voce del Maestro.

## 3. I buchi

| # | Buco | Risposta |
| --- | --- | --- |
| G1 | Nessun primo avvio guidato né tutorial (c'è solo il nome) | Macro 1 |
| G2 | I pezzi sono funzioni separate, non un percorso | Macro 2 |
| G3 | La scala ha solo 5 avversari | Macro 2, 3, 4 |
| G4 | Nessun confronto con gli altri, nemmeno leggero | Macro 5 |
| G5 | Nessuna misura dei ritorni (solo Crashlytics) | Macro 5 |
| G6 | Nessun promemoria | Macro 5 |
| G7 | `architecture.md` dice ancora «Fase 2» per l'online (D10 l'ha spostato in Fase 3) | PR di sola correzione, da approvare (file protetto) |

## 4. Game loop a tre livelli [Proposta]

- **Partita** (3–10 min): dopo la partita il Maestro lega l'esito al capitolo e propone **una sola** cosa da fare.
- **Giornata** («serata al circolo», 5–15 min): Sfida del giorno + una partita + lezione o ripasso se il radar lo consiglia; il Maestro chiude con «per oggi basta».
- **Settimana e mese**: la Stagione (capitoli), scala, titoli, esperienza, obiettivi.

Per ogni meccanica di ritorno: «se salto un giorno, perdo qualcosa che avevo già?» Se sì, si ammorbidisce. Niente casse a sorpresa, niente oggetti a tempo, nessuna classifica per crediti.

## 5. La Stagione: «Scalata dei Circoli» [Proposta]

I cinque Circoli della visione (`vision.md` §6: Tavolino del Bar, Circolo Rionale, Salotto Nobile, Gran Casinò, Olimpo dei Maestri) abbinati ai cinque avversari, in 10 capitoli; tono da apprendista del Maestro. Nessun blocco a tempo. Il racconto usa solo ciò che esiste: Check = «la prova del Maestro», Sparring = il retrobottega, Accademia = le lezioni, Sfida del giorno = il problema in bacheca, revisione = il taccuino, Bottega = «arreda il tuo tavolo».

| # | Capitolo | Cosa si gioca |
| --- | --- | --- |
| 1 | La prima sera | Mano d'esordio guidata (seme fisso, Sparring), poi Gigi con suggerimenti |
| 2 | La prima partita vera | Gigi, Classica; sblocca Nonna Rosa |
| 3 | Il Maestro ti mette alla prova | Sparring con Check sui Sette |
| 4 | Il problema in bacheca | Sfida del giorno |
| 5 | Nonna Rosa e il suo pianerottolo | Nonna Rosa, Classica |
| 6 | Le regole del circolo | Accademia, capitolo 1 |
| 7 | Il taccuino del Maestro | Revisione dell'ultima partita |
| 8 | Tonino | Tonino, Classica |
| 9 | La scienza della primiera | Accademia capitolo 2 + Check Primiera |
| 10 | Don Alfonso, poi il Maestro | Classica |

L'abbinamento Circoli ↔ avversari è un'ipotesi (i nomi degli avversari sono provvisori, D11). Alternative scritte dalla ricerca, non scelte: «Giro delle osterie» (regioni e mazzi) come espansione futura.

## 6. Primo giorno [Proposta]

Nome → mano d'esordio guidata con seme fisso, in Sparring, **saltabile** («So già giocare») → prima partita con Gigi → la Home si «accende» dopo la prima vittoria (rivelazione progressiva) → il giorno dopo la Home mostra una sola azione, la Sfida del giorno. Promemoria facoltativo (una notifica al giorno, con consenso): può richiedere permesso o libreria nuova → serve un Perimetro autorizzato di Michele.

## 7. Regole della casa e varianti [D22]

Ordine deciso da Michele: prima le varianti già nel motore (`reBello`, `redealOnThreeKings`, `targetScore` 16/21), poi **napola** e **asso che taglia** (asso pigliatutto). Napola e asso sono oggi «varianti escluse» (`docs/rules/scopa.md` §9): serve una nuova versione della specifica, con le regole cercate da fonti pubbliche e **approvate da Michele** prima di scrivere codice. La ricerca ha già visto che le fonti web divergono su: da quale carta parte la sequenza, quanti punti vale la napola, se si somma ai punti normali, se conta per il punteggio obiettivo; per l'asso, cosa prende se sul tavolo c'è già un asso, se vale come prima mossa, se conta scopa, tavolo vuoto.

Idea gradita da Michele: sbloccare una variante dopo ogni avversario («regole della casa» che si accumulano, spiegate dal Maestro con una mini-lezione). Proposta di abbinamento: C1 base; C2 re bello; C3 ridistribuzione con 3 re o punteggio 16; C4 napola; C5 asso che taglia.

Ogni regola nuova richiede: specifica, motore, test, aumento di `RULES_VERSION` (`MatchLog.rulesVersion`), ricalibrazione dei bot (Elo misurati a regole base), lezioni di Accademia, Check/puzzle/revisione. In classificata online valgono solo le regole standard (ADR-0011: code separate per combinazione di varianti).

## 8. Mazzi regionali [D24]

Piacciono a Michele per «far conoscere l'Italia e le regioni»; si scaricano da internet come il napoletano (ADR-0013). Rischio vero: la **licenza**. Per ogni mazzo serve una scheda fonte (licenza, autore, link, 40 carte, semi latini, leggibilità) prima di includerlo. Il napoletano pesa ~6 MB: i mazzi vanno nell'APK, non scaricati a runtime. Le grafiche nuove le genera o le procura Michele (D14).

## 9. Avversari nuovi [D23]

Per ora solo «pelle» (nome, ritratto, voce) sugli stessi 5 livelli del motore. Gli stili di gioco diversi si esploreranno più avanti. Nessun lavoro sul motore AI.

## 10. Multiplayer (dopo il rilascio) [Proposta]

Ordine: Tavolo amichevole su link → Partita del giorno con classifica → Classificata (Glicko-2 nascosto + trofei) → leghe piccole con bot dichiarati → classifiche regionali (regione dichiarata, non GPS, solo sopra una soglia di giocatori) → nazionali. Classifiche per rating, vittorie della settimana, precisione (Maestria); mai per crediti. Server autoritativo (ADR-0002); il server rigioca il `MatchLog` (ADR-0003). Preparazioni offline possibili: «Partita del giorno» a seme comune con testo condivisibile (da verificare se il bot è deterministico dal seme), rating nascosto locale, identificatore locale casuale (dati personali da decidere).

## 11. Domande ancora aperte per Michele

Q2 forma della storia (Circoli + tono apprendista) · Q3 primo giorno prima dell'alpha F1-15 · Q4 come misurare i ritorni · Q5 prima partita di Gigi (mano guidata in Sparring, poi Classica) · Q7 promemoria opt-in · Q10 abbinamento Circoli ↔ avversari e chi scrive i testi · Q13 dati personali prima dell'account · Q14 soglia delle classifiche regionali · Q16 bot dichiarati nelle leghe · N5 quali tre regioni per prime · N6 mazzi guadagnati giocando + edizioni dorate in Bottega. Le risposte vanno scritte nei task del backlog, nelle loro «Domande aperte».

## 12. Fonti esterne (sintesi)

Duolingo (serie e retention), Chess.com (puzzle del giorno, Play Coach), Wordle (una sfida al giorno, condivisione senza spoiler), Balatro (sblocchi, critiche al giro obbligato), app di carte italiane esistenti (nessuna ha un coach), Glicko-2 (rating provvisorio nelle prime partite), studi sui dark pattern nei giochi mobile (arXiv:2412.05039). Molte fonti sono secondarie: nessun numero qui è usato come prova. Non trovati: dati ufficiali di retention di Wordle, il primo avvio di Chess.com, studi specifici sulle serie di giorni.
