#!/usr/bin/env python3
"""Prepara gli asset audio di Maestro (task F1R-20) da una cartella di file grezzi.

Uso: python3 scripts/prepare_audio.py <cartella_grezzi> [cartella_uscita]

- Musica del circolo e ambiente: toglie la dissolvenza finale e chiude il loop con una
  dissolvenza incrociata tra la fine e l'inizio, così il salto non si sente.
- Vittoria e sconfitta: stacchetto dai primi secondi, con attacco e dissolvenza brevi.
- Volumi allineati a un livello comune (LUFS per musica e ambiente, picco per gli effetti):
  il volume vero lo decide poi il mixer (`AudioMixer`).
Richiede ffmpeg (con libvorbis) e numpy.
"""
import re
import subprocess
import sys
from pathlib import Path

import numpy as np

SR = 22050
XFADE = 2.0          # secondi di dissolvenza incrociata per chiudere il loop
STINGER_SECONDS = 20.0
STINGER_FADE_OUT = 2.5
OUT_DEFAULT = Path("app/android/src/main/res/raw")

# Il brusio del bar è lungo: ne basta un tratto stabile (inizio, fine) in secondi.
SLICES = {"ambient_bar": (30.0, 90.0)}

# nome → LUFS integrati di destinazione
LOOPS = {"music_circle_day": -20, "music_circle_evening": -20, "music_tense": -20, "ambient_bar": -30, "ambient_table": -32}
STINGERS = {"music_victory": -20, "music_defeat": -20}
SFX = {"sfx_goal_unlocked": -3.0, "sfx_purchase": -3.0}  # picco in dBFS


def run(*args) -> str:
    done = subprocess.run(args, capture_output=True, text=True)
    if done.returncode != 0:
        sys.exit(f"errore: {' '.join(args)}\n{done.stderr}")
    return done.stderr


def find_input(src: Path, name: str) -> Path:
    found = sorted(p for p in src.iterdir() if p.stem == name)
    if not found:
        sys.exit(f"manca {name}.* in {src}")
    return found[0]


def duration(path: Path) -> float:
    return float(subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", str(path)], capture_output=True, text=True).stdout)


def levels_db(path: Path) -> np.ndarray:
    raw = subprocess.run(["ffmpeg", "-v", "error", "-i", str(path), "-f", "f32le", "-ac", "1", "-ar", str(SR), "-"], capture_output=True).stdout
    x = np.frombuffer(raw, dtype=np.float32)
    w = SR // 10
    return np.array([20 * np.log10(np.sqrt(np.mean(x[i * w:(i + 1) * w] ** 2)) + 1e-9) for i in range(len(x) // w)])


def loudness(path: Path, filter_chain: str) -> float:
    err = run("ffmpeg", "-nostats", "-i", str(path), "-af", f"{filter_chain},ebur128=peak=true", "-f", "null", "-")
    return float(re.findall(r"I:\s+(-?\d+\.\d+) LUFS", err)[-1])


def gain_for(path: Path, filter_chain: str, target: float) -> float:
    return target - loudness(path, filter_chain)


def encode_ogg(src: Path, chain: str, gain: float, dst: Path, mono: bool = False) -> None:
    run("ffmpeg", "-y", "-v", "error", "-i", str(src), "-af", f"{chain},volume={gain:.2f}dB,alimiter=limit=0.89", *(["-ac", "1"] if mono else []), "-c:a", "libvorbis", "-q:a", "4", str(dst))


def make_loop(path: Path, dst: Path) -> None:
    """Taglia la dissolvenza finale e chiude il loop: la fine sfuma nell'inizio e il brano riparte da lì."""
    level = levels_db(path)
    loud = np.where(level > np.max(level) - 15)[0]
    end_s = min((loud[-1] + 1) / 10.0 if len(loud) else duration(path), duration(path))
    raw = subprocess.run(["ffmpeg", "-v", "error", "-i", str(path), "-f", "f32le", "-ac", "2", "-ar", "44100", "-"], capture_output=True).stdout
    x = np.frombuffer(raw, dtype=np.float32).reshape(-1, 2)
    n, e = int(XFADE * 44100), int(end_s * 44100)
    t = np.linspace(0.0, 1.0, n, endpoint=True)[:, None]
    fade_in, fade_out = np.sin(t * np.pi / 2), np.cos(t * np.pi / 2)  # potenza costante
    join = x[e - n:e] * fade_out + x[:n] * fade_in
    out = np.concatenate([x[n:e - n], join]).astype(np.float32)
    subprocess.run(["ffmpeg", "-y", "-v", "error", "-f", "f32le", "-ac", "2", "-ar", "44100", "-i", "-", str(dst)], input=out.tobytes(), check=True)


def main() -> None:
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    src = Path(sys.argv[1])
    out = Path(sys.argv[2]) if len(sys.argv) > 2 else OUT_DEFAULT
    out.mkdir(parents=True, exist_ok=True)
    work = Path("/tmp/prepare_audio")
    work.mkdir(exist_ok=True)

    for name, lufs in LOOPS.items():
        source = find_input(src, name)
        if name in SLICES:
            start, stop = SLICES[name]
            piece = work / f"{name}_slice.wav"
            run("ffmpeg", "-y", "-v", "error", "-i", str(source), "-af", f"atrim={start}:{stop},asetpts=PTS-STARTPTS", str(piece))
            source = piece
        tmp = work / f"{name}.wav"
        make_loop(source, tmp)
        encode_ogg(tmp, "anull", gain_for(tmp, "anull", lufs), out / f"{name}.ogg")
        print(f"{name}: loop, {duration(tmp):.1f} s")

    for name, lufs in STINGERS.items():
        source = find_input(src, name)
        chain = f"atrim=0:{STINGER_SECONDS},asetpts=PTS-STARTPTS,afade=t=in:d=0.05,afade=t=out:st={STINGER_SECONDS - STINGER_FADE_OUT}:d={STINGER_FADE_OUT}"
        tmp = work / f"{name}.wav"
        run("ffmpeg", "-y", "-v", "error", "-i", str(source), "-af", chain, str(tmp))
        encode_ogg(tmp, "anull", gain_for(tmp, "anull", lufs), out / f"{name}.ogg")
        print(f"{name}: stacchetto di {STINGER_SECONDS:.0f} s")

    for name, peak in SFX.items():
        source = find_input(src, name)
        err = run("ffmpeg", "-nostats", "-i", str(source), "-af", "volumedetect", "-f", "null", "-")
        current = float(re.findall(r"max_volume: (-?\d+\.\d+) dB", err)[-1])
        run("ffmpeg", "-y", "-v", "error", "-i", str(source), "-af", f"volume={peak - current:.2f}dB", "-ac", "1", "-ar", "44100", "-c:a", "pcm_s16le", str(out / f"{name}.wav"))
        print(f"{name}: picco a {peak} dBFS")


if __name__ == "__main__":
    main()
