package util;

import java.nio.charset.StandardCharsets;

public class Util {

    static public byte[] constructString(String string , int size){

        byte[] message= new byte[size];

        byte[] temp = string.getBytes(StandardCharsets.UTF_8);

        for(int i = 0; i < temp.length; i++){
            message[i] = temp[i];
        }

        return message;

    }


}
