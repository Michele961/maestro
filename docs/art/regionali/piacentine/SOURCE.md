# Mazzo piacentino — scheda fonte (task F1G-10)

Stato: **40 carte ritagliabili, qualità sfocata a 532x868, licenza dichiarata dall'autore ma con un dubbio sul disegno originale.** Verifica del 2026-10-30. Nessuna immagine è nell'app: l'integrazione è F1G-11.

## Fonte

| Voce | Valore |
| --- | --- |
| File | `Carte piacentine al completo.jpg` su Wikimedia Commons |
| Pagina del file | <https://commons.wikimedia.org/wiki/File:Carte_piacentine_al_completo.jpg> |
| Autore | Florixc (`User:Florixc`), «Own work» |
| Descrizione sulla pagina | «scansione di tradizionali carte piacentine» |
| Data | 2009-09-07 (caricato 2009-11-19) |
| Licenza (testo della pagina) | `{{PD-self}}`: l'autore rilascia l'opera nel pubblico dominio, in tutto il mondo; dove non fosse possibile, concede a chiunque il diritto di usarla per qualsiasi scopo senza condizioni (testo standard del modello). Commons la mostra come «Public domain» |
| Come è stata letta | wikitext della pagina e metadati dell'API di Commons. Il rendering del modello non è stato aperto nel browser |
| Formato | JPEG, 3507 x 2417 px, 2,9 MB, foglio 4 righe x 10 colonne |
| Hash/controllo | scaricato con `Special:FilePath/Carte_piacentine_al_completo.jpg` il 2026-10-30 |

## Cosa va attribuito

Con `PD-self` non c'è obbligo di attribuzione né di condivisione alle stesse condizioni. Per correttezza si può citare «Florixc, Wikimedia Commons» nei crediti.

**Dubbio da sciogliere (non verificato).** Chi carica dichiara di rilasciare la *scansione*, ma il disegno scansionato è di un mazzo stampato da un editore che la pagina non nomina. Il foglio riproduce un mazzo moderno (colori e tratto da stampa industriale). Una scansione non crea diritti nuovi, ma se il disegno è ancora protetto la dichiarazione dello scansionatore non lo libera. Il tipo piacentino è tradizionale (ottocentesco), però l'edizione scansionata non è identificata. Prima di includerlo nell'app serve la decisione di Michele sul rischio, oppure un'altra fonte.

## Carte e ritaglio

- **40 carte singole presenti**, tutte riconoscibili: righe del foglio = denari, coppe, **bastoni, spade** (l'ordine del foglio ha i bastoni prima delle spade; lo script rimette l'ordine del progetto), colonne = Asso, 2..7, Fante, Cavallo, Re.
- Ritaglio fatto con `scripts/crop_sheet.py` (griglia regolare con ricerca dei confini; le carte hanno scarti di qualche pixel e alcune quasi si toccano). Il programma ha trovato 40 riquadri su 40.
- Carta intera nel foglio: circa 284 x 548 px (rapporto 0,52, più stretto delle napoletane a 0,64).
- La parte con il disegno, dentro il filetto stampato, è circa 170-240 x 510 px e cambia da carta a carta (l'Asso è più largo delle altre): per un ritaglio pulito sul disegno serve un secondo passo di rilevamento del riquadro stampato, non solo il bordo della carta.

## Qualità a 532 x 868 px

**Sfocata.** Il disegno originale è sotto i 250 px di larghezza, quindi l'ingrandimento a 868 px di altezza è circa 1,7-2,5x (LANCZOS). Il risultato mostra la retinatura della stampa e gli artefatti JPEG: i semi e i valori si leggono bene, le facce delle figure no; rispetto alla napoletana di base è chiaramente più morbida. Anteprima: `docs/art/regionali/anteprime/piacentine.webp` (riga alta = napoletana dell'app, riga bassa = piacentina con la stessa pipeline).

## Difetti

1. **Asso di denari con il cerchio centrale bianco**: nel foglio manca la moneta al centro dell'aquila (buco bianco). Va ritoccata (la moneta del 2 di denari è copiabile) o ridisegnata.
2. Figure a doppia testa con una linea di giunzione al centro, come nel tipo piacentino: normale, ma diverso dalle napoletane.
3. Inclinazione di 1-2 gradi e cartoncino con bordo scuro e residui dei vicini ai lati del ritaglio grezzo.
4. Rapporto 0,52: nel cartoncino 600x936 il disegno resta stretto, con molto bianco ai lati.
5. Nessun dorso.

## Cosa manca

- Il dorso (`card_back`): nel foglio non c'è.
- Il ritocco dell'Asso di denari.
- Una fonte a risoluzione più alta o un ridisegno per arrivare a una resa «netta» a 868 px.
- Il chiarimento sull'edizione scansionata (vedi dubbio sopra).

## Altre fonti piacentine viste

SVG parziali di Flanker e Argento (PD-self), ridisegni Inkscape di un mazzo Modiano: incompleti e legati a un marchio commerciale, non usati.

## Peso

Il foglio sorgente pesa 2,9 MB. Le 40 carte in WebP a 600x936 (qualità 88, stessa pipeline delle napoletane) pesano 3,8 MB misurati, contro i 2,8 MB delle 41 napoletane in `res`.
