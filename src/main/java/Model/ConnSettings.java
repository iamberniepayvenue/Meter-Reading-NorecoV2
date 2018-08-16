package Model;

public class ConnSettings {

    private String CoopID;
    private String Host;
    private String Port;

    public ConnSettings(String coopid, String host, String port) {
        CoopID = coopid;
        Host = host;
        Port = port;
    }

    public String getCoopID() {
        return CoopID;
    }

    public String getHost() {
        return Host;
    }

    public String getPort() {
        return Port;
    }
}
