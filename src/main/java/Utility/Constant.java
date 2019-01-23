package Utility;

public class Constant {
    public static String MYPREFERENCES = "MYPREFERENCES";
    public static String RATE_SCHEDULE_COUNT_NON_HIGHERVOLT = "rate_schedule_non_higher";
    public static String RATE_SCHEDULE_COUNT_HIGHERVOLT = "rate_schedule_higher_volt";
    public static String COOP_DETAILS_COUNT = "coop_details_count";
    public static String RATE_COMPONENT_COUNT = "rate_component_count";
    public static String RATE_SEGMENT_COUNT = "rate_segment";
    public static String BILLING_POLICY_NONHIGHVOLT_COUNT = "billing_policy_count";
    public static String BILLING_POLICY_HIGHVOLT_COUNT = "billing_policy_highvolt_count";
    public static String LIFELINE_POLICY_COUNT = "lifeline_policy_count";
    public static String RATE_CODE_COUNT = "rate_code";
    public static String THRESHOLD_COUNT = "threshold";
    public static String NET_METERING_POLICY = "net_metering_policy";
    public static String NET_METERING_RATE_SCHEDULE = "net_metering_rate";
    public static String TAGCLASS = "TAGCLASS";


    /**Production*/
    public static String PORT = "8080/teslaclient/noreco_api/billing_api.asp";

    /**Development
     * 8080
     * */
    //public static String PORT = "8080/noreco_api/billing_api.asp";


    public static String DISCONNECTIONNOTICE = "Pursuant to NORECO II Policy on Delinquent Accounts (FIN 11-6), we are serving you this 48 - Hour NOTICE OF DISCONNECTION due to your unpaid electric bills. " +
            " \n \n In connection hereto, please pay the aforementioned bills \n within forty eight (48) hours from receipt hereof,otherwise, we will be constrained to disconnect your electric service \n without further notice." +
            " \n \n Please disregard this Notice of Disconnection if you have \n already paid.";

    public static String DISCONNECTIONNOTICE_BIX = "Pursuant to NORECO II Policy on Delinquent \nAccounts (FIN 11-6), we are serving you this 48 - Hour NOTICE OF DISCONNECTION due to your \nunpaid electric bills."+
            "\n \nIn connection hereto, please pay the \naforementioned bills within forty eight (48) \nhours from receipt hereof,otherwise, we will be constrained to disconnect your electric service without further notice." +
            "\n \nPlease disregard this Notice of Disconnection if you have already paid.";

    public static String OFFICIALRECIEPT = "This is not an Official Receipt. Payment of this bill does not   mean payment of previous delinquencies if any.";
    public static String OFFICIALRECIEPT_BIX = "This is not an Official Receipt. Payment of this bill does not mean payment of previous \n delinquencies if any.";

    public static String WARNING = "WARNING!SUBJECT TO DISCONNECTION";

    public static String FOOTERMESSAGE = "NOTE: Please pay in the office 2 days after \nreceipt of this Statement of Account or within 7 days from Due Date to avoid Penalty.";

}
