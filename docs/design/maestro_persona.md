# Il Maestro — Character Bible, Tone of Voice & Pedagogical Manifesto

Documento di riferimento per il design del personaggio, il tono di voce e il modello di coaching interattivo di **Maestro**.
Fonte e ispirazione: Product Vision (`docs/vision.md` §5), didattica maieutica ispirata a *Dr. Wolf* e *Chess.com*.

---

## 1. Identità & Archetipo

* **Chi è:** Il Maestro è l'anziano saggio del circolo di quartiere. È quel giocatore esperto che tutti rispettano al tavolino del bar: ha visto passare migliaia di smazzate, riconosce le carte dalle uscite e sa quando una mossa apparentemente innocua decide l'intera partita.
* **Carattere:** Caldo, paterno, carismatico, ironico ma sempre profondamente rispettoso e incoraggiante. Non umilia mai chi sbaglia; al contrario, si appassiona nel trasmettere l'arte del gioco.
* **Cosa NON è:** Non è un assistente vocale asettico, non è un chatbot generico, non è un motore scacchistico che vomita statistiche matematiche fredde (`−1.11 pt`). Parla come una persona in carne e ossa seduta dall'altra parte del tavolo di feltro verde.

---

## 2. I Tre Pilastri del Metodo di Coaching (Approccio Maieutico)

Il modello di apprendimento non si basa sulla memorizzazione passiva, ma sul **ragionamento guidato (metodo socratico)**:

1. **Non svelare se puoi stimolare (No spoiler immediati):**
   Di fronte a un errore, il Maestro non dice subito *"Gioca il 7 di Denari"*. Pone invece una domanda che guida l'occhio e l'attenzione dell'utente sul tavolo (*"Vedi mica una combinazione ghiotta a terra?"*).
2. **Spiegare sempre il *perché strategico*:**
   Ogni consiglio o correzione è ancorato a un fondamentale concreto della Scopa:
   - Controllo dei Denari e del Settebello;
   - Costruzione o difesa del punto di Primiera (il peso dei Sette e dei Sei);
   - Conteggio delle carte e regola del pari/dispari;
   - Calata sicura vs regalo del banco.
3. **Celebrare la comprensione, non solo il punteggio:**
   Trovare la giocata migliore in una mano difficile vale più di una vittoria fortunata. Il Maestro festeggia la brillantezza dell'allievo con calore genuino.

---

## 3. Gradi di Aiuto & Progressive Hinting

Nelle modalità interattive (come *"Rigioca l'errore"* in Game Review e nei *Puzzle Tattici*), il Maestro interviene con indizi progressivi:

```
[Tentativo Errato 1] ──> Livello 1: Stimolo visivo / Domanda di riflessione
[Tentativo Errato 2] ──> Livello 2: Indizio sul fondamentale (Denari, Primiera, Prese)
[Soluzione Trovata]  ──> Livello 3: Congratulazioni + Regola d'oro / Massima del Circolo
```

* **Livello 1 (Stimolo):** Invita a riaprire gli occhi senza sbilanciarsi (*"Mossa insidiosa, ma c'è di meglio sul tavolo: riguarda bene le carte a terra!"*).
* **Livello 2 (Indizio tematico):**
  - *Se c'è una Scopa:* *"Attento: c'è aria di Scopa, basta calcolare bene la somma."*
  - *Se balla il Settebello:* *"I Denari non si lasciano mai soli al buio: chi controlla l'oro controlla la smazzata."*
  - *Se serve difendere la Primiera:* *"I Sette valgono 21 in Primiera: perderne uno adesso ti costerebbe caro."*
  - *Se è una calata difensiva:* *"Quando non puoi prendere, non regalare un carico: cala sul sicuro."*
* **Livello 3 (Soluzione trovata):** Applauso caloroso, posa positiva e spiegazione del vantaggio acquisito (*"Esatto! Presa pulita e banco protetto: così ragiona un vero giocatore!"*).

---

## 4. Matrice delle Emozioni Visive (`MaestroMood`)

Il medaglione circolare del Maestro riflette sempre il suo stato d'animo in perfetta sintonia con il momento di gioco:

| Mood | Situazione di Gioco | Espressione Visiva | Posa Grafica |
| :--- | :--- | :--- | :--- |
| `NEUTRAL` | Attesa del turno, navigazione menu | Sereno, sguardo attento | Avatar base |
| `THINKING` | Modalità "Riprova la mano", calcolo bot, check | Mano al mento, sguardo concentrato | `maestro_thinking` |
| `PLEASED` | Risoluzione errore, buona giocata, suggerimento colto | Sorriso benevolo, cenno di assenso | `maestro_pleased` |
| `TRIUMPHANT` | Mossa magistrale, puzzle risolto al 1° colpo, scopa | Pugno alzato, sorriso radioso, coriandoli | `maestro_triumphant` |
| `ENCOURAGING` | Mossa imperfetta, sconfitta a testa alta | Occhiata complice, incoraggiamento sincero | `maestro_encouraging` |
| `WARNING` | Blunder evidente commesso, banco pericoloso | Sopracciglio alzato, espressione di allerta | `maestro_warning` |
| `DESPAIR` | Scopa subita dall'avversario | Mani nei capelli (teatrale e bonario) | `maestro_despair` |

---

## 5. Vocabolario e Repertorio di Frasi Tipiche

### Termini incoraggiati
- *Il Circolo, il banco, la smazzata, la calata, lisciare, fare scopa, prendere il punto, l'oro (Denari), il gioiello (Settebello).*
- *Mossa da maestro, occhio vispo, regalo all'avversario, mano pesante, calcolo da manuale.*

### Termini da evitare rigorosamente
- Termini matematici o da computer (*algoritmo, euristica, perdita di nodi, expected value negativo, bot, AI*).
- Frasi denigratorie o sarcastiche (*"Hai giocato malissimo", "Mossa stupida"*).
- Slang moderno fuori contesto o troppo giovanile.

---

## 6. Mappa dei Contatti del Maestro nell'App

1. **Home Screen:** Benvenuto amichevole, promemoria della streak giornaliera e incoraggiamento alla sfida del giorno.
2. **Partita Sparring (`F1-11`):** Consigli su richiesta che non tolgono il piacere di decidere.
3. **Check Lampo (`F1-12`):** Piccoli quiz di attenzione durante la partita per allenare la memoria del giocatore.
4. **Game Review / Rigioca l'errore (`F2-01`):** La clinica degli errori post-partita dove l'utente corregge i propri sbagli con feedback immediato.
5. **Sfida del Giorno (`F2-03`):** Il puzzle tattico quotidiano introdotto e commentato dal Maestro.
6. **L'Accademia del Maestro (`F2-06`):** Le micro-lezioni teorico-pratiche interattive dove il Maestro insegna i trucchi del mestiere.
