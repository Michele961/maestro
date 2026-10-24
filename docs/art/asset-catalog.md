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
| `maestro_neutral.webp` | Stato normale / neutro con cornice ottone e targa | `512 × 512 px` | **Approvato v1.0** (integrato in `res/drawable-nodpi/`) |
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
