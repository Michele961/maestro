#!/usr/bin/env python3
"""Prepara gli asset generati con AI per res/drawable-nodpi (task F1-13).

Due modalità:

  sheet    Immagine con uno o più oggetti su fondo verde piatto (#00FF00),
           oppure magenta (#FF00FF) con --key magenta se gli oggetti contengono verde.
           Rimuove il verde, separa gli oggetti per forma (non per griglia),
           li ordina per righe e colonne e li salva come WebP con trasparenza.
  texture  Immagine di sfondo: ridimensiona e salva come WebP, senza trasparenza.

Esempi:
  scripts/prepare_assets.py sheet docs/art/generated/menu_icons.jpg out/ \
      icon_trophy icon_book_quill icon_pouch icon_coin ...
  scripts/prepare_assets.py sheet ornaments.jpg out/ ornament_top ornament_bottom ornament_corner --merge 60
  scripts/prepare_assets.py texture wood.jpg out/ texture_wood --size 1024
  scripts/prepare_assets.py texture bg.jpg out/ story_bg_bar_table --size 2000 --quality 80

Un nome "-" scarta l'oggetto in quella posizione.
Richiede Pillow e numpy.
"""
import argparse
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter


def chroma_key(img, key="green"):
    """Restituisce l'immagine RGBA senza il fondo (verde o magenta) e con i bordi senza alone."""
    a = np.asarray(img.convert("RGB")).astype(float)
    r, g, b = a[..., 0], a[..., 1], a[..., 2]
    if key == "magenta":
        # Per gli oggetti che contengono verde (panno, pulsanti verdi): fondo #FF00FF.
        bg = np.clip((np.minimum(r, b) - g - 40) / 60, 0, 1)
        # despill solo sui bordi semitrasparenti: rossi e blu pieni restano intatti
        edge = bg > 0
        a[..., 0] = np.where(edge, np.minimum(r, g + 60), r)
        a[..., 2] = np.where(edge, np.minimum(b, g + 60), b)
    else:
        bg = np.clip((g - np.maximum(r, b) - 40) / 60, 0, 1)
        a[..., 1] = np.minimum(g, np.maximum(r, b) + 10)  # despill
    alpha = ((1 - bg) * 255).astype("uint8")
    out = Image.fromarray(a.astype("uint8")).convert("RGBA")
    out.putalpha(Image.fromarray(alpha).filter(ImageFilter.MinFilter(3)))
    return out


def components(alpha, merge, min_area):
    """Etichetta le zone opache; i pezzi più vicini di `merge` px diventano un solo oggetto."""
    # Etichettatura su una copia ridotta di 4 volte: stesso risultato, molto più veloce.
    scale = 4
    full = alpha.size
    solid = Image.fromarray(np.where(np.asarray(alpha) > 40, 255, 0).astype("uint8"))
    solid = solid.resize((full[0] // scale, full[1] // scale), Image.BOX).point(lambda v: 255 if v > 0 else 0)
    k = merge // scale
    grown = solid.filter(ImageFilter.MaxFilter(2 * k + 1)) if k else solid
    labels = grown.copy()
    next_id = 1
    ys, xs = np.nonzero(np.asarray(grown) == 255)
    for y, x in zip(ys[::5], xs[::5]):
        if labels.getpixel((int(x), int(y))) == 255:
            if next_id == 255:
                sys.exit("troppi oggetti nell'immagine")
            ImageDraw.floodfill(labels, (int(x), int(y)), next_id)
            next_id += 1
    lab = np.asarray(labels.resize(full, Image.NEAREST))
    lab = np.where(np.asarray(alpha) > 0, lab, 0)
    found = []
    for i in range(1, next_id):
        ys, xs = np.nonzero(lab == i)
        if len(ys) >= min_area:
            found.append((ys.min(), ys.max(), xs.mean(), lab == i, full[0]))
    return found


def reading_order(found):
    """Ordina per righe (oggetti che si sovrappongono in verticale) e poi da sinistra a destra."""
    found = sorted(found, key=lambda c: c[0])
    rows, row, bottom = [], [], -1
    for c in found:
        if row and c[0] > bottom:
            rows.append(row)
            row = []
        row.append(c)
        bottom = max(bottom, c[1])
    rows.append(row)
    # Nella riga: da sinistra a destra per colonne (1/8 della larghezza), poi dall'alto.
    return [c for r in rows for c in sorted(r, key=lambda c: (int(c[2] // (c[4] / 8)), c[0]))]


def sheet(args):
    rgba = chroma_key(Image.open(args.input), args.key)
    found = reading_order(components(rgba.getchannel("A"), args.merge, args.min_area))
    if len(found) != len(args.names):
        sys.exit(f"trovati {len(found)} oggetti, ma ci sono {len(args.names)} nomi")
    for name, (_, _, _, mask, _) in zip(args.names, found):
        if name == "-":
            continue
        a = np.asarray(rgba).copy()
        a[..., 3] = np.where(mask, a[..., 3], 0)
        obj = Image.fromarray(a)
        obj = obj.crop(obj.getchannel("A").point(lambda v: 255 if v > 40 else 0).getbbox())
        obj.thumbnail((args.size, args.size), Image.LANCZOS)
        save(obj, args.out, name)


def texture(args):
    if len(args.names) != 1:
        sys.exit("texture: serve un solo nome")
    img = Image.open(args.input).convert("RGB")
    img.thumbnail((args.size, args.size), Image.LANCZOS)
    save(img, args.out, args.names[0], args.quality)


def save(img, out, name, quality=90):
    path = Path(out) / f"{name}.webp"
    img.save(path, "WEBP", quality=quality, method=6)
    print(f"{path} {img.size[0]}x{img.size[1]}")


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("mode", choices=["sheet", "texture"])
    p.add_argument("input")
    p.add_argument("out")
    p.add_argument("names", nargs="+", help="nomi dei file in ordine di lettura (- per scartare)")
    p.add_argument("--size", type=int, default=512, help="lato massimo in px (default 512)")
    p.add_argument("--quality", type=int, default=90, help="qualità WebP in modalità texture (default 90)")
    p.add_argument("--merge", type=int, default=2, help="unisce pezzi distanti meno di N px (default 2)")
    p.add_argument("--key", choices=["green", "magenta"], default="green", help="colore del fondo da togliere")
    p.add_argument("--min-area", type=int, default=5000, help="ignora frammenti più piccoli (px)")
    args = p.parse_args()
    Path(args.out).mkdir(parents=True, exist_ok=True)
    sheet(args) if args.mode == "sheet" else texture(args)


if __name__ == "__main__":
    main()
