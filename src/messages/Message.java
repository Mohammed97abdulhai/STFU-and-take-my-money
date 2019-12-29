package messages;

import Core.crypto.DigitalCertificate;
import Core.crypto.Asymmetric;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.text.ParseException;

public abstract class Message {

    public  static final int MESSAGE_LENGTH_FIELD_SIZE = 4;

    //Message Type Enumeration
    public enum Type{

        connectionRequest(1),
        connectionResponse(2),
        transferRequest(3),
        transferResponse( 4),
        balanceRequest(5),
        balanceResponse(6),
        keyExchange(7),
        CertSignRequest(8),
        CertResponse(9);


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

    }


    private final Type type;
    private final ByteBuffer data;

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

    //Master parse function
    public static Message parse(ByteBuffer buffer) throws ParseException{

        int length = buffer.getInt();
        if(length == 0){
            throw new ParseException("no size specified" , 0);
        }

        else if (length != buffer.remaining()){
            throw new ParseException("specified message size doesn't match the actual ", 0);
        }

        Type type = Type.get(buffer.get());

        if (type == null){
            throw new ParseException("Unknown message ID" , buffer.position() - 1);
        }

        switch (type){
            case  connectionRequest:
                return ConnectionRequest.parse(buffer.slice() , length);

            case connectionResponse:
                return ConnectionResponse.parse(buffer.slice(), length);

            case transferRequest:
                return TransactionRequest.parse(buffer.slice());

            case transferResponse:
                return TransactionResponse.parse(buffer.slice());

            case keyExchange:
                return KeyExchange.parse(buffer.slice());

            case CertSignRequest:
                return CertSignRequest.parse(buffer.slice(), length);

            case CertResponse:
                return CertResponse.parse(buffer.slice(), length);

            default:
                throw new IllegalStateException("the message type isnt defined");
        }

    }


    //Message Classes

    public static class ConnectionRequest extends  Message{

        public static final int BASE_Size = 5;

        int id;
        byte[] publickey;


        private ConnectionRequest(ByteBuffer buffer,int id , byte[] publickey){
            super(Type.connectionRequest , buffer);
            this.id = id;
            this.publickey = publickey;

        }

        public static ConnectionRequest parse(ByteBuffer buffer ,  int messageLength){

            int id = buffer.getInt();
            byte[] publickey = new byte[messageLength - ConnectionRequest.BASE_Size];
            buffer.get(publickey , 0 , publickey.length);

            return  new ConnectionRequest(buffer , id , publickey);


        }

        public static ByteBuffer  craft(int id , byte[] publickey){

            ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_LENGTH_FIELD_SIZE + ConnectionRequest.BASE_Size + publickey.length);
            buffer.putInt(ConnectionRequest.BASE_Size + publickey.length);
            buffer.put(Type.connectionRequest.getTypeByte());
            buffer.putInt(id); //add ID
            buffer.put(publickey , 0 , publickey.length);
            buffer.flip();

            return buffer;
        }




        public int getId(){

            return this.id;
        }

        public byte[] getPublicKey(){

            return this.publickey;
        }

    }

    public static class ConnectionResponse extends  Message{

        public static final int BASE_Size = 258;

        byte flag;
        byte[] Reason;
        byte[] publicKey;

        private ConnectionResponse(ByteBuffer buffer,byte flag , byte[] message , byte[] publicKey){
            super(Type.connectionResponse , buffer);
            this.flag = flag;
            this.Reason = message;
            this.publicKey = publicKey;

        }

        public static ConnectionResponse parse(ByteBuffer buffer, int pkeyBytesLength){

            byte flag  = buffer.get();
            byte[] message = new byte[256];
            buffer.get(message , 0 , message.length); //read into message array
            byte[] publicKey = new byte[pkeyBytesLength - BASE_Size];
            buffer.get(publicKey , 0 , publicKey.length);
            return new ConnectionResponse(buffer, flag, message, publicKey);

        }

        public static ByteBuffer  craft(byte flag , byte[] message , byte[] publicKey){

            ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_LENGTH_FIELD_SIZE + ConnectionResponse.BASE_Size + publicKey.length);
            buffer.putInt(ConnectionResponse.BASE_Size + publicKey.length);
            buffer.put(Type.connectionResponse.getTypeByte());
            buffer.put(flag);
            buffer.put(message , 0 , 256);
            buffer.put(publicKey , 0 , publicKey.length);
            buffer.flip();

            return buffer;
        }

        public byte getFlag(){
            return this.flag;
        }

        public String getMessage(){
            return  new String(this.Reason , StandardCharsets.UTF_8);
        }

        public byte[] getPublicKey(){
            return this.publicKey;
        }
    }

    public static class KeyExchange extends  Message{

        public static final int BASE_Size = 17;

        byte[] secretKey;

        private KeyExchange(ByteBuffer buffer, byte[] secretKey){
            super(Type.keyExchange , buffer);
            this.secretKey = secretKey;
        }

        public static KeyExchange parse(ByteBuffer buffer){
            byte[] secretKey = new byte[16];
            buffer.get(secretKey , 0 , secretKey.length);
            return  new KeyExchange(buffer, secretKey);
        }

        public static ByteBuffer craft(byte[] secretKey){

            ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_LENGTH_FIELD_SIZE + KeyExchange.BASE_Size);
            buffer.putInt(KeyExchange.BASE_Size);
            buffer.put(Type.keyExchange.getTypeByte());
            buffer.put(secretKey , 0 , 16);
            buffer.flip();

            return buffer;
        }

        public byte[] getSecretKey(){
            return secretKey;
        }
    }


    public static class TransactionRequest extends  Message{

        public static final int BASE_Size = 269;

        int id; //the id of the person we want to give money to

        double moneyAmount; // the amount of money we want to transfer

        byte[] reason; // the message explaining why we want to transfer money to that person

        byte[] message;

        private TransactionRequest(ByteBuffer buffer, int id, byte [] message, double moneyAmount /*, byte[] bytesMessage*/){
            super(Type.transferRequest , buffer);
            this.id = id;
            this.reason = message;
            this.moneyAmount = moneyAmount;
/*
            this.message = bytesMessage;
*/

        }

        public static TransactionRequest parse(ByteBuffer buffer){

            byte[] message = new byte[256];
            int id = buffer.getInt();
            double amount = buffer.getDouble();
            buffer.get(message , 0 , message.length);

           /* buffer.rewind();
            byte[] data = new byte[TransactionRequest.BASE_Size + TransactionRequest.MESSAGE_LENGTH_FIELD_SIZE];
            buffer.get(data , 0 , data.length);*/
            return  new TransactionRequest(buffer, id, message, amount/* ,data*/);

        }

        public static ByteBuffer craft(int id, double moneyAmount, byte [] message){

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

        public byte[] get_message(){

                return this.message;
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


    public static class CertSignRequest extends Message {
        public static final int BASE_Size = 5;

        int ownerLength;
        byte[] owner;
        byte[] publickey;

        private CertSignRequest(ByteBuffer data, byte[] owner, byte[] publickey){
            super(Type.CertSignRequest, data);
            this.ownerLength = owner.length;
            this.owner = owner;
            this.publickey = publickey;
        }

        public static CertSignRequest parse(ByteBuffer buffer, int messageLength){
            int ownerLength = buffer.getInt();

            byte[] owner = new byte[ownerLength];
            buffer.get(owner, 0, ownerLength);

            byte[] publickey = new byte[buffer.remaining()];
            buffer.get(publickey, 0, buffer.remaining());

            return new CertSignRequest(buffer, owner, publickey);
        }

        public static ByteBuffer craft(String owner, PublicKey publickey){
            byte[] ownerbytes = owner.getBytes(StandardCharsets.UTF_8);
            byte[] pkeyBytes = publickey.getEncoded();

            ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_LENGTH_FIELD_SIZE + BASE_Size + ownerbytes.length + pkeyBytes.length);
            buffer.putInt(BASE_Size  + ownerbytes.length + pkeyBytes.length);
            buffer.put(Type.CertSignRequest.getTypeByte());
            buffer.putInt(ownerbytes.length);
            buffer.put(ownerbytes);
            buffer.put(pkeyBytes);
            buffer.flip();
            return buffer;
        }

        public String getOwner(){
            return new String(this.owner, StandardCharsets.UTF_8);
        }

        public PublicKey getPublicKey(){
            return Asymmetric.rebuildPublicKey(this.publickey);
        }

    }

    public static class CertResponse extends Message{
        public static final int BASE_Size = 14;

        byte flag;
        DigitalCertificate certificate;

        private CertResponse(ByteBuffer data, byte flag, byte[] owner, byte[] issuer, byte[] publickey, byte[] signature){
            super(Type.CertResponse, data);
            String ownerStr = new String(owner, StandardCharsets.UTF_8);
            String issuerStr = new String(issuer, StandardCharsets.UTF_8);
            PublicKey pkey = Asymmetric.rebuildPublicKey(publickey);
            this.certificate = new DigitalCertificate(ownerStr, issuerStr, pkey, signature);
        }

        public static CertResponse parse(ByteBuffer buffer, int messageLength){
            byte flag = buffer.get();

            int ownerLength = buffer.getInt();
            int issuerLength = buffer.getInt();
            int publickeyLength = buffer.getInt();

            byte[] owner = new byte[ownerLength];
            buffer.get(owner, 0, ownerLength);

            byte[] issuer = new byte[issuerLength];
            buffer.get(issuer, 0, issuerLength);

            byte[] publickey = new byte[publickeyLength];
            buffer.get(publickey, 0, publickeyLength);

            byte[] signature = new byte[buffer.remaining()];
            buffer.get(signature, 0, buffer.remaining());

            return new CertResponse(buffer, flag, owner, issuer, publickey, signature);
        }

        public static ByteBuffer craft(DigitalCertificate cert, byte flag){
            ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_LENGTH_FIELD_SIZE + BASE_Size + cert.getCertAndSignBytes().length);
            buffer.putInt(BASE_Size + cert.getCertAndSignBytes().length);
            buffer.put(Type.CertResponse.getTypeByte());
            buffer.put(flag);
            buffer.putInt(cert.getOwner().length());
            buffer.putInt(cert.getIssuer().length());
            buffer.putInt(cert.getPublicKey().getEncoded().length);
            buffer.put(cert.getOwner().getBytes(StandardCharsets.UTF_8));
            buffer.put(cert.getIssuer().getBytes(StandardCharsets.UTF_8));
            buffer.put(cert.getPublicKey().getEncoded());
            buffer.put(cert.getSignature());
            buffer.flip();

            return buffer;
        }

        public DigitalCertificate getCertificate(){
            return this.certificate;
        }

        public byte getFlag(){
            return this.flag;
        }

    }

}
