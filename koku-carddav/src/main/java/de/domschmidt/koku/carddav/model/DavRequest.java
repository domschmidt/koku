package de.domschmidt.koku.carddav.model;

import java.util.List;

public record DavRequest(
        DavMethod method,
        String path,
        String hrefBasePath,
        Integer depth,
        DavPropertyRequestType propertyRequestType,
        List<DavPropertyName> requestedProperties,
        List<String> hrefs,
        DavPropertyName reportName,
        DavTimeRange timeRange,
        String syncToken) {}
