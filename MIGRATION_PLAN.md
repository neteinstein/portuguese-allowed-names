# KMP + Compose Multiplatform Migration Plan

Status: **in progress**. Phases 0–1 and the first step of Phase 2
(`feature:settings`) merged to `main` via PR #46. Remaining work continues
on `claude/kmp-cmp-migration-plan-m6dlst` (and follow-up branches based on
it) until the web + Android targets are verified stable side by side with
the app's current feature set.

**Phase 0 is implemented** (see §5): `core:model`, `core:domain`, and
`core:designsystem` exist with the domain layer and theme moved over
verbatim (same package names, so `:app`'s own source needed zero import
changes — only its `build.gradle.kts` gained three `project(...)`
dependencies); `composeApp`/`androidApp`/`webApp` exist with a placeholder
`App()` proving the Android + wasmJs pipeline end to end; `deploy-web.yml`
builds and publishes that placeholder to GitHub Pages. Two deliberate
scope trims versus the original Phase 0 description below:

- **`build-logic` convention plugins are deferred**, not added yet. With
  only 3 leaf modules so far, hand-writing each `build.gradle.kts` against
  the version catalog directly is less risk than also standing up an
  included build; §3.2's convention-plugin design is revisited once
  Phase 2's feature modules actually make the per-module boilerplate
  repeat enough to be worth abstracting.
- **`core:navigation` is deferred**, not added yet. The obvious first
  content for it — today's `Routes.kt` — currently depends on
  `SyncOrigin`, a Sync-feature type that hasn't moved yet; moving Routes
  alone would either strand that dependency or force a premature Sync
  move. It lands in Phase 3 alongside the real Navigation-Compose swap,
  once there's real content ready for it rather than an empty module.

**Known limitation of the sandbox this was implemented in**: outbound
access to `dl.google.com` (Google's Maven repository, which serves the
Android Gradle Plugin) is blocked by that environment's network policy —
confirmed this blocks `./gradlew help` on the *pre-existing, untouched*
`:app` module too, not just the new modules. Nothing Android-related
(which, transitively, is every module here except a pure-JVM slice) could
be locally configured or compiled while writing Phase 0; it was written
carefully against known-good Kotlin Multiplatform/Compose Multiplatform/
AGP conventions and validated via CI (GitHub Actions, which does have
Google Maven access) after pushing, not locally beforehand.

**First CI round on Phase 0 found two real issues**, both fixed in a
follow-up commit:
1. `settings.gradle.kts` had `repositoriesMode =
   RepositoriesMode.FAIL_ON_PROJECT_REPOS`, which rejects the Kotlin
   Multiplatform plugin's own Node.js-distribution repository that
   `wasmJs` targets need — incompatible with having a wasmJs target at
   all. First tried `RepositoriesMode.PREFER_SETTINGS`, which stops the
   hard failure but turned out to silently *drop* the plugin's repo
   instead of using it (so the `org.nodejs:node` lookup then 404'd
   against `google()`/`mavenCentral()`, which don't carry that artifact
   either). Landed on `RepositoriesMode.PREFER_PROJECT` instead — safe
   here since nothing else in this build declares its own per-module
   repositories, so it only ever affects that one plugin-added repo.
2. `composeMultiplatform = "1.12.0"` (latest stable at the time) pulls in
   an Android runtime artifact that requires `compileSdk 37` + `AGP
   9.1.0+` — newer than this repo's proven `agp = "8.13.2"` /
   `compileSdk = 36` (shared with the shipping `:app` module, which this
   migration deliberately doesn't touch). Pinned down to `1.8.2` instead
   of bumping AGP/compileSdk project-wide to chase the latest release.

**Second CI round found one more issue**, this time a real code fix rather
than a version/config tweak: Compose Multiplatform 1.8.2's bundled
Material3 predates the "fixed" color-role parameters (`primaryFixed`,
`secondaryFixed`, etc.) that `Theme.kt` was passing to
`lightColorScheme()`/`darkColorScheme()` — those parameters didn't exist
in that Material3 API surface yet, so `core:designsystem` failed to
compile. Rather than keep hunting for a Compose Multiplatform version that
has both the fixed-role API *and* stays under the compileSdk 36/AGP 8.13.2
ceiling (fragile either way), removed the fixed-role arguments from both
color schemes — Material3 supplies its own computed defaults for them.
The brand hex values stay defined in `Color.kt` for whenever this module
moves to a Compose Multiplatform version whose Material3 has that API.

**Third CI round**: `core:designsystem`/`composeApp` then compiled clean
for wasmJs (confirms the Theme.kt fix); `webApp/main.kt`'s
`ComposeViewport` call needed `@OptIn(ExperimentalComposeUiApi::class)` -
it's still an experimental API in Compose Multiplatform 1.8.2. One
unrelated infra flake also hit this round (Gradle wrapper zip download
reset by peer, before any build code ran) - re-ran that job once per the
usual flake handling, and it then got past that point cleanly, surfacing
the real `ComposeViewport` issue above.

Phase 0 finished green: Lint, Unit Tests, Build APK, Instrumented Tests,
and the wasmJs build all passed. Only `Deploy to GitHub Pages` stayed red,
for the documented reason (Settings → Pages → Source isn't set to "GitHub
Actions" yet - a one-time manual step, not a bug).

**Phase 1 is implemented**: `core:network`, `core:datastore`,
`core:database`, `core:parser`, and `core:data` now hold the whole data
layer, each an **Android-library-only** module for now (not KMP) — Ktor
and multiplatform-settings are genuinely multiplatform already, but Room
(risk R1) and pdfbox-android have no web story yet, so all five modules
stay plain `com.android.library` until their web actuals land in Phase 3,
rather than mixing KMP and non-KMP modules inconsistently across the data
layer. `:app`'s own source needed zero import changes here either (same
package-preservation approach as Phase 0) — only its `build.gradle.kts`
and two DI files (`AppModule.kt`, `DataStoreModule.kt`) changed:
- `NameListRemoteDataSource` (`core:network`): OkHttp → Ktor
  (`ktor-client-okhttp` engine), tested with `ktor-client-mock` instead of
  OkHttp's `MockWebServer`.
- `SettingsRepositoryImpl` (`core:datastore`): DataStore Preferences →
  `multiplatform-settings`'s `FlowSettings`, backed by
  `SharedPreferencesSettings` on Android; tested against `MapSettings`
  instead of an in-memory `DataStore` fake.
- `AppDatabase`/`NameDao`/`NameEntity`/`NameRepositoryImpl` (`core:database`):
  moved verbatim (Room's Android behavior is unchanged).
- `PdfTextExtractor`/`NameListTextParser`/`ParsedName` (`core:parser`):
  moved verbatim; `core:parser` exposes `pdfbox-android` as `api` since
  `PickANameApplication` still calls `PDFBoxResourceLoader.init(context)`
  directly at startup.
- `NameSyncRepositoryImpl` + the `ParsedName → NameEntity` mapper
  (`core:data`, the one module that legitimately depends on network +
  parser + database together): moved with one real behavior-preserving
  change — its network-failure `catch` no longer names `java.io.IOException`
  specifically (not available outside JVM/Android targets, and this data
  layer will need to run on wasmJs in Phase 3), it catches `Exception`
  broadly instead after the `IllegalArgumentException` check, so the same
  NETWORK/INVALID_SOURCE classification survives unchanged.
- **Mapper split**: the original single `NameMappers.kt` bridged both
  `NameEntity` (now in `core:database`) and `ParsedName` (now in
  `core:parser`) — keeping it as one file would have made `core:database`
  and `core:data` depend on each other in a cycle. Split into
  `NameEntityMappers.kt` (`core:database`: `toEntityCode`/`toDomainGender`/
  `toDomain`/`toFilterInitial`) and a slim `NameMappers.kt` (`core:data`:
  just `ParsedName.toEntity()`, which calls into `core:database`'s mapper
  functions since both keep the same `org.neteinstein.pickaname.data.mapper`
  package name).
- The leftover `domain`/`usecase` test files in `app/src/test` (testing
  code that already moved to `core:model`/`core:domain` back in Phase 0)
  are **not** relocated in this pass — deliberately deferred to keep this
  push's diff focused on the data layer; they still pass today since
  package names didn't change.

Like Phase 0, none of this could be verified locally (same `dl.google.com`
sandbox limitation) — written carefully, pushed, and validated via CI.

**First CI round on Phase 1** hit the same class of issue as Phase 0's
Compose Multiplatform surprise: `ktor-client-okhttp:3.6.0` transitively
pulls in `com.squareup.okhttp3:okhttp-android:5.5.0`, which itself
requires `compileSdk 37+` — one compileSdk generation ahead of this repo's
`compileSdk 36`. Rather than fight OkHttp version pinning (excluding the
transitive dep and forcing an older one is fragile and easy to get subtly
wrong), switched `core:network`'s Android engine from `ktor-client-okhttp`
to **`ktor-client-cio`** — a pure-Kotlin/coroutines Ktor engine with no
OkHttp dependency at all, so no AAR-metadata constraint to trip over.
Equally fine for this app's plain GET-and-download-bytes use case.

**Second CI round**: past the OkHttp issue (Ktor/CIO compiled clean), the
next failure was `core:datastore`'s test source: `MapSettings` isn't in
the main `multiplatform-settings` artifact - it lives in a dedicated
`multiplatform-settings-test` artifact, which wasn't yet a dependency.
Added it as `testImplementation`.

**Third CI round**: `:app:compileDebugKotlin` itself failed with a string
of "unresolved reference"/"cannot access class" errors for `Room` and
`FlowSettings`. Cause: `:app`'s own `di/DatabaseModule.kt` and
`di/DataStoreModule.kt` construct the Room database and the
`FlowSettings`/`SharedPreferencesSettings` instances directly (that
construction logic was deliberately kept in `:app`'s DI, not pushed into
the core modules - see §5 Phase 1), so `:app` needs Room and
multiplatform-settings on its own compile classpath, not just visible
*inside* `core:database`/`core:datastore`. Both modules had declared them
as `implementation` (module-private), so nothing flowed through the
`implementation(project(":core:database"))`/`":core:datastore"` edges.
Changed both to `api` in the two core modules - the correct fix, since
these are exactly the "leaky by design" dependencies a consuming DI module
needs, not implementation details to hide.

Phase 1 finished fully green (Lint, Unit Tests, Build APK, Instrumented
Tests, wasmJs build all passed; only the documented "Pages not enabled"
failure remained).

**Phase 2 (feature module extraction) is started**: `feature:settings` is
the first of the four (settings → sync → namelist → splash, per §5's
order - simplest and most self-contained first). Extracting it surfaced a
real problem the data-layer extractions never hit: `SettingsScreen.kt`
imports `org.neteinstein.pickaname.R` for its strings, and Android
resources don't flow "backward" from `:app` into a library module `:app`
depends on - `feature:settings` has no dependency edge to `:app`, so it
can't see `:app`'s resources at all.

Fix, done once so every later feature extraction is as mechanical as
Phase 1's data-layer moves: **all of `app/src/main/res/values{,-pt}/
strings.xml` moved into `core:designsystem/src/androidMain/res/`** (a
shared module every feature already depends on for theming), rather than
trying to carefully split strings per-feature - several strings
(`cd_back`, the gender labels, etc.) are already shared across screens
that haven't moved yet, so a per-feature split would've been fragile for
little benefit in an app this size. Every file that imported
`org.neteinstein.pickaname.R` (5 files - not just Settings, since Splash/
Sync/NameList/GenderTag all still live in `:app` but need the same
strings) now imports `org.neteinstein.pickaname.core.designsystem.R`
instead; only the import line changed, no logic.

Also added **`core:testing`**, a small shared module holding
`MainDispatcherRule` (previously duplicated implicitly by living in
`:app`'s own test source set) - every feature's ViewModel tests need it,
so it moved to a shared module the same way `core:designsystem` now hosts
shared strings, instead of copy-pasting it into each feature module.

`feature:settings` itself is, like the Phase 1 data modules, plain
`com.android.library` (not KMP) for now - it uses `LocalContext`,
`stringResource(R.string...)` via the classic Android resource system,
and an Android-only "open the OS per-app language settings" intent, none
of which have a web equivalent yet. Making feature modules truly
multiplatform (Compose Multiplatform resources instead of `R.string`,
`expect`/`actual` for the platform-specific bits) is Phase 3 work, same as
the data layer's web actuals.

**First CI round on Phase 2** caught one thing the string-centralization
sweep missed: `SplashScreen.kt` (which stayed in `:app`, not moved) also
uses `R.drawable.ic_launcher_foreground` - a real app-identity asset that
correctly stays in `:app`, not `core:designsystem` (it's the launcher
icon, not shared UI). Blanket-swapping its `R` import broke that one
reference. Fixed by importing both: `org.neteinstein.pickaname.R as AppR`
alongside the unaliased `core:designsystem` one, using `AppR.drawable...`
for the launcher asset and the plain `R.string...` calls for everything
else.

**Second CI round on Phase 2** caught a variant of the same class of bug:
`app/src/androidTest/.../SplashSmokeTest.kt` is in the bare
`org.neteinstein.pickaname` package (same as `:app`'s own namespace), so it
was resolving `R.string.app_name` via Kotlin's implicit same-package `R`
class - no explicit `import` line existed for it at all, which is why the
earlier grep for explicit `import ... R` statements never caught this
file. Fixed by adding an explicit
`import org.neteinstein.pickaname.core.designsystem.R`. Followed up with a
broader sweep (`\bR\.(string|plurals|drawable|mipmap|style|xml|color|array)\.`
across all of `app/src`) to check for any other implicit references this
pattern could hide; no further instances were found.

**`feature:sync` extraction (second of the four Phase 2 modules)** followed
the exact mechanical pattern `feature:settings` established, with no new
class of bug this time: `SyncScreen.kt`, `SyncViewModel.kt` (which also
declares `SyncOrigin`, the enum `Routes.kt` and `PickANameNavHost.kt` key
off), and `SyncViewModelTest.kt` moved via `git mv` into
`feature/sync/src/{main,test}/...`, package names unchanged
(`org.neteinstein.pickaname.presentation.sync`). `SyncScreen.kt` already
imported `core.designsystem.R` (fixed in the first Phase 2 round's sweep),
and `SyncViewModelTest.kt` already imported `core:testing`'s
`MainDispatcherRule` from its preserved `org.neteinstein.pickaname.util`
package, so neither file needed any content change beyond the move itself.
`feature/sync/build.gradle.kts` mirrors `feature/settings/build.gradle.kts`
exactly (same dependency shape: `core:model`/`core:domain`/`core:designsystem`,
Compose + Material icons extended, Koin, `core:testing` for tests).
`Routes.kt`/`PickANameNavHost.kt`/`ViewModelModule.kt` (all staying in
`:app`) needed zero import changes — they already referenced
`org.neteinstein.pickaname.presentation.sync.*`, which now simply resolves
through the new `:feature:sync` module dependency instead of `:app`'s own
source set, exactly like the `feature:settings` extraction.

**Also fixed while continuing this branch**: PR #46 (Phases 0–2 through
`feature:settings`) was merged to `main` directly by the repo owner outside
this session, and two follow-up PRs landed on `main` after that (an
app-name rename touching `strings.xml`, then a README/screenshot update
touching `NameListScreen.kt`). Per the "merged PR → restart the branch"
rule, `claude/kmp-cmp-migration-plan-m6dlst` was fast-forwarded onto the
latest `main` before starting `feature:sync`, rather than continuing to
build on top of now-merged history.

**`feature:namelist` extraction (third of the four Phase 2 modules)**
surfaced one genuinely new problem the first two extractions never hit:
`NameListScreen.kt` uses `GenderTag`, a small shared composable that lived
in `:app`'s `presentation/common/` package - not feature-specific UI, but
exactly the kind of "shared across screens" building block §3's target
layout already calls out for `core:designsystem` (`theme, colors,
typography, shapes, gradients, GenderTag, shared composables`). Moved it
there via `git mv`, package unchanged
(`org.neteinstein.pickaname.presentation.common`), landing in
`core/designsystem/src/androidMain/kotlin/...` rather than `commonMain`:
`GenderTag` calls the classic `androidx.compose.ui.res.stringResource(
@StringRes Int)` overload against this module's own `androidMain`
`strings.xml`, and pulls in `androidx.compose.material:material-icons-
extended` (Female/Male icons) - neither is part of Compose Multiplatform's
common API surface yet, so `core:designsystem` picked up its first real
`androidMain`-only Kotlin source (previously it only had `commonMain`
Kotlin + `androidMain` resources). Also added `core:model` as a
`commonMain` dependency of `core:designsystem` (for `GenderTag`'s `Gender`
parameter) - safe since `core:model` is the pure-Kotlin leaf module with no
dependencies of its own, so this doesn't create any cross-module cycle
risk.

`NameListScreen.kt`/`NameListViewModel.kt`/`NameListViewModelTest.kt`
themselves moved exactly like the prior two extractions - package
preserved, `PickANameNavHost.kt`/`ViewModelModule.kt` (staying in `:app`)
needed no import changes. `feature/namelist/build.gradle.kts` needed two
dependencies the settings/sync templates didn't: `androidx.browser`
(Custom Tabs fallback for the "open name meaning" link) and
`androidx.activity.compose` (`BackHandler` for the meaning-search bottom
sheet's swipe-to-dismiss). Removed `androidx.browser` from `:app`'s own
`build.gradle.kts` since `NameListScreen` was its only consumer there and
it's now declared directly by `feature:namelist`.

**`deploy-web.yml` retargeted to `main` only.** It previously ran on every
push to `claude/kmp-cmp-migration-plan-m6dlst` so the wasmJs build got
CI'd on every Phase 2 push; changed to deploy only on push to `main` (per
explicit instruction), since the live Pages site should always reflect the
last stable, merged state, not in-progress migration-branch work.
`workflow_dispatch` stays available to build/deploy on demand. One
trade-off worth noting: pushes to the migration branch no longer get a
wasmJs build check in CI as a side effect of this trigger (`pr-checks.yml`
doesn't build wasmJs itself) - acceptable since Phase 2's four extractions
are Android-only anyway (§5); revisit once Phase 5 adds a real
`wasmJsBrowserTest`/build check to `pr-checks.yml` for every PR.

**`feature:splash` extraction (fourth and last of Phase 2's four feature
modules)** hit the same class of problem `feature:namelist` did with
`GenderTag`, but in a shape that couldn't be solved by moving a file:
`SplashScreen.kt` renders `AppR.drawable.ic_launcher_foreground` - the
app's launcher icon, deliberately kept in `:app`'s own resources rather
than `core:designsystem` back in the very first Phase 2 CI round (it's
app-identity, not shared UI). A `feature:*` module structurally cannot
depend on `:app` (§3.1: dependencies only point inward), so `feature:splash`
can't reach that drawable by import, and duplicating the launcher icon
into `core:designsystem` would contradict that earlier, correct call.

Fixed by inverting the dependency instead of relocating the asset:
`SplashScreen`/`SplashContent` now take a `@DrawableRes logoRes: Int`
parameter rather than reaching for a hardcoded resource themselves, and
`PickANameNavHost.kt` (staying in `:app`, the one place that already knows
about all of `:app`'s own resources) passes
`R.drawable.ic_launcher_foreground` in at the call site. This is a real
(small) design improvement, not just a migration workaround: the splash
composable no longer needs to know *which* app it's branding, which is
exactly the kind of decoupling feature-module boundaries are supposed to
force.

`SplashViewModel.kt`/`SplashViewModelTest.kt` moved with zero changes
(same pattern as every prior extraction). Also removed
`androidx.material.icons.extended` from `:app`'s own `build.gradle.kts`:
with `NameListScreen`/`GenderTag`/`SyncScreen` all moved out over the last
three rounds, nothing left in `:app`'s own source references any Material
icon at all.

**Phase 2 is now content-complete**: all four features (`settings`,
`sync`, `namelist`, `splash`) live in their own modules. What's left of
`:app` is `MainActivity`, the manifest, launcher icons/proguard rules, DI
wiring (`di/*Module.kt`), and `PickANameNavHost`/`Routes` (deferred to
Phase 3 alongside the real Navigation-Compose swap, per §5's original
plan). The one thing Phase 2's roadmap description also called for -
"moving `PickANameNavHost` assembly into `composeApp`" - is deliberately
**not** done in this round: `composeApp`/`androidApp` are still Phase 0
placeholders (`androidApp`'s `MainActivity` even lives in an isolated
`.../next/` package), and retiring `:app` in favor of them is coupled to
`release.yml` (hardcoded `app/` paths for the keystore, the versionName
bump regex, and build output paths). That cutover deserves its own
careful, explicitly-reviewed step rather than riding along with a feature
extraction.

**Phase 3 (web actuals) is started**, first step: **`core:datastore` is
now a real KMP module** (`androidTarget` + `wasmJs`), not
`com.android.library`. This one needed almost no code change:
`SettingsRepositoryImpl` was already written entirely against
`multiplatform-settings`'s `FlowSettings` interface with no Android-
specific code at all (the one genuinely platform-specific piece - actually
constructing a `SharedPreferencesSettings` on Android - already lived in
`:app`'s own `DataStoreModule.kt`, not in this module), so converting it
was purely mechanical: move `SettingsRepositoryImpl.kt` into
`commonMain`, and `SettingsRepositoryImplTest.kt` into `androidUnitTest`
(kept on JUnit4/mockk/truth/turbine for now rather than rewritten against
`kotlin.test` - a full `commonTest` port for every module is Phase 5
work, per §5). No `wasmJsMain` actuals were needed in this module at
all - `multiplatform-settings` ships its own ready-made browser-
`localStorage`-backed `Settings` for wasmJs already; a future web
`composeApp` DI wiring step is what will actually construct and inject
one, the same way `:app`'s `DataStoreModule.kt` does for Android today.

**Also checked, and blocked**: tried to verify risk R3 (does the IRN PDF
source send CORS headers a browser `fetch`/Ktor-JS call could use) with a
plain `curl -H "Origin: ..."` against the real source URL from this
sandbox. Got back a same-shape 403 as every other blocked external host
in this environment (confirmed via the proxy's own status endpoint: a
`connect_rejected` policy denial, not a real response from
`irn.justica.gov.pt`) - this sandbox's network policy blocks that host
outright, the same class of limitation as `dl.google.com` blocking local
Gradle builds all along. R3 stays genuinely unverified; the cheapest real
check is still a single `curl -sI -H "Origin: https://<pages-domain>"
<source-url>` run from an unrestricted network (or a browser console)
looking for `Access-Control-Allow-Origin` in the response.

**`core:network` is now a real KMP module** (`androidTarget` + `wasmJs`),
same story as `core:datastore`: `NameListRemoteDataSource` was already
written entirely against Ktor's platform-agnostic `HttpClient` type with
zero engine-specific code, taking a pre-built `HttpClient` via constructor
injection - the actual engine choice (`CIO` on Android today) already
lived in `:app`'s own `AppModule.kt`. Pure source-set move:
`NameListRemoteDataSource.kt` into `commonMain`,
`NameListRemoteDataSourceTest.kt` into `androidUnitTest`.

One real fix alongside the move: `core:network`'s `build.gradle.kts` had
carried an `implementation(libs.ktor.client.cio)` dependency that nothing
in the module's own source actually used (confirmed by grep - only
`ktor-client-core` types are imported). Left in place, this would have
broken the new `wasmJs` target outright, since `ktor-client-cio` doesn't
publish a `wasmJs` artifact at all (CIO is JVM/Native-only; the wasmJs
engine is `ktor-client-js`). Dropped it. No engine dependency of any kind
was added back, for either target: this module doesn't need one, only
whoever *constructs* the `HttpClient` does - `:app`'s `AppModule.kt` for
Android today, and a future `composeApp` web DI module for `wasmJs`
(needing `ktor-client-js`, not yet added to the version catalog since
nothing consumes it yet).

**`core:parser` is now a real KMP module too, but honestly incomplete** -
this is the first Phase 3 module that couldn't be a pure source-set move.
`NameListTextParser.kt`/`ParsedName.kt` are pure Kotlin (no Android
imports) and moved into `commonMain` exactly like the previous two
modules' code did. `PdfTextExtractor` is different: it directly calls
`pdfbox-android` APIs, which is a real platform-specific implementation,
not just Android-hosted common code - so it's now an
`expect class PdfTextExtractor() { suspend fun extractText(...): String }`
in `commonMain`, with:
- an `androidMain actual` that's the exact same pdfbox-android code as
  before, just relocated and marked `actual`;
- a `wasmJsMain actual` that **throws `NotImplementedError`** rather than
  attempting real `pdf.js` JS interop (risk R2) in this pass.

Reasoning for stopping at a stub rather than writing the interop:
`pdf.js` integration needs `external` declarations against its
Promise-based API, a `<script>` tag in `webApp/index.html`, and bridging
JS Promises into a Kotlin suspend function - all real code this sandbox
has no way to verify. There's no browser here to run
`wasmJsBrowserTest` or otherwise exercise it, so writing that interop now
would be unverified guesswork masquerading as a real implementation,
exactly the kind of "claim success without being able to test it" this
project's own working norms rule out. The stub keeps the module
compiling for `wasmJs` (CI *can* verify that much) while making the real
gap explicit rather than silently faking web PDF parsing. Implementing
`pdf.js` interop for real, and verifying it against the actual names-list
PDF in a real browser, stays open Phase 3 work.

**Local builds now work — the sandbox limitation above is gone.** Every
phase up to here was written blind and validated only via CI (no
`dl.google.com` access, no Android SDK, no browser). This session runs on a
machine with the Android SDK and Chrome available, so the whole graph —
`assembleDebug`, `testDebugUnitTest`, every module's `compileKotlinWasmJs`,
and even `wasmJsBrowserTest` against real headless Chrome — builds and runs
locally before anything is pushed. Notes from here on report *locally
verified* results, not "written carefully, will find out in CI".

**First thing that found: `core:domain`'s wasmJs target never actually
compiled.** Nothing in CI ever built it — `deploy-web.yml` builds
`:webApp:wasmJsBrowserDistribution`, and that only reaches
`composeApp → core:designsystem → core:model`, so `core:domain`'s `wasmJs`
compilation had been dead code since Phase 0 declared the target. Running
`:core:domain:compileKotlinWasmJs` locally failed immediately on two
JVM-only APIs sitting in `commonMain`:
- `UpdateSourceUrlUseCase` validated URLs with `java.net.URI`. Replaced
  with an `internal fun isValidHttpUrl(String)` in the same file — a
  hand-rolled scheme/host check rather than a new dependency, since the
  domain layer never needs the *parsed* URL, only a yes/no answer. It
  preserves every accept/reject case the existing JVM test asserts
  (whitespace rejected like `URI` does, `https://` with no host rejected,
  non-http(s) schemes rejected), and drops userinfo/port before checking
  the host.
- `SyncNamesUseCase`/`RefreshNamesIfDueUseCase` both defaulted their
  injectable clock to `System::currentTimeMillis`. Both now default to
  `systemCurrentTimeMillis()` (`domain/time/CurrentTime.kt`), a one-line
  wrapper over `kotlin.time.Clock.System.now().toEpochMilliseconds()` from
  the Kotlin stdlib — no `kotlinx-datetime` dependency needed. Tests keep
  passing their own `() -> Long`, unchanged.

Also added `core:domain`'s first `commonTest`: `IsValidHttpUrlTest`, written
against `kotlin.test` rather than JUnit/Truth/MockK so it runs on *both*
targets — it passes under `:core:domain:wasmJsTest` (real ChromeHeadless)
and rides along in `testDebugUnitTest` on Android. The older MockK-based
use-case tests still live in `app/src/test`; relocating and porting those to
`commonTest` stays Phase 5 work, unchanged.

`kotlin-js-store/` (the Kotlin Gradle plugin's generated yarn lock, which
appears the first time a wasmJs test task runs locally) is gitignored rather
than committed, with the reasoning written next to the entry: Kotlin
recommends committing it, but the only wasmJs CI job today runs *after*
merge to `main`, so a lock mismatch would surface too late to catch in
review. Revisit when Phase 5 puts a wasmJs job in `pr-checks.yml`.

**Risk R1 (Room on web) is resolved: Room stays Android-only, the web gets
its own store.** Checked Google's Maven index rather than guessing:
`room-runtime` publishes no `wasm-js` artifact at all (only
`room-common-wasm-js`, i.e. the annotations), and neither does
`androidx.sqlite`'s bundled driver - so there is no "Room on wasmJs" option
to pick. `core:database` is now KMP with the store expressed as a plain
Kotlin interface both platforms implement:
- `commonMain`: `NameLocalDataSource` (the DAO contract, documented so the
  two implementations can be checked against each other) + `NameRecord` (a
  platform-neutral row), with `NameRepositoryImpl` and the mappers now
  target-agnostic.
- `androidMain`: `AppDatabase`/`NameDao`/`NameEntity` exactly as before
  (KSP wired through `kspAndroid` only), behind a thin
  `RoomNameLocalDataSource` adapter. Android behavior is unchanged.
- `wasmJsMain`: `LocalStorageNameLocalDataSource` - the list in memory,
  mirrored into `localStorage`, with filtering/ordering/dedup mirroring the
  DAO's SQL. **Deviation from the plan's original R1 sketch** (IndexedDB):
  the list is ~7,500 short strings, comfortably inside `localStorage`'s
  ~5 MB budget, and its synchronous API needs no async plumbing, no schema
  and no extra dependency. The write path fails soft if the quota is ever
  exceeded, which is the signal to revisit IndexedDB.
- `java.text.Normalizer` (in `toFilterInitial`) became an expect/actual
  `stripDiacritics()`: `Normalizer` on Android, the browser's own
  `String.normalize('NFD')` on web. Both real Unicode data - no hand-rolled
  accent table that would quietly miss a letter.

`core:data` went KMP in the same pass (`NameSyncRepositoryImpl` against the
new interface, `ParsedName.toEntity()` → `toRecord()`), and `:app`'s
`DatabaseModule` binds `NameLocalDataSource` to the Room implementation.
Nine new browser tests (`:core:database:wasmJsTest`, real ChromeHeadless)
cover the web store and the diacritic actual.

**Risk R2 (PDF parsing on web) is resolved, and verified against the real
document.** `PdfTextExtractor`'s wasmJs actual is no longer a stub: it wraps
`pdf.js` (`pdfjs-dist`, an npm dependency of `core:parser`'s wasmJs target,
bundled by webpack). Three things this turned up that only a real browser
could have told us:
1. **Worker loading.** The documented ways to point
   `GlobalWorkerOptions.workerSrc` at pdf.js's worker bundle
   (`import.meta.url`, `new Worker(new URL(...))`) don't exist in Kotlin/
   Wasm's webpack output, which is a classic script. Assigning the imported
   worker module to `globalThis.pdfjsWorker` puts pdf.js on its own
   fake-worker path, which works (parsing runs on the main thread).
2. **pdf.js emits positioned fragments, not lines** - plus synthetic
   whitespace-only items. Those are dropped and every separator is derived
   from x positions instead, since a synthetic space carries a whole
   column's width.
3. **The column separator cannot be recovered from gap size.** Measured on
   the real document: the gender→name gap inside a cell is ~73-80pt and the
   name→next-column gap is ~89pt, and the latter shrinks below the former
   as names get longer - no threshold separates them. Rather than guess,
   `NameListTextParser` now splits cells **on gender keywords as well as on
   double spaces**; pdfbox's double-space output parses exactly as before
   (its tests are untouched and still pass), and pdf.js can join a row with
   single spaces and stay correct. Row grouping also had to grow from
   "round y into buckets" to "grow a row from its topmost baseline within
   ~0.7x the text height", which is what folds the document's one wrapped
   hyphenated name (`Darius-` / `Alexandru`) back into its own row the way
   pdfbox does.

Verified end to end, in ChromeHeadless, against the real 2.9 MB source PDF:
the pdf.js path produces **7,481 names, an identical (name, gender) set to
Apache PDFBox 2.0.27's `-sort` output** run through the same parser. (That
comparison used the live document and a locally served copy, so it isn't a
committed test; what is committed is `PdfTextExtractorTest`, which runs the
extractor in a real browser against a small hand-built two-column PDF, plus
`NameListTextParserCellSplitTest` in `commonTest`, which pins both
platforms' whitespace shapes to the same parsed names.)

Note for R6 (binary size): the current `:webApp` distribution is ~11 MB and
does **not** yet include pdf.js, since `composeApp` is still a Phase 0
placeholder that never reaches `core:parser`. Wiring the real app up will
add roughly 1.5 MB of pdf.js (448 KB main + 1.0 MB worker, uncompressed).

**Risk R3 (web sync/CORS) is answered, and the answer is no.** Now that
this environment can reach the source host, a plain request settles what
earlier phases could only flag: the IRN PDF comes back `200 OK`
(`application/pdf`, 2,905,263 bytes) **with no `Access-Control-Allow-Origin`
header at all**, and an `OPTIONS` preflight to the same URL returns
`502 Bad Gateway`. A browser `fetch`/Ktor-JS request from the GitHub Pages
origin will therefore be blocked - the web build cannot download the names
list directly, no matter which HTTP client it uses. (Second, smaller
finding: the host also 502s any request without a `User-Agent` header, so
whatever eventually fetches it must send one.)

That makes Phase 4's fallback the real plan rather than a contingency, and
it's worth deciding between two shapes before building either:
- **Ship a snapshot.** A scheduled/CI job fetches and parses the PDF and
  commits a small names file next to the Pages site; the web build loads
  that same-origin file, so "sync" on web means "pull the latest snapshot".
  Needs new CI infrastructure (and a JVM-side parse), but gives the web
  build a working, always-current list and sidesteps CORS entirely.
- **Disable sync on web.** Ship the page with whatever snapshot is baked in
  and tell the user plainly that refreshing the list is Android-only, per
  the original Phase 4 text.
The pdf.js work above is not wasted either way: it's what makes a
user-supplied, CORS-enabled URL (and a future "open a local PDF" file
picker) work on web.

**The UI layer's strings are now Compose Multiplatform resources**, which is
the step that unblocks moving the feature modules themselves off Android.
`core:designsystem`'s `androidMain/res/values{,-pt}/strings.xml` moved to
`commonMain/composeResources/values{,-pt}/strings.xml`, the generated `Res`
class is made public (`compose.resources { publicResClass = true }`) so
every feature module can reach it, and all ~73 `stringResource(R.string.x)`
call sites across the four feature screens became
`stringResource(Res.string.x)`. `GenderTag` moved from `androidMain` to
`commonMain` at the same time - the two things that kept it Android-only
(the `@StringRes` overload and material-icons-extended) are both solved
here, the icons by switching to `org.jetbrains.compose.material:material-
icons-extended`, which is multiplatform and, on Android, resolves to the
androidx artifact anyway. It's pinned at 1.7.3 because JetBrains stopped
publishing that artifact after 1.7.3 while Compose Multiplatform itself
moved on; the icons are plain `ImageVector`s, so the version skew is inert.

Feature modules are still `com.android.library` after this step - they get
the resources runtime transitively from `core:designsystem` (`api(compose.
components.resources)`), so nothing forced them to become KMP yet. Turning
each one into a real KMP module is the next step, and it is now a
module-shaped change rather than a resource-system change.

Three things this surfaced that only running the app could have shown, all
fixed:
- **Android's backslash escaping is not Compose Multiplatform's.** `\'` and
  `\"` came through literally - the title bar read `Portugal\'s Approved
  Names`. Compose resources take the XML text as-is, so every escape was
  removed.
- **Only positional format args are substituted.** The plurals entry's bare
  `%d` rendered as the literal text "%d names"; `%1$d` works. (The
  `%1$s`-style args elsewhere were already fine.)
- **`Context.getString` has no non-composable Compose-resources
  equivalent.** `NameListScreen.buildMeaningSearchUrl` read the search-query
  string off a `Context` inside a click handler; it now takes the resolved
  text, read with `stringResource` in composable scope by each of its three
  call sites.

`app_name` is the one string deliberately duplicated: `AndroidManifest.xml`'s
`android:label` can only read a classic Android resource, so a two-entry
`app/src/main/res/values{,-pt}/strings.xml` now holds the launcher label
while the in-UI app name comes from the shared Compose resources.

Verified on a running emulator (API 36), not just by building: the list
screen renders 7,481 names with correct `GenderTag` badges and a correctly
formatted "7481 names" count, the apostrophes render properly, and
switching the app locale to `pt-PT` (`cmd locale set-app-locales`) shows the
whole UI - list and settings screens - in Portuguese from
`composeResources/values-pt`, plural included.

**All four feature modules are now real KMP modules** (`androidTarget` +
`wasmJs`), built on Compose Multiplatform rather than the androidx Compose
BOM. The pieces that made this possible without rewriting the screens:
- **JetBrains' multiplatform AndroidX builds** for lifecycle
  (`org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` /
  `-runtime-compose`, 2.9.6) keep the same package names and APIs, so
  `androidx.lifecycle.ViewModel`, `viewModelScope` and
  `collectAsStateWithLifecycle` needed no import changes at all.
- **Koin's multiplatform Compose artifacts**: `org.koin.androidx.compose.
  koinViewModel` → `org.koin.compose.viewmodel.koinViewModel`. `:app`'s
  `viewModelModule` already used `org.koin.core.module.dsl.viewModelOf`,
  which is the multiplatform DSL, so the DI side is unchanged.
- Note for anyone copying these build files: in Kotlin 2.3 a KMP source-set
  block can't call `platform(...)` directly (KT-58759), it has to be
  `project.dependencies.platform(libs.koin.bom)`.

The genuinely platform-specific bits became `expect`/`actual` pairs, each
with a real web answer rather than a stub:
- `feature:settings` - `rememberAppLanguageSettingsLauncher()`: Android
  hands off to the OS per-app language screen; web returns `null` and the
  Settings screen **omits the language card entirely**, since a browser
  page follows the browser's own language and a button there could do
  nothing.
- `feature:namelist` - `rememberExternalUrlOpener()` (Custom Tab →
  `window.open(url, "_blank", "noopener")`), `isInAppBrowserSupported()`
  and `InAppBrowser()`. The WebView-backed name-meaning sheet moved
  wholesale into `androidMain`; on web `isInAppBrowserSupported()` is
  **false on purpose** - search engines refuse to be framed
  (`X-Frame-Options`/`frame-ancestors`), so the web build opens the search
  in a new tab instead of an in-page panel that could only ever be blank.
- `feature:namelist` - `PlatformBackHandler()`: `androidx.activity.compose.
  BackHandler` on Android, a no-op on web (Compose Multiplatform 1.8.2 has
  no common `BackHandler`, and hooking the browser's history button belongs
  with the navigation step, not here).
- `feature:splash` - no expect/actual needed: `SplashScreen` now takes a
  `Painter` instead of an `@DrawableRes Int`, so it stays off any one
  platform's resource system while keeping the "the splash doesn't know
  which app it's branding" decision from Phase 2. `:app` passes
  `painterResource(R.drawable.ic_launcher_foreground)`.

Two API changes were forced by Compose Multiplatform 1.8.2's slightly older
Material3: `ExposedDropdownMenuAnchorType` doesn't exist there yet
(`MenuAnchorType` does), and `android.net.Uri.Builder` had to go - the
meaning-search URL is now built with a small `encodeUrlQueryValue()` in
common code.

Verified on the emulator again after the conversion, since this touched the
name-meaning path directly: tapping a name still opens the in-app WebView
sheet, with the hand-rolled percent-encoding producing the same search URL
(apostrophe included) the `Uri.Builder` did.

**`composeApp` is now the real app shell, and the web build runs the whole
app.** This is the step where the migration stops being structural and
starts being visible:
- **`core:navigation`** finally exists, holding `Routes` and
  `NavTransitions`. The dependency that blocked it in Phase 0 - `Routes`
  needing `SyncOrigin` from `feature:sync` - is gone because `SyncOrigin`
  moved *here*: it only ever picked a distinct route, and the sync screen
  never reads it, so navigation is where it belongs. Navigation itself is
  now JetBrains' multiplatform `navigation-compose`.
- **`PickANameNavHost` and the whole Koin graph moved into `composeApp`**,
  which is the one module allowed to see every feature. `appModules()`
  assembles repositories + use cases + view models (all shared) plus an
  `expect fun platformModule()`: Room/SharedPreferences/CIO on Android,
  `localStorage`/`StorageSettings`/Ktor-JS in the browser. Both shells -
  `:app`'s `PickANameApplication` and `webApp`'s `main()` - register exactly
  the same list.
- **`:app` is now a thin Android shell**: `MainActivity`, the manifest,
  launcher resources, Koin startup. Its leftover domain/use-case tests
  (deferred since Phase 1) moved with their code into `core:domain`'s
  `androidUnitTest`, and `TraditionalNameRulesTest` was ported to
  `kotlin.test` in `core:model`'s `commonTest`, where it now runs on both
  targets. `androidApp` became a real second shell (its own Application and
  Koin startup) so the side-by-side comparison Phase 7 needs actually works.

Three web-only problems surfaced, all of which only a running browser could
have shown:
1. **Compose version skew → `IrLinkageError` at runtime.** JetBrains'
   lifecycle 2.9.6 and navigation 2.9.2 pull Compose 1.10.x transitively,
   while the plugin still pinned **1.8.2** - the app compiled fine and then
   died on `ComposeViewport` not existing with that signature. Fixed by
   moving Compose Multiplatform to **1.10.2**, which is still fine under
   this repo's AGP 8.13.2 / compileSdk 36 (Android build, unit tests and
   lint all re-verified). Worth remembering for later bumps: the *runtime*
   is what the transitive AndroidX-multiplatform artifacts decide, so the
   plugin version has to keep up with them.
2. **webpack failed the build over Skiko's dynamic exports.** As soon as a
   real ESM npm package (pdf.js) is in the bundle, webpack's
   `exportsPresence` check turns `export 'skikoApi' was not found in
   './skiko.mjs'` from a warning into an error.
   `webApp/webpack.config.d/skiko-exports.js` downgrades that one check.
3. **A failed browser `fetch` is not an `Exception`.** `NameSyncRepositoryImpl`
   caught `Exception`, but Kotlin/Wasm surfaces a rejected fetch as
   `JsException`, which extends `Throwable` directly - so a CORS failure
   escaped the coroutine and the Sync screen span forever. Both catch
   boundaries now catch `Throwable`, and the web build shows the real
   "Couldn't reach the names source" error state instead.
   (`index.html` also needed `html, body { height: 100% }`, or Compose sizes
   its canvas to a thin strip.)

Verified in a real browser (headless Chrome with software WebGL, serving the
production `wasmJsBrowserDistribution`): splash → sync, Material 3 theming,
typography, icons, Compose-resource strings and the error state all render,
Koin resolves the browser-backed stores, and the sync attempt fails exactly
the way R3 says it must. The distribution is ~16 MB uncompressed now that
pdf.js is in it (R6).

### R3 decision (made by the repo owner): ship a CI-generated snapshot

Of the two options above, the chosen direction is **snapshot, not
degrade**: a CI job fetches and parses the source PDF and publishes a small
names file alongside the Pages site, the web build loads that same-origin
file, and **the web Settings screen drops the sync/source controls
entirely** (they'd be meaningless there). That is the next PR's work, not
this one. What it needs:
- a scheduled + `workflow_dispatch` GitHub Actions job that downloads the
  PDF (**with a `User-Agent`** - the host 502s requests without one),
  extracts text, runs `NameListTextParser`, and writes the snapshot;
- a decision on where the parse runs: the cheapest honest option is a JVM
  target for `core:parser` using Apache PDFBox, which is exactly what this
  session already used as the reference implementation;
- a snapshot loader behind the existing `NameSyncRepository` interface on
  web (fetch + parse the snapshot, no PDF work in the browser), which also
  means pdf.js stops being on the web critical path - keep it for a
  user-supplied CORS-enabled URL, or drop it from the web bundle to reclaim
  ~1.5 MB;
- `feature:settings` hiding the source-URL and refresh-period cards on web,
  the same way it already hides the language card
  (`rememberAppLanguageSettingsLauncher()` returning null is the pattern).

## 1. Goal

Turn Pick-A-Name from a single Android Gradle module into a **feature-modular**
Kotlin Multiplatform / Compose Multiplatform project, following the module
shape used by [neteinstein/loopgain](https://github.com/neteinstein/loopgain)
(a shared module + thin per-platform app shells) as the starting point, then
going one step further than loopgain by splitting that shared module into
per-feature and per-concern modules — loopgain's `composeApp` keeps
`domain/data/ui` as packages inside one module; this app keeps them as
**separate Gradle modules** so features can be built, tested, and reasoned
about in isolation. On top of that, this project adds a **web target**
(Compose Multiplatform for Web, Kotlin/Wasm) that deploys to **GitHub
Pages** — something loopgain doesn't have at all. Android stays the
primary, fully-featured target; iOS is structured for from day one
(loopgain's pattern) but isn't built/tested in this pass; web gets a real,
working feature set, not just a demo shell.

## 2. Current app, in one page

- Single module: `app/`, package `org.neteinstein.pickaname`.
- Clean-ish layered structure already: `domain` (pure Kotlin, verified zero
  `android.*` imports), `data`, `di` (Koin), `presentation` (Compose +
  Navigation-Compose + ViewModel), with four screens/flows: **Splash**,
  **Sync** (onboarding + re-sync), **NameList** (browse/search/filter, the
  main screen), **Settings**.
- What the app does: downloads a PDF of legally allowed Portuguese given
  names, extracts its text, parses name/gender pairs, stores them locally,
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

```
portuguese-allowed-names/
├── build-logic/                        # included build: Gradle convention plugins (see §3.2)
│   └── convention/src/main/kotlin/…
├── core/
│   ├── model/            # NameEntry, Gender, RefreshPeriod, SearchEngine, SyncOutcome, … — no deps
│   ├── domain/           # repository interfaces + use cases — depends only on :core:model
│   ├── network/          # Ktor client + NameListRemoteDataSource (expect/actual engine per platform)
│   ├── database/         # NameStore contract; Room actual (Android), IndexedDB-backed actual (web)
│   ├── datastore/        # SettingsRepositoryImpl on multiplatform-settings
│   ├── parser/           # PdfTextExtractor (expect/actual) + NameListTextParser (pure, unchanged)
│   ├── designsystem/     # theme, colors, typography, shapes, gradients, GenderTag, shared composables
│   └── navigation/       # Routes + nav-transition contracts shared by every feature + the app shell
├── feature/
│   ├── splash/           # SplashScreen + SplashViewModel + its own Koin module
│   ├── sync/             # SyncScreen + SyncViewModel + SyncOrigin + its own Koin module
│   ├── namelist/         # NameListScreen + NameListViewModel + its own Koin module
│   └── settings/         # SettingsScreen + SettingsViewModel + its own Koin module
├── composeApp/           # app shell: the ONLY module allowed to depend on every feature module.
│   │                     # Assembles PickANameNavHost from the feature modules' screens + routes,
│   │                     # combines every module's Koin module, exposes the root App() composable.
│   └── build.gradle.kts
├── androidApp/           # thin Android launcher: MainActivity, manifest, launcher icons → composeApp.App()
├── webApp/               # wasmJs browser entry point: main.kt (ComposeViewport) + index.html → composeApp.App()
├── iosApp/               # Xcode project shell (loopgain parity, deferred build-out)
├── distribution/         # unchanged (Play Store listing metadata)
├── .github/workflows/
│   ├── release.yml       # unchanged: Android signed release on merge to main
│   ├── pr-checks.yml     # extended: runs commonTest across every module + wasmJsBrowserTest
│   └── deploy-web.yml    # NEW: build wasmJs distribution, deploy to GitHub Pages
├── gradle/libs.versions.toml  # + Compose Multiplatform, Ktor, Koin (multiplatform), multiplatform-settings
└── settings.gradle.kts   # includes build-logic + every core/*, feature/*, and app module
```

### 3.1 Module boundaries and dependency rules

The whole point of going feature-modular is that Gradle *enforces* the
boundaries the current single-module `presentation/` package only enforces
by convention. Rules, in order of how much they buy:

1. **A `feature/*` module never depends on another `feature/*` module.**
   `feature:sync` cannot import anything from `feature:settings`, even
   though the Sync screen currently navigates to Settings. Cross-feature
   navigation is expressed as a **callback lambda + a route constant from
   `core:navigation`** — exactly the pattern `PickANameNavHost` already uses
   today (`onEditSource = { navController.navigate(Routes.SETTINGS) }`), so
   this rule costs nothing to adopt, it just gets a compiler-enforced
   guarantee on top of it.
2. **`core/*` modules never depend on `feature/*` modules.** Dependencies
   only point inward: `feature → core`, never the reverse. `core:domain`
   depends only on `core:model`; `core:data`-equivalent modules
   (`network`/`database`/`datastore`/`parser`) depend on `core:model` (and,
   where they implement a repository interface, on `core:domain`); nothing
   in `core/` ever needs to know a feature exists.
3. **Only `composeApp` sees every module.** It's the single place that
   assembles the nav graph and the full Koin dependency graph. This keeps
   the "does everything" module intentionally thin (wiring only, no
   business logic of its own) instead of letting the old `app` module's
   role quietly turn into a god-module again.
4. **Feature modules depend on `core:domain`, `core:designsystem`, and
   `core:navigation` only — never directly on `core:network`/`database`/
   `datastore`.** A `SettingsViewModel` calls `UpdateSourceUrlUseCase`, not
   `SettingsRepositoryImpl` directly; Koin resolves the concrete
   implementation at runtime. This is the same dependency-inversion the app
   already has between `presentation` and `data` today, just drawn at a
   module boundary instead of a package boundary.
5. **`core:model` and `core:designsystem` have (almost) no dependencies on
   each other or on anything else in the graph.** They're the leaves —
   safe for every other module to depend on, and cheap for Gradle to
   rebuild when something downstream changes.

Enforcement isn't just aspirational: Gradle physically cannot compile a
`feature:sync → feature:settings` dependency unless it's declared in
`feature/sync/build.gradle.kts`, and code review only needs to check that
new module dependency lines match the rules above, not chase package-level
imports through a single module.

### 3.2 Build-logic convention plugins

Fourteen-ish modules each hand-writing their own `kotlin { androidTarget();
wasmJs { browser() }; iosArm64(); … }` block plus repeated test-dependency
wiring is exactly the kind of boilerplate that makes multi-module KMP
projects painful. Mitigation: an included build `build-logic/` (same
pattern as Google's Now in Android sample) providing a couple of Gradle
convention plugins:

- `pickaname.kmp.core` — applied by every `core/*` module: Kotlin
  Multiplatform plugin, configures the android/wasmJs/iOS targets and
  common test dependencies (`kotlin-test`, `kotlinx-coroutines-test`,
  `turbine`, `truth`), no Compose.
- `pickaname.kmp.feature` — applied by every `feature/*` module: everything
  `pickaname.kmp.core` gives, plus the Compose Multiplatform plugin,
  `koin-compose`, and a dependency on `core:designsystem` + `core:navigation`
  (the two things literally every feature needs) so each feature's own
  `build.gradle.kts` only has to list what's specific to it.
- `pickaname.android.application` — applied by `androidApp` only:
  `compileSdk`/`minSdk`/signing config, kept close to today's
  `app/build.gradle.kts` `android {}` block.

This turns each `core/*` and `feature/*` module's `build.gradle.kts` into
roughly 5–10 lines (apply the convention plugin + list module-specific
dependencies), which is what makes 14 modules maintainable instead of a
copy-paste liability.

## 4. Dependency replacement map

| Layer | Today | Target (common) | Android actual | Web (wasmJs) actual | Lives in |
|---|---|---|---|---|---|
| DI | Koin (android artifacts) | `koin-core` + `koin-compose` (multiplatform) | `koin-android` for `androidContext()` | `koin-core` only | each module's own Koin module, combined in `composeApp` |
| Networking | OkHttp | `ktor-client-core` | `ktor-client-okhttp` | `ktor-client-js` (or `-wasm-js` engine) | `core:network` |
| Settings | DataStore Preferences | `russhwolf/multiplatform-settings` (`Settings` interface) | `DataStoreSettings` or `SharedPreferencesSettings` | `StorageSettings` (browser `localStorage`) | `core:datastore` |
| DB | Room + KSP | `Room` 2.7+ KMP with `RoomDatabase.Builder` in each `actual` | `Room` + bundled SQLite (`androidx.sqlite:sqlite-bundled`) — same as today | **No official Room wasmJs target yet.** Hand-rolled `expect class NameStore` backed by IndexedDB; see §6 risk R1 | `core:database` |
| PDF → text | `pdfbox-android` | `expect fun extractPdfText(bytes: ByteArray): String` | keep `pdfbox-android` (works today) | delegate to `pdf.js` via JS interop (see §6 risk R2) | `core:parser` |
| Navigation | `navigation-compose` (AndroidX) | `org.jetbrains.androidx.navigation:navigation-compose` (CMP multiplatform build, API-compatible) | same | same | `core:navigation` (routes) + `composeApp` (graph assembly) |
| "Open URL" | `androidx.browser` CCT | `expect fun openUrl(url: String)` | Custom Tabs intent | `window.open(url, "_blank")` | `core:navigation` (or a small `core:common`) |
| Splash | `core-splashscreen` | n/a (common) | keep as Android-only, lives in `androidApp` | web has no splash concept — CSS loading state in `index.html` instead | `androidApp` only |
| Coroutines | `kotlinx-coroutines-{core,android}` | `kotlinx-coroutines-core` | `+android` for `Dispatchers.Main` | ships its own `Dispatchers.Main`, no extra artifact | wherever needed |

Room's official KMP support (`androidx.room:room-runtime` 2.7.0+) currently
targets Android, JVM, and Kotlin/Native — **wasmJs is not on the supported
target list**. This is the single biggest platform gap and is called out
again in §6 (R1).

## 5. Phased roadmap

Each phase is its own PR against `claude/kmp-cmp-migration-plan-m6dlst` (or
stacked branches off it), keeps the app buildable at every step, and is
independently reviewable. Nothing merges to `main` until Phase 6 is signed
off manually. Phases 1–2 deliberately stay **Android-only** so the
modularization itself — the part with the widest blast radius — is proven
out and kept regression-free before web platform work is layered on top.

### Phase 0 — Build-logic + leaf modules
- Add `build-logic/` with the convention plugins from §3.2.
- Add `core:model`, `core:domain`, `core:designsystem`, `core:navigation` —
  the leaf modules with no platform-specific code, so they're the cheapest
  possible proof that the module graph and convention plugins actually
  work.
- Move the **domain** layer's models into `core:model` and its
  repository-interfaces/use-cases into `core:domain` verbatim (already zero
  Android imports — a free move).
- Stand up `composeApp`/`androidApp`/`webApp` shells with a placeholder
  `App()` so both targets build and show *something* end to end before any
  real feature lands, and stand up `deploy-web.yml` against that
  placeholder so the Pages pipeline is validated early.

### Phase 1 — Core technical modules (Android actuals only)
- `core:network` (Ktor, Android engine only for now), `core:datastore`
  (multiplatform-settings, Android actual), `core:database` (Room, same
  behavior as today), `core:parser` (PdfTextExtractor + NameListTextParser,
  Android actual keeps `pdfbox-android`).
- Repository implementations move alongside their data source into the
  relevant `core:*` module (or a `core:data` module if keeping
  implementations together reads better once written — decide when the
  code is in front of us) and are wired into Koin.
- Milestone: `androidApp` builds and runs with full feature parity to
  today's `app` module, just restructured into modules — no web yet.

### Phase 2 — Extract feature modules one at a time
Each of the four extractions is its own reviewable PR, in this order
(simplest/most self-contained first):
1. `feature:settings`
2. `feature:sync`
3. `feature:namelist`
4. `feature:splash`, plus moving `PickANameNavHost` assembly into
   `composeApp`.

After each step the app stays fully working on Android — this phase is a
pure refactor with no behavior change, which is what makes it safe to land
incrementally instead of as one large "move everything" commit.

### Phase 3 — Web actuals
- `core:network`, `core:datastore`, `core:database`, `core:parser` each
  gain a wasmJs `actual`: Ktor-JS engine, `localStorage`-backed settings,
  IndexedDB-backed name store (R1), pdf.js-backed text extraction (R2).
- Swap `navigation-compose` for CMP's multiplatform Navigation artifact in
  `core:navigation`/`composeApp`; replace `androidx.browser` CCT calls with
  the `expect fun openUrl(url: String)` actuals (Custom Tabs / `window.open`).
- Verify Material3 theming, `GenderTag`, gradients, and typography all
  render correctly on web (font loading via Compose Multiplatform resources
  is the usual snag).

### Phase 4 — Feature parity + web-specific UX
- Wire every feature module together end to end in `webApp`.
- Add responsive layout tweaks where the current design assumed a phone
  viewport — CMP Web runs at arbitrary window widths.
- Resolve the web sync/CORS question (R3): confirm the IRN PDF source
  allows browser-origin `fetch`/Ktor-JS requests; if blocked, the web build
  ships with the last synced snapshot and a clear "sync only works in the
  Android app" message instead of a silently-broken button.

### Phase 5 — Test + CI parity
- `commonTest` in each `core`/`feature` module carries over the relevant
  parser/mapper/usecase/viewmodel tests, now scoped to the module that owns
  that code (smaller, faster test targets instead of one monolithic suite).
- Add `wasmJsTest` (headless-Chrome Kotlin/Wasm test runner) for
  platform-specific actuals.
- Extend `pr-checks.yml` to run unit tests across every module (Android)
  and `wasmJsBrowserTest` (web) on every PR, and add a lightweight
  module-boundary check (e.g. a Gradle task that fails the build if a
  `feature/*` module declares a dependency on another `feature/*` module)
  so §3.1's rules don't erode silently over time.
- `deploy-web.yml` deploys only on push to `main` (never the migration
  branch), so the live Pages site always reflects the last stable, merged
  state; `workflow_dispatch` remains available to build/deploy on demand.

### Phase 6 — Stabilization + merge decision
- Manual pass on a real device/browser matrix (Android phone, Chrome,
  Firefox, Safari desktop).
- Only then: rebase onto `main`, open the "real" PR, let CI + review gate
  the actual merge. `release.yml` (Play Store) and `deploy-web.yml` (GitHub
  Pages) become the two release paths off the same `main`.

### Phase 7 — Retire `:app` in favor of `composeApp`/`androidApp`
Not part of the original six phases above - surfaced during Phase 2 (first
called out in the `feature:splash` PR) as a prerequisite that got
deferred out rather than folded into a feature extraction, since it's
riskier and touches the release pipeline rather than just app code.
Recorded here as its own explicit, last phase so it doesn't stay an
unstated gap:

- Move what's left in `:app` - `MainActivity`, `AndroidManifest.xml`,
  launcher icon resources, `proguard-rules.pro`, the `di/*Module.kt` Koin
  wiring, and `PickANameNavHost`/`Routes` (plus the nav-transition specs) -
  into `androidApp` (thin Android launcher shell) and `composeApp` (the
  one module allowed to see every feature module, per §3.1) respectively,
  matching §3's original target module layout.
- Delete the `:app` module and its `settings.gradle.kts` entry once
  nothing references it.
- Update `release.yml`, which is hardcoded to `app/` paths throughout:
  the keystore decode target, the `versionName` bump `sed`/grep (currently
  matched against `app/build.gradle.kts`), and the release APK/AAB output
  paths (`app/build/outputs/...`). Also re-check `assembleRelease`/
  `bundleRelease` (invoked bare, with no module prefix) still resolve
  unambiguously once `androidApp` - not `:app` - is the only
  `com.android.application` module in the graph.
- Verify with an actual signed release build (or as close to one as CI
  secrets allow) before calling this done - `release.yml` is what ships
  to the Play Store, so this is the one Phase 7 step that's riskier to get
  wrong than to leave alone, and it's the reason this work stayed out of
  Phase 2 in the first place.
- Only after this lands does `composeApp` stop being a Phase 0 placeholder
  and start being the real app shell §3 always described it as.

## 6. Risk register

- **R1 — No official Room support for wasmJs.** Mitigation: a small
  hand-rolled common `NameStore` interface (`getAll()`, `replaceAll()`,
  `observeCount()`) in `core:database`, with a Room-backed Android `actual`
  and an IndexedDB/JSON-in-`localStorage` web `actual`. Since the whole
  dataset is only the names list (a few thousand short rows, replaced
  wholesale on each sync, never queried relationally), this is a reasonable
  scope reduction rather than a hack — revisit only if Room ships wasmJs
  support upstream.
- **R2 — No native PDF library for Kotlin/Wasm.** Mitigation: JS interop
  with `pdf.js` in `core:parser`'s wasmJs `actual` (`external` declarations
  + a `<script>` tag in `webApp/index.html`), converting its extracted text
  into the same line-based format `NameListTextParser` already expects, so
  the parser itself needs zero changes. Fallback if pdf.js interop proves
  too fragile: parse a pre-generated JSON snapshot of the names list
  shipped as a static web asset — document as a degradation, not use
  silently.
- **R3 — CORS on the IRN PDF source.** The current Android app sidesteps
  browser CORS entirely (native OkHttp call). A browser `fetch`/Ktor-JS
  call to the same URL may be blocked if the server doesn't send
  `Access-Control-Allow-Origin`. Verify early (Phase 4) with a manual
  `fetch()` from a browser console against the real source URL; if blocked,
  ship the last synced snapshot with a clear message rather than a
  silently-broken sync button on web.
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
- **R7 — Module-count overhead.** Going from 1 module to ~14 (4 `core` +
  4 `feature` + 4 app shells + `build-logic` + room to grow) adds Gradle
  configuration time and more files to navigate. Mitigated by the
  convention plugins in §3.2 (so per-module boilerplate stays tiny),
  Gradle's configuration cache, and by not fragmenting further than §3's
  layout — e.g. not splitting a feature's own UI/viewmodel into yet more
  sub-modules unless a concrete pain point shows up later.

## 7. GitHub Pages deployment

- New workflow `.github/workflows/deploy-web.yml`:
  - Triggers on push to `main` only (plus manual `workflow_dispatch`), so
    the live Pages site never deploys in-progress work from the migration
    branch.
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
- Does not fragment modules further than §3's layout "just in case" — the
  boundaries are drawn around the app's actual four features and its actual
  technical concerns, not a generic template applied for its own sake.

## 9. Gaps in this plan, found while implementing Phase 3

Things the phases above don't cover, written down as they surfaced rather
than left implicit. Roughly in order of how much they'd hurt if ignored.

### 9.1 CI cannot currently catch a broken web build - FIXED

`pr-checks.yml` runs lint, Android unit tests, the emulator suite and an
APK build. It never builds wasmJs; `deploy-web.yml` does, but only *after*
merge to `main`. Now that the web target is the real app rather than a
placeholder, that gap means a PR can go green and break the site.

Worse, a build check alone wouldn't have caught this phase's most expensive
bug: the Compose version skew compiled perfectly and only failed when the
page loaded (`IrLinkageError`). So Phase 5 needs **two** web checks, not
one:
- `:webApp:wasmJsBrowserDistribution` on every PR, plus the module
  `wasmJsTest` suites (they run in headless Chrome on a CI runner the same
  way they do locally);
- a **runtime smoke check** that actually loads the built page in a browser
  and fails on any uncaught exception. A `runComposeUiTest` in
  `composeApp`'s `commonTest` that mounts `App()` would cover the same
  class of failure and run on both targets.

`pr-checks.yml` now has a **web** job (wasmJs tests in headless Chrome plus
the same production distribution `deploy-web.yml` publishes) and an **iOS**
job (shared tests on a simulator plus linking the framework). The runtime
smoke check is still missing - a `runComposeUiTest` in `composeApp` that
mounts `App()` would be the natural home for it - and that is what would
have caught this phase's `IrLinkageError` before a browser did.

`kotlin-js-store/` stays gitignored; now that a wasm build runs on every
PR, committing the lock is a decision that could reasonably be revisited.

### 9.2 `build-logic` convention plugins are now overdue

Phase 0 deferred them with an explicit trigger: "once Phase 2's feature
modules make the per-module boilerplate repeat enough to be worth
abstracting". That threshold has passed - there are 15 modules, and the
KMP ones' `build.gradle.kts` files are near-identical (same two targets,
same `jvmTarget`, same `compileSdk`/`minSdk`, same Compose set). Three of
them in this phase were written by copying another module's file. The next
structural change (a new target, an AGP bump, a compileSdk bump) has to be
made 15 times by hand.

### 9.3 The settings-store swap silently dropped existing users' preferences — FIXED

Phase 1 moved `SettingsRepositoryImpl` from DataStore Preferences to
multiplatform-settings backed by `SharedPreferencesSettings("pick_a_name_
settings")`, with **no migration** from the old DataStore file. For anyone
who already had the app installed, that resets the configured source URL,
the refresh period and the last-refresh timestamp to defaults on first
launch after the update (the reset timestamp also forces one extra sync).

This already shipped (it went to `main` before this branch), so it couldn't
be prevented - but it *can* still be recovered, and now is:
`core:datastore`'s `androidMain` has a one-time
`migrateLegacyDataStoreSettings()` that reads the old
`pick_a_name_settings.preferences_pb` file and copies the four keys across
(they never changed name - only the backend did), then deletes it. It runs
inside the `single<FlowSettings>` provider, before anything can read a
setting, and starts with a `File.exists()` check so every launch after the
first pays nothing. Existing values in the new store always win, and an
unreadable legacy file is left in place rather than deleted - deleting data
we failed to read once is the exact mistake being repaired here.

The lesson for future store swaps stands: a backend change is a data
migration, whether or not the keys move.

### 9.4 The snapshot decision (R3) needs a JVM target that doesn't exist yet

The chosen R3 direction - a CI-generated snapshot - has to parse the PDF
*somewhere that isn't a browser or an Android device*. `core:parser` has no
`jvm()` target today, and its Android actual uses `pdfbox-android`. The
straightforward route is a JVM target whose actual uses Apache PDFBox
(exactly the reference implementation this phase compared pdf.js against),
driven by a small Gradle task the workflow calls. That's a new target and a
new dependency - not a detail of Phase 4, a small piece of design.

It also raises questions Phase 4 should answer explicitly: how the web UI
communicates snapshot freshness ("list as of <date>"), and what happens
when the scheduled job fails (stale snapshot, or visible warning?).

### 9.5 The web build has no URL or history story (title and icon: FIXED)

Navigation works, but the browser's address bar never changes - every
screen is `/index.html`, so links can't be shared, refresh always restarts
at splash, and the browser back button does nothing (`PlatformBackHandler`
is deliberately a no-op on web). None of this is covered by Phase 4's
"responsive layout tweaks"; it's the difference between "the app renders in
a browser" and "it behaves like a web page".

The page title and icon *are* now sorted: the title is set, and
`favicon.svg` is the app's adaptive-icon artwork converted to SVG (browsers
can't read Android vector drawables), which also clears the 404 the console
used to show on every load.

### 9.6 Binary size has a number but no budget (R6)

The production distribution is ~16 MB uncompressed (~8 MB of that is
skiko.wasm, ~1.5 MB pdf.js). `deploy-web.yml` prints the size but nothing
acts on it. Phase 4 should set a target and name the levers: dropping
pdf.js from the web bundle once snapshots land (it stays useful only for a
user-supplied CORS-enabled URL), and checking what GitHub Pages actually
serves compressed.

### 9.7 The only end-to-end Android test is still disabled - FIXED

`SplashSmokeTest` has been `@Ignore`d since PR #38 for CI flakiness, so the
instrumented job currently proves only that the app compiles and installs.
It is re-enabled - and the "flakiness" turned out to be a wrong assertion,
not an emulator problem. The test waited for the **app name**, which is on
the splash screen and the name list but *not* on the sync screen. On a
fresh install (every CI emulator) the database is empty, so the app routes
splash → Sync, and the app name is only on screen for
`SplashViewModel`'s ~900 ms minimum - the test was racing that window and
losing. It passed locally only because the local emulator already had a
populated database, which is exactly the sort of difference that makes a
test look haunted.

It now waits for **any** of the app's legitimate first screens (app name,
sync loading, sync error, or the list's search hint), read from the real
Compose resources rather than hardcoded. That removes the race while still
failing for the regression worth catching: an app that launches to nothing.
Verified three consecutive runs on two emulators, from a *fresh install*
(the case that used to fail).

### 9.8 Smaller items

- **`material-icons-extended` is pinned at 1.7.3**, the last multiplatform
  release JetBrains published. It works (icons are just `ImageVector`s) but
  it is a dead coordinate; a maintained icon source will be needed
  eventually.
- **iOS** is no longer an aside: every KMP module now has `iosArm64` +
  `iosSimulatorArm64` targets with real actuals (PDFKit for text
  extraction, `NSUserDefaults` for both stores, Foundation's diacritic
  folding, `UIApplication` for opening URLs and the per-app language
  screen), `composeApp` exposes a `MainViewController()` entry point, and
  CI links the framework and runs the shared tests on a simulator. What
  does **not** exist is an Xcode project, so nothing has been *run* on iOS
  - the checks prove it compiles, links and passes shared tests, which is
  the honest limit without an app shell.
- **No shared UI tests at all.** Compose Multiplatform supports
  `runComposeUiTest` in `commonTest`; the four feature modules have
  ViewModel tests only - though those now run on all three platforms (see
  §9.9), so the gap is UI rendering specifically.
- **Two Android shells now exist** (`:app` and `androidApp`) with separate
  Applications and manifests. That is the intended Phase 7 setup, but it
  means every Android-shell change has to be made twice until `:app` is
  retired - so Phase 7 shouldn't drift.

### 9.9 Test parity, and what it cost

All four ViewModel test suites moved from `androidUnitTest` to
`commonTest`, so the same 30 tests now run on Android, wasmJs **and** iOS.
Two things had to change to get there:
- **MockK is JVM-only**, so mocked use cases became real use cases over
  hand-written fakes in `core:testing` (`FakeSettingsRepository`,
  `FakeNameRepository`, `FakeNameSyncRepository`). That is a better test
  anyway: assertions now read "the setting was persisted" rather than "a
  mock was called", and the use-case wiring is covered too. `core:testing`
  is a KMP module now; `MainDispatcherRule` stays in its `androidMain` for
  the suites that still use JUnit.
- **JUnit rules are JVM-only**, so `MainDispatcherHarness` replaces the
  rule in common code. Install it from `@BeforeTest`/`@AfterTest`, *not*
  around the `runTest` body: a `stateIn(viewModelScope, ...)` job can still
  be dispatching while `runTest` drains its scheduler, and resetting `Main`
  before that finishes makes those dispatches throw.

Also found on the way: building the feature modules' wasmJs **test**
binaries needs more heap than the Gradle template's 2 GB default, and the
failure is misleading - an `OutOfMemoryError` in one module surfaces as
"Back-end: Please report this problem" in whichever module compiles next.
`org.gradle.jvmargs` is now 4 GB.

The use-case tests in `core:domain` stay on JUnit/MockK for now: they are
pure logic with no platform surface, so running them three times buys less
than the ViewModel suites did.

## 10. Phase 7, as actually done: one Android shell, not a renamed one

Phase 7's goal was "`composeApp` stops being a placeholder and becomes the
real app shell", with `:app`'s remains moved into `androidApp` and `:app`
deleted. The **goal is met**, but the *direction* of the move was inverted,
deliberately:

**`androidApp` was deleted; `:app` is the Android shell.**

Why that way round:
- The goal was never about the module's name. `composeApp` now owns the
  theme, nav graph, every feature module and the whole Koin graph; the
  Android module owns `MainActivity`, the manifest, launcher resources,
  proguard rules and Koin startup - which is exactly the "thin Android
  launcher shell" Phase 7 describes. `webApp` and the iOS framework consume
  `composeApp` the same way.
- `:app` is the module the Play Store pipeline points at, in eight places
  in `release.yml` (keystore path, the `versionName` bump, APK/AAB output
  paths, artifact names). Renaming it is a rename of the one path that
  ships to users, and Phase 7 itself demands verification "with an actual
  signed release build" - which cannot be done from here, because the
  signing secrets live in GitHub Actions.
- Keeping two Android shells was itself a problem (§9.8: every shell change
  had to be made twice). Deleting the duplicate solves that *now*, at zero
  risk to the release path, instead of trading it for pipeline risk.

What was verified, as close to a real release as local secrets allow: a
full `assembleRelease` (R8 in full mode, `isShrinkResources`, the real
proguard rules), signed with a locally generated key, installed on an
emulator and run. It synced and listed all 7,481 names - so Koin, Room,
Ktor and pdfbox all survive obfuscation with the DI graph now living in
`composeApp`.

If the `androidApp` name is still wanted, it is a mechanical rename plus
those eight `release.yml` paths, and it should be done by someone who can
watch a real signed release run afterwards.
