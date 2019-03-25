package com.payvenue.meterreader;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.payvenue.meterreader.Adapter.AccountAdapter;

import java.util.ArrayList;

import Model.Account;
import Model.Route;

public class AccountListActivity extends AppCompatActivity {

    ListView listvew;
    String currentfilter = "";
    String routecode;
    String routePrimarykey;
    Snackbar snackbar;
    private static final String TAG = "AccountListActivity";

    ArrayList<Account> myAccounts = new ArrayList<>();
    ArrayList<Account> searchAccount = new ArrayList<>();
    ImageView ivNoAccounts;
    private ArrayList<Route> routeList;
    private int routeCount;
    SearchView searchView;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);
        Log.e(TAG,TAG);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Grab the data to display on this activity
        Bundle b = getIntent().getExtras();
        routecode = b.getString("RouteCode");
        routePrimarykey = b.getString("RouteID"); // primary key
        String[] district = routecode.split("-");
        final String districtno = district[0];
        String routeno = district[1];
        Log.e(TAG,"routeno: "+routeno);
        ivNoAccounts = findViewById(R.id.iv_no_accounts);
        listvew = (ListView) findViewById(R.id.list);
        listvew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setEnabled(false);
                String accountid = ((TextView) view.findViewById(R.id.accountid)).getText().toString();
                Bundle b = new Bundle();
                b.putInt("purpose", 2222);
                b.putString("disctrictNo",districtno);
                MainActivity.db.getAccountDetails(MainActivity.db, accountid,routecode,routePrimarykey,0);
                Intent intent = new Intent(view.getContext(), ViewDetails.class);
                intent.putExtras(b);
                startActivityForResult(intent, 1);
            }
        });


        routeCount = MainActivity.db.countsOfRouteNoInRoutesTable(MainActivity.db,routeno,districtno);

        getSearchData(routecode, currentfilter);

    }

    public void setSnackbar(String msg) {

        snackbar = Snackbar.make(findViewById(R.id.relativeLayout_activity_accounts), msg, Snackbar.LENGTH_LONG);
    }

    private void getSearchData(String routecode, String filter) {

        try{
            MainActivity.selectedRouteList = MainActivity.db.getRoute(MainActivity.db,routePrimarykey);

            if(routeCount > 1) {
                myAccounts = MainActivity.db.getAccountList(MainActivity.db, routecode, MainActivity.myMode, filter,1);
            }else{
                myAccounts = MainActivity.db.getAccountList(MainActivity.db, routecode, MainActivity.myMode, filter,0);
            }

            if(myAccounts.size() > 0) {
                AccountAdapter adapter = new AccountAdapter(myAccounts);
                listvew.setAdapter(adapter);
            }else{
                ivNoAccounts.setVisibility(View.VISIBLE);
                listvew.setVisibility(View.GONE);
            }

        }catch (NullPointerException e) {
            Log.e(TAG,e.getMessage());
        }
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
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.psearch).getActionView();

        if(searchView != null) {
            searchAccount.clear();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);

            MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.psearch), new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    getSearchData(routecode, currentfilter);
                    return true;
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
                        searchAccount = MainActivity.db.searchItem(MainActivity.db,s,MainActivity.myMode,routePrimarykey,currentfilter); //add primary key in routemodel during download
                        if(searchAccount.isEmpty()) {
                            setSnackbar("No results found...");
                            snackbar.show();
                        }

                        AccountAdapter adapter = new AccountAdapter(searchAccount);
                        listvew.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
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

                    getSearchData(routecode, currentfilter);

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                getSearchData(routecode, currentfilter);
                this.finish();
                break;
            case R.id.filter_serial_number:
                currentfilter = "MeterSerialNo";
                break;
            case R.id.filter_name:
                currentfilter = "LastName";
                getSearchData(routecode, currentfilter);
                break;
            case R.id.filter_account_id:
                currentfilter = "AccountID";
                getSearchData(routecode, currentfilter);
                break;

            case R.id.turn_off_filter:
                currentfilter = "";
                getSearchData(routecode, currentfilter);
                break;
        }

        updateMenuTitle();
        return super.onOptionsItemSelected(item);
    }

    private void updateMenuTitle() {

        if(!currentfilter.equalsIgnoreCase("")) {
            searchView.setQueryHint(currentfilter);
        } else {
            searchView.setQueryHint("Search");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        //handleSearch();
    }
}
