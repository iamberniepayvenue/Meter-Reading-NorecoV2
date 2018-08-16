package com.payvenue.meterreader;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import Model.Bill;
import Model.RateSegmentModel;
import Model.Rates;

import static com.payvenue.meterreader.R.menu.bill;

public class BillPreview extends AppCompatActivity {


    Bill mBill;
    List<Rates> mRates = new ArrayList<>();
    ArrayList<RateSegmentModel> listRateSegment = new ArrayList<>();
    private static final String TAG = "BillPreview";

    TextView txtotalamount, account,
            actclass, acctname,
            acctadd, meterserial,
            prevdate,
            prevread, curdate,
            curread, consumption,
            txtperiod, txtduedate,
            txtbillmonth,
            txtpolerental, txtarrears,
            txttransrental, txtpilferagepenalty,
            txtmeterdeposit, txtkvatsubsidy,
            txtpayableamt, txtpayaftrduedate,
            txtsurchage, txtmeterreader,
            mUnderOver,tvTotalCurrentDue;

    TableLayout tableComponents;
    TableLayout.LayoutParams lastTxtParams;
    TableRow.LayoutParams Rowparams;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_layout_bill);
        getSupportActionBar().setHomeButtonEnabled(true);

        Log.e(TAG,"Current Page");
        lastTxtParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        Rowparams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT, 1f);
        Rowparams.weight = 1;

        initViews();

        setValues();

        populateBill();


    }


    //region Functions

    public void initViews() {

        account = findViewById(R.id.txtacctnum);
        actclass =  findViewById(R.id.txtaccountclass);
        acctname = findViewById(R.id.txtacctname);
        acctadd =  findViewById(R.id.txtacctaddress);
        meterserial = findViewById(R.id.txtmeterserial);
        prevdate =  findViewById(R.id.txtprevdate);
        prevread =  findViewById(R.id.txtprevread);
        curdate =  findViewById(R.id.txtcurdate);
        curread =  findViewById(R.id.txtcurread);
        consumption =  findViewById(R.id.txtconsump);
        txtperiod =  findViewById(R.id.txtperiod);
        txtduedate =  findViewById(R.id.txtduedate);
        txtbillmonth =  findViewById(R.id.txtbillmonth);
        txtpolerental =  findViewById(R.id.txtpolerental);
        txtarrears =  findViewById(R.id.txtarrears);
        txttransrental =  findViewById(R.id.txttransrental);
        txtpilferagepenalty =  findViewById(R.id.txtpilferagepenalty);
        txtmeterdeposit =  findViewById(R.id.txtmeterdeposit);
        txtkvatsubsidy =  findViewById(R.id.txtkvatsubsidy);
        txtpayableamt =  findViewById(R.id.txtpayableamt);
        txtpayaftrduedate =  findViewById(R.id.txtpayaftrduedate);
        txtmeterreader =  findViewById(R.id.txtmeterreader);
        txtsurchage =  findViewById(R.id.txtsurchage);
        txtotalamount =  findViewById(R.id.txt_totalamount);
        mUnderOver = findViewById(R.id.tvUnderOverRecovery);
        tvTotalCurrentDue = findViewById(R.id.tvTotalCurrentDue);

        tableComponents = findViewById(R.id.tableLayout1);


    }

    public void setValues() {
        Log.e(TAG,"BillMonth:" + MainActivity.selectedAccount.getBillMonth());
        account.setText(MainActivity.selectedAccount.getAccountID());
        acctname.setText(MainActivity.selectedAccount.getLastName());
        acctadd.setText(MainActivity.selectedAccount.getAddress());
        actclass.setText(MainActivity.selectedAccount.getSubClassification().substring(0, 1));
        meterserial.setText(MainActivity.selectedAccount.getMeterSerialNo());
        consumption.setText(MainActivity.selectedAccount.getConsume());
        txtbillmonth.setText(MainActivity.selectedAccount.getBillMonth());
        curdate.setText(MainActivity.selectedAccount.getDateRead());
        curread.setText(MainActivity.selectedAccount.getReading());
        prevread.setText(MainActivity.selectedAccount.getPrevReading());
        txtmeterreader.setText(MainActivity.reader.getReaderName());
        txtperiod.setText(MainActivity.selectedAccount.getLastReadingDate() + " to " + MainActivity.selectedAccount.getDateRead());
        txtduedate.setText(MainActivity.selectedAccount.getDueDate());
        mUnderOver.setText(""+MainActivity.dec2.format(Double.valueOf(MainActivity.selectedAccount.getUnderOverRecovery())));

        mBill = MainActivity.selectedAccount.getBill();
        tvTotalCurrentDue.setText(MainActivity.dec2.format(Double.valueOf(mBill.getTotalAmount())));
        txtpayableamt.setText(MainActivity.dec2.format(Double.valueOf(mBill.getTotalBilledAmount())));
        mRates = mBill.getRates();

    }

    public void populateBill() {

        /**Initalize Rate Segment*/
        listRateSegment = MainActivity.db.getRateSegment(MainActivity.db);

        TableRow row;
        TableRow rowcomponent;
        TextView segment;
        TextView mComponent, mRateAmount, mAmount;

        Rates rates;
        if(listRateSegment.size() > 0){
            for (RateSegmentModel s: listRateSegment) {

                row = new TableRow(this);
                row.setPadding(10, 10, 10, 10);
                segment = new TextView(this);

                segment.setText(s.getRateSegmentName());
                segment.setGravity(Gravity.LEFT);
                segment.setTextSize(18f);
                segment.setTextColor(Color.parseColor("#660000"));
                segment.setTypeface(null, Typeface.BOLD);
                segment.setLayoutParams(Rowparams);
                row.addView(segment);
                tableComponents.addView(row, lastTxtParams);//


                for (int i = 0; i < mRates.size(); i++) {

                    rates = mRates.get(i);

                    rowcomponent = new TableRow(this);
                    rowcomponent.setPadding(10, 0, 10, 0);

                    if (rates.getRateSegment().equals(s.getRateSegmentCode())) {

                        //getRateDetails();
                        mComponent = new TextView(this);
                        mComponent.setText(rates.getCodeName());
                        mComponent.setTextSize(12f);
                        mComponent.setLayoutParams(Rowparams);
                        rowcomponent.addView(mComponent);

                        mRateAmount = new TextView(this);
                        mRateAmount.setText(MainActivity.dec.format(rates.getRateAmount()));
                        mRateAmount.setTextSize(12f);
                        mRateAmount.setGravity(Gravity.RIGHT);
                        mRateAmount.setLayoutParams(Rowparams);
                        rowcomponent.addView(mRateAmount);

                        mAmount = new TextView(this);
                        mAmount.setText(MainActivity.dec2.format(rates.getAmount()));
                        mAmount.setTextSize(12f);
                        mAmount.setGravity(Gravity.RIGHT);
                        mAmount.setLayoutParams(Rowparams);
                        rowcomponent.addView(mAmount);

                    }

                    tableComponents.addView(rowcomponent);

                }
            }
        }
    }

    //endregion


    //region Triggers


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(bill, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        menu.findItem(R.id.item1).setVisible(false);

        if (MainActivity.selectedAccount.getUploadStatus().equals("1")) {
            menu.findItem(R.id.item4).setVisible(false);
        } else {
            menu.findItem(R.id.item4).setVisible(true);
        }

        if (MainActivity.mIsConnected) {
            menu.getItem(0).setEnabled(false);
            menu.getItem(1).setEnabled(true);
            menu.getItem(2).setEnabled(true);
            menu.getItem(3).setEnabled(true);
        } else {
            menu.getItem(0).setEnabled(true);
            menu.getItem(1).setEnabled(true);
            menu.getItem(2).setEnabled(false);
            menu.getItem(3).setEnabled(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item1:
                //dialog.show();
                break;
            case R.id.item2: /**Done*/
                finish();
                return true;
            case R.id.item3:


                if (MainActivity.mIsConnected) {
                    preparePrint();
                } else {
                    Toast.makeText(getBaseContext(), "Printer is not connected.", Toast.LENGTH_SHORT).show();
                }


                break;
            case R.id.item4: /**Edit Reading*/
                Intent intent = new Intent(this, Accounts.class);
                startActivity(intent);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            Log.i("Home Button", "Clicked");
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        }
        return false;
    }

    public void preparePrint() {


//        printText("Negros Oriental II Electric Cooperative");
//        printText("Real St., Dumaguete City");
//        printText("NORECO2");
//        printText("Statement of Account");
//        printText("Meter No:" + MainActivity.selectedAccount.getMeterSerialNo(), "Type" + MainActivity.selectedAccount.getAccountClassification());
//        printText("Account No:" + MainActivity.selectedAccount.getAccountID(), "BillMonth" + MainActivity.selectedAccount.getBillMonth());
//        printText("Account Name:", MainActivity.selectedAccount.getFirstName() + " " + MainActivity.selectedAccount.getMiddleName() + " " + MainActivity.selectedAccount.getFullName());
//        printText("Address", MainActivity.selectedAccount.getAddress());
//        printText("Period", MainActivity.selectedAccount.getLastReadingDate() + " to " + MainActivity.selectedAccount.getDateRead());
//        printText("Due Date:", "July 5,2018" );//MainActivity.selectedAccount.getDueDate()
//        printText("Meter Reader:", MainActivity.reader.getReaderName());
//        printText("Date    Prev    Pres    KWH");
//        printText(MainActivity.selectedAccount.getDateRead() + "    " + MainActivity.selectedAccount.getPrevReading() + "    " + MainActivity.selectedAccount.getReading() + "    " + MainActivity.selectedAccount.getConsume());
//
//        Cursor cr_segment = MainActivity.db.getRateSegment(MainActivity.db);
//
//        Rates rates;
//        int padding;
//        while (cr_segment.moveToNext()) {
//
//            MainActivity.printText(cr_segment.getString(cr_segment.getColumnIndex("RateSegmentName")));
//
//            for (int i = 0; i < mRates.size(); i++) {
//
//                rates = mRates.get(i);
//
//                if (rates.getRateSegment().equals(cr_segment.getString(cr_segment.getColumnIndex("RateSegmentCode")))) {
//
//                    padding = 17 - String.valueOf(rates.getRateAmount()).length() - String.valueOf(rates.getAmount()).length();
//                    String paddingChar = " ";
//                    Log.d("Padding", "" + padding + "");
//                    for (int x = 0; x < padding; x++) {
//                        paddingChar = paddingChar.concat(" ");
//                    }
//                    String item = rates.getRateAmount() + paddingChar + rates.getAmount() + "\n";
//
//                    MainActivity.printText(rates.getCodeName(), item);
//                }
//
//
//            }
//
//
//        }//end of loop
//
//
//        MainActivity.printText("Total Amount", MainActivity.dec.format(mBill.getTotalAmount()));
//        MainActivity.printText("Add:");
//        MainActivity.printText("Penalty:", MainActivity.selectedAccount.getPenalty());
//        MainActivity.printText("Arrears:", MainActivity.selectedAccount.getPrevBilling());
//        MainActivity.printText("Pole Rental", MainActivity.selectedAccount.getPoleRental());
//        MainActivity.printText("Space Rental", MainActivity.selectedAccount.getSpaceRental());
//        MainActivity.printText("Less:");
//        MainActivity.printText("Advance Payment:", MainActivity.selectedAccount.getAdvancePayment());
//        MainActivity.printText("Under Over Recovery (+/-)", MainActivity.selectedAccount.getUnderOverRecovery());
//        MainActivity.printText("Total Billed Amount", MainActivity.dec.format(mBill.getTotalBilledAmount()));

        //endregion


    }
}
