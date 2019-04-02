package com.payvenue.meterreader;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bixolon.printer.BixolonPrinter;
import com.mapswithme.maps.api.MapsWithMeApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import DataBase.DataBaseHandler;
import Model.Account;
import Model.Bill;
import Model.RateSegmentModel;
import Model.Rates;
import Utility.BixolonPrinterClass;
import Utility.CommonFunc;
import Utility.Constant;
import Utility.MobilePrinter;

import static com.payvenue.meterreader.MainActivity.myMode;
import static com.payvenue.meterreader.MainActivity.whichPrinter;

public class ViewDetails extends AppCompatActivity implements OnClickListener {



    private static final String TAG = "ViewDetails";

    //ConstraintLayout constraintLayout;
    TableLayout constraintLayout;
    TextView viewacctid, viewacctserial, viewacctname, viewacctadd, txtclass, txtmeterbrand,
            mReading, mPrevReading, mConsume, mLocation, tvBillAmount;
    Button btnViewMap, btnReadAccount, btnNotFound;
    SearchView searchView;
    Snackbar snackbar;

    ArrayList<RateSegmentModel> listRateSegment = new ArrayList<>();
    public DataBaseHandler db;
    private boolean IsMotherMeter = false;
    private Account mAccount;
    private Account originalAccount;
    ArrayList<String> arrearsAmountList = new ArrayList<>();
    ArrayList<String> arrearsBillMonthList = new ArrayList<>();
    ArrayList<String> arrearsPenaltyList = new ArrayList<>();
    ArrayList<String> arrearsBillNumberList = new ArrayList<>();
    int myPurpose = 0;
    String _disctrictNo = "";
    Double latitude = 0.00;
    Double longtitude = 0.00;
    Double curlat = 0.00;
    Double curlong = 0.00;
    String currentfilter = "";

    BixolonPrinterClass bp;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_view_details);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        db = new DataBaseHandler(this);
        Log.e(TAG, "Current Page");
        Bundle b = getIntent().getExtras();


        if (b != null) {
            myPurpose = b.getInt("purpose");
            _disctrictNo = b.getString("disctrictNo");
        }


        originalAccount = MainActivity.selectedAccount;
        callAccounts();
        initViews();
        setValues();
        displayButton();
    }

    public void callAccounts() {
        mAccount = MainActivity.selectedAccount;
        if (mAccount.getIsCheckSubMeterType().toLowerCase().equalsIgnoreCase("m")) {
            IsMotherMeter = true;
        }


        //Log.e(TAG,"status: "+ mAccount.getReadStatus());
        bp = MainActivity.bp;
        /**Initalize Rate Segment*/
        listRateSegment = db.getRateSegment(db);
        simplifyArrears();
    }

    //region Functions

    public void setSnackbar(String msg) {

        snackbar = Snackbar.make(findViewById(R.id.relativeLayout_view_details), msg, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void initViews() {

        constraintLayout = findViewById(R.id.constraintLayout);

        mReading = findViewById(R.id.mReading);
        mPrevReading = findViewById(R.id.mPrevReading);
        mConsume = findViewById(R.id.mConsume);
        mLocation = findViewById(R.id.mLocation);
        viewacctid = findViewById(R.id.viewacctid);
        viewacctserial = findViewById(R.id.viewacctserial);
        viewacctname = findViewById(R.id.viewacctname);
        viewacctadd = findViewById(R.id.viewacctadd);
        txtclass = findViewById(R.id.txtclass);
        txtmeterbrand = findViewById(R.id.txtmeterbrand);
        btnViewMap = findViewById(R.id.btnViewmap);
        btnReadAccount = findViewById(R.id.btnReadAccount);
        btnNotFound = findViewById(R.id.btnNotFound);
        tvBillAmount = findViewById(R.id.tv_bill_amount);

        btnReadAccount.setOnClickListener(this);
        btnViewMap.setOnClickListener(this);
        btnNotFound.setOnClickListener(this);

    }

    public void setValues() {
        if (mAccount == null) {
            setSnackbar("No results found...");
            snackbar.show();
        } else {

            String fname = "";
            String mname = "";
            String lname = "";
            String fullname = "";
            try {
                fname = mAccount.getFirstName();
                mname = mAccount.getMiddleName();
                lname = mAccount.getLastName();

                if (fname.equalsIgnoreCase("")) {
                    fname = "";
                }

                if (fname.equalsIgnoreCase(".")) {
                    fname = "";
                }

                if (mname.equalsIgnoreCase("")) {
                    mname = "";
                }

                if (mname.equalsIgnoreCase(".")) {
                    mname = "";
                }

                if (lname.equalsIgnoreCase("")) {
                    lname = "";
                }

                if (lname.equalsIgnoreCase(".")) {
                    lname = "";
                }

                fullname = fname.trim() + " " + mname.trim() + " " + lname.trim();

            } catch (NullPointerException e) {
                Log.e(TAG, "NullPointerException: " + e.getMessage());
            }

            viewacctid.setText(mAccount.getAccountID());
            viewacctserial.setText(mAccount.getMeterSerialNo());
            viewacctname.setText(fullname.trim());
            viewacctadd.setText(mAccount.getAddress());
            txtclass.setText(mAccount.getAccountClassification());
            txtmeterbrand.setText(mAccount.getMeterBrand());
            mReading.setText(mAccount.getReading());
            mPrevReading.setText(mAccount.getInitialReading());
            mConsume.setText(mAccount.getConsume());
            if (myMode == MainActivity.Modes.MODE_2 || myMode == MainActivity.Modes.MODE_3) {
                if (mAccount.getBill() == null) {
                    db.updateStatus(db, mAccount.getAccountID());
                } else {
                    Bill bill = mAccount.getBill();
                    tvBillAmount.setText("" + bill.getTotalAmount());
                }
            }

            mLocation.setText(mAccount.getLatitude() + "," + mAccount.getLongitude());
        }
    }

    public void displayButton() {

        switch (MainActivity.myMode) {

            case MainActivity.Modes.MODE_1://2222
                btnNotFound.setVisibility(View.VISIBLE);
                constraintLayout.setVisibility(View.GONE);
                btnReadAccount.setVisibility(View.VISIBLE);
                btnViewMap.setVisibility(View.VISIBLE);
                break;
            case MainActivity.Modes.MODE_2://3333
            case MainActivity.Modes.MODE_3:
                constraintLayout.setVisibility(View.VISIBLE);
                break;
            case MainActivity.Modes.MODE_4://4444
                btnNotFound.setVisibility(View.GONE);
                btnViewMap.setVisibility(View.GONE);
                btnReadAccount.setVisibility(View.GONE);
                break;
        }

    }

    //endregion


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        switch (requestCode) {

            case 1:
                this.finish();
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_details, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false; //do the default
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (s.length() > 1) {

                        if(MainActivity.myMode.equalsIgnoreCase("NotFound")) {
                            MainActivity.db.getAccountDetails(MainActivity.db, s, mAccount.getRouteNo(), mAccount.getRoutePrimaryKey(), 5,currentfilter);
                        }else{
                            MainActivity.db.getAccountDetails(MainActivity.db, s, mAccount.getRouteNo(), mAccount.getRoutePrimaryKey(), 2,currentfilter);
                        }


                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                callAccounts();
                                setValues();
                            }
                        }, 1000);
                    } else if (s.length() == 0) {
                        //TODO: reset the displayed data
                    }
                    return false;
                }
            });

            int searchCloseButtonId = searchView.getContext().getResources()
                    .getIdentifier("android:id/search_close_btn", null, null);
            ImageView closeButton = (ImageView) searchView.findViewById(searchCloseButtonId);

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.db.getAccountDetails(MainActivity.db, originalAccount.getAccountID(), originalAccount.getRouteNo(), originalAccount.getRoutePrimaryKey(), 0,currentfilter);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                callAccounts();
                                setValues();

                                int searchCloseButtonId = searchView.getContext().getResources()
                                        .getIdentifier("android:id/search_src_text", null, null);
                                EditText et = (EditText) findViewById(searchCloseButtonId);
                                et.setText("");

                                //Clear query
                                searchView.setQuery("", false);
                            } catch (NullPointerException e) {
                                Log.e(TAG, "closeButton: " + e.getMessage());
                            }
                        }
                    }, 1000);
                }
            });
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // menu.findItem(R.id.action_settings).setVisible(false);
        // return super.onPrepareOptionsMenu(menu);

        switch (MainActivity.myMode) {

            case MainActivity.Modes.MODE_1://2222
                menu.findItem(R.id.btnGen).setVisible(false);
                menu.findItem(R.id.btnEdit).setVisible(false);
                menu.findItem(R.id.btnRead).setVisible(false);
                menu.findItem(R.id.btnCancel).setVisible(false);
                break;
            case MainActivity.Modes.MODE_2://read
            case MainActivity.Modes.MODE_3://printed
                menu.findItem(R.id.search).setVisible(false);
                menu.findItem(R.id.filter_serial_number).setVisible(false);
                menu.findItem(R.id.filter_name).setVisible(false);
                menu.findItem(R.id.filter_account_id).setVisible(false);
                menu.findItem(R.id.turn_off_filter).setVisible(false);
                if (mAccount.getUploadStatus().equals("1")) {
                    menu.findItem(R.id.btnEdit).setVisible(false);
                    menu.findItem(R.id.btnCancel).setVisible(false);
                } else {
                    menu.findItem(R.id.btnEdit).setVisible(true);
                    menu.findItem(R.id.btnCancel).setVisible(true);
                }

                if (!mAccount.getReadStatus().equalsIgnoreCase("Cannot Generate")) {
                    menu.findItem(R.id.btnGen).setVisible(true);
                } else {
                    menu.findItem(R.id.btnGen).setVisible(false);
                    setSnackbar("Stop meter...");
                }


                menu.findItem(R.id.btnRead).setVisible(false);

                break;
            case MainActivity.Modes.MODE_4://not found
                //menu.findItem(R.id.search).setVisible(false);
                menu.findItem(R.id.btnGen).setVisible(false);
                menu.findItem(R.id.btnEdit).setVisible(false);
                menu.findItem(R.id.btnRead).setVisible(true);
                menu.findItem(R.id.btnCancel).setVisible(false);

                break;
        }


        return super.onPrepareOptionsMenu(menu);

        // return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.btnGen:
                if (CommonFunc.toDigit(mAccount.getConsume()) < 0) {
                    Toast.makeText(this,
                            "Can't Generate Billing for erroneous reading.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (MainActivity.mIsConnected) {
                        preparePrint();
                    } else {
                        Toast.makeText(getBaseContext(), "Printer is not connected.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case R.id.btnEdit:
                MainActivity.db.getAccountDetails(MainActivity.db, mAccount.getAccountID(), mAccount.getRouteNo(), mAccount.getRoutePrimaryKey(), 0,currentfilter);
                int editCount = db.getEditAttemp(db, mAccount.getAccountID());

//            if(editCount == 3) {
//                Toast.makeText(getBaseContext(), "Exceed the maximum count of editing...", Toast.LENGTH_SHORT).show();
//            }else{
                Intent intent = new Intent(this, Accounts.class);
                intent.putExtra("disctrictNo", _disctrictNo);
                startActivityForResult(intent, 5);
//            }
                break;

            case R.id.btnRead:
                Intent intent1 = new Intent(this, Accounts.class);
                intent1.putExtra("disctrictNo", _disctrictNo);
                startActivityForResult(intent1, 5);
                break;

            case R.id.btnCancel:
                db.updateStatus(db, mAccount.getAccountID());
                finish();
                break;

            case R.id.filter_serial_number:
                currentfilter = "MeterSerialNo";
                updateMenuTitle();
                break;

            case R.id.filter_name:
                currentfilter = "Name";
                updateMenuTitle();
                break;

            case R.id.filter_account_id:
                currentfilter = "AccountID";
                updateMenuTitle();
                break;

            case R.id.turn_off_filter:
                currentfilter = "";
                updateMenuTitle();
                break;

                default:
        }


        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        switch (v.getId()) {

            case R.id.btnNotFound:


                mAccount.setReadStatus(MainActivity.Modes.MODE_4);
                mAccount.setLatitude(String.valueOf(curlat));
                mAccount.setLongitude(String.valueOf(curlong));

                db.updateReadAccount(db, MainActivity.Modes.MODE_4, false);

                Toast.makeText(getBaseContext(),
                        "Accounts successfully tagged as not found.", Toast.LENGTH_LONG)
                        .show();

                this.finish();


                break;

            case R.id.btnViewmap:

                btnViewMap.setEnabled(false);

                if (latitude == 0 && longtitude == 0) {
                    Toast.makeText(getBaseContext(),
                            "Invalid Latitude ang longitude.", Toast.LENGTH_LONG)
                            .show();
                    btnViewMap.setEnabled(true);
                } else {
                    MapsWithMeApi.showPointOnMap(this, latitude, longtitude, MainActivity.reader.getReaderName());
                    btnViewMap.setEnabled(true);
                }


                break;


            case R.id.btnReadAccount:

                Intent intent = new Intent(this, Accounts.class);
                intent.putExtra("disctrictNo", _disctrictNo);
                startActivityForResult(intent, 1);

                break;

        }

    }


    private void updateMenuTitle() {

        if(!currentfilter.equalsIgnoreCase("")) {
            searchView.setQueryHint(currentfilter);
        } else {
            searchView.setQueryHint("Search");
        }
    }

    private void simplifyArrears() {
        arrearsAmountList.clear();
        arrearsBillMonthList.clear();
        arrearsPenaltyList.clear();
        arrearsBillNumberList.clear();
        try {
            JSONArray jsonArray = new JSONArray(mAccount.getArrears());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                arrearsAmountList.add(jsonObject.getString("BillAmount"));
                arrearsBillMonthList.add(jsonObject.getString("BillMonth"));
                arrearsPenaltyList.add(jsonObject.getString("Penalty"));
                arrearsBillNumberList.add(jsonObject.getString("BillNo"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "simplifyArrears:" + e.getMessage());
        }
    }

    public String segmentName(ArrayList<String> duplicateSegmentName, String segmentName) {
        String name = "";
        if (duplicateSegmentName.size() > 0) {
            for (String segName : duplicateSegmentName) {
                if (segmentName.equalsIgnoreCase(segName)) {
                    return segmentName;
                }
            }
        }

        return name;
    }

    public void displayNextAfterPrint() {


        int tag = MainActivity.myMode == "Read" ? 3 : 4;

        db.getAccountDetails(MainActivity.db, mAccount.getAccountID(), mAccount.getRouteNo(), mAccount.getRoutePrimaryKey(), tag,currentfilter);
        this.finish();
        Intent intent = new Intent(this, ViewDetails.class);
        startActivity(intent);
    }

//    public void printLogoBix() {
//
//        if(bp == null) {
//            bp = BixolonPrinterClass.newInstance(this);
//        }
//
//        bp.printBitmap(this);
//
//    }

    public void preparePrint() {
        DecimalFormat df = new DecimalFormat("#.####");
        ArrayList<String> vatListCode = new ArrayList<>();
        ArrayList<String> vatListValue = new ArrayList<>();
        ArrayList<String> rateComponentForExport = new ArrayList<>();
        ArrayList<String> exportRateDueAmount = new ArrayList<>();
        ArrayList<String> duplicateSegmentName = new ArrayList<>();
        String name = mAccount.getLastName();
        if (mAccount.getFirstName().equalsIgnoreCase(".")
                || mAccount.getMiddleName().equalsIgnoreCase(".")) {
            name = mAccount.getLastName() + ", " + mAccount.getFirstName()
                    + mAccount.getMiddleName();
        }

        String[] date = mAccount.getDateRead().split(" ");
        String dateRead = date[0];

        String penalty = mAccount.getPenalty();
        if (penalty.equalsIgnoreCase("-") || penalty.equalsIgnoreCase(".")) {
            penalty = "0";
        }


        Bill mBill = null;
        List<Rates> mRates = null;
        if (!IsMotherMeter) {
            mBill = mAccount.getBill();
            mRates = mBill.getRates();
            /**Check duplicate name*/
            if (listRateSegment.size() > 0) {
                for (RateSegmentModel seg : listRateSegment) {
                    for (Rates r : mRates) {
                        String segmentName = seg.getRateSegmentName();
                        String codeName = r.getCodeName();
                        String sCode = seg.getRateSegmentCode();
                        if (segmentName.equalsIgnoreCase(codeName)) {
                            duplicateSegmentName.add(segmentName);
                        }
                    }
                }
            }
        }


        String a_class = mAccount.getAccountClassification();
        if (a_class.toLowerCase().contains("residential")) {
            a_class = "Res";
        } else if (a_class.toLowerCase().contains("lower")) {
            a_class = "LV";
        } else if (a_class.toLowerCase().contains("higher")) {
            a_class = "HV";
        }

////        mp.printText("           Negros Oriental II Electric Cooperative\n");
////        mp.printText("                  Real St., Dumaguete City\n");
////        mp.printText("                         (NORECO2)\n");
////        mp.printText("                   STATEMENT OF ACCOUNT\n");
////        mp.printText("================================================================\n");


        MobilePrinter mp = null;
        int mPrinter;
        int bixTag = 0;

        if (whichPrinter.equalsIgnoreCase("woo")) {
            mPrinter = 0;
        } else if (whichPrinter.equalsIgnoreCase("bix")) {
            mPrinter = 0; //1; tempory set to zero until issues resolve of disconnetion
            bixTag = 1;

        } else {
            mPrinter = 0;
        }

        if (mPrinter != 1) {
            mp = MainActivity.printer;
            if (mp == null) {
                mp = MobilePrinter.getInstance(this);
            }

            if (bixTag == 1) {
                mp.setDeviceTag(1);
                mp.printText("     Negros Oriental II Electric Cooperative\n");
                mp.printText("             Real St., Dumaguete City\n");
                mp.printText("                   (NORECO2)\n");
                mp.printText("             STATEMENT OF ACCOUNT\n");
                mp.printText("================================================\n");
                mp.printText("\n");
                mp.printextEmphasizedNormalFont("Account No:" + mAccount.getAccountID() + "\n");
                mp.printextEmphasizedNormalFont(name + "\n");
            } else {
                mp.setDeviceTag(0);
                String path = CommonFunc.getPrivateAlbumStorageDir(this, "noreco_logo.bmp").toString();
                mp.printBitmap(path);
                mp.printText("\n");
                mp.printextEmphasized("Account No:" + mAccount.getAccountID() + "\n");
                mp.printextEmphasized(name + "\n");
            }
        } else {
            bp.printBitmap();
        }

//        CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
//        CommonFunc.printingNormal("Account No:" + mAccount.getAccountID() + "\n", "", 1, 0, 0, mPrinter);
//        CommonFunc.printingNormal(name + "\n", "", 1, 0, 0, mPrinter);
//        CommonFunc.printingNormal(mAccount.getAddress() + "\n", "", 0, 0, 1, mPrinter);
//        CommonFunc.printingNormal("Meter No:" + mAccount.getMeterSerialNo() + "\n", "", 0, 0, 0, mPrinter);
//        CommonFunc.printingNormal("Period Covered: " + CommonFunc.changeDateFormat(mAccount.getLastReadingDate()) + " to " + CommonFunc.changeDateFormat(dateRead) + "\n", "", 0, 0, 0, mPrinter);
//        CommonFunc.printingNormal("Due Date: " + mAccount.getDueDate() + "\n", "", 0, 0, 0, mPrinter);
//        CommonFunc.printingNormal("Meter Reader:" + MainActivity.reader.getReaderName() + "\n", "", 0, 0, 0, mPrinter);
//        CommonFunc.printingNormal("Multiplier:" + mAccount.getMultiplier(), "Consumer Type:" + a_class + "\n", 0, 0, 0, mPrinter);
//        CommonFunc.printingNormal("Consumption:" + mAccount.getActualConsumption(), "BillMonth:" + mAccount.getBillMonth() + "\n", 0, 0, 0, mPrinter);


        mp.printextEmphasizedNormalFont(mAccount.getAddress() + "\n");
        mp.printText("Meter No:" + mAccount.getMeterSerialNo() + "\n");
        mp.printText("Period Covered: " + CommonFunc.changeDateFormat(mAccount.getLastReadingDate()) + " to " + CommonFunc.changeDateFormat(dateRead) + "\n");
        mp.printText("Due Date: " + mAccount.getDueDate() + "\n");//
        mp.printText("Meter Reader:" + MainActivity.reader.getReaderName() + "\n");
        mp.printText("Multiplier:" + mAccount.getMultiplier(), "Consumer Type:" + a_class + "\n");
        mp.printText("Consumption:" + mAccount.getActualConsumption(), "BillMonth:" + mAccount.getBillMonth() + "\n");
        if (a_class.equalsIgnoreCase("HV")) {
            //CommonFunc.printingNormal("Coreloss:" + mAccount.getCoreloss(), "DemandKW:" + mAccount.getDemandKW() + "\n", 0, 0, 0, mPrinter);
            mp.printText("Coreloss:" + mAccount.getCoreloss(), "DemandKW:" + mAccount.getDemandKW() + "\n");
        } else {
            //CommonFunc.printingNormal("Coreloss:" + mAccount.getCoreloss() + "\n", "", 0, 0, 0, mPrinter);
            mp.printText("Coreloss:" + mAccount.getCoreloss() + "\n");
        }

        if (mAccount.getIsNetMetering().equalsIgnoreCase("1")) {
            //CommonFunc.printingNormal("Net-Metering Customer - IMPORT BILL\n", "", 0, 0, 0, mPrinter);
            mp.printText("Net-Metering Customer - IMPORT BILL\n");
        }

        if (mPrinter == 1) {
            bp.printText("------------------------------------------------" + "\n", BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
            bp.printText("Date          Prev            Pres           KWH" + "\n", BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
        } else {

            if (bixTag == 1) {
                mp.printText("------------------------------------------------" + "\n");
                mp.printText("Date           Prev           Pres           KWH" + "\n");
            } else {
                mp.printText("--------------------------------------------------------------" + "\n");
                mp.printText("Date                Prev                 Pres              KWH" + "\n");
            }
        }


        int padding1 = 20 - dateRead.length() - mAccount.getInitialReading().length();
        String spacing = " ";
        for (int p = 0; p < padding1; p++) {
            spacing = spacing.concat(" ");
        }
        String strRight = dateRead + spacing + mAccount.getInitialReading();

        int paddingLeft = 20 - mAccount.getReading().length() - mAccount.getConsume().length();
        String _spacing = " ";
        for (int p = 0; p < paddingLeft; p++) {
            _spacing = _spacing.concat(" ");
        }
        String strLeft = mAccount.getReading() + _spacing + mAccount.getConsume();
        //CommonFunc.printingNormal(strRight, strLeft + "\n", 0, 0, 0, mPrinter);
        mp.printText(strRight, strLeft + "\n");
        if (mPrinter == 1) {
            bp.printText("------------------------------------------------" + "\n", BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
        } else {
            if (bixTag == 1) {
                mp.printText("------------------------------------------------" + "\n");
            } else {
                mp.printText("--------------------------------------------------------------" + "\n");
            }

        }

        if (!IsMotherMeter) {
            if (listRateSegment.size() > 0) {
                for (RateSegmentModel s : listRateSegment) {
                    String segmentName = s.getRateSegmentName();
                    String rateSegmentCode = s.getRateSegmentCode();

                    /** Avoid printing same segment name*/
                    if (duplicateSegmentName.size() > 0) {
                        for (String seg : duplicateSegmentName) {
                            if (!seg.equalsIgnoreCase(segmentName)) {
                                mp.printText(segmentName + "\n");
                                //CommonFunc.printingNormal(segmentName + "\n", "", 0, 0, 0, mPrinter);
                            }
                        }
                    } else {
                        //CommonFunc.printingNormal(segmentName + "\n", "", 0, 0, 0, mPrinter);
                        mp.printText(segmentName + "\n");
                    }


                    for (Rates r : mRates) {
                        if (r.getRateSegment().equals(rateSegmentCode)) {
                            String codeName = r.getCodeName();
                            String rateComponent = r.getCode();
                            @SuppressLint("DefaultLocale")
                            String rateAmount = String.format("%.4f", Float.valueOf(r.getRateAmount()));
                            //String rateAmount = String.valueOf(r.getRateAmount());
                            String amount;
                            amount = df.format(Double.parseDouble(r.getAmount()));

                            int padding = 20 - rateAmount.length() - amount.length();
                            String paddingChar = " ";
                            for (int p = 0; p < padding; p++) {
                                paddingChar = paddingChar.concat(" ");
                            }
                            String rightText = rateAmount + paddingChar + amount;

                            if (codeName.contains("VAT on")) {
//                                if (whichPrinter.equalsIgnoreCase("bix")) {
//                                    if (codeName.length() > 18) {
//                                        vatListCode.add(rateComponent);
//                                    } else {
//                                        vatListCode.add(codeName);
//                                    }
//                                } else {
                                vatListCode.add(codeName);
                                //}

                                vatListValue.add(rightText);
                            }

                            /** print here */
                            if (codeName.equalsIgnoreCase("Subsidy on Lifeline")) {

                                if (!mAccount.getTotalLifeLineDiscount().equalsIgnoreCase("0.0")) {
                                    //CommonFunc.printingNormal("  Lifeline Discount(R)", "-" + MainActivity.dec2.format(Double.valueOf(mAccount.getTotalLifeLineDiscount())) + "\n", 0, 0, 0, mPrinter);
                                    mp.printText("  Lifeline Discount(R)", "-" + MainActivity.dec2.format(Double.valueOf(mAccount.getTotalLifeLineDiscount())) + "\n");
                                } else {
                                    if (mPrinter == 1) {
                                        if (codeName.length() > 18) {
                                            CommonFunc.printingNormal("  " + rateComponent, rightText + "\n", 0, 0, 0, mPrinter);
                                            //mp.printText("  " + rateComponent, rightText + "\n");
                                        } else {
                                            CommonFunc.printingNormal("  " + codeName, rightText + "\n", 0, 0, 0, mPrinter);
                                            //mp.printText("  " + codeName, rightText + "\n");
                                        }
                                    } else {
                                        //CommonFunc.printingNormal("  " + codeName, rightText + "\n", 0, 0, 0, mPrinter);
                                        mp.printText("  " + codeName, rightText + "\n");
                                    }
                                }
                            }

                            if (codeName.equalsIgnoreCase("Senior Citizens Subsidy")) {

                                if (!mAccount.getTotalSCDiscount().equalsIgnoreCase("0.0")) {
                                    //CommonFunc.printingNormal("  Senior Citizens Discount(R)", "-" + MainActivity.dec2.format(Double.valueOf(mAccount.getTotalSCDiscount())) + "\n", 0, 0, 0, mPrinter);
                                    if (bixTag == 1) {
                                        mp.printText("  SC Discount(R)", "-" + MainActivity.dec2.format(Double.valueOf(mAccount.getTotalSCDiscount())) + "\n");
                                    } else {
                                        mp.printText("  Senior Citizens Discount(R)", "-" + MainActivity.dec2.format(Double.valueOf(mAccount.getTotalSCDiscount())) + "\n");
                                    }

                                } else {
                                    if (mPrinter == 1) {
                                        if (codeName.length() > 18) {
                                            //CommonFunc.printingNormal("  " + rateComponent, rightText + "\n", 0, 0, 0, mPrinter);
                                            mp.printText("  " + rateComponent, rightText + "\n");
                                        } else {
                                            //CommonFunc.printingNormal("  " + codeName, rightText + "\n", 0, 0, 0, mPrinter);
                                            mp.printText("  " + codeName, rightText + "\n");
                                        }
                                    } else {
                                        //CommonFunc.printingNormal("  " + codeName + rateComponent, rightText + "\n", 0, 0, 0, mPrinter);
                                        if (bixTag == 1) {
                                            mp.printText("  SC Subsidy", rightText + "\n");
                                        } else {
                                            mp.printText("  " + codeName, rightText + "\n");
                                        }
                                    }
                                }
                            }


                            if (!codeName.equalsIgnoreCase("Subsidy on Lifeline") && !codeName.equalsIgnoreCase("Senior Citizens Subsidy") && !codeName.contains("VAT on")) {
                                String sn = segmentName(duplicateSegmentName, codeName);
                                if (!sn.equalsIgnoreCase("")) {
                                    //CommonFunc.printingNormal(codeName, rightText + "\n", 0, 0, 0, mPrinter);
                                    mp.printText(codeName, rightText + "\n");
                                } else {
                                    if (mPrinter == 1) {
                                        if (codeName.length() > 18) {
                                            //CommonFunc.printingNormal("  " + rateComponent, rightText + "\n", 0, 0, 0, mPrinter);
                                            mp.printText("  " + rateComponent, rightText + "\n");
                                        } else {
                                            //CommonFunc.printingNormal("  " + codeName, rightText + "\n", 0, 0, 0, mPrinter);
                                            mp.printText("  " + codeName, rightText + "\n");
                                        }
                                    } else {
                                        //CommonFunc.printingNormal("  " + codeName, rightText + "\n", 0, 0, 0, mPrinter);
                                        if (bixTag == 1) {
                                            if (codeName.length() > 10) {
                                                codeName = codeName.substring(0, 13) + "...";
                                            }
                                        }

                                        mp.printText("  " + codeName, rightText + "\n");
                                    }
                                }
                            }

                            /**NET METERING*/
                            if (mAccount.getIsNetMetering().equalsIgnoreCase("1")) {
                                String amountExport = df.format(r.getAmountDueExport());
                                if (r.getIsExport().equalsIgnoreCase("1") || r.getIsExport().equalsIgnoreCase("Yes")) {
                                    int padding2 = 20 - rateAmount.length() - amountExport.length();
                                    String paddingChar2 = " ";
                                    for (int p = 0; p < padding2; p++) {
                                        paddingChar2 = paddingChar2.concat(" ");
                                    }
                                    String rightText1 = rateAmount + paddingChar2 + amountExport;
                                    if (mPrinter == 1) {
                                        if (codeName.length() > 18) {
                                            rateComponentForExport.add(rateComponent);
                                        } else {
                                            rateComponentForExport.add(codeName);
                                        }
                                    } else {
                                        if (bixTag == 1) {
                                            if (codeName.length() > 10) {
                                                codeName = codeName.substring(0, 13) + "...";
                                            }
                                        }

                                        rateComponentForExport.add(codeName);
                                    }

                                    exportRateDueAmount.add(rightText1);
                                }
                            }
                        }
                    }
                } // end loop
            }

            //mp.printText("VAT Charges" + "\n");
            CommonFunc.printingNormal("VAT Charges" + "\n", "", 0, 0, 0, mPrinter);
            for (int i = 0; i < vatListCode.size(); i++) {
                //CommonFunc.printingNormal("  " + vatListCode.get(i), vatListValue.get(i) + "\n", 0, 0, 0, mPrinter);
                String vatName = vatListCode.get(i);
                if (bixTag == 1) {
                    if (vatName.length() > 10) {
                        vatName = vatName.substring(0, 13) + "...";
                    }
                }

                mp.printText("   " + vatName, vatListValue.get(i) + "\n");
            }

            //CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
            //CommonFunc.printingNormal("Total Current Due", MainActivity.dec2.format(mBill.getTotalAmount()) + "\n", 1, 0, 0, mPrinter);
            //CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
            mp.printText("", "" + "\n");
            mp.printTextEmphasized("Total Current Due", MainActivity.dec2.format(mBill.getTotalAmount()));
            mp.printText("", "" + "\n");

            if (arrearsBillMonthList.size() > 0) {

                if (mPrinter == 1) {
                    bp.printText("BillingDate   BillNumber    Amount     Surcharge" + "\n", BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                    bp.printText("------------------------------------------------" + "\n", BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                } else {
                    if (bixTag == 1) {
                        mp.printText("BillingDate   BillNumber    Amount     Surcharge" + "\n");
                        mp.printText("------------------------------------------------" + "\n");
                    } else {
                        mp.printText("BillingDate        BillNumber         Amount         Surcharge" + "\n");
                        mp.printText("--------------------------------------------------------------" + "\n");
                    }
                }
                for (int i = 0; i < arrearsBillMonthList.size(); i++) {
                    String billdate = arrearsBillMonthList.get(i);
                    String billnumber = arrearsBillNumberList.get(i);
                    String amount = arrearsAmountList.get(i);
                    String _penalty = arrearsPenaltyList.get(i);
                    String[] str = billdate.split(" ");

                    int p1 = 20 - str[0].length() - billnumber.length();
                    String paddingChar1 = " ";
                    for (int p = 0; p < p1; p++) {
                        paddingChar1 = paddingChar1.concat(" ");
                    }
                    String str1 = str[0] + paddingChar1 + billnumber;

                    int p2 = 20 - amount.length() - _penalty.length();
                    String _paddingLeft1 = " ";
                    for (int p = 0; p < p2; p++) {
                        _paddingLeft1 = _paddingLeft1.concat(" ");
                    }
                    String str2 = amount + _paddingLeft1 + _penalty;
                    //CommonFunc.printingNormal(str1, str2 + "\n", 0, 0, 0, mPrinter);
                    mp.printText(str1, str2 + "\n");

                }
                if (mPrinter == 1) {
                    bp.printText("------------------------------------------------" + "\n", BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                } else {
                    if (bixTag == 1) {
                        mp.printText("------------------------------------------------" + "\n");
                    } else {
                        mp.printText("--------------------------------------------------------------" + "\n");
                    }
                }
            }

//            CommonFunc.printingNormal("Add:SURCHARGE:", MainActivity.dec2.format(Double.valueOf(penalty)) + "\n", 0, 0, 0, mPrinter);
//            CommonFunc.printingNormal("Arrears:", MainActivity.dec2.format(Double.valueOf(mAccount.getPrevBilling())) + "\n", 0, 0, 0, mPrinter);
//            CommonFunc.printingNormal("Less:Advance Payment:", MainActivity.dec2.format(Double.valueOf(mAccount.getAdvancePayment())) + "\n", 0, 0, 0, mPrinter);
//            CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
//            CommonFunc.printingNormal("TOTAL AMOUNT PAYABLE", MainActivity.dec2.format(mBill.getTotalBilledAmount()) + "\n", 0, 1, 0, mPrinter);
//            CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
//            CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
//            CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
//            CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);

            mp.printText("Add:SURCHARGE:", MainActivity.dec2.format(Double.valueOf(penalty)) + "\n");
            mp.printText("Arrears:", MainActivity.dec2.format(Double.valueOf(mAccount.getPrevBilling())) + "\n");
            mp.printText("Less:Advance Payment:", MainActivity.dec2.format(Double.valueOf(mAccount.getAdvancePayment())) + "\n");
            mp.printTextEmphasized1("TOTAL AMOUNT PAYABLE", MainActivity.dec2.format(mBill.getTotalBilledAmount()));


        }


        if (mAccount.getIsNetMetering().equalsIgnoreCase("1")) {
            mp.printText("", "" + "\n");
            mp.printText("", "" + "\n");
            mp.printText("", "" + "\n");
            mp.printText("", "" + "\n");

            if (mPrinter == 1) {
                CommonFunc.printingNormal("EXPORT BILL" + "\n", "", 1, 0, 0, mPrinter);
                bp.printText("------------------------------------------------" + "\n", BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                bp.printText("Date         Prev           Pres             KWH" + "\n", BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
            } else {
                if (bixTag == 1) {
                    mp.printTextBoldRight("", "EXPORT BILL" + "\n");
                    mp.printText("------------------------------------------------" + "\n");
                    mp.printText("Date         Prev           Pres             KWH" + "\n");
                } else {
                    mp.printTextBoldRight("", "EXPORT BILL" + "\n");
                    mp.printText("--------------------------------------------------------------" + "\n");
                    mp.printText("Date                Prev                 Pres              KWH" + "\n");
                }
//                mp.printTextBoldRight("", "EXPORT BILL" + "\n");
//                mp.printText("--------------------------------------------------------------" + "\n");
//                mp.printText("Date                Prev                 Pres              KWH" + "\n");
            }

            String exportConsume = MainActivity.dec2.format(Double.valueOf(mAccount.getExportConsume()));

            int padding3 = 20 - dateRead.length() - mAccount.getExportPreviousReading().length();
            String spacing3 = " ";
            for (int p = 0; p < padding3; p++) {
                spacing3 = spacing3.concat(" ");
            }
            String strRight1 = dateRead + spacing3 + mAccount.getExportPreviousReading();

            int paddingLeft1 = 20 - mAccount.getExportReading().length() - exportConsume.length();
            String _spacing2 = " ";
            for (int p = 0; p < paddingLeft1; p++) {
                _spacing2 = _spacing2.concat(" ");
            }
            String strLeft1 = mAccount.getExportReading() + _spacing2 + exportConsume;
            //CommonFunc.printingNormal("" + strRight1, strLeft1 + "\n", 0, 0, 0, mPrinter);
            mp.printText(strRight1, strLeft1 + "\n");
            if (mPrinter == 1) {
                bp.printText("------------------------------------------------" + "\n", BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
            } else {
                if (bixTag == 1) {
                    mp.printText("------------------------------------------------" + "\n");
                } else {
                    mp.printText("--------------------------------------------------------------" + "\n");
                }

            }

            if (!IsMotherMeter) {
                //CommonFunc.printingNormal("Customer Charge to DU\n", "", 0, 0, 0, mPrinter);
                mp.printText("Customer Charge to DU\n");
                for (int i = 0; i < rateComponentForExport.size(); i++) {
                    //CommonFunc.printingNormal("  " + rateComponentForExport.get(i), exportRateDueAmount.get(i) + "\n", 0, 0, 0, mPrinter);
                    mp.printText("  " + rateComponentForExport.get(i), exportRateDueAmount.get(i) + "\n");
                }
//                CommonFunc.printingNormal("Amount Export Due", MainActivity.dec2.format(mBill.getTotalAmountDueExport()) + "\n", 1, 0, 0, mPrinter);
//                CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
//                CommonFunc.printingNormal("NET BILL AMOUNT", MainActivity.dec2.format(mBill.getNetBillAmountExport()) + "\n", 1, 0, 0, mPrinter);
                mp.printTextEmphasized("Amount Export Due", MainActivity.dec2.format(mBill.getTotalAmountDueExport()));
                mp.printText("\n");
                mp.printText("\n");
                mp.printTextEmphasized("NET BILL AMOUNT", MainActivity.dec2.format(mBill.getNetBillAmountExport()) + "\n");
            }
        }


        if (arrearsBillMonthList.size() > 0) {

            if (mPrinter == 1) {
                bp.printNextLine(2);
                bp.printText(Constant.DISCONNECTIONNOTICE_BIX + "\n", BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                bp.printNextLine(1);
            } else {
                mp.printText("\n");
                mp.printText("\n");
                mp.printText("\n");
                mp.printText("\n");
                if (bixTag == 1) {
                    mp.printText(Constant.DISCONNECTIONNOTICE_BIX + "\n");
                } else {
                    mp.printText(Constant.DISCONNECTIONNOTICE + "\n");
                }

                mp.printText("\n");
            }

            if (mPrinter == 1) {
                bp.printText(Constant.OFFICIALRECIEPT_BIX + "\n", BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                bp.printNextLine(1);
            } else {
                if (bixTag == 1) {
                    mp.printText(Constant.OFFICIALRECIEPT_BIX + "\n");
                } else {
                    mp.printText(Constant.OFFICIALRECIEPT + "\n");
                }

                mp.printText("\n");
            }

            if (mPrinter == 1) {
                bp.printText("     " + Constant.WARNING + "\n", BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
            } else {
                if (bixTag == 1) {
                    mp.printText("     " + Constant.WARNING + "\n");
                } else {
                    mp.printText("                " + Constant.WARNING + "                 " + "\n");
                }

            }
        } else {
            //CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
            //CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
            //CommonFunc.printingNormal(Constant.FOOTERMESSAGE + "\n", "", 0, 0, 0, mPrinter);
            mp.printText("\n");
            mp.printText("\n");
            mp.printText("\n");
            mp.printText("\n");
            mp.printText(Constant.FOOTERMESSAGE + "\n");
            mp.printText("\n");
            //CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
            if (mPrinter == 1) {
                bp.printText(Constant.OFFICIALRECIEPT_BIX + "\n", BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
            } else {
                if (bixTag == 1) {
                    mp.printText(Constant.OFFICIALRECIEPT_BIX + "\n");
                } else {
                    mp.printText(Constant.OFFICIALRECIEPT + "\n");
                }
            }
        }

        if (!mAccount.getPrintCount().equalsIgnoreCase("0")) {
            mp.printText("REPRINTED" + "\n");
            //CommonFunc.printingNormal("REPRINTED" + "\n", "", 0, 0, 0, mPrinter);
        }

        mp.printText("\n");
        mp.printText("\n");
        mp.printText("\n");
        mp.printText("\n");
//        CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
//        CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
//        CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);
//        CommonFunc.printingNormal("\n", "", 0, 0, 0, mPrinter);

        String stat = "Printed";
        if (mAccount.getReadStatus().equalsIgnoreCase("PrintedSM")) {
            stat = "PrintedSM";
        }

        if (mAccount.getReadStatus().equalsIgnoreCase("ReadSM")) {
            stat = "PrintedSM";
        }


        db.updateAccountToPrinted(db, mAccount.getAccountID(), stat);
        displayNextAfterPrint();
    }
}
