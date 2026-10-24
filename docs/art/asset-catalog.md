# Catalogo Asset e Nomi di File — Maestro

Questo documento elenca l'elenco esatto dei file richiesti per il mazzo di carte napoletane, il tavolo e l'avatar del Maestro (task F1-13).

Posizione target nel progetto Android:
`app/android/src/main/res/drawable/` (oppure `app/android/src/main/res/drawable-nodpi/` per file WebP/PNG ad alta risoluzione non scalati).

---

## 1. Dorso e Tavolo

| File Name | Descrizione | Risoluzione Minima Consigliata |
|---|---|---|
| `card_back.webp` (o `.png`) | Dorso della carta comune | `600 × 936 px` |
| `table_felt_bg.webp` (o `.png`) | Texture panno verde con vignettatura e luce centrale | `1080 × 1920 px` |

---

## 2. Avatar del Maestro (5 Stati)

| File Name | Stato Emotivo | Risoluzione Minima |
|---|---|---|
| `maestro_neutral.webp` | Stato normale / neutro | `512 × 512 px` |
| `maestro_thinking.webp` | Il bot o l'utente stanno pensando | `512 × 512 px` |
| `maestro_pleased.webp` | Risposta corretta nei check o mossa ottima | `512 × 512 px` |
| `maestro_warning.webp` | Risposta errata nei check o blunder | `512 × 512 px` |
| `maestro_celebrating.webp` | Scopa messa a segno o vittoria | `512 × 512 px` |

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
7. `card_denari_7.webp` *(Il Settebello: cura speciale su questa carta!)*
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
31. `card_bastoni_asso.webp`
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
- Se l'asset `card_<seme>_<valore>` è presente nella cartella `res/drawable/`, verrà renderizzato direttamente con ombreggiatura e bordi appropriati.
- Se un asset non è ancora presente, il sistema utilizzerà automaticamente e senza crash il rendering vettoriale segnaposto.
Questo permette di aggiungere o sostituire carte anche una alla volta o a gruppi.
