# Architekturüberblick

Koku ist eine modulare Webanwendung für ein Kosmetikstudio-Umfeld. Das System besteht aus einer Angular-Single-Page-App, mehreren fachlich geschnittenen Spring-Boot-Services, Keycloak für Identity und Access Management, PostgreSQL für persistente Daten und Kafka für asynchrone Domain-Events.

Die Anwendung folgt einem DTO-getriebenen Ansatz: Backends liefern nicht nur fachliche Daten, sondern auch deklarative UI-Beschreibungen für Formulare, Listen, Kalender, Charts und Dashboards. Das Frontend rendert diese Beschreibungen über zentrale Registries und Komponenten.

## Zielbild

- Fachliche Domänen sind in eigene Services mit eigener Datenbank getrennt.
- Synchrone Benutzerinteraktion läuft über REST hinter einem Nginx-Reverse-Proxy.
- Asynchrone Kommunikation läuft über kompaktierte Kafka-Topics.
- Authentifizierung erfolgt zentral über Keycloak, Services validieren JWTs als Resource Server.
- Gemeinsame Maven-Module stabilisieren DTOs, UI-Verträge und wiederverwendbare Bausteine.
- Deployment ist containerisiert und lokal über `docker-compose.yml` ausführbar.

## Systemübersicht

```mermaid
flowchart LR
  Browser["Browser / Nutzer"]

  subgraph Frontend["koku-frontend"]
    Angular["Angular SPA"]
    Nginx["Nginx\nTLS, SPA Hosting, Reverse Proxy"]
  end

  Keycloak["Keycloak / idm\nOAuth2 / OIDC"]

  subgraph Services["Spring Boot Services"]
    Users["koku-users\n:8420"]
    Customers["koku-customers\n:8320"]
    Promotions["koku-promotions\n:8520"]
    Activities["koku-activities\n:8620"]
    Products["koku-products\n:9320"]
    Documents["koku-documents\n:8720"]
    Files["koku-files\n:8020"]
    CardDav["koku-carddav\n:8220"]
  end

  subgraph Platform["Plattformdienste"]
    Postgres["PostgreSQL\nseparate DB je Service"]
    Kafka["Kafka\nkompaktierte Domain-Topics"]
    KafkaUI["Kafka UI\n:8181"]
  end

  Browser -->|HTTPS| Nginx
  Nginx --> Angular
  Angular -->|Login / Token| Keycloak
  Angular -->|REST /services/*| Nginx
  Nginx --> Users
  Nginx --> Customers
  Nginx --> Promotions
  Nginx --> Activities
  Nginx --> Products
  Nginx --> Documents
  Nginx --> Files
  Nginx --> CardDav

  Users -->|JWT Validierung| Keycloak
  Customers -->|JWT Validierung| Keycloak
  Promotions -->|JWT Validierung| Keycloak
  Activities -->|JWT Validierung| Keycloak
  Products -->|JWT Validierung| Keycloak
  Documents -->|JWT Validierung| Keycloak
  Files -->|JWT Validierung| Keycloak

  Users --> Postgres
  Customers --> Postgres
  Promotions --> Postgres
  Activities --> Postgres
  Products --> Postgres
  Documents --> Postgres
  Files --> Postgres
  Keycloak --> Postgres

  Users -->|users| Kafka
  Customers -->|customers, customerappointments| Kafka
  Promotions -->|promotions| Kafka
  Activities -->|activities, activitysteps| Kafka
  Products -->|products, productmanufacturers| Kafka
  CardDav -->|Kafka-Konfiguration / Integrationsdaten| Kafka
  KafkaUI --> Kafka
```

## Architekturstil

Koku kombiniert mehrere Muster:

- Modularer Monorepo-Aufbau mit Maven-Aggregator.
- Service-orientierte fachliche Laufzeitmodule.
- Shared-Kernel-DTOs für UI- und API-Verträge.
- Backend-for-Frontend-nahe DTO-Deklarationen für dynamische UI.
- Event-getriebene Veröffentlichung fachlicher Änderungen.
- Containerisierte Infrastruktur für lokale Entwicklung und Deployment.

## Zentrale Module

| Modul | Art | Aufgabe |
| --- | --- | --- |
| `koku-frontend` | Frontend | Angular-SPA, Nginx-Hosting, Reverse Proxy |
| `koku-user` | Service | Nutzer, Regionen, Willkommensdaten, private Termine |
| `koku-customer` | Service | Kunden, Kundentermine, Umsatz- und Termin-Dashboards |
| `koku-product` | Service | Produkte, Hersteller, Preisentwicklung |
| `koku-promotion` | Service | Aktionen und Promotions |
| `koku-activity` | Service | Aktivitäten, Aktivitätsschritte, Preisverläufe |
| `koku-document` | Service | Dokumente und Dokumentvorlagen |
| `koku-file` | Service | Datei-Upload, Dateiabruf und Dateiverwaltung |
| `koku-carddav` | Integrationsservice | CardDAV-kompatible Schnittstellen |
| `koku-dto` | Bibliothek | Gemeinsame Koku-DTOs |
| `formular`, `list`, `calendar`, `chart`, `dashboard` | Bibliotheken | Deklarative UI-Verträge und Factories |
| `business-logic`, `business-exception` | Bibliotheken | Business Rules und fachliche Fehlerdialoge |

