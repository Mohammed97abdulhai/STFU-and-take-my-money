package Core.crypto;

import java.security.*;

public class DigitalSignature {

    public static byte[] sign(byte[] message , PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature signatureInstance = Signature.getInstance("SHA256withRSA");
        signatureInstance.initSign(privateKey);
        signatureInstance.update(message);

        byte[] signature = signatureInstance.sign();

        return signature;
    }


    public  static boolean verify(byte[] message , byte[] signature , PublicKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature signatureInstance = Signature.getInstance("SHA256withRSA");
        signatureInstance.initVerify(key);
        signatureInstance.update(message);

        return signatureInstance.verify(signature);
    }

}
