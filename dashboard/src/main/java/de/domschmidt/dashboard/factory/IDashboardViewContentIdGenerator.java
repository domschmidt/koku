package de.domschmidt.dashboard.factory;

public interface IDashboardViewContentIdGenerator {

    String generateUniqueId(
            final String initialProposal,
            final String prefix
    );

}
