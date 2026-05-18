# Prompt: Architektur-Dokumentation aktualisieren

Bitte aktualisiere `docs/ARCHITECTURE.md` passend zu den aktuellen Codeänderungen.

Arbeite im bestehenden Stil der Datei weiter:

- Deutschsprachig, sachlich und verständlich.
- Markdown mit kurzen Abschnitten, Tabellen und Mermaid-Diagrammen, wo sinnvoll.
- Keine Marketing-Sprache, sondern konkrete Architekturinformationen.
- Bestehende Struktur respektieren und nur erweitern oder ändern, wo die Codeänderungen es rechtfertigen.
- Beispiele sollen zum tatsächlichen Code passen und nicht generisch wirken.

Vorgehen:

1. Prüfe zuerst `git diff`, `git status` und die betroffenen Dateien.
2. Identifiziere, ob sich Architektur, Kommunikation, Module, DTOs, UI-Deklarationen, Deployment, Datenhaltung, Kafka-Topics, REST-Routen, Authentifizierung oder Frontend-Registries geändert haben.
3. Aktualisiere `docs/ARCHITECTURE.md` so, dass sie den aktuellen Stand beschreibt.
4. Wenn neue UI-Typen, DTOs, Factories, Registries oder deklarative Konzepte hinzugekommen sind, ergänze sie im Abschnitt `Deklarative UI-Konzepte` im gleichen Stil:
   - kurze Erklärung
   - Rolle von Backend und Frontend
   - konkretes Codebeispiel aus dem Stil des Projekts
   - typische Bestandteile als Bullet-Liste
5. Wenn Services, Ports, Datenbanken, Kafka-Topics oder Reverse-Proxy-Routen geändert wurden, aktualisiere die Tabellen und Mermaid-Diagramme.
6. Wenn ein vorhandenes Diagramm durch die Änderung falsch oder unvollständig geworden ist, passe es an.
7. Entferne veraltete Aussagen, aber vermeide unnötige Umformulierungen ohne fachlichen Mehrwert.

Wichtig:

- Nutze konkrete Namen aus dem Code, zum Beispiel Services, DTOs, Factories, Registries, Topics und Pfade.
- Keine spekulativen Aussagen. Wenn etwas nicht eindeutig aus dem Code hervorgeht, formuliere vorsichtig oder lasse es weg.
- Halte die Datei konsistent: gleiche Begriffe, gleiche Tabellenformate, gleicher Ton.
- Prüfe am Ende, dass Markdown-Codeblöcke und Mermaid-Blöcke korrekt geöffnet und geschlossen sind.
- Gib abschließend kurz an, welche Architekturabschnitte aktualisiert wurden.

