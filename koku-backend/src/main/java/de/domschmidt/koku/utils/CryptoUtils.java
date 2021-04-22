package de.domschmidt.koku.utils;

import io.jsonwebtoken.security.Keys;

import java.security.Key;

public final class CryptoUtils {

    private CryptoUtils() {
    }

    public static Key getPublicKey(final String jwtSecret) {
        Key signingKey;
        if (jwtSecret != null && !jwtSecret.isEmpty()) {
            signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        } else {
            throw new IllegalArgumentException("JWT Signature key undefined");
        }
        return signingKey;
    }

    public static Key getPrivateKey(final String jwtSecret) {
        Key signingKey;
        if (jwtSecret != null && !jwtSecret.isEmpty()) {
            signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        } else {
            throw new IllegalArgumentException("JWT Signature key undefined");
        }
        return signingKey;
    }
}