package Core;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankingServer implements  Runnable{

    protected int port = 8080;
    protected  boolean isStopped = false;

    protected  ServerSocket serverSocket = null;
    protected  ExecutorService threadPool  = null;

    public BankingServer(int port , int numThreads){

        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(numThreads);

    }



    @Override
    public void run() {

        try {
            this.serverSocket = new ServerSocket(this.port);

        } catch(IOException e){
            throw new RuntimeException("Cannot open port " + String.valueOf(this.port) , e);
        }

        while (!isStopped()) {
            Socket clientSocket = null;
            try {

                clientSocket = this.serverSocket.accept(); //blocking

            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println();
                    break;
                }
                throw new RuntimeException("Error accepting client connection ", e);

            }

            this.threadPool.execute(new Banker(clientSocket));
        }
        this.threadPool.shutdown();
        System.out.println("BankingServer Stopped");
    }



    private synchronized boolean isStopped(){

        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;

        try{


            this.serverSocket.close();

        } catch (IOException e){
            throw new RuntimeException("Error closing server" , e);
        }

    }


    private class Banker implements Runnable{

        protected  Socket clientSocket= null;

        public Banker(Socket clientSocket){

            this.clientSocket= clientSocket;

        }

        @Override
        public void run() {

            try {
                System.out.println("Connected to " + clientSocket.toString());
                Scanner input = new Scanner(clientSocket.getInputStream());
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream());
                while(input.hasNextLine()){

                    String s = input.nextLine();
                    output.println(s + "YO ICHIGO SUCK MY Ass");

                }
                output.close();
                input.close();




            } catch (IOException e){

                e.printStackTrace();
            }

        }
    }

}
