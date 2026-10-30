# Specifica normativa — Scopa a 2 giocatori

Versione specifica: 2 (`CoreGame.RULES_VERSION` resta 1: le nuove varianti sono spente di default e i log esistenti restano validi) · Approvata da: Michele · 2026-10-23; versione 2 (aggiunte `napola` e `assoPigliatutto`, §4.10 e §6.4) approvata da Michele il 2026-10-30, ricerca in `docs/research/napola-e-asso.md`
Riferimento esterno usato per la stesura: [pagat.com — Scopa](https://www.pagat.com/fishing/scopa.html) (consultato il 2026-10-23).

**Questa specifica vince sul codice.** Un comportamento non descritto qui non va implementato. I punti marcati `DA CONFERMARE` hanno un default provvisorio: implementalo, ma senza estenderlo.
Ogni test di regola cita il paragrafo (es. `spec4_2_...`, vedi `docs/conventions.md`).

## 1. Materiale

1.1. Mazzo di 40 carte: 4 semi (Denari, Coppe, Spade, Bastoni) × 10 valori.
1.2. Valori di presa: Asso 1, Due…Sette 2–7, Fante 8, Cavallo 9, Re 10.
1.3. Il tema grafico del mazzo (napoletane, piacentine, …) non influisce sulle regole.

## 2. Partita e smazzate

2.1. Una partita è una sequenza di smazzate. Il punteggio si aggiorna solo **a fine smazzata**.
2.2. Il primo mazziere è determinato dal seed della partita (`SeededRng`). `DA CONFERMARE`: in futuro potrebbe essere scelto con un'animazione; il risultato resta deterministico.
2.3. A ogni nuova smazzata il mazziere passa all'altro giocatore.

## 3. Distribuzione

3.1. Il mazzo si mescola con Fisher–Yates su `SeededRng` (ADR-0009), partendo dal mazzo completo in ordine canonico. Nel mazzo mescolato l'indice 0 è la carta in cima.
3.2. Il mazziere dà le carte una alla volta dalla cima, alternando, iniziando dall'avversario, fino a 3 carte a testa; poi mette le 4 carte successive scoperte sul tavolo, nell'ordine in cui escono.
3.3. Se tra le 4 carte del tavolo ci sono **3 o 4 Re**, la smazzata si annulla: si rimescola e si ridistribuisce con lo stesso mazziere. Variante `redealOnThreeKings`, default `true`. Il nuovo mescolamento continua la stessa sequenza di `SeededRng`.
3.4. Quando entrambi i giocatori hanno finito le carte e il mazzo non è vuoto, il mazziere dà altre 3 carte a testa come in 3.2 (una alla volta, prima all'avversario), senza carte nuove sul tavolo. La ridistribuzione avviene automaticamente dentro la giocata che svuota l'ultima mano (evento `CardsDealt`).
3.5. Con 2 giocatori ogni smazzata ha 6 distribuzioni: 36 carte in mano più 4 sul tavolo.

## 4. Turno di gioco

4.1. Gioca per primo l'avversario del mazziere; poi i turni si alternano, una carta per turno.
4.2. **Precedenza della carta uguale.** Se sul tavolo c'è almeno una carta dello stesso valore della carta giocata, la presa deve essere **una sola** di quelle carte, anche se esiste una combinazione per somma. Se le carte uguali sono più di una, il giocatore sceglie quale prendere.
4.3. **Presa per somma.** Se non ci sono carte dello stesso valore, la carta giocata prende un insieme di due o più carte del tavolo la cui somma è uguale al suo valore. Se ci sono più insiemi possibili, il giocatore sceglie quale.
4.4. **Presa obbligatoria.** Il giocatore può giocare qualunque carta della mano. Se la carta scelta può prendere (4.2 o 4.3), deve prendere: non può lasciarla sul tavolo.
4.5. Se la carta giocata non può prendere, resta scoperta sul tavolo.
4.6. Con il tavolo vuoto la carta giocata resta sul tavolo.
4.7. Le carte prese e la carta giocata vanno nel mazzetto delle prese del giocatore.
4.8. Esempio: tavolo AD, 3C, 4S, 5B, 7C (notazione: valore + seme, vedi F0-03). Il Fante di Coppe (FC) può prendere {AD, 7C}, {AD, 3C, 4S} oppure {3C, 5B}. Il 5 di Denari (5D) prende solo {5B}, non {AD, 4S}.

### 4.9. Rappresentazione delle mosse

- Una mossa è `ScopaMove(card, capture)` dove `capture` è l'insieme delle carte prese (vuoto se non prende).
- `legalMoves()` restituisce, per ogni carta in mano: una mossa per ogni carta uguale (4.2); altrimenti una mossa per ogni insieme per somma (4.3); altrimenti una sola mossa con `capture` vuoto.
- Ordine delle mosse: prima per carta giocata, nell'ordine canonico (`docs/conventions.md`); a parità di carta, per presa. Ogni presa si ordina internamente in ordine canonico; due prese si confrontano carta per carta in ordine lessicografico, e se una è prefisso dell'altra viene prima la più corta.
- Ordine canonico delle carte dell'esempio 4.8: AD, 3C, 7C, 4S, 5B (prima il seme: Denari, Coppe, Spade, Bastoni; poi il valore). Le mosse del Fante FC sono quindi, in ordine: {AD, 3C, 4S}, {AD, 7C}, {3C, 5B}.

### 4.10. Asso che taglia (variante `assoPigliatutto`, default `false`)

- Con la variante attiva, l'**Asso** giocato quando sul tavolo **non ci sono Assi** (e il tavolo non è vuoto) prende **tutte** le carte del tavolo. Non ci sono altre prese possibili per quell'Asso: `legalMoves()` restituisce una sola mossa con `capture` uguale a tutte le carte del tavolo.
- Se sul tavolo c'è almeno un Asso, valgono 4.2 e 4.5 come sempre: l'Asso giocato può prendere solo un Asso (a scelta, se più di uno).
- Con il tavolo vuoto l'Asso resta sul tavolo (4.6).
- La presa di tutte le carte fatta dall'Asso con questa regola **non è una scopa** e non vale punti, anche se svuota il tavolo. Il resto delle prese (compresa la presa normale di un Asso su un Asso) segue 5.1.
- Prende anche il Settebello e il Re di Denari eventualmente sul tavolo; le prese contano per tutte le categorie di 6.2.
- Indipendente dalle altre varianti.

## 5. Scopa

5.1. Una presa che lascia il tavolo vuoto è una **scopa** e vale 1 punto.
5.2. **Eccezione:** l'ultima giocata dell'ultima distribuzione della smazzata non vale mai scopa, anche se svuota il tavolo.
5.3. Dopo una scopa la carta giocata dall'avversario resta sul tavolo, come da 4.6.

## 6. Fine smazzata e punteggio

6.1. A fine smazzata, le carte rimaste sul tavolo vanno a chi ha fatto l'**ultima presa**. Non è una scopa. Se nessuno ha preso nulla nell'intera smazzata, restano assegnate a nessuno.
6.2. Punti della smazzata (1 punto ciascuno):

| Categoria | Assegnazione | Parità |
| --- | --- | --- |
| Carte | Più carte prese (oltre 20) | 20–20: nessuno |
| Denari | Più carte di Denari (oltre 5) | 5–5: nessuno |
| Settebello | Chi ha preso il 7 di Denari | — |
| Primiera | Primiera più alta (6.3) | Pari: nessuno |
| Scope | 1 punto per ogni scopa | — |
| Re bello | Chi ha preso il Re di Denari, solo con variante `reBello` attiva (default `false`) | — |
| Napola | Punti della napola (6.4), solo con variante `napola` attiva (default `false`) | — |

6.3. **Primiera.** Ogni giocatore sceglie la carta di valore più alto in ciascun seme secondo questa tabella e le somma:

| Carta | 7 | 6 | Asso | 5 | 4 | 3 | 2 | Fante, Cavallo, Re |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Punti primiera | 21 | 18 | 16 | 15 | 14 | 13 | 12 | 10 |

- Chi non ha almeno una carta in ognuno dei 4 semi non può vincere la primiera.
- Se nessuno dei due ha tutti i semi, il punto non va a nessuno. `DA CONFERMARE`.
- A parità di somma, il punto non va a nessuno.

6.4. **Napola** (variante `napola`, default `false`). Alla fine della smazzata, dopo l'assegnazione delle carte residue (6.1), chi ha preso nella smazzata l'Asso, il 2 e il 3 di Denari ha una napola. Vale **3 punti**, più **1 punto** per ogni carta di Denari consecutiva che segue (4, 5, …, Re), fino a 10 punti con tutti i Denari. La sequenza parte sempre dall'Asso e deve essere continua: se manca una carta, le successive non contano (con Asso, 2, 4, 5 la napola non c'è; con Asso, 2, 3, 5 vale 3). I punti della napola si sommano agli altri punti della smazzata (6.2), contano per il punteggio obiettivo (7.2) e non si assegnano ai pareggi (sono dell'unico giocatore che ha l'Asso di Denari). Indipendente dalle altre varianti.

## 7. Fine partita

7.1. Punteggio obiettivo `targetScore`: 11 (default), 16 o 21. Altri valori non ammessi.
7.2. A fine smazzata, se almeno un giocatore ha raggiunto o superato l'obiettivo, vince chi ha il punteggio totale più alto.
7.3. Se i totali sono pari (anche oltre l'obiettivo), si gioca un'altra smazzata.
7.4. La "chiamata" anticipata della vittoria (chiamarsi fuori) **non** è implementata.
7.5. Fine smazzata e partita avvengono automaticamente dentro la giocata che chiude la smazzata: assegnazione delle carte residue (6.1), punteggio (evento `DealEnded`), poi nuova smazzata con il mazziere cambiato, oppure fine partita (evento `MatchEnded`).
7.6. A partita finita `legalMoves()` restituisce una lista vuota e `play()` restituisce `Rejected(MATCH_OVER)`.

## 8. Configurazione (`ScopaConfig`)

| Campo | Valori | Default | Paragrafo |
| --- | --- | --- | --- |
| `targetScore` | 11, 16, 21 | 11 | 7.1 |
| `redealOnThreeKings` | true / false | true | 3.3 |
| `reBello` | true / false | false | 6.2 |
| `napola` | true / false | false | 6.4 |
| `assoPigliatutto` | true / false | false | 4.10 |

Qualunque altro campo richiede una modifica di questa specifica.

## 9. Varianti escluse (non implementare)

Scopa d'assi, scopa di quindici, scalette, bazzica, sbarazzina, piva, scopone (4 giocatori), scopa a perdere, chiamarsi fuori. Ognuna richiede prima una nuova versione di questa specifica.

## 10. Casi limite da coprire nei test

- 3.3: tavolo iniziale con 3 Re e con 4 Re → ridistribuzione; con 2 Re → nessuna.
- 4.2: carta uguale presente insieme a una somma possibile → si può prendere solo la carta uguale.
- 4.2: due carte uguali sul tavolo → due mosse legali distinte.
- 4.3: più insiemi per somma, anche con carte in comune → una mossa per insieme.
- 4.4: nessuna mossa legale con `capture` vuoto per una carta che può prendere.
- 5.2: scopa all'ultima giocata della smazzata → nessun punto scopa.
- 5.1: scopa all'ultima giocata di una distribuzione non finale → punto valido.
- 6.1: carte residue all'ultimo che ha preso.
- 6.2: parità 20–20 nelle carte e 5–5 nei denari.
- 6.3: giocatore senza un seme; entrambi senza un seme; parità di primiera.
- 7.2 e 7.3: entrambi oltre l'obiettivo nella stessa smazzata; parità oltre l'obiettivo.
- 4.10: Asso con il tavolo senza Assi → una sola mossa che prende tutto; con un Asso sul tavolo → solo la presa dell'Asso (a scelta tra più Assi); con il tavolo vuoto → nessuna presa; presa di tutto il tavolo con l'Asso → nessun punto scopa; con la variante spenta → comportamento di 4.2–4.6.
- 6.4: napola con Asso, 2, 3 (3 punti); con Asso, 2, 3, 4, 5 (5 punti); tutti i Denari (10 punti); Asso, 2, 4, 5 (nessuna napola); Asso, 2, 3, 5 (3 punti); nessuno ha l'Asso di Denari; napola che fa raggiungere il punteggio obiettivo; variante spenta → nessun punto.
- Invarianti: 40 carte distinte in ogni momento tra mazzo, mani, tavolo e prese; somma dei punti "carte" e "denari" coerente con le prese.
