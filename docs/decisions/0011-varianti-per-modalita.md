# ADR-0011 — Varianti di regole come configurazione per modalità

Stato: accettato · 2026-10-23

## Decisione
Ogni variante (punteggio obiettivo, re bello, ridistribuzione con tre re, ...) è un campo di `ScopaConfig` con valore di default documentato in `docs/rules/scopa.md`. Ogni modalità (classificata, sparring, amichevole) dichiara le varianti ammesse.

## Conseguenze
- Nessuna variante cablata nel codice con `if` sparsi: si legge sempre la configurazione.
- In classificata, code separate per combinazione di varianti.
- Si implementano solo le varianti elencate nella specifica.
