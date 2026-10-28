# Bottega e wallet — dossier

Stato: **bozza** (da approvare) · 2026-10-28 · task collegati: F1R-23 (dossier), F1R-30 (da creare dopo l'approvazione), F1R-14, F2-11
Procedimento: `docs/app/features/README.md`. Va approvato da Michele prima che i task partano. Le proposte non approvate sono segnate **[proposta]**.

## 1. Cosa fa per il giocatore

Gli fa spendere i Crediti Maestro guadagnati giocando per personalizzare il proprio Circolo: tavolo, dorso delle carte, ritratto. È un premio, non un obbligo: non vende nulla che dia vantaggio in partita (`docs/vision.md` §8). Ci passa ogni tanto, quando ha crediti da spendere o vuole cambiare aria.

## 2. Com'è oggi

**Codice** (`feature/shop`, `data/local/cosmetics`, `domain/wallet`): `ShopScreen.kt` **532 righe**, `ShopViewModel.kt` 130, `CosmeticModels.kt` 137 (catalogo), `domain/wallet` (modelli e interfaccia già nel dominio da F1R-14b). Si apre dal pulsante dei crediti in Home. Tre tab (Tavoli, Dorsi, Stemmi), una scheda per oggetto con prezzo e pulsante. Catalogo: 4 tavoli (verde gratuito, rosso 40, blu 50, noce 60), 3 dorsi (classico gratuito, rosso 35, oro 50), 3 ritratti (base gratuito, alt 40, esperto 75).

Come si guadagnano i crediti oggi (F1R-14, `RewardEngine`): benvenuto 50, sfida del giorno 15/20/25, lezioni 5, missioni. Le partite non danno crediti (D19). Nessuna spesa in partita (suggerimenti, revisione, ripetizione puzzle non addebitano nulla).

| # | Problema | Dove |
| --- | --- | --- |
| B1 | **Le anteprime mentono**: i tre tavoli di colore usano la stessa immagine, i tre dorsi la stessa `card_back`; il colore vero è applicato altrove con filtri in `TableScreen` e `CardBackView`. Il giocatore compra senza vedere cosa compra | `previewDrawableRes` |
| B2 | **I colori sono ricavati con `when` su id stringa** in tre posti (tavolo, dorso, avatar in Home): aggiungere un oggetto vuol dire toccare UI sparse. Il catalogo dovrebbe portare con sé come si disegna | `TableScreen`, `CardView`, `HomeParts` |
| B3 | **Nessuna conferma e nessun anteprima in contesto**: un tocco spende i crediti e equipaggia subito; non si prova prima su un mini-tavolo, non si può annullare | `unlockAndEquip` |
| B4 | **Feedback povero**: un testo «Elemento sbloccato ed equipaggiato!»; nessun movimento, nessun suono, il saldo cambia senza che si veda scalare | `shop_unlock_success` |
| B5 | **Messaggi come id stringa**: il ViewModel scrive `"insufficient_funds"` o l'id dell'oggetto nello stato e la UI li interpreta; la descrizione della transazione è italiano nel Kotlin (`"Sblocco cosmetico: …"`), contro ADR-0007 | `ShopViewModel` |
| B6 | **Obiettivo di risparmio assente**: con crediti insufficienti il pulsante dice solo «CREDITI INSUFFICIENTI»; non dice quanto manca né come guadagnarli | `shop_action_insufficient_funds` |
| B7 | **I ritratti non seguono la scala**: lo «Stemma Esperto» (75) si compra con crediti, mentre i titoli di grado si guadagnano battendo gli avversari (F1R-18e); il nome «Stemmi» è anche l'icona dello scudo | catalogo |
| B8 | **Il movimento del wallet non si vede**: nessuna cronologia dei crediti (guadagni e spese), benché il registro esista (append-only, ADR-0006) | — |
| B9 | **Eccezione `UI_IMPORTS_DATA`** (UI e ViewModel importano `data.*`) in attesa di F1R-23 | `ArchitectureRulesTest` |
| B10 | **Acquisti reali e annunci**: `IAP_PACK`, `REWARDED_AD` esistono come causali ma nulla le usa; sono Fase 2 (F2-08…F2-10, D12) e non vanno anticipati | — |

## 3. Come deve diventare **[proposta]**

Resta una schermata sola, con tre tab, ma ogni oggetto si vede come sarà.

1. **Intestazione.** Saldo ben visibile («120 Crediti») e un tocco apre **«Movimenti»**: elenco dei guadagni e delle spese (data, motivo in italiano, importo). Il registro c'è già (B8).
2. **Anteprima vera.** Ogni scheda mostra l'oggetto **disegnato dal catalogo stesso**: il tavolo come mini-feltro, il dorso come carta coperta, il ritratto incorniciato. Una sola funzione di disegno per oggetto, usata dalla Bottega, dalla Home e dal tavolo (B1, B2).
3. **Provalo.** Un tocco sulla scheda apre un riquadro con il mini-tavolo (carte e dorsi veri) con l'oggetto applicato; da lì **Equipaggia** (se posseduto), **Sblocca per N** (se acquistabile) o «Ti mancano N crediti» con un rimando a come guadagnarli (missioni, sfida del giorno) (B3, B6).
4. **Acquisto.** Lo sblocco chiede conferma nel riquadro (non nella scheda), poi: il saldo scala con un conteggio, l'oggetto compare equipaggiato, un suono breve e una battuta del Maestro («Bel tavolo. Si vede subito chi ha gusto.»), tutto dalla regia con i tempi di `MotionSpec` (B4).
5. **Ritratti.** Restano acquistabili, ma il ritratto «da Maestro» (oggi «Stemma Esperto») diventa **sbloccabile battendo l'ultimo avversario** invece che a 75 crediti; gli altri restano a crediti. In questo modo i ritratti non contraddicono la scala. Se non lo vuoi, resta com'è oggi (B7).
6. **Nessun vantaggio in partita** e nessun oggetto a tempo o casuale (niente casse a sorpresa).

**Stati da coprire con screenshot** (matrice a 4 configurazioni): tab Tavoli/Dorsi/Ritratti; oggetto posseduto, equipaggiato, acquistabile, non acquistabile; riquadro «Provalo» nei tre casi; elenco Movimenti vuoto e pieno; font al 130%.

## 4. Contratti

- **`CosmeticItem`** nel dominio (`domain/cosmetics`), senza risorse Android; la UI associa a ogni id il proprio disegno con una mappa tipizzata in `designsystem` (`CosmeticStyle`), così il dominio non conosce `R` e il tavolo, la Home e la Bottega leggono la stessa fonte. Le impostazioni salvano ancora l'id dell'oggetto equipaggiato (nessuna migrazione).
- **`ShopViewModel`** espone stato tipizzato (`ShopEvent` sealed: `Unlocked(item)`, `NotEnoughCredits(missing)`), mai stringhe; la descrizione della transazione è una chiave, non un testo (ADR-0007).
- **`WalletRepository.observeTransactions()`** (lettura del registro) per i Movimenti; nessun cambio di schema.
- **Acquisto**: sempre tramite `WalletRepository.spendCredits` (ADR-0006), idempotente per `referenceId`; nessuna logica di ricompensa nella Bottega (ricompense solo da `RewardEngine`).
- **Regia**: l'effetto di acquisto passa dal `TableDirector`/`MotionSpec` della schermata; nessun `delay` locale.
- Nessuna dipendenza nuova, nessun cambio a `core:*`. IAP e annunci restano fuori (Fase 2).

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-30a | Catalogo nel dominio e stile dal catalogo | `CosmeticItem` senza `R`; `CosmeticStyle` in `designsystem`; tavolo, dorso e ritratto leggono la stessa fonte; test sul disegno di ogni id e fine delle eccezioni `UI_IMPORTS_DATA` della Bottega |
| F1R-30b | Anteprime vere e riquadro «Provalo» | Scheda con oggetto disegnato; riquadro con mini-tavolo; matrice a 4 configurazioni |
| F1R-30c | Acquisto con conferma, eventi tipizzati e feedback | `ShopEvent`; conferma nel riquadro; saldo animato, suono, battuta del Maestro; nessuna stringa nel ViewModel; test su fondi insufficienti e doppio tocco |
| F1R-30d | Movimenti | `observeTransactions()`; elenco con motivo localizzato; test su vuoto e pieno |
| F1R-30e | Ritratto da Maestro dalla scala | Sblocco dalla vittoria sull'ultimo avversario (`RewardEngine`/`OpponentRepository`); test su una volta sola |
| F1R-30f | Divisione del file e matrice finale | Nessun file sopra 400 righe; screenshot degli stati di §3 |

## 6. Domande per Michele

1. **Provalo prima di comprare**: ti torna il riquadro con il mini-tavolo, o preferisci l'acquisto diretto come oggi ma con anteprime vere?
2. **Conferma d'acquisto**: la vuoi sempre, o solo oltre una certa cifra?
3. **Ritratto da Maestro**: sbloccato battendo l'ultimo avversario invece che a 75 crediti? Oppure resta a crediti?
4. **Movimenti**: li vuoi nella Bottega, o li considero poco utili per ora?
5. **Catalogo**: i dieci oggetti attuali e i prezzi (35…75) sono provvisori. Li confermi, o cambi quantità e prezzi? Nuovi tavoli o dorsi richiedono grafica che non posso inventare: me li indichi tu.
6. **Dove si guadagnano i crediti**: oggi solo benvenuto, sfida del giorno, lezioni e missioni (D19: le partite non ne danno). Con la Bottega così, un giocatore che compra tutto impiega alcune settimane: ti sta bene, o va ritoccata l'economia nel dossier Missioni?
7. **Fuori da questa feature**: confermi che pacchetti di crediti, annunci e abbonamento restano alla Fase 2 (F2-08…F2-10)?
