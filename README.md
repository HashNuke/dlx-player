# DLX Player

DLX Player is a Kotlin Multiplatform starter for a media player and Jellyfin client. The app currently opens to a title screen on each platform. Media playback and Jellyfin connectivity are future work. See the [project brief](PROJECT_BRIEF.md).

## Android and Android TV

Install JDK 17 and Android SDK Platform 36 with Build Tools 36.0.0. Set `ANDROID_HOME` to your SDK directory or open this project in Android Studio.

```sh
./gradlew :androidApp:assembleDebug
```

Run `androidApp` on a phone, tablet, or Android TV device or emulator. The same APK includes phone and TV launcher entries.

## iOS, iPadOS, and tvOS

Use a Mac with Xcode 26 and JDK 17. Open `appleApp/DLX Player.xcodeproj`, then choose the **DLX Player iOS** scheme for an iPhone or iPad, or **DLX Player tvOS** for an Apple TV. The simulator targets use Apple silicon. For a physical device, select your development team in Signing & Capabilities and set a unique bundle identifier if needed. If Xcode cannot find Java, set `JAVA_HOME` to your JDK in the build environment.

Xcode builds the shared Kotlin framework through the Gradle wrapper. The shared Kotlin code lives in `shared/src/commonMain`.
