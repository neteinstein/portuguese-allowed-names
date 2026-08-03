# Allowed Names in Portugal (Nomes Permitidos em Portugal)

| Splash Screen | Name List | Name List | Settings |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/neteinstein/portuguese-allowed-names/blob/master/distribution/metadata/android/en-US/images/phoneScreenshots/1_splash.png" width="250" alt="Splash Screen" /> | <img src="https://github.com/neteinstein/portuguese-allowed-names/blob/master/distribution/metadata/android/en-US/images/phoneScreenshots/2_name_list.png" width="250" alt="Name List" /> | <img src="https://github.com/neteinstein/portuguese-allowed-names/blob/master/distribution/metadata/android/en-US/images/phoneScreenshots/3_name_list_filtered.png" width="250" alt="Filtered Name List" /> | <img src="https://github.com/neteinstein/portuguese-allowed-names/blob/master/distribution/metadata/android/en-US/images/phoneScreenshots/4_settings.png" width="250" alt="Settings" /> |


An Android app that lists the first names legally allowed for newborns in Portugal, based on
the official register published by the [Instituto dos Registos e do Notariado](https://irn.justica.gov.pt/en-gb/)
(IRN). Browse the full list, filter it by gender or initial letter, and see the matching count
update live. Available in English and Portuguese.

(PT) Uma app Android que lista os nomes próprios permitidos para recém-nascidos em Portugal,
com base na lista oficial publicada pelo IRN. Permite consultar a lista completa e filtrar por
género ou letra inicial, com a contagem de resultados atualizada em tempo real.

## Where the data comes from

The app downloads the official "Lista de Nomes Próprios" PDF published by the IRN, parses each
entry (name + allowed gender), and stores the result in a local database. The source URL is
configurable from **Settings** and defaults to the IRN's published PDF. Changing the URL (or
running the app for the first time) triggers a fresh download-and-parse pass that purges and
repopulates the database.

## Architecture

MVVM + Clean Architecture, split into three layers:

```
presentation/   Compose UI + ViewModels (splash, sync/loading, name list, settings)
domain/         Use cases, repository interfaces, and plain domain models — no Android deps
data/           Repository implementations, Room database, DataStore, remote fetch + PDF parser
di/             Koin modules wiring the above together
```

- **DI**: [Koin](https://insert-koin.io/)
- **Persistence**: [Room](https://developer.android.com/training/data-storage/room) (names) +
  [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) (settings)
- **UI**: Jetpack Compose + Navigation Compose, theme derived from the IRN site's palette
- **PDF parsing**: [pdfbox-android](https://github.com/TomRoush/PdfBox-Android)
- **Async**: Kotlin Coroutines & Flow

## Building & testing

```
./gradlew assembleDebug     # build the debug APK
./gradlew testDebugUnitTest # run the unit test suite
./gradlew lintDebug         # run Android Lint
```

## AI agent roles

Developer, QA, Architect, and Security Manager agent definitions for both Claude Code
(`.claude/agents/`) and GitHub Copilot (`.github/agents/`) live in this repo — see
[.github/AGENT_ORCHESTRATION.md](.github/AGENT_ORCHESTRATION.md) for how delegation and
triggering work on each platform.

## Privacy

The app does not collect any personal data — see [PRIVACY_POLICY.md](PRIVACY_POLICY.md) for
details.

(PT) A app não recolhe quaisquer dados pessoais — consulte
[PRIVACY_POLICY.md](PRIVACY_POLICY.md) para mais detalhes.

