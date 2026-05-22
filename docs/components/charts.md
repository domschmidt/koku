# Charts

Charts visualisieren aggregierte oder historische Fachwerte. Das Backend liefert Chart-DTOs mit Datenreihen, Achsen, Kategorien und Filtern. Das Frontend rendert diese DTOs generisch.

## Rolle im System

Charts werden für Auswertungen wie Umsatzentwicklung, Preisverläufe, Kundenstatistiken oder Aktivitätsdaten genutzt. Sie können eigenständig geroutet, in Listen-Details eingebettet oder in Dashboards angezeigt werden.

## Backend-Aufbau

Chart-Endpunkte liefern konkrete Chart-DTOs, zum Beispiel `LineChartDto` oder `BarChartDto`.

```java
return LineChartDto.builder()
        .title("Preisentwicklung")
        .series(List.of(LineChartSeriesDto.builder()
                .name("Preis")
                .data(priceHistory)
                .build()))
        .axes(AxesDto.builder()
                .x(CategoricalXAxisDto.builder()
                        .categories(monthLabels)
                        .build())
                .y(List.of(YAxisDto.builder()
                        .text("EUR")
                        .build()))
                .build())
        .build();
```

Das Backend ist verantwortlich für:

- fachliche Berechnung der Kennzahlen
- Aggregation und Sortierung
- Auswahl der Chart-Art
- Achsen- und Serienbeschriftung
- optionale Filterdefinitionen

## Frontend-Rendering

Das Frontend nutzt `CHART_CONTENT_SETUP` aus `koku-frontend/src/app/chart-binding/registry.ts`. Die Chart-Komponenten interpretieren das DTO und rendern es konsistent.

Das Frontend kennt dabei nicht die fachliche Berechnungslogik. Es rendert nur die deklarierte Struktur.

## Filter

Chart-DTOs können Filter enthalten. Ein typisches Beispiel sind Monatsfilter für Umsatzstatistiken.

```java
InputChartFilterDto.builder()
        .queryParamName("from")
        .type(EnumInputChartFilterType.MONTH)
        .build()
```

Das Frontend stellt den Filter dar, aktualisiert Query-Parameter oder Request-Parameter und lädt den Chart mit den neuen Werten erneut.

## Einbettung

Charts können in mehreren Kontexten auftauchen:

- als eigene Route
- als Inline-Content in Listen
- als Dashboard-Panel
- als Detailauswertung zu einem Produkt, Kunden oder einer Aktivität

## Erweiterung

Neue Chart-Typen folgen diesem Muster:

1. Chart-DTO und `@JsonTypeName` ergänzen.
2. TypeScript-Typen generieren.
3. Chart-Rendering-Komponente ergänzen.
4. In `CHART_CONTENT_SETUP` registrieren.
5. Backend-Endpunkt liefert neues Chart-DTO.

## Pflegehinweise

- Seriennamen und Achsentitel sollten fachlich verständlich sein.
- Zahlenformate und Währungen sollten konsistent gerendert werden.
- Filter müssen Query-Parameter und Backend-Endpunkte eindeutig verbinden.
- Aggregationslogik gehört ins Backend; Darstellungslogik ins Frontend.

