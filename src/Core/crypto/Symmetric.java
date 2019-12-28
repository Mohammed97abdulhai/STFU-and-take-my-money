package Core.crypto;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class Symmetric {

    public static final byte[] key = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

    public static final byte[] iv = {17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};

    static public String algorithm = "AES";
    static public String mode = "CBC";
    static public String paddingMode = "PKCS5PADDING";

    static private SecureRandom secureRandom;


    public static byte[] encrypt(byte[] data, byte[] key, byte[] iv) {

        try {

            IvParameterSpec iv_spec = new IvParameterSpec(iv);
            SecretKeySpec key_spec = new SecretKeySpec(key, Symmetric.algorithm);

            Cipher cipher = Cipher.getInstance(String.join("/", Symmetric.algorithm, Symmetric.mode, Symmetric.paddingMode));
            cipher.init(Cipher.ENCRYPT_MODE, key_spec, iv_spec);

            byte[] encryptedData = cipher.doFinal(data);

            return encryptedData;

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            System.out.println("Symmertric Encryption Failure");
            e.printStackTrace();
        }

        return null;

    }


    public static byte[] decrypt(byte[] data, byte[] key, byte[] iv) {

        try {
            IvParameterSpec iv_spec = new IvParameterSpec(iv);
            SecretKeySpec key_spec = new SecretKeySpec(key, Symmetric.algorithm);

            Cipher cipher = Cipher.getInstance(String.join("/", Symmetric.algorithm, Symmetric.mode, Symmetric.paddingMode));
            cipher.init(Cipher.DECRYPT_MODE, key_spec, iv_spec);

            byte[] originalData = cipher.doFinal(data);

            return originalData;

        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            System.out.println("Symmetric Decryption Failure");
            e.printStackTrace();
        }

        return null;
    }

}

