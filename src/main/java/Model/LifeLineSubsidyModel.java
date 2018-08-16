package Model;

public class LifeLineSubsidyModel {
    String LifelineConsumption;
    String LifelinePercentage;
    String LifelineInDecimal;

    public LifeLineSubsidyModel(String lifelineConsumption, String lifelinePercentage, String lifelineInDecimal) {
        LifelineConsumption = lifelineConsumption;
        LifelinePercentage = lifelinePercentage;
        LifelineInDecimal = lifelineInDecimal;
    }

    public String getLifelineConsumption() {
        return LifelineConsumption;
    }

    public String getLifelinePercentage() {
        return LifelinePercentage;
    }

    public String getLifelineInDecimal() {
        return LifelineInDecimal;
    }
}
