package de.domschmidt.koku.dav.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DavResponseBuilder {

    private final String href;
    private final Map<DavPropertyName, DavPropertyValue> supportedProperties = new LinkedHashMap<>();

    public DavResponseBuilder(final String href) {
        this.href = href;
    }

    public DavResponseBuilder property(final DavPropertyName name, final DavPropertyValue value) {
        supportedProperties.put(name, value);
        return this;
    }

    public DavResponse build(final List<DavPropertyName> requestedProperties) {
        return build(DavPropertyRequestType.NAMED, requestedProperties);
    }

    public DavResponse build(
            final DavPropertyRequestType requestType, final List<DavPropertyName> requestedProperties) {
        if (requestType == DavPropertyRequestType.NAMES_ONLY) {
            return new DavResponse(
                    href,
                    List.of(new DavPropStat(
                            200,
                            supportedProperties.keySet().stream()
                                    .map(propertyName -> new DavProperty(propertyName, new EmptyValue()))
                                    .toList())));
        }
        final List<DavPropertyName> requested = requestedProperties == null || requestedProperties.isEmpty()
                ? List.copyOf(supportedProperties.keySet())
                : requestedProperties;
        final List<DavProperty> okProperties = new ArrayList<>();
        final List<DavProperty> notFoundProperties = new ArrayList<>();

        for (final DavPropertyName propertyName : requested) {
            final DavPropertyValue value = supportedProperties.get(propertyName);
            if (value == null) {
                notFoundProperties.add(new DavProperty(propertyName, new EmptyValue()));
            } else {
                okProperties.add(new DavProperty(propertyName, value));
            }
        }

        final List<DavPropStat> propStats = new ArrayList<>();
        if (!okProperties.isEmpty()) {
            propStats.add(new DavPropStat(200, okProperties));
        }
        if (!notFoundProperties.isEmpty()) {
            propStats.add(new DavPropStat(404, notFoundProperties));
        }
        return new DavResponse(href, propStats);
    }
}
