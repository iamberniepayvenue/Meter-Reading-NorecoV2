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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;

import DataBase.DBInfo;
import Model.Account;
import Model.Bill;
import Utility.CommonFunc;
import Utility.Constant;
import Utility.NetworkUtil;

import static com.payvenue.meterreader.R.id.btnResetUpload;

public class FragmentUpload extends Fragment implements IVolleyListener {

    TextView cuurmac, txtreadcount, txtunreadcount, txtuploadCount;
    Button BtnUpload;
    int uploadCount, returnCount, readCount, unreadCount;
    String HostName;
    String PortNumber;
    String CoopID;
    View rootView;
    Spinner spinHost;
    int lengthOfData = 0;
    int countToUpload = 0;

    Button btnExtract;
    Button btnRestUpload;

    ProgressDialog mDialog;
    String mac;
    Context mcontext;
    String strPort;
    EditText txtPort;

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
        txtPort = rootView.findViewById(R.id.txtupPort);
        txtreadcount = (TextView) rootView.findViewById(R.id.txtreadcount);
        txtunreadcount = (TextView) rootView.findViewById(R.id.txtunreadcount);
        cuurmac = (TextView) rootView.findViewById(R.id.txtmac);
        spinHost = (Spinner) rootView.findViewById(R.id.spinnHost);
        HostName = spinHost.getSelectedItem().toString();
        btnRestUpload = (Button) rootView.findViewById(btnResetUpload);
        btnExtract = (Button) rootView.findViewById(R.id.btnExtract);
        BtnUpload = (Button) rootView.findViewById(R.id.btnUpload);
        txtuploadCount = rootView.findViewById(R.id.valuploaded);
        spinHost.setEnabled(false);
        txtPort.setEnabled(false);
    }


    public void setValues() {
        txtPort.setText(Constant.PORT);
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

        PortNumber = txtPort.getText().toString();
        String strRequest = "http://" + HostName + ":" + PortNumber + "?cmd=uploadData" + "&coopid=" + MainActivity.connSettings.getCoopID() + "&mac=" + CommonFunc.getMacAddress();


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

            lengthOfData = cursor.getCount();
            mDialog.setMessage("Uploading data.Please wait.");
            mDialog.show();
            JSONArray resultSet;
            JSONObject rowObject;
            String details,districtID,reader;
            Account account;

            Gson gson = new GsonBuilder().create();

            JSONObject FinalData;

            while (cursor.moveToNext()) {

                countToUpload = countToUpload + 1;
                resultSet = new JSONArray();
                rowObject = new JSONObject();
                FinalData = new JSONObject();

                String accountClass = cursor.getString(cursor.getColumnIndex(DBInfo.AccountClassification));
                details = cursor.getString(cursor.getColumnIndex("ReadingDetails"));
                String routeID = cursor.getString(cursor.getColumnIndex(DBInfo.RouteNo));
                int columnID = cursor.getInt(cursor.getColumnIndex("_id"));
                String []arrDateRead = cursor.getString(cursor.getColumnIndex(DBInfo.DateRead)).split(" ");
                districtID = MainActivity.db.getDistrictID(MainActivity.db,routeID);
                reader = MainActivity.db.getReaderID(MainActivity.db);
                account = gson.fromJson(details, Account.class);
                try {
                    rowObject.put(DBInfo.DateSync,cursor.getString(cursor.getColumnIndex(DBInfo.DateSync)));
                    rowObject.put(DBInfo.DateRead,arrDateRead[0]);
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
                    rowObject.put("NewMeterSerial",cursor.getString(cursor.getColumnIndex(DBInfo.Extra1)));
                    rowObject.put("ExportReading",account.getExportReading());
                    rowObject.put("IsExport",account.getIsNetMetering());
                    rowObject.put("PrevExportReading",account.getExportPreviousReading());
                    rowObject.put("ExportConsumption",account.getExportConsume());
                    rowObject.put("ActualConsumption",cursor.getString(cursor.getColumnIndex("Extra2")));
                    //add billmonth from rate schedule date_from
                    Bill mBill = account.getBill();
                    double exportBillAmount = 0;
                    double billAmount = 0;
                    if(!cursor.getString(cursor.getColumnIndex(DBInfo.IsCheckSubMeterType)).equalsIgnoreCase("M")) {
                        exportBillAmount = mBill.getNetBillAmountExport();
                        billAmount = mBill.getTotalAmount();
                    }

                    rowObject.put("ExportBillAmount",exportBillAmount);
                    rowObject.put("BillAmount",billAmount);
                    float lifelineDiscount = Float.valueOf(account.getTotalLifeLineDiscount());
                    rowObject.put("LifelineDiscount",String.valueOf(-lifelineDiscount));
                    rowObject.put("LifelineSubsidy",account.getLifeLineSubsidy());
                    rowObject.put("SCDiscount",account.getTotalSCDiscount());
                    rowObject.put("SCSubsidy",account.getSeniorSubsidy());
                    rowObject.put("UORDiscount",account.getOverUnderDiscount());
                    rowObject.put("IsCheckSubMeterType",cursor.getString(cursor.getColumnIndex(DBInfo.IsCheckSubMeterType)));
                    rowObject.put("DemandKWReading",account.getDemandKW());
                    rowObject.put("ExportBill",account.getExportBill());
                    rowObject.put("Mac",CommonFunc.getMacAddress());
                    String exportDateCounter = "0";
                    if(account.getExportDateCounter() != null) {
                        exportDateCounter = account.getExportDateCounter();
                    }

                    rowObject.put("ExportDateCounter",exportDateCounter);
                    String billMonth = MainActivity.db.getBillMonth(MainActivity.db,accountClass);
                    String []strArray = billMonth.split("/");
                    String yr = strArray[2];
                    String month = strArray[0];
                    billMonth = yr+month;
                    rowObject.put("billmonth",billMonth);
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

                String url = null;
                try {
                    url = strRequest + "&data=" + URLEncoder.encode(FinalData.toString(),"UTF-8");
                    MainActivity.webRequest.sendRequest(url, "UploadData",FinalData.toString(),String.valueOf(countToUpload),"", this);
                    Log.e(TAG,"upload:"+url);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Log.e(TAG,"UnsupportedEncodingException: " + e.getMessage());
                }
            }
        }catch (IllegalArgumentException i) {
            Toast.makeText(getContext(),i.getMessage(),Toast.LENGTH_LONG).show();
            Log.e(TAG,"IllegalArgumentException: " + i.getMessage());
        }
    }


    @Override
    public void onSuccess(String type, String response,String params,String param2,String param3) {

        if(Integer.valueOf(param2) == lengthOfData) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
            lengthOfData = 0;
            countToUpload = 0;
            Toast.makeText(mcontext,"Data successfully uploaded",Toast.LENGTH_SHORT).show();
        }


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
        uploadCount = MainActivity.db.getDataCount(MainActivity.db, "uploaded","upload");
        readCount = MainActivity.db.getDataCount(MainActivity.db, "read","upload");
        unreadCount = MainActivity.db.getDataCount(MainActivity.db, "unread","upload");
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


        //check if sd card is available
        String state;
        state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state))
        {
            File sqliteDir = Environment.getDataDirectory();
            File root = Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath()+"/Documents");
            if(!dir.exists()) {
                dir.mkdir();
            }


            String currentDBPath = "/data/" + "com.payvenue.meterreader" + "/databases/" + DBInfo.DATABASE_NAME;
            File backupDB = new File(dir,DBInfo.DATABASE_NAME  +".db");
            File currentDB = new File(sqliteDir,currentDBPath);

            try {
                FileChannel source = new FileInputStream(currentDB).getChannel();
                FileChannel destination = new FileOutputStream(backupDB).getChannel();
                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.close();
                Toast.makeText(mcontext, "DB Exported!", Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG,e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"IOException:"+e.getMessage());
            }
        }
        else
        {
            Toast.makeText(mcontext, "sd card not found", Toast.LENGTH_LONG).show();
        }
    }
}

