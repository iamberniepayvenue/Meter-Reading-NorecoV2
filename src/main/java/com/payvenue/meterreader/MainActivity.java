package com.payvenue.meterreader;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapswithme.maps.api.MapsWithMeApi;
import com.payvenue.meterreader.Fragments.FragmentDownLoad;
import com.payvenue.meterreader.Fragments.FragmentFound;
import com.payvenue.meterreader.Fragments.FragmentNotFound;
import com.payvenue.meterreader.Fragments.FragmentReading;
import com.payvenue.meterreader.Fragments.FragmentRoute;
import com.payvenue.meterreader.Fragments.FragmentUpload;
import com.payvenue.meterreader.Fragments.RatesFragment;
import com.woosim.bt.WoosimPrinter;

import java.text.DecimalFormat;
import java.util.Set;

import DataBase.DataBaseHandler;
import Model.Account;
import Model.ConnSettings;
import Model.Reader;
import Utility.CommonFunc;
import Utility.GPSTracker;
import Utility.MobilePrinter;
import Utility.WebRequest;
import device.scanner.DecodeResult;
import device.scanner.IScannerService;


public class MainActivity extends AppCompatActivity {
    private static Thread thread;
    private static Handler handler;


    // private DataBaseHandler datasource;

    // int prevPosition = 0;
    public CharSequence mTitle;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    public static GPSTracker gps;
    private Toolbar toolbar;
    public static DataBaseHandler db;
    public static NavigationView navigationView;
    final int CAMERA_PERMISSION_REQUEST_CODE = 00001;
    final int REQUEST_READ_PHONE_STATE = 110,
            REQUEST_ACCESS_FINE_LOCATION = 111,
            REQUEST_WRITE_STORAGE = 112;
    public static IScannerService iScanner = null;
    public static DecodeResult mDecodeResult = new DecodeResult();
    public static String currFragment = null;
    public static MobilePrinter printer;

    static private BluetoothAdapter mBtAdapter;
    public static ArrayAdapter<String> mPairedDevicesArrayAdapter;
    static ArrayAdapter<String> mNewDevicesArrayAdapter;
    static String address;
    public static Dialog dialog;
    static boolean mIsConnected = false;

    public static View header;
    public static TextView tv_userid;
    public static TextView tv_name;
    public static String macAddress;
    public static Account selectedAccount;
    public static DecimalFormat dec;
    public static DecimalFormat dec2;
    public static WebRequest webRequest;
    public static String myMode;
    public static Reader reader;
    public static ConnSettings connSettings;
    //public static Cursor cursorRateSegment;
    //public static ArrayList<Components> componentsList = new ArrayList<>();



    static WoosimPrinter woosim;
    static private byte[] cardData ;
    //static private byte[] cardData = new byte[113];
    static private byte[] extractdata = new byte[300];
    static String EUC_KR = "EUC-KR";
    static final int LINE_CHARS = 62;
    private static final String TAG = "MainActivity";

    public interface Modes {

        String MODE_1 = "Unread";
        String MODE_2 = "Read";
        String MODE_3 = "Printed";
        String Mode_6 = "Paid";
        String MODE_4 = "NotFound";
        String MODE_5 = "Found";
    }



    @SuppressWarnings("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);
        setResult(Activity.RESULT_CANCELED);

        //woosim = new WoosimPrinter();
        //woosim.setHandle(acthandler);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.option);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dec = new DecimalFormat("#,###,###,###.####");
        dec.setMinimumFractionDigits(4);

        dec2 = new DecimalFormat("#,###,###,###.##");
        dec2.setMinimumFractionDigits(2);

        db = new DataBaseHandler(this);
        webRequest = new WebRequest(this);
        gps = new GPSTracker(this);

        printer = MobilePrinter.getInstance(this);


        MapsWithMeApi.isMapsWithMeInstalled(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.option, // nav menu toggle icon
                R.string.app_name, // nav drawer open - description for
                R.string.app_name // nav drawer close - description for
        ) {

            @Override
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();


            }

            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();

            }

        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);


        //region Navigation

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                mDrawerLayout.closeDrawer(GravityCompat.START);

                int id = menuItem.getItemId();

                Bundle b = new Bundle();


                Fragment fragment = null;

                switch (id) {
                    case R.id.addreader:
                        // METER READER
                        currFragment = "Home";
                        myMode = Modes.MODE_1;
                        fragment = new FragmentReading();
                        break;
                    case R.id.pendingacct:
                        // PENDING ACCOUNTS

                        b.putString("Mode", Modes.MODE_1);
                        myMode = Modes.MODE_1;
                        fragment = new FragmentRoute();

                        break;

                    case R.id.readacct:
                        // READ ACCOUNTS

                        b.putString("Mode", Modes.MODE_2);
                        myMode = Modes.MODE_2;
                        fragment = new FragmentRoute();
                        break;

                    case R.id.printedacct:
                        // PRINTED

                        b.putString("Mode", Modes.MODE_3);
                        myMode = Modes.MODE_3;
                        fragment = new FragmentRoute();
                        break;


                    case R.id.notfound:
                        // NOT FOUND

                        b.putString("Mode", Modes.MODE_4);
                        myMode = Modes.MODE_4;
                        fragment = new FragmentNotFound();
                        break;

                    case R.id.found:
                        // FOUND

                        b.putString("Mode", Modes.MODE_5);
                        myMode = Modes.MODE_5;
                        fragment = new FragmentFound();
                        break;

                    case R.id.download:
                        // DOWNLOAD

                        fragment = new FragmentDownLoad();
                        break;

                    case R.id.upload:
                        // UPLOAD
                        fragment = new FragmentUpload();
                        break;

                    case R.id.rates:
                        // RATES
                        fragment = new RatesFragment();
                        break;

                    case R.id.logout:
                        // EXIT
                        finish();
                        break;
                    default:
                        break;
                }


                if (fragment != null) {
                    fragment.setArguments(b);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment).commit();
                }

                return true;
            }
        });

        //endregion

        Fragment deffragment = new FragmentReading();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frame_container, deffragment).commit();


        macAddress = CommonFunc.getMacAddress();

        CheckPermissions();
        setReader();
        setConnSettings();
        exportLogo();

    }//end of create

    private void exportLogo() {

        if(!CommonFunc.hasExternalStoragePrivateFile(this,"noreco_logo.bmp")) {
            CommonFunc.createExternalStoragePrivateFile(this);
        }
    }

    public static void setConnSettings() {
        Cursor conn_dr = db.getConnectionSettings(db);

        conn_dr.moveToFirst();
        if (conn_dr.getCount() > 0) {
            do {
                connSettings = new ConnSettings(conn_dr.getString(0),
                        conn_dr.getString(1),
                        conn_dr.getString(2));
            } while (conn_dr.moveToNext());
        }

    }

    public static void setReader() {

        db.getReader(db);

        // Display current reader name

        header = navigationView.getHeaderView(0);
        tv_userid = (TextView) header.findViewById(R.id.tv_userid);
        tv_name = (TextView) header.findViewById(R.id.tv_name);


        if (MainActivity.reader != null) {
            tv_name.setText(reader.getReaderName());
            tv_userid.setText(reader.getReaderID());
        }


    }


    public void CheckPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }


            boolean hasPermissionPhoneState = (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermissionPhoneState) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        REQUEST_READ_PHONE_STATE);

            }

            boolean hasPermissionLocation = (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermissionLocation) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_FINE_LOCATION);
            } else {

                if (gps.canGetLocation()) {

                    Toast.makeText(getBaseContext(), "" + gps.getLatitude(), Toast.LENGTH_SHORT).show();

                } else {
                    gps.showSettingAlert();
                }
            }

            boolean hasPermissionWrite = (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermissionWrite) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Start your camera handling here
                } else {
                    //Toast.makeText(getBaseContext(), "You declined to allow the app to access your camera", Toast.LENGTH_SHORT).show();
                }
            case REQUEST_READ_PHONE_STATE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                }
            }
            case REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    if (gps.canGetLocation()) {

                        Toast.makeText(getBaseContext(), "" + gps.getLatitude(), Toast.LENGTH_SHORT).show();

                    } else {
                        gps.showSettingAlert();
                    }

                } else {
                }
            }

            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.menu_scan:
                InitializedPrinter();
                dialog.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);

    }


    @Override
    public void setTitle(CharSequence title) {

        mTitle = title;


    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * <p>
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);


    }

    @Override
    public void onDestroy() {

        super.onDestroy();


        android.os.Debug.stopMethodTracing();

        if (iScanner != null) {
            try {
                iScanner.aDecodeAPIDeinit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        iScanner = null;


    }

    @Override
    public void onPause() {

        super.onPause();

    }

    @Override
    public void onResume() {

        super.onResume();

    }

    public void onRestart() {

        super.onRestart();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_MENU) {

        }
        return super.onKeyDown(keyCode, event);
    }


    //region printing

    public void InitializedPrinter() {

        dialog = new Dialog(new ContextThemeWrapper(this,
                android.R.style.Theme_Holo_Light));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.device_list);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) dialog
                .findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) dialog
                .findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        // this.unregisterReceiver(mReceiver);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        try{
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices.size() > 0) {

                dialog.findViewById(R.id.title_paired_devices).setVisibility(
                        View.VISIBLE);
                for (BluetoothDevice device : pairedDevices) {
                    mPairedDevicesArrayAdapter.add(device.getName() + "\n"
                            + device.getAddress());
                }
            } else {
                String noDevices = "No devices have been paired";
                mPairedDevicesArrayAdapter.add(noDevices);
            }
        }catch (NullPointerException e) {
            Log.e(TAG,""+e.getMessage());
        }
    }



    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    //mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                //setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    //mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };


    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            mBtAdapter.cancelDiscovery();
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            address = info.substring(info.length() - 17);

            int reVal = printer.setConnection(address);

            if (reVal == 1) {
                Toast t = Toast.makeText(getBaseContext(), "SUCCESS CONNECTION!", Toast.LENGTH_SHORT);
                mIsConnected = true;
                t.show();
            } else if (reVal == -2) {
                Toast t = Toast.makeText(getBaseContext(), "NOT CONNECTED", Toast.LENGTH_SHORT);
                t.show();
            } else if (reVal == -5) {
                Toast t = Toast.makeText(getBaseContext(), "DEVICE IS NOT BONDED", Toast.LENGTH_SHORT);
                t.show();
            } else if (reVal == -6) {
                Toast t = Toast.makeText(getBaseContext(), "ALREADY CONNECTED", Toast.LENGTH_SHORT);
                t.show();
            } else if (reVal == -8) {
                Toast t = Toast.makeText(getBaseContext(), "Please enable your Bluetooth and re-run this program!", Toast.LENGTH_LONG);
                t.show();
            } else {
                Toast t = Toast.makeText(getBaseContext(), "ELSE", Toast.LENGTH_SHORT);
                t.show();
            }

            dialog.dismiss();

        }
    };

}
