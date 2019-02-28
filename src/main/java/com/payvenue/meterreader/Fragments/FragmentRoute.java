package com.payvenue.meterreader.Fragments;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.payvenue.meterreader.AccountListActivity;
import com.payvenue.meterreader.MainActivity;
import com.payvenue.meterreader.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentRoute extends Fragment {


    Bundle b;
    View view;
    String mode;
    ListView listview;
    ImageView ivNoAccounts;
    private static final String TAG = "FragmentRoute";

    public FragmentRoute() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_route, container, false);
        checkRouteIfHasAccounts();
        Bundle b = getArguments();

        if (b != null) {
            if (!b.getString("Mode").isEmpty())
                mode = b.getString("Mode");
        }

        Log.e(TAG,"Current Page["+mode+"]");

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        listview = view.findViewById(R.id.listView);
        ivNoAccounts = view.findViewById(R.id.iv_no_accounts);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getRoute();
            }
        },1000);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle b = new Bundle();
                String RouteCode = ((TextView) view.findViewById(R.id.txRouteCode)).getText().toString();
                b.putString("RouteCode", RouteCode);
                Intent intent = new Intent(view.getContext(), AccountListActivity.class);
                intent.putExtras(b);
                startActivityForResult(intent, 1);

            }
        });

    }

    public void checkRouteIfHasAccounts() {
        MainActivity.db.removeRoutes(MainActivity.db);
    }

    public void getRoute() {

        Cursor cursor = MainActivity.db.getRoutes(MainActivity.db);

        if (!cursor.isClosed()) {
            if(cursor.getCount() > 0) {
                String[] FromFieldNames = new String[]{"RouteCode",};

                int[] toViewIDs = new int[]{R.id.txRouteCode};

                // create adapter to map coloums of the database to the elements of
                // the UI
                SimpleCursorAdapter myCursorAdapter = new SimpleCursorAdapter(
                        getActivity(), // context
                        R.layout.route_list, // row_layout
                        cursor, // Cursor
                        FromFieldNames, // FromFields DataBaseColumns
                        toViewIDs // ToFields View IDs
                );

                listview.setAdapter(myCursorAdapter);
            }else{
                listview.setVisibility(View.GONE);
                ivNoAccounts.setVisibility(View.VISIBLE);
            }
        }


    }

}
