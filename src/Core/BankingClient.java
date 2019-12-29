package Core;

import Core.crypto.Asymmetric;
import Core.crypto.DigitalCertificate;
import Core.crypto.DigitalSignature;
import Core.crypto.Symmetric;
import messages.Message;
import util.KeyStorage;

import java.io.IOException;
import java.net.ConnectException;
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

        SocketChannel socketChannel = null;

        while(!closed) {
            //opening a socket channel to the server at the specified port
            try{
                socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress("127.0.0.1" , 5000));
                socketChannel.configureBlocking(true);
            }
            catch(ConnectException e){
                System.out.println("Banking Server Seems to be offline...");
                break;
            }


            System.out.println("Enter Choice : \nTransfer(1)\nClose(2) ");
            choice = scanner.nextInt();

            switch (choice){

                case 1:
                    //Message.ConnectionResponse handshake = null;
                    Message.CertResponse handshake = null;
                    boolean validCertReceived = false;

                    //1) handle connection request and response
                    do {

                        //ask for an id
                        System.out.println("Enter your ID number");
                        selfId = scanner.nextInt();

                        //send a connection request
                        ByteBuffer sendBuff = Message.ConnectionRequest.craft(selfId, pukeyBytes);
                        socketChannel.write(sendBuff);

                        //receive a connection response (certificate)
                        socketChannel.read(readbuff);
                        readbuff.flip();

                        //parse the received message
                        //handshake = (Message.ConnectionResponse) Message.parse(readbuff);
                        handshake = (Message.CertResponse) Message.parse(readbuff);


                        //get an object from the public key bytes
                        if(handshake.getFlag() ==0){
                            PublicKey CAPublicKey = KeyStorage.loadPublicKey("CAPublicKey");

                            if(DigitalCertificate.verify(handshake.getCertificate(), CAPublicKey, "GullAndBullFinance")) {
                                //remotePublickey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(handshake.getPublicKey()));
                                remotePublickey = handshake.getCertificate().getPublicKey();
                                validCertReceived = true;
                            }
                            else{
                                System.out.println("Invalid Certificate Received... closing connection...");
                            }
                        }
                        else{
                            System.out.println("Failed to establish connection to the server...");
                        }

                        /*
                        //print the message
                        int lastIndex = handshake.getMessage().indexOf(0);
                        String responseMessage = handshake.getMessage().substring(0, lastIndex);
                        System.out.println(responseMessage);
                        */


                        readbuff.clear();

                    }while(handshake.getFlag() == 1 || !validCertReceived);


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

                        //System.out.println(messageWithSignatureEncrypted.length);

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

                        //Decrypt the transaction response with the session key
                        byte[] encryptedResponse = new byte[readbuff.remaining()];
                        readbuff.get(encryptedResponse, 0, readbuff.remaining());

                        byte[] responseAndSignature = Symmetric.decrypt(encryptedResponse, key, Symmetric.iv);

                        byte[] responseSignature = new byte[128];
                        byte[] responseBytes = new byte[responseAndSignature.length - 128];

                        System.arraycopy(responseAndSignature, 0, responseBytes, 0, responseBytes.length);
                        System.arraycopy(responseAndSignature, responseBytes.length, responseSignature, 0, responseSignature.length);

                        //parse the received message
                        transactionResponse = (Message.TransactionResponse) Message.parse(ByteBuffer.wrap(responseBytes));

                        boolean verified = DigitalSignature.verify(responseBytes, responseSignature, remotePublickey);

                        if(verified){
                            System.out.println("Transaction Response Verified");
                        }
                        else{
                            System.out.println("Transaction Response Invalid");
                            break;
                        }

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

        socketChannel.close();

        System.out.println("exiting....");

    }

}
