# Workflow

Come nasce, si esegue e si chiude un task. Vale per persone e agenti.

## Ruoli

| Ruolo | Chi | Responsabilità |
| --- | --- | --- |
| Proprietario del prodotto | Michele | Decide regole, priorità, ADR; approva eccezioni ai guardrail; fa il merge |
| Agente esecutore | Agente AI | Esegue un task alla volta nel suo perimetro, apre una PR |
| Agente revisore | Agente AI (sessione separata) | Rivede la PR contro task, specifica e guardrail; non modifica il codice |

## Stati del task

`da fare` → `in corso` → `in revisione` → `fatto`

`bloccato` si può raggiungere da qualunque stato e richiede almeno una domanda nella sezione "Domande aperte" del task.

Lo stato vive solo nella riga **Stato** di ciascun task nel file di backlog della fase (`docs/backlog/fase-N.md`). L'agente esecutore aggiorna solo lo **Stato** e le **Domande aperte** del proprio task, sul branch del task, non il resto del backlog.

## Ciclo di un task

1. **Assegnazione.** Michele indica l'ID del task (es. "esegui F0-05").
2. **Preparazione.** L'agente segue `CLAUDE.md` §2 e verifica che le dipendenze siano `fatto`.
3. **Esecuzione.** Branch `task/<ID>-<slug>`, commit piccoli, test insieme al codice.
4. **Verifica.** `./gradlew check` verde; criteri di accettazione controllati uno per uno.
5. **PR.** Template compilato; stato `in revisione`.
6. **Revisione.** Un agente revisore usa `/review-task <ID>`; Michele legge il verdetto e decide.
7. **Merge.** Solo Michele: imposta lo stato a `fatto` nella PR e poi fa il merge.

## Escalation

Le domande vanno scritte così, nella sezione "Domande aperte" del task:

```
- [ ] Q1 (F0-09) [esempio fittizio]: il task non dice con quante cifre decimali scrivere i tempi nel JSON.
      Proposta: 2 cifre, punto come separatore. Bloccante: no.
```

Ogni domanda indica: contesto, riferimento alla specifica, proposta, se è bloccante.
Michele risponde sotto la domanda; se la risposta cambia una regola, la specifica viene aggiornata da lui (o su sua richiesta esplicita) prima di riprendere il task.

## Nuovi task

Gli agenti **non** creano task nel backlog. Possono proporli nella sezione "Fuori dal perimetro" della PR. Michele decide se aggiungerli.

## Modifiche ai guardrail

I file protetti elencati in `CLAUDE.md` §3 e il version catalog si cambiano solo su richiesta esplicita di Michele, in una PR dedicata, oppure nel **Perimetro autorizzato** di un task.
