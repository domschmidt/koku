package de.domschmidt.formular.factory;

public enum FormOutlet {
    CONTENT("content"),
    PREPEND_OUTER("prependOuter"),
    PREPEND_INNER("prependInner"),
    APPEND_INNER("appendInner"),
    APPEND_OUTER("appendOuter");

    private final String name;

    FormOutlet(final String name) {
        this.name = name;
    }

    public String outletName() {
        return this.name;
    }
}
