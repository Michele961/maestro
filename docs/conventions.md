# Convenzioni

## Kotlin

- Stile ufficiale Kotlin (`kotlin.code.style=official`), righe ≤ 120 caratteri, indentazione 4 spazi.
- API pubbliche dei moduli `core:*` con visibilità esplicita (`public`/`internal`) e KDoc. Tutto ciò che non serve fuori dal modulo è `internal`.
- Modelli immutabili: `data class` con `val`, collezioni `List`/`Set`/`Map` di sola lettura. Niente `var` nei modelli di dominio.
- Niente `!!`, niente `lateinit` in `core:*`.
- Stati e risultati con `sealed interface`; `when` esaustivi senza ramo `else` sui sealed.
- Errori di gioco come valori (`PlayResult.Rejected`); `require`/`check`/`error` solo per invarianti violate (bug).
- Nessun testo per l'utente in `core:*` (ADR-0007).
- Commenti: spiegano il *perché* o citano la specifica (`// spec §4.3`), non ripetono il codice.

## Determinismo

- Unica fonte di casualità: `SeededRng` (ADR-0009).
- Liste di mosse legali e di prese possibili in **ordine deterministico e documentato** (ordine canonico delle carte: seme nell'ordine DENARI, COPPE, SPADE, BASTONI, poi valore crescente).
- Niente iterazione su `HashSet`/`HashMap` quando l'ordine influisce sul risultato.

## Test

- Framework: `kotlin.test` (nessun altro, ADR-0010).
- Ogni regola della specifica ha almeno un test il cui nome inizia con il paragrafo citato, in camelCase
  (i nomi con spazi tra backtick non funzionano su tutti i target KMP):
  `fun spec4_2_cartaDiPariValoreHaPrecedenzaSullaSomma()`.
- Casi costruiti a mano tramite helper di test (`table("3D", "4C")`, notazione: valore + iniziale del seme D/C/S/B), non tramite seed "fortunati".
- Test di proprietà: cicli su seed fissi (es. `for (seed in 0L until 500L)`), mai seed casuali.
- Fixture di regressione: log di partite salvati come costanti nel test, con stato finale atteso.
- Un test che fallisce non si "aggiusta" cambiando l'atteso senza una ragione citata dalla specifica.

## Struttura dei file

- Un tipo pubblico principale per file, file con il nome del tipo.
- Test nello stesso package del codice testato, in `commonTest`.

## App Android

Regole complete in `docs/app/` (ADR-0014…0017). In breve:

- Package per layer: `shell`, `domain`, `data`, `designsystem`, `feature.<nome>.{ui,presentation}` (`docs/app/layers.md`).
- File ≤ 400 righe, composable ≤ 80 righe, un'intera schermata in più file della sua feature.
- Composable senza logica: ricevono uno stato e callback; nessun `delay`, nessuna lettura di orologio o repository.
- Durate solo da `MotionSpec`, misure di carte e avatar solo da una geometria (`responsive-ui.md`).
- Testi solo da `strings.xml` (it/en) con `plurals` dove serve; il dominio restituisce codici.
- Test per layer come in `docs/app/testing.md`; screenshot sulla matrice di ADR-0016.

## Git e PR

- Branch `task/<ID>-<slug>`, Conventional Commits con ID (`feat(game): F0-05 presa per somma`).
- PR piccole (indicativamente < 400 righe di codice non di test), template compilato.
