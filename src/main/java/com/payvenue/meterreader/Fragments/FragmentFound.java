package com.payvenue.meterreader.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.payvenue.meterreader.MainActivity;
import com.payvenue.meterreader.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

import DataBase.DataBaseHandler;
import Utility.GPSTracker;
import Utility.NetworkUtil;

public class FragmentFound extends Fragment {

    View rootView;
    ListView listview;


    Context mcontext;
    DataBaseHandler db;

    Dialog addDialog;
    GPSTracker gps;
    double longitude;
    double latitude;
    private final String TAG = "FragmentFound";
    LinearLayout.LayoutParams tv1Params;

    EditText etmeterserial;
    EditText etmeterreading;
    EditText etremarks;
    EditText etaccountnumber;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.activity_found__meters, container,
                false);

        mcontext = getActivity();
        db = new DataBaseHandler(mcontext);

        tv1Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        listview = (ListView) rootView.findViewById(R.id.listview);


        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        getFoundMeters();

    }

    //region Triggers

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.found__meters, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.actionAdd) {
            showDialog();
        }

        if (item.getItemId() == R.id.actionUpload) {


            int status = NetworkUtil.getConnectivityStatusString(mcontext);

            if (status == 0) {
                Toast.makeText(getActivity(),
                        "Please check your internet connection.",
                        Toast.LENGTH_LONG).show();
                return false;
            }

            prepareData();
        }

        return super.onOptionsItemSelected(item);

    }

    //endregion

    //region Functions

    public void getFoundMeters() {

        Cursor cursor = db.getFoundMeters(db);
        // ManageCursor
        // getActivity().startManagingCursor(cursor);

        if (!cursor.isClosed()) {
            // Setup mapping from cursor to view fields;
            String[] FromFieldNames = new String[]{"AccountID", "MeterSerialNo", "Reading", "Remarks"};

            int[] toViewIDs = new int[]{R.id.txtAccountId, R.id.txtmeterserial, R.id.txtreading, R.id.txtremarks};

            // create adapter to map coloums of the database to the elements of
            // the UI
            SimpleCursorAdapter myCursorAdapter = new SimpleCursorAdapter(
                    mcontext, // context
                    R.layout.list_found, // row_layout
                    cursor, // Cursor
                    FromFieldNames, // FromFields DataBaseColumns
                    toViewIDs // ToFields View IDs
            );

            listview.setAdapter(myCursorAdapter);
        }


    }

    public void showDialog() {

        addDialog = new Dialog(getContext()); // new AlertDialog.Builder(mcontext);
        addDialog.setContentView(R.layout.pop_found_meter);
        addDialog.setCancelable(false);


        etaccountnumber = (EditText) addDialog.findViewById(R.id.etaccountnumber);
        etmeterserial = (EditText) addDialog.findViewById(R.id.etmeterserial);
        etmeterreading = (EditText) addDialog.findViewById(R.id.etmeterreading);
        etremarks = (EditText) addDialog.findViewById(R.id.etremarks);

        addDialog.setTitle("Found Meters:");


        // Create the AlertDialog object and return
        gps = new GPSTracker(mcontext);

        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        } else {
            gps.showSettingAlert();
        }


        Button buttonOK = (Button) addDialog.findViewById(R.id.btnOk);
        buttonOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                v.setEnabled(false);

                String meterserial = etmeterserial.getText().toString();
                String reading = etmeterreading.getText().toString();
                String remarks = etremarks.getText().toString();
                String acctnumber = etaccountnumber.getText().toString();

                if (meterserial.isEmpty() || reading.isEmpty() || remarks.isEmpty()) {
                    Toast.makeText(mcontext, "Please provide sufficient data,", Toast.LENGTH_SHORT).show();
                } else {

                    listview.setAdapter(null);
                    db.saveFoundMeter(db, acctnumber, meterserial, reading, remarks, latitude, longitude);
                    getFoundMeters();
                    addDialog.dismiss();


                }
                v.setEnabled(true);
            }
        });

        Button btnCancel = (Button) addDialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                addDialog.dismiss();

            }
        });


        addDialog.show();
    }


    public void prepareData() {

        Cursor c = db.getFoundMeters(db);

        String columnid = "";


        if (c.getCount() == 0) {
            Toast.makeText(mcontext, "No data to upload.", Toast.LENGTH_SHORT).show();
            return;
        }



        while (c.moveToNext()) {
            JSONArray myArray = new JSONArray();
            JSONObject myobj = new JSONObject();

            try {
                myobj.put("MeterSerialNo", c.getString(c.getColumnIndex("MeterSerialNo")));
                myobj.put("Reading", c.getString(c.getColumnIndex("Reading")));
                myobj.put("DateRead", c.getString(c.getColumnIndex("DateRead")));
                myobj.put("Remarks", c.getString(c.getColumnIndex("Remarks")));
                myobj.put("Coordinates", c.getString(c.getColumnIndex("Coordinates")));
                myobj.put("ReaderID", MainActivity.reader.getReaderID());
                myArray.put(myobj);

                columnid = c.getString(0);

                JSONObject FinalData = new JSONObject();
                try {
                    FinalData.put("FoundMeter", myArray);
                    FinalData.put("columnid", columnid);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                new uploadFoundMeters().execute(FinalData);

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }


    //endregion

    //region Threads

    public class uploadFoundMeters extends AsyncTask<JSONObject, Void, String> {


        String columnID;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(JSONObject... params) {

            Log.d("Data", "" + params[0] + "");

            final JSONObject myData = params[0];

            try {
                columnID = myData.getString("columnid").toString();
                Log.d("ColumnID", "" + columnID);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HttpParams myParams = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(myParams, 20000);
            HttpConnectionParams.setSoTimeout(myParams, 20000);
            HttpClient httpclient = new DefaultHttpClient(myParams);
            String json = myData.toString();


            final StringBuilder request = new StringBuilder(MainActivity.connSettings.getHost() + ":" + MainActivity.connSettings.getPort() + "?cmd=uploadFoundMeter");
            request.append("&data=").append(URLEncoder.encode(json));
            request.append("&coopid=").append(MainActivity.connSettings.getCoopID());
            request.append("&mac=").append(MainActivity.macAddress);

            Log.d(TAG,"url : " + request.toString());

            String uploadResult;

            try {

                HttpPost httppost = new HttpPost(request.toString());
                httppost.setHeader("Content-type", "application/json");

                StringEntity se = new StringEntity(json.toString());
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                        "application/json"));
                httppost.setEntity(se);

                HttpResponse response = httpclient.execute(httppost);
                uploadResult = EntityUtils.toString(response.getEntity());
                Log.i("tag", uploadResult);

            } catch (Exception e) {
                return "Failed";

            }

            return uploadResult;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equalsIgnoreCase("Failed")) {
                Toast.makeText(getActivity(), "Uploading of data failed.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (s.matches("[0-9]+")) {

                int myresult = Integer.parseInt(s);
                if (myresult == 01) {
                    Toast.makeText(getActivity(),
                            "Data successfully uploaded.", Toast.LENGTH_SHORT).show();

                    db.updateFoundMeters(db, columnID, "1"); //1 is uploaded

                    getFoundMeters();

                } else {
                    Toast.makeText(getActivity(), "Uploading of data failed.", Toast.LENGTH_SHORT).show();
                }
            } else {

                Toast.makeText(getActivity(), "Uploading of data failed.", Toast.LENGTH_SHORT).show();


            }

        }
    }


    //endregion

}



