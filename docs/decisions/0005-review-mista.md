# ADR-0005 — Review mista, fiducia sul server

Stato: accettato · 2026-10-23

## Decisione
- L'anteprima (precisione %, numero di errori critici) si calcola sul telefono, gratis.
- La review completa a pagamento è sbloccata da un token firmato dal server; per le partite classificate il risultato ufficiale è calcolato o verificato dal server.
- Nell'MVP (senza backend) la review è gratuita e solo locale.

## Conseguenze
L'analizzatore vive in `core:ai`, così gira sia sul telefono sia sul server.
