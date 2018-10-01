package Model;

public class Policies {
   private String  PolicyCode,PolicyType,CustomerClass,MinkWh,MaxkWh,PercentAmount;

    public Policies(String policyCode, String policyType, String customerClass, String minkWh, String maxkWh, String percentAmount) {
        PolicyCode = policyCode;
        PolicyType = policyType;
        CustomerClass = customerClass;
        MinkWh = minkWh;
        MaxkWh = maxkWh;
        PercentAmount = percentAmount;
    }

    public String getPolicyCode() {
        return PolicyCode;
    }

    public String getPolicyType() {
        return PolicyType;
    }

    public String getCustomerClass() {
        return CustomerClass;
    }

    public String getMinkWh() {
        return MinkWh;
    }

    public String getMaxkWh() {
        return MaxkWh;
    }

    public String getPercentAmount() {
        return PercentAmount;
    }
}
