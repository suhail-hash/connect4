package de.hsos.connectfour.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hilfsklasse zur sicheren Verarbeitung von Passwörtern.
 *
 * Diese Klasse stellt Methoden zur Generierung eines zufälligen Salts
 * sowie zur Berechnung eines SHA-256-Hashwertes (Salt + Passwort) bereit.
 * Sie dient der sicheren Speicherung von Passwörtern in der Datenbank.
 */
public class PasswordUtil {
    private static final int SALT_BYTES = 16;

    /**
     * Erzeugt ein kryptographisch sicheres, zufälliges Salt.
     *
     * @return Salt als Hex-String
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        new java.security.SecureRandom().nextBytes(salt);
        return bytesToHex(salt);
    }

    /**
     * Berechnet den SHA-256-Hash eines Passworts unter Verwendung
     * eines zuvor generierten Salts.
     *
     * Das Salt wird mit dem Passwort kombiniert und anschließend
     * gehasht. Das Ergebnis wird als Hex-String zurückgegeben.
     *
     * @param password Klartext-Passwort
     * @param saltHex  Salt als Hex-String
     * @return Hashwert (Hex-String)
     */
    public static String hashPassword(String password , String saltHex) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] saltBytes = hexToBytes(saltHex);
            byte[] pwdBytes = password.getBytes(StandardCharsets.UTF_8);
            // concat salt + password
            byte[] combined = new byte[saltBytes.length + pwdBytes.length];
            System.arraycopy(saltBytes, 0, combined, 0, saltBytes.length);
            System.arraycopy(pwdBytes, 0, combined, saltBytes.length, pwdBytes.length);
            byte[] digest = md.digest(combined);
            String hex = bytesToHex(digest);
            return hex;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No such hash algorithm", e);
        }
    }

    /**
     * Wandelt ein Byte-Array in einen Hex-String um.
     *
     * @param bytes Byte-Array
     * @return Hex-Repräsentation
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Wandelt einen Hex-String in ein Byte-Array um.
     *
     * @param hex Hex-String
     * @return entsprechendes Byte-Array
     */
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return out;
    }
}
