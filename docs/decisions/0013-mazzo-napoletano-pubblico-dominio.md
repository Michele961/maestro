# ADR-0013 — Mazzo base: carte napoletane tradizionali in pubblico dominio

Stato: accettato · 2026-10-24 · approvato da Michele in chat

## Decisione
Il mazzo base dell'app usa le carte napoletane tradizionali in pubblico dominio di Wikimedia Commons
(autore Trocche100, `PD-user-it`), messe su cartoncino da `scripts/prepare_napoletane.py`.
Fonte e procedura: `docs/art/napoletane/SOURCE.md`.

## Rapporto con ADR-0008
Restringe ADR-0008 per il solo mazzo base: le carte non vengono ridisegnate con AI. Il dorso, il tavolo,
il Maestro e il resto della grafica restano generati come da ADR-0008. I mazzi personalizzati (tema
sostituibile, `DeckTheme`) arriveranno più avanti e potranno essere generati.
