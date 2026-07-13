package de.domschmidt.koku.dav.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.dav.http.DavHttpHeaders;
import de.domschmidt.koku.dav.model.DavMultiStatus;
import de.domschmidt.koku.dav.model.DavRequest;
import de.domschmidt.koku.dav.service.CardDavService;
import de.domschmidt.koku.dav.xml.DavXmlReader;
import de.domschmidt.koku.dav.xml.DavXmlWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

class DavEndpointControllerTest {

    private final DavXmlReader reader = mock(DavXmlReader.class);
    private final DavXmlWriter writer = mock(DavXmlWriter.class);
    private final CardDavService service = mock(CardDavService.class);
    private final Authentication authentication = mock(Authentication.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final DavRequest davRequest = mock(DavRequest.class);
    private final DavMultiStatus multiStatus = mock(DavMultiStatus.class);

    @BeforeEach
    void setUp() throws Exception {
        when(authentication.getName()).thenReturn("authenticated-user");
        when(request.getMethod()).thenReturn("PROPFIND");
        when(reader.read(request)).thenReturn(davRequest);
        when(writer.write(multiStatus)).thenReturn("<multistatus/>");
    }

    @Test
    void rootDelegatesPropfindToCardDavService() {
        when(service.handleRoot(davRequest, "authenticated-user")).thenReturn(multiStatus);

        final ResponseEntity<String> response =
                new RootController(reader, writer, service).rootRequest(request, authentication);

        assertMultiStatus(response);
        verify(service).handleRoot(davRequest, "authenticated-user");
    }

    @Test
    void principalUsesAuthenticatedIdentityInsteadOfPathValue() {
        when(service.handlePrincipal(davRequest, "authenticated-user")).thenReturn(multiStatus);

        final ResponseEntity<String> response = new PrincipalController(reader, writer, service)
                .principalRequest(request, "different-path-user", authentication);

        assertMultiStatus(response);
        verify(service).handlePrincipal(davRequest, "authenticated-user");
    }

    @Test
    void addressBookAcceptsReportAndDelegatesToCardDavService() {
        when(request.getMethod()).thenReturn("REPORT");
        when(service.handleAddressBook(davRequest, "authenticated-user")).thenReturn(multiStatus);

        final ResponseEntity<String> response = new AddressBookController(reader, writer, service)
                .propfindOrReportRequest(request, "different-path-user", authentication);

        assertMultiStatus(response);
        verify(service).handleAddressBook(davRequest, "authenticated-user");
    }

    @Test
    void addressBookReturnsVCardWithEtagAndNoStore() {
        when(service.getContactEtag(42L)).thenReturn("etag-42");
        when(service.getVCard(42L)).thenReturn("BEGIN:VCARD\nEND:VCARD");

        final ResponseEntity<String> response =
                new AddressBookController(reader, writer, service).getContact("ignored", 42L, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("BEGIN:VCARD\nEND:VCARD");
        assertThat(response.getHeaders().getETag()).isEqualTo("etag-42");
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        assertThat(response.getHeaders().getContentType().toString())
                .contains("text/vcard")
                .contains("UTF-8");
    }

    @Test
    void headAdvertisesDavCapabilities() {
        final ResponseEntity<Void> response = new DavHeadController().options();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertDavCapabilityHeaders(response.getHeaders());
    }

    @Test
    void optionsAdvertisesDavCapabilities() {
        final ResponseEntity<Void> response = new DavOptionsController().options();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertDavCapabilityHeaders(response.getHeaders());
    }

    @Test
    void exceptionHandlerPreservesResponseStatus() {
        final ResponseEntity<Void> response =
                new DavExceptionHandler().responseStatusException(new ResponseStatusException(HttpStatus.BAD_REQUEST));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void serializationFailureBecomesServerError() throws Exception {
        when(service.handleRoot(davRequest, "authenticated-user")).thenReturn(multiStatus);
        when(writer.write(multiStatus)).thenThrow(new IOException("broken XML"));
        final RootController controller = new RootController(reader, writer, service);

        assertThatThrownBy(() -> controller.rootRequest(request, authentication))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private static void assertMultiStatus(final ResponseEntity<String> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.MULTI_STATUS);
        assertThat(response.getBody()).isEqualTo("<multistatus/>");
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        assertDavCapabilityHeaders(response.getHeaders());
    }

    private static void assertDavCapabilityHeaders(final HttpHeaders headers) {
        assertThat(headers.getFirst(DavHttpHeaders.DAV)).isEqualTo(DavHttpHeaders.DAV_COMPLIANCE);
        assertThat(headers.getFirst(DavHttpHeaders.MS_AUTHOR_VIA)).isEqualTo(DavHttpHeaders.MS_AUTHOR_VIA_VALUE);
        assertThat(headers.getFirst(DavHttpHeaders.ALLOW)).isEqualTo(DavHttpHeaders.ALLOW_VALUE);
    }
}
