package util;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyStorage {

    public static void saveKey(Key key, String filepath){

        try{
            byte[] keyBytes = key.getEncoded();

            File keyFile = new File(filepath);
            if(!keyFile.exists() || keyFile.isDirectory()){
                keyFile.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(keyFile);
            out.write(keyBytes);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static PublicKey loadPublicKey(String filepath){

        PublicKey publicKey = null;
        try{
            File pubkeyFile = new File(filepath);
            if(pubkeyFile.exists() && !pubkeyFile.isDirectory()){
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");

                FileInputStream in = new FileInputStream(pubkeyFile);
                byte[] pubBytes = new byte[(int)pubkeyFile.length()];
                in.read(pubBytes, 0, (int)pubkeyFile.length());
                in.close();

                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubBytes);
                publicKey = keyFactory.generatePublic(keySpec);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;

    }


    public static PrivateKey loadPrivateKey(String filepath){

        PrivateKey privateKey = null;
        try{
            File privKeyFile = new File(filepath);
            if(privKeyFile.exists() && !privKeyFile.isDirectory()){
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");

                FileInputStream in = new FileInputStream(privKeyFile);
                byte[] pubBytes = new byte[(int)privKeyFile.length()];
                in.read(pubBytes, 0, (int)privKeyFile.length());
                in.close();

                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pubBytes);
                privateKey = keyFactory.generatePrivate(keySpec);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return privateKey;

    }

}
