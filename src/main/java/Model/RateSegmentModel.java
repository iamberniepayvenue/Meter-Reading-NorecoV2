package Model;

public class RateSegmentModel {
    String rateSegmentCode;
    String rateSegmentName;
    String segmentDetails;

    public RateSegmentModel(String rateSegmentCode, String rateSegmentName, String segmentDetails) {
        this.rateSegmentCode = rateSegmentCode;
        this.rateSegmentName = rateSegmentName;
        this.segmentDetails = segmentDetails;
    }

    public String getRateSegmentCode() {
        return rateSegmentCode;
    }

    public String getRateSegmentName() {
        return rateSegmentName;
    }

    public String getSegmentDetails() {
        return segmentDetails;
    }
}
