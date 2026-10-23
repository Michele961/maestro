# Maestro — Scopa e Briscola con coach AI

Gioco di carte italiano per Android con un coach AI locale ("il Maestro") che insegna memoria, calcolo e strategia.

- Visione di prodotto: [docs/vision.md](docs/vision.md)
- Architettura: [docs/architecture.md](docs/architecture.md)
- Decisioni: [docs/decisions/](docs/decisions/README.md)
- Regole della Scopa: [docs/rules/scopa.md](docs/rules/scopa.md)
- Backlog attuale: [docs/backlog/fase-0.md](docs/backlog/fase-0.md)
- **Agenti AI: leggete [CLAUDE.md](CLAUDE.md) prima di tutto.**

## Stato

Fase 0 — motore headless (regole, AI, simulatore). Nessuna UI.

## Requisiti

JDK 21. La build usa il Gradle wrapper incluso.

Prima pubblicazione su GitHub (una volta sola, con `gh` autenticato): `./scripts/setup-github.sh maestro`.

```
./gradlew check
```
