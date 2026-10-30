#!/usr/bin/env python3
"""Ritaglia le carte singole da un foglio di scansione di un mazzo regionale (griglia 4 righe x 10 colonne).

Serve alle schede fonte di docs/art/regionali/ (task F1G-10): non aggiunge nulla all'app.
Solo Python con Pillow. Divide il foglio in una griglia regolare, trova in ogni cella il riquadro
della carta (filetto scuro su fondo bianco), ritaglia con un margine interno opzionale e salva i file come NN_<seme>_<valore>.png.

Uso:
  scripts/crop_sheet.py FOGLIO.jpg CARTELLA_USCITA [--rows seme1,seme2,seme3,seme4] [--inset N] [--inset-x N] [--edge N] [--dark-bg] [--equal]

`--rows` dice i semi nell'ordine in cui compaiono nelle righe del foglio; l'uscita e' sempre
numerata nell'ordine del progetto (denari, coppe, spade, bastoni; Asso, 2..7, Fante, Cavallo, Re).
Il foglio va scaricato fuori dal repository e lo script non deve essere lanciato dalla cartella dei download.
"""
import argparse
import sys
from pathlib import Path

from PIL import Image, ImageChops

SUITS = ["denari", "coppe", "spade", "bastoni"]
RANKS = ["asso", "2", "3", "4", "5", "6", "7", "fante", "cavallo", "re"]
EDGE = 20  # pixel di bordo del foglio da ignorare nel cercare la griglia
SEARCH = 60  # semi-ampiezza (px) della ricerca del confine fra due colonne
DARK_THRESHOLD = 90  # con sfondo scuro: canale piu' luminoso sopra questo valore = carta
THRESHOLD = 200  # pixel piu' scuri di questo = inchiostro (carta e sfondo del foglio sono bianchi)


def find_cards(sheet, cols=10, rows=4, edge=EDGE, dark_bg=False, equal=False):
    """Restituisce i bounding box (x0, y0, x1, y1) delle carte, per riga poi per colonna.

    Il foglio e' una griglia quasi regolare, con scarti di qualche pixel e carte quasi a contatto.
    Per ogni riga si guarda la fascia centrale della cella; ogni confine fra due colonne e' la colonna
    con meno inchiostro vicino alla posizione nominale (passo regolare +/- SEARCH px). Dentro ogni
    colonna si prende il riquadro stretto dell'inchiostro.
    """
    # sfondo chiaro: inchiostro = pixel scuri; sfondo scuro (foto su un tavolo): inchiostro = tutto cio' che
    # non e' sfondo, cioe' il canale piu' luminoso supera DARK_THRESHOLD (i colori scuri delle carte restano carta)
    if dark_bg:
        r, g, b = sheet.convert("RGB").split()
        ink = ImageChops.lighter(ImageChops.lighter(r, g), b).point(lambda v: 255 if v > DARK_THRESHOLD else 0)
    else:
        ink = sheet.convert("L").point(lambda v: 255 if v < THRESHOLD else 0)
    # i bordi del foglio scansionato (strisce scure di pochi pixel) non sono carte
    whole = ink.crop((edge, edge, ink.width - edge, ink.height - edge)).getbbox()
    if whole is None:
        sys.exit("foglio vuoto")
    gx0, gy0, gx1, gy1 = whole[0] + edge, whole[1] + edge, whole[2] + edge, whole[3] + edge
    pitch, ch = (gx1 - gx0) / cols, (gy1 - gy0) / rows
    boxes = []
    for r in range(rows):
        cy = gy0 + (r + 0.5) * ch
        top, bot = round(cy - 0.5 * ch), round(cy + 0.5 * ch)
        band = ink.crop((0, round(cy - 0.4 * ch), ink.width, round(cy + 0.4 * ch)))
        prof = band.resize((band.width, 1), Image.BOX).tobytes()  # per colonna: 255 * frazione di inchiostro
        if equal:
            # carte a contatto (foto): niente confini da cercare; ogni riga si divide in parti uguali fra
            # il primo e l'ultimo pixel di carta della fascia, e i riquadri sono le celle nominali
            bx = band.getbbox()
            step = (bx[2] - bx[0]) / cols
            for c in range(cols):
                boxes.append((round(bx[0] + c * step), round(gy0 + r * ch), round(bx[0] + (c + 1) * step), round(gy0 + (r + 1) * ch)))
            continue
        cuts = [gx0 - 1]
        for k in range(1, cols):
            nominal = round(gx0 + k * pitch)
            window = range(nominal - SEARCH, nominal + SEARCH)
            cuts.append(min(window, key=lambda x: (sum(prof[x - 2:x + 3]), abs(x - nominal))))
        cuts.append(gx1 + 1)
        for c in range(cols):
            x0, x1 = cuts[c], cuts[c + 1]
            bx = ink.crop((x0, top, x1, bot)).getbbox()
            if bx is None:
                sys.exit(f"cella vuota: riga {r + 1}, colonna {c + 1}")
            boxes.append((x0 + bx[0], top + bx[1], x0 + bx[2], top + bx[3]))
    return boxes


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("sheet")
    ap.add_argument("out")
    ap.add_argument("--rows", default="denari,coppe,bastoni,spade")
    ap.add_argument("--edge", type=int, default=EDGE, help="pixel di bordo del foglio da ignorare")
    ap.add_argument("--dark-bg", action="store_true", help="foglio fotografato su sfondo scuro")
    ap.add_argument("--equal", action="store_true", help="carte a contatto: celle uguali per riga, senza ricerca dei confini")
    ap.add_argument("--inset", type=int, default=0, help="pixel da togliere su ogni lato del riquadro")
    ap.add_argument("--inset-x", type=int, default=None, help="come --inset ma solo a sinistra e a destra")
    a = ap.parse_args()
    row_suits = a.rows.split(",")
    if sorted(row_suits) != sorted(SUITS):
        sys.exit("--rows deve elencare i quattro semi")
    sheet = Image.open(a.sheet).convert("RGB")
    boxes = find_cards(sheet, edge=a.edge, dark_bg=a.dark_bg, equal=a.equal)
    out = Path(a.out)
    out.mkdir(parents=True, exist_ok=True)
    for idx, (x0, y0, x1, y1) in enumerate(boxes):
        suit, rank = row_suits[idx // 10], RANKS[idx % 10]
        n = SUITS.index(suit) * 10 + (idx % 10) + 1
        i, ix = a.inset, a.inset if a.inset_x is None else a.inset_x
        sheet.crop((x0 + ix, y0 + i, x1 - ix, y1 - i)).save(out / f"{n:02d}_{suit}_{rank}.png")
    print(f"ritagliate {len(boxes)} carte in {out}")


if __name__ == "__main__":
    main()
