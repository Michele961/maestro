# Game Design & Product Vision: Briscola & Scopa AI Coach

## 1. Visione del Prodotto e Concept
Un'applicazione mobile dedicata ai due grandi classici della tradizione italiana, **Briscola** e **Scopa**, ripensata in chiave competitiva ed educativa. 
A differenza dei competitor tradizionali (spesso datati o puramente casual), il cuore dell'esperienza risiede nell'integrazione di un **Coach AI locale ("Il Maestro")** ispirato a piattaforme come *Chess.com* e *Dr. Wolf*. L'obiettivo non è solo far giocare l'utente, ma insegnargli a padroneggiare la matematica, la memoria e la strategia dietro le carte, creando un ciclo continuo di sfida e miglioramento personale.

---

## 2. Direzione Artistica e Stile Visivo
L'estetica fonde il calore della tradizione italiana con l'eleganza di un'interfaccia tecnologica moderna (*"Tavern-meets-Tech"*):
* **Ambientazione (Il Tavolo):** Texture in legno caldo naturale, panno verde scuro con finiture dorate e illuminazione accogliente da circolo/osteria storica.
* **Le Carte:** Stile tradizionale italiano (Napoletane, Piacentine, ecc.) ridisegnate in alta definizione, con bordi curati, texture di carta leggermente vissuta e colori vividi (rossi profondi, ori lucenti).
* **Il Personaggio ("Maestro AI"):** Un avatar carismatico che rappresenta un giocatore anziano, saggio e amichevole, vestito con gilet tradizionale ma incorniciato da elementi grafici digitali (bagliori ciano/oro, micro-circuiti neurali) che si attivano quando analizza le partite.
* **Effetti UI:** Evidenziazioni olografiche sulle carte suggerite durante i replay, indicatori chiari ma non invasivi e icone tridimensionali per i crediti e i bauli premio.

---

## 3. Core Loop (Il Flusso dell'Esperienza)
Il percorso dell'utente all'interno dell'app è progettato per essere rapido (sessioni da 3-5 minuti) e ad alta ritenzione:

1. **Main Lobby (La Sala da Gioco):** Punto di accesso centrale con scelta rapida della partita, visualizzazione del proprio grado/Lega, saldo dei *Crediti Maestro*, missioni giornaliere e accesso diretto all'Accademia.
2. **Pre-Partita:** Scelta del gioco (Briscola o Scopa), della modalità e selezione opzionale di un **"Obiettivo di Allenamento" (Check)** su cui concentrarsi.
3. **In-Game (Il Match):** Partita fluida e veloce. Nelle partite classificate contro altri giocatori il Coach resta in silenzio per garantire equità, ma registra ogni singola mossa e calcolo probabilistico in background.
4. **Post-Partita (Il Punto di Conversione):** Al termine del match, il Coach presenta un'anteprima immediata della prestazione (es. *"Precisione di gioco: 76% - Hai commesso 2 errori critici"* + esito del Check di memoria). Da qui l'utente può scegliere di giocare subito una rivincita o spendere Crediti per entrare nella **Game Review** approfondita.

---

## 4. Modalità di Gioco
L'offerta copre sia chi cerca una partita rilassante sia chi vuole scalare le classifiche:

| Modalità | Descrizione | Ruolo del Coach |
| :--- | :--- | :--- |
| **Partita Classificata (Ranked 1v1)** | Sfide contro giocatori reali del proprio livello. Briscola (secca o al meglio dei 3) e Scopa (a 11 o 16 punti). | Silenzioso in partita. Analisi completa e pagella disponibile a fine match. |
| **Sparring col Maestro (Vs AI)** | Partite di allenamento contro il motore locale calibrato su vari livelli di abilità (da *Principiante* a *Maestro*). | Attivo. Può interrompere il gioco per fare domande (Check) o dare suggerimenti su richiesta. |
| **Tavolo Amichevole (1v1 e 2v2)** | Partite private tramite link d'invito con amici, inclusa la variante a coppie (Scopone / Briscola in 4). | Opzionale (può essere attivato come "partita didattica" per imparare insieme). |
| **Sfida del Giorno (Puzzle Tattici)** | Scenari preimpostati (es. *"Ultime 3 mani, sei sotto di 2 punti: trova la sequenza unica per vincere"*). | Feedback istantaneo mossa per mossa per spiegare la logica della soluzione. |

---

## 5. Il Sistema di Coaching e Apprendimento
Il Coach non è un chatbot testuale esterno, ma un **motore analitico locale** capace di calcolare in tempo reale le probabilità e il valore atteso di ogni carta giocata. Si articola in tre funzioni chiave:

### A. I "Check" di Allenamento (Focus sul Miglioramento)
Prima di iniziare una sessione, il giocatore può scegliere un fondamentale da allenare (es. *Conteggio dei Sette*, *Memoria delle Briscole uscite*, *Controllo dei Denari*). 
* Durante le partite di allenamento, il Coach copre momentaneamente il tavolo e pone un quesito lampo a scelta multipla: *"Quanti carichi sono già passati?"* o *"Chi ha in mano il vantaggio per la Primiera?"*.
* A fine partita, l'utente riceve un punteggio specifico su quanto è stato attento a quel parametro, facendo crescere il proprio **Grafico Radar delle Abilità** (Memoria, Calcolo, Tattica, Gestione Briscola, Lettura Avversario).

### B. Game Review (Analisi Post-Partita stile Chess.com)
Sbloccando la revisione del match, l'utente può rivedere i momenti chiave della partita classificati con codici colore intuitivi:
* **Mossa Magistrale (Blu):** La giocata matematicamente perfetta.
* **Imprecisione (Giallo):** Una carta che ha concesso un piccolo vantaggio probabilistico all'avversario.
* **Errore Grave / "Regalo" (Rosso):** Una mossa che ha ribaltato le sorti della partita (es. regalare una Scopa evitabile o bruciare un carico al buio). Il Maestro mostra visivamente sul tavolo cosa sarebbe successo giocando la carta alternativa.

### C. L'Accademia del Maestro
Una sezione strutturata a percorsi sbloccabili dove imparare le tecniche avanzate attraverso brevi lezioni interattive (es. *"La regola del dispari nella Scopa"*, *"Quando lisciare e quando prendere a Briscola"*, *"Tecniche di memoria rapida"*).

---

## 6. Sistema Competitivo, Leghe e Classifiche
Per stimolare la competizione sul lungo periodo, il sistema di ranking unisce l'accuratezza matematica alla gratificazione visiva:

* **Progressione a "Circoli" (Leghe):** Guadagnando trofei si sale di categoria, sbloccando ambientazioni e tavoli sempre più prestigiosi:
  1. *Tavolino del Bar* (Bronzo)
  2. *Circolo Rionale* (Argento)
  3. *Salotto Nobile* (Oro)
  4. *Gran Casinò* (Diamante)
  5. *Olimpo dei Maestri* (Lega finale basata su punteggio ELO puro).
* **Soglie Salvezza:** Raggiunto un nuovo Circolo, non si può retrocedere a quello inferiore durante la stagione in corso, incoraggiando gli utenti a giocare senza la paura di perdere il traguardo raggiunto.
* **Leaderboards (Classifiche):** Oltre alla classifica Globale e a quella tra Amici, un ruolo centrale è affidato alle **Classifiche Regionali e Provinciali** (es. *Campione di Briscola della Lombardia* o *Top 50 Scopa in Sicilia*), facendo leva sul forte senso di appartenenza territoriale legato a questi giochi.

---

## 7. Social, Emote e Quick-Chat (Stile Clash Royale)
Per rendere le partite vive, espressive e sicure (senza i rischi e i costi di moderazione di una chat libera), i giocatori comunicano attraverso un sistema rapido:

* **Emote Animate con Audio:** Vignette animate del Maestro o di gesti iconici da tavolo da gioco accompagnate da effetti sonori caratteristici:
  * *La "Bussata" sul tavolo* o *lo "Striscio"* (tipici segnali della tradizione).
  * *La Scopa che spazza lo schermo* per festeggiare una presa pulita.
  * *Il Sospiro teatrale* quando si pescano solo carte basse.
  * *L'Occhiolino furbo* dopo una trappola ben riuscita.
* **Frasi Rapide Preimpostate:** Fumetti veloci come *"Bella giocata!"*, *"Che sfortuna!"*, *"Buona partita!"*.
* **Anti-Disturbo:** Possibilità di silenziare le emote dell'avversario con un singolo tocco e pausa automatica di alcuni secondi tra un invio e l'altro per evitare lo spam.

---

## 8. Modello di Monetizzazione ed Economia di Gioco
Il gioco è *Free-to-Play* (gratuito e senza meccaniche "Pay-to-Win" durante le partite), basato su un'economia ibrida pensata per monetizzare sia gli utenti non paganti sia quelli più appassionati:

1. **Sistema a "Crediti Maestro" (Valuta Principale):**
   * Gli utenti ricevono una piccola quantità di crediti gratuiti ogni giorno e possono acquistarne pacchetti nello shop.
   * **Come si spendono:** Per sbloccare le *Game Review* approfondite dopo le partite importanti, per acquistare moduli di studio avanzati nell'*Accademia*, o per sbloccare sessioni di allenamento specifiche sui propri punti deboli.
2. **Annunci Pubblicitari Intelligenti (Rewarded Ads):**
   * Nessun banner invasivo durante il calcolo delle mosse in partita.
   * Video pubblicitari opzionali su scelta dell'utente per ottenere una *Game Review* gratuita a fine match, raddoppiare il premio della *Sfida del Giorno* o ricaricare qualche credito extra.
3. **Elementi Cosmetici (Personalizzazione):**
   * Utilizzo di crediti o acquisti diretti per sbloccare **mazzi regionali storici o dorati**, **dorsi delle carte animati**, **nuove Emote sonore** e **tappeti da gioco** personalizzati.
4. **Abbonamento "Pass del Maestro" (Fase Successiva):**
   * Un'opzione mensile per gli utenti più fedeli che garantisce *Game Review* illimitate, accesso completo all'Accademia, rimozione totale delle pubblicità ed emote esclusive stagionali.