package Model;

public class Thresholds {
    private String SettingsCode;
    private String ThresholdPercentage;

    public Thresholds(String settingsCode, String thresholdPercentage) {
        SettingsCode = settingsCode;
        ThresholdPercentage = thresholdPercentage;
    }

    public String getSettingsCode() {
        return SettingsCode;
    }

    public String getThresholdPercentage() {
        return ThresholdPercentage;
    }
}
