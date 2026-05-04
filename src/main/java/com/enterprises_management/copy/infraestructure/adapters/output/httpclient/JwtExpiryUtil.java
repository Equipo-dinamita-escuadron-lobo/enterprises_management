package com.enterprises_management.copy.infraestructure.adapters.output.httpclient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utilidad para inspeccionar el claim {@code exp} de un JWT sin dependencias externas.
 * Usado exclusivamente para logging preventivo de vencimiento en el saga engine (H2-R1).
 */
final class JwtExpiryUtil {

    private JwtExpiryUtil() {}

    /**
     * Devuelve los segundos restantes hasta el vencimiento del token.
     * Si el token está vencido, el valor es negativo.
     * Si el token es inválido o no tiene claim {@code exp}, devuelve {@code Long.MAX_VALUE}.
     */
    static long secondsUntilExpiry(String jwtToken) {
        if (jwtToken == null || jwtToken.isBlank()) return Long.MAX_VALUE;
        try {
            String[] parts = jwtToken.split("\\.");
            if (parts.length < 2) return Long.MAX_VALUE;
            byte[] payloadBytes = Base64.getUrlDecoder().decode(padBase64(parts[1]));
            String payload = new String(payloadBytes, StandardCharsets.UTF_8);
            long exp = extractLong(payload, "\"exp\"");
            if (exp < 0) return Long.MAX_VALUE;
            return exp - (System.currentTimeMillis() / 1000L);
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    private static String padBase64(String s) {
        return switch (s.length() % 4) {
            case 2 -> s + "==";
            case 3 -> s + "=";
            default -> s;
        };
    }

    private static long extractLong(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) return -1;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return -1;
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        return Long.parseLong(json.substring(start, end));
    }
}
