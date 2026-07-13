package de.domschmidt.koku.dav.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.dav.model.DavMethod;
import de.domschmidt.koku.dav.model.DavMultiStatus;
import de.domschmidt.koku.dav.model.DavPropertyNames;
import de.domschmidt.koku.dav.model.DavPropertyRequestType;
import de.domschmidt.koku.dav.model.DavRequest;
import de.domschmidt.koku.dav.model.DavResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class CardDavServiceTest {
    private static final String USERNAME = "current-user";
    private final CustomerContactRepository repository = mock(CustomerContactRepository.class);
    private final VCardFactory vCardFactory = mock(VCardFactory.class);
    private final CardDavService service = new CardDavService(repository, vCardFactory);

    @Test
    void rootAndPrincipalAdvertiseDiscoveryResources() {
        assertThat(service.handleRoot(request(DavMethod.PROPFIND, List.of(), null, null), USERNAME)
                        .responses())
                .singleElement()
                .extracting(DavResponse::href)
                .isEqualTo("/services/caldav/");
        assertThat(service.handlePrincipal(request(DavMethod.PROPFIND, List.of(), null, null), USERNAME)
                        .responses())
                .singleElement()
                .extracting(DavResponse::href)
                .isEqualTo("/services/caldav/principals/current-user/");
    }

    @Test
    void propfindReturnsCollectionAndActiveContacts() {
        final CustomerKafkaDto active = customer(42L, false, 1);
        final CustomerKafkaDto deleted = customer(43L, true, 2);
        when(repository.findAllContacts()).thenReturn(List.of(active, deleted));
        when(vCardFactory.toVCard(active)).thenReturn("BEGIN:VCARD\nEND:VCARD");

        final DavMultiStatus result = service.handleAddressBook(
                new DavRequest(
                        DavMethod.PROPFIND,
                        "/addressbooks/current-user/",
                        "/services/caldav",
                        1,
                        DavPropertyRequestType.ALL,
                        List.of(),
                        List.of(),
                        null,
                        null,
                        null),
                USERNAME);

        assertThat(result.responses())
                .extracting(DavResponse::href)
                .containsExactly(
                        "/services/caldav/addressbook/current-user/",
                        "/services/caldav/addressbook/current-user/42.vcf");
    }

    @Test
    void multigetReturnsRequestedContactAndNotFoundEntry() {
        final CustomerKafkaDto active = customer(42L, false, 1);
        when(repository.findAllContacts()).thenReturn(List.of(active));
        when(repository.findActiveContact(42L)).thenReturn(Optional.of(active));
        when(repository.findActiveContact(99L)).thenReturn(Optional.empty());
        when(vCardFactory.toVCard(active)).thenReturn("VCARD");

        final DavMultiStatus result = service.handleAddressBook(
                request(
                        DavMethod.REPORT,
                        List.of("/addressbooks/current-user/42.vcf", "/addressbooks/current-user/99.vcf"),
                        DavPropertyNames.ADDRESSBOOK_MULTIGET,
                        null),
                USERNAME);

        assertThat(result.responses()).hasSize(2);
        assertThat(result.responses()).extracting(DavResponse::status).containsExactlyInAnyOrder(null, 404);
    }

    @Test
    void incrementalSyncReportsDeletionOnce() {
        final CustomerKafkaDto active = customer(42L, false, 1);
        final CustomerKafkaDto deleted = customer(42L, true, 2);
        when(repository.findAllContacts()).thenReturn(List.of(active), List.of(deleted), List.of(deleted));
        when(vCardFactory.toVCard(active)).thenReturn("VCARD");

        final DavMultiStatus initial = service.handleAddressBook(syncRequest(null), USERNAME);
        final DavMultiStatus deletion = service.handleAddressBook(syncRequest(initial.syncToken()), USERNAME);
        final DavMultiStatus unchanged = service.handleAddressBook(syncRequest(deletion.syncToken()), USERNAME);

        assertThat(initial.responses()).singleElement();
        assertThat(deletion.responses())
                .singleElement()
                .extracting(DavResponse::status)
                .isEqualTo(404);
        assertThat(unchanged.responses()).isEmpty();
    }

    @Test
    void vcardAndEtagRequireActiveContact() {
        final CustomerKafkaDto active = customer(42L, false, 1);
        when(repository.findActiveContact(42L)).thenReturn(Optional.of(active));
        when(repository.findActiveContact(99L)).thenReturn(Optional.empty());
        when(vCardFactory.toVCard(active)).thenReturn("VCARD");

        assertThat(service.getVCard(42L)).isEqualTo("VCARD");
        assertThat(service.getContactEtag(42L)).isNotBlank();
        assertThatThrownBy(() -> service.getVCard(99L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.getContactEtag(99L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void unsupportedReportIsRejected() {
        when(repository.findAllContacts()).thenReturn(List.of());
        final DavRequest unsupported = request(DavMethod.REPORT, List.of(), DavPropertyNames.CALENDAR_QUERY, null);
        assertThatThrownBy(() -> service.handleAddressBook(unsupported, USERNAME))
                .isInstanceOf(ResponseStatusException.class);
    }

    private DavRequest request(
            DavMethod method,
            List<String> hrefs,
            de.domschmidt.koku.dav.model.DavPropertyName report,
            String syncToken) {
        return new DavRequest(
                method,
                "/addressbooks/current-user/",
                "/services/caldav",
                1,
                DavPropertyRequestType.ALL,
                List.of(),
                hrefs,
                report,
                null,
                syncToken);
    }

    private DavRequest syncRequest(String token) {
        return request(DavMethod.REPORT, List.of(), DavPropertyNames.SYNC_COLLECTION, token);
    }

    private static CustomerKafkaDto customer(long id, boolean deleted, int updatedDay) {
        return CustomerKafkaDto.builder()
                .id(id)
                .firstname("Ada")
                .lastname("Lovelace")
                .deleted(deleted)
                .updated(LocalDateTime.of(2026, java.time.Month.JULY, updatedDay, 12, 0))
                .build();
    }
}
