# core:ai

Il Maestro: bot ai vari livelli e analizzatore delle mosse.

## Responsabilità

- Interfaccia `Bot` che sceglie una mossa a partire dalla **vista** del giocatore (mai dallo stato completo).
- Bot di riferimento (casuale, avido), bot PIMC, risolutore esatto dell'ultima smazzata.
- Analizzatore: valore atteso di ogni mossa legale, perdita della mossa giocata, precisione %, classificazione.

## Non fa

- Non modifica le regole: usa solo l'API pubblica di `core:game`.
- Non legge mai informazioni nascoste. Un bot che riceve lo stato completo è un bug grave.

## Dipendenze ammesse

`core:game` e stdlib Kotlin.

## Fonte normativa

`docs/ai/maestro.md`.
