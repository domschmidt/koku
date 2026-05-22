# Business Rules

Business Rules ermöglichen deklaratives Verhalten in Formularen, Kalendern und anderen UI-Kontexten. Regeln reagieren auf Feldwerte, Events oder Payloads und ändern UI-Zustände oder Werte.

## Rolle im System

Business Rules kapseln dynamische UI-Logik, die nicht fest in einzelne Angular-Komponenten eingebaut werden soll. Sie erlauben, Feldabhängigkeiten und Reaktionen deklarativ zu beschreiben.

## Frontend-Integration

Das Frontend bindet Business Rules über Plugins und `BUSINESS_RULES_CONTENT_SETUP` ein. Relevante Logik liegt unter anderem in:

- `BusinessRuleExecutor`
- `BusinessRulePlugin`
- `business-rules-binding/registry.ts`
- Formular- und Kalender-Plugins in `app.component.ts`

## Typische Einsatzfälle

- Feldwerte anhand anderer Feldwerte setzen.
- Sichtbarkeit oder Zustand von UI-Bausteinen beeinflussen.
- Events aus Formularen oder globalen Bussen verarbeiten.
- Payloads in Felder oder Source-Objekte übernehmen.

## Event- und Source-Prinzip

Regeln arbeiten häufig mit Pfaden in Source-Objekten oder Event-Payloads. Dadurch können sie generisch auf DTO-Strukturen zugreifen.

```java
FormViewEventPayloadSourceUpdateGlobalEventListenerDto.builder()
        .eventName("user-appointment-updated")
        .idPath(KokuUserAppointmentDto.Fields.id)
        .build()
```

Das Frontend vergleicht das `idPath` im Event-Payload mit der aktuellen Formular-Source und aktualisiert die Source, wenn beide zusammengehören.

## Verhältnis zu Formularen

Business Rules ergänzen Formulare, ersetzen aber nicht deren Struktur. Die Formular-DTOs beschreiben Felder und Aktionen; Business Rules beschreiben dynamische Reaktionen.

## Erweiterung

Neue Regelbausteine folgen diesem Muster:

1. DTO für Regel, Reference, Execution oder Listener ergänzen.
2. TypeScript-Typen generieren.
3. Executor- oder Plugin-Logik ergänzen.
4. UI-Editor-Komponente in `BUSINESS_RULES_CONTENT_SETUP` registrieren, falls die Regel editierbar sein soll.
5. Backend oder UI-Konfiguration verwendet den neuen Regelbaustein.

## Pflegehinweise

- Business Rules sollten nachvollziehbar und klein bleiben.
- Pfadbasierte Zugriffe müssen robust gegen fehlende Werte sein.
- Neue Rule-Typen brauchen klare Fehlerpfade bei unbekannten `@type`s.
- Bei fachlich kritischer Logik prüfen, ob sie wirklich ins UI gehört oder in den Backend-Service.

