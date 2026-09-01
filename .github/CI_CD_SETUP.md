# CI/CD Pipeline Setup

This project uses GitHub Actions for continuous integration and continuous deployment.

## Workflows

### 1. PR Checks (`pr-checks.yml`)

Runs on every pull request to `main` or `master` branch:

- **Lint**: Runs Android lint to check code quality
- **Unit Tests**: Executes all unit tests
- **Instrumented Tests**: Runs UI tests on an Android emulator
- **Build**: Builds debug APK

Lint and Unit Tests run as two legs of the same matrix job so they execute in parallel; the
matrix's `fail-fast` behavior means that as soon as one of them fails, the other is cancelled
instead of running to completion, saving CI time on a PR that's already broken. Instrumented
Tests and Build only start once both have passed.

All jobs must pass before a PR can be merged.

### 2. Warm Gradle Cache (`cache-warm.yml`)

Runs on every `push` to `main` or `master` (i.e. right after a PR merges). Its only job is to
build up a shared Gradle dependency cache under a well-known key while running in a trusted
(read-write cache) context — see [Gradle cache](#gradle-cache) below for why `release.yml` can't
do this itself.

### 3. Release (`release.yml`)

Runs when a pull request targeting `main` or `master` is **merged** (`pull_request` with
`types: [closed]`, gated by `github.event.pull_request.merged == true`). Closing a PR without
merging does not trigger a release, and neither does a direct push that bypasses PR review:

- **Test**: Runs lint and unit tests on release variant, against the actual merge commit
- **Bump patch version**: Auto-increments the PATCH component of `versionName`
  (`MAJOR.MINOR.PATCH`) in `app/build.gradle.kts` and pushes that commit straight to the base
  branch, before anything is built. This means `versionName` no longer needs a manual "bump
  version" commit in every PR — MAJOR/MINOR are still bumped by hand when you want one, PATCH
  bumps itself on every merge.
- **Build Release**: Creates a signed APK and AAB bundle, using the version bumped above. The
  signing secrets below are **required** — the job fails fast with a clear error if
  `KEYSTORE_BASE64` is missing, rather than silently producing an unsigned build
- **Verify APK signature**: Runs `apksigner verify` on the built APK so the workflow log always
  shows proof the artifact was actually signed (and with which certificate)
- **Generate Release Notes**: Automatically generates release notes from commits
- **Create Release**: Creates a GitHub release (via `gh release create`) with the version tag,
  attaching both the signed APK and AAB
- **Upload to Play Store**: uploads the signed AAB to the Play Store **internal** testing track
  (`tracks: internal`) via `PLAY_STORE_JSON_KEY`. It does not touch `production` or any other
  track — promoting a build beyond internal testing is still a manual step in Play Console.
  Whether the upload's commit needs `changesNotSentForReview: true`, needs it omitted, or
  rejects either depends on this Play Console account's "Managed publishing" setting (Play
  Console → Setup → Advanced settings), which isn't readable via the Play Developer API and has
  flipped on this account before (with `true`: `Changes cannot be sent for review automatically.
  Please set the query parameter changesNotSentForReview to true.`; without it: `Changes are
  sent for review automatically. The query parameter changesNotSentForReview must not be set.`).
  Rather than hardcoding a value that breaks every time that setting flips, the workflow tries
  `changesNotSentForReview: true` first and, only if that attempt fails, retries the same upload
  without it — so the release survives either state without a workflow edit. **If Managed
  publishing is on, every release still needs one manual step after CI finishes**: open Play
  Console → Internal testing → and click "Send \[N\] changes for review" (or "Publish") before
  the new build actually reaches testers; if it's off, the build goes live on the internal track
  as soon as CI finishes.

### Gradle cache

`release.yml`'s jobs are triggered by `pull_request` `closed`, which (once merged) resolves its
cache scope to the default branch. GitHub only ever issues those jobs a **read-only** cache
token (see [this changelog
post](https://github.blog/changelog/2026-06-26-read-only-actions-cache-for-untrusted-triggers/)),
so they use restore-only `actions/cache/restore` steps with an explicit key/path instead of
`actions/setup-java`'s `cache: gradle` (which would keep attempting, and failing, a save every
run). `cache-warm.yml` runs on every `push` to `main`/`master` — a trigger that always keeps
read-write cache access — purely to populate that same cache key so `release.yml`'s restores
actually have something to hit.

## Required Secrets

To enable signing (required for every release) and, later, Play Store deployment, add the
following secrets in GitHub:

### Signing Secrets (required)

1. **KEYSTORE_BASE64**: Base64-encoded release keystore file
   ```bash
   base64 -i release-keystore.jks | pbcopy
   ```

2. **KEYSTORE_PASSWORD**: Password for the keystore

3. **KEY_ALIAS**: Alias of the signing key

4. **KEY_PASSWORD**: Password for the signing key

Without these four secrets, the `build-release` job fails intentionally at the "Decode
Keystore" step instead of publishing an unsigned release.

### Play Store Secrets (required for the Upload to Play Store step)

5. **PLAY_STORE_JSON_KEY**: Service account JSON key for Play Store API
   - Create a service account in Google Play Console
   - Grant "Release Manager" role
   - Create and download JSON key
   - Paste entire JSON content as secret value

## Branch Protection Rules

To enforce quality gates, configure the following branch protection rules for the default
branch (currently `master` in this repo; the workflows also match `main` in case it's renamed
later):

1. Go to Repository Settings → Branches → Add Rule
2. Branch name pattern: `master` (or `main`)
3. Enable:
   - ✅ Require a pull request before merging
   - ✅ Require approvals: 1
   - ✅ Require status checks to pass before merging
   - ✅ Require branches to be up to date before merging
4. Required status checks:
   - `Lint Code`
   - `Unit Tests`
   - `Instrumented Tests`
   - `Build APK`
5. Save changes

## Release Notes

Release notes are automatically generated from commit messages between tags. Follow conventional commits for better changelog:

- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation changes
- `refactor:` - Code refactoring
- `test:` - Test additions/changes
- `chore:` - Build/tooling changes

## WhatsNew Directory

Not currently used — `release.yml` relies solely on the auto-generated GitHub release notes
(see [Release Notes](#release-notes) above) and does not pass `whatsNewDirectory` to the Play
Store upload step. If you want localized "what's new" text on the Play Store listing itself,
add a `distribution/whatsnew/` directory and wire `whatsNewDirectory: distribution/whatsnew`
back into the upload step. Files must be named `whatsnew-<BCP 47 locale>` (e.g.
`whatsnew-en-US`, `whatsnew-pt-PT` — no `.txt` extension), max 500 characters each. See the
[`upload-google-play` README](https://github.com/r0adkll/upload-google-play#readme) for details.

## Store Listing Metadata

The Play Console "Store listing" fields — text (app name, short description, full description)
and graphics (icon, feature graphic, phone/tablet screenshots) — are tracked as source-controlled
files at `distribution/metadata/android/<locale>/` (the `fastlane supply` layout), so listing
content can be reviewed in PRs like any other change:

```
distribution/
  metadata/
    android/
      pt-PT/   # default/main listing language
        title.txt
        short_description.txt
        full_description.txt
        images/
          icon.png
          featureGraphic.png
          phoneScreenshots/
          sevenInchScreenshots/
          tenInchScreenshots/
      en-US/
        title.txt
        short_description.txt
        full_description.txt
        images/            # same layout as pt-PT
```

These files aren't uploaded automatically yet — the `upload-google-play` action wired up in
`release.yml` only handles the release binary, not the main store listing. Until that's
automated (e.g. via `fastlane supply` or the
`triple-t/gradle-play-publisher` plugin), copy these values into Play Console manually. See
[`distribution/STORE_LISTING.md`](../distribution/STORE_LISTING.md) for the full checklist of
every field Play Console requires to create the listing, including the Console-only settings
(category, contact details, content rating, data safety) that can't live in this repo as plain
text or files.

## Local Testing

Test workflows locally using [act](https://github.com/nektos/act):

```bash
# Install act
brew install act

# Run PR checks (matrix job - runs both the lint and unit-tests legs)
act pull_request -j checks

# Run the release workflow. It triggers on a merged pull_request, so act needs an event
# payload with `pull_request.merged: true` (a plain `act pull_request` payload defaults to
# merged: false and the job will be skipped by design):
echo '{"pull_request": {"merged": true, "number": 1, "merge_commit_sha": "HEAD", "base": {"ref": "master"}}}' > /tmp/pr-merged-event.json
act pull_request -j build-release -e /tmp/pr-merged-event.json
```

## Gradle Configuration

The build.gradle.kts should define version:

```kotlin
android {
    defaultConfig {
        versionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
        versionName = "2.2.1"
    }
}
```

`versionName` is a plain literal in the repo, but you should treat it as owned by CI: the
"Bump patch version" step in `release.yml` increments the PATCH segment and pushes the commit
directly to the base branch on every merge, so day-to-day merges never need a manual edit.
Bump MAJOR or MINOR by hand in a PR (e.g. `"2.3.0"`) only when you want to signal a bigger
release; the next merge's automated bump will then continue from `2.3.1`, `2.3.2`, etc.
`versionCode` is read from a `-PversionCode=<n>` Gradle property so it can always be an
ever-increasing integer; `release.yml` passes `-PversionCode=${{ github.run_number }}` when
building the release APK/AAB, so every release build gets a unique, monotonically increasing
code tied to its CI run. Local/dev builds that don't pass the property fall back to `1`.

`VERSION_NAME` is extracted from `build.gradle.kts` and used for release tagging; `VERSION_CODE`
is taken directly from `github.run_number` (the same value passed to Gradle) rather than parsed
from the file, since it's no longer a literal there.

`app/build.gradle.kts` also declares an empty `signingConfigs.release` and only attaches it to
the `release` build type when the `android.injected.signing.store.file` Gradle property is
present (`release.yml` passes it, along with the store/key password and key alias, on the
command line). This keeps local `./gradlew assembleRelease` runs working unsigned for
day-to-day development while guaranteeing CI produces a properly signed artifact.
