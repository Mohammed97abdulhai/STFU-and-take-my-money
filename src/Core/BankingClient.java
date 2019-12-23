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
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Scanner;

import static util.Util.constructString;

public class BankingClient {




    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, ParseException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        int selfId = 0;
        int choice = 0;

        boolean closed = false;
        boolean cipherMode = false;
        boolean symmetricOnly = true;

        byte[] pukeyBytes;

        PublicKey publicKey ;
        PrivateKey privateKey;


        PublicKey remotePublickey;


        // byte[] secretKey = null;


        //opening a socket channel to the server at the specified port
        SocketChannel socketChannel =   SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1" , 5000));
        socketChannel.configureBlocking(true);


        //generate public and private keys (should be stored using Keystore in a file and later retreived!)
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        keygen.initialize(1024);
        KeyPair pair = keygen.generateKeyPair();
        publicKey = pair.getPublic();
        privateKey = pair.getPrivate();

        //getting the bytes from the public key to be sent across the network
        pukeyBytes = publicKey.getEncoded();
       // System.out.println(pukeyBytes.length);
        System.out.println(new String(pukeyBytes , StandardCharsets.UTF_8));


        Scanner scanner = new Scanner(System.in);

        ByteBuffer readbuff  = ByteBuffer.allocate(1024);

        while(!closed) {

            System.out.println("Enter Choice : \nTransfer(1)\nClose(2) ");
            choice = scanner.nextInt();

            switch (choice){


                case 1:


                    Message.ConnectionResponse handshake = null;

                    //1) handle connection request and response

                    do {

                        //ask for an id
                        System.out.println("Enter your ID number");
                        selfId = scanner.nextInt();

                        //send a connection request
                        ByteBuffer sendBuff = Message.ConnectionRequest.craft(selfId, pukeyBytes);
                        socketChannel.write(sendBuff);

                        //receive a connection response
                        socketChannel.read(readbuff);
                        readbuff.flip();

                        //parse the received message
                        handshake = (Message.ConnectionResponse) Message.parse(readbuff);


                        //get an object from the public key bytes
                        if(handshake.getFlag() ==0){
                            remotePublickey =
                                    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(handshake.getPublicKey()));
                        }

                        System.out.println(new String (handshake.getPublicKey() , StandardCharsets.UTF_8));

                        //print the message
                        int lastIndex = handshake.getMessage().indexOf(0);
                        String responseMessage = handshake.getMessage().substring(0, lastIndex);
                        System.out.println(responseMessage);


                        readbuff.clear();

                    }while(handshake.getFlag() == 1);


                    System.out.println("Connected!!");




                    //2) transfer request and response


                    Message.TransactionResponse transactionResponse = null;

                    do {

                        int receiverId;
                        double amount;
                        String reason;

                        //read relevant data from the user

                        System.out.println("Enter receiver ID : ");
                        receiverId = scanner.nextInt();

                        System.out.println("Enter Amount : ");
                        amount = scanner.nextDouble();
                        scanner.nextLine();

                        System.out.println("Enter reason : ");
                        reason = scanner.nextLine();



                        //send a transaction request to the user
                        ByteBuffer Sendbuff = Message.TransactionRequest.craft(receiverId , amount , constructString(reason,256));

                        byte[] message = Sendbuff.array();
                        System.out.println(new String(message , StandardCharsets.UTF_8));
                        byte[] signature= Symmetric.sign(message , privateKey);


                        //concat the two messagaes to send
                        byte[] messageWithSignature = new byte[message.length + signature.length];
                        System.arraycopy(signature , 0  , messageWithSignature, 0 , signature.length);
                        System.arraycopy(message , 0 , messageWithSignature , signature.length , message.length);



                        System.out.println(signature.length);



                        socketChannel.write(ByteBuffer.wrap(messageWithSignature));




                        //receive a transaction response
                        socketChannel.read(readbuff);
                        readbuff.flip();



                        //parse the received message
                        transactionResponse = (Message.TransactionResponse) Message.parse(readbuff);

                        //and print the message
                        int lastIndex = transactionResponse.getMessage().indexOf(0);
                        String responseMessage = transactionResponse.getMessage().substring(0, lastIndex);
                        System.out.println(responseMessage);


                        readbuff.clear();


                    }while(transactionResponse.getFlag() == 1);


                    break;


                case 2:
                    System.out.println("2");
                    closed = true;
                    break;

                default:
                    System.out.println("invalid Choice!\n");


            }


        }




        System.out.println("finished Writing exiting....");

        socketChannel.close();
    }


}
