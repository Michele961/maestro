# ADR-0007 — Italiano e inglese dal primo giorno

Stato: accettato · 2026-10-23

## Decisione
Ogni testo per l'utente è esternalizzato con traduzione it/en. I termini di gioco seguono `docs/rules/glossary.md`.

## Conseguenze
- `core:*` non contiene testi per l'utente: espone codici (enum) che la UI traduce.
- Nessuna stringa letterale per l'utente nel codice dell'app.
