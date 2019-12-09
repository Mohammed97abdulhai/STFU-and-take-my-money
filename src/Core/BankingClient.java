package Core;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class BankingClient {

    public static void main(String[] args) throws IOException {

        SocketChannel socketChannel =   SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1" , 5000));

        socketChannel.configureBlocking(true);

        ByteBuffer buffer = ByteBuffer.allocate(20);

        while(socketChannel.read(buffer)!=-1){
            buffer.flip();

            int value = buffer.getInt();

            buffer.clear();
            System.out.println(value);


        }




        socketChannel.close();

    }

}
