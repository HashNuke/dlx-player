#!/bin/sh
# Reference Jellyfin server for DLX Player development.
#
# Boots a disposable Jellyfin server in Docker, walks its first-run
# wizard, and seeds sample media (a movie, a three-episode show, a music
# track) so the client can be exercised against a real library.
#
#     sh scripts/reference-jellyfin.sh start   # boot + bootstrap + seed
#     sh scripts/reference-jellyfin.sh reset   # wipe config and re-bootstrap
#     sh scripts/reference-jellyfin.sh stop     # remove the container
#
# The automated test suite NEVER talks to this server: it replays fixtures
# captured from it through Ktor's MockEngine. The server exists for
# capturing fixtures and for manual verification:
#
#     ./gradlew :shared:jvmTest --tests '*ReferenceServerSmokeTest*' \
#         -PjellyfinRefUrl=http://127.0.0.1:8096
#
# Jellyfin 12 notes learned from the real server:
#   - Responses use PascalCase keys (e.g. "ServerName", "StartupWizardCompleted").
#   - Startup routes require an "Authorization: MediaBrowser ..." header even
#     before login; without it they answer 404 as if the route did not exist.
#   - The wizard routes answer 404 for the first few seconds after the port
#     opens (core startup still running); bootstrap gates on the wizard route
#     answering 200/401 before walking it.
#   - On a fresh database the admin user "root" is created lazily (first user
#     store touch); the wizard's password step must wait for that creation to
#     settle, and bootstrap verifies the password by logging in BEFORE
#     /Startup/Complete — a completed wizard with a passwordless root cannot
#     be recovered except by a reset.
#   - A fresh install auto-creates the admin user "root"; the wizard's
#     /Startup/User POST sets that user's password.
#   - Library creation needs name/collectionType/paths as QUERY parameters
#     on POST /Library/VirtualFolders; the body's Name is ignored and a
#     missing name= query fails with 400.
#
# Sample media is generated once with the host's ffmpeg into
# $DATA_DIR/media and mounted read-only at /media; it survives resets.
set -u

NAME=dlx-jellyfin-ref
HOST_PORT=8096
BASE_URL=http://127.0.0.1:$HOST_PORT
DATA_DIR=${JELLYFIN_DATA_DIR:-/tmp/opencode/jellyfin}
MEDIA_DIR="$DATA_DIR/media"
PASSWORD=${JELLYFIN_REF_PASSWORD:-dlx-test-pass}
AUTH='Authorization: MediaBrowser Client="DLX Player", Device="Dev Bootstrap", DeviceId="bootstrap-1", Version="0.1.0"'
TOKEN=''

wait_up() {
  i=1
  while [ $i -le 60 ]; do
    if curl -sf -m 3 "$BASE_URL/System/Info/Public" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
    i=$((i + 1))
  done
  echo "server did not come up within 120s; check: docker logs $NAME" >&2
  return 1
}

login_token() {
  curl -sf -m 5 -X POST "$BASE_URL/Users/AuthenticateByName" \
    -H "Content-Type: application/json" -H "$AUTH" \
    -d "{\"Username\":\"root\",\"Pw\":\"$PASSWORD\"}" 2>/dev/null |
    python3 -c 'import json,sys; print(json.load(sys.stdin)["AccessToken"])' 2>/dev/null
}

wizard_completed() {
  state=$(curl -sf -m 3 "$BASE_URL/System/Info/Public" 2>/dev/null) || return 1
  printf '%s' "$state" |
    python3 -c 'import json,sys; sys.exit(0 if json.load(sys.stdin).get("StartupWizardCompleted") else 1)' 2>/dev/null
}

# The wizard routes themselves answer 404 for the first few seconds after
# the port opens (core startup is still running), so gate on the route
# answering at all: 200 = wizard open, 401 = wizard already completed.
wizard_route_ready() {
  status=$(curl -s -o /dev/null -w '%{http_code}' -m 3 "$BASE_URL/Startup/Configuration" -H "$AUTH")
  [ "$status" = "200" ] || [ "$status" = "401" ]
}

start_container() {
  mkdir -p "$DATA_DIR/config" "$DATA_DIR/cache" \
    "$MEDIA_DIR/movies" "$MEDIA_DIR/shows/Demo Show (2026)/Season 01" "$MEDIA_DIR/music"
  docker run -d --name "$NAME" \
    -p 127.0.0.1:$HOST_PORT:8096 \
    -v "$DATA_DIR/config:/config" \
    -v "$DATA_DIR/cache:/cache" \
    -v "$MEDIA_DIR:/media:ro" \
    jellyfin/jellyfin:latest >/dev/null
}

# Generates the sample media once; safe to re-run.
seed_media() {
  if [ ! -f "$MEDIA_DIR/movies/Sample Movie (2026).mp4" ]; then
    ffmpeg -y -loglevel error \
      -f lavfi -i "testsrc2=duration=60:size=640x360:rate=24" \
      -f lavfi -i "sine=frequency=440:duration=60" \
      -c:v libx264 -preset veryfast -pix_fmt yuv420p -c:a aac -shortest \
      "$MEDIA_DIR/movies/Sample Movie (2026).mp4" || return 1
  fi
  i=1
  while [ $i -le 3 ]; do
    episode="$MEDIA_DIR/shows/Demo Show (2026)/Season 01/Demo Show S01E0$i - Episode $i.mp4"
    if [ ! -f "$episode" ]; then
      ffmpeg -y -loglevel error \
        -f lavfi -i "testsrc2=duration=20:size=640x360:rate=24" \
        -f lavfi -i "sine=frequency=$((300 + i * 100)):duration=20" \
        -c:v libx264 -preset veryfast -pix_fmt yuv420p -c:a aac -shortest \
        "$episode" || return 1
    fi
    i=$((i + 1))
  done
  if [ ! -f "$MEDIA_DIR/music/Demo Track.mp3" ]; then
    ffmpeg -y -loglevel error \
      -f lavfi -i "sine=frequency=330:duration=12" \
      -c:a libmp3lame "$MEDIA_DIR/music/Demo Track.mp3" || return 1
  fi
}

# Creates a library if it does not exist yet (Jellyfin 12 takes
# name/collectionType/paths as query parameters).
ensure_library() {
  if curl -sf -m 5 "$BASE_URL/Library/VirtualFolders" -H "$AUTH, Token=\"$TOKEN\"" 2>/dev/null |
    python3 -c 'import json,sys
names = [v.get("Name") for v in json.load(sys.stdin)]
sys.exit(0 if sys.argv[1] in names else 1)' "$1" 2>/dev/null; then
    return 0
  fi
  curl -sf -m 60 -X POST \
    "$BASE_URL/Library/VirtualFolders?refreshLibrary=true&name=$1&collectionType=$2&paths=$3" \
    -H "$AUTH, Token=\"$TOKEN\"" >/dev/null
}

# Waits until every seeded library exposes at least one item.
library_item_count() {
  parent_id=$(curl -sf -m 5 "$BASE_URL/Users/$2/Views" -H "$AUTH, Token=\"$TOKEN\"" 2>/dev/null |
    python3 -c 'import json,sys
items = json.load(sys.stdin).get("Items", [])
match = [i for i in items if i.get("Name") == sys.argv[1]]
print(match[0]["Id"] if match else "")' "$3" 2>/dev/null)
  if [ -z "$parent_id" ]; then
    echo 0
    return 0
  fi
  curl -sf -m 5 "$BASE_URL/Users/$2/Items?parentId=$parent_id&limit=1" -H "$AUTH, Token=\"$TOKEN\"" 2>/dev/null |
    python3 -c 'import json,sys
try:
    print(json.load(sys.stdin).get("TotalRecordCount", 0))
except Exception:
    print(0)' 2>/dev/null
}

wait_for_scan() {
  user_id=$(curl -sf -m 5 "$BASE_URL/Users/Me" -H "$AUTH, Token=\"$TOKEN\"" |
    python3 -c 'import json,sys; print(json.load(sys.stdin)["Id"])')
  i=1
  while [ $i -le 60 ]; do
    movies=$(library_item_count "$TOKEN" "$user_id" "Movies")
    shows=$(library_item_count "$TOKEN" "$user_id" "Shows")
    music=$(library_item_count "$TOKEN" "$user_id" "Music")
    if [ "${movies:-0}" -ge 1 ] && [ "${shows:-0}" -ge 1 ] && [ "${music:-0}" -ge 1 ]; then
      return 0
    fi
    sleep 2
    i=$((i + 1))
  done
  echo "library scan did not finish within 120s; check: docker logs $NAME" >&2
  return 1
}

# The wizard routes themselves answer 404 for the first few seconds after
# the port opens (core startup is still running), so gate on the route
# answering at all: 200 = wizard open, 401 = wizard already completed.
wizard_route_ready() {
  status=$(curl -s -o /dev/null -w '%{http_code}' -m 3 "$BASE_URL/Startup/Configuration" -H "$AUTH")
  [ "$status" = "200" ] || [ "$status" = "401" ]
}

# Wizard walk + login, retried by bootstrap. Order matters, learned the
# hard way against 12.4.0:
#   - On a fresh database the admin user is created lazily, and touching
#     the user store (GET /Startup/User) triggers that creation. The
#     password step must wait for it to settle or the write is lost.
#   - The password is verified by logging in BEFORE /Startup/Complete:
#     once the wizard closes, a passwordless root is stranded (login is
#     always denied, even with an empty password) and only a reset helps.
# Every step is idempotent, so failures just retry.
bootstrap_core() {
  wizard_route_ready || return 1
  if ! wizard_completed; then
    curl -sf -m 5 -X POST "$BASE_URL/Startup/Configuration" -H "Content-Type: application/json" -H "$AUTH" \
      -d '{"ServerName":"DLX Reference","UICulture":"en-US","MetadataCountryCode":"US","PreferredMetadataLanguage":"en"}' >/dev/null || return 1
    # Touch the user store to trigger the lazy root creation, then wait
    # a moment for it to settle.
    curl -sf -m 3 "$BASE_URL/Startup/User" -H "$AUTH" >/dev/null || return 1
    sleep 3
    # Sets the admin password. 403 = the initial user is already
    # configured (an earlier attempt landed); the login probe below
    # decides success either way.
    curl -sf -m 5 -X POST "$BASE_URL/Startup/User" -H "Content-Type: application/json" -H "$AUTH" \
      -d "{\"Name\":\"root\",\"Password\":\"$PASSWORD\"}" >/dev/null 2>&1 || true
    login_token >/dev/null || return 1
    curl -sf -m 5 -X POST "$BASE_URL/Startup/Complete" -H "$AUTH" >/dev/null || return 1
  fi
  TOKEN=$(login_token) || return 1
  [ -n "$TOKEN" ] || return 1
}

bootstrap() {
  wait_up || return 1
  i=1
  until bootstrap_core; do
    if [ $i -ge 40 ]; then
      echo "first-run bootstrap did not succeed in time; check: docker logs $NAME" >&2
      return 1
    fi
    sleep 2
    i=$((i + 1))
  done
  seed_media || return 1
  ensure_library "Movies" "movies" "/media/movies" || return 1
  ensure_library "Shows" "tvshows" "/media/shows" || return 1
  ensure_library "Music" "music" "/media/music" || return 1
  curl -sf -m 5 -X POST "$BASE_URL/Library/Refresh" -H "$AUTH, Token=\"$TOKEN\"" >/dev/null
  wait_for_scan || return 1
  echo "reference server ready at $BASE_URL (user: root / $PASSWORD; libraries: Movies, Shows, Music)"
}

case "${1:-}" in
  start)
    if docker ps --format '{{.Names}}' | grep -qx "$NAME"; then
      echo "container already running"
      bootstrap
    else
      start_container
      bootstrap
    fi
    ;;
  reset)
    docker rm -f "$NAME" >/dev/null 2>&1
    sudo rm -rf "$DATA_DIR/config" "$DATA_DIR/cache"
    start_container
    bootstrap
    ;;
  stop)
    docker rm -f "$NAME" >/dev/null 2>&1
    echo "stopped"
    ;;
  *)
    echo "usage: sh scripts/reference-jellyfin.sh {start|reset|stop}" >&2
    exit 2
    ;;
esac
