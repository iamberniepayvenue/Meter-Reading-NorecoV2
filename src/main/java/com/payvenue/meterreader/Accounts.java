package com.payvenue.meterreader;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import DataBase.DataBaseHandler;
import Model.Bill;
import Model.LifeLineSubsidyModel;
import Model.RateSchedule;
import Model.RateSegmentModel;
import Model.Rates;
import Utility.CommonFunc;
import Utility.MobilePrinter;


/**
 * Created by andrewlaurienrsocia on 16/04/2018.
 */


public class Accounts extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Accounts";
    ConstraintLayout constraintButton;
    TextView mSerial, mAccountName, mAccountID, mAccountClass, mAccountAddress;
    CheckBox mOval, mCycle, mStop, mChange;
    EditText mReading, mDemand, mRemarks, mReadingCycle;
    Button btnGenerate, btnTakePic;
    Context mcontext;
    boolean isOvalCheck = false, isRecycleCheck = false, isStopCheck = false, isChangeCheck = false;
    double maxreadingvalue = 0;
    String strReading = "", strRemarks = "", strDemands = "";
    float rateMultiplier;
    float scPercentage = (float) 0.05;
    float componentAmount, componentvat, componentltax, componentftax, totalcomponentftax, totalcomponentltax,
            scDiscountRate, lifelineDiscountRate, scSubsidyAmount, scDiscountedAmount,
            lifelineDiscountAmount, DistributionVat, totaldistributionvat, totalLifelineDiscount,
            totalVat, totalComponent, billedAmount;
    float overUnderRecovery = 0;

    boolean canAvailLifelineDiscount = false, canAvailSCDiscount = false;
    boolean scInvalidDate = false;
    boolean isSCOverPolicy = false;
    ArrayList<Rates> myRates = new ArrayList<>();
    ArrayList<RateSegmentModel> listRateSegment = new ArrayList<>();
    //public  ArrayList<Components> componentsList = new ArrayList<>();
    Bill mBill;
    String billMonth;
    public DataBaseHandler db;
    InputMethodManager imm;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_details);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        db = new DataBaseHandler(this);

        try {
            imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        } catch (Exception e) {
            // TODO: handle exception
        }

        mcontext = this;

        /**Initalize Rate Segment*/
        listRateSegment = db.getRateSegment(db);


        if (!MainActivity.gps.canGetLocation()) {
            MainActivity.gps.showSettingAlert();
        }

        initViews();

        setValues();

        Log.e(TAG,"Current Page");
    }


    //region Functions

    public void initViews() {

        constraintButton = findViewById(R.id.constraintButton);
        mSerial = (TextView) findViewById(R.id.mSerial);
        mAccountName = (TextView) findViewById(R.id.mAccountName);
        mAccountID = (TextView) findViewById(R.id.mAccountID);
        mAccountClass = (TextView) findViewById(R.id.mAccountClass);
        mAccountAddress = (TextView) findViewById(R.id.mAccountAddress);
        mOval = (CheckBox) findViewById(R.id.mOval);
        mCycle = (CheckBox) findViewById(R.id.mCycle);
        mStop = (CheckBox) findViewById(R.id.mStop);
        mChange = (CheckBox) findViewById(R.id.mChange);
        mReading = (EditText) findViewById(R.id.mReading);
        mDemand = (EditText) findViewById(R.id.mDemand);
        mRemarks = (EditText) findViewById(R.id.mRemarks);
        mReadingCycle = findViewById(R.id.mReadingCycle);
        btnGenerate = (Button) findViewById(R.id.btnGenerate);
        btnTakePic = (Button) findViewById(R.id.btnTakePic);

        btnGenerate.setOnClickListener(this);
        btnTakePic.setOnClickListener(this);

        //Text Watcher
        mReadingCycle.addTextChangedListener(new TextChangeListener(mcontext, mReadingCycle));
        mReading.addTextChangedListener(new TextChangeListener(mcontext, mReading));
        mRemarks.addTextChangedListener(new TextChangeListener(mcontext, mRemarks));

        //Action Listener
        mReading.setOnEditorActionListener(new ActionListener());
        mDemand.setOnEditorActionListener(new ActionListener());

        //Check if Higher Voltage. If True show Demand Editext.
        if (MainActivity.selectedAccount.getAccountClassification().equalsIgnoreCase("Higher Voltage"))
            mDemand.setVisibility(View.VISIBLE);

    }

    public void setValues() {

        mSerial.setText(MainActivity.selectedAccount.getMeterSerialNo());
        mAccountName.setText(MainActivity.selectedAccount.getLastName());
        mAccountID.setText(MainActivity.selectedAccount.getAccountID());
        mAccountClass.setText(MainActivity.selectedAccount.getAccountClassification());
        mAccountAddress.setText(MainActivity.selectedAccount.getAddress());

    }

    public void displayButtons() {
        constraintButton.setVisibility(View.VISIBLE);
    }

    public void showToast(String message) {
        Toast.makeText(mcontext, message, Toast.LENGTH_SHORT).show();
    }

    public void calculateBill() {


        //Toast.makeText(mcontext, MainActivity.selectedAccount.getReading() + "===" + MainActivity.selectedAccount.getInitialReading() + "=====" + MainActivity.selectedAccount.getConsume(), Toast.LENGTH_SHORT).show();

        /**
         *
         * Algorithm for Generation of Bill
         * 1. Check if 0 and there is a Remark. Save to DB dont generate Bill.
         * 2. Check is LastReadingDate a valid Date.
         * 3. Check if Initial Reading is a valid Digit.
         * 4. Check if account classification is Higher Voltage. Save Reading but dont Generate Bill.
         * 5. Get RateSchedule
         *      - Params : RateSched and Account Classification
         *      - This will return List RateComponents.
         *
         *
         *
         * . Check If Senior Citizen for Discount
         *      - If senior citizen check if consumption can avail to policy discount.
         * . Check if AccountClassification is Residential for  Lifeline Discount
         *      - If can avail get the Lifeline Discount Rate
         *
         * Note: If Account can avail the Lifeline Discount we dont calculate for the SOL(Subsidy On Lifeline)
         *       If Account can avail the SC Discount we dont dont calculate for the SCS(Senior Citizen Subsidy)
         * .
         *
         */

        MainActivity.selectedAccount.setRemarks(strRemarks);
        Log.e(TAG,"remar : " +MainActivity.selectedAccount.getRemarks() );

        if (MainActivity.selectedAccount.getConsume().equals("0") && !MainActivity.selectedAccount.getRemarks().isEmpty()) {
            MainActivity.db.updateReadAccount(MainActivity.db, "Cannot Generate",isStopCheck);
            return;
        }

        if(isChangeCheck) {
            MainActivity.db.updateReadAccount(MainActivity.db, "Cannot Generate",isStopCheck);
            return;
        }


        //getLastReadingDate()
        Log.e(TAG,"Date Read : " + MainActivity.selectedAccount.getDateRead());
//        if (MainActivity.selectedAccount.getDateRead() != null) {
////            if (!CommonFunc.isLegalDate(MainActivity.selectedAccount.getDateRead())) {
////
////                showToast("Last Reading Date is not valid Date.");
////                return;
////            }
////        } else {
////            showToast("Last Reading Date is blank.");
////            return;
////        }

        if (Float.parseFloat(MainActivity.selectedAccount.getInitialReading()) < 0) {
            showToast("Previous Reading is invalid.");
            return;
        }

        if (MainActivity.selectedAccount.getAccountClassification().equalsIgnoreCase("Higher Voltage")) {
            MainActivity.db.updateReadAccount(MainActivity.db, "Cannot Generate",isStopCheck);
            showToast("Cant generate billing for a Higher Voltage classification.");
            return;
        }

        /**Check Senior Status Discount*/
        if (MainActivity.selectedAccount.getSeniorCitizenStatus().equals("1") && MainActivity.selectedAccount.getAccountClassification().equalsIgnoreCase("Residential") ) {
            Log.e(TAG,"SC expiry date: " + MainActivity.selectedAccount.getSCExpiryDate());
                if(CommonFunc.isValidDate(MainActivity.selectedAccount.getSCExpiryDate())) {
                    Log.e(TAG,"SC DATE is Valid");
                    String consumption = MainActivity.selectedAccount.getConsume();
                    if(Float.valueOf(consumption) <= 100) {
                        canAvailSCDiscount = true;
                    }else{
                        isSCOverPolicy = true;
                    }
                }else{
                    Log.e(TAG,"SC DATE expired");
                    scInvalidDate = true;
                }
        }

        /**Check Lifeliner*/
        if (MainActivity.selectedAccount.getAccountClassification().equalsIgnoreCase("Residential")) {
            String consumption = MainActivity.selectedAccount.getConsume();
            lifelineDiscountRate = getLifeLinerPercentage(Float.valueOf(consumption));
            if(lifelineDiscountRate > 0) {
                canAvailLifelineDiscount = true;
            }

            Log.e(TAG,"Lifeline Discount :"+lifelineDiscountRate);
        }


        Cursor cursor = MainActivity.db.getRateSched(MainActivity.db,
                MainActivity.selectedAccount.getRateSched(),
                MainActivity.selectedAccount.getAccountClassification());

        if (cursor.getCount() <= 0) {
            showToast("No Rateschedule Created for this Classification");
            return;
        }


        RateSchedule rateSchedule = null;

        billMonth = CommonFunc.getBillMonth(); //+ "-" + MainActivity.selectedAccount.getRouteNo() + "-" + MainActivity.selectedAccount.getAccountID();
        MainActivity.selectedAccount.setBillMonth(billMonth);
        MainActivity.selectedAccount.setDateRead(CommonFunc.getDateOnly());
        String strMultiplier = MainActivity.selectedAccount.getMultiplier();
        float flMultiplier = Float.parseFloat(strMultiplier);
        String strConsume = MainActivity.selectedAccount.getConsume();
        float flConsume = Float.parseFloat(strConsume);
        rateMultiplier = flConsume * flMultiplier;

        /**STOP METER*/
        if(isStopCheck) {
            String strAveraging = MainActivity.selectedAccount.getAveraging();
            //Log.e(TAG,"averaging "+ strAveraging);
            try {
                float flConsumption = 0;
                JSONObject json = new JSONObject(strAveraging);
                JSONArray jsonArrayNew = json.getJSONArray("New");
                JSONArray jsonArrayOld = json.getJSONArray("Old");
                JSONArray finalArray;
                JSONObject finalJson;

                finalArray = jsonArrayNew.length() == 3 ? jsonArrayNew : jsonArrayOld;
                //Log.e(TAG,"isStopCheck "+ finalArray);
                for(int i = 0; i < finalArray.length();i++){
                    finalJson = finalArray.getJSONObject(i);
                    String consumption = finalJson.getString("Consumption");
                    flConsumption = flConsumption + Float.valueOf(consumption);

                }

                if(finalArray.length() == 3) {
                    flConsumption = flConsumption / 3;
                }


                rateMultiplier = flConsumption;



            } catch (JSONException e) {
                e.printStackTrace();
                //Log.e(TAG,"isStopCheck "+ e.getMessage());
                Toast.makeText(getApplicationContext(),"No available past 3 consumption(averaging)..",Toast.LENGTH_SHORT).show();
                return;
            }

        }

        while (cursor.moveToNext()) {
            String VATRate = cursor.getString(cursor.getColumnIndex("VATRate")); //componentVatRate
            String FranchiseTaxRate = cursor.getString(cursor.getColumnIndex("FranchiseTaxRate"));
            String LocalTaxRate = cursor.getString(cursor.getColumnIndex("LocalTaxRate"));
            String Amount = cursor.getString(cursor.getColumnIndex("Amount"));

                    rateSchedule = new RateSchedule(
                    cursor.getString(cursor.getColumnIndex("RateSegment")),
                    cursor.getString(cursor.getColumnIndex("RateComponent")),//Rate Code
                    cursor.getString(cursor.getColumnIndex("Extra1")),//rateComponent
                    cursor.getString(cursor.getColumnIndex("RateSchedType")),
                    cursor.getString(cursor.getColumnIndex("IsVAT")),
                    cursor.getString(cursor.getColumnIndex("IsDVAT")),
                    cursor.getString(cursor.getColumnIndex("IsFranchiseTax")),
                    cursor.getString(cursor.getColumnIndex("IsLocalTax")),
                    Float.valueOf(VATRate), //componentVatRate
                    Float.valueOf(FranchiseTaxRate),
                    Float.valueOf(LocalTaxRate),
                    cursor.getString(cursor.getColumnIndex("IsSCDiscount")),
                    cursor.getString(cursor.getColumnIndex("IsLifeLine")),
                    Float.valueOf(Amount),//componentRate
                    cursor.getString(cursor.getColumnIndex("IsOverUnder")));

            componentAmount = CommonFunc.calcComponentAmount(rateSchedule.getComponentRate(), rateMultiplier);


            if (canAvailSCDiscount && rateSchedule.getIsSCDiscount().equalsIgnoreCase("Yes")) {
                scDiscountedAmount = scDiscountedAmount + componentAmount;
            }

            if (canAvailLifelineDiscount && rateSchedule.getIsLifeline().equalsIgnoreCase("Yes")) {
                lifelineDiscountAmount = componentAmount * lifelineDiscountRate;
                totalLifelineDiscount = CommonFunc.round(totalLifelineDiscount + lifelineDiscountAmount,2);
            }

            if(MainActivity.selectedAccount.getUnderOverRecovery().equalsIgnoreCase("1") &&
                    rateSchedule.getIsOverUnder().equalsIgnoreCase("Yes")) {
                overUnderRecovery = overUnderRecovery + componentAmount;//rateSchedule.getComponentRate();
                componentAmount = 0;
                componentvat = 0;
                componentftax = 0;
                componentltax = 0;
            }

            if (canAvailLifelineDiscount && rateSchedule.getRateCode().equalsIgnoreCase("SOL")) {
                componentAmount = 0;
                componentvat = 0;
                componentftax = 0;
                componentltax = 0;
            }
            if ((canAvailSCDiscount|| scInvalidDate || isSCOverPolicy) && rateSchedule.getRateCode().equalsIgnoreCase("SCS")) {
                componentAmount = 0;
                componentvat = 0;
                componentftax = 0;
                componentltax = 0;
            }

            totalComponent = totalComponent + componentAmount;
            myRates.add(new Rates(rateSchedule.getRateSegment(),
                    rateSchedule.getRateCode(),
                    rateSchedule.getRateComponent(),
                    rateSchedule.getComponentRate(),rateSchedule.getIsLifeline(),rateSchedule.getIsSCDiscount(), componentAmount, componentvat, componentftax, componentltax));


        }/**end of loop*/



        if (canAvailSCDiscount) {
            scDiscountedAmount = CommonFunc.round(scDiscountedAmount * scPercentage,2);
        }

        float vtax = (float) .12;
        float currentDue = totalComponent;
        totalComponent = CommonFunc.round(totalComponent,2)  - (totalLifelineDiscount + scDiscountedAmount);

        if(overUnderRecovery < 0) {
            totalComponent = totalComponent + overUnderRecovery;
        } else{
            totalComponent = totalComponent - overUnderRecovery;
        }


        billedAmount = totalComponent + CommonFunc.toDigit(MainActivity.selectedAccount.getPenalty())
                + CommonFunc.toDigit(MainActivity.selectedAccount.getPrevBilling())
                + CommonFunc.toDigit(MainActivity.selectedAccount.getPoleRental())
                + CommonFunc.toDigit(MainActivity.selectedAccount.getSpaceRental());


        MainActivity.selectedAccount.setLatitude("" + MainActivity.gps.getLatitude());
        MainActivity.selectedAccount.setLongitude("" + MainActivity.gps.getLongitude());
        MainActivity.selectedAccount.setTotalSCDiscount(String.valueOf(scDiscountedAmount));
        MainActivity.selectedAccount.setTotalLifeLineDiscount(String.valueOf(totalLifelineDiscount));
        MainActivity.selectedAccount.setOverUnderDiscount(String.valueOf(overUnderRecovery));
        billedAmount = CommonFunc.round(billedAmount,2) - CommonFunc.round(CommonFunc.toDigit(MainActivity.selectedAccount.getAdvancePayment()),2);
        mBill = new Bill(myRates, CommonFunc.round(currentDue,2), billedAmount);
        MainActivity.selectedAccount.setBill(mBill);
        MainActivity.db.updateReadAccount(MainActivity.db, "Read",isStopCheck);
    }

    private float getLifeLinerPercentage(float consume) {
        float value = 0f;
        int kwh = (int)Math.ceil(consume);

        ArrayList<LifeLineSubsidyModel> list = db.getLifeLinePolicy(db);
        if(list.size() > 0) {
            for(LifeLineSubsidyModel l : list) {
                int _kwh = Integer.valueOf(l.getLifelineConsumption());
                if(_kwh == kwh) {
                    return Float.valueOf(l.getLifelineInDecimal());
                }
            }
        }else{
            if(kwh >= 1 && kwh <= 15 ) {
                return 0.50f;
            }

            if(kwh == 16) {
                return 0.40f;
            }

            if(kwh == 17) {
                return 0.30f;
            }

            if(kwh == 18) {
                return 0.20f;
            }

            if (kwh == 19) {
                return 0.10f;
            }

            if(kwh == 20) {
                return 0.005f;
            }
        }

        return  value;
    }

    public void onCheckboxClicked(View view) {

        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();


        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.mOval:
                if (checked) {
                    isOvalCheck = true;
                } else {
                    isOvalCheck = false;
                }
                break;

            case R.id.mCycle: // not used
                if (checked) {
                    //isRecycleCheck = true;
                    //mReadingCycle.setVisibility(View.VISIBLE);
                } else {
                    isRecycleCheck = false;
                    mReadingCycle.setVisibility(View.GONE);
                }
                break;
            case R.id.mStop:
                if (!isStopCheck) {
                    isStopCheck = true;
                    mChange.setChecked(false);
                    isChangeCheck = false;
                } else {
                    isStopCheck = false;
                }
                break;
            case R.id.mChange:
                if (!isChangeCheck) {
                    isChangeCheck = true;
                    mStop.setChecked(false);
                    isStopCheck = false;
                } else {
                    isChangeCheck = false;
                }
                break;

        }

    }

    public void checkReading() {


        MainActivity.selectedAccount.setReading(strReading);

        //check if Higher Voltage
        if (MainActivity.selectedAccount.getAccountClassification().equalsIgnoreCase("Higher Voltage")) {
            if (strDemands.isEmpty()) {
                showToast("Please add Demand Reading");
                return;
            }
        }


        Log.e(TAG,"PrevReading: " + MainActivity.selectedAccount.getInitialReading());
        float consume = CommonFunc.round(maxreadingvalue +
                Double.parseDouble(MainActivity.selectedAccount.getReading()) -
                Double.parseDouble(MainActivity.selectedAccount.getInitialReading()), 2);

        if (consume < 0) {
            showToast("Invalid Reading. Current reading is less than the Previous Reading");
            return;
        }

        /**Check if Change Meter*/

        if (MainActivity.selectedAccount.getIsChangeMeter().equals("1")) {
            String strAveraging = MainActivity.selectedAccount.getAveraging();
            try {
                float flConsumption = 0;
                JSONObject json = new JSONObject(strAveraging);
                JSONArray jsonArrayNew = json.getJSONArray("New");
                JSONArray jsonArrayOld = json.getJSONArray("Old");
                JSONArray finalArray;
                JSONObject finalJson;

                finalArray = jsonArrayNew.length() == 3 ? jsonArrayNew : jsonArrayOld;

                for(int i = 0; i < finalArray.length();i++){
                    finalJson = finalArray.getJSONObject(i);
                    String consumption = finalJson.getString("Consumption");
                    flConsumption = flConsumption + Float.valueOf(consumption);
                }

                if(finalArray.length() == 3) {
                    consume = flConsumption / 3;
                }else{
                    consume = flConsumption;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG,"isStopCheck "+ e.getMessage());
                Toast.makeText(getApplicationContext(),"No availbale past 3 consumption(averaging)..",Toast.LENGTH_SHORT).show();
                return;
            }
        }

        MainActivity.selectedAccount.setConsume(String.valueOf(consume));
        displayButtons();

    }

    public void hideKeyboard() {
        try {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        } catch (Exception e) {

        }
    }

    //endregion

    //region Triggers

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // return true;
            this.finish();
        }
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btnGenerate:
                if(isChangeCheck) {
                    if(strRemarks.isEmpty()) {
                        showToast("Please input remarks...");
                        return;
                    }

                    MainActivity.selectedAccount.setRemarks(strRemarks);
                }


                calculateBill();
                /**PRINTING SOA*/
                if (MainActivity.mIsConnected) {
                    preparePrint();
                } else {
                    Toast.makeText(getBaseContext(), "Printer is not connected.", Toast.LENGTH_SHORT).show();
                }

                String route = MainActivity.selectedAccount.getRouteNo();
                String sequenceNumber = MainActivity.selectedAccount.getSequenceNo();
                try{
                    if(sequenceNumber.equalsIgnoreCase(".") || sequenceNumber == null){
                        showToast("Sequence number is not a number, please check");
                        return;
                    }
                } catch (NullPointerException e) {
                    showToast("Sequence number is not a number, please check");
                    return;
                }

                int count = db.searchNextAccountToRead(db,route,sequenceNumber,MainActivity.selectedAccount.getAccountID());
                if (count == 0) {
                        this.finish();
                }
                //Intent intent = new Intent(this, BillPreview.class);
                Intent intent = new Intent(this, Accounts.class);
                startActivity(intent);
                this.finish();
                break;
            case R.id.btnTakePic:
                break;
        }
    }

    //endregion

    //region SubClass


    public class TextChangeListener implements TextWatcher {

        Context c;
        EditText editText;

        public TextChangeListener() {
        }

        public TextChangeListener(Context c, EditText editText) {
            this.c = c;
            this.editText = editText;
        }


        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (editText.getId() == R.id.mReading)
                if (isOvalCheck) {
                    mReading.setSelection(i);
                    Log.e(TAG,"oval checked: "+i);
                }
        }

        @Override
        public void afterTextChanged(Editable editable) {


            switch (editText.getId()) {

                case R.id.mReadingCycle:

                    if (!mReadingCycle.getText().toString().isEmpty()) {
                        if (Double.parseDouble(mReadingCycle.getText().toString()) != 0) {
                            maxreadingvalue = Float.parseFloat(mReadingCycle.getText().toString());
                        }
                    }

                    break;

                case R.id.mReading:
                    if (mReading.getText().toString().matches("^0")) {
                        mReading.setText("");
                    } else {
                        strReading = mReading.getText().toString();
                    }

                    break;


                case R.id.mDemand:
                    if (mDemand.getText().toString().matches("^0")) {
                        mDemand.setText("");
                    } else {
                        strDemands = mDemand.getText().toString();
                    }

                    break;

                case R.id.mRemarks:
                    //Log.e(TAG,"afterTextChanged (mRemarks)" + mRemarks.getText().toString());
                    strRemarks = mRemarks.getText().toString();
                    break;
            }

        }
    }

    public class ActionListener implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

            hideKeyboard();

            switch (textView.getId()) {

                case R.id.mReading:
                    Log.e(TAG,"onEditorAction (mReading)");
                    if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_NEXT) {


                        if (strReading.isEmpty()) {
                            showToast("Please input a Reading");
                            return false;
                        }

                        //check if Higher Voltage
                        if (MainActivity.selectedAccount.getAccountClassification().equalsIgnoreCase("Higher Voltage")) {
                            if (strDemands.isEmpty()) {
                                showToast("Please add Demand Reading");
                                return false;
                            }
                        }

                        /**triggers if meter is stop*/
                        if (strReading.equals(".1") && !strRemarks.isEmpty()) {
                            MainActivity.selectedAccount.setConsume("0");
                            MainActivity.selectedAccount.setReading(".1");
                            MainActivity.selectedAccount.setRemarks(strRemarks);
                            displayButtons();
                            return false;
                        }

                        if (Double.parseDouble(strReading) < 0) {
                            showToast("Please input a valid reading");
                            return false;
                        }


                        checkReading();


                    }

                    break;
                case R.id.mDemand:

                    if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_NEXT) {


                        if (strReading.isEmpty()) {
                            showToast("Please add a new Reading");
                            return false;
                        }

                        //check if Higher Voltage
                        if (MainActivity.selectedAccount.getAccountClassification().equalsIgnoreCase("Higher Voltage")) {
                            if (strDemands.isEmpty()) {
                                showToast("Please add Demand Reading");
                                return false;
                            }
                        }

                        if (strReading.equals(".1") && !strRemarks.isEmpty()) {
                            MainActivity.selectedAccount.setConsume("0");
                            MainActivity.selectedAccount.setReading(".1");
                            MainActivity.selectedAccount.setRemarks(strRemarks);
                            displayButtons();
                            return false;
                        }

                        if (Double.parseDouble(strReading) < 0) {
                            showToast("Please input a valid reading");
                            return false;
                        }


                        checkReading();


                    }

                    break;
                case R.id.mRemarks:
                    strRemarks = mRemarks.getText().toString();
                    //Log.e(TAG,"Remarks :" + mRemarks.getText().toString());
                    break;
            }
            return false;
        }
    }


    public void preparePrint() {
        List<Rates> mRates;
        Bill mBill = MainActivity.selectedAccount.getBill();
        Rates rates;
        mRates = mBill.getRates();
        MobilePrinter mp = MobilePrinter.getInstance(this);
        mp.printText("           Negros Oriental II Electric Cooperative\n");
        mp.printText("                  Real St., Dumaguete City\n");
        mp.printText("                         (NORECO2)\n");
        mp.printText("                   STATEMENT OF ACCOUNT\n");
        mp.printText("================================================================\n");
        mp.printText("Meter No:" + MainActivity.selectedAccount.getMeterSerialNo(), "Type:" + MainActivity.selectedAccount.getAccountClassification()+"\n");
        mp.printText("Account No:" + MainActivity.selectedAccount.getAccountID(), "BillMonth:" + MainActivity.selectedAccount.getBillMonth()+"\n");
        mp.printText("Account Name:"+MainActivity.selectedAccount.getLastName()+"\n");
        mp.printText("Address:"+ MainActivity.selectedAccount.getAddress()+"\n");
        mp.printText("Period Covered: "+MainActivity.selectedAccount.getLastReadingDate() + " to " + MainActivity.selectedAccount.getDateRead()+"\n");
        mp.printText("Due Date: "+MainActivity.selectedAccount.getDueDate()+"\n");//
        mp.printText("Meter Reader:" + MainActivity.reader.getReaderName()+"\n");
        mp.printText("--------------------------------------------------------------"+"\n");
        mp.printText("Date              Prev                 Pres              KWH"+"\n");
        //mp.printText(MainActivity.selectedAccount.getDateRead() + "         " + MainActivity.selectedAccount.getPrevReading() + "              " + MainActivity.selectedAccount.getReading() + "              " + MainActivity.selectedAccount.getConsume()+"\n");
        mp.printText(MainActivity.selectedAccount.getDateRead(),MainActivity.selectedAccount.getPrevReading(),MainActivity.selectedAccount.getReading(),MainActivity.selectedAccount.getConsume()+"\n");
        mp.printText("--------------------------------------------------------------"+"\n");

        /**
         * NOTE
         cursorRateSegment is defined in MainActivity, and was assign in ViewDetails class
         * */

        if(listRateSegment.size() > 0){
            //int s = 0; s < listRateSegment.size(); s++
            for (RateSegmentModel seg: listRateSegment){
                String segmentName = seg.getRateSegmentName();
                String rateSegmentCode =  seg.getRateSegmentCode();

                /** print here */
                mp.printText(segmentName+"\n");

                for(Rates r: mRates) {
                    if(r.getRateSegment().equals(rateSegmentCode)) {
                        String codeName = r.getCodeName();
                        String rateAmount = String.valueOf(r.getRateAmount());
                        String amount = String.valueOf(r.getAmount());
                        int padding = 20 - rateAmount.length() - amount.length();
                        String paddingChar = " ";
                        for (int p = 0; p < padding; p++) {
                            paddingChar = paddingChar.concat(" ");
                        }
                        String rightText = rateAmount + paddingChar + amount;

                        /** print here */
                        mp.printText(codeName,rightText+"\n");
                    }
                }
            } // end loop
        }


        mp.printText("Total Current Due", MainActivity.dec2.format(mBill.getTotalAmount())+"\n");
        mp.printText("Add:Penalty:", MainActivity.dec2.format(Double.valueOf(MainActivity.selectedAccount.getPenalty()))+"\n");
        mp.printText("Arrears:", MainActivity.dec2.format(Double.valueOf(MainActivity.selectedAccount.getPrevBilling()))+"\n");
        mp.printText("Pole Rental", MainActivity.dec2.format(Double.valueOf(MainActivity.selectedAccount.getPoleRental()))+"\n");
        mp.printText("Space Rental", MainActivity.dec2.format(Double.valueOf(MainActivity.selectedAccount.getSpaceRental()))+"\n");
        mp.printText("Less:Advance Payment:", MainActivity.dec2.format(Double.valueOf(MainActivity.selectedAccount.getAdvancePayment()))+"\n");
        if(canAvailLifelineDiscount) {
            mp.printText("Life Line Discount(R)", MainActivity.dec2.format(Double.valueOf(MainActivity.selectedAccount.getTotalLifeLineDiscount()))+"\n");
        }
        if(canAvailSCDiscount) {
            mp.printText("SC Discount(R)", MainActivity.dec2.format(Double.valueOf(MainActivity.selectedAccount.getTotalSCDiscount()))+"\n");
        }
        if(MainActivity.selectedAccount.getUnderOverRecovery().equalsIgnoreCase("1")) {
            mp.printText("Over Under Recovery", MainActivity.dec2.format(Double.valueOf(MainActivity.selectedAccount.getOverUnderDiscount()))+"\n");
        }


        mp.printText("PAYABLE AMOUNT(AfterDueDate)", MainActivity.dec2.format(mBill.getTotalBilledAmount())+"\n\n\n");

        //Printed
        db.updateAccountToPrinted(db,"Printed");
    }

}
