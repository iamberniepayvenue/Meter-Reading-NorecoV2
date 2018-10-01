package com.payvenue.meterreader;

import android.annotation.SuppressLint;
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
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapswithme.maps.api.MapsWithMeApi;

import java.util.ArrayList;
import java.util.List;

import DataBase.DataBaseHandler;
import Model.Bill;
import Model.RateSegmentModel;
import Model.Rates;
import Utility.CommonFunc;
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

        if(MainActivity.selectedAccount.getIsCheckSubMeterType().equalsIgnoreCase("M") || MainActivity.selectedAccount.getIsCheckSubMeterType().equalsIgnoreCase("m")) {
            IsMotherMeter = true;
        }

        /**Initalize Rate Segment*/
        listRateSegment = db.getRateSegment(db);
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
        viewacctid.setText(MainActivity.selectedAccount.getAccountID());
        viewacctserial.setText(MainActivity.selectedAccount.getMeterSerialNo());
        //MainActivity.selectedAccount.getFirstName() + " " + MainActivity.selectedAccount.getMiddleName() + " " + MainActivity.selectedAccount.getLastName()
        viewacctname.setText(MainActivity.selectedAccount.getLastName());
        viewacctadd.setText(MainActivity.selectedAccount.getAddress());
        txtclass.setText(MainActivity.selectedAccount.getAccountClassification());
        txtmeterbrand.setText(MainActivity.selectedAccount.getMeterBrand());
        mReading.setText(MainActivity.selectedAccount.getReading());
        mPrevReading.setText(MainActivity.selectedAccount.getInitialReading());
        mConsume.setText(MainActivity.selectedAccount.getConsume());
        mLocation.setText(MainActivity.selectedAccount.getLatitude() +","+MainActivity.selectedAccount.getLongitude());
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
        return true;
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
                if (MainActivity.selectedAccount.getUploadStatus().equals("1")) {
                    menu.findItem(R.id.btnEdit).setVisible(false);
                } else {
                    menu.findItem(R.id.btnEdit).setVisible(true);
                }
                menu.findItem(R.id.btnGen).setVisible(true);
                menu.findItem(R.id.btnRead).setVisible(false);
                break;
            case MainActivity.Modes.MODE_4://4444
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

            if (CommonFunc.toDigit(MainActivity.selectedAccount.getConsume()) < 0) {
                Toast.makeText(this,
                        "Can't Generate Billing for erroneous reading.",
                        Toast.LENGTH_SHORT).show();
            } else {

                if (MainActivity.selectedAccount.getAccountClassification().equalsIgnoreCase("Higher Voltage")) {
                    Toast.makeText(this, "Can't Generate Billing for Higher Voltage Classification.", Toast.LENGTH_SHORT).show();
                } else {
                    //Intent intent = new Intent(this, BillPreview.class);
                    //startActivityForResult(intent, 5);
                    if (MainActivity.mIsConnected) {
                        preparePrint();
                    } else {
                        Toast.makeText(getBaseContext(), "Printer is not connected.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        if (id == R.id.btnEdit) {
            int editCount = db.getEditAttemp(db,MainActivity.selectedAccount.getAccountID());

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

        return super.

                onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        switch (v.getId()) {

            case R.id.btnNotFound:


                MainActivity.selectedAccount.setReadStatus(MainActivity.Modes.MODE_4);
                MainActivity.selectedAccount.setLatitude(String.valueOf(curlat));
                MainActivity.selectedAccount.setLongitude(String.valueOf(curlong));

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

    public void preparePrint() {
        ArrayList<String> vatListCode = new ArrayList<>();
        ArrayList<String> vatListValue = new ArrayList<>();
        ArrayList<String> rateComponentForExport = new ArrayList<>();
        ArrayList<String> exportRateDueAmount = new ArrayList<>();
        String name = MainActivity.selectedAccount.getLastName();
        if(MainActivity.selectedAccount.getFirstName().equalsIgnoreCase(".")
                || MainActivity.selectedAccount.getMiddleName().equalsIgnoreCase(".")){
            name = MainActivity.selectedAccount.getLastName() + ", " + MainActivity.selectedAccount.getFirstName()
                    + MainActivity.selectedAccount.getMiddleName();
        }

        String penalty = MainActivity.selectedAccount.getPenalty();
        if(penalty.equalsIgnoreCase("-") || penalty.equalsIgnoreCase(".")) {
            penalty = "0";
        }

        MobilePrinter mp = MobilePrinter.getInstance(this);
        Bill mBill;
        List<Rates> mRates;
        mBill = MainActivity.selectedAccount.getBill();
        String a_class = MainActivity.selectedAccount.getAccountClassification();
        if(a_class.equalsIgnoreCase("RESIDENTIAL") || a_class.equalsIgnoreCase("Residential")) {
            a_class = "Res";
        } else if(a_class.equalsIgnoreCase("Lower Voltage")) {
            a_class = "LV";
        }else if (a_class.equalsIgnoreCase("Higher Voltage")) {
            a_class = "HV";
        }
        //Rates rates;
        mRates = mBill.getRates();
//        mp.printText("           Negros Oriental II Electric Cooperative\n");
//        mp.printText("                  Real St., Dumaguete City\n");
//        mp.printText("                         (NORECO2)\n");
//        mp.printText("                   STATEMENT OF ACCOUNT\n");
//        mp.printText("================================================================\n");

        String path = CommonFunc.getPrivateAlbumStorageDir(this,"noreco_logo.bmp").toString();
        mp.printBitmap(path);
        mp.printText("\n");
        mp.printText("Meter No:" + MainActivity.selectedAccount.getMeterSerialNo(), "Type:" + a_class+"\n");
        mp.printTextBoldRight("Account No:", MainActivity.selectedAccount.getAccountID());
        mp.printTextExceptLeft("Account No:"+ MainActivity.selectedAccount.getAccountID(),"BillMonth:" + CommonFunc.monthAbrev(MainActivity.selectedAccount.getBillMonth())+"\n");
        mp.printTextBoldRight("Account Name:",name+"\n");
        mp.printText("Address:"+ MainActivity.selectedAccount.getAddress()+"\n");
        mp.printText("Period Covered: "+ CommonFunc.changeDateFormat(MainActivity.selectedAccount.getLastReadingDate()) + " to " + CommonFunc.changeDateFormat(MainActivity.selectedAccount.getDateRead()) +"\n");
        mp.printText("Due Date: "+MainActivity.selectedAccount.getDueDate()+"\n");//
        mp.printText("Meter Reader:" + MainActivity.reader.getReaderName()+"\n");
        mp.printText("Multiplier:" + MainActivity.selectedAccount.getMultiplier()+"\n");
        mp.printText("Consumption:" + MainActivity.selectedAccount.getActualConsumption()+"\n");
        mp.printText("--------------------------------------------------------------"+"\n");
        mp.printText("Date              Prev                 Pres              KWH"+"\n");
        mp.printText(MainActivity.selectedAccount.getDateRead()
                + "         " + MainActivity.selectedAccount.getInitialReading()
                + "                " + MainActivity.selectedAccount.getReading()
                + "          " + MainActivity.selectedAccount.getConsume()+"\n");
        mp.printText("--------------------------------------------------------------"+"\n");
        //Cursor cursorRateSegment = db.getRateSegment(db);
        //ArrayList<Components> componentsList= db.getRateComponent(db);
        if(!IsMotherMeter) {
            if (listRateSegment.size() > 0) {
                for (RateSegmentModel s : listRateSegment) {
                    String segmentName = s.getRateSegmentName();
                    String rateSegmentCode = s.getRateSegmentCode();
                    if (!segmentName.equalsIgnoreCase("FIT-ALL")) {
                        mp.printText(segmentName + "\n");
                    }
                    for (Rates r : mRates) {
                        if (r.getRateSegment().equals(rateSegmentCode)) {
                            String codeName = r.getCodeName();
                            String rateAmount = String.valueOf(r.getRateAmount());
                            String amount;
                            amount = String.valueOf(r.getAmount());

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

                                if (!MainActivity.selectedAccount.getTotalLifeLineDiscount().equalsIgnoreCase("0.0")) {
                                    mp.printText("  Lifeline Discount(R)", "-" + MainActivity.dec2.format(Double.valueOf(MainActivity.selectedAccount.getTotalLifeLineDiscount())) + "\n");
                                } else {
                                    mp.printText("  " + codeName, rightText + "\n");
                                }
                            }

                            if (codeName.equalsIgnoreCase("Senior Citizens Subsidy")) {

                                if (!MainActivity.selectedAccount.getTotalSCDiscount().equalsIgnoreCase("0.0")) {
                                    mp.printText("  Senior Citizens Discount(R)", "-" + MainActivity.dec2.format(Double.valueOf(MainActivity.selectedAccount.getTotalSCDiscount())) + "\n");
                                } else {
                                    mp.printText("  " + codeName, rightText + "\n");
                                }
                            }


                            if (!codeName.equalsIgnoreCase("Subsidy on Lifeline") && !codeName.equalsIgnoreCase("Senior Citizens Subsidy") && !codeName.contains("VAT on")) {
                                mp.printText("  " + codeName, rightText + "\n");
                            }

                            /**NET METERING*/
                            if (MainActivity.selectedAccount.getIsNetMetering().equalsIgnoreCase("1")) {
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
            mp.printText("Arrears:", MainActivity.dec2.format(Double.valueOf(MainActivity.selectedAccount.getPrevBilling())) + "\n");
            mp.printText("Less:Advance Payment:", MainActivity.dec2.format(Double.valueOf(MainActivity.selectedAccount.getAdvancePayment())) + "\n");
            mp.printTextEmphasized1("TOTAL AMOUNT PAYABLE", MainActivity.dec2.format(mBill.getTotalBilledAmount()));
            mp.printText("", "" + "\n");
            mp.printText("", "" + "\n");
            mp.printText("", "" + "\n");
            mp.printText("", "" + "\n");

        }


        if(MainActivity.selectedAccount.getIsNetMetering().equalsIgnoreCase("1")) {
            mp.printText("EXPORT BILL\n");
            mp.printText("--------------------------------------------------------------"+"\n");
            mp.printText("Date              Prev                 Pres              KWH"+"\n");
            mp.printText(MainActivity.selectedAccount.getDateRead()
                    + "         " + MainActivity.selectedAccount.getExportPreviousReading()
                    + "                " + MainActivity.selectedAccount.getExportReading()
                    + "          " + MainActivity.selectedAccount.getExportConsume()+"\n");
            mp.printText("--------------------------------------------------------------"+"\n");

            if(!IsMotherMeter) {
                mp.printText("Customer Charge to DU\n");
                for (int i = 0; i < rateComponentForExport.size(); i++) {
                    mp.printText("  " + rateComponentForExport.get(i), exportRateDueAmount.get(i) + "\n");
                }
                mp.printTextEmphasized("Amount Export Due", MainActivity.dec2.format(mBill.getTotalAmountDueExport()));

                mp.printText("\n");
                mp.printText("\n");
                mp.printTextEmphasized("NET BILL AMOUNT", MainActivity.dec2.format(mBill.getNetBillAmountExport()) + "\n");
                if (!MainActivity.selectedAccount.getPrintCount().equalsIgnoreCase("0")) {
                    mp.printText("REPRINTED" + "\n");
                }
            }


            mp.printText("\n");
            mp.printText("\n");
            mp.printText("\n");
            mp.printText("\n");

        }


        db.updateAccountToPrinted(db,"Printed");

    }
}
