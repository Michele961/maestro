# Guida di Stile Visivo e UI — Maestro

**Versione:** 1.0  
**Data:** 2026-10-24  
**Approvato da:** Michele  
**Riferimento grafico ufficiale:** [`style_guide_sheet.jpg`](style_guide_sheet.jpg)  
**Target:** Mobile (Android / iOS)  
**Atmosfera Chiave:** Circolo italiano tradizionale, artigianale, caldo e accogliente, con finiture tattili e contrasto netto per la massima leggibilità.

---

## 1. Filosofia Visiva e Direzione Artistica

- **No Sci-Fi / No Cyberpunk:** Escluso qualsiasi elemento olografico, neon blu elettrico, microchip, circuiti stampati o menzioni esplicite a "AI".
- **Personificazione Naturale:** Il coach è semplicemente **"Il Maestro"**, un anziano signore autorevole ma caloroso, tipico frequentatore e maestro del bar/circolo.
- **Autenticità Materica:** Superfici in noce scuro/rovere, panno verde bottiglia da tavolo da gioco, cornici in ottone/oro caldo e carte stampate su cartoncino bianco puro.
- **Profondità Tattile (Skeuomorphic Leggero):** Pulsanti arrotondati, pillole con rilievi morbidi, ombre soffuse e riflessi satinati per rendere l'interfaccia appetibile al tocco.

---

## 2. Palette Colori

### Tavolo e Sfondi
- **Panno da Gioco (Verde Tavolo):** `#1E4D2B` a `#14381E` (sfumatura radiale morbida con texture tessuto sottile).
- **Superficie Legno (Cornici & Bordi):** `#3B2213` (legno scuro) con riflessi caldi `#5A3821`.
- **Fondo Modali e Box Testo:** `#FDFBF7` (carta calda/pergamena chiarissima) con bordi dorati o legno.

### Pulsanti Principali
- **Verde Azione Principale (Gioca / Classificata):** `#2E8B57` / `#1F683D`, bordo dorato `#D4AF37`.
- **Blu Funzionalità Didattica (Palestra / Tutorial):** `#1E5A8A` / `#164366`, riflesso superiore chiaro.
- **Ambra / Legno Chiaro (Azioni Secondarie / Nuova Partita):** `#A05A2C` / `#7D3F19`.

### Metriche e Segnalazioni (Game Review)
- **Bollino Mossa Ottimale (Magistrale):** `#1E88E5` (Blu cobalto).
- **Bollino Mossa Naturale/Standard:** `#43A047` (Verde smeraldo).
- **Bollino Imprecisione:** `#FBC02D` (Giallo zafferano).
- **Bollino Errore Grave:** `#D32F2F` (Rosso rubino).

---

## 3. Specifiche degli Asset

### Le Carte da Gioco
- **Sfondo della Carta:** Bianco puro (`#FFFFFF`) o cartoncino tipografico quasi impercettibilmente caldo (`#FBFBFB`). Nessuna dominante giallastra o effetto invecchiato artificiale.
- **Stile Illustrativo:** Carte Napoletane fedeli all'iconografia classica (Bastoni fioriti con tronco tagliato, Coppe cerimoniali, Spade ricurve incrociate, Denari/Soli decorati).
- **Tratto:** Linea di contorno nera/marrone scuro netta, campiture piene e vivaci (rosso vivo, verde smeraldo, giallo oro).
- **Leggibilità:** Angoli con indici chiari (valore e miniatura seme) per consentire la lettura immediata a ventaglio su schermi da 5" a 6.7".
- **Dorso Carta:** Verde bottiglia geometrico con cornice dorata e monogramma "M" centrale in stile classico.

### Il Maestro (Avatar & Feedback)
- **Ritratto:** Mezzo busto dentro una cornice circolare dorata con texture in ottone/legno. Gilet verde bosco, camicia chiara, barba e capelli bianchi corti, espressione saggia e sorridente.
- **Varianti Espressione:**
  - *Sorriso incoraggiante* (apertura partita, mossa corretta, vittoria).
  - *Mano al mento / Pensieroso* (momento critico, check a metà partita, imprecisione).
  - *Espressione neutra / Guida* (spiegazione didattica, replay mani).
- **Box di Dialogo:** Fumetto pulito su fondo avorio/panna (`#FDFBF7`), bordo sottile ottone, testo scuro ad alto contrasto (`#1A1A1A`). Header del fumetto sempre titolato **"Il Maestro"**.

### Componenti UI & HUD
- **Pannello Punteggio:** Targhetta orizzontale in legno/metallo divisa a scomparti: `"Noi [7]" | "Voi [3]"`. Contrasto chiaro sui blocchi punteggio (es. blu per il giocatore, rosso per l'avversario).
- **Bottega e Crediti:**
  - Indicatore crediti sempre visibile in alto a destra: icona moneta d'oro massiccia (`#FFD700`) + contatore numerico.
  - Asset negozio: Moneta singola, Sacchetto di cuoio chiuso da laccio con monete debordanti (150 Crediti), Scrigno di legno e ottone colmo di monete e pergamene (500 Crediti).
- **Barra di Navigazione Inferiore:** 4 o 5 tab minimali in finitura ottone opaco: Lobby, Il Maestro, Classifiche, Bottega.

---

## 4. Prompt Base per la Generazione degli Asset (Midjourney / Stable Diffusion)

### Per le Schermate / Mockup UI:
```text
Mobile game UI design of traditional Italian card game Briscola and Scopa, warm cozy Italian social club tavern atmosphere, dark rich polished wood table, bottle green felt background, 2D tactile glossy buttons with golden brass borders, clean white Italian playing cards, natural elderly Italian mentor avatar with warm smile in classic brass frame, elegant gold score plaques, sharp contrast, professional mobile game interface, portrait 9:16 --no sci-fi, neon, hologram, futuristic, cyber
```

### Per l'Avatar del Maestro:
```text
2D character avatar of an elderly wise Italian card game coach named Maestro, friendly warm smile, short neat white hair and trimmed beard, wearing a classic dark green vest and linen shirt, inside an ornate circular polished gold frame, warm tavern lighting, detailed digital painting, cozy game asset, isolated dark background --no robotic, cyber, neon, blue glow, hologram
```

### Per i Pacchetti della Bottega:
```text
Set of mobile game shop assets, cozy tavern style, 2D digital art with soft 3D shading: 1) Single solid gold antique coin embossed with playing card symbol. 2) Small rustic brown leather pouch overflowing with gold coins. 3) Sturdy dark wood treasure chest bound in brass filled with shining gold coins and vintage strategy scrolls. High resolution, isolated on black background --no futuristic, sci-fi, neon
```
