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


    public Route(String districtID, String routeID, String accountIDTo, String accountIDFrom, String dueDate) {
        DistrictID = districtID;
        RouteID = routeID;
        AccountIDTo = accountIDTo;
        AccountIDFrom = accountIDFrom;
        DueDate = dueDate;
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
}
