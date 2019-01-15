package Model;

public class Components {
    private String amount;
    private String component;

    public Components(String amount, String component) {
        this.amount = amount;
        this.component = component;
    }

    public String getRateComponent() {
        return amount;
    }

    public String getDetails() {
        return component;
    }
}
