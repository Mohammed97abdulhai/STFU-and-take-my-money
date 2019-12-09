package messages;

import com.sun.deploy.security.ValidationState;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

public abstract class Message {


    public  static final int MESSAGE_LENGTH_FIELD_SIZE = 4;

    public enum Type{

        connectionRequest(1),
        connectionResponse(2),
        transferRequest(3),
        transferResponse( 4),
        balanceRequest(5),
        balanceResponse(6);


        private byte id;

        Type(int id){
            this.id = (byte)id;
        }

        public boolean equals(byte c){
            return this.id ==c;
        }

        public byte getTypeByte(){

            return this.id;
        }

        public static Type get(byte c){

            for(Type t : Type.values()){
                if(t.equals(c)){
                    return t;
                }
            }
            return null;
        }

    };


    private  final Type type;
    private  final ByteBuffer data;

    private Message(Type type , ByteBuffer data){

        this.type = type;
        this.data = data;
        this.data.rewind();

    }

    public Type getType(){
        return this.type;
    }

    public ByteBuffer getData(){
        return this.data.duplicate();

    }

    public String toString(){

        return this.getType().name();
    }


    public static Message parse(ByteBuffer buffer) throws ParseException{

        int length = buffer.getInt();
        if(length ==0){
            throw new ParseException("no size specified" , 0);
        }

        else if (length != buffer.remaining()){
            throw new ParseException("specified message size doesn't match the actual ",0);
        }

        Type type = Type.get(buffer.get());

        if (type ==null){
            throw new ParseException("Unknown message ID" , buffer.position()-1);

        }

        switch (type){

            //case CHOKE:
            case  connectionRequest:
                return ConnectionRequest.parse(buffer.slice());


            case connectionResponse:
                return ConnectionResponse.parse(buffer.slice());


            case transferRequest:
                return TransactionRequest.parse(buffer.slice());

            case transferResponse:
                return TransactionResponse.parse(buffer.slice());


            default:
                throw new IllegalStateException("the message type isnt defined");
        }





    }




    public static class ConnectionRequest extends  Message{

        public static final int BASE_Size = 5;

        int id;


        private ConnectionRequest(ByteBuffer buffer,int id){
            super(Type.connectionRequest , buffer);
            this.id = id;

        }

        public static ConnectionRequest parse(ByteBuffer buffer){

            return  new ConnectionRequest(buffer , buffer.getInt());


        }

        public static ByteBuffer  craft(int id){

            ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_LENGTH_FIELD_SIZE + ConnectionRequest.BASE_Size);
            buffer.putInt(ConnectionRequest.BASE_Size);
            buffer.put(Type.connectionRequest.getTypeByte());
            buffer.putInt(id); //add ID
            buffer.flip();

            return buffer;
        }


        public int getId(){

            return this.id;
        }
    }




    public static class ConnectionResponse extends  Message{

        public static final int BASE_Size = 257;

        byte flag;

        byte[] Reason;
        private ConnectionResponse(ByteBuffer buffer,byte flag , byte[] message){
            super(Type.connectionResponse , buffer);
            this.flag = flag;
            this.Reason = message;

        }

        public static ConnectionResponse parse(ByteBuffer buffer){


            byte flag  = buffer.get();
            byte[] message = new byte[256];
            buffer.get(message , 0 , message.length); //read into message array
            return  new ConnectionResponse(buffer , flag , message);



        }

        public static ByteBuffer  craft(byte id , byte[] message){

            ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_LENGTH_FIELD_SIZE + ConnectionResponse.BASE_Size);
            buffer.putInt(ConnectionResponse.BASE_Size);
            buffer.put(Type.connectionResponse.getTypeByte());
            buffer.putInt(id);
            buffer.put(message , 0 , 256);
            buffer.flip();

            return buffer;
        }


        public byte getFlag(){
            return this.flag;
        }

        public String getMessage(){

            return  new String(this.Reason , StandardCharsets.UTF_8);
        }
    }

    public static class TransactionRequest extends  Message{

        public static final int BASE_Size = 269;

        int id; //the id of the person we want to give money to

        double moneyAmount; // the amount of money we want to transfer

        byte[] reason; // the message explaining why we want to transfer money to that person


        private TransactionRequest(ByteBuffer buffer,int id, byte [] message, double moneyAmount){
            super(Type.transferRequest , buffer);
            this.id = id;
            this.reason = message;
            this.moneyAmount = moneyAmount;

        }

        public static TransactionRequest parse(ByteBuffer buffer){

            byte[] message = new byte[256];
            int id = buffer.getInt();
            double amount = buffer.getDouble();
            buffer.get(message , 0 , message.length);
            return  new TransactionRequest(buffer , id,message,amount );

        }

        public static ByteBuffer  craft(int id, double moneyAmount, byte [] message){

            ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_LENGTH_FIELD_SIZE + TransactionRequest.BASE_Size);
            buffer.putInt(TransactionRequest.BASE_Size);
            buffer.put(Type.transferRequest.getTypeByte());
            buffer.putInt(id); //add ID
            buffer.putDouble(moneyAmount);
            buffer.put(message , 0 , 256);
            buffer.flip();

            return buffer;
        }


        public double getAmount(){
            return this.moneyAmount;
        }

        public String getMessage(){

            return new  String(this.reason , StandardCharsets.UTF_8);
        }

        public int getId(){
            return this.id;

        }

    }

    public static class TransactionResponse extends Message{

        public static final int BASE_Size = 258;


        byte flag;

        byte[] Reason;
        private TransactionResponse(ByteBuffer data, Byte flag, byte[] message) {
            super(Type.transferResponse, data);
            this.flag = flag;
            this.Reason = message;
        }

        public static TransactionResponse parse(ByteBuffer buffer)
        {
            byte flag;
            flag = buffer.get();

            byte[] message = new byte[256];
            buffer.get(message, 0, message.length);

            return new TransactionResponse(buffer, flag, message);

        }

        public static ByteBuffer craft(byte flag, byte [] message)
        {

            ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_LENGTH_FIELD_SIZE + TransactionResponse.BASE_Size);
            buffer.putInt(TransactionResponse.BASE_Size);
            buffer.put(Type.transferResponse.getTypeByte());
            buffer.put(flag);
            buffer.put(message, 0,256);
            buffer.flip();
            return buffer;
        }
        public byte getFlag(){
            return this.flag;
        }

        public  String getMessage(){

            return new String(this.Reason , StandardCharsets.UTF_8);

        }
    }

 /*   public static class ConnectionRequest extends  Message{

        private static final int BASE_Size = 5;

        int id;


        private ConnectionRequest(ByteBuffer buffer,int id){
            super(Type.connectionRequest , buffer);
            this.id = id;

        }

        public static ConnectionRequest parse(ByteBuffer buffer){

            return  new ConnectionRequest(buffer , buffer.getInt());


        }

        public static ByteBuffer  craft(int id){

            ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_LENGTH_FIELD_SIZE + ConnectionRequest.BASE_Size);
            buffer.putInt(ConnectionRequest.BASE_Size);
            buffer.put(Type.connectionRequest.getTypeByte());
            buffer.putInt(id); //add ID
            buffer.flip();

            return buffer;
        }


    }

*/

}
