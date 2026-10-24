#!/usr/bin/env python3
"""Prepara il mazzo napoletano in pubblico dominio (Wikimedia Commons, autore Trocche100, PD-user-it).

Ogni disegno viene messo su un cartoncino bianco con angoli arrotondati e filetto scuro,
600x936 px, e salvato come card_<seme>_<valore>.webp (i file 8, 9 e 10 sono Fante, Cavallo, Re).

  scripts/prepare_napoletane.py docs/art/generated/napoletane_pd app/android/src/main/res/drawable-nodpi
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw

W, H, RADIUS, MARGIN = 600, 936, 36, 34
SUITS = ["denari", "coppe", "spade", "bastoni"]
RANKS = ["asso", "2", "3", "4", "5", "6", "7", "fante", "cavallo", "re"]


def card(art_path):
    art = Image.open(art_path).convert("RGB")
    art.thumbnail((W - 2 * MARGIN, H - 2 * MARGIN), Image.LANCZOS)
    big = Image.new("RGBA", (W * 2, H * 2), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    d.rounded_rectangle((0, 0, W * 2 - 1, H * 2 - 1), RADIUS * 2, fill="#2B1A10")
    d.rounded_rectangle((5, 5, W * 2 - 6, H * 2 - 6), RADIUS * 2 - 5, fill="#FFFFFF")
    out = big.resize((W, H), Image.LANCZOS)
    out.paste(art, ((W - art.width) // 2, (H - art.height) // 2))
    return out


def main():
    src, dst = Path(sys.argv[1]), Path(sys.argv[2])
    files = sorted((p for p in src.glob("*.jpg") if p.name[0].isdigit()), key=lambda p: int(p.name.split("_")[0]))
    if len(files) != 40:
        sys.exit(f"attese 40 carte, trovate {len(files)}")
    for i, f in enumerate(files):
        name = f"card_{SUITS[i // 10]}_{RANKS[i % 10]}"
        card(f).save(dst / f"{name}.webp", "WEBP", quality=88, method=6)
        print(f.name, "->", name)
    back = src / "Carte_Napoletane_retro.jpg"
    if back.exists():
        card(back).save(dst / "card_back.webp", "WEBP", quality=88, method=6)
        print(back.name, "-> card_back")


if __name__ == "__main__":
    main()
