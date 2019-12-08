package Core;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class BankingClient {

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("127.0.0.1",5000);
        Scanner scanner = new Scanner(System.in);
        Scanner in = new Scanner(socket.getInputStream());
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        while(scanner.hasNextLine()) {

            out.println(scanner.nextLine());
            System.out.println(in.nextLine());


        }

        socket.close();

    }

    public void startClient() throws IOException {

    }
}
