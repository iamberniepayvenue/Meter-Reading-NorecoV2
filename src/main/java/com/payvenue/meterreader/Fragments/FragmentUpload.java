package com.payvenue.meterreader.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.payvenue.meterreader.Interface.IVolleyListener;
import com.payvenue.meterreader.MainActivity;
import com.payvenue.meterreader.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;

import DataBase.DBInfo;
import Model.Account;
import Utility.CommonFunc;
import Utility.NetworkUtil;

import static com.payvenue.meterreader.R.id.btnResetUpload;

public class FragmentUpload extends Fragment implements IVolleyListener {

    TextView cuurmac, txtreadcount, txtunreadcount, txtupport, txtuploadCount;
    Button BtnUpload;
    int uploadCount, returnCount, readCount, unreadCount;
    String HostName;
    String PortNumber;
    String CoopID;
    View rootView;
    Spinner spinHost;


    Button btnExtract;
    Button btnRestUpload;

    ProgressDialog mDialog;
    String mac;
    Context mcontext;

    private static final String TAG = "FragmentUpload";

    public FragmentUpload() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_upload, container, false);

        mcontext = getActivity();

        mDialog = new ProgressDialog(this.getActivity());
        mDialog.setCancelable(false);
        mDialog.setMessage("Compressing Data. Please wait...");


        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        getDataCount();
        initView();
        setValues();
        setActions();
    }

    public void initView() {

        txtreadcount = (TextView) rootView.findViewById(R.id.txtreadcount);
        txtunreadcount = (TextView) rootView.findViewById(R.id.txtunreadcount);
        txtupport = (TextView) rootView.findViewById(R.id.txtupPort);
        cuurmac = (TextView) rootView.findViewById(R.id.txtmac);
        spinHost = (Spinner) rootView.findViewById(R.id.spinnHost);
        HostName = spinHost.getSelectedItem().toString();
        btnRestUpload = (Button) rootView.findViewById(btnResetUpload);
        btnExtract = (Button) rootView.findViewById(R.id.btnExtract);
        BtnUpload = (Button) rootView.findViewById(R.id.btnUpload);
        txtuploadCount = rootView.findViewById(R.id.valuploaded);

    }


    public void setValues() {
        cuurmac.setText(CommonFunc.getMacAddress());
        txtreadcount.setText("" + readCount);
        txtunreadcount.setText("" + unreadCount);
        txtuploadCount.setText("" + uploadCount);
    }

    public void setActions() {

        btnRestUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                MainActivity.db.resetAllAccounts(MainActivity.db);
                getDataCount();
                v.setEnabled(true);

            }
        });

        btnExtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportDB();
            }
        });

        spinHost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                HostName = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        BtnUpload.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                prepareData();
            }

        });


    }

    public void prepareData() {


        PortNumber = ((EditText) rootView.findViewById(R.id.txtupPort)).getText().toString();

        String strRequest = HostName + ":"
                + PortNumber
                + "?cmd=uploadData"
                + "&coopid=" + MainActivity.connSettings.getCoopID() + "&mac=" + CommonFunc.getMacAddress();


        if (PortNumber.trim().length() == 0) {
            Toast.makeText(mcontext, "Please provide a host and port to sync data.", Toast.LENGTH_LONG).show();
            return;
        }
        int status = NetworkUtil.getConnectivityStatusString(mcontext);
        if (status == 0) {
            Toast.makeText(mcontext, "Please check your internet connection.", Toast.LENGTH_LONG).show();
            return;
        }

        try{
            Cursor cursor = MainActivity.db.getAccountList(MainActivity.db,  "Read' Or ReadStatus='Printed");

            if (cursor.getCount() == 0) {
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                Toast.makeText(mcontext, "No Data to upload.", Toast.LENGTH_LONG).show();
                return;
            }

            mDialog.setMessage("Uploading data.Please wait.");
            mDialog.show();
            JSONArray resultSet;
            JSONObject rowObject;
            String details,districtID,reader;
            Account account;

            Gson gson = new GsonBuilder().create();

            JSONObject FinalData;

            while (cursor.moveToNext()) {


                resultSet = new JSONArray();
                rowObject = new JSONObject();
                FinalData = new JSONObject();


                details = cursor.getString(cursor.getColumnIndex("ReadingDetails"));
                String routeID = cursor.getString(cursor.getColumnIndex(DBInfo.RouteNo));
                int columnID = cursor.getInt(cursor.getColumnIndex("_id"));
                districtID = MainActivity.db.getDistrictID(MainActivity.db,routeID);
                reader = MainActivity.db.getReaderID(MainActivity.db);
                account = gson.fromJson(details, Account.class);
                try {
                    rowObject.put(DBInfo.DateSync,cursor.getString(cursor.getColumnIndex(DBInfo.DateSync)));
                    rowObject.put(DBInfo.DateRead,cursor.getString(cursor.getColumnIndex(DBInfo.DateRead)));
                    rowObject.put(DBInfo.COOPID,cursor.getString(cursor.getColumnIndex(DBInfo.COOPID)));
                    rowObject.put("DistrictID",districtID);
                    rowObject.put("RouteNo",routeID);
                    rowObject.put("AccountID",cursor.getString(cursor.getColumnIndex(DBInfo.AccountID)));
                    rowObject.put("LastName",cursor.getString(cursor.getColumnIndex(DBInfo.LastName)));
                    rowObject.put("FirstName",cursor.getString(cursor.getColumnIndex(DBInfo.FirstName)));
                    rowObject.put("MiddleName",cursor.getString(cursor.getColumnIndex(DBInfo.MiddleName)));
                    rowObject.put("Address",account.getAddress());
                    rowObject.put("MeterSerialNo",cursor.getString(cursor.getColumnIndex(DBInfo.MeterSerialNo)));
                    rowObject.put("PrevReading",account.getInitialReading());
                    rowObject.put("NewReading",account.getReading());
                    rowObject.put("Consume",account.getConsume());
                    rowObject.put("Latitude",account.getLatitude());
                    rowObject.put("Longitude",account.getLongitude());
                    rowObject.put("ReaderID",reader);
                    rowObject.put("ReadStatus",cursor.getString(cursor.getColumnIndex(DBInfo.ReadStatus)));
                    rowObject.put("Remarks",account.getRemarks());
                    rowObject.put("DueDate",cursor.getString(cursor.getColumnIndex(DBInfo.DueDate)));

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }

                resultSet.put(rowObject);

                try {
                    FinalData.put("readAccounts", resultSet);
                    FinalData.put("columnid", String.valueOf(columnID));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Log.e(TAG,"JSONException1: " + e.getMessage());
                    e.printStackTrace();
                }

                String url = strRequest + "&data=" + URLEncoder.encode(FinalData.toString());
                Log.e(TAG,"request: "+url);
                Log.e(TAG,"final data: "+FinalData);
                MainActivity.webRequest.sendRequest(url, "UploadData",FinalData.toString(),"","", this);
            }
        }catch (IllegalArgumentException i) {
            Toast.makeText(getContext(),i.getMessage(),Toast.LENGTH_LONG).show();
            Log.e(TAG,"IllegalArgumentException: " + i.getMessage());
        }
    }


    @Override
    public void onSuccess(String type, String response,String params,String param2,String param3) {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
        Toast.makeText(mcontext,"Data successfully uploaded",Toast.LENGTH_SHORT).show();
        getDataCount();
        setValues();
    }

    @Override
    public void onFailed(VolleyError error,String type) {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
        Log.e(TAG,"error volley: " + error.getMessage());
        if(error.getMessage() == null) {
            Toast.makeText(mcontext,"Failed to upload",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(mcontext,"Error" + error.getMessage(),Toast.LENGTH_SHORT).show();
        }

        getDataCount();
        setValues();
    }

    public void getDataCount() {


        uploadCount = MainActivity.db.getDataCount(MainActivity.db, "uploaded");
        readCount = MainActivity.db.getDataCount(MainActivity.db, "read");
        unreadCount = MainActivity.db.getDataCount(MainActivity.db, "unread");

    }


    @Override
    public void onDestroy() {
        super.onDestroy(); // Always call the superclass

        // Stop method tracing that the activity started during onCreate()
        android.os.Debug.stopMethodTracing();
    }

    @Override
    public void onPause() {
        super.onPause(); // Always call the superclass method first
    }






    private void exportDB() {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String currentDBPath = "/data/" + "com.marelco.meterreader" + "/databases/" + DBInfo.DATABASE_NAME;
        String backupDBPath = "Documents/" + DBInfo.DATABASE_NAME + ".db";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(mcontext, "DB Exported!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
