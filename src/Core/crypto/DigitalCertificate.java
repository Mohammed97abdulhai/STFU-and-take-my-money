package Core.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;


public class DigitalCertificate {

    private String owner;
    private String issuer;
    private PublicKey publicKey;
    private byte[] signature;

    public DigitalCertificate(String owner, String issuer, PublicKey publicKey){
        this.owner = owner;
        this.issuer = issuer;
        this.publicKey = publicKey;
    }

    public DigitalCertificate(String owner, String issuer, PublicKey publicKey, byte[] signature){
        this(owner, issuer, publicKey);
        this.signature = signature;
    }

    public byte[] getCertBytes(){
        byte[] ownerBytes = owner.getBytes(StandardCharsets.UTF_8);
        byte[] issuerBytes = issuer.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = publicKey.getEncoded();
        byte[] fullBytes = new byte[ownerBytes.length + issuerBytes.length + keyBytes.length];

        System.arraycopy(ownerBytes, 0, fullBytes, 0, ownerBytes.length);
        System.arraycopy(issuerBytes, 0, fullBytes, ownerBytes.length, issuerBytes.length);
        System.arraycopy(keyBytes, 0, fullBytes, ownerBytes.length + issuerBytes.length, keyBytes.length);

        return fullBytes;
    }

    public byte[] getCertAndSignBytes(){
        byte[] certBytes = getCertBytes();
        byte[] fullBytes = new byte[certBytes.length + signature.length];

        System.arraycopy(certBytes, 0, fullBytes, 0, certBytes.length);
        System.arraycopy(signature, 0, fullBytes, certBytes.length, signature.length);

        return fullBytes;
    }

    public byte[] getSignature(){
        return signature;
    }

    public String getOwner(){
        return owner;
    }

    public String getIssuer(){
        return issuer;
    }

    public PublicKey getPublicKey(){
        return publicKey;
    }

    public boolean isSigned(){
        return signature != null;
    }



    public static byte[] store(DigitalCertificate cert){
        byte[] certAndSignBytes = cert.getCertAndSignBytes();

        ByteBuffer buff = ByteBuffer.allocate(12 + certAndSignBytes.length);
        buff.putInt(cert.owner.length());
        buff.putInt(cert.issuer.length());
        buff.putInt(cert.publicKey.getEncoded().length);
        buff.put(certAndSignBytes);

        return buff.array();
    }

    public static DigitalCertificate load(byte[] certBytes){
        ByteBuffer buff = ByteBuffer.wrap(certBytes);

        int ownerLength = buff.getInt();
        int issuerLength = buff.getInt();
        int keyLength = buff.getInt();

        byte[] ownerBytes = new byte[ownerLength];
        buff.get(ownerBytes, 0, ownerLength);

        byte[] issuerBytes = new byte[issuerLength];
        buff.get(issuerBytes, 0, issuerLength);

        byte[] keyBytes = new byte[keyLength];
        buff.get(keyBytes, 0, keyLength);


        int signatureLength = buff.remaining();

        byte[] signature = new byte[signatureLength];
        buff.get(signature, 0, signatureLength);

        String owner = new String(ownerBytes, StandardCharsets.UTF_8);
        String issuer = new String(issuerBytes, StandardCharsets.UTF_8);
        PublicKey pkey = Asymmetric.rebuildPublicKey(keyBytes);

        return new DigitalCertificate(owner, issuer, pkey, signature);
    }

    public static boolean verify(DigitalCertificate cert, PublicKey issuerPublicKey, String expectedOwner){
        boolean authenticity;
        try{
            authenticity = DigitalSignature.verify(cert.getCertBytes(), cert.getSignature(), issuerPublicKey);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Failed to verify certificate (Verification Algorithm Failure)");
            authenticity = false;
        } catch (SignatureException e) {
            System.out.println("Failed to verify certificate (Incorrect Message, signature or Public key)");
            authenticity = false;
        } catch (InvalidKeyException e) {
            System.out.println("Failed to verify certificate (Invalid public key)");
            authenticity = false;
        }

        if(!expectedOwner.equals(cert.getOwner())){
            System.out.println("Expected owner does not match owner in certificate. (Possible MITM attack)");
            authenticity = false;
        }

        return authenticity;
    }

    public static void sign(DigitalCertificate cert, PrivateKey issuerPrivateKey){
        try {
            cert.signature = DigitalSignature.sign(cert.getCertBytes(), issuerPrivateKey);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Failed to sign certificate (Algorithm Failure");
        } catch (InvalidKeyException e) {
            System.out.println("Failed to sign certificate (Invalid private key)");
        } catch (SignatureException e) {
            System.out.println("Failed to sign certificate (Signature Creation Failure)");
        }
    }

}
