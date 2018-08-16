package com.payvenue.meterreader.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.payvenue.meterreader.Accounts;
import com.payvenue.meterreader.Camera.ZBarScannerActivity;
import com.payvenue.meterreader.MainActivity;
import com.payvenue.meterreader.R;

import java.util.ArrayList;

import DataBase.DataBaseHandler;
import Model.Account;
import Utility.GPSTracker;
import ZBar.ZBarConstants;

import static com.payvenue.meterreader.MainActivity.iScanner;
import static com.payvenue.meterreader.MainActivity.mDecodeResult;

public class FragmentReading extends Fragment implements OnClickListener {

    public FragmentReading() {
    }

    View rootView;
    String searchSerial;
    static EditText txtSerial;
    ImageButton btnScan;
    Button btnSearch;
    String RouteArea;

    GPSTracker gps;

    public static final int ZBAR_SCANNER_REQUEST = 0;
    public static final int ZBAR_QR_SCANNER_REQUEST = 5;
    private static final String TAG ="FragmentReading";

    ArrayList<String> routeList = new ArrayList<String>();

    ArrayAdapter<String> spinnerArrayAdapter;
    Spinner spinRoute;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        Log.e(TAG,"Current Page");
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();

        initButton();

        initRoutes();

        initSpinner();



        spinRoute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                spinRoute.setSelection(pos);
                ((TextView) spinRoute.getSelectedView()).setTextColor(Color.parseColor("#999999"));
                RouteArea = spinRoute.getSelectedItem().toString();

            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


    }


    //region Functions


    public void initViews() {
        txtSerial = rootView.findViewById(R.id.txtSearchSerial);
        btnScan = rootView.findViewById(R.id.imageButton1);
        //btnScan.setVisibility(View.GONE);
        btnSearch = rootView.findViewById(R.id.btnsearchserial);
        spinRoute = rootView.findViewById(R.id.spinner1);
    }

    public void initButton() {
        btnSearch.setOnClickListener(this);
        btnScan.setOnClickListener(this);
    }

    public void initSpinner() {

        spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, routeList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinRoute.setAdapter(spinnerArrayAdapter);

    }

    public void initRoutes() {

        routeList.add("All");
        Cursor c = MainActivity.db.getRoutes(MainActivity.db);
        while (c.moveToNext()) {
            routeList.add(c.getString(c.getColumnIndex("RouteCode")));
        }
    }

    //endregion

    public static class ScanResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (iScanner != null) {
                try {
                    //mDecodeResult.recycle();


                    //mDecodeResult.recycle();
                    iScanner.aDecodeGetResult(mDecodeResult);
                    Log.d("Data", mDecodeResult.decodeValue);
                    setValue(mDecodeResult.decodeValue);
                    //  barTypeView.setText(mDecodeResult.symName);
                    //  resultView.setText(mDecodeResult.decodeValue);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void setValue(String serial) {
        txtSerial.setText(serial);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        switch (requestCode) {
            case ZBAR_SCANNER_REQUEST:
            case ZBAR_QR_SCANNER_REQUEST:
                if (resultCode == getActivity().RESULT_OK) {

                    ToneGenerator toneG = new ToneGenerator(
                            AudioManager.STREAM_ALARM, 100);
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

                    if (data != null) {
                        txtSerial.setText(data
                                .getStringExtra(ZBarConstants.SCAN_RESULT));
                    }
                }

                break;

            case 1:
                txtSerial.setText("");
                break;

            case 2:
                txtSerial.setText("");
                //}
                break;
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnsearchserial:

                DataBaseHandler db = new DataBaseHandler(getActivity());
                searchSerial = txtSerial.getText().toString();
                if (searchSerial.length() == 0) {
                    Toast.makeText(this.getActivity(),
                            "Invalid Meter Serial Number.", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    ArrayList<Account> accounts = MainActivity.db.searchAccount(db, RouteArea, searchSerial,"Unread");

                    if (accounts.size() == 0) {
                        Toast.makeText(this.getActivity(), "No Data found.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (accounts.size() == 1) {
                        Intent intent = new Intent(getActivity(), Accounts.class);
                        MainActivity.selectedAccount = accounts.get(0);
                        startActivityForResult(intent, 1);
                    }

                    if (accounts.size() > 1) {
                        Toast.makeText(this.getActivity(), "To many Data is found.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                break;

            case R.id.imageButton1:


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(), "You declined to allow the app to access your camera", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (isCameraAvailable()) {
                    Intent intent = new Intent(getActivity(), ZBarScannerActivity.class);
                    startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
                } else {
                    Toast.makeText(getActivity(), "Rear Facing Camera Unavailable",
                            Toast.LENGTH_SHORT).show();
                }

                break;
        }

    }

    public boolean isCameraAvailable() {
        PackageManager pm = getActivity().getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView(); // Always call the superclass
        android.os.Debug.stopMethodTracing();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

    }


}


