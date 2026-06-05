# Formulare

Formulare sind in Koku deklarative UI-Beschreibungen, die im Backend erzeugt und im Frontend generisch gerendert werden. Ein Formular beschreibt Felder, Container, Layouts, Buttons, Submit-Verhalten, Events und optionale Business Rules.

## Rolle im System

Formulare werden verwendet, wenn fachliche Objekte erstellt, bearbeitet oder als Inline-Inhalt in anderen UI-Typen angezeigt werden. Beispiele sind Nutzer, Produkte, Aktivitäten, Dokumente sowie private und kundenbezogene Termine.

Das Backend liefert eine `FormViewDto`-Struktur. Das Frontend rendert diese Struktur über `FORMULAR_CONTENT_REGISTRY` und die Formular-Renderer.

## Backend-Aufbau

Im Backend werden Formulare typischerweise mit `FormViewFactory` aufgebaut. Die Factory sorgt für konsistente IDs, Container-Struktur und eine einheitliche Ausgabe als DTO.

```java
final FormViewFactory formFactory = new FormViewFactory(
        new DefaultViewContentIdGenerator(),
        GridContainer.builder().cols(1).build());

formFactory.addField(InputFormularField.builder()
        .label("Name")
        .valuePath(KokuProductDto.Fields.name)
        .build());

formFactory.addField(InputFormularField.builder()
        .label("Preis")
        .type(EnumInputFormularFieldType.NUMBER)
        .valuePath(KokuProductDto.Fields.price)
        .build());

formFactory.addButton(KokuFormButton.builder()
        .buttonType(EnumButtonType.SUBMIT)
        .text("Speichern")
        .icon("SAVE")
        .build());

return formFactory.create();
```

## Frontend-Rendering

Das Frontend nutzt `FORMULAR_CONTENT_REGISTRY` aus `koku-frontend/src/app/formular-binding/registry.ts`. Dort werden Formular-DTOs nach `@type` auf Angular-Komponenten gemappt.

Wichtige Renderer und Zustände:

- `FormularComponent` lädt Formular-Definition und Source-Daten.
- `FieldRendererComponent` rendert Felder.
- `ContainerRendererComponent` rendert Container.
- `ButtonRendererComponent` rendert Buttons.
- Formular-Plugins reagieren auf Business Rules, globale Events, Unsaved Changes und Button-Events.

## Datenbindung

Felder binden über `valuePath` an das Source-Objekt des Formulars. Beim Laden werden Werte aus der Source in Feldzustände übernommen. Bei Änderungen schreibt das Frontend den Feldwert zurück in die Source.

```java
InputFormularField.builder()
        .label("Beschreibung")
        .valuePath(KokuUserAppointmentDto.Fields.description)
        .build()
```

Das erlaubt dem Backend, fachliche DTO-Felder direkt mit UI-Feldern zu verbinden, ohne dass im Frontend pro Fachobjekt eigene Formularlogik geschrieben werden muss.

## Aktionen und Events

Buttons können deklarativ Verhalten beschreiben:

- `buttonType`: zum Beispiel `SUBMIT`
- `submitPayload`: zusätzliche Payload-Werte für den Submit
- `postProcessingActions`: zum Beispiel Reload
- `userConfirmation`: Bestätigungsdialog vor Ausführung
- `successEvents` und `failEvents`: Toasts oder globale Events

Beispiel für eine fachliche Aktion:

```java
KokuFormButton.builder()
        .id("DeletePrivateAppointmentButton")
        .buttonType(EnumButtonType.SUBMIT)
        .text("Löschen")
        .submitPayload(KokuUserAppointmentDto.builder().deleted(true).build())
        .userConfirmation(FormUserConfirmationDto.builder()
                .headline("Termin löschen")
                .content("Termin wirklich löschen?")
                .build())
        .build()
```

## Container und Layouts

Formulare bestehen aus Containern. Beispiele:

- `GridContainer` für responsive Spalten.
- `FieldsetContainer` für gruppierte Inhalte.
- `ConditionalContainer` für bedingte Sichtbarkeit.

Conditional Container sind besonders relevant für Workflows wie Löschen/Wiederherstellen, bei denen Buttons abhängig vom Source-Wert angezeigt werden.

## Erweiterung

Neue Formularbestandteile folgen diesem Muster:

1. Java-DTO mit `@JsonTypeName` ergänzen.
2. TypeScript-Typen generieren.
3. Angular-Komponente bauen.
4. In `FORMULAR_CONTENT_REGISTRY` registrieren.
5. In Controller oder Factory verwenden.

## Pflegehinweise

- `@JsonTypeName` und Frontend-Registry-Key müssen zusammenpassen.
- Neue Felder brauchen ein klares Source-Mapping über `valuePath`.
- Button-Events sollten Success- und Failure-Pfade abdecken.
- Generische Formularlogik gehört ins Frontend; fachliche Struktur gehört ins Backend.

