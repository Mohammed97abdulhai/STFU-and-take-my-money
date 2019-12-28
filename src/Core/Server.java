package Core;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Server implements Runnable{

    protected int port = 9999;
    protected boolean stopped = false;

    protected ServerSocketChannel serverSocketChannel;
    protected ExecutorService threadPool;

    public Server(int port, int numThreads){
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(numThreads);
    }

    public synchronized boolean isStopped(){
        return stopped;
    }

    public synchronized void stop() {
        stopped = true;
    }


    protected static abstract class ClientHandler implements Runnable{

        protected SocketChannel socketChannel;

        public ClientHandler(SocketChannel socketChannel){
            this.socketChannel = socketChannel;
        }

    }

}
