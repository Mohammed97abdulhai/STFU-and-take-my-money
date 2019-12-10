package Core;


import com.opencsv.bean.CsvToBeanBuilder;
import messages.Message;
import models.ClientModel;
import util.Util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
    private List<ClientModel> clients;
    private Map<Integer , ClientModel> clientModelMap;

    public BankingServer(int port , int numThreads) throws FileNotFoundException {





       clients = new CsvToBeanBuilder(new FileReader("src\\text.csv"))
                .withType(ClientModel.class).build().parse();



        clientModelMap = new HashMap<>();

        for(ClientModel client : clients){

            clientModelMap.put(client.getID() ,client);
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

    }


    private synchronized boolean userInServer(int id){

        return clientModelMap.containsKey(id);

    }

    private synchronized  boolean userHasEnoughMoney(double amount , int id){


        return false;


    }
    private synchronized void handleMessage(Message msg , SocketChannel socketChannel){

       /* switch(msg.getType()){

            case connectionRequest:
                ByteBuffer sendbuffer =






        }*/




    }


    private class Banker implements Runnable{

        private SocketChannel socketChannel;
        private boolean connected ;

        private int id;


        public Banker(SocketChannel socketChannel){

            this.socketChannel= socketChannel;
            this.connected = false;


        }


        @Override
        public void run() {

            try {
                System.out.println("Connected to " + socketChannel.toString());

               this.socketChannel.configureBlocking(true);

                ByteBuffer readbuff = ByteBuffer.allocate(1024);
                String reason;
                byte flag;

                // ByteBuffer writebuff = ByteBuffer.allocate(1024);


                while(!Thread.currentThread().isInterrupted()){

                    socketChannel.read(readbuff);
                    readbuff.flip();

                    Message message =  Message.parse(readbuff);

                    if(!(message instanceof  Message.ConnectionRequest )&& !this.connected){

                        reason = "U Must Be Connected.... Try Again";
                        flag = 1;


                        ByteBuffer writeBuff = Message.ConnectionResponse.craft( flag , Util.constructString(reason , 256));
                        socketChannel.write(writeBuff);

                    }

                    else if(message instanceof  Message.ConnectionRequest && this.connected){

                        reason = "you are already connected";
                        flag = 1;


                        ByteBuffer writeBuff = Message.ConnectionResponse.craft( flag , Util.constructString(reason , 256));
                        socketChannel.write(writeBuff);

                    }

                   // System.out.println(message.getMessage());

                    else {
                        System.out.println("inside else");

                        switch (message.getType()){

                            case connectionRequest:


                                Message.ConnectionRequest cMessage= (Message.ConnectionRequest)message;

                                if(userInServer((cMessage).getId())){
                                    reason = "Connected!";

                                    flag = 0;

                                    ByteBuffer writeBuff=  Message.ConnectionResponse.craft(flag, Util.constructString(reason , 256));
                                    socketChannel.write(writeBuff);

                                    this.connected = true;
                                    this.id = cMessage.getId();

                                }
                                else{

                                    reason = "U are Not  A user in this bank......";
                                    flag = 1;

                                    ByteBuffer writeBuff=  Message.ConnectionResponse.craft(flag , Util.constructString(reason , 256));
                                    socketChannel.write(writeBuff);

                                    this.connected = false;


                                }


                                break;



                            case transferRequest:

                                System.out.println("yooooo");


                                Message.TransactionRequest tMessage= (Message.TransactionRequest)message;

                                if(userInServer(tMessage.getId())){

                                    reason = "the User U wish to transfer Money to doesn't exist in our service currently! maybe stop sending money to ur imaginary friends";
                                    flag = 1;

                                    ByteBuffer writeBuff=  Message.TransactionResponse.craft(flag , Util.constructString(reason , 256));
                                    socketChannel.write(writeBuff);



                                }
                                else{

                                    if(userHasEnoughMoney(tMessage.getAmount() , this.id) ){

                                        reason = "Money trasnferred";
                                        flag = 0;

                                        ByteBuffer writeBuff=  Message.TransactionResponse.craft(flag , Util.constructString(reason , 256));
                                        socketChannel.write(writeBuff);



                                    }

                                    else{

                                        reason = "you are broke dude";
                                        flag = 0;


                                        ByteBuffer writeBuff=  Message.TransactionResponse.craft(flag , Util.constructString(reason , 256));
                                        socketChannel.write(writeBuff);


                                    }

                                }

                                break;





                        }



                    }



                    readbuff.clear();

                    //handleMessage(message);



                    //System.out.println(message.getId());
                    //System.out.println(message.getAmount());
                    //System.out.println(message.getMessage());

                    //readbuff.rewind();

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
