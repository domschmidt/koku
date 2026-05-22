# Betrieb, Observability und Qualität

Dieses Dokument beschreibt betriebliche und qualitätssichernde Aspekte der Anwendung.

## Health und Runtime-Sichtbarkeit

Die Spring-Services stellen Actuator-Healthchecks bereit, die im Compose-Setup genutzt werden. Kafka UI ist als Hilfswerkzeug für Topic-Inspektion eingebunden.

| Bereich | Aktueller Mechanismus |
| --- | --- |
| Service Health | `/actuator/health` Healthchecks |
| Kafka Analyse | `kafka-ui` |
| Nginx Logs | Access- und Error-Logs im Container |
| Datenbankmigration | Flyway |
| Frontend Build | Angular CLI |

## Qualitätsaspekte

| Aspekt | Beschreibung |
| --- | --- |
| Wartbarkeit | Fachmodule und Shared DTOs trennen Domänenlogik und UI-Verträge |
| Erweiterbarkeit | Neue UI-Typen können über DTOs und Frontend-Registries ergänzt werden |
| Konsistenz | Gemeinsame Factories erzeugen wiederkehrende UI-Strukturen |
| Testbarkeit | Services und DTO-Verträge können modulweise getestet werden |
| Betriebsfähigkeit | Healthchecks und containerisierte Infrastruktur unterstützen lokale und deploybare Laufzeiten |

## CI und Build-Qualität

Die Qualitätssicherung ist in eine CI-Pipeline und eine Release-Pipeline getrennt.

| Pipeline | Datei | Zweck |
| --- | --- | --- |
| CI | `.github/workflows/ci.yml` | Prüft Pull Requests und Pushes mit Backend-Build, Tests, Frontend-Linting und Frontend-Build |
| Release | `.github/workflows/main.yml` | Führt Semantic Release aus und baut Images nur bei neuer Release-Version |
| Dependency Updates | `.github/dependabot.yml` | Aktualisiert Maven-, npm-, Docker- und GitHub-Actions-Abhängigkeiten |

Die CI führt `mvn -B -T 1C verify` aus. Dadurch laufen Java-Compile, Spotless und vorhandene Tests über den Maven-Reactor. Für das Frontend laufen `npm ci`, `npm run lint` und `npm run build`. Pull Requests werden zusätzlich über `actions/dependency-review-action` auf auffällige Dependency-Änderungen geprüft.

## Risiken und Architekturentscheidungen

- Der DTO-getriebene UI-Ansatz ist mächtig, erzeugt aber eine enge Kopplung zwischen Backend-DTOs und Frontend-Renderer.
- Änderungen an `@type`-Namen, DTO-Feldern oder Registries müssen synchron erfolgen.
- Große Frontend-Bundles sollten beobachtet werden, da das Frontend viele generische Renderer und Bibliotheken enthält.
- Kafka-Events sollten kompatibel weiterentwickelt werden, da sie potenziell von mehreren Consumern genutzt werden.

## Enterprise-Hinweise

- Metriken, strukturierte Logs und Tracing sollten für produktive Umgebungen verbindlich ergänzt werden.
- CI sollte dauerhaft um Contract-Tests, Integrationstests und Security-/SBOM-Prüfungen erweitert werden.
- Architekturentscheidungen sollten bei größeren Änderungen als ADRs dokumentiert werden.
- Nichtfunktionale Anforderungen wie Performance, Recovery Time, Backup, Datenschutz und Audit sollten als eigene Betriebsanforderungen gepflegt werden.

