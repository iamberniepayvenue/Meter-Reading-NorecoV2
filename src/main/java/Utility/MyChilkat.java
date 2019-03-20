package Utility;

import android.util.Log;

public class MyChilkat {
    private static MyChilkat instance;
    private final String TAG = "MyChilkat";
    private String keyHex;
    private String ivHex;
    private com.chilkatsoft.CkCrypt2 crypt;

    public MyChilkat() {

        String key = CommonFunc.generateSha(CallNative.getInstance().getNative1());
        String secretKey = CommonFunc.generateSha(CallNative.getInstance().getNative4() + key.toUpperCase()).toUpperCase();


        StringBuffer buffer = new StringBuffer(secretKey);
        buffer.reverse();
        keyHex = secretKey + buffer.substring(0, 24);

        ivHex = CallNative.getInstance().getNative4();

        crypt = new com.chilkatsoft.CkCrypt2();

        boolean success = crypt.UnlockComponent(CallNative.getInstance().getNative2());
        if (!success) {
            Log.e(TAG, "chilkat: " + crypt.lastErrorText());
            return;
        }

        //Log.e(TAG,"keyhex: " + keyHex);
        //Log.e(TAG,"ivHex: " + ivHex);

        crypt.put_CryptAlgorithm("aes");
        crypt.put_CipherMode("cbc");
        crypt.put_KeyLength(256);
        crypt.put_PaddingScheme(0);
        crypt.put_EncodingMode("hex");
        crypt.SetEncodedIV(ivHex.substring(0, 32), "hex");
        crypt.SetEncodedKey(keyHex, "hex");
    }

    public static MyChilkat getInstance() {
        if (instance == null) {
            instance = new MyChilkat();
        }

        return instance;
    }


    public String encrypString(String data) {
        return crypt.encryptStringENC(data);

    }

    public String decryptString(String data) {

        return crypt.decryptStringENC(data);
    }
}
