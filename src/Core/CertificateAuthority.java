package Core;

import Core.crypto.DigitalCertificate;
import messages.Message;
import util.KeyStorage;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.*;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Scanner;

public class CertificateAuthority extends Server {

    public static final String issuer = "CertMaster";
    public static PublicKey publicKey;
    private static PrivateKey privateKey;
    private static HashSet<String> registeredOwners;

    public CertificateAuthority(int port, int maxThreads){
        super(port, maxThreads);

        registeredOwners = new HashSet<>();
        loadOwners();

        publicKey = KeyStorage.loadPublicKey("CAPublicKey");
        privateKey = KeyStorage.loadPrivateKey("CAPrivateKey");

        //If no existing keys are found, generate new key pair
        if(publicKey == null || privateKey == null){
            System.out.println("CA Keys not found... Generating new key pair");
            try{
                KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
                keygen.initialize(1024);
                KeyPair keyPair = keygen.generateKeyPair();
                publicKey = keyPair.getPublic();
                privateKey = keyPair.getPrivate();

                KeyStorage.saveKey(publicKey, "CAPublicKey");
                KeyStorage.saveKey(privateKey, "CAPrivateKey");
                System.out.println("Saved new key pair...");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Loaded existing CA keys");
        }

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
                //Wait for request
                socketChannel = this.serverSocketChannel.accept();

            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Certificate server is stopped");
                    break;
                }
                throw new RuntimeException("Error accepting client connection ", e);
            }

            this.threadPool.execute(new CertificateSigner(socketChannel));
        }
        this.threadPool.shutdownNow();
        System.out.println("Certificate Server Stopped");
    }

    private void loadOwners(){
        File ownersFile = new File("CertificateOwners.txt");
        if(ownersFile.exists() && !ownersFile.isDirectory()){
            try{
                FileInputStream in = new FileInputStream(ownersFile);
                Scanner scanner = new Scanner(in);

                while(scanner.hasNext()){
                    String entry = scanner.nextLine();
                    registeredOwners.add(entry);
                }

                scanner.close();
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                ownersFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static synchronized void registerOwner(String owner){
        registeredOwners.add(owner);
        try{
            FileOutputStream out = new FileOutputStream("CertificateOwners.txt");
            PrintStream printer = new PrintStream(out);
            printer.println(owner);
            printer.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class CertificateSigner extends Server.ClientHandler{

        public CertificateSigner(SocketChannel socketChannel){
            super(socketChannel);
        }

        @Override
        public void run(){

            try{
                System.out.println("Connected : " + socketChannel.toString());
                System.out.println("*******************************************");
                this.socketChannel.configureBlocking(true);

                ByteBuffer readbuff = ByteBuffer.allocate(1024);
                Message.CertSignRequest csr = null;

                //Handle Certificate Signing Request and Certificate Response

                System.out.println("Waiting for CSR");
                this.socketChannel.read(readbuff);
                System.out.println("Received CSR");
                readbuff.flip();

                csr = (Message.CertSignRequest)Message.parse(readbuff);

                DigitalCertificate cert = new DigitalCertificate(csr.getOwner(), issuer, csr.getPublicKey());

                ByteBuffer writebuff;
                if(!registeredOwners.contains(csr.getOwner())){
                    DigitalCertificate.sign(cert, privateKey);
                    //registerOwner(csr.getOwner());
                    writebuff = Message.CertResponse.craft(cert, (byte)0);
                }
                else{
                    writebuff = Message.CertResponse.craft(cert, (byte)1);
                }

                this.socketChannel.write(writebuff);

                System.out.println("Sent Certificate Response");
                System.out.println("*******************************************");

                readbuff.clear();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                System.out.println("Error parsing request");
            }

        }

    }

    public static void main(String[] args){

        CertificateAuthority server = new CertificateAuthority(15000, 10);
        new Thread(server).start();

        try {
            Thread.sleep(3600 * 1000);
        } catch (InterruptedException e) {
            System.out.println("Certificate Server Interrupted... closing");
        }
        server.stop();
        System.out.println("Stopped Certificate Server");
    }

}
