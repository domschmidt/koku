package de.domschmidt.dashboard.factory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DefaultDashboardViewContentIdGenerator implements IDashboardViewContentIdGenerator {

    Set<String> knownNames = new HashSet<>();

    public String generateUniqueId(final String initialProposal, final String prefix) {
        String proposal = initialProposal;
        if (proposal == null) {
            proposal = prefix + "-" + UUID.randomUUID();
        } else if (knownNames.contains(proposal)) {
            throw new IllegalArgumentException(String.format(
                    "Proposal %s already exists. If you specify your own id, please make sure to set them uniquely.",
                    proposal));
        }

        final StringBuilder result = new StringBuilder(proposal);
        while (knownNames.contains(result.toString())) {
            result.append('1');
        }
        knownNames.add(result.toString());
        return result.toString();
    }
}
