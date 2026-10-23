# ADR-0001 — Motore condiviso in Kotlin Multiplatform

Stato: accettato · 2026-10-23

## Contesto
Le stesse regole servono all'app (partite offline e AI), al server (validazione) e in futuro a iOS. Riscriverle in più linguaggi crea divergenze, quindi bug e possibilità di barare.

## Decisione
Regole, AI e protocollo stanno in moduli `core:*` Kotlin Multiplatform senza dipendenze Android. App e server sono strati sottili.

## Conseguenze
- Un solo punto in cui correggere una regola.
- Il server deve essere su JVM (Ktor), vedi ADR-0006.
- Scartati: Unity/Godot (troppo pesanti per un gioco con molta UI applicativa), Nakama (regole da riscrivere in Go/TS), Flutter/React Native (non nativo).
