# UI adattiva

Fonte: ADR-0016. Stato: proposta (PR #77).

## 1. Principi

1. Ogni schermata parte dallo **spazio utile**: larghezza e altezza dopo gli inset di sistema (`WindowInsets.safeDrawing`).
2. Una **geometria** calcolata una volta per schermata (`TableGeometry`, `ReviewGeometry`, …) contiene tutte le misure; i composable la ricevono e non inventano numeri.
3. Le misure sono **frazioni** dello spazio con `coerceIn(min, max)`; le costanti stanno nel design system.
4. Il testo usa `sp` e regge il 130%: targhette con `maxLines` e riduzione controllata, mai testo tagliato a metà parola.
5. Schermi larghi (≥ 600 dp): colonna centrata, larghezza massima `Dimens.maxContentWidth` (480 dp iniziali).

## 2. Esempio: `TableGeometry`

```kotlin
data class TableGeometry(
    val cardWidth: Dp,              // carta a terra
    val handCardWidth: Dp,          // carta in mano, più grande
    val opponentCardWidth: Dp,
    val pileWidth: Dp,
    val maestroSize: Dp,
    val zones: TableZones,          // rettangoli: avversario, zona di gioco, mazzo, mano, pile, Maestro
) {
    companion object {
        fun from(width: Dp, height: Dp): TableGeometry  // funzione pura, testata
    }
}
```

`from` è una funzione pura: si testa con le 4 configurazioni della matrice senza Compose (nessuna zona fuori dai bordi, nessuna sovrapposizione tra mano e carte a terra, carte a terra leggibili ≥ 56 dp di larghezza).

## 3. Proporzioni iniziali del tavolo

| Elemento | Regola | Min | Max |
| --- | --- | --- | --- |
| Carta in mano | 26% della larghezza | 84 dp | 128 dp |
| Carta a terra | 19% della larghezza | 56 dp | 96 dp |
| Carta avversario (coperta) | 13% della larghezza | 40 dp | 64 dp |
| Zona di gioco | 42% dell'altezza | — | — |
| Mano | 24% dell'altezza, carte intere e visibili | — | — |
| Maestro | 14% della larghezza | 44 dp | 72 dp |

Rapporto carta: 1 : 1,72 (carte napoletane, ADR-0013).

## 4. Matrice di test (ADR-0016)

Configurazioni Roborazzi (`@Config(qualifiers = …)`):

| Nome | Qualificatori |
| --- | --- |
| `small` | `w360dp-h640dp-xhdpi` |
| `medium` | `w393dp-h852dp-xxhdpi` |
| `pixel10pro` | da confermare con `adb shell wm size` / `wm density` sul dispositivo di Michele |
| `small-font130` | `w360dp-h640dp-xhdpi` + `fontScale = 1.3f` |

Ogni schermata ha un test screenshot per configurazione negli stati chiave. La CI pubblica gli screenshot come artifact; la PR mostra almeno `small` e `pixel10pro`.

## 5. Controlli prima di una PR di UI

- [ ] Nessun nuovo `.dp` per dimensioni di carte, mano, pile, avatar (solo token del design system).
- [ ] Screenshot delle 4 configurazioni guardati: niente tagli, sovrapposizioni o testo fuori dai bordi.
- [ ] Inset di sistema rispettati (barra di navigazione a gesti, notch).
