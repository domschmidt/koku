# Prompt: Komponenten-Dokumentation aktualisieren

Bitte aktualisiere die Komponenten-Dokumentation passend zu den aktuellen Codeänderungen.

Die Komponenten-Dokumentation ist aufgeteilt in:

- `docs/components/overview.md` für den Überblick über wiederkehrende Bausteine, Registries und Erweiterungsprinzipien.
- `docs/components/declarative-ui.md` für das gemeinsame DTO-getriebene UI-Prinzip.
- `docs/components/forms.md` für Formulare.
- `docs/components/lists.md` für Listen.
- `docs/components/calendars.md` für Kalender.
- `docs/components/charts.md` für Charts.
- `docs/components/dashboards.md` für Dashboards.
- `docs/components/business-rules.md` für Business Rules und deklarative UI-Logik.

Arbeite im bestehenden Stil weiter:

- Deutschsprachig, sachlich und verständlich.
- Konkrete Komponentennamen, DTOs, Factories und Registries aus dem Code verwenden.
- Pro UI-Typ eine eigene Beschreibung pflegen.
- Beispiele sollen wie echter Projektcode aussehen und zum aktuellen Stand passen.
- Keine unnötige Theorie; Fokus auf wie Koku es konkret macht.

Vorgehen:

1. Prüfe `git status`, `git diff` und die betroffenen Frontend-/Backend-Dateien.
2. Suche nach neuen oder geänderten DTOs, `@JsonTypeName`, Factories, Angular-Komponenten und `*_CONTENT_SETUP`-Registries.
3. Aktualisiere `docs/components/overview.md`, wenn neue Komponentenarten, Registries oder Erweiterungsmuster entstehen.
4. Aktualisiere `docs/components/declarative-ui.md`, wenn sich das gemeinsame deklarative Prinzip, Trade-offs oder übergreifende Konzepte ändern.
5. Aktualisiere die passende Detaildatei:
   - Formularänderungen: `docs/components/forms.md`
   - Listenänderungen: `docs/components/lists.md`
   - Kalenderänderungen: `docs/components/calendars.md`
   - Chartänderungen: `docs/components/charts.md`
   - Dashboardänderungen: `docs/components/dashboards.md`
   - Business-Rule-Änderungen: `docs/components/business-rules.md`
6. Ergänze für neue UI-Typen oder größere neue Bausteine jeweils:
   - kurze Erklärung
   - Rolle des Backends
   - Rolle des Frontends
   - konkretes Codebeispiel
   - typische Bestandteile als Bullet-Liste
7. Entferne oder korrigiere veraltete Beispiele.

Checkliste für deklarative UI:

- `@type` / `@JsonTypeName` korrekt benannt?
- Java-DTO und TypeScript-Typ passen zusammen?
- Registry-Eintrag im Frontend dokumentiert?
- Renderer-, Binding- oder Plugin-Verhalten beschrieben?
- Events, Actions, Filter, Source-Mapping und globale Listener erwähnt, falls relevant?
- Beispielcode kompakt und plausibel?
- Hauptindex `docs/ARCHITECTURE.md` aktualisiert, falls neue Komponentendateien hinzukommen?

Wichtig:

- Keine spekulativen Komponenten beschreiben.
- Keine generischen Framework-Erklärungen, wenn sie im Projekt nicht konkret sichtbar sind.
- Markdown-Codeblöcke sauber schließen.
- Abschließend kurz nennen, welche Komponenten-Dokumente aktualisiert wurden.
