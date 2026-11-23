# Jellyfin client milestones

Roadmap for the multiplatform Jellyfin client in `shared` (consumed by the Android and Apple apps). Each milestone is sized as a series of small TDD increments and verified against the local Jellyfin reference server before it counts as done.

## Working agreements (every milestone)

1. **Red first.** Capture the real response from the reference server with `curl`, turn it into a fixture in `Fixtures.kt`, and write the failing MockEngine test.
2. **Hermetic automated tests.** The suite never talks to a real server. Live verification happens only through the opt-in smoke test:
   `./gradlew :shared:jvmTest --tests '*ReferenceServerSmokeTest*' -PjellyfinRefUrl=http://127.0.0.1:8096`
3. **Gates stay green.** `./gradlew detekt spotlessCheck :shared:jvmTest` passes before every commit; CI enforces it on push.
4. **Small commits** per increment, in the style of the existing history.
5. **One build at a time.** The host is memory-constrained; never run concurrent Gradle builds.
6. **Verify routes before promising them.** Jellyfin 12 changed several routes from 10.x (e.g. first-run flow). Anything listed below marked *verified* was confirmed in the 12.4.0 server binaries; exact request/response shapes still get captured before implementation.

## Status

| Milestone | State |
|---|---|
| M0 — Foundations | **Done** (`aef8cd0`…`fc55021`): toolchain, linters, CI, reference server, `publicSystemInfo()`, `authenticateByName()`, `JellyfinHttpException` |

## M1 — Authenticated session

**Goal:** after login the app holds a usable session and can ask "who am I" from the server.

- Session object carrying `accessToken` + `userId` + device identity
- Authorized request header: `Authorization: MediaBrowser Client="…", Device="…", DeviceId="…", Version="…", Token="…"` *(capture via curl first)*
- `GET /System/Info` — authenticated server info *(verified: system info routes)*
- `GET /Users/Me` — current user *(verified)*
- `POST /Sessions/Logout` — invalidate the token *(verified)*
- 401 during an authenticated call surfaces as "session expired", distinct from a bad login

**Out of scope:** token persistence in platform keystores (integration checkpoint), Quick Connect (M6).

## M2 — Library browsing

**Goal:** list the user's libraries and page through a library's items.

- `GET /Users/{userId}/Views` — libraries *(verified)*
- `GET /Users/{userId}/Items` with `parentId`, `startIndex`, `limit`, `sortBy`, `fields`, `mediaTypes` *(verified)*
- `PagedItems` wrapper (items + `TotalRecordCount`)
- Item model subset: id, name, type, series/production year, runtime, `UserData` (playback position, played, favorite), image tags
- Image URL builder: `GET /Items/{itemId}/Images/{imageType}` with sizing + auth, suitable for platform image loaders that can't set headers *(verified; confirm the token-in-query form on the real server)*

**Out of scope:** search and smart lists (M3), TV season/episode nesting (M3).

## M3 — Continue watching, favorites, next up, search

**Goal:** everything a "home" screen needs.

- `GET /Users/{userId}/Items/Resume` — continue watching *(verified)*
- Next up for shows *(verified: NextUp routes exist; capture exact template)*
- Favorites via `Users/{userId}/Items` filters
- Search via `Users/{userId}/Items` with a search term
- TV structure: seasons and episodes of a show *(verified)*

**Out of scope:** playlists, live TV, SyncPlay.

## M4 — Playback info and stream URLs

**Goal:** for any playable item, decide direct-play vs transcode and build the URL a player can open.

- `POST /Items/{id}/PlaybackInfo` with a minimal `DeviceProfile` → `MediaSources` *(verified)*
- Stream URL builders *(verified):* `Videos/{itemId}/main` (progressive), `Videos/{itemId}/master` + `hls` segments, `Audio/{itemId}/universal`
- `api_key` in query for players that cannot send headers
- Extend `scripts/reference-jellyfin.sh` to seed a small sample media file so the reference server returns real media sources

**Out of scope:** external subtitles and track selection, trickplay images.

## M5 — Playback session reporting

**Goal:** playback positions sync across devices and the server knows what's playing.

- `POST /Sessions/Playing`, `/Sessions/Playing/Progress`, `/Sessions/Playing/Stopped` *(verified)* with `PlayMethod`, `PositionTicks`, item ids
- A small reporter the platform players drive from their playback callbacks

**Out of scope:** SyncPlay group watch.

## M6 — Quick Connect login

**Goal:** log in on a TV by typing a short code on another device — the right login for tvOS/Android TV.

- Initiate a Quick Connect session, poll until authorized, exchange the code for a token *(verified: QuickConnect controller exists; capture exact templates before implementing)*
- Client-side state machine exposing progress so TV UIs can show the code

**Out of scope:** full remote-control of other sessions.

## Integration checkpoints

The client is built first; the platform apps pick it up at natural points:

- **After M1:** login screen on phone/tablet, token stored in the platform keystore
- **After M2:** library grid + item lists (touch and remote navigation)
- **After M3:** home screen with continue watching and next up
- **After M4–M5:** basic player screens with resume support
- **After M6:** TV login without a keyboard

## Later (not scheduled)

External subtitles and track selection, trickplay, offline caching, multi-server management, Live TV, SyncPlay.

## Verified wire-format notes (Jellyfin 12.4.0)

- Responses use **PascalCase** keys; model fields with `@SerialName` and `ignoreUnknownKeys`.
- The `Authorization: MediaBrowser …` header is **required even on pre-login routes**; without it, protected routes answer **404 as if they did not exist**.
- A fresh install auto-creates the admin user `root`; the first-run wizard sets its password.
- Ktor: request bodies need an explicit `Content-Type`; the JVM/Android engine is CIO (Ktor 3.6's OkHttp needs compileSdk 37, above AGP 9.0's cap of 36).
