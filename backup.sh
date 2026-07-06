#!/usr/bin/env sh
set -eu

BACKUP_FILE="${1:-../koku-postgres-backups/koku-backup.sql}"
REMOTE_FILE="/tmp/koku-backup.sql"

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

cleanup() {
  docker compose exec -T koku-db rm -f "$REMOTE_FILE" >/dev/null 2>&1 || true
}

command -v docker >/dev/null 2>&1 || fail "docker is not available"

BACKUP_DIR=$(dirname "$BACKUP_FILE")
mkdir -p "$BACKUP_DIR"

PG_CONTAINER=$(docker compose ps -q koku-db)
[ -n "$PG_CONTAINER" ] || fail "koku-db container not found. Start it first with: docker compose up -d koku-db"

trap cleanup EXIT HUP INT TERM

echo "Dumping PostgreSQL cluster with pg_dumpall"
docker compose exec -T koku-db sh -c "pg_dumpall -U \"\$POSTGRES_USER\" --clean --if-exists -f '$REMOTE_FILE'"

docker cp "${PG_CONTAINER}:${REMOTE_FILE}" "$BACKUP_FILE"
[ -s "$BACKUP_FILE" ] || fail "backup file is empty: $BACKUP_FILE"

chmod 600 "$BACKUP_FILE" 2>/dev/null || true
echo "Backup written to $BACKUP_FILE"
