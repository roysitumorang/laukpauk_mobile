# Laukpauk native Android migration

Target: native Android using Java, Android SDK views/XML, and Gradle. Flutter/Dart must not be part of the final build.

## Migration safety

This is an incremental migration. Original `lib/` sources remain as reference until each feature has a tested native replacement. Do not delete the Dart sources or cross-platform directories yet.

## Completed in this branch

- [x] Isolated migration branch; `main` remains unchanged.
- [x] Replaced Flutter-dependent Android Gradle configuration with Android Gradle Plugin configuration.
- [x] Native Java launcher, route placeholders, and HTTPS deep-link intent filters.
- [x] Native preferences helper with legacy Flutter SharedPreferences read fallback.
- [x] Asynchronous JSON HTTP helper and Indonesian currency formatter.
- [x] Build-time API base URL from `LAUKPAUK_API_BASE_URL` Gradle property/environment variable; HTTPS validation.
- [x] Build-time Maps key placeholder from `MAPS_API_KEY`; no API key hardcoded in Android manifest.
- [x] Copied existing image assets and Poppins regular font into Android assets.
- [x] Added JUnit unit tests for currency formatting and Android instrumentation tests for native navigation.

## Remaining before parity

- [ ] Port all `lib/models`, global components, buyer screens, seller screens, and state management to Java.
- [ ] Identify/confirm the production API base URL. It is not discoverable from tracked repository sources: `ServerPath.dart` is imported by Dart code but absent from the repository tree. Do not guess an API host.
- [ ] Implement every API contract and authentication workflow, plus error handling and serialization.
- [ ] Migrate persisted session/cart/delivery data; verify legacy SharedPreferences keys on device.
- [ ] Implement Firebase/OneSignal notifications, maps/location, image picking, sharing, URL launching, and Xendit payment flows with valid project configuration.
- [ ] Port all required assets/fonts and screen layouts; only the existing images and Poppins regular font are copied at this stage.
- [ ] Run `./gradlew testDebugUnitTest`, `./gradlew assembleDebug`, `./gradlew assembleRelease`, and connected instrumentation tests on JDK 17/Android SDK.
- [ ] Remove `lib/`, `pubspec.yaml`, `pubspec.lock`, Flutter metadata, iOS runner, and Flutter web artifacts only after native parity is proven.

## Build configuration

Set `LAUKPAUK_API_BASE_URL=https://your-verified-api-host` in the local build environment or `LAUKPAUK_API_BASE_URL=...` in untracked `~/.gradle/gradle.properties`. This repository does not contain a verified API host, so builds default to an empty value until configured. Supply `MAPS_API_KEY` separately when Maps is enabled.

## Verification status

No Gradle build or device tests have been run by the repository-editing environment. The Java launcher is a migration scaffold, not feature parity or release-ready.
