package com.payvenue.meterreader;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import Utility.CommonFunc;
import Utility.Constant;
import Utility.MobilePrinter;

public class ViewDetails extends AppCompatActivity implements OnClickListener {


    int myPurpose = 0;

    Double latitude = 0.00;
    Double longtitude = 0.00;
    Double curlat = 0.00;
    Double curlong = 0.00;
    private static final String TAG = "ViewDetails";

    //ConstraintLayout constraintLayout;
    TableLayout constraintLayout;
    TextView viewacctid, viewacctserial, viewacctname, viewacctadd, txtclass, txtmeterbrand,
            mReading, mPrevReading, mConsume, mLocation;
    Button btnViewMap, btnReadAccount, btnNotFound;
    ArrayList<RateSegmentModel> listRateSegment = new ArrayList<>();
    public DataBaseHandler db;
    private boolean IsMotherMeter = false;
    private Account mAccount;
    private Account searchAccount;
    ArrayList<String> arrearsAmountList = new ArrayList<>();
    ArrayList<String> arrearsBillMonthList = new ArrayList<>();
    ArrayList<String> arrearsPenaltyList = new ArrayList<>();
    ArrayList<String> arrearsBillNumberList = new ArrayList<>();
    private boolean isSearch = false;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_view_details);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        db = new DataBaseHandler(this);
        Log.e(TAG,"Current Page");
        Bundle b = getIntent().getExtras();


        if (b != null) {
            myPurpose = b.getInt("purpose");
        }

        mAccount =  MainActivity.selectedAccount;
        if(mAccount.getIsCheckSubMeterType().toLowerCase().equalsIgnoreCase("m")) {
            IsMotherMeter = true;
        }


        /**Initalize Rate Segment*/
        listRateSegment = db.getRateSegment(db);
        simplifyArrears();

        initViews();
        setValues();
        displayButton();
    }


    //region Functions

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

        btnReadAccount.setOnClickListener(this);
        btnViewMap.setOnClickListener(this);
        btnNotFound.setOnClickListener(this);

    }

    public void setValues() {
        if(!isSearch) {
            viewacctid.setText(mAccount.getAccountID());
            viewacctserial.setText(mAccount.getMeterSerialNo());
            //mAccount.getFirstName() + " " + mAccount.getMiddleName() + " " + mAccount.getLastName()
            viewacctname.setText(mAccount.getLastName());
            viewacctadd.setText(mAccount.getAddress());
            txtclass.setText(mAccount.getAccountClassification());
            txtmeterbrand.setText(mAccount.getMeterBrand());
            mReading.setText(mAccount.getReading());
            mPrevReading.setText(mAccount.getInitialReading());
            mConsume.setText(mAccount.getConsume());
            mLocation.setText(mAccount.getLatitude() + "," + mAccount.getLongitude());
        }else{
            searchAccount = MainActivity.selectedAccount;
            viewacctid.setText(searchAccount.getAccountID());
            viewacctserial.setText(searchAccount.getMeterSerialNo());
            //mAccount.getFirstName() + " " + mAccount.getMiddleName() + " " + mAccount.getLastName()
            viewacctname.setText(searchAccount.getLastName());
            viewacctadd.setText(searchAccount.getAddress());
            txtclass.setText(searchAccount.getAccountClassification());
            txtmeterbrand.setText(searchAccount.getMeterBrand());
            mReading.setText(searchAccount.getReading());
            mPrevReading.setText(searchAccount.getInitialReading());
            mConsume.setText(searchAccount.getConsume());
            mLocation.setText(searchAccount.getLatitude() + "," + searchAccount.getLongitude());
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
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
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
                    Log.e(TAG,"close");
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
                break;
            case MainActivity.Modes.MODE_2://3333
            case MainActivity.Modes.MODE_3:
                menu.findItem(R.id.search).setVisible(false);
                if (mAccount.getUploadStatus().equals("1")) {
                    menu.findItem(R.id.btnEdit).setVisible(false);
                } else {
                    menu.findItem(R.id.btnEdit).setVisible(true);
                }
                menu.findItem(R.id.btnGen).setVisible(true);
                menu.findItem(R.id.btnRead).setVisible(false);
                break;
            case MainActivity.Modes.MODE_4://4444
                menu.findItem(R.id.search).setVisible(false);
                menu.findItem(R.id.btnGen).setVisible(false);
                menu.findItem(R.id.btnEdit).setVisible(false);
                menu.findItem(R.id.btnRead).setVisible(true);
                break;
        }


        return super.onPrepareOptionsMenu(menu);

        // return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }

        if (id == R.id.btnGen) {

            if (CommonFunc.toDigit(mAccount.getConsume()) < 0) {
                Toast.makeText(this,
                        "Can't Generate Billing for erroneous reading.",
                        Toast.LENGTH_SHORT).show();
            } else {
                //preparePrint();
                if (MainActivity.mIsConnected) {
                    preparePrint();
                } else {
                    Toast.makeText(getBaseContext(), "Printer is not connected.", Toast.LENGTH_SHORT).show();
                }
//                if (mAccount.getAccountClassification().equalsIgnoreCase("Higher Voltage")) {
//                    Toast.makeText(this, "Can't Generate Billing for Higher Voltage Classification.", Toast.LENGTH_SHORT).show();
//                } else {
//                    //Intent intent = new Intent(this, BillPreview.class);
//                    //startActivityForResult(intent, 5);
//
//                }
            }
        }

        if (id == R.id.btnEdit) {
            int editCount = db.getEditAttemp(db,mAccount.getAccountID());

//            if(editCount == 3) {
//                Toast.makeText(getBaseContext(), "Exceed the maximum count of editing...", Toast.LENGTH_SHORT).show();
//            }else{
                Intent intent = new Intent(this, Accounts.class);
                startActivityForResult(intent, 5);
//            }


        }

        if (id == R.id.btnRead)

        {
            Intent intent = new Intent(this, Accounts.class);
            startActivityForResult(intent, 5);

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

                db.updateReadAccount(db, MainActivity.Modes.MODE_4,false);

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
                startActivityForResult(intent, 1);

                break;

        }

    }

    private void simplifyArrears(){
        try {
            JSONArray jsonArray = new JSONArray(mAccount.getArrears());
            for(int i = 0; i < jsonArray.length();i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                arrearsAmountList.add(jsonObject.getString("Amount"));
                arrearsBillMonthList.add(jsonObject.getString("BillingDate"));
                arrearsPenaltyList.add(jsonObject.getString("Penalty"));
                arrearsBillNumberList.add(jsonObject.getString("BillNumber"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG,"simplifyArrears:" + e.getMessage());
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

        String []date = mAccount.getDateRead().split(" ");
        String dateRead = date[0];

        String penalty = mAccount.getPenalty();
        if(penalty.equalsIgnoreCase("-") || penalty.equalsIgnoreCase(".")) {
            penalty = "0";
        }

        MobilePrinter mp = MobilePrinter.getInstance(this);

        Bill mBill = null;
        List<Rates> mRates = null;
        if(!IsMotherMeter) {
            mBill = mAccount.getBill();
            mRates = mBill.getRates();
            /**Check duplicate name*/
            if (listRateSegment.size() > 0) {
                for (RateSegmentModel seg : listRateSegment) {
                    for(Rates r : mRates){
                        String segmentName = seg.getRateSegmentName();
                        String codeName = r.getCodeName();
                        String sCode = seg.getRateSegmentCode();
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

////        mp.printText("           Negros Oriental II Electric Cooperative\n");
////        mp.printText("                  Real St., Dumaguete City\n");
////        mp.printText("                         (NORECO2)\n");
////        mp.printText("                   STATEMENT OF ACCOUNT\n");
////        mp.printText("================================================================\n");

        String path = CommonFunc.getPrivateAlbumStorageDir(this,"noreco_logo.bmp").toString();
        mp.printBitmap(path);
        mp.printText("\n");
        mp.printextEmphasized("Account No:"+ mAccount.getAccountID()+"\n");
        mp.printextEmphasized(name+"\n");
        mp.printextEmphasizedNormalFont(mAccount.getAddress()+"\n");
        mp.printText("Meter No:" + mAccount.getMeterSerialNo()+"\n");
        mp.printText("Period Covered: "+ CommonFunc.changeDateFormat(mAccount.getLastReadingDate()) + " to " + CommonFunc.changeDateFormat(dateRead) +"\n");
        mp.printText("Due Date: "+mAccount.getDueDate()+"\n");//
        mp.printText("Meter Reader:" + MainActivity.reader.getReaderName()+"\n");
        mp.printText("Multiplier:" + mAccount.getMultiplier(),"Consumer Type:" + a_class+"\n");
        mp.printText("Consumption:" + mAccount.getActualConsumption(),"BillMonth:" + mAccount.getBillMonth()+"\n");
        if(a_class.equalsIgnoreCase("HV")) {
            mp.printText("Coreloss:" + mAccount.getCoreloss(),"DemandKW:"+mAccount.getDemandKW()+"\n");
        }else{
            mp.printText("Coreloss:" + mAccount.getCoreloss()+"\n");
        }

        if(mAccount.getIsNetMetering().equalsIgnoreCase("1")) {
            mp.printText("Net-Metering Customer - IMPORT BILL\n");
        }

        mp.printText("--------------------------------------------------------------"+"\n");
        mp.printText("Date                Prev                 Pres              KWH"+"\n");



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

        mp.printText(strRight,strLeft+"\n");
        mp.printText("--------------------------------------------------------------"+"\n");
        if(!IsMotherMeter) {
            if (listRateSegment.size() > 0) {
                for (RateSegmentModel s : listRateSegment) {
                    String segmentName = s.getRateSegmentName();
                    String rateSegmentCode = s.getRateSegmentCode();

                    /** Avoid printing same segment name*/
                    if(duplicateSegmentName.size() > 0) {
                        for(String seg : duplicateSegmentName) {
                            if (!seg.equalsIgnoreCase(segmentName)) {
                                mp.printText(segmentName + "\n");
                            }
                        }
                    }else{
                        mp.printText(segmentName + "\n");
                    }


                    for (Rates r : mRates) {
                        if (r.getRateSegment().equals(rateSegmentCode)) {
                            String codeName = r.getCodeName();
                            @SuppressLint("DefaultLocale")
                            String rateAmount = String.format("%.4f",Float.valueOf(r.getRateAmount()));
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
                                vatListCode.add(codeName);
                                vatListValue.add(rightText);
                            }

                            /** print here */
                            if (codeName.equalsIgnoreCase("Subsidy on Lifeline")) {

                                if (!mAccount.getTotalLifeLineDiscount().equalsIgnoreCase("0.0")) {
                                    mp.printText("  Lifeline Discount(R)", "-" + MainActivity.dec2.format(Double.valueOf(mAccount.getTotalLifeLineDiscount())) + "\n");
                                } else {
                                    mp.printText("  " + codeName, rightText + "\n");
                                }
                            }

                            if (codeName.equalsIgnoreCase("Senior Citizens Subsidy")) {

                                if (!mAccount.getTotalSCDiscount().equalsIgnoreCase("0.0")) {
                                    mp.printText("  Senior Citizens Discount(R)", "-" + MainActivity.dec2.format(Double.valueOf(mAccount.getTotalSCDiscount())) + "\n");
                                } else {
                                    mp.printText("  " + codeName, rightText + "\n");
                                }
                            }


                            if (!codeName.equalsIgnoreCase("Subsidy on Lifeline") && !codeName.equalsIgnoreCase("Senior Citizens Subsidy") && !codeName.contains("VAT on")) {
                                String sn = segmentName(duplicateSegmentName,codeName);
                                if(!sn.equalsIgnoreCase("")){
                                    mp.printText(codeName, rightText + "\n");
                                }else{
                                    mp.printText("  " + codeName, rightText + "\n");
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
                mp.printText("  " + vatListCode.get(i), vatListValue.get(i) + "\n");
            }

            mp.printTextEmphasized("Total Current Due", MainActivity.dec2.format(mBill.getTotalAmount()));
            mp.printText("", "" + "\n");

            if(arrearsBillMonthList.size() > 0) {
                mp.printText("BillingDate        BillNumber         Amount         Surcharge" + "\n");
                mp.printText("--------------------------------------------------------------" + "\n");
                for (int i = 0; i < arrearsBillMonthList.size(); i++) {
                    String billdate = arrearsBillMonthList.get(i);
                    String billnumber = arrearsBillNumberList.get(i);
                    String amount = arrearsAmountList.get(i);
                    String _penalty = arrearsPenaltyList.get(i);
                    String[] str = billdate.split(" ");
                    mp.printText(str[0], billnumber, amount, _penalty + "\n", 0);
                }
                mp.printText("--------------------------------------------------------------" + "\n");
            }
            mp.printText("Add:SURCHARGE:", MainActivity.dec2.format(Double.valueOf(penalty)) + "\n");
            mp.printText("Arrears:", MainActivity.dec2.format(Double.valueOf(mAccount.getPrevBilling())) + "\n");
            mp.printText("Less:Advance Payment:", MainActivity.dec2.format(Double.valueOf(mAccount.getAdvancePayment())) + "\n");
            mp.printTextEmphasized1("TOTAL AMOUNT PAYABLE", MainActivity.dec2.format(mBill.getTotalBilledAmount()));
            mp.printText("", "" + "\n");
            mp.printText("", "" + "\n");
            mp.printText("", "" + "\n");
            mp.printText("", "" + "\n");

        }


        if(mAccount.getIsNetMetering().equalsIgnoreCase("1")) {
            mp.printTextBoldRight("","EXPORT BILL"+"\n");
            mp.printText("--------------------------------------------------------------"+"\n");
            mp.printText("Date                Prev                 Pres              KWH"+"\n");
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
            mp.printText(strRight1,strLeft1+"\n");
            mp.printText("--------------------------------------------------------------"+"\n");

            if(!IsMotherMeter) {
                mp.printText("Customer Charge to DU\n");
                for (int i = 0; i < rateComponentForExport.size(); i++) {
                    mp.printText("  " + rateComponentForExport.get(i),  exportRateDueAmount.get(i) + "\n");
                }
                mp.printTextEmphasized("Amount Export Due", MainActivity.dec2.format(mBill.getTotalAmountDueExport()));

                mp.printText("\n");
                mp.printText("\n");
                mp.printTextEmphasized("NET BILL AMOUNT", MainActivity.dec2.format(mBill.getNetBillAmountExport()) + "\n");

            }
        }



        if(arrearsBillMonthList.size() > 0) {
            mp.printText("\n");
            mp.printText("\n");
            mp.printText(Constant.DISCONNECTIONNOTICE+"\n");
            mp.printText("\n");
            mp.printText(Constant.OFFICIALRECIEPT+"\n");
            mp.printText("\n");
            mp.printText("                "+Constant.WARNING+"                 "+"\n");
        }else{
            mp.printText("\n");
            mp.printText("\n");
            mp.printText(Constant.FOOTERMESSAGE+"\n");
            mp.printText("\n");
            mp.printText(Constant.OFFICIALRECIEPT+"\n");
        }

        if (!mAccount.getPrintCount().equalsIgnoreCase("0")) {
            mp.printText("REPRINTED" + "\n");
        }
        mp.printText("\n");
        mp.printText("\n");
        mp.printText("\n");
        mp.printText("\n");
        db.updateAccountToPrinted(db,"Printed");

    }
}
