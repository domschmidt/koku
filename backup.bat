@echo off
setlocal

set "BACKUP_FILE=%~1"
if "%BACKUP_FILE%"=="" set "BACKUP_FILE=..\koku-postgres-backups\koku-backup.sql"

for %%I in ("%BACKUP_FILE%") do (
  if not exist "%%~dpI" mkdir "%%~dpI"
)

docker compose exec -T koku-db sh -c "pg_dumpall -U $POSTGRES_USER --clean --if-exists -f /tmp/koku-backup.sql"
if errorlevel 1 exit /b 1

for /f "delims=" %%I in ('docker compose ps -q koku-db') do set "PG_CONTAINER=%%I"
if "%PG_CONTAINER%"=="" (
  echo koku-db container not found.
  exit /b 1
)

docker cp "%PG_CONTAINER%:/tmp/koku-backup.sql" "%BACKUP_FILE%"
if errorlevel 1 exit /b 1

docker compose exec -T koku-db rm -f /tmp/koku-backup.sql
echo Backup written to %BACKUP_FILE%
