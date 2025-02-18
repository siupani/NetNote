package client.utils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    // Helper method to compute file hash (SHA-256)
    public String computeFileHash(byte[] fileContent) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); // You can use MD5 or another algorithm
        byte[] hashBytes = digest.digest(fileContent); // Compute the hash
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString(); // Return the file hash as a string
    }

}
