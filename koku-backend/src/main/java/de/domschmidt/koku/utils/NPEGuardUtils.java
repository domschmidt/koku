package de.domschmidt.koku.utils;

import java.math.BigDecimal;

public final class NPEGuardUtils {

    private NPEGuardUtils() {
    }

    public static BigDecimal get(final BigDecimal value, BigDecimal defaultValue) {
        BigDecimal result = defaultValue;
        if (value != null) {
            result = value;
        }
        return result;
    }

    public static BigDecimal get(final BigDecimal value) {
        return NPEGuardUtils.get(value, BigDecimal.ZERO);
    }
}
