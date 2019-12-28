package Core.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Asymmetric {

    static public String algorithm = "RSA";

    public static byte[] encrypt(byte[] data, byte[] publicKey) {
        return encrypt(data, rebuildPublicKey(publicKey));
    }

    public static byte[] encrypt(byte[] data, PublicKey publicKey){
        try{
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);

        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
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

    public static PublicKey rebuildPublicKey(byte[] keyBytes){
        PublicKey publicKey = null;
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

            publicKey = keyFactory.generatePublic(keySpec);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            System.out.println("Failed to rebuild key from bytes");
        }
        return publicKey;
    }

    public static PrivateKey rebuildPrivateKey(byte[] keyBytes){
        PrivateKey privateKey = null;
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

            privateKey = keyFactory.generatePrivate(keySpec);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            System.out.println("Failed to rebuild key from bytes");
        }
        return privateKey;
    }

}
