package de.domschmidt.list.factory;

import java.util.HashSet;
import java.util.Set;

public class DefaultListViewContentIdGenerator implements IListViewContentIdGenerator {

    Set<String> knownNames = new HashSet<>();

    public String generateUniqueId(
            final String proposal
    ) {
        if (knownNames.contains(proposal)) {
            throw new IllegalArgumentException("Proposal " + proposal + " already exists. If you specify your own id, please make sure to set them uniquely.");
        }
        knownNames.add(proposal);
        return proposal;
    }

}
