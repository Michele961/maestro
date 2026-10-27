# Difetti di layout trovati con la matrice (F1R-06)

Data: 2026-10-27 · Fonte: screenshot Roborazzi su `small` (360×640), `small-font130`, `medium` (393×852), `pixel10pro` (≈410×914).
Questi difetti si correggono nei dossier e nei task delle rispettive feature (`README.md` di questa cartella), non in F1R-06.

| # | Schermata | Config | Difetto |
| --- | --- | --- | --- |
| D1 | Pre-partita | small, small-font130 | "Inizia partita" fuori schermo e Classica/Sparring tagliati: **non si può avviare una partita**. Il test `start_passes_the_selected_options` gira solo su medium/pixel10pro finché non è corretto |
| D2 | Sfida del giorno | small, small-font130 | Nel flusso di risoluzione il fumetto del Maestro o la carta vincente escono dallo schermo. Il test `daily_puzzle_interactiveSolveFlow` gira solo su medium/pixel10pro finché non è corretto |
| D3 | Tavolo | small | La mano copre le pile di destra; la carta in gioco si sovrappone alla mano avversaria; le tre pile a destra si toccano |
| D4 | Tavolo | medium, pixel10pro | L'opposto: carte a terra e mano piccole, molto panno vuoto. Le misure fisse non crescono con lo schermo (ADR-0016) |
| D5 | Tavolo | small-font130 | "GIOCA" e "Trascina sul tavolo" si sovrappongono alle carte a terra; "Ultima giocata" invade le pile |
| D6 | Home | small | Il pannello del Maestro sparisce del tutto (viene compresso a zero) |
| D7 | Home | small-font130 | "Presto dispo…" troncato |
| D8 | Pre-partita | small-font130 | "Principiant", "CLASS", "SPAR" troncati |
| D9 | Fine partita | small-font130 | Il commento del Maestro spinge i pulsanti fuori schermo: "Rivincita", "Radar" e "Menu" non visibili senza scorrere |
| D10 | Tutte | medium, pixel10pro | Testi piccoli: le misure in `sp`/`dp` fisse non sfruttano lo spazio in più |

Prossimo passo: ogni difetto entra nel dossier della sua feature; D3–D5 sono già coperti da F1R-11 e F1R-12 (`TableGeometry`).
