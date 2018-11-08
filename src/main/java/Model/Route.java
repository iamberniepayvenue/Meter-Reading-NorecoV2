package Model;

/**
 * Created by andrewlaurienrsocia on 19/04/2018.
 */

public class Route extends Reader {

    private String DistrictID;
    private String RouteID;
    private String AccountIDTo;
    private String AccountIDFrom;
    private String DueDate;
    private String TagClass;



    public Route(String districtID, String routeID, String accountIDTo, String accountIDFrom, String dueDate,String tagClass,String coopID,String readerID,String readerName) {
        super(coopID,readerID,readerName);
        DistrictID = districtID;
        RouteID = routeID;
        AccountIDTo = accountIDTo;
        AccountIDFrom = accountIDFrom;
        DueDate = dueDate;
        TagClass = tagClass;
    }


    public String getDistrictID() {
        return DistrictID;
    }

    public String getRouteID() {
        return RouteID;
    }

    public String getAccountIDTo() {
        return AccountIDTo;
    }

    public String getAccountIDFrom() {
        return AccountIDFrom;
    }

    public String getDueDate() {
        return DueDate;
    }

    public String getTagClass() {
        return TagClass;
    }


}
