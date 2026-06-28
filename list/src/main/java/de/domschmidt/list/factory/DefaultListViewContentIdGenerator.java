package de.domschmidt.list.factory;

import java.util.HashSet;
import java.util.Set;

public class DefaultListViewContentIdGenerator implements IListViewContentIdGenerator {

    Set<String> knownNames = new HashSet<>();

    public String generateUniqueId(final String proposal) {
        if (knownNames.contains(proposal)) {
            throw new IllegalArgumentException(String.format(
                    "Proposal %s already exists. If you specify your own id, please make sure to set them uniquely.",
                    proposal));
        }
        knownNames.add(proposal);
        return proposal;
    }
}
