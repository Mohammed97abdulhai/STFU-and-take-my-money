package Core;

import Core.crypto.Cryptography;
import messages.Message;
import util.Util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;

import javax.crypto.KeyGenerator;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

      //  String temp = "suck my dick dudeN asdnwjasndjajd  awdnwndjwa nadwaskdn fsdffsff wetedgrDFHgRS";

        //Cryptography.Symmmetric crypt = new Cryptography.Symmmetric(16 , "AES" , "CBC" , "PKCS5PADDING");


        //ByteBuffer buffer = ByteBuffer.allocate(4 +1 /*+ 128*/);

        //buffer.putInt(69);
       // buffer.putInt(6);
       // buffer.put((byte)1);
      //  byte[] message =  Util.constructString(temp , 256);
        //buffer.put(message , 0 , message.length);
        //ByteBuffer buffer = Message.TransactionResponse.craft((byte)1 , message);

       // byte[] data = buffer.array();

        //String transform = new String(data, StandardCharsets.UTF_8);

        //System.out.println(data.length);
        //System.out.println(transform);




        //byte[] encryptedData = crypt.encrypt(data);


        //System.out.println(encryptedData.length);

        //String original_string = new String(encryptedData , StandardCharsets.UTF_8);




     /*   byte[] original = crypt.decrypt(encryptedData);

        try {
           Message.TransactionResponse msg = (Message.TransactionResponse) Message.parse(ByteBuffer.wrap(original));
            System.out.println(msg.getMessage());
            System.out.println(msg.getFlag());
            //System.out.println(msg.getAmount());
            //System.out.println(msg.getId());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        String original_string = new String(original , StandardCharsets.UTF_8);


       System.out.println(original.length);
       System.out.println(original_string);
*/
       /* String str = "eyad suck my dick u can't sing";
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

        SecureRandom secureRandom = new SecureRandom();
        int keyBitSize = 128;

        keyGenerator.init(keyBitSize , secureRandom);


        Key key = keyGenerator.generateKey();

        byte[] encrypted_text= Cryptography.encrypt(key , str);

        String data = new String(encrypted_text , StandardCharsets.UTF_8);

        System.out.println(data);
        byte[] decrypted_bytes = Cryptography.decrypt(key, data);

        String decrypted_string = new String(decrypted_bytes, StandardCharsets.UTF_8);
        System.out.println(decrypted_bytes);*/



      // Cryptography val =



        /*for (ClientModel clientModel : clients)
        {
            System.out.println(clientModel.getID() + " balance:" + clientModel.getBalance());
        }*/

        BankingServer server =new BankingServer(5000, 10);
        new Thread(server).start();


        try {
            Thread.sleep(100 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Stopping Server");
        server.stop();




/*
        ByteBuffer buffer = Message.ConnectionRequest.craft((int)5);
*/

        /*  ByteBuffer buffer = ByteBuffer.allocate(4 + 1 + 1+ 256);

        buffer.putInt(258);
        buffer.put((Message.Type.connectionResponse.getTypeByte()));
        buffer.put((byte)1);
        buffer.put(message , 0 , message.length);
        buffer.flip();*/


      /*  try {


           Message.ConnectionRequest mess =  (Message.ConnectionRequest) Message.parse(buffer);


          // System.out.println(mess.getFlag());
          // System.out.println(mess.getAmount());
          // System.out.println(mess.getMessage());
            System.out.println(mess.getId());

        }catch(ParseException e){
            System.out.println(e);
        }
*/






    }
}
