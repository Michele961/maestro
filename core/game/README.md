# core:game

Regole, stato e log delle partite. È il cuore del progetto: lo usano app, server, AI e simulatore.

## Responsabilità

- Modello: carte, mazzo, mani, tavolo, prese, punteggi.
- Regole della Scopa (poi Briscola) come funzione pura: `stato + mossa → nuovo stato + eventi`.
- Elenco delle mosse legali per il giocatore di turno.
- `MatchLog` (seed + configurazione + mosse) e replay deterministico.
- Viste per giocatore (cosa vede ciascuno), usate da AI e protocollo.

## Non fa

- Nessuna AI, nessuna UI, nessuna rete, nessun I/O.
- Nessuna lettura dell'orologio, nessun `Random` non seminato, nessuno stato globale.

## Dipendenze ammesse

Solo la stdlib Kotlin. Verificato da `./gradlew :core:game:checkModuleBoundaries`.

## Fonti normative

- Regole: `docs/rules/scopa.md` (la specifica vince sul codice; se divergono, è un bug).
- Termini e nomi: `docs/rules/glossary.md`.
