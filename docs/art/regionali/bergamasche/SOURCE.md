# Mazzo bergamasco — scheda fonte (task F1G-10)

Stato: **40 carte ritagliabili in modo pulito; qualità a 532x868 discreta (netta nei contorni, un po' morbida); licenza dichiarata come pubblico dominio ma con un dubbio forte sulla datazione.** Verifica del 2026-10-30. Nessuna immagine è nell'app: l'integrazione è F1G-11.

## Fonte primaria

| Voce | Valore |
| --- | --- |
| File | `Carte bergamasche.jpg` su Wikimedia Commons |
| Pagina del file | <https://commons.wikimedia.org/wiki/File:Carte_bergamasche.jpg> |
| Autore (come scritto) | «the firm of Pietro Masenghini» (ditta Masenghini, Bergamo) |
| Scansione | Luigi Chiesa (`User:Luigi Chiesa`), 16 agosto 2006; caricata il 2006-09-08 |
| Data dichiarata | «Late 19th century» (fine Ottocento) |
| Descrizione sulla pagina | «Playing cards of Bergamo, Italy» / «Carte da gioco bergamasche»; nella fonte dice «tarot cards produced by the firm of Pietro Masenghini in Bergamo towards the end of the 19th century» |
| Licenza (testo della pagina) | campo `Permission = {{PD-old}}`; Commons mostra «Public domain» (metadato `License: pd`, `Copyrighted: False`) |
| Come è stata letta | wikitext e metadati dell'API di Commons. **Il testo «CC Public Domain Mark 1.0» citato nella ricerca non compare nella pagina**: la pagina dice solo `PD-old` / «Public domain»; la dicitura Public Domain Mark potrebbe venire da un altro sito |
| Formato | JPEG, 3089 x 2229 px, 1,6 MB, foglio 4 righe x 10 colonne |

## Dubbio sulla licenza (da decidere con Michele)

1. `PD-old` vale se l'autore è morto da oltre 70 anni; qui l'autore è una ditta e la pagina non dà nessuna data di morte né alcuna prova della datazione.
2. **Il foglio non sembra di fine Ottocento.** L'Asso di denari porta «MADE IN ITALY», «MASENGHINI BERGAMO» e un codice, e la stampa è una policromia nitida e moderna: è un mazzo del Novecento, non un'edizione ottocentesca. La ditta Masenghini esiste ancora e vende questo tipo di mazzo. Quindi la dichiarazione `PD-old` è **non verificata e probabilmente errata**, e il marchio sull'Asso di denari è un marchio commerciale.
3. La serie di Poulpy (CC BY-SA 3.0, vedi sotto) è ritagliata **dallo stesso foglio** («Cropped from Carte bergamasche.jpg»): la sua licenza CC BY-SA copre il ritaglio, non rende libero il disegno di Masenghini. Il ripiego non risolve il dubbio: ha lo stesso disegno a risoluzione minore.

La scelta di Michele (2026-10-30) sulla CC BY-SA 3.0 riguardava le immagini di «Poulpy»; non copre il dubbio sul disegno originale, che qui va segnalato.

## Cosa va attribuito

Se si usa il foglio così com'è (`PD-old`): nessun obbligo di legge, ma si consiglia di citare «scansione di Luigi Chiesa, Wikimedia Commons; mazzo Masenghini, Bergamo». Se si ripiega sulle carte di Poulpy: attribuzione a «Poulpy» (CC BY-SA 3.0) e rilascio delle immagini adattate con la stessa licenza.

## Carte e ritaglio

- **40 carte presenti e riconoscibili** (Asso, 2..7, Fante, Cavallo, Re per seme). Righe del foglio: **bastoni, spade, coppe, denari** (lo script le rimette nell'ordine del progetto: denari, coppe, spade, bastoni).
- Ritaglio automatico con `scripts/crop_sheet.py FOGLIO OUT --rows bastoni,spade,coppe,denari --inset 18`: 40 riquadri su 40, nessun intervento manuale. Le carte del foglio sono separate da un margine bianco.
- Carta nel foglio: circa 275-307 x 535-548 px (rapporto 0,51-0,56). Il disegno senza margine bianco è circa 245 x 500 px.
- Figure a doppia testa (stile bergamasco), con Asso con nastro «VINCERAI» (bastoni), corona (spade), putto (coppe), cerchio con marchio (denari).

## Qualità a 532 x 868 px

**Discreta: contorni netti e colori piatti puliti, ma ingrandita 1,7x e quindi un po' morbida** (si vede nell'anteprima: nulla di illeggibile; la retinatura è assente, è una stampa a tinte piatte). Chiaramente meglio di piacentine e triestine. Anteprima: `docs/art/regionali/anteprime/bergamasche.webp`.

## Difetti

1. **Asso di denari con marchio Masenghini** («MADE IN ITALY», logo, scritta «MASENGHINI BERGAMO») al posto di un disegno tradizionale: va rimosso o ridisegnato, e comunque è il punto più a rischio.
2. Rapporto 0,51-0,56 contro 0,64 delle napoletane: nel cartoncino 600x936 il disegno resta più stretto.
3. Il ritaglio grezzo conserva a volte una striscia chiara dei vicini sul bordo: l'inset di 18 px la toglie ma taglia gli angoli decorativi degli Assi.
4. Nessun dorso (il foglio non lo contiene).

## Ripiego: serie di Poulpy

Categoria `Category:Bergamo_deck`: 40 file singoli `Bergamo Deck - <Coins|Cups|Swords|Wands> - <Ace|02..07|Jack|Knight|King>.jpg`. Autore Poulpy, **CC BY-SA 3.0** (`{{self|cc-by-sa-3.0}}` su ogni pagina, data 2012-12-29), 300 x 550 px a carta. Fonte dichiarata: ritaglio di `Carte bergamasche.jpg`. Risoluzione più bassa del foglio (circa 0,55x in larghezza): da usare solo se il foglio non fosse accettabile, e con lo stesso dubbio sul disegno. Letto il wikitext di tutti e 40 i file: tutti hanno `{{self|cc-by-sa-3.0}}`, autore Poulpy, 300 x 550 px.

## Cosa manca

- Il dorso.
- La rimozione del marchio sull'Asso di denari.
- La verifica indipendente dell'epoca del mazzo (e quindi del `PD-old`).

## Peso

Il foglio pesa 1,6 MB; le 40 carte in WebP a 600x936 (qualità 88) pesano 1,6 MB misurati, contro i 2,8 MB delle 41 napoletane in `res`.
