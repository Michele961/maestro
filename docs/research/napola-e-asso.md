# Napola e asso che taglia — ricerca per la specifica

Stato: **bozza per Michele, non approvata** · 2026-10-30 · task F1G-05 (macro M2, decisione D22)

Questo documento **non modifica** `docs/rules/scopa.md` (file protetto). Raccoglie ciò che dicono le fonti pubbliche e propone, per ogni punto in cui divergono, una scelta. Michele approva (o corregge) punto per punto; solo dopo si scrive la nuova versione della specifica e parte F1G-06.

Le fonti sono quasi tutte secondarie (guide, siti di carte, app): nessuna è un regolamento ufficiale, e dove divergono lo dico. Non ho trovato una federazione che le codifichi.

## 1. Napola

**Cosa dicono le fonti.** La napola (anche «napoli», «nupule») è una sequenza di carte **di Denari** partendo dall'**Asso**: Asso, 2, 3 valgono **3 punti**, e ogni carta consecutiva in più (4, 5, …) aggiunge **1 punto**; fino a 10 punti con tutti i Denari. La sequenza deve essere continua dall'Asso: se manca il 3, vale 0, e le carte più alte non sostituiscono quelle mancanti. Nello scopone la napola da 10 punti è chiamata anche «Napoleone» o «cappotto». Nel tressette «napoli» è un'altra cosa (Asso, 2, 3 dello stesso seme in mano) e **non va confusa**.

Una fonte riferisce una versione con 4 punti se si ha anche il 4 di Denari; è un'eccezione isolata, non la adotto.

**Punti su cui le fonti non dicono abbastanza** (non le ho trovate scritte in modo affidabile):

| # | Domanda | Cosa propongo (da approvare) |
| --- | --- | --- |
| N-a | Si conta sulle **carte prese** a fine mano (non su quelle in mano)? | Sì: sulle carte prese dal giocatore nella mano, come gli altri punti di mazzo |
| N-b | Si **somma** ai punti normali (Carte, Denari, Settebello, Primiera, scope)? | Sì, come punti in più |
| N-c | Conta per il **punteggio obiettivo** (11/16/21)? | Sì, come ogni altro punto della mano |
| N-d | Vale anche se **entrambi** hanno parte della sequenza? | Solo chi ha la sequenza completa dall'Asso (minimo Asso, 2, 3) |
| N-e | Può essere **attiva con `reBello`**, `redealOnThreeKings`, 16/21? | Sì, indipendente dalle altre varianti |
| N-f | Cosa succede ai punti della **Primiera/Denari**? | Invariati; la napola è un bonus a parte |

Sulle lezioni: la napola cambia il valore tattico di Asso, 2, 3 di Denari. Serve un ripasso dell'Accademia e la verifica dei bot (F1G-06), non qui.

## 2. Asso che taglia (asso pigliatutto)

**Cosa dicono le fonti.** L'asso, quando si gioca, prende **tutte le carte del tavolo**. Casi:

| Caso | Cosa dicono le fonti |
| --- | --- |
| Sul tavolo **non ci sono assi** | L'asso prende tutto. Concordi |
| Sul tavolo **c'è già un asso** | L'asso prende **solo l'asso** (qualcherisposta.it). Una variante tradizionale (Board Game Arena): se si è primi a giocare o c'è un asso sul tavolo, l'asso può catturare **solo altri assi** |
| **Tavolo vuoto** | L'asso viene scartato senza prese (BetFlag) |
| La presa conta come **scopa**? | **Divergono**: Board Game Arena dice di sì; qualcherisposta.it e iPhoneItalia dicono di no. La più diffusa sembra «no» |

Attenzione: *Asso pigliatutto* è anche un **gioco diverso** (con valori di carta propri, p. es. asso = 1 punto): quelle regole non sono applicabili alla Scopa.

Punti da decidere:

| # | Domanda | Cosa propongo (da approvare) |
| --- | --- | --- |
| A-a | L'asso prende tutto anche se può fare una presa normale (asso su asso, somma)? | Con assi sul tavolo prende **solo gli assi**; senza assi prende tutto |
| A-b | La presa dell'asso è **scopa**? | No (la più diffusa); un solo asso preso a tavolo vuoto non dà scopa |
| A-c | Tavolo vuoto: l'asso si può giocare? | Sì, resta a terra come ogni carta calata |
| A-d | L'**ultima mano** (ultima carta): se l'asso prende tutto, vale la scopa finale? | Come per ogni presa: no scopa, perché A-b |
| A-e | Il **Settebello** o il Re di Denari in tavolo: l'asso li prende (sì, come tutto) | Sì, prende tutto, anche Settebello (cambia il valore del 7) |
| A-f | `redealOnThreeKings` e le altre varianti | Indipendenti |
| A-g | Il **bot** vede l'asso come mossa obbligata? | No: resta una mossa legale tra le altre. Il motore elenca le mosse legali; l'asso ha **una sola** presa possibile (tutto, o solo assi) |

Nota di motore: l'asso che taglia **cambia la regola della presa** (§1 della specifica: oggi un asso prende solo un asso o somme). Con la variante attiva, per l'asso le prese possibili si riducono a una. Il Settebello ne esce più fragile (un asso lo porta via insieme al resto); le lezioni e i Check sul Sette vanno riviste (F1G-06).

## 3. Cosa succede dopo l'approvazione

1. Michele risponde a N-a…N-f e A-a…A-g (anche «ok a tutte le proposte»).
2. Si scrive la nuova versione di `docs/rules/scopa.md` (§9 «varianti escluse» perde napola e asso; nuovi flag `napola` e `assoPigliatutto`, default `false`) e si aumenta la versione delle regole.
3. F1G-06: motore, test, `RULES_VERSION`, verifica bot e analizzatore, lezioni.

## Fonti consultate

- aranzulla.it, «Come si gioca a scopa» e «Come si gioca ad asso piglia tutto»
- qualcherisposta.it, «Cos'è la napola?» e «Come funziona asso pigliatutto?»
- Board Game Arena, scheda e guida di Scopa
- BetFlag SportNews, «Asso Pigliatutto Online»; sisal.it, scopa; iPhoneItalia (iScopa con Asso piglia tutto)
- Wikipedia, «Scopa»; pagat.com (scopone)
