#!/bin/sh
# Reference Jellyfin server for DLX Player development.
#
# Boots a disposable Jellyfin server in Docker and walks its first-run
# wizard so the client can be exercised against a real implementation.
#
#     sh scripts/reference-jellyfin.sh start   # boot + bootstrap
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
#   - A fresh install auto-creates the admin user "root"; the wizard's
#     /Startup/User POST sets that user's password.
set -u

NAME=dlx-jellyfin-ref
HOST_PORT=8096
BASE_URL=http://127.0.0.1:$HOST_PORT
DATA_DIR=${JELLYFIN_DATA_DIR:-/tmp/opencode/jellyfin}
PASSWORD=${JELLYFIN_REF_PASSWORD:-dlx-test-pass}
AUTH='Authorization: MediaBrowser Client="DLX Player", Device="Dev Bootstrap", DeviceId="bootstrap-1", Version="0.1.0"'

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

start_container() {
  mkdir -p "$DATA_DIR/config" "$DATA_DIR/cache"
  docker run -d --name "$NAME" \
    -p 127.0.0.1:$HOST_PORT:8096 \
    -v "$DATA_DIR/config:/config" \
    -v "$DATA_DIR/cache:/cache" \
    jellyfin/jellyfin:latest >/dev/null
}

bootstrap() {
  wait_up
  # Walk the wizard in order: configuration, then set the root user's
  # password, then complete. Each step needs the MediaBrowser header.
  curl -sf -m 5 -X POST "$BASE_URL/Startup/Configuration" -H "Content-Type: application/json" -H "$AUTH" \
    -d '{"ServerName":"DLX Reference","UICulture":"en-US","MetadataCountryCode":"US","PreferredMetadataLanguage":"en"}' >/dev/null
  curl -sf -m 5 -X POST "$BASE_URL/Startup/User" -H "Content-Type: application/json" -H "$AUTH" \
    -d "{\"Name\":\"root\",\"Password\":\"$PASSWORD\"}" >/dev/null
  curl -sf -m 5 -X POST "$BASE_URL/Startup/Complete" -H "$AUTH" >/dev/null
  echo "reference server ready at $BASE_URL (user: root / $PASSWORD)"
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
