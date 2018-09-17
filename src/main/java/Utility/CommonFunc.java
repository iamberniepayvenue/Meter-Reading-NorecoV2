package Utility;

import android.net.Uri;
import android.util.Log;

import com.payvenue.meterreader.BuildConfig;
import com.payvenue.meterreader.R;

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
                return res1.toString().toLowerCase();
                //return "0:fd:20:44:bb:55";
            }
        } catch (Exception ex) {
        }
        return "";
    }

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
        String sub = billMonth.substring(0,2);
        String val = "";
        Log.e("CommonFunc","sub: " + sub);
        switch (sub) {
            case "01":
                val = "JAN";
                break;
            case "02":
                val = "FEB";
                break;
            case "03":
                val = "MAR";
                break;
            case "04":
                val = "APR";
                break;
            case "05":
                val = "MAY";
                break;
            case "06":
                val = "JUN";
                break;
            case "07":
                val = "JUL";
                break;
            case "08":
                val = "AUG";
                break;
            case "09":
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
        Log.e("CommonFunc","sub: " + val);
        return val;
    }

    public static String getDrawablePath() {


        Uri path = Uri.parse("android.resource://"+ BuildConfig.APPLICATION_ID+ R.drawable.woosim);
        return path.toString();
    }
}
