package models;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvNumber;

public class ClientModel {

    @CsvBindByPosition(position = 0)
    private String ID;

    @CsvBindByPosition(position = 1)
    private String Balance;

    private boolean connected = false;

    public ClientModel()
    {}


    public boolean getConnected() {
        return this.connected;
    }

    public Double getBalance() {
        return Double.parseDouble(this.Balance);
    }

    public void setBalance(String  balance) {
        Balance = balance;
    }

    public Integer getID() {
        return Integer.parseInt(this.ID);

       //byte[] string= this.ID.getBytes();

       //String s = new String(string , st)


    }

    public void setID(String  ID) {
        this.ID = ID;
    }

    @Override
    public String toString() {
        return  new String("this id:" + this.ID + "this balance:" + this.Balance + "connected?:" + this.connected );
    }
}
