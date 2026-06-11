package de.domschmidt.koku.dav.model;

import java.util.List;

public record SupportedAddressDataValue(List<AddressDataType> addressDataTypes) implements DavPropertyValue {}
