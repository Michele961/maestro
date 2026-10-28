# Bottega — lista dei contenuti da produrre

Per Michele (`shop.md`, decisione 4). Formato proposto: immagini `webp`, stesso stile e proporzioni dei file esistenti in `app/android/src/main/res/drawable-nodpi/` (`table_felt_bg.webp`, `card_back.webp`, `avatar_player_default.webp`). I nomi file sono indicativi; gli id definitivi li fissa il task F1R-30a. I prezzi sono [da calibrare] con l'economia.

## Oggi nel catalogo (da rifare con anteprima vera)

| Id | Categoria | Prezzo | Cosa serve |
| --- | --- | --- | --- |
| `felt_royal_red` | Tavolo | 40 | feltro rosso vero (oggi è il verde con filtro) |
| `felt_noble_blue` | Tavolo | 50 | feltro blu vero |
| `felt_walnut_wood` | Tavolo | 60 | già esistente (`texture_wood`), verificare la resa |
| `back_vintage_red` | Dorso | 35 | dorso rosso disegnato |
| `back_golden_maestro` | Dorso | 50 | dorso oro disegnato |
| `avatar_alt`, `avatar_expert` | Ritratto | 40 / 75 | già esistenti |

## Nuovi contenuti proposti

| Categoria | Idee | Quantità |
| --- | --- | --- |
| Tavoli | feltro verde scuro del circolo, tavolo da osteria in legno chiaro, tavolo con tovaglia a quadri, tavolo notturno con lume | 4 |
| Dorsi | dorso regionale (napoletano, piacentino, siciliano), dorso a rombi classico, dorso con stemma del Maestro | 4 |
| Ritratti | 6 ritratti di personaggi del circolo (stesso stile degli avversari, cornice comune) | 6 |
| Cornici | cornice del ritratto in bronzo / argento / oro (sbloccate dalla scala, non in vendita) | 3 |
| Stemmi e titoli | un emblema per ciascun avversario battuto + stemma finale della scala (decisione 2 di `shop.md`) | 6 |

Specifiche comuni: sfondo senza testo; tavoli 1920×1080 con zona centrale libera; dorsi 600×900 con angoli arrotondati trasparenti; ritratti e stemmi 512×512 su trasparente. Nessun testo dentro le immagini (ADR-0007).
