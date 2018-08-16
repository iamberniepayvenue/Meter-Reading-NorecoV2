package Model;

/**
 * Created by andrewlaurienrsocia on 19/04/2018.
 */

public class Reader {

    private String CoopID;
    private String ReaderID;
    private String ReaderName;

    public Reader() {
    }

    public Reader(String coopID, String readerID, String readerName) {
        CoopID = coopID;
        ReaderID = readerID;
        ReaderName = readerName;
    }

    public String getCoopID() {
        return CoopID;
    }

    public String getReaderID() {
        return ReaderID;
    }

    public String getReaderName() {
        return ReaderName;
    }
}
