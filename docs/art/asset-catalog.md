# Catalogo Asset e Nomi di File — Maestro

Questo documento elenca l'elenco esatto dei file richiesti per il mazzo di carte napoletane, il tavolo e l'avatar del Maestro (task F1-13).

Posizione target nel progetto Android:
`app/android/src/main/res/drawable-nodpi/` (WebP, non ridimensionati da Android in base alla densità dello schermo).

---

## 1. Dorso e Tavolo

| File Name | Descrizione | Risoluzione Minima Consigliata | Stato |
|---|---|---|---|
| `card_back.webp` | Dorso della carta comune (verde bottiglia e oro) | `600 × 936 px` | **Approvato v1.0** (integrato in `res/drawable-nodpi/`) |
| `table_felt_bg.webp` | Texture panno verde con vignettatura e luce centrale | `1080 × 1920 px` | **Approvato v1.0** (`res/drawable-nodpi/`, 1000 × 1116) |

---

## 2. Avatar del Maestro (5 Stati)

| File Name | Stato Emotivo | Risoluzione Minima | Stato |
|---|---|---|---|
| `maestro_neutral.webp` | Stato normale / neutro | `512 × 512 px` | **Approvato v1.0** (`res/drawable-nodpi/`, ritratto senza cornice) |
| `maestro_thinking.webp` | Il bot o l'utente stanno pensando | `512 × 512 px` | **Approvato v1.0** (`res/drawable-nodpi/`, ritratto senza cornice) |
| `maestro_pleased.webp` | Risposta corretta nei check o mossa ottima | `512 × 512 px` | **Approvato v1.0** (`res/drawable-nodpi/`, ritratto senza cornice) |
| `maestro_warning.webp` | Risposta errata nei check o blunder | `512 × 512 px` | **Approvato v1.0** (`res/drawable-nodpi/`, ritratto senza cornice) |
| `maestro_celebrating.webp` | Scopa messa a segno o vittoria | `512 × 512 px` | **Approvato v1.0** (`res/drawable-nodpi/`, ritratto senza cornice) |

---

## 2bis. Asset del menu e dell'interfaccia (v1.0)

Generati con Gemini su fondo verde piatto e preparati con `scripts/prepare_assets.py`. Gli originali sono in `docs/art/generated/`. Tutti in `app/android/src/main/res/drawable-nodpi/`, WebP con trasparenza.

Pulsanti, pannelli, targhette, fumetti e cornici dritte non sono immagini: li disegna il codice Compose con i colori di `design-system.md`. Gli asset qui sotto si appoggiano sopra.

| File | Uso | Sorgente |
|---|---|---|
| `icon_trophy`, `icon_book_quill`, `icon_pouch` | Barra in basso: Classifica, Palestra, Bottega | `menu_icons.jpg` |
| `icon_coin`, `icon_gem` | Contatori crediti e gemme | `menu_icons.jpg` |
| `icon_envelope`, `icon_gear` | Messaggi, impostazioni | `menu_icons.jpg` |
| `icon_gift`, `icon_newspaper`, `icon_shield_fleur` | Premi giornalieri, Notizie dal circolo, stemma livello | `menu_icons.jpg` |
| `icon_book_closed` | Riserva (regole / tutorial) | `menu_icons.jpg` |
| `ornament_top`, `ornament_bottom`, `ornament_corner` | Fregi in ottone sopra i pannelli (l'angolo si ribalta per i 4 lati) | `ornaments.jpg` |
| `avatar_beginner`, `avatar_expert` | Avversari Principiante ed Esperto | `avatars.jpg` |
| `opponent_1..5_neutral` | Ritratti degli avversari (Gigi, Nonna Rosa, Tonino, Don Alfonso, Il Maestro) **con cornice in ottone già inclusa** | `opponent_portraits.jpg` |
| `opponent_1..5_win`, `opponent_1..5_lose` | Espressioni di vittoria e sconfitta | `opponent_win_lose.jpg`, `opponent_missing_expressions.jpg` (Maestro vittoria, Tonino sconfitta) |
| `crest_1..5` | Stemmi degli avversari, dal bronzo semplice all'oro con nastro | `opponent_crests.jpg` |
| `lock_closed`, `lock_open` | Lucchetto per gli avversari bloccati / appena sbloccati | `opponent_locks.webp` |
| `avatar_player_default`, `avatar_player_alt` | Avatar del giocatore (due scelte) | `avatars.jpg` |
| `avatar_friends` | Scheda Amici | `avatars.jpg` |
| `texture_wood` | Sfondo legno, steso a tutto schermo (non ripetuto) | `texture_wood.jpg` |
| `texture_parchment` | Fondo di modali e schede | `texture_parchment.jpg` |

Avatar e ritratti del Maestro sono **senza cornice**: cerchio, bordo e targhetta li aggiunge il codice.

---

## 3. Le 40 Carte Napoletane

La convenzione di denominazione è:
`card_<seme>_<valore>` (tutto minuscolo, underscore come separatore).

### 3.1. Denari (`denari`)
1. `card_denari_asso.webp` (o `card_denari_1`)
2. `card_denari_2.webp`
3. `card_denari_3.webp`
4. `card_denari_4.webp`
5. `card_denari_5.webp`
6. `card_denari_6.webp`
7. `card_denari_7.webp` *(Il Settebello: **Approvato v1.0**, integrato in `res/drawable-nodpi/`)*
8. `card_denari_fante.webp`
9. `card_denari_cavallo.webp`
10. `card_denari_re.webp`

### 3.2. Coppe (`coppe`)
11. `card_coppe_asso.webp`
12. `card_coppe_2.webp`
13. `card_coppe_3.webp`
14. `card_coppe_4.webp`
15. `card_coppe_5.webp`
16. `card_coppe_6.webp`
17. `card_coppe_7.webp`
18. `card_coppe_fante.webp`
19. `card_coppe_cavallo.webp`
20. `card_coppe_re.webp`

### 3.3. Spade (`spade`)
21. `card_spade_asso.webp`
22. `card_spade_2.webp`
23. `card_spade_3.webp`
24. `card_spade_4.webp`
25. `card_spade_5.webp`
26. `card_spade_6.webp`
27. `card_spade_7.webp`
28. `card_spade_fante.webp`
29. `card_spade_cavallo.webp`
30. `card_spade_re.webp`

### 3.4. Bastoni (`bastoni`)
31. `card_bastoni_asso.webp` *(Asso di Bastoni: **Approvato v1.0**, integrato in `res/drawable-nodpi/`)*
32. `card_bastoni_2.webp`
33. `card_bastoni_3.webp`
34. `card_bastoni_4.webp`
35. `card_bastoni_5.webp`
36. `card_bastoni_6.webp`
37. `card_bastoni_7.webp`
38. `card_bastoni_fante.webp`
39. `card_bastoni_cavallo.webp`
40. `card_bastoni_re.webp`

---

## 4. Integrazione nell'App

Nel codice Compose, la classe [`DeckTheme`](file:///Users/mike/Projects/maestro/app/android/src/main/kotlin/it/maestro/app/ui/theme/DeckTheme.kt) controllerà la presenza del drawable con l'ID corrispondente:
- Se l'asset `card_<seme>_<valore>` è presente nella cartella `res/drawable-nodpi/`, verrà renderizzato direttamente con ombreggiatura e bordi appropriati.
- Se un asset non è ancora presente, il sistema utilizzerà automaticamente e senza crash il rendering vettoriale segnaposto.
Questo permette di aggiungere o sostituire carte anche una alla volta o a gruppi.

---

## 5. Asset Game Feel (task F1-25)

Generati con Gemini su fondo magenta/verde e preparati con `scripts/prepare_assets.py`.
Sorgenti in `docs/art/generated/game-feel/`.

| File | Uso |
|---|---|
| `text_scopa_gold.webp` | Scritta "SCOPA!" oro con allori per scopa del giocatore |
| `text_scopa_red.webp` | Scritta "SCOPA!" rossa per scopa dell'avversario |
| `text_ancora_gold.webp` | Scritta "ANCORA!" per seconda scopa consecutiva |
| `text_doppia_gold.webp` | Scritta "DOPPIA!" per scope multiple |
| `particle_leaf_1.webp` … `particle_leaf_8.webp` | 8 foglioline d'oro per l'effetto burst della Scopa |
| `particle_confetti_1.webp` … `particle_confetti_10.webp` | 10 coriandoli tricolore e oro per vittoria partita |
| `card_frame_gold.webp` | Cornice oro luminosa per catture importanti (es. Settebello) |
| `maestro_applauding.webp` | Ritratto Maestro che applaude felice |
| `maestro_triumphant.webp` | Ritratto Maestro pugno della vittoria |
| `maestro_despair.webp` | Ritratto Maestro mano sulla fronte (scopa subita) |
| `maestro_encouraging.webp` | Ritratto Maestro pollice in su incoraggiante |


---

## 6. Sfondi dei Circoli (task F1G-21)

Fondali verticali **senza persone e senza testo**, generati da Michele (D14) con i prompt di `docs/design/proposta-grafica-scalata.md` §4; dietro le scene della storia e in cima alla Scalata, ritagliati con `ContentScale.Crop`. Originali JPEG 989 × 2000, convertiti con `scripts/prepare_assets.py texture <originale>.jpg res/drawable-nodpi story_bg_<circolo> --size 2000 --quality 80` (WebP senza trasparenza; il lato massimo 2000 evita di ingrandire). Il nome corrisponde all'enum `Circle`, che li collega con `backgroundRes`.

| File | Circolo | Peso |
|---|---|---|
| `story_bg_bar_table.webp` | Tavolino del Bar (capitoli 1–3, benvenuto) | 116 KB |
| `story_bg_neighborhood_club.webp` | Circolo di quartiere (capitoli 4–6) | 118 KB |
| `story_bg_noble_lounge.webp` | Salotto nobile (capitoli 7–8) | 103 KB |
| `story_bg_grand_casino.webp` | Gran Casinò (capitolo 9) | 186 KB |
| `story_bg_olympus.webp` | Olimpo dei Maestri (capitolo 10, epilogo) | 96 KB |

Totale 619 KB (989 × 2000 px ciascuno; la proposta indicava 1080 × 2160: la risoluzione generata da Michele basta per un fondale ritagliato).

---

## 7. Sedia dell'Olimpo (task F1G-22)

Due oggetti con trasparenza, generati da Michele su fondo verde chroma-key con un alone verde luminoso (sorgenti: `story_chair_empty_src.webp` 1024 × 765 e `story_chair_taken_src.jpg` 2000 × 1493). In cima alla Scalata quando il capitolo 10 è in corso (vuota) e a Scalata completa (occupata). Stessa tela (441 × 800 px, sedia alta 760 px con 20 px di margine) e stessa scala: si sovrappongono a parte taccuino e carte sul sedile.

```
python3 scripts/prepare_chairs.py story_chair_empty_src.webp story_chair_taken_src.jpg \
    app/android/src/main/res/drawable-nodpi
```

Lo script (Pillow + numpy, solo offline) toglie il verde luminoso (matte sul canale verde, il panno verde scuro della sedia resta opaco), sopprime l'alone sui bordi (despill nella fascia vicina alla trasparenza), ritaglia alla sedia e normalizza altezza e centro. Verificata a vista su bianco, nero e verde panno `#1E4D2B`: nessun contorno verdastro.

| File | Stato | Peso |
|---|---|---|
| `story_chair_empty.webp` | Capitolo 10 in corso | 39 KB |
| `story_chair_taken.webp` | Scalata completa (taccuino aperto e carte napoletane) | 42 KB |
