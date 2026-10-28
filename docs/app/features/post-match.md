# Fine partita e ricompense — dossier

Stato: **approvato** (Michele, 2026-10-27) · 2026-10-27 · task collegati: F1R-15 (da suddividere dopo l'approvazione), F1R-14, F1R-24
Procedimento: `docs/app/features/README.md`. Va approvato da Michele prima che i task partano.

## 1. Cosa fa per il giocatore

È l'ultimo momento di ogni sessione: dice com'è andata, cosa si è guadagnato (maestria, missioni, streak) e porta dritto a "Rivedi partita", dove si impara. Deve far venire voglia di un'altra partita e di vedere gli errori, non essere un elenco di numeri.

## 2. Com'è oggi

**Codice**
- `feature/review/ui/PostMatchPreviewScreen.kt` (332 righe): fa tutto da solo. Analizza la partita (con uno spinner "Analizzo…"), calcola il punteggio finale rigiocando il log, e a analisi finita notifica la shell (`onMatchAnalyzed`), che registra la partita in `RewardEngine`.
- Il risultato della registrazione (`RewardSummary`: missioni avanzate, streak, variazione di maestria) esiste ma **non si vede da nessuna parte**: l'indice di maestria si scopre solo nel Profilo.
- Il Maestro dice una frase fissa ("Ho segnato i momenti chiave. Rivediamo insieme gli errori, mossa per mossa") qualunque sia la partita.

**Screenshot** (matrice, `post_match.png`): schermata statica; targa, scheda con precisione e tre conteggi (ottime/inesattezze/errori gravi), fumetto del Maestro, quattro pulsanti (Rivedi partita, Rivincita, Vedi radar abilità, Torna al menu). Su Pixel 10 Pro **metà schermo è panno vuoto**. Il pulsante primario ("Rivedi partita") è blu, "Rivincita" è verde e grande quasi uguale: la gerarchia non è chiara.

**Problemi**
| # | Problema |
| --- | --- |
| P1 | Nessuna sequenza: tutto appare insieme, nessun "momento" di vittoria o sconfitta |
| P2 | La ricompensa non si vede: missioni, streak e maestria sono invisibili al giocatore |
| P3 | Spinner di attesa invece di un'attesa con senso (il Maestro che "segna i momenti chiave") |
| P4 | Frase del Maestro fissa, non legata alla partita (vedi voce del Maestro, F1R-24) |
| P5 | Logica di registrazione dentro una schermata di anteprima |
| P6 | Metà schermo vuoto; gerarchia dei pulsanti poco chiara |

## 3. Come deve diventare

Una **sequenza unica** (`game-feel.md` §6), ogni passo saltabile con un tocco, senza dialog:

1. **Targa Vittoria/Sconfitta** con il punteggio finale (1,2 s), posa e suono del Maestro (festante o incoraggiante; mai umiliante, `maestro_persona.md`).
2. **Il Maestro segna i momenti chiave**: durante l'analisi (che resta fuori dal thread UI) il Maestro "scrive" sul taccuino, invece dello spinner. Se l'analisi è già pronta il passo dura poco.
3. **Precisione e errori**: scheda con precisione e conteggi che si riempiono; una battuta del Maestro **dinamica** sulla partita (dossier voce del Maestro).
4. **Cosa hai guadagnato** (`RewardSummary`), senza crediti di partita (D19):
   - **Maestria**: l'indice che sale o scende, con la differenza (+18).
   - **Missioni**: quelle avanzate, con la barra che si muove; se una si completa, un segno e il rimando a riscuoterla (i crediti delle missioni restano).
   - **Streak**: la fiamma, con il giorno nuovo se è salita.
5. **Azioni**: **Rivedi partita** (primaria, grande, colore del brand), poi **Rivincita**, **Menu**; il radar delle abilità si raggiunge dal Profilo (o da un link secondario).

Layout: contenuti in colonna centrata, che occupa lo schermo (nessun panno vuoto): targa in alto, sequenza al centro, azioni in basso. Misure da `TableGeometry`-style, non fisse (ADR-0016).

**Stati da coprire con gli screenshot** (matrice): vittoria, sconfitta, analisi in corso, ricompensa con missione completata, ricompensa senza missioni, partita ripresa dalla cronologia.

## 4. Contratti

- `RewardSummary` (F1R-14, già pronto) è l'unico input della sequenza; `ShellViewModel.lastReward` lo espone.
- `PostMatchViewModel` (presentazione): carica analisi e punteggio, produce la sequenza di passi (come la regia del tavolo: dati, non timer; durate da `MotionSpec`).
- La **registrazione** della partita esce dalla schermata: l'analisi si lancia da un punto del dominio/shell e la schermata la osserva.
- `PostMatchPreviewScreen` si spezza in file piccoli (targa, scheda, ricompensa, azioni).

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-15a | `PostMatchViewModel` e sequenza | Passi come dati; registrazione fuori dalla schermata; test della sequenza con durate virtuali |
| F1R-15b | Schermata a passi | Targa, attesa del Maestro, scheda, azioni; ogni passo saltabile; nessun file > 400 righe |
| F1R-15c | Ricompensa | Maestria, missioni, streak da `RewardSummary`; screenshot con e senza missioni completate |
| F1R-15d | Messa a punto | Gerarchia dei pulsanti, uso dello spazio, matrice di screenshot, suoni |

## 6. Decisioni di Michele (2026-10-27)

1. **Sequenza**: l'ordine targa → analisi → precisione → ricompensa → azioni piace.
2. **Maestria**: **sempre visibile**, come in Clash Royale (indice con la differenza, in ogni partita, vittoria o sconfitta).
3. **Missioni**: riscossione **dalla sequenza stessa** (un tocco), come proposto.
4. **Pulsanti**: «Rivedi partita» primaria, poi «Rivincita» e «Menu»; radar nel Profilo.
5. **Vittoria/sconfitta**: festa per la vittoria e gesto di incoraggiamento per la sconfitta, o sobrio: **decide l'agente**. Scelta: vittoria con foglie d'oro e Maestro festante (come la scopa, ma più breve); sconfitta sobria, il Maestro incoraggia, mai teatrale.
6. **Ordine dei lavori**: prima la voce del Maestro (F1R-24), poi questa feature (F1R-15), poi la revisione. Le ricompense (crediti di partita, economia) si decidono con l'economia di gioco, il multiplayer e le modalità di gioco: qui restano maestria, missioni e streak.
7. **Dopo la prova sul telefono** (2026-10-27): la sequenza a passi (targa, poi scheda, poi ricompensa, poi azioni) dava l'effetto di componenti che compaiono in ordine sparso, anche riaprendo la partita dalla cronologia. **Cambia**: finché l'analisi (e, per una partita appena giocata, la ricompensa) non è pronta c'è solo il Maestro che analizza; poi la schermata compare intera, tutta insieme. Niente pause tra i passi e niente salto con un tocco; l'ordine dei contenuti dall'alto in basso resta quello del §3. Michele ha approvato l'aggiornamento di questo documento.

## 7. La revisione (Game Review) non è in questa feature

Chiarimento chiesto da Michele: la Game Review ha un **suo dossier** (`review.md`, terzo nell'ordine di `docs/app/features/README.md`) e **non** fa parte di F1R-15. Qui «Rivedi partita» è solo il pulsante che ci porta. In due pezzi:
- **F1R-24g** (voce del Maestro): spiegazioni dinamiche e tuning della precisione.
- **`review.md`** (feature a sé): schermata, flusso «Rigioca l'errore», uso della regia del tavolo (F1R-17). Il dossier è in bozza.
