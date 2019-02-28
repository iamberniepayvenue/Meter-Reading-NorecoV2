package Utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.bxl.BXLConst;
import com.bxl.config.editor.BXLConfigLoader;

import java.nio.ByteBuffer;

import jpos.JposConst;
import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.config.JposEntry;
import jpos.events.DirectIOEvent;
import jpos.events.DirectIOListener;
import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;
import jpos.events.OutputCompleteEvent;
import jpos.events.OutputCompleteListener;
import jpos.events.StatusUpdateEvent;
import jpos.events.StatusUpdateListener;

public class NorecoBixolonPrinter implements StatusUpdateListener, ErrorListener, OutputCompleteListener, DirectIOListener {

    // ------------------- alignment ------------------- //
    public static int ALIGNMENT_LEFT = 1;
    public static int ALIGNMENT_CENTER = 2;
    public static int ALIGNMENT_RIGHT = 4;

    // ------------------- Text attribute ------------------- //
    public static int ATTRIBUTE_NORMAL = 0;
    public static int ATTRIBUTE_FONT_A = 1;
    public static int ATTRIBUTE_FONT_B = 2;
    public static int ATTRIBUTE_FONT_C = 4;
    public static int ATTRIBUTE_BOLD = 8;
    public static int ATTRIBUTE_UNDERLINE = 16;
    public static int ATTRIBUTE_REVERSE = 32;

    private final static int UTF_8 = 1;
    private final String TAG = "NorecoBixolonPrinter";
    private POSPrinter posPrinter = null;
    private BXLConfigLoader bxlConfigLoader = null;
    private Context context;
    private int mPortType;
    private String mAddress;
    public NorecoBixolonPrinter(Context context) {
        this.context = context;
        posPrinter = new POSPrinter(this.context);
        posPrinter.addStatusUpdateListener(this);
        posPrinter.addErrorListener(this);
        posPrinter.addOutputCompleteListener(this);
        posPrinter.addDirectIOListener(this);

        setCharacterSet();
        setBXLConfigLoader();
    }

    public void setBXLConfigLoader() {
        bxlConfigLoader = new BXLConfigLoader(context);
        try {
            bxlConfigLoader.openFile();
        } catch (Exception e) {
            bxlConfigLoader.newFile();
        }
    }

    @Override
    public void statusUpdateOccurred(StatusUpdateEvent statusUpdateEvent) {
        Log.e(TAG,"statusUpdateEvent: " + statusUpdateEvent.getStatus());
        getERMessage(statusUpdateEvent.getStatus());
    }

    @Override
    public void errorOccurred(ErrorEvent errorEvent) {
        Log.e(TAG,"errorEvent: " + errorEvent);
        getERMessage(errorEvent.getErrorCodeExtended());
    }

    @Override
    public void outputCompleteOccurred(OutputCompleteEvent outputCompleteEvent) {
        Log.e(TAG,"outputCompleteEvent: " + outputCompleteEvent.getOutputID());
    }

    @Override
    public void directIOOccurred(DirectIOEvent directIOEvent) {
        Log.e(TAG,"directIOEvent: " + directIOEvent.getObject());
    }


    public boolean printerOpen(int portType, String logicalName, String address, boolean isAsyncMode) {
        if (setTargetDevice(portType, logicalName, BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER, address)) {
            try {
                posPrinter.open(logicalName);
                posPrinter.claim(5000);
                posPrinter.setDeviceEnabled(true);
                posPrinter.setAsyncMode(isAsyncMode);

                mPortType = portType;
                mAddress = address;
            } catch (JposException e) {
                e.printStackTrace();
                Log.e(TAG,"printerOpen: "+ e.getMessage());
                try {
                    posPrinter.close();
                } catch (JposException e1) {
                    e1.printStackTrace();
                }

                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    private boolean setTargetDevice(int portType, String logicalName, int deviceCategory, String address) {
        try {
            for (Object entry : bxlConfigLoader.getEntries()) {
                JposEntry jposEntry = (JposEntry) entry;
                if (jposEntry.getLogicalName().equals(logicalName)) {
                    bxlConfigLoader.removeEntry(jposEntry.getLogicalName());
                }
            }

            bxlConfigLoader.addEntry(logicalName, deviceCategory, getProductName(logicalName), portType, address);

            bxlConfigLoader.saveFile();
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    private String getProductName(String name) {
        String productName = BXLConfigLoader.PRODUCT_NAME_SPP_R200II;

        if ((name.indexOf("SPP-R200II") >= 0)) {
            if (name.length() > 10) {
                if (name.substring(10, 11).equals("I")) {
                    productName = BXLConfigLoader.PRODUCT_NAME_SPP_R200III;
                }
            }
        } else if ((name.indexOf("SPP-R210") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SPP_R210;
        } else if ((name.indexOf("SPP-R215") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SPP_R215;
        } else if ((name.indexOf("SPP-R220") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SPP_R220;
        } else if ((name.indexOf("SPP-R300") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SPP_R300;
        } else if ((name.indexOf("SPP-R310") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SPP_R310;
        } else if ((name.indexOf("SPP-R318") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SPP_R318;
        } else if ((name.indexOf("SPP-R400") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SPP_R400;
        } else if ((name.indexOf("SPP-R410") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SPP_R410;
        } else if ((name.indexOf("SPP-R418") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SPP_R418;
        } else if ((name.indexOf("SRP-350III") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SRP_350III;
        } else if ((name.indexOf("SRP-352III") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SRP_352III;
        } else if ((name.indexOf("SRP-350plusIII") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SRP_350PLUSIII;
        } else if ((name.indexOf("SRP-352plusIII") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SRP_352PLUSIII;
        } else if ((name.indexOf("SRP-380") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SRP_380;
        } else if ((name.indexOf("SRP-382") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SRP_382;
        } else if ((name.indexOf("SRP-383") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SRP_383;
        } else if ((name.indexOf("SRP-340II") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SRP_340II;
        } else if ((name.indexOf("SRP-342II") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SRP_342II;
        } else if ((name.indexOf("SRP-Q300") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SRP_Q300;
        } else if ((name.indexOf("SRP-Q302") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SRP_Q302;
        } else if ((name.indexOf("SRP-QE300") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SRP_QE300;
        } else if ((name.indexOf("SRP-QE302") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SRP_QE302;
        } else if ((name.indexOf("SRP-E300") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SRP_E300;
        } else if ((name.indexOf("SRP-E302") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SRP_E302;
        } else if ((name.indexOf("SRP-330II") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SRP_330II;
        } else if ((name.indexOf("SRP-332II") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SRP_332II;
        } else if ((name.indexOf("SRP-S300") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SRP_S300;
        } else if ((name.indexOf("SRP-F310II") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SRP_F310II;
        } else if ((name.indexOf("SRP-F312II") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SRP_F312II;
        } else if ((name.indexOf("SRP-F313II") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_SRP_F313II;
        } else if ((name.indexOf("SRP-275III") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SRP_275III;
        } else if ((name.indexOf("BK3-3") >= 0)) {
            //productName = BXLConfigLoader.PRODUCT_NAME_BK3_3;
        } else if ((name.indexOf("MSR") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_MSR;
        } else if ((name.indexOf("SmartCardRW") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_SMART_CARD_RW;
        } else if ((name.indexOf("CashDrawer") >= 0)) {
            productName = BXLConfigLoader.PRODUCT_NAME_CASH_DRAWER;
        }

        return productName;
    }

    public boolean printerClose() {
        try {
            if (posPrinter.getClaimed()) {
                posPrinter.setDeviceEnabled(false);
                posPrinter.close();
            }
        } catch (JposException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void getERMessage(int status) {
        switch (status) {
            case POSPrinterConst.JPOS_EPTR_COVER_OPEN:
                //return "Cover open";
                toastMessage("Cover open");
                break;
            case POSPrinterConst.JPOS_EPTR_REC_EMPTY:
                //return "Paper empty";
                toastMessage("Paper empty");
                break;
            case JposConst.JPOS_SUE_POWER_OFF_OFFLINE:
               // return "Power off";
                toastMessage("Power off");
                break;
            default:
               // return "Unknown";
        }
    }

    public boolean beginTransactionPrint() {
        try {
            if (!posPrinter.getDeviceEnabled()) {
                return false;
            }

            posPrinter.transactionPrint(POSPrinterConst.PTR_S_RECEIPT, POSPrinterConst.PTR_TP_TRANSACTION);
        } catch (JposException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    public boolean endTransactionPrint() {
        try {
            if (!posPrinter.getDeviceEnabled()) {
                return false;
            }

            posPrinter.transactionPrint(POSPrinterConst.PTR_S_RECEIPT, POSPrinterConst.PTR_TP_NORMAL);
        } catch (JposException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    public int getCharacterSet() {
        int cs = -1;
        try {
            cs = posPrinter.getCharacterSet();
        } catch (JposException e) {
            e.printStackTrace();
        }

        return cs;
    }

    public boolean setCharacterSet() {
        try {
            posPrinter.setCharacterSet(BXLConst.CS_437_USA_STANDARD_EUROPE);
            posPrinter.setCharacterEncoding(UTF_8);
        } catch (JposException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    public void toastMessage(String message) {
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }

    public boolean printImage(Bitmap path, int width , int alignment, int brightness) {
        boolean ret = true;

        try {
            if (!posPrinter.getDeviceEnabled()) {
                return false;
            }

//            posPrinter.storeImage(path,160,32,1);
//            posPrinter.displayImage(1,0,0);
//            posPrinter.clearImage(true,1);

            if (alignment == ALIGNMENT_LEFT) {
                alignment = POSPrinterConst.PTR_BM_LEFT;
            } else if (alignment == ALIGNMENT_CENTER) {
                alignment = POSPrinterConst.PTR_BM_CENTER;
            } else {
                alignment = POSPrinterConst.PTR_BM_RIGHT;
            }

            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.put((byte) POSPrinterConst.PTR_S_RECEIPT);
            buffer.put((byte) brightness); // brightness
            buffer.put((byte) 0x01); // compress
            buffer.put((byte) 0x00); // Reserve
//            Log.e(TAG,"printImage: "+ path);
            Log.e(TAG,"width: "+ width);
            posPrinter.printBitmap(buffer.getInt(0), path, width , alignment);
        } catch (JposException e) {
            e.printStackTrace();
            Log.e(TAG,"printImage JposException: "+ e.getMessage());
            ret = false;
        }

        return ret;
    }

    public boolean startPageMode(int xPos, int yPos, int width, int height, int direction) {
        try {
            if (!posPrinter.getDeviceEnabled()) {
                return false;
            }

            // "x,y,w,h"
            String area = xPos + "," + yPos + "," + width + "," + height;
            posPrinter.setPageModePrintArea(area);

            // LEFT_TO_RIGHT = 1;
            // BOTTOM_TO_TOP = 2;
            // RIGHT_TO_LEFT = 3;
            // TOP_TO_BOTTOM = 4;
            posPrinter.setPageModePrintDirection(direction);
            posPrinter.pageModePrint(POSPrinterConst.PTR_PM_PAGE_MODE);
        } catch (JposException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    public boolean endPageMode(boolean isLabelMode) {
        try {
            if (!posPrinter.getDeviceEnabled()) {
                return false;
            }

            posPrinter.pageModePrint(POSPrinterConst.PTR_PM_NORMAL);
            if (isLabelMode) {

            }
        } catch (JposException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    public boolean printext(String data) {
        Log.e(TAG,data);
        boolean ret = true;
        try {
            if (!posPrinter.getDeviceEnabled()) {
                Log.e(TAG,"printext: device not enabled");
                return false;
            }

            String area = 0 +"," + 0 + "," + 384 + "," + 1200;
            posPrinter.setPageModePrintArea(area);
            posPrinter.setPageModePrintDirection(POSPrinterConst.PTR_PD_LEFT_TO_RIGHT);
            posPrinter.pageModePrint(POSPrinterConst.PTR_PM_PAGE_MODE);
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, data);
            posPrinter.pageModePrint(POSPrinterConst.PTR_PM_NORMAL);
        } catch (JposException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG,"printext: "+ e.getMessage());
            ret = false;
        }

        return ret;
    }

    public boolean printText(String data, int alignment, int attribute, int textSize) {
        boolean ret = true;

        try {
            if (!posPrinter.getDeviceEnabled()) {
                return false;
            }

            String strOption = EscapeSequence.getString(0);

            if ((alignment & ALIGNMENT_LEFT) == ALIGNMENT_LEFT) {
                strOption += EscapeSequence.getString(4);
            }

            if ((alignment & ALIGNMENT_CENTER) == ALIGNMENT_CENTER) {
                strOption += EscapeSequence.getString(5);
            }

            if ((alignment & ALIGNMENT_RIGHT) == ALIGNMENT_RIGHT) {
                strOption += EscapeSequence.getString(6);
            }

            if ((attribute & ATTRIBUTE_FONT_A) == ATTRIBUTE_FONT_A) {
                strOption += EscapeSequence.getString(1);
            }

            if ((attribute & ATTRIBUTE_FONT_B) == ATTRIBUTE_FONT_B) {
                strOption += EscapeSequence.getString(2);
            }

            if ((attribute & ATTRIBUTE_FONT_C) == ATTRIBUTE_FONT_C) {
                strOption += EscapeSequence.getString(3);
            }

            if ((attribute & ATTRIBUTE_BOLD) == ATTRIBUTE_BOLD) {
                strOption += EscapeSequence.getString(7);
            }

            if ((attribute & ATTRIBUTE_UNDERLINE) == ATTRIBUTE_UNDERLINE) {
                strOption += EscapeSequence.getString(9);
            }

            if ((attribute & ATTRIBUTE_REVERSE) == ATTRIBUTE_REVERSE) {
                strOption += EscapeSequence.getString(11);
            }

            switch (textSize) {
                case 1:
                    strOption += EscapeSequence.getString(17);
                    strOption += EscapeSequence.getString(25);
                    break;
                case 2:
                    strOption += EscapeSequence.getString(18);
                    strOption += EscapeSequence.getString(26);
                    break;
                case 3:
                    strOption += EscapeSequence.getString(19);
                    strOption += EscapeSequence.getString(27);
                    break;
                case 4:
                    strOption += EscapeSequence.getString(20);
                    strOption += EscapeSequence.getString(28);
                    break;
                case 5:
                    strOption += EscapeSequence.getString(21);
                    strOption += EscapeSequence.getString(29);
                    break;
                case 6:
                    strOption += EscapeSequence.getString(22);
                    strOption += EscapeSequence.getString(30);
                    break;
                case 7:
                    strOption += EscapeSequence.getString(23);
                    strOption += EscapeSequence.getString(31);
                    break;
                case 8:
                    strOption += EscapeSequence.getString(24);
                    strOption += EscapeSequence.getString(32);
                    break;
                default:
                    strOption += EscapeSequence.getString(17);
                    strOption += EscapeSequence.getString(25);
                    break;
            }

            Log.e(TAG,"data: "+ strOption + data);
            //String area = 0 +"," + 0 + "," + 384 + "," + 1200;
            //posPrinter.setPageModePrintArea(area);
            //posPrinter.setPageModePrintDirection(POSPrinterConst.PTR_PD_LEFT_TO_RIGHT);
            //posPrinter.pageModePrint(POSPrinterConst.PTR_PM_PAGE_MODE);
            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, strOption + data);
            //posPrinter.pageModePrint(POSPrinterConst.PTR_PM_NORMAL);
        } catch (JposException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG,"printext: "+ e.getMessage());
            ret = false;
        }

        return ret;
    }
}
