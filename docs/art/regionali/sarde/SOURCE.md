# Mazzo sardo — scheda fonte (task F1G-10)

Stato: **40 carte presenti e riconoscibili, ma NON ritagliabili in modo pulito in automatico; qualità a 532x868 sfocata; licenza CC BY-SA 4.0 letta sulla pagina, con obbligo di attribuzione e share-alike; stampa di un editore commerciale (Dal Negro) visibile sull'Asso di denari.** Verifica del 2026-10-30. Nessuna immagine è nell'app: l'integrazione è F1G-11.

## Fonte

| Voce | Valore |
| --- | --- |
| File | `Carte sarde.jpg` su Wikimedia Commons |
| Pagina del file | <https://commons.wikimedia.org/wiki/File:Carte_sarde.jpg> |
| Autore | GattoFigaro (`User:GattoFigaro`, pagina utente inesistente), «Own work» |
| Data | 2019-11-06 |
| Descrizione sulla pagina | «Mazzo da 40 carte sarde» |
| Licenza (testo della pagina) | `{{self|cc-by-sa-4.0}}`; Commons mostra «Creative Commons Attribution-Share Alike 4.0», <https://creativecommons.org/licenses/by-sa/4.0> |
| Come è stata letta | wikitext e metadati dell'API di Commons (rendering del modello non aperto nel browser) |
| Formato | JPEG, 3010 x 1791 px, 0,87 MB, **fotografia** di un mazzo su un tavolo scuro, 4 righe x 10 colonne |

## Cosa va attribuito

CC BY-SA 4.0: **attribuzione obbligatoria** («GattoFigaro, Wikimedia Commons, CC BY-SA 4.0», con link alla pagina del file e alla licenza, indicando le modifiche) e **share-alike**: i derivati (ritagli, ridimensionamenti, ritocchi) vanno rilasciati con la stessa licenza o una compatibile. Servono una schermata di crediti nell'app e il testo della licenza nel repository.

**Dubbio da sciogliere (non verificato).** La licenza copre la fotografia. L'Asso di denari porta stampato «FABBRICA CARTE DA GIUOCO TEODOMIRO DAL NEGRO TREVISO»: il disegno è di un editore commerciale ancora attivo, e la pagina non dice nulla sui diritti del disegno né sul marchio.

## Carte e ritaglio

- **40 carte presenti**, tutte riconoscibili. Righe del foglio: **spade, coppe, bastoni, denari**; colonne: Asso, 2..7, **10 (Fante), 11 (Cavallo), 12 (Re)**: la numerazione stampata salta 8 e 9, come nel mazzo sardo. Le figure non hanno nome stampato.
- **Non ritagliabile in modo pulito in automatico.** Come le triestine: carte a contatto, fotografate con prospettiva e piccole rotazioni, riquadri che si sovrappongono di qualche decina di pixel; lo sfondo scuro non separa le carte. `scripts/crop_sheet.py --dark-bg` trova 40 riquadri ma con confini sbagliati in più punti. Serve un ritaglio manuale carta per carta con raddrizzamento.
- Carta nel foglio: circa 270-290 x 400-430 px.
- Stile spagnoleggiante, diverso dalle napoletane; figure intere (non a doppia testa); il 4 di ogni seme ha una scenetta.

## Qualità a 532 x 868 px

**Sfocata.** Fotografia, carte di circa 270 x 400 px utili: ingrandimento circa 2,1x, con sfocatura, ombre e riflessi. Semi e valori si leggono, i dettagli delle figure no. Anteprima (ritagli manuali approssimati di 5 carte): `docs/art/regionali/anteprime/sarde.webp`.

## Difetti

1. Fotografia con prospettiva, ombre, colori non uniformi.
2. Carte a contatto: ritaglio manuale.
3. Scritta e marchio Dal Negro sull'Asso di denari.
4. Rapporto 0,66-0,69: il più vicino alle napoletane (0,64) fra i mazzi esaminati.
5. Nessun dorso.

## Cosa manca

- Ritaglio manuale pulito delle 40 carte.
- Il dorso.
- Una risoluzione migliore (scansione o ridisegno).
- Chiarimento sui diritti del disegno e del marchio.
- Rimozione del marchio sull'Asso di denari.

## Peso

Il foglio pesa 0,87 MB; 40 carte WebP a 600x936 stimano 2-4 MB (non misurato: manca il ritaglio pulito).
