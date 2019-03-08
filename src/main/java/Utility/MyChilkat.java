package Utility;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MyChilkat {
    private static MyChilkat instance;
    private final String TAG = null;
    private SecretKeySpec secretKeySpec;
    private Cipher cipher;

    public MyChilkat() {
        String key = CommonFunc.generateSha("CallNative.getInstance().getNative1()");
        String secretKey = CommonFunc.generateSha(CommonFunc.getDateOnly() + key.toUpperCase()).toUpperCase();
        secretKey = secretKey.substring(0, 32);

        try {
            byte[] secret = secretKey.getBytes("UTF-8");
            secretKeySpec = new SecretKeySpec(secret, "AES");
            cipher = Cipher.getInstance("AES");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(TAG, "UnsupportedEncodingException:" + e.getMessage());
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            Log.e(TAG, "NoSuchPaddingException:" + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e(TAG, "NoSuchAlgorithmException:" + e.getMessage());
        }
//        String vector = "3FEA67D9DE3FE29962C2363276350833840CFABB";
//        StringBuffer buffer = new StringBuffer(vector);
//        buffer.reverse();
//        String reverse = buffer.toString();
//        String keyHex = vector + reverse.substring(0, 24);
    }

    public static MyChilkat getInstance() {
        if (instance == null) {
            instance = new MyChilkat();
        }

        return instance;
    }


    public String encrypString(String data) {

        byte[] encVal = new byte[0];
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);//,new IvParameterSpec(keyHex.substring(0, 16).getBytes("utf-8"))
            encVal = cipher.doFinal(data.getBytes());
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            Log.e(TAG, "InvalidKeyException:" + e.getMessage());
        }

        return Base64.encodeToString(encVal, Base64.DEFAULT);
    }

    public String decryptString(String data) {

        String decrypted = "";
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec); //, new IvParameterSpec(keyHex.substring(0, 16).getBytes("utf-8"))
            byte[] decodeValue = Base64.decode(data, Base64.DEFAULT);
            byte[] decVal = cipher.doFinal(decodeValue);
            decrypted = new String(decVal);
        }
        catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        catch (BadPaddingException e) {
            e.printStackTrace();
        }
        catch (InvalidKeyException e) {
            Log.e(TAG, "InvalidKeyException:" + e.getMessage());
        }

        return decrypted;
    }
}
