# Keycloak Setup fuer KoKu DAV

KoKu DAV verwendet Basic Auth fuer CardDAV/CalDAV Clients wie iOS. Der DAV-Service validiert diese Basic-Credentials nicht selbst, sondern tauscht sie per Keycloak Direct Access Grant gegen ein Access Token. Aus dem Token wird die Keycloak User-ID (`sub`) gelesen. Diese `sub` ist danach die einzige Identitaet fuer CardDAV/CalDAV Filtering.

## Zielbild

```text
iPhone CardDAV/CalDAV
  -> Basic Auth: Keycloak username + password
KoKu DAV
  -> POST /realms/{realm}/protocol/openid-connect/token
  -> grant_type=password
  -> client_id=koku-dav
  -> client_secret=...
  -> username=...
  -> password=...
Keycloak
  -> access_token mit sub
KoKu DAV
  -> filtert Customer Appointments und private Appointments nach userId == sub
```

## Keycloak Client anlegen

1. In Keycloak den passenden Realm oeffnen.
2. `Clients` -> `Create client`.
3. `Client type`: `OpenID Connect`.
4. `Client ID`: `koku-dav`.
5. `Client authentication`: `On`.
6. `Standard flow`: `Off`.
7. `Direct access grants`: `On`.
8. `Service accounts`: `Off`.
9. Client speichern.
10. Im Tab `Credentials` das Client Secret kopieren.

Der Client ist bewusst confidential. Das iPhone kennt das Client Secret nicht; nur der DAV-Service verwendet es beim Token-Request an Keycloak.

## User vorbereiten

Die DAV-Anmeldedaten sind die normalen Keycloak User-Credentials:

```text
username: Keycloak username oder email
password: Keycloak password
```

Wichtig: Fuer dieses Direct-Access-Grants-Modell darf fuer DAV-User keine interaktive Required Action offen sein, die beim Passwort-Grant nicht geloest werden kann. Beispiele:

- Passwort muss beim naechsten Login geaendert werden
- Terms and Conditions
- Profil vervollstaendigen
- OTP, falls der Direct Grant Flow OTP nicht passend entgegennimmt

Falls MFA fuer normale Browser-Logins Pflicht bleiben soll, muss der Direct Grant Flow fuer DAV so konfiguriert sein, dass DAV-Clients die benoetigten Faktoren nicht interaktiv nachreichen muessen.

## KoKu Konfiguration

In `.env`:

```properties
KEYCLOAK_BASE_URL=https://<keycloak-host>:8443
KEYCLOAK_REALM=<realm>
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}
DAV_KEYCLOAK_TOKEN_URI=${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token
DAV_KEYCLOAK_CLIENT_ID=koku-dav
DAV_KEYCLOAK_CLIENT_SECRET=<client-secret>
```

Im lokalen Docker-Setup ist der aktuelle Default:

```properties
KEYCLOAK_BASE_URL=http://localhost:8443
KEYCLOAK_REALM=master
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}
DAV_KEYCLOAK_TOKEN_URI=${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token
DAV_KEYCLOAK_CLIENT_ID=koku-dav
DAV_KEYCLOAK_CLIENT_SECRET=
```

`DAV_KEYCLOAK_CLIENT_SECRET` muss gesetzt werden, sobald der Keycloak Client confidential ist.

## iOS Account

CardDAV:

```text
Server: https://<host>/services/carddav
Benutzer: <Keycloak username>
Passwort: <Keycloak password>
```

CalDAV:

```text
Server: https://<host>/services/caldav
Benutzer: <Keycloak username>
Passwort: <Keycloak password>
```

Der sichtbare Pfad-User ist nicht sicherheitsrelevant. KoKu DAV ignoriert den URL-User fuer die Berechtigungsentscheidung und verwendet die `sub` aus dem Keycloak Token.

## Sicherheitshinweise

- Direct Access Grants bedeuten: Das Geraet speichert das normale Keycloak Passwort.
- TLS ist zwingend.
- Das Client Secret darf nur serverseitig im DAV-Service liegen.
- Wenn ein User sein Keycloak Passwort aendert, muss das iPhone Passwort aktualisiert werden.
- Wenn ein einzelnes Geraet widerrufen werden soll, geht das in diesem Modell nur indirekt ueber Passwortwechsel oder User-Sperre.

## Referenz

Keycloak dokumentiert Direct Access Grants als Resource Owner Password Credentials Grant. Der Request enthaelt User-Credentials, Client-ID und bei confidential Clients das Client Secret; der Token-Endpoint ist `/realms/{realm-name}/protocol/openid-connect/token`.
