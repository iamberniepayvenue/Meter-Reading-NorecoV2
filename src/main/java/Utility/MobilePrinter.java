package Utility;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.woosim.bt.WoosimPrinter;


public class MobilePrinter {

    Context c;
    private static MobilePrinter mobilePrinter;
    private static WoosimPrinter woosim;
    //private static Handler handler;
    static private byte[] cardData;
    static private byte[] extractdata = new byte[300];
    static String EUC_KR = "EUC-KR";
    static final int LINE_CHARS = 63;
    private static final String TAG = "MobilePrinter";
    private static Thread thread;


    public MobilePrinter(Context c) {
        this.c = c;
        woosim = getWoosimPrinter();
    }

    public static synchronized MobilePrinter getInstance(Context c) {
        if (mobilePrinter == null) {
            mobilePrinter = new MobilePrinter(c);
        }
        return mobilePrinter;
    }

    private WoosimPrinter getWoosimPrinter() {
        boolean isWoosim = (woosim == null) ? true : false;
        //Log.e(TAG,"getWoosimPrinter: " + isWoosim);
        if (woosim == null) {
            woosim = new WoosimPrinter();
            woosim.setHandle(acthandler);
        }
        return woosim;
    }

    public Handler acthandler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == 0x01) {
                Log.e("+++Activity+++", "******0x01");
                Object obj1 = msg.obj;
                cardData = (byte[]) obj1;
                ToastMessage();
            } else if (msg.what == 0x02) {
                //ardData[msg.arg1] = (byte) msg.arg2;
                Log.e("+++Activity+++", "MSRFAIL: [" + msg.arg1 + "]: ");
            } else if (msg.what == 0x03) {
                Log.e("+++Activity+++", "******EOT");
            } else if (msg.what == 0x04) {
                Log.e("+++Activity+++", "******ETX");
            } else if (msg.what == 0x05) {
                Log.e("+++Activity+++", "******NACK");
            }
        }
    };

//    private static Handler getHandler() {
//        final Handler[] handler = {null};
//        boolean ishandler = (handler[0] ==null) ? true : false;
//        Log.e(TAG,"handler ? : " + ishandler);
//        if (handler[0] == null) {
//            thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.e(TAG,"run thread");
//                    Looper.prepare();
//                    handler[0] = new Handler() {
//                        @Override
//                        public void handleMessage(Message msg) {
//                            if (msg.what == 0x01) {
//                                Log.e("+++Activity+++", "******0x01");
//                                Object obj1 = msg.obj;
//                                cardData = (byte[]) obj1;
//                                ToastMessage();
//                            } else if (msg.what == 0x02) {
//                                //ardData[msg.arg1] = (byte) msg.arg2;
//                                Log.e("+++Activity+++", "MSRFAIL: [" + msg.arg1 + "]: ");
//                            } else if (msg.what == 0x03) {
//                                Log.e("+++Activity+++", "******EOT");
//                            } else if (msg.what == 0x04) {
//                                Log.e("+++Activity+++", "******ETX");
//                            } else if (msg.what == 0x05) {
//                                Log.e("+++Activity+++", "******NACK");
//                            }
//
//                            Log.e(TAG, "handle message: " + msg.what);
//
//                        }
//                    };
//                    Looper.loop();
//                }
//            });
//        }
//        return handler[0];
//    }

    public void printText(String leftText, String rightText) {

//        byte[] init = {0x1b, '@'};
//        woosim.controlCommand(init, init.length);

        //Log.e(TAG,"printing: " + leftText + ":  " + rightText);
        boolean isWoosim = (woosim == null) ? true : false;
        Log.e(TAG,"MobilePrinter is null? " + isWoosim);
        int padding = LINE_CHARS - leftText.length() - rightText.length();
        String paddingChar = " ";
        for (int i = 0; i < padding; i++) {
            paddingChar = paddingChar.concat(" ");
        }

        int pool = woosim.saveSpool(EUC_KR, leftText + paddingChar + rightText, 0, false);
        int print = woosim.printSpool(true);

        //Log.e(TAG,"save spool: " + pool);
        //Log.e(TAG,"print spool: " + print);

    }

    public void printText(String text) {
        woosim.saveSpool(EUC_KR, text , 0, false);
        woosim.printSpool(true);
    }

    public int setConnection(String address) {
        boolean isWoosim = (woosim == null) ? true : false;
        //Log.e(TAG,"setConnection-MobilePrinter is null? " + isWoosim);
        int reVal = woosim.BTConnection(address, false);

        return reVal;
    }

    private static void ToastMessage() {

        byte[] track1Data = new byte[76];
        byte[] track2Data = new byte[37];
        byte[] track3Data = new byte[104];

        int dataLength = woosim.extractCardData(cardData, extractdata);
        int i = 0, j = 0, k = 0;
        if (dataLength == 76) {
            String str = new String(extractdata);
        } else if (dataLength == 37) {
            String str = new String(extractdata);

        } else if (dataLength == 104) {
            String str = new String(extractdata);
        }
        //1,2track
        else if (dataLength == 113) {
            Log.e("+++Activitiy+++", "dataLength: " + dataLength);
            for (i = 0; i < 113; i++) {
                if (i < 76) {
                    track1Data[i] = extractdata[i];
                } else {
                    track2Data[j++] = extractdata[i];
                }
            }
        }
        //1,3track
        else if (dataLength == 180) {
            for (i = 0; i < 180; i++) {
                if (i < 76) {
                    track1Data[i] = extractdata[i];
                } else {
                    track3Data[j++] = extractdata[i];
                }
            }


        }
        //2,3track
        else if (dataLength == 141) {
            for (i = 0; i < 141; i++) {
                if (i < 37) {
                    track2Data[i] = extractdata[i];
                } else {
                    track3Data[j++] = extractdata[i];
                }
            }

        }
        //1,2,3track
        else if (dataLength == 217) {
            for (i = 0; i < 217; i++) {
                if (i < 76) {
                    track1Data[i] = extractdata[i];
                } else if (i >= 76 && i < 113) {
                    track2Data[j++] = extractdata[i];
                } else {
                    track3Data[k++] = extractdata[i];
                }
            }

            String str1 = new String(track1Data);
            String str2 = new String(track2Data);
            String str3 = new String(track3Data);

        }

    }
}
