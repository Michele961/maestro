# Verifica visiva della UI — guida per gli agenti

Come controllare che una schermata assomigli al mockup **senza un telefono**, con gli screenshot generati dai test (Roborazzi su Robolectric). Vale per ogni task che tocca la UI (piano in [`ui-redesign-plan.md`](ui-redesign-plan.md)).

## 1. Regola

Ogni modifica visibile della UI si chiude solo dopo aver **guardato lo screenshot** accanto al mockup di riferimento. Non basta che i test passino: un test di screenshot che non si guarda non verifica niente.

Mockup di riferimento in questa cartella:

| Schermata | Mockup | Test | Screenshot prodotti |
|---|---|---|---|
| Menu | `reference_menu_screen.jpg` | `HomeScreenScreenshotTest` | `home_screen.png`, `home_screen_active_match.png` |
| Preparati a giocare | `reference_pre_match_dialog.png` | `PreGameScreenScreenshotTest` | `pre_game.png` |
| Tavolo | `reference_table_screen.jpg` | `TableScreenScreenshotTest` | `table_mid_deal.png`, `table_hint.png` |
| Fine partita e review | `reference_review_screen.png` | `ReviewScreensScreenshotTest` | `post_match.png`, `game_review.png` |
| Dialoghi | `style_guide_sheet.jpg` | `DialogsScreenshotTest` | `dialog_*.png` |
| Libreria componenti | — | `ComponentCatalogScreenshotTest` | `component_catalog.png` |

I test sono in `app/android/src/test/kotlin/it/maestro/app/ui/screenshot/`.

## 2. Generare gli screenshot

```
./gradlew :app:android:testDebugUnitTest -Proborazzi.record=true
```

Le immagini finiscono in `app/android/build/outputs/roborazzi/`. Per una sola classe: `--tests '*TableScreen*'`.

Senza `-Proborazzi.record=true` i test girano ma non scrivono immagini. La CI le scrive sempre e le allega alla run come artifact **`screenshots`** (7 giorni): da lì si scaricano senza ricompilare.

## 3. Confrontarli con il mockup

Affiancare mockup e screenshot alla stessa altezza e guardarli insieme (lo strumento di lettura immagini dell'agente apre i PNG):

```python
from PIL import Image
R = 'app/android/build/outputs/roborazzi/'
pairs = [('docs/art/reference_table_screen.jpg', R + 'table_mid_deal.png')]
h = 1300
for ref, shot in pairs:
    ims = [Image.open(p).convert('RGB') for p in (ref, shot)]
    ims = [i.resize((int(i.width * h / i.height), h)) for i in ims]
    out = Image.new('RGB', (ims[0].width + ims[1].width + 20, h), 'white')
    out.paste(ims[0], (0, 0)); out.paste(ims[1], (ims[0].width + 20, 0))
    out.save('/tmp/confronto.png')   # nella sessione: la cartella scratchpad
```

Cosa guardare, in quest'ordine:
1. **Proporzioni e disposizione:** gli elementi stanno dove nel mockup e riempiono lo schermo allo stesso modo?
2. **Materiali:** pulsanti, targhe e pannelli usano le pelli (`NineSliceBox`), non sfumature piatte.
3. **Leggibilità:** niente testo chiaro su pergamena o scuro su legno; niente testi a capo spezzati male.
4. **Sovrapposizioni:** fumetti, bollini e carte non coprono altri elementi.
5. **Contenuto vero:** carte napoletane (mai semi francesi), testi da `strings.xml`.

Allegare il confronto (o descriverlo) nella PR.

## 4. Scrivere un nuovo test di screenshot

Modello minimo:

```kotlin
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// Application vuota: MaestroApp avvia Koin, che non serve per disegnare.
@Config(sdk = [35], qualifiers = "it-w411dp-h891dp-xxhdpi", application = Application::class)
class MiaSchermataScreenshotTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun mia_schermata_screenshot() {
        composeRule.setContent { MaestroTheme { MiaSchermata(/* stato di esempio */) } }
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/mia_schermata.png")
    }
}
```

- **Qualificatori:** `it-` per i testi italiani, `w411dp-h891dp-xxhdpi` è un telefono tipico. Per schermate lunghe aumentare l'altezza (`h1250dp`).
- **Stato:** passare dati finti costruiti nel test (es. `GameUiState(...)`), non il `ViewModel`. Per partite vere usare un `MatchLog` deterministico (seed fisso), come in `ReviewScreensScreenshotTest`.
- **Dialoghi:** `onRoot()` non vede le finestre dei dialoghi; usare `captureScreenRoboImage(...)` (`@OptIn(ExperimentalRoborazziApi::class)`), come in `DialogsScreenshotTest`.
- **Interazioni:** si può cliccare prima dello scatto (`onNodeWithTag(...).performClick()`), ad esempio per aprire la pausa o saltare al primo errore della review.
- **Comportamento:** affiancare allo screenshot almeno un test che verifica i callback (cosa succede premendo i pulsanti), con i test tag esistenti.
- I test di screenshot girano solo nella variante debug (in release manca l'activity di test di Compose): sono esclusi in `app/android/build.gradle.kts`.

## 5. Asset nuovi

Gli asset si generano con i prompt di [`asset-prompts.md`](asset-prompts.md) (li genera Michele) e si preparano con:

```
scripts/prepare_assets.py sheet <immagine> app/android/src/main/res/drawable-nodpi <nome1> <nome2> ... [--key magenta] [--size N]
```

Dopo la preparazione **controllare sempre un foglio di anteprima** con tutti gli oggetti e i loro nomi: l'ordine automatico può sbagliare con oggetti incolonnati, e il fondo magenta va usato quando l'oggetto contiene verde. Gli originali vanno in `docs/art/generated/`.

Per tutto ciò che deve essere preciso (carte, dorso, simboli, pattern) si usano immagini originali o il codice, non asset generati: vedi ADR-0013 e `docs/art/napoletane/SOURCE.md`.

## 6. Ambiente cloud (sessioni Claude Code sul web)

Il container non ha l'Android SDK. Prima della prima build:

```bash
S=$HOME/android-sdk; mkdir -p $S/cmdline-tools
curl -sSLo /tmp/clt.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip -qo /tmp/clt.zip -d $S/cmdline-tools && mv $S/cmdline-tools/cmdline-tools $S/cmdline-tools/latest
yes | $S/cmdline-tools/latest/bin/sdkmanager --sdk_root=$S "platforms;android-35" "build-tools;35.0.0" "platform-tools"
echo "sdk.dir=$S" > local.properties   # già in .gitignore
```

Se Gradle non riesce a scaricare alcuni jar da Maven Central (404 dal proxy), usare il mirror Google con uno script di inizializzazione **fuori dal repository**, in `~/.gradle/init.d/repos.gradle.kts`:

```kotlin
val mirror = "https://maven-central.storage-download.googleapis.com/maven2/"
fun org.gradle.api.artifacts.dsl.RepositoryHandler.useMirror() {
    removeIf { it is MavenArtifactRepository && it.url.toString().contains("repo.maven.apache.org") }
    maven { url = uri(mirror) }
}
settingsEvaluated {
    pluginManagement.repositories.apply { clear(); google(); maven { url = uri(mirror) }; gradlePluginPortal() }
    dependencyResolutionManagement.repositories.useMirror()
}
```

Robolectric scarica al primo avvio il runtime Android (`android-all-instrumented`, ~200 MB). Se il download fallisce, scaricarlo dallo stesso mirror in `~/.m2/repository/org/robolectric/android-all-instrumented/<versione>/` (la versione è nel messaggio d'errore).

Una build completa (`./gradlew check :app:android:assembleDebug -Proborazzi.record=true`) dura circa 1–2 minuti dopo il primo avvio.
