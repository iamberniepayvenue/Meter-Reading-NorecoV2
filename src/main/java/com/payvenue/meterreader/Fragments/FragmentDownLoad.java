package com.payvenue.meterreader.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.payvenue.meterreader.MainActivity;
import com.payvenue.meterreader.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import DataBase.DBInfo;
import DataBase.DataBaseHandler;
import Model.Account;
import Model.Route;
import Utility.CommonFunc;
import Utility.Constant;
import Utility.MyPreferences;
import Utility.NetworkUtil;
import Utility.WebRequest;

public class FragmentDownLoad extends Fragment implements OnClickListener { //, IVolleyListener

    private Button BtnDownLoad, BtnDelete;
    private static View rootView;
    SQLiteDatabase database;
    String mac;
    static Snackbar snackbar;
    public static String baseurl;

    static TextView mTvView;
    TextView lblMAC;
    EditText txtPort;
    String strPort, txtHost, dueDate;
    private String tagRoutid = "";
    Spinner spinHost;
    public static ProgressDialog mDialog;
    static DataBaseHandler DB;
    Context ctx;
    private int numberOfRoutesDownloaded = 0;
    private int numberOfAccountSavingPerRoutes = 0;
    //private int numberOfRoutesPassing = 0;
    private int numberOfTimesDeletingTable = 0;
    private static final String TAG = "FragmentDownLoad";
    private boolean ifRouteExist = false;
    private static MyPreferences myPreferences;
    private int totalAccountSave = 0;
    private int accountSavePerRoute = 0;
    private int routeLength = 0;
    private int rdsuccess = 0;
    private boolean noAccounts = false;
    private static Gson gson;
    private boolean isRatesScheduleErrorDownloading = false;


    private static int rateschedsize = 0;
    private static int rateshedsave = 0;
    private static int billingpolicysize = 0;
    private static int billingpolicysave = 0;
    private static int accountssave = 0;
    private static int accountssize = 0;
    private static int ratecodesize = 0;
    private static int ratecodesave = 0;
    private static int coopdetailssize = 0;
    private static int coopdetailssave = 0;
    private static int ratecomponentsize = 0;
    private static int ratecomponentsave = 0;
    private static int ratesegmentsize = 0;
    private static int ratesegmentsave = 0;
    private static int lifelinepolicysave = 0;
    private static int lifelinepolicysize = 0;
    private static int thresholdsize = 0;
    private static int thresholdsave = 0;


    private ArrayList<Route> routeArrayList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myPreferences = MyPreferences.getInstance(getActivity());
        DB = new DataBaseHandler(getActivity());
        ctx = this.getActivity();
        rootView = inflater.inflate(R.layout.fragment_download, container, false);

        gson = new GsonBuilder().create();

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


    public static void setSnackbar(String msg) {
        snackbar = Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG);
    }

    public void initViews() {
        spinHost = (Spinner) rootView.findViewById(R.id.spinnHost);
        txtHost = spinHost.getSelectedItem().toString();
        mTvView = rootView.findViewById(R.id.mTvView);
        lblMAC = (TextView) rootView.findViewById(R.id.lblmac);
        BtnDownLoad = (Button) rootView.findViewById(R.id.btnDownLoad);
        BtnDelete = (Button) rootView.findViewById(R.id.button1);
        txtPort = ((EditText) rootView.findViewById(R.id.txtport));
        spinHost.setEnabled(false);
        txtPort.setEnabled(false);
    }

    public void enableButton() {
        BtnDownLoad.setClickable(true);
    }

    public void disableButton() {
        BtnDownLoad.setClickable(false);
    }

    public void showToast(String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
        //MainActivity.db.errorDownLoad(MainActivity.db, getContext());
        numberOfTimesDeletingTable++;
        setZeroPreference();

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public void clearData() {
        numberOfTimesDeletingTable++;
        //MainActivity.db.errorDownLoad(MainActivity.db, getContext());
        setZeroPreference();

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public void setZeroPreference() {
        //myPreferences.savePrefInt(Constant.RATE_SCHEDULE_COUNT_NON_HIGHERVOLT,0);
        //myPreferences.savePrefInt(Constant.RATE_SCHEDULE_COUNT_HIGHERVOLT,0);
        myPreferences.savePrefInt(Constant.COOP_DETAILS_COUNT, 0);
        //myPreferences.savePrefInt(Constant.RATE_COMPONENT_COUNT,0);
        //myPreferences.savePrefInt(Constant.RATE_SEGMENT_COUNT,0);
        //myPreferences.savePrefInt(Constant.BILLING_POLICY_NONHIGHVOLT_COUNT,0);
        //myPreferences.savePrefInt(Constant.BILLING_POLICY_HIGHVOLT_COUNT,0);
        //myPreferences.savePrefInt(Constant.LIFELINE_POLICY_COUNT,0);
        myPreferences.savePrefInt(Constant.RATE_CODE_COUNT, 0);
    }

    //endregion

    //region Triggers

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnDownLoad:

                totalAccountSave = 0;
                numberOfTimesDeletingTable = 0;
                numberOfAccountSavingPerRoutes = 0;
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

                baseurl = "http://" + txtHost + ":" + strPort;
                String cmdRoute = baseurl + "?cmd=getRoutes&mac=" + mac;
                Log.e(TAG, "accounts: " + cmdRoute);
                MainActivity.webRequest.setRequestListenerDownload(cmdRoute, new WebRequest.RequestListener() {
                    @Override
                    public void onRequestListener(String response, String param) {

                        mTvView.append(response);

                        if (response.equalsIgnoreCase("500")) {
                            setSnackbar("Failed, " + param);
                            snackbar.show();
                            return;
                        }


                        try {
                            int save = 0;
                            JSONArray jsonArray = new JSONArray(response);
                            Route route = null;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                String coopID = obj.getString("CoopID");

                                if (coopID.equalsIgnoreCase("FAILED")) {

                                    if (mDialog.isShowing()) {
                                        mDialog.dismiss();
                                    }

                                    setSnackbar("Api Logs table not exist...");
                                    snackbar.show();
                                    return;
                                }

                                String districtID = obj.getString("DistrictID");
                                String routeID = obj.getString("RouteID");
                                String tagClass = obj.getString("TagClass");
                                route = gson.fromJson(obj.toString(), Route.class);
                                myPreferences.savePrefString(Constant.TAGCLASS, tagClass);
                                /**check to avoid duplication of routes and accounts*/
                                ifRouteExist = DB.checkRouteIsExist(DB, routeID, districtID);

                                if (!ifRouteExist) {
                                    save = +DB.saveRoute(DB, route);
                                }


                            }

                            if (jsonArray.length() == 0) {
                                if (mDialog.isShowing()) {
                                    mDialog.dismiss();
                                }

                                setSnackbar("Device not registered \n or routes not available...");
                                snackbar.show();
                                return;
                            }

                            if (jsonArray.length() != save) {
                                if (mDialog.isShowing()) {
                                    mDialog.dismiss();
                                }

                                setSnackbar("Routes not completely downloaded \n please fix error or download again");
                                snackbar.show();
                                DB.errorDownLoad(DB, getContext(), "Routes");
                                return;
                            }

                            if (jsonArray.length() == save) {
                                setSnackbar("routes completed..");
                                snackbar.show();
                                DB.syncSettingsInfo(DB, route);
                                routeArrayList = DB.getRoute(DB);

                                downloadRateSchedule();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            setSnackbar("" + e.getMessage());
                            snackbar.show();
                        }

                    }
                });




//                MainActivity.webRequest.sendRequest(cmdRoute, "dlRoutes", "", "", "", this);
//                if(isRatesScheduleErrorDownloading) {
//
//                }

                break;

            case R.id.button1:

                DataBaseHandler db = new DataBaseHandler(getActivity());

                //db.errorDownLoad(db, getContext());

                break;
        }

    }

    public void checkDataNotSuccessfullyDownloaded() {

        if(myPreferences.getPrefString(Constant.ACCOUNT_STATUS).equalsIgnoreCase(Constant.NO)){
            downloadAccouns();
        }

        if(myPreferences.getPrefString(Constant.RATE_CODE_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadRateCode();
        }

        if(myPreferences.getPrefString(Constant.COOP_DETAILS_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadCoopDetails();
        }

        if(myPreferences.getPrefString(Constant.RATE_COMPONENT_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadRateComponent();
        }

        if(myPreferences.getPrefString(Constant.RATE_SEGMENT_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadRateSegment();
        }

        if(myPreferences.getPrefString(Constant.POLICY_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadBillingPolicy();
        }

        if(myPreferences.getPrefString(Constant.RATE_SCHEDULE_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadRateSchedule();
        }

        if(myPreferences.getPrefString(Constant.LIFELINE_POLICY_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadLifeLineDiscountPolicy();
        }

        if(myPreferences.getPrefString(Constant.THRESHOLD_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadThreshold();
        }
    }

    public void downloadRateSchedule() {
        for (Route r : routeArrayList) {
            mDialog.setMessage("DownLoading Rate Schedule...");
            mDialog.show();
            String cmdSchedule = baseurl + "?cmd=getRateSchedule&coopid=" + r.getCoopID() + "&mac=" + mac + "&tagclass=" + r.getTagClass();
            new downloadRateScheduleAsync().execute(cmdSchedule);
        }

        if (rateschedsize != rateshedsave) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            DB.errorDownLoad(DB, getContext(), "RateSchedule");
            setSnackbar("Rate Schedule not completely downloaded...Download again");
            snackbar.show();
            return;
        }

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        Log.e(TAG,"rateschedule_save: "+ rateshedsave);
        setSnackbar("Rate Schedule Completed.");
        snackbar.show();
        myPreferences.savePrefString(Constant.RATE_SCHEDULE_STATUS,Constant.YES);
        downloadBillingPolicy();
    }

    public void downloadBillingPolicy() {
        mDialog.setMessage("DownLoading billing policy...");
        mDialog.show();
        for (Route r : routeArrayList) {
            String cmdPolicy = baseurl + "?cmd=getBillingPolicy&coopid=" + r.getCoopID() + "&mac=" + mac + "&tagclass=" + r.getTagClass();
            new downloadBillingPolicyAsync().execute(cmdPolicy);
        }

        if (billingpolicysize != billingpolicysave) {

            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            DB.errorDownLoad(DB, getContext(), "Policy");
            setSnackbar("Billing policy not completely downloaded...Download again");
            snackbar.show();
            return;
        }

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        Log.e(TAG,"billingpolicysave: "+ billingpolicysave);
        setSnackbar("Billing Policy Completed.");
        snackbar.show();
        myPreferences.savePrefString(Constant.POLICY_STATUS,Constant.YES);
        downloadRateCode();
    }

    public void downloadRateCode() {
        if (routeArrayList.size() > 0) {
            mDialog.setMessage("DownLoading rate codes...");
            mDialog.show();

            String cmdRateCode = baseurl + "?cmd=getRateCode&coopid=" + routeArrayList.get(0).getCoopID() + "&mac=" + mac;
            new downloadRateCodeAsync().execute(cmdRateCode);
        }

        if (ratecodesize != ratecodesave) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            DB.errorDownLoad(DB, getContext(), "RateCode");
            setSnackbar("Rate Code not completely downloaded...Download again");
            snackbar.show();
            myPreferences.savePrefString(Constant.RATE_CODE_STATUS, Constant.NO);
            return;
        }

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        Log.e(TAG,"ratecodesave: "+ ratecodesave);
        setSnackbar("Rate Code completed...");
        snackbar.show();
        myPreferences.savePrefString(Constant.RATE_CODE_STATUS,Constant.YES);
        downloadCoopDetails();
    }


    public void downloadCoopDetails() {
        if (routeArrayList.size() > 0) {
            mDialog.setMessage("DownLoading details...");
            mDialog.show();
            String cmdCoopDetails = baseurl + "?cmd=getCoopDetails&coopid=" + routeArrayList.get(0).getCoopID() + "&mac=" + mac;
            new downloadCoopDetailsAsync().execute(cmdCoopDetails);
        }

        if (coopdetailssize != coopdetailssave) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            DB.errorDownLoad(DB, getContext(), "CoopDetails");
            setSnackbar("Details not completely downloaded...Download again");
            snackbar.show();
            myPreferences.savePrefString(Constant.COOP_DETAILS_STATUS, Constant.NO);
            return;
        }

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        Log.e(TAG,"coopdetailssave: "+ coopdetailssave);
        setSnackbar("Coop details completed...");
        snackbar.show();
        myPreferences.savePrefString(Constant.COOP_DETAILS_STATUS,Constant.YES);
        downloadRateComponent();
    }

    public void downloadRateComponent() {
        if (routeArrayList.size() > 0) {
            mDialog.setMessage("DownLoading rate component...");
            mDialog.show();
            String cmdRateComponent = baseurl + "?cmd=getRateComponent&coopid=" + routeArrayList.get(0).getCoopID() + "&mac=" + mac;
            new downloadRateComponentAsync().execute(cmdRateComponent);
        }

        if (ratecomponentsize != ratecomponentsave) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            DB.errorDownLoad(DB, getContext(), "RateComponent");
            setSnackbar("Rate Component not completely downloaded...Download again");
            snackbar.show();
            myPreferences.savePrefString(Constant.RATE_COMPONENT_STATUS, Constant.NO);
            return;
        }

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        Log.e(TAG,"ratecomponentsave: "+ ratecomponentsave);
        setSnackbar("Rate Component completed...");
        snackbar.show();
        myPreferences.savePrefString(Constant.RATE_COMPONENT_STATUS,Constant.YES);
        downloadRateSegment();
    }

    public void downloadRateSegment() {
        if (routeArrayList.size() > 0) {
            mDialog.setMessage("DownLoading rate segment...");
            mDialog.show();
            String cmdRateSegment = baseurl + "?cmd=getRateSegment&coopid=" + routeArrayList.get(0).getCoopID() + "&mac=" + mac;
            new downloadRateSegmentAsync().execute(cmdRateSegment);
        }

        if (ratesegmentsize != ratesegmentsave) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            DB.errorDownLoad(DB, getContext(), "RateSegment");
            setSnackbar("Rate Segment not completely downloaded...Download again");
            snackbar.show();
            myPreferences.savePrefString(Constant.RATE_SEGMENT_STATUS, Constant.NO);
            return;
        }

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        Log.e(TAG,"ratesegmentsave: "+ ratesegmentsave);
        setSnackbar("Rate Segment completed...");
        snackbar.show();
        myPreferences.savePrefString(Constant.RATE_SEGMENT_STATUS,Constant.YES);
        downloadLifeLineDiscountPolicy();

    }


    public void downloadLifeLineDiscountPolicy() {
        if (routeArrayList.size() > 0) {
            mDialog.setMessage("DownLoading lifeline discount policy...");
            mDialog.show();
            String cmdLifeLineDiscount = baseurl + "?cmd=ld&coopid=" + routeArrayList.get(0).getCoopID() + "&mac=" + mac;
            new downloadLifeLineDiscountPolicyAsync().execute(cmdLifeLineDiscount);
        }

        if (lifelinepolicysize != lifelinepolicysave) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            DB.errorDownLoad(DB, getContext(), "lifeline");
            setSnackbar("Lifeline Discount Policy not completely downloaded...Download again");
            snackbar.show();
            myPreferences.savePrefString(Constant.LIFELINE_POLICY_STATUS, Constant.NO);
            return;
        }

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        Log.e(TAG,"lifelinepolicysave: "+ lifelinepolicysave);
        setSnackbar("Lifeline Discount Policy completed...");
        snackbar.show();
        myPreferences.savePrefString(Constant.LIFELINE_POLICY_STATUS,Constant.YES);
        downloadThreshold();

    }


    public void downloadThreshold() {
        mDialog.setMessage("DownLoading threshold...");
        mDialog.show();
        String cmdThreshold = baseurl + "?cmd=threshold&mac=" + mac;

        new downloadThresholdAsync().execute(cmdThreshold);
        if (thresholdsize != thresholdsave) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            DB.errorDownLoad(DB, getContext(), "threshold");
            setSnackbar("Threshold not completely downloaded...Download again");
            snackbar.show();
            myPreferences.savePrefString(Constant.THRESHOLD_STATUS, Constant.NO);
            return;
        }

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        Log.e(TAG,"thresholdsave: "+ thresholdsave);
        setSnackbar("Threshold completed...");
        snackbar.show();
        myPreferences.savePrefString(Constant.THRESHOLD_STATUS,Constant.YES);
        downloadAccouns();
    }

    public void downloadAccouns() {
        mDialog.setMessage("DownLoading accounts...");
        mDialog.show();
        for (Route r : routeArrayList) {
            String cmdAccounts = baseurl + "?cmd=getaccounts&coopid=" + r.getCoopID()
                    + "&districtid=" + URLEncoder.encode(r.getDistrictID())
                    + "&routeid=" + URLEncoder.encode(r.getRouteID())
                    + "&idFrom=" + r.getAccountIDFrom()
                    + "&idTo=" + r.getAccountIDTo()
                    + "&mac=" + mac
                    + "&tagclass=" + r.getTagClass()
                    + "&rd=0"
                    + "&sequenceRef=" + r.getDownloadRef()
                    + "&offset=0";


            if (r.getDownloadRef().equalsIgnoreCase("1")) {
                cmdAccounts = cmdAccounts + "&sequenceNoFrom=" + r.getSequenceNoFrom() + "&sequenceNoTo=" + r.getSequenceNoTo();
            }

            Log.e(TAG, "accounts: " + cmdAccounts);
            new downloadAccountsAsync().execute(cmdAccounts, r.getDueDate(), r.getRouteID());
        }

        if (accountssize != accountssave) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            DB.errorDownLoad(DB, getContext(), "Accounts");
            setSnackbar("Accounts not completely downloaded...Download again");
            snackbar.show();
            return;
        }

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if(accountssave > 0) {
            Log.e(TAG, "accountssave: " + accountssave);
            setSnackbar("Accounts Completed...");
            snackbar.show();
            updateReaderTable();
            BtnDownLoad.setEnabled(true);
            myPreferences.savePrefString(Constant.ACCOUNT_STATUS, Constant.YES);
        }
    }


    public static class downloadRateScheduleAsync extends AsyncTask<String, Void, Void> {

        public downloadRateScheduleAsync() {

        }

        @Override
        protected Void doInBackground(String... strings) {
            Log.e(TAG,"rateschedule: "+strings[0]);
            MainActivity.webRequest.setRequestListenerDownload(strings[0], new WebRequest.RequestListener() {
                @Override
                public void onRequestListener(String response, String param) {

                    mTvView.setText("");
                    mTvView.append(response);
                    if (response.equalsIgnoreCase("500")) {

                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        setSnackbar("Failed, " + param);
                        snackbar.show();

                        myPreferences.savePrefString(Constant.RATE_SCHEDULE_STATUS, Constant.NO);
                        return;

                    }


                    try {
                        JSONArray jsonArray = new JSONArray(response);

                        if (jsonArray.length() == 0) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            setSnackbar("No Available Rate Schedule...");
                            snackbar.show();

                            myPreferences.savePrefString(Constant.RATE_SCHEDULE_STATUS, Constant.NO);
                            return;
                        }

                        rateschedsize = +jsonArray.length();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String ratesegment = obj.getString("RateSegment");
                            String ratecomponent = obj.getString("RateComponent");
                            String printorder = obj.getString("PrintOrder");
                            String classification = obj.getString("Classification");
                            String rateschedtype = obj.getString("RateSchedType");
                            String amount = obj.getString("Amount");
                            String isUnderOver = obj.getString("IsOverUnder");
                            String islifeline = obj.getString("IsLifeLine");
                            String isscdiscount = obj.getString("IsSCDiscount");
                            String dateFrom = obj.getString("BillMonth");
                            String extra1 = obj.getString("RateComponentDetails");
                            String IsExport = obj.getString("IsExport");


                            rateshedsave = rateshedsave + DB.saveRateSchedule(DB, ratesegment,
                                    ratecomponent, printorder, classification,
                                    rateschedtype, amount, isUnderOver, islifeline,
                                    isscdiscount, dateFrom, extra1, IsExport);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        setSnackbar("RateSchedule, " + e.getMessage());
                        snackbar.show();
                    }


                }
            });

            return null;
        }
    }

    public static class downloadBillingPolicyAsync extends AsyncTask<String, Void, Void> {


        public downloadBillingPolicyAsync() {

        }

        @Override
        protected Void doInBackground(final String... strings) {
            Log.e(TAG,"BillingPolicy: "+strings[0]);
            MainActivity.webRequest.setRequestListenerDownload(strings[0], new WebRequest.RequestListener() {
                @Override
                public void onRequestListener(String response, String param) {

                    mTvView.setText("");
                    mTvView.append(response);

                    if (response.equalsIgnoreCase("500")) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        setSnackbar("Failed, " + param);
                        snackbar.show();

                        myPreferences.savePrefString(Constant.POLICY_STATUS, Constant.NO);
                        return;
                    }

                    try {
                        JSONArray jsonArray = new JSONArray(response);

                        if (jsonArray.length() == 0) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            setSnackbar("No Available Policy...");
                            snackbar.show();

                            myPreferences.savePrefString(Constant.POLICY_STATUS, Constant.NO);
                            return;
                        }

                        billingpolicysize = +jsonArray.length();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String PolicyCode = obj.getString("PolicyCode");
                            String PolicyType = obj.getString("PolicyType");
                            String CustomerClass = obj.getString("CustomerClass");
                            String MinkWh = obj.getString("MinkWh");
                            String MaxkWh = obj.getString("MaxkWh");
                            String PercentAmount = obj.getString("PercentAmount");

                            billingpolicysave = billingpolicysave + DB.saveBillingPolicy(DB,
                                    PolicyCode, PolicyType,
                                    CustomerClass, MinkWh, MaxkWh,
                                    PercentAmount);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        setSnackbar("Billing Policy, " + e.getMessage());
                        snackbar.show();
                    }

                }
            });

            return null;
        }
    }

    public static class downloadRateCodeAsync extends AsyncTask<String, Void, Void> {


        public downloadRateCodeAsync() {

        }

        @Override
        protected Void doInBackground(String... strings) {

            Log.e(TAG,"RateCode: "+strings[0]);
            MainActivity.webRequest.setRequestListenerDownload(strings[0], new WebRequest.RequestListener() {
                @Override
                public void onRequestListener(String response, String param) {

                    mTvView.setText("");
                    mTvView.append(response);

                    if (response.equalsIgnoreCase("500")) {

                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        setSnackbar("Failed Download Rates Code ," + param);
                        snackbar.show();

                        myPreferences.savePrefString(Constant.RATE_CODE_STATUS, Constant.NO);
                        return;
                    }

                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray.length() == 0) {

                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            setSnackbar("No available Rate Codes");
                            snackbar.show();

                            myPreferences.savePrefString(Constant.RATE_CODE_STATUS, Constant.NO);

                            return;
                        }

                        rateschedsize = +jsonArray.length();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String CoopID = obj.getString("PowerUtilityID");
                            String CoopName = obj.getString("PowerUtilityName");
                            String RateCode = obj.getString("RateCode");
                            String Details = obj.getString("Details");
                            String IsActive = obj.getString("IsActive");

                            ratecodesave = ratecodesave + DB.saveRateCode(DB, CoopID, CoopName, RateCode, Details, IsActive);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        setSnackbar("Rate Code, " + e.getMessage());
                        snackbar.show();
                    }
                }
            });

            return null;
        }
    }

    public static class downloadCoopDetailsAsync extends AsyncTask<String, Void, Void> {


        public downloadCoopDetailsAsync() {

        }

        @Override
        protected Void doInBackground(String... strings) {

            Log.e(TAG,"CoopDetails: "+strings[0]);

            MainActivity.webRequest.setRequestListenerDownload(strings[0], new WebRequest.RequestListener() {
                @Override
                public void onRequestListener(String response, String param) {

                    mTvView.setText("");
                    mTvView.append(response);

                    if (response.equalsIgnoreCase("500")) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        setSnackbar("Failed Download Coop Details ," + param);
                        snackbar.show();

                        myPreferences.savePrefString(Constant.COOP_DETAILS_STATUS, Constant.NO);
                        return;
                    }

                    try {
                        JSONArray jsonArray = new JSONArray(response);

                        if (jsonArray.length() == 0) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            setSnackbar("No Details for this coop...");
                            snackbar.show();

                            myPreferences.savePrefString(Constant.COOP_DETAILS_STATUS, Constant.NO);
                            return;
                        }

                        coopdetailssize = +jsonArray.length();
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject obj = jsonArray.getJSONObject(i);

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

                            coopdetailssave = +DB.saveCoopDetails(DB, CoopID, CoopName, CoopType,
                                    Classification, ReadingToDueDate, Acronym,
                                    BillingCode, businessaddress, telno, tinno);


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        setSnackbar("Coop Details, " + e.getMessage());
                        snackbar.show();
                    }
                }
            });

            return null;
        }
    }

    public static class downloadRateComponentAsync extends AsyncTask<String, Void, Void> {

        public downloadRateComponentAsync() {
        }

        @Override
        protected Void doInBackground(String... strings) {

            Log.e(TAG,"RateComponent: "+strings[0]);

            MainActivity.webRequest.setRequestListenerDownload(strings[0], new WebRequest.RequestListener() {
                @Override
                public void onRequestListener(String response, String param) {

                    mTvView.setText("");
                    mTvView.append(response);

                    if (response.equalsIgnoreCase("500")) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        setSnackbar("Failed Download Rate Component ," + param);
                        snackbar.show();

                        myPreferences.savePrefString(Constant.RATE_COMPONENT_STATUS, Constant.NO);
                        return;
                    }

                    try {
                        JSONArray jsonArray = new JSONArray(response);

                        if (jsonArray.length() == 0) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            setSnackbar("No Rate Component...");
                            snackbar.show();

                            myPreferences.savePrefString(Constant.RATE_COMPONENT_STATUS, Constant.NO);
                            return;
                        }

                        ratecomponentsize = +jsonArray.length();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String CoopID = obj.getString("PowerUtilityID");
                            String CoopName = obj.getString("PowerUtilityName");
                            String RateComponent = obj.getString("RateComponent");
                            String Details = obj.getString("Details");
                            String IsActive = obj.getString("IsActive");
                            String Notes1 = obj.getString("Notes1");
                            ratecomponentsave = +DB.saveRateComponent(DB, CoopID, CoopName, RateComponent, Details, IsActive, Notes1);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        setSnackbar("Rate Component, " + e.getMessage());
                        snackbar.show();
                    }
                }
            });
            return null;
        }
    }

    public static class downloadRateSegmentAsync extends AsyncTask<String, Void, Void> {

        public downloadRateSegmentAsync() {

        }

        @Override
        protected Void doInBackground(String... strings) {

            Log.e(TAG,"RateSegment: "+strings[0]);
            MainActivity.webRequest.setRequestListenerDownload(strings[0], new WebRequest.RequestListener() {
                @Override
                public void onRequestListener(String response, String param) {

                    mTvView.setText("");
                    mTvView.append(response);

                    if (response.equalsIgnoreCase("500")) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        setSnackbar("Failed Download Rate Segment ," + param);
                        snackbar.show();

                        myPreferences.savePrefString(Constant.RATE_SEGMENT_STATUS, Constant.NO);
                        return;
                    }

                    try {
                        JSONArray jsonArray = new JSONArray(response);

                        if (jsonArray.length() == 0) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            setSnackbar("No Rate Segment...");
                            snackbar.show();

                            myPreferences.savePrefString(Constant.RATE_SEGMENT_STATUS, Constant.NO);
                            return;
                        }

                        ratesegmentsize = +jsonArray.length();
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject obj = jsonArray.getJSONObject(i);

                            String CoopID = obj.getString("PowerUtilityID");
                            String RateSegmentCode = obj.getString("RateSegmentCode");
                            String RateSegmentName = obj.getString("RateSegmentName");
                            String Details = obj.getString("Details");
                            String IsActive = obj.getString("IsActive");

                            ratesegmentsave = +DB.saveRateSegment(DB, CoopID, RateSegmentCode,
                                    RateSegmentName, Details, IsActive);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        setSnackbar("Rate Segment, " + e.getMessage());
                        snackbar.show();
                    }
                }
            });
            return null;
        }
    }

    public static class downloadLifeLineDiscountPolicyAsync extends AsyncTask<String, Void, Void> {


        public downloadLifeLineDiscountPolicyAsync() {

        }

        @Override
        protected Void doInBackground(String... strings) {
            Log.e(TAG,"LifeLineDiscount: "+strings[0]);
            MainActivity.webRequest.setRequestListenerDownload(strings[0], new WebRequest.RequestListener() {
                @Override
                public void onRequestListener(String response, String param) {

                    mTvView.setText("");
                    mTvView.append(response);

                    if (response.equalsIgnoreCase("500")) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        setSnackbar("Failed Download Lifeline discount policy ," + param);
                        snackbar.show();

                        myPreferences.savePrefString(Constant.LIFELINE_POLICY_STATUS, Constant.NO);
                        return;
                    }

                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray.length() == 0) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            setSnackbar("No Lifeline policy...");
                            snackbar.show();

                            myPreferences.savePrefString(Constant.LIFELINE_POLICY_STATUS, Constant.NO);
                            return;
                        }

                        lifelinepolicysize = +jsonArray.length();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String lifeLineKwh = object.getString(DBInfo.LifelineConsumption);
                            String percent = object.getString(DBInfo.LifelinePercentage);
                            String decimal = object.getString(DBInfo.LifelineInDecimal);
                            lifelinepolicysave = +DB.saveLifeLifePolicy(DB, lifeLineKwh, percent, decimal);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        setSnackbar("Lifeline Policy, " + e.getMessage());
                        snackbar.show();
                    }
                }
            });

            return null;
        }
    }

    public static class downloadThresholdAsync extends AsyncTask<String, Void, Void> {



        public downloadThresholdAsync() {

        }

        @Override
        protected Void doInBackground(String... strings) {

            Log.e(TAG,"Threshold: "+strings[0]);
            MainActivity.webRequest.setRequestListenerDownload(strings[0], new WebRequest.RequestListener() {
                @Override
                public void onRequestListener(String response, String param) {

                    mTvView.setText("");
                    mTvView.append(response);

                    if (response.equalsIgnoreCase("500")) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        setSnackbar("Failed Download threshold ," + param);
                        snackbar.show();

                        myPreferences.savePrefString(Constant.THRESHOLD_STATUS, Constant.NO);
                        return;
                    }

                    try {
                        JSONArray jsonArray = new JSONArray(response);

                        if (jsonArray.length() == 0) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            setSnackbar("No threshold...");
                            snackbar.show();

                            myPreferences.savePrefString(Constant.THRESHOLD_STATUS, Constant.NO);
                            return;
                        }

                        thresholdsize = +jsonArray.length();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String settingcode = object.getString(DBInfo.SettingsCode);
                            String percent = object.getString("Details");

                            thresholdsave = +DB.saveThreshold(DB, settingcode, percent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        setSnackbar("Threshold, " + e.getMessage());
                        snackbar.show();
                    }

                }
            });

            return null;
        }
    }

    public static class downloadAccountsAsync extends AsyncTask<String, Void, Void> {

        public downloadAccountsAsync() {

        }

        @Override
        protected Void doInBackground(final String... strings) {

            MainActivity.webRequest.setRequestListenerDownload(strings[0], new WebRequest.RequestListener() {
                @Override
                public void onRequestListener(String response, String param) {

                    mTvView.setText("");
                    mTvView.append(response);

                    if (response.equalsIgnoreCase("500")) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        setSnackbar("Download Accounts failed, " + param);
                        snackbar.show();
                        myPreferences.savePrefString(Constant.ACCOUNT_STATUS, Constant.NO);
                        return;
                    }

                    try {
                        JSONObject details;
                        Account account;
                        String a = "";
                        JSONArray jsonArray = new JSONArray(response);
                        if (jsonArray.length() > 0) {
                            a = jsonArray.getJSONObject(0).getString("AccountID");
                            if (!a.equalsIgnoreCase("0")) {
                                accountssize = +jsonArray.length();
                            }
                        }


                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String acc = obj.getString("AccountID");
                            if (!acc.equalsIgnoreCase("0")) {
                                details = new JSONObject();
                                details.put("BillMonth", "");
                                details.put("PrevBilling", "0");
                                details.put("Address", obj.getString("Address"));
                                details.put("SeniorCitizenStatus", obj.getString("SeniorCitizenStatus"));
                                details.put("SCExpiryDate", obj.getString("SCExpiryDate"));
                                details.put("Penalty", "0");
                                details.put("RateSched", obj.getString("RateSched"));
                                details.put("Multiplier", obj.getString("Multiplier"));
                                details.put("DemandKW", obj.getString("DemandKW"));
                                details.put("AdvancePayment", obj.getString("AdvancePayment"));
                                details.put("BillDeposit", obj.getString("BillDeposit"));
                                details.put("LastReadingDate", obj.getString("LastReadingDate"));
                                details.put("InitialReading", obj.getString("InitialReading"));
                                details.put("ExportPreviousReading", obj.getString("ExportReading"));
                                details.put("IsChangeMeter", obj.getString("IsChangeMeter"));
                                details.put("MeterBrand", obj.getString("MeterBrand"));
                                details.put("Consume", "0");
                                details.put("ExportConsume", "0");
                                details.put("Reading", "0");
                                details.put("ExportReading", "0");
                                details.put("Remarks", "");
                                String arrears = obj.getString("Arrears");
                                String interestFlag = obj.getString("InterestFlag");
                                JSONArray jsonArrears = new JSONArray(arrears);
                                JSONObject jsonArr;
                                JSONArray jsonArray1 = new JSONArray();
                                if (jsonArrears.length() > 0) {
                                    for (int aa = 0; aa < jsonArrears.length(); aa++) {
                                        jsonArr = new JSONObject();
                                        JSONObject jsonObject = jsonArrears.getJSONObject(aa);
                                        if (jsonObject.getString("Status").equalsIgnoreCase("UNPAID")) {
                                            jsonArr.put("BillNo", jsonObject.getString("BillNo"));
                                            jsonArr.put("BillMonth", jsonObject.getString("BillMonth"));
                                            jsonArr.put("BillAmount", jsonObject.getString("BillAmount"));
                                            if (interestFlag.equalsIgnoreCase("0")) {
                                                jsonArr.put("Penalty", "0");
                                            } else {
                                                jsonArr.put("Penalty", jsonObject.getString("Penalty"));
                                            }
                                        }

                                        jsonArray1.put(jsonArr);

                                    }
                                }

                                String strArr;
                                if (jsonArray.length() == 0) {
                                    strArr = arrears;
                                } else {
                                    strArr = jsonArray1.toString();
                                }

                                account = gson.fromJson(obj.toString(), Account.class);
                                String routeID = strings[2];
                                String dueDate = strings[1];
                                account.setDueDate(dueDate);
                                accountssave = accountssave + DB.saveAccount(DB, account, details.toString(), routeID, strArr, "");
                            } else {

                            }
                        }

                        if (jsonArray.length() > 0) {
                            if (!a.equalsIgnoreCase("0")) {
                                new downloadAccountsAsync().execute(strings[0]);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        setSnackbar("Accounts, " + e.getMessage());
                        snackbar.show();
                    }

                }
            });

            return null;
        }

    }

    public void updateReaderTable() {

        /** check table is attempt to clear */

        for(Route r: routeArrayList) {
            String cmdUpdateReaderTable = baseurl + "?cmd=update&routeid=" + r.getRouteID() + "&mac=" + mac;
            MainActivity.webRequest.sendRequest(cmdUpdateReaderTable, "updateReaderTable");
        }

//        if (numberOfTimesDeletingTable == 0) {
//            /** then update Reader table isDownload... in server */
//
//            Cursor cursorRouteID = DB.getRoutesNo(DB);
//            if (cursorRouteID.moveToFirst()) {
//                while (!cursorRouteID.isAfterLast()) {
//                    String routeID = cursorRouteID.getString(cursorRouteID.getColumnIndex("RouteID"));
//                    String cmdUpdateReaderTable = baseurl + "?cmd=update&routeid=" + routeID + "&mac=" + mac;
//                    MainActivity.webRequest.sendRequest(cmdUpdateReaderTable, "updateReaderTable");
//                    cursorRouteID.moveToNext();
//                }
//            }
//        } else {
//            clearData();
//            if (mDialog.isShowing()) {
//                mDialog.dismiss();
//            }
//
//            setSnackbar("Please download again...");
//            snackbar.show();
//            mTvView.setText("");
//        }
    }
    //endregion

    //region IListener

//    @Override
//    public void onSuccess(String type, String response, String params, String param2, String param3) {
//        //Toast.makeText(getContext(), type, Toast.LENGTH_SHORT).show();
//
//
//        Gson gson = new GsonBuilder().create();
//
//        mTvView.append(response);
//
//
//        switch (type) {
//
//            case "dlRoutes":
//
//                Route route;
//
//                String coopID = null, readerID = null, readerName = null,
//                        accountIDFrom, accountIDTo, districtID, routeID, tagClass = null;
//                String urlParam, cmdAccounts, sequenceRef, sequenceNoFrom, sequenceNoTo;
//
//                try {
//                    JSONArray jsonArray = new JSONArray(response);
//
//                    if (jsonArray.length() > 0) {
//
//                        numberOfRoutesDownloaded = jsonArray.length();
//
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            accountSavePerRoute = 0;
//                            JSONObject obj = jsonArray.getJSONObject(i);
//                            coopID = obj.getString("CoopID");
//
//                            if (coopID.equalsIgnoreCase("FAILED")) {
//
//                                if (mDialog.isShowing()) {
//                                    mDialog.dismiss();
//                                }
//
//
//                                setSnackbar("Api Logs table not exist...");
//                                snackbar.show();
//                                return;
//                            }
//                            sequenceRef = obj.getString("DownloadRef");
//                            sequenceNoFrom = obj.getString("SequenceNoFrom");
//                            sequenceNoTo = obj.getString("SequenceNoTo");
//                            readerID = obj.getString("ReaderID");
//                            readerName = obj.getString("ReaderName");
//                            districtID = obj.getString("DistrictID");
//                            routeID = obj.getString("RouteID");
//                            tagRoutid = routeID;
//                            accountIDFrom = obj.getString("AccountIDFrom");
//                            accountIDTo = obj.getString("AccountIDTo");
//                            dueDate = obj.getString("DueDate");
//                            tagClass = obj.getString("TagClass");
//                            route = gson.fromJson(obj.toString(), Route.class);
//                            myPreferences.savePrefString(Constant.TAGCLASS, tagClass);
//                            /**check to avoid duplication of routes and accounts*/
//                            ifRouteExist = DB.checkRouteIsExist(DB, routeID, districtID);
//                            if (!ifRouteExist) {
//                                DB.saveRoute(DB, route);
//                            }
//
//
//                            cmdAccounts = baseurl + "?cmd=getaccounts&coopid=" + coopID
//                                    + "&districtid=" + URLEncoder.encode(districtID)
//                                    + "&routeid=" + URLEncoder.encode(routeID)
//                                    + "&idFrom=" + accountIDFrom
//                                    + "&idTo=" + accountIDTo
//                                    + "&mac=" + mac
//                                    + "&tagclass=" + tagClass
//                                    + "&rd=0"
//                                    + "&sequenceRef=" + sequenceRef;
//
//                            urlParam = baseurl + "?cmd=getaccounts&coopid=" + coopID
//                                    + "&districtid=" + URLEncoder.encode(districtID)
//                                    + "&routeid=" + URLEncoder.encode(routeID)
//                                    + "&idFrom=" + accountIDFrom
//                                    + "&idTo=" + accountIDTo
//                                    + "&mac=" + mac
//                                    + "&tagclass=" + tagClass
//                                    + "&rd=0"
//                                    + "&sequenceRef=" + sequenceRef;
//
//                            if (sequenceRef.equalsIgnoreCase("1")) {
//                                cmdAccounts = cmdAccounts + "&sequenceNoFrom=" + sequenceNoFrom + "&sequenceNoTo=" + sequenceNoTo;
//                                urlParam = urlParam + "&sequenceNoFrom=" + sequenceNoFrom + "&sequenceNoTo=" + sequenceNoTo;
//                            }
//
//
//                            /**rd means download all thus not downloaded accounts*/
//                            cmdAccounts = cmdAccounts + "&offset=0";
//                            if (numberOfTimesDeletingTable == 0) {
//                                int update_error = MyPreferences.getInstance(ctx).getPrefInt("update_error");
//                                if (update_error == 1) {
//                                    MyPreferences.getInstance(ctx).savePrefInt("update_error", 0);
//                                }
//
//                                myPreferences.savePrefInt("rd", 0);
//                                MainActivity.webRequest.sendRequest(cmdAccounts, "Accounts", routeID, dueDate, urlParam, this);
//                                //Log.e(TAG,"firstFetchaccount: " + cmdAccounts);
//                            }
//                        }
//
//
//                        Reader reader = new Reader(coopID, readerID, readerName);
//
//                        DB.syncSettingsInfo(DB, reader);
//                        DB.saveConnection(DB, coopID, txtHost, strPort);
//
//                        MainActivity.setConnSettings();
//
//                        MainActivity.setReader();
//                    } else {
//                        if (!params.equalsIgnoreCase("1")) {
//
//                            String tagC = myPreferences.getPrefString(Constant.TAGCLASS);
//                            int rd = myPreferences.getPrefInt("rd");
//                            if (rd == 0) {
//                                ArrayList<Route> _list = DB.getRoute(DB);
//
//                                routeLength = _list.size();
//
//                                for (int i = 0; i < _list.size(); i++) {
//                                    cmdAccounts = baseurl + "?cmd=getaccounts&coopid=" + _list.get(i).getCoopID()
//                                            + "&districtid=" + _list.get(i).getDistrictID()
//                                            + "&routeid=" + _list.get(i).getRouteID()
//                                            + "&idFrom=" + _list.get(i).getAccountIDFrom()
//                                            + "&idTo=" + _list.get(i).getAccountIDTo()
//                                            + "&mac=" + mac
//                                            + "&tagclass=" + tagC
//                                            + "&rd=1";
//
//                                    String ddate = DB.getDueDate(DB, _list.get(i).getRouteID());
//                                    MainActivity.webRequest.sendRequest(cmdAccounts, "Accounts", _list.get(i).getRouteID(), ddate, "rd", this);
//                                }
//                            } else {
//                                if (mDialog.isShowing()) {
//                                    mDialog.dismiss();
//                                }
//                                Toast.makeText(ctx, "No Routes Downloaded", Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//
//                            /** stop the process, waiting for the assign routes*/
//                        } else {
//                            Toast.makeText(ctx, "No New Assign Routes", Toast.LENGTH_SHORT).show();
//                        }
//                    } // End IF
//                } catch (JSONException e) {
//                    Log.e(TAG, "json error: " + response);
//                    Log.e(TAG, "json error: " + e.getMessage());
//                    e.printStackTrace();
//                }
//
//
//                /**
//                 1 params indicates that user click more times in download button
//                 check the reader table from clouds where the IsDownloadStatus = 0
//                 */
//
//
//                String cmdRateCode = baseurl + "?cmd=getRateCode&coopid=" + coopID + "&mac=" + mac;
//                String cmdCoopDetails = baseurl + "?cmd=getCoopDetails&coopid=" + coopID + "&mac=" + mac;
//                String cmdRateComponent = baseurl + "?cmd=getRateComponent&coopid=" + coopID + "&mac=" + mac;
//                String cmdRateSegment = baseurl + "?cmd=getRateSegment&coopid=" + coopID + "&mac=" + mac;
//                String cmdPolicy = baseurl + "?cmd=getBillingPolicy&coopid=" + coopID + "&mac=" + mac + "&tagclass=" + tagClass;
//                String cmdSchedule = baseurl + "?cmd=getRateSchedule&coopid=" + coopID + "&mac=" + mac + "&tagclass=" + tagClass;
//                String cmdLifeLineDiscount = baseurl + "?cmd=ld&coopid=" + coopID + "&mac=" + mac;
//                String cmdThreshold = baseurl + "?cmd=threshold&mac=" + mac;
//                //Log.e(TAG)
//                if (myPreferences.getPrefInt(Constant.RATE_CODE_COUNT) == 0) {
//                    MainActivity.webRequest.sendRequest(cmdRateCode, "Code", "", "", "", this);
//                }
//
//                if (myPreferences.getPrefInt(Constant.COOP_DETAILS_COUNT) == 0) {
//                    MainActivity.webRequest.sendRequest(cmdCoopDetails, "Coop", "", "", "", this);
//                }
//
//                if (myPreferences.getPrefInt(Constant.RATE_COMPONENT_COUNT) == 0) {
//                    MainActivity.webRequest.sendRequest(cmdRateComponent, "Component", "", "", "", this);
//                }
//
//                if (myPreferences.getPrefInt(Constant.RATE_SEGMENT_COUNT) == 0) {
//                    MainActivity.webRequest.sendRequest(cmdRateSegment, "Segment", "", "", "", this);
//                }
//
//                if (tagClass == "1") {
//                    if (myPreferences.getPrefInt(Constant.BILLING_POLICY_HIGHVOLT_COUNT) == 0) {
//                        MainActivity.webRequest.sendRequest(cmdPolicy, "Policy", "", "", "", this);
//                    }
//
//                    if (myPreferences.getPrefInt(Constant.RATE_SCHEDULE_COUNT_HIGHERVOLT) == 0) {
//                        MainActivity.webRequest.sendRequest(cmdSchedule, "Schedule", "", "", "", this);
//                    }
//                } else if (tagClass == "0") {
//                    if (myPreferences.getPrefInt(Constant.BILLING_POLICY_NONHIGHVOLT_COUNT) == 0) {
//                        MainActivity.webRequest.sendRequest(cmdPolicy, "Policy", "", "", "", this);
//                    }
//
//                    if (myPreferences.getPrefInt(Constant.RATE_SCHEDULE_COUNT_NON_HIGHERVOLT) == 0) {
//                        MainActivity.webRequest.sendRequest(cmdSchedule, "Schedule", "", "", "", this);
//                    }
//                } else {
//                    if (myPreferences.getPrefInt(Constant.NET_METERING_POLICY) == 0) {
//                        MainActivity.webRequest.sendRequest(cmdPolicy, "Policy", "", "", "", this);
//                    }
//
//                    if (myPreferences.getPrefInt(Constant.NET_METERING_RATE_SCHEDULE) == 0) {
//                        MainActivity.webRequest.sendRequest(cmdSchedule, "Schedule", "", "", "", this);
//                    }
//                }
//
//                if (myPreferences.getPrefInt(Constant.LIFELINE_POLICY_COUNT) == 0) {
//                    MainActivity.webRequest.sendRequest(cmdLifeLineDiscount, "LifeLineDiscount", "", "", "", this);
//                }
//
//                if (myPreferences.getPrefInt(Constant.THRESHOLD_COUNT) == 0) {
//                    //Log.e(TAG,"cmdThreshold: "+ cmdThreshold);
//                    MainActivity.webRequest.sendRequest(cmdThreshold, "threshold", "", "", "", this);
//                }
//
//                break;
//
//
//            case "Code":
//                try {
//
//                    JSONArray array = new JSONArray(response);
//
//                    for (int i = 0; i < array.length(); i++) {
//
//                        JSONObject obj = array.getJSONObject(i);
//
//                        String CoopID = obj.getString("PowerUtilityID");
//                        String CoopName = obj.getString("PowerUtilityName");
//                        String RateCode = obj.getString("RateCode");
//                        String Details = obj.getString("Details");
//                        String IsActive = obj.getString("IsActive");
//
//
//                        int save = DB.saveRateCode(DB, CoopID, CoopName, RateCode, Details, IsActive);
//                        myPreferences.savePrefInt(Constant.RATE_CODE_COUNT, save);
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//
//                break;
//
//            case "Coop":
//                try {
//
//                    JSONArray array = new JSONArray(response);
//
//                    if (array.length() > 0) {
//
//                        for (int i = 0; i < array.length(); i++) {
//
//                            JSONObject obj = array.getJSONObject(i);
//
//                            String CoopID = obj.getString("PowerUtilityID");
//                            String CoopName = obj.getString("PowerUtilityName");
//                            String CoopType = obj.getString("CoopType");
//                            String Classification = obj.getString("Classification");
//                            String ReadingToDueDate = obj.getString("ReadingToDueDate");
//                            String Acronym = obj.getString("Acronym");
//                            String BillingCode = obj.getString("BillingCode");
//                            String businessaddress = obj.getString("BusinessAddress");
//                            String telno = obj.getString("TelNo");
//                            String tinno = obj.getString("Extra2");
//
//                            int save = DB.saveCoopDetails(DB, CoopID, CoopName, CoopType,
//                                    Classification, ReadingToDueDate, Acronym,
//                                    BillingCode, businessaddress, telno, tinno);
//
//                            myPreferences.savePrefInt(Constant.COOP_DETAILS_COUNT, save);
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                break;
//
//            case "Component":
//                try {
//                    int save = 0;
//                    int arraylength;
//                    JSONArray array = new JSONArray(response);
//
//                    if (array.length() > 0) {
//                        arraylength = array.length();
//                        for (int i = 0; i < array.length(); i++) {
//                            JSONObject obj = array.getJSONObject(i);
//                            String CoopID = obj.getString("PowerUtilityID");
//                            String CoopName = obj.getString("PowerUtilityName");
//                            String RateComponent = obj.getString("RateComponent");
//                            String Details = obj.getString("Details");
//                            String IsActive = obj.getString("IsActive");
//                            String Notes1 = obj.getString("Notes1");
//                            save = save + DB.saveRateComponent(DB, CoopID, CoopName, RateComponent, Details, IsActive, Notes1);
//
//                            if (save == arraylength) {
//                                myPreferences.savePrefInt(Constant.RATE_COMPONENT_COUNT, save);
//                            } else {
//                                myPreferences.savePrefInt(Constant.RATE_COMPONENT_COUNT, 0);
//                            }
//                        }
//
//                    } else {
//                        showToast("No Components Downloaded");
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                break;
//
//            case "Segment":
//                try {
//                    int save = 0;
//                    int arraylength;
//                    JSONArray array = new JSONArray(response);
//
//                    if (array.length() > 0) {
//                        arraylength = array.length();
//                        for (int i = 0; i < array.length(); i++) {
//
//                            JSONObject obj = array.getJSONObject(i);
//
//                            String CoopID = obj.getString("PowerUtilityID");
//                            String RateSegmentCode = obj
//                                    .getString("RateSegmentCode");
//                            String RateSegmentName = obj
//                                    .getString("RateSegmentName");
//                            String Details = obj.getString("Details");
//                            String IsActive = obj.getString("IsActive");
//
//
//                            save = save + DB.saveRateSegment(DB, CoopID, RateSegmentCode,
//                                    RateSegmentName, Details, IsActive);
//
//                            if (save == arraylength) {
//                                myPreferences.savePrefInt(Constant.RATE_SEGMENT_COUNT, save);
//                            } else {
//                                myPreferences.savePrefInt(Constant.RATE_SEGMENT_COUNT, 0);
//                            }
//                        }
//
//                    } else {
//                        showToast("No Rate Segment Available,Please check...");
//                        return;
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                break;
//
//            case "Policy":
//                try {
//                    int save = 0;
//                    int arraylength;
//                    JSONArray array = new JSONArray(response);
//
//
//                    if (array.length() > 0) {
//                        arraylength = array.length();
//                        for (int i = 0; i < array.length(); i++) {
//
//                            JSONObject obj = array.getJSONObject(i);
//
//                            //String CoopID = obj.getString("PowerUtilityID");
//                            //String CoopName = obj.getString("PowerUtilityName");
//                            String PolicyCode = obj.getString("PolicyCode");
//                            //String PolicyName = obj.getString("PolicyName");
//                            String PolicyType = obj.getString("PolicyType");
//                            String CustomerClass = obj
//                                    .getString("CustomerClass");
//                            //String SubClass = obj.getString("SubClass");
//                            String MinkWh = obj.getString("MinkWh");
//                            String MaxkWh = obj.getString("MaxkWh");
//                            String PercentAmount = obj
//                                    .getString("PercentAmount");
//
//                            save = save + DB.saveBillingPolicy(DB,
//                                    PolicyCode, PolicyType,
//                                    CustomerClass, MinkWh, MaxkWh,
//                                    PercentAmount);
//
//                            String tclass = myPreferences.getPrefString(Constant.TAGCLASS);
//                            if (tclass.equalsIgnoreCase("1")) {
//                                if (save == arraylength) {
//                                    myPreferences.savePrefInt(Constant.BILLING_POLICY_HIGHVOLT_COUNT, save);
//                                } else {
//                                    myPreferences.savePrefInt(Constant.BILLING_POLICY_HIGHVOLT_COUNT, 0);
//                                }
//
//                            } else if (tclass.equalsIgnoreCase("0")) {
//                                if (save == arraylength) {
//                                    myPreferences.savePrefInt(Constant.BILLING_POLICY_NONHIGHVOLT_COUNT, save);
//                                } else {
//                                    myPreferences.savePrefInt(Constant.BILLING_POLICY_NONHIGHVOLT_COUNT, 0);
//                                }
//                            } else {
//                                if (save == arraylength) {
//                                    myPreferences.savePrefInt(Constant.NET_METERING_POLICY, save);
//                                } else {
//                                    myPreferences.savePrefInt(Constant.NET_METERING_POLICY, 0);
//                                }
//                            }
//                        }
//
//                    } else {
//                        showToast("No Billing Policy Downloaded");
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                break;
//
//            case "Schedule":
//                try {
//                    int save = 0;
//                    int arraylength;
//                    JSONArray array = new JSONArray(response);
//                    arraylength = array.length();
//                    if (array.length() > 0) {
//
//                        for (int i = 0; i < array.length(); i++) {
//                            JSONObject obj = array.getJSONObject(i);
//                            //String coopid = obj.getString("PowerUtilityID");
//                            String ratesegment = obj.getString("RateSegment");
//                            String ratecomponent = obj.getString("RateComponent");
//                            String printorder = obj.getString("PrintOrder");
//                            String classification = obj.getString("Classification");
//                            //String ratesched = obj.getString("RateSched");
//                            String rateschedtype = obj.getString("RateSchedType");
//                            String amount = obj.getString("Amount");
//                            //String vatrate = obj.getString("VATRate");
//                            //String vatamount = obj.getString("VATAmount");
//                            //String franchisetaxrate = obj.getString("FranchiseTaxRate");
//                            //String franchisetaxamount = obj.getString("FranchiseTaxAmount");
//                            //String localtaxrate = obj.getString("LocalTaxRate");
//                            //String localtaxamount = obj.getString("LocalTaxAmount");
//                            //String totalamount = obj.getString("TotalAmount");
//                            //String isvat = obj.getString("IsVAT");
//                            //String isdvat = obj.getString("IsDVAT");
//                            String isUnderOver = obj.getString("IsOverUnder");
//                            // String isfranchisetax = obj.getString("IsFranchiseTax");
//                            //String islocaltax = obj.getString("IsLocalTax");
//                            String islifeline = obj.getString("IsLifeLine");
//                            String isscdiscount = obj.getString("IsSCDiscount");
//                            //String ratestatus = obj.getString("RateStatus");
//                            String dateFrom = obj.getString("BillMonth");
//                            String extra1 = obj.getString("RateComponentDetails");
//                            String IsExport = obj.getString("IsExport");
//
//
//                            save = save + DB.saveRateSchedule(DB, ratesegment,
//                                    ratecomponent, printorder, classification,
//                                    rateschedtype, amount, isUnderOver, islifeline,
//                                    isscdiscount, dateFrom, extra1, IsExport);
//
//                            String tclass = myPreferences.getPrefString(Constant.TAGCLASS);
//                            if (tclass.equalsIgnoreCase("1")) {
//                                if (save == arraylength) {
//                                    myPreferences.savePrefInt(Constant.RATE_SCHEDULE_COUNT_HIGHERVOLT, save);
//                                    isRatesScheduleErrorDownloading = false;
//                                } else {
//                                    myPreferences.savePrefInt(Constant.RATE_SCHEDULE_COUNT_HIGHERVOLT, 0);
//                                }
//                            } else if (tclass.equalsIgnoreCase("0")) {
//                                if (save == arraylength) {
//                                    myPreferences.savePrefInt(Constant.RATE_SCHEDULE_COUNT_NON_HIGHERVOLT, save);
//                                    isRatesScheduleErrorDownloading = false;
//                                } else {
//                                    myPreferences.savePrefInt(Constant.RATE_SCHEDULE_COUNT_NON_HIGHERVOLT, 0);
//                                }
//                            } else {
//                                if (save == arraylength) {
//                                    myPreferences.savePrefInt(Constant.NET_METERING_RATE_SCHEDULE, save);
//                                    isRatesScheduleErrorDownloading = false;
//                                } else {
//                                    myPreferences.savePrefInt(Constant.NET_METERING_RATE_SCHEDULE, 0);
//                                }
//                            }
//                        }
//                    } else {
//                        showToast("No Available Rates, Please check Rate Schedule");
//                        return;
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                break;
//            case "LifeLineDiscount":
//                try {
//                    int save = 0;
//                    JSONArray array = new JSONArray(response);
//                    int arraycount = array.length();
//                    if (array.length() > 0) {
//                        for (int i = 0; i < array.length(); i++) {
//                            JSONObject object = array.getJSONObject(i);
//                            String lifeLineKwh = object.getString(DBInfo.LifelineConsumption);
//                            String percent = object.getString(DBInfo.LifelinePercentage);
//                            String decimal = object.getString(DBInfo.LifelineInDecimal);
//                            save = save + DB.saveLifeLifePolicy(DB, lifeLineKwh, percent, decimal);
//                            if (save == arraycount) {
//                                myPreferences.savePrefInt(Constant.LIFELINE_POLICY_COUNT, save);
//                            } else {
//                                myPreferences.savePrefInt(Constant.LIFELINE_POLICY_COUNT, 0);
//                            }
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    Log.e(TAG, "LifeLineDiscount(err)" + e.getMessage());
//                }
//                break;
//            case "Accounts":
//                int DownloadCount;
//                int DownloadSave = 0;
//                try {
//
//                    Account account;
//
//                    JSONArray array = new JSONArray(response);
//                    if (array.length() > 0) {
//
//                        DownloadCount = array.length();
//                        JSONObject details;
//                        for (int i = 0; i < array.length(); i++) {
//                            JSONObject obj = array.getJSONObject(i);
//                            String acc = obj.getString("AccountID");
//                            if (!acc.equalsIgnoreCase("0")) {
//                                noAccounts = false;
//                                details = new JSONObject();
//                                details.put("BillMonth", "");
//                                details.put("PrevBilling", "0");
//                                details.put("Address", obj.getString("Address"));
//                                details.put("SeniorCitizenStatus", obj.getString("SeniorCitizenStatus"));
//                                details.put("SCExpiryDate", obj.getString("SCExpiryDate"));
//                                details.put("Penalty", "0");
//                                details.put("RateSched", obj.getString("RateSched"));
//                                details.put("Multiplier", obj.getString("Multiplier"));
//                                details.put("DemandKW", obj.getString("DemandKW"));
//                                details.put("AdvancePayment", obj.getString("AdvancePayment"));
//                                details.put("BillDeposit", obj.getString("BillDeposit"));
//                                details.put("LastReadingDate", obj.getString("LastReadingDate"));
//                                details.put("InitialReading", obj.getString("InitialReading"));
//                                //details.put("PrevReading", obj.getString("PrevReading"));
//                                //details.put("PrevFinalReading", obj.getString("PrevFinalReading"));
//                                details.put("ExportPreviousReading", obj.getString("ExportReading"));
//                                details.put("IsChangeMeter", obj.getString("IsChangeMeter"));
//                                details.put("MeterBrand", obj.getString("MeterBrand"));
//                                details.put("Consume", "0");
//                                details.put("ExportConsume", "0");
//                                details.put("Reading", "0");
//                                details.put("ExportReading", "0");
//                                details.put("Remarks", "");
//                                String arrears = obj.getString("Arrears");
//                                String interestFlag = obj.getString("InterestFlag");
//                                JSONArray jsonArray = new JSONArray(arrears);
//                                JSONObject jsonArr;
//                                JSONArray jsonArray1 = new JSONArray();
//                                if (jsonArray.length() > 0) {
//                                    for (int a = 0; a < jsonArray.length(); a++) {
//                                        jsonArr = new JSONObject();
//                                        JSONObject jsonObject = jsonArray.getJSONObject(a);
//                                        if (jsonObject.getString("Status").equalsIgnoreCase("UNPAID")) {
//                                            jsonArr.put("BillNo", jsonObject.getString("BillNo"));
//                                            jsonArr.put("BillMonth", jsonObject.getString("BillMonth"));
//                                            jsonArr.put("BillAmount", jsonObject.getString("BillAmount"));
//                                            if (interestFlag.equalsIgnoreCase("0")) {
//                                                jsonArr.put("Penalty", "0");
//                                            } else {
//                                                jsonArr.put("Penalty", jsonObject.getString("Penalty"));
//                                            }
//                                        }
//
//                                        jsonArray1.put(jsonArr);
//                                        //Log.e(TAG,"here: "+ jsonObject.getString("Penalty"));
//                                    }
//                                }
//
//                                String strArr;
//                                if (jsonArray.length() == 0) {
//                                    strArr = arrears;
//                                } else {
//                                    strArr = jsonArray1.toString();
//                                }
//                                //Log.e(TAG,"strArr: "+ strArr);
//                                account = gson.fromJson(obj.toString(), Account.class);
//                                account.setDueDate(param2);
//
//                                /** update_error means something wrong when updating reader table IsDownload*/
//                                int update_error = MyPreferences.getInstance(ctx).getPrefInt("update_error");
//                                if (update_error == 0) {
//                                    DownloadSave = DownloadSave + DB.saveAccount(DB, account, details.toString(), params, strArr, param3);
//
//                                    if (DownloadSave == DownloadCount) {
//                                        totalAccountSave = totalAccountSave + DownloadSave;
//                                        accountSavePerRoute = totalAccountSave;
//                                        if (!param3.equalsIgnoreCase("rd")) {
//                                            String url = param3 + "&offset=0"; //+ offset;
//
//                                            //Log.e(TAG,"accountfetch: "+ url);
//                                            /**
//                                             *  offset set to zero always bec. isDownload in billing_connection_profile set to 1 if download success.
//                                             *
//                                             * numberOfRoutesPassing means counter of failing request
//                                             * */
//                                            if (numberOfTimesDeletingTable == 0) {
//                                                mDialog.setMessage("" + totalAccountSave + " Accounts saved/DownLoading Data.Please wait.");
//                                                MainActivity.webRequest.sendRequest(url, "Accounts", params, dueDate, param3, this);
//                                                enableButton();
//                                            }
//                                        }
//
//                                        if (param3.equalsIgnoreCase("rd")) {
//                                            rdsuccess = rdsuccess + 1;
//                                            if (mDialog.isShowing()) {
//                                                mDialog.dismiss();
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    clearData();
//                                }
//                            } else {
//                                if (acc.equalsIgnoreCase("FAILED")) {
//
//                                }
//
//                                /**params = routeNo*/
//                                int accountCount = DB.getDataCountThisRoute(DB, params);
//                                if (accountCount > 0) {
//                                    numberOfAccountSavingPerRoutes++;
//                                }
//
//                                /**rd = redownload*/
//                                if (param3.equalsIgnoreCase("rd")) {
//
//                                    if (routeLength == rdsuccess) {
//                                        myPreferences.savePrefInt("rd", 1);
//                                    }
//
//                                    if (mDialog.isShowing()) {
//                                        mDialog.dismiss();
//                                    }
//                                }
//
//                                if (acc.equalsIgnoreCase("0")) {
//                                    if (accountSavePerRoute == 0) {
//                                        noAccounts = true;
//                                    }
//                                }
//                            }
//                        }
//                    } else {
//                        showToast("No Accounts Downloaded");
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    Log.e(TAG, "JSONException :" + e.getMessage());
//                }
//
//                break;
//
//            case "threshold":
//                try {
//                    int save = 0;
//                    JSONArray array = new JSONArray(response);
//                    int arraycount = array.length();
//                    if (array.length() > 0) {
//                        for (int i = 0; i < array.length(); i++) {
//                            JSONObject object = array.getJSONObject(i);
//                            String settingcode = object.getString(DBInfo.SettingsCode);
//                            String percent = object.getString("Details");
//
//                            save = save + DB.saveThreshold(DB, settingcode, percent);
//                            if (save == arraycount) {
//                                myPreferences.savePrefInt(Constant.THRESHOLD_COUNT, save);
//                            } else {
//                                myPreferences.savePrefInt(Constant.THRESHOLD_COUNT, 0);
//                            }
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    Log.e(TAG, "threshold(err)" + e.getMessage());
//                }
//                break;
//            default:
//        }
//
//
//        if (numberOfRoutesDownloaded > 0) {
//            if (numberOfRoutesDownloaded == numberOfAccountSavingPerRoutes) {
//                if (mDialog.isShowing()) {
//                    mDialog.dismiss();
//                }
//
//                if (numberOfTimesDeletingTable == 0) {
//                    Toast.makeText(ctx, "Download Complete", Toast.LENGTH_LONG).show();
//                    updateReaderTable();
//
//                }
//
//            } else {
//                if (noAccounts) {
//                    if (mDialog.isShowing()) {
//                        mDialog.dismiss();
//                    }
//
//                    Toast.makeText(ctx, "No Available Accounts in this route...", Toast.LENGTH_LONG).show();
//                }
//            }
//
//            /**failed download*/
//            if (numberOfTimesDeletingTable > 0) {
//                if (mDialog.isShowing()) {
//                    mDialog.dismiss();
//                }
//
//                mTvView.setText("");
//                clearData();
//                setSnackbar("Download accounts not completed, please download again.");
//                snackbar.show();
//                mTvView.setText("");
//                return;
//            }
//
//            if (ifRouteExist) {
//                if (mDialog.isShowing()) {
//                    mDialog.dismiss();
//                }
//            }
//        }
//
//
//        BtnDownLoad.setEnabled(true);
//    }




//    @Override
//    public void onFailed(VolleyError error, String type) {
//        BtnDownLoad.setEnabled(true);
//        if (mDialog.isShowing()) {
//            mDialog.dismiss();
//        }
//
//
//        if (type.equalsIgnoreCase("Accounts")) {
//            clearData();
//            setSnackbar("Failed to download Accounts, please retry...");
//            snackbar.show();
//            mTvView.setText("");
//        }
//
//        if (type.equalsIgnoreCase("dlRoutes")) {
//            clearData();
//            setSnackbar("Failed to download Routes, please retry...");
//            snackbar.show();
//            mTvView.setText("");
//        }
//
//        if (type.equalsIgnoreCase("threshold")) {
//            clearData();
//            setSnackbar("Failed to download threshold, please retry...");
//            snackbar.show();
//            mTvView.setText("");
//        }
//
//        if (type.equalsIgnoreCase("Schedule")) {
//            setSnackbar("Failed to download Rate Schedule, please click button to download again...");
//            snackbar.show();
//        }
//
//        if (error.getMessage() == null) {
//            setSnackbar("Server error...");
//        } else {
//            setSnackbar(error.getMessage());
//            snackbar.show();
//            mTvView.setText("");
//            Log.e(TAG, "failed download: " + error.getMessage());
//        }
//    }
}