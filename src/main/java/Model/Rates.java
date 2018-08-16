package Model;

public class Rates extends RateSchedule {

    private String Code;
    private String CodeName;
    private float RateAmount;
    private float Amount;
    private float VatAmount;
    private float FranchiseTax;
    private float LocatlTax;


    public Rates() {

    }

    public Rates(String segment, String code, String codeName, float rateAmount,String isLifeLine,String isSCDiscount, float amount, float vatAmount, float franchiseTax, float locatlTax) {

        super(segment,isLifeLine,isSCDiscount);

        Code = code;
        CodeName = codeName;
        RateAmount = rateAmount;
        Amount = amount;
        VatAmount = vatAmount;
        FranchiseTax = franchiseTax;
        LocatlTax = locatlTax;
    }


    public String getCode() {
        return Code;
    }

    public String getCodeName() {
        return CodeName;
    }

    public float getRateAmount() {
        return RateAmount;
    }

    public float getAmount() {
        return Amount;
    }

    public float getVatAmount() {
        return VatAmount;
    }

    public float getFranchiseTax() {
        return FranchiseTax;
    }

    public float getLocatlTax() {
        return LocatlTax;
    }

    public void setIsSeniorCitizen(String isSeniorCitizen) {

    }
}
