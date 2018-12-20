package Utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.bixolon.printer.BixolonPrinter;
import com.payvenue.meterreader.MainActivity;
import com.payvenue.meterreader.R;


public class BixolonPrinterClass {

    private static BixolonPrinterClass mInstance;
    private static Context context;
    public static BixolonPrinter bixolonPrinter;
    public static Boolean connectedPrinter = false;
    private static final int LINE_CHARS = 42;
    static private byte[] cardData;
    static private byte[] extractdata = new byte[300];
    private static final String TAG = "BixolonPrinterClass";


    public BixolonPrinterClass(Context mContext) {
        context = mContext;
        bixolonPrinter = getBixolonPrinter();
    }

    public static synchronized BixolonPrinterClass newInstance(Context context) {

        if(mInstance == null) {
            Log.e("here","here");
            mInstance = new BixolonPrinterClass(context);
        }

        return mInstance;
    }

    public static BixolonPrinter getBixolonPrinter() {
        if(bixolonPrinter == null) {
            bixolonPrinter = new BixolonPrinter(context,handler,null);
        }
        return bixolonPrinter;
    }

    @SuppressLint("HandlerLeak")
    private static final Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
             Log.e("Handler", msg.what + " " + msg.arg1 + " " + msg.arg2);

            switch (msg.what) {

                case BixolonPrinter.MESSAGE_STATE_CHANGE:
                    Log.e("Handler", "BixolonPrinter.MESSAGE_STATE_CHANGE");
                    switch (msg.arg1) {
                        case BixolonPrinter.STATE_CONNECTED:
                            MainActivity.mIsConnected = true;
                            Log.e("Handler", "BixolonPrinter.STATE_CONNECTED");
                            Toast.makeText(context,"SUCCESS CONNECTION!",Toast.LENGTH_SHORT).show();
                            connectedPrinter = true;
                            break;

                        case BixolonPrinter.STATE_CONNECTING:
                            Log.e("Handler", "BixolonPrinter.STATE_CONNECTING");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(!connectedPrinter) {
                                        Toast.makeText(context,"Please restart printer device",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            },1000);

                            connectedPrinter = false;
                            break;

                        case BixolonPrinter.STATE_NONE:
                            Log.e("Handler", "BixolonPrinter.STATE_NONE");
                            connectedPrinter = false;
                            break;
                    }
                    break;

                case BixolonPrinter.MESSAGE_WRITE:
                    switch (msg.arg1) {
                        case BixolonPrinter.PROCESS_SET_SINGLE_BYTE_FONT:
                            Log.e("Handler", "BixolonPrinter.PROCESS_SET_SINGLE_BYTE_FONT");
                            break;

                        case BixolonPrinter.PROCESS_SET_DOUBLE_BYTE_FONT:
                            Log.e("Handler", "BixolonPrinter.PROCESS_SET_DOUBLE_BYTE_FONT");
                            break;

                        case BixolonPrinter.PROCESS_DEFINE_NV_IMAGE:
                            Log.e("Handler", "BixolonPrinter.PROCESS_DEFINE_NV_IMAGE");
                            break;

                        case BixolonPrinter.PROCESS_REMOVE_NV_IMAGE:
                            Log.e("Handler", "BixolonPrinter.PROCESS_REMOVE_NV_IMAGE");
                            break;

                        case BixolonPrinter.PROCESS_UPDATE_FIRMWARE:
                            Log.e("Handler", "BixolonPrinter.PROCESS_UPDATE_FIRMWARE");
                            break;
                    }
                    break;

                case BixolonPrinter.MESSAGE_READ:
                    Log.e("Handler", "BixolonPrinter.MESSAGE_READ");
                    break;

                case BixolonPrinter.MESSAGE_DEVICE_NAME:
                    Log.e("Handler", "BixolonPrinter.MESSAGE_DEVICE_NAME - " + msg.getData().getString(BixolonPrinter.KEY_STRING_DEVICE_NAME));
                    break;

                case BixolonPrinter.MESSAGE_TOAST:
                    Log.e("Handler", "BixolonPrinter.MESSAGE_TOAST - " + msg.getData().getString("toast"));
                    Toast.makeText(context, msg.getData().getString("toast"), Toast.LENGTH_SHORT).show();
                    break;

                // The list of paired printers
                case BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
                    Log.e("Handler", "BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET");
                    if (msg.obj == null) {

                    } else {

                    }
                    break;

                case BixolonPrinter.MESSAGE_PRINT_COMPLETE:
                    Log.e("Handler", "BixolonPrinter.MESSAGE_PRINT_COMPLETE");
                    break;

                case BixolonPrinter.MESSAGE_COMPLETE_PROCESS_BITMAP:
                    Log.e("Handler", "BixolonPrinter.MESSAGE_COMPLETE_PROCESS_BITMAP");
                    break;

                case BixolonPrinter.MESSAGE_USB_DEVICE_SET:
                    Log.e("Handler", "BixolonPrinter.MESSAGE_USB_DEVICE_SET");
                    if (msg.obj == null) {
                        Toast.makeText(context, "No connected device", Toast.LENGTH_SHORT).show();
                    } else {
                        // DialogManager.showUsbDialog(MainActivity.this,
                        // (Set<UsbDevice>) msg.obj, mUsbReceiver);
                    }
                    break;

                case BixolonPrinter.MESSAGE_NETWORK_DEVICE_SET:
                    Log.e("Handler", "BixolonPrinter.MESSAGE_NETWORK_DEVICE_SET");
                    if (msg.obj == null) {

                    }
                    // DialogManager.showNetworkDialog(PrintingActivity.this, (Set<String>) msg.obj);
                    break;
            }
        }
    };

    public void setConnection(String address) {
        bixolonPrinter.connect(address);
    }

    public void printText(String textToPrint,int size) {
        bixolonPrinter.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
        bixolonPrinter.printText(textToPrint,BixolonPrinter.ALIGNMENT_LEFT,BixolonPrinter.TEXT_ATTRIBUTE_FONT_A,size,false);
    }
    public void printTextBoldRight(String leftText, String rightText) {
        bixolonPrinter.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
        bixolonPrinter.printText(leftText+"\n",BixolonPrinter.ALIGNMENT_LEFT,BixolonPrinter.TEXT_ATTRIBUTE_FONT_A,0,false);
        bixolonPrinter.printText(rightText+"\n",BixolonPrinter.ALIGNMENT_LEFT,BixolonPrinter.TEXT_ATTRIBUTE_FONT_B,0,false);
    }

    public void printNextLine(int lines) {
        bixolonPrinter.lineFeed(lines,false);
    }
    public void printText(String leftText, String rightText) {
        int LINE_CHARS = 47;
        int padding = LINE_CHARS - leftText.length() - rightText.length();
        String paddingChar = " ";
        for (int i = 0; i < padding; i++) {
            paddingChar = paddingChar.concat(" ");
        }
        String toPrint = leftText + paddingChar + rightText;
        if(leftText.equalsIgnoreCase("Total Current Due") || leftText.equalsIgnoreCase("TOTAL AMOUNT PAYABLE")
                || leftText.equalsIgnoreCase("Amount Export Due") || leftText.equalsIgnoreCase("NET BILL AMOUNT")) {
            printText(toPrint,BixolonPrinter.TEXT_SIZE_HORIZONTAL2);
        }else {
            printText(toPrint,BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
        }
    }

    public void printBitmap() {
        bixolonPrinter.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.noreco);
        bixolonPrinter.printBitmap(bitmap,BixolonPrinter.ALIGNMENT_CENTER,600, 50, false);
//        Thread t = new Thread() {
//            @Override
//            public void run() {
//                Looper.prepare();
//                try {
//                    bixolonPrinter.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
//                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.noreco);
//                    bixolonPrinter.printBitmap(bitmap,BixolonPrinter.ALIGNMENT_CENTER,600, 50, false);
//                    bixolonPrinter.lineFeed(2,false);
//                    Log.e(TAG,"printBitmap");
//                    mListener.afterPrint(true);
//                }catch (Exception e){
//                    Log.e(TAG,"Printing: "+ e.getMessage());
//                    mListener.afterPrint(false);
//                }
//                Looper.loop();
//            }
//        };
//        t.start();
    }
}
