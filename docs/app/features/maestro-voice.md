# La voce del Maestro — dossier

Stato: **direzione confermata da Michele** (2026-10-27); restano da chiudere le domande di dettaglio del §6 · 2026-10-27 · task collegati: F1R-24 (da suddividere dopo l'approvazione), F1R-16, F1R-13
Procedimento: `docs/app/features/README.md`. Va approvato da Michele prima che i task partano.
Fonte: indicazione di Michele del 2026-10-27 («battute dinamiche, nei momenti chiave, né troppo spesso né ravvicinate, con umanità e spirito; il Maestro che ti dice che puoi chiedere aiuto»).

## 1. Cosa fa per il giocatore

Durante la partita il Maestro è una **presenza**, non un'etichetta: di tanto in tanto, nei momenti che contano, dice la cosa giusta per *quella* mano. Commenta quello che è successo davvero (le carte uscite, le prese, il punteggio, il momento della smazzata), a volte avverte, a volte incoraggia, a volte scherza. Se il giocatore è in difficoltà, gli ricorda che può chiedere un indizio. Questo è ciò che ci distingue dagli altri giochi di carte (`docs/vision.md`, `docs/design/maestro_persona.md`).

## 2. Com'è oggi

- Il Maestro al tavolo ha **pose** (`MaestroMood`) e un solo fumetto, il **consiglio su richiesta** (`TableHints.kt`).
- Il consiglio non è ancora dinamico come si potrebbe credere: `determineLogicalHintRes` sceglie **una di sei frasi fisse** (`hint_sweep_opportunity`, `hint_settebello_priority`, `hint_denari_priority`, `hint_primiera_priority`, `hint_safe_discard`, generica) in base al tipo di mossa consigliata. Non nomina le carte, non guarda cosa è uscito, il punteggio o il momento della partita.
- Nessuna battuta spontanea. Le righe di `game-feel.md` §5 (saluto, promemoria, scopa subita) non sono fatte: servono testi e, secondo questa indicazione, un meccanismo, non frasi fisse.
- Il Maestro non dice mai al giocatore che può chiedere aiuto: la scoperta del consiglio è affidata al tocco sul ritratto.

## 2b. Decisioni di Michele (2026-10-27)

- La direzione è confermata: Maestro **dinamico**, non testi fissi (D18).
- **Vale anche per la revisione** (Game Review): le spiegazioni devono usare lo stesso meccanismo di fatti e componenti. Michele nota che oggi la revisione talvolta **non è precisissima** e va **tarata** (tuning): fa parte di questa feature, non di un lavoro a parte.
- Le partite contro i bot **non danno crediti** per ora; un sistema monetario più ampio verrà **più avanti**. La voce del Maestro non deve quindi promettere né citare crediti di partita.

## 3. Come deve diventare

### 3.1 Tre strati, tutti puri e testabili

```
Vista del giocatore ──► Fatti ──► Momenti ──► Battuta
 (ScopaView + storia)  (MatchFacts) (MomentDirector) (VoiceComposer → testo localizzato)
```

1. **Fatti** (`MatchFacts`, funzione pura sulla vista): cosa è vero ora, in codici con argomenti. Esempi: «tavolo con somma ≤ 10: chi gioca dopo può fare scopa», «il Settebello è ancora in gioco e tu hai un 7 in mano», «l'avversario ha già 5 Denari», «hai 18 carte: ne servono 3 per i Carte», «ultima distribuzione», «tre prese di fila», «l'avversario ha appena regalato una scopa», «sei in vantaggio di 4 punti», «mano senza prese possibili».
2. **Momenti** (`MomentDirector`): sceglie *se* parlare e *di cosa*. Regole di ritmo: al massimo una battuta spontanea ogni N mosse del giocatore (iniziale N = 3, come `game-feel.md` §5), mai due nello stesso turno, mai durante una scena della regia, tetto per smazzata, un tipo di battuta non si ripete prima di M mosse, memoria delle battute già dette nella partita. Ogni momento ha un peso (avviso > spiegazione > incoraggiamento > colore) e vince il più importante tra i candidati. Una battuta non detta non si recupera.
3. **Battuta** (`VoiceComposer`): trasforma fatto + tono in testo. Non frasi intere fisse: **componenti** (apertura, osservazione con le carte nominate, ragione di gioco, chiusura) con varianti scelte da un `SeededRng` della sessione, così a parità di partita il Maestro dice le stesse cose (replay e test) ma partite diverse suonano diverse. Testi in `strings.xml` it/en con argomenti e plurali (ADR-0007, `game-feel.md` §8); nessun testo nel codice Kotlin.

### 3.2 Carattere (da `maestro_persona.md`)

Toni: **caldo** (incoraggia), **maieutico** (fa una domanda invece di svelare), **ironico/bonario** (scherza, mai sul giocatore), **teatrale** (la scopa subita). Mai denigratorio, mai gergo da computer, mai punteggi interni con decimali. L'umorismo è raro e legato a un fatto vero («Il Settebello se n'è andato in pensione presto, eh?»), mai a caso.

### 3.3 Aiuto: far sapere che esiste

- Prima partita (e finché non l'ha mai usato): il Maestro dice una volta, in un momento tranquillo, che toccando il suo ritratto dà un indizio.
- Se il giocatore esita molto o ha una scelta difficile (molte prese possibili, tavolo pericoloso) e non ha chiesto aiuto, può offrirlo, al massimo una volta ogni poche smazzate; se il giocatore ignora l'offerta, tace per un po'.
- Il consiglio su richiesta diventa dinamico con lo stesso meccanismo: stessi Fatti, `VoiceComposer` con tono maieutico (livelli di `maestro_persona.md` §3), nomina le carte e la ragione ("il Sette di Denari che hai in mano vale 21 in Primiera: se lo cali, lo regali").

### 3.4 Come si vede

- Fumetto nella zona della corsa ai punti (F1R-13): non copre mai mano né carte a terra. La battuta compare all'inizio del turno del giocatore, a scena ferma, e scompare da sola dopo un tempo di lettura proporzionale al testo (`MotionSpec`), o al tocco, o quando si gioca. Il Maestro cambia posa con il tono.
- **Classica** (senza Sparring): solo pose, nessun testo (`game-feel.md` §5). Sparring: battute spontanee attive.
- Impostazione: «Chiacchiere del Maestro» con tre valori (Parla spesso / Normale / Silenzioso), che cambiano N e il tetto per smazzata.

### 3.5 Come si controlla che sia buono

Michele non può leggere ogni combinazione. Si fa così:
- **Trascrizioni.** Uno strumento di test (o `:sim`) gioca una partita a seed fisso e scrive su file cosa avrebbe detto il Maestro e quando, mossa per mossa, con i fatti che l'hanno causato. Michele legge, sceglie le frasi da correggere e il tono.
- **Test di ritmo.** Su centinaia di partite simulate: mai due battute ravvicinate, tetto rispettato, nessuna ripetizione ravvicinata, copertura dei tipi di momento.
- **Approvazione dei testi** (D16): Michele approva il prontuario (aperture, chiusure, battute spiritose) prima del rilascio; le combinazioni nascono da lì.

## 4. Contratti

- `domain/coach/MatchFacts` e `Moment` (puri, su `ScopaView`; nessun orologio: l'esitazione del giocatore arriva come parametro dal `Clock` iniettato).
- `domain/coach/MomentDirector` (stato di ritmo e memoria; riceve `SeededRng`).
- `domain/coach/VoiceComposer` → `VoiceLine(codes, args, tone, mood)`, risolta in testo dalla presentazione con `strings.xml`.
- Presentazione: la `TableScene` porta la `VoiceLine` corrente; `TableDirector` decide quando mostrarla (è una battuta di regia, non un timer della UI).
- Si ricollega a **F1R-16** (`CoachService`): i motivi del consiglio diventano gli stessi codici con argomenti. F1R-16 va fatto *dentro* questo disegno, non prima come lavoro separato.

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-24a | `MatchFacts` | Funzioni pure sulla vista, un test per ogni fatto; nessuna carta nascosta |
| F1R-24b | `MomentDirector` | Ritmo, tetti, pesi, memoria; test su partite simulate |
| F1R-24c | Prontuario e `VoiceComposer` | Componenti it/en con argomenti e plurali; varianti da `SeededRng`; nessun testo in Kotlin |
| F1R-24d | Fumetto e impostazione | Battute a inizio turno a scena ferma; Classica solo pose; impostazione di frequenza |
| F1R-24e | Consiglio dinamico (con F1R-16) | Il consiglio nomina carte e ragione; livelli maieutici |
| F1R-24f | Trascrizioni | Strumento che scrive cosa direbbe il Maestro in una partita a seed fisso |
| F1R-24g | Revisione dinamica e tuning | La Game Review usa fatti e componenti; elenco delle spiegazioni imprecise (da Michele e dai test) e correzione una per una, con test su casi noti |

## 6. Domande per Michele

1. **Modello**: va bene un Maestro **componibile e senza rete** (frasi costruite da fatti veri, varianti da seed, testi approvati da te)? In alternativa o in aggiunta, un Maestro con un modello linguistico dal server (battute libere): più «vivo», ma richiede rete, costa a ogni partita, non è riproducibile e va controllato. Proposta: partire offline, lasciare lo spazio per l'LLM in Fase 3.
2. **Frequenza iniziale**: una battuta spontanea ogni 3 mosse del giocatore al massimo (e in pratica meno) ti sembra giusta come punto di partenza da tarare nel playtest?
3. **Umorismo**: quanto spinto? Bonario e raro, sempre legato a un fatto di gioco, mai sul giocatore. Ci sono temi da evitare o da usare (il bar, il circolo, il caffè)?
4. **Offerta d'aiuto**: il Maestro può prendere l'iniziativa («vuoi un indizio?») o deve solo dire una volta che esiste e poi aspettare?
5. **Impostazione**: ti va un controllo «Chiacchiere del Maestro» a tre livelli, o preferisci nessun controllo per non complicare?
6. **Sparring/Classica**: in Classica solo pose, come da `game-feel.md`. Confermi?
7. **Ordine**: questa feature viene prima o dopo la fine partita e ricompense (F1R-14/15)? Proposta: dopo F1R-14, perché cambia il sapore di ogni partita ma non il loop.
