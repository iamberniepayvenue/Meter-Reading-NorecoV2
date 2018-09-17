package Utility;

import android.content.Context;
import android.content.SharedPreferences;

public class MyPreferences {
    private SharedPreferences sharedPreferences;

    private static MyPreferences preference;


    public static MyPreferences getInstance(Context context) {
        if(preference == null) {
            preference = new MyPreferences(context);
        }

        return preference;
    }

    public MyPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(Constant.MYPREFERENCES, Context.MODE_PRIVATE);
    }
    public void savePrefString(String key, String value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }
    public void savePrefBoolean(String key, Boolean value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.commit();
    }

    public void savePrefInt(String key, int value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putInt(key, value);
        prefsEditor.commit();
    }

    public String getPrefString(String key) {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(key, "");
        }
        return "";
    }
    public Boolean getPrefBoolean(String key) {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(key, false);
        }
        return false;
    }

    public int getPrefInt(String key) {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt(key,0);
        }
        return 0;
    }
}
