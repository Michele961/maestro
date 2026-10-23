# ADR-0004 — La Scopa a 2 è il primo gioco

Stato: accettato · 2026-10-23

## Decisione
Fase 0 e MVP partono dalla Scopa a 2 giocatori. La Briscola a 2 segue sullo stesso motore. Scopone e giochi a 4 vengono dopo.

## Conseguenze
- La specifica normativa è `docs/rules/scopa.md`; la Briscola non va implementata finché non esiste `docs/rules/briscola.md`.
- Le prese multiple rendono l'albero delle mosse più largo: il tempo per mossa dell'AI va misurato subito (F0-10).
