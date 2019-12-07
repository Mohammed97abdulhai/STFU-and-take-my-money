package Core;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class BankingClient {
    public static void main(String[] args) throws IOException {

    }

    public void startClient() throws IOException {
        Socket socket = new Socket("127.0.0.1",5000);
        Scanner in = new Scanner(socket.getInputStream());
        System.out.println("server response" + in.nextLine());
        System.out.println(socket.getOutputStream());

    }
}
