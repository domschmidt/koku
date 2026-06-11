package de.domschmidt.koku.dav.xml;

import de.domschmidt.koku.dav.DAVConstants;
import de.domschmidt.koku.dav.model.*;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.stereotype.Component;

@Component
public class DavXmlWriter {

    private static final Namespace DAV = Namespace.get("d", DAVConstants.DAV_NAMESPACE);
    private static final Namespace CARD = Namespace.get("card", DAVConstants.CARDDAV_NAMESPACE);
    private static final Namespace CAL = Namespace.get("cal", DAVConstants.CALDAV_NAMESPACE);
    private static final Namespace CALENDAR_SERVER = Namespace.get("cs", DAVConstants.CALENDAR_SERVER_NAMESPACE);
    private static final Map<String, String> STANDARD_PREFIXES = Map.of(
            DAVConstants.DAV_NAMESPACE, "d",
            DAVConstants.CARDDAV_NAMESPACE, "card",
            DAVConstants.CALDAV_NAMESPACE, "cal",
            DAVConstants.CALENDAR_SERVER_NAMESPACE, "cs");

    public String write(final DavMultiStatus multiStatus) throws IOException {
        final Document document = DocumentHelper.createDocument();
        final Element root = document.addElement(qName("multistatus", DAV));
        namespaces(multiStatus).forEach((namespaceUri, prefix) -> root.addNamespace(prefix, namespaceUri));
        for (final DavResponse response : multiStatus.responses()) {
            appendResponse(root, response);
        }
        if (multiStatus.syncToken() != null && !multiStatus.syncToken().isBlank()) {
            addDav(root, "sync-token").setText(multiStatus.syncToken());
        }
        return toXml(document);
    }

    private void appendResponse(final Element root, final DavResponse davResponse) {
        final Element response = addDav(root, "response");
        addDav(response, "href").setText(davResponse.href());
        if (davResponse.status() != null) {
            addDav(response, "status")
                    .setText("HTTP/1.1 " + davResponse.status() + " " + reasonPhrase(davResponse.status()));
            return;
        }
        for (final DavPropStat propStat : davResponse.propStats()) {
            appendPropStat(response, propStat);
        }
    }

    private void appendPropStat(final Element response, final DavPropStat davPropStat) {
        final Element propStat = addDav(response, "propstat");
        final Element prop = addDav(propStat, "prop");
        for (final DavProperty property : davPropStat.properties()) {
            appendProperty(prop, property);
        }
        addDav(propStat, "status")
                .setText("HTTP/1.1 " + davPropStat.status() + " " + reasonPhrase(davPropStat.status()));
    }

    private void appendProperty(final Element prop, final DavProperty property) {
        final Element element = prop.addElement(qName(property.name()));
        switch (property.value()) {
            case EmptyValue ignored -> {}
            case CalendarDataValue calendarDataValue -> element.setText(calendarDataValue.calendarData());
            case CollationSetValue collationSetValue -> appendCollationSet(property.name(), element, collationSetValue);
            case HrefValue hrefValue -> addDav(element, "href").setText(hrefValue.href());
            case PrivilegeSetValue privilegeSetValue -> appendPrivilegeSet(element, privilegeSetValue);
            case ResourceTypeValue resourceTypeValue ->
                resourceTypeValue.resourceTypes().forEach(resourceType -> element.addElement(qName(resourceType)));
            case SupportedAddressDataValue supportedAddressDataValue ->
                appendSupportedAddressData(element, supportedAddressDataValue);
            case SupportedCalendarComponentSetValue supportedCalendarComponentSetValue ->
                appendSupportedCalendarComponentSet(element, supportedCalendarComponentSetValue);
            case SupportedCalendarDataValue supportedCalendarDataValue ->
                appendSupportedCalendarData(element, supportedCalendarDataValue);
            case SupportedReportSetValue supportedReportSetValue ->
                appendSupportedReportSet(element, supportedReportSetValue);
            case TextValue textValue -> element.setText(textValue.value());
            case VCardValue vCardValue -> element.setText(vCardValue.vcard());
        }
    }

    private void appendCollationSet(
            final DavPropertyName propertyName, final Element element, final CollationSetValue collationSetValue) {
        final Namespace namespace = DAVConstants.CALDAV_NAMESPACE.equals(propertyName.namespace()) ? CAL : CARD;
        for (final String collation : collationSetValue.collations()) {
            element.addElement(qName("supported-collation", namespace)).setText(collation);
        }
    }

    private void appendPrivilegeSet(final Element element, final PrivilegeSetValue privilegeSetValue) {
        for (final DavPropertyName privilege : privilegeSetValue.privileges()) {
            final Element privilegeElement = addDav(element, "privilege");
            privilegeElement.addElement(qName(privilege));
        }
    }

    private void appendSupportedAddressData(
            final Element element, final SupportedAddressDataValue supportedAddressDataValue) {
        for (final AddressDataType addressDataType : supportedAddressDataValue.addressDataTypes()) {
            final Element addressDataTypeElement = element.addElement(qName("address-data-type", CARD));
            addressDataTypeElement.addAttribute("content-type", addressDataType.contentType());
            addressDataTypeElement.addAttribute("version", addressDataType.version());
        }
    }

    private void appendSupportedCalendarData(
            final Element element, final SupportedCalendarDataValue supportedCalendarDataValue) {
        for (final CalendarDataType calendarDataType : supportedCalendarDataValue.calendarDataTypes()) {
            final Element calendarDataTypeElement = element.addElement(qName("calendar-data", CAL));
            calendarDataTypeElement.addAttribute("content-type", calendarDataType.contentType());
            calendarDataTypeElement.addAttribute("version", calendarDataType.version());
        }
    }

    private void appendSupportedCalendarComponentSet(
            final Element element, final SupportedCalendarComponentSetValue supportedCalendarComponentSetValue) {
        for (final DavPropertyName component : supportedCalendarComponentSetValue.components()) {
            element.addElement(qName("comp", CAL)).addAttribute("name", component.name());
        }
    }

    private void appendSupportedReportSet(
            final Element element, final SupportedReportSetValue supportedReportSetValue) {
        for (final DavPropertyName report : supportedReportSetValue.reports()) {
            final Element supportedReport = addDav(element, "supported-report");
            final Element reportElement = addDav(supportedReport, "report");
            reportElement.addElement(qName(report));
        }
    }

    private String toXml(final Document document) throws IOException {
        final StringWriter out = new StringWriter();
        final OutputFormat format = new OutputFormat();
        format.setLineSeparator("\n");
        format.setOmitEncoding(true);
        final XMLWriter writer = new XMLWriter(out, format);
        writer.write(document);
        writer.flush();
        return out.toString();
    }

    private Element addDav(final Element parent, final String name) {
        return parent.addElement(qName(name, DAV));
    }

    private QName qName(final DavPropertyName propertyName) {
        if (DAVConstants.DAV_NAMESPACE.equals(propertyName.namespace())) {
            return qName(propertyName.name(), DAV);
        }
        if (DAVConstants.CARDDAV_NAMESPACE.equals(propertyName.namespace())) {
            return qName(propertyName.name(), CARD);
        }
        if (DAVConstants.CALDAV_NAMESPACE.equals(propertyName.namespace())) {
            return qName(propertyName.name(), CAL);
        }
        if (DAVConstants.CALENDAR_SERVER_NAMESPACE.equals(propertyName.namespace())) {
            return qName(propertyName.name(), CALENDAR_SERVER);
        }
        return QName.get(
                propertyName.name(),
                Namespace.get(namespacePrefix(propertyName.namespace()), propertyName.namespace()));
    }

    private QName qName(final String name, final Namespace namespace) {
        return QName.get(name, namespace);
    }

    private String reasonPhrase(final int status) {
        return switch (status) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            default -> "";
        };
    }

    private Map<String, String> namespaces(final DavMultiStatus multiStatus) {
        final Map<String, String> namespaces = new LinkedHashMap<>();
        namespaces.put(DAVConstants.DAV_NAMESPACE, "d");
        namespaces.put(DAVConstants.CARDDAV_NAMESPACE, "card");
        namespaces.put(DAVConstants.CALDAV_NAMESPACE, "cal");
        for (final DavResponse response : multiStatus.responses()) {
            for (final DavPropStat propStat : response.propStats()) {
                for (final DavProperty property : propStat.properties()) {
                    namespaces.putIfAbsent(
                            property.name().namespace(),
                            namespacePrefix(property.name().namespace()));
                    if (property.value() instanceof ResourceTypeValue resourceTypeValue) {
                        resourceTypeValue
                                .resourceTypes()
                                .forEach(resourceType -> namespaces.putIfAbsent(
                                        resourceType.namespace(), namespacePrefix(resourceType.namespace())));
                    } else if (property.value() instanceof PrivilegeSetValue privilegeSetValue) {
                        privilegeSetValue
                                .privileges()
                                .forEach(privilege -> namespaces.putIfAbsent(
                                        privilege.namespace(), namespacePrefix(privilege.namespace())));
                    } else if (property.value() instanceof SupportedReportSetValue supportedReportSetValue) {
                        supportedReportSetValue
                                .reports()
                                .forEach(report -> namespaces.putIfAbsent(
                                        report.namespace(), namespacePrefix(report.namespace())));
                    } else if (property.value()
                            instanceof SupportedCalendarComponentSetValue supportedCalendarComponentSetValue) {
                        supportedCalendarComponentSetValue
                                .components()
                                .forEach(component -> namespaces.putIfAbsent(
                                        component.namespace(), namespacePrefix(component.namespace())));
                    }
                }
            }
        }
        return namespaces;
    }

    private String namespacePrefix(final String namespaceUri) {
        final String standardPrefix = STANDARD_PREFIXES.get(namespaceUri);
        if (standardPrefix != null) {
            return standardPrefix;
        }
        final String normalized = StringUtils.defaultString(namespaceUri).replaceAll("[^A-Za-z0-9]", "");
        return normalized.isBlank() ? "x" : "x" + Math.abs(normalized.hashCode());
    }
}
