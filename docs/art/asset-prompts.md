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
