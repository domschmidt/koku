package de.domschmidt.koku.carddav.xml;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.carddav.DAVConstants;
import de.domschmidt.koku.carddav.model.*;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;

class DavXmlWriterTest {

    private final DavXmlWriter writer = new DavXmlWriter();

    @Test
    void writesMultiStatusWithResolvedAndMissingProperties() throws IOException {
        final DavMultiStatus multistatus = new DavMultiStatus(List.of(new DavResponse(
                "/services/carddav/addressbook/koku/",
                List.of(
                        new DavPropStat(
                                200,
                                List.of(new DavProperty(
                                        new DavPropertyName(DAVConstants.DAV_NAMESPACE, "displayname"),
                                        new TextValue("KoKu Address Book")))),
                        new DavPropStat(
                                404,
                                List.of(new DavProperty(
                                        new DavPropertyName(DAVConstants.DAV_NAMESPACE, "unsupported"),
                                        new EmptyValue())))))));

        final String xml = writer.write(multistatus);

        assertThat(xml).contains("<d:multistatus");
        assertThat(xml).contains("<d:href>/services/carddav/addressbook/koku/</d:href>");
        assertThat(xml).contains("<d:displayname>KoKu Address Book</d:displayname>");
        assertThat(xml).contains("<d:status>HTTP/1.1 200 OK</d:status>");
        assertThat(xml).contains("<d:unsupported/>");
        assertThat(xml).contains("<d:status>HTTP/1.1 404 Not Found</d:status>");
    }

    @Test
    void escapesXmlTextAtTheBoundary() throws IOException {
        final DavMultiStatus multistatus = new DavMultiStatus(List.of(new DavResponse(
                "/",
                List.of(new DavPropStat(
                        200,
                        List.of(new DavProperty(
                                new DavPropertyName(DAVConstants.DAV_NAMESPACE, "displayname"),
                                new TextValue("A & B < C"))))))));

        assertThat(writer.write(multistatus)).contains("<d:displayname>A &amp; B &lt; C</d:displayname>");
    }

    @Test
    void writesWebDavSyncTokenAtMultiStatusLevel() throws IOException {
        final DavMultiStatus multistatus = new DavMultiStatus(
                List.of(new DavResponse(
                        "/services/carddav/addressbook/koku/1.vcf",
                        List.of(new DavPropStat(
                                200, List.of(new DavProperty(DavPropertyNames.GETETAG, new TextValue("\"1\""))))))),
                "urn:koku:carddav:sync:1");

        final String xml = writer.write(multistatus);

        assertXmlSimilar("""
                <d:multistatus xmlns:d="DAV:">
                  <d:response>
                    <d:href>/services/carddav/addressbook/koku/1.vcf</d:href>
                    <d:propstat>
                      <d:prop>
                        <d:getetag>"1"</d:getetag>
                      </d:prop>
                      <d:status>HTTP/1.1 200 OK</d:status>
                    </d:propstat>
                  </d:response>
                  <d:sync-token>urn:koku:carddav:sync:1</d:sync-token>
                </d:multistatus>
                """, xml);
    }

    @Test
    void writesDeletedWebDavSyncResourcesAsResponseLevelNotFound() throws IOException {
        final DavMultiStatus multistatus = new DavMultiStatus(
                List.of(DavResponse.notFound("/services/carddav/addressbook/koku/1.vcf")), "urn:koku:carddav:sync:2");

        final String xml = writer.write(multistatus);

        assertXmlSimilar("""
                <d:multistatus xmlns:d="DAV:">
                  <d:response>
                    <d:href>/services/carddav/addressbook/koku/1.vcf</d:href>
                    <d:status>HTTP/1.1 404 Not Found</d:status>
                  </d:response>
                  <d:sync-token>urn:koku:carddav:sync:2</d:sync-token>
                </d:multistatus>
                """, xml);
    }

    @Test
    void writesCardDavCompatibilityCapabilities() throws IOException {
        final DavMultiStatus multistatus = new DavMultiStatus(List.of(new DavResponse(
                "/services/carddav/addressbook/koku/",
                List.of(new DavPropStat(
                        200,
                        List.of(
                                new DavProperty(
                                        DavPropertyNames.SUPPORTED_REPORT_SET,
                                        new SupportedReportSetValue(List.of(
                                                DavPropertyNames.ADDRESSBOOK_MULTIGET,
                                                DavPropertyNames.ADDRESSBOOK_QUERY,
                                                DavPropertyNames.SYNC_COLLECTION))),
                                new DavProperty(
                                        DavPropertyNames.SUPPORTED_ADDRESS_DATA,
                                        new SupportedAddressDataValue(
                                                List.of(new AddressDataType("text/vcard", "3.0")))),
                                new DavProperty(
                                        DavPropertyNames.CURRENT_USER_PRIVILEGE_SET,
                                        new PrivilegeSetValue(List.of(DavPropertyNames.READ_PRIVILEGE))),
                                new DavProperty(
                                        DavPropertyNames.SUPPORTED_COLLATION_SET,
                                        new CollationSetValue(
                                                List.of("i;ascii-casemap", "i;octet", "i;unicode-casemap"))),
                                new DavProperty(DavPropertyNames.GETCTAG, new TextValue("token-1"))))))));

        final String xml = writer.write(multistatus);

        assertXmlSimilar("""
                <d:multistatus xmlns:d="DAV:" xmlns:card="urn:ietf:params:xml:ns:carddav"
                               xmlns:cs="http://calendarserver.org/ns/">
                  <d:response>
                    <d:href>/services/carddav/addressbook/koku/</d:href>
                    <d:propstat>
                      <d:prop>
                        <d:supported-report-set>
                          <d:supported-report>
                            <d:report>
                              <card:addressbook-multiget/>
                            </d:report>
                          </d:supported-report>
                          <d:supported-report>
                            <d:report>
                              <card:addressbook-query/>
                            </d:report>
                          </d:supported-report>
                          <d:supported-report>
                            <d:report>
                              <d:sync-collection/>
                            </d:report>
                          </d:supported-report>
                        </d:supported-report-set>
                        <card:supported-address-data>
                          <card:address-data-type content-type="text/vcard" version="3.0"/>
                        </card:supported-address-data>
                        <d:current-user-privilege-set>
                          <d:privilege>
                            <d:read/>
                          </d:privilege>
                        </d:current-user-privilege-set>
                        <card:supported-collation-set>
                          <card:supported-collation>i;ascii-casemap</card:supported-collation>
                          <card:supported-collation>i;octet</card:supported-collation>
                          <card:supported-collation>i;unicode-casemap</card:supported-collation>
                        </card:supported-collation-set>
                        <cs:getctag>token-1</cs:getctag>
                      </d:prop>
                      <d:status>HTTP/1.1 200 OK</d:status>
                    </d:propstat>
                  </d:response>
                </d:multistatus>
                """, xml);
    }

    @Test
    void writesCalDavCalendarCapabilities() throws IOException {
        final DavMultiStatus multistatus = new DavMultiStatus(
                List.of(
                        new DavResponse(
                                "/services/caldav/calendars/koku/appointments/",
                                List.of(
                                        new DavPropStat(
                                                200,
                                                List.of(
                                                        new DavProperty(
                                                                DavPropertyNames.SUPPORTED_REPORT_SET,
                                                                new SupportedReportSetValue(List.of(
                                                                        DavPropertyNames.CALENDAR_MULTIGET,
                                                                        DavPropertyNames.CALENDAR_QUERY))),
                                                        new DavProperty(
                                                                DavPropertyNames.SUPPORTED_CALENDAR_DATA,
                                                                new SupportedCalendarDataValue(List.of(
                                                                        new CalendarDataType("text/calendar", "2.0")))),
                                                        new DavProperty(
                                                                DavPropertyNames.SUPPORTED_CALENDAR_COMPONENT_SET,
                                                                new SupportedCalendarComponentSetValue(
                                                                        List.of(DavPropertyNames.VEVENT))),
                                                        new DavProperty(
                                                                DavPropertyNames.CALENDAR_SUPPORTED_COLLATION_SET,
                                                                new CollationSetValue(List.of("i;octet"))),
                                                        new DavProperty(
                                                                DavPropertyNames.CALENDAR_DATA,
                                                                new CalendarDataValue(
                                                                        "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nBEGIN:VEVENT\r\nUID:1\r\nEND:VEVENT\r\nEND:VCALENDAR\r\n"))))))));

        final String xml = writer.write(multistatus);

        assertXmlSimilar("""
                <d:multistatus xmlns:d="DAV:" xmlns:cal="urn:ietf:params:xml:ns:caldav">
                  <d:response>
                    <d:href>/services/caldav/calendars/koku/appointments/</d:href>
                    <d:propstat>
                      <d:prop>
                        <d:supported-report-set>
                          <d:supported-report>
                            <d:report>
                              <cal:calendar-multiget/>
                            </d:report>
                          </d:supported-report>
                          <d:supported-report>
                            <d:report>
                              <cal:calendar-query/>
                            </d:report>
                          </d:supported-report>
                        </d:supported-report-set>
                        <cal:supported-calendar-data>
                          <cal:calendar-data content-type="text/calendar" version="2.0"/>
                        </cal:supported-calendar-data>
                        <cal:supported-calendar-component-set>
                          <cal:comp name="VEVENT"/>
                        </cal:supported-calendar-component-set>
                        <cal:supported-collation-set>
                          <cal:supported-collation>i;octet</cal:supported-collation>
                        </cal:supported-collation-set>
                        <cal:calendar-data>BEGIN:VCALENDAR
VERSION:2.0
BEGIN:VEVENT
UID:1
END:VEVENT
END:VCALENDAR
</cal:calendar-data>
                      </d:prop>
                      <d:status>HTTP/1.1 200 OK</d:status>
                    </d:propstat>
                  </d:response>
                </d:multistatus>
                """, xml);
    }

    private void assertXmlSimilar(final String expected, final String actual) {
        final var diff = DiffBuilder.compare(expected)
                .withTest(actual)
                .ignoreWhitespace()
                .checkForSimilar()
                .build();
        assertThat(diff.hasDifferences()).as(diff.toString()).isFalse();
    }
}
