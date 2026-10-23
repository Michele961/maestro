# ADR-0002 — Server autoritativo per le partite online

Stato: accettato · 2026-10-23

## Contesto
Se il client conosce mani avversarie o ordine del mazzo, barare è banale.

## Decisione
Nelle partite online il server mescola, distribuisce, valida ogni mossa e invia a ciascun giocatore solo la propria vista (`viewFor`).

## Conseguenze
- Il client non riceve mai informazioni nascoste, nemmeno "per comodità".
- Serve gestire latenza, timer e riconnessioni sul server.
