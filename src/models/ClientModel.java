package models;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvNumber;

public class ClientModel {

    @CsvBindByPosition(position = 0)
    private String ID;

    @CsvBindByPosition(position = 1)
    private String Balance;

    public ClientModel()
    {}


    public String getBalance() {
        return Balance;
    }

    public void setBalance(String  balance) {
        Balance = balance;
    }

    public String getID() {
        return ID;
    }

    public void setID(String  ID) {
        this.ID = ID;
    }
}
