# Maestro — Scopa e Briscola con coach AI

Gioco di carte italiano per Android con un coach AI locale ("il Maestro") che insegna memoria, calcolo e strategia.

- Visione di prodotto: [docs/vision.md](docs/vision.md)
- Architettura: [docs/architecture.md](docs/architecture.md)
- Roadmap: [docs/roadmap.md](docs/roadmap.md)
- Decisioni: [docs/decisions/](docs/decisions/README.md)
- Regole della Scopa: [docs/rules/scopa.md](docs/rules/scopa.md)
- Backlog attuale: [docs/backlog/fase-1.md](docs/backlog/fase-1.md)
- **Agenti AI: leggete [CLAUDE.md](CLAUDE.md) prima di tutto.**

## Stato

Fase 1 — MVP offline Android (Jetpack Compose, Koin, Scopa contro il Maestro, Game Review locale). Motore headless completato in Fase 0.

## Requisiti

JDK 21. La build usa il Gradle wrapper incluso.

Prima pubblicazione su GitHub (una volta sola, con `gh` autenticato): `./scripts/setup-github.sh maestro`.

```
./gradlew check
```
