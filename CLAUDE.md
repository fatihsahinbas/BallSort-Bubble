# Ball Sort Puzzle (Android) – project notes

Kotlin + Jetpack Compose, MVVM/StateFlow, single `:app` module. Offline, no backend.
applicationId `io.github.fatihsahinbas.ballsort` (namespace still `com.example`).

## Hard rules (security / Play policy)
- No Gemini/AI/LLM calls, no API keys or secrets in code or git history.
- Permissions only: INTERNET, ACCESS_NETWORK_STATE, AD_ID, VIBRATE (haptics).
- SDKs only: AdMob (`play-services-ads`, includes UMP), Play Billing. No analytics/crash/Firebase.
- UMP consent before any ad request (`UMPManager.canRequestAds`); Settings re-opens privacy form.
- Billing: acknowledge purchases; restore via `queryPurchasesAsync`; PENDING never unlocks.
- AdMob test IDs live in `ad/AdConfig.kt` + manifest `APPLICATION_ID`; swap only for release. Never tap own live ads.
- Keystores (`*.jks`, `*.keystore`) never committed. Release signing via env: `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`.
- Target audience 13+.

## Domain
- `model/` pure game core: `Ball`, `BallColor`, `Tube`, `BallSortGenerator` (reverse-move scramble ⇒ always solvable, seed = level). Single source of truth for levels.
- `ui/GameViewModel.kt` move rules, undo stack, bonus tubes, win → `GamePreferences` + `LeaderboardManager`.
- `data/LevelManager.kt` legacy wrapper, unused by the game (safe to delete).

## Commands
- Unit tests: `./gradlew :app:testDebugUnitTest` (no wrapper committed: use Gradle 9.3.1)
- Release bundle: `KEYSTORE_PATH=... STORE_PASSWORD=... KEY_PASSWORD=... ./gradlew :app:bundleRelease`

## Release checklist
1. Real AdMob App ID (manifest + `AdConfig.ADMOB_APP_ID`) and ad unit IDs.
2. `remove_ads` in-app product created in Play Console.
3. Bump `versionCode`/`versionName`.
4. Privacy page live (`docs/privacy/`, GitHub Pages from `/docs`), CONTACT_EMAIL filled.
5. Data Safety per `docs/DATA_SAFETY.md`.
