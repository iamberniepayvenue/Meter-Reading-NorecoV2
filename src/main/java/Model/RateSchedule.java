package Model;

public class RateSchedule {

    private String RateSegment;
    private String RateCode;
    private String RateComponent;
    private String RateType;
    private String IsVatable;
    private String IsDVAT;
    private String IsFranchiseTaxable;
    private String IsLocalTaxable;
    private float ComponentVatRate;
    private float ComponentFTaxRate;
    private float ComponentLTaxRate;
    private String IsSCDiscount;
    private String IsLifeline;
    private float componentRate;
    private String IsOverUnder;
    private String IsExport;

    public RateSchedule() {

    }

    public RateSchedule(String rateSegment,String isSCDiscount,String isLifeline,String isExport) {
        RateSegment = rateSegment;
        IsLifeline = isLifeline;
        IsSCDiscount = isSCDiscount;
        IsExport = isExport;
    }

    public RateSchedule(String rateSegment, String rateCode, String rateComponent, String rateType,
                        String isVatable, String isDVAT, String isFranchiseTaxable,
                        String isLocalTaxable, float componentVatRate,
                        float componentFTaxRate, float componentLTaxRate,
                        String isSCDiscount, String isLifeline, float componentRate,String isOverUnder,String isExport) {

        RateSegment = rateSegment;
        RateCode = rateCode;
        RateComponent = rateComponent;
        RateType = rateType;
        IsVatable = isVatable;
        IsDVAT = isDVAT;
        IsFranchiseTaxable = isFranchiseTaxable;
        IsLocalTaxable = isLocalTaxable;
        ComponentVatRate = componentVatRate;
        ComponentFTaxRate = componentFTaxRate;
        ComponentLTaxRate = componentLTaxRate;
        IsSCDiscount = isSCDiscount;
        IsLifeline = isLifeline;
        this.componentRate = componentRate;
        IsOverUnder = isOverUnder;
        IsExport = isExport;
    }

    public String getRateSegment() {
        return RateSegment;
    }

    public String getRateCode() {
        return RateCode;
    }

    public String getRateComponent() {
        return RateComponent;
    }

    public String getRateType() {
        return RateType;
    }

    public String getIsVatable() {
        return IsVatable;
    }

    public String getIsDVAT() {
        return IsDVAT;
    }

    public String getIsFranchiseTaxable() {
        return IsFranchiseTaxable;
    }

    public String getIsLocalTaxable() {
        return IsLocalTaxable;
    }

    public float getComponentVatRate() {
        return ComponentVatRate;
    }

    public float getComponentFTaxRate() {
        return ComponentFTaxRate;
    }

    public float getComponentLTaxRate() {
        return ComponentLTaxRate;
    }

    public String getIsSCDiscount() {
        return IsSCDiscount;
    }

    public float getComponentRate() {
        return componentRate;
    }

    public String getIsLifeline() {
        return IsLifeline;
    }

    public String getIsOverUnder() {
        return IsOverUnder;
    }

    public String getIsExport() {
        return IsExport;
    }
}
