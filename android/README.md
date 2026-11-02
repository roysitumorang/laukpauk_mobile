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

`.github/workflows/android.yml` builds and lints every push to `dev`/`main` and every pull request that touches
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
| Edge-to-edge enforced; opt-out ignored for targetSdk 36+ | `MainActivity` (the only Activity) applies status-bar, navigation-bar, cutout and keyboard insets once and paints a status-bar scrim (`setStatusBarColor`, green under headers via `ui/common/StatusBar`) |
| Predictive back on by default; `onBackPressed()` and `KEYCODE_BACK` no longer delivered | `enableOnBackInvokedCallback="true"`; back is handled by the Navigation component, and screens that intercept back register an `OnBackPressedCallback` with their view lifecycle |
| Orientation/resizability ignored on screens ≥ 600 dp | XML layouts use match_parent/weights, never fixed screen widths; Fragments + ViewModels keep state across configuration changes |
| `scheduleAtFixedRate` / JobScheduler quota changes | No Timer, IntentService or JobService: the delivery-schedule expiry check is a main-thread Handler loop that runs only while `MainActivity` is resumed (`ui/startup/AppStartup`) |
| Photo access / partial media access | `ActivityResultContracts.PickVisualMedia` and `TakePicture` through `FileProvider` (`ui/common/ImagePicker`); no storage or media permissions |
| `POST_NOTIFICATIONS` (13+) | Requested once at runtime on API 33+ |
| 16 KB memory pages | AGP 9.5 packages native libraries uncompressed and 16 KB-aligned; CI runs `zipalign -c -P 16` on the APK |
| Play target-API deadline (Aug 31, 2026: new apps and updates must target API 36) | `targetSdk 37`, `compileSdk 37` |

Toolchain: Android Gradle Plugin 9.5.0, Gradle 9.8.2, JDK 17, minSdk 24.

## Architecture

A standard single-Activity Android app in Java, following Google's "Guide to app architecture":

```
UI layer       ui/<feature>/XxxFragment  ── observes ──▶  XxxViewModel (LiveData<Resource<T>>)
                     │ ViewBinding (res/layout/*.xml)            │
                     │ NavController (res/navigation/nav_graph)  ▼
Data layer                                              data/repository/XxxRepository
                                                          │ Retrofit API (data/remote/XxxApi)
                                                          │ DTOs (data/model/<feature>)
                                                          └ SessionStore / Prefs (data/local)
```

| Package | Contents |
| --- | --- |
| `MainActivity` | the only Activity: NavHostFragment, bottom navigation, insets, deep links (`ui/startup/MainRouter`) |
| `ui/<feature>` | Fragments, their ViewModels, RecyclerView `ListAdapter`s and small custom views — one package per feature (auth, home, search, product, market, cart, checkout, orders, account, seller, …) |
| `ui/common` | shared UI: `HeaderView` (buyer header with cart/notification badges), `AppBars`, `Messages`, `SearchBox`, `ImagePicker`, `AuthGate` (sign-in gating), `NavResults` (results to the previous screen), `Images` (Glide) |
| `ui/delivery`, `ui/product/ProductActions` | cross-feature flows: choosing the delivery address + schedule, the "Beli" unit sheet + add to cart |
| `data/repository` | one repository per domain (`CartRepository`, `DeliveryRepository`, `ProdukRepository`, …). UI-free; network through Retrofit `enqueue`, results as `Resource<T>` (`LOADING` / `SUCCESS` / `ERROR`) |
| `data/remote` | Retrofit interfaces, `ApiClient` (OkHttp + Moshi; adds `versionCode` to every request), `LenientJsonAdapterFactory` (the backend sends `"1"`/`1`/`true`, `""`/`false` for missing objects) |
| `data/model/<feature>` | typed JSON DTOs: public fields named like the JSON keys, `@Json(name = …)` where they differ. Genuinely dynamic payloads are untyped trees (`Map<String, Object>` / `List<Object>`, read with `util/Json`; whole numbers are `Long`, see `util/JsonTreeAdapter`) |
| `data/local` | `Prefs` (Flutter's `FlutterSharedPreferences` file and `flutter.` keys, so existing users stay signed in) and `SessionStore` |
| `di` | `AppContainer` (generated) and `ViewModelFactory` |
| `util` | stateless helpers and constants (`Json`, `Currency`, `ServerPath`, …) |

### Navigation

Every screen is a destination in `res/navigation/nav_graph.xml`. Arguments are primitives; structured data is
passed as JSON text and parsed with the app's Moshi (`apiClient.fromJson` / `toJson`). Screens that need a signed-in user call
`AuthGate.require(this, this::load)`; `LoginFragment` reports back through the previous back-stack entry's
`SavedStateHandle` (conditional navigation). Screens return results the same way (`NavResults`). Named server
routes (push notifications, konten banners) go through `MainRouter.route(navController, "/produk", args)`.

### Dependency injection

Manual constructor injection, no DI library. Each repository takes `RepositoryDeps` (Retrofit, session,
prefs, executors) and may take other repositories (`CartRepository(RepositoryDeps, DeliveryRepository)`).
`di/AppContainer` creates every repository once, in dependency order, and lives in `LaukpaukApp`. It is
**generated** by `tools/gen_container.py` from the classes in `data/repository`:

```bash
python3 tools/gen_container.py   # after adding/removing a repository or changing its constructor
```

CI fails when the committed `AppContainer` is out of date. Fragments reach the container once and hand the
repositories to their ViewModel:

```java
AppContainer c = AppContainer.from(requireContext());
viewModel = new ViewModelProvider(this,
        ViewModelFactory.of(() -> new NotifikasiViewModel(c.notifikasiRepository)))
        .get(NotifikasiViewModel.class);
```

`ui/notification` is the reference implementation of a feature (repository, API, DTOs, ViewModel, Fragment,
adapters, layouts).

### Resources

Layouts `fragment_<screen>`, `item_<feature>_*`, `view_<feature>_*`, `sheet_<feature>_*`, `dialog_<feature>_*`;
strings in `values/strings_<feature>.xml`; Poppins (`res/font`) is the theme font; bitmap artwork stays in
`assets/img` (loaded with `Images.asset`).

## Replaced Flutter plugins

| Flutter | Android |
| --- | --- |
| http | Retrofit 3 + OkHttp 5 + Moshi (`data/remote`) |
| provider | ViewModel + LiveData, shared state in repositories (`cartCount()`, `selection()`, `unreadCount()`) |
| shared_preferences | `SharedPreferences`, same file and keys (`data/local/Prefs`) |
| geolocator, google_maps_flutter | Play services Fused Location, Maps SDK `MapView` |
| onesignal_flutter | OneSignal Android SDK 5 |
| firebase_dynamic_links | Android App Links (`lp.me`). Firebase Dynamic Links shut down on Aug 25, 2025; `lp2021.page.link` links return 404 |
| image_picker | Photo picker / camera (`ui/common/ImagePicker`) |
| carousel_slider | `ViewPager2` / RecyclerView |
| url_launcher, share | `ACTION_VIEW` / `ACTION_DIAL` / `ACTION_SEND` |
| Xendit method channel | Xendit Android SDK 4.2.5 (no CardinalCommerce repository needed) |

## Not verified yet

The port has been compiled (CI) but not run against the production backend or on a device. Before release:
run the buyer and seller flows on staging, every payment method included, and compare the API request
sequence of both apps for the same flows.
