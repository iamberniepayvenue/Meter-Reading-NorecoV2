package com.payvenue.meterreader;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
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
import com.payvenue.meterreader.provider.SearchSuggestionProvider;

import java.util.ArrayList;

import Model.Account;

public class AccountListActivity extends AppCompatActivity {

    ListView listvew;
    String currentfilter = "";
    String routecode;
    private static final String TAG = "AccountListActivity";

    ArrayList<Account> myAccounts = new ArrayList<>();
    ArrayList<Account> searchAccount = new ArrayList<>();
    ImageView ivNoAccounts;

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
        ivNoAccounts = findViewById(R.id.iv_no_accounts);
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

        try{
            myAccounts = MainActivity.db.getAccountList(MainActivity.db, routecode, MainActivity.myMode, filter);

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
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        if(searchView != null) {
            searchAccount.clear();
            // Assumes current activity is the searchable activity
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

                        Log.e(TAG,"here: "+ s);
                        searchAccount = MainActivity.db.searchItem(MainActivity.db,s);
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
                    Log.e(TAG,"close");
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

//        if(id == R.id.search) {
//            super.onSearchRequested();
//        }

//        if(id == R.id.clear_search) {
//            clearSearchHistory();
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleSearch();
    }


    private void handleSearch() {
        searchAccount.clear();
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);

//            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
//                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
//            suggestions.saveRecentQuery(searchQuery, null);

        }else if(Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.e(TAG,"handleSearch: "+ intent.getData());
            Log.e(TAG,"handleSearch: "+ intent.getDataString());
            Log.e(TAG,"handleSearch: "+ intent.getScheme());
        }
    }

    public void clearSearchHistory(){
        new SearchRecentSuggestions(this,SearchSuggestionProvider.AUTHORITY,SearchSuggestionProvider.MODE)
                .clearHistory();
    }

    //endregion
}
