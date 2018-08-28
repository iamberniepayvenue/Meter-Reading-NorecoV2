package Model;

/**
 * Created by andrewlaurienrsocia on 23/08/2017.
 */

public class Account extends Person {


    private String DateRead;
    private String DateUploaded;
    private String DateSync;
    private String PowerUtilityID;
    private String TownCode;
    private String RouteNo;
    private String AccountID;
    private String MeterSerialNo;
    private String AccountType;
    private String AccountClassification;
    private String SubClassification;
    private String RateSched;
    private String SequenceNo;
    private String ReadStatus;
    private String EditCount;
    private String PrintCount;
    private String InitialReading;
    private String Reading;
    private String DueDate;
    private String DisoDate;
    private String AccountStatus;

    private String SeniorCitizenID;
    private String SeniorCitizenStatus;
    private String SCExpiryDate;
    private String IsPrepaid;
    private String Disconnection;
    private String Penalty;
    private String Multiplier;
    private String DemandKWMinimum;
    private String DemandKWMaximum;
    private String AdvancePayment;
    private String BillDeposit;
    private String LastReadingDate;
    private String PrevBilling;
    private String Latitude;
    private String Longitude;
    private String ConnectionStatus;
    private String MeterBrand;
    private String IsNetMetering;
    private String PoleRental;
    private String SpaceRental;
    private String PilferagePenalty;
    private String UnderOverRecovery;
    private String PrevFinalReading;
    private String IsChangeMeter;
    private String DemandKW;
    private String PrevReading;
    private String Remarks;

    private String ReadeID;
    private String ReaderName;
    private String Details;
    private String UploadStatus = "0";
    private String Consume = "0";
    private String PrevSerialNo;
    private Bill mBill;
    private String BillMonth;
    private String TotalSCDiscount;
    private String TotalLifeLineDiscount;
    private Object Averaging;
    private String OverUnderDiscount;




    public String getDateRead() {
        return DateRead;
    }

    public void setDateRead(String dateRead) {
        DateRead = dateRead;
    }

    public String getDateUploaded() {
        return DateUploaded;
    }

    public void setDateUploaded(String dateUploaded) {
        DateUploaded = dateUploaded;
    }

    public String getDateSync() {
        return DateSync;
    }

    public void setDateSync(String dateSync) {
        DateSync = dateSync;
    }

    public String getCoopID() {
        return PowerUtilityID;
    }

    public void setCoopID(String coopID) {
        this.PowerUtilityID = coopID;
    }

    public String getTownCode() {
        return TownCode;
    }

    public void setTownCode(String townCode) {
        TownCode = townCode;
    }

    public String getRouteNo() {
        return RouteNo;
    }

    public void setRouteNo(String routeNo) {
        RouteNo = routeNo;
    }

    public String getAccountID() {
        return AccountID;
    }

    public void setAccountID(String accountID) {
        AccountID = accountID;
    }

    public String getPrevBilling() {
        return PrevBilling;
    }

    public void setPrevBilling(String prevBilling) {
        PrevBilling = prevBilling;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getConnectionStatus() {
        return ConnectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        ConnectionStatus = connectionStatus;
    }

    public String getMeterBrand() {
        return MeterBrand;
    }

    public void setMeterBrand(String meterBrand) {
        MeterBrand = meterBrand;
    }

    public String getIsNetMetering() {
        return IsNetMetering;
    }

    public void setIsNetMetering(String isNetMetering) {
        IsNetMetering = isNetMetering;
    }

    public String getPoleRental() {
        return PoleRental;
    }

    public void setPoleRental(String poleRental) {
        PoleRental = poleRental;
    }

    public String getSpaceRental() {
        return SpaceRental;
    }

    public void setSpaceRental(String spaceRental) {
        SpaceRental = spaceRental;
    }

    public String getPilferagePenalty() {
        return PilferagePenalty;
    }

    public void setPilferagePenalty(String pilferagePenalty) {
        PilferagePenalty = pilferagePenalty;
    }

    public String getUnderOverRecovery() {
        return UnderOverRecovery;
    }

    public void setUnderOverRecovery(String underOverRecovery) {
        UnderOverRecovery = underOverRecovery;
    }

    public String getReadeID() {
        return ReadeID;
    }

    public void setReadeID(String readeID) {
        ReadeID = readeID;
    }

    public String getReaderName() {
        return ReaderName;
    }

    public void setReaderName(String readerName) {
        ReaderName = readerName;
    }

    public String getDetails() {
        return Details;
    }

    public void setDetails(String details) {
        Details = details;
    }

    public String getMeterSerialNo() {
        return MeterSerialNo;
    }

    public void setMeterSerialNo(String meterSerialNo) {
        MeterSerialNo = meterSerialNo;
    }

    public String getAccountType() {
        return AccountType;
    }

    public void setAccountType(String accountType) {
        AccountType = accountType;
    }

    public String getAccountClassification() {
        return AccountClassification;
    }

    public void setAccountClassification(String accountClassification) {
        AccountClassification = accountClassification;
    }

    public String getSubClassification() {
        return SubClassification;
    }

    public void setSubClassification(String subClassification) {
        SubClassification = subClassification;
    }

    public String getRateSched() {
        return RateSched;
    }

    public void setRateSched(String rateSched) {
        RateSched = rateSched;
    }

    public String getSequenceNo() {
        return SequenceNo;
    }

    public void setSequenceNo(String sequenceNumber) {
        SequenceNo = sequenceNumber;
    }

    public String getReadStatus() {
        return ReadStatus;
    }

    public void setReadStatus(String readStatus) {

        if (readStatus.isEmpty())
            this.ReadStatus = "Unread";
        else
            ReadStatus = readStatus;
    }

    public String getEditCount() {
        return EditCount;
    }

    public void setEditCount(String editCount) {
        EditCount = editCount;
    }

    public String getPrintCount() {
        return PrintCount;
    }

    public void setPrintCount(String printCount) {
        PrintCount = printCount;
    }

    public String getInitialReading() {
        return InitialReading;
    }

    public void setInitialReading(String initialReading) {
        InitialReading = initialReading;
    }

    public String getReading() {
        return Reading;
    }

    public void setReading(String reading) {
        Reading = reading;
    }

    public String getDueDate() {
        return DueDate;
    }

    public void setDueDate(String dueDate) {
        DueDate = dueDate;
    }

    public String getDisoDate() {
        return DisoDate;
    }

    public void setDisoDate(String disoDate) {
        DisoDate = disoDate;
    }

    public String getAccountStatus() {
        return AccountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        AccountStatus = accountStatus;
    }


    public String getSeniorCitizenID() {
        return SeniorCitizenID;
    }

    public void setSeniorCitizenID(String seniorCitizenID) {
        SeniorCitizenID = seniorCitizenID;
    }

    public String getSeniorCitizenStatus() {
        return SeniorCitizenStatus;
    }

    public void setSeniorCitizenStatus(String seniorCitizenStatus) {
        SeniorCitizenStatus = seniorCitizenStatus;
    }

    public String getSCExpiryDate() {
        return SCExpiryDate;
    }

    public void setSCExpiryDate(String SCExpiryDate) {
        this.SCExpiryDate = SCExpiryDate;
    }

    public String getIsPrepaid() {
        return IsPrepaid;
    }

    public void setIsPrepaid(String isPrepaid) {
        IsPrepaid = isPrepaid;
    }

    public String getDisconnection() {
        return Disconnection;
    }

    public void setDisconnection(String disconnection) {
        Disconnection = disconnection;
    }

    public String getPenalty() {
        return Penalty;
    }

    public void setPenalty(String penalty) {
        Penalty = penalty;
    }

    public String getMultiplier() {
        return Multiplier;
    }

    public void setMultiplier(String multiplier) {
        Multiplier = multiplier;
    }

    public String getDemandKWMinimum() {
        return DemandKWMinimum;
    }

    public void setDemandKWMinimum(String demandKWMinimum) {
        DemandKWMinimum = demandKWMinimum;
    }

    public String getDemandKWMaximum() {
        return DemandKWMaximum;
    }

    public void setDemandKWMaximum(String demandKWMaximum) {
        DemandKWMaximum = demandKWMaximum;
    }

    public String getAdvancePayment() {
        return AdvancePayment;
    }

    public void setAdvancePayment(String advancePayment) {
        AdvancePayment = advancePayment;
    }

    public String getLastReadingDate() {
        return LastReadingDate;
    }

    public void setLastReadingDate(String lastReadingDate) {
        LastReadingDate = lastReadingDate;
    }


    public String getUploadStatus() {
        return UploadStatus;
    }

    public void setUploadStatus(String uploadStatus) {

        if (uploadStatus.isEmpty()) {
            this.UploadStatus = "0";
        }

        this.UploadStatus = uploadStatus;
    }

    public String getConsume() {
        return Consume;
    }

    public void setConsume(String consume) {

        if (consume.isEmpty())
            consume = "0";
        else
            this.Consume = consume;
    }


    public String getPrevSerialNo() {
        return PrevSerialNo;
    }

    public void setPrevSerialNo(String prevSerialNo) {
        PrevSerialNo = prevSerialNo;
    }

    public String getPrevFinalReading() {
        return PrevFinalReading;
    }

    public void setPrevFinalReading(String prevFinalReading) {
        PrevFinalReading = prevFinalReading;
    }

    public String getIsChangeMeter() {
        return IsChangeMeter;
    }

    public void setIsChangeMeter(String isChangeMeter) {
        IsChangeMeter = isChangeMeter;
    }

    public String getDemandKW() {
        return DemandKW;
    }

    public void setDemandKW(String demandKW) {
        DemandKW = demandKW;
    }

    public String getPrevReading() {
        return PrevReading;
    }

    public void setPrevReading(String prevReading) {
        PrevReading = prevReading;
    }


    public String getRemarks() {
        return Remarks;
    }

    public void setRemarks(String remarks) {
        Remarks = remarks;
    }


    public Bill getBill() {
        return mBill;
    }

    public void setBill(Bill myBill) {
        this.mBill = myBill;
    }

    public String getBillMonth() {
        return BillMonth;
    }

    public void setBillMonth(String billMonth) {
        BillMonth = billMonth;
    }

    public String getPowerUtilityID() {
        return PowerUtilityID;
    }

    public String getBillDeposit() {
        return BillDeposit;
    }

    public void setPowerUtilityID(String powerUtilityID) {
        PowerUtilityID = powerUtilityID;
    }

    public void setBillDeposit(String billDeposit) {
        BillDeposit = billDeposit;
    }

    public String getTotalSCDiscount() {
        return TotalSCDiscount;
    }

    public String getTotalLifeLineDiscount() {
        return TotalLifeLineDiscount;
    }

    public void setTotalSCDiscount(String totalSCDiscount) {
        TotalSCDiscount = totalSCDiscount;
    }

    public void setTotalLifeLineDiscount(String totalLifeLineDiscount) {
        TotalLifeLineDiscount = totalLifeLineDiscount;
    }

    public String getAveraging() {
        return Averaging.toString();
    }

    public void setAveraging(Object averaging) {
        Averaging = averaging;
    }

    public String getOverUnderDiscount() {
        return OverUnderDiscount;
    }

    public void setOverUnderDiscount(String overUnderDiscount) {
        OverUnderDiscount = overUnderDiscount;
    }
}
