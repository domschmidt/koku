package de.domschmidt.dashboard.factory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DefaultDashboardViewContentIdGenerator implements IDashboardViewContentIdGenerator {

    Set<String> knownNames = new HashSet<>();

    public String generateUniqueId(
            final String initialProposal,
            final String prefix
    ) {
        String proposal = initialProposal;
        if (proposal == null) {
            proposal = prefix + "-" + UUID.randomUUID();
        } else if (knownNames.contains(proposal)) {
            throw new IllegalArgumentException("Proposal " + proposal + " already exists. If you specify your own id, please make sure to set them uniquely.");
        }

        String result = proposal;
        while (knownNames.contains(result)) {
            result = result + "1";
        }
        knownNames.add(result);
        return result;
    }

}
