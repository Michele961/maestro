# Revisione feature per feature

Le fondamenta (ondata A di `docs/backlog/fase-1r.md`) valgono per tutta l'app. Ogni **feature** invece si rivede e si migliora **da sola**, una alla volta, con lo stesso procedimento. Così ogni intervento resta piccolo, verificabile e giocabile.

## Procedimento

1. **Dossier.** Un agente scrive `docs/app/features/<feature>.md` partendo dal modello qui sotto: com'è oggi (codice, screenshot della matrice di ADR-0016, problemi noti), cosa deve diventare, quali contratti usa.
2. **Approvazione.** Michele legge il dossier, prova la feature sul telefono e decide cosa tenere, cambiare o togliere. Il dossier passa a `approvato`.
3. **Task.** Dal dossier approvato nascono i task della feature nel backlog (piccoli, < 400 righe ciascuno), con criteri presi dal dossier.
4. **Esecuzione e verifica.** Task eseguiti uno alla volta; a fine feature Michele rigioca la feature e il dossier passa a `fatto`, con gli screenshot finali.

Un dossier non introduce regole di gioco, dipendenze o moduli: se servono, diventano domande aperte (`CLAUDE.md` §5).

## Ordine

| # | Feature | Dossier | Prerequisiti | Stato |
| --- | --- | --- | --- | --- |
| 1 | Partita al tavolo | `table.md` | Ondata A, F1R-08…F1R-11 | da fare |
| 2 | Fine partita e ricompense | `post-match.md` | F1R-14 | da fare |
| 3 | Game Review e Rigioca l'errore | `review.md` (approvato) | F1R-10, F1R-16 | da fare |
| 4 | Suggerimenti e Check in partita | `coach-in-game.md` | F1R-16 | approvato (2026-10-28) |
| 5 | Pre-partita e avversari | `pre-game.md` | D11 | da fare |
| 6 | Home | `home.md` (bozza) | F1R-04 | da fare |
| 7 | Sfida del giorno | `daily-puzzle.md` | F1R-02, F1R-10 | da fare |
| 8 | Accademia | `academy.md` | F1R-10, F1R-21 | da fare |
| 9 | Profilo, Maestria e Radar | `profile.md` | F1R-14 | da fare |
| 10 | Missioni e streak | `missions.md` | F1R-14 | da fare |
| 11 | Bottega e wallet | `shop.md` | F1R-03, F1R-14 | da fare |
| 12 | Impostazioni e audio | `settings.md` | F1R-09, F1R-20 | da fare |

L'ordine segue l'impatto sul giocatore: prima ciò che si usa in ogni sessione.

## Modello di dossier

```markdown
# <Feature> — dossier

Stato: bozza | approvato | fatto · data · task collegati

## 1. Cosa fa per il giocatore
Una frase. Quando la usa, quanto spesso, cosa deve provare.

## 2. Com'è oggi
- File e righe principali; layer coinvolti.
- Screenshot della matrice (small, medium, pixel10pro, small-font130).
- Problemi: layout, ritmo, feedback, testi, architettura (riferimento a `docs/app/` violato).

## 3. Come deve diventare
- Flusso passo per passo (schermate, scene, tocchi).
- Feedback e tempi (token di `game-feel.md`); comportamento del Maestro.
- Geometria e stati da coprire con screenshot.

## 4. Contratti
Dominio e presentazione usati o da introdurre (nomi da `layers.md`).

## 5. Task proposti
| ID | Titolo | Criteri principali |

## 6. Domande per Michele
```
