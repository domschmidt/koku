package de.domschmidt.formular.factory;

public interface IFormViewContentIdGenerator {

    String generateUniqueId(
            final String initialProposal,
            final String prefix
    );

}
