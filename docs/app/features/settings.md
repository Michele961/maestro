# Impostazioni e audio — dossier

Stato: **bozza** (da approvare) · 2026-10-28 · task collegati: F1R-23 (dossier), F1R-31 (da creare dopo l'approvazione), F1R-20 (audio a tre livelli), F1R-09
Procedimento: `docs/app/features/README.md`. Va approvato da Michele prima che i task partano. Le proposte non approvate sono segnate **[proposta]**.

## 1. Cosa fa per il giocatore

Gli lascia accordare il gioco ai suoi gusti (suoni, velocità, quanto parla il Maestro, aiuti al tavolo) e, in fondo, gli dice chi è chi fa l'app. Lo apre di rado, ma quando lo apre deve trovare subito la cosa che cerca e capire cosa fa senza provarla.

## 2. Com'è oggi

**Codice**: `feature/settings/ui/SettingsDialog.kt` (270 righe, un `AlertDialog` che scorre), `data/local/datastore/SettingsRepository.kt` (287), `designsystem/audio/SoundManager.kt` (116, `SoundPool`, 6 effetti `.wav`). Si apre dall'icona in Home. Voci: effetti sonori (interruttore), velocità di gioco (3 livelli), «Chiacchiere del Maestro» (4 livelli), «Mostra la corsa ai punti» (interruttore) e, solo con gli strumenti di sviluppo, crash di prova e partita rapida all'ultima mano.

| # | Problema | Dove |
| --- | --- | --- |
| S1 | **Un solo interruttore per tutto l'audio**; `game-feel.md` §7 prevede tre livelli con volume separato (musica, ambiente, effetti) e musica che si abbassa quando parla il Maestro. Non esistono né musica né ambiente | `SoundManager`, F1R-20 |
| S2 | **Tutti i suoni sono nel gestore ma non passano da un livello «effetti»**: `SoundManager` ha solo acceso/spento, senza volume; `game-feel.md` §7 vuole che passino tutti dal livello effetti | `SoundManager` |
| S3 | **Vibrazione**: `game-feel.md` §5 prevede un'impostazione per spegnerla; nell'app c'è del feedback tattile nelle celebrazioni (Scopa, riepilogo smazzata) ma nessuna voce per disattivarla | `SweepCelebrationOverlay`, `DealSummaryDialog` |
| S4 | **Lingua salvata ma non applicata**: `AppSettings.language` esiste (`"system"`) ma nessuna schermata la cambia e nessun codice la legge. L'app ha già `strings.xml` it/en (ADR-0007) | `SettingsRepository` |
| S5 | **Un dialogo che scorre non basta**: sei-sette voci senza raggruppamento, nessuna spiegazione tranne due sottotitoli; con font al 130% e schermo piccolo la fila degli ultimi controlli esce dal dialogo | `SettingsDialog` |
| S6 | **Voci d'aiuto sparse**: la corsa ai punti sta qui, i suggerimenti e il Check sono altrove (`coach-in-game.md`); il giocatore non sa dove si regola «quanto mi aiuta il Maestro» | — |
| S7 | **Strumenti di sviluppo nel dialogo di tutti**: sono nascosti da `DeveloperTools.isEnabled`, ma vivono nello stesso file; il crash di prova e la partita rapida non hanno posto nella UI del giocatore | `SettingsDialog` |
| S8 | **Mancano le voci attese in un gioco da Play Store**: informazioni sull'app (versione, crediti, licenze), privacy e cancellazione dei dati, gestione del consenso (UMP, Fase 2), contatti/segnala un problema | — |
| S9 | **Eccezione `UI_IMPORTS_DATA`** su `SoundManager` (UI importa `data.*`/audio direttamente) in attesa di F1R-20 | `ArchitectureRulesTest` |

## 3. Come deve diventare **[proposta]**

Da dialogo a **schermata** raggiungibile dall'icona di Home, a sezioni con titolo. Le impostazioni si applicano subito (niente «Salva»), con un'anteprima dove ha senso (premere un volume fa sentire il suono).

1. **Audio.** Tre cursori: **Effetti**, **Musica**, **Ambiente**, ciascuno con il proprio volume e un tocco sull'icona per spegnerlo. Una nota breve: «La musica si abbassa quando parla il Maestro.» Sopra, un interruttore **Vibrazione** (S1, S2, S3). Finché musica e ambiente non esistono (assets di Michele), i due cursori non si mostrano: niente controlli per cose assenti.
2. **Gioco.** Velocità di gioco, **corsa ai punti** e un solo posto per «quanto aiuta il Maestro»: **Chiacchiere** (come oggi, 4 livelli). Ogni voce con una riga che dice cosa cambia (S5, S6).
3. **Lingua.** Sistema / Italiano / English. Si applica subito con `AppCompatDelegate.setApplicationLocales` e legge `AppSettings.language`, che oggi non fa nulla (S4).
4. **Informazioni.** Versione dell'app, crediti, licenze open source, privacy e come cancellare i propri dati, «Segnala un problema» (S8). Il consenso pubblicitario arriva con la monetizzazione, non adesso.
5. **Sviluppo.** Sezione separata, visibile solo se `DeveloperTools.isEnabled` (crash di prova, partita rapida). Resta fuori dalla matrice di screenshot dell'utente (S7).

**Stati da coprire con screenshot** (matrice a 4 configurazioni): schermata con effetti spenti, volumi diversi, lingua selezionata, sezione sviluppo visibile e nascosta, font al 130%.

## 4. Contratti

- **`AppSettings`** guadagna `effectsVolume`, `musicVolume`, `ambientVolume` (0–100, default alti per gli effetti e bassi per la musica) e `hapticsEnabled`; `soundEnabled` resta come interruttore generale per non rompere le preferenze salvate (nessuna migrazione distruttiva, regola 10 di `docs/app/README.md`).
- **`AudioMixer`** nel dominio dell'app (`domain/audio`): tre livelli e la regola «ducking» come funzione pura (`duckMusicWhile(maestroSpeaking)`), testabile senza Android. Il `SoundManager` ne è l'implementazione Android.
- **Voce del Maestro → ducking**: l'abbassamento lo comanda la regia (`TableDirector`) quando il Maestro parla; nessun tempo nuovo: durate da `MotionSpec`.
- **Lingua**: scritta in `AppSettings`, applicata dalla `Application`/`MainActivity` all'avvio e al cambio; nessun testo in Kotlin (ADR-0007).
- **UI/ViewModel**: nessun import di `data.*` (si toglie l'eccezione `UI_IMPORTS_DATA` del `SoundManager`); un `SettingsViewModel` espone lo stato e gli eventi.
- Nessuna dipendenza nuova (`SoundPool` e `MediaPlayer` bastano); gli asset audio di musica e ambiente li fornisce Michele (F1R-20).

## 5. Task proposti (dopo l'approvazione)

| ID | Titolo | Criteri principali |
| --- | --- | --- |
| F1R-31a | Schermata Impostazioni e `SettingsViewModel` | Da dialogo a schermata a sezioni; stato tipizzato; matrice a 4 configurazioni; strumenti di sviluppo in sezione separata |
| F1R-31b | Volumi e vibrazione | `AppSettings` con i nuovi campi; `AudioMixer` puro con test sul ducking; tutti i suoni sul livello effetti |
| F1R-31c | Lingua | Selettore Sistema/it/en applicato subito; test sul valore salvato e sul default |
| F1R-31d | Informazioni, privacy e segnalazioni | Versione, licenze, privacy, cancellazione dati, segnala un problema; testi in it/en |
| F1R-20 | Musica e ambiente | Con gli asset di Michele; cursori mostrati solo se gli asset esistono |
| F1R-31e | Divisione dei file e matrice finale | Nessun file sopra 400 righe; screenshot degli stati di §3 |

## 6. Domande per Michele

1. **Schermata o dialogo**: va bene passare a una schermata a sezioni, o preferisci tenere il dialogo e aggiungere solo le voci?
2. **Lingua**: la vuoi tra le impostazioni (Sistema/Italiano/English), o per ora l'app segue il telefono?
3. **Vibrazione**: la vuoi come interruttore a parte?
4. **Volumi**: tre cursori separati come in `game-feel.md` §7 (dopo che esistono musica e ambiente), o ti basta un volume generale più l'interruttore?
5. **Informazioni e privacy**: serve per il Play Store; il testo della privacy e il contatto per le segnalazioni li fornisci tu? Io preparo il posto e non invento il contenuto legale.
6. **Musica e ambiente**: quando prevedi di fornire gli asset (F1R-20)? Nel frattempo i due cursori non compaiono.
7. **«Chiacchiere»**: restano nelle Impostazioni, oppure vuoi un accesso rapido anche al tavolo?
