# ADR-0016 — UI adattiva alle dimensioni dello schermo

Stato: accettato · 2026-10-27 · approvato da Michele in chat

## Contesto
Gli screenshot Roborazzi girano su una sola configurazione. Sul Pixel 10 Pro di Michele alcune schermate non corrispondono agli screenshot. `TableScreen.kt` contiene 148 misure fisse in `dp` e solo 6 punti in tutta l'app leggono le dimensioni disponibili.

## Decisione
1. **Orientamento verticale** su telefono (`screenOrientation="portrait"`). Su schermi larghi (≥ 600 dp) il contenuto resta una colonna centrata con larghezza massima definita in `app:designsystem`.
2. **Le misure derivano dallo spazio disponibile.** Ogni schermata calcola una geometria (es. `TableGeometry`) a partire dalla larghezza e dall'altezza utili, al netto degli inset di sistema. Le dimensioni di carte, mano, pile e Maestro sono frazioni dello spazio, con minimi e massimi. Misure fisse in `dp` solo per spaziature e bordi, dai token del design system.
3. **Matrice di riferimento obbligatoria** per i test screenshot di ogni schermata:

| Nome | Dimensioni (dp) | Densità | Note |
| --- | --- | --- | --- |
| `small` | 360 × 640 | xhdpi | Telefono economico, caso peggiore in altezza |
| `medium` | 393 × 852 | xxhdpi | Telefono medio |
| `pixel10pro` | ≈ 410 × 914 | xxxhdpi | Dispositivo di Michele (misure da confermare con `adb shell wm size` e `wm density`) |
| `small-font130` | 360 × 640 | xhdpi | Testo di sistema al 130% |

4. **Testo:** nessun testo tagliato o sovrapposto con la scala del 130%; le targhette si restringono o vanno a capo, non escono dai bordi.

## Conseguenze
- Un errore di layout si vede in CI su 4 configurazioni, non sul telefono dopo il merge.
- I test screenshot crescono di 4 volte: si tengono per le schermate e gli stati chiave, non per ogni variante.
- Dettagli: `docs/app/responsive-ui.md`.
