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
    private String DownloadRef;
    private String SequenceNoFrom;
    private String SequenceNoTo;
    private int IsDownload;
    private int primaryKey;



    public Route(int primaryKey,String districtID, String routeID, String accountIDTo, String accountIDFrom, String dueDate,String tagClass,String coopID,String readerID,String readerName,String downloadRef,String sequenceNoFrom,String sequenceNoTo,int isDownload) {
        super(coopID,readerID,readerName);
        DistrictID = districtID;
        RouteID = routeID;
        AccountIDTo = accountIDTo;
        AccountIDFrom = accountIDFrom;
        DueDate = dueDate;
        TagClass = tagClass;
        DownloadRef = downloadRef;
        SequenceNoFrom = sequenceNoFrom;
        SequenceNoTo = sequenceNoTo;
        IsDownload = isDownload;
        this.primaryKey = primaryKey;
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

    public String getDownloadRef() {
        return DownloadRef;
    }

    public String getSequenceNoFrom() {
        return SequenceNoFrom;
    }

    public String getSequenceNoTo() {
        return SequenceNoTo;
    }

    public int getIsDownload() {
        return IsDownload;
    }

    public void setIsDownload(int isDownload) {
        IsDownload = isDownload;
    }

    public void setPrimaryKey(int primaryKey) {
        this.primaryKey = primaryKey;
    }

    public int getPrimaryKey() {
        return primaryKey;
    }
}
