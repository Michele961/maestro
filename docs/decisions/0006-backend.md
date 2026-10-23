# ADR-0006 — Backend Ktor + Postgres + Redis

Stato: accettato · 2026-10-23

## Decisione
Server di gioco in Ktor (JVM) che riusa `core:*`. Postgres per account, wallet, storico; Redis per code di matchmaking e classifiche. Supabase solo per l'autenticazione, se fa risparmiare tempo.

## Conseguenze
Nessun backend prima della Fase 2. Il wallet è un registro append-only, mai un saldo modificabile.
