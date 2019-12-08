package Core;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {


        BankingServer server =new BankingServer(5000, 10);
        new Thread(server).start();

        try {
            Thread.sleep(80 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Stopping Server");
        server.stop();


    }
}
