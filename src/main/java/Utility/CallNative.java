package Utility;

public class CallNative {

    public static CallNative instance;

    public CallNative(){

    }

    public static CallNative getInstance() {
        if(instance == null) {
            instance = new CallNative();
        }

        return instance;
    }

    public native String getNative1();

    public native String getNative2();

    public native String getNative3();

    public native String getNative4();

    static {
        System.loadLibrary("native-lib");
    }
}
