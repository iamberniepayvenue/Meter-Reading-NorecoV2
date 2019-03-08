package com.payvenue.meterreader.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.payvenue.meterreader.R;

import java.util.ArrayList;

import Model.Account;

public class AccountAdapter extends BaseAdapter {

    ArrayList<Account> mList;
    Context c;
    private static final String TAG = "AccountAdapter";
    public AccountAdapter(ArrayList<Account> list) {
        this.mList = list;
    }

    public AccountAdapter(Context c, ArrayList<Account> list) {
        this.c = c;
        this.mList = list;

    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_accounts, viewGroup, false);
        }

        Log.e(TAG,"Current Page");

        Account account = mList.get(i);

        TextView tvname = convertView.findViewById(R.id.accountname);
        TextView tvaccount = convertView.findViewById(R.id.accountid);
        TextView tvserial = convertView.findViewById(R.id.serialmeter);
        TextView tvaccountstatus = convertView.findViewById(R.id.accountstatus);
        TextView tvsubclass = convertView.findViewById(R.id.accountsubclass);
        TextView tvaddress = convertView.findViewById(R.id.address);

        String fname = "";
        String mname = "";
        String lname = "";
        String fullname = "";
        try {
            fname = account.getFirstName();
             mname = account.getMiddleName();
             lname = account.getLastName();

             if(fname.equalsIgnoreCase("")) {
                 fname = "";
             }

             if(fname.equalsIgnoreCase(".")) {
                 fname = "";
             }

             if(mname.equalsIgnoreCase("")) {
                 mname = "";
             }

             if(mname.equalsIgnoreCase(".")) {
                 mname = "";
             }

             if(lname.equalsIgnoreCase("")) {
                 lname = "";
             }

             if(lname.equalsIgnoreCase(".")) {
                 lname = "";
             }

                fullname = fname.trim() + " " + mname.trim()+" "+ lname.trim();

        }catch (NullPointerException e) {
            Log.e(TAG,"NullPointerException: "+ e.getMessage());
        }


        tvname.setText(fullname.trim());
        tvaccount.setText(account.getAccountID());
        tvserial.setText(account.getMeterSerialNo());
        tvaccountstatus.setText(account.getAccountStatus());
        tvsubclass.setText(account.getSubClassification());
        tvaddress.setText(account.getAddress());


        return convertView;
    }
}
