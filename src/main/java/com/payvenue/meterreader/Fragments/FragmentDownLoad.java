package com.payvenue.meterreader.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
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

import java.net.URLEncoder;

import DataBase.DBInfo;
import DataBase.DataBaseHandler;
import Model.Account;
import Model.Reader;
import Model.Route;
import Utility.CommonFunc;
import Utility.Constant;
import Utility.MyPreferences;
import Utility.NetworkUtil;

public class FragmentDownLoad extends Fragment implements OnClickListener, IVolleyListener {

    private Button BtnDownLoad, BtnDelete;
    private View rootView;
    SQLiteDatabase database;
    String mac;


    TextView lblMAC, mTvView;
    EditText txtPort;
    String strPort, txtHost, baseurl, dueDate;
    private String tagRoutid = "";
    Spinner spinHost;
    ProgressDialog mDialog;
    DataBaseHandler DB;
    Context ctx;
    private int numberOfRoutesDownloaded = 0;
    private int numberOfAccountSavingPerRoutes = 0;
    private int numberOfRoutesPassing = 0;
    private int numberOfTimesDeletingTable = 0;
    private static final String TAG = "FragmentDownLoad";
    private boolean ifRouteExist = false;
    private MyPreferences myPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myPreferences = MyPreferences.getInstance(getActivity());
        DB = new DataBaseHandler(getActivity());
        ctx = this.getActivity();
        rootView = inflater.inflate(R.layout.fragment_download, container, false);


        mDialog = new ProgressDialog(this.getActivity());
        mDialog.setCancelable(false);
        mDialog.setMessage("Connecting to server. Please wait...");
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);


        if (android.os.Build.VERSION.SDK_INT >= 23) {
            mac = CommonFunc.getMacAddress();
        } else {
            WifiManager wimanager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            mac = wimanager.getConnectionInfo().getMacAddress();
            if (mac == null) {
                mac = "Device don't have mac address.";
            }
        }


        initViews();

        lblMAC.setText(mac);
        txtPort.setText(Constant.PORT);
        BtnDownLoad.setOnClickListener(this);
        BtnDelete.setOnClickListener(this);

        spinHost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                txtHost = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return rootView;
    }


    //region Functions

    public void initViews() {
        spinHost = (Spinner) rootView.findViewById(R.id.spinnHost);
        txtHost = spinHost.getSelectedItem().toString();
        mTvView = rootView.findViewById(R.id.mTvView);
        lblMAC = (TextView) rootView.findViewById(R.id.lblmac);
        BtnDownLoad = (Button) rootView.findViewById(R.id.btnDownLoad);
        BtnDelete = (Button) rootView.findViewById(R.id.button1);
        txtPort = ((EditText) rootView.findViewById(R.id.txtport));
    }

    public void enableButton() {
        BtnDownLoad.setClickable(true);
    }

    public void disableButton() {
        BtnDownLoad.setClickable(false);
    }

    public void showToast(String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
        MainActivity.db.errorDownLoad(MainActivity.db);
        numberOfTimesDeletingTable++;

        myPreferences.savePrefInt(Constant.RATE_SCHEDULE_COUNT_NON_HIGHERVOLT,0);
        myPreferences.savePrefInt(Constant.RATE_SCHEDULE_COUNT_HIGHERVOLT,0);
        myPreferences.savePrefInt(Constant.COOP_DETAILS_COUNT,0);
        myPreferences.savePrefInt(Constant.RATE_COMPONENT_COUNT,0);
        myPreferences.savePrefInt(Constant.RATE_SEGMENT_COUNT,0);
        myPreferences.savePrefInt(Constant.BILLING_POLICY_NONHIGHVOLT_COUNT,0);
        myPreferences.savePrefInt(Constant.BILLING_POLICY_HIGHVOLT_COUNT,0);
        myPreferences.savePrefInt(Constant.LIFELINE_POLICY_COUNT,0);
        myPreferences.savePrefInt(Constant.RATE_CODE_COUNT,0);

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    //endregion

    //region Triggers

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnDownLoad:


                strPort = txtPort.getText().toString();
                if (strPort.trim().length() == 0) {//|| txtHost.trim().length() == 0
                    Toast.makeText(ctx, "Please provide a Host and Port to download data.", Toast.LENGTH_LONG).show();
                    return;
                }
                //disableButton();
                mDialog.setMessage("DownLoading Data.Please wait.");
                mDialog.show();
                int status = NetworkUtil.getConnectivityStatusString(ctx);

                if (status == 0) {
                    Toast.makeText(ctx, "Please check your internet connection.", Toast.LENGTH_LONG).show();
                    enableButton();
                    return;
                }

                baseurl =  "http://" + txtHost + ":" + strPort;
                String cmdRoute = baseurl + "?cmd=getRoutes&mac=" + mac;
                MainActivity.webRequest.sendRequest(cmdRoute, "dlRoutes", "","","", this);
                Log.e(TAG,"route: " + cmdRoute);
                break;

            case R.id.button1:

                DataBaseHandler db = new DataBaseHandler(getActivity());

                db.errorDownLoad(db);

                break;
        }

    }

    //endregion

    //region IListener

    @Override
    public void onSuccess(String type, String response,String params,String param2,String param3) {
        //Toast.makeText(getContext(), type, Toast.LENGTH_SHORT).show();


        Gson gson = new GsonBuilder().create();

        mTvView.append(response);


        switch (type) {

            case "dlRoutes":

                Route route;

                String coopID = null, readerID = null, readerName = null,
                        accountIDFrom = null, accountIDTo = null,
                        districtID = null, routeID = null, cmdAccounts,tagClass = null;

                try {
                    JSONArray jsonArray = new JSONArray(response);

                    if (jsonArray.length() > 0) {

                        numberOfRoutesDownloaded = jsonArray.length();
                        Log.e(TAG,"route length : " + numberOfRoutesDownloaded);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            coopID = obj.getString("CoopID");
                            readerID = obj.getString("ReaderID");
                            readerName = obj.getString("ReaderName");
                            districtID = obj.getString("DistrictID");
                            routeID = obj.getString("RouteID");
                            tagRoutid = routeID;
                            accountIDFrom = obj.getString("AccountIDFrom");
                            accountIDTo = obj.getString("AccountIDTo");
                            dueDate = obj.getString("DueDate");
                            tagClass = obj.getString("TagClass");
                            route = gson.fromJson(obj.toString(), Route.class);

                            /**check to avoid duplication of routes and accounts*/
                            ifRouteExist = DB.checkRouteIsExist(DB,routeID,districtID);
                            Log.e(TAG,"route exist: " + ifRouteExist);
                            if(!ifRouteExist) {
                                DB.saveRoute(DB, route);
                            }

                            cmdAccounts = baseurl + "?cmd=getaccounts&coopid=" + coopID
                                    + "&districtid=" + URLEncoder.encode(districtID)
                                    + "&routeid=" + URLEncoder.encode(routeID)
                                    + "&idFrom=" + accountIDFrom
                                    + "&idTo=" + accountIDTo
                                    + "&mac=" + mac
                                    + "&tagclass=" + tagClass
                                    + "&offset=0";
                            String urlParam = baseurl + "?cmd=getaccounts&coopid=" + coopID
                                    + "&districtid=" + URLEncoder.encode(districtID)
                                    + "&routeid=" + URLEncoder.encode(routeID)
                                    + "&idFrom=" + accountIDFrom
                                    + "&idTo=" + accountIDTo
                                    + "&mac=" + mac
                                    + "&tagclass=" + tagClass;
                            Log.e(TAG,urlParam);
                            if(numberOfRoutesPassing == 0) {
                                MainActivity.webRequest.sendRequest(cmdAccounts, "Accounts",routeID,dueDate,urlParam, this);
                            }
                        }


                        Reader reader = new Reader(coopID, readerID, readerName);

                        DB.syncSettingsInfo(DB, reader);
                        DB.saveConnection(DB, coopID, txtHost, strPort);

                        MainActivity.setConnSettings();

                        MainActivity.setReader();
                    } else {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        if(!params.equalsIgnoreCase("1")){
                            Toast.makeText(ctx,"No Routes Downloaded",Toast.LENGTH_SHORT).show();
                            return;
                            /** stop the process, waiting for the assign routes*/
                        }else{
                            Toast.makeText(ctx,"No New Assign Routes",Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG,"json error: " + response);
                    Log.e(TAG,"json error: " + e.getMessage());
                    e.printStackTrace();
                }


                /**
                 1 params indicates that user click more times in download button
                 check the reader table from clouds where the IsDownloadStatus = 0
                 */


                    String cmdRateCode = baseurl + "?cmd=getRateCode&coopid=" + coopID + "&mac=" + mac;
                    String cmdCoopDetails = baseurl + "?cmd=getCoopDetails&coopid=" + coopID + "&mac=" + mac;
                    String cmdRateComponent = baseurl + "?cmd=getRateComponent&coopid=" + coopID + "&mac=" + mac;
                    String cmdRateSegment = baseurl + "?cmd=getRateSegment&coopid=" + coopID + "&mac=" + mac;
                    String cmdPolicy = baseurl + "?cmd=getBillingPolicy&coopid=" + coopID + "&mac=" + mac + "&tagclass=" + tagClass;
                    String cmdSchedule = baseurl + "?cmd=getRateSchedule&coopid=" + coopID + "&mac=" + mac + "&tagclass=" + tagClass;
                    String cmdLifeLineDiscount = baseurl + "?cmd=ld&coopid=" + coopID;

                    if(myPreferences.getPrefInt(Constant.RATE_CODE_COUNT) == 0) {
                        MainActivity.webRequest.sendRequest(cmdRateCode, "Code", "","","", this);
                    }

                    if(myPreferences.getPrefInt(Constant.COOP_DETAILS_COUNT) == 0) {
                        MainActivity.webRequest.sendRequest(cmdCoopDetails, "Coop", "","","", this);
                    }

                    if(myPreferences.getPrefInt(Constant.RATE_COMPONENT_COUNT) == 0) {
                        MainActivity.webRequest.sendRequest(cmdRateComponent, "Component", "","","", this);
                    }

                    if(myPreferences.getPrefInt(Constant.RATE_SEGMENT_COUNT) == 0) {
                        MainActivity.webRequest.sendRequest(cmdRateSegment, "Segment", "","","", this);
                    }

                    if(tagClass == "1"){
                        if (myPreferences.getPrefInt(Constant.BILLING_POLICY_HIGHVOLT_COUNT) == 0) {
                            MainActivity.webRequest.sendRequest(cmdPolicy, "Policy", "", "", "", this);

                        }

                        if(myPreferences.getPrefInt(Constant.RATE_SCHEDULE_COUNT_HIGHERVOLT) == 0) {
                            MainActivity.webRequest.sendRequest(cmdSchedule, "Schedule", "","","", this);
                        }
                    }else {
                        if (myPreferences.getPrefInt(Constant.BILLING_POLICY_NONHIGHVOLT_COUNT) == 0) {
                            MainActivity.webRequest.sendRequest(cmdPolicy, "Policy", "", "", "", this);
                        }

                        if(myPreferences.getPrefInt(Constant.RATE_SCHEDULE_COUNT_NON_HIGHERVOLT) == 0) {
                            MainActivity.webRequest.sendRequest(cmdSchedule, "Schedule", "","","", this);
                        }
                    }

                    if(myPreferences.getPrefInt(Constant.LIFELINE_POLICY_COUNT) == 0) {
                        MainActivity.webRequest.sendRequest(cmdLifeLineDiscount, "LifeLineDiscount", "","","", this);
                    }

                break;


            case "Code":
                try {

                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i++) {

                        JSONObject obj = array.getJSONObject(i);

                        String CoopID = obj.getString("PowerUtilityID");
                        String CoopName = obj.getString("PowerUtilityName");
                        String RateCode = obj.getString("RateCode");
                        String Details = obj.getString("Details");
                        String IsActive = obj.getString("IsActive");


                        int save = DB.saveRateCode(DB, CoopID, CoopName, RateCode, Details, IsActive);
                        myPreferences.savePrefInt(Constant.RATE_CODE_COUNT,save);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                break;

            case "Coop":
                try {

                    JSONArray array = new JSONArray(response);

                    if (array.length() > 0) {

                        for (int i = 0; i < array.length(); i++) {

                            JSONObject obj = array.getJSONObject(i);

                            String CoopID = obj.getString("PowerUtilityID");
                            String CoopName = obj.getString("PowerUtilityName");
                            String CoopType = obj.getString("CoopType");
                            String Classification = obj.getString("Classification");
                            String ReadingToDueDate = obj.getString("ReadingToDueDate");
                            String Acronym = obj.getString("Acronym");
                            String BillingCode = obj.getString("BillingCode");
                            String businessaddress = obj.getString("BusinessAddress");
                            String telno = obj.getString("TelNo");
                            String tinno = obj.getString("Extra2");

                            int save = DB.saveCoopDetails(DB, CoopID, CoopName, CoopType,
                                    Classification, ReadingToDueDate, Acronym,
                                    BillingCode, businessaddress, telno, tinno);

                            myPreferences.savePrefInt(Constant.COOP_DETAILS_COUNT,save);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;

            case "Component":
                try {

                    JSONArray array = new JSONArray(response);

                    if (array.length() > 0) {

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String CoopID = obj.getString("PowerUtilityID");
                            String CoopName = obj.getString("PowerUtilityName");
                            String RateComponent = obj.getString("RateComponent");
                            String Details = obj.getString("Details");
                            String IsActive = obj.getString("IsActive");
                            String Notes1 = obj.getString("Notes1");
                            int save = DB.saveRateComponent(DB, CoopID, CoopName, RateComponent, Details, IsActive, Notes1);
                            myPreferences.savePrefInt(Constant.RATE_COMPONENT_COUNT,save);
                        }

                    } else {
                        showToast("No Components Downloaded");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;

            case "Segment":
                try {

                    JSONArray array = new JSONArray(response);

                    if (array.length() > 0) {

                        for (int i = 0; i < array.length(); i++) {

                            JSONObject obj = array.getJSONObject(i);

                            String CoopID = obj.getString("PowerUtilityID");
                            String RateSegmentCode = obj
                                    .getString("RateSegmentCode");
                            String RateSegmentName = obj
                                    .getString("RateSegmentName");
                            String Details = obj.getString("Details");
                            String IsActive = obj.getString("IsActive");


                            int save = DB.saveRateSegment(DB, CoopID, RateSegmentCode,
                                    RateSegmentName, Details, IsActive);
                            myPreferences.savePrefInt(Constant.RATE_SEGMENT_COUNT,save);
                        }

                    } else {
                        showToast("No Rate Segment Available,Please check...");
                        return;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;

            case "Policy":
                try {

                    JSONArray array = new JSONArray(response);


                    if (array.length() > 0) {

                        for (int i = 0; i < array.length(); i++) {

                            JSONObject obj = array.getJSONObject(i);

                            String CoopID = obj.getString("PowerUtilityID");
                            String CoopName = obj.getString("PowerUtilityName");
                            String PolicyCode = obj.getString("PolicyCode");
                            String PolicyName = obj.getString("PolicyName");
                            String PolicyType = obj.getString("PolicyType");
                            String CustomerClass = obj
                                    .getString("CustomerClass");
                            String SubClass = obj.getString("SubClass");
                            String MinkWh = obj.getString("MinkWh");
                            String MaxkWh = obj.getString("MaxkWh");
                            String PercentAmount = obj
                                    .getString("PercentAmount");

                            int save = DB.saveBillingPolicy(DB, CoopID, CoopName,
                                    PolicyCode, PolicyName, PolicyType,
                                    CustomerClass, SubClass, MinkWh, MaxkWh,
                                    PercentAmount);
                            if(CustomerClass.equalsIgnoreCase("Higher Voltage")) {
                                myPreferences.savePrefInt(Constant.BILLING_POLICY_HIGHVOLT_COUNT,save);
                            }else{
                                myPreferences.savePrefInt(Constant.BILLING_POLICY_NONHIGHVOLT_COUNT,save);
                            }
                        }

                    } else {
                        showToast("No Billing Policy Downloaded");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;

            case "Schedule":
                try {

                    JSONArray array = new JSONArray(response);

                    if (array.length() > 0) {

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String coopid = obj.getString("PowerUtilityID");
                            String ratesegment = obj.getString("RateSegment");
                            String ratecomponent = obj.getString("RateComponent");
                            String printorder = obj.getString("PrintOrder");
                            String classification = obj.getString("Classification");
                            String ratesched = obj.getString("RateSched");
                            String rateschedtype = obj.getString("RateSchedType");
                            String amount = obj.getString("Amount");
                            String vatrate = obj.getString("VATRate");
                            String vatamount = obj.getString("VATAmount");
                            String franchisetaxrate = obj.getString("FranchiseTaxRate");
                            String franchisetaxamount = obj.getString("FranchiseTaxAmount");
                            String localtaxrate = obj.getString("LocalTaxRate");
                            String localtaxamount = obj.getString("LocalTaxAmount");
                            String totalamount = obj.getString("TotalAmount");
                            String isvat = obj.getString("IsVAT");
                            String isdvat = obj.getString("IsDVAT");
                            String isUnderOver = obj.getString("IsOverUnder");
                            String isfranchisetax = obj.getString("IsFranchiseTax");
                            String islocaltax = obj.getString("IsLocalTax");
                            String islifeline = obj.getString("IsLifeLine");
                            String isscdiscount = obj.getString("IsSCDiscount");
                            String ratestatus = obj.getString("RateStatus");
                            String dateadded = obj.getString("DateFrom");
                            String extra1 = obj.getString("Extra1");


                            int save = DB.saveRateSchedule(DB, coopid, ratesegment,
                                    ratecomponent, printorder, classification,
                                    ratesched, rateschedtype, amount, vatrate,
                                    vatamount, franchisetaxrate,
                                    franchisetaxamount, localtaxrate,
                                    localtaxamount, totalamount, isvat, isdvat,isUnderOver,
                                    isfranchisetax, islocaltax, islifeline,
                                    isscdiscount, ratestatus, dateadded, extra1);

                            if(classification.equalsIgnoreCase("Higher Voltage")) {
                                myPreferences.savePrefInt(Constant.RATE_SCHEDULE_COUNT_HIGHERVOLT,save);
                            }else {
                                myPreferences.savePrefInt(Constant.RATE_SCHEDULE_COUNT_NON_HIGHERVOLT,save);
                            }
                        }
                    } else {
                        showToast("No Available Rates, Please check Rate Schedule");
                        return;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case "LifeLineDiscount":
                try {
                    JSONArray array = new JSONArray(response);
                    if(array.length() > 0) {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            String lifeLineKwh = object.getString(DBInfo.LifelineConsumption);
                            String percent = object.getString(DBInfo.LifelinePercentage);
                            String decimal = object.getString(DBInfo.LifelineInDecimal);
                            int save = DB.saveLifeLifePolicy(DB,lifeLineKwh,percent,decimal);
                            myPreferences.savePrefInt(Constant.LIFELINE_POLICY_COUNT,save);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG,"LifeLineDiscount(err)" + e.getMessage());
                }
                break;
            case "Accounts":
                int DownloadCount;
                int DownloadSave = 0;
                try {

                    Account account;

                    JSONArray array = new JSONArray(response);
                    if (array.length() > 0) {

                        DownloadCount = array.length();
                        JSONObject details;
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String acc = obj.getString("AccountID");
                            if(!acc.equalsIgnoreCase("0")) {
                                details = new JSONObject();
                                details.put("BillMonth","");
                                details.put("PrevBilling", obj.getString("PrevBilling"));
                                details.put("Address", obj.getString("Address"));
                                details.put("SeniorCitizenStatus", obj.getString("SeniorCitizenStatus"));
                                details.put("SCExpiryDate", obj.getString("SCExpiryDate"));
                                details.put("Penalty", obj.getString("Penalty"));
                                details.put("RateSched", obj.getString("RateSched"));
                                details.put("Multiplier", obj.getString("Multiplier"));
                                details.put("DemandKW", obj.getString("DemandKW"));
                                details.put("AdvancePayment", obj.getString("AdvancePayment"));
                                details.put("BillDeposit", obj.getString("BillDeposit"));
                                details.put("LastReadingDate", obj.getString("LastReadingDate"));
                                details.put("InitialReading",obj.getString("InitialReading"));
                                details.put("PrevReading", obj.getString("PrevReading"));
                                details.put("PrevFinalReading", obj.getString("PrevFinalReading"));
                                details.put("ExportPreviousReading", obj.getString("ExportReading"));
                                details.put("IsChangeMeter", obj.getString("IsChangeMeter"));
                                details.put("MeterBrand", obj.getString("MeterBrand"));
                                details.put("Consume", "0");
                                details.put("ExportConsume","0");
                                details.put("Reading", "0");
                                details.put("ExportReading","0");
                                details.put("Remarks", "");
                                account = gson.fromJson(obj.toString(), Account.class);
                                account.setDueDate(param2);
                                DownloadSave = DownloadSave + DB.saveAccount(DB, account, details.toString(),params);
                                if (DownloadSave == DownloadCount) {
                                    long offset = DB.getAccountSaveCount(DB,params);
                                    String url = param3 + "&offset=" + offset;
                                    /**
                                     * numberOfRoutesPassing means counter of failing request
                                     * */
                                    if(numberOfRoutesPassing == 0) {
                                        MainActivity.webRequest.sendRequest(url, "Accounts", params, dueDate, param3, this);
                                        enableButton();
                                    }
                                }
                            }else{
                                int accountCount = DB.getDataCountThisRoute(DB,params);
                                if(accountCount > 0) {
                                    numberOfAccountSavingPerRoutes++;
                                }
                            }
                        }
                    } else {
                        showToast("No Accounts Downloaded");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG,"JSONException :"+ e.getMessage());
                }

                break;
                default:
        }

        if(numberOfRoutesDownloaded > 0 ) {
            if(numberOfRoutesDownloaded == numberOfAccountSavingPerRoutes) {
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }

                Toast.makeText(ctx,"Download Complete",Toast.LENGTH_LONG).show();
                updateReaderTable();
            }else{
                //mDialog.show();
            }

            /**failed download*/
            if(numberOfRoutesPassing > 0) {
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                mTvView.setText("");
                showToast("Download accounts not completed, please download again.");
                return;
            }

            if(ifRouteExist) {
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        }

        BtnDownLoad.setEnabled(true);
    }

    public void updateReaderTable(){
        /** check table is attempt to clear */
        if(numberOfTimesDeletingTable == 0) {
            /** then update Reader table isDownload... in server */

            Cursor cursorRouteID = DB.getRoutesNo(DB);
            if(cursorRouteID.moveToFirst()) {
                while(!cursorRouteID.isAfterLast()) {
                    String routeID = cursorRouteID.getString(cursorRouteID.getColumnIndex("RouteID"));
                    String cmdUpdateReaderTable = baseurl + "?cmd=update&routeid=" + routeID + "&mac=" + mac;
                    MainActivity.webRequest.sendRequest(cmdUpdateReaderTable);
                    Log.e(TAG,"update reader table : " + routeID);
                    cursorRouteID.moveToNext();
                }
            }
        }
    }

    @Override
    public void onFailed(VolleyError error,String type) {
        BtnDownLoad.setEnabled(true);
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if(type.equalsIgnoreCase("Accounts")) {
            numberOfRoutesPassing++;
            Toast.makeText(getContext(), "Failed to download Accounts, please retry...", Toast.LENGTH_SHORT).show();
            MainActivity.db.errorDownLoad(MainActivity.db);
        }

        if(type.equalsIgnoreCase("dlRoutes")) {
            Toast.makeText(getContext(), "Failed to download Routes, please retry...", Toast.LENGTH_SHORT).show();
        }

        if(error.getMessage() == null) {
            Log.e(TAG,"failed download: " + type );
        }else{
            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}