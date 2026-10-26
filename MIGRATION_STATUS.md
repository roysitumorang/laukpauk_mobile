# Laukpauk native Android migration

Target: native Android using Java, Android SDK views/XML, and Gradle. Flutter/Dart must not be part of the final build.

## Migration safety

This branch is an incremental migration. Original `lib/` Dart files are retained as source material until each feature has a tested native replacement. Do not delete the legacy sources or merge this branch as a completed migration yet.

## Work started

- [x] Create isolated branch `migration/native-android-java`; leave `main` unchanged.
- [x] Replace Flutter-dependent Gradle configuration with Android Gradle Plugin configuration.
- [x] Replace Flutter `MainActivity` with a native Java Android Views launcher.
- [x] Remove Flutter engine/method/event channels from the Android entry point and manifest.
- [x] Add native buyer/seller route placeholders and preserve HTTPS deep-link intent filters.
- [x] Add native preferences helper with legacy Flutter SharedPreferences read fallback.
- [x] Add asynchronous JSON HTTP helper and Indonesian currency formatter.
- [ ] Port all `lib/models`, `lib/libraries`, global components, buyer screens, seller screens, and state management to Java.
- [ ] Configure the actual API base URL securely from known project configuration; port authentication and all endpoint contracts.
- [ ] Migrate persisted user/session/cart/delivery data and verify backwards compatibility.
- [ ] Complete native Firebase/OneSignal notifications, maps/location, image picking, sharing, URL launching, and Xendit payment flows.
- [ ] Port app assets/fonts and all layouts.
- [ ] Add unit/instrumentation tests and verify debug/release APK builds on JDK 17.
- [ ] Remove `lib/`, `pubspec.yaml`, `pubspec.lock`, Flutter metadata, iOS Flutter runner, and Flutter web artifacts only after parity checks pass.

## Current limitations

The native shell is not feature parity: buyer/seller destinations currently explain that the selected route is not migrated yet. The API helper requires an explicit base URL and is not yet connected to screens. Maps configuration is not enabled. No Android SDK/Gradle build or device tests have been run in this environment.

Do not treat this branch as release-ready until every item above is complete and tested.
