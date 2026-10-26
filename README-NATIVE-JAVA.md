# Laukpauk Native Android (Java)

This branch begins the migration from Flutter to a native Android application.

## Stack
- Java 11 source compatibility
- Android SDK and Android Views
- AndroidX AppCompat and Material Components
- Native SharedPreferences and HttpURLConnection

## Build
Open the `android/` directory in current Android Studio, install Android SDK Platform 35, and use JDK 17 for Gradle 8.7 / Android Gradle Plugin 8.6.1.

## Migration status
The native application shell, launcher activity, preferences wrapper, and asynchronous HTTP client are the initial migration slice. The home screen intentionally labels unfinished feature areas rather than pretending those workflows have been ported.

Not yet ported: authentication/OTP, catalog/search, markets and geolocation, cart/checkout, Xendit payments, order history and seller tools, Firebase/dynamic links, push notifications, image upload, and delivery scheduling. These need endpoint-by-endpoint and flow-by-flow parity work and device testing.

The API base URL must be confirmed from the existing backend configuration before wiring ApiClient into feature repositories. Migrate preference keys deliberately and securely.

## Security
The legacy repository contains signing-key material and references payment configuration. Rotate exposed credentials, remove keystores and machine-local configuration from version control, and use release signing configuration outside source control. Do not embed secret API credentials in the APK.
