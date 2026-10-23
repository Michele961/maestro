# ADR-0010 — `core:game` senza dipendenze esterne

Stato: accettato · 2026-10-23

## Decisione
`core:game` dipende solo dalla stdlib Kotlin. Gli altri moduli hanno le dipendenze elencate in `docs/architecture.md`. I confini sono verificati da `./gradlew check`.

## Conseguenze
Niente librerie di utilità, di serializzazione o di test property-based in `core:game`: i test property-based si scrivono con cicli su seed fissi.
