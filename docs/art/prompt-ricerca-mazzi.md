# Prompt di ricerca dei mazzi regionali (per Grok)

Serve a cercare immagini **già esistenti e libere** dei mazzi che mancano (Piacentine, Siciliane e, per le Bergamasche, la fonte completa). Il risultato va confrontato con `docs/art/mazzi-regionali.md` e con la scheda `docs/art/napoletane/SOURCE.md`. Una carta entra nell'app solo con la licenza verificata sulla pagina del file (ADR-0008, ADR-0013, D14).

## Formato di riferimento (le napoletane)

- 40 carte singole, **una immagine per carta**, solo il disegno (niente cornice, niente cartoncino: lo script `scripts/prepare_napoletane.py` aggiunge il cartoncino bianco con angoli arrotondati e filetto scuro).
- Ordine e nomi: semi `denari`, `coppe`, `spade`, `bastoni`; per ogni seme Asso, 2, 3, 4, 5, 6, 7, Fante (8), Cavallo (9), Re (10). Numerazione da `01` a `40` in quest'ordine.
- Formato d'uscita nell'app: `card_<seme>_<valore>.webp` (es. `card_denari_asso`, `card_spade_fante`), 600×936 px, proporzione circa 0,64; il disegno viene ridimensionato dentro 532×868 px.
- Sorgente consigliata: JPG/PNG/SVG ad alta risoluzione (almeno 600 px di larghezza per carta), sfondo piatto, semi latini (denari, coppe, spade, bastoni), carte riconoscibili a dimensione piccola.
- In più: un **dorso** (`card_back`).
- Per ogni mazzo: scheda con fonte (URL della pagina del file), autore, licenza (letta sulla pagina del file, non sul riassunto), numero di carte, peso totale.

## Prompt

```
Cerco immagini libere (pubblico dominio, CC0, CC BY o CC BY-SA) di mazzi di carte regionali italiane da usare in un'app commerciale di Scopa. Mazzi richiesti: PIACENTINE e SICILIANE (e, se esiste, un secondo set completo per le BERGAMASCHE oltre a quello di Wikimedia Commons caricato da «Poulpy», CC BY-SA 3.0).

Cosa mi serve, per ogni mazzo:
1. 40 carte SINGOLE, una immagine per file (non fogli d'insieme), con i quattro semi latini (denari, coppe, spade, bastoni) e per ogni seme: Asso, 2, 3, 4, 5, 6, 7, Fante, Cavallo, Re. Per le siciliane indica se esistono anche le carte 2–7 (alcuni mazzi siciliani ne hanno solo 16: Asso, Cavallo, Donna, Re; qui mi servono le 24 mancanti, oppure un mazzo completo diverso).
2. Un dorso, se esiste.
3. Immagini ad alta risoluzione (almeno 600 px di larghezza per carta), solo il disegno, senza cornice né sfondo decorativo.
4. Per ogni file o gruppo: URL esatto della pagina del file, autore, LICENZA come scritta sulla pagina del file (non da un riassunto o da un motore di ricerca), data di pubblicazione, e se la licenza consente uso commerciale e modifiche (ridimensionare, ritagliare, mettere su cartoncino).

Fonti da controllare, in ordine: Wikimedia Commons (categorie dei mazzi regionali), Wikisource, Internet Archive, Gallica/BnF, Europeana, biblioteche digitali e musei con licenza aperta, archivi di mazzi storici il cui autore è morto da oltre 70 anni.

Escludi: mazzi commerciali recenti protetti da copyright (Dal Negro, Modiano, Masenghini) salvo licenza esplicita; immagini di cui non riesci a leggere la licenza sulla pagina originale; ridisegni di mazzi commerciali con marchio.

Formato della risposta: una tabella per mazzo con colonne Regione | N° carte singole | Autore | Licenza (testo esatto) | URL della pagina | Uso commerciale? | Note (carte mancanti, qualità, dorso). Poi una conclusione di 3 righe: quali mazzi sono utilizzabili subito, quali richiedono carte da generare o commissionare. Se un dato non è verificabile scrivi «non verificato», senza indovinare.
```
