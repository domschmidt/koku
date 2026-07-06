#!/usr/bin/env sh
set -eu

REMOTE_FILE="/tmp/koku-postgres-restore.sql"
VERIFY_DATABASES="${KOKU_POSTGRES_DATABASES:-users promotions activities customers products documents files keycloak}"

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

cleanup() {
  docker compose exec -T koku-db rm -f "$REMOTE_FILE" >/dev/null 2>&1 || true
}

if [ "$#" -ne 1 ]; then
  echo "Usage: sh restore.sh path/to/pg_dumpall-backup.sql" >&2
  exit 2
fi

BACKUP_FILE="$1"
[ -f "$BACKUP_FILE" ] || fail "backup file not found: $BACKUP_FILE"
[ -s "$BACKUP_FILE" ] || fail "backup file is empty: $BACKUP_FILE"

command -v docker >/dev/null 2>&1 || fail "docker is not available"

PG_CONTAINER=$(docker compose ps -q koku-db)
[ -n "$PG_CONTAINER" ] || fail "koku-db container not found. Start it first with: docker compose up -d koku-db"

trap cleanup EXIT HUP INT TERM

echo "Copying backup into koku-db"
docker cp "$BACKUP_FILE" "${PG_CONTAINER}:${REMOTE_FILE}"

echo "Restoring pg_dumpall backup into PostgreSQL with psql"
docker compose exec -T koku-db sh -c "psql -X -U \"\$POSTGRES_USER\" -d postgres -f '$REMOTE_FILE'"

for db in $VERIFY_DATABASES; do
  case "$db" in
    "" | *[!A-Za-z0-9_]*)
      fail "invalid database name for verification: $db"
      ;;
  esac

  DB_EXISTS=$(docker compose exec -T koku-db sh -c "psql -X -U \"\$POSTGRES_USER\" -d postgres -tAc \"SELECT 1 FROM pg_database WHERE datname = '$db'\"")
  DB_EXISTS=$(printf '%s' "$DB_EXISTS" | tr -d '[:space:]')
  [ "$DB_EXISTS" = "1" ] || fail "restore verification failed; database is missing: $db"
done

echo "Restore completed from $BACKUP_FILE"
