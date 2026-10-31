#!/usr/bin/env python3
"""Prepara le due immagini della sedia dell'Olimpo (task F1G-22) per res/drawable-nodpi.

Le sorgenti sono sedie su fondo verde chroma-key con un alone verde luminoso attorno ai bordi.
Per ciascuna: toglie il verde (matte morbido sulla dominanza del canale verde), sopprime l'alone sui bordi
(despill: nella fascia vicina alla trasparenza il verde non supera rosso/blu), ritaglia alla sedia e porta
tutte le immagini sulla STESSA tela e sulla STESSA scala (altezza della sedia normalizzata, centrata sul bbox
della prima immagine), cosi' la sedia occupata si sovrappone alla vuota a parte gli oggetti sul sedile.

Uso:
  scripts/prepare_chairs.py <vuota_src> <occupata_src> <cartella_out> [--height 760] [--margin 20] [--quality 85]
Esempio:
  scripts/prepare_chairs.py story_chair_empty_src.webp story_chair_taken_src.jpg \
      app/android/src/main/res/drawable-nodpi
Scrive story_chair_empty.webp e story_chair_taken.webp (WebP con trasparenza).
Richiede Pillow e numpy (nessuna dipendenza del progetto Gradle).
"""
import argparse
from pathlib import Path

import numpy as np
from PIL import Image, ImageFilter


def matte(img):
    """RGBA senza fondo: alpha dalla dominanza del verde, poi despill dei bordi."""
    a = np.asarray(img.convert("RGB")).astype(float)
    r, g, b = a[..., 0], a[..., 1], a[..., 2]
    # Il fondo e l'alone sono verdi MOLTO luminosi (g ~ 230, il rosso sale solo verso il giallo); il panno verde
    # della sedia (g <= ~160) e il legno/ottone (rosso >= verde) restano opachi.
    bg = np.clip((g - 175) / 40, 0, 1) * np.clip((g - np.maximum(r, b) - 15) / 25, 0, 1)
    alpha_img = Image.fromarray(((1 - bg) * 255).astype("uint8"))
    alpha_img = alpha_img.filter(ImageFilter.MinFilter(3))  # mangia 1 px di alone
    alpha_img = alpha_img.filter(ImageFilter.GaussianBlur(0.7))  # bordo morbido
    alpha = np.asarray(alpha_img).astype(float) / 255
    # fascia di bordo: vicino alla trasparenza il verde non puo' superare rosso/blu
    clear = Image.fromarray(np.where(alpha < 0.98, 255, 0).astype("uint8"))
    band = np.asarray(clear.filter(ImageFilter.MaxFilter(9))) > 0
    limit = np.maximum(r, b) + 4
    a[..., 1] = np.where(band, np.minimum(g, limit), g)
    out = Image.fromarray(a.astype("uint8")).convert("RGBA")
    out.putalpha(Image.fromarray((alpha * 255).astype("uint8")))
    return out


def bbox(rgba):
    return Image.fromarray(np.where(np.asarray(rgba.getchannel("A")) > 128, 255, 0).astype("uint8")).getbbox()


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("empty")
    p.add_argument("taken")
    p.add_argument("out")
    p.add_argument("--height", type=int, default=760, help="altezza della sedia in px")
    p.add_argument("--margin", type=int, default=20)
    p.add_argument("--quality", type=int, default=85)
    args = p.parse_args()

    keyed = [matte(Image.open(args.empty)), matte(Image.open(args.taken))]
    boxes = [bbox(k) for k in keyed]
    scales = [args.height / (bx[3] - bx[1]) for bx in boxes]
    widths = [round((bx[2] - bx[0]) * s) for bx, s in zip(boxes, scales)]
    cw = max(widths) + 2 * args.margin
    ch = args.height + 2 * args.margin
    out = Path(args.out)
    out.mkdir(parents=True, exist_ok=True)
    for name, k, bx, s in zip(("story_chair_empty", "story_chair_taken"), keyed, boxes, scales):
        chair = k.crop(bx)
        chair = chair.resize((round(chair.width * s), args.height), Image.LANCZOS)
        canvas = Image.new("RGBA", (cw, ch), (0, 0, 0, 0))
        canvas.paste(chair, ((cw - chair.width) // 2, args.margin))  # centrata sul piede (bbox), stesso fondo
        path = out / f"{name}.webp"
        canvas.save(path, "WEBP", quality=args.quality, method=6, exact=True)
        print(f"{path}: {cw}x{ch}, {path.stat().st_size // 1024} KB")


if __name__ == "__main__":
    main()
