package Model;

public class Rates extends RateSchedule {

    private String Code;
    private String CodeName;
    private String RateAmount;
    private String Amount;
//    private float VatAmount;
//    private float FranchiseTax;
//    private float LocatlTax;
    private double AmountDueExport;



    public Rates() {

    }

    /**this part included vat amount*/
//    public Rates(String segment, String code, String codeName, String rateAmount,String isLifeLine,String isSCDiscount,String isExport,float amount, float vatAmount, float franchiseTax, float locatlTax,float amountDueExport) {
//
//        super(segment,isLifeLine,isSCDiscount,isExport);
//
//        Code = code;
//        CodeName = codeName;
//        RateAmount = rateAmount;
//        Amount = amount;
//        VatAmount = vatAmount;
//        FranchiseTax = franchiseTax;
//        LocatlTax = locatlTax;
//        AmountDueExport = amountDueExport;
//    }

    public Rates(String segment, String code, String codeName, String rateAmount,String isLifeLine,String isSCDiscount,String isExport,String amount,double amountDueExport){
        super(segment,isLifeLine,isSCDiscount,isExport);

        Code = code;
        CodeName = codeName;
        RateAmount = rateAmount;
        Amount = amount;
        AmountDueExport = amountDueExport;
    }


    public String getCode() {
        return Code;
    }

    public String getCodeName() {
        return CodeName;
    }

    public String getRateAmount() {
        return RateAmount;
    }

    public String getAmount() {
        return Amount;
    }

//    public float getVatAmount() {
//        return VatAmount;
//    }
//
//    public float getFranchiseTax() {
//        return FranchiseTax;
//    }
//
//    public float getLocatlTax() {
//        return LocatlTax;
//    }
//
//    public void setIsSeniorCitizen(String isSeniorCitizen) {
//
//    }

    public double getAmountDueExport() {
        return AmountDueExport;
    }
}
