package Core;

import Core.crypto.Asymmetric;
import Core.crypto.Symmetric;
import messages.Message;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Scanner;

import static util.Util.constructString;

public class BankingClient {

    public static void main(String[] args) throws IOException {

        int selfId = 0;
        boolean cipherMode = false;
        boolean symmetricOnly = true;

        byte[] publicKey;

        byte[] secretKey = null;

        SocketChannel socketChannel =   SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1" , 5000));
        socketChannel.configureBlocking(true);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your ID number");
        selfId = scanner.nextInt();
        scanner.nextLine();

        ByteBuffer readbuff  = ByteBuffer.allocate(1024);

        if(symmetricOnly){
            secretKey = Symmetric.key;
            cipherMode = true;
        }

        while(scanner.hasNextLine()){

            String line = scanner.nextLine();

            if(line.startsWith("t")){
                int receiverId;
                double amount;
                String reason;

                System.out.println("Enter receiver ID : ");
                receiverId = scanner.nextInt();

                System.out.println("Enter Amount : ");
                amount = scanner.nextDouble();
                scanner.nextLine();

                System.out.println("Enter reason : ");
                reason = scanner.nextLine();

                ByteBuffer Sendbuffer = Message.TransactionRequest.craft(receiverId , amount , constructString(reason,256));

                //encrypt outgoing data
                if(cipherMode){
                    byte[] plainText = Sendbuffer.array();
                    byte[] cipherText = Symmetric.encrypt(plainText, secretKey, Symmetric.iv);
                    socketChannel.write(ByteBuffer.wrap(cipherText));

                }
                else{
                    socketChannel.write(Sendbuffer);
                }

                socketChannel.read(readbuff);

                readbuff.flip();

                Message message = null;

                //decrypt incoming data
                try{
                    if(cipherMode){
                        byte[] cipherText = new byte[readbuff.remaining()];
                        readbuff.get(cipherText);
                        byte[] plainText = Symmetric.decrypt(cipherText, secretKey, Symmetric.iv);
                        message = Message.parse(ByteBuffer.wrap(plainText));
                    }
                    else{
                        message = Message.parse(readbuff);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //handle message
                Message.TransactionResponse msg = (Message.TransactionResponse) message;
                byte flag = msg.getFlag();

                int lastIndex = msg.getMessage().indexOf(0);
                String responseMessage = msg.getMessage().substring(0, lastIndex);
                System.out.println(responseMessage);

                readbuff.clear();

            }
            else if(line.startsWith("c")){

                ByteBuffer Sendbuffer = Message.ConnectionRequest.craft(selfId);

                if(cipherMode){
                    byte[] plainText = Sendbuffer.array();
                    byte[] cipherText = Symmetric.encrypt(plainText, secretKey, Symmetric.iv);
                    socketChannel.write(ByteBuffer.wrap(cipherText));
                }
                else{
                    socketChannel.write(Sendbuffer);
                }


                socketChannel.read(readbuff);

                readbuff.flip();

                Message message = null;

                //decrypt incoming data
                try{
                    if(cipherMode){
                        byte[] cipherText = new byte[readbuff.remaining()];
                        readbuff.get(cipherText);
                        byte[] plainText = Symmetric.decrypt(cipherText, secretKey, Symmetric.iv);
                        message = Message.parse(ByteBuffer.wrap(plainText));
                    }
                    else{
                        message = Message.parse(readbuff);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                Message.ConnectionResponse msg = (Message.ConnectionResponse) message;

                if(msg.getFlag() == 0 && !cipherMode){


                    SecureRandom rand = new SecureRandom();

                    secretKey = new byte[16];

                    rand.nextBytes(secretKey);

                    ByteBuffer textbuff = Message.KeyExchange.craft(secretKey);

                    byte[] plainText = textbuff.array();

                    publicKey = msg.getPublicKey();

                    byte[] cipherText = Asymmetric.encrypt(plainText, publicKey);

                    socketChannel.write(ByteBuffer.wrap(cipherText));

                    cipherMode = true;
                }

                int lastIndex = msg.getMessage().indexOf(0);
                String responseMessage = msg.getMessage().substring(0, lastIndex);
                System.out.println(responseMessage);

                readbuff.clear();
            }
            else{
                System.out.println("invalid choide");
            }
        }

        System.out.println("finished Writing exiting....");

        socketChannel.close();
    }


}
