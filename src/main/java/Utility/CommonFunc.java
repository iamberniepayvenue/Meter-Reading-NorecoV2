package Utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.payvenue.meterreader.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by andrewlaurienrsocia on 22/06/2017.
 */

public class CommonFunc {

    public static double roundOff(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    public static double toDigit(String todigit) {
        return Float.parseFloat(todigit);
    }

    public static float toFloat(String todigit) {
        return Float.parseFloat(todigit);
    }

    public static float round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (float) tmp / factor;
    }

    public static boolean isLegalDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString().toLowerCase();
                //return "00:54:06:b7:28:2c";
            }
        } catch (Exception ex) {
        }
        return "";
    }

    public static String getDateOnly() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getDateComplete() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE d MMM yyyy HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getTimeNow(){
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }


    public double convertDiscount(double amount) {

        double result;
        // if(amount > 0){
        result = amount * -1;
        // }
        // else{
        // result = amount;
        // }
        return result;
    }

    public static String getBillMonth() {
        String billmonth;
        Calendar rightNow = Calendar.getInstance();

        String myear;
        int mymonth;
        mymonth = rightNow.get(Calendar.MONTH) + 1;
        myear = String.valueOf(rightNow.get(Calendar.YEAR)).substring(2);

        billmonth = mymonth + "" + myear;
        if (mymonth < 10) {
            billmonth = "0" + mymonth + "" + myear;
        }

        return billmonth;
    }

    private static String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);

    }

    public static Calendar stringToCalendar(String dateString) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        try {
            date = formatter.parse(dateString);
            cal.setTime(date);
        } catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cal;
    }

    public static String getElapsedDaysText(Calendar c1, Calendar c2) {
        long milliSeconds1 = c1.getTimeInMillis();
        long milliSeconds2 = c2.getTimeInMillis();
        long periodSeconds = (milliSeconds2 - milliSeconds1) / 1000;
        long elapsedDays = periodSeconds / 60 / 60 / 24;
        String elapsedDaysText = String.format("%d days ago", elapsedDays);
        return elapsedDaysText;
    }

    public static long getDateDifference(Calendar c1, Calendar c2) {
        long milliSeconds1 = c1.getTimeInMillis();
        long milliSeconds2 = c2.getTimeInMillis();
        long periodSeconds = (milliSeconds2 - milliSeconds1) / 1000;
        long elapsedDays = periodSeconds / 60 / 60 / 24;
        //String elapsedDaysText = String.format("%d days ago", elapsedDays);
        return elapsedDays;
    }



    public static String calcComponentAmount(float rate, double consumption) {
        DecimalFormat df = new DecimalFormat("#.####");
        double res = rate * consumption;
        return df.format(res);

        //float res = (float)rate * (float)consumption ;
        //return String.valueOf(res);
    }


    public static double calcComponentAmounthigher(float rate, double consumption) {
        DecimalFormat df = new DecimalFormat("#.####");
        double res = rate * consumption;
//        Log.e("CommonFunc","rate :"+rate);
//        Log.e("CommonFunc","consumption :"+consumption);
//        Log.e("CommonFunc","calcComponentAmounthigher :"+ res);
//        Log.e("CommonFunc","calcComponentAmounthigher :"+df.format(res));
        return  round(res,4);//df.format(res);

    }

    public static float calcComponentTax(String isvatable, float amount, float ratemultiplier) {

        float result = 0;
        if (isvatable.equalsIgnoreCase("Yes")) {
            ratemultiplier = ratemultiplier / 100;
            result = amount * ratemultiplier;
            return Math.round(result);
        }
        return result;
    }

    public static boolean isValidDate(String date) {
        boolean v = false;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {String d = "8/28/2018";
            Date strDate = sdf.parse(date);
            if(new Date().before(strDate)) {
                v= true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("CommonFunc","date err :" + e.getMessage());
        }

        return v;
    }

    public static String changeDateFormat(String sourcedatevalue) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date sourceDate = null;
        try {
            sourceDate = dateFormat.parse(sourcedatevalue);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat targetFormat = new SimpleDateFormat("MM/dd/yyyy");
        return targetFormat.format(sourceDate);
    }

    public static String monthAbrev(String billMonth) {
        String sub = billMonth;//billMonth.substring(0,2);
        String val = "";
        switch (sub) {
            case "01":
            case "1":
                val = "JAN";
                break;
            case "02":
            case "2":
                val = "FEB";
                break;
            case "03":
            case "3":
                val = "MAR";
                break;
            case "04":
            case "4":
                val = "APR";
                break;
            case "05":
            case "5":
                val = "MAY";
                break;
            case "06":
            case "6":
                val = "JUN";
                break;
            case "07":
            case "7":
                val = "JUL";
                break;
            case "08":
            case "8":
                val = "AUG";
                break;
            case "09":
            case "9":
                val = "SEPT";
                break;
            case "10":
                val = "OCT";
                break;
            case "11":
                val = "NOV";
                break;
            case "12":
                val = "DEC";
                break;
        }
        return val;
    }

    public static void createExternalStoragePrivateFile(Context context) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            File file = new File(context.getExternalFilesDir(
                    null), "noreco_logo.bmp");
            try {
                //Bitmap bitmap = BitmapFactory.decodeResource(context.getApplicationContext().getResources(),R.drawable.noreco_logo);

                @SuppressLint("ResourceType")
                InputStream is = context.getResources().openRawResource(R.drawable.noreco);
                OutputStream os = new FileOutputStream(file);
                byte[] data = new byte[is.available()];
                is.read(data);
                os.write(data);
                is.close();
                os.close();
            } catch (FileNotFoundException e) {
                Log.e("CommonFunc","createExternalStoragePrivateFile: " + e.getMessage());
            }catch (IOException e) {
                Log.e("CommonFunc","createExternalStoragePrivateFile(IOException): " + e.getMessage());
            }
        }
    }

    public static File getPrivateAlbumStorageDir(Context context, String albumName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                null), albumName);
        if (!file.mkdirs()) {
            Log.e("CommonFunc", "Directory not created");
        }
        return file;
    }

    public static boolean hasExternalStoragePrivateFile(Context context, String albumName) {
        // Get path for the file on external storage.  If external
        // storage is not currently mounted this will fail.
        File file = new File(context.getExternalFilesDir(null), albumName);
        if (file != null) {
            return file.exists();
        }
        return false;
    }

    /**
     * Check if the connection is fast
     * @param type
     * @param subType
     * @return
     */
    public static boolean isConnectionFast(int type, int subType){
        if(type==ConnectivityManager.TYPE_WIFI){
            return true;
        }else if(type==ConnectivityManager.TYPE_MOBILE){
            switch(subType){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                // NOT AVAILABLE YET IN API LEVEL 7
//                case Connectivity.NETWORK_TYPE_EHRPD:
//                    return true; // ~ 1-2 Mbps
//                case Connectivity.NETWORK_TYPE_EVDO_B:
//                    return true; // ~ 5 Mbps
//                case Connectivity.NETWORK_TYPE_HSPAP:
//                    return true; // ~ 10-20 Mbps
//                case Connectivity.NETWORK_TYPE_IDEN:
//                    return false; // ~25 kbps
//                case Connectivity.NETWORK_TYPE_LTE:
//                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    return false;
                default:
                    return false;
            }
        }else{
            return false;
        }
    }
}
