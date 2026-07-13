package de.domschmidt.koku.dav.model;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.dav.DAVConstants;
import java.util.List;
import org.junit.jupiter.api.Test;

class DavResponseBuilderTest {

    @Test
    void buildsPropnameDiscoveryWithOnlySupportedPropertyNames() {
        final DavResponse response = new DavResponseBuilder("/addressbook/koku/")
                .property(DavPropertyNames.DISPLAYNAME, new TextValue("KoKu"))
                .property(DavPropertyNames.GETETAG, new TextValue("\"1\""))
                .build(
                        DavPropertyRequestType.NAMES_ONLY,
                        List.of(new DavPropertyName(DAVConstants.DAV_NAMESPACE, "ignored")));

        assertThat(response.propStats()).hasSize(1);
        assertThat(response.propStats().get(0).status()).isEqualTo(200);
        assertThat(response.propStats().get(0).properties())
                .extracting(DavProperty::name)
                .containsExactly(DavPropertyNames.DISPLAYNAME, DavPropertyNames.GETETAG);
        assertThat(response.propStats().get(0).properties())
                .extracting(DavProperty::value)
                .allMatch(EmptyValue.class::isInstance);
    }

    @Test
    void separatesRequestedUnsupportedPropertiesIntoNotFoundPropstat() {
        final DavPropertyName unsupported = new DavPropertyName(DAVConstants.DAV_NAMESPACE, "unsupported");

        final DavResponse response = new DavResponseBuilder("/addressbook/koku/")
                .property(DavPropertyNames.DISPLAYNAME, new TextValue("KoKu"))
                .build(DavPropertyRequestType.NAMED, List.of(DavPropertyNames.DISPLAYNAME, unsupported));

        assertThat(response.propStats()).hasSize(2);
        assertThat(response.propStats().get(0).status()).isEqualTo(200);
        assertThat(response.propStats().get(0).properties())
                .extracting(DavProperty::name)
                .containsExactly(DavPropertyNames.DISPLAYNAME);
        assertThat(response.propStats().get(1).status()).isEqualTo(404);
        assertThat(response.propStats().get(1).properties())
                .extracting(DavProperty::name)
                .containsExactly(unsupported);

        assertThat(new DavResponseBuilder("/default")
                        .property(DavPropertyNames.DISPLAYNAME, new TextValue("Default"))
                        .build((List<DavPropertyName>) null)
                        .propStats())
                .singleElement();
    }
}
