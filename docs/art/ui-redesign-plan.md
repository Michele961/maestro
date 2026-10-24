# Piano di rifacimento di UI e UX — Maestro

**Stato:** proposta, da approvare da Michele · 2026-10-24
**Riferimenti:** [`style-guide.md`](style-guide.md), [`design-system.md`](design-system.md), [`asset-catalog.md`](asset-catalog.md), mockup `reference_*` in questa cartella.

## 1. Obiettivo

Portare tutte le schermate dell'app allo stile dei mockup approvati (circolo italiano: legno, panno, ottone, pergamena), con un'esperienza coerente dalla Home al tavolo alla review.

## 2. Metodo (validato con F1-13 e F1-18)

1. **Asset illustrati generati con AI** (Gemini, nella stessa conversazione dei mockup, per tenere lo stile), un oggetto per immagine o più oggetti separati su fondo verde `#00FF00`, senza testo né cornici.
2. **Preparazione con `scripts/prepare_assets.py`**: scontorno, ridimensionamento, WebP in `res/drawable-nodpi/`; gli originali vanno in `docs/art/generated/`.
3. **Tutto il resto in Compose**: pulsanti, pannelli, targhette, fumetti, cornici dritte, con i colori di `design-system.md`. Niente testo dentro le immagini.
4. **Verifica con screenshot Roborazzi**: ogni schermata ha un test che la disegna; la CI allega gli screenshot alla run e la PR mostra il confronto con il mockup.
5. **Voci senza funzione** (monete, gemme, negozio, amici…): decorazioni non cliccabili con valori fissi, finché non arrivano i task dedicati.

## 3. Task proposti (Fase 1)

| ID | Titolo | Mockup | Asset da generare | Dipende da |
|---|---|---|---|---|
| F1-18 | Menu iniziale con gli asset definitivi | `reference_menu_screen.jpg` | — (pronti) | F1-13 |
| F1-19 | Fondamenta UI: tipografia, sfondi e componenti condivisi | tutti | font (vedi §5) | F1-18 |
| F1-20 | Modale "Preparati a giocare" | `reference_pre_match_dialog.png` | avatar livello Maestro senza cornice (riuso `maestro_neutral`) | F1-19 |
| F1-21 | Tavolo da gioco | `reference_table_screen.jpg` | icona pausa; 40 carte (F1-13) | F1-19 |
| F1-22 | Fine partita e Game Review | `reference_review_screen.png` | bollini ✕ / ★, freccia, icone timeline | F1-19 |
| F1-23 | Impostazioni, pausa e check didattici | `style_guide_sheet.jpg` | eventuali icone (audio, lingua) | F1-19 |
| F1-24 | Movimento e feedback (UX) | — | — | F1-20…F1-23 |

### F1-19 — Fondamenta UI
- Tipografia del gioco (titoli condensati in grassetto come nei mockup, testo leggibile) in `Theme.kt`.
- Sfondi condivisi (legno, panno, legno + panno) e scaffold di schermata riusabile.
- Riorganizzazione dei componenti: `TactileButtons.kt` e `MenuComponents.kt` diventano la libreria unica, con KDoc e una pagina di screenshot "catalogo componenti".
- Aggiornamento di `design-system.md` (§3 libreria componenti) con i componenti reali.

### F1-20 — Modale pre-partita
- Pannello pergamena con fregi, targhette di sezione, selettori 11/16/21, livelli con avatar (Principiante, Esperto, Maestro), Classica/Sparring, "Inizia partita".

### F1-21 — Tavolo
- Panno a tutto schermo con cornice in legno, targhetta punteggio, avatar avversario, mazzo e prese, mano a ventaglio, Maestro con fumetto compatto, pausa.
- Le carte restano quelle di `DeckTheme`: le 40 definitive arrivano da F1-13 senza cambiare il codice.

### F1-22 — Fine partita e Review
- Targa "Vittoria/Sconfitta" con punteggio, conteggio punti (carte, denari, settebello, primiera, scope), precisione, mano evidenziata con bollini, timeline colorata, spiegazione del Maestro, Rivincita / Nuova partita / Torna al menu.

### F1-23 — Impostazioni, pausa e check didattici
- Modali su pergamena con lo stesso linguaggio della modale pre-partita.

### F1-24 — Movimento e feedback
- Transizioni tra schermate, pressione dei pulsanti (scala e ombra), animazione delle carte giocate e prese, cambio d'espressione del Maestro, vibrazione leggera opzionale.

## 4. Ordine consigliato

F1-18 → F1-19 → F1-21 (la schermata più usata) → F1-20 → F1-22 → F1-23 → F1-24.
F1-20, F1-22 e F1-23 si possono fare in parallelo dopo F1-19.

## 5. Decisioni per Michele

1. **Font:** i mockup usano un carattere condensato in grassetto. Proposta: un font open source (licenza OFL) incluso in `res/font/` come file, senza nuove dipendenze. Da scegliere (es. Oswald o Roboto Condensed).
2. **Approvazione del piano** e dei task F1-19…F1-24 nel backlog.
