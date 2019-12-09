package Core;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import models.ClientModel;
import org.omg.PortableServer.LIFESPAN_POLICY_ID;

import java.io.*;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {




        List<ClientModel> clients = new CsvToBeanBuilder(new FileReader("G:\\5th year projects\\CyberSecurity\\src\\text.csv"))
                .withType(ClientModel.class).build().parse();

        for (ClientModel clientModel : clients)
        {
            System.out.println(clientModel.getID() + " balance:" + clientModel.getBalance());
        }

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
