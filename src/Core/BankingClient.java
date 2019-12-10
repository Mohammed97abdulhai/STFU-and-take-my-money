package Core;

import Core.crypto.Cryptography;
import messages.Message;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Scanner;

import static util.Util.constructString;

public class BankingClient {

    public static void main(String[] args) throws IOException {

        int selfId = 1;


        Cryptography.Symmmetric crypt = new Cryptography.Symmmetric(16 , "AES" , "CBC" , "PKCS5PADDING");


        SocketChannel socketChannel =   SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1" , 5000));
        socketChannel.configureBlocking(true);

        Scanner scanner = new Scanner(System.in);

        ByteBuffer readbuff  = ByteBuffer.allocate(1024);

        while(scanner.hasNextLine()){

            String line = scanner.nextLine();

            if(line.startsWith("t")){

                ByteBuffer Sendbuffer = Message.TransactionRequest.craft(20 , 20.0 , constructString("hey ichigo",256));

                socketChannel.write(Sendbuffer);




                socketChannel.read(readbuff);

                readbuff.flip();


                try {
                    handleGeneralMessage(Message.parse(readbuff));



                }catch (ParseException e){
                    System.out.println(e);
                }


                readbuff.clear();



            }
            else if(line.startsWith("c")){



                ByteBuffer Sendbuffer = Message.ConnectionRequest.craft(selfId);

                socketChannel.write(Sendbuffer);




                socketChannel.read(readbuff);

                readbuff.flip();


                try {
                    Message.ConnectionResponse msg = (Message.ConnectionResponse) Message.parse(readbuff);

                    System.out.println(msg.getMessage());



                }catch (ParseException e){
                    System.out.println(e);
                }


                readbuff.clear();



            }
            else{

                System.out.println("write ok u nigger");
            }
        }



        System.out.println("finished Writing exiting....");



        socketChannel.close();

    }


    private static void handleGeneralMessage(Message message){
        switch (message.getType()){

            case connectionResponse:
                Message.ConnectionResponse cMessage = (Message.ConnectionResponse)message;
                System.out.println(cMessage.getMessage());
                break;


            case transferResponse:
                Message.TransactionResponse tMessage = (Message.TransactionResponse)message;
                System.out.println(tMessage.getMessage());
                break;



        }


    }


}
