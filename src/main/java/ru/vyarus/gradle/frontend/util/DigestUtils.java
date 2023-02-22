package ru.vyarus.gradle.frontend.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author Vyacheslav Rusakov
 * @since 22.02.2023
 */
public class DigestUtils {

    /**
     * Parse SRI token (integrity attribute in script and link tags). Token consists of two parts: encoding
     * algorithm (hash) base64 encoded token.
     *
     * @param integrity token
     * @return parsed token object
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity">docs</a>
     */
    public static SriToken parseSri(final String integrity) {
        final int idx = integrity.indexOf('-');
        if (idx <= 0) {
            throw new IllegalStateException("Invalid SRI token: " + integrity);
        }
        String alg = integrity.substring(0, idx);
        // convert to canonical algorithm name (suitable for default MessageDigest)
        if (!alg.contains("-") && alg.toLowerCase().startsWith("sha")) {
            alg = "SHA-" + alg.substring(3);
        }
        String token = integrity.substring(idx + 1);
        return new SriToken(alg, Base64.getDecoder().decode(token));
    }

    public static boolean validateSriToken(final File file, final String integrity) {
        final SriToken token = parseSri(integrity);
        byte[] hash = hash(file, token.getAlg());
        return Arrays.equals(token.getToken(), hash);
    }

    public static String buildSri(final File file, final String alg) {
        byte[] hash = hash(file, alg);
        String res = Base64.getEncoder().encodeToString(hash);
        return alg.replace("-", "").toLowerCase() + "-" + res;
    }

    public static byte[] hash(final File file, final String alg) {
        byte[] data;
        try {
            data = Files.readAllBytes(file.toPath());
            return MessageDigest.getInstance(alg).digest(data);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate " + alg + " hash for file " + file.getAbsolutePath(), e);
        }
    }

    public static class SriToken {
        private final String alg;
        private final byte[] token;

        public SriToken(final String alg, final byte[] token) {
            this.alg = alg;
            this.token = token;
        }

        public String getAlg() {
            return alg;
        }

        public byte[] getToken() {
            return token;
        }

        public String getTokenString() {
            return new String(token, StandardCharsets.UTF_8);
        }

        @Override
        public String toString() {
            return alg + ": " + getTokenString();
        }
    }
}
