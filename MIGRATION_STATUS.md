# Laukpauk native Android migration

Target: native Android using Java, Android SDK views/XML, and Gradle. Flutter/Dart must not be part of the final build.

## Migration safety

This branch is an incremental migration. The original `lib/` Dart files are retained as source material until each feature has a tested native replacement. Do not delete the legacy sources or merge this branch as a completed migration yet.

## Work started

- [x] Create isolated branch `migration/native-android-java`.
- [x] Replace Flutter-coupled Gradle configuration with Android Gradle Plugin configuration.
- [x] Remove Flutter engine and method/event channel usage from the Android launcher.
- [x] Add a Java-native launcher and native feature placeholder.
- [x] Keep buyer/seller entry points and https deep-link intent filters.
- [ ] Port all `lib/models`, `lib/libraries`, global components, buyer screens, seller screens, and their state management to Java.
- [ ] Replace Flutter SharedPreferences storage keys and migrate persisted user data.
- [ ] Complete native Firebase/OneSignal notifications, maps/location, image picking, sharing, URL launching, and Xendit payment flows.
- [ ] Port app assets/fonts and all layouts.
- [ ] Add unit/instrumentation tests and verify debug/release APK builds on JDK 17.
- [ ] Remove `lib/`, `pubspec.yaml`, `pubspec.lock`, Flutter metadata, iOS Flutter runner, and Flutter web artifacts only after parity checks pass.

## Important

The native shell is not feature parity: buyer/seller destinations currently explain that the selected route is not migrated yet. This commit must not be represented as a finished conversion.
