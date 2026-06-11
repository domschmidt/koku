package de.domschmidt.koku.carddav.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.domschmidt.koku.carddav.DAVConstants;
import de.domschmidt.koku.carddav.http.DavHrefResolver;
import de.domschmidt.koku.carddav.model.DavMethod;
import de.domschmidt.koku.carddav.model.DavPropertyRequestType;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

class DavXmlReaderTest {

    private final DavXmlReader reader = new DavXmlReader(new DavHrefResolver());

    @Test
    void readsMethodDepthRequestedPropertiesAndHrefs() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("REPORT", "/addressbook/koku/");
        request.addHeader(DAVConstants.DAV_DEPTH_HEADER_NAME, "1");
        request.setContent("""
                <?xml version="1.0" encoding="UTF-8"?>
                <D:addressbook-multiget xmlns:D="urn:ietf:params:xml:ns:carddav" xmlns:A="DAV:">
                  <A:prop>
                    <A:getetag/>
                    <D:address-data/>
                  </A:prop>
                  <A:href>/services/carddav/addressbook/koku/1.vcf</A:href>
                </D:addressbook-multiget>
                """.getBytes(StandardCharsets.UTF_8));

        final var davRequest = reader.read(request);

        assertThat(davRequest.method()).isEqualTo(DavMethod.REPORT);
        assertThat(davRequest.hrefBasePath()).isEmpty();
        assertThat(davRequest.depth()).isEqualTo(1);
        assertThat(davRequest.requestedProperties()).extracting("name").containsExactly("getetag", "address-data");
        assertThat(davRequest.hrefs()).containsExactly("/services/carddav/addressbook/koku/1.vcf");
        assertThat(davRequest.reportName().name()).isEqualTo("addressbook-multiget");
    }

    @Test
    void resolvesForwardedPrefixForExternalDavHrefs() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("PROPFIND", "/calendars/koku/");
        request.addHeader("X-Forwarded-Prefix", "/services/caldav");

        final var davRequest = reader.read(request);

        assertThat(davRequest.hrefBasePath()).isEqualTo("/services/caldav");
    }

    @Test
    void rejectsDoctypeDeclarations() {
        final MockHttpServletRequest request = new MockHttpServletRequest("PROPFIND", "/");
        request.setContent("""
                <?xml version="1.0"?>
                <!DOCTYPE foo [ <!ENTITY xxe SYSTEM "file:///etc/passwd"> ]>
                <A:propfind xmlns:A="DAV:"><A:prop><A:displayname/></A:prop></A:propfind>
                """.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> reader.read(request)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void readsPropnameRequestsAsNamesOnlyDiscovery() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("PROPFIND", "/addressbook/koku/");
        request.setContent("""
                <?xml version="1.0" encoding="UTF-8"?>
                <A:propfind xmlns:A="DAV:">
                  <A:propname/>
                </A:propfind>
                """.getBytes(StandardCharsets.UTF_8));

        final var davRequest = reader.read(request);

        assertThat(davRequest.propertyRequestType()).isEqualTo(DavPropertyRequestType.NAMES_ONLY);
        assertThat(davRequest.requestedProperties()).isEmpty();
    }

    @Test
    void readsCalendarQueryTimeRangeForServerSideFiltering() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("REPORT", "/calendars/koku/appointments/");
        request.setContent("""
                <?xml version="1.0" encoding="UTF-8"?>
                <cal:calendar-query xmlns:cal="urn:ietf:params:xml:ns:caldav" xmlns:d="DAV:">
                  <d:prop>
                    <d:getetag/>
                    <cal:calendar-data/>
                  </d:prop>
                  <cal:filter>
                    <cal:comp-filter name="VCALENDAR">
                      <cal:comp-filter name="VEVENT">
                        <cal:time-range start="20260611T080000Z" end="20260612T080000Z"/>
                      </cal:comp-filter>
                    </cal:comp-filter>
                  </cal:filter>
                </cal:calendar-query>
                """.getBytes(StandardCharsets.UTF_8));

        final var davRequest = reader.read(request);

        assertThat(davRequest.reportName().name()).isEqualTo("calendar-query");
        assertThat(davRequest.timeRange().start()).isEqualTo(Instant.parse("2026-06-11T08:00:00Z"));
        assertThat(davRequest.timeRange().end()).isEqualTo(Instant.parse("2026-06-12T08:00:00Z"));
        assertThat(davRequest.requestedProperties()).extracting("name").containsExactly("getetag", "calendar-data");
    }

    @Test
    void readsWebDavSyncTokenForIncrementalSynchronization() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("REPORT", "/addressbook/koku/");
        request.setContent("""
                <?xml version="1.0" encoding="UTF-8"?>
                <A:sync-collection xmlns:A="DAV:">
                  <A:sync-token>urn:koku:carddav:sync:123</A:sync-token>
                  <A:sync-level>1</A:sync-level>
                  <A:prop>
                    <A:getetag/>
                  </A:prop>
                </A:sync-collection>
                """.getBytes(StandardCharsets.UTF_8));

        final var davRequest = reader.read(request);

        assertThat(davRequest.reportName().name()).isEqualTo("sync-collection");
        assertThat(davRequest.syncToken()).isEqualTo("urn:koku:carddav:sync:123");
        assertThat(davRequest.requestedProperties()).extracting("name").containsExactly("getetag");
    }
}
