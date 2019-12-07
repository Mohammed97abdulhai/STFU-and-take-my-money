package Core;

public class Main {

    public static void main(String[] args) {


        BankingServer server =new BankingServer(5000, 10);
        new Thread(server).start();


        try {
            Thread.sleep(20 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Stopping Server");
        server.stop();


    }
}
