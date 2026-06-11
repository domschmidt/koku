package de.domschmidt.koku.carddav.model;

import java.util.List;

public record SupportedAddressDataValue(List<AddressDataType> addressDataTypes) implements DavPropertyValue {}
