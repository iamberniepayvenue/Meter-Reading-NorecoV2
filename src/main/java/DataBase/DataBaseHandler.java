package DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.payvenue.meterreader.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import Model.Account;
import Model.Components;
import Model.LifeLineSubsidyModel;
import Model.Policies;
import Model.RateSegmentModel;
import Model.Reader;
import Model.ReadingDetailsModel;
import Model.Route;
import Utility.CommonFunc;


public class DataBaseHandler extends SQLiteOpenHelper {


    Context mcontext;
    public static final int database_version = 1;
    private static final String TAG = "DataBaseHandler";

    public DataBaseHandler(Context c) {
        super(c, DBInfo.DATABASE_NAME, null, database_version);
        mcontext = c;

    }



    @Override
    public void onCreate(SQLiteDatabase sql) {

        sql.execSQL(DBInfo.CREATE_ACCOUNTS);
        sql.execSQL(DBInfo.CREATE_SETTINGS);
        sql.execSQL(DBInfo.CREATE_ROUTES);
        sql.execSQL(DBInfo.CREATE_CONN);
        sql.execSQL(DBInfo.CREATE_RATECODE);
        sql.execSQL(DBInfo.CREATE_COMPONENT);
        sql.execSQL(DBInfo.CREATE_SCHEDULE);
        sql.execSQL(DBInfo.CREATE_SEGMENT);
        sql.execSQL(DBInfo.CREATE_POLICY);
        sql.execSQL(DBInfo.CREATE_UTILITY);
        sql.execSQL(DBInfo.CREATE_FOUNDMETERS);
        sql.execSQL(DBInfo.CREATE_LIFELINEDISCOUNT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

//    public Cursor chkIsUploaded(DataBaseHandler db) {
//        SQLiteDatabase sql = db.getReadableDatabase();
//        Cursor c = sql.rawQuery("SELECT * FROM " + DBInfo.TBLACCOUNTINFO + " Where AccountStatus != 'Deactivated' AND UploadStatus = 0", null);
//        return c;
//    }

    public long chkIsUploaded(DataBaseHandler db) {
        SQLiteDatabase sql = db.getReadableDatabase();
        //long cnt = DatabaseUtils.queryNumEntries(sql,DBInfo.TBLACCOUNTINFO,DBInfo.UploadStatus + "= ? AND " + DBInfo.AccountStatus + "!=?",new String[]{"0","DC"});
        long cnt = DatabaseUtils.queryNumEntries(sql,DBInfo.TBLACCOUNTINFO);
        sql.close();
        return cnt;
    }

    public void errorDownLoad(DataBaseHandler db) {
        SQLiteDatabase sql = db.getReadableDatabase();
        sql.execSQL("Delete from " + DBInfo.TBLACCOUNTINFO + "");
        sql.execSQL("Delete from " + DBInfo.TBLRoutes + "");
        sql.execSQL("Delete from " + DBInfo.TBLConn_settings + "");
        sql.execSQL("Delete from " + DBInfo.TBLRateCode + "");
        sql.execSQL("Delete from " + DBInfo.TBLRateSegment + "");
        sql.execSQL("Delete from " + DBInfo.TBlRateComponent + "");
        sql.execSQL("Delete from " + DBInfo.TBLPolicy + "");
        sql.execSQL("Delete from " + DBInfo.TBlRateSchedule + "");
        sql.execSQL("Delete from " + DBInfo.TBLSettings + "");
        sql.execSQL("Delete from " + DBInfo.TBLLifeLineDiscount + "");
        sql.execSQL("Delete from sqlite_sequence");
        sql.close();
        db.close();
    }

    public void dropTable(DataBaseHandler db, String tblname) {
        SQLiteDatabase sql = db.getReadableDatabase();
        sql.execSQL("DROP TABLE IF EXISTS " + tblname + "");
        if (tblname == DBInfo.TBLRoutes) {
            sql.execSQL(DBInfo.CREATE_ROUTES);
            sql.execSQL("Delete from sqlite_sequence");
        } else if (tblname == DBInfo.TBLSettings) {
            sql.execSQL(DBInfo.CREATE_SETTINGS);
        }

        sql.close();
        db.close();
    }

    public void saveRoute(DataBaseHandler db, Route route) {

        SQLiteDatabase sql = db.getReadableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DBInfo.COOPID, route.getCoopID());
        cv.put(DBInfo.ReaderID, route.getReaderID());
        cv.put(DBInfo.DistrictID, route.getDistrictID());
        cv.put(DBInfo.RouteID, route.getRouteID());
        cv.put(DBInfo.AccountIDFrom, route.getAccountIDFrom());
        cv.put(DBInfo.AccountIDTo, route.getAccountIDTo());
        //cv.put(DBInfo.DueDate, route.getDueDate());

        sql.insert(DBInfo.TBLRoutes, null, cv);
        sql.close();
        db.close();

    }

    public String getDistrictID(DataBaseHandler db,String routeID) {
        String district = "";
        SQLiteDatabase sq = db.getReadableDatabase();
        Cursor cursor = sq.query(DBInfo.TBLRoutes,new String[]{DBInfo.DistrictID},DBInfo.RouteID + "=?",new String[]{routeID},null,null,null,"1");
        while (cursor.moveToNext()) {
            district = cursor.getString(cursor.getColumnIndex(DBInfo.DistrictID));
        }
        return district;
    }

    public String getReaderID(DataBaseHandler db) {
        String readerID = "";
        SQLiteDatabase sq = db.getReadableDatabase();
        Cursor cursor = sq.query(DBInfo.TBLSettings,new String[]{DBInfo.ReaderID},null,null,null,null,null);
        while (cursor.moveToNext()) {
            readerID = cursor.getString(cursor.getColumnIndex(DBInfo.ReaderID));
        }
        return readerID;
    }

    public boolean checkRouteIsExist(DataBaseHandler db,String routeID,String districtID) {
        boolean res = false;
        SQLiteDatabase sq = db.getReadableDatabase();

        Cursor cursor = sq.query(DBInfo.TBLRoutes,null,DBInfo.RouteID + "=? AND "+ DBInfo.DistrictID + "=?",new String[]{routeID,districtID},null,null,null);

        if(cursor.getCount() > 0) {
            res = true;
        }

        return res;
    }

    public void syncSettingsInfo(DataBaseHandler db, Reader reader) {

        SQLiteDatabase sql = db.getReadableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DBInfo.COOPID, reader.getCoopID());
        cv.put(DBInfo.ReaderID, reader.getReaderID());
        cv.put(DBInfo.ReaderName, reader.getReaderName());

        sql.insert(DBInfo.TBLSettings, null, cv);

        sql.close();
        db.close();
    }

    public void saveConnection(DataBaseHandler db, String coopid,
                               String hostname, String port) {

        SQLiteDatabase sql = db.getReadableDatabase();

        sql.execSQL("Delete from " + DBInfo.TBLConn_settings + "");

        ContentValues cv = new ContentValues();
        cv.put(DBInfo.COOPID, coopid);
        cv.put(DBInfo.Host, hostname);
        cv.put(DBInfo.Port, port);

        sql.insert(DBInfo.TBLConn_settings, null, cv);

        sql.close();
        db.close();

    }

    public int saveRateCode(DataBaseHandler db, String coopid,
                             String coopname, String ratecode, String details, String isactive) {

        SQLiteDatabase sql = db.getReadableDatabase();
        // sql.execSQL("Delete from "+DBInfo.TBLRateCode+"");

        ContentValues data = new ContentValues();

        data.put(DBInfo.COOPID, coopid);
        data.put(DBInfo.CoopName, coopname);
        data.put(DBInfo.RateCode, ratecode);
        data.put(DBInfo.Details, details);
        data.put(DBInfo.IsActive, isactive);
        data.put(DBInfo.Extra1, ".");
        data.put(DBInfo.Extra2, ".");
        data.put(DBInfo.Notes1, ".");
        data.put(DBInfo.Notes2, ".");

        long save = sql.insert(DBInfo.TBLRateCode, null, data);
        sql.close();
        db.close();

        return  (int)save;
    }

    public int saveCoopDetails(DataBaseHandler db, String coopid,
                                String coopname, String cooptype, String classification,
                                String readingduedate, String acronym, String billingcode,
                                String businessaddress, String telnum, String tinnum) {

        SQLiteDatabase sql = db.getReadableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(DBInfo.COOPID, coopid);
        cv.put(DBInfo.CoopName, coopname);
        cv.put(DBInfo.CoopType, cooptype);
        cv.put(DBInfo.Classification, classification);
        cv.put(DBInfo.ReadingToDueDate, readingduedate);
        cv.put(DBInfo.Acronym, acronym);
        cv.put(DBInfo.BillingCode, billingcode);
        cv.put(DBInfo.BusinessAddress, businessaddress);
        cv.put(DBInfo.TelNo, telnum);
        cv.put(DBInfo.TinNo, tinnum);

        long save = sql.insert(DBInfo.TBLUtility, null, cv);
        sql.close();
        db.close();
        return (int)save;
    }

    public int saveRateComponent(DataBaseHandler db, String coopid,
                                  String coopname, String ratecomponent, String details,
                                  String isactive, String notes1) {

        SQLiteDatabase sql = db.getReadableDatabase();
        // sql.execSQL("Delete from "+DBInfo.TBlRateComponent+"");

        ContentValues data = new ContentValues();

        data.put(DBInfo.COOPID, coopid);
        data.put(DBInfo.CoopName, coopname);
        data.put(DBInfo.RateComponent, ratecomponent);
        data.put(DBInfo.Details, details);
        data.put(DBInfo.IsActive, isactive);
        data.put(DBInfo.Extra1, ".");
        data.put(DBInfo.Extra2, ".");
        data.put(DBInfo.Notes1, notes1);
        data.put(DBInfo.Notes2, ".");

        long save = sql.insert(DBInfo.TBlRateComponent, null, data);
        sql.close();
        db.close();
        return (int)save;
    }

    public ArrayList<Components> getRateComponent(DataBaseHandler db) {
        ArrayList<Components> list = new ArrayList<>();
        SQLiteDatabase sql = db.getReadableDatabase();
        Cursor cursor = sql.query(DBInfo.TBlRateComponent,new String[]{DBInfo.RateComponent,DBInfo.Details},null,null,null,null,null);

        if(cursor.moveToFirst()){
            while (!cursor.isAfterLast()) {
                String rateComponent = cursor.getString(cursor.getColumnIndex(DBInfo.RateComponent));
                String details = cursor.getString(cursor.getColumnIndex(DBInfo.Details));
                list.add(new Components(rateComponent,details));
                cursor.moveToNext();
            }
        }

        sql.close();
        return list;
    }

    public int saveRateSegment(DataBaseHandler db, String coopid,
                                String ratesegmentcode, String ratesegmentname, String details,
                                String isactive) {

        SQLiteDatabase sql = db.getReadableDatabase();
        // sql.execSQL("DELETE From "+DBInfo.TBLRateSegment+"");

        ContentValues data = new ContentValues();

        data.put(DBInfo.COOPID, coopid);
        data.put(DBInfo.RateSegmentCode, ratesegmentcode);
        data.put(DBInfo.RateSegmentName, ratesegmentname);
        data.put(DBInfo.Details, details);
        data.put(DBInfo.IsActive, isactive);
        data.put(DBInfo.Extra1, ".");
        data.put(DBInfo.Extra2, ".");
        data.put(DBInfo.Notes1, ".");
        data.put(DBInfo.Notes2, ".");

        long save = sql.insert(DBInfo.TBLRateSegment, null, data);
        sql.close();
        db.close();
        return (int)save;
    }

    public int saveBillingPolicy(DataBaseHandler db,String policycode,String policytype, String customerclass,
                                  String minkwh, String maxkwh, String percentamount) {

        SQLiteDatabase sql = db.getReadableDatabase();
        ContentValues data = new ContentValues();

        //data.put(DBInfo.COOPID, coopid);
        //data.put(DBInfo.CoopName, coopname);
        data.put(DBInfo.PolicyCode, policycode);
        //data.put(DBInfo.PolicyName, policyname);
        data.put(DBInfo.PolicyType, policytype);
        data.put(DBInfo.CustomerClass, customerclass);
        //data.put(DBInfo.SubClass, subclass);
        data.put(DBInfo.MinkWh, minkwh);
        data.put(DBInfo.MaxkWh, maxkwh);
        data.put(DBInfo.PercentAmount, percentamount);

        long save = sql.insert(DBInfo.TBLPolicy, null, data);
        sql.close();
        db.close();
        return (int)save;
    }

    public int saveRateSchedule(DataBaseHandler db,String ratesegment, String ratecomponent, String printorder,
                                 String classification,String rateschedtype,
                                 String amount,String isOverUnder,String islifeline, String isscdiscount,String extra1,String isExport) {

        SQLiteDatabase sql = db.getReadableDatabase();
        // sql.execSQL("DELETE From "+DBInfo.TBlRateSchedule+"");

        ContentValues data = new ContentValues();

        //data.put(DBInfo.COOPID, coopid);
        data.put(DBInfo.RateSegment, ratesegment);
        data.put(DBInfo.RateComponent, ratecomponent);
        data.put(DBInfo.PrintOrder, printorder);
        data.put(DBInfo.Classification, classification);
        //data.put(DBInfo.RateSched, ratesched);
        data.put(DBInfo.RateSchedType, rateschedtype);
        data.put(DBInfo.Amount, amount);
        //data.put(DBInfo.VATRate, vatrate);
        //data.put(DBInfo.VATAmount, vatamount);
        //data.put(DBInfo.FranchiseTaxRate, franchisetaxrate);
        //data.put(DBInfo.FranchiseTaxAmount, franchisetaxamount);
        //data.put(DBInfo.LocalTaxRate, localtaxrate);
        //data.put(DBInfo.LocalTaxAmount, localtaxamount);
        //data.put(DBInfo.TotalAmount, totalamount);
        //data.put(DBInfo.IsVAT, isvat);
        //data.put(DBInfo.IsDVAT, isdvat);
        data.put(DBInfo.IsOverUnder,isOverUnder);
        //data.put(DBInfo.IsFranchiseTax, isfranchisetax);
        //data.put(DBInfo.IsLocalTax, islocaltax);
        data.put(DBInfo.IsLifeLine, islifeline);
        data.put(DBInfo.IsSCDiscount, isscdiscount);
        //data.put(DBInfo.RateStatus, ratestatus);
        //data.put(DBInfo.DateAdded, dateadded);
        data.put(DBInfo.IsExport,isExport);
        data.put(DBInfo.Extra1, extra1);
        data.put(DBInfo.Extra2, ".");
        data.put(DBInfo.Notes1, ".");
        data.put(DBInfo.Notes2, ".");

        long save = sql.insert(DBInfo.TBlRateSchedule, null, data);
        sql.close();
        db.close();
        return (int)save;
    }


    public int saveAccount(DataBaseHandler db, Account account, String details,String routeID) {
        int count = 0;

        SQLiteDatabase sql = db.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBInfo.DateSync, CommonFunc.getDateOnly());
        cv.put(DBInfo.DateRead, CommonFunc.getDateOnly());
        cv.put(DBInfo.DateUploaded, CommonFunc.getDateOnly());
        cv.put(DBInfo.COOPID, account.getCoopID());
        cv.put(DBInfo.FirstName, account.getFirstName());
        cv.put(DBInfo.MiddleName, account.getMiddleName());
        cv.put(DBInfo.LastName, account.getLastName());
        cv.put(DBInfo.TownCode, account.getTownCode());
        cv.put(DBInfo.RouteNo, routeID);
        cv.put(DBInfo.AccountID, account.getAccountID());
        cv.put(DBInfo.AccountType, account.getAccountType());
        cv.put(DBInfo.AccountClassification, account.getAccountClassification());
        cv.put(DBInfo.SubClassification, account.getSubClassification());
        cv.put(DBInfo.SequenceNo, account.getSequenceNo());
        cv.put(DBInfo.ReadStatus, "Unread");
        cv.put(DBInfo.EditCount, 0);
        cv.put(DBInfo.PrintCount, 0);
        cv.put(DBInfo.DueDate, account.getDueDate());
        cv.put(DBInfo.DisoDate, account.getDisoDate());
        cv.put(DBInfo.AccountStatus, account.getAccountStatus());
        cv.put(DBInfo.ReadingDetails, details);
        cv.put(DBInfo.UploadStatus, account.getUploadStatus());
        cv.put(DBInfo.MeterSerialNo, account.getMeterSerialNo());
        //cv.put(DBInfo.PoleRental,account.getPoleRental());
        //cv.put(DBInfo.SpaceRental,account.getSpaceRental());
        //cv.put(DBInfo.PilferagePenalty,account.getPilferagePenalty());
        cv.put(DBInfo.UnderOverRecovery,account.getUnderOverRecovery());
        cv.put(DBInfo.LastReadingDate,account.getLastReadingDate());
        cv.put(DBInfo.Averaging,account.getAveraging());
        cv.put(DBInfo.IsNetMetering,account.getIsNetMetering());
        cv.put(DBInfo.IsCheckSubMeterType,account.getIsCheckSubMeterType());
        cv.put(DBInfo.CheckMeterAccountNo,account.getCheckMeterAccountNo());
        cv.put(DBInfo.CheckMeterName,account.getCheckMeterName());
        cv.put(DBInfo.Coreloss,account.getCoreloss());
        sql.insert(DBInfo.TBLACCOUNTINFO, null, cv);

        sql.close();
        db.close();
        return  1;
    }

    public void updateSerialNumber(DataBaseHandler db,String accountID,String newMeterSerial) {
        SQLiteDatabase sql = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBInfo.Extra1,newMeterSerial);
        sql.update(DBInfo.TBLACCOUNTINFO, cv, "AccountID='" + accountID + "'", null);
        db.close();
        sql.close();
    }

    public long getAccountSaveCount(DataBaseHandler db, String routeId) {
        long c = 0;
        SQLiteDatabase sql = db.getReadableDatabase();

        String query = "select count(RouteNo) as count from accounts where RouteNo = '" + routeId +"'";
        Cursor cursor = sql.rawQuery(query,null);
        while (cursor.moveToNext()) {
            c = cursor.getLong(cursor.getColumnIndex("count"));
        }

        return c;
    }

    // get Rate Schedule
    public Cursor getRateSchedule(DataBaseHandler db) {

        SQLiteDatabase sql = db.getReadableDatabase();

        String query = "SELECT * From " + DBInfo.TBlRateSchedule
                + " ORDER BY RateSched,Classification ASC";

        Cursor c = sql.rawQuery(query, null);

        return c;

    }

    // get Billing Policy
    public ArrayList<Policies> getBillingPolicy(DataBaseHandler db, String classification) {
        ArrayList<Policies> list = new ArrayList<>();
        SQLiteDatabase sql = db.getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT * From " + DBInfo.TBLPolicy
                + " Where CustomerClass = '"+ classification +"' ORDER BY PolicyCode ASC;", null);

        if(c.moveToFirst()) {
            while(!c.isAfterLast()) {
                String policyCode = c.getString(c.getColumnIndex(DBInfo.PolicyCode));
                String policyType = c.getString(c.getColumnIndex(DBInfo.PolicyType));
                String cusClass = c.getString(c.getColumnIndex(DBInfo.PolicyType));
                String minKWH = c.getString(c.getColumnIndex(DBInfo.MinkWh));
                String maxKWH = c.getString(c.getColumnIndex(DBInfo.MaxkWh));
                String percentAmount = c.getString(c.getColumnIndex(DBInfo.PercentAmount));
                list.add(new Policies(policyCode,policyType,cusClass,minKWH,maxKWH,percentAmount));
                c.moveToNext();
            }
        }

        return list;
    }


    public Cursor getRoutes(DataBaseHandler db) {
        SQLiteDatabase sql = db.getReadableDatabase();
        Cursor c = sql.rawQuery(
                "SELECT (DistrictID||'-'||RouteID ) AS RouteCode ,_id FROM "
                        + DBInfo.TBLRoutes, null);


        return c;
    }

    public Cursor getRoutesNo(DataBaseHandler db) {
        SQLiteDatabase sql = db.getReadableDatabase();
        Cursor c = sql.rawQuery(
                "SELECT RouteID FROM " + DBInfo.TBLRoutes, null);
        return c;
    }

    public ArrayList<Account> getAccountList(DataBaseHandler db, String RouteCode, String mode, String filter) {

        ArrayList<Account> myList = new ArrayList<>();

        SQLiteDatabase sql = db.getReadableDatabase();
        String myQuery = "";
        Gson gson = new GsonBuilder().create();

        String sub = RouteCode.substring(0,3);
        String routeID = RouteCode.replace(sub,"");

        myQuery = "Select * From " + DBInfo.TBLACCOUNTINFO + " Where " +
                " ReadStatus = '" + mode + "' AND  RouteNo = '" + routeID + "' "; //
        //(TownCode||'-'||RouteNo)

        if (filter.equalsIgnoreCase("LastName")) {
            myQuery = myQuery + " Order By AccountID ASC,LastName ASC";
        } else {
            myQuery = myQuery + " Order BY AccountID, cast(IFNULL(SequenceNo, 99999) as REAL) ASC ";
        }

        Cursor c = sql.rawQuery(myQuery, null);


        Account account = null;
        String details = null;

        if (c.getCount() > 0)
            while (c.moveToNext()) {
                try{
                    details = c.getString(c.getColumnIndex("ReadingDetails"));
                    account = gson.fromJson(details.toString(), Account.class);
                    account.setLastName(c.getString(c.getColumnIndex("LastName")));
                    account.setAccountID(c.getString(c.getColumnIndex("AccountID")));
                    account.setMeterSerialNo(c.getString(c.getColumnIndex("MeterSerialNo")));
                    account.setAccountStatus(c.getString(c.getColumnIndex("AccountStatus")));
                    account.setUploadStatus(c.getString(c.getColumnIndex("UploadStatus")));
                    account.setSubClassification(c.getString(c.getColumnIndex("SubClassification")));
                    account.setDueDate(c.getString(c.getColumnIndex("DueDate")));
                    account.setPrintCount(c.getString(c.getColumnIndex("PrintCount")));
                    account.setEditCount(c.getString(c.getColumnIndex("EditCount")));
                    myList.add(account);
                }catch (JsonSyntaxException e) {
                    Log.e(TAG,"jsonexception : " + e.getMessage());
                }

            }

        c.close();
        sql.close();
        db.close();

        return myList;

    }

    public Cursor getAccountList(DataBaseHandler db, String mode) {

        SQLiteDatabase sql = db.getReadableDatabase();
        String myQuery = "";

        myQuery = "Select a.*,r.DistrictID From " + DBInfo.TBLACCOUNTINFO + " a Left Join " + DBInfo.TBLRoutes + " r On a.RouteNo = r.RouteID Where " +
                "(a.ReadStatus = '" + mode + "') And UploadStatus = '"+ 0 +"'";


        Cursor c = sql.rawQuery(myQuery, null);

        return c;

    }

    public Cursor getRateSched(DataBaseHandler db, String ratesched,
                               String acctclass) {
        SQLiteDatabase sql = db.getReadableDatabase();

        String myQuery = "SELECT * From " + DBInfo.TBlRateSchedule
                + " Where " + DBInfo.Classification + " Like'" + acctclass + "' Order By RateSegment,PrintOrder";
        Cursor c = sql.rawQuery(myQuery, null);
        return c;
    }

    public void updateReadAccount(DataBaseHandler db, String status,boolean isStopMeterCheck) {
        String isStopMeter = "0";

        if(isStopMeterCheck) {
            isStopMeter = "1";
        }

        SQLiteDatabase sql = db.getReadableDatabase();
        ContentValues cv = new ContentValues();
        Gson gson = new GsonBuilder().create();
        Account a = MainActivity.selectedAccount;
        ReadingDetailsModel readingDetailsModel;
        if(MainActivity.selectedAccount.getIsCheckSubMeterType().equalsIgnoreCase("M") || MainActivity.selectedAccount.getIsCheckSubMeterType().equalsIgnoreCase("m")) {
            readingDetailsModel = new ReadingDetailsModel(a.getBillMonth(),a.getPrevBilling(),a.getAddress(), a.getSeniorCitizenStatus(), a.getSCExpiryDate(),
                    a.getPenalty(),a.getRateSched(),a.getMultiplier(),a.getDemandKW(),a.getAdvancePayment(),a.getBillDeposit(),a.getLastReadingDate(),
                    a.getInitialReading(),a.getIsChangeMeter(),a.getMeterBrand(),a.getConsume(),a.getReading(),
                    a.getRemarks(),a.getLatitude(),a.getLongitude(),a.getTotalLifeLineDiscount(),a.getTotalSCDiscount(),null,a.getOverUnderDiscount(),
                    isStopMeter,a.getExportConsume(),a.getExportReading(),a.getExportPreviousReading(),a.getSeniorSubsidy(),a.getLifeLineSubsidy(),a.getIsNetMetering());
        }else {
            readingDetailsModel = new ReadingDetailsModel(a.getBillMonth(), a.getPrevBilling(), a.getAddress(), a.getSeniorCitizenStatus(), a.getSCExpiryDate(),
                    a.getPenalty(), a.getRateSched(), a.getMultiplier(), a.getDemandKW(), a.getAdvancePayment(), a.getBillDeposit(), a.getLastReadingDate(),
                    a.getInitialReading(), a.getIsChangeMeter(), a.getMeterBrand(), a.getConsume(), a.getReading(),
                    a.getRemarks(), a.getLatitude(), a.getLongitude(), a.getTotalLifeLineDiscount(), a.getTotalSCDiscount(), a.getBill(), a.getOverUnderDiscount(),
                    isStopMeter, a.getExportConsume(), a.getExportReading(), a.getExportPreviousReading(), a.getSeniorSubsidy(), a.getLifeLineSubsidy(), a.getIsNetMetering());
        }

        String readingDetails = gson.toJson(readingDetailsModel);
        cv.put(DBInfo.ReadStatus, status);
        cv.put(DBInfo.ReadingDetails, readingDetails);
        cv.put(DBInfo.EditCount, CommonFunc.toDigit(MainActivity.selectedAccount.getEditCount()) + 1);
        cv.put(DBInfo.Extra2,MainActivity.selectedAccount.getActualConsumption());
        sql.update(DBInfo.TBLACCOUNTINFO, cv, "AccountID='" + MainActivity.selectedAccount.getAccountID() + "'", null);
        sql.close();
        db.close();
    }

    public void updateAccountToPrinted(DataBaseHandler db,String status) {
        SQLiteDatabase sql = db.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBInfo.ReadStatus, status);
        cv.put(DBInfo.PrintCount,CommonFunc.toDigit(MainActivity.selectedAccount.getPrintCount()) + 1);
        sql.update(DBInfo.TBLACCOUNTINFO, cv, "AccountID='" + MainActivity.selectedAccount.getAccountID() + "'", null);
        sql.close();
        db.close();
    }


    public int searchNextAccountToRead(DataBaseHandler db, String routeno,String sequenceNumber,String _accountID) {
        SQLiteDatabase sql = db.getReadableDatabase();
        int tmpSequence = 0;
        String accountID = "";
        int ctr = 0;
        Cursor cursor;
        cursor= sql.query(DBInfo.TBLACCOUNTINFO,null,DBInfo.RouteNo+"=? AND "+DBInfo.ReadStatus+"=?",new String[]{routeno,"Unread"},null,null,DBInfo.SequenceNo + " ASC");

        if(cursor.getCount() > 0){
            while(cursor.moveToNext()) {

                int sequence = cursor.getInt(cursor.getColumnIndex(DBInfo.SequenceNo));


                if(ctr == 0) {
                    tmpSequence = sequence;
                    accountID = cursor.getString(cursor.getColumnIndex(DBInfo.AccountID));
                }else{
                    if(sequence < tmpSequence) {
                        tmpSequence = sequence;
                        accountID = cursor.getString(cursor.getColumnIndex(DBInfo.AccountID));
                    }
                }
                ctr++;
            }
            getAccountDetails(db,accountID);
        }
        return  cursor.getCount();
    }



    public void getAccountDetails(DataBaseHandler db, String accountid) {

        SQLiteDatabase sql = db.getReadableDatabase();

        String myQuery;
        Gson gson = new GsonBuilder().create();

        myQuery = "Select * From " + DBInfo.TBLACCOUNTINFO + " Where AccountID = '" + accountid + "' ";
        Cursor c = sql.rawQuery(myQuery, null);

        String details;

        Account account;

        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                details = c.getString(c.getColumnIndex("ReadingDetails"));
                account = gson.fromJson(details, Account.class);
                account.setUploadStatus(c.getString(c.getColumnIndex("UploadStatus")));
                account.setDueDate(c.getString(c.getColumnIndex("DueDate")));
                account.setPrintCount(c.getString(c.getColumnIndex("PrintCount")));
                account.setEditCount(c.getString(c.getColumnIndex("EditCount")));
                account.setLastName(c.getString(c.getColumnIndex("LastName")));
                account.setFirstName(c.getString(c.getColumnIndex(DBInfo.FirstName)));
                account.setMiddleName(c.getString(c.getColumnIndex(DBInfo.MiddleName)));
                account.setAccountID(c.getString(c.getColumnIndex("AccountID")));
                account.setMeterSerialNo(c.getString(c.getColumnIndex("MeterSerialNo")));
                account.setAccountStatus(c.getString(c.getColumnIndex("AccountStatus")));
                account.setAccountClassification(c.getString(c.getColumnIndex(DBInfo.AccountClassification)));
                account.setSubClassification(c.getString(c.getColumnIndex(DBInfo.SubClassification)));
                account.setPoleRental(c.getString(c.getColumnIndex(DBInfo.PoleRental)));
                account.setSpaceRental(c.getString(c.getColumnIndex(DBInfo.SpaceRental)));
                account.setPilferagePenalty(c.getString(c.getColumnIndex(DBInfo.PilferagePenalty)));
                account.setUnderOverRecovery(c.getString(c.getColumnIndex(DBInfo.UnderOverRecovery)));
                account.setSubClassification(c.getString(c.getColumnIndex(DBInfo.SubClassification)));
                account.setSequenceNo(c.getString(c.getColumnIndex(DBInfo.SequenceNo)));
                account.setRouteNo(c.getString(c.getColumnIndex(DBInfo.RouteNo)));
                account.setDateRead(c.getString(c.getColumnIndex(DBInfo.DateRead)));
                account.setReadStatus(c.getString(c.getColumnIndex(DBInfo.ReadStatus)));
                account.setActualConsumption(c.getString(c.getColumnIndex(DBInfo.Extra2)));
                String ave = c.getString(c.getColumnIndex(DBInfo.Averaging));
                account.setIsCheckSubMeterType(c.getString(c.getColumnIndex(DBInfo.IsCheckSubMeterType)));
                account.setCheckMeterAccountNo(c.getString(c.getColumnIndex(DBInfo.CheckMeterAccountNo)));
                account.setCheckMeterName(c.getString(c.getColumnIndex(DBInfo.CheckMeterName)));
                account.setCoreloss(c.getString(c.getColumnIndex(DBInfo.Coreloss)));
                account.setIsNetMetering(c.getString(c.getColumnIndex(DBInfo.IsNetMetering)));
                account.setPrintCount(c.getString(c.getColumnIndex(DBInfo.PrintCount)));

                try {
                    JSONObject object = new JSONObject(ave);
                    account.setAveraging(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG,"ave : " + e.getMessage());
                }
                MainActivity.selectedAccount = account;
            }
        }

        c.close();

        sql.close();
        db.close();

    }



    public ArrayList<Account> searchAccount(DataBaseHandler db, String routeCode, String searchKey, String status) {

        String substring = routeCode.substring(0,3);
        String routeID = routeCode.replace(substring,"");

        ArrayList<Account> myList = new ArrayList<>();

        SQLiteDatabase sql = db.getReadableDatabase();
        String myQuery = "";
        Gson gson = new GsonBuilder().create();

        myQuery = "Select * From " + DBInfo.TBLACCOUNTINFO;


        if (routeCode.equalsIgnoreCase("All") || routeCode.isEmpty()) {
            myQuery = myQuery + " Where ReadStatus='" + status + "' AND  (AccountID Like '%" + searchKey + "%' OR MeterSerialNo Like '%" + searchKey + "%')";
        } else {
            myQuery = myQuery + " Where ReadStatus='" + status + "' AND RouteNo = '" + routeID + "' AND (AccountID Like '%" + searchKey + "%' OR MeterSerialNo Like '%" + searchKey + "%')";
        }



        Cursor c = sql.rawQuery(myQuery, null);


        Account account = null;
        String details = null;

        if (c.getCount() > 0)
            while (c.moveToNext()) {
                details = c.getString(c.getColumnIndex("ReadingDetails"));
                account = gson.fromJson(details, Account.class);
                account.setUploadStatus(c.getString(c.getColumnIndex("UploadStatus")));
                account.setDueDate(c.getString(c.getColumnIndex("DueDate")));
                account.setPrintCount(c.getString(c.getColumnIndex("PrintCount")));
                account.setEditCount(c.getString(c.getColumnIndex("EditCount")));
                account.setAccountClassification(c.getString(c.getColumnIndex(DBInfo.AccountClassification)));
                account.setSubClassification(c.getString(c.getColumnIndex(DBInfo.SubClassification)));
                account.setLastName(c.getString(c.getColumnIndex(DBInfo.LastName)));
                account.setMiddleName(c.getString(c.getColumnIndex(DBInfo.MiddleName)));
                account.setFirstName(c.getString(c.getColumnIndex(DBInfo.FirstName)));
                account.setAccountID(c.getString(c.getColumnIndex(DBInfo.AccountID)));
                account.setPoleRental(c.getString(c.getColumnIndex(DBInfo.PoleRental)));
                account.setSpaceRental(c.getString(c.getColumnIndex(DBInfo.SpaceRental)));
                account.setPilferagePenalty(c.getString(c.getColumnIndex(DBInfo.PilferagePenalty)));
                account.setUnderOverRecovery(c.getString(c.getColumnIndex(DBInfo.UnderOverRecovery)));
                account.setSubClassification(c.getString(c.getColumnIndex(DBInfo.SubClassification)));
                account.setMeterSerialNo(c.getString(c.getColumnIndex(DBInfo.MeterSerialNo)));
                account.setSequenceNo(c.getString(c.getColumnIndex(DBInfo.SequenceNo)));
                account.setRouteNo(c.getString(c.getColumnIndex(DBInfo.RouteNo)));
                String ave = c.getString(c.getColumnIndex(DBInfo.Averaging));
                account.setIsCheckSubMeterType(c.getString(c.getColumnIndex(DBInfo.IsCheckSubMeterType)));
                account.setCheckMeterAccountNo(c.getString(c.getColumnIndex(DBInfo.CheckMeterAccountNo)));
                account.setCheckMeterName(c.getString(c.getColumnIndex(DBInfo.CheckMeterName)));
                account.setCoreloss(c.getString(c.getColumnIndex(DBInfo.Coreloss)));
                account.setIsNetMetering(c.getString(c.getColumnIndex(DBInfo.IsNetMetering)));
                try {
                    JSONObject object = new JSONObject(ave);
                    account.setAveraging(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG,"ave : " + e.getMessage());
                }

                myList.add(account);
            }

        c.close();
        sql.close();
        db.close();

        return myList;

    }

    public void getReader(DataBaseHandler db) {

        SQLiteDatabase sql = db.getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT * FROM " + DBInfo.TBLSettings + "",
                null);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                MainActivity.reader = new Reader(c.getString(c.getColumnIndex("CoopID")),
                        c.getString(c.getColumnIndex("ReaderID")),
                        c.getString(c.getColumnIndex("ReaderName")));
            }
        }

        c.close();
        sql.close();
        db.close();
    }


    public ArrayList<RateSegmentModel> getRateSegment(DataBaseHandler db) {
        ArrayList<RateSegmentModel> list = new ArrayList<>();
        SQLiteDatabase sql = db.getReadableDatabase();
        String query = "SELECT r.RateSegmentCode,r.RateSegmentName,r.Details From " + DBInfo.TBLRateSegment
                + " r Left Join " +DBInfo.TBlRateSchedule +" rs On r.RateSegmentCode = rs.RateSegment Where rs.Classification Like '" + MainActivity.selectedAccount.getAccountClassification() + "' Group By r.RateSegmentCode Order By r.RateSegmentCode";
        Cursor c = sql.rawQuery(query, null);

        if(c.moveToFirst()) {
            while (!c.isAfterLast()) {

                String rateSegmentCode = c.getString(c.getColumnIndex("RateSegmentCode"));
                String rateSegmentName = c.getString(c.getColumnIndex("RateSegmentName"));
                String rateDetails = c.getString(c.getColumnIndex("Details"));
                list.add(new RateSegmentModel(rateSegmentCode,rateSegmentName,rateDetails));

                c.moveToNext();
            }
        }

        return list;
    }

    public Cursor getConnectionSettings(DataBaseHandler db) {
        SQLiteDatabase sql = db.getReadableDatabase();
        Cursor c = sql.rawQuery(
                "SELECT * From " + DBInfo.TBLConn_settings + "", null);
        return c;
    }


    public void updateUploadStaus(DataBaseHandler db, String colid, String status, String flag) {

        SQLiteDatabase sql = db.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBInfo.DateUploaded, CommonFunc.getDateOnly());
        cv.put(DBInfo.UploadStatus, flag);
        sql.update(DBInfo.TBLACCOUNTINFO, cv, "_id=" + colid, null);
        sql.close();
        db.close();

    }

    public Cursor getFoundMeters(DataBaseHandler db) {

        SQLiteDatabase sql = db.getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT * From " + DBInfo.TBlFound_Meters + " Where Extra1='0'", null);

        return c;

    }

    public void saveFoundMeter(DataBaseHandler db, String accountnumber, String meterserial, String reading, String remarks, Double latitude, Double longitude) {

        SQLiteDatabase sql = db.getWritableDatabase();

        ContentValues data = new ContentValues();

        data.put(DBInfo.AccountID, accountnumber);
        data.put(DBInfo.MeterSerialNo, meterserial);
        data.put(DBInfo.Reading, reading);
        data.put(DBInfo.DateRead, CommonFunc.getDateOnly());
        data.put(DBInfo.Remarks, remarks);
        data.put(DBInfo.Latitude, latitude);
        data.put(DBInfo.Longitude,longitude);
        data.put(DBInfo.Extra1, 0);

        sql.insert(DBInfo.TBlFound_Meters, null, data);


    }


    public void updateFoundMeters(DataBaseHandler db, String columnid, String flag) {
        SQLiteDatabase sql = db.getReadableDatabase();
        ContentValues data = new ContentValues();
        data.put(DBInfo.Extra1, flag);// 1 is uploaded : 0 is not
        sql.update(DBInfo.TBlFound_Meters, data, "_id=" + columnid, null);
        sql.close();
        db.close();
    }

    public int getDataCountThisRoute(DataBaseHandler db,String routeID) {
        SQLiteDatabase sql = db.getReadableDatabase();
        String strQuery = "Select * From " + DBInfo.TBLACCOUNTINFO + " Where RouteNo = '"+routeID+"'";
        Cursor c = sql.rawQuery(strQuery, null);
        return c.getCount();
    }

    public int getDataCount(DataBaseHandler db, String status) {


        SQLiteDatabase sql = db.getReadableDatabase();

        String strQuery = "Select * From " + DBInfo.TBLACCOUNTINFO + "";

        if (status.equalsIgnoreCase("read")) {
            strQuery += " Where ReadStatus='Read' Or ReadStatus='Printed'";
        } else if (status.equalsIgnoreCase("unread")) {
            strQuery += " Where ReadStatus='Unread'";
        } else {
            strQuery += " Where UploadStatus='1'";
        }


        Cursor c = sql.rawQuery(strQuery, null);

        return c.getCount();

    }

    public int getRateScheduleCountNonHigherVoltage(DataBaseHandler db) {
        SQLiteDatabase sql = db.getReadableDatabase();
        String strQuery = "Select * From " + DBInfo.TBlRateSchedule + " Where " + DBInfo.Classification + " != 'Higher Voltage'" ;
        Cursor c = sql.rawQuery(strQuery, null);
        db.close();
        return c.getCount();
    }

    public int getRateScheduleCountHigherVoltage(DataBaseHandler db) {
        SQLiteDatabase sql = db.getReadableDatabase();
        String strQuery = "Select * From " + DBInfo.TBlRateSchedule + " Where " + DBInfo.Classification + " = 'Higher Voltage'" ;
        Cursor c = sql.rawQuery(strQuery, null);
        db.close();
        return c.getCount();
    }

    public void resetAllAccounts(DataBaseHandler db) {
        SQLiteDatabase sql = db.getReadableDatabase();
        ContentValues data = new ContentValues();
        data.put(DBInfo.UploadStatus, 0);
        sql.update(DBInfo.TBLACCOUNTINFO, data, "ReadStatus!='NotFound'", null);
        sql.close();
        db.close();
    }

    public int getEditAttemp(DataBaseHandler db,String accountID) {
        SQLiteDatabase sql = db.getReadableDatabase();
        int count = 0;

        Cursor cursor = sql.query(DBInfo.TBLACCOUNTINFO,new String[]{DBInfo.EditCount},DBInfo.AccountID+"=?",new String[]{accountID},null,null,null);
        if(cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                count = cursor.getInt(cursor.getColumnIndex(DBInfo.EditCount));
            }
        }

        return count;
    }

    /**Life Liner/ Lifeline Subsidy*/

    public int saveLifeLifePolicy(DataBaseHandler db,String kwh,String percent,String decimal) {
        SQLiteDatabase sql = db.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBInfo.LifelineConsumption,kwh);
        cv.put(DBInfo.LifelinePercentage,percent);
        cv.put(DBInfo.LifelineInDecimal,decimal);
        long save = sql.insert(DBInfo.TBLLifeLineDiscount,null,cv);
        sql.close();
        db.close();
        return (int)save;
    }

    public ArrayList<LifeLineSubsidyModel> getLifeLinePolicy(DataBaseHandler db) {
        ArrayList<LifeLineSubsidyModel> list = new ArrayList<>();
        SQLiteDatabase sql = db.getReadableDatabase();
        Cursor cursor = sql.rawQuery("Select * From " +DBInfo.TBLLifeLineDiscount,null);
        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                String kwh = cursor.getString(cursor.getColumnIndex(DBInfo.LifelineConsumption));
                String percent = cursor.getString(cursor.getColumnIndex(DBInfo.LifelinePercentage));
                String decimal = cursor.getString(cursor.getColumnIndex(DBInfo.LifelineInDecimal));
                list.add(new LifeLineSubsidyModel(kwh,percent,decimal));
                cursor.moveToNext();
            }
        }
        cursor.close();
        sql.close();
        db.close();
        return list;
    }
}