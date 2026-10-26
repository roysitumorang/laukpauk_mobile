# Laukpauk native Android migration

Target: native Android using Java, Android SDK views/XML, and Gradle. Flutter/Dart must not be part of the final build.

## Migration safety

This is an incremental migration. Original `lib/` sources remain as reference until each feature has a tested native replacement. Do not delete the Dart sources or cross-platform directories yet.

## Completed in this branch

- [x] Isolated migration branch; `main` remains unchanged.
- [x] Native Java launcher, route placeholders, and HTTPS deep-link intent filters.
- [x] Native preferences helper with legacy Flutter SharedPreferences read fallback.
- [x] Asynchronous JSON HTTP helper, Indonesian currency formatter, and authentication adapter for the two known AuthModel endpoints.
- [x] Build-time API base URL from `LAUKPAUK_API_BASE_URL` Gradle property/environment variable; HTTPS validation.
- [x] Build-time Maps key placeholder from `MAPS_API_KEY`; no API key hardcoded in Android manifest.
- [x] Native Android helpers for image selection, sharing, URL launching, and fused location.
- [x] Copied existing image assets and Poppins regular font into Android assets.
- [x] Added JUnit unit tests and Android instrumentation tests.
- [x] Added GitHub Actions workflow to run unit tests and build debug/release APKs on JDK 17.

## Remaining before parity

- [ ] Port all `lib/models`, global components, buyer screens, seller screens, and state management to Java. Current buyer/seller screen destinations are still placeholders.
- [ ] Identify/confirm production API base URL. It is not discoverable from tracked repository sources: Dart imports `ServerPath.dart`, but no such file is present in the repository tree. Do not guess an API host.
- [ ] Implement every API contract and full authentication workflow, serialization, retries/error handling, pagination, and upload contracts.
- [ ] Migrate persisted session/cart/delivery data and verify legacy SharedPreferences keys on device.
- [ ] Complete Firebase/OneSignal notification integration and configure real Firebase project files/credentials. Existing Dart dependencies do not provide a tracked native Firebase configuration.
- [ ] Complete Maps UI and delivery scheduling flows; configure a valid Maps API key.
- [ ] Complete and test Xendit payment flows against merchant configuration; do not use production payment credentials in source.
- [ ] Port all required assets/fonts and screen layouts; only existing image assets and Poppins regular font are copied at this stage.
- [ ] Run workflow, fix any build failures, run connected instrumentation tests, and verify login, checkout, notifications, payments, and legacy data on devices.
- [ ] Remove `lib/`, `pubspec.yaml`, `pubspec.lock`, Flutter metadata, iOS runner, and Flutter web artifacts only after native parity is proven.

## Build configuration

Set `LAUKPAUK_API_BASE_URL=https://your-verified-api-host` in the local build environment or `LAUKPAUK_API_BASE_URL=...` in untracked `~/.gradle/gradle.properties`. The repository does not contain a verified API host, so builds default to an empty value until configured. Supply `MAPS_API_KEY` separately when Maps is enabled.

## Verification status

No Gradle build or device tests have yet been run by the repository-editing environment. The GitHub Actions workflow has been added but its result is pending. This remains an incomplete migration, not a release-ready application.
