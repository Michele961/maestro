# ADR-0017 — Dati locali con migrazioni esplicite

Stato: accettato · 2026-10-27 · approvato da Michele in chat

## Contesto
Il database Room usa `fallbackToDestructiveMigration()`: a ogni cambio di schema si cancellano storico, partita in corso e wallet. Con i crediti acquistati (F2-09) diventerebbe una perdita di denaro per l'utente.

## Decisione
- Vietato `fallbackToDestructiveMigration()` in ogni sua forma.
- Schema esportato in `app/.../schemas/` e versionato in Git; ogni cambio di schema ha una `Migration` esplicita e un test di migrazione (`MigrationTestHelper`) dalla versione precedente.
- DataStore: le chiavi non si rinominano; se serve, si migra con una `DataMigration` testata.
- Le partite salvate restano `MatchLog` (ADR-0003): un cambio di `RULES_VERSION` non cancella lo storico, lo segna come non rigiocabile.
- Il wallet resta un registro append-only; nessuna migrazione può riscrivere o cancellare movimenti.

## Conseguenze
- Ogni task che tocca le entità Room include migrazione e test.
- Prima della prima release pubblica si congela la versione 1 dello schema.
