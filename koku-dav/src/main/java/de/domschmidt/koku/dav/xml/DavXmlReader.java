package de.domschmidt.koku.dav.xml;

import de.domschmidt.koku.dav.DAVConstants;
import de.domschmidt.koku.dav.http.DavHrefResolver;
import de.domschmidt.koku.dav.model.DavMethod;
import de.domschmidt.koku.dav.model.DavPropertyName;
import de.domschmidt.koku.dav.model.DavPropertyRequestType;
import de.domschmidt.koku.dav.model.DavRequest;
import de.domschmidt.koku.dav.model.DavTimeRange;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.SAXException;

@Component
public class DavXmlReader {

    private static final DateTimeFormatter DAV_UTC_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private final DavHrefResolver davHrefResolver;

    public DavXmlReader(final DavHrefResolver davHrefResolver) {
        this.davHrefResolver = davHrefResolver;
    }

    public DavRequest read(final HttpServletRequest request) throws IOException {
        final Document document = readDocument(request);
        return new DavRequest(
                DavMethod.from(request.getMethod()),
                request.getRequestURI(),
                davHrefResolver.resolveHrefBasePath(request),
                parseDepth(request.getHeader(DAVConstants.DAV_DEPTH_HEADER_NAME)),
                readPropertyRequestType(document),
                readRequestedProperties(document),
                readHrefs(document),
                readReportName(document),
                readTimeRange(document),
                readSyncToken(document));
    }

    private Document readDocument(final HttpServletRequest request) throws IOException {
        final PushbackInputStream inputStream = new PushbackInputStream(request.getInputStream());
        final int firstByte = inputStream.read();
        if (firstByte == -1) {
            return null;
        }
        inputStream.unread(firstByte);
        try {
            return secureReader().read(inputStream);
        } catch (final DocumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to parse DAV XML request", e);
        }
    }

    private SAXReader secureReader() {
        final SAXReader reader = new SAXReader();
        setFeature(reader, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setFeature(reader, "http://xml.org/sax/features/external-general-entities", false);
        setFeature(reader, "http://xml.org/sax/features/external-parameter-entities", false);
        setFeature(reader, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        reader.setEntityResolver((publicId, systemId) -> new org.xml.sax.InputSource(new java.io.StringReader("")));
        return reader;
    }

    private void setFeature(final SAXReader reader, final String feature, final boolean value) {
        try {
            reader.setFeature(feature, value);
        } catch (final SAXException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Unable to configure secure XML parser feature " + feature, e);
        }
    }

    private Integer parseDepth(final String depthHeader) {
        return NumberUtils.isCreatable(depthHeader) ? Integer.parseInt(depthHeader) : null;
    }

    private List<DavPropertyName> readRequestedProperties(final Document document) {
        if (document == null || document.getRootElement() == null) {
            return List.of();
        }
        final Element prop =
                document.getRootElement().element(new QName("prop", Namespace.get(DAVConstants.DAV_NAMESPACE)));
        if (prop == null) {
            return List.of();
        }
        final List<DavPropertyName> properties = new ArrayList<>();
        for (final Element element : prop.elements()) {
            properties.add(new DavPropertyName(element.getNamespaceURI(), element.getName()));
        }
        return properties;
    }

    private DavPropertyRequestType readPropertyRequestType(final Document document) {
        if (document == null || document.getRootElement() == null) {
            return DavPropertyRequestType.ALL;
        }
        final Element root = document.getRootElement();
        if (root.element(new QName("propname", Namespace.get(DAVConstants.DAV_NAMESPACE))) != null) {
            return DavPropertyRequestType.NAMES_ONLY;
        }
        if (root.element(new QName("allprop", Namespace.get(DAVConstants.DAV_NAMESPACE))) != null) {
            return DavPropertyRequestType.ALL;
        }
        return root.element(new QName("prop", Namespace.get(DAVConstants.DAV_NAMESPACE))) == null
                ? DavPropertyRequestType.ALL
                : DavPropertyRequestType.NAMED;
    }

    private List<String> readHrefs(final Document document) {
        if (document == null || document.getRootElement() == null) {
            return List.of();
        }
        final List<String> hrefs = new ArrayList<>();
        for (final Element href :
                document.getRootElement().elements(new QName("href", Namespace.get(DAVConstants.DAV_NAMESPACE)))) {
            hrefs.add(href.getStringValue());
        }
        return hrefs;
    }

    private DavPropertyName readReportName(final Document document) {
        if (document == null || document.getRootElement() == null) {
            return null;
        }
        final Element root = document.getRootElement();
        return new DavPropertyName(root.getNamespaceURI(), root.getName());
    }

    private DavTimeRange readTimeRange(final Document document) {
        if (document == null || document.getRootElement() == null) {
            return null;
        }
        final Element timeRange =
                findFirstElement(document.getRootElement(), DAVConstants.CALDAV_NAMESPACE, "time-range");
        if (timeRange == null) {
            return null;
        }
        return new DavTimeRange(
                parseUtcInstant(timeRange.attributeValue("start")), parseUtcInstant(timeRange.attributeValue("end")));
    }

    private String readSyncToken(final Document document) {
        if (document == null || document.getRootElement() == null) {
            return null;
        }
        final Element syncToken = findFirstElement(document.getRootElement(), DAVConstants.DAV_NAMESPACE, "sync-token");
        if (syncToken == null
                || syncToken.getStringValue() == null
                || syncToken.getStringValue().isBlank()) {
            return null;
        }
        return syncToken.getStringValue().trim();
    }

    private Element findFirstElement(final Element element, final String namespace, final String name) {
        if (namespace.equals(element.getNamespaceURI()) && name.equals(element.getName())) {
            return element;
        }
        for (final Iterator<Element> iterator = element.elementIterator(); iterator.hasNext(); ) {
            final Element found = findFirstElement(iterator.next(), namespace, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private Instant parseUtcInstant(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DAV_UTC_DATE_TIME).toInstant(ZoneOffset.UTC);
        } catch (final DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CalDAV time-range instant: " + value, e);
        }
    }
}
