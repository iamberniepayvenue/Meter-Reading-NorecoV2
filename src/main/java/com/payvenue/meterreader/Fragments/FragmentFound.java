package com.payvenue.meterreader.Fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
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

import com.android.volley.VolleyError;
import com.payvenue.meterreader.Interface.IVolleyListener;
import com.payvenue.meterreader.MainActivity;
import com.payvenue.meterreader.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

import DataBase.DataBaseHandler;
import Utility.CommonFunc;
import Utility.GPSTracker;
import Utility.NetworkUtil;

public class FragmentFound extends Fragment implements IVolleyListener {

    View rootView;
    ListView listview;


    Context mcontext;
    DataBaseHandler db;
    ProgressDialog mDialog;
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
        mDialog = new ProgressDialog(this.getActivity());
        mDialog.setCancelable(false);
        mDialog.setMessage("Compressing Data. Please wait...");

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


        //etaccountnumber = (EditText) addDialog.findViewById(R.id.etaccountnumber);
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
                //String acctnumber = etaccountnumber.getText().toString();

                if (meterserial.isEmpty() || reading.isEmpty() || remarks.isEmpty()) {
                    Toast.makeText(mcontext, "Please provide sufficient data,", Toast.LENGTH_SHORT).show();
                } else {

                    listview.setAdapter(null);

                    db.saveFoundMeter(db, ".", meterserial, reading, remarks, latitude, longitude,CommonFunc.getTimeNow());
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
        String columnid;

        if (c.getCount() == 0) {
            Toast.makeText(mcontext, "No data to upload.", Toast.LENGTH_SHORT).show();
            return;
        }


        mDialog.setMessage("Uploading data.Please wait.");
        mDialog.show();
        while (c.moveToNext()) {
            JSONArray myArray = new JSONArray();
            JSONObject myobj = new JSONObject();
                try {
                    myobj.put("AccountID", "FM"); // Found Meter
                    myobj.put("CoopID", MainActivity.connSettings.getCoopID());
                    myobj.put("MeterSerialNo", c.getString(c.getColumnIndex("MeterSerialNo")));
                    myobj.put("NewReading", c.getString(c.getColumnIndex("Reading")));
                    myobj.put("DateRead", c.getString(c.getColumnIndex("DateRead")));
                    myobj.put("Remarks", c.getString(c.getColumnIndex("Remarks")));
                    myobj.put("Latitude", c.getString(c.getColumnIndex("Latitude")));
                    myobj.put("Longitude", c.getString(c.getColumnIndex("Longitude")));
                    myobj.put("ReaderID", MainActivity.reader.getReaderID());
                    myobj.put("ReadStatus", "Found");
                    myobj.put("Mac",CommonFunc.getMacAddress());
                    myArray.put(myobj);

                    columnid = c.getString(0);

                    JSONObject FinalData = new JSONObject();
                    try {
                        FinalData.put("readAccounts", myArray);
                        FinalData.put("columnid", columnid);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    String url = "http://" + MainActivity.connSettings.getHost() + ":" + MainActivity.connSettings.getPort() + "?cmd=uploadData" + "&data=" + URLEncoder.encode(FinalData.toString(), "UTF-8");
                    Log.e(TAG, "FM :" + MainActivity.connSettings.getHost() + ":" + MainActivity.connSettings.getPort() + "?cmd=uploadData" + "&data=" + FinalData.toString());
                    MainActivity.webRequest.sendRequest(url, "FM", FinalData.toString(), "", "", this);

                } catch (Exception e) {
                    e.printStackTrace();

                }
        }
    }

    @Override
    public void onSuccess(String type, String response, String params, String param2, String param3) {
        Log.e(TAG,"onSuccess: "+ response);
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        Toast.makeText(mcontext,"Data successfully uploaded",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onFailed(VolleyError error, String type) {
        Log.e(TAG,"onFailed: "+ error.getMessage());
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        Toast.makeText(mcontext,"Failed to upload",Toast.LENGTH_SHORT).show();

    }

}



