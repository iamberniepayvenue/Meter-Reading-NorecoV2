    package com.payvenue.meterreader;

    import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bixolon.printer.BixolonPrinter;
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
import Model.Thresholds;
import Utility.BixolonPrinterClass;
import Utility.CommonFunc;
import Utility.Constant;
import Utility.MobilePrinter;
import Utility.MyProgressBar;
import ZBar.ZBarConstants;

import static com.payvenue.meterreader.Fragments.FragmentReading.ZBAR_SCANNER_REQUEST;
import static com.payvenue.meterreader.MainActivity.whichPrinter;


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
        boolean isStopMeter = true;
        double maxreadingvalue = 0;
        String strReading = "", strRemarks = "", strDemands = "";
        double rateMultiplier,exportMultiplier;
        double multiplier,totalAmountDueExport = 0,netBillAmountExport = 0;
        double scPercentage;// = (float) 0.05;
        double componentvat, componentltax, componentftax,lifelineDiscountRate,scDiscountedAmount,
                lifelineDiscountAmount,totalLifelineDiscount,totalComponent, billedAmount;
        double componentAmount;
        double scSubsidy = 0;
        double sol = 0;
        double overUnder = 0;
        double totalSeniorDiscount;
        double demandKWMininum;
        double totalArrears = 0,arrearsPenalty = 0;
        boolean canAvailLifelineDiscount = false, canAvailSCDiscount = false;
        boolean scInvalidDate = false;
        boolean isSCOverPolicy = false;
        ArrayList<Rates> myRates = new ArrayList<>();
        ArrayList<RateSegmentModel> listRateSegment = new ArrayList<>();
        ArrayList<Policies> policiesArrayList = new ArrayList<>();
        ArrayList<String> arrearsAmountList = new ArrayList<>();
        ArrayList<String> arrearsBillMonthList = new ArrayList<>();
        ArrayList<String> arrearsPenaltyList = new ArrayList<>();
        ArrayList<String> arrearsBillNumberList = new ArrayList<>();
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
        private Account searchAccount;
        private boolean isHigherVoltage = false;
        private boolean isLowerVoltage = false;
        private boolean isMotherMeter = false;
        private boolean isSearch = false;

        private MyProgressBar myProgressBar;
        BixolonPrinterClass bp;


        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.account_details);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            Log.e(TAG,TAG);

            db = new DataBaseHandler(this);


            try {
                imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            } catch (Exception e) {
                // TODO: handle exception
            }

            mcontext = this;
            myProgressBar = MyProgressBar.newInstance(mcontext);

            /**Initalize Rate Segment*/
            listRateSegment = db.getRateSegment(db);

            if (!MainActivity.gps.canGetLocation()) {
                MainActivity.gps.showSettingAlert();
            }

            mAccount =  MainActivity.selectedAccount;
            a_class = mAccount.getAccountClassification();

            if(mAccount.getIsCheckSubMeterType().toLowerCase().equalsIgnoreCase("m")) {
                isMotherMeter = true;
            }

            bp = MainActivity.bp;

            if(a_class.toLowerCase().contains("higher")) {
                isHigherVoltage = true;
            }

            if(a_class.toLowerCase().contains("lower")) {
                isLowerVoltage = true;
            }

            simplifyArrears();

            if(mAccount.getMultiplier().equalsIgnoreCase(".") ||
                    mAccount.getMultiplier().equalsIgnoreCase("")) {
                multiplier = 0;
            }else{
                multiplier = Float.valueOf(mAccount.getMultiplier());
            }


            isNetMetering = mAccount.getIsNetMetering();

            policiesArrayList = db.getBillingPolicy(db,a_class);

            scPercentage = getSCDiscount();

            initViews();
            setValues();

        }

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
            if(!isSearch) {
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
            }else{
                searchAccount = MainActivity.selectedAccount;
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

            double totalLifelineComponentAmount = 0;

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


            /**Check Senior Status Discount*/
            if (mAccount.getSeniorCitizenStatus().equals("1") && a_class.toLowerCase().equalsIgnoreCase("residential") ) {
                    if(CommonFunc.isValidDate(mAccount.getSCExpiryDate())) {
                        String consumption = mAccount.getConsume();
                        int scPolicyMax = getSCDMaxPolicy();
                        int scPolicyMin = getSCDMinPolicy();

                        if(scPolicyMax == -1 || scPolicyMin == -1 ) {
                            Toast.makeText(this,"No Policy for Senior... cannot process",Toast.LENGTH_LONG).show();
                            return;
                        }


                        if(Float.valueOf(consumption) >= (float) scPolicyMin && Float.valueOf(consumption) <= (float) scPolicyMax) {
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
                showToast("No rate schedule created for this classification");
                return;
            }


            RateSchedule rateSchedule;

            billMonth =  db.getBillMonth(db,a_class);
            String []strArray = billMonth.split("/");
            mAccount.setBillMonth(CommonFunc.monthAbrev(strArray[0]));
            mAccount.setDateRead(CommonFunc.getDateOnly());
            mAccount.setTimeRead(CommonFunc.getTimeNow());

            String strConsume = mAccount.getConsume();
            rateMultiplier =  Double.parseDouble(strConsume); //flConsume;
            rateMultiplier = CommonFunc.roundOff(rateMultiplier,2);

            /**STOP METER
             *
             *  isStopMeter will be false if reading is equal to prev. reading
             *  if pressing ok button from alert dialog..
             *
             * */
            if(isStopMeter) {
                if (isStopCheck) {
                    float av = getAveraging();
                    if (av != -2000) {
                        double presentRead = CommonFunc.roundOff(Double.parseDouble(mAccount.getReading()), 1);
                        double average = (av + presentRead) - Double.parseDouble(initialRead);
                        double presreading = av + presentRead;
                        rateMultiplier = (average) * multiplier + Float.valueOf(coreLoss);
                        mAccount.setReading(String.valueOf(CommonFunc.round(presreading, 1)));
                        mAccount.setConsume(String.valueOf(CommonFunc.roundOff(rateMultiplier, 1)));
                        mAccount.setActualConsumption(String.valueOf(CommonFunc.round(average, 1)));
                    }
                }
            }

            /**Check Lifeliner*/
            if (a_class.toLowerCase().equalsIgnoreCase("residential")) {

                lifelineDiscountRate = getLifeLinerPercentage(rateMultiplier);
                if(lifelineDiscountRate > 0) {
                    canAvailLifelineDiscount = true;
                }
            }

            /**
             *  Check Mother meter
             *
             * */
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


                    if(isHigherVoltage) {
                        int fixed = 0;
                        if (rateSchedule.getRateComponent().equalsIgnoreCase("Supply Retail Customer Charge") || rateSchedule.getRateComponent().equalsIgnoreCase("Supply System Charge") || rateSchedule.getRateComponent().equalsIgnoreCase("Metering Retail Customer Charge")) {
                            componentAmount = rateSchedule.getComponentRate();
                            strComponentAmount = String.valueOf((float) componentAmount);
                            fixed = 1;
                        }


                        if (fixed == 0) {

                            String _rateType = rateSchedule.getRateType().toUpperCase();
                            if (_rateType.equalsIgnoreCase("PERKW") || _rateType.toLowerCase().equalsIgnoreCase("PKW") || _rateType.toLowerCase().equalsIgnoreCase("P/KW")) {
                                demandKWMininum = Double.valueOf(mAccount.getDemandKW());
                                demandKWMininum = CommonFunc.roundOff(demandKWMininum,2);
                                componentAmount =  CommonFunc.calcComponentAmount(Double.parseDouble(Amount),demandKWMininum);
                                strComponentAmount = String.valueOf(componentAmount);
                            }else{
                                /**
                                 *  rateMultiplier is consumption with coreloss and multiplier
                                 * */
                                componentAmount = CommonFunc.calcComponentAmount(Double.parseDouble(Amount), rateMultiplier);
                                strComponentAmount = String.valueOf(componentAmount);
                            }
                        }

                        if(fixed == 1) {
                            componentAmount =  CommonFunc.roundOff(Double.valueOf(strComponentAmount),4);
                        }

                        if (mAccount.getUnderOverRecovery().equalsIgnoreCase("0") &&
                                rateSchedule.getIsOverUnder().equalsIgnoreCase("Yes")) {
                            componentAmount = 0;
                            strComponentAmount = String.valueOf(componentAmount);
                        }

                        if (mAccount.getUnderOverRecovery().equalsIgnoreCase("1") &&
                                rateSchedule.getIsOverUnder().equalsIgnoreCase("Yes")) {
                            overUnder = overUnder + componentAmount;
                        }

                        /**record senior subsidy*/
                        if (rateSchedule.getRateCode().equalsIgnoreCase("SCS")) {
                            scSubsidy = componentAmount;
                        }

                        /**record lifeline subsidy*/
                        if (rateSchedule.getRateCode().equalsIgnoreCase("SOL")) {
                            sol = componentAmount;
                        }

                    }else{ // end of higher voltage
                        int fixed = 0;
                        if (isLowerVoltage) {
                            if (rateSchedule.getRateComponent().equalsIgnoreCase("Supply Retail Customer Charge") || rateSchedule.getRateComponent().equalsIgnoreCase("Supply System Charge") || rateSchedule.getRateComponent().equalsIgnoreCase("Metering Retail Customer Charge")) {
                                componentAmount = rateSchedule.getComponentRate();
                                strComponentAmount = String.valueOf((float) componentAmount);
                                fixed = 1;
                            }
                        } else {
                            if (rateSchedule.getRateComponent().equalsIgnoreCase("Metering Retail Customer Charge")) {
                                componentAmount = rateSchedule.getComponentRate();
                                strComponentAmount = String.valueOf(componentAmount);
                                fixed = 1;
                            }
                        }


                        if (fixed == 0) {
                            componentAmount = CommonFunc.calcComponentAmount(Double.parseDouble(Amount), rateMultiplier);
                            strComponentAmount = String.valueOf(componentAmount);
                        }

                        if(fixed == 1) {
                            componentAmount = CommonFunc.roundOff(Double.valueOf(strComponentAmount),4);
                        }

                        if (canAvailSCDiscount && rateSchedule.getIsSCDiscount().equalsIgnoreCase("Yes")) {
                            scDiscountedAmount = componentAmount * scPercentage;
                            totalSeniorDiscount = totalSeniorDiscount + scDiscountedAmount;
                        }

                        if (canAvailLifelineDiscount && rateSchedule.getIsLifeline().equalsIgnoreCase("Yes")) {
                            lifelineDiscountAmount = componentAmount * lifelineDiscountRate;
                            totalLifelineComponentAmount = totalLifelineComponentAmount + componentAmount;
                            totalLifelineDiscount = totalLifelineDiscount + lifelineDiscountAmount;
                        }

                        /** Under Over Recovery*/
                        if (mAccount.getUnderOverRecovery().equalsIgnoreCase("0") &&
                                rateSchedule.getIsOverUnder().equalsIgnoreCase("Yes")) {
                            componentAmount = 0;
                            strComponentAmount = String.valueOf(componentAmount);
                        }

                        /**Compute Over Under Discount*/
                        if (mAccount.getUnderOverRecovery().equalsIgnoreCase("1") &&
                                rateSchedule.getIsOverUnder().equalsIgnoreCase("Yes")) {
                            overUnder = overUnder + componentAmount;
                        }

                        if (canAvailLifelineDiscount && rateSchedule.getRateCode().equalsIgnoreCase("SOL")) {
                            componentAmount = 0;
                            strComponentAmount = String.valueOf(componentAmount);
                        }

                        if (canAvailLifelineDiscount && (rateSchedule.getRateCode().equalsIgnoreCase("ICCS")
                                || rateSchedule.getRateCode().equalsIgnoreCase("OLRA")
                                || rateSchedule.getRateCode().equalsIgnoreCase("SCS"))) {
                            componentAmount = 0;
                            strComponentAmount = String.valueOf(componentAmount);
                        }

                        if ((scInvalidDate || isSCOverPolicy) && (rateSchedule.getRateCode().equalsIgnoreCase("SCS"))) {
                            componentAmount = 0;
                            strComponentAmount = String.valueOf(componentAmount);
                        }

                        if (canAvailSCDiscount && rateSchedule.getRateCode().equalsIgnoreCase("SCS")) {
                            componentAmount = 0;
                            strComponentAmount = String.valueOf(componentAmount);
                        }


                        /**record senior subsidy*/
                        if (rateSchedule.getRateCode().equalsIgnoreCase("SCS")) {
                            scSubsidy = componentAmount;
                        }

                        /**record lifeline subsidy*/
                        if (rateSchedule.getRateCode().equalsIgnoreCase("SOL")) {
                            sol = componentAmount;
                        }
                    } /**End If*/



                    totalComponent = totalComponent + componentAmount;
                    double amountDueExport = 0;

                    /**FOR NET METERING*/
                    if (isNetMetering.equalsIgnoreCase("1")) {
                        exportMultiplier = Float.valueOf(mAccount.getExportConsume());
                        if (rateSchedule.getIsExport().equalsIgnoreCase("1") || rateSchedule.getIsExport().equalsIgnoreCase("Yes")) {
                            amountDueExport = CommonFunc.calcComponentAmount(Double.parseDouble(Amount), exportMultiplier);

                            if (rateSchedule.getRateComponent().toLowerCase().equalsIgnoreCase("generation system charges")) {
                                amountDueExport = CommonFunc.calcComponentAmount(Double.parseDouble(Amount), exportMultiplier);
                            }

                            if (rateSchedule.getRateComponent().toLowerCase().equalsIgnoreCase("metering system charge")) {
                                amountDueExport = -CommonFunc.calcComponentAmount(Double.parseDouble(Amount), exportMultiplier);
                            }

                            if (rateSchedule.getRateComponent().toLowerCase().equalsIgnoreCase("metering retail customer charge")) {
                                amountDueExport = -rateSchedule.getComponentRate();
                            }

                            if (rateSchedule.getRateComponent().toLowerCase().equalsIgnoreCase("supply retail customer charge")) {
                                amountDueExport = -rateSchedule.getComponentRate();
                            }

                            totalAmountDueExport = totalAmountDueExport + amountDueExport;
                        }
                    }

                    strComponentAmount = String.valueOf(CommonFunc.roundOff(Double.parseDouble(strComponentAmount),4));
                    myRates.add(new Rates(rateSchedule.getRateSegment(),
                            rateSchedule.getRateCode(),
                            rateSchedule.getRateComponent(),
                            Amount,rateSchedule.getIsLifeline(),rateSchedule.getIsSCDiscount(),rateSchedule.getIsExport(),strComponentAmount,amountDueExport)); //componentvat, componentftax, componentltax,

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

                totalComponent = totalComponent - (totalLifelineDiscount + totalSeniorDiscount);
                billedAmount =  totalComponent + totalArrears + arrearsPenalty;

                /** Not included */
            /*
            + CommonFunc.toDigit(mAccount.getPoleRental())
                    + CommonFunc.toDigit(mAccount.getSpaceRental()
                            + CommonFunc.toDigit(mAccount.getPilferagePenalty())
                    */

                /** billedAmount
                 *          is the total computation of payment included arrears,penalty, and advance payment
                 * */

                billedAmount =  billedAmount - Double.valueOf(mAccount.getAdvancePayment());

                if (isNetMetering.equalsIgnoreCase("1")) {
                    if(totalAmountDueExport < 0) {
                        netBillAmountExport = totalComponent + totalAmountDueExport;
                    }else{
                        netBillAmountExport = totalComponent - totalAmountDueExport;
                    }
                }


                double exportbill = 0;
                int exportCounter = Integer.valueOf(mAccount.getExportDateCounter());
                if(exportCounter < 12) {
                    if (mAccount.getExportBill() != null) {
                        if (mAccount.getExportBill().equalsIgnoreCase("")) {
                            exportbill = 0;
                        } else {
                            exportbill = Double.parseDouble(mAccount.getExportBill());
                        }
                    }

                    if (netBillAmountExport < 0) {
                        netBillAmountExport = netBillAmountExport + exportbill;
                    } else {
                        netBillAmountExport = netBillAmountExport + exportbill;
                    }
                }

                mBill = new Bill(myRates, CommonFunc.roundOff(totalComponent,2), CommonFunc.roundOff(billedAmount,2),netBillAmountExport,totalAmountDueExport);
                mAccount.setBill(mBill);
            }

            mAccount.setLatitude("" + MainActivity.gps.getLatitude());
            mAccount.setLongitude("" + MainActivity.gps.getLongitude());
            mAccount.setTotalSCDiscount(String.valueOf(totalSeniorDiscount));
            mAccount.setTotalLifeLineDiscount(String.valueOf(totalLifelineDiscount));
            mAccount.setOverUnderDiscount(String.valueOf(overUnder));
            mAccount.setLifeLineSubsidy(String.valueOf(sol));
            mAccount.setSeniorSubsidy(String.valueOf(scSubsidy));

            MainActivity.db.updateReadAccount(MainActivity.db, "Read",isStopCheck);
        }

        private void simplifyArrears(){

            try {
                JSONArray jsonArray = new JSONArray(mAccount.getArrears());
                for(int i = 0; i < jsonArray.length();i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    arrearsPenalty = arrearsPenalty + Double.valueOf(jsonObject.getString("Penalty"));
                    totalArrears = totalArrears + Double.valueOf(jsonObject.getString("BillAmount"));
                    arrearsAmountList.add(jsonObject.getString("BillAmount"));
                    arrearsBillMonthList.add(jsonObject.getString("BillMonth"));
                    arrearsPenaltyList.add(jsonObject.getString("Penalty"));
                    arrearsBillNumberList.add(jsonObject.getString("BillNo"));
                }

                mAccount.setPenalty(String.valueOf(arrearsPenalty));
                mAccount.setPrevBilling(String.valueOf(totalArrears));

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG,"simplifyArrears:" + e.getMessage());
            }
        }

        private float getSCDiscount() {
            for(Policies p: policiesArrayList) {
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

        private float getLifeLinerPercentage(double consume) {
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

            boolean checked = ((CheckBox) view).isChecked();

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


            final double presReading = CommonFunc.roundOff(Double.parseDouble(mAccount.getReading()),1);
            double consume = CommonFunc.round((maxreadingvalue + presReading - Double.parseDouble(initialRead)), 1);




            if (isHigherVoltage) {
                if (strDemands.isEmpty()) {
                    showToast("Please add Demand Reading");
                    return;
                }

                demandKWMininum = Double.valueOf(strDemands);
                double dm = CommonFunc.roundOff(demandKWMininum * multiplier,2);
                /**Update demandKWMininum*/
                dm = CommonFunc.roundOff(dm,2);
                mAccount.setDemandKW(String.valueOf(dm));
            }

            if(a_class.equalsIgnoreCase("Contestable Class")){
                demandKWMininum = Double.valueOf(mAccount.getDemandKW());
                if(demandKWMininum < Double.valueOf(strDemands)){
                    demandKWMininum = Double.valueOf(strDemands);
                }
            }

            if(!mAccount.getReading().equalsIgnoreCase("0")){
                if (consume < 0) {
                    showToast("Invalid Reading. Current reading is less than the Previous Reading");
                    return;
                }
            }


            /**Check if Change Meter*/

            if (mAccount.getIsChangeMeter().equals("1")) {
                double av = getAveraging();
                if(av != -2000) {
                    consume = av;
                }else{
                    return;
                }
            }


            if(initialRead.equalsIgnoreCase(mAccount.getReading())) {
                final double av = getAveraging();
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Stop Meter");
                builder.setMessage("This is stop meter or not?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        isStopMeter = false;

                        if(av != -2000) {
                            double _av = av + presReading;
                            mAccount.setReading(String.valueOf(CommonFunc.round(_av,1)));
                            setKwh(_av,0);
                        }
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setKwh(0,1);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                setKwh(consume,1);
            }
        }

        public void setKwh(double consume,int tag) {
            double consumption;
            double kwh;

            if(tag == 1) {
                consumption = consume;
            }else{
                consumption = consume - Double.parseDouble(initialRead);
            }

            kwh = consumption * multiplier + Float.valueOf(coreLoss);

            boolean showAlert = false;
            float kWhRead = Float.valueOf(mAccount.getkWhReading());
            if(kWhRead != 0) {
                ArrayList<Thresholds> thresholds =  db.getThreshold(db);
                float lowerThreshold = 0;
                float upperThreshold = 0;
                if(thresholds.size() > 0) {
                    for(Thresholds t: thresholds){
                        if(t.getSettingsCode().toLowerCase().contains("low")) {
                            lowerThreshold = Float.valueOf(t.getThresholdPercentage());
                        }

                        if(t.getSettingsCode().toLowerCase().contains("up")){
                            upperThreshold = Float.valueOf(t.getThresholdPercentage());
                        }
                    }
                }


                float _lowerThreshold = (lowerThreshold/100) * kWhRead;
                float _upperThreshold = (upperThreshold/100) * kWhRead;


                if(kwh < _lowerThreshold) {
                    showAlert((float) consume,kWhRead,"lower");
                    showAlert = true;
                }

                if(kwh > _upperThreshold) {
                    showAlert((float) consume,kWhRead,"higher");
                    showAlert = true;
                }
            }



            mAccount.setConsume(String.valueOf(CommonFunc.roundOff(kwh,1)));
            mAccount.setActualConsumption(String.valueOf(CommonFunc.roundOff(consumption,1)));

            if(!showAlert) {
                forNetMetering();
            }
        }

        public void forNetMetering(){
            if (isNetMetering.equalsIgnoreCase("1")) {
                android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                MyDialogFragment dialogFragment = new MyDialogFragment();
                dialogFragment.mListener = this;
                dialogFragment.show(fm, "MyDialogFragment");
            } else {
                displayButtons();
            }
        }

        public void showAlert(float consumption,float kWhRead,String tag) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setIcon(android.R.drawable.ic_dialog_info);
            alertDialog.setMessage("Consumption("+consumption+" kwh) seems to be " + tag + " than previous consumption "+kWhRead + "kwh");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    forNetMetering();
                }
            });

            AlertDialog dialog = alertDialog.create();
            dialog.show();
        }

        @Override
        public void onMyDialogDismiss(int tag) {
            if(tag == 0) {
                double consumeExport = CommonFunc.round((maxreadingvalue + Double.parseDouble(mAccount.getExportReading())) - Double.parseDouble(mAccount.getExportPreviousReading()), 2);
                double kwhExport = consumeExport * multiplier;
                mAccount.setExportConsume(String.valueOf(kwhExport));
                mAccount.setActualExportConsume(String.valueOf(consumeExport));
                displayButtons();
            }
        }

        public float getAveraging() {
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
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
            if(searchView != null) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                searchView.setIconifiedByDefault(false);
                searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                    @Override
                    public boolean onClose() {
                        return false;
                    }
                });
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        return false; //do the default
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        //NOTE: doing anything here is optional, onNewIntent is the important bit
                        if (s.length() > 1) {
                            MainActivity.db.getAccountDetails(MainActivity.db, s,2);
                            isSearch = true;
                            setValues();

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
                        isSearch = false;
                        setValues();

                        int searchCloseButtonId = searchView.getContext().getResources()
                                .getIdentifier("android:id/search_src_text", null, null);
                        EditText et = (EditText) findViewById(searchCloseButtonId);
                        et.setText("");

                        //Clear query
                        searchView.setQuery("", false);
                    }
                });
            }
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
        protected void onNewIntent(Intent intent) {
            setIntent(intent);
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
                        myProgressBar.setTitle("Printing process...");
                        preparePrint();
                    } else {
                        showToast("Printer is not connected.");
                    }

                    /** This part is for sequencing account*/
//                    String route = mAccount.getRouteNo();
//                    String sequenceNumber = mAccount.getSequenceNo();
//                    try{
//                        if(sequenceNumber.equalsIgnoreCase(".") || sequenceNumber == null){
//                            showToast("Sequence number is not a number, please check");
//                            return;
//                        }
//                    } catch (NullPointerException e) {
//                        showToast("Sequence number is not a number, please check");
//                        return;
//                    }
//
//                    int count = db.searchNextAccountToRead(db,route,sequenceNumber,mAccount.getAccountID());
//                    if (count == 0) {
//                            this.finish();
//                    }
                    //Intent intent = new Intent(this, BillPreview.class);
                    db.getAccountDetails(MainActivity.db, mAccount.getAccountID(),1);
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
//                        if (mReading.getText().toString().matches("^0")) {
//                            //mReading.setText("");
//                        } else {
//                            strReading = mReading.getText().toString();
//                        }
                        strReading = mReading.getText().toString();
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
                            if (mAccount.getAccountClassification().toLowerCase().equalsIgnoreCase("higher voltage")) {
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
                            if (mAccount.getAccountClassification().toLowerCase().equalsIgnoreCase("higher voltage")) {
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

        public String segmentName(ArrayList<String> duplicateSegmentName,String segmentName){
            String name = "";
            if(duplicateSegmentName.size() > 0) {
                for(String segName: duplicateSegmentName) {
                    if(segmentName.equalsIgnoreCase(segName)){
                        return segmentName;
                    }
                }
            }

            return name;
        }

//        public void printLogoBix() {
//            BixolonPrinterClass.newInstance(this).printBitmap(this);
//        }

        public void preparePrint() {
            DecimalFormat df = new DecimalFormat("#.####");
            ArrayList<String> vatListCode = new ArrayList<>();
            ArrayList<String> vatListValue = new ArrayList<>();
            ArrayList<String> rateComponentForExport = new ArrayList<>();
            ArrayList<String> exportRateDueAmount = new ArrayList<>();
            ArrayList<String> duplicateSegmentName = new ArrayList<>();
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

            String []date = mAccount.getDateRead().split(" ");
            String dateRead = date[0];

            try{
                List<Rates> mRates = null;
                if(!isMotherMeter) {
                    Bill mBill = mAccount.getBill();
                    mRates = mBill.getRates();
                    //Log.e(TAG,"mRates: "+ mRates.toString());
                    /**Check duplicate name*/
                    if (listRateSegment.size() > 0) {
                        for (RateSegmentModel seg : listRateSegment) {
                            for(Rates r : mRates){
                                String segmentName = seg.getRateSegmentName();
                                String codeName = r.getCodeName();
                                if(segmentName.equalsIgnoreCase(codeName)){
                                    duplicateSegmentName.add(segmentName);
                                }
                            }
                        }
                    }
                }


                String a_class = mAccount.getAccountClassification();
                if(a_class.toLowerCase().contains("residential")) {
                    a_class = "Res";
                } else if(a_class.toLowerCase().contains("lower")) {
                    a_class = "LV";
                } else if (a_class.toLowerCase().contains("higher")) {
                    a_class = "HV";
                }

    //            mp.printText("           Negros Oriental II Electric Cooperative\n");
    //            mp.printText("                  Real St., Dumaguete City\n");
    //            mp.printText("                         (NORECO2)\n");
    //            mp.printText("                   STATEMENT OF ACCOUNT\n");
    //            mp.printText("================================================================\n");


            MobilePrinter mp = null;
            int mPrinter;
                if(whichPrinter.equalsIgnoreCase("woo")) {
                    mPrinter = 0;
                }else if (whichPrinter.equalsIgnoreCase("bix")) {
                    mPrinter = 1;
                }else {
                    mPrinter = 0;
                }
                if(mPrinter != 1){
                    mp = MainActivity.printer;
                    if(mp == null) {
                        mp = MobilePrinter.getInstance(this);
                    }

                    String path = CommonFunc.getPrivateAlbumStorageDir(this,"noreco_logo.bmp").toString();
                    mp.printBitmap(path);
                }else {
                    bp.printBitmap();
                }

//            mp.printText("\n");
//            mp.printextEmphasized("Account No:"+ mAccount.getAccountID()+"\n");
//            mp.printextEmphasized(name+"\n");
//            mp.printextEmphasizedNormalFont(mAccount.getAddress()+"\n");
//            mp.printText("Meter No:" + mAccount.getMeterSerialNo()+"\n");
//            mp.printText("Period Covered: "+ CommonFunc.changeDateFormat(mAccount.getLastReadingDate()) + " to " + CommonFunc.changeDateFormat(dateRead) +"\n");
//            mp.printText("Due Date: "+mAccount.getDueDate()+"\n");//
//            mp.printText("Meter Reader:" + MainActivity.reader.getReaderName()+"\n");
//            mp.printText("Multiplier:" + mAccount.getMultiplier(), "Consumer Type:" + a_class +"\n");
//            mp.printText("Consumption:" + mAccount.getActualConsumption(),"BillMonth:" + mAccount.getBillMonth()+"\n")

                CommonFunc.printingNormal("\n","",0,0,0,mPrinter);
                CommonFunc.printingNormal("Account No:"+ mAccount.getAccountID()+"\n","",1,0,0,mPrinter);
                CommonFunc.printingNormal(name+"\n","",1,0,0,mPrinter);
                CommonFunc.printingNormal(mAccount.getAddress()+"\n","",0,0,1,mPrinter);
                CommonFunc.printingNormal("Meter No:" + mAccount.getMeterSerialNo()+"\n","",0,0,0,mPrinter);
                CommonFunc.printingNormal("Period Covered: "+ CommonFunc.changeDateFormat(mAccount.getLastReadingDate()) + " to " + CommonFunc.changeDateFormat(dateRead) +"\n","",0,0,0,mPrinter);
                CommonFunc.printingNormal("Due Date: "+mAccount.getDueDate()+"\n","",0,0,0,mPrinter);
                CommonFunc.printingNormal("Meter Reader:" + MainActivity.reader.getReaderName()+"\n","",0,0,0,mPrinter);
                CommonFunc.printingNormal("Multiplier:" + mAccount.getMultiplier(),"Consumer Type:" + a_class+"\n",0,0,0,mPrinter);
                CommonFunc.printingNormal("Consumption:" + mAccount.getActualConsumption(),"BillMonth:" + mAccount.getBillMonth()+"\n",0,0,0,mPrinter);
            if(isHigherVoltage) {
                CommonFunc.printingNormal("Coreloss:" + mAccount.getCoreloss(),"DemandKW:"+mAccount.getDemandKW()+"\n",0,0,0,mPrinter);
                //mp.printText("Coreloss:" + mAccount.getCoreloss(),"DemandKW:"+mAccount.getDemandKW()+"\n");
            }else{
                CommonFunc.printingNormal("Coreloss:" + mAccount.getCoreloss()+"\n","",0,0,0,mPrinter);
                //mp.printText("Coreloss:" + mAccount.getCoreloss()+"\n");
            }

            if(isNetMetering.equalsIgnoreCase("1")) {
                CommonFunc.printingNormal("Net-Metering Customer - IMPORT BILL\n","",0,0,0,mPrinter);
                //mp.printText("Net-Metering Customer - IMPORT BILL\n");
            }

                if(mPrinter == 1) {
                    bp.printText("------------------------------------------------" + "\n",BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                    bp.printText("Date          Prev            Pres           KWH" + "\n",BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                }else{
                    mp.printText("--------------------------------------------------------------" + "\n");
                    mp.printText("Date                Prev                 Pres              KWH" + "\n");
                }

                int padding = 20 - dateRead.length() - mAccount.getConsume().length();
                String spacing = " ";
                for (int p = 0; p < padding; p++) {
                    spacing = spacing.concat(" ");
                }
                String strRight = mAccount.getReading() + spacing + mAccount.getConsume();
                int paddingLeft = 20 - dateRead.length() - mAccount.getInitialReading().length();
                String _spacing = " ";
                for (int p = 0; p < paddingLeft; p++) {
                    _spacing = _spacing.concat(" ");
                }
                String strLeft = dateRead + _spacing +mAccount.getInitialReading();
                CommonFunc.printingNormal(strLeft,strRight+"\n",0,0,0,mPrinter);
                if(mPrinter == 1) {
                    bp.printText("------------------------------------------------" + "\n",BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                }else{
                    mp.printText("--------------------------------------------------------------" + "\n");
                }

            /**
             * NOTE
             cursorRateSegment is defined in MainActivity, and was assign in ViewDetails class
             * */
            if(!isMotherMeter) {
                if (listRateSegment.size() > 0) {
                    for (RateSegmentModel seg : listRateSegment) {
                        String segmentName = seg.getRateSegmentName();
                        String rateSegmentCode = seg.getRateSegmentCode();


                        /** Avoid printing same segment name*/

                        if(duplicateSegmentName.size() > 0) {
                            for(String s : duplicateSegmentName) {
                                if (!s.equalsIgnoreCase(segmentName)) {
                                    CommonFunc.printingNormal(segmentName + "\n","",0,0,0,mPrinter);
                                }
                            }
                        }else{
                            CommonFunc.printingNormal(segmentName + "\n","",0,0,0,mPrinter);
                        }


                        for (Rates r : mRates) {
                            if (r.getRateSegment().equals(rateSegmentCode)) {
                                String codeName = r.getCodeName();
                                String rateComponent = r.getCode();
                                @SuppressLint("DefaultLocale")
                                String rateAmount = String.format("%.4f",Float.valueOf(r.getRateAmount()));
                                String amount = df.format(Double.parseDouble(r.getAmount()));

                                int padding2 = 20 - rateAmount.length() - amount.length();
                                String paddingChar2 = " ";
                                for (int p = 0; p < padding2; p++) {
                                    paddingChar2 = paddingChar2.concat(" ");
                                }

                                String rightText = rateAmount + paddingChar2 + amount;

                                if (codeName.contains("VAT on")) {
                                    if(whichPrinter.equalsIgnoreCase("bix")) {
                                        if(codeName.length() > 18) {
                                            vatListCode.add(rateComponent);
                                        }else {
                                            vatListCode.add(codeName);
                                        }
                                    }else {
                                        vatListCode.add(codeName);
                                    }

                                    vatListValue.add(rightText);
                                }

                                /** print here */
                                if (codeName.equalsIgnoreCase("Subsidy on Lifeline")) {

                                    if (canAvailLifelineDiscount) {
                                        CommonFunc.printingNormal("  Lifeline Discount(R)", "-" + MainActivity.dec2.format(Double.valueOf(mAccount.getTotalLifeLineDiscount())) + "\n",0,0,0,mPrinter);
                                    } else {
                                        if(mPrinter == 1) {
                                            if (codeName.length() > 18) {
                                                CommonFunc.printingNormal("  " + rateComponent, rightText + "\n",0,0,0,mPrinter);
                                            } else {
                                                CommonFunc.printingNormal("  " + codeName, rightText + "\n",0,0,0,mPrinter);
                                            }
                                        }else {
                                            CommonFunc.printingNormal("  " + codeName, rightText + "\n",0,0,0,mPrinter);
                                        }
                                    }
                                }

                                if (codeName.equalsIgnoreCase("Senior Citizens Subsidy")) {
                                    if (canAvailSCDiscount) {
                                        CommonFunc.printingNormal("  Senior Citizens Discount(R)", "-" + MainActivity.dec2.format(Double.valueOf(mAccount.getTotalSCDiscount())) + "\n",0,0,0,mPrinter);
                                    } else {
                                        if(mPrinter == 1) {
                                            if (codeName.length() > 18) {
                                                CommonFunc.printingNormal("  " + rateComponent,rightText + "\n",0,0,0,mPrinter);
                                            } else {
                                                CommonFunc.printingNormal("  " + codeName,rightText + "\n",0,0,0,mPrinter);
                                            }
                                        }else {
                                            CommonFunc.printingNormal("  " + codeName + rateComponent,rightText + "\n",0,0,0,mPrinter);
                                        }
                                    }
                                }


                                if (!codeName.equalsIgnoreCase("Subsidy on Lifeline") &&
                                        !codeName.equalsIgnoreCase("Senior Citizens Subsidy") &&
                                        !codeName.contains("VAT on")) {

                                    String sn = segmentName(duplicateSegmentName,codeName);
                                    if(!sn.equalsIgnoreCase("")){
                                        CommonFunc.printingNormal(codeName,rightText + "\n",0,0,0,mPrinter);
                                    }else{
                                        if(mPrinter == 1){
                                            if(codeName.length() > 18) {
                                                CommonFunc.printingNormal("  " + rateComponent,rightText + "\n",0,0,0,mPrinter);
                                            }else {
                                                CommonFunc.printingNormal("  " + codeName,rightText + "\n",0,0,0,mPrinter);
                                            }
                                        }else {
                                            CommonFunc.printingNormal("  " + codeName,rightText + "\n",0,0,0,mPrinter);
                                        }
                                    }
                                }

                                if (isNetMetering.equalsIgnoreCase("1")) {

                                    String amountExport = df.format((r.getAmountDueExport()));
                                    if (r.getIsExport().equalsIgnoreCase("1") || r.getIsExport().toLowerCase().equalsIgnoreCase("yes")) {
                                        int padding1 = 20 - rateAmount.length() - amountExport.length();
                                        String paddingChar1 = " ";
                                        for (int p = 0; p < padding1; p++) {
                                            paddingChar1 = paddingChar1.concat(" ");
                                        }
                                        String rightText1 = rateAmount + paddingChar1 + amountExport;
                                        if(whichPrinter.equalsIgnoreCase("bix")) {
                                            if(codeName.length() > 18) {
                                                rateComponentForExport.add(rateComponent);
                                            }else {
                                                rateComponentForExport.add(codeName);
                                            }
                                        }else{
                                            rateComponentForExport.add(codeName);
                                        }
                                        exportRateDueAmount.add(rightText1);
                                    }
                                }
                            }
                        }
                    } // end loop
                }

                CommonFunc.printingNormal("VAT Charges" + "\n","",0,0,0,mPrinter);
                for (int i = 0; i < vatListCode.size(); i++) {
                    CommonFunc.printingNormal("  " + vatListCode.get(i),vatListValue.get(i) + "\n",0,0,0,mPrinter);
                }

                CommonFunc.printingNormal("\n","", 0,0,0,mPrinter);
                CommonFunc.printingNormal("Total Current Due",MainActivity.dec2.format(mBill.getTotalAmount())+ "\n",1,0,0,mPrinter);
                CommonFunc.printingNormal("\n","", 0,0,0,mPrinter);

                if(arrearsBillMonthList.size() > 0) {
                    if(mPrinter == 1) {
                        bp.printText("BillingDate   BillNumber    Amount     Surcharge" + "\n",BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                        bp.printText("------------------------------------------------" + "\n",BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                    }else{
                        mp.printText("BillingDate        BillNumber         Amount         Surcharge" + "\n");
                        mp.printText("--------------------------------------------------------------" + "\n");
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

                        CommonFunc.printingNormal(str1,str2+"\n",0,0,0,mPrinter);
                    }

                    if(mPrinter == 1) {
                        bp.printText("------------------------------------------------" + "\n",BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                    }else{
                        mp.printText("--------------------------------------------------------------" + "\n");
                    }
                }

                CommonFunc.printingNormal("Add:SURCHARGE:", MainActivity.dec2.format(Double.valueOf(penalty)) + "\n",0,0,0,mPrinter);
                CommonFunc.printingNormal("Arrears:", MainActivity.dec2.format(Double.valueOf(mAccount.getPrevBilling())) + "\n",0,0,0,mPrinter);
                CommonFunc.printingNormal("Less:Advance Payment:", MainActivity.dec2.format(Double.valueOf(mAccount.getAdvancePayment())) + "\n",0,0,0,mPrinter);
                CommonFunc.printingNormal("\n","", 0,0,0,mPrinter);
                CommonFunc.printingNormal("TOTAL AMOUNT PAYABLE",MainActivity.dec2.format(mBill.getTotalBilledAmount()) +"\n",0,1,0,mPrinter);
                CommonFunc.printingNormal("\n","", 0,0,0,mPrinter);
                CommonFunc.printingNormal("\n", "" ,0,0,0,mPrinter);
                CommonFunc.printingNormal("\n","",0,0,0,mPrinter);
                CommonFunc.printingNormal("\n","",0,0,0,mPrinter);
            }


            if(isNetMetering.equalsIgnoreCase("1")) {
                if(mPrinter == 1) {
                    CommonFunc.printingNormal("EXPORT BILL"+"\n","",1,0,0,mPrinter);
                    bp.printText("------------------------------------------------" + "\n",BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                    bp.printText("Date         Prev           Pres             KWH" + "\n",BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                }else {
                    mp.printTextBoldRight("","EXPORT BILL"+"\n");
                    mp.printText("--------------------------------------------------------------" + "\n");
                    mp.printText("Date                Prev                 Pres              KWH" + "\n");
                }

                String exportConsume = MainActivity.dec2.format(Double.valueOf(mAccount.getExportConsume()));

                int padding1 = 20 - dateRead.length() - mAccount.getExportPreviousReading().length();
                String paddingChar1 = " ";
                for (int p = 0; p < padding1; p++) {
                    paddingChar1 = paddingChar1.concat(" ");
                }
                String strRight1 = dateRead + paddingChar1 + mAccount.getExportPreviousReading();

                int paddingLeft1 = 20 - mAccount.getExportReading().length() - exportConsume.length();
                String _paddingLeft1 = " ";
                for (int p = 0; p < paddingLeft1; p++) {
                    _paddingLeft1 = paddingChar1.concat(" ");
                }
                String strLeft1 = mAccount.getExportReading() + _paddingLeft1 + exportConsume;
                CommonFunc.printingNormal(""+strRight1,strLeft1+"\n",0,0,0,mPrinter);
                if(mPrinter == 1) {
                    bp.printText("------------------------------------------------" + "\n",BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                }else {
                    mp.printText("--------------------------------------------------------------" + "\n");
                }

                if(!isMotherMeter) {
                    CommonFunc.printingNormal("Customer Charge to DU\n","",0,0,0,mPrinter);
                    for (int i = 0; i < rateComponentForExport.size(); i++) {
                        CommonFunc.printingNormal("  " + rateComponentForExport.get(i),  exportRateDueAmount.get(i) + "\n",0,0,0,mPrinter);
                    }

                    CommonFunc.printingNormal("Amount Export Due",MainActivity.dec2.format(mBill.getTotalAmountDueExport())+ "\n",1,0,0,mPrinter);
                    CommonFunc.printingNormal("\n","",0,0,0,mPrinter);
                    CommonFunc.printingNormal("NET BILL AMOUNT", MainActivity.dec2.format(mBill.getNetBillAmountExport()) + "\n",1,0,0,mPrinter);
                }
            }


                if(arrearsBillMonthList.size() > 0) {
                    if(mPrinter == 1) {
                        bp.printNextLine(2);
                        bp.printText(Constant.DISCONNECTIONNOTICE_BIX + "\n",BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                        bp.printNextLine(1);
                    }else {
                        mp.printText("\n");
                        mp.printText("\n");
                        mp.printText(Constant.DISCONNECTIONNOTICE + "\n");
                        mp.printText("\n");
                    }

                    if(mPrinter == 1) {
                        bp.printText(Constant.OFFICIALRECIEPT_BIX + "\n",BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                        bp.printNextLine(1);
                    }else {
                        mp.printText(Constant.OFFICIALRECIEPT + "\n");
                        mp.printText("\n");
                    }

                    if(mPrinter == 1) {
                        bp.printText("     " + Constant.WARNING + "\n",BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                    }else {
                        mp.printText("                " + Constant.WARNING + "                 " + "\n");
                    }
                }else{
                    CommonFunc.printingNormal("\n","",0,0,0,mPrinter);
                    CommonFunc.printingNormal("\n","",0,0,0,mPrinter);
                    CommonFunc.printingNormal(Constant.FOOTERMESSAGE+"\n","",0,0,0,mPrinter);
                    if(mPrinter == 1) {
                        bp.printText(Constant.OFFICIALRECIEPT_BIX + "\n",BixolonPrinter.TEXT_SIZE_HORIZONTAL1);
                    }else {
                        mp.printText(Constant.OFFICIALRECIEPT + "\n");
                    }
                }

                CommonFunc.printingNormal("\n","",0,0,0,mPrinter);
                CommonFunc.printingNormal("\n","",0,0,0,mPrinter);
                CommonFunc.printingNormal("\n","",0,0,0,mPrinter);
                CommonFunc.printingNormal("\n","",0,0,0,mPrinter);
                db.updateAccountToPrinted(db,"Printed");

            }catch (NullPointerException e) {
                Log.e(TAG,"preparePrint : "+ e.getMessage());
            }

            myProgressBar.dismissDialog();
        }
    }
