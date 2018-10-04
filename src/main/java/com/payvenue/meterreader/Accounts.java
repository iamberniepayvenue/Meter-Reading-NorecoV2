    package com.payvenue.meterreader;

    import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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

import com.google.gson.GsonBuilder;
import com.payvenue.meterreader.Camera.ZBarScannerActivity;
import com.payvenue.meterreader.Fragments.MyDialogFragment;
import com.payvenue.meterreader.Interface.MyDialogInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

    import java.text.DecimalFormat;
    import java.util.ArrayList;
import java.util.List;

import DataBase.DataBaseHandler;
import Model.Account;
import Model.Bill;
import Model.LifeLineSubsidyModel;
import Model.Policies;
import Model.RateSchedule;
import Model.RateSegmentModel;
import Model.Rates;
import Utility.CommonFunc;
import Utility.MobilePrinter;
import ZBar.ZBarConstants;

import static com.payvenue.meterreader.Fragments.FragmentReading.ZBAR_SCANNER_REQUEST;


    /**
     * Created by andrewlaurienrsocia on 16/04/2018.
     */


    public class Accounts extends AppCompatActivity implements View.OnClickListener,MyDialogInterface {

        private static final String TAG = "Accounts";
        ConstraintLayout constraintButton;
        TextView mSerial, mAccountName, mAccountID, mAccountClass, mAccountAddress,mPrevReading;
        CheckBox mOval, mCycle, mStop, mChange;
        EditText mReading, mDemand, mRemarks, mReadingCycle;
        Button btnGenerate, btnTakePic;
        Context mcontext;
        boolean isOvalCheck = false, isRecycleCheck = false, isStopCheck = false, isChangeCheck = false;
        double maxreadingvalue = 0;
        String strReading = "", strRemarks = "", strDemands = "";
        float rateMultiplier,exportMultiplier;
        float multiplier,totalAmountDueExport = 0,netBillAmountExport = 0;
        float scPercentage;// = (float) 0.05;
        float componentAmount, componentvat, componentltax, componentftax,lifelineDiscountRate,scDiscountedAmount,
                lifelineDiscountAmount,totalLifelineDiscount,totalComponent, billedAmount;
        float scSubsidy = 0;
        float sol = 0;
        float overUnder = 0;
        float totalSeniorDiscount;
        float demandKWMininum;
        boolean canAvailLifelineDiscount = false, canAvailSCDiscount = false;
        boolean scInvalidDate = false;
        boolean isSCOverPolicy = false;
        ArrayList<Rates> myRates = new ArrayList<>();
        ArrayList<RateSegmentModel> listRateSegment = new ArrayList<>();
        ArrayList<Policies> policiesArrayList = new ArrayList<>();
        //public  ArrayList<Components> componentsList = new ArrayList<>();
        Bill mBill;
        String billMonth;
        public DataBaseHandler db;
        InputMethodManager imm;

        private Snackbar snackbar;
        private String initialRead;
        private String coreLoss;
        private String isNetMetering;
        private String a_class;
        private Account mAccount;
        private boolean isHigherVoltage = false;
        private boolean isLowerVoltage = false;
        private boolean isMotherMeter = false;


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




            mAccount =  MainActivity.selectedAccount;
            a_class = mAccount.getAccountClassification();
            if(mAccount.getIsCheckSubMeterType().equalsIgnoreCase("M") || mAccount.getIsCheckSubMeterType().equalsIgnoreCase("m")) {
                isMotherMeter = true;
            }
    //        if(a_class.contains("Voltage") || a_class.contains("voltage")) {
    //            a_class = a_class.replace(" "," ");
    //        }
    //
    //        if(a_class.equalsIgnoreCase(a_class.toUpperCase())) {
    //            a_class = a_class.substring(0,1).toUpperCase() + a_class.substring(1).toLowerCase();
    //        }

            if(a_class.contains("Higher") || a_class.contains("higher") || a_class.contains("HIGHER")) {
                isHigherVoltage = true;
            }

            if(a_class.contains("Lower") || a_class.contains("lower")) {
                isLowerVoltage = true;
                Log.e(TAG,"Lower V here");
            }

            if(mAccount.getMultiplier().equalsIgnoreCase(".") ||
                    mAccount.getMultiplier().equalsIgnoreCase("")) {
                multiplier = 0;
            }else{
                multiplier = Float.valueOf(mAccount.getMultiplier());
            }

            Log.e(TAG,"Classification: " + a_class);
            isNetMetering = mAccount.getIsNetMetering();

            policiesArrayList = db.getBillingPolicy(db,a_class);

            scPercentage = getSCDiscount();

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
            mPrevReading = findViewById(R.id.mPrevReading);
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
            mDemand.addTextChangedListener(new TextChangeListener(mcontext,mDemand));

            //Action Listener
            mReading.setOnEditorActionListener(new ActionListener());
            mDemand.setOnEditorActionListener(new ActionListener());

            //Check if Higher Voltage. If True show Demand Editext.
            if (isHigherVoltage)
                mDemand.setVisibility(View.VISIBLE);

        }

        public void setValues() {

            mSerial.setText(mAccount.getMeterSerialNo());
            mAccountName.setText(mAccount.getLastName());
            mAccountID.setText(mAccount.getAccountID());
            mAccountClass.setText(mAccount.getAccountClassification());
            mAccountAddress.setText(mAccount.getAddress());
            String prevReading = mAccount.getInitialReading();
            if(prevReading.equalsIgnoreCase(".") || prevReading.equalsIgnoreCase("")) {
                mPrevReading.setText(" Previous Reading: 0 kwh");
            }else{
                mPrevReading.setText(" Previous Reading: "+prevReading+" kwh");
            }


        }

        public void displayButtons() {
            constraintButton.setVisibility(View.VISIBLE);
        }

        public void showToast(String message) {
            Toast.makeText(mcontext, message, Toast.LENGTH_SHORT).show();
        }

        public void setSnackbar(String msg) {
            snackbar = Snackbar.make(getWindow().getDecorView().getRootView(),msg,Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        public void calculateBill() {

            float totalLifelineComponentAmount = 0;

            mAccount.setRemarks(strRemarks);

            if (mAccount.getConsume().equals("0") && !mAccount.getRemarks().isEmpty()) {
                MainActivity.db.updateReadAccount(MainActivity.db, "Cannot Generate",isStopCheck);
                return;
            }

            if(isChangeCheck) {
                MainActivity.db.updateReadAccount(MainActivity.db, "Cannot Generate",isStopCheck);
                return;
            }

            /** initialRead is Previous Reading more details in checkingReading method*/
            if(initialRead.equalsIgnoreCase("") || initialRead.equalsIgnoreCase(".")) {
                initialRead = "0";
            }

            if (Float.parseFloat(initialRead) < 0) {
                showToast("Previous Reading is invalid.");
                return;
            }

    //        if (mAccount.getAccountClassification().equalsIgnoreCase("Higher Voltage")) {
    //            MainActivity.db.updateReadAccount(MainActivity.db, "Cannot Generate",isStopCheck);
    //            showToast("Cant generate billing for a Higher Voltage classification.");
    //            return;
    //        }

            /**Check Senior Status Discount*/
            if (mAccount.getSeniorCitizenStatus().equals("1") && a_class.equalsIgnoreCase("Residential") ) {
                    if(CommonFunc.isValidDate(mAccount.getSCExpiryDate())) {
                        Log.e(TAG,"SC DATE is Valid");
                        String consumption = mAccount.getConsume();
                        int scPolicyMax = getSCDMaxPolicy();
                        int scPolicyMin = getSCDMinPolicy();

                        if(scPolicyMax == -1 || scPolicyMin == -1 ) {
                            setSnackbar("No Policy for Senior... cannot process");
                            return;
                        }

                        //Float.valueOf(consumption) <= 100
                        if(Float.valueOf(consumption) >= Float.valueOf(scPolicyMin) && Float.valueOf(consumption) <= Float.valueOf(scPolicyMax)) {
                            canAvailSCDiscount = true;
                        }else{
                            isSCOverPolicy = true;
                        }
                    }else{
                        scInvalidDate = true;
                    }
            }



            /**Fetch Rate Schedule*/
            Cursor cursor = MainActivity.db.getRateSched(MainActivity.db,
                    mAccount.getRateSched(),
                    a_class);

            if (cursor.getCount() <= 0) {
                showToast("No Rateschedule Created for this Classification");
                return;
            }


            RateSchedule rateSchedule = null;

            billMonth = CommonFunc.getBillMonth(); //+ "-" + mAccount.getRouteNo() + "-" + mAccount.getAccountID();
            mAccount.setBillMonth(billMonth);
            mAccount.setDateRead(CommonFunc.getDateOnly());

            String strConsume = mAccount.getConsume();
            float flConsume = Float.parseFloat(strConsume);
            rateMultiplier = flConsume;


            /**STOP METER*/
            if(isStopCheck) {
                float av = getAveraging();
                if(av != -2000) {
                    rateMultiplier = av;
                }else{
                    return;
                }
            }


            /**Check Lifeliner*/
            if (a_class.equalsIgnoreCase("Residential")) {

                lifelineDiscountRate = getLifeLinerPercentage(rateMultiplier);
                if(lifelineDiscountRate > 0) {
                    canAvailLifelineDiscount = true;
                }

                Log.e(TAG,"Lifeline Discount :"+lifelineDiscountRate);
            }

            Log.e(TAG,"Is Mother Meter: " + isMotherMeter);
            if(!isMotherMeter) {

                while (cursor.moveToNext()) {
                    String strComponentAmount = "0";
                    String Amount = cursor.getString(cursor.getColumnIndex("Amount"));
                    rateSchedule = new RateSchedule(
                            cursor.getString(cursor.getColumnIndex("RateSegment")),
                            cursor.getString(cursor.getColumnIndex("RateComponent")),//Rate Code
                            cursor.getString(cursor.getColumnIndex("Extra1")),//rateComponent
                            cursor.getString(cursor.getColumnIndex("RateSchedType")),
//                            cursor.getString(cursor.getColumnIndex("IsVAT")),
//                            cursor.getString(cursor.getColumnIndex("IsDVAT")),
//                            cursor.getString(cursor.getColumnIndex("IsFranchiseTax")),
//                            cursor.getString(cursor.getColumnIndex("IsLocalTax")),
//                            0, //componentVatRate
//                            0,//FranchiseTaxRate
//                            0,//LocalTaxRate
                            cursor.getString(cursor.getColumnIndex("IsSCDiscount")),
                            cursor.getString(cursor.getColumnIndex("IsLifeLine")),
                            Float.valueOf(Amount),//componentRate
                            cursor.getString(cursor.getColumnIndex("IsOverUnder")),
                            cursor.getString(cursor.getColumnIndex("IsExport")));


                    /** Lower Voltage and Higher Voltage
                     *
                     * FIXED
                     *  Supply Retail Customer Charge
                     *  Supply System Charge
                     *  Metering Retail Customer Charge
                     * */
                    int fixed = 0;
                    if (isLowerVoltage || isHigherVoltage) {
                        if (rateSchedule.getRateComponent().equalsIgnoreCase("Supply Retail Customer Charge") || rateSchedule.getRateComponent().equalsIgnoreCase("Supply System Charge") || rateSchedule.getRateComponent().equalsIgnoreCase("Metering Retail Customer Charge")) {
                            componentAmount = rateSchedule.getComponentRate();
                            strComponentAmount = String.valueOf(componentAmount);
                            fixed = 1;
                        }
                    } else {
                        if (rateSchedule.getRateComponent().equalsIgnoreCase("Metering Retail Customer Charge")) {
                            componentAmount = rateSchedule.getComponentRate();
                            strComponentAmount = String.valueOf(componentAmount);
                            fixed = 1;
                        }
                    }

                    Log.e(TAG, "fixed : " + fixed);
                    if (fixed == 0) {
                        String _strComponentAmount = CommonFunc.calcComponentAmount(rateSchedule.getComponentRate(), rateMultiplier);
                        componentAmount = CommonFunc.toDigit(_strComponentAmount);
                        strComponentAmount = _strComponentAmount;
                    }

                    //Log.e(TAG,"component amount : " + componentAmount);
                    if (isHigherVoltage) {
                        demandKWMininum = Float.valueOf(mAccount.getDemandKW());
                        Log.e(TAG, "reg: " + rateSchedule.getRateComponent() + " ----- " + componentAmount);
                        if (rateSchedule.getRateType().equalsIgnoreCase("PERKW") || rateSchedule.getRateType().equalsIgnoreCase("PKW") || rateSchedule.getRateType().equalsIgnoreCase("P/kW")) {
                            if (fixed == 0) {
                                Log.e(TAG, "here : here" + demandKWMininum);
                                String _strComponentAmount = CommonFunc.calcComponentAmount(rateSchedule.getComponentRate(), demandKWMininum);
                                componentAmount = CommonFunc.toDigit(_strComponentAmount);
                                strComponentAmount = _strComponentAmount;
                                Log.e(TAG, "demand : " + rateSchedule.getRateComponent() + " ----- " + componentAmount);
                            }
                        }
                    }


                    if (canAvailSCDiscount && rateSchedule.getIsSCDiscount().equalsIgnoreCase("Yes")) {
                        scDiscountedAmount = componentAmount * scPercentage;
                        totalSeniorDiscount = totalSeniorDiscount + scDiscountedAmount;
                    }

                    if (canAvailLifelineDiscount && rateSchedule.getIsLifeline().equalsIgnoreCase("Yes")) {
                        lifelineDiscountAmount = componentAmount * lifelineDiscountRate;
                        totalLifelineComponentAmount = totalLifelineComponentAmount + componentAmount;
                        totalLifelineDiscount = totalLifelineDiscount + lifelineDiscountAmount;
                        //Log.e(TAG,"lifeline :("+componentAmount+" * "+ lifelineDiscountRate +") = " + lifelineDiscountAmount);
                        //Log.e(TAG,"total :("+totalLifelineComponentAmount);
                        //Log.e(TAG,"totaldiscount :("+totalLifelineDiscount);
                    }

                    if (mAccount.getUnderOverRecovery().equalsIgnoreCase("0") &&
                            rateSchedule.getIsOverUnder().equalsIgnoreCase("Yes")) {
                        overUnder = overUnder + componentAmount;
                        componentAmount = 0;
                        strComponentAmount = String.valueOf(componentAmount);
                        componentvat = 0;
                        componentftax = 0;
                        componentltax = 0;
                    }

                    if (canAvailLifelineDiscount && rateSchedule.getRateCode().equalsIgnoreCase("SOL")) {
                        componentAmount = 0;
                        strComponentAmount = String.valueOf(componentAmount);
                        componentvat = 0;
                        componentftax = 0;
                        componentltax = 0;
                    }

                    if (canAvailLifelineDiscount && (rateSchedule.getRateCode().equalsIgnoreCase("ICCS")
                            || rateSchedule.getRateCode().equalsIgnoreCase("OLRA")
                            || rateSchedule.getRateCode().equalsIgnoreCase("SCS"))) {
                        componentAmount = 0;
                        strComponentAmount = String.valueOf(componentAmount);
                    }

                    if ((scInvalidDate || isSCOverPolicy) && (rateSchedule.getRateCode().equalsIgnoreCase("SCS"))) {
                        //rateSchedule.getRateCode().equalsIgnoreCase("SOL"))
                        componentAmount = 0;
                        strComponentAmount = String.valueOf(componentAmount);
                        componentvat = 0;
                        componentftax = 0;
                        componentltax = 0;
                    }

                    if (canAvailSCDiscount && rateSchedule.getRateCode().equalsIgnoreCase("SCS")) {
                        componentAmount = 0;
                        strComponentAmount = String.valueOf(componentAmount);
                        componentvat = 0;
                        componentftax = 0;
                        componentltax = 0;
                    }


                    /**record senior subsidy*/
                    if (rateSchedule.getRateCode().equalsIgnoreCase("SCS")) {
                        scSubsidy = componentAmount;
                        DecimalFormat df = new DecimalFormat("##.####");
                        df.format(scSubsidy);
                    }

                    /**record lifeline subsidy*/
                    if (rateSchedule.getRateCode().equalsIgnoreCase("SOL")) {
                        sol = componentAmount;
                    }


                    totalComponent = totalComponent + componentAmount;
                    float amountDueExport = 0;
                    if (isNetMetering.equalsIgnoreCase("1")) {
                        exportMultiplier = Float.valueOf(mAccount.getExportConsume());
                        if (rateSchedule.getIsExport().equalsIgnoreCase("1") || rateSchedule.getIsExport().equalsIgnoreCase("Yes")) {
                            String _strComponentAmount = CommonFunc.calcComponentAmount(rateSchedule.getComponentRate(), exportMultiplier);
                            if (rateSchedule.getRateComponent().equalsIgnoreCase("Generation System Charges")) {
                                amountDueExport = CommonFunc.toDigit(_strComponentAmount);
                            }

                            if (rateSchedule.getRateComponent().equalsIgnoreCase("METERING SYSTEM CHARGE") || rateSchedule.getRateComponent().equalsIgnoreCase("Metering System Charge")) {
                                amountDueExport = -CommonFunc.toDigit(_strComponentAmount);
                            }

                            if (rateSchedule.getRateComponent().equalsIgnoreCase("Metering Retail Customer Charge")) {
                                amountDueExport = -rateSchedule.getComponentRate();
                            }


                            totalAmountDueExport = totalAmountDueExport + amountDueExport;
                        }

                    }

                    Log.e(TAG, "component amount --- : " + componentAmount);
                    Log.e(TAG, "component amount1 --- : " + strComponentAmount);
                    myRates.add(new Rates(rateSchedule.getRateSegment(),
                            rateSchedule.getRateCode(),
                            rateSchedule.getRateComponent(),
                            Amount, rateSchedule.getIsLifeline(), rateSchedule.getIsSCDiscount(), rateSchedule.getIsExport(), strComponentAmount, amountDueExport)); //componentvat, componentftax, componentltax,

                }/**end of loop*/


                if (canAvailSCDiscount) {
                    int kwh = (int) Math.ceil(rateMultiplier);

                    if (kwh >= getMinkWhLifeLine() && kwh <= getMaxkWhLifeLine()) {
                        totalSeniorDiscount = (totalLifelineComponentAmount - totalLifelineDiscount) * scPercentage;
                    }
                    /**
                     *  Formula for senior if 1 to 20
                     * seniordiscount = (totallifeline - (totalLifeLine * lifelinerate)) * .05
                     *
                     *
                     **/
                }
                //Log.e(TAG,"senior: " + totalSeniorDiscount);
                totalComponent = totalComponent - (totalLifelineDiscount + totalSeniorDiscount);
                String penalty = mAccount.getPenalty();
                if (!penalty.equalsIgnoreCase("0")) {
                    penalty = "0";
                }

                billedAmount = totalComponent + CommonFunc.toDigit(penalty)
                        + CommonFunc.toDigit(mAccount.getPrevBilling());

                /** Not included */
            /*
            + CommonFunc.toDigit(mAccount.getPoleRental())
                    + CommonFunc.toDigit(mAccount.getSpaceRental()
                            + CommonFunc.toDigit(mAccount.getPilferagePenalty())
                    */

                billedAmount = CommonFunc.round(billedAmount,2) - CommonFunc.round(CommonFunc.toDigit(mAccount.getAdvancePayment()),2);
                netBillAmountExport = billedAmount - totalAmountDueExport;
                mBill = new Bill(myRates, CommonFunc.round(totalComponent,2), billedAmount,netBillAmountExport,totalAmountDueExport);
                mAccount.setBill(mBill);

            }

            mAccount.setLatitude("" + MainActivity.gps.getLatitude());
            mAccount.setLongitude("" + MainActivity.gps.getLongitude());
            mAccount.setTotalSCDiscount(String.valueOf(totalSeniorDiscount));
            mAccount.setTotalLifeLineDiscount(String.valueOf(totalLifelineDiscount));
            mAccount.setOverUnderDiscount(String.valueOf(overUnder));
            mAccount.setLifeLineSubsidy(String.valueOf(sol));
            mAccount.setSeniorSubsidy(String.valueOf(scSubsidy));

            Log.e(TAG, "SeniorSubsidy: " + scSubsidy);
            Log.e(TAG, "LifeLineSubsidy: " + sol);
            Log.e(TAG, "scSubsidy: " + totalSeniorDiscount);
            Log.e(TAG, "scSubsidy: " + totalLifelineDiscount);
            Log.e(TAG, "mBill: " + new GsonBuilder().create().toJson(mAccount.getBill()));
            MainActivity.db.updateReadAccount(MainActivity.db, "Read",isStopCheck);
        }

        private float getSCDiscount() {
            for(Policies p: policiesArrayList) {
                int _kwh = Integer.valueOf(p.getMaxkWh());
                if(p.getPolicyCode().equals("SCD")){
                    return Float.valueOf(p.getPercentAmount());
                }
            }

            return -1;
        }

        private int getSCDMaxPolicy() {
            for(Policies p: policiesArrayList) {
                if(p.getPolicyCode().equals("SCD")){
                    return Integer.valueOf(p.getMaxkWh());
                }
            }

            return -1;
        }

        private int getSCDMinPolicy() {
            for(Policies p: policiesArrayList) {
                if(p.getPolicyCode().equals("SCD")){
                    return Integer.valueOf(p.getMinkWh());
                }
            }

            return -1;
        }

        private int getMaxkWhLifeLine(){
            for(Policies p: policiesArrayList) {
                if(p.getPolicyCode().equals("LLD")){
                    return Integer.valueOf(p.getMaxkWh());
                }
            }

            return -1;
        }

        private int getMinkWhLifeLine(){
            for(Policies p: policiesArrayList) {
                if(p.getPolicyCode().equals("LLD")){
                    return Integer.valueOf(p.getMinkWh());
                }
            }

            return -1;
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

            mAccount.setReading(strReading);

            initialRead = mAccount.getInitialReading();
            if(initialRead.equalsIgnoreCase("") || initialRead.equalsIgnoreCase(".")) {
                initialRead = "0";
            }

            coreLoss = mAccount.getCoreloss();
            if(coreLoss.equalsIgnoreCase("") || coreLoss.equalsIgnoreCase(".")) {
                coreLoss = "0";
            }


            float consume = CommonFunc.round((maxreadingvalue + Double.parseDouble(mAccount.getReading())) - Double.parseDouble(initialRead), 2);

            if (isHigherVoltage) {
                if (strDemands.isEmpty()) {
                    showToast("Please add Demand Reading");
                    return;
                }

                demandKWMininum = Float.valueOf(mAccount.getDemandKW());
                if(demandKWMininum < Float.valueOf(strDemands)){
                    demandKWMininum = Float.valueOf(strDemands);
                }

                /**Update demandKWMininum*/
                mAccount.setDemandKW(String.valueOf(demandKWMininum));
            }

            if (consume < 0) {
                showToast("Invalid Reading. Current reading is less than the Previous Reading");
                return;
            }

            /**Check if Change Meter*/

            if (mAccount.getIsChangeMeter().equals("1")) {
                float av = getAveraging();
                if(av != -2000) {
                    consume = av;
                }else{
                    return;
                }
            }



            float kwh = consume * multiplier + Float.valueOf(coreLoss);
            Log.e(TAG,"new Reading : " + strReading);
            Log.e(TAG,"consumption * multiplier: " + kwh);
            mAccount.setConsume(String.valueOf(kwh));
            mAccount.setActualConsumption(String.valueOf(consume));


            if(isNetMetering.equalsIgnoreCase("1")) {
                android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                MyDialogFragment dialogFragment = new MyDialogFragment();
                dialogFragment.mListener = this;
                dialogFragment.show(fm,"MyDialogFragment");
            }else{
                displayButtons();
            }
        }

        @Override
        public void onMyDialogDismiss(int tag) {
            if(tag == 0) {
                float consumeExport = CommonFunc.round((maxreadingvalue + Double.parseDouble(mAccount.getExportReading())) - Double.parseDouble(mAccount.getExportPreviousReading()), 2);
                float kwhExport = consumeExport * multiplier + Float.valueOf(coreLoss);
                mAccount.setExportConsume(String.valueOf(kwhExport));
                mAccount.setActualExportConsume(String.valueOf(consumeExport));
                Log.e(TAG,"consumeExport:" + consumeExport);
                Log.e(TAG,"kwhExport:" + kwhExport);
                displayButtons();
            }
        }

        public float getAveraging() {
            //if (mAccount.getIsChangeMeter().equals("1")) {
            float val;
                String strAveraging = mAccount.getAveraging();
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

                    val = finalArray.length() == 3 ? flConsumption/3 : flConsumption;

                } catch (JSONException e) {
                    e.printStackTrace();
                    showToast("No availbale past 3 consumption(averaging)..");
                    val = -2000;
                }
            //}

            return val;
        }

        public void hideKeyboard() {
            try {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            } catch (Exception e) {

            }
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
            if (id == R.id.menu_scanner) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        showToast("You declined to allow the app to access your camera");
                        return true;
                    }
                }

                if (isCameraAvailable()) {
                    Intent intent = new Intent(this, ZBarScannerActivity.class);
                    startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
                } else {
                    showToast("Rear Facing Camera Unavailable");
                }
            }
            return super.onOptionsItemSelected(item);
        }

        public boolean isCameraAvailable() {
            PackageManager pm = this.getPackageManager();
            return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case ZBAR_SCANNER_REQUEST:
                    if (resultCode == this.RESULT_OK) {

                        ToneGenerator toneG = new ToneGenerator(
                                AudioManager.STREAM_ALARM, 100);
                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

                        if (data != null) {
                            mSerial.setText(data
                                    .getStringExtra(ZBarConstants.SCAN_RESULT));
                            db.updateSerialNumber(db,mAccount.getAccountID(),data
                                    .getStringExtra(ZBarConstants.SCAN_RESULT));
                        }
                    }

                    break;
            }
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

                        mAccount.setRemarks(strRemarks);
                    }


                    calculateBill();
                    /**PRINTING SOA*/
                    if (MainActivity.mIsConnected) {
                        preparePrint();
                    } else {
                        showToast("Printer is not connected.");
                    }

                    String route = mAccount.getRouteNo();
                    String sequenceNumber = mAccount.getSequenceNo();
                    try{
                        if(sequenceNumber.equalsIgnoreCase(".") || sequenceNumber == null){
                            showToast("Sequence number is not a number, please check");
                            return;
                        }
                    } catch (NullPointerException e) {
                        showToast("Sequence number is not a number, please check");
                        return;
                    }

                    int count = db.searchNextAccountToRead(db,route,sequenceNumber,mAccount.getAccountID());
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
                        if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_NEXT) {


                            if (strReading.isEmpty()) {
                                showToast("Please input a Reading");
                                return false;
                            }

                            //check if Higher Voltage
                            if (mAccount.getAccountClassification().equalsIgnoreCase("Higher Voltage")) {
                                if (strDemands.isEmpty()) {
                                    showToast("Please add Demand Reading");
                                    return false;
                                }
                            }

                            /**triggers if meter is stop*/
                            if (strReading.equals(".1") && !strRemarks.isEmpty()) {
                                mAccount.setConsume("0");
                                mAccount.setReading(".1");
                                mAccount.setRemarks(strRemarks);
                                displayButtons();
                                return false;
                            }

                            if (Double.parseDouble(strReading) < 0) {
                                showToast("Please input a valid reading");
                                return false;
                            }


                            if(!isHigherVoltage){
                                checkReading();
                            }
                        }

                        break;
                    case R.id.mDemand:

                        if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_NEXT) {


                            if (strReading.isEmpty()) {
                                showToast("Please add a new Reading");
                                return false;
                            }

                            //check if Higher Voltage
                            if (mAccount.getAccountClassification().equalsIgnoreCase("Higher Voltage")) {
                                if (strDemands.isEmpty()) {
                                    showToast("Please add Demand Reading");
                                    return false;
                                }
                            }

                            if (strReading.equals(".1") && !strRemarks.isEmpty()) {
                                mAccount.setConsume("0");
                                mAccount.setReading(".1");
                                mAccount.setRemarks(strRemarks);
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
                        break;
                }
                return false;
            }
        }


        public void preparePrint() {

            ArrayList<String> vatListCode = new ArrayList<>();
            ArrayList<String> vatListValue = new ArrayList<>();
            ArrayList<String> rateComponentForExport = new ArrayList<>();
            ArrayList<String> exportRateDueAmount = new ArrayList<>();
            String name = mAccount.getLastName();
            if(mAccount.getFirstName().equalsIgnoreCase(".")
                    || mAccount.getMiddleName().equalsIgnoreCase(".")){
                name = mAccount.getLastName() + ", " + mAccount.getFirstName()
                        + mAccount.getMiddleName();
            }

            String penalty = mAccount.getPenalty();
            if(penalty.equalsIgnoreCase("-") || penalty.equalsIgnoreCase(".")) {
                penalty = "0";
            }

            try{
                List<Rates> mRates;
                Bill mBill = mAccount.getBill();
                mRates = mBill.getRates();
                MobilePrinter mp = MobilePrinter.getInstance(this);
                String a_class = mAccount.getAccountClassification();
                if(a_class.equalsIgnoreCase("RESIDENTIAL") || a_class.equalsIgnoreCase("Residential")) {
                    a_class = "Res";
                } else if(a_class.equalsIgnoreCase("Lower Voltage")) {
                    a_class = "LV";
                }else if (a_class.equalsIgnoreCase("Higher Voltage")) {
                    a_class = "HV";
                }

    //            mp.printText("           Negros Oriental II Electric Cooperative\n");
    //            mp.printText("                  Real St., Dumaguete City\n");
    //            mp.printText("                         (NORECO2)\n");
    //            mp.printText("                   STATEMENT OF ACCOUNT\n");
    //            mp.printText("================================================================\n");
                String path = CommonFunc.getPrivateAlbumStorageDir(this,"noreco_logo.bmp").toString();
                mp.printBitmap(path);
                mp.printText("\n");
                mp.printText("Meter No:" + mAccount.getMeterSerialNo(), "Type:" + a_class +"\n");
                mp.printTextBoldRight("Account No:", mAccount.getAccountID());
                mp.printTextExceptLeft("Account No:"+ mAccount.getAccountID(),"BillMonth:" + CommonFunc.monthAbrev(mAccount.getBillMonth())+"\n");
                mp.printTextBoldRight("Account Name:",name+"\n");
            mp.printText("Address:"+ mAccount.getAddress()+"\n");
            mp.printText("Period Covered: "+ CommonFunc.changeDateFormat(mAccount.getLastReadingDate()) + " to " + CommonFunc.changeDateFormat(mAccount.getDateRead()) +"\n");
            mp.printText("Due Date: "+mAccount.getDueDate()+"\n");//
            mp.printText("Meter Reader:" + MainActivity.reader.getReaderName()+"\n");
            mp.printText("Multiplier:" + mAccount.getMultiplier()+"\n");
            mp.printText("Consumption:" + mAccount.getActualConsumption()+"\n");
            mp.printText("Coreloss:" + mAccount.getCoreloss()+"\n");
            if(isNetMetering.equalsIgnoreCase("1")) {
                mp.printText("Net-Metering Customer - IMPORT BILL\n");
            }
            mp.printText("--------------------------------------------------------------"+"\n");
            mp.printText("Date              Prev                 Pres              KWH"+"\n");
            mp.printText(mAccount.getDateRead()
                    + "         " + initialRead
                    + "                " + mAccount.getReading()
                    + "          " + mAccount.getConsume()+"\n");
            mp.printText("--------------------------------------------------------------"+"\n");

            /**
             * NOTE
             cursorRateSegment is defined in MainActivity, and was assign in ViewDetails class
             * */
            if(!isMotherMeter) {
                if (listRateSegment.size() > 0) {
                    for (RateSegmentModel seg : listRateSegment) {
                        String segmentName = seg.getRateSegmentName();
                        String rateSegmentCode = seg.getRateSegmentCode();

                        /** print here */
                        if (!segmentName.equalsIgnoreCase("FIT-ALL")) {
                            mp.printText(segmentName + "\n");
                        }


                        for (Rates r : mRates) {
                            if (r.getRateSegment().equals(rateSegmentCode)) {
                                String codeName = r.getCodeName();
                                String rateAmount = String.valueOf(r.getRateAmount());
                                String amount = r.getAmount();

                                int padding = 20 - rateAmount.length() - amount.length();
                                String paddingChar = " ";
                                for (int p = 0; p < padding; p++) {
                                    paddingChar = paddingChar.concat(" ");
                                }
                                String rightText = rateAmount + paddingChar + amount;

                                if (codeName.contains("VAT on")) {
                                    vatListCode.add(codeName);
                                    vatListValue.add(rightText);
                                }

                                /** print here */
                                if (codeName.equalsIgnoreCase("Subsidy on Lifeline")) {

                                    if (canAvailLifelineDiscount) {
                                        mp.printText("  Lifeline Discount(R)", "-" + MainActivity.dec2.format(Double.valueOf(mAccount.getTotalLifeLineDiscount())) + "\n");
                                    } else {
                                        mp.printText("  " + codeName, rightText + "\n");
                                    }
                                }

                                if (codeName.equalsIgnoreCase("Senior Citizens Subsidy")) {
                                    if (canAvailSCDiscount) {
                                        mp.printText("  Senior Citizens Discount(R)", "-" + MainActivity.dec2.format(Double.valueOf(mAccount.getTotalSCDiscount())) + "\n");
                                    } else {
                                        mp.printText("  " + codeName, rightText + "\n");
                                    }
                                }


                                if (!codeName.equalsIgnoreCase("Subsidy on Lifeline") &&
                                        !codeName.equalsIgnoreCase("Senior Citizens Subsidy") &&
                                        !codeName.contains("VAT on")) {
                                    mp.printText("  " + codeName, rightText + "\n");
                                }

                                if (isNetMetering.equalsIgnoreCase("1")) {

                                    String amountExport = String.valueOf(r.getAmountDueExport());
                                    if (r.getIsExport().equalsIgnoreCase("1") || r.getIsExport().equalsIgnoreCase("Yes")) {
                                        int padding1 = 20 - rateAmount.length() - amountExport.length();
                                        String paddingChar1 = " ";
                                        for (int p = 0; p < padding1; p++) {
                                            paddingChar1 = paddingChar1.concat(" ");
                                        }
                                        String rightText1 = rateAmount + paddingChar1 + amountExport;
                                        rateComponentForExport.add(codeName);
                                        exportRateDueAmount.add(rightText1);
                                    }
                                }
                            }
                        }
                    } // end loop
                }

                mp.printText("VAT Charges" + "\n");
                for (int i = 0; i < vatListCode.size(); i++) {
                    mp.printText("  " + vatListCode.get(i).toString(), vatListValue.get(i).toString() + "\n");
                }

                mp.printTextEmphasized("Total Current Due", MainActivity.dec2.format(mBill.getTotalAmount()));
                mp.printText("", "" + "\n");
                mp.printText("Add:Penalty:", MainActivity.dec2.format(Double.valueOf(penalty)) + "\n");
                mp.printText("Arrears:", MainActivity.dec2.format(Double.valueOf(mAccount.getPrevBilling())) + "\n");
                mp.printText("Less:Advance Payment:", MainActivity.dec2.format(Double.valueOf(mAccount.getAdvancePayment())) + "\n");
                mp.printTextEmphasized1("TOTAL AMOUNT PAYABLE", MainActivity.dec2.format(mBill.getTotalBilledAmount()));
                mp.printText("", "" + "\n");
                mp.printText("", "" + "\n");
                mp.printText("", "" + "\n");
                mp.printText("", "" + "\n");
            }


            if(isNetMetering.equalsIgnoreCase("1")) {
                mp.printText("EXPORT BILL\n");
                mp.printText("--------------------------------------------------------------"+"\n");
                mp.printText("Date              Prev                 Pres              KWH"+"\n");
                mp.printText(mAccount.getDateRead()
                        + "         " + mAccount.getExportPreviousReading()
                        + "                " + mAccount.getExportReading()
                        + "          " + mAccount.getExportConsume()+"\n");
                mp.printText("--------------------------------------------------------------"+"\n");

                if(!isMotherMeter) {
                    mp.printText("Customer Charge to DU\n");
                    for (int i = 0; i < rateComponentForExport.size(); i++) {
                        mp.printText("  " + rateComponentForExport.get(i), exportRateDueAmount.get(i) + "\n");
                    }
                    mp.printTextEmphasized("Amount Export Due", MainActivity.dec2.format(mBill.getTotalAmountDueExport()));

                    mp.printText("\n");
                    mp.printText("\n");
                    mp.printTextEmphasized("NET BILL AMOUNT", MainActivity.dec2.format(mBill.getNetBillAmountExport()));
                    mp.printText("\n");
                    mp.printText("\n");
                    mp.printText("\n");
                    mp.printText("\n");
                }
            }

            db.updateAccountToPrinted(db,"Printed");

            }catch (NullPointerException e) {
                Log.e(TAG,"preparePrint : "+ e.getMessage());
            }
        }
    }
