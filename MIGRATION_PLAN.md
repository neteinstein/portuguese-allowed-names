# KMP + Compose Multiplatform Migration Plan

Status: **in progress, not merged to `main`**. All work happens on
`claude/kmp-cmp-migration-plan-m6dlst` (and follow-up branches based on it)
until the web + Android targets are verified stable side by side with the
current Android-only app.

## 1. Goal

Turn Pick-A-Name from a single Android Gradle module into a Kotlin
Multiplatform / Compose Multiplatform project, following the module shape
used by [neteinstein/loopgain](https://github.com/neteinstein/loopgain)
(shared `composeApp` module + thin per-platform app shells), extended with a
**web target** (Compose Multiplatform for Web, Kotlin/Wasm) that deploys to
**GitHub Pages**. Android stays the primary, fully-featured target; iOS is
structured for from day one (loopgain's pattern) but is not this repo's
priority since there's no iOS device/App Store presence today; web gets a
real, working feature set, not just a demo shell.

## 2. Current app, in one page

- Single module: `app/`, package `org.neteinstein.pickaname`.
- Clean-ish layered structure already: `domain` (pure Kotlin, verified zero
  `android.*` imports), `data`, `di` (Koin), `presentation` (Compose +
  Navigation-Compose + ViewModel).
- What the app does: downloads a PDF of legally allowed Portuguese given
  names, extracts its text, parses name/gender pairs, stores them in Room,
  and lets the user browse/search/filter them. Settings let the user change
  the source URL, refresh cadence, and search engine used from "look this
  name up online" links.
- Platform-coupled pieces that block a straight copy into `commonMain`:

  | Concern | Current impl | Android-only? |
  |---|---|---|
  | HTTP client | OkHttp (`NameListRemoteDataSource`) | Yes (JVM) |
  | PDF text extraction | `pdfbox-android` (`PdfTextExtractor`) | Yes (Android-specific native init) |
  | Local DB | Room (`AppDatabase`, `NameDao`) | KMP-capable via Room 2.7+ w/ Bundled SQLite, but **no wasmJs support** |
  | Settings persistence | AndroidX DataStore Preferences | KMP-capable, but wasmJs support is still immature/experimental |
  | DI | Koin (`di/*Module.kt`) | Already multiplatform |
  | Navigation | `androidx.navigation:navigation-compose` | Android/JVM only — CMP has its own multiplatform Navigation artifact |
  | "Open in browser" (search engine links) | `androidx.browser` Custom Tabs | Android-only concept; web opens a new tab, no CCT equivalent |
  | Splash screen | `androidx.core:core-splashscreen` | Android-only API |
  | Release pipeline | Signed APK/AAB → GitHub Release + Play Store | Unaffected; web gets its own pipeline |

## 3. Target module layout

Follows loopgain's naming so the two repos stay easy to cross-reference:

```
portuguese-allowed-names/
├── composeApp/                  # shared KMP module (was `app/`)
│   ├── src/
│   │   ├── commonMain/kotlin/org/neteinstein/pickaname/
│   │   │   ├── domain/          # unchanged, moved as-is (already pure Kotlin)
│   │   │   ├── data/            # repositories + mappers, using expect/actual for I/O
│   │   │   ├── di/              # Koin modules, common bindings
│   │   │   └── ui/              # screens, components, theme, nav, viewmodels
│   │   │       (renamed from `presentation/` to match loopgain's `ui/`)
│   │   ├── commonTest/kotlin/…  # parser/mapper/usecase tests (pure Kotlin today, move as-is)
│   │   ├── androidMain/kotlin/… # Android actuals: Room/SQLite driver, DataStore, OkHttp
│   │   │                        # engine, PdfBox-Android extractor, CCT browser launcher
│   │   ├── wasmJsMain/kotlin/…  # Web actuals: Ktor-JS engine, IndexedDB/localStorage-backed
│   │   │                        # settings + name store, pdf.js-backed extractor, window.open
│   │   ├── iosMain/kotlin/…     # stubbed from day 1 (loopgain parity); not built/signed yet
│   │   └── androidUnitTest, wasmJsTest/  # platform-specific test source sets as needed
│   └── build.gradle.kts
├── androidApp/                  # thin Android shell: MainActivity, manifest, launcher icons
│   └── build.gradle.kts
├── webApp/                      # wasmJs browser entry point: main.kt (ComposeViewport) + index.html
│   └── build.gradle.kts          (or folded into composeApp's own wasmJs target — see §5.1)
├── iosApp/                      # Xcode project shell (loopgain parity, deferred build-out)
├── distribution/                # unchanged (Play Store listing metadata)
├── .github/workflows/
│   ├── release.yml              # unchanged: Android signed release on merge to main
│   ├── pr-checks.yml            # extended: also run commonTest + wasmJsBrowserTest
│   └── deploy-web.yml           # NEW: build wasmJs distribution, deploy to GitHub Pages
├── gradle/libs.versions.toml    # + Compose Multiplatform, Ktor, Koin (multiplatform artifacts),
│                                #   multiplatform-settings, kotlinx-datetime, Room KMP
└── settings.gradle.kts          # include(":composeApp", ":androidApp", ":webApp")
```

Whether the wasmJs entry point is its own `:webApp` module or just a
`wasmJs { browser() }` target block inside `composeApp` is a style choice —
loopgain doesn't need to answer it (Android/iOS only). This plan uses a
**separate `:webApp` module** for a cleaner GitHub Pages build step (`./gradlew
:webApp:wasmJsBrowserDistribution` produces exactly the static site, nothing
else), mirroring how `:androidApp` is kept thin. If it proves unnecessary
overhead once the web target is live, both can be collapsed later — that's a
non-breaking follow-up.

## 4. Dependency replacement map

| Layer | Today | Target (common) | Android actual | Web (wasmJs) actual |
|---|---|---|---|---|
| DI | Koin (android artifacts) | `koin-core` + `koin-compose` (multiplatform) | `koin-android` for `androidContext()` | `koin-core` only |
| Networking | OkHttp | `ktor-client-core` | `ktor-client-okhttp` | `ktor-client-js` (or `-wasm-js` engine) |
| Settings | DataStore Preferences | `russhwolf/multiplatform-settings` (`Settings` interface) | `DataStoreSettings` or `SharedPreferencesSettings` | `StorageSettings` (browser `localStorage`) |
| DB | Room + KSP | `Room` 2.7+ KMP with `RoomDatabase.Builder` in each `actual` | `Room` + bundled SQLite (`androidx.sqlite:sqlite-bundled`) — same as today | **No official Room wasmJs target yet.** Use a hand-rolled `expect class NameStore` backed by IndexedDB (via `kotlinx-browser`) storing the parsed name list as JSON; see §6 risk R1 |
| PDF → text | `pdfbox-android` | `expect fun extractPdfText(bytes: ByteArray): String` | keep `pdfbox-android` (works today) | delegate to `pdf.js` loaded from a CDN via `external`/JS interop (see §6 risk R2) |
| Navigation | `navigation-compose` (AndroidX) | `org.jetbrains.androidx.navigation:navigation-compose` (CMP multiplatform build of Navigation, API-compatible) | same | same |
| "Open URL" | `androidx.browser` CCT | `expect fun openUrl(url: String)` | Custom Tabs intent | `window.open(url, "_blank")` |
| Splash | `core-splashscreen` | n/a (common) | keep as Android-only, lives in `androidApp` | web has no splash concept — CSS loading state in `index.html` instead |
| Coroutines | `kotlinx-coroutines-{core,android}` | `kotlinx-coroutines-core` | `+android` for `Dispatchers.Main` | ships its own `Dispatchers.Main` via `kotlinx-coroutines-core-wasm-js`, no extra artifact |
| Persisted preferences keys, `RefreshPeriod`, `SearchEngine`, etc. | n/a | unchanged, pure Kotlin | — | — |

Room's official KMP support (`androidx.room:room-runtime` 2.7.0+) currently
targets Android, JVM, and Kotlin/Native — **wasmJs is not on the supported
target list**. This is the single biggest platform gap and is called out
again in §6.

## 5. Phased roadmap

Each phase is its own PR against `claude/kmp-cmp-migration-plan-m6dlst` (or
stacked branches off it), keeps the app buildable at every step, and is
independently reviewable. Nothing merges to `main` until Phase 6 is signed
off manually.

### Phase 0 — Scaffolding (this session)
- Add `composeApp` KMP module (Android + wasmJs targets configured;
  iosArm64/iosSimulatorArm64 targets declared but not fleshed out).
- Move `settings.gradle.kts`, version catalog, root `build.gradle.kts` to
  the new module list.
- Move the **domain** layer into `commonMain` verbatim (it already has zero
  Android imports — free win, validates the toolchain).
- Add a placeholder shared `App()` composable + theme so Android and wasmJs
  both build and show *something*, proving the Gradle/Compose Multiplatform
  wiring end-to-end before any real feature is ported.
- Stand up `deploy-web.yml` against that placeholder so the Pages pipeline
  is validated early, independent of feature completeness.

### Phase 1 — DI, settings, networking
- Introduce `koin-core`/`koin-compose` multiplatform modules; keep
  `koin-android` only in `androidMain` for `androidContext()`.
- Replace DataStore with `multiplatform-settings`; port
  `SettingsRepositoryImpl` to `commonMain` against the `Settings` interface.
- Replace OkHttp with Ktor client behind the existing
  `NameListRemoteDataSource` shape (same method signature, new
  implementation), with per-platform engines.

### Phase 2 — PDF parsing + name storage
- Introduce `expect fun extractPdfText(bytes: ByteArray): String` /
  `actual` per platform (pdf.js on web — see risk R2).
- Introduce a common `NameStore` abstraction so `data/repository` code
  doesn't hard-depend on Room; `actual` implementations: Room on Android,
  IndexedDB-backed store on web (risk R1).
- `NameListTextParser` and mappers move to `commonMain` untouched (already
  pure Kotlin, already unit-tested).

### Phase 3 — UI port
- Move `presentation/` → `commonMain/…/ui/`, replacing:
  - `navigation-compose` → CMP's multiplatform Navigation artifact
    (near-identical API, mostly import changes).
  - `androidx.browser` CCT calls → `expect fun openUrl(url: String)`.
  - Any `androidx.core.splashscreen` usage stays in `androidApp`'s
    `MainActivity` only; the web target renders straight into `SplashScreen`
    composable (loses the native pre-Compose flash, which doesn't exist on
    web anyway).
- Verify Material3 theming, `GenderTag`, gradients, and typography all
  render correctly on web through Compose Multiplatform Web resources
  (font loading is the usual snag — confirm the app's fonts, if custom, load
  via multiplatform resources, not Android `res/font`).

### Phase 4 — Feature parity + web-specific UX
- Wire the ported UI/data/domain stack together end to end in `webApp`.
- Add responsive layout tweaks where the current design assumed a phone
  viewport (name list, settings sheet, sync screen) — CMP Web runs at
  arbitrary window widths.
- Decide the web sync story: since the target PDF fetch is cross-origin
  (IRN's server), confirm CORS allows browser `fetch`/Ktor-JS requests; if
  the source blocks browser-origin requests, document a fallback (e.g. a
  tiny same-origin proxy function is out of scope for a static GitHub Pages
  site — see risk R3) rather than silently shipping a broken sync on web.

### Phase 5 — Test + CI parity
- `commonTest` carries over parser/mapper/usecase tests unchanged.
- Add `wasmJsTest` (Kotlin/Wasm test runner via headless Chrome, already
  supported by the Compose Multiplatform Gradle plugin) for
  platform-specific actuals (settings, PDF extraction JS interop).
- Extend `pr-checks.yml` to run `:composeApp:testDebugUnitTest` (Android)
  and `:composeApp:wasmJsBrowserTest` (web) on every PR.
- Extend `deploy-web.yml` to run on every push to the migration branch so
  the live Pages preview always reflects the latest state (safe: Pages
  hosting a WIP build doesn't touch `main` or the Play Store pipeline).

### Phase 6 — Stabilization + merge decision
- Manual pass on a real device/browser matrix (Android phone, Chrome,
  Firefox, Safari desktop).
- Only then: rebase onto `main`, open the "real" PR, let CI + review gate
  the actual merge. `release.yml` (Play Store) and `deploy-web.yml` (GitHub
  Pages) become the two release paths off the same `main`.

## 6. Risk register

- **R1 — No official Room support for wasmJs.** Mitigation: a small
  hand-rolled common `NameStore` interface (`getAll()`, `replaceAll()`,
  `observeCount()`) with a Room-backed Android `actual` and an
  IndexedDB/JSON-in-`localStorage` web `actual`. Since the whole dataset is
  only the names list (a few thousand short rows, replaced wholesale on
  each sync, never queried relationally), this is a reasonable scope
  reduction rather than a hack — revisit only if Room ships wasmJs support
  upstream.
- **R2 — No native PDF library for Kotlin/Wasm.** Mitigation: JS interop
  with `pdf.js` (`external` declarations + a `<script>` tag in
  `index.html`), converting its extracted text into the same line-based
  format `NameListTextParser` already expects, so the parser itself needs
  zero changes. Fallback if pdf.js interop proves too fragile: parse a
  pre-generated JSON snapshot of the names list shipped as a static web
  asset, refreshed by an Android-side (or CI) job — document as a
  degradation, not use silently.
- **R3 — CORS on the IRN PDF source.** The current Android app sidesteps
  browser CORS entirely (native OkHttp call). A browser `fetch`/Ktor-JS
  call to the same URL may be blocked if the server doesn't send
  `Access-Control-Allow-Origin`. Verify early (Phase 4) with a manual
  `fetch()` from a browser console against the real source URL; if blocked,
  the web build ships with the last synced snapshot and a clear "sync only
  works in the Android app" message, rather than a silently-broken button.
- **R4 — No iOS build/test environment in this session.** `iosMain`/`iosApp`
  are scaffolded per loopgain's layout for future parity but not compiled
  or tested here (no macOS/Xcode toolchain available in this remote
  environment). Tracked as explicitly out of scope for this migration pass.
- **R5 — GitHub Pages base path.** A project Pages site is served from
  `https://neteinstein.github.io/portuguese-allowed-names/`, not `/` — the
  wasmJs build's resource/asset URLs must be relative or the Compose
  Multiplatform Gradle plugin's base-href needs to be set accordingly, or
  fonts/wasm binaries 404 in production despite working under `./gradlew
  wasmJsBrowserRun`'s dev server (which serves from `/`).
- **R6 — Bundle size.** Kotlin/Wasm + Compose Multiplatform Web binaries are
  multi-MB. Acceptable for a hobby names-lookup tool, but `deploy-web.yml`
  should report the built artifact size on every deploy so growth is
  visible, not just merged into the repo silently over time.

## 7. GitHub Pages deployment

- New workflow `.github/workflows/deploy-web.yml`:
  - Triggers on push to the migration branch (while stabilizing) and later
    on push to `main` (once merged).
  - `./gradlew :webApp:wasmJsBrowserDistribution` produces
    `webApp/build/dist/wasmJs/productionExecutable`.
  - Deploys that directory via `actions/upload-pages-artifact` +
    `actions/deploy-pages` (the standard GitHub Pages Actions flow — no
    `gh-pages` branch or personal token needed, uses the repo's built-in
    `github-pages` OIDC environment).
  - Requires enabling "GitHub Actions" as the Pages source once in repo
    Settings → Pages — a one-time manual step for the repo owner, called
    out explicitly since Claude can't flip that setting via the API.
- Kept entirely separate from `release.yml` (Android/Play Store): different
  trigger, different job, no shared state, so a broken web build can never
  block an Android release or vice versa.

## 8. What this plan deliberately does not do

- Does not touch `release.yml`, Play Store credentials, or any signing
  config.
- Does not merge anything to `main` — everything lands on the migration
  branch until a manual stability sign-off.
- Does not attempt a working iOS build in this pass (R4) — structural
  parity with loopgain only.
- Does not change the app's product behavior/UX beyond what's forced by
  going multiplatform (e.g. CCT → `window.open` on web is a platform
  necessity, not a redesign).
