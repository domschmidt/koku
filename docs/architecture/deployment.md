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
| `koku-carddav` | CardDAV-Service |

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

## Enterprise-Hinweise

- Für Produktivbetrieb sollten Umgebungsprofile, Secret-Management und Image-Versionierung dokumentiert werden.
- Healthchecks existieren für die Spring-Services und sollten in Orchestrierung übernommen werden.
- Datenbank-Initialisierung über `db-init` ist für lokale Umgebungen praktisch; produktive Datenbanken sollten kontrolliert provisioniert werden.
- TLS-Material sollte aus Secret Stores oder orchestrierten Secret-Objekten kommen.
- Rollback-Strategien müssen Datenbankmigrationen und Event-Verträge berücksichtigen.

