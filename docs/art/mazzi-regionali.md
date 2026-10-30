# Mazzi regionali — schede fonte (bozza)

Stato: **bozza di ricerca, nessun mazzo approvato** · 2026-10-30 · task F1G-10 (macro M4, decisione D24)

Regola: un mazzo entra nell'app solo con una scheda completa (fonte, autore, licenza verificata sulla pagina del file, 40 carte, semi latini, leggibilità, peso) e dopo che Michele ha scelto le regioni (domanda N5). Modello: `docs/decisions/0013-*` e `docs/art/napoletane/SOURCE.md`. Le grafiche che servono le genera o le procura Michele (D14).

## Cosa è stato verificato (Wikimedia Commons, pagine di categoria, 2026-10-30)

Letti solo gli elenchi delle categorie; **nessuna pagina di singolo file è stata aperta**, quindi autore e licenza di ogni carta sono **non verificati**. L'unico testo di licenza visto è quello generico del sito («Files are available under licenses specified on their description page»).

| Mazzo | Categoria Commons | File | Set completo di 40 carte singole? |
| --- | --- | --- | --- |
| Napoletane | `Category:Naples_deck` | 61 | Sì, dai nomi dei file («01 Asso di denari» … «40 Dieci di Bastoni»); è il mazzo base già in uso (ADR-0013, autore Trocche100, `PD-user-it`) |
| Piacentine | `Category:Piacenza_deck` | 28 | No: miniature «Armanino» per seme e un set SVG parziale |
| Siciliane | `Category:Sicily_deck` | 26 | No: solo Asso, Donna, Cavallo, Re per seme |
| Romagnole | `Category:Romagna_deck` | 46 | Incerto: file PNG con titoli in polacco (Asso … Re), da contare carta per carta |
| Bresciane | `Category:Brescia_deck` | 58 | No come 40 carte: 52 carte (13 valori per seme), non adatte a una Scopa a 40 |
| Toscane, Trevisane | nessuna sottocategoria trovata (Trevisane forse in `Category:Venetian_pattern`, non aperta) | — | Non verificato |
| Bergamasche, Genovesi, Sarde, Trentine, Triestine | `Category:Bergamo_deck` (47), `Category:Genoese_deck`, `Category:Sardinia_deck` (7), `Category:Trentine_pattern`, `Category:Trieste_pattern` (8) | — | Non aperte: non verificato |
| Piemontesi | `Category:Piedmontese_tarot` | — | Sono tarocchi, non un mazzo da 40 carte |

Altre piste (non verificate): file singoli di carte piacentine su Commons segnalati come pubblico dominio da un mirror, e raccolte di musei (Smithsonian, National Gallery of Art) con carte italiane antiche in pubblico dominio.

## Verifica sulle pagine dei file (2026-10-30)

Aperte le pagine di 2 file per mazzo (1 per i genovesi) e lette tutte le categorie. La licenza è **la dichiarazione di chi ha caricato il file**: nulla conferma in modo indipendente lo stato dei diritti sul disegno originale. I conteggi delle carte vengono dai nomi dei file.

| Mazzo | Autore e licenza (come mostrati) | Carte singole | Completo? | Note |
| --- | --- | --- | --- | --- |
| Napoletane | Trocche100, `PD-user` (pubblico dominio dall'autore) | 40 numerate, dorso `Carte_Napoletane_retro.jpg` | Sì | È il mazzo base (ADR-0013) |
| Bergamasche | «Poulpy», **CC BY-SA 3.0** | 40 (Asso, 2–7, Fante, Cavallo, Re per seme) | Sì, dai nomi | Foto ritagliate di un mazzo non identificato; attribuzione e condivisione alle stesse condizioni; nessun dorso |
| Piacentine | PD-self, autori diversi (Flanker, Argento) | 15 SVG parziali | No | Ridisegni Inkscape di un mazzo Modiano (marchio commerciale); foto di partenza non su Commons |
| Romagnole | «Marteau et Georges» 1850, **CC0** | 39, mancano i Re di Denari e Bastoni | No | Scansioni Gallica (BnF); Commons le segna «da controllare» |
| Siciliane | OppidumNissenae, CC BY-SA 4.0 | 16 (Asso, Cavallo, Donna, Re) | No | Mancano i numeri 2–7 |
| Trentine, Triestine, Sarde, Genovesi, Trevigiane | vari (PD-self o CC BY-SA 4.0) | nessuna carta singola | No | Solo immagini d'insieme o simboli dei semi |

## Conclusione per ora

Il solo secondo mazzo che ha già 40 carte singole è il **bergamasco**, con licenza CC BY-SA 3.0: usarlo richiede attribuzione («Poulpy») e rilascio delle immagini adattate alla stessa licenza; da valutare con Michele se è accettabile per un'app a pagamento. Le romagnole sarebbero CC0 ma incomplete e con un controllo di Gallica in sospeso. Tutti gli altri richiedono carte generate o procurate da Michele.

## Cosa serve ancora, per qualsiasi mazzo

1. Aprire la pagina di un file rappresentativo e copiare **licenza e autore** (come per Trocche100).
2. Contare le 40 carte singole dello **stesso autore e della stessa edizione**, con dorso.
3. Controllare leggibilità a dimensione piccola (matrice screenshot) e peso (il napoletano pesa ~6 MB; i mazzi vanno nell'APK).
4. Dove i file sono scansioni o ridisegni moderni di un editore, **assumere che non siano liberi** finché la pagina non lo dice.

## Per Michele

- Scegliere le prime tre regioni (N5) e come ottenerle: Bergamasche (CC BY-SA 3.0, da accettare), oppure carte generate o procurate da te per Piacentine, Romagnole, Siciliane, Trentine e le altre.
- Se vuoi generare tu i mazzi (ADR-0008 vale per tutto tranne il mazzo base), le carte nuove vanno in `docs/art/` con la stessa scheda e senza dipendere da fonti terze.
