package Utility;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.payvenue.meterreader.Interface.IVolleyListener;
import com.payvenue.meterreader.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by andrewlaurienrsocia on 19/04/2018.
 */


public class WebRequest {

    Context c;
    private static final String TAG = "WebRequest";


    public WebRequest(Context c) {
        this.c = c;
    }

    public void sendRequest(String url, final String myType, final String params, final String param2,final String param3, final IVolleyListener listener) {
        //Log.e(TAG,"url : " + url);
        final JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (myType.equalsIgnoreCase("NotFound") || myType.equalsIgnoreCase("uploadData")) {
                    try {
                        JSONObject array = new JSONObject(params);
                        String columnID = array.getString("columnid").toString();
                        MainActivity.db.updateUploadStaus(MainActivity.db, columnID, "Uploaded", "1");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG,"JSONException: " + e.getMessage());
                    }
                }

                listener.onSuccess(myType,response.toString(),params,param2,param3);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onFailed(error,myType);
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(5000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(c).addToRequestQueue(request);
    }

    public void sendRequest(String url) {
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG,"response : " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"response : " + error.getMessage());
            }
        });


        VolleySingleton.getInstance(c).addToRequestQueue(request);
    }
}