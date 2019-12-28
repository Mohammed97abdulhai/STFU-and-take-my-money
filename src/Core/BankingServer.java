package Core;


import Core.crypto.Asymmetric;
import Core.crypto.DigitalCertificate;
import Core.crypto.DigitalSignature;
import Core.crypto.Symmetric;
import com.opencsv.bean.CsvToBeanBuilder;
import messages.Message;
import models.ClientModel;
import util.KeyStorage;
import util.Util;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class BankingServer implements  Runnable{

    private int port = 5000;
    private boolean stopped = false;

    public static PublicKey publicKey;
    private static PrivateKey privateKey;

    private ServerSocketChannel serverSocketChannel;
    private ExecutorService threadPool;
    List<ClientModel> clients;
    Map<Integer , ClientModel> clientMap = new HashMap<>();

    public BankingServer(int port , int numThreads) throws FileNotFoundException {

        clients = new CsvToBeanBuilder(new FileReader("src/text.csv")).withType(ClientModel.class).build().parse();

        for(ClientModel client : clients){

            clientMap.put(client.getID() ,client);
        }

        publicKey = KeyStorage.loadPublicKey("ServerPublicKey");
        privateKey = KeyStorage.loadPrivateKey("ServerPrivateKey");

        //If no existing keys are found, generate new key pair
        if(publicKey == null || privateKey == null){
            System.out.println("Server Keys not found... Generating new key pair");
            try{
                KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
                keygen.initialize(1024);
                KeyPair keyPair = keygen.generateKeyPair();
                publicKey = keyPair.getPublic();
                privateKey = keyPair.getPrivate();

                KeyStorage.saveKey(publicKey, "ServerPublicKey");
                KeyStorage.saveKey(privateKey, "ServerPrivateKey");
                System.out.println("Saved new key pair...");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Loaded existing Server keys");
        }


        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(numThreads);

    }



    @Override
    public void run() {

        Scanner scan = new Scanner(System.in);
        System.out.println("1 - Start Banking Server");
        System.out.println("2 - Acquire New Certificate");
        int choice = scan.nextInt();

        if(choice == 1){
            try {
                this.serverSocketChannel = ServerSocketChannel.open();
                this.serverSocketChannel.bind(new InetSocketAddress(this.port));

            } catch(IOException e){
                throw new RuntimeException("Cannot open port " + this.port , e);
            }

            while (!isStopped()) {
                SocketChannel socketChannel;
                try {

                    socketChannel = this.serverSocketChannel.accept();//blocking

                } catch (IOException e) {
                    if (isStopped()) {
                        System.out.println("server is stopped u idiot");
                        break;
                    }
                    throw new RuntimeException("Error accepting client connection ", e);

                }

                this.threadPool.execute(new Banker(socketChannel));
            }
            this.threadPool.shutdownNow();
            System.out.println("BankingServer Stopped");
        }

        else if (choice == 2){

            try{
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress("127.0.0.1", 15000));
                socketChannel.configureBlocking(true);

                System.out.println("Connected : " + socketChannel.toString());

                ByteBuffer readbuff = ByteBuffer.allocate(1024);


                //Create a certificate signing request
                ByteBuffer writebuff = Message.CertSignRequest.craft("GullAndBullFinance", BankingServer.publicKey);

                //Send the CSR
                System.out.println("Sending CSR");
                socketChannel.write(writebuff);

                //Accept a Response
                System.out.println("Waiting for certificate Response");
                socketChannel.read(readbuff);
                System.out.println("Certificate Response Received");
                readbuff.flip();

                //Parse the response message
                Message.CertResponse response = (Message.CertResponse)Message.parse(readbuff);

                //Check if the certificate was signed
                if(response.getFlag() == 0){
                    //Store the certificate
                    response.getCertificate();

                    File certFile = new File("ServerCertificate");
                    if(!certFile.exists() || certFile.isDirectory()){
                        certFile.createNewFile();
                    }
                    else{
                        certFile.delete();
                        certFile.createNewFile();
                    }
                    FileOutputStream out = new FileOutputStream(certFile);
                    out.write(DigitalCertificate.store(response.getCertificate()));
                    out.close();
                }
                else{
                    //pass
                }

            } catch (IOException e) {
                System.out.println("Certificate Server seems to be offline");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Invalid choice... closing");
        }

        this.stop();
    }


    private synchronized boolean isStopped(){
        return stopped;
    }

    public synchronized void stop(){
        stopped = true;

        try{
            this.serverSocketChannel.close();

        } catch (IOException e){
            throw new RuntimeException("Error closing server" , e);
        }

        saveDatabase();
    }

    private void saveDatabase(){
        System.out.println("Something happen?");
        try {
            FileOutputStream out = new FileOutputStream("src/text.csv");

            for(int key : clientMap.keySet()){
                ClientModel client = clientMap.get(key);
                double clientBalance = client.getBalance();
                String line = key + "," + clientBalance + "\n";
                out.write(line.getBytes(StandardCharsets.UTF_8));
            }

        } catch (FileNotFoundException e) {
            System.out.println("Cannot find save file");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Cannot write to file");
            e.printStackTrace();
        }
    }



    private class Banker implements Runnable{

        private SocketChannel socketChannel;
        private boolean connected  = false;
        private boolean validTransaction = false;
        private int userId = 0;
        private boolean cipherMode = false;
        private boolean handshakeReceived = false;
        private boolean symmetricOnly = true;

        private PublicKey publicKey;
        private PrivateKey privateKey;

        private PublicKey remotePublickey;

        private byte[] pkeyBytes;

        private byte[] secretKey;



        public Banker(SocketChannel socketChannel){

            this.socketChannel = socketChannel;
            this.connected = false;

            try {
                KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
                keygen.initialize(1024);
                KeyPair pair = keygen.generateKeyPair();
                this.publicKey = pair.getPublic();
                this.privateKey = pair.getPrivate();

                this.pkeyBytes = this.publicKey.getEncoded();


            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            if(symmetricOnly){
                this.secretKey = Symmetric.key;
                cipherMode = true;
            }
        }


        @Override
        public void run() {

            try {
                System.out.println("Connected to " + socketChannel.toString());

                this.socketChannel.configureBlocking(true);

                ByteBuffer readbuff = ByteBuffer.allocate(1024);

                Message.ConnectionRequest handshake = null;


                //1) handle connection request and connection response


                do{
                    socketChannel.read(readbuff);
                    readbuff.flip();

                    handshake = (Message.ConnectionRequest) Message.parse(readbuff);

                    ByteBuffer writeBuff = null;

                    int id = handshake.getId();


                    if(clientMap.containsKey(id)){
                        this.connected = true;
                        this.userId = id;


                        //read the publickey bytes and create a PublicKey object from it

                        this.remotePublickey = Asymmetric.rebuildPublicKey(handshake.getPublicKey());



                        writeBuff = Message.ConnectionResponse.craft((byte)0, Util.constructString("Successfully established connection to user " + id, 256), pkeyBytes);
                    }
                    else{
                        writeBuff = Message.ConnectionResponse.craft((byte)1, Util.constructString("failed to establish connection to user " + id, 256), pkeyBytes);
                    }

                    this.socketChannel.write(writeBuff);

                    readbuff.clear();

                }while(!this.connected );





                //2) handle transfer request and response

                Message.TransactionRequest transactionRequest = null;

                do{

                    socketChannel.read(readbuff);
                    readbuff.flip();


                    //read the encrypted session key
                    byte[] encryptedSessionKey = new byte[128];
                    readbuff.get(encryptedSessionKey , 0 , encryptedSessionKey.length);

                    //decrypt it
                    byte[] sessionKey = Asymmetric.decrypt(encryptedSessionKey , this.privateKey);



                    //read the encrypted message + signature
                    byte[] messagePlusSignatureEncrypted = new byte[readbuff.remaining()];
                    readbuff.get(messagePlusSignatureEncrypted , 0 , messagePlusSignatureEncrypted.length);

                    //decrypt it
                    byte[] messagePlusSignature = Symmetric.decrypt(messagePlusSignatureEncrypted , sessionKey , Symmetric.iv);

                    //readbuff.get(signature , 0 , signature.length);


                    //split the array into signature and message
                    byte[] signature = new byte[128];
                    byte[] message = new byte[messagePlusSignature.length - 128];

                    System.arraycopy(messagePlusSignature , 0 , signature , 0 , signature.length);
                    System.arraycopy(messagePlusSignature , signature.length ,message , 0 , message.length );



                   /* ByteBuffer temp = readbuff.slice();
                    byte[] m = new byte[temp.remaining()];

                    temp.get(m);
                    temp.rewind();*/

                    transactionRequest = (Message.TransactionRequest)Message.parse(ByteBuffer.wrap(message));


                   boolean verfied =  DigitalSignature.verify(message , signature , this.remotePublickey);

                   if(verfied){
                       System.out.println("VERFIED");
                   }

                    ByteBuffer writeBuff = null;


                    int id = transactionRequest.getId();
                    double amount = transactionRequest.getAmount();
                    String reason = transactionRequest.getMessage();

                    if(clientMap.containsKey(id)){
                        ClientModel sender = clientMap.get(userId);
                        double currentBalance = sender.getBalance();

                        ClientModel receiver = clientMap.get(id);
                        double receiverBalance = receiver.getBalance();

                        if(currentBalance > amount) {
                            writeBuff = Message.TransactionResponse.craft((byte)0 , Util.constructString("Transaction successful. new balance : " + (currentBalance - amount) , 256));
                            clientMap.get(userId).setBalance(String.valueOf(currentBalance - amount));
                            clientMap.get(id).setBalance(String.valueOf(receiverBalance + amount));
                            validTransaction = true;
                        }
                        else{
                            writeBuff = Message.TransactionResponse.craft((byte)1 , Util.constructString("Insufficient funds" , 256));
                        }

                    }
                    else{
                        writeBuff = Message.TransactionResponse.craft((byte)1 , Util.constructString("User : " + id + " does not exist"  , 256));
                    }

                    this.socketChannel.write(writeBuff);
                    readbuff.clear();

                } while(!validTransaction);




            } catch (IOException e){
                System.out.println(e.getMessage());
            } catch (ParseException e){
                System.out.println("Parse Error" + e.getMessage());
            } catch (BufferUnderflowException e){
                System.out.println("Buffer Error caused by interruption");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException | SignatureException e) {
                System.out.println("Signature Verification Failure");
            } finally {
                try {
                    socketChannel.close();
                    System.out.println("Closed: " + socketChannel);
                } catch (IOException e){
                    System.out.println("Error closing Socket");
                }
            }


        }
    }

    public static void main(String[] args){
        BankingServer server = null;
        try {
            server = new BankingServer(5000, 10);
            new Thread(server).start();
            while(!server.isStopped()){
                Thread.sleep(5 * 1000);
            }

        } catch (InterruptedException e) {
            server.stop();
            System.out.println("Server Interrupted... closing...");
        } catch (FileNotFoundException e) {
            System.out.println("Server Cannot find database file... closing...");
        }

        System.out.println("Stopped Server...");
    }

}
