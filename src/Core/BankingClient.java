package Core;

import Core.crypto.Asymmetric;
import Core.crypto.DigitalSignature;
import Core.crypto.Symmetric;
import messages.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
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


        PublicKey remotePublickey = null;


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


        Scanner scanner = new Scanner(System.in);

        ByteBuffer readbuff = ByteBuffer.allocate(1024);

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


                        //print the message
                        int lastIndex = handshake.getMessage().indexOf(0);
                        String responseMessage = handshake.getMessage().substring(0, lastIndex);
                        System.out.println(responseMessage);


                        readbuff.clear();

                    }while(handshake.getFlag() == 1);


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


                        // get the message and the signature
                        byte[] message = Sendbuff.array();
                        byte[] signature= DigitalSignature.sign(message , privateKey);



                        //generate the session key
                        SecureRandom secureRandom = new SecureRandom();

                        byte[] key = new byte[16];
                        secureRandom.nextBytes(key);


                        byte[] encryptedKey = Asymmetric.encrypt(key , remotePublickey.getEncoded());


                        //for testing currently
                        byte[] messageWithSignature = new byte[signature.length + message.length];
                        System.arraycopy(signature , 0 , messageWithSignature , 0 , signature.length);
                        System.arraycopy(message , 0 , messageWithSignature , signature.length , message.length);

                        byte[] messageWithSignatureEncrypted = Symmetric.encrypt(messageWithSignature , key , Symmetric.iv);

                        System.out.println(messageWithSignatureEncrypted.length);

                        //testing ends




                        //concat the two messages to send
                        byte[] fullMessage = new byte[ encryptedKey.length + messageWithSignatureEncrypted.length ];
                        System.arraycopy(encryptedKey , 0 , fullMessage , 0 , encryptedKey.length);
                        System.arraycopy(messageWithSignatureEncrypted , 0 , fullMessage , encryptedKey.length , messageWithSignatureEncrypted.length);

                       /* System.arraycopy(encryptedKey , 0 , messageWithSignature , 0 , encryptedKey.length);
                        System.arraycopy(signature , 0  , messageWithSignature, encryptedKey.length , signature.length);
                        System.arraycopy(message , 0 , messageWithSignature , encryptedKey.length + signature.length , message.length);*/







                        socketChannel.write(ByteBuffer.wrap(fullMessage));




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
