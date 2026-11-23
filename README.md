# DLX Player

DLX Player is a Kotlin Multiplatform media player and Jellyfin client for iOS, iPadOS, tvOS, Android, and Android TV. Media playback and the app UIs are future work; the shared Jellyfin client is built test-first. See the [project brief](PROJECT_BRIEF.md).

## Shared Jellyfin client

`shared` contains the multiplatform Jellyfin client (`app.dlxplayer.shared.jellyfin.JellyfinClient`), built with Ktor and kotlinx.serialization. It targets the JVM (for fast tests), Android, and the Apple platforms, and currently supports:

- `publicSystemInfo()` — public server details without credentials
- `authenticateByName()` — username/password login issuing an access token
- `JellyfinHttpException` — typed errors for HTTP failure statuses

## Development loop

The project is driven by automated tests written before the code, and linted continuously:

```sh
./gradlew :shared:jvmTest   # automated tests (hermetic, Ktor MockEngine only)
./gradlew detekt            # static analysis (config in config/detekt/detekt.yml)
./gradlew spotlessCheck     # ktlint format check
./gradlew spotlessApply     # ktlint auto-format
```

The TDD loop is red/green: add a failing test in `shared/src/commonTest` replaying a captured fixture, run `:shared:jvmTest`, then write the smallest client change that passes, then rerun `detekt` and `spotlessCheck`.

Test fixtures (`shared/src/commonTest/.../Fixtures.kt`) are real responses captured from the Jellyfin reference server, so the suite verifies the exact wire format — including Jellyfin's PascalCase keys — without any network access. **The automated tests never talk to a real server.**

GitHub Actions runs detekt, the format check, and the JVM tests on every push (`.github/workflows/ci.yml`).

## Reference Jellyfin server

A disposable Jellyfin server exists for development-time verification only: capturing wire-format fixtures for tests, and manually checking that the client really works.

```sh
sh scripts/reference-jellyfin.sh start   # boot + bootstrap (root / dlx-test-pass)
sh scripts/reference-jellyfin.sh reset   # wipe config and re-bootstrap
sh scripts/reference-jellyfin.sh stop
```

With the server running, exercise the client against it deliberately:

```sh
./gradlew :shared:jvmTest --tests '*ReferenceServerSmokeTest*' \
    -PjellyfinRefUrl=http://127.0.0.1:8096
```

The smoke test is an opt-in manual check: it returns immediately unless the reference URL is passed, so CI and normal test runs stay hermetic.

## Android and Android TV

Install a JDK (17 or newer; 21 recommended) and Android SDK Platform 36 with Build Tools 36.0.0. Set `ANDROID_HOME` to your SDK directory or open this project in Android Studio.

```sh
./gradlew :androidApp:assembleDebug
```

Run `androidApp` on a phone, tablet, or Android TV device or emulator. The same APK includes phone and TV launcher entries.

## iOS, iPadOS, and tvOS

Use a Mac with Xcode 26 and a JDK. Open `appleApp/DLX Player.xcodeproj`, then choose the **DLX Player iOS** scheme for an iPhone or iPad, or **DLX Player tvOS** for an Apple TV. The simulator targets use Apple silicon. For a physical device, select your development team in Signing & Capabilities and set a unique bundle identifier if needed. If Xcode cannot find Java, set `JAVA_HOME` to your JDK in the build environment.

Xcode builds the shared Kotlin framework through the Gradle wrapper. The shared Kotlin code lives in `shared/src/commonMain`.
