package de.domschmidt.koku.business_logic.dto;

public enum KokuBusinessRuleFieldReferenceListenerEventEnum {
    CHANGE,
    INPUT,
    CLICK,
    BLUR,
    FOCUS,

    CLICK_PREPEND_OUTER,
    BLUR_PREPEND_OUTER,
    FOCUS_PREPEND_OUTER,

    CLICK_PREPEND_INNER,
    BLUR_PREPEND_INNER,
    FOCUS_PREPEND_INNER,

    CLICK_APPEND_INNER,
    BLUR_APPEND_INNER,
    FOCUS_APPEND_INNER,

    CLICK_APPEND_OUTER,
    BLUR_APPEND_OUTER,
    FOCUS_APPEND_OUTER,

    INIT,
    REINIT,
}
