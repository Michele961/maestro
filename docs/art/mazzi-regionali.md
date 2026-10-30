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

## Conclusione per ora

Non c'è ancora un secondo mazzo che si possa includere: la verifica che manca per ciascun candidato è la stessa.

1. Aprire la pagina di un file rappresentativo e copiare **licenza e autore** (come per Trocche100).
2. Contare le 40 carte singole dello **stesso autore e della stessa edizione**, con dorso.
3. Controllare leggibilità a dimensione piccola (matrice screenshot) e peso (il napoletano pesa ~6 MB; i mazzi vanno nell'APK).
4. Dove i file sono scansioni o ridisegni moderni di un editore, **assumere che non siano liberi** finché la pagina non lo dice.

## Per Michele

- Scegliere le prime tre regioni (N5). Suggerimento: partire da quelle con un set più completo (Piacentine, Romagnole) dopo la verifica sopra.
- Se vuoi generare tu i mazzi (ADR-0008 vale per tutto tranne il mazzo base), le carte nuove vanno in `docs/art/` con la stessa scheda e senza dipendere da fonti terze.
