package ru.vyarus.gradle.frontend.core.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Hash utilities.
 *
 * @author Vyacheslav Rusakov
 * @since 22.02.2023
 */
public final class DigestUtils {

    private DigestUtils() {
    }

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
        final String token = integrity.substring(idx + 1);
        return new SriToken(alg, Base64.getDecoder().decode(token));
    }

    /**
     * Computes file integrity hash with the same algorithm as specified in integrity string (attribute) and
     * compares with provided hash.
     *
     * @param file      file to check integrity
     * @param integrity integrity string
     * @return true if validation token correct (from integrity), false otherwise
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity">docs</a>
     */
    public static boolean validateSriToken(final File file, final String integrity) {
        final SriToken token = parseSri(integrity);
        final byte[] hash = hash(file, token.getAlg());
        return Arrays.equals(token.getToken(), hash);
    }

    /**
     * @param file file to build integrity token for
     * @param alg  token algorithm (e.g. SHA-384)
     * @return computed integrity string
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Security/Subresource_Integrity">docs</a>
     */
    public static String buildSri(final File file, final String alg) {
        final byte[] hash = hash(file, alg);
        final String res = Base64.getEncoder().encodeToString(hash);
        return alg.replace("-", "").toLowerCase() + "-" + res;
    }

    /**
     * Build hash with specified algorithm for provided file (not complete SRI token!).
     *
     * @param file file to build hash for
     * @param alg  hash algorithm
     * @return hash bytes (better for further manipulations, comparing to pure string)
     */
    public static byte[] hash(final File file, final String alg) {
        final byte[] data;
        try {
            data = Files.readAllBytes(file.toPath());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read file " + file.getAbsolutePath(), e);
        }
        try {
            return MessageDigest.getInstance(alg).digest(data);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate " + alg + " hash for file "
                    + file.getAbsolutePath(), e);
        }
    }

    /**
     * SRI token object.
     */
    public static class SriToken {
        private final String alg;
        private final byte[] token;

        public SriToken(final String alg, final byte[] token) {
            this.alg = alg;
            this.token = token;
        }

        /**
         * @return token encoding algorithm
         */
        public String getAlg() {
            return alg;
        }

        /**
         * @return token bytes
         */
        @SuppressWarnings("PMD.MethodReturnsInternalArray")
        public byte[] getToken() {
            return token;
        }

        /**
         * For logging purposes.
         *
         * @return token in string form
         */
        public String getTokenString() {
            return new String(token, StandardCharsets.UTF_8);
        }

        @Override
        public String toString() {
            return alg + ": " + getTokenString();
        }
    }
}
