package Model;

public class ReadingDetailsModel {

     String BillMonth;
     String PrevBilling;
     String Address;
     String SeniorCitizenStatus;
     String SCExpiryDate;
     String Penalty;
     String RateSched;
     String Multiplier;
     String DemandKW;
     String AdvancePayment;
     String BillDeposit;
     String LastReadingDate;
     String InitialReading;
     String PrevReading;
     String PrevFinalReading;
     String IsChangeMeter;
     String MeterBrand;
     String Consume;
     String Reading;
     String Remarks;
     String Latitude;
     String Longitude;
     String TotalLifeLineDiscount;
     String TotalSCDiscount;
     Bill mBill;
     String Averaging;
     String OverUnderDiscount;
     String IsStopMeter;


    public ReadingDetailsModel(String billMonth, String prevBilling, String address, String seniorCitizenStatus, String SCExpiryDate,
                               String penalty, String rateSched, String multiplier, String demandKW, String advancePayment, String billDeposit,
                               String lastReadingDate, String initialReading, String prevReading, String prevFinalReading, String isChangeMeter,
                               String meterBrand, String consume, String reading, String remarks, String latitude,
                               String longitude, String totalLifeLineDiscount, String totalSCDiscount, Bill mBill,String overUnderDiscount,String isStopMeter) { //,String Averaging
        BillMonth = billMonth;
        PrevBilling = prevBilling;
        Address = address;
        SeniorCitizenStatus = seniorCitizenStatus;
        this.SCExpiryDate = SCExpiryDate;
        Penalty = penalty;
        RateSched = rateSched;
        Multiplier = multiplier;
        DemandKW = demandKW;
        AdvancePayment = advancePayment;
        BillDeposit = billDeposit;
        LastReadingDate = lastReadingDate;
        InitialReading = initialReading;
        PrevReading = prevReading;
        PrevFinalReading = prevFinalReading;
        IsChangeMeter = isChangeMeter;
        MeterBrand = meterBrand;
        Consume = consume;
        Reading = reading;
        Remarks = remarks;
        Latitude = latitude;
        Longitude = longitude;
        TotalLifeLineDiscount = totalLifeLineDiscount;
        TotalSCDiscount = totalSCDiscount;
        this.mBill = mBill;
        OverUnderDiscount = overUnderDiscount;
        //this.Averaging = Averaging;
        IsStopMeter = isStopMeter;
    }
}
