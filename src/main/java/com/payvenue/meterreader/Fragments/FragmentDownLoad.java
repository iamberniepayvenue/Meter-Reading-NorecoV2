package com.payvenue.meterreader.Fragments;

import android.annotation.SuppressLint;
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

    private static Button BtnDownLoad;
    private Button BtnDelete;
    private static View rootView;
    SQLiteDatabase database;
    static String mac;
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


    private static ArrayList<Route> routeArrayList = new ArrayList<>();

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

                final int[] save = {0};
                baseurl = "http://" + txtHost + ":" + strPort;
                String cmdRoute = baseurl + "?cmd=getRoutes&mac=" + mac;
                Log.e(TAG, "routes: " + cmdRoute);
                MainActivity.webRequest.setRequestListenerDownload(cmdRoute, new WebRequest.RequestListener() {
                    @Override
                    public void onRequestListener(String response, String param) {

                        mTvView.append(response);

                        if (response.equalsIgnoreCase("500")) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }
                            setSnackbar("Failed, " + param);
                            snackbar.show();
                            return;
                        }


                        try {

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
                                    save[0] = save[0] + DB.saveRoute(DB, route);
                                }
                            }


                            Log.e(TAG, "route save: " + save[0]);
                            Log.e(TAG, "route length: " + jsonArray.length());
                            if (jsonArray.length() == 0) {
                                if (mDialog.isShowing()) {
                                    mDialog.dismiss();
                                }

                                setSnackbar("Device not registered \n or routes not available...");
                                snackbar.show();
                                return;
                            }

                            if (jsonArray.length() == save[0]) {
                                setSnackbar("routes completed..");
                                snackbar.show();
                                DB.syncSettingsInfo(DB, route);
                                routeArrayList = DB.getRoute(DB);
                                downloadRateSchedule();
                            }

                            if (jsonArray.length() > 0 || save[0] == 0) {
                                if (mDialog.isShowing()) {
                                    mDialog.dismiss();
                                }

                                setSnackbar("routes saved already..");
                                snackbar.show();
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

        if (myPreferences.getPrefString(Constant.ACCOUNT_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadAccounts(getContext(), 0);
        }

        if (myPreferences.getPrefString(Constant.RATE_CODE_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadRateCode(getContext());
        }

        if (myPreferences.getPrefString(Constant.COOP_DETAILS_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadCoopDetails(getContext());
        }

        if (myPreferences.getPrefString(Constant.RATE_COMPONENT_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadRateComponent(getContext());
        }

        if (myPreferences.getPrefString(Constant.RATE_SEGMENT_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadRateSegment(getContext());
        }

        if (myPreferences.getPrefString(Constant.POLICY_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadBillingPolicy(getContext());
        }

        if (myPreferences.getPrefString(Constant.RATE_SCHEDULE_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadRateSchedule();
        }

        if (myPreferences.getPrefString(Constant.LIFELINE_POLICY_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadLifeLineDiscountPolicy(getContext());
        }

        if (myPreferences.getPrefString(Constant.THRESHOLD_STATUS).equalsIgnoreCase(Constant.NO)) {
            downloadThreshold(getContext());
        }
    }

    public void downloadRateSchedule() {
        ArrayList<String> tagclassList = new ArrayList<>();
        String tagclass = "";
        String coopID = "";
        for (Route r : routeArrayList) {
            if (!r.getTagClass().equalsIgnoreCase(tagclass)) {
                tagclass = r.getTagClass();
                coopID = r.getCoopID();
                tagclassList.add(tagclass);
            }
        }

        mDialog.setMessage("DownLoading Rate Schedule...");
        mDialog.show();
        int length = tagclassList.size();
        for (String str : tagclassList) {
            String cmdSchedule = baseurl + "?cmd=getRateSchedule&coopid=" + coopID + "&mac=" + mac + "&tagclass=" + str;
            new downloadRateScheduleAsync(getContext(), length).execute(cmdSchedule);
        }
    }

    public static void downloadBillingPolicy(Context context) {

        ArrayList<String> tagclassList = new ArrayList<>();
        String tagclass = "";
        String coopID = "";

        for (Route r : routeArrayList) {
            if (!r.getTagClass().equalsIgnoreCase(tagclass)) {
                tagclass = r.getTagClass();
                coopID = r.getCoopID();
                tagclassList.add(tagclass);
            }

        }


        mDialog.setMessage("DownLoading billing policy...");
        mDialog.show();
        int length = tagclassList.size();
        for (String str : tagclassList) {
            String cmdPolicy = baseurl + "?cmd=getBillingPolicy&coopid=" + coopID + "&mac=" + mac + "&tagclass=" + str;
            new downloadBillingPolicyAsync(context, length).execute(cmdPolicy);
        }
    }

    public static void downloadRateCode(Context context) {
        if (routeArrayList.size() > 0) {
            mDialog.setMessage("DownLoading rate codes...");
            mDialog.show();

            String cmdRateCode = baseurl + "?cmd=getRateCode&coopid=" + routeArrayList.get(1).getCoopID() + "&mac=" + mac;
            new downloadRateCodeAsync(context).execute(cmdRateCode);
        }
    }


    public static void downloadCoopDetails(Context context) {
        if (routeArrayList.size() > 0) {
            mDialog.setMessage("DownLoading details...");
            mDialog.show();
            String cmdCoopDetails = baseurl + "?cmd=getCoopDetails&coopid=" + routeArrayList.get(1).getCoopID() + "&mac=" + mac;
            new downloadCoopDetailsAsync(context).execute(cmdCoopDetails);
        }
    }

    public static void downloadRateComponent(Context context) {
        if (routeArrayList.size() > 0) {
            mDialog.setMessage("DownLoading rate component...");
            mDialog.show();
            String cmdRateComponent = baseurl + "?cmd=getRateComponent&coopid=" + routeArrayList.get(1).getCoopID() + "&mac=" + mac;
            new downloadRateComponentAsync(context).execute(cmdRateComponent);
        }
    }

    public static void downloadRateSegment(Context context) {
        if (routeArrayList.size() > 0) {
            mDialog.setMessage("DownLoading rate segment...");
            mDialog.show();
            String cmdRateSegment = baseurl + "?cmd=getRateSegment&coopid=" + routeArrayList.get(1).getCoopID() + "&mac=" + mac;
            new downloadRateSegmentAsync(context).execute(cmdRateSegment);
        }
    }


    public static void downloadLifeLineDiscountPolicy(Context context) {
        if (routeArrayList.size() > 0) {
            mDialog.setMessage("DownLoading lifeline discount policy...");
            mDialog.show();
            String cmdLifeLineDiscount = baseurl + "?cmd=ld&coopid=" + routeArrayList.get(1).getCoopID() + "&mac=" + mac;
            new downloadLifeLineDiscountPolicyAsync(context).execute(cmdLifeLineDiscount);
        }
    }


    public static void downloadThreshold(Context context) {
        mDialog.setMessage("DownLoading threshold...");
        mDialog.show();
        String cmdThreshold = baseurl + "?cmd=threshold&mac=" + mac;
        new downloadThresholdAsync(context).execute(cmdThreshold);
    }

    public static void downloadAccounts(final Context context, int counter) {
        mDialog.setMessage("DownLoading " + Constant.accountssave + " accounts...");
        mDialog.show();

        if (routeArrayList.size() > 0 && counter < routeArrayList.size()) {
            Route r = routeArrayList.get(counter);
            String cmdAccounts = baseurl + "?cmd=getaccounts&coopid=NORECO2"
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

            new downloadAccountsAsync(context, routeArrayList.size(), counter).execute(cmdAccounts, r.getDueDate(), r.getRouteID());
        } else {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            int downloadRoute = DB.getRoutesDownloadCount(DB);

            if (routeArrayList.size() == downloadRoute) {

                if (Constant.accountssize != Constant.accountssave) {
                    if (mDialog.isShowing()) {
                        mDialog.dismiss();
                    }

                    DB.errorDownLoad(DB, context, "Accounts");
                    setSnackbar("Accounts not completely downloaded...Download again");
                    snackbar.show();
                    mTvView.setText("");
                    mTvView.append("Accounts not completely downloaded...Download again");
                    return;
                }

                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }


                if (Constant.accountssave > 0 && Constant.accountssize == Constant.accountssave) {
                    Log.e(TAG, "accountssave: " + Constant.accountssave);
                    setSnackbar("Accounts Completed...");
                    snackbar.show();
                    updateReaderTable();
                    BtnDownLoad.setEnabled(true);
                    myPreferences.savePrefString(Constant.ACCOUNT_STATUS, Constant.YES);
                }

                if (Constant.accountssave == 0) {
                    setSnackbar("No available accounts...");
                    snackbar.show();
                }

            }

            if(routeArrayList.size() != downloadRoute) {
                if (mDialog.isShowing()) {
                    mDialog.dismiss();
                }

                setSnackbar("Maybe there is no available accounts in this route " + routeArrayList.get(counter - 1).getRouteID());
                snackbar.show();
            }
        }
    }


    public static class downloadRateScheduleAsync extends AsyncTask<String, Void, Void> {

        @SuppressLint("StaticFieldLeak")
        Context context;
        int lengthOfClassification;
        int count = 0;

        public downloadRateScheduleAsync(Context context, int lengthOfClassification) {
            this.context = context;
            this.lengthOfClassification = lengthOfClassification;
        }

        @Override
        protected Void doInBackground(String... strings) {
            Log.e(TAG, "rateschedule: " + strings[0]);
            MainActivity.webRequest.setRequestListenerDownload(strings[0], new WebRequest.RequestListener() {
                @Override
                public void onRequestListener(String response, String param) {
                    count++;
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

                        Constant.rateschedsize = Constant.rateschedsize + jsonArray.length();
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


                            Constant.rateshedsave = Constant.rateshedsave + DB.saveRateSchedule(DB, ratesegment,
                                    ratecomponent, printorder, classification,
                                    rateschedtype, amount, isUnderOver, islifeline,
                                    isscdiscount, dateFrom, extra1, IsExport);

                        }


                        if (Constant.rateschedsize != Constant.rateshedsave) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            DB.errorDownLoad(DB, context, "RateSchedule");
                            setSnackbar("Rate Schedule not completely downloaded...Download again");
                            snackbar.show();
                            return;
                        }

                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }


                        if (count == lengthOfClassification) {
                            if (Constant.rateshedsave > 0 && Constant.rateschedsize == Constant.rateshedsave) {
                                setSnackbar("Rate Schedule Completed.");
                                snackbar.show();
                                myPreferences.savePrefString(Constant.RATE_SCHEDULE_STATUS, Constant.YES);
                                downloadBillingPolicy(context);
                            }
                        }

                    } catch (JSONException e) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        setSnackbar("RateSchedule, " + e.getMessage());
                        snackbar.show();
                    }
                }
            });

            return null;
        }
    }

    public static class downloadBillingPolicyAsync extends AsyncTask<String, Void, Void> {

        @SuppressLint("StaticFieldLeak")
        Context context;
        int lengthOfClassification;
        int count;

        public downloadBillingPolicyAsync(Context context, int lengthOfClassification) {
            this.context = context;
            this.lengthOfClassification = lengthOfClassification;
        }

        @Override
        protected Void doInBackground(final String... strings) {
            MainActivity.webRequest.setRequestListenerDownload(strings[0], new WebRequest.RequestListener() {
                @Override
                public void onRequestListener(String response, String param) {
                    count++;
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

                        Constant.billingpolicysize = Constant.billingpolicysize + jsonArray.length();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String PolicyCode = obj.getString("PolicyCode");
                            String PolicyType = obj.getString("PolicyType");
                            String CustomerClass = obj.getString("CustomerClass");
                            String MinkWh = obj.getString("MinkWh");
                            String MaxkWh = obj.getString("MaxkWh");
                            String PercentAmount = obj.getString("PercentAmount");

                            Constant.billingpolicysave = Constant.billingpolicysave + DB.saveBillingPolicy(DB,
                                    PolicyCode, PolicyType,
                                    CustomerClass, MinkWh, MaxkWh,
                                    PercentAmount);
                        }


                        if (lengthOfClassification == count) {

                            if (Constant.billingpolicysize != Constant.billingpolicysave) {
                                if (mDialog.isShowing()) {
                                    mDialog.dismiss();
                                }

                                DB.errorDownLoad(DB, context, "Policy");
                                setSnackbar("Billing policy not completely downloaded...Download again");
                                snackbar.show();
                                return;
                            }

                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            if (Constant.billingpolicysave > 0 && Constant.billingpolicysize == Constant.billingpolicysave) {
                                Log.e(TAG, "billingpolicysave: " + Constant.billingpolicysave);
                                setSnackbar("Billing Policy Completed.");
                                snackbar.show();
                                myPreferences.savePrefString(Constant.POLICY_STATUS, Constant.YES);
                                downloadRateCode(context);
                            }
                        }

                    } catch (JSONException e) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
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

        @SuppressLint("StaticFieldLeak")
        Context context;

        public downloadRateCodeAsync(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... strings) {

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

                            mTvView.setText("No available Rate Codes");

                            myPreferences.savePrefString(Constant.RATE_CODE_STATUS, Constant.NO);

                            return;
                        }

                        Constant.ratecodesize = Constant.ratecodesize + jsonArray.length();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String CoopID = obj.getString("PowerUtilityID");
                            String CoopName = obj.getString("PowerUtilityName");
                            String RateCode = obj.getString("RateCode");
                            String Details = obj.getString("Details");
                            String IsActive = obj.getString("IsActive");

                            Constant.ratecodesave = Constant.ratecodesave + DB.saveRateCode(DB, CoopID, CoopName, RateCode, Details, IsActive);
                        }


                        if (Constant.ratecodesize != Constant.ratecodesave) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            DB.errorDownLoad(DB, context, "RateCode");
                            setSnackbar("Rate Code not completely downloaded...Download again");
                            snackbar.show();
                            mTvView.setText("Rate Code not completely downloaded...Download again");

                            myPreferences.savePrefString(Constant.RATE_CODE_STATUS, Constant.NO);
                            return;
                        }

                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        if (Constant.ratecodesave > 0 && Constant.ratecodesize == Constant.ratecodesave) {
                            setSnackbar("Rate Code completed...");
                            snackbar.show();
                            myPreferences.savePrefString(Constant.RATE_CODE_STATUS, Constant.YES);
                            downloadCoopDetails(context);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        setSnackbar("Rate Code, " + e.getMessage());
                        snackbar.show();
                    }
                }
            });

            return null;
        }
    }

    public static class downloadCoopDetailsAsync extends AsyncTask<String, Void, Void> {

        @SuppressLint("StaticFieldLeak")
        Context context;

        public downloadCoopDetailsAsync(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... strings) {
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
                            mTvView.setText("");
                            mTvView.append("No Details for this coop...");
                            myPreferences.savePrefString(Constant.COOP_DETAILS_STATUS, Constant.NO);
                            return;
                        }

                        Constant.coopdetailssize = Constant.coopdetailssize + jsonArray.length();
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

                            Constant.coopdetailssave = Constant.coopdetailssave + DB.saveCoopDetails(DB, CoopID, CoopName, CoopType,
                                    Classification, ReadingToDueDate, Acronym,
                                    BillingCode, businessaddress, telno, tinno);


                        }


                        if (Constant.coopdetailssize != Constant.coopdetailssave) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            DB.errorDownLoad(DB, context, "CoopDetails");
                            setSnackbar("Details not completely downloaded...Download again");
                            snackbar.show();
                            mTvView.setText("");
                            mTvView.append("Details not completely downloaded...Download again");
                            myPreferences.savePrefString(Constant.COOP_DETAILS_STATUS, Constant.NO);
                            return;
                        }

                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        if (Constant.coopdetailssave > 0 && Constant.coopdetailssize == Constant.coopdetailssave) {
                            setSnackbar("Coop details completed...");
                            snackbar.show();
                            myPreferences.savePrefString(Constant.COOP_DETAILS_STATUS, Constant.YES);
                            downloadRateComponent(context);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        mTvView.setText("");
                        mTvView.append(e.getMessage());
                        setSnackbar("Coop Details, " + e.getMessage());
                        snackbar.show();
                    }
                }
            });

            return null;
        }
    }

    public static class downloadRateComponentAsync extends AsyncTask<String, Void, Void> {

        @SuppressLint("StaticFieldLeak")
        Context context;

        public downloadRateComponentAsync(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... strings) {

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

                        Constant.ratecomponentsize = Constant.ratecomponentsize + jsonArray.length();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String CoopID = obj.getString("PowerUtilityID");
                            String CoopName = obj.getString("PowerUtilityName");
                            String RateComponent = obj.getString("RateComponent");
                            String Details = obj.getString("Details");
                            String IsActive = obj.getString("IsActive");
                            String Notes1 = obj.getString("Notes1");
                            Constant.ratecomponentsave = Constant.ratecomponentsave + DB.saveRateComponent(DB, CoopID, CoopName, RateComponent, Details, IsActive, Notes1);
                        }


                        if (Constant.ratecomponentsize != Constant.ratecomponentsave) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            DB.errorDownLoad(DB, context, "RateComponent");
                            setSnackbar("Rate Component not completely downloaded...Download again");
                            snackbar.show();
                            mTvView.setText("");
                            mTvView.append("Rate Component not completely downloaded...Download again");
                            myPreferences.savePrefString(Constant.RATE_COMPONENT_STATUS, Constant.NO);
                            return;
                        }

                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        if (Constant.ratecomponentsave > 0 && Constant.ratecomponentsize == Constant.ratecomponentsave) {
                            Log.e(TAG, "ratecomponentsave: " + Constant.ratecomponentsave);
                            setSnackbar("Rate Component completed...");
                            snackbar.show();
                            myPreferences.savePrefString(Constant.RATE_COMPONENT_STATUS, Constant.YES);
                            downloadRateSegment(context);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        mTvView.setText("");
                        mTvView.append(e.getMessage());
                        setSnackbar("Rate Component, " + e.getMessage());
                        snackbar.show();
                    }
                }
            });
            return null;
        }
    }

    public static class downloadRateSegmentAsync extends AsyncTask<String, Void, Void> {
        @SuppressLint("StaticFieldLeak")
        Context context;

        public downloadRateSegmentAsync(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... strings) {

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
                        mTvView.setText("");
                        mTvView.append("Failed Download Rate Segment ," + param);
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

                            mTvView.setText("");
                            mTvView.append("No Rate Segment...");
                            return;
                        }

                        Constant.ratesegmentsize = Constant.ratesegmentsize + jsonArray.length();
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject obj = jsonArray.getJSONObject(i);

                            String CoopID = obj.getString("PowerUtilityID");
                            String RateSegmentCode = obj.getString("RateSegmentCode");
                            String RateSegmentName = obj.getString("RateSegmentName");
                            String Details = obj.getString("Details");
                            String IsActive = obj.getString("IsActive");

                            Constant.ratesegmentsave = Constant.ratesegmentsave + DB.saveRateSegment(DB, CoopID, RateSegmentCode,
                                    RateSegmentName, Details, IsActive);

                        }

                        if (Constant.ratesegmentsize != Constant.ratesegmentsave) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            DB.errorDownLoad(DB, context, "RateSegment");
                            setSnackbar("Rate Segment not completely downloaded...Download again");
                            snackbar.show();
                            mTvView.setText("");
                            mTvView.append("Rate Segment not completely downloaded...Download again");
                            myPreferences.savePrefString(Constant.RATE_SEGMENT_STATUS, Constant.NO);
                            return;
                        }

                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        if (Constant.ratesegmentsave > 0 && Constant.ratesegmentsize == Constant.ratesegmentsave) {
                            setSnackbar("Rate Segment completed...");
                            snackbar.show();
                            myPreferences.savePrefString(Constant.RATE_SEGMENT_STATUS, Constant.YES);
                            downloadLifeLineDiscountPolicy(context);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        setSnackbar("Rate Segment, " + e.getMessage());
                        snackbar.show();
                        mTvView.setText("");
                        mTvView.append(e.getMessage());
                    }
                }
            });
            return null;
        }
    }

    public static class downloadLifeLineDiscountPolicyAsync extends AsyncTask<String, Void, Void> {

        @SuppressLint("StaticFieldLeak")
        Context context;

        public downloadLifeLineDiscountPolicyAsync(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... strings) {

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

                        mTvView.setText("");
                        mTvView.append("Failed Download Lifeline discount policy ," + param);
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
                            mTvView.setText("");
                            mTvView.append("No Lifeline policy...");
                            myPreferences.savePrefString(Constant.LIFELINE_POLICY_STATUS, Constant.NO);
                            return;
                        }

                        Constant.lifelinepolicysize = Constant.lifelinepolicysize + jsonArray.length();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String lifeLineKwh = object.getString(DBInfo.LifelineConsumption);
                            String percent = object.getString(DBInfo.LifelinePercentage);
                            String decimal = object.getString(DBInfo.LifelineInDecimal);
                            Constant.lifelinepolicysave = Constant.lifelinepolicysave + DB.saveLifeLifePolicy(DB, lifeLineKwh, percent, decimal);
                        }

                        if (Constant.lifelinepolicysize != Constant.lifelinepolicysave) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            DB.errorDownLoad(DB, context, "lifeline");
                            setSnackbar("Lifeline Discount Policy not completely downloaded...Download again");
                            snackbar.show();

                            mTvView.setText("");
                            mTvView.append("Lifeline Discount Policy not completely downloaded...Download again");
                            myPreferences.savePrefString(Constant.LIFELINE_POLICY_STATUS, Constant.NO);
                            return;
                        }

                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        if (Constant.lifelinepolicysave > 0 && Constant.lifelinepolicysize == Constant.lifelinepolicysave) {
                            setSnackbar("Lifeline Discount Policy completed...");
                            snackbar.show();
                            myPreferences.savePrefString(Constant.LIFELINE_POLICY_STATUS, Constant.YES);
                            downloadThreshold(context);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        setSnackbar("Lifeline Policy, " + e.getMessage());
                        snackbar.show();
                        mTvView.setText("");
                        mTvView.append(e.getMessage());
                    }
                }
            });

            return null;
        }
    }

    public static class downloadThresholdAsync extends AsyncTask<String, Void, Void> {

        @SuppressLint("StaticFieldLeak")
        Context context;

        public downloadThresholdAsync(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... strings) {

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
                        mTvView.setText("");
                        mTvView.append("Failed Download threshold ," + param);
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
                            mTvView.setText("");
                            mTvView.append("No threshold...");
                            return;
                        }

                        Constant.thresholdsize = Constant.thresholdsize + jsonArray.length();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String settingcode = object.getString(DBInfo.SettingsCode);
                            String percent = object.getString("Details");

                            Constant.thresholdsave = Constant.thresholdsave + DB.saveThreshold(DB, settingcode, percent);
                        }

                        if (Constant.thresholdsize != Constant.thresholdsave) {
                            if (mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            DB.errorDownLoad(DB, context, "threshold");
                            setSnackbar("Threshold not completely downloaded...Download again");
                            snackbar.show();
                            myPreferences.savePrefString(Constant.THRESHOLD_STATUS, Constant.NO);
                            mTvView.setText("");
                            mTvView.append("Threshold not completely downloaded...Download again");
                            return;
                        }

                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        if (Constant.thresholdsave > 0 && Constant.thresholdsize == Constant.thresholdsave) {
                            Log.e(TAG, "thresholdsave: " + Constant.thresholdsave);
                            setSnackbar("Threshold completed...");
                            snackbar.show();
                            myPreferences.savePrefString(Constant.THRESHOLD_STATUS, Constant.YES);
                            downloadAccounts(context, 0);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        setSnackbar("Threshold, " + e.getMessage());
                        snackbar.show();
                        mTvView.setText("");
                        mTvView.append(e.getMessage());
                    }

                }
            });

            return null;
        }
    }

    public static class downloadAccountsAsync extends AsyncTask<String, Void, Void> {
        Context context;
        int sizeOfRoute;
        int counter;


        public downloadAccountsAsync(Context context, int sizeOfRoute, int counter) {
            this.context = context;
            this.sizeOfRoute = sizeOfRoute;
            this.counter = counter;
        }

        @Override
        protected Void doInBackground(final String... strings) {

            MainActivity.webRequest.setRequestListenerDownload(strings[0], new WebRequest.RequestListener() {
                @Override
                public void onRequestListener(String response, String param) {

                    mTvView.append(response);

                    if (response.equalsIgnoreCase("500")) {
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }

                        setSnackbar("Download Accounts failed, " + param);
                        snackbar.show();
                        myPreferences.savePrefString(Constant.ACCOUNT_STATUS, Constant.NO);
                        mTvView.setText("");
                        mTvView.append("Download Accounts failed, " + param);
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
                                Constant.accountssize = Constant.accountssize + jsonArray.length();
                            }
                        }

                        String acc = null;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            acc = obj.getString("AccountID");
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
                                Constant.accountssave = Constant.accountssave + DB.saveAccount(DB, account, details.toString(), routeID, strArr, "");
                                Constant.decoy_save_account = Constant.decoy_save_account + Constant.accountssave;
                            }
                        }


                        if (jsonArray.length() > 0) {
                            if (!acc.equalsIgnoreCase("0")) {
                                downloadAccounts(context, counter);
                            } else {
                                if (Constant.decoy_save_account > 0) {
                                    Log.e(TAG, "here update is download");
                                    Constant.decoy_save_account = 0;
                                    DB.updateRouteIsDownload(DB, 1, strings[2]);
                                    downloadAccounts(context, counter + 1);
                                }else{
                                    downloadAccounts(context, counter + 1);
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        setSnackbar("Accounts, " + e.getMessage());
                        snackbar.show();
                        mTvView.setText("");
                        mTvView.append(e.getMessage());
                    }

                }
            });

            return null;
        }

    }

    public static void updateReaderTable() {

        /** check table is attempt to clear */

        for (Route r : routeArrayList) {
            String cmdUpdateReaderTable = baseurl + "?cmd=update&routeid=" + r.getRouteID() + "&mac=" + mac;
            MainActivity.webRequest.sendRequest(cmdUpdateReaderTable, "updateReaderTable");
        }
    }

}