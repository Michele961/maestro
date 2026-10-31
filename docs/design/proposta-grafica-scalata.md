# Proposta grafica per la Scalata dei Circoli — vedere la storia

Stato: **proposta, in attesa di Michele** (task F1G-19, 2026-10-31). È solo un documento: nessun codice, nessuna immagine, nessuna voce di backlog modificata. Le immagini le genera Michele (D14); qui si decide **quali** e **come** entrano nell'app.

Richiesta di Michele: «per la grafica della Scalata inerente alla storia, proponi delle grafiche e capiamo insieme quali generare e come integrarle». Feedback di partenza: «la rivedrei con una grafica che permetta di vedere in sé la storia».

Fonti lette: `docs/design/stagione-storia.md` e `stagione-scene.md` (trama, Circoli, personaggi), `docs/art/style-guide.md`, `design-system.md`, `asset-prompts.md`, `asset-catalog.md` (stile e naming), il codice di `SeasonScreen.kt`, `StoryScenePlayer.kt`, `SeasonChapter.kt` e le immagini in `app/android/src/main/res/drawable-nodpi`.

## 1. Com'è oggi (5 righe)

1. La Scalata è una schermata su sfondo legno con una fascia di panno verde in basso (`ClubBackground`), scurita al 60%: non cambia mai, qualunque sia il Circolo.
2. In alto: titolo, «Ancora N tavoli all'Olimpo dei Maestri» e, sotto, **una sola carta verde** «capitolo corrente» (nome del Circolo, titolo, «Capitolo N di 10», testo del prossimo passo, pulsante rosso).
3. Sotto la carta, l'elenco dei capitoli già fatti: righe color crema con titolo, crediti e frase di chiusura. È un elenco di testo; la storia si legge, non si vede.
4. Le scene (`StoryScenePlayer`) si aprono su un velo nero al 90%: ritratto del personaggio (cornice d'ottone), nome e fumetto crema; nei dialoghi l'altro personaggio è in alto a destra, il Maestro in basso a sinistra. Nessun luogo, nessun oggetto: si vede *chi* parla, non *dove*.
5. Le immagini esistenti sono ritratti (Maestro, Gigi, Nonna Rosa, Tonino, Don Alfonso), stemmi, icone, texture legno/panno/pergamena: nulla che racconti i cinque Circoli, la sedia vuota, il taccuino strappato.

**Lo stile da rispettare** (visto nelle immagini esistenti): pittura digitale 2D, linea morbida, colori caldi e saturi, leggera caricatura, luce calda di taverna con vignettatura ambra; legno di noce scuro (`#3B2213`), panno verde bottiglia (`#1E4D2B`), ottone/oro (`#D4AF37`), avorio (`#FDFBF7`). Niente fantascienza, niente testo nelle immagini (lo mette il codice).

## 2. Tre direzioni

Le tre direzioni non si escludono: la B è la base su cui A e C possono innestarsi dopo.

### Direzione A — La mappa dei Circoli (il percorso illustrato)

**Come appare.** La Scalata diventa una **mappa verticale** da leggere dal basso (il Tavolino del Bar) verso l'alto (l'Olimpo): una scala/strada che sale attraverso i cinque Circoli, ognuno illustrato come una piccola scena. Sulla strada ci sono 10 tappe (i capitoli): quelle fatte sono accese con una spunta d'ottone, la corrente pulsa, le future sono in penombra con un lucchetto (`lock_closed`/`lock_open` già esistenti). Toccando una tappa fatta si rilegge la sua chiusura; toccando la corrente compare la carta con il pulsante. In cima, la targa «Olimpo» e la sedia.

```
+---------------------------+
| La Scalata          [X]   |
| Ancora 6 tavoli all'Olimpo|
|        [ OLIMPO  :) ]     |
|          (10)             |
|     ~~ Gran Casino ~~     |
|      (9)                  |
|   (8)  Salotto Nobile     |
|      (7)                  |
|  ===  Circolo Rionale === |
|   (6)--(5)<- sei qui      |
|          (4)              |
|  ~~ Tavolino del Bar ~~   |
|   (3)--(2)--(1)  v        |
+---------------------------+
```

**Cosa cambia nel codice (a grandi linee).**
- `SeasonScreen.kt`: la colonna di carte diventa una mappa scorrevole; nuovo composable `SeasonMap` (file a parte) che riceve `SeasonProgress` (già contiene `chapters`, `done`, `current`) e disegna lo stato; il pulsante «prossimo passo» resta quello esistente (`onStepClicked`).
- Posizione delle tappe come **frazioni** della larghezza/altezza della tessera (non dp fissi), altezza della mappa dalla larghezza disponibile; la tappa corrente scorre in vista all'apertura.
- Nessuna logica nuova di avanzamento: solo lettura di `ChapterStatus` (DONE, CURRENT, UPCOMING).
- Pulsazione della tappa corrente: durata da un nuovo token di `MotionSpec` (mai numeri nel composable).
- Test screenshot da aggiornare nelle quattro configurazioni (small, medium, pixel10pro, small-font130); il testo accanto a ogni tappa deve reggere il carattere 1,3 (etichette brevi, `maxLines`).

**Pro.** Risponde alla lettera a «vedere la storia»: il percorso, dove sei, quanto manca. Ricompensa visiva forte a ogni capitolo (la tappa si accende). Rimette in vista i 5 Circoli.
**Contro.** Le scene a dialogo restano senza luogo (non risolve il punto 4 di §1). Tappe e testi su immagine sono fragili su schermi diversi (small 360×640 contro pixel10pro lungo e stretto). Con la mappa il testo del capitolo corrente deve comunque stare in una carta.

**Costo.** 5 tessere di mappa (una per Circolo, da impilare, 1080×1200 ciascuna) + 3 segnalini di tappa (fatta, corrente, futura) = **8 immagini**, circa **1,0–1,6 MB** in webp. Rischio: **medio-alto** (le tessere devono combaciare ai bordi e restare belle quando la mappa scorre; tappe sovrapposte al disegno richiedono di non toccare le zone «vive» dell'immagine).

### Direzione B — Un luogo per ogni Circolo, dietro le scene e in cima alla Scalata

**Come appare.** Ogni Circolo ha **uno sfondo illustrato** (il bar, il circolo rionale con la bacheca, il salotto di velluto, il casinò, l'Olimpo con la scala). Compare in due punti:
1. **Dietro le scene a dialogo:** il velo nero scompare (resta una sfumatura leggera per leggere il testo); i personaggi sono nel luogo, con i ritratti e i fumetti già pronti.
2. **In cima alla Scalata:** una «finestra» illustrata sul Circolo del capitolo corrente, con sopra il nome del Circolo; le carte dei capitoli fatti diventano **cartoline** (lo sfondo del Circolo ritagliato dentro la cornice di pergamena già esistente, con titolo e frase di chiusura): una piccola collezione di luoghi visitati.

```
Scena a dialogo                     Scalata
+---------------------------+   +---------------------------+
|[luogo: Circolo Rionale]   |   | La Scalata          [X]   |
|          (o) Nonna Rosa   |   | +-----------------------+ |
|          [fumetto Rosa]   |   | | [luogo del capitolo]  | |
|                           |   | |  Circolo Rionale      | |
|  (o) Maestro              |   | +-----------------------+ |
|  [fumetto Maestro]        |   | Cap. 5 di 10 - Nonna Rosa |
|     tocca per continuare  |   | [ Sfida Rosa ]            |
+---------------------------+   | Fatti: [cartolina 4][3]...|
                                +---------------------------+
```

**Cosa cambia nel codice (a grandi linee).**
- `SeasonChapter.kt`: l'enum `Circle` ha già `nameRes`; si aggiunge `backgroundRes` (stesso schema, nessun nuovo tipo).
- `StoryScenePlayer.kt`: nuovo parametro `backdrop: Int?` (se manca, comportamento di oggi); il velo passa da nero 90% a una sfumatura (più scura dietro il fumetto); immagine a tutto schermo con `ContentScale.Crop`. I ritratti e le misure restano calcolati dallo spazio disponibile, come ora.
- `SeasonScreen.kt`: chi apre la scena passa il Circolo del capitolo (benvenuto = Tavolino del Bar, epilogo = Olimpo); in cima compare l'immagine del Circolo corrente, **dentro** la colonna scorrevole (non toglie spazio a small-font130); i capitoli fatti usano lo sfondo del loro Circolo.
- Il dominio resta senza Android: `backgroundRes` segue lo stesso schema di `nameRes`.
- Se si vuole dissolvenza tra scene: durata solo da `MotionSpec` (nuovo token); altrimenti nessuna animazione, come oggi.
- Test screenshot esistenti (`SeasonScreenScreenshotTest`, `StoryScenesScreenshotTest`): nuove immagini di riferimento, nessuna asserzione indebolita.

**Pro.** Il maggior effetto per il minor numero di immagini: le scene smettono di essere «ritratti nel buio» e il giocatore sa sempre *dove* si trova nella storia. Nessun problema di coerenza dei volti (le persone restano i ritratti già approvati). Si adatta a qualunque schermo (sfondo ritagliato, nessun elemento posizionato sull'immagine). Si può fare in due passi.
**Contro.** Non mostra «tutto il percorso» in un colpo d'occhio (lo fa l'indicatore «Ancora N tavoli»). I 10 capitoli di uno stesso Circolo condividono lo stesso luogo.

**Costo.** **5 immagini** (una per Circolo), circa **1,0–1,5 MB** in webp (oggi la cartella `drawable-nodpi` pesa circa 5,9 MB). Rischio: **basso**.

### Direzione C — Il diario del Maestro (album di «foto»)

**Come appare.** La Scalata diventa un **album**: pagine del taccuino del Maestro con una «foto» per capitolo (la prima sera al bar, il foglio «R» in bacheca, la sedia di Tonino, la scala dell'Olimpo...) e sotto la riga scritta dal Maestro (la `close` del capitolo). Le foto dei capitoli futuri sono lo spazio vuoto di una pagina con gli angolini; il pezzo di pagina strappata si ricompone man mano (insegna, Chi, impara, due volte). È la direzione che racconta di più, perché ogni capitolo ha un'immagine sua.

```
+---------------------------+
| Il taccuino del Maestro   |
| +-------+  +-------+      |
| | foto  |  | foto  |      |
| |  1    |  |  2    |      |
| +-------+  +-------+      |
| "La prima sera"  "Gigi"   |
| +-------+  +-------+      |
| | foto  |  | [   ] |      |
| |  3    |  |  4    |      |
| +-------+  +-------+      |
|  Pezzi: [insegna] [ ? ]   |
| Cap. 4: [ Vai alla Sfida ]|
+---------------------------+
```

**Cosa cambia nel codice (a grandi linee).**
- `SeasonScreen.kt`: la lista dei capitoli fatti diventa una griglia di «foto» (stessa fonte di stato, nessuna logica nuova); le future restano vuote.
- Nuovo composable per la pagina/foto con cornice (pergamena e ottone già nel design system: `skin_panel_parchment`, `ornament_corner`).
- Mappa capitolo → immagine (10 risorse); i pezzi di pagina sono un indicatore costruito sui capitoli 2, 5, 8, 10 (solo lettura dello stato).
- Griglia a colonne calcolate dalla larghezza (2 colonne su small, 3 su schermi larghi), didascalie con `maxLines` per il carattere 1,3.
- Test screenshot nelle quattro configurazioni.

**Pro.** Ogni capitolo ha un momento riconoscibile, da collezionare e rivedere; il mistero della frase in pezzi diventa visibile. Molto «raccontante».
**Contro.** Servono 10 illustrazioni **con personaggi** in scene diverse: è il punto più difficile da generare in modo coerente con i ritratti (volti, abiti, luce). Le scene a dialogo restano senza luogo. Molti capitoli sono attività (Accademia, Sfida del giorno, revisione) con poca azione da illustrare. Più asset = più peso e più lavoro per Michele.

**Costo.** 10 «foto» (3:4, 900×1200) + 4 pezzi di pagina + 1 cornice = **15 immagini**, circa **2,5–3,5 MB** in webp. Rischio: **alto** (coerenza dei personaggi, e le immagini decidono tutta la riuscita della schermata).

### Confronto in breve

| | A — Mappa | B — Luoghi | C — Diario |
| --- | --- | --- | --- |
| Si vede il percorso | molto | poco (indicatore a testo) | medio |
| Le scene a dialogo hanno un luogo | no | **sì** | no |
| Immagini da generare | 8 | **5** | 15 |
| Peso stimato (webp) | 1,0–1,6 MB | 1,0–1,5 MB | 2,5–3,5 MB |
| Rischio di coerenza grafica | medio-alto | **basso** | alto |
| Difficoltà su schermi diversi | alta | **bassa** | media |
| Righe di codice (stima) | 250–350 | **100–200** | 250–350 |

## 3. Raccomandazione

**Direzione B, in due fasi: «prima il minimo che dà più effetto».**

- **Fase 1 (5 immagini):** gli sfondi dei cinque Circoli, usati dietro le scene a dialogo e in cima alla Scalata (con le cartoline dei capitoli fatti, fatte dal codice con gli sfondi, senza immagini in più).
- **Fase 2 (facoltativa, 2 immagini, solo dopo aver visto la fase 1 sul telefono):** «la sedia dell'Olimpo», vuota e occupata, per dare un volto alla posta in gioco (la sala che chiude) in cima alla Scalata e nell'epilogo.

Perché la B:
1. È l'unica che risolve il difetto più evidente delle scene (ritratti nel buio, senza un posto) **e** quello della schermata (carta verde e lista di testo) con lo stesso lavoro.
2. Il numero di immagini è il più basso (5) e non contengono persone: i personaggi restano i ritratti già approvati, quindi **non c'è rischio di volti incoerenti**.
3. Funziona su tutti i formati perché lo sfondo si ritaglia: nessun elemento va allineato al disegno.
4. Non chiude la strada: la mappa (A) può usare gli stessi cinque sfondi come tessere, e il diario (C) può nascere dalle cartoline.
5. Il Circolo è già nei dati (`Circle` e i nomi in `strings.xml`): il cambiamento è piccolo e a basso rischio.

## 4. Elenco asset per la direzione raccomandata

### Regole comuni (per tutti i prompt)

- Nome file: `story_bg_<circolo>.webp`, minuscolo e con underscore, con `<circolo>` uguale al nome dell'enum `Circle` del codice (`bar_table`, `neighborhood_club`, `noble_lounge`, `grand_casino`, `olympus`); stessa logica di `opponent_1_neutral`, `card_bastoni_re`, `texture_wood`. Cartella: `app/android/src/main/res/drawable-nodpi/`.
- Formato: **verticale 1:2, 1080×2160 px** (si ritaglia bene sia su 9:16 come small 360×640 sia sui telefoni lunghi come pixel10pro). Fondo pieno, **senza trasparenza**: si prepara con `scripts/prepare_assets.py texture <file> out/ <nome> --size 2160` (come `texture_wood`). Peso atteso: 200–300 KB l'uno.
- **Zona sicura:** i dettagli importanti nella fascia centrale (circa 80% della larghezza e 70% dell'altezza). I bordi servono solo da atmosfera, perché verranno tagliati.
- **Composizione per le scene:** in alto a destra e in basso a sinistra devono esserci zone **calme e un po' più scure** (lì stanno i ritratti e i fumetti); il centro può avere i dettagli. Luce calda dall'alto o da un lato, mai al centro-basso dove va il testo.
- **Niente persone, niente testo, niente lettere** (cartelli, targa, fogli, lavagna: vuoti o con segni astratti).
- **Varianti:** generarne 2–3 per ciascun luogo, Michele sceglie la migliore; in app ne entra **una**.
- **Coerenza con i ritratti esistenti:** stessa conversazione di Gemini dei ritratti e dei mockup; stessa pittura, stessa luce ambra con vignettatura, stesso legno scuro, panno verde e ottone. Da incollare all'inizio di ogni prompt come «ancora di stile» (sotto).

**Ancora di stile (da incollare in ogni prompt, in inglese):**

```
In the same painted style as the Maestro portraits and the club mockups: 2D digital painting,
soft thick outlines, warm saturated colors, slightly caricatured and cozy, glossy tactile
finishes, warm tavern lighting with an amber vignette. Palette: dark walnut wood (#3B2213 with
warm highlights #5A3821), bottle-green felt (#1E4D2B to #14381E), polished brass and gold
(#D4AF37), ivory paper (#FDFBF7), brick-red and cream accents. Traditional Italian card club
atmosphere. Vertical 1:2 composition, 1080x2160. Calm, slightly darker areas in the upper-right
and lower-left corners; important details in the central 80%. NO people, NO characters, NO text,
NO letters, NO numbers, no signs with writing, no sci-fi, no neon, no hologram, no photorealism.
```

### Fase 1 — gli sfondi dei Circoli (5 asset)

| # | File | Uso | Dimensioni | Varianti da generare |
| --- | --- | --- | --- | --- |
| 1 | `story_bg_bar_table.webp` | Capitoli 1–3 (Gigi); scena di benvenuto | 1080×2160 | 2–3, ne entra 1 |
| 2 | `story_bg_neighborhood_club.webp` | Capitoli 4–6 (Nonna Rosa) | 1080×2160 | 2–3, ne entra 1 |
| 3 | `story_bg_noble_lounge.webp` | Capitoli 7–8 (Tonino) | 1080×2160 | 2–3, ne entra 1 |
| 4 | `story_bg_grand_casino.webp` | Capitolo 9 e prima parte del 10 (Don Alfonso) | 1080×2160 | 2–3, ne entra 1 |
| 5 | `story_bg_olympus.webp` | Fine del capitolo 10 (il Maestro); epilogo | 1080×2160 | 2–3, ne entra 1 |

**Prompt 1 — `story_bg_bar_table` (Tavolino del Bar)**

```
[style anchor above] Interior of a small, cozy Italian neighborhood bar in the evening. A worn dark
wood bar counter with a brass coffee machine and a few espresso cups, one little round table with
a bottle-green felt top and a deck of Italian playing cards fanned on it, two simple wooden chairs,
a small door to a back room slightly ajar with warm light, a counter drawer slightly open, an
Italian tricolor cloth banner on a wooden rod on the wall, hanging lamp with a warm amber glow.
Friendly, humble, welcoming; the first evening at the club.
```

**Prompt 2 — `story_bg_neighborhood_club` (Circolo Rionale)**

```
[style anchor above] Interior of a neighborhood social club (circolo rionale), early evening. On the
wall a cork bulletin board with several pinned paper sheets (no readable writing; one of the sheets
carries only a small red ink swirl instead of letters), a green
chalkboard with abstract chalk doodles and no letters, a small wooden table with a worn deck of cards
next to a single chair with a knitted shawl draped over its back (a cozy 'landing' corner), faded
framed pictures, brass wall lamp, warm walnut paneling and a bottle-green felt table in the corner.
Homey, lived-in, slightly nostalgic.
```

**Prompt 3 — `story_bg_noble_lounge` (Salotto Nobile)**

```
[style anchor above] An elegant lounge, evening. Round card tables covered in deep bottle-green
velvet with brass edges, high-backed armchairs in dark red leather, heavy burgundy curtains, a
bookcase with leather-bound books, a small table with an open leather notebook and a fountain pen,
low brass lamps casting soft golden pools of light. Hushed, refined, a little mysterious.
```

**Prompt 4 — `story_bg_grand_casino` (Gran Casinò)**

```
[style anchor above] A grand casino card room, bright and lively. A large crystal and brass
chandelier overhead, rows of green felt card tables with brass rims and scattered Italian playing
cards and gold coins, rich red carpet, marble columns with gold trim, a staircase seen in the
background leading to a door of light (the way up). Bright glittering warm light, quick and
sparkling mood, but keep the same warm cozy palette and thick painted outlines.
```

**Prompt 5 — `story_bg_olympus` (Olimpo dei Maestri)**

```
[style anchor above] The top hall of the Olympus of Masters, calm and solemn. A short wide stone
and dark-wood staircase rising toward the center of the image, a polished walnut table with
bottle-green felt at the top with ONE empty wooden chair at its far side, a blank brass wall
plaque above it (no writing), warm golden light streaming from above and behind, floating soft
dust motes, laurel branch decorations in brass. Majestic but still warm and welcoming, the end
of the climb.
```

**Indicazioni di coerenza (per tutti e cinque):**
- Stessa luce: calda e ambrata, con vignettatura sui bordi; il casinò è il più luminoso, l'Olimpo il più dorato.
- Stessa palette (legno, panno verde, ottone, avorio); il rosso solo come accento (tappeti, tende).
- Stesso tratto dei ritratti: contorni morbidi e spessi, pittura con riflessi satinati, nessun fotorealismo.
- Se un'immagine esce con testo o lettere, rigenerarla: il testo lo mette sempre il codice.
- Ordine consigliato per generare: Tavolino del Bar (è il riferimento), poi gli altri nella **stessa conversazione**, citando «same room style as the Bar Table image».

### Fase 2 — la sedia dell'Olimpo (2 asset, facoltativa)

Oggetti su fondo verde piatto come gli altri set (si preparano con `prepare_assets.py sheet`). Si usano in cima alla Scalata (sedia vuota fino al capitolo 10) e nell'epilogo (occupata, con la targa col tuo nome scritta dal codice).

| # | File | Uso | Dimensioni | Varianti |
| --- | --- | --- | --- | --- |
| 6 | `story_chair_empty.webp` | In cima alla Scalata finché il capitolo 10 non è fatto | 800×1000 (4:5), trasparenza | 2, ne entra 1 |
| 7 | `story_chair_taken.webp` | Epilogo e Scalata completata | 800×1000, trasparenza | 2, ne entra 1 |

```
[style anchor above, but on a plain flat bright green background #00FF00, front view, no perspective]
A single dark walnut wooden chair with a bottle-green cushion and brass studs, seen from the front,
standing alone, with a small blank brass plaque on its backrest (no writing), gentle warm glow around it.
```

```
[same as above] The same chair, now with a worn leather notebook resting open on the seat and a small
fan of Italian playing cards on the cushion, the brass plaque on the backrest slightly brighter.
```

**Totale: 5 immagini in fase 1 (circa 1,0–1,5 MB), 7 con la fase 2 (circa 1,2–1,7 MB).**

## 5. Come si integra

### File di codice toccati (fase 1)

- `app/android/src/main/kotlin/it/maestro/app/domain/season/SeasonChapter.kt` — `Circle` riceve `backgroundRes` (come `nameRes`).
- `.../feature/season/ui/StoryScenePlayer.kt` — parametro `backdrop`, velo a sfumatura, immagine a tutto schermo.
- `.../feature/season/ui/SeasonScreen.kt` — immagine del Circolo in cima (dentro la colonna scorrevole), cartoline per i capitoli fatti, passaggio del Circolo alla scena (benvenuto = Bar, epilogo = Olimpo).
- `.../domain/story/StoryDirector.kt` / `StoryScene.kt` — solo se serve che la scena porti il suo Circolo; di base il Circolo si ricava dal capitolo in `SeasonScreen`, senza toccarli.
- `app/android/src/main/res/drawable-nodpi/` — i 5 file `story_bg_*.webp`.
- `app/android/src/test/.../SeasonScreenScreenshotTest.kt` e `StoryScenesScreenshotTest.kt` — nuove immagini di riferimento nelle quattro configurazioni (small, medium, pixel10pro, small-font130).
- `docs/art/asset-catalog.md` — nuova sezione con i cinque file (dopo le immagini approvate).
- Nessuna dipendenza, nessun modulo, nessuna stringa nuova; gli altri file protetti restano com'è.

### Rispetto delle regole dell'app

- La UI disegna lo stato che riceve: il Circolo arriva dal capitolo, nessuna decisione nel composable.
- Misure dallo spazio disponibile (`BoxWithConstraints`): l'altezza dell'immagine in cima = frazione della larghezza, entro un massimo; nessuno spazio perso su small-font130 perché scorre con il contenuto.
- Tempi: nessuna animazione in fase 1 (come le scene di oggi); se si vuole una dissolvenza, un token in `MotionSpec`.
- Leggibilità: il fumetto ha già fondo crema; sotto i ritratti una sfumatura scura leggera, così il testo bianco del nome resta leggibile su ogni sfondo.
- Se uno sfondo manca, si torna al velo nero di oggi (stesso modo in cui le carte usano il fallback).

### Bozza di voce di backlog (da aggiungere solo dopo l'approvazione di Michele)

```
## F1G-20 — Sfondi dei Circoli nella Scalata

- **Stato:** da fare
- **Dipende da:** F1G-19 approvato da Michele; i cinque sfondi generati e consegnati da Michele
- **Obiettivo:** la Scalata e le scene mostrano il luogo del Circolo (Tavolino del Bar, Circolo Rionale,
  Salotto Nobile, Gran Casinò, Olimpo dei Maestri) invece di un velo nero e di una carta verde.
- **Perimetro:** `Circle.backgroundRes`; sfondo dietro `StoryScenePlayer` (velo a sfumatura, parametro
  opzionale, fallback al velo di oggi); immagine del Circolo corrente in cima a `SeasonScreen` e cartoline
  per i capitoli fatti; nessuna nuova logica di avanzamento, nessuna stringa nuova, nessuna dipendenza.
- **Criteri di accettazione:** ogni capitolo mostra il suo Circolo; benvenuto = Bar, epilogo = Olimpo;
  senza immagine resta il comportamento attuale; testo leggibile su ogni sfondo; screenshot nella matrice
  (small, medium, pixel10pro, small-font130) per una scena e per la Scalata; i test esistenti passano
  senza asserzioni cambiate.
- **Per Michele:** generare i 5 sfondi (`story_bg_*`, 1080×2160) con i prompt di
  `docs/design/proposta-grafica-scalata.md` §4 e prepararli con `scripts/prepare_assets.py texture`.
- **Domande aperte:** nessuna (vedi quelle di F1G-19).

## F1G-21 — La sedia dell'Olimpo (facoltativa)

- **Stato:** da fare
- **Dipende da:** F1G-20 fatto e visto sul telefono; `story_chair_empty` e `story_chair_taken` generate
- **Obiettivo:** dare un volto alla posta in gioco: la sedia vuota in cima alla Scalata, occupata
  nell'epilogo.
- **Perimetro:** due immagini in `SeasonScreen` (stato letto da `SeasonProgress.isComplete`);
  nessuna logica nuova.
- **Criteri di accettazione:** sedia vuota fino al capitolo 10, occupata a Scalata completa; screenshot
  nella matrice.
- **Per Michele:** generare le 2 immagini.
- **Domande aperte:** nessuna.
```

Totale: **2 task** (F1G-20 obbligatorio, F1G-21 facoltativo); ognuno sotto le 400 righe di codice non di test.

### Domande aperte per Michele (con risposta proposta)

1. **Direzione.** Va bene la **B** (luoghi dei Circoli), con A e C rimandate? *Proposta: sì, solo la B; la mappa o il diario si rivalutano dopo averla vista sul telefono.*
2. **Persone negli sfondi.** Gli sfondi sono luoghi **senza persone** (i personaggi restano i ritratti già approvati)? *Proposta: sì, per non rischiare volti diversi dai ritratti.*
3. **Testi e scritte nell'immagine.** Targa «Olimpo», foglio firmato «R» e lavagna restano vuoti nell'immagine (se serve, la scritta la aggiunge l'app)? *Proposta: sì, nessuna lettera nelle immagini, come per tutti gli altri asset.*
4. **Quante prove generare.** 2–3 varianti per ogni Circolo, scegli tu la migliore? *Proposta: sì, partendo dal Tavolino del Bar come riferimento di stile.*
5. **La sedia dell'Olimpo (fase 2).** La vuoi subito con i 5 sfondi o dopo aver visto la fase 1 sul telefono? *Proposta: dopo, così si decide con l'effetto reale.*

Fuori da questa proposta (da decidere se serve): musica o suoni per i Circoli, animazioni di transizione, una seconda stagione («Giro delle osterie») con altri luoghi.
