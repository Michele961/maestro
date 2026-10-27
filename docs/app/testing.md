# Test dell'app

Stato: approvata da Michele il 2026-10-27. Si somma a `docs/conventions.md` (Test).

| Layer | Tipo di test | Strumenti | Obbligatorio per |
| --- | --- | --- | --- |
| Dominio | Unit test puri, tempo virtuale | `kotlin.test`, `kotlinx-coroutines-test` | Ogni caso d'uso e ogni regola di ricompensa |
| Presentazione | ViewModel e `TableDirector` con tempo virtuale | `runTest`, `advanceTimeBy`, repository finti | Ogni intento, ogni tipo di battuta |
| Dati | Room in memoria, test di migrazione | Robolectric, `MigrationTestHelper` | Ogni DAO, ogni cambio di schema (ADR-0017) |
| UI | Screenshot sulla matrice di ADR-0016 | Roborazzi | Ogni schermata e ogni stato chiave |
| UI | Interazione (tocco, trascinamento, salto animazione) | Compose UI test | Mano, scelta della presa, review |
| Architettura | Regole di import e divieti di `layers.md` §6 | test JVM che legge i sorgenti (F1R-01) | Sempre in `./gradlew check` |

Regole:

- Niente `Thread.sleep` né attese reali: il tempo si controlla con lo scheduler dei test.
- Dati di prova costruiti con helper (`table("3D", "4C")`), non con seed fortunati.
- Uno screenshot cambiato in una PR va guardato e citato nella descrizione.
- I test di gioco completo (partita intera su seed fisso) girano con `GameSpeed.Instant`.
