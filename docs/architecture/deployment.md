# Deployment und Laufzeitumgebung

Das lokale und containerisierte Deployment wird über `docker-compose.yml` beschrieben.

## Container

| Container | Aufgabe |
| --- | --- |
| `koku-frontend` | Baut und hostet Angular mit Nginx |
| `koku-db` | PostgreSQL |
| `db-init` | Erstellt Service-Datenbanken |
| `kafka` | Kafka im KRaft-Modus |
| `init-kafka` | Erstellt Kafka-Topics |
| `idm` | Keycloak |
| `kafka-ui` | UI für Kafka-Analyse |
| `koku-users` | User-Service |
| `koku-customers` | Customer-Service |
| `koku-promotions` | Promotion-Service |
| `koku-activities` | Activity-Service |
| `koku-products` | Product-Service |
| `koku-documents` | Document-Service |
| `koku-files` | File-Service |
| `koku-dav` | CardDAV-/CalDAV-Service |

## Konfigurationsquellen

| Quelle | Inhalt |
| --- | --- |
| `.env` / Umgebungsvariablen | DB, Keycloak, Kafka, Zertifikatspfade |
| `application.yml` | lokale Defaults der Spring-Services |
| `docker-compose.yml` | Container, Ports, Healthchecks, Volumes |
| `authconfig.template.json` | Frontend-Keycloak-Konfiguration zur Laufzeit |
| `bindings/ca-certificates` | CA-Zertifikate für Spring-Services |

## Build- und Artefaktmodell

Das Projekt ist ein Maven-Monorepo mit mehreren Java-Modulen und einem Angular-Frontend. Die Spring-Services werden als Container-Images verwendet. Das Frontend wird per Angular build gebaut und anschließend von Nginx ausgeliefert.

## Frontend Reverse Proxy

Der `koku-frontend`-Container hostet die Angular-SPA und proxyt `/services/...` auf die internen Spring-Services. Der Container läuft als nicht privilegierter `nginx`-User. Deshalb lauscht Nginx im Container auf den unprivilegierten Ports `8080` und `8443`; `docker-compose.yml` veröffentlicht diese weiterhin als Host-Ports `80` und `443`.

| Zugriff | Host-Port | Container-Port |
| --- | --- | --- |
| HTTP Redirect | `80` | `8080` |
| HTTPS / SPA / API Reverse Proxy | `443` | `8443` |

Die TLS-Dateien werden über `FRONTEND_SSL_CERT` und `FRONTEND_SSL_KEY` nach `/etc/ssl/certs/koku.cert.pem` und `/etc/ssl/certs/koku.key.pem` gemountet. Da Nginx nicht als Root läuft, müssen die gemounteten Zertifikats- und Key-Dateien für den Container-User lesbar sein. Das Frontend erzeugt `authconfig.json` beim Containerstart aus `authconfig.template.json`; das HTML-Verzeichnis ist dafür im Image dem `nginx`-User zugeordnet.

## Enterprise-Hinweise

- Für Produktivbetrieb sollten Umgebungsprofile, Secret-Management und Image-Versionierung dokumentiert werden.
- Healthchecks existieren für die Spring-Services und sollten in Orchestrierung übernommen werden.
- Datenbank-Initialisierung über `db-init` ist für lokale Umgebungen praktisch; produktive Datenbanken sollten kontrolliert provisioniert werden.
- TLS-Material sollte aus Secret Stores oder orchestrierten Secret-Objekten kommen und für den nicht privilegierten Nginx-Prozess lesbar bereitgestellt werden.
- Rollback-Strategien müssen Datenbankmigrationen und Event-Verträge berücksichtigen.

