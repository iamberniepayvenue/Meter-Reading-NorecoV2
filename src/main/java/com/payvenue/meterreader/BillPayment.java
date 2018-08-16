package com.payvenue.meterreader;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import DataBase.DataBaseHandler;
import Model.Bill;
import Utility.CommonFunc;

import static com.payvenue.meterreader.MainActivity.dialog;
import static com.payvenue.meterreader.MainActivity.mIsConnected;

public class BillPayment extends AppCompatActivity implements View.OnClickListener {

    DataBaseHandler db;
    Context mcontext;
    TextView txtreadername, coopname, businessadd, txttxnid, txtAccountName, txtAddress,
            txtpaymentdate, txtcurrentbill, txtArrears, txttotalAmount, txtAccountId, txtmacaddress;
    Button btnprint;

    EditText editpayment;

    String mac, txnid;

    float billedAmount;
    DecimalFormat dec;
    float arrears;

    TextView txtpolerental;
    TextView txtSpaceRental;
    TextView txtpilferagepenalty;
    TextView txtmeterdeposit;
    TextView txtkvatsubsidy;
    String myaccountid;

    float meterdeposit;
    String ts;
    Bill mbill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_payment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        mcontext = this;

        Long tsLong = System.currentTimeMillis() / 1000;
        ts = tsLong.toString();

        mbill = MainActivity.selectedAccount.getBill();


        initViews();
        setValues();
        setActions();


    }


    public void initViews() {
        coopname = (TextView) findViewById(R.id.txtCoopName);
        businessadd = (TextView) findViewById(R.id.txtCoopAddress);
        txtreadername = (TextView) findViewById(R.id.txtreadername);
        txtpaymentdate = (TextView) findViewById(R.id.txtPaymentDate);
        txttxnid = (TextView) findViewById(R.id.txtTxnID);
        txtcurrentbill = (TextView) findViewById(R.id.txtCurrentBill);
        txtArrears = (TextView) findViewById(R.id.txtArrears);
        txttotalAmount = (TextView) findViewById(R.id.txttotalAmount);
        txtAccountId = (TextView) findViewById(R.id.txtAccountId);
        txtmacaddress = (TextView) findViewById(R.id.txtmacaddress);
        txtAccountName = (TextView) findViewById(R.id.txtAccountName);
        txtAddress = (TextView) findViewById(R.id.txtAddress);
        txtpolerental = (TextView) findViewById(R.id.txtpolerental);
        txtSpaceRental = (TextView) findViewById(R.id.txtSpaceRental);
        txtpilferagepenalty = (TextView) findViewById(R.id.txtpilferagepenalty);
        txtmeterdeposit = (TextView) findViewById(R.id.txtmeterdeposit);
        editpayment = (EditText) findViewById(R.id.editPayment);
        btnprint = (Button) findViewById(R.id.btnPrint);

    }


    public void setValues() {
        txttxnid.setText(ts);
        txtreadername.setText(MainActivity.reader.getReaderName());
        txtmacaddress.setText(CommonFunc.getMacAddress());
        txttxnid.setText(txnid);

        coopname.setText("Negros Oriental II Electric Cooperative");
        businessadd.setText("Real St., Dumaguete City");


        txtAccountId.setText(MainActivity.selectedAccount.getAccountID());
        txtAccountName.setText(MainActivity.selectedAccount.getFirstName()
                + " " + MainActivity.selectedAccount.getMiddleName()
                + " " + MainActivity.selectedAccount.getFullName());
        txtAddress.setText(MainActivity.selectedAccount.getAddress());
        txtpolerental.setText(MainActivity.selectedAccount.getPoleRental());
        txtpilferagepenalty.setText(MainActivity.selectedAccount.getPilferagePenalty());
        txtSpaceRental.setText(MainActivity.selectedAccount.getSpaceRental());
        txtmeterdeposit.setText(MainActivity.selectedAccount.getAdvancePayment());
        txttotalAmount.setText(MainActivity.dec.format(mbill.getTotalBilledAmount()));


    }

    public void setActions() {
        btnprint.setOnClickListener(this);
    }

    //region Triggeres

    @Override
    public void onClick(View v) {


        switch (v.getId()) {

            case R.id.btnPrint:

                btnprint.setEnabled(false);

                if (editpayment.getText().toString().isEmpty()) {
                    Toast.makeText(mcontext, "Please input payment.", Toast.LENGTH_SHORT).show();
                    btnprint.setEnabled(true);

                    return;
                }

                if (Double.parseDouble(editpayment.getText().toString()) <= 0) {
                    Toast.makeText(mcontext, "Please input payment.", Toast.LENGTH_SHORT).show();
                    btnprint.setEnabled(true);

                    return;
                }


                if (Float.parseFloat(editpayment.getText().toString()) < billedAmount) {
                    Toast.makeText(mcontext, "Invalid Payment", Toast.LENGTH_SHORT).show();
                    btnprint.setEnabled(true);

                    return;
                }


                if (!MainActivity.mIsConnected) {
                    Toast.makeText(mcontext, "Please connect to printer first, before printing.", Toast.LENGTH_SHORT).show();
                    btnprint.setEnabled(true);


                    return;
                }

                if (arrears > 0) {
                    Toast.makeText(mcontext, "Cannot accept payment. Accounts have Arrears advise to pay in Main Office.", Toast.LENGTH_SHORT).show();
                    btnprint.setEnabled(true);

                    return;
                }

//
//                int reVal = woosim.BTConnection(address, false);
//
//                byte[] init = {0x1b, '@'};
//                woosim.controlCommand(init, init.length);

//                woosim.saveSpool(EUC_KR, "  " + coopname.getText().toString() + "\n", 0, true);
//                woosim.saveSpool(EUC_KR, "    " + businessadd.getText().toString() + "\n", 0, true);
//
//

//                printText(" MARINDUQUE ELECTRIC COOPERATIVE, INC.\n", "");
//                printText("        Ihatub, Boac, Marinduque\n", "");
//                printText("             (MARELCO)", "");
//                woosim.saveSpool(EUC_KR, "        Acknowledgement Receipt\n", 0, true);
//                printText("Accounts No:" + txtAccountId.getText().toString() + "\n", "");
//                printText("Accounts Name:" + txtAccountName.getText().toString() + "\n", "");
//                printText("Address:" + txtAddress.getText().toString() + "\n", "");
//                printText("TransactionID:" + txttxnid.getText().toString() + "\n", "");
//                printText("Payment Date:" + txtpaymentdate.getText().toString(), "");
//                woosim.saveSpool(EUC_KR, "Payment Details\n", 0, true);
//                printText("     Bill Amount:" + txtcurrentbill.getText().toString() + "\n", "");
//                printText("Add:Arrears:" + txtArrears.getText().toString() + "\n", "");
//                printText("    Pole Rental", "" + 0.00 + "\n");
//                printText("    Space Rental", txtSpaceRental.getText().toString() + "\n");
//                printText("    Penalty(RA 7832)", txtpilferagepenalty.getText().toString() + "\n");
//                printText("Less:Meter Deposit", txtmeterdeposit.getText().toString() + "\n");
//                printText("     KVAT", txtkvatsubsidy.getText().toString() + "\n");
//                printText("Total Amount", txttotalAmount.getText().toString() + "\n");
//                printText("Paid Amount:" + editpayment.getText().toString() + "\n", "");
//                printText("Reader Name:" + txtreadername.getText().toString() + "\n", "");
//                printText("Equipment ID:" + txtmacaddress.getText().toString() + "\n", "");


                // woosim.saveSpool(EUC_KR, "Reader Name:" + txtreadername.getText().toString(), 0, true);
                // woosim.saveSpool(EUC_KR, "Equipment ID:" + txtmacaddress.getText().toString(), 0, true);

//                woosim.printSpool(true);

                // db.updtePaidAccounts(db, txtAccountId.getText().toString(), txttxnid.getText().toString(), editpayment.getText().toString());

                btnprint.setEnabled(true);

                this.finish();

                break;

        }

    }

//    @Override
//    public void onDestroy() {
//
//        super.onDestroy(); // Always call the superclass
//
//
//        if (mBtAdapter != null) {
//            mBtAdapter.cancelDiscovery();
//        }
//        // Unregister broadcast listeners
//        this.unregisterReceiver(mReceiver);
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bill, menu);

        // return super.onCreateOptionsMenu(menu);
        return false;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


       // menu.findItem(R.id.item5).setEnabled(false);
        menu.findItem(R.id.item4).setVisible(false);
        menu.findItem(R.id.item3).setVisible(false);
        menu.findItem(R.id.item2).setVisible(false);


        if (mIsConnected) {
            menu.getItem(0).setEnabled(false);
        } else {
            menu.getItem(0).setEnabled(true);
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item1:
                dialog.show();
                break;
        }
        return false;

    }

    //endregion


}
