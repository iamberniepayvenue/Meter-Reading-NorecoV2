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

    private RequestListener requestListener;


    public WebRequest(Context c) {
        this.c = c;
    }

    public void sendRequest(final String url, final String myType, final String params, final String param2, final String param3, final IVolleyListener listener) {

        final JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                String res = response.toString();
                if (myType.equalsIgnoreCase("NotFound") || myType.equalsIgnoreCase("uploadData")) {

                    try {
                        if (response.length() > 0) {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                res = obj.getString("result");
                                if (res.equalsIgnoreCase("404")) {

                                } else {
                                    JSONObject array = new JSONObject(params);
                                    String columnID = array.getString("columnid");
                                    MainActivity.db.updateUploadStaus(MainActivity.db, columnID, "Uploaded", "1");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (myType.equalsIgnoreCase("FM")) {
                    try {
                        JSONObject array = new JSONObject(params);
                        String columnID = array.getString("columnid");
                        MainActivity.db.updateUploadStatusFoundMeter(MainActivity.db, columnID);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "JSONException: " + e.getMessage());
                    }
                }

                if (myType.equalsIgnoreCase("accounts")) {
                    /***
                     *  param2 = duedate
                     *  params = routeid
                     * */
                    listener.onSuccess(myType, res, params, url, param2);
                } else {
                    listener.onSuccess(myType, res, params, param2, param3);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error:" + error.getMessage());
                listener.onFailed(error, myType);
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(c).addToRequestQueue(request);
    }

    public void sendRequest(String url, final String tag) {
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error : " + error.getMessage());
//                if (tag.equalsIgnoreCase("saveAccount")) {
//                    MyPreferences.getInstance(c).savePrefInt("update_error", 1);
//                }
            }
        });


        VolleySingleton.getInstance(c).addToRequestQueue(request);
    }

    public void sendRequestUpload(String url, final String myType, final String params, final String param2) {
        /**
         *      params = reading data in json form
         *      param2 = count of data for uploading from cursor
         *
         * */
        final JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                String res;
                if (myType.equalsIgnoreCase("uploadData")) {
                    try {
                        if (response.length() > 0) {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                res = obj.getString("result");
                                if (res.equalsIgnoreCase("404")) {
                                    requestListener.onRequestListener("404", "");
                                } else {
                                    JSONObject array = new JSONObject(params);
                                    String columnID = array.getString("columnid");
                                    MainActivity.db.updateUploadStaus(MainActivity.db, columnID, "Uploaded", "1");
                                    requestListener.onRequestListener("200", param2);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestListener.onRequestListener("500", error.getMessage());
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(c).addToRequestQueue(request);
    }


    public interface RequestListener {
        void onRequestListener(String response, String param);
    }

    public void setRequestListener(String url, String myType, String params, String param2, RequestListener listener) {
        this.requestListener = listener;
        //Log.e(TAG,"upload : " + url);
        sendRequestUpload(url, myType, params, param2);
    }

    public void setRequestListenerDownload(String url, RequestListener listener) {
        this.requestListener = listener;
        download(url);
    }


    public void download(String url) {
        final JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                requestListener.onRequestListener(response.toString(), "");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestListener.onRequestListener("500", error.getMessage());
            }
        });

        //request.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(c).addToRequestQueue(request);
    }
}