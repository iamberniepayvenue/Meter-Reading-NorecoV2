package com.payvenue.meterreader.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.payvenue.meterreader.AccountListActivity;
import com.payvenue.meterreader.Interface.IVolleyListener;
import com.payvenue.meterreader.MainActivity;
import com.payvenue.meterreader.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import DataBase.DBInfo;
import Model.Account;
import Utility.CommonFunc;
import Utility.NetworkUtil;

public class FragmentNotFound extends Fragment implements IVolleyListener {

    View rootView;
    ListView listvew;
    String HostName;
    String PortNumber;
    String coopid;
    private final String TAG = "FragmentNotFound";
    Context mcontxt;
    ProgressDialog mDialog;
    ImageView ivNoAccounts;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_no_found, container, false);
        mcontxt = getActivity();
        listvew = (ListView) rootView.findViewById(R.id.list);
        ivNoAccounts = rootView.findViewById(R.id.iv_no_accounts);
        listvew.setClickable(true);
        listvew.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // final int myPosition = position;
                view.setEnabled(false);
                Bundle b = new Bundle();
                String RouteCode = ((TextView) view.findViewById(R.id.txRouteCode)).getText().toString();
                String RouteID = ((TextView) view.findViewById(R.id.txtRouteId)).getText().toString();
                b.putString("RouteCode", RouteCode);
                b.putString("RouteID", RouteID);
                Intent intent = new Intent(mcontxt, AccountListActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }

        });

        mDialog = new ProgressDialog(this.getActivity());
        mDialog.setCancelable(false);
        getSearchData();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.fragment_no_found, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_upload) {

            int status = NetworkUtil.getConnectivityStatusString(mcontxt);

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void prepareData() {

        Cursor cursor = MainActivity.db.getAccountList(MainActivity.db, MainActivity.myMode);
        String columnid = "";

        String strRequest = "http://" + MainActivity.connSettings.getHost() + ":"
                + MainActivity.connSettings.getPort()
                + "?cmd=uploadData"
                + "&coopid=" + MainActivity.connSettings.getCoopID() + "&mac=" + CommonFunc.encrypt(CommonFunc.getMacAddress());

        if (cursor.getCount() == 0) {
            Toast.makeText(getActivity(), "No Data to upload.", Toast.LENGTH_SHORT).show();
            return;
        }

        mDialog.setMessage("Uploading data.Please wait.");
        mDialog.show();
        JSONArray resultSet;
        JSONObject rowObject;
        String details,districtID,reader,billMonth = null;
        Account account;
        Gson gson = new GsonBuilder().create();

        while (cursor.moveToNext()) {

            resultSet = new JSONArray();
            rowObject = new JSONObject();
            String accountClass = cursor.getString(cursor.getColumnIndex(DBInfo.AccountClassification));
            details = cursor.getString(cursor.getColumnIndex("ReadingDetails"));
            String []arrDateRead = cursor.getString(cursor.getColumnIndex(DBInfo.DateRead)).split(" ");
            account = gson.fromJson(details, Account.class);
            String routeID = cursor.getString(cursor.getColumnIndex(DBInfo.RouteNo));
            int columnID = cursor.getInt(cursor.getColumnIndex("_id"));
            districtID = MainActivity.db.getDistrictID(MainActivity.db,routeID);
            reader = MainActivity.db.getReaderID(MainActivity.db);
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
                rowObject.put("ActualConsumption","0");
                rowObject.put("ExportBillAmount","0");
                rowObject.put("Latitude",account.getLatitude());
                rowObject.put("Longitude",account.getLongitude());
                rowObject.put("ReaderID",reader);
                rowObject.put("ReadStatus",cursor.getString(cursor.getColumnIndex(DBInfo.ReadStatus)));
                rowObject.put("Remarks",account.getRemarks());
                rowObject.put("DueDate",cursor.getString(cursor.getColumnIndex(DBInfo.DueDate)));
                billMonth = MainActivity.db.getBillMonth(MainActivity.db,accountClass);
                rowObject.put("billmonth",billMonth);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            resultSet.put(rowObject);
            JSONObject FinalData = new JSONObject();
            try {
                FinalData.put("readAccounts", resultSet);
                FinalData.put("columnid", String.valueOf(columnID));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            //Log.e(TAG, "Upload Not Found: " + strRequest + "&data="+ FinalData.toString());

            String url;
            try {
                url = strRequest + "&data=" + CommonFunc.encrypt(FinalData.toString())+ "&BillMonth="+CommonFunc.encrypt(billMonth.trim());
                //Log.e(TAG, "Upload Not Found: " + url);
                MainActivity.webRequest.sendRequest(url, "NotFound", FinalData.toString(),"","", this);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
//            catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
        }
    }

    private void getSearchData() {

        Cursor cursor = MainActivity.db.getRoutes(MainActivity.db);
        if (!cursor.isClosed()) {

            if(cursor.getCount() > 0) {
                String[] FromFieldNames = new String[]{"RouteCode","_id"};

                int[] toViewIDs = new int[]{R.id.txRouteCode,R.id.txtRouteId};
                SimpleCursorAdapter myCursorAdapter = new SimpleCursorAdapter(
                        getActivity(), // context
                        R.layout.route_list, // row_layout
                        cursor, // Cursor
                        FromFieldNames, // FromFields DataBaseColumns
                        toViewIDs // ToFields View IDs
                );

                listvew.setAdapter(myCursorAdapter);
            }else {
                listvew.setVisibility(View.GONE);
                ivNoAccounts.setVisibility(View.VISIBLE);
            }
        }


    }

    @Override
    public void onSuccess(String type, String response,String params,String param2,String param3) {
        getSearchData();

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
        if(response.equals("404")) {
            Toast.makeText(mcontxt, "Billing Summary Details table not exist...", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(mcontxt, "Successfully Uploaded", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailed(VolleyError error,String type) {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if(error.getMessage() == null){
            Toast.makeText(mcontxt, "Unexpected Error. Please try again.", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(mcontxt, "Unexpected Error. Please try again." + error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
