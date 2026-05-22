# Listen

Listen sind deklarative Ansichten für Mengen fachlicher Objekte. Das Backend beschreibt Felder, Filter, Aktionen, Styling, Inline-Inhalte und Routing. Das Frontend rendert daraus eine interaktive Listenansicht.

## Rolle im System

Listen sind die primäre Navigation in fachlichen Beständen wie Kunden, Produkten, Aktivitäten, Dokumenten, Nutzern und Terminen. Sie können einfache Datenübersichten sein oder als Ausgangspunkt für Detailansichten, Inline-Formulare und Charts dienen.

## Backend-Aufbau

Listen werden mit `ListViewFactory` erstellt. Die Factory erhält das ID-Feld des Listenelements und erzeugt daraus eine `ListViewDto`-Beschreibung.

```java
final ListViewFactory listViewFactory =
        new ListViewFactory(new DefaultListViewContentIdGenerator(), KokuUserAppointmentDto.Fields.id);

final ListViewFieldReference startDateRef = listViewFactory.addField(
        ListViewInputFieldDto.builder()
                .valuePath(KokuUserAppointmentDto.Fields.startDate)
                .type(ListViewInputFieldTypeEnumDto.DATE)
                .build());

final ListViewFieldReference descriptionRef = listViewFactory.addField(
        ListViewInputFieldDto.builder()
                .valuePath(KokuUserAppointmentDto.Fields.description)
                .build());

return listViewFactory.create();
```

## Frontend-Rendering

Das Frontend nutzt `LIST_CONTENT_SETUP` aus `koku-frontend/src/app/list-binding/registry.ts`. Die Registry verbindet DTO-`@type`s mit Komponenten für Felder, Container, Actions und Inline-Content.

Wichtige Aufgaben:

- Listenquelle laden.
- Item-Zustände verwalten.
- Felder nach Definition rendern.
- Filter und Actions ausführen.
- Inline-Content in Listenitems einbetten.

## Felder und Source-Mapping

Listenfelder lesen Werte über `valuePath` aus jedem Item. Das trennt die visuelle Darstellung von der konkreten Backend-Implementierung.

```java
ListViewInputFieldDto.builder()
        .valuePath(KokuCustomerDto.Fields.fullName)
        .build()
```

Feldreferenzen können anschließend in Containern, Actions oder Parameter-Mappings verwendet werden.

## Aktionen

Listenaktionen können deklarativ beschreiben, was bei einem Item passieren soll:

- Route öffnen.
- Inline-Formular öffnen.
- HTTP-Request ausführen.
- Globales Event propagieren.
- Confirmation anzeigen.

Dadurch kann ein Backend zum Beispiel eine Wiederherstellen-Aktion anbieten, ohne dass das Frontend eine fachliche Sonderkomponente braucht.

## Conditional Styling

Items können abhängig von Werten anders dargestellt werden.

```java
listViewFactory.addItemStyling(ListViewConditionalItemValueStylingDto.builder()
        .compareValuePath(KokuUserAppointmentDto.Fields.deleted)
        .expectedValue(Boolean.TRUE)
        .positiveStyling(ListViewItemStylingDto.builder()
                .lineThrough(true)
                .opacity((short) 50)
                .build())
        .build());
```

Das Frontend wertet die Bedingung pro Item aus und wendet das deklarierte Styling an.

## Inline-Content

Listen können weitere UI-Typen einbetten:

- Formulare für Bearbeitung.
- Charts für historische Werte.
- Listen für Unterobjekte.
- Kalender- oder Dashboard-Inhalte.

Dadurch entstehen wiederverwendbare Detailbereiche, ohne dass jede Liste eine eigene Angular-Seite braucht.

## Erweiterung

Neue Listenbestandteile folgen diesem Muster:

1. DTO im `list`- oder `koku-dto`-Umfeld ergänzen.
2. TypeScript-Typen generieren.
3. Angular-Komponente und Registry-Eintrag in `LIST_CONTENT_SETUP` ergänzen.
4. Controller/Factory auf das neue DTO umstellen.

## Pflegehinweise

- Item-ID-Felder müssen stabil sein, da sie Routing und Updates stützen.
- `valuePath` sollte auf DTO-Felder verweisen, nicht auf zufällige JSON-Strukturen.
- Actions brauchen klare Success-/Fail-Pfade, wenn sie HTTP-Calls auslösen.
- Conditional Styling sollte fachlich nachvollziehbar und nicht als versteckte Business Rule missbraucht werden.

