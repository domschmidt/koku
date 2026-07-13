package de.domschmidt.koku.customer.transformer;

import de.domschmidt.koku.customer.persistence.Customer;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

final class CustomerNameFormatter {
    private static final String PART_SEPARATOR = " ";
    private static final String ON_FIRSTNAME_BASIS_MARKER = "*";

    private CustomerNameFormatter() {}

    static String displayName(final Customer customer) {
        return joinParts(customer.getFirstname(), customer.getLastname());
    }

    static String displayNameWithFirstnameBasisMarker(final Customer customer) {
        final String displayName = displayName(customer);
        if (!customer.isOnFirstnameBasis()) {
            return displayName;
        }
        return joinParts(displayName, ON_FIRSTNAME_BASIS_MARKER);
    }

    static String initials(final Customer customer) {
        return Arrays.stream(new String[] {customer.getFirstname(), customer.getLastname()})
                .filter(Objects::nonNull)
                .filter(part -> !part.isEmpty())
                .map(part -> part.substring(0, 1))
                .collect(Collectors.joining());
    }

    private static String joinParts(final String... parts) {
        return Arrays.stream(parts)
                .filter(Objects::nonNull)
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(PART_SEPARATOR));
    }
}
