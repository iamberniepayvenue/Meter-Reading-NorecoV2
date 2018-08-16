package Model;

public class Components {
    private String rateComponent;
    private String details;

    public Components(String rateComponent, String details) {
        this.rateComponent = rateComponent;
        this.details = details;
    }

    public String getRateComponent() {
        return rateComponent;
    }

    public String getDetails() {
        return details;
    }
}
