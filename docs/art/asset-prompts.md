# Prompt per gli asset — Maestro

Prompt da usare in Gemini, **nella stessa conversazione dei mockup e delle icone già approvate**, così lo stile resta coerente. Dopo la generazione: `scripts/prepare_assets.py` (vedi [`asset-catalog.md`](asset-catalog.md)).

## Regole comuni

- Più oggetti per immagine vanno bene, purché **separati e non a contatto**.
- **Fondo piatto:** verde `#00FF00`; se l'oggetto contiene verde (pulsanti verdi, panno) usare **magenta `#FF00FF`** e preparare con `--key magenta`.
- **Niente testo**, niente scritte, niente icone dentro pulsanti e pannelli: i testi li mette il codice.
- **Vista frontale piatta**, senza prospettiva né inclinazione.

## Pelli a 9 sezioni (pulsanti, pannelli, targhe)

Il codice stira solo la parte centrale: angoli e bordi restano intatti. Per questo il centro deve essere **uniforme** e le decorazioni (rivetti, angolari) **solo negli angoli**.

### Set 1 — Pulsanti (fondo magenta)

```
In the same painted style as the mockups above, on a plain flat magenta background (#FF00FF),
draw 6 separate empty game buttons, not touching, each a wide horizontal rounded rectangle
(about 3:1), front view, no perspective, no text, no icons:
1) glossy red button with a thick polished gold rim and one small brass rivet in each corner;
2) glossy green pill button with a gold rim;
3) glossy blue pill button with a gold rim;
4) cream/beige pill button with a gold rim (unselected option);
5) amber/orange pill button with a gold rim;
6) dark walnut wood pill button with a thin gold rim.
Soft top highlight and subtle bottom shadow inside each button. The middle of each button
must be plain and uniform (no decorations), so it can be stretched horizontally.
```

### Set 2 — Pannelli e targhe (fondo verde)

```
In the same painted style, on a plain flat bright green background (#00FF00), draw 5 separate
empty UI frames, not touching, front view, no text:
1) a parchment panel (portrait 3:4) with a thick polished brass frame and ornate brass corner
   brackets only in the 4 corners;
2) a dark walnut wood plaque (wide 4:1) with a brass border and one brass rivet in each corner;
3) a thin dark wood bar (wide 8:1) with a thin brass border;
4) a horizontal brown ribbon band (wide 6:1) fading to transparent at both ends;
5) a round brass-rimmed close button with a white X inside.
The inner area of frames 1, 2 and 3 must be plain and uniform so it can be stretched.
```

### Set 3 — Tavolo (fondo magenta)

```
In the same painted style, on a plain flat magenta background (#FF00FF), draw 4 separate
objects, not touching, front view, no text:
1) a square dark wood button with a brass rim and a cream pause symbol (two bars);
2) a stack of face-down playing cards seen from the front, bottle-green backs with a gold coin
   in the center, about 6 cards slightly offset downward;
3) an oval bottle-green felt base with a thin brass rim and small gold curl decorations at the
   sides (the pedestal under the main play button);
4) a small hanging Italian tricolor cloth banner on a wooden rod.
```

## Set Avversari (task F1R-18d)

Cinque avversari del circolo, dal più debole al più forte (livelli del motore: Principiante, Dilettante, Intermedio, Avanzato, Maestro). **Nomi e titoli sono proposte**: cambiali pure prima di generare, ma il titolo non compare nell'immagine (niente testo), lo mette il codice.

| # | Livello | Nome (proposta) | Titolo (proposta) | Carattere |
| --- | --- | --- | --- | --- |
| 1 | Principiante | Gigi | «L'ultimo arrivato» | Ragazzo imbranato, entusiasta, berretto di lana |
| 2 | Dilettante | Nonna Rosa | «Campionessa del pianerottolo» | Signora allegra, scialle, occhiali sul naso |
| 3 | Intermedio | Tonino | «Il migliore del bar» | Cinquantenne sicuro di sé, baffi, camicia a maniche rimboccate |
| 4 | Avanzato | Don Alfonso | «Il terrore del circolo» | Signore elegante e severo, coppola, sigaro spento |
| 5 | Maestro | Il Maestro | «Il Maestro» | Il coach (vedi `style-guide.md` §3), qui nella veste di sfidante finale |

### Regole specifiche

- **Stessa conversazione** dei mockup approvati e dell'avatar del Maestro (`maestro_avatar_v1.jpg`): stesso pennello, stessa cornice.
- Ritratto = mezzo busto in **cornice circolare in ottone**, sguardo verso chi guarda, luce calda di taverna. Il fondo fuori dalla cornice è piatto: verde `#00FF00` (se usi il verde nel personaggio, magenta `#FF00FF`).
- Niente testo, niente scritte sulle cornici e sugli stemmi.
- Un'immagine per set, oggetti **separati e non a contatto**. Se la coerenza dei volti cala, genera un personaggio per immagine ripartendo dalla riga del personaggio.

### Set 4 — Ritratti, espressione neutra (fondo verde)

```
In the same painted style as the Maestro avatar above, on a plain flat bright green background
(#00FF00), draw 5 separate round portrait avatars, not touching, each a half-length bust of a
different Italian card-club character inside an ornate circular polished brass frame, warm tavern
lighting, friendly caricature-leaning style, front view, no text, no letters:
1) Gigi: a clumsy enthusiastic young man in his early 20s, wool beanie, wide eager smile, slightly
   oversized sweater;
2) Nonna Rosa: a cheerful elderly woman with grey bun, shawl and glasses low on her nose, mischievous smile;
3) Tonino: a confident man in his 50s with a thick moustache, shirt with rolled-up sleeves, smug half-smile;
4) Don Alfonso: an elegant stern older gentleman, flat cap (coppola), unlit cigar in the corner of
   his mouth, one raised eyebrow;
5) the Maestro: the wise elderly coach, short white hair and trimmed beard, dark green vest over a
   linen shirt, calm warm smile.
All five faces must clearly differ in age, hair and expression. No robots, no neon, no sci-fi.
```

### Set 5 — Ritratti, vittoria e sconfitta (fondo verde)

Uno per personaggio (5 immagini), ripartendo dal ritratto neutro dello stesso personaggio.

```
Same character as in the previous image (<NOME>), same circular brass frame, same style, on a
plain flat bright green background (#00FF00). Draw 2 separate round portraits, not touching:
1) the character delighted after winning the game, triumphant grin, fist pumped;
2) the character defeated after losing the game, rueful smile, hand on forehead.
No text.
```

### Set 6 — Stemmi (fondo magenta)

Uno stemma per avversario, mostrato accanto al ritratto sulla scala. Medaglione di ottone con un emblema, **senza lettere**.

```
In the same painted style, on a plain flat magenta background (#FF00FF), draw 5 separate round
brass crest medallions, not touching, front view, no text, no letters, each with a different
emblem embossed in the center:
1) a small wool beanie over a single playing card;
2) a ball of yarn with knitting needles crossed over a cup;
3) a coffee cup and a deck of cards on a bar counter;
4) a flat cap and an unlit cigar crossed over a sword;
5) a laurel wreath around a gold coin (the Settebello Denari symbol).
Increasing richness: crest 1 is plain bronze, crest 5 is ornate gold with a red ribbon.
```

### Set 7 — Stato bloccato (fondo verde)

Il codice oscura il ritratto per l'avversario bloccato; serve solo il lucchetto.

```
In the same painted style, on a plain flat bright green background (#00FF00), draw 2 separate
objects, not touching: 1) a heavy brass padlock, closed, with a small dark wood backing disk;
2) the same padlock, open. No text.
```

### Dopo la generazione

Nomi di file attesi (`docs/art/asset-catalog.md` va aggiornato dal task F1R-18d):
`opponent_1..5_neutral|win|lose`, `crest_1..5`, `lock_closed`, `lock_open`. Passare i ritagli da `scripts/prepare_assets.py` come per gli altri asset.

### Set 5 bis — Le due espressioni mancanti (fondo verde) — fatto il 2026-10-28

Dal primo giro mancano `opponent_5_win` (il Maestro che vince: il foglio mostrava un ritratto senza cornice, con la pelle verde) e `opponent_3_lose` (Tonino che perde: il foglio ne aveva uno sorridente). Stessa conversazione, ripartendo dai ritratti neutri.

```
Same style, same circular brass frame, plain flat bright green background (#00FF00), no text.
Draw 2 separate round portraits, not touching, each inside the complete circular brass frame:
1) the Maestro (elderly man, short white hair and trimmed beard, dark green vest over a linen shirt)
   delighted after winning, warm triumphant smile, fist gently raised;
2) Tonino (man in his 50s, thick moustache, grey shirt with rolled-up sleeves) defeated after losing,
   rueful smile, one hand on his forehead.
Keep the skin tones natural and warm (no green tint on the faces).
```
