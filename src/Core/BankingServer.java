package Core;


import com.opencsv.bean.CsvToBeanBuilder;
import messages.Message;
import models.ClientModel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankingServer implements  Runnable{

    private int port = 5000;
    private   boolean isStopped = false;

    private ServerSocketChannel serverSocketChannel;
    private   ExecutorService threadPool;
    List<ClientModel> clients;

    public BankingServer(int port , int numThreads) throws FileNotFoundException {


       clients = new CsvToBeanBuilder(new FileReader("G:\\5th year projects\\CyberSecurity\\src\\text.csv"))
                .withType(ClientModel.class).build().parse();


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
                    System.out.println();
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

    }


    private class Banker implements Runnable{

        private SocketChannel socketChannel;

        public Banker(SocketChannel socketChannel){

            this.socketChannel= socketChannel;



        }

        @Override
        public void run() {

            try {
                System.out.println("Connected to " + socketChannel.toString());

               this.socketChannel.configureBlocking(true);

                ByteBuffer buf = ByteBuffer.allocate(1024);


                while(!Thread.currentThread().isInterrupted()){

                    socketChannel.read(buf);

                    buf.flip();

                    Message.TransactionRequest message = (Message.TransactionRequest) Message.parse(buf);

                    System.out.println(message.getId());
                    System.out.println(message.getAmount());
                    System.out.println(message.getMessage());

                    buf.rewind();

                }


            } catch (IOException e){

                System.out.println(e.getMessage());
            } catch (ParseException e){

                System.out.println(e);
            }

            finally {

                try {socketChannel.close();

                } catch (IOException e){

                    System.out.println("Error closing Socket");

                }

                System.out.println("Closed: " + socketChannel);

            }



        }
    }

}
