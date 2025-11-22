package de.domschmidt.koku.carddav.controller;

import de.domschmidt.koku.carddav.APIConstants;
import de.domschmidt.koku.carddav.DAVConstants;
import de.domschmidt.koku.carddav.helper.DavUtils;
import de.domschmidt.koku.carddav.kafka.customers.service.CustomerKTableProcessor;
import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping(APIConstants.ADDRESSBOOK_PATH)
@Slf4j
@RequiredArgsConstructor
public class AddressBookController {

    private final CustomerKTableProcessor customerKTableProcessor;

    @Data
    @Builder
    private static class AddressBookInfoRequest {

    }

    private static final Map<String, Map<String, BiConsumer<AddressBookInfoRequest, Element>>> ADDRESS_BOOK_PROPS_RESOLVER = Map.of(
            DAVConstants.DAV_NAMESPACE, Map.of(
                    DAVConstants.DAV_PROP_DISPLAYNAME, (addressBookInfoRequest, outputRoot) -> {
                        final Element outputNode = outputRoot.addElement("d:" + DAVConstants.DAV_PROP_DISPLAYNAME);
                        outputNode.setText("KoKu Address Book");
                    },
                    DAVConstants.DAV_PROP_RESOURCETYPE, (addressBookInfoRequest, outputRoot) -> {
                        final Element outputNode = outputRoot.addElement("d:" + DAVConstants.DAV_PROP_RESOURCETYPE);
                        outputNode.addElement("d:collection");
                        outputNode.addElement("card:addressbook");
                    },
                    DAVConstants.DAV_PROP_CURRENT_USER_PRINCIPAL, (addressBookInfoRequest, outputRoot) -> {
                        final Element outputNode = outputRoot.addElement("d:" + DAVConstants.DAV_PROP_CURRENT_USER_PRINCIPAL);
                        final Element hrefNode = outputNode.addElement("d:href");
                        hrefNode.setText(APIConstants.API_BASEPATH + APIConstants.PRINCIPALS_PATH + "/koku/");
                    }
            )
    );

    @RequestMapping(consumes = {
            MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_XML_VALUE,
    }, produces = MediaType.APPLICATION_XML_VALUE, path = "/{userName}")
    @ResponseStatus(HttpStatus.MULTI_STATUS)
    public String propfindOrReportRequest(final HttpServletRequest request, final @PathVariable String userName) {
        final Document result = DocumentHelper.createDocument();
        final Element multiStatusResponse = DavUtils.attachMultiStatusResponse(result);

        try {
            final Document document = new SAXReader().read(request.getInputStream());
            final List<Element> requestedProps = DavUtils.parseRequestedDavProps(document);

            if (requestedProps != null) {
                final String depthHeader = request.getHeader(DAVConstants.DAV_DEPTH_HEADER_NAME);

                if (DAVConstants.DAV_PROPFIND_METHOD_NAME.equalsIgnoreCase(request.getMethod())) {
                    final List<Element> notFoundProps = new ArrayList<>();
                    final List<BiConsumer<AddressBookInfoRequest, Element>> resolvedPropGenerators = new ArrayList<>();
                    for (final Element requestedPropElement : requestedProps) {
                        final Map<String, BiConsumer<AddressBookInfoRequest, Element>> nameSpaceSpecificPropResolvers = ADDRESS_BOOK_PROPS_RESOLVER.get(requestedPropElement.getNamespaceURI());
                        if (nameSpaceSpecificPropResolvers == null) {
                            notFoundProps.add((Element) requestedPropElement.clone());
                        } else {
                            final BiConsumer<AddressBookInfoRequest, Element> propResolver = nameSpaceSpecificPropResolvers.get(requestedPropElement.getName());
                            if (propResolver == null) {
                                notFoundProps.add((Element) requestedPropElement.clone());
                            } else {
                                resolvedPropGenerators.add(propResolver);
                            }
                        }
                    }
                    appendResponse(multiStatusResponse, APIConstants.API_BASEPATH + APIConstants.ADDRESSBOOK_PATH + "/" + userName + "/", resolvedPropGenerators, AddressBookInfoRequest.builder().build(), notFoundProps);
                }

                if (NumberUtils.isCreatable(depthHeader) && Integer.parseInt(depthHeader) >= 1) {
                    final Collection<CustomerKafkaDto> filteredContacts;
                    final Set<Long> contactFilter = parseContactFilter(document);
                    if (contactFilter != null && !contactFilter.isEmpty()) {
                        filteredContacts = this.customerKTableProcessor.getCustomers().values().stream().filter(customer -> contactFilter.contains(customer.getId())).toList();
                    } else {
                        filteredContacts = this.customerKTableProcessor.getCustomers().values();
                    }

                    for (final CustomerKafkaDto currentContact : filteredContacts) {
                        buildAddressBookResponse(currentContact, requestedProps, multiStatusResponse, userName);
                    }
                }
            }
        } catch (final DocumentException ue) {
            log.error("Unable to parse requested document", ue);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (final IOException ioe) {
            log.error("Unable to handle input stream", ioe);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            return DavUtils.toXmlString(result);
        } catch (final IOException e) {
            log.error("Unable to write xml response", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Data
    @Builder
    private static class AddressBookContactsRequest {

        CustomerKafkaDto contact;

    }

    private static final Map<String, Map<String, BiConsumer<AddressBookContactsRequest, Element>>> ADDRESS_BOOK_QUERY_PROPS_RESOLVER = Map.of(
            DAVConstants.DAV_NAMESPACE, Map.of(
                    DAVConstants.DAV_PROP_GETETAG, (addressBookItem, outputRoot) -> {
                        Element etagElement = outputRoot.addElement("d:" + DAVConstants.DAV_PROP_GETETAG);
                        etagElement.setText("&quot;" + addressBookItem.hashCode() + "&quot;");
                    }
            ),
            DAVConstants.CARDDAV_NAMESPACE, Map.of(
                    DAVConstants.CARD_PROP_ADDRESS_DATA, (addressBookItem, outputRoot) -> {
                        Element addressDataElement = outputRoot.addElement("card:" + DAVConstants.CARD_PROP_ADDRESS_DATA);
                        StringBuilder result = new StringBuilder();
                        final CustomerKafkaDto currentContact = addressBookItem.getContact();
                        result.append("BEGIN:VCARD&#13;\n");
                        result.append("VERSION:3.0&#13;\n");
                        result.append("PRODID:-//KoKu//KoKu Carddav//DE&#13;\n");
                        result.append("N:").append(StringUtils.defaultString(currentContact.getLastname())).append(";").append(StringUtils.defaultString(currentContact.getFirstname())).append(";;;&#13;\n");
                        result.append("FN:").append(StringUtils.defaultString(currentContact.getFullname())).append("&#13;\n");
                        if (StringUtils.isNotBlank(currentContact.getBusinessTelephoneNo())) {
                            result.append("TEL;TYPE=work:").append(currentContact.getBusinessTelephoneNo()).append("&#13;\n");
                        }
                        if (StringUtils.isNotBlank(currentContact.getMobileTelephoneNo())) {
                            result.append("TEL;TYPE=cell:").append(currentContact.getMobileTelephoneNo()).append("&#13;\n");
                        }
                        if (StringUtils.isNotBlank(currentContact.getPrivateTelephoneNo())) {
                            result.append("TEL;TYPE=home:").append(currentContact.getPrivateTelephoneNo()).append("&#13;\n");
                        }
                        if (StringUtils.isNotBlank(currentContact.getEmail())) {
                            result.append("EMAIL:").append(currentContact.getEmail()).append("&#13;\n");
                        }
                        result.append("UID:").append(currentContact.getId()).append("&#13;\n");
                        result.append("REV:").append(currentContact.getUpdated().format(DateTimeFormatter.BASIC_ISO_DATE)).append("&#13;\n");
                        result.append("END:VCARD&#13;\n");
                        addressDataElement.setText(result.toString());
                    }
            )
    );

    private Set<Long> parseContactFilter(final Document document) {
        final Set<Long> contactFilter;

        final List<Element> hrefs = document.getRootElement().elements(new QName("href", Namespace.get(DAVConstants.DAV_NAMESPACE)));
        if (hrefs != null && !hrefs.isEmpty()) {
            contactFilter = new HashSet<>();
            for (final Element hrefElement : hrefs) {
                try {
                    final Pattern vcfIdPattern = Pattern.compile(".*/(\\d+)\\.vcf$");
                    final Matcher matcher = vcfIdPattern.matcher(hrefElement.getStringValue());
                    if (matcher.find()) {
                        final String group = matcher.group(1);
                        contactFilter.add(Long.parseLong(group));
                    }
                } catch (final Exception e) {
                    log.error("Unexpected error during href id extraction", e);
                }
            }
        } else {
            contactFilter = null;
        }

        return contactFilter;
    }

    private static void buildAddressBookResponse(CustomerKafkaDto resolvedCustomer, List<Element> requestedProps, Element multiStatusResponse, final String userName) {
        final List<Element> notFoundProps = new ArrayList<>();
        final List<BiConsumer<AddressBookContactsRequest, Element>> resolvedPropGenerators = new ArrayList<>();
        for (final Element requestedPropElement : requestedProps) {
            final Map<String, BiConsumer<AddressBookContactsRequest, Element>> nameSpaceSpecificPropResolvers = ADDRESS_BOOK_QUERY_PROPS_RESOLVER.get(requestedPropElement.getNamespaceURI());
            if (nameSpaceSpecificPropResolvers == null) {
                notFoundProps.add((Element) requestedPropElement.clone());
            } else {
                final BiConsumer<AddressBookContactsRequest, Element> propResolver = nameSpaceSpecificPropResolvers.get(requestedPropElement.getName());
                if (propResolver == null) {
                    notFoundProps.add((Element) requestedPropElement.clone());
                } else {
                    resolvedPropGenerators.add(propResolver);
                }
            }
        }
        appendResponse(multiStatusResponse, APIConstants.API_BASEPATH + APIConstants.ADDRESSBOOK_PATH + "/" + userName + "/" + resolvedCustomer.getId() + ".vcf", resolvedPropGenerators, AddressBookContactsRequest.builder()
                .contact(resolvedCustomer)
                .build(), notFoundProps);
    }

    private static <T> void appendResponse(final Element root, final String responseHref, List<BiConsumer<T, Element>> resolvedPropGenerators, final T propRequest, List<Element> notFoundProps) {
        Element response = root.addElement("d:response");
        Element href = response.addElement("d:href");
        href.setText(responseHref);

        if (!resolvedPropGenerators.isEmpty()) {
            Element posPropStat = response.addElement("d:propstat");
            Element prop = posPropStat.addElement("d:prop");
            posPropStat.addElement("d:status").setText("HTTP/1.1 200 OK");
            for (final BiConsumer<T, Element> resolvedPropGenerator : resolvedPropGenerators) {
                resolvedPropGenerator.accept(propRequest, prop);
            }
        }
        if (!notFoundProps.isEmpty()) {
            Element posPropStat = response.addElement("d:propstat", DAVConstants.DAV_NAMESPACE);
            Element prop = posPropStat.addElement("d:prop", DAVConstants.DAV_NAMESPACE);
            posPropStat.addElement("d:status", DAVConstants.DAV_NAMESPACE).setText("HTTP/1.1 404 Not Found");
            for (final Element notFoundProp : notFoundProps) {
                prop.add(notFoundProp);
            }
        }
    }


}
