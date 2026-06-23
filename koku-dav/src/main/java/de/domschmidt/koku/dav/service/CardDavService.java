package de.domschmidt.koku.dav.service;

import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.dav.APIConstants;
import de.domschmidt.koku.dav.http.DavMediaTypes;
import de.domschmidt.koku.dav.model.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CardDavService {

    private static final String SYNC_STATE_PREFIX = "urn:koku:carddav:sync:";
    private static final Pattern VCF_ID_PATTERN = Pattern.compile(".*/(\\d+)\\.vcf$");

    private final CustomerContactRepository customerContactRepository;
    private final VCardFactory vCardFactory;

    public CardDavService(final CustomerContactRepository customerContactRepository, final VCardFactory vCardFactory) {
        this.customerContactRepository = customerContactRepository;
        this.vCardFactory = vCardFactory;
    }

    public DavMultiStatus handleRoot(final DavRequest request, final String username) {
        return new DavMultiStatus(List.of(new DavResponseBuilder(request.hrefBasePath() + "/")
                .property(
                        DavPropertyNames.CURRENT_USER_PRINCIPAL,
                        new HrefValue(principalHref(request.hrefBasePath(), username)))
                .build(request.propertyRequestType(), request.requestedProperties())));
    }

    public DavMultiStatus handlePrincipal(final DavRequest request, final String username) {
        return new DavMultiStatus(List.of(new DavResponseBuilder(principalHref(request.hrefBasePath(), username))
                .property(
                        DavPropertyNames.ADDRESSBOOK_HOME_SET,
                        new HrefValue(addressBookHref(request.hrefBasePath(), username)))
                .property(
                        DavPropertyNames.CALENDAR_HOME_SET,
                        new HrefValue(calendarHomeHref(request.hrefBasePath(), username)))
                .property(DavPropertyNames.RESOURCETYPE, new ResourceTypeValue(List.of(DavPropertyNames.PRINCIPAL)))
                .property(DavPropertyNames.DISPLAYNAME, new TextValue("KoKu Principal"))
                .property(
                        DavPropertyNames.CURRENT_USER_PRINCIPAL,
                        new HrefValue(principalHref(request.hrefBasePath(), username)))
                .property(DavPropertyNames.OWNER, new HrefValue(principalHref(request.hrefBasePath(), username)))
                .property(DavPropertyNames.CURRENT_USER_PRIVILEGE_SET, DavCapabilities.readPrivileges())
                .build(request.propertyRequestType(), request.requestedProperties())));
    }

    public DavMultiStatus handleAddressBook(final DavRequest request, final String username) {
        final List<DavResponse> responses = new ArrayList<>();
        final List<CustomerKafkaDto> allContacts = allCustomers();
        final List<CustomerKafkaDto> activeContacts = activeCustomers(allContacts);
        final boolean syncCollectionReport =
                DavMethod.REPORT == request.method() && DavPropertyNames.SYNC_COLLECTION.equals(request.reportName());
        final String currentSyncToken = syncToken(allContacts);
        if (request.method() == DavMethod.PROPFIND) {
            responses.add(addressBookCollectionResponse(username, request, currentSyncToken));
        }
        if (request.method() == DavMethod.REPORT) {
            requireSupportedReport(
                    request.reportName(),
                    DavPropertyNames.ADDRESSBOOK_MULTIGET,
                    DavPropertyNames.ADDRESSBOOK_QUERY,
                    DavPropertyNames.SYNC_COLLECTION);
        }
        if (syncCollectionReport && currentSyncToken.equals(request.syncToken())) {
            return new DavMultiStatus(responses, currentSyncToken);
        }
        if (syncCollectionReport) {
            allContacts.stream()
                    .filter(customer -> Boolean.TRUE.equals(customer.getDeleted()))
                    .map(customer ->
                            DavResponse.notFound(contactHref(request.hrefBasePath(), username, customer.getId())))
                    .forEach(responses::add);
        }
        if (request.method() == DavMethod.REPORT || (request.depth() != null && request.depth() >= 1)) {
            final Set<Long> requestedContactIds = parseContactIds(request.hrefs());
            if (requestedContactIds.isEmpty()) {
                activeContacts.stream()
                        .map(customer -> contactResponse(
                                request.hrefBasePath(),
                                username,
                                customer,
                                request.propertyRequestType(),
                                request.requestedProperties()))
                        .forEach(responses::add);
            } else {
                requestedContactIds.stream()
                        .map(contactId -> customerContactRepository
                                .findActiveContact(contactId)
                                .map(customer -> contactResponse(
                                        request.hrefBasePath(),
                                        username,
                                        customer,
                                        request.propertyRequestType(),
                                        request.requestedProperties()))
                                .orElseGet(() ->
                                        DavResponse.notFound(contactHref(request.hrefBasePath(), username, contactId))))
                        .forEach(responses::add);
            }
        }
        if (syncCollectionReport) {
            return new DavMultiStatus(responses, currentSyncToken);
        }
        return new DavMultiStatus(responses);
    }

    public String getVCard(final long contactId) {
        return customerContactRepository
                .findActiveContact(contactId)
                .map(vCardFactory::toVCard)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));
    }

    public String getContactEtag(final long contactId) {
        return customerContactRepository
                .findActiveContact(contactId)
                .map(this::etag)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));
    }

    private DavResponse addressBookCollectionResponse(
            final String username, final DavRequest request, final String syncToken) {
        return new DavResponseBuilder(addressBookHref(request.hrefBasePath(), username))
                .property(DavPropertyNames.DISPLAYNAME, new TextValue("KoKu Address Book"))
                .property(DavPropertyNames.ADDRESSBOOK_DESCRIPTION, new TextValue("KoKu customer contacts"))
                .property(
                        DavPropertyNames.RESOURCETYPE,
                        new ResourceTypeValue(List.of(DavPropertyNames.COLLECTION, DavPropertyNames.ADDRESSBOOK)))
                .property(
                        DavPropertyNames.CURRENT_USER_PRINCIPAL,
                        new HrefValue(principalHref(request.hrefBasePath(), username)))
                .property(DavPropertyNames.OWNER, new HrefValue(principalHref(request.hrefBasePath(), username)))
                .property(DavPropertyNames.CURRENT_USER_PRIVILEGE_SET, DavCapabilities.readPrivileges())
                .property(DavPropertyNames.SUPPORTED_REPORT_SET, supportedReports())
                .property(DavPropertyNames.SUPPORTED_ADDRESS_DATA, supportedAddressData())
                .property(DavPropertyNames.SUPPORTED_COLLATION_SET, DavCapabilities.supportedCollations())
                .property(
                        DavPropertyNames.MAX_RESOURCE_SIZE, new TextValue(DavResourceMetadata.MAX_RESOURCE_SIZE_BYTES))
                .property(DavPropertyNames.SYNC_TOKEN, new TextValue(syncToken))
                .property(DavPropertyNames.GETCTAG, new TextValue(syncToken))
                .build(request.propertyRequestType(), request.requestedProperties());
    }

    private DavResponse contactResponse(
            final String hrefBasePath,
            final String username,
            final CustomerKafkaDto customer,
            final DavPropertyRequestType propertyRequestType,
            final List<DavPropertyName> requestedProperties) {
        final String vcard = vCardFactory.toVCard(customer);
        return new DavResponseBuilder(contactHref(hrefBasePath, username, customer.getId()))
                .property(DavPropertyNames.GETETAG, new TextValue(etag(customer)))
                .property(DavPropertyNames.GETCONTENTTYPE, new TextValue(DavMediaTypes.VCARD_UTF8))
                .property(
                        DavPropertyNames.GETLASTMODIFIED,
                        new TextValue(DavResourceMetadata.lastModified(customer.getUpdated())))
                .property(DavPropertyNames.ADDRESS_DATA, new VCardValue(vcard))
                .property(
                        DavPropertyNames.GETCONTENTLENGTH,
                        new TextValue(String.valueOf(DavResourceMetadata.byteLength(vcard))))
                .build(propertyRequestType, requestedProperties);
    }

    private Set<Long> parseContactIds(final List<String> hrefs) {
        final Set<Long> contactIds = new HashSet<>();
        for (final String href : hrefs) {
            final Matcher matcher = VCF_ID_PATTERN.matcher(href);
            if (matcher.find()) {
                contactIds.add(Long.parseLong(matcher.group(1)));
            }
        }
        return contactIds;
    }

    private List<CustomerKafkaDto> allCustomers() {
        return customerContactRepository.findAllContacts();
    }

    private List<CustomerKafkaDto> activeCustomers(final List<CustomerKafkaDto> customers) {
        return customers.stream()
                .filter(customer -> !Boolean.TRUE.equals(customer.getDeleted()))
                .toList();
    }

    private void requireSupportedReport(final DavPropertyName reportName, final DavPropertyName... supportedReports) {
        if (reportName == null || Arrays.stream(supportedReports).noneMatch(reportName::equals)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported CardDAV REPORT");
        }
    }

    private SupportedAddressDataValue supportedAddressData() {
        return new SupportedAddressDataValue(
                List.of(new AddressDataType(DavMediaTypes.VCARD, DavMediaTypes.VCARD_VERSION)));
    }

    private SupportedReportSetValue supportedReports() {
        return new SupportedReportSetValue(List.of(
                DavPropertyNames.ADDRESSBOOK_MULTIGET,
                DavPropertyNames.ADDRESSBOOK_QUERY,
                DavPropertyNames.SYNC_COLLECTION));
    }

    private String syncToken(final List<CustomerKafkaDto> customers) {
        final int token = customers.stream()
                .map(customer -> Objects.hash(customer.getId(), customer.getUpdated(), customer.getDeleted()))
                .sorted()
                .reduce(1, (left, right) -> 31 * left + right);
        return SYNC_STATE_PREFIX + Integer.toUnsignedString(token);
    }

    private String etag(final CustomerKafkaDto customer) {
        return DavResourceMetadata.etag(customer.getId(), customer.getUpdated());
    }

    private String principalHref(final String hrefBasePath, final String username) {
        return hrefBasePath + APIConstants.PRINCIPALS_PATH + "/" + username + "/";
    }

    private String addressBookHref(final String hrefBasePath, final String username) {
        return hrefBasePath + APIConstants.ADDRESSBOOK_PATH + "/" + username + "/";
    }

    private String calendarHomeHref(final String hrefBasePath, final String username) {
        return hrefBasePath + APIConstants.CALENDAR_PATH + "/" + username + "/";
    }

    private String contactHref(final String hrefBasePath, final String username, final long contactId) {
        return addressBookHref(hrefBasePath, username) + contactId + ".vcf";
    }
}
