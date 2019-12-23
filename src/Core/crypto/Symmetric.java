package Core.crypto;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.management.openmbean.InvalidKeyException;
import java.security.*;

public class Symmetric {

    public static final byte[] key = {1, 2 , 3, 4, 5, 6, 7, 8, 9, 10 ,11, 12, 13, 14, 15, 16};

    public static final  byte[] iv =  {17 ,18 ,19 ,20 , 21 ,22 , 23, 24 ,25 ,26, 27, 28, 29  , 30 , 31, 32};


    /* private byte[] priv_key;*/


    //rivate byte[] iv;

    static public String algorithm = "AES";
    static public String mode = "CBC";
    static public String paddingMode =  "PKCS5PADDING";

    static private SecureRandom secureRandom;

       /*  public symmetric(byte[] key , String algorithm , String mode , String paddingMode){

             this.secureRandom = new SecureRandom();

             this.priv_key = key;

             this.algorithm = algorithm;
             this.mode  = mode;
             this.paddingMode = paddingMode;


             //this.iv = new byte[16];
             //secureRandom.nextBytes(this.iv);

         }

         public  symmetric(int keySize ,String algorithm , String mode , String paddingMode ){

             this.secureRandom = new SecureRandom();

             this.priv_key = new byte[keySize];
             this.secureRandom.nextBytes(key);


             this.algorithm = algorithm;
             this.mode = mode;
             this.paddingMode = paddingMode;


             //this.iv = new byte[16];
             //this.iv =




         }*/


    public static byte[] encrypt(byte[] data , byte[] key , byte[] iv) {



        try {

            IvParameterSpec iv_spec = new IvParameterSpec(iv);
            SecretKeySpec key_spec = new SecretKeySpec(key, Symmetric.algorithm);



            Cipher cipher = Cipher.getInstance(String.join("/" , Symmetric.algorithm , Symmetric.mode , Symmetric.paddingMode));
            cipher.init(Cipher.ENCRYPT_MODE , key_spec , iv_spec);

            byte[]  encryptedData = cipher.doFinal(data);

            return encryptedData;


        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | java.security.InvalidKeyException e) {

            e.printStackTrace();
        }

        return null;

    }


    public static byte[] decrypt(byte[] data , byte[] key , byte[] iv) {

            /*this.iv = new byte[16];
            secureRandom.nextBytes(this.iv);*/


        try {

            IvParameterSpec iv_spec = new IvParameterSpec(iv);
            SecretKeySpec key_spec = new SecretKeySpec(key , Symmetric.algorithm);



            Cipher cipher = Cipher.getInstance(String.join("/" , Symmetric.algorithm, Symmetric.mode , Symmetric.paddingMode));
            cipher.init(Cipher.DECRYPT_MODE , key_spec , iv_spec);

            byte[]  originalData = cipher.doFinal(data);

            return originalData;


        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | java.security.InvalidKeyException e) {

            e.printStackTrace();
        }


        return null;

    }


    public static byte[] sign(byte[] message , PrivateKey privateKey) throws NoSuchAlgorithmException, java.security.InvalidKeyException, SignatureException {


        Signature signatureInstance = Signature.getInstance("SHA256withRSA");
        signatureInstance.initSign(privateKey);
        signatureInstance.update(message);


        byte[] signature = signatureInstance.sign();

        return signature;

    }


    public  static boolean verify(byte[] message , byte[] signature , PublicKey key) throws NoSuchAlgorithmException, java.security.InvalidKeyException, SignatureException {

        Signature signatureInstance = Signature.getInstance("SHA256withRSA");
        signatureInstance.initVerify(key);
        signatureInstance.update(message);


        return signatureInstance.verify(signature);

    }







}

  /*  class ASymmetric implements Cryptography{


        @Override
        public byte[] encrypt(String data) {
            return new byte[0];
        }

        @Override
        public byte[] decrypt(byte[] data) {
            return new byte[0];
        }
    }

*/







  /* static public byte[] encrypt(Key key ,String data)  {


       try {
           Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
           cipher.init(Cipher.ENCRYPT_MODE , key);

           byte[] plainText = data.getBytes(StandardCharsets.UTF_8);

           return cipher.doFinal(plainText);


       } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException e) {
           e.printStackTrace();
       }

       return null;

   }

    static public   byte[] decrypt(Key key , String data){

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE , key);

            byte[] plainText = data.getBytes(StandardCharsets.UTF_8);

            return cipher.doFinal(plainText);


        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        return null;


    }
*/


