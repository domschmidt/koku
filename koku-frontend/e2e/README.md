# Playwright E2E Tests

These tests run against the real Koku frontend, Keycloak and backend services. They intentionally do not mock authentication or API calls.

Structure:

- `fixtures/` wires Playwright fixtures to reusable Koku page objects.
- `pages/` contains one page object per concrete application page and keeps selectors out of specs.
- `support/` contains cross-cutting helpers such as credential loading.

Required environment variables:

- `KOKU_E2E_USERNAME`
- `KOKU_E2E_PASSWORD`

Optional environment variables:

- `KOKU_E2E_BASE_URL`, defaults to `https://127.0.0.1:4200`
- `KOKU_E2E_SEED=true` seeds a small baseline through the real APIs before the tests
- `KOKU_E2E_KEYCLOAK_URL`, required when baseline seeding is enabled

Example local setup:

```powershell
$env:KOKU_E2E_BASE_URL = "https://192.168.178.36/"
$env:KOKU_E2E_USERNAME = "admin"
$env:KOKU_E2E_PASSWORD = "admin"
npm run e2e
```

## Ephemeral local stack

The CI-like stack uses `compose.e2e.yml` from the repository root. It creates fresh PostgreSQL databases, Kafka topics,
a Keycloak realm and all Koku services without using the developer `.env` or local TLS files.

Build the backend images and generated frontend DTO types first:

```powershell
mvn.cmd -B install -Dmaven.test.skip=true -Pjib-dockerBuild
docker compose -f compose.e2e.yml build koku-frontend
docker compose -f compose.e2e.yml up -d --wait --wait-timeout 240
```

Run the suite against that stack:

```powershell
$env:CI = "true"
$env:KOKU_E2E_BASE_URL = "http://127.0.0.1:4200"
$env:KOKU_E2E_KEYCLOAK_URL = "http://host.docker.internal:8081"
$env:KOKU_E2E_USERNAME = "e2e-admin"
$env:KOKU_E2E_PASSWORD = "e2e-admin"
$env:KOKU_E2E_SEED = "true"
npm run e2e:ci
```

Remove the complete environment, including its data, after the run:

```powershell
docker compose -f compose.e2e.yml down -v --remove-orphans
```

Run:

```sh
npm run playwright:install
npm run e2e
```
