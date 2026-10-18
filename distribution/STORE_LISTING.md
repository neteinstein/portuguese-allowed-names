# Play Store Listing Checklist

Reference for every field Google Play Console asks for when creating the store listing for
**Allowed Names in Portugal** (`org.neteinstein.pickaname`). **Default/main listing language:
Portuguese (Portugal) — `pt-PT`**, with English (`en-US`) as a secondary translation, matching
the app's own supported locales (`values/` and `values-pt/`).

Text fields and graphics are both checked into this repo (see below); only Console-only settings
are not representable as repo files and are called out explicitly as manual steps.

## 1. Create app

- **App name**: `Nomes Permitidos em Portugal` (see `title.txt` below)
- **Default language**: Portuguese (Portugal) — `pt-PT`
- **App or game**: App
- **Free or paid**: Free

## 2. Main store listing — text (✅ in this repo)

Source of truth: `distribution/metadata/android/<locale>/`. Character counts below include no
trailing newline and are within Play Console's limits.

| Field | Limit | `pt-PT` (default) | `en-US` |
|---|---|---|---|
| Title | 30 | `title.txt` — 28 chars | `title.txt` — 25 chars |
| Short description | 80 | `short_description.txt` — 74 chars | `short_description.txt` — 69 chars |
| Full description | 4000 | `full_description.txt` — 1731 chars | `full_description.txt` — 1502 chars |

Copy each file's contents verbatim into the matching Play Console field for that language (or
wire up `fastlane supply` / `triple-t/gradle-play-publisher` to publish them directly).

## 3. Main store listing — graphics (✅ in this repo)

Source of truth: `distribution/metadata/android/<locale>/images/`, following the same
[fastlane `supply`](https://docs.fastlane.tools/actions/supply/#images-and-screenshots) layout as
the text metadata, so the whole `distribution/metadata/android/` tree can be published as-is by
`fastlane supply` or `triple-t/gradle-play-publisher`.

| Field | Spec | Path |
|---|---|---|
| App icon | 512×512, 32-bit PNG, alpha | `images/icon.png` (same file in both locale folders — it has no text) |
| Feature graphic | 1024×500, 24-bit PNG, no alpha | `images/featureGraphic.png` (localized — contains the app name) |
| Phone screenshots | 1080×1920, 24-bit PNG, no alpha | `images/phoneScreenshots/1_splash.png` … `4_settings.png` |
| 7" tablet screenshots | 1200×1920, 24-bit PNG, no alpha | `images/sevenInchScreenshots/1_splash.png` … `4_settings.png` |
| 10" tablet screenshots | 1600×2560, 24-bit PNG, no alpha | `images/tenInchScreenshots/1_splash.png` … `4_settings.png` |

Each device bucket has the same 4 screenshots: splash, name list (unfiltered), name list
(filtered by gender + initial, to show the filtering feature), and settings. All dimensions and
alpha channels were verified programmatically against the current Play Console asset spec.

**Provenance note:** no Android emulator/device was available to capture these, so instead of
being live screen captures, they were generated pixel-accurately from the app's actual source of
truth — the exact theme colors (`presentation/theme/Color.kt`), launcher icon vector geometry
(`res/drawable/ic_launcher_{background,foreground}.xml`), and screen layouts/copy
(`NameListScreen.kt`, `SettingsScreen.kt`, `SplashScreen.kt`, `strings.xml`). They are faithful
recreations of the real UI, not mockups with invented colors or fabricated layouts — but if you'd
prefer genuine device captures before publishing, that's a reasonable follow-up (e.g. via
`fastlane screengrab` or manual capture) and these files can simply be overwritten in place.

## 4. Store settings (Console-only — recommended values)

| Field | Recommended value |
|---|---|
| App category | **Books & Reference** (closest fit for a lookup/reference tool); *Lifestyle* is a reasonable alternative |
| Tags | Pick up to 5 from Play Console's own list, e.g. Reference, Government, Parenting |
| Contact email (required) | `neteinstein@gmail.com` |
| Website (optional) | `https://github.com/neteinstein/portuguese-allowed-names` |
| Phone (optional) | none |
| External marketing | Optional opt-in; no strong recommendation either way |

## 5. App content declarations (Console-only — recommended answers)

These match the app's actual behavior as documented in
[`PRIVACY_POLICY.md`](../PRIVACY_POLICY.md) (no accounts, no ads, no analytics, no data
collection):

| Declaration | Recommended answer |
|---|---|
| Privacy Policy URL | `https://nomespermitidosemportugal.neteinstein.org/privacy.html` (generated from `PRIVACY_POLICY.md`; the same page the website and the app's Settings link to) |
| App access | "All functionality is available without special access" (no login) |
| Ads | "No, my app does not contain ads" |
| Content rating questionnaire — category | "Reference, News, or Educational" |
| Content rating questionnaire — content | Answer "No" to all violence/sexual/gambling/controlled-substance/user-generated-content questions → expect Everyone / PEGI 3 / USK 0 (confirm by completing the questionnaire in Console) |
| Target audience | Primary audience 18+ (app is a utility for adults/parents); answer "No" to "designed to appeal primarily to children" |
| News app | No |
| COVID-19 contact tracing/status app | Not applicable |
| Government app | No |
| Financial features | No |
| Data safety — collects/shares user data? | **No data collected** |
| Data safety — has a privacy policy? | Yes — URL above |

## 6. Release

Signed AAB build + GitHub release is already automated (`.github/workflows/release.yml`). The
`upload-google-play` step exists but is intentionally disabled (`if: false`) until:

1. `PLAY_STORE_JSON_KEY` secret is configured (see `.github/CI_CD_SETUP.md`), and
2. The app package (`org.neteinstein.pickaname`) has been created and had at least one manual
   AAB/APK upload in Play Console — the Play Developer API used by that action can only update
   an app that already exists there, it cannot create one from scratch.
