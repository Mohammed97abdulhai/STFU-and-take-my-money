package Core.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Asymmetric {

    static public String algorithm = "RSA";

    public static byte[] encrypt(byte[] data, byte[] publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);

            PublicKey pkey = null;
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
            
            try {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                pkey = keyFactory.generatePublic(keySpec);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }


            cipher.init(Cipher.ENCRYPT_MODE, pkey);

            return cipher.doFinal(data);

        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] decrypt(byte[] data, PrivateKey privateKey){
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(algorithm);

            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            return cipher.doFinal(data);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return null;
    }
}
