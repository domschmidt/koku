package de.domschmidt.koku.dav.service;

import de.domschmidt.koku.dav.model.CollationSetValue;
import de.domschmidt.koku.dav.model.DavPropertyNames;
import de.domschmidt.koku.dav.model.PrivilegeSetValue;
import java.util.List;

final class DavCapabilities {

    private static final List<String> SUPPORTED_COLLATIONS = List.of("i;ascii-casemap", "i;octet", "i;unicode-casemap");

    private DavCapabilities() {}

    static PrivilegeSetValue readPrivileges() {
        return new PrivilegeSetValue(List.of(DavPropertyNames.READ_PRIVILEGE));
    }

    static CollationSetValue supportedCollations() {
        return new CollationSetValue(SUPPORTED_COLLATIONS);
    }
}
