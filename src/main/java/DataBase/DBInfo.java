package DataBase;

import android.provider.BaseColumns;

public class DBInfo implements BaseColumns {

    // Databse Name
    public static final String DATABASE_NAME = "mpa_reader_app";

    // Table Name
    public static final String TBLACCOUNTINFO = "accounts";
    public static final String TBLSettings = "settings";
    public static final String TBLRoutes = "routes";
    public static final String TBLConn_settings = "connection_settings";
    public static final String TBLAccount_Billing = "account_billing";
    public static final String TBlFound_Meters = "found_meters";
    public static final String TBLThreshold = "threshold";

    // Table For on-site printing
    public static final String TBLRateCode = "rate_code";
    public static final String TBlRateComponent = "rate_component";
    public static final String TBlRateSchedule = "rate_schedule";
    public static final String TBLRateSegment = "rate_segment";
    public static final String TBLPolicy = "billing_policy";
    public static final String TBLUtility = "power_utility";
    public static final String TBLLifeLineDiscount = "lifeline_discount";

    // Field
    /**Life line discount*/
    public static final String LifelineConsumption = "LifelineConsumption";
    public static final String LifelinePercentage = "LifelinePercentage";
    public static final String LifelineInDecimal = "LifelineInDecimal";


    public static String COOPID = "CoopID";
    public static String ReaderID = "ReaderID";
    public static String ReaderName = "ReaderName";

    public static String DistrictID = "DistrictID";
    public static String RouteID = "RouteID";
    public static String AccountIDFrom = "AccountIDFrom";
    public static String AccountIDTo = "AccountIDTo";
    public static String TagClass = "TagClass";
    public static String DownloadRef = "DownloadRef";
    public static String SequenceNoFrom = "SequenceNoFrom";
    public static String SequenceNoTo = "SequenceNoTo";



    public static String Host = "Host";
    public static String Port = "Port";

    public static String CoopName = "CoopName";
    public static String RateCode = "RateCode";
    public static String RateComponent = "RateComponent";
    public static String Details = "Details";
    public static String IsActive = "IsActive";


    public static String RateSegment = "RateSegment";
    public static String PrintOrder = "PrintOrder";
    public static String Classification = "Classification";
    public static String RateSched = "RateSched";
    public static String RateSchedType = "RateSchedType";
    public static String Amount = "Amount";
    public static String VATRate = "VATRate";
    public static String VATAmount = "VATAmount";
    public static String FranchiseTaxRate = "FranchiseTaxRate";
    public static String FranchiseTaxAmount = "FranchiseTaxAmount";
    public static String LocalTaxRate = "LocalTaxRate";
    public static String LocalTaxAmount = "LocalTaxAmount";
    public static String TotalAmount = "TotalAmount";
    public static String IsVAT = "IsVAT";
    public static String IsDVAT = "IsDVAT";
    public static String IsFranchiseTax = "IsFranchiseTax";
    public static String IsLocalTax = "IsLocalTax";
    public static String IsLifeLine = "IsLifeLine";
    public static String IsSCDiscount = "IsSCDiscount";
    public static String RateStatus = "RateStatus";
    public static String DateAdded = "DateAdded";
    public static String IsOverUnder = "IsOverUnder";
    public static String IsExport = "IsExport";


    public static String RateSegmentCode = "RateSegmentCode";
    public static String RateSegmentName = "RateSegmentName";

    public static String PolicyCode = "PolicyCode";
    public static String PolicyName = "PolicyName";
    public static String PolicyType = "PolicyType";
    public static String CustomerClass = "CustomerClass";
    public static String SubClass = "SubClass";
    public static String MinkWh = "MinkWh";
    public static String MaxkWh = "MaxkWh";
    public static String PercentAmount = "PercentAmount";

    public static String CoopType = "CoopType";
    public static String ReadingToDueDate = "ReadingToDueDate";
    public static String Acronym = "Acronym";
    public static String BillingCode = "BillingCode";
    public static String BusinessAddress = "BusinessAddress";
    public static String TelNo = "TelNo";
    public static String TinNo = "TinNo";

    public static String DateUploaded = "DateUploaded";
    public static String DateSync = "DateSync";
    public static String FirstName = "FirstName";
    public static String MiddleName = "MiddleName";
    public static String LastName = "LastName";
    public static String TownCode = "TownCode";
    public static String RouteNo = "RouteNo";
    public static String AccountType = "AccountType";
    public static String AccountClassification = "AccountClassification";
    public static String SubClassification = "SubClassification";
    public static String SequenceNo = "SequenceNo";
    public static String ReadStatus = "ReadStatus";
    public static String EditCount = "EditCount";
    public static String PrintCount = "PrintCount";
    public static String DueDate = "DueDate";
    public static String DisoDate = "DisoDate";
    public static String AccountStatus = "AccountStatus";
    public static String ReadingDetails = "ReadingDetails";
    public static String UploadStatus = "UploadStatus";
    public static String PoleRental = "PoleRental";
    public static String SpaceRental = "SpaceRental";
    public static String PilferagePenalty = "PilferagePenalty";
    public static String UnderOverRecovery = "UnderOverRecovery";
    public static String BillDeposit = "BillDeposit";
    public static String Averaging = "Averaging";
    public static String Arrears = "Arrears";
    public static String IsNetMetering = "IsNetMetering";
    public static String IsCheckSubMeterType = "IsCheckSubMeterType";
    public static String CheckMeterAccountNo = "CheckMeterAccountNo";
    public static String CheckMeterName = "CheckMeterName";
    public static String Coreloss = "Coreloss";
    public static String ExportDateCounter = "ExportDateCounter";
    public static String ExportBill = "ExportBill";
    public static String kWhReading = "kWhReading";

    //public static String Consume = "Consume";
    //public static String NewReading = "NewReading";

    public static String AccountID = "AccountID";
    public static String MeterSerialNo = "MeterSerialNo";
    public static String Reading = "Reading";
    public static String DateRead = "DateRead";
    public static String LastReadingDate = "LastReadingDate";
    public static String Coordinates = "Coordinates";
    public static String Latitude = "Latitude";
    public static String Longitude = "Longitude";
    public static String Remarks = "Remarks";

    /**threshold table*/
    public static String SettingsCode = "SettingsCode";
    public static String ThresholdPercentage = "ThresholdPercentage";

    public static String Extra1 = "Extra1";
    public static String Extra2 = "Extra2";
    public static String Extra3 = "Extra3";
    public static String Extra4 = "Extra4";
    public static String Notes1 = "Notes1";
    public static String Notes2 = "Notes2";





    public static final String CREATE_LIFELINEDISCOUNT = " CREATE TABLE " + TBLLifeLineDiscount
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + LifelineConsumption + " TEXT,"
            + LifelinePercentage + " TEXT," + LifelineInDecimal + " TEXT)";

    public static final String CREATE_SETTINGS = "CREATE TABLE " + TBLSettings
            + "(" + COOPID + " TEXT, " + ReaderID + " TEXT,"
            + ReaderName + " TEXT)";

    public static final String CREATE_ROUTES = "CREATE TABLE " + DBInfo.TBLRoutes
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT ," + COOPID
            + " TEXT," + ReaderID + " TEXT, " + DistrictID
            + " TEXT, " + RouteID + " TEXT, " + AccountIDFrom
            + " TEXT, " + AccountIDTo + " TEXT, " + DueDate
            + " TEXT, " + TagClass + " TEXT, " + DownloadRef
            + " TEXT, " + SequenceNoFrom + " TEXT, " + SequenceNoTo + " TEXT)";

    public static final String CREATE_CONN = "CREATE TABLE "
            + DBInfo.TBLConn_settings + "(" + COOPID + " TEXT ,"
            + Host + " TEXT, " + Port + " TEXT)";

    public static final String CREATE_RATECODE = "CREATE TABLE " + DBInfo.TBLRateCode
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT , " + COOPID
            + " TEXT ," + CoopName + " TEXT, " + RateCode
            + " TEXT , " + Details + " TEXT ," + IsActive
            + " TEXT , " + Extra1 + " TEXT ," + Extra2
            + " TEXT, " + Notes1 + " TEXT, " + Notes2
            + " TEXT)";


    public static final String CREATE_COMPONENT = "CREATE TABLE "
            + DBInfo.TBlRateComponent
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT ," + COOPID + " TEXT , "
            + CoopName + " TEXT, "
            + RateComponent + " TEXT, " + Details + " TEXT, "
            + IsActive + " TEXT, " + Extra1 + " TEXT, "
            + Extra2 + " TEXT, " + Notes1 + " TEXT, "
            + Notes2 + " TEXT )";


    public static final String CREATE_SCHEDULE = "CREATE TABLE "
            + DBInfo.TBlRateSchedule
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + COOPID + " TEXT, " + RateSegment
            + " TEXT , " + RateComponent + " TEXT , " + PrintOrder
            + " TEXT , " + Classification + " TEXT ," + RateSched
            + " TEXT , " + RateSchedType + " TEXT, " + Amount
            + " TEXT," + VATRate + " TEXT, " + VATAmount
            + " TEXT, " + FranchiseTaxRate + " TEXT, " + FranchiseTaxAmount
            + " TEXT, " + LocalTaxRate + " TEXT , " + LocalTaxAmount
            + " TEXT, " + TotalAmount + " TEXT, " + IsVAT
            + " TEXT, " + IsDVAT + " TEXT, " + IsOverUnder
            + " TEXT, " + IsFranchiseTax + " TEXT ," + IsLocalTax
            + " TEXT, " + IsLifeLine + " TEXT , " + IsSCDiscount
            + " TEXT, " + RateStatus + " TEXT , " + DateAdded
            + " TEXT, " + IsExport
            + " TEXT," + Extra1 + " TEXT, " + Extra2
            + " TEXT, " + Notes1 + " TEXT, " + Notes2
            + " TEXT)";

    public static final String CREATE_SEGMENT = "CREATE TABLE "
            + DBInfo.TBLRateSegment
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + COOPID + " TEXT, " + RateSegmentCode
            + " TEXT , " + RateSegmentName + " TEXT, " + Details
            + " TEXT, " + IsActive + " TEXT, " + Extra1
            + " TEXT, " + Extra2 + " TEXT , " + Notes1
            + " TEXT ," + Notes2 + " TEXT)";

    public static final String CREATE_POLICY = "CREATE TABLE " + DBInfo.TBLPolicy
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + PolicyCode
            + " TEXT ," + PolicyType + " TEXT," + CustomerClass
            + " TEXT," + MinkWh + " TEXT, " + MaxkWh
            + " TEXT, " + PercentAmount + " TEXT, " + Extra1
            + " TEXT, " + Extra2 + " TEXT)";

    public static final String CREATE_UTILITY = "CREATE TABLE " + DBInfo.TBLUtility + "("
            + COOPID + " TEXT , " + CoopName + " TEXT, "
            + CoopType + " TEXT," + Classification + " TEXT, "
            + ReadingToDueDate + " TEXT,"
            + Acronym + " TEXT, " + BillingCode + " TEXT, "
            + BusinessAddress + " TEXT," + TelNo + " TEXT,"
            + TinNo + " TEXT, " + Extra1 + " TEXT, "
            + Extra2 + " TEXT)";

    public static final String CREATE_FOUNDMETERS = "CREATE TABLE " + TBlFound_Meters + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + AccountID + " TEXT, "
            + MeterSerialNo + " TEXT, " + Reading + " TEXT," + DateRead + " TEXT, "
            + Latitude + " TEXT, "+ Longitude + " TEXT," + Remarks + " TEXT, "
            + Extra1 + " TEXT, " + Extra2 + " TEXT)";


    public static final String CREATE_ACCOUNTS = "CREATE TABLE " + TBLACCOUNTINFO + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DateSync + " TEXT, " + DateRead + " TEXT, " + LastReadingDate + " TEXT,"
            + DateUploaded + " TEXT," + COOPID + " TEXT,"
            + FirstName + " TEXT, " + MiddleName + " TEXT,"
            + LastName + " TEXT, " + TownCode + " TEXT,"
            + RouteNo + " TEXT," + AccountID + " TEXT,"
            + AccountType + " TEXT," + AccountClassification + " TEXT,"
            + SubClassification + " TEXT," + SequenceNo + " TEXT," + IsNetMetering + " TEXT,"
            + IsCheckSubMeterType + " TEXT," + CheckMeterAccountNo + " TEXT,"
            + CheckMeterName + " TEXT," + ReadStatus + " TEXT," + EditCount + " TEXT,"
            + PrintCount + " TEXT," + DueDate + " TEXT,"
            + DisoDate + " TEXT," + AccountStatus + " TEXT,"
            + ReadingDetails + " TEXT," + MeterSerialNo + " TEXT,"
            + UploadStatus + " TEXT," + PoleRental + " TEXT,"
            + SpaceRental + " TEXT," + PilferagePenalty + " TEXT,"
            + UnderOverRecovery + " TEXT," + Averaging + " TEXT," + Coreloss + " TEXT," +  Arrears + " TEXT,"
            + ExportBill + " TEXT," + ExportDateCounter + " TEXT," + kWhReading + " TEXT,"
            + Extra1 + " TEXT," + Extra2 + " TEXT,"
            + Notes1 + " TEXT," + Notes2 + " TEXT)";

    public static final String CREATE_THRESHOLD = " CREATE TABLE " + TBLThreshold
            + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + SettingsCode + " TEXT,"
            + ThresholdPercentage + " TEXT)";
}

