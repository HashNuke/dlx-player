# DLX Player project brief

DLX Player is a media player and Jellyfin client for iOS, iPadOS, tvOS, Android, and Android TV.

The app will connect to Jellyfin servers so users can browse their media libraries and play their content. The phone and tablet apps will support touch input, and the TV apps will support remote navigation.

Kotlin Multiplatform provides a shared module for media models and Jellyfin client logic. Android and Android TV share an Android app, while iOS and iPadOS share an Apple app target. tvOS has its own Apple app target.

## iOS and iPadOS milestones

Library is the home screen. Its first row lists sources, with This iPhone or This iPad first and remembered Jellyfin servers following. Opening a device shows folders and files. Opening a server shows the libraries available to the signed-in user. Selecting media from either source opens the same player.

Plan each milestone when it becomes active. Record the research question, evidence, implementation checkpoints, and acceptance scenario before changing runtime behavior. Carry findings from device testing into the next plan.

| Order | Milestone | Research focus | Acceptance scenario |
| --- | --- | --- | --- |
| 1 | Local library and first playback | Files access, persistence, and player lifecycle | Import a video, relaunch, find it in the device source, play, pause, seek, and return to the same folder. |
| 2 | Native format support | Container, video, and audio combinations on supported devices | Open representative OS-supported media and recover from an unplayable file without leaving the library. |
| 3 | Subtitles | Embedded track selection, sidecar formats, and timing after seeking | Choose a language, turn captions off, and play a local video with an external subtitle file. |
| 4 | Jellyfin library browsing | Session persistence, server identity, pagination, and offline behavior | Connect a server, relaunch, browse its libraries from the source row, and reconnect after an outage. |
| 5 | Jellyfin playback | Direct play, server-provided alternatives, subtitles, and progress reporting | Play a browsed item in the existing player and verify that Jellyfin receives playback progress. |

### Milestone 1 plan

Use an app-managed media folder for the first local library. Copying an imported file costs device storage, but makes it available on relaunch even if the original moves. SwiftUI's file importer supplies a security-scoped URL. Acquire access for the copy and release it when finished. [Apple file importer documentation](https://developer.apple.com/documentation/swiftui/view/fileimporter%28ispresented%3Aallowedcontenttypes%3Aoncompletion%3A%29)

Use AVKit for the first playback screen. AVFoundation accepts a local URL, so the library can pass the selected file directly to the player. [Apple AVURLAsset documentation](https://developer.apple.com/documentation/avfoundation/avurlasset) and [VideoPlayer documentation](https://developer.apple.com/documentation/avkit/videoplayer)

1. Open Library on launch. Test that the device source appears first and opens its root. Support navigating into a folder and back.
2. Add import from Files into the current folder. Test relaunch persistence, same-name imports preserving existing files, and failed copies leaving no partial entry.
3. Open a selected file in the native player. Test the app's selection and dismissal behavior. Exercise the full acceptance scenario on an iPhone with a known-playable video and check the iPad layout.

Start each behavior checkpoint with its focused failing test. Run the project completion checks and verify the affected Apple target before committing the checkpoint.
