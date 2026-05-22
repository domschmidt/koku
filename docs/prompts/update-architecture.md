# Prompt: Architektur-Dokumentation aktualisieren

Bitte aktualisiere die Architektur-Dokumentation passend zu den aktuellen Codeänderungen.

Die Architektur-Dokumentation ist in mehrere Dateien aufgeteilt:

- `docs/ARCHITECTURE.md` ist nur der Einstiegspunkt und Index.
- `docs/architecture/overview.md` beschreibt Zielbild, Architekturstil und zentrale Module.
- `docs/architecture/system-context.md` beschreibt Systemkontext, Request-Flows, Reverse-Proxy-Routen und Kommunikation.
- `docs/architecture/data-and-events.md` beschreibt Datenhaltung, Datenbanken, Kafka-Topics, Event-Flüsse und Konsistenzmodell.
- `docs/architecture/security.md` beschreibt Identity, Authentifizierung, JWT-Validierung, Zertifikate und Security-Hinweise.
- `docs/architecture/deployment.md` beschreibt Container, Konfiguration, Build- und Deployment-Modell.
- `docs/architecture/operations-and-quality.md` beschreibt Betrieb, Observability, Qualität, Risiken und Enterprise-Hinweise.

Arbeite im bestehenden Stil weiter:

- Deutschsprachig, sachlich und verständlich.
- Markdown mit kurzen Abschnitten, Tabellen und Mermaid-Diagrammen, wo sinnvoll.
- Keine Marketing-Sprache, sondern konkrete Architekturinformationen.
- Bestehende Struktur respektieren und nur die betroffenen Dateien ändern.
- Beispiele und Aussagen sollen zum tatsächlichen Code passen.

Vorgehen:

1. Prüfe zuerst `git status`, `git diff` und die betroffenen Dateien.
2. Identifiziere, ob sich Architektur, Kommunikation, Module, DTOs, Deployment, Datenhaltung, Kafka-Topics, REST-Routen, Security, Observability oder Betriebsverhalten geändert haben.
3. Aktualisiere die passende Datei unter `docs/architecture/`.
4. Aktualisiere `docs/ARCHITECTURE.md` nur, wenn neue Dokumente hinzukommen oder Links geändert werden.
5. Wenn Services, Ports, Datenbanken, Kafka-Topics oder Reverse-Proxy-Routen geändert wurden, aktualisiere Tabellen und Mermaid-Diagramme.
6. Wenn ein vorhandenes Diagramm durch die Änderung falsch oder unvollständig geworden ist, passe es an.
7. Entferne veraltete Aussagen, aber vermeide unnötige Umformulierungen ohne fachlichen Mehrwert.

Enterprise-Checkliste:

- Systemkontext und Kommunikationswege korrekt?
- Service-, Modul- und Port-Übersichten aktuell?
- Datenbanken, Migrationen und Persistenzmodell aktuell?
- Kafka-Topics, Producer und Konsistenzmodell aktuell?
- Authentifizierung, Zertifikate, Secrets und Autorisierung korrekt beschrieben?
- Deployment, Konfiguration, Healthchecks und Laufzeitumgebung aktuell?
- Betriebs-, Qualitäts-, Risiko- und Observability-Hinweise aktuell?

Wichtig:

- Nutze konkrete Namen aus dem Code, zum Beispiel Services, DTOs, Factories, Registries, Topics, Pfade und Properties.
- Keine spekulativen Aussagen. Wenn etwas nicht eindeutig aus dem Code hervorgeht, formuliere vorsichtig oder lasse es weg.
- Halte Begriffe, Tabellenformate und Ton konsistent.
- Prüfe am Ende, dass Markdown-Codeblöcke und Mermaid-Blöcke korrekt geöffnet und geschlossen sind.
- Gib abschließend kurz an, welche Architekturdateien aktualisiert wurden.

