package Core;

import messages.Message;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class BankingClient {

    public static void main(String[] args) throws IOException {

        SocketChannel socketChannel =   SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1" , 5000));

        socketChannel.configureBlocking(true);

        Scanner scanner = new Scanner(System.in);

        while(scanner.hasNextLine()){

            String line = scanner.nextLine();

            if(line.startsWith("ok")){

                ByteBuffer Sendbuffer = Message.TransactionRequest.craft(10 , 20.0 , constructString("hey ichigo"));



                socketChannel.write(Sendbuffer);

            }
            else{

                System.out.println("write ok u nigger");
            }

            buffer.clear();
            System.out.println(value);
        }



        System.out.println("finished Writing exiting....");



        socketChannel.close();

    }
    static private byte[] constructString(String string){

        byte[] message= new byte[256];

        byte[] temp = string.getBytes(StandardCharsets.UTF_8);

        for(int i = 0; i < temp.length; i++){
            message[i] = temp[i];
        }

        return message;

    }

}
