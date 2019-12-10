package Core.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public interface Cryptography {


     byte[] encrypt(byte[] data);


      byte[] decrypt(byte[] data);




     class Symmmetric implements Cryptography{

         private byte[] key;
         private byte[] iv;

         private String algorithm;
         private String mode;
         private String paddingMode;

         private SecureRandom secureRandom;

         public Symmmetric(byte[] key , String algorithm , String mode , String paddingMode){

             this.secureRandom = new SecureRandom();

             this.key = key;

             this.algorithm = algorithm;
             this.mode  = mode;
             this.paddingMode = paddingMode;

         }

         public  Symmmetric(int keySize ,String algorithm , String mode , String paddingMode ){

             this.secureRandom = new SecureRandom();

             this.key = new byte[keySize];
             this.secureRandom.nextBytes(key);


             this.algorithm = algorithm;
             this.mode = mode;
             this.paddingMode = paddingMode;






         }

        @Override
        public byte[] encrypt(byte[] data) {

             this.iv = new byte[16];
             secureRandom.nextBytes(this.iv);

            try {

                IvParameterSpec iv_spec = new IvParameterSpec(this.iv);
                SecretKeySpec key_spec = new SecretKeySpec(this.key , this.algorithm);



                Cipher cipher = Cipher.getInstance(String.join("/" , this.algorithm , this.mode , this.paddingMode));
                cipher.init(Cipher.ENCRYPT_MODE , key_spec , iv_spec);

                byte[]  encryptedData = cipher.doFinal(data/*.getBytes(StandardCharsets.UTF_8)*/);

                return encryptedData;


            } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {

                e.printStackTrace();
            }

            return null;

        }

        @Override
        public byte[] decrypt(byte[] data) {

            /*this.iv = new byte[16];
            secureRandom.nextBytes(this.iv);*/


            try {

                IvParameterSpec iv_spec = new IvParameterSpec(this.iv);
                SecretKeySpec key_spec = new SecretKeySpec(this.key , this.algorithm);



                Cipher cipher = Cipher.getInstance(String.join("/" , this.algorithm , this.mode , this.paddingMode));
                cipher.init(Cipher.DECRYPT_MODE , key_spec , iv_spec);

                byte[]  originalData = cipher.doFinal(data);

                return originalData;


            } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {

                e.printStackTrace();
            }


            return null;

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

}
