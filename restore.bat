@echo off
setlocal

set "BACKUP_FILE=%~1"
if "%BACKUP_FILE%"=="" (
  echo Usage: restore.bat path\to\pg_dumpall-backup.sql
  exit /b 2
)

if not exist "%BACKUP_FILE%" (
  echo Backup file not found: %BACKUP_FILE%
  exit /b 2
)

for /f "delims=" %%I in ('docker compose ps -q koku-db') do set "PG_CONTAINER=%%I"
if "%PG_CONTAINER%"=="" (
  echo koku-db container not found.
  exit /b 1
)

docker cp "%BACKUP_FILE%" "%PG_CONTAINER%:/tmp/koku-postgres-restore.sql"
if errorlevel 1 exit /b 1

echo Restoring pg_dumpall backup into PostgreSQL with psql
docker compose exec -T koku-db sh -c "psql -X -U $POSTGRES_USER -d postgres -f /tmp/koku-postgres-restore.sql"
if errorlevel 1 exit /b 1

docker compose exec -T koku-db rm -f /tmp/koku-postgres-restore.sql
echo Restore completed from %BACKUP_FILE%
