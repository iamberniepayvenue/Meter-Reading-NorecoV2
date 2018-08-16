package com.payvenue.meterreader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.payvenue.meterreader.Adapter.AccountAdapter;

import java.util.ArrayList;

import Model.Account;

public class AccountListActivity extends AppCompatActivity {

    ListView listvew;
    String currentfilter = "";
    String routecode;
    private static final String TAG = "AccountListActivity";

    ArrayList<Account> myAccounts = new ArrayList<>();

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Grab the data to display on this activity
        Bundle b = getIntent().getExtras();
        routecode = b.getString("RouteCode");
        Log.e(TAG,"routecode: " + routecode);
        Log.e(TAG,"Current Page");
        listvew = (ListView) findViewById(R.id.list);
        listvew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setEnabled(false);
                String accountid = ((TextView) view.findViewById(R.id.accountid)).getText().toString();
                Bundle b = new Bundle();
                b.putInt("purpose", 2222);
                MainActivity.db.getAccountDetails(MainActivity.db, accountid);
                Intent intent = new Intent(view.getContext(), ViewDetails.class);
                intent.putExtras(b);
                startActivityForResult(intent, 1);
            }

        });


        getSearchData(routecode, currentfilter);

    }

    private void getSearchData(String routecode, String filter) {

        myAccounts = MainActivity.db.getAccountList(MainActivity.db, routecode, MainActivity.myMode, filter);

        AccountAdapter adapter = new AccountAdapter(myAccounts);
        listvew.setAdapter(adapter);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        switch (requestCode) {


            case 1:

                getSearchData(routecode, currentfilter);

                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pending__accounts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }

        if (id == R.id.filter_account) {
            currentfilter = "Sequnce Number";
            getSearchData(routecode, currentfilter);
        }

        if (id == R.id.filter_name) {
            currentfilter = "LastName";
            getSearchData(routecode, currentfilter);
        }

        return super.onOptionsItemSelected(item);
    }


    //endregion
}
