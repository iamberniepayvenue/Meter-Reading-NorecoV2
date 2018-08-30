package Utility;

import android.util.Log;

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

    public static float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (float) tmp / factor;
    }


    public static Float toDigit(String todigit) {
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
                //return res1.toString().toLowerCase();
                return "0:fd:20:44:bb:55";
            }
        } catch (Exception ex) {
        }
        return "";
    }


//    public static String getMacAddress() {
//        try {
//            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
//            for (NetworkInterface nif : all) {
//                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
//
//                byte[] macBytes = nif.getHardwareAddress();
//                if (macBytes == null) {
//                    return "";
//                }
//
//                StringBuilder res1 = new StringBuilder();
//                for (byte b : macBytes) {
//                    res1.append(Integer.toHexString(b & 0xFF) + ":");
//                }
//
//                if (res1.length() > 0) {
//                    res1.deleteCharAt(res1.length() - 1);
//                }
//                return res1.toString();
//            }
//        } catch (Exception ex) {
//            //handle exception
//        }
//
//        //return "00:24:06:f2:4f:6f";
//        //return "1C:48:Ce:4C:22:A7";
//        //return "No mac address";
//        return "5C:70:A3:3C:F3:8C";//"00:24:06:f2:4f:5d";//"No mac address";
//    }


    public static String getDateOnly() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault());
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


    public static String calcComponentAmount(float rate, float consumption) {
        DecimalFormat df = new DecimalFormat(".####");
        float res = rate * consumption;
        return df.format(res);
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
}
