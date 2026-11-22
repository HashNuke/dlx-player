# DLX Player project brief

DLX Player is a media player and Jellyfin client for iOS, iPadOS, tvOS, Android, and Android TV.

The app will connect to Jellyfin servers so users can browse their media libraries and play their content. The phone and tablet apps will support touch input, and the TV apps will support remote navigation.

Kotlin Multiplatform provides a shared module for media models and Jellyfin client logic. Android and Android TV share an Android app, while iOS and iPadOS share an Apple app target. tvOS has its own Apple app target.
