# Laukpauk — native Android (Java)

A screen-for-screen Java port of the Flutter app (its Dart sources were removed from this branch; they remain on `main` before this PR and in git history, commit 5390872). Same package name (`id.laukpauk`), same
version code (50600) and same backend API, so it ships as an update to the existing Play listing and users
stay logged in (the app reads Flutter's `FlutterSharedPreferences` file and `flutter.` key prefix as-is).

## Build

Requirements: JDK 17, Android SDK platform 36 and build-tools 36.0.0.

```bash
cp laukpauk.properties.example laukpauk.properties   # fill in the backend URL and keys (git-ignored)
./gradlew assembleDebug                             # app/build/outputs/apk/debug/
./gradlew lintDebug
```

`.github/workflows/android.yml` builds and lints every push to `java-rewrite`/`main` that touches
`android/`. CI runs without `laukpauk.properties`, so placeholder values are compiled in: it proves the
code builds, not that it talks to production.

Release signing reads `key.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`; git-ignored).
Prefer Play App Signing with a fresh upload key: the old `laukpauk.keystore` was committed to this repository and is still in its git history, so treat it as exposed. The Flutter project (lib/, android/, ios/, web/, assets/, pubspec) was removed from the tree; see git history before commit 7e0efa0 for the original sources.

## Configuration (`laukpauk.properties`)

| Key | Was in the Flutter app |
| --- | --- |
| `API_BASE_URL`, `BASE_URL`, `UPLOAD_GAMBAR_*` | `lib/libraries/ServerPath.dart` (never committed) |
| `MAPS_API_KEY` | the Flutter `android/app/src/main/AndroidManifest.xml` (in git history — rotate it) |
| `ONESIGNAL_APP_ID_PRODUCTION` / `_DEVELOPMENT` | `lib/models/MainModel.dart` |
| `XENDIT_PUBLIC_KEY` | the Flutter `android/.../MainActivity.java` (in git history — rotate it) |
| `ENVIRONMENT` | `lib/libraries/Environment.dart` |

## Android 16 (API 36) compatibility

| Android 15/16 change | How the app handles it |
| --- | --- |
| Edge-to-edge enforced; opt-out ignored for targetSdk 36 | `BaseActivity` applies status-bar, navigation-bar, cutout and keyboard insets for every screen and paints a per-screen status-bar scrim (`setStatusBarScrimColor`, green under headers) |
| Predictive back on by default; `onBackPressed()` and `KEYCODE_BACK` no longer delivered | `enableOnBackInvokedCallback="true"`; no screen overrides `onBackPressed`; `WillPopScope` ports use `BaseActivity.setOnBack()` / `goBack()` (AndroidX `OnBackPressedDispatcher`) |
| Orientation/resizability ignored on screens ≥ 600 dp | Layouts use weights, not fixed screen widths; width-scaled text (`Ui.hb`) is capped at a 600 dp screen so tablets and landscape keep phone-sized text |
| `scheduleAtFixedRate` / JobScheduler quota changes | No Timer, IntentService or JobService: the delivery-schedule expiry check is a main-thread Handler loop that runs only while a screen is resumed (`MainModel.startBackgroundWatchers`) |
| Photo access / partial media access | System photo picker (`ACTION_PICK_IMAGES`, `GET_CONTENT` below API 33) and camera capture through `FileProvider`; no storage or media permissions |
| `POST_NOTIFICATIONS` (13+) | Requested once at runtime on API 33+ |
| 16 KB memory pages | AGP 9.5 packages native libraries uncompressed and 16 KB-aligned; CI runs `zipalign -c -P 16` on the APK |
| Play target-API deadline (Aug 31, 2026: new apps and updates must target API 36) | `targetSdk 36`, `compileSdk 36` |

Toolchain: Android Gradle Plugin 9.5.0 (supports up to API 37), Gradle 9.8.2, JDK 17, build-tools 36.0.0+, minSdk 23 (current Google
Play services releases require it; the Flutter app allowed 21).

## Code layout

Every Dart file has a Java counterpart with the same name (see `tools/` and the mapping below), which keeps
review against the Dart original mechanical.

| Dart | Java |
| --- | --- |
| `lib/models/FooModel.dart` | `id.laukpauk.model.FooModel`, an injected singleton (blocking network methods run off the main thread; methods that showed dialogs take the Activity and a callback) |
| a pushed screen, e.g. `lib/views/pembeli/DetailProduk.dart` | `id.laukpauk.view.pembeli.DetailProdukActivity` (constructor arguments travel as a JSON extra named after the Dart fields) |
| an embedded widget, e.g. `components/HomePage/Kategori.dart` | `id.laukpauk.view.pembeli.components.homepage.Kategori` (a `View`) |
| `lib/global-components/*` | `id.laukpauk.ui.*` |
| `lib/libraries/*`, `HeaderViewModel` (Provider) | `id.laukpauk.core.*`, `core.AppState` (injected) |

Views are built in code (`ui/Ui`, `ui/Txt`, `ui/Layouts`), not XML. `AndroidManifest.xml` is generated from
the Activity classes by `tools/gen_manifest.py`; re-run it after adding a screen.

## Dependency injection

Services and models are **singletons created once and injected**, never called statically. There is no DI
library: `core/AppContainer` is a hand-written composition root.

- **Who is injected:** the core services (`Http`, `HTTPRequest`, `Prefs`, `LocalStorage`, `AuthModel`,
  `Async`, `AppState`) and every class in `model/`. Each one declares its dependencies as constructor
  parameters (e.g. `new NotifikasiModel(http, httpRequest, authModel)`).
- **Who builds them:** `AppContainer`, in dependency order, created once in `LaukpaukApp.onCreate()`.
  Nothing else calls their constructors.
- **Cycles:** `AppState` -> `HeaderModel` and `HeaderModel` -> `TransaksiModel` would make construction
  circular, so those two edges are injected as `Lazy<T>` and resolved on first use.
- **Where Android creates the object** (it can't take constructor parameters), the container is reached once:
  - Activities: `container()` (from `BaseActivity`), e.g. `container().headerModel.adjd(...)`
  - Views and dialogs: a `deps` field, `AppContainer.from(getContext())`
  - static helpers: `AppContainer.from(<context parameter>)`
- **Still static, on purpose:** stateless helpers with no I/O (`Json`, `Currency`, `Http.enc`, `Ui`, `Txt`,
  `Layouts`, `Nav`, `CustomAlert`, the `*Ui` screen helpers, `ShopModelUi`) and constants (`ServerPath`,
  `CustomColors`).

When adding a model: give it a constructor taking what it uses, add it to `AppContainer`, and use it through
`container()` / `deps`. Never add a `static` method that does I/O or holds state.

## Replaced Flutter plugins

| Flutter | Android |
| --- | --- |
| http | OkHttp 4 (`core/Http`) |
| provider | `core/AppState` |
| shared_preferences | `SharedPreferences`, same file and keys (`core/Prefs`) |
| geolocator, google_maps_flutter | Play services Fused Location, Maps SDK `MapView` |
| onesignal_flutter | OneSignal Android SDK 5 |
| firebase_dynamic_links | Android App Links (`lp.me`). Firebase Dynamic Links shut down on Aug 25, 2025; `lp2021.page.link` links return 404 |
| image_picker | Photo picker / camera intents (`model/ImagePickerModel`) |
| carousel_slider | `ViewPager2` |
| url_launcher, share | `ACTION_VIEW` / `ACTION_DIAL` / `ACTION_SEND` |
| Xendit method channel | Xendit Android SDK 4.2.5 (no CardinalCommerce repository needed) |

## Not verified yet

The port has been compiled (CI) but not run against the production backend or on a device. Before release:
run the buyer and seller flows on staging, every payment method included, and compare the API request
sequence of both apps for the same flows.
