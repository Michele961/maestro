# Guida dell'app Android

Stato: approvata da Michele il 2026-10-27 (ADR-0014…0017). Vincolante per ogni task che tocca `app:*`.

Questa cartella dice **come** si costruisce l'app. Il **perché** è negli ADR; il **cosa** è nei task del backlog.
Ogni agente che tocca `app:*` legge questo file e il documento del layer o della funzione che modifica.

| Documento | Quando leggerlo |
| --- | --- |
| [`layers.md`](layers.md) | Sempre: layer, moduli, contratti, dove va ogni cosa, divieti |
| [`table-director.md`](table-director.md) | Tutto ciò che si muove al tavolo, in review, nei puzzle, nell'Accademia |
| [`game-feel.md`](game-feel.md) | Animazioni, tempi, suoni, vibrazione, comportamento del Maestro, ricompense |
| [`responsive-ui.md`](responsive-ui.md) | Qualunque layout o schermata nuova o modificata |
| [`testing.md`](testing.md) | Sempre: quali test servono per ogni layer |
| [`features/`](features/README.md) | Prima di rivedere una feature: procedimento, ordine e modello di dossier |

Riferimenti grafici: `docs/art/style-guide.md`, `docs/art/design-system.md`. Personaggio e tono: `docs/design/maestro_persona.md`.

## Le dieci regole

1. La UI disegna uno stato; non decide, non calcola, non aspetta.
2. Il dominio non conosce Android, Compose né le stringhe localizzate: restituisce codici.
3. Una sola fonte di tempo (`Clock`) e una sola tabella di durate (`MotionSpec`).
4. Al tavolo si muove solo ciò che il `TableDirector` mette in scena; il bot aspetta la regia.
5. Nessuna carta nascosta oltre il layer di dominio: la presentazione riceve solo `ScopaView` e `Beat`.
6. Misure dallo spazio disponibile, non numeri fissi; ogni schermata passa la matrice di ADR-0016.
7. Ogni ricompensa (crediti, missioni, streak, maestria) passa da `RewardEngine`.
8. Ogni cambio di schema dati ha migrazione e test (ADR-0017).
9. Un file ≤ 400 righe, una funzione composable ≤ 80 righe; oltre si divide.
10. Ogni comportamento nuovo ha un test del layer giusto (`testing.md`).
