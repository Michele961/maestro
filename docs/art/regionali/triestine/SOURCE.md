# Mazzo triestino — scheda fonte (task F1G-10)

Stato: **40 carte presenti e riconoscibili, ma NON ritagliabili in modo pulito in automatico; qualità a 532x868 sfocata; licenza CC BY-SA 4.0 letta sulla pagina, con obbligo di attribuzione e share-alike.** Verifica del 2026-10-30. Nessuna immagine è nell'app: l'integrazione è F1G-11.

## Fonte

| Voce | Valore |
| --- | --- |
| File | `Carte triestine.jpg` su Wikimedia Commons |
| Pagina del file | <https://commons.wikimedia.org/wiki/File:Carte_triestine.jpg> |
| Autore | GattoFigaro (`User:GattoFigaro`, pagina utente inesistente), «Own work» |
| Data | 2019-11-06 |
| Descrizione sulla pagina | «Mazzo triestino da 40 carte» |
| Licenza (testo della pagina) | `{{self|cc-by-sa-4.0}}`; Commons mostra «Creative Commons Attribution-Share Alike 4.0», <https://creativecommons.org/licenses/by-sa/4.0> |
| Come è stata letta | wikitext e metadati dell'API di Commons (rendering del modello non aperto nel browser) |
| Formato | JPEG, 2744 x 1927 px, 1,3 MB, **fotografia** di un mazzo disteso su un tavolo scuro, 4 righe x 10 colonne |

## Cosa va attribuito

CC BY-SA 4.0: **attribuzione obbligatoria** («GattoFigaro, Wikimedia Commons, CC BY-SA 4.0», con link alla pagina del file e alla licenza, indicando se le immagini sono state modificate) e **share-alike**: le immagini derivate (ritagli, ridimensionamenti, ritocchi) vanno rilasciate con la stessa licenza o una compatibile. Va inserita una schermata di crediti nell'app e il testo della licenza nel repository.

**Dubbio da sciogliere (non verificato).** La licenza copre la fotografia dell'autore. Il disegno delle carte è di un editore non nominato (il mazzo triestino è un prodotto commerciale con lettere e indici stampati) e la pagina non dice nulla sui suoi diritti. Vale lo stesso dubbio delle piacentine, ed è più forte perché le carte sono moderne.

## Carte e ritaglio

- **40 carte presenti**: righe denari, coppe, bastoni, spade; colonne Asso, 2..7, Fante (11), Cavallo (12), Re (13). Tutte riconoscibili.
- **Non ritagliabile in modo pulito in automatico.** Le carte sono a contatto, con prospettiva e piccole rotazioni (fino a circa 40 px di scarto fra una riga e l'altra); le righe si sovrappongono di qualche pixel e lo sfondo scuro non separa le carte. `scripts/crop_sheet.py` trova solo parte dei confini: con `--dark-bg --equal` produce celle approssimate che tagliano le carte in diversi punti. Serve un ritaglio manuale carta per carta, con raddrizzamento della prospettiva.
- Carta nel foglio: circa 250-290 x 440-470 px.

## Qualità a 532 x 868 px

**Sfocata.** Fotografia (non scansione) di una carta di circa 230 x 400 px utili: l'ingrandimento è circa 2,1-2,3x e si vedono sfocatura, ombre e riflessi. Le scritte «FANTE DI COPPE», gli indici e i motti dell'Asso («NON VAL SAPER CHI HA FORTUNA CONTRA») si leggono ma sono morbide. Anteprima (ritagli manuali approssimati di 5 carte): `docs/art/regionali/anteprime/triestine.webp`.

## Difetti

1. Foto con prospettiva, ombre e riflessi del flash; colori non uniformi fra una carta e l'altra.
2. Indici e scritte stampate su ogni carta (numeri agli angoli, nome della figura), diversi dalle napoletane.
3. Carte a contatto: ritaglio manuale.
4. Rapporto 0,57-0,61 (più vicino alle napoletane degli altri mazzi).
5. Nessun dorso.

## Cosa manca

- Ritaglio manuale pulito delle 40 carte.
- Il dorso.
- Una risoluzione migliore: serve una scansione o un ridisegno.
- Chiarimento sui diritti del disegno.

## Peso

Il foglio pesa 1,3 MB; 40 carte WebP a 600x936 stimano 2-4 MB (non misurato: manca il ritaglio pulito).
