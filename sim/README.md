# sim

Simulatore headless da riga di comando: partite bot contro bot, report su vittorie, punteggi e tempi.

Serve a validare regole e AI prima di qualsiasi grafica (Fase 0).

```
./gradlew :sim:run --args="--games 1000 --seed 42 --p1 pimc --p2 greedy"
```

Output previsto (task F0-09): riepilogo su stdout e report JSON in `sim/reports/` (ignorato da git).

## Dipendenze ammesse

`core:game`, `core:ai`, stdlib Kotlin.
