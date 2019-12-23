package Core;


import Core.crypto.Asymmetric;
import Core.crypto.Symmetric;
import com.opencsv.bean.CsvToBeanBuilder;
import messages.Message;
import models.ClientModel;
import util.Util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class BankingServer implements  Runnable{

    private int port = 5000;
    private   boolean isStopped = false;

    private ServerSocketChannel serverSocketChannel;
    private   ExecutorService threadPool;
    List<ClientModel> clients;
    Map<Integer , ClientModel> clientMap = new HashMap<>();

    public BankingServer(int port , int numThreads) throws FileNotFoundException {

        clients = new CsvToBeanBuilder(new FileReader("src/text.csv")).withType(ClientModel.class).build().parse();

        for(ClientModel client : clients){

            clientMap.put(client.getID() ,client);
        }

        //ClientModel model = clientMap.get(1);
        //System.out.println(model);


        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(numThreads);

    }



    @Override
    public void run() {

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



    private synchronized boolean isStopped(){



        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;

        try{
            this.serverSocketChannel.close();

        } catch (IOException e){
            throw new RuntimeException("Error closing server" , e);
        }

        saveDatabase();
    }

    private synchronized void handleMessage(Message msg , SocketChannel socketChannel){

       /* switch(msg.getType()){

            case connectionRequest:
                ByteBuffer sendbuffer =






        }*/

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

            this.socketChannel= socketChannel;
            this.connected = false;

            try {
                KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
                keygen.initialize(1024);
                KeyPair pair = keygen.generateKeyPair();
                this.publicKey = pair.getPublic();
                this.privateKey = pair.getPrivate();

                this.pkeyBytes = this.publicKey.getEncoded();

                //System.out.println(this.pkeyBytes.length);
                System.out.println(new String(this.pkeyBytes , StandardCharsets.UTF_8));

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

                Message.ConnectionRequest handshake= null;


                //1) handle connection request and connection response


                do{
                    socketChannel.read(readbuff);
                    readbuff.flip();

                    handshake = (Message.ConnectionRequest) Message.parse(readbuff);

                    ByteBuffer writeBuff = null;

                    int id = handshake.getId();

                    System.out.println(new String(handshake.getPublicKey() , StandardCharsets.UTF_8));

                    if(clientMap.containsKey(id)){
                        this.connected = true;
                        this.userId = id;


                        //read the publickey bytes and create a PublicKey object from it

                        this.remotePublickey =
                                KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(handshake.getPublicKey()));





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


                    byte[] signature = new byte[128];
                    readbuff.get(signature , 0 , signature.length);


                    transactionRequest = (Message.TransactionRequest)Message.parse(readbuff.slice());

                    System.out.println(new String(transactionRequest.getData().array() , StandardCharsets.UTF_8));

                   boolean verfied =  Symmetric.verify(transactionRequest.getData().array() , signature , this.remotePublickey);

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


                }while(!validTransaction);






            }
            catch (IOException e){
                System.out.println(e.getMessage());
            }
            catch (ParseException e){
                System.out.println("Parse Error");
                System.out.println(e.getMessage());
            }
            catch (BufferUnderflowException e){
                System.out.println("Buffer Error caused by interruption");
                //System.out.println(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            } finally {

                try {socketChannel.close();

                } catch (IOException e){

                    System.out.println("Error closing Socket");

                }

                System.out.println("Closed: " + socketChannel);

            }


        }
    }

}
