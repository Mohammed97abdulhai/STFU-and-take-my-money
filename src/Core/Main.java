package Core;

import com.sun.xml.internal.bind.api.impl.NameConverter;
import messages.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

public class Main {

    public static void main(String[] args) throws IOException {


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
