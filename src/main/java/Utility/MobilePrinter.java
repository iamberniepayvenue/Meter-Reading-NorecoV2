package Utility;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.woosim.bt.WoosimPrinter;

import java.io.IOException;

import static com.payvenue.meterreader.MainActivity.whichPrinter;


public class MobilePrinter {

    Context c;
    private static MobilePrinter mobilePrinter;
    private static WoosimPrinter woosim;
    static private byte[] cardData;
    static private byte[] extractdata = new byte[300];
    static String EUC_KR = "EUC-KR";
    static int LINE_CHARS = 63;
    private static final String TAG = "MobilePrinter";
    private int device_tag = 0;


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
        if (woosim == null) {
            woosim = new WoosimPrinter();
            woosim.setHandle(acthandler);
        }
        return woosim;
    }

    public void setDeviceTag(int device_tag) {
        this.device_tag = device_tag;
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


    public void printText(String leftText, String rightText) {
        //byte[] init = {0x1b, '@'};
        //woosim.controlCommand(init, init.length);

//        if(whichPrinter.equalsIgnoreCase("bix")) {
//            LINE_CHARS = 47;
//        }

        if(device_tag == 1) {
            LINE_CHARS = 47;
        }

        int padding = LINE_CHARS - leftText.length() - rightText.length();
        String paddingChar = " ";
        for (int i = 0; i < padding; i++) {
            paddingChar = paddingChar.concat(" ");
        }

        if(leftText.equalsIgnoreCase("Total Current Due") || leftText.equalsIgnoreCase("TOTAL AMOUNT PAYABLE")
                || leftText.equalsIgnoreCase("Amount Export Due") || leftText.equalsIgnoreCase("NET BILL AMOUNT")) {
            woosim.saveSpool(EUC_KR, leftText + paddingChar + rightText, 1, false);
            woosim.printSpool(true);
        }else {
            woosim.saveSpool(EUC_KR, leftText + paddingChar + rightText, 0, false);
            woosim.printSpool(true);
        }
    }

    public void printTextExceptLeft(String leftText, String rightText) {

        int padding = LINE_CHARS - leftText.length() - rightText.length();
        String paddingChar = " ";
        for (int i = 0; i < padding; i++) {
            paddingChar = paddingChar.concat(" ");
        }

        woosim.saveSpool(EUC_KR, paddingChar + rightText, 0, false);
        woosim.printSpool(true);
    }

    public void printTextEmphasized(String leftText, String rightText) {

        int padding = 30 - leftText.length() - rightText.length();
        int font = 1;
        boolean emphasis = false;
        if(whichPrinter.equalsIgnoreCase("bix")) {
            padding = 46 - leftText.length() - rightText.length();
            emphasis = true;
            font = 0;
        }

        String paddingChar = " ";
        for (int i = 0; i < padding; i++) {
            paddingChar = paddingChar.concat(" ");
        }

        woosim.saveSpool(EUC_KR, leftText + paddingChar + rightText, font, emphasis);
        woosim.printSpool(true);
    }

    public void printTextEmphasized1(String leftText, String rightText) {
        int padding = 31 - leftText.length() - rightText.length();

        if(device_tag == 1) {
            padding = 46 - leftText.length() - rightText.length();
        }

        int font = 1;
        boolean emphasis = false;
//        if(whichPrinter.equalsIgnoreCase("bix")) {
//            padding = 46 - leftText.length() - rightText.length();
//            font = 0;
//            emphasis = true;
//        }

        String paddingChar = " ";
        for (int i = 0; i < padding; i++) {
            paddingChar = paddingChar.concat(" ");
        }

        woosim.saveSpool(EUC_KR, leftText + paddingChar + rightText, font, emphasis);
        woosim.printSpool(true);
    }

    public void printText(String text) {
        woosim.saveSpool(EUC_KR, text , 0, false);
        woosim.printSpool(true);
    }

    public void printTextBoldRight(String leftText, String rightText) {
        woosim.saveSpool(EUC_KR, leftText, 0, false);
        woosim.saveSpool(EUC_KR, rightText, 0, true);
        woosim.printSpool(true);
    }

    public void printText(String one,String two,String three,String four,int tag) {

        int padding;
        if(tag == 0) {
            padding = 37 - (one.length() + two.length() + three.length() + four.length());
        }else{
            //62
            padding = (30 - (one.length() + two.length() + three.length() + four.length()));
        }
        String paddingChar = " ";
        for (int i = 0; i < padding; i++) {
            paddingChar = paddingChar.concat(" ");
        }
        String text = one + paddingChar + two + paddingChar + three + paddingChar + four;
        woosim.saveSpool(EUC_KR, text, 0, false);
        woosim.printSpool(true);
    }

    public void printextEmphasized(String text) {
        int font = 2;
//        if(whichPrinter.equalsIgnoreCase("bix")) {
//            font = 1;
//        }

        woosim.saveSpool(EUC_KR,text,font,false);
        woosim.printSpool(true);
    }

    public void printextEmphasizedNormalFont(String text) {
        woosim.saveSpool(EUC_KR, text , 1, false);
        woosim.printSpool(true);
    }


    public void printBitmap(String path) {

        try {
            woosim.printBitmap(path);
            byte[] ff ={0x0c};
            woosim.controlCommand(ff, 1);
            woosim.printSpool(true);
        } catch (IOException e) {
            Log.e(TAG,"printBitmap : " + e.getMessage());
        }
    }

    public void printBMPByte(byte[] b,int length) {
        woosim.printBitmap(b,length);
        byte[] ff ={0x0c};
        woosim.controlCommand(ff, 1);
        woosim.printSpool(true);
    }

    public void barcode(String AccountID) {
        //byte[] bardode = WoosimBarcode.createBarcode(WoosimBarcode.CODEBAR,60,2,);
    }

    public int setConnection(String address) {
        int reVal = woosim.BTConnection(address, false);
        return reVal;
    }

    public void disconnect() {
        woosim.closeConnection();
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
