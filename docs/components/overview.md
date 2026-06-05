# Komponentenüberblick

Diese Dokumentation beschreibt die wiederkehrenden Bausteine der Anwendung. Im Fokus stehen die deklarativen UI-Komponenten, gemeinsame DTOs und Frontend-Registries.

## Komponenten-Dokumente

| Dokument | Beschreibung |
| --- | --- |
| [Deklarative UI-Konzepte](declarative-ui.md) | Gemeinsames Prinzip hinter DTO-getriebener UI |
| [Formulare](forms.md) | Felder, Container, Buttons, Submit, Events und Business-Rule-Anbindung |
| [Listen](lists.md) | Felder, Filter, Actions, Styling, Inline-Content und Routing |
| [Kalender](calendars.md) | Terminquellen, Item-Mapping, FullCalendar-Rendering und Interaktion |
| [Charts](charts.md) | Chart-DTOs, Serien, Achsen, Filter und Einbettung |
| [Dashboards](dashboards.md) | Panels, Grid-Layouts, async Inhalte, Charts und Kalender |
| [Business Rules](business-rules.md) | deklarative UI-Logik, Events, Source-Pfade und Rule-Executor |

## Frontend-Registries

| Registry | Aufgabe | Detaildokument |
| --- | --- | --- |
| `FORMULAR_CONTENT_REGISTRY` | Formularfelder, Container, Layouts und Buttons | [Formulare](forms.md) |
| `LIST_CONTENT_SETUP` | Listenfelder, Actions, Container und Inline-Content | [Listen](lists.md) |
| `CALENDAR_CONTENT_SETUP` | Kalendercontainer, Header, Listen- und Formularintegration | [Kalender](calendars.md) |
| `CHART_FILTER_REGISTRY` | Chart-Filter-Rendering | [Charts](charts.md) |
| `DASHBOARD_CONTENT_REGISTRY` | Dashboard-Panels und Layouts | [Dashboards](dashboards.md) |
| `BUSINESS_RULES_CONTENT_SETUP` | Business-Rule-Editor und Regelbausteine | [Business Rules](business-rules.md) |

## Erweiterungsprinzip

Neue Komponenten folgen immer demselben Muster:

1. Java-DTO mit eindeutigem `@JsonTypeName`.
2. TypeScript-Typgenerierung für das Frontend.
3. Angular-Komponente für Rendering oder Verhalten.
4. Registrierung in der passenden Registry.
5. Nutzung durch Backend-Factory, Controller oder Route.
6. Dokumentation in der passenden Datei unter `docs/components/`.

## Pflege

Bei Codeänderungen an UI-DTOs, Factories, Renderern, Registries oder Plugins sollte [update-components.md](../prompts/update-components.md) verwendet werden.

