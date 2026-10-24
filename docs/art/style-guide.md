# Guida di Stile e Specifiche Asset Grafici — Maestro

Data: 2026-10-24  
Riferimenti normativi: `CLAUDE.md`, `docs/vision.md`, ADR-0008 (`docs/decisions/0008-asset-generati-ai.md`), `docs/backlog/fase-1.md` (task F1-13).

---

## 1. Visione e Direzione Artistica

Maestro fonde la tradizione centenaria delle carte da gioco regionali italiane con un'estetica moderna, pulita e raffinata.

- **Mood & Atmosfera:** *Elegante, caldo, artigianale ma contemporaneo.* L'atmosfera ricorda una sala da gioco privata o un circolo storico italiano illuminato da luce calda: panno verde bottiglia di qualità, legni caldi, finiture dorate, carte con superficie materica e illustrazioni nitide e leggibili.
- **Originalità (ADR-0008):** Le carte napoletane sono ridisegnate da zero. Non si clonano né ricalcano edizioni commerciali esistenti (es. Dal Negro, Modiano), ma si reinterpretano i simboli e le figure tradizionali con uno stile illustrativo coerente e proprietario.
- **Leggibilità su schermi piccoli (5"–6.7"):** Le carte devono risultare immediatamente distinguibili a colpo d'occhio anche quando disposte in mano (64×96 dp) o sul tavolo di gioco, con indici chiari negli angoli (valore numerico/lettera + seme).

---

## 2. Palette Colori Ufficiale

### 2.1. Ambiente e Tavolo da Gioco
| Ruolo | Nome Colore | HEX | Uso |
|---|---|---|---|
| **Feltro Centrale** | Emerald Felt | `#1B4332` | Panno centrale illuminato |
| **Feltro Ombre** | Deep Forest Felt | `#081C15` | Bordo esterno e zone d'ombra del tavolo |
| **Bordo Tavolo / Legno** | Dark Walnut | `#2C1810` | Cornice perimetrale del tavolo in legno |
| **Accento Oro** | Golden Ochre | `#D4AF37` | Bordi attivi, bottoni primari, bordi carte giocabili |
| **Accento Oro Chiaro** | Warm Gold | `#F1C40F` | Punti luce, bagliori di scopa, testo in evidenza |

### 2.2. Mazzo e Semi (Carte Napoletane)
| Seme / Elemento | HEX Primario | HEX Secondario | Tratti distintivi |
|---|---|---|---|
| **Sfondo Carta** | `#FFFDF8` (Papiro caldo) | `#F4EFE6` (Pergamena) | Texture carta opaca con bordo sottile color nocciola (`#BCAAA4`) |
| **Denari (◆)** | `#D4AF37` (Oro antico) | `#B7860B` (Oro brunito) | Monete con motivi solari/geometrici scolpiti; Settebello arricchito con corona o aquila |
| **Spade (♠)** | `#2C3E50` (Acciaio brunito) | `#1A252F` (Blu notte) | Spade ricurve (scimitarre) tradizionali napoletane, impugnature decorate |
| **Coppe (♥)** | `#C0392B` (Rosso rubino) | `#8E1B0F` (Bordeaux) | Calici cerimoniali lavorati a cesello con dettagli floreali dorati |
| **Bastoni (♣)** | `#27AE60` (Verde smeraldo) | `#1E3A1E` (Legno vivo) | Mazze nodose e rami lavorati con gemme o foglie stilizzate |

---

## 3. Specifiche Tecniche delle Carte

### 3.1. Dimensioni e Risoluzioni
- **Rapporto d'aspetto classico napoletano:** `1 : 1.56` (carte strette e allungate, tipiche dello stile spagnolo/napoletano).
- **Risoluzione master asset (alta definizione):**
  - **Dimensione file consigliata:** `600 × 936 px` (oppure `800 × 1248 px` per schermi 3x/4x o vettoriale SVG).
  - **Formato:** `.webp` (senza perdita di qualità o compressione 90%) o `.png` a 32-bit con canale alpha.
- **Anatomia della carta:**
  - **Raggio angoli arrotondati:** ~`6%` della larghezza (~36 px sul master da 600 px).
  - **Margine esterno di sicurezza (Safe margin):** `4%` perimetro vuoto prima del filetto di contorno.
  - **Cornice interna:** doppio filetto sottile (uno esterno nocciola tenue, uno interno dorato/seme).
  - **Indici angolari:**
    - Angolo in alto a sinistra e in basso a destra: valore del rango (`1`, `2`, `3`, `4`, `5`, `6`, `7`, `F`, `C`, `R`) con micro-simbolo del seme.
    - Questo garantisce la lettura immediata anche quando le carte sono sovrapposte o a ventaglio.

---

## 4. Composizione del Mazzo (40 Carte)

Il mazzo è composto da 4 semi × 10 carte ciascuno:

### 4.1. Carte Numerali (1–7 per ogni seme)
- **Asso (1):** Simbolo del seme grande e maestoso al centro della carta, arricchito da motivi ornamentali (es. Asso di Coppe come calice trionfale, Asso di Bastoni come grande bastone fiorito, Asso di Spade con lama decorata, Asso di Denari con grande moneta soleggiata).
- **2, 3, 4, 5, 6, 7:** Disposizione simmetrica dei simboli secondo la tradizione napoletana (es. spade che si incrociano, bastoni incrociati a X).
- **Settebello (7 di Denari):** La carta più prestigiosa della Scopa; la moneta centrale deve essere chiaramente distinguibile, più grande e dorata con raggi solari evidenti.

### 4.2. Figure (8, 9, 10 per ogni seme)
A differenza del mazzo di poker a due teste, le carte napoletane hanno **figure intere**:
- **Fante (8 / F):** Giovane cortigiano/fante in piedi con abito d'epoca rinascimentale italiana, che impugna il proprio seme.
- **Cavallo / Cavaliere (9 / C):** Nobile cavaliere a cavallo con gualdrappa coordinata ai colori del seme.
- **Re (10 / R):** Sovrano seduto in trono o in piedi con corona, manto reale e lo scettro/seme tenuto con dignità.

### 4.3. Dorso della Carta (`card_back`)
- **Colore:** Fondo blu reale intenso (`#1E3A8A`) o bordeaux scuro (`#5B1414`).
- **Motivo:** Trama geometrica ornamentale simmetrica a rombi/arabesco con cornice dorata (`#D4AF37`), senza indizi sull'orientamento della carta.

---

## 5. L'Avatar del Maestro

Il "Maestro" è il mentore AI dell'app: un saggio appassionato di carte della tradizione italiana.
- **Aspetto:** Uomo maturo (60–65 anni), sguardo acuto e benevolo, barba e baffi curati brizzolati, gilet elegante e camicia con colletto morbido.
- **Formato master:** `512 × 512 px` (trasparente o con cornice a medaglione dorato circolare).
- **5 Espressioni chiave richieste:**
  1. `maestro_neutral`: Sguardo calmo, accogliente, sorriso accennato.
  2. `maestro_thinking`: Mano al mento o sopracciglio alzato, sguardo concentrato sul tavolo.
  3. `maestro_pleased`: Sorriso caloroso e cenni di approvazione (giocata ottima dell'allievo).
  4. `maestro_warning`: Espressione ammonitrice ma paterna, occhiata attenta (allievo ha commesso un errore o lasciato scopa).
  5. `maestro_celebrating`: Espressione entusiasta, occhi luminosi, applauso o brindisi con calice (scopa o fine partita vinta).

---

## 6. Prompt Framework per Generazione AI

Se generi le immagini con strumenti come Midjourney, Flux, DALL-E 3 o Google ImageFX, ecco una struttura di prompt pronta all'uso con stile uniforme:

### Prompt Base per le Carte Napoletane:
```text
Italian Neapolitan playing card illustration, [CARD_NAME], [SUIT], historical Italian Renaissance style, clean crisp lines, elegant vintage warm paper parchment background, subtle golden filigree border, symmetrical composition, full-body court figure, woodcut and watercolor texture, high contrast, mobile game asset, flat white background outside the card, 8k resolution, vector art feel --ar 1:1.56 --style raw
```

#### Esempi specifici:
- **Settebello (7 di Denari):**
  ```text
  Italian Neapolitan playing card illustration, Seven of Coins (Settebello), seven ornate golden coins arranged with a radiant glowing central sun coin, historical Italian renaissance style, warm parchment texture, delicate golden filigree borders, clear indices "7" in corners, mobile game card asset --ar 1:1.56
  ```
- **Re di Spade:**
  ```text
  Italian Neapolitan playing card illustration, King of Swords, full length regal king holding a decorated curved sword, Renaissance velvet royal garments, crown, stern dignified expression, warm antique paper texture, golden filigree frame, clear indices "R" and sword icon in corners --ar 1:1.56
  ```
- **Dorso della Carta:**
  ```text
  Playing card back design, ornate Italian renaissance pattern, symmetrical geometric diamond and floral filigree, royal navy blue and burnished gold color palette, elegant border, no central focal orientation, luxury card deck back --ar 1:1.56
  ```
- **Maestro (Avatar neutro):**
  ```text
  Character portrait of an Italian chess and scopa card game master, distinguished 60-year-old gentleman with neat silver beard, warm intelligent friendly eyes, wearing a dark green velvet waistcoat and white linen shirt, vintage Italian salon ambiance, painterly digital portrait style, circular frame with gold trim, transparent background --ar 1:1
  ```
