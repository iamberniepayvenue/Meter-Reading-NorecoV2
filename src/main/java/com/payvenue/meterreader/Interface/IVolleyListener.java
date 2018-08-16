package com.payvenue.meterreader.Interface;

import com.android.volley.VolleyError;

/**
 * Created by andrewlaurienrsocia on 19/04/2018.
 */

public interface IVolleyListener {

    public void onSuccess(String type, String response,String params,String param2,String param3);

    public void onFailed(VolleyError error,String type);

}
