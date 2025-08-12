package Utils;


import java.security.MessageDigest;
import java.util.HexFormat;

public class HashUtils {

    public static String hashKeySha256(String keyToHash){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keyToHash.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing", e);
        }

    }

}
